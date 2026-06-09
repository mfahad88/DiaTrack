package com.example.repository

import com.example.data.*
import kotlinx.coroutines.flow.Flow

class TrackerRepository(private val trackerDao: TrackerDao) {

    // --- Blood Sugar Readings ---
    val allGlucoseReadings: Flow<List<GlucoseReading>> = trackerDao.getAllGlucoseReadings()

    suspend fun insertGlucoseReading(reading: GlucoseReading) {
        trackerDao.insertGlucoseReading(reading)
    }

    suspend fun updateGlucoseReading(reading: GlucoseReading) {
        trackerDao.updateGlucoseReading(reading)
    }

    suspend fun deleteGlucoseReading(reading: GlucoseReading) {
        trackerDao.deleteGlucoseReading(reading)
    }


    // --- Meal Logs ---
    val allMealLogs: Flow<List<MealLog>> = trackerDao.getAllMealLogs()

    suspend fun insertMealLog(mealLog: MealLog) {
        trackerDao.insertMealLog(mealLog)
    }

    suspend fun updateMealLog(mealLog: MealLog) {
        trackerDao.updateMealLog(mealLog)
    }

    suspend fun deleteMealLog(mealLog: MealLog) {
        trackerDao.deleteMealLog(mealLog)
    }


    // --- Medications ---
    val allMedications: Flow<List<Medication>> = trackerDao.getAllMedications()

    suspend fun insertMedication(medication: Medication) {
        trackerDao.insertMedication(medication)
    }

    suspend fun updateMedication(medication: Medication) {
        trackerDao.updateMedication(medication)
    }

    suspend fun deleteMedication(medication: Medication) {
        trackerDao.deleteMedication(medication)
    }


    // --- Medication Logs ---
    val allMedicationLogs: Flow<List<MedicationLog>> = trackerDao.getAllMedicationLogs()

    suspend fun insertMedicationLog(log: MedicationLog) {
        trackerDao.insertMedicationLog(log)
    }

    suspend fun deleteMedicationLog(log: MedicationLog) {
        trackerDao.deleteMedicationLog(log)
    }


    // --- Insulin Logs ---
    val allInsulinLogs: Flow<List<InsulinLog>> = trackerDao.getAllInsulinLogs()

    suspend fun insertInsulinLog(log: InsulinLog) {
        trackerDao.insertInsulinLog(log)
    }

    suspend fun updateInsulinLog(log: InsulinLog) {
        trackerDao.updateInsulinLog(log)
    }

    suspend fun deleteInsulinLog(log: InsulinLog) {
        trackerDao.deleteInsulinLog(log)
    }


    // --- Reminders ---
    val allReminders: Flow<List<Reminder>> = trackerDao.getAllReminders()

    suspend fun insertReminder(reminder: Reminder) {
        trackerDao.insertReminder(reminder)
    }

    suspend fun updateReminder(reminder: Reminder) {
        trackerDao.updateReminder(reminder)
    }

    suspend fun deleteReminder(reminder: Reminder) {
        trackerDao.deleteReminder(reminder)
    }


    // --- Thresholds ---
    val alertThreshold: Flow<AlertThreshold?> = trackerDao.getAlertThreshold()

    suspend fun getAlertThresholdSync(): AlertThreshold? {
        return trackerDao.getAlertThresholdSync()
    }

    suspend fun setAlertThreshold(threshold: AlertThreshold) {
        trackerDao.setAlertThreshold(threshold)
    }


    // --- User Profile ---
    val userProfile: Flow<UserProfile?> = trackerDao.getUserProfile()

    suspend fun getUserProfileSync(): UserProfile? {
        return trackerDao.getUserProfileSync()
    }

    suspend fun setUserProfile(profile: UserProfile) {
        trackerDao.setUserProfile(profile)
    }

    // --- Water Intake ---
    val allWaterIntakes: Flow<List<WaterIntake>> = trackerDao.getAllWaterIntakes()

    suspend fun insertWaterIntake(water: WaterIntake) {
        trackerDao.insertWaterIntake(water)
    }

    suspend fun deleteWaterIntake(water: WaterIntake) {
        trackerDao.deleteWaterIntake(water)
    }

    // --- Exercise ---
    val allExercises: Flow<List<Exercise>> = trackerDao.getAllExercises()

    suspend fun insertExercise(exercise: Exercise) {
        trackerDao.insertExercise(exercise)
    }

    suspend fun deleteExercise(exercise: Exercise) {
        trackerDao.deleteExercise(exercise)
    }

    // --- Blood Pressure ---
    val allBloodPressures: Flow<List<BloodPressure>> = trackerDao.getAllBloodPressures()

    suspend fun insertBloodPressure(bp: BloodPressure) {
        trackerDao.insertBloodPressure(bp)
    }

    suspend fun deleteBloodPressure(bp: BloodPressure) {
        trackerDao.deleteBloodPressure(bp)
    }

    suspend fun clearAllLogs() {
        trackerDao.deleteAllGlucoseReadings()
        trackerDao.deleteAllMealLogs()
        trackerDao.deleteAllMedications()
        trackerDao.deleteAllMedicationLogs()
        trackerDao.deleteAllInsulinLogs()
        trackerDao.deleteAllWaterIntakes()
        trackerDao.deleteAllExercises()
        trackerDao.deleteAllBloodPressures()
    }
}
