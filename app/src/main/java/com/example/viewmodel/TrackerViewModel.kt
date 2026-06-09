package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.GeminiPredictionService
import com.example.api.TrendPredictionResult
import com.example.data.*
import com.example.repository.TrackerRepository
import com.example.ui.components.PdfExportHelper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

class TrackerViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = TrackerRepository(db.trackerDao())

    // --- Core Subscribed States (M3 standard architecture flow) ---
    val glucoseReadings: StateFlow<List<GlucoseReading>> = repository.allGlucoseReadings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val mealLogs: StateFlow<List<MealLog>> = repository.allMealLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val medications: StateFlow<List<Medication>> = repository.allMedications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val medicationLogs: StateFlow<List<MedicationLog>> = repository.allMedicationLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val insulinLogs: StateFlow<List<InsulinLog>> = repository.allInsulinLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reminders: StateFlow<List<Reminder>> = repository.allReminders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val waterIntakes: StateFlow<List<WaterIntake>> = repository.allWaterIntakes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val exercises: StateFlow<List<Exercise>> = repository.allExercises
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bloodPressures: StateFlow<List<BloodPressure>> = repository.allBloodPressures
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Unit Preference: default to "mg/dL", can be toggled by user
    private val _glucoseUnit = MutableStateFlow("mg/dL")
    val glucoseUnit: StateFlow<String> = _glucoseUnit.asStateFlow()

    // Threshold state, default configured upon initialization if db is empty
    val alertThreshold: StateFlow<AlertThreshold> = repository.alertThreshold
        .map { it ?: AlertThreshold() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, AlertThreshold())

    // User profile state
    val userProfile: StateFlow<UserProfile> = repository.userProfile
        .map { it ?: UserProfile() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, UserProfile())

    // --- UI/UX Temporary Alert States ---
    private val _activeAlert = MutableStateFlow<String?>(null)
    val activeAlert: StateFlow<String?> = _activeAlert.asStateFlow()

    private val _isEmergency = MutableStateFlow(false)
    val isEmergency: StateFlow<Boolean> = _isEmergency.asStateFlow()

    // --- AI Prediction States ---
    private val _predictionResult = MutableStateFlow<TrendPredictionResult?>(null)
    val predictionResult: StateFlow<TrendPredictionResult?> = _predictionResult.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    init {
        // Run seed check for defaults upon app launch
        viewModelScope.launch {
            seedInitialDatabase()
        }
    }

    private suspend fun seedInitialDatabase() {
        // Seed default thresholds if empty in database
        val existingThreshold = repository.getAlertThresholdSync()
        if (existingThreshold == null) {
            repository.setAlertThreshold(AlertThreshold())
        }

        // Seed default profile if empty
        val existingProfile = repository.getUserProfileSync()
        if (existingProfile == null) {
            repository.setUserProfile(UserProfile())
        }

        // Seed some sample reminders if they don't exist yet
        repository.allReminders.first().let { list ->
            if (list.isEmpty()) {
                repository.insertReminder(Reminder(title = "Morning Fasting Glucose Check", timeHour = 8, timeMinute = 0, type = "Blood Sugar"))
                repository.insertReminder(Reminder(title = "Breakfast Meal Logging", timeHour = 8, timeMinute = 30, type = "Meal"))
                repository.insertReminder(Reminder(title = "Afternoon Glucose Routine check", timeHour = 14, timeMinute = 0, type = "Blood Sugar"))
                repository.insertReminder(Reminder(title = "Evening Insulin Dosage logs", timeHour = 20, timeMinute = 0, type = "Insulin"))
                repository.insertReminder(Reminder(title = "Bedtime Blood Sugar Check", timeHour = 22, timeMinute = 30, type = "Blood Sugar"))
            }
        }
    }

    // --- Unit conversion Helper ---
    fun toggleGlucoseUnit() {
        val current = _glucoseUnit.value
        val newUnit = if (current == "mg/dL") "mmol/L" else "mg/dL"
        _glucoseUnit.value = newUnit
        
        // Regenerate prediction so units scale matches the newly selected preferences
        if (_predictionResult.value != null) {
            runAiPrediction()
        }
    }

    // --- Add/Delete handlers for metrics ---
    fun addGlucoseReading(value: Double, type: String, notes: String, symptoms: String, mealRelation: String) {
        viewModelScope.launch {
            val unit = _glucoseUnit.value
            val reading = GlucoseReading(
                value = value,
                unit = unit,
                type = type,
                notes = notes,
                symptoms = symptoms,
                mealRelation = mealRelation
            )
            repository.insertGlucoseReading(reading)
            
            // Instantly perform safety evaluations based on thresholds (Requirement 7)
            evaluateSafetyThresholds(value, unit)
        }
    }

    fun updateGlucoseReading(reading: GlucoseReading) {
        viewModelScope.launch {
            repository.updateGlucoseReading(reading)
            evaluateSafetyThresholds(reading.value, reading.unit)
        }
    }

    fun deleteGlucoseReading(reading: GlucoseReading) {
        viewModelScope.launch {
            repository.deleteGlucoseReading(reading)
            clearCurrentAlerts()
        }
    }

    fun addWaterIntake(amountMl: Double, notes: String) {
        viewModelScope.launch {
            val water = WaterIntake(amountMl = amountMl, notes = notes)
            repository.insertWaterIntake(water)
            
            // Check if user is dehydrated
            val totalToday = (waterIntakes.value.sumOf { it.amountMl } + amountMl)
            if (totalToday < 1500.0) {
                _isEmergency.value = false
                _activeAlert.value = "HYDRATION PRECAUTION: Daily hydration logging is currently $totalToday mL (Recommended: >2000 mL). Dehydration can artificially elevate blood sugar concentration, strain kidneys, and cause blood pressure fluctuations."
            } else if (totalToday >= 2000.0 && _activeAlert.value?.contains("HYDRATION") == true) {
                clearCurrentAlerts()
            }
        }
    }

    fun deleteWaterIntake(water: WaterIntake) {
        viewModelScope.launch {
            repository.deleteWaterIntake(water)
        }
    }

    fun addExercise(activityType: String, durationMinutes: Int, intensity: String, caloriesBurned: Double, notes: String) {
        viewModelScope.launch {
            val exercise = Exercise(
                activityType = activityType,
                durationMinutes = durationMinutes,
                intensity = intensity,
                caloriesBurned = caloriesBurned,
                notes = notes
            )
            repository.insertExercise(exercise)
            
            // Exercise medical safety alert
            if (durationMinutes >= 30 && (intensity == "High" || intensity == "Moderate")) {
                _isEmergency.value = false
                _activeAlert.value = "EXERCISE CLINICAL ALERT: You logged a $durationMinutes-min $intensity intensity workout. Sustained exercise significantly increases insulin sensitivity and muscle glucose uptake, posing a risk of delayed hypoglycemia. Please verify blood glucose and keep rapid carbs nearby."
            }
        }
    }

    fun deleteExercise(exercise: Exercise) {
        viewModelScope.launch {
            repository.deleteExercise(exercise)
        }
    }

    fun addBloodPressure(systolic: Int, diastolic: Int, pulse: Int, symptoms: String, notes: String) {
        viewModelScope.launch {
            val bp = BloodPressure(
                systolic = systolic,
                diastolic = diastolic,
                pulse = pulse,
                symptoms = symptoms,
                notes = notes
            )
            repository.insertBloodPressure(bp)
            
            // Clinical blood pressure safety threshold check
            when {
                systolic >= 180 || diastolic >= 120 -> {
                    _isEmergency.value = true
                    _activeAlert.value = "CRITICAL HYPERTENSIVE CRISIS ALARM: Blood pressure reading is dangerously high ($systolic/$diastolic mmHg)! Please rest quietly for 5 minutes and remeasure. If pressure is still high or if you experience chest pain, breathlessness, back pain, numbness, weakness, or difficulty speaking, seek IMMEDIATE emergency ER medical assistance."
                }
                systolic < 90 || diastolic < 60 -> {
                    _isEmergency.value = true
                    _activeAlert.value = "CLINICAL HYPOTENSION ALARM: Low blood pressure detected ($systolic/$diastolic mmHg). If experiencing severe dizziness, cold clammy skin, rapid shallow breathing, or confusion, lay flat with elevated legs, drink fluids, and call your primary physician."
                }
                systolic >= 140 || diastolic >= 90 -> {
                    _isEmergency.value = false
                    _activeAlert.value = "CLINICAL WARNING (Hypertension Stage 2): Blood pressure is elevated at $systolic/$diastolic mmHg. Restrict dietary sodium, stay hydrated, avoid physical over-exertion, and review standard daily medications with your provider."
                }
                systolic >= 130 || diastolic >= 80 -> {
                    _isEmergency.value = false
                    _activeAlert.value = "CLINICAL NOTICE (Hypertension Stage 1): Blood pressure is elevated at $systolic/$diastolic mmHg. Consider lifestyle interventions including stress reduction and medical checkups."
                }
            }
        }
    }

    fun deleteBloodPressure(bp: BloodPressure) {
        viewModelScope.launch {
            repository.deleteBloodPressure(bp)
            if (_activeAlert.value?.contains("HYPERTENSIVE") == true || _activeAlert.value?.contains("HYPOTENSION") == true) {
                clearCurrentAlerts()
            }
        }
    }

    fun clearCurrentAlerts() {
        _activeAlert.value = null
        _isEmergency.value = false
    }

    private fun evaluateSafetyThresholds(glucoseValue: Double, unit: String) {
        val thresholds = alertThreshold.value
        // Standards are processed in mg/dL inside calculations for consistency
        val isMmolWithMgConversion = unit == "mmol/L"
        val valInMgDl = if (isMmolWithMgConversion) glucoseValue * 18.0 else glucoseValue

        when {
            valInMgDl < thresholds.emergencyLow -> {
                _isEmergency.value = true
                _activeAlert.value = "CRITICAL HYPOGLYCEMIA WARNING: Your blood sugar reading ($glucoseValue $unit) is dangerously low! Please ingest 15-20g of fast-acting glucose (fruit juice, soda, sugar tablets) immediately. Re-test in 15 minutes. Call emergency services immediately if you feel confused, faint, or severe symptoms persist."
            }
            valInMgDl < thresholds.lowThreshold -> {
                _isEmergency.value = false
                _activeAlert.value = "HYPOGLYCEMIA ALERT: Your sugar reading ($glucoseValue $unit) is below your set threshold. Please consume small carbs and inform your family or doctor if trends persist."
            }
            valInMgDl > thresholds.emergencyHigh -> {
                _isEmergency.value = true
                _activeAlert.value = "CRITICAL HYPERGLYCEMIA WARNING: Your blood sugar reading ($glucoseValue $unit) is dangerously high! Avoid heavy graphics exertion, check your urine ketones if instructed, elevate hydration, and seek immediate clinical care if you experience vomiting, fatigue, or breathing issues."
            }
            valInMgDl > thresholds.highThreshold -> {
                _isEmergency.value = false
                _activeAlert.value = "HYPERGLYCEMIA ALERT: Your sugar reading ($glucoseValue $unit) is elevated above your configured target bounds. Monitor your hydration, meal carbs, and review active medication parameters."
            }
            else -> {
                // Clear active alerts if within healthy ranges
                _activeAlert.value = null
                _isEmergency.value = false
            }
        }
    }

    // --- Meals CRUD ---
    fun addMealLog(mealType: String, foodName: String, carbsGrams: Double, notes: String) {
        viewModelScope.launch {
            repository.insertMealLog(
                MealLog(
                    mealType = mealType,
                    foodName = foodName,
                    carbsGrams = carbsGrams,
                    notes = notes
                )
            )
        }
    }

    fun deleteMealLog(mealLog: MealLog) {
        viewModelScope.launch {
            repository.deleteMealLog(mealLog)
        }
    }

    // --- Medications CRUD ---
    fun addMedication(name: String, dosage: String, timeOfDay: String, frequency: String, notes: String) {
        viewModelScope.launch {
            repository.insertMedication(
                Medication(
                    name = name,
                    dosage = dosage,
                    timeOfDay = timeOfDay,
                    frequency = frequency,
                    notes = notes
                )
            )
        }
    }

    fun deleteMedication(medication: Medication) {
        viewModelScope.launch {
            repository.deleteMedication(medication)
        }
    }

    fun takeMedicationLog(medication: Medication, notes: String = "") {
        viewModelScope.launch {
            repository.insertMedicationLog(
                MedicationLog(
                    medicationId = medication.id,
                    medicationName = medication.name,
                    dosage = medication.dosage,
                    notes = notes
                )
            )
        }
    }

    fun deleteMedicationLog(log: MedicationLog) {
        viewModelScope.launch {
            repository.deleteMedicationLog(log)
        }
    }

    // --- Insulin CRUD ---
    fun addInsulinLog(insulinType: String, units: Double, mealRelation: String, notes: String) {
        viewModelScope.launch {
            repository.insertInsulinLog(
                InsulinLog(
                    insulinType = insulinType,
                    units = units,
                    mealRelation = mealRelation,
                    notes = notes
                )
            )
        }
    }

    fun deleteInsulinLog(log: InsulinLog) {
        viewModelScope.launch {
            repository.deleteInsulinLog(log)
        }
    }

    // --- Reminders CRUD ---
    fun addReminder(title: String, hour: Int, minute: Int, type: String) {
        viewModelScope.launch {
            repository.insertReminder(
                Reminder(
                    title = title,
                    timeHour = hour,
                    timeMinute = minute,
                    type = type,
                    isEnabled = true
                )
            )
        }
    }

    fun toggleReminder(reminder: Reminder) {
        viewModelScope.launch {
            val updated = reminder.copy(isEnabled = !reminder.isEnabled)
            repository.updateReminder(updated)
        }
    }

    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch {
            repository.deleteReminder(reminder)
        }
    }

    // --- Alert thresholds ---
    fun updateAlertThresholds(low: Double, high: Double, emergencyLow: Double, emergencyHigh: Double) {
        viewModelScope.launch {
            repository.setAlertThreshold(
                AlertThreshold(
                    lowThreshold = low,
                    highThreshold = high,
                    emergencyLow = emergencyLow,
                    emergencyHigh = emergencyHigh
                )
            )
        }
    }

    // --- Profile setting ---
    fun updateUserProfile(name: String, age: Int, gender: String, type: String) {
        viewModelScope.launch {
            repository.setUserProfile(
                UserProfile(
                    name = name,
                    age = age,
                    gender = gender,
                    diabetesType = type
                )
            )
        }
    }

    // --- AI Prediction Fetch flow ---
    fun runAiPrediction() {
        viewModelScope.launch {
            _isAnalyzing.value = true
            try {
                val profile = userProfile.value
                val thresholds = alertThreshold.value
                val readings = glucoseReadings.value
                val meals = mealLogs.value
                val meds = medications.value
                val medLogsList = medicationLogs.value
                val insulinLogsList = insulinLogs.value
                val currentUnit = _glucoseUnit.value

                val result = GeminiPredictionService.getPredictions(
                    profile = profile,
                    thresholds = thresholds,
                    readings = readings,
                    meals = meals,
                    medications = meds,
                    medLogs = medLogsList,
                    insulinLogs = insulinLogsList,
                    unit = currentUnit
                )

                _predictionResult.value = result
            } catch (e: Exception) {
                Log.e("TrackerViewModel", "AI pattern prediction failed", e)
            } finally {
                _isAnalyzing.value = false
            }
        }
    }

    // --- PDF Clinical Report Generator ---
    fun generateAndShareClinicalReport(context: Context) {
        viewModelScope.launch {
            val profile = userProfile.value
            val thresholds = alertThreshold.value
            val readings = glucoseReadings.value
            val meals = mealLogs.value
            val meds = medications.value
            val medLogsList = medicationLogs.value
            val insulinLogsList = insulinLogs.value
            val currentUnit = _glucoseUnit.value

            val pdfFile = PdfExportHelper.exportReport(
                context = context,
                profile = profile,
                thresholds = thresholds,
                readings = readings,
                meals = meals,
                medications = meds,
                medLogs = medLogsList,
                insulinLogs = insulinLogsList,
                unit = currentUnit
            )

            if (pdfFile != null) {
                PdfExportHelper.sharePdf(context, pdfFile)
            } else {
                Log.e("TrackerViewModel", "PDF Export failed. File handle is null.")
            }
        }
    }

    fun loadSampleDataset() {
        viewModelScope.launch {
            repository.clearAllLogs()

            val now = System.currentTimeMillis()
            val hourMs = 3600000L

            // 1. Set custom profile details
            repository.setUserProfile(
                UserProfile(
                    name = "Alex Dawson",
                    age = 38,
                    gender = "Male",
                    diabetesType = "Type 2"
                )
            )

            // 2. Insert default thresholds for target triggers
            repository.setAlertThreshold(
                AlertThreshold(
                    lowThreshold = 70.0,
                    highThreshold = 140.0,
                    emergencyLow = 55.0,
                    emergencyHigh = 200.0
                )
            )

            // 3. Insert default medications
            val med1 = Medication(id = 1, name = "Metformin", dosage = "500mg", timeOfDay = "Morning & Dinner", frequency = "Daily", notes = "Take with food")
            val med2 = Medication(id = 2, name = "Jardiance", dosage = "10mg", timeOfDay = "Morning", frequency = "Daily", notes = "Cardio-protective SGLT2 inhibitor")
            repository.insertMedication(med1)
            repository.insertMedication(med2)

            // 4. Insert medication logs
            repository.insertMedicationLog(MedicationLog(medicationId = 1, medicationName = "Metformin", dosage = "500mg", timestamp = now - 2 * hourMs, notes = "Taken after breakfast"))
            repository.insertMedicationLog(MedicationLog(medicationId = 2, medicationName = "Jardiance", dosage = "10mg", timestamp = now - 2 * hourMs, notes = "Taken with breakfast"))
            repository.insertMedicationLog(MedicationLog(medicationId = 1, medicationName = "Metformin", dosage = "500mg", timestamp = now - 14 * hourMs, notes = "Taken with dinner"))
            repository.insertMedicationLog(MedicationLog(medicationId = 1, medicationName = "Metformin", dosage = "500mg", timestamp = now - 26 * hourMs, notes = "Taken with morning breakfast"))

            // 5. Insert meals
            repository.insertMealLog(MealLog(mealType = "Breakfast", foodName = "Oatmeal with chia seeds, banana slices & coffee", carbsGrams = 45.0, timestamp = now - 2 * hourMs, notes = "Slightly over-carbed"))
            repository.insertMealLog(MealLog(mealType = "Dinner", foodName = "Lemon herb baked salmon, quinoa & asparagus", carbsGrams = 30.0, timestamp = now - 13 * hourMs))
            repository.insertMealLog(MealLog(mealType = "Lunch", foodName = "Grilled chicken wrap with light mayo", carbsGrams = 40.0, timestamp = now - 20 * hourMs))
            repository.insertMealLog(MealLog(mealType = "Breakfast", foodName = "Greek yogurt with blueberries & pumpkin seeds", carbsGrams = 20.0, timestamp = now - 26 * hourMs))

            // 6. Insert Insulin logs
            repository.insertInsulinLog(InsulinLog(insulinType = "Rapid-acting", units = 6.0, mealRelation = "After Breakfast", timestamp = now - 2 * hourMs, notes = "Correction dosage sliding scale"))
            repository.insertInsulinLog(InsulinLog(insulinType = "Rapid-acting", units = 8.0, mealRelation = "Before Dinner", timestamp = now - 14 * hourMs))
            repository.insertInsulinLog(InsulinLog(insulinType = "Long-acting", units = 14.0, mealRelation = "Bedtime", timestamp = now - 11 * hourMs, notes = "Basal pattern support"))
            repository.insertInsulinLog(InsulinLog(insulinType = "Rapid-acting", units = 8.0, mealRelation = "Before Lunch", timestamp = now - 21 * hourMs))

            // 7. Insert Glucose Readings
            val readings = listOf(
                GlucoseReading(value = 182.0, unit = "mg/dL", type = "After Meal", mealRelation = "After Breakfast", timestamp = now - 12 * 60000L, symptoms = "Slight fatigue", notes = "Tired after eating breakfast"),
                GlucoseReading(value = 104.0, unit = "mg/dL", type = "Fasting", mealRelation = "Before Breakfast", timestamp = now - 4 * hourMs, symptoms = "Feeling great", notes = "Woke up with normal fasting"),
                GlucoseReading(value = 122.0, unit = "mg/dL", type = "Bedtime", mealRelation = "Bedtime", timestamp = now - 10 * hourMs, symptoms = "Normal", notes = "Pre-sleep check"),
                GlucoseReading(value = 195.0, unit = "mg/dL", type = "After Meal", mealRelation = "After Dinner", timestamp = now - 13 * hourMs, symptoms = "High thirst", notes = "Spiked after quinoa portion size"),
                GlucoseReading(value = 110.0, unit = "mg/dL", type = "Before Meal", mealRelation = "Before Dinner", timestamp = now - 14 * hourMs, symptoms = "Normal"),
                GlucoseReading(value = 135.0, unit = "mg/dL", type = "After Meal", mealRelation = "After Lunch", timestamp = now - 19 * hourMs, symptoms = "Normal"),
                GlucoseReading(value = 98.0, unit = "mg/dL", type = "Before Meal", mealRelation = "Before Lunch", timestamp = now - 21 * hourMs, symptoms = "Hungry"),
                GlucoseReading(value = 118.0, unit = "mg/dL", type = "After Meal", mealRelation = "After Breakfast", timestamp = now - 25 * hourMs),
                GlucoseReading(value = 102.0, unit = "mg/dL", type = "Fasting", mealRelation = "Before Breakfast", timestamp = now - 28 * hourMs)
            )

            for (r in readings) {
                repository.insertGlucoseReading(r)
            }

            // 8. Insert Water intakes
            repository.insertWaterIntake(WaterIntake(amountMl = 250.0, timestamp = now - 1 * hourMs, notes = "Post-workout glass"))
            repository.insertWaterIntake(WaterIntake(amountMl = 500.0, timestamp = now - 5 * hourMs, notes = "Morning tall bottle"))
            repository.insertWaterIntake(WaterIntake(amountMl = 350.0, timestamp = now - 11 * hourMs, notes = "With lunch"))
            repository.insertWaterIntake(WaterIntake(amountMl = 400.0, timestamp = now - 18 * hourMs, notes = "Bedtime hydrater"))

            // 9. Insert Exercises
            repository.insertExercise(Exercise(activityType = "Power Walking", durationMinutes = 35, intensity = "Moderate", caloriesBurned = 180.0, timestamp = now - 2 * hourMs, notes = "Felt steady, checked glucose after"))
            repository.insertExercise(Exercise(activityType = "Cardio Cycling", durationMinutes = 45, intensity = "High", caloriesBurned = 420.0, timestamp = now - 20 * hourMs, notes = "High carbs consumed earlier to prevent drop"))

            // 10. Insert Blood Pressures
            repository.insertBloodPressure(BloodPressure(systolic = 122, diastolic = 78, pulse = 70, timestamp = now - 2 * hourMs, symptoms = "Normal", notes = "Post-stretch review"))
            repository.insertBloodPressure(BloodPressure(systolic = 138, diastolic = 88, pulse = 74, timestamp = now - 14 * hourMs, symptoms = "None", notes = "Pre-meal baseline"))
            repository.insertBloodPressure(BloodPressure(systolic = 118, diastolic = 76, pulse = 68, timestamp = now - 26 * hourMs, symptoms = "Good", notes = "Morning check"))

            evaluateSafetyThresholds(182.0, "mg/dL")

            runAiPrediction()
        }
    }
}
