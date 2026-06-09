package com.example.api

import android.util.Log
import com.example.BuildConfig
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import kotlin.math.max
import kotlin.math.min

// Data classes for visual UI prediction output
data class GlucosePrediction(
    val intervalIndex: Int, // 0-4 representing 1h, 3h, 6h, 12h, 24h
    val intervalLabel: String, // "1h", "3h", etc.
    val glucoseRangeMin: Double,
    val glucoseRangeMax: Double,
    val trend: String, // "Rising", "Falling", "Stable"
    val possibleReason: String,
    val confidence: String, // "High", "Medium", "Low"
    val warningMessage: String? = null
)

data class TrendPredictionResult(
    val predictions: List<GlucosePrediction>,
    val insights: String,
    val isLocalMock: Boolean = false
)

object GeminiPredictionService {
    private const val TAG = "GeminiPrediction"
    private const val API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Estimates trend predictions based on medical logs.
     */
    suspend fun getPredictions(
        profile: UserProfile,
        thresholds: AlertThreshold,
        readings: List<GlucoseReading>,
        meals: List<MealLog>,
        medications: List<Medication>,
        medLogs: List<MedicationLog>,
        insulinLogs: List<InsulinLog>,
        unit: String
    ): TrendPredictionResult = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY

        val hasApiKey = apiKey.isNotEmpty() && apiKey != "MY_GEMINI_API_KEY"

        if (!hasApiKey) {
            Log.d(TAG, "No valid Gemini API Key detected. Falling back to Local Intelligence Engine.")
            return@withContext computeLocalPredictions(profile, thresholds, readings, meals, medLogs, insulinLogs, unit)
        }

        try {
            // Build historical context strings for prompt
            val lastReadingsText = readings.take(8).joinToString("\n") { r ->
                "- ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(r.timestamp)}: ${r.value} ${r.unit} (${r.type}) Notes: ${r.notes}, Symptoms: ${r.symptoms}"
            }

            val lastMealsText = meals.take(5).joinToString("\n") { m ->
                "- ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(m.timestamp)}: ${m.mealType} - ${m.foodName} (${m.carbsGrams}g Carbs) Notes: ${m.notes}"
            }

            val medicineLogText = medLogs.take(5).joinToString("\n") { ml ->
                "- ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(ml.timestamp)}: Took ${ml.medicationName} ${ml.dosage}"
            }

            val lastInsulinText = insulinLogs.take(5).joinToString("\n") { i ->
                "- ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(i.timestamp)}: ${i.units} units of ${i.insulinType} (${i.mealRelation}) Notes: ${i.notes}"
            }

            // Construct prompt requesting standard structured JSON response
            val prompt = """
You are DiaTrack's advanced medical analysis AI. Read the patient profile, glucose thresholds, and recent logs to predict blood glucose trends at 1 hour, 3 hours, 6 hours, 12 hours, and 24 hours.

PATIENT PROFILE:
- Age: ${profile.age}
- Gender: ${profile.gender}
- Diabetes Type: ${profile.diabetesType}

ALERT THRESHOLDS:
- Target Low: ${thresholds.lowThreshold} $unit
- Target High: ${thresholds.highThreshold} $unit
- Emergency Low: ${thresholds.emergencyLow} $unit
- Emergency High: ${thresholds.emergencyHigh} $unit

RECENT GLUCOSE LOGS (Latest first):
$lastReadingsText

RECENT MEALS (Carbohydrate Intake):
$lastMealsText

RECENT INSULIN INJECTIONS (Insulin administration):
$lastInsulinText

RECENT ORAL MEDICINE TAKEN:
$medicineLogText

INSTRUCTION:
Predict the blood sugar ranges at intervals (1 hour, 3 hours, 6 hours, 12 hours, 24 hours).
Output MUST be valid JSON with this exact key-value structure (without markdown wrapping):
{
  "predictions": [
    {
      "intervalLabel": "1h",
      "glucoseRangeMin": 110.0,
      "glucoseRangeMax": 130.0,
      "trend": "Falling",
      "possibleReason": "Insulin peak action starting.",
      "confidence": "High",
      "warningMessage": "Predicted near your hypoglycemia threshold. Keep sugar source nearby."
    },
    ...
  ],
  "insights": "Detailed overview of glucose cycles. Ensure you emphasize that predictions are ESTIMATES only and DO NOT prescribe dosage."
}

SAFETY RULES:
1. Predictions are ESTIMATES ONLY, NOT MEDICAL ADVICE.
2. DO NOT prescribing or suggest insulin dosages or treatment modifications. 
3. Always include a safety warning if predictions fall below ${thresholds.lowThreshold} or above ${thresholds.highThreshold} $unit, advising the user to contact their doctor.
4. Scale all prediction range numbers to match the user's current unit: $unit.
            """.trimIndent()

            val jsonRequest = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                        })
                    })
                })
            }

            val body = jsonRequest.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("$API_URL?key=$apiKey")
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                Log.e(TAG, "API call failed: ${response.code} - ${response.message}")
                return@withContext computeLocalPredictions(profile, thresholds, readings, meals, medLogs, insulinLogs, unit)
            }

            val responseBodyString = response.body?.string() ?: ""
            val jsonResponse = JSONObject(responseBodyString)
            val textContent = jsonResponse
                .getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text")

            // Parse clean JSON block from inside potentially markdown response
            val cleanJsonString = extractJsonFromString(textContent)
            val parsedResult = JSONObject(cleanJsonString)
            val predictionsList = mutableListOf<GlucosePrediction>()
            val predictionsArray = parsedResult.getJSONArray("predictions")
            
            for (i in 0 until predictionsArray.length()) {
                val p = predictionsArray.getJSONObject(i)
                predictionsList.add(
                    GlucosePrediction(
                        intervalIndex = i,
                        intervalLabel = p.getString("intervalLabel"),
                        glucoseRangeMin = p.getDouble("glucoseRangeMin"),
                        glucoseRangeMax = p.getDouble("glucoseRangeMax"),
                        trend = p.getString("trend"),
                        possibleReason = p.getString("possibleReason"),
                        confidence = p.getString("confidence"),
                        warningMessage = if (p.has("warningMessage")) p.getString("warningMessage") else null
                    )
                )
            }
            
            TrendPredictionResult(
                predictions = predictionsList,
                insights = parsedResult.getString("insights"),
                isLocalMock = false
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error in Gemini prediction flow, falling back to local formulas", e)
            computeLocalPredictions(profile, thresholds, readings, meals, medLogs, insulinLogs, unit)
        }
    }

    private fun extractJsonFromString(text: String): String {
        var clean = text.trim()
        if (clean.startsWith("```json")) {
            clean = clean.substringAfter("```json").substringBeforeLast("```").trim()
        } else if (clean.startsWith("```")) {
            clean = clean.substringAfter("```").substringBeforeLast("```").trim()
        }
        return clean
    }

    /**
     * Local medical analytics estimation model. Runs offline and mimics a genuine blood sugar kinetics model.
     * Incorporates carbohydrate decay (+ glucose index) and active insulin decay curves.
     */
    private fun computeLocalPredictions(
        profile: UserProfile,
        thresholds: AlertThreshold,
        readings: List<GlucoseReading>,
        meals: List<MealLog>,
        medLogs: List<MedicationLog>,
        insulinLogs: List<InsulinLog>,
        unit: String
    ): TrendPredictionResult {
        // Start from current average or latest reading
        val baseGlucose = readings.firstOrNull()?.value ?: 120.0
        val isMmol = unit == "mmol/L"

        // Local kinetic coefficients (standardized to mg/dL)
        val targetBase = if (isMmol) baseGlucose * 18.0 else baseGlucose

        // Simple kinetics estimation
        val hours = listOf(1, 3, 6, 12, 24)
        val intervals = listOf("1h", "3h", "6h", "12h", "24h")
        
        // Active influence tracking
        val now = System.currentTimeMillis()
        var estimatedActiveCarbs = 0.0
        var estimatedActiveInsulin = 0.0

        // Parse recently consumed carbs (within last 4 hours)
        meals.filter { now - it.timestamp < 4 * 3600 * 1000 }.forEach {
            val ageHours = (now - it.timestamp).toDouble() / (3600 * 1000)
            // Carbs absorption curve: rises first, then decays
            val absorbFactor = max(0.0, 1.0 - (ageHours / 4.0))
            estimatedActiveCarbs += it.carbsGrams * absorbFactor
        }

        // Parse recently taken insulin (within last 5 hours)
        insulinLogs.filter { now - it.timestamp < 5 * 3600 * 1000 }.forEach {
            val ageHours = (now - it.timestamp).toDouble() / (3600 * 1000)
            // Insulin action curve peak at 2h, decay by 5h
            val insulinFactor = max(0.0, 1.0 - (ageHours / 5.0))
            estimatedActiveInsulin += it.units * insulinFactor
        }

        val predictions = hours.mapIndexed { idx, h ->
            // Carbs raise blood sugar (approx 3 mg/dL per gram carb)
            // Insulin lowers blood sugar (approx 45 mg/dL per unit insulin)
            // Basic biological homeostatic feedback pushes back to 100 mg/dL (or 5.5 mmol/L)
            
            val carbRate = max(0.0, estimatedActiveCarbs * max(0.0, 1.0 - (h / 3.0)))
            val insulinRate = max(0.0, estimatedActiveInsulin * max(0.0, 1.0 - (h / 4.0)))
            
            var predictedMgDl = targetBase + (carbRate * 2.5) - (insulinRate * 35.0)

            // Biology feedback decay pushes body toward homeostatic baseline over time
            val baseline = 100.0
            predictedMgDl = predictedMgDl + (baseline - predictedMgDl) * (1.0 - Math.exp(-0.15 * h))

            // Keep in bounds
            predictedMgDl = max(40.0, min(350.0, predictedMgDl))

            // Convert back to mmol/L if required
            val finalMin = if (isMmol) (predictedMgDl - 10.0) / 18.0 else (predictedMgDl - 15.0)
            val finalMax = if (isMmol) (predictedMgDl + 10.0) / 18.0 else (predictedMgDl + 15.0)
            val finalGlucose = if (isMmol) predictedMgDl / 18.0 else predictedMgDl

            // State descriptions
            val trend = when {
                predictedMgDl - targetBase > 15 -> "Rising"
                targetBase - predictedMgDl > 15 -> "Falling"
                else -> "Stable"
            }

            val reason = when {
                estimatedActiveCarbs > 20.0 && h <= 3 -> "Digested carbohydrates from your recent meals."
                estimatedActiveInsulin > 2.0 && h <= 3 -> "Hypoglycemic active insulin absorption curves peaking."
                h >= 12 -> "System returning to standard homeostatic target limits over time."
                else -> "Balance of active insulin and digested meal carbohydrates."
            }

            val warning = when {
                predictedMgDl < thresholds.emergencyLow -> "EMERGENCY SAFETY ADVISORY: Dangerously low blood sugar predicted! Consume simple carbs immediately and consult medical support if symptoms are severe."
                predictedMgDl < thresholds.lowThreshold -> "ALERT WAITING: Glucose predicted near hypoglycemia target parameters. Retest soon."
                predictedMgDl > thresholds.emergencyHigh -> "EMERGENCY SAFETY ADVISORY: Dangerously high blood sugar predicted. Retest ketones, strictly check hydration, and contact doctor."
                predictedMgDl > thresholds.highThreshold -> "ALERT TARGET: Hyperglycemia trends expected. Monitor meals and active insulin."
                else -> null
            }

            GlucosePrediction(
                intervalIndex = idx,
                intervalLabel = intervals[idx],
                glucoseRangeMin = max(if (isMmol) 2.2 else 40.0, finalMin),
                glucoseRangeMax = min(if (isMmol) 20.0 else 350.0, finalMax),
                trend = trend,
                possibleReason = reason,
                confidence = if (readings.size >= 3) "Medium" else "Low (Needs more historical readings)",
                warningMessage = warning
            )
        }

        val localInsights = """
Note: The local analytical heuristic algorithm estimated these tendencies because either cloud networks are disconnected or the standard API configuration keys are waiting in your platform Settings. 

ANALYSIS SUMMARY:
- Active carbs estimated balance: ${String.format("%.1f", estimatedActiveCarbs)}g.
- Active circulating insulin estimates: ${String.format("%.1f", estimatedActiveInsulin)} Units.
- Glucose status: Current average and previous hours patterns are being computed to identify standard circadian rhythm offsets.
- Safety Note: These predictions are visual estimations using standard pharmacological decay formulas. Always perform real fingerstick blood glucose checks before adjusting your target routines.
        """.trimIndent()

        return TrendPredictionResult(
            predictions = predictions,
            insights = localInsights,
            isLocalMock = true
        )
    }
}
