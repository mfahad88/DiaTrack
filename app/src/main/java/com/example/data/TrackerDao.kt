package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackerDao {

    // --- Blood Sugar ---
    @Query("SELECT * FROM glucose_readings ORDER BY timestamp DESC")
    fun getAllGlucoseReadings(): Flow<List<GlucoseReading>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGlucoseReading(reading: GlucoseReading)

    @Update
    suspend fun updateGlucoseReading(reading: GlucoseReading)

    @Delete
    suspend fun deleteGlucoseReading(reading: GlucoseReading)

    @Query("DELETE FROM glucose_readings")
    suspend fun deleteAllGlucoseReadings()


    // --- Meals ---
    @Query("SELECT * FROM meal_logs ORDER BY timestamp DESC")
    fun getAllMealLogs(): Flow<List<MealLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealLog(mealLog: MealLog)

    @Update
    suspend fun updateMealLog(mealLog: MealLog)

    @Delete
    suspend fun deleteMealLog(mealLog: MealLog)

    @Query("DELETE FROM meal_logs")
    suspend fun deleteAllMealLogs()


    // --- Medications ---
    @Query("SELECT * FROM medications ORDER BY name ASC")
    fun getAllMedications(): Flow<List<Medication>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedication(medication: Medication)

    @Update
    suspend fun updateMedication(medication: Medication)

    @Delete
    suspend fun deleteMedication(medication: Medication)

    @Query("DELETE FROM medications")
    suspend fun deleteAllMedications()


    // --- Medication Logs ---
    @Query("SELECT * FROM medication_logs ORDER BY timestamp DESC")
    fun getAllMedicationLogs(): Flow<List<MedicationLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicationLog(log: MedicationLog)

    @Delete
    suspend fun deleteMedicationLog(log: MedicationLog)

    @Query("DELETE FROM medication_logs")
    suspend fun deleteAllMedicationLogs()


    // --- Insulin Logs ---
    @Query("SELECT * FROM insulin_logs ORDER BY timestamp DESC")
    fun getAllInsulinLogs(): Flow<List<InsulinLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInsulinLog(log: InsulinLog)

    @Update
    suspend fun updateInsulinLog(log: InsulinLog)

    @Delete
    suspend fun deleteInsulinLog(log: InsulinLog)

    @Query("DELETE FROM insulin_logs")
    suspend fun deleteAllInsulinLogs()


    // --- Reminders ---
    @Query("SELECT * FROM reminders ORDER BY timeHour ASC, timeMinute ASC")
    fun getAllReminders(): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders")
    suspend fun getAllRemindersSync(): List<Reminder>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: Reminder): Long

    @Update
    suspend fun updateReminder(reminder: Reminder)

    @Delete
    suspend fun deleteReminder(reminder: Reminder)

    @Query("DELETE FROM reminders")
    suspend fun deleteAllReminders()


    // --- Thresholds ---
    @Query("SELECT * FROM alert_thresholds WHERE id = 1 LIMIT 1")
    fun getAlertThreshold(): Flow<AlertThreshold?>

    @Query("SELECT * FROM alert_thresholds WHERE id = 1 LIMIT 1")
    suspend fun getAlertThresholdSync(): AlertThreshold?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setAlertThreshold(threshold: AlertThreshold)


    // --- User Profile ---
    @Query("SELECT * FROM user_profiles WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profiles WHERE id = 1 LIMIT 1")
    suspend fun getUserProfileSync(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setUserProfile(profile: UserProfile)

    // --- Water Intake ---
    @Query("SELECT * FROM water_intakes ORDER BY timestamp DESC")
    fun getAllWaterIntakes(): Flow<List<WaterIntake>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWaterIntake(water: WaterIntake)

    @Delete
    suspend fun deleteWaterIntake(water: WaterIntake)

    @Query("DELETE FROM water_intakes")
    suspend fun deleteAllWaterIntakes()

    // --- Exercise ---
    @Query("SELECT * FROM exercises ORDER BY timestamp DESC")
    fun getAllExercises(): Flow<List<Exercise>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: Exercise)

    @Delete
    suspend fun deleteExercise(exercise: Exercise)

    @Query("DELETE FROM exercises")
    suspend fun deleteAllExercises()

    // --- Blood Pressure ---
    @Query("SELECT * FROM blood_pressures ORDER BY timestamp DESC")
    fun getAllBloodPressures(): Flow<List<BloodPressure>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBloodPressure(bp: BloodPressure)

    @Delete
    suspend fun deleteBloodPressure(bp: BloodPressure)

    @Query("DELETE FROM blood_pressures")
    suspend fun deleteAllBloodPressures()

    // --- Sleep ---
    @Query("SELECT * FROM sleep_logs ORDER BY timestamp DESC")
    fun getAllSleepLogs(): Flow<List<SleepLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSleepLog(log: SleepLog)

    @Delete
    suspend fun deleteSleepLog(log: SleepLog)

    @Query("DELETE FROM sleep_logs")
    suspend fun deleteAllSleepLogs()

    // --- Stress & Mood ---
    @Query("SELECT * FROM stress_mood_logs ORDER BY timestamp DESC")
    fun getAllStressMoodLogs(): Flow<List<StressMoodLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStressMoodLog(log: StressMoodLog)

    @Delete
    suspend fun deleteStressMoodLog(log: StressMoodLog)

    @Query("DELETE FROM stress_mood_logs")
    suspend fun deleteAllStressMoodLogs()

    // --- Weight ---
    @Query("SELECT * FROM weight_logs ORDER BY timestamp DESC")
    fun getAllWeightLogs(): Flow<List<WeightLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeightLog(log: WeightLog)

    @Delete
    suspend fun deleteWeightLog(log: WeightLog)

    @Query("DELETE FROM weight_logs")
    suspend fun deleteAllWeightLogs()

    // --- Labs ---
    @Query("SELECT * FROM lab_results ORDER BY timestamp DESC")
    fun getAllLabResults(): Flow<List<LabResult>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLabResult(result: LabResult)

    @Delete
    suspend fun deleteLabResult(result: LabResult)

    @Query("DELETE FROM lab_results")
    suspend fun deleteAllLabResults()

    // --- Sick Day ---
    @Query("SELECT * FROM sick_day_logs ORDER BY timestamp DESC")
    fun getAllSickDayLogs(): Flow<List<SickDayLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSickDayLog(log: SickDayLog)

    @Delete
    suspend fun deleteSickDayLog(log: SickDayLog)

    @Query("DELETE FROM sick_day_logs")
    suspend fun deleteAllSickDayLogs()

    // --- Food Photo AI Estimates ---
    @Query("SELECT * FROM food_photo_estimates ORDER BY timestamp DESC")
    fun getAllFoodPhotoEstimates(): Flow<List<FoodPhotoEstimate>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodPhotoEstimate(estimate: FoodPhotoEstimate)

    @Delete
    suspend fun deleteFoodPhotoEstimate(estimate: FoodPhotoEstimate)

    @Query("DELETE FROM food_photo_estimates")
    suspend fun deleteAllFoodPhotoEstimates()

    // --- Wearable / Health Connect Snapshots ---
    @Query("SELECT * FROM wearable_snapshots ORDER BY timestamp DESC")
    fun getAllWearableSnapshots(): Flow<List<WearableSnapshot>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWearableSnapshot(snapshot: WearableSnapshot)

    @Delete
    suspend fun deleteWearableSnapshot(snapshot: WearableSnapshot)

    @Query("DELETE FROM wearable_snapshots")
    suspend fun deleteAllWearableSnapshots()
}
