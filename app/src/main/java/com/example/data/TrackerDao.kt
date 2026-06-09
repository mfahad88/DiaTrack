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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: Reminder)

    @Update
    suspend fun updateReminder(reminder: Reminder)

    @Delete
    suspend fun deleteReminder(reminder: Reminder)


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
}
