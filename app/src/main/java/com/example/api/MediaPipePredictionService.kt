package com.example.api

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.example.data.*
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import kotlin.math.max
import kotlin.math.min

data class GlucosePrediction(
    val intervalIndex: Int,
    val intervalLabel: String,
    val glucoseRangeMin: Double,
    val glucoseRangeMax: Double,
    val trend: String,
    val possibleReason: String,
    val confidence: String,
    val warningMessage: String? = null
)

data class TrendPredictionResult(
    val predictions: List<GlucosePrediction>,
    val insights: String,
    val isLocalMock: Boolean = false
)

object MediaPipePredictionService {
    private const val TAG = "MediaPipePrediction"
    private const val DEFAULT_MODEL_PATH = "/data/local/tmp/llm/gemma-3-270m-it.task"

    @Volatile
    private var llmInference: LlmInference? = null

    @Volatile
    private var cachedModelPath: String? = null

    private fun resolveModelPath(): String {
        val configured = BuildConfig.LLM_MODEL_PATH.trim()
        return when {
            configured.isNotBlank() -> configured
            else -> DEFAULT_MODEL_PATH
        }
    }

    private fun getEngine(context: Context): LlmInference? {
        val modelPath = resolveModelPath()
        if (modelPath.isBlank()) return null

        val existing = llmInference
        if (existing != null && cachedModelPath == modelPath) return existing

        synchronized(this) {
            val current = llmInference
            if (current != null && cachedModelPath == modelPath) return current

            val options = LlmInference.LlmInferenceOptions.builder()
                .setModelPath(modelPath)
                .setMaxTokens(384)
                .setMaxTopK(40)
                .build()

            val engine = LlmInference.createFromOptions(context.applicationContext, options)
            llmInference = engine
            cachedModelPath = modelPath
            return engine
        }
    }

    suspend fun getPredictions(
        context: Context,
        profile: UserProfile,
        thresholds: AlertThreshold,
        readings: List<GlucoseReading>,
        meals: List<MealLog>,
        medications: List<Medication>,
        medLogs: List<MedicationLog>,
        insulinLogs: List<InsulinLog>,
        unit: String
    ): TrendPredictionResult = withContext(Dispatchers.IO) {
        val engine = runCatching { getEngine(context) }.getOrNull()
        if (engine == null) {
            Log.w(TAG, "MediaPipe LLM not available, using local predictor.")
            return@withContext computeLocalPredictions(profile, thresholds, readings, meals, medLogs, insulinLogs, unit)
        }

        try {
            val lastReadingsText = readings.take(8).joinToString("\n") { r ->
                "- ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(r.timestamp)}: ${r.value} ${r.unit} (${r.type}) Notes: ${r.notes}, Symptoms: ${r.symptoms}"
            }
            val lastMealsText = meals.take(5).joinToString("\n") { m ->
                "- ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(m.timestamp)}: ${m.mealType} - ${m.foodName} (${m.carbsGrams}g Carbs) Notes: ${m.notes}"
            }
            val medicineLogText = medLogs.take(5).joinToString("\n") { ml ->
                "- ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(ml.timestamp)}: Took ${ml.medicationName} ${ml.dosage}"
            }
            val activeMedicationText = medications.take(5).joinToString("\n") { m ->
                "- ${m.name} ${m.dosage} at ${m.timeOfDay} (${m.frequency})"
            }
            val lastInsulinText = insulinLogs.take(5).joinToString("\n") { i ->
                "- ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(i.timestamp)}: ${i.units} units of ${i.insulinType} (${i.mealRelation}) Notes: ${i.notes}"
            }

            val prompt = """
You are DiaTrack's local on-device health analysis assistant.
Return STRICT JSON only. No markdown, no explanation outside JSON.

Task:
Predict blood glucose trends at 1 hour, 3 hours, 6 hours, 12 hours, and 24 hours.
Use the patient's logs only for estimation.
Do not prescribe medication or insulin doses.
Keep the response compact and clinically cautious.

Patient:
- Age: ${profile.age}
- Gender: ${profile.gender}
- Diabetes Type: ${profile.diabetesType}

Thresholds:
- Target Low: ${thresholds.lowThreshold} $unit
- Target High: ${thresholds.highThreshold} $unit
- Emergency Low: ${thresholds.emergencyLow} $unit
- Emergency High: ${thresholds.emergencyHigh} $unit

Recent glucose:
$lastReadingsText

Recent meals:
$lastMealsText

Recent oral meds:
$medicineLogText

Current medications:
$activeMedicationText

Recent insulin:
$lastInsulinText

JSON format:
{
  "predictions": [
    {
      "intervalLabel": "1h",
      "glucoseRangeMin": 110.0,
      "glucoseRangeMax": 130.0,
      "trend": "Falling",
      "possibleReason": "Short explanation.",
      "confidence": "High",
      "warningMessage": "Optional warning."
    }
  ],
  "insights": "Short summary."
}
""".trimIndent()

            val responseText = engine.generateResponse(prompt)
            val cleanJsonString = extractJsonFromString(responseText)
            val parsedResult = JSONObject(cleanJsonString)
            val predictionsArray = parsedResult.getJSONArray("predictions")
            val predictionsList = mutableListOf<GlucosePrediction>()

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
                insights = parsedResult.optString("insights", "On-device prediction completed."),
                isLocalMock = false
            )
        } catch (e: Exception) {
            Log.e(TAG, "MediaPipe LLM prediction failed, falling back to local formulas", e)
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

    private fun computeLocalPredictions(
        profile: UserProfile,
        thresholds: AlertThreshold,
        readings: List<GlucoseReading>,
        meals: List<MealLog>,
        medLogs: List<MedicationLog>,
        insulinLogs: List<InsulinLog>,
        unit: String
    ): TrendPredictionResult {
        val baseGlucose = readings.firstOrNull()?.value ?: 120.0
        val isMmol = unit == "mmol/L"
        val targetBase = if (isMmol) baseGlucose * 18.0 else baseGlucose
        val hours = listOf(1, 3, 6, 12, 24)
        val intervals = listOf("1h", "3h", "6h", "12h", "24h")
        val now = System.currentTimeMillis()
        var estimatedActiveCarbs = 0.0
        var estimatedActiveInsulin = 0.0

        meals.filter { now - it.timestamp < 4 * 3600 * 1000 }.forEach {
            val ageHours = (now - it.timestamp).toDouble() / (3600 * 1000)
            val absorbFactor = max(0.0, 1.0 - (ageHours / 4.0))
            estimatedActiveCarbs += it.carbsGrams * absorbFactor
        }

        insulinLogs.filter { now - it.timestamp < 5 * 3600 * 1000 }.forEach {
            val ageHours = (now - it.timestamp).toDouble() / (3600 * 1000)
            val insulinFactor = max(0.0, 1.0 - (ageHours / 5.0))
            estimatedActiveInsulin += it.units * insulinFactor
        }

        val medPresence = medLogs.isNotEmpty()
        val profileOffset = when (profile.diabetesType) {
            "Type 1" -> -8.0
            "Gestational" -> -4.0
            else -> 0.0
        }

        val predictions = hours.mapIndexed { idx, h ->
            val carbRate = max(0.0, estimatedActiveCarbs * max(0.0, 1.0 - (h / 3.0)))
            val insulinRate = max(0.0, estimatedActiveInsulin * max(0.0, 1.0 - (h / 4.0)))
            var predictedMgDl = targetBase + profileOffset + (carbRate * 2.3) - (insulinRate * 34.0)
            val baseline = 100.0
            predictedMgDl = predictedMgDl + (baseline - predictedMgDl) * (1.0 - kotlin.math.exp(-0.15 * h))
            predictedMgDl = max(40.0, min(350.0, predictedMgDl))

            val finalMin = if (isMmol) (predictedMgDl - 10.0) / 18.0 else (predictedMgDl - 15.0)
            val finalMax = if (isMmol) (predictedMgDl + 10.0) / 18.0 else (predictedMgDl + 15.0)

            val trend = when {
                predictedMgDl - targetBase > 15 -> "Rising"
                targetBase - predictedMgDl > 15 -> "Falling"
                else -> "Stable"
            }
            val reason = when {
                estimatedActiveCarbs > 20.0 && h <= 3 -> "Recent meal carbohydrates are still active."
                estimatedActiveInsulin > 2.0 && h <= 3 -> "Recent insulin may still be lowering glucose."
                medPresence && h <= 6 -> "Medication history suggests ongoing metabolic control effects."
                else -> "Balance of meal absorption, insulin activity, and baseline drift."
            }
            val warning = when {
                predictedMgDl < thresholds.emergencyLow -> "Predicted dangerously low. Verify with a real reading."
                predictedMgDl < thresholds.lowThreshold -> "Predicted near low range. Monitor closely."
                predictedMgDl > thresholds.emergencyHigh -> "Predicted dangerously high. Verify with a real reading."
                predictedMgDl > thresholds.highThreshold -> "Predicted above target range. Monitor closely."
                else -> null
            }

            GlucosePrediction(
                intervalIndex = idx,
                intervalLabel = intervals[idx],
                glucoseRangeMin = max(if (isMmol) 2.2 else 40.0, finalMin),
                glucoseRangeMax = min(if (isMmol) 20.0 else 350.0, finalMax),
                trend = trend,
                possibleReason = reason,
                confidence = if (readings.size >= 3) "Medium" else "Low",
                warningMessage = warning
            )
        }

        val localInsights = """
On-device fallback prediction used.
- Active carbs estimated: ${String.format("%.1f", estimatedActiveCarbs)}g
- Active insulin estimated: ${String.format("%.1f", estimatedActiveInsulin)}U
- Predictions are estimates only and not medical advice.
        """.trimIndent()

        return TrendPredictionResult(
            predictions = predictions,
            insights = localInsights,
            isLocalMock = true
        )
    }
}
