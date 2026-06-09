package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        GlucoseReading::class,
        MealLog::class,
        Medication::class,
        MedicationLog::class,
        InsulinLog::class,
        Reminder::class,
        AlertThreshold::class,
        UserProfile::class,
        WaterIntake::class,
        Exercise::class,
        BloodPressure::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun trackerDao(): TrackerDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "diatrack_database"
                )
                .fallbackToDestructiveMigration() // Simple strategy for schema evolution in tracking apps
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
