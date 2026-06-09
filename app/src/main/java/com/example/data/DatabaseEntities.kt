package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "glucose_readings")
data class GlucoseReading(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val value: Double,
    val unit: String, // "mg/dL" or "mmol/L"
    val type: String, // "Fasting", "Before Meal", "After Meal", "Bedtime", "Random"
    val notes: String = "",
    val symptoms: String = "", // Comma-separated or descriptive text
    val mealRelation: String = "" // "Before Meal", "After Meal", etc.
)

@Entity(tableName = "meal_logs")
data class MealLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val mealType: String, // "Breakfast", "Lunch", "Dinner", "Snack"
    val foodName: String,
    val carbsGrams: Double,
    val notes: String = ""
)

@Entity(tableName = "medications")
data class Medication(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val dosage: String,
    val timeOfDay: String, // e.g. "08:00 AM", "Night"
    val frequency: String, // "Daily", "Weekly", etc.
    val startDate: Long = System.currentTimeMillis(),
    val endDate: Long = System.currentTimeMillis(),
    val notes: String = ""
)

@Entity(tableName = "medication_logs")
data class MedicationLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val medicationId: Int,
    val medicationName: String,
    val dosage: String,
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = ""
)

@Entity(tableName = "insulin_logs")
data class InsulinLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val insulinType: String, // e.g., "Rapid-acting", "Long-acting", "Rapid/Short-acting Regular"
    val units: Double,
    val mealRelation: String, // "Before Breakfast", "Before Lunch", "Before Dinner", "Bedtime", "None"
    val notes: String = ""
)

@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val timeHour: Int, // 0-23
    val timeMinute: Int, // 0-59
    val type: String, // "Blood Sugar", "Insulin", "Medication", "Meal", "Appointment"
    val daysOfWeek: String = "Mon,Tue,Wed,Thu,Fri,Sat,Sun", // Comma separated active days
    val isEnabled: Boolean = true
)

@Entity(tableName = "alert_thresholds")
data class AlertThreshold(
    @PrimaryKey val id: Int = 1, // Only 1 settings row
    val lowThreshold: Double = 70.0, // mg/dL
    val highThreshold: Double = 180.0, // mg/dL
    val emergencyLow: Double = 50.0, // mg/dL
    val emergencyHigh: Double = 250.0 // mg/dL
)

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey val id: Int = 1, // Only 1 profile row
    val name: String = "Medical Consult Guest",
    val age: Int = 45,
    val gender: String = "Male",
    val diabetesType: String = "Type 2" // "Type 1", "Type 2", "Gestational", "Pre-diabetes"
)

@Entity(tableName = "water_intakes")
data class WaterIntake(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val amountMl: Double,
    val notes: String = ""
)

@Entity(tableName = "exercises")
data class Exercise(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val activityType: String,
    val durationMinutes: Int,
    val intensity: String, // "Low", "Moderate", "High"
    val caloriesBurned: Double = 0.0,
    val notes: String = ""
)

@Entity(tableName = "blood_pressures")
data class BloodPressure(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val systolic: Int,
    val diastolic: Int,
    val pulse: Int = 0,
    val symptoms: String = "",
    val notes: String = ""
)

@Entity(tableName = "sleep_logs")
data class SleepLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val durationHours: Double,
    val quality: String,
    val wakeGlucose: Double = 0.0,
    val notes: String = ""
)

@Entity(tableName = "stress_mood_logs")
data class StressMoodLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val stressLevel: Int,
    val mood: String,
    val symptoms: String = "",
    val notes: String = ""
)

@Entity(tableName = "weight_logs")
data class WeightLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val weightKg: Double,
    val waistCm: Double = 0.0,
    val bmi: Double = 0.0,
    val notes: String = ""
)

@Entity(tableName = "lab_results")
data class LabResult(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val hba1c: Double = 0.0,
    val ldl: Double = 0.0,
    val hdl: Double = 0.0,
    val triglycerides: Double = 0.0,
    val creatinine: Double = 0.0,
    val egfr: Double = 0.0,
    val urineAlbumin: Double = 0.0,
    val ketones: String = "",
    val notes: String = ""
)

@Entity(tableName = "sick_day_logs")
data class SickDayLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val temperatureC: Double = 0.0,
    val ketones: String = "",
    val appetite: String = "",
    val vomiting: Boolean = false,
    val hydrationConcern: Boolean = false,
    val notes: String = ""
)

@Entity(tableName = "food_photo_estimates")
data class FoodPhotoEstimate(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val description: String,
    val estimatedCarbsGrams: Double,
    val estimatedCalories: Double,
    val estimatedProteinGrams: Double = 0.0,
    val estimatedFatGrams: Double = 0.0,
    val confidence: String = "Medium",
    val photoUri: String = "",
    val notes: String = ""
)

@Entity(tableName = "wearable_snapshots")
data class WearableSnapshot(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val source: String = "Manual",
    val steps: Int = 0,
    val heartRate: Int = 0,
    val sleepHours: Double = 0.0,
    val activeCalories: Double = 0.0,
    val notes: String = ""
)
