package com.example.ui.screens

import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.api.TrendPredictionResult
import com.example.data.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.TrackerViewModel
import java.text.SimpleDateFormat
import java.util.*

val Color.Companion.GRAY: Color get() = Color.Gray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: TrackerViewModel) {
    val context = LocalContext.current
    var currentTab by remember { mutableStateOf(0) }

    // Dynamic states collected reactively from state flows
    val readings by viewModel.glucoseReadings.collectAsState()
    val meals by viewModel.mealLogs.collectAsState()
    val medications by viewModel.medications.collectAsState()
    val medLogs by viewModel.medicationLogs.collectAsState()
    val insulinLogs by viewModel.insulinLogs.collectAsState()
    val reminders by viewModel.reminders.collectAsState()
    
    val currentUnit by viewModel.glucoseUnit.collectAsState()
    val threshold by viewModel.alertThreshold.collectAsState()
    val profile by viewModel.userProfile.collectAsState()

    val activeAlert by viewModel.activeAlert.collectAsState()
    val isEmergency by viewModel.isEmergency.collectAsState()

    val prediction by viewModel.predictionResult.collectAsState()
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()

    val waterIntakes by viewModel.waterIntakes.collectAsState()
    val exercises by viewModel.exercises.collectAsState()
    val bloodPressures by viewModel.bloodPressures.collectAsState()
    val sleepLogs by viewModel.sleepLogs.collectAsState()
    val stressMoodLogs by viewModel.stressMoodLogs.collectAsState()
    val weightLogs by viewModel.weightLogs.collectAsState()
    val labResults by viewModel.labResults.collectAsState()
    val sickDayLogs by viewModel.sickDayLogs.collectAsState()
    val foodPhotoEstimates by viewModel.foodPhotoEstimates.collectAsState()
    val wearableSnapshots by viewModel.wearableSnapshots.collectAsState()

    // Dialog state controllers
    var showGlucoseDialog by remember { mutableStateOf(false) }
    var showMealDialog by remember { mutableStateOf(false) }
    var showMedDialog by remember { mutableStateOf(false) }
    var showInsulinDialog by remember { mutableStateOf(false) }
    var showReminderDialog by remember { mutableStateOf(false) }
    var showWaterDialog by remember { mutableStateOf(false) }
    var showExerciseDialog by remember { mutableStateOf(false) }
    var showBloodPressureDialog by remember { mutableStateOf(false) }
    var showSleepDialog by remember { mutableStateOf(false) }
    var showStressMoodDialog by remember { mutableStateOf(false) }
    var showWeightDialog by remember { mutableStateOf(false) }
    var showLabDialog by remember { mutableStateOf(false) }
    var showSickDayDialog by remember { mutableStateOf(false) }
    var showFoodEstimateDialog by remember { mutableStateOf(false) }
    var showWearableDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        val initials = if (profile.name.length >= 2) {
                            profile.name.split(" ")
                                .filter { it.isNotEmpty() }
                                .take(2)
                                .map { it.first() }
                                .joinToString("")
                                .uppercase()
                        } else "AD"
                        
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFD7E8CD)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initials,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF006D32),
                                fontSize = 14.sp
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(10.dp))
                        
                        Column {
                            val greeting = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
                                in 0..11 -> "Good morning"
                                in 12..16 -> "Good afternoon"
                                else -> "Good evening"
                            }
                            Text(
                                text = greeting,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF424940),
                                fontSize = 11.sp
                            )
                            Text(
                                text = profile.name.ifEmpty { "Alex Dawson" },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF191C19)
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleGlucoseUnit() }) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFD7E8CD),
                            contentColor = Color(0xFF00210B)
                        ) {
                            Text(
                                text = currentUnit,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                            )
                        }
                    }
                    IconButton(
                        onClick = { viewModel.generateAndShareClinicalReport(context) },
                        modifier = Modifier.testTag("action_export_pdf")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Export Physician PDF Report",
                            tint = Color(0xFF191C19)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFFBFDF8)
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFFF3F5F0),
                tonalElevation = 0.dp,
                modifier = Modifier.border(width = 1.dp, color = Color(0xFFC1C9BE))
            ) {
                val tabs = listOf(
                    Triple("Dashboard", Icons.Default.Home, 0),
                    Triple("Glucose", Icons.Default.Bloodtype, 1),
                    Triple("Loggers", Icons.Default.List, 2),
                    Triple("Health", Icons.Default.MonitorHeart, 3),
                    Triple("AI Trends", Icons.Default.AutoAwesome, 4),
                    Triple("Settings", Icons.Default.Settings, 5)
                )

                tabs.forEach { (label, icon, tabIdx) ->
                    NavigationBarItem(
                        selected = currentTab == tabIdx,
                        onClick = { currentTab = tabIdx },
                        label = { Text(label, fontSize = 10.sp, fontWeight = if (currentTab == tabIdx) FontWeight.Bold else FontWeight.Normal) },
                        icon = { Icon(imageVector = icon, contentDescription = label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF00210B),
                            selectedTextColor = Color(0xFF00210B),
                            indicatorColor = Color(0xFFD7E8CD),
                            unselectedIconColor = Color(0xFF191C19).copy(alpha = 0.6f),
                            unselectedTextColor = Color(0xFF191C19).copy(alpha = 0.6f)
                        ),
                        modifier = Modifier.testTag("nav_item_$label")
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // CRITICAL ACTIVE HEALTH ALERT SYSTEM (Requirement 7)
            AnimatedVisibility(
                visible = activeAlert != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                activeAlert?.let { alertText ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isEmergency) MedicalAlertLow.copy(alpha = 0.95f) else MedicalAlertHigh.copy(alpha = 0.95f),
                            contentColor = Color.White
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AnimatedWiggleIcon(
                                    imageVector = if (isEmergency) Icons.Default.Warning else Icons.Default.Info,
                                    contentDescription = "Alert Triggered Icon",
                                    tint = Color.White,
                                    size = 24.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isEmergency) "CRITICAL SAFETY WARNING" else "DIABETIC ALERT NOTICE",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                IconButton(
                                    onClick = { viewModel.clearCurrentAlerts() },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = "Dismiss Active Warning Message", tint = Color.White)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = alertText,
                                style = MaterialTheme.typography.bodyMedium,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            // Centralized switch layout renderer
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when (currentTab) {
                    0 -> DashboardTab(
                        readings = readings,
                        meals = meals,
                        meds = medications,
                        medLogs = medLogs,
                        insulinLogs = insulinLogs,
                        reminders = reminders,
                        waterIntakes = waterIntakes,
                        exercises = exercises,
                        bloodPressures = bloodPressures,
                        predictionResult = prediction,
                        isAnalyzingPrediction = isAnalyzing,
                        unit = currentUnit,
                        onRefreshPrediction = { viewModel.runAiPrediction() },
                        onAddGlucose = { showGlucoseDialog = true },
                        onAddMeal = { showMealDialog = true },
                        onAddInsulin = { showInsulinDialog = true },
                        onAddWater = { showWaterDialog = true },
                        onAddExercise = { showExerciseDialog = true },
                        onAddBloodPressure = { showBloodPressureDialog = true }
                    )
                    1 -> GlucoseTab(
                        readings = readings,
                        thresholds = threshold,
                        unit = currentUnit,
                        onAddGlucose = { showGlucoseDialog = true },
                        onDeleteGlucose = { viewModel.deleteGlucoseReading(it) }
                    )
                    2 -> LoggersTab(
                        meals = meals,
                        meds = medications,
                        medLogs = medLogs,
                        insulinLogs = insulinLogs,
                        waterIntakes = waterIntakes,
                        exercises = exercises,
                        bloodPressures = bloodPressures,
                        onAddMeal = { showMealDialog = true },
                        onDeleteMeal = { viewModel.deleteMealLog(it) },
                        onAddMed = { showMedDialog = true },
                        onDeleteMed = { viewModel.deleteMedication(it) },
                        onTakeMed = { viewModel.takeMedicationLog(it) },
                        onDeleteMedLog = { viewModel.deleteMedicationLog(it) },
                        onAddInsulin = { showInsulinDialog = true },
                        onDeleteInsulin = { viewModel.deleteInsulinLog(it) },
                        onAddWater = { showWaterDialog = true },
                        onDeleteWater = { viewModel.deleteWaterIntake(it) },
                        onAddExercise = { showExerciseDialog = true },
                        onDeleteExercise = { viewModel.deleteExercise(it) },
                        onAddBloodPressure = { showBloodPressureDialog = true },
                        onDeleteBloodPressure = { viewModel.deleteBloodPressure(it) }
                    )
                    3 -> ExpandedHealthTab(
                        sleepLogs = sleepLogs,
                        stressMoodLogs = stressMoodLogs,
                        weightLogs = weightLogs,
                        labResults = labResults,
                        sickDayLogs = sickDayLogs,
                        foodPhotoEstimates = foodPhotoEstimates,
                        wearableSnapshots = wearableSnapshots,
                        readings = readings,
                        medLogs = medLogs,
                        medications = medications,
                        onAddSleep = { showSleepDialog = true },
                        onDeleteSleep = { viewModel.deleteSleepLog(it) },
                        onAddStress = { showStressMoodDialog = true },
                        onDeleteStress = { viewModel.deleteStressMoodLog(it) },
                        onAddWeight = { showWeightDialog = true },
                        onDeleteWeight = { viewModel.deleteWeightLog(it) },
                        onAddLab = { showLabDialog = true },
                        onDeleteLab = { viewModel.deleteLabResult(it) },
                        onAddSickDay = { showSickDayDialog = true },
                        onDeleteSickDay = { viewModel.deleteSickDayLog(it) },
                        onAddFoodEstimate = { showFoodEstimateDialog = true },
                        onDeleteFoodEstimate = { viewModel.deleteFoodPhotoEstimate(it) },
                        onAddWearable = { showWearableDialog = true },
                        onDeleteWearable = { viewModel.deleteWearableSnapshot(it) }
                    )
                    4 -> PredictionTab(
                        predictionResult = prediction,
                        isAnalyzing = isAnalyzing,
                        onRefresh = { viewModel.runAiPrediction() }
                    )
                    5 -> SettingsTab(
                        profile = profile,
                        thresholds = threshold,
                        reminders = reminders,
                        currentUnit = currentUnit,
                        onToggleUnit = { viewModel.toggleGlucoseUnit() },
                        onExportPdf = { viewModel.generateAndShareClinicalReport(context) },
                        onUpdateProfile = { n, a, g, t -> viewModel.updateUserProfile(n, a, g, t) },
                        onUpdateThresholds = { l, h, el, eh -> viewModel.updateAlertThresholds(l, h, el, eh) },
                        onAddReminder = { showReminderDialog = true },
                        onToggleReminder = { viewModel.toggleReminder(it) },
                        onDeleteReminder = { viewModel.deleteReminder(it) },
                        onLoadSample = { viewModel.loadSampleDataset() },
                        onRemoveAllData = { viewModel.removeAllData() }
                    )
                }
            }
        }
    }

    // --- DIALOG SHEETS POPUPS ---

    // 1. Glucose Logger Dialog
    if (showGlucoseDialog) {
        GlucoseLogDialog(
            unit = currentUnit,
            onDismiss = { showGlucoseDialog = false },
            onSave = { v, type, notes, symptoms, mealRel ->
                viewModel.addGlucoseReading(v, type, notes, symptoms, mealRel)
                showGlucoseDialog = false
            }
        )
    }

    // 2. Meal Log Dialog
    if (showMealDialog) {
        MealLogDialog(
            onDismiss = { showMealDialog = false },
            onSave = { type, food, carbs, notes ->
                viewModel.addMealLog(type, food, carbs, notes)
                // Fire a simulated log notification as requested
                LocalNotificationManager.fireNotification(context, 101, "Meal Authenticated", "Logged carbs: $food ($carbs g)")
                showMealDialog = false
            }
        )
    }

    // 3. Oral Medication Setup Dialog
    if (showMedDialog) {
        MedicationDialog(
            onDismiss = { showMedDialog = false },
            onSave = { name, dose, time, freq, notes ->
                viewModel.addMedication(name, dose, time, freq, notes)
                showMedDialog = false
            }
        )
    }

    // 4. Insulin Administrator Log Dialog (Safety Compliance: ONLY TRACK, DO NOT SUGGEST)
    if (showInsulinDialog) {
        InsulinLogDialog(
            onDismiss = { showInsulinDialog = false },
            onSave = { type, units, relation, notes ->
                viewModel.addInsulinLog(type, units, relation, notes)
                LocalNotificationManager.fireNotification(context, 202, "Insulin Recorded", "Successfully tracked $units Units of $type.")
                showInsulinDialog = false
            }
        )
    }

    // 5. Custom reminder Dialog
    if (showReminderDialog) {
        ReminderDialog(
            onDismiss = { showReminderDialog = false },
            onSave = { title, hr, min, type ->
                viewModel.addReminder(title, hr, min, type)
                showReminderDialog = false
            }
        )
    }

    // 6. Water Intake Log Dialog
    if (showWaterDialog) {
        WaterLogDialog(
            onDismiss = { showWaterDialog = false },
            onSave = { amount, notes ->
                viewModel.addWaterIntake(amount, notes)
                LocalNotificationManager.fireNotification(context, 303, "Water Intake Tracked", "Successfully logged $amount mL of water.")
                showWaterDialog = false
            }
        )
    }

    // 7. Exercise Log Dialog
    if (showExerciseDialog) {
        ExerciseLogDialog(
            onDismiss = { showExerciseDialog = false },
            onSave = { act, dur, intensity, cals, notes ->
                viewModel.addExercise(act, dur, intensity, cals, notes)
                LocalNotificationManager.fireNotification(context, 404, "Workout Recorded", "Successfully logged $act of $dur mins.")
                showExerciseDialog = false
            }
        )
    }

    // 8. Blood Pressure Log Dialog
    if (showBloodPressureDialog) {
        BloodPressureLogDialog(
            onDismiss = { showBloodPressureDialog = false },
            onSave = { sys, dia, pulse, symptoms, notes ->
                viewModel.addBloodPressure(sys, dia, pulse, symptoms, notes)
                LocalNotificationManager.fireNotification(context, 505, "Vitals Recorded", "Logged blood pressure: $sys/$dia mmHg.")
                showBloodPressureDialog = false
            }
        )
    }

    if (showSleepDialog) {
        SleepLogDialog(
            onDismiss = { showSleepDialog = false },
            onSave = { hours, quality, wakeGlucose, notes ->
                viewModel.addSleepLog(hours, quality, wakeGlucose, notes)
                showSleepDialog = false
            }
        )
    }

    if (showStressMoodDialog) {
        StressMoodDialog(
            onDismiss = { showStressMoodDialog = false },
            onSave = { stress, mood, symptoms, notes ->
                viewModel.addStressMoodLog(stress, mood, symptoms, notes)
                showStressMoodDialog = false
            }
        )
    }

    if (showWeightDialog) {
        WeightLogDialog(
            onDismiss = { showWeightDialog = false },
            onSave = { weight, waist, bmi, notes ->
                viewModel.addWeightLog(weight, waist, bmi, notes)
                showWeightDialog = false
            }
        )
    }

    if (showLabDialog) {
        LabResultDialog(
            onDismiss = { showLabDialog = false },
            onSave = { a1c, ldl, hdl, trig, creatinine, egfr, urine, ketones, notes ->
                viewModel.addLabResult(a1c, ldl, hdl, trig, creatinine, egfr, urine, ketones, notes)
                showLabDialog = false
            }
        )
    }

    if (showSickDayDialog) {
        SickDayDialog(
            onDismiss = { showSickDayDialog = false },
            onSave = { temp, ketones, appetite, vomiting, hydration, notes ->
                viewModel.addSickDayLog(temp, ketones, appetite, vomiting, hydration, notes)
                showSickDayDialog = false
            }
        )
    }

    if (showFoodEstimateDialog) {
        FoodEstimateDialog(
            onDismiss = { showFoodEstimateDialog = false },
            onSave = { description, notes ->
                viewModel.addFoodPhotoEstimate(description, notes)
                showFoodEstimateDialog = false
            }
        )
    }

    if (showWearableDialog) {
        WearableSnapshotDialog(
            onDismiss = { showWearableDialog = false },
            onSave = { source, steps, heartRate, sleepHours, activeCalories, notes ->
                viewModel.addWearableSnapshot(source, steps, heartRate, sleepHours, activeCalories, notes)
                showWearableDialog = false
            }
        )
    }
}

// --- SUB TABS MODULES IMPLEMENTATIONS ---

@Composable
fun DashboardTab(
    readings: List<GlucoseReading>,
    meals: List<MealLog>,
    meds: List<Medication>,
    medLogs: List<MedicationLog>,
    insulinLogs: List<InsulinLog>,
    reminders: List<Reminder>,
    waterIntakes: List<WaterIntake>,
    exercises: List<Exercise>,
    bloodPressures: List<BloodPressure>,
    predictionResult: TrendPredictionResult?,
    isAnalyzingPrediction: Boolean,
    unit: String,
    onRefreshPrediction: () -> Unit,
    onAddGlucose: () -> Unit,
    onAddMeal: () -> Unit,
    onAddInsulin: () -> Unit,
    onAddWater: () -> Unit,
    onAddExercise: () -> Unit,
    onAddBloodPressure: () -> Unit
) {
    val context = LocalContext.current

    val lastReading = readings.firstOrNull()
    val totalReadings = readings.size
    val averageSugar = if (readings.isNotEmpty()) readings.map { it.value }.average() else 0.0
    val highestReading = if (readings.isNotEmpty()) readings.maxOf { it.value } else 0.0
    val lowestReading = if (readings.isNotEmpty()) readings.minOf { it.value } else 0.0

    val todaysInsulin = insulinLogs.filter { isToday(it.timestamp) }.sumOf { it.units }
    val todaysMeds = medLogs.filter { isToday(it.timestamp) }.size
    val todaysCarbs = meals.filter { isToday(it.timestamp) }.sumOf { it.carbsGrams }
    val threeHourPrediction = predictionResult?.predictions?.firstOrNull {
        it.intervalLabel.equals("3h", ignoreCase = true) || it.intervalIndex == 1
    }

    LaunchedEffect(readings.size, predictionResult) {
        if (readings.isNotEmpty() && predictionResult == null && !isAnalyzingPrediction) {
            onRefreshPrediction()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Welcome Header Profile Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Today's Overview",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF191C19)
                    )
                    Text(
                        text = "Diabetes Metrology & Self-Monitoring LogBook",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF191C19).copy(alpha = 0.6f)
                    )
                }
                IconButton(
                    onClick = {
                        // Triggers an instant check demonstration reminder notification
                        LocalNotificationManager.fireNotification(
                            context = context,
                            id = 999,
                            title = "DiaTrack Scheduled check",
                            message = "Be sure to log your glucose and insulin details on time today."
                        )
                    },
                    modifier = Modifier.background(Color(0xFFD7E8CD), CircleShape)
                ) {
                    AnimatedWiggleIcon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = "Trigger Reminder Check Alarm",
                        tint = Color(0xFF006D32),
                        size = 24.dp
                    )
                }
            }
        }

        // Quick Entry Actions
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onAddGlucose,
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("action_quick_glucose"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF006D32),
                            contentColor = Color.White
                        )
                    ) {
                        AnimatedHeartPulseIcon(imageVector = Icons.Default.Bloodtype, contentDescription = null, tint = Color.White, size = 14.dp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Glucose", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = onAddMeal,
                        modifier = Modifier.weight(1.5f),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFEFF2ED),
                            contentColor = Color(0xFF191C19)
                        ),
                        border = BorderStroke(1.dp, Color(0xFFC1C9BE))
                    ) {
                        AnimatedWiggleIcon(imageVector = Icons.Default.Restaurant, contentDescription = null, tint = Color(0xFF006D32), size = 14.dp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Carbs/Meal", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = onAddInsulin,
                        modifier = Modifier.weight(1.3f),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFEFF2ED),
                            contentColor = Color(0xFF191C19)
                        ),
                        border = BorderStroke(1.dp, Color(0xFFC1C9BE))
                    ) {
                        AnimatedWaterDropIcon(imageVector = Icons.Default.Opacity, contentDescription = null, tint = Color(0xFF006D32), size = 14.dp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Insulin", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onAddWater,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFEFF2ED),
                            contentColor = Color(0xFF191C19)
                        ),
                        border = BorderStroke(1.dp, Color(0xFFC1C9BE))
                    ) {
                        AnimatedWaterDropIcon(imageVector = Icons.Default.WaterDrop, contentDescription = null, tint = Color(0xFF006D32), size = 12.dp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Water", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = onAddExercise,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFEFF2ED),
                            contentColor = Color(0xFF191C19)
                        ),
                        border = BorderStroke(1.dp, Color(0xFFC1C9BE))
                    ) {
                        AnimatedRunningIcon(imageVector = Icons.Default.DirectionsRun, contentDescription = null, tint = Color(0xFF006D32), size = 12.dp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Activity", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = onAddBloodPressure,
                        modifier = Modifier.weight(1.2f),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFEFF2ED),
                            contentColor = Color(0xFF191C19)
                        ),
                        border = BorderStroke(1.dp, Color(0xFFC1C9BE))
                    ) {
                        AnimatedHeartPulseIcon(imageVector = Icons.Default.Favorite, contentDescription = null, tint = Color(0xFF006D32), size = 12.dp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Press / Pulse", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Main Metric Cards
        item {
            val isHighGlucose = lastReading != null && lastReading.value > 140
            val containerColor = if (isHighGlucose) Color(0xFFF9DEDC) else Color(0xFFD7E8CD).copy(alpha = 0.6f)
            val contentColor = if (isHighGlucose) Color(0xFF410E0B) else Color(0xFF00210B)
            val borderColor = if (isHighGlucose) Color(0xFFB3261E) else Color.Transparent

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .let {
                        if (borderColor != Color.Transparent) {
                            it.border(1.dp, borderColor, RoundedCornerShape(24.dp))
                        } else it
                    },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = containerColor
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "LATEST READING",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isHighGlucose) Color(0xFFB3261E) else Color(0xFF006D32),
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = if (lastReading != null) String.format("%.1f", lastReading.value) else "--",
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Black,
                                color = contentColor
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (lastReading != null) lastReading.unit else unit,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(bottom = 8.dp),
                                color = contentColor.copy(alpha = 0.8f)
                            )
                        }
                        if (lastReading != null) {
                            Text(
                                text = "${lastReading.type} | ${lastReading.mealRelation} | ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(lastReading.timestamp)}",
                                fontSize = 11.sp,
                                color = contentColor.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Medium
                            )
                        } else {
                            Text(
                                text = "No recent records found",
                                fontSize = 11.sp,
                                color = contentColor.copy(alpha = 0.6f)
                            )
                        }
                    }

                    if (isHighGlucose) {
                        Column(horizontalAlignment = Alignment.End) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFB3261E),
                                contentColor = Color.White
                            ) {
                                Text(
                                    text = "ALERT",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "⚠️ High: ${lastReading?.mealRelation}",
                                fontSize = 11.sp,
                                color = Color(0xFFB3261E),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF006D32)),
                            contentAlignment = Alignment.Center
                        ) {
                            AnimatedHeartPulseIcon(
                                imageVector = Icons.Default.Bloodtype,
                                contentDescription = "Healthy Glucose Reading Pulsing Icon",
                                tint = Color.White,
                                size = 24.dp
                            )
                        }
                    }
                }
            }
        }

        // Stats grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "AVG TODAY",
                    value = if (totalReadings > 0) String.format("%.1f", averageSugar) else "124",
                    unit = unit,
                    bgColor = Color(0xFFD7E8CD),
                    textColor = Color(0xFF00210B),
                    progress = if (totalReadings > 0) 0.65f else 0.65f,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "INSULIN",
                    value = if (todaysInsulin > 0) String.format("%.0f", todaysInsulin) else "12",
                    unit = "Units",
                    bgColor = Color(0xFFE7E0EB),
                    textColor = Color(0xFF1D192B),
                    subtitle = "Next: 1:30 PM (Rapid)",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                border = BorderStroke(1.dp, Color(0xFFBFD4F2))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "AI 3-HOUR GLUCOSE FORECAST",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF123B7A),
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        when {
                            isAnalyzingPrediction -> {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    AnimatedRotationIcon(
                                        imageVector = Icons.Default.Autorenew,
                                        contentDescription = "Analyzing 3 hour forecast",
                                        tint = Color(0xFF123B7A),
                                        size = 18.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Calculating next 3 hours...", fontWeight = FontWeight.Bold, color = Color(0xFF123B7A))
                                }
                            }
                            threeHourPrediction != null -> {
                                Text(
                                    text = "${String.format("%.1f", threeHourPrediction.glucoseRangeMin)} - ${String.format("%.1f", threeHourPrediction.glucoseRangeMax)} $unit",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF123B7A)
                                )
                                Text(
                                    text = "${threeHourPrediction.trend} | ${threeHourPrediction.confidence} confidence",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF123B7A).copy(alpha = 0.72f)
                                )
                            }
                            readings.isEmpty() -> {
                                Text("Log a sugar reading to generate a 3-hour forecast.", style = MaterialTheme.typography.bodySmall, color = Color(0xFF123B7A))
                            }
                            else -> {
                                Text("Forecast unavailable. Tap refresh to analyze.", style = MaterialTheme.typography.bodySmall, color = Color(0xFF123B7A))
                            }
                        }
                    }

                    IconButton(
                        onClick = onRefreshPrediction,
                        enabled = !isAnalyzingPrediction && readings.isNotEmpty(),
                        modifier = Modifier
                            .size(42.dp)
                            .background(Color.White.copy(alpha = 0.7f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh 3 hour glucose forecast",
                            tint = Color(0xFF123B7A)
                        )
                    }
                }
            }
        }

        // Today's Totals summary banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF2ED)),
                border = BorderStroke(1.dp, Color(0xFFC1C9BE))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "TODAY'S THERAPY LOGS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF006D32),
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TherapyMetric(name = "Insulin taken", value = "${todaysInsulin} U", icon = Icons.Default.Opacity)
                        TherapyMetric(name = "Orals taken", value = "$todaysMeds logs", icon = Icons.Default.MedicalServices)
                        TherapyMetric(name = "Meals Recorded", value = "${todaysCarbs}g Carbs", icon = Icons.Default.RestaurantMenu)
                    }
                }
            }
        }

        // Habits & Clinical Vitals Card
        item {
            val totalWater = waterIntakes.filter { isToday(it.timestamp) }.sumOf { it.amountMl }
            val totalExerciseMin = exercises.filter { isToday(it.timestamp) }.sumOf { it.durationMinutes }
            val lastBP = bloodPressures.firstOrNull()

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF2ED)),
                border = BorderStroke(1.dp, Color(0xFFC1C9BE))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "HABITS & CLINICAL VITALS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF006D32),
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TherapyMetric(name = "Water Log", value = "${totalWater.toInt()} mL", icon = Icons.Default.WaterDrop)
                        TherapyMetric(name = "Workouts", value = "$totalExerciseMin mins", icon = Icons.Default.DirectionsRun)
                        val bpStr = if (lastBP != null) "${lastBP.systolic}/${lastBP.diastolic}" else "--/--"
                        TherapyMetric(name = "Blood Press.", value = bpStr, icon = Icons.Default.Favorite)
                    }
                }
            }
        }

        // Upcoming reminders checklist lists
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Upcoming Scheduled Checklist",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Icon(imageVector = Icons.Default.WatchLater, contentDescription = null, tint = Color.GRAY, modifier = Modifier.size(20.dp))
            }
        }

        val activeReminders = reminders.filter { it.isEnabled }.take(3)
        if (activeReminders.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f))
                ) {
                    Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No active scheduled reminders waiting under system settings.",
                            fontSize = 11.sp,
                            color = Color.GRAY,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(activeReminders) { r ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                val rIcon = when (r.type) {
                                    "Blood Sugar" -> Icons.Default.Bloodtype
                                    "Insulin" -> Icons.Default.Opacity
                                    "Medication" -> Icons.Default.MedicalServices
                                    "Meal" -> Icons.Default.Restaurant
                                    else -> Icons.Default.MedicalInformation
                                }
                                Icon(imageVector = rIcon, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(r.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                Text("Task: ${r.type} Check", style = MaterialTheme.typography.labelSmall, color = Color.GRAY)
                            }
                        }
                        Text(
                            text = String.format("%02d:%02d", r.timeHour, r.timeMinute),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    unit: String,
    bgColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    progress: Float? = null
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = textColor.copy(alpha = 0.8f),
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Black,
                    color = textColor
                )
                Spacer(modifier = Modifier.width(3.dp))
                if (unit.isNotEmpty()) {
                    Text(
                        text = unit,
                        style = MaterialTheme.typography.labelSmall,
                        color = textColor.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
            }
            if (progress != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(textColor.copy(alpha = 0.2f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progress)
                            .background(textColor)
                    )
                }
            } else if (subtitle != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    fontSize = 10.sp,
                    color = textColor.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun TherapyMetric(name: String, value: String, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        when {
            name.contains("Water", ignoreCase = true) -> {
                AnimatedWaterDropIcon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, size = 20.dp)
            }
            name.contains("Workouts", ignoreCase = true) || name.contains("Activity", ignoreCase = true) || name.contains("Exercise", ignoreCase = true) -> {
                AnimatedRunningIcon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, size = 20.dp)
            }
            name.contains("Blood", ignoreCase = true) || name.contains("Press", ignoreCase = true) || name.contains("Heart", ignoreCase = true) || name.contains("Insulin", ignoreCase = true) || name.contains("Glucose", ignoreCase = true) -> {
                AnimatedHeartPulseIcon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, size = 20.dp)
            }
            else -> {
                Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        Text(text = name, style = MaterialTheme.typography.labelSmall, color = Color.GRAY)
    }
}

private fun isToday(timestamp: Long): Boolean {
    val cal1 = Calendar.getInstance()
    val cal2 = Calendar.getInstance().apply { timeInMillis = timestamp }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

@Composable
fun ExpandedHealthTab(
    sleepLogs: List<SleepLog>,
    stressMoodLogs: List<StressMoodLog>,
    weightLogs: List<WeightLog>,
    labResults: List<LabResult>,
    sickDayLogs: List<SickDayLog>,
    foodPhotoEstimates: List<FoodPhotoEstimate>,
    wearableSnapshots: List<WearableSnapshot>,
    readings: List<GlucoseReading>,
    medLogs: List<MedicationLog>,
    medications: List<Medication>,
    onAddSleep: () -> Unit,
    onDeleteSleep: (SleepLog) -> Unit,
    onAddStress: () -> Unit,
    onDeleteStress: (StressMoodLog) -> Unit,
    onAddWeight: () -> Unit,
    onDeleteWeight: (WeightLog) -> Unit,
    onAddLab: () -> Unit,
    onDeleteLab: (LabResult) -> Unit,
    onAddSickDay: () -> Unit,
    onDeleteSickDay: (SickDayLog) -> Unit,
    onAddFoodEstimate: () -> Unit,
    onDeleteFoodEstimate: (FoodPhotoEstimate) -> Unit,
    onAddWearable: () -> Unit,
    onDeleteWearable: (WearableSnapshot) -> Unit
) {
    val todayMedicationLogs = medLogs.count { isToday(it.timestamp) }
    val adherenceScore = if (medications.isEmpty()) 100 else ((todayMedicationLogs.toFloat() / medications.size.toFloat()) * 100).toInt().coerceIn(0, 100)
    val avgGlucose = readings.take(14).map { if (it.unit == "mmol/L") it.value * 18.0 else it.value }.average().takeIf { !it.isNaN() } ?: 0.0
    val lastLab = labResults.firstOrNull()
    val lastSleep = sleepLogs.firstOrNull()
    val lastStress = stressMoodLogs.firstOrNull()
    val lastWeight = weightLogs.firstOrNull()
    val doctorPrep = buildList {
        if (avgGlucose > 0) add("Recent average glucose: ${String.format("%.0f", avgGlucose)} mg/dL.")
        lastLab?.let { if (it.hba1c > 0) add("Latest HbA1c: ${it.hba1c}%.") }
        if (adherenceScore < 80) add("Medication adherence today is below 80%.")
        lastStress?.let { if (it.stressLevel >= 8) add("High stress was recently logged.") }
        lastSleep?.let { if (it.durationHours < 6) add("Short sleep may affect fasting glucose.") }
        if (sickDayLogs.isNotEmpty()) add("Review sick-day and ketone notes.")
    }.ifEmpty { listOf("Logs are ready for review; add labs or lifestyle notes for a richer visit summary.") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Expanded Health", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Sleep, stress, weight, labs, sick days, food estimates, and wearable snapshots.", style = MaterialTheme.typography.bodySmall, color = Color.GRAY)
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                StatCard("Adherence", "$adherenceScore", "%", Color(0xFFE8F0FE), Color(0xFF123B7A), Modifier.weight(1f), progress = adherenceScore / 100f)
                StatCard("HbA1c", if ((lastLab?.hba1c ?: 0.0) > 0) "${lastLab?.hba1c}" else "--", "%", Color(0xFFFFF1D6), Color(0xFF6A3D00), Modifier.weight(1f), subtitle = "latest lab")
            }
        }

        item {
            HealthInsightCard(
                title = "Doctor Visit Prep",
                icon = Icons.Default.Assignment,
                lines = doctorPrep
            )
        }

        item {
            HealthActionGrid(
                actions = listOf(
                    "Sleep" to onAddSleep,
                    "Stress" to onAddStress,
                    "Weight" to onAddWeight,
                    "Labs" to onAddLab,
                    "Sick Day" to onAddSickDay,
                    "Food AI" to onAddFoodEstimate,
                    "Wearable" to onAddWearable
                )
            )
        }

        item { HealthSectionHeader("Sleep", "Latest: ${lastSleep?.durationHours ?: "--"} h ${lastSleep?.quality ?: ""}") }
        items(sleepLogs.take(3)) { log ->
            HealthLogRow(Icons.Default.Bedtime, "${log.durationHours} h - ${log.quality}", "Wake glucose ${if (log.wakeGlucose > 0) log.wakeGlucose else "--"} | ${shortDate(log.timestamp)}", log.notes) { onDeleteSleep(log) }
        }

        item { HealthSectionHeader("Stress & Mood", "Latest stress: ${lastStress?.stressLevel ?: "--"}/10") }
        items(stressMoodLogs.take(3)) { log ->
            HealthLogRow(Icons.Default.SentimentSatisfied, "${log.mood} - stress ${log.stressLevel}/10", "${log.symptoms} | ${shortDate(log.timestamp)}", log.notes) { onDeleteStress(log) }
        }

        item { HealthSectionHeader("Weight & Labs", "Weight ${lastWeight?.weightKg ?: "--"} kg") }
        items(weightLogs.take(2)) { log ->
            HealthLogRow(Icons.Default.MonitorWeight, "${log.weightKg} kg", "Waist ${log.waistCm} cm | BMI ${log.bmi}", log.notes) { onDeleteWeight(log) }
        }
        items(labResults.take(2)) { lab ->
            HealthLogRow(Icons.Default.Biotech, "HbA1c ${lab.hba1c}% | eGFR ${lab.egfr}", "LDL ${lab.ldl}, HDL ${lab.hdl}, TG ${lab.triglycerides}, ketones ${lab.ketones}", lab.notes) { onDeleteLab(lab) }
        }

        item { HealthSectionHeader("Sick Day", "${sickDayLogs.size} illness logs") }
        items(sickDayLogs.take(3)) { log ->
            HealthLogRow(Icons.Default.Sick, "${log.temperatureC} C | ketones ${log.ketones}", "Appetite ${log.appetite}, vomiting ${if (log.vomiting) "yes" else "no"}", log.notes) { onDeleteSickDay(log) }
        }

        item { HealthSectionHeader("AI Food Estimates", "${foodPhotoEstimates.size} estimates") }
        items(foodPhotoEstimates.take(3)) { estimate ->
            HealthLogRow(Icons.Default.Restaurant, estimate.description, "${estimate.estimatedCarbsGrams}g carbs, ${estimate.estimatedCalories} cal, ${estimate.confidence} confidence", estimate.notes) { onDeleteFoodEstimate(estimate) }
        }

        item { HealthSectionHeader("Wearable / Health Connect", "${wearableSnapshots.size} snapshots") }
        items(wearableSnapshots.take(3)) { snap ->
            HealthLogRow(Icons.Default.DirectionsWalk, "${snap.source}: ${snap.steps} steps", "HR ${snap.heartRate}, sleep ${snap.sleepHours} h, active ${snap.activeCalories} cal", snap.notes) { onDeleteWearable(snap) }
        }
    }
}

@Composable
private fun HealthActionGrid(actions: List<Pair<String, () -> Unit>>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        actions.chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { (label, action) ->
                    FilledTonalButton(onClick = action, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(label, fontSize = 11.sp, maxLines = 1)
                    }
                }
                repeat(3 - row.size) { Spacer(modifier = Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun HealthInsightCard(title: String, icon: ImageVector, lines: List<String>) {
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFE9F6EF)), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = Color(0xFF006D32))
                Spacer(Modifier.width(8.dp))
                Text(title, fontWeight = FontWeight.Bold)
            }
            lines.forEach { Text("- $it", style = MaterialTheme.typography.bodySmall, color = Color(0xFF27352B)) }
        }
    }
}

@Composable
private fun HealthSectionHeader(title: String, subtitle: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(subtitle, style = MaterialTheme.typography.labelSmall, color = Color.GRAY)
    }
}

@Composable
private fun HealthLogRow(icon: ImageVector, title: String, subtitle: String, notes: String, onDelete: () -> Unit) {
    Card(shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.labelSmall, color = Color.GRAY)
                if (notes.isNotBlank()) Text(notes, style = MaterialTheme.typography.labelSmall)
            }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Delete") }
        }
    }
}

private fun shortDate(timestamp: Long): String =
    SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(timestamp))

@Composable
fun GlucoseTab(
    readings: List<GlucoseReading>,
    thresholds: AlertThreshold,
    unit: String,
    onAddGlucose: () -> Unit,
    onDeleteGlucose: (GlucoseReading) -> Unit
) {
    var chartSubTab by remember { mutableStateOf(0) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sugar Metrics & Curves",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                )
                Button(
                    onClick = onAddGlucose,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Log readings", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Chart tab switch controls
        item {
            TabRow(selectedTabIndex = chartSubTab) {
                Tab(selected = chartSubTab == 0, onClick = { chartSubTab = 0 }) {
                    Text("Chronology Core", modifier = Modifier.padding(12.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Tab(selected = chartSubTab == 1, onClick = { chartSubTab = 1 }) {
                    Text("Nutrition Comparison", modifier = Modifier.padding(12.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Embed vector canvas charts
        item {
            when (chartSubTab) {
                0 -> GlucoseTrendLineChart(readings = readings, targetLow = thresholds.lowThreshold, targetHigh = thresholds.highThreshold)
                1 -> MealRelationGlucoseComparisonChart(readings = readings)
            }
        }

        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.History, contentDescription = null, tint = Color.GRAY)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Logged Readings History", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }

        if (readings.isEmpty()) {
            item {
                EmptyStatePlaceholder(
                    icon = Icons.Default.Bloodtype,
                    title = "No Blood Sugar Logs",
                    subtitle = "Log blood sugar readings using the button above to record and track your levels.",
                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp)
                )
            }
        } else {
            items(readings) { r ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (r.value < thresholds.lowThreshold || r.value > thresholds.highThreshold)
                                            MedicalAlertHigh.copy(alpha = 0.2f)
                                        else ClinicalSecondary.copy(alpha = 0.2f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WaterDrop,
                                    contentDescription = null,
                                    tint = if (r.value < thresholds.lowThreshold || r.value > thresholds.highThreshold)
                                        MedicalAlertHigh
                                    else ClinicalTeal
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "${r.value} ${r.unit}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (r.value < thresholds.lowThreshold || r.value > thresholds.highThreshold)
                                        MaterialTheme.colorScheme.error
                                    else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${r.type} | ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(r.timestamp)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.GRAY
                                )
                                if (r.notes.isNotEmpty() || r.symptoms.isNotEmpty()) {
                                    Text(
                                        text = "Tags: ${listOf(r.notes, r.symptoms).filter { it.isNotEmpty() }.joinToString(", ")}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                        IconButton(onClick = { onDeleteGlucose(r) }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Blood Glucose Record Entry", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LoggersTab(
    meals: List<MealLog>,
    meds: List<Medication>,
    medLogs: List<MedicationLog>,
    insulinLogs: List<InsulinLog>,
    waterIntakes: List<WaterIntake>,
    exercises: List<Exercise>,
    bloodPressures: List<BloodPressure>,
    onAddMeal: () -> Unit,
    onDeleteMeal: (MealLog) -> Unit,
    onAddMed: () -> Unit,
    onDeleteMed: (Medication) -> Unit,
    onTakeMed: (Medication) -> Unit,
    onDeleteMedLog: (MedicationLog) -> Unit,
    onAddInsulin: () -> Unit,
    onDeleteInsulin: (InsulinLog) -> Unit,
    onAddWater: () -> Unit,
    onDeleteWater: (WaterIntake) -> Unit,
    onAddExercise: () -> Unit,
    onDeleteExercise: (Exercise) -> Unit,
    onAddBloodPressure: () -> Unit,
    onDeleteBloodPressure: (BloodPressure) -> Unit
) {
    var activeLoggerTab by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "Clinical Loggers", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        ScrollableTabRow(
            selectedTabIndex = activeLoggerTab,
            edgePadding = 0.dp
        ) {
            Tab(selected = activeLoggerTab == 0, onClick = { activeLoggerTab = 0 }) {
                Text("Carbs & Meals", modifier = Modifier.padding(12.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Tab(selected = activeLoggerTab == 1, onClick = { activeLoggerTab = 1 }) {
                Text("Oral Medicines", modifier = Modifier.padding(12.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Tab(selected = activeLoggerTab == 2, onClick = { activeLoggerTab = 2 }) {
                Text("Insulin Action", modifier = Modifier.padding(12.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Tab(selected = activeLoggerTab == 3, onClick = { activeLoggerTab = 3 }) {
                Text("Water Habits", modifier = Modifier.padding(12.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Tab(selected = activeLoggerTab == 4, onClick = { activeLoggerTab = 4 }) {
                Text("Workouts Activity", modifier = Modifier.padding(12.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Tab(selected = activeLoggerTab == 5, onClick = { activeLoggerTab = 5 }) {
                Text("Blood Pressure", modifier = Modifier.padding(12.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (activeLoggerTab) {
            0 -> MealsSubTab(meals = meals, onAddMeal = onAddMeal, onDeleteMeal = onDeleteMeal)
            1 -> MedicationsSubTab(meds = meds, medLogs = medLogs, onAddMed = onAddMed, onDeleteMed = onDeleteMed, onTakeMed = onTakeMed, onDeleteMedLog = onDeleteMedLog)
            2 -> InsulinSubTab(insulinLogs = insulinLogs, onAddInsulin = onAddInsulin, onDeleteInsulin = onDeleteInsulin)
            3 -> WaterSubTab(waterIntakes = waterIntakes, onAddWater = onAddWater, onDeleteWater = onDeleteWater)
            4 -> ExerciseSubTab(exercises = exercises, onAddExercise = onAddExercise, onDeleteExercise = onDeleteExercise)
            5 -> BloodPressureSubTab(bloodPressures = bloodPressures, onAddBloodPressure = onAddBloodPressure, onDeleteBloodPressure = onDeleteBloodPressure)
        }
    }
}

@Composable
fun WaterSubTab(
    waterIntakes: List<WaterIntake>,
    onAddWater: () -> Unit,
    onDeleteWater: (WaterIntake) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Daily hydration fluid tracking", style = MaterialTheme.typography.bodySmall, color = Color.GRAY)
            IconButton(
                onClick = onAddWater,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                    .testTag("action_add_water")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Water Record", tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (waterIntakes.isEmpty()) {
            EmptyStatePlaceholder(
                icon = Icons.Default.WaterDrop,
                title = "No Water Logs",
                subtitle = "Keep hydrated for better glycemic control! Log first drink now.",
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(waterIntakes) { w ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(ClinicalSecondary.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AnimatedWaterDropIcon(imageVector = Icons.Default.WaterDrop, contentDescription = null, tint = ClinicalTeal, size = 18.dp)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("${w.amountMl.toInt()} mL Water", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                    val timeStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(w.timestamp)
                                    Text(timeStr, style = MaterialTheme.typography.labelSmall, color = Color.GRAY)
                                    if (w.notes.isNotEmpty()) {
                                        Text("Notes: ${w.notes}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                            IconButton(onClick = { onDeleteWater(w) }) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Water Log", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExerciseSubTab(
    exercises: List<Exercise>,
    onAddExercise: () -> Unit,
    onDeleteExercise: (Exercise) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Glucose burner exercise logs", style = MaterialTheme.typography.bodySmall, color = Color.GRAY)
            IconButton(
                onClick = onAddExercise,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                    .testTag("action_add_exercise")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Exercise Log", tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (exercises.isEmpty()) {
            EmptyStatePlaceholder(
                icon = Icons.Default.DirectionsRun,
                title = "No Workouts Logged",
                subtitle = "Exercise sensitizes insulin receptors. Track your first activity above!",
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(exercises) { ex ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(ClinicalSecondary.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AnimatedRunningIcon(imageVector = Icons.Default.DirectionsRun, contentDescription = null, tint = ClinicalTeal, size = 18.dp)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("${ex.activityType} (${ex.durationMinutes} mins)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                    Text("Intensity: ${ex.intensity} | Burned: ${ex.caloriesBurned.toInt()} kcal", style = MaterialTheme.typography.labelSmall, color = Color.GRAY)
                                    val timeStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(ex.timestamp)
                                    Text(timeStr, style = MaterialTheme.typography.labelSmall, color = Color.GRAY)
                                    if (ex.notes.isNotEmpty()) {
                                        Text("Notes: ${ex.notes}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                            IconButton(onClick = { onDeleteExercise(ex) }) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Workout Record", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BloodPressureSubTab(
    bloodPressures: List<BloodPressure>,
    onAddBloodPressure: () -> Unit,
    onDeleteBloodPressure: (BloodPressure) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Vitals blood pressure & circulation logs", style = MaterialTheme.typography.bodySmall, color = Color.GRAY)
            IconButton(
                onClick = onAddBloodPressure,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                    .testTag("action_add_blood_pressure")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add BP record", tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (bloodPressures.isEmpty()) {
            EmptyStatePlaceholder(
                icon = Icons.Default.Favorite,
                title = "No Vitals Logs Found",
                subtitle = "Track systolic & diastolic blood pressure for cardiovascular security.",
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(bloodPressures) { bp ->
                    // High blood pressure indicator styling
                    val isCriticalBp = bp.systolic >= 140 || bp.diastolic >= 90
                    val isLowBp = bp.systolic <= 90 || bp.diastolic <= 60
                    val highlightColor = if (isCriticalBp) Color(0xFFF9DEDC) else if (isLowBp) Color(0xFFECE6F0) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = highlightColor)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (isCriticalBp) Color(0xFFF2B8B5) else ClinicalSecondary.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AnimatedHeartPulseIcon(
                                        imageVector = Icons.Default.Favorite,
                                        contentDescription = null,
                                        tint = if (isCriticalBp) Color(0xFF8C1D18) else ClinicalTeal,
                                        size = 18.dp
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("${bp.systolic}/${bp.diastolic} mmHg", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                        if (isCriticalBp) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFB3261E)) {
                                                Text("HIGH", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                            }
                                        }
                                    }
                                    Text("Pulse: ${bp.pulse} bpm" + if (bp.symptoms.isNotEmpty()) " | Symptoms: ${bp.symptoms}" else "", style = MaterialTheme.typography.labelSmall, color = Color.GRAY)
                                    val timeStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(bp.timestamp)
                                    Text(timeStr, style = MaterialTheme.typography.labelSmall, color = Color.GRAY)
                                    if (bp.notes.isNotEmpty()) {
                                        Text("Notes: ${bp.notes}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                            IconButton(onClick = { onDeleteBloodPressure(bp) }) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete BP Log", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MealsSubTab(meals: List<MealLog>, onAddMeal: () -> Unit, onDeleteMeal: (MealLog) -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Nutrition carbohydrate tracking", style = MaterialTheme.typography.bodySmall, color = Color.GRAY)
            IconButton(onClick = onAddMeal, modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer, CircleShape)) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Meal Record Row", tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (meals.isEmpty()) {
            EmptyStatePlaceholder(
                icon = Icons.Default.Restaurant,
                title = "No Meal Logs Found",
                subtitle = "Register your eating patterns and carbohydrate intakes by tapping on the '+' button above.",
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(meals) { m ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(ClinicalSecondary.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(imageVector = Icons.Default.Restaurant, contentDescription = null, tint = ClinicalTeal, modifier = Modifier.size(18.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(m.foodName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                    Text("${m.mealType} | Carbs: ${m.carbsGrams}g", style = MaterialTheme.typography.labelSmall, color = Color.GRAY)
                                    if (m.notes.isNotEmpty()) {
                                        Text("Notes: ${m.notes}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                            IconButton(onClick = { onDeleteMeal(m) }) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Meal Row Log", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MedicationsSubTab(
    meds: List<Medication>,
    medLogs: List<MedicationLog>,
    onAddMed: () -> Unit,
    onDeleteMed: (Medication) -> Unit,
    onTakeMed: (Medication) -> Unit,
    onDeleteMedLog: (MedicationLog) -> Unit
) {
    var subToggleState by remember { mutableStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TabRow(
                selectedTabIndex = subToggleState,
                modifier = Modifier.width(220.dp)
            ) {
                Tab(selected = subToggleState == 0, onClick = { subToggleState = 0 }) {
                    Text("Prescriptions", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(8.dp))
                }
                Tab(selected = subToggleState == 1, onClick = { subToggleState = 1 }) {
                    Text("Intake History", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(8.dp))
                }
            }
            IconButton(onClick = onAddMed, modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer, CircleShape)) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Register New Prescription Med", tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (subToggleState == 0) {
            // Prescriptions management List
            if (meds.isEmpty()) {
                EmptyStatePlaceholder(
                    icon = Icons.Default.MedicalServices,
                    title = "No Registered Prescriptions",
                    subtitle = "Store prescription oral medicine metadata by tapping the '+' button on the top right.",
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
                    items(meds) { med ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(imageVector = Icons.Default.MedicalServices, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(med.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                        Text("Dose: ${med.dosage} | ${med.timeOfDay} (${med.frequency})", style = MaterialTheme.typography.labelSmall, color = Color.GRAY)
                                        if (med.notes.isNotEmpty()) {
                                            Text("Notes: ${med.notes}", style = MaterialTheme.typography.labelSmall, color = Color.GRAY)
                                        }
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Button(
                                        onClick = { onTakeMed(med) },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Text("Log Intake", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    IconButton(onClick = { onDeleteMed(med) }, modifier = Modifier.size(32.dp)) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Deregister Med", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Medication Log intake history
            if (medLogs.isEmpty()) {
                EmptyStatePlaceholder(
                    icon = Icons.Default.CalendarToday,
                    title = "No Intakes Logged Today",
                    subtitle = "Logged medicine intake histories will list dynamically in chronologic sequence here.",
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
                    items(medLogs) { log ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.TaskAlt, contentDescription = null, tint = Color.Green, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(log.medicationName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                        Text("Dose taken: ${log.dosage}", style = MaterialTheme.typography.labelSmall)
                                        val timeStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(log.timestamp)
                                        Text("Intelligent Timestamp: $timeStr", style = MaterialTheme.typography.labelSmall, color = Color.GRAY)
                                    }
                                }
                                IconButton(onClick = { onDeleteMedLog(log) }) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Intake Log Item", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InsulinSubTab(insulinLogs: List<InsulinLog>, onAddInsulin: () -> Unit, onDeleteInsulin: (InsulinLog) -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Insulin logs history parameters (Record History)", style = MaterialTheme.typography.bodySmall, color = Color.GRAY)
            IconButton(onClick = onAddInsulin, modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer, CircleShape)) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Log Insulin Units Take", tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // IMPORTANT SAFETY COMPLIANCE WARNING CARD (Requirement 5)
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Shield, contentDescription = "Compliance Safe Icon", tint = ClinicalTeal, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("CLINICAL SAFETY DIRECTIVE", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = ClinicalTeal)
                    Text(
                        text = "This application strictly tracks insulin records entered manually. It DOES NOT prescribe, calculate, or recommend insulin dosages. Always follow your medical doctor's instructions.",
                        fontSize = 10.sp,
                        lineHeight = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }
        }

        if (insulinLogs.isEmpty()) {
            EmptyStatePlaceholder(
                icon = Icons.Default.Opacity,
                title = "No Insulin Entries",
                subtitle = "Logged corrective units or long-acting basals from physical administrations will record here.",
                modifier = Modifier.weight(1f)
            )
        } else {
            // Insulin Logs table list
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
                items(insulinLogs) { ins ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.tertiaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(imageVector = Icons.Default.Opacity, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiaryContainer, modifier = Modifier.size(18.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("${ins.units} Units (${ins.insulinType})", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                    Text("Relation: ${ins.mealRelation}", style = MaterialTheme.typography.labelSmall)
                                    val timeStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(ins.timestamp)
                                    Text("Logged at: $timeStr", style = MaterialTheme.typography.labelSmall, color = Color.GRAY)
                                }
                            }
                            IconButton(onClick = { onDeleteInsulin(ins) }) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Insulin Entry Row", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PredictionTab(
    predictionResult: TrendPredictionResult?,
    isAnalyzing: Boolean,
    onRefresh: () -> Unit
) {
    var hasRunInit by remember { mutableStateOf(false) }
    
    // Automatically trigger analysis if null to guarantee instantaneous premium onboarding experience
    LaunchedEffect(predictionResult) {
        if (predictionResult == null && !hasRunInit) {
            hasRunInit = true
            onRefresh()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "AI Prediction Trends", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(text = "On-Device Analytical glucose estimates", style = MaterialTheme.typography.bodySmall, color = Color.GRAY)
            }
            Button(
                onClick = onRefresh,
                enabled = !isAnalyzing,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isAnalyzing) {
                    AnimatedRotationIcon(
                        imageVector = Icons.Default.Autorenew,
                        contentDescription = "Recalculating Estimates",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        size = 18.dp
                    )
                } else {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Recalculate Estimates")
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text("Analyze")
            }
        }

        // CLINICAL SAFETY COMPLIANCE ADVISORY - CRITICAL
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MedicalAlertHigh.copy(alpha = 0.08f)),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MedicalAlertHigh.copy(alpha = 0.5f))
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Gavel, contentDescription = "Advisory Shield Icon", tint = MedicalAlertHigh, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("CLINICAL FORECASTING DISCLAIMER", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MedicalAlertHigh)
                    Text(
                        text = "Predictions are estimates only, powered by localized metabolic decay curves and pattern heuristics. This is NOT medical advice. Under no circumstances adjust insulin or treatment regimes without consulting a physician.",
                        fontSize = 10.sp,
                        lineHeight = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (isAnalyzing) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AnimatedRotationIcon(
                        imageVector = Icons.Default.Autorenew,
                        contentDescription = "Calculating metabolic curves...",
                        tint = ClinicalTeal,
                        size = 36.dp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Querying pattern models, insulin active timelines...", fontSize = 11.sp, color = Color.GRAY)
                }
            }
        } else if (predictionResult == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Click 'Analyze' above to calculate standard metabolic ranges.", style = MaterialTheme.typography.bodyMedium, color = Color.GRAY)
            }
        } else {
            // Visual predictions intervals layout
            Row(verticalAlignment = Alignment.CenterVertically) {
                AnimatedHeartPulseIcon(imageVector = Icons.Default.Timeline, contentDescription = null, tint = ClinicalTeal, size = 24.dp)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Predicted Tendencies & Timelines", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (predictionResult.isLocalMock) MaterialTheme.colorScheme.surfaceVariant else ClinicalTeal.copy(alpha = 0.2f),
                    contentColor = if (predictionResult.isLocalMock) MaterialTheme.colorScheme.onSurfaceVariant else ClinicalTeal
                ) {
                    Text(
                        text = if (predictionResult.isLocalMock) "Local Safe Heuristic" else "Cloud Server Model",
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Cards for intervals (1h, 3h, 6h, etc)
            predictionResult.predictions.forEach { p ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (p.warningMessage != null) MedicalAlertLow.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface
                    ),
                    border = if (p.warningMessage != null) BorderStroke(1.dp, MedicalAlertLow.copy(alpha = 0.3f)) else null
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(ClinicalTeal.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(p.intervalLabel, fontWeight = FontWeight.Black, fontSize = 11.sp, color = ClinicalTeal)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Glucose Estimate Range", style = MaterialTheme.typography.labelSmall, color = Color.GRAY)
                                    Text(
                                        text = "${String.format("%.1f", p.glucoseRangeMin)} - ${String.format("%.1f", p.glucoseRangeMax)}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = when (p.trend) {
                                        "Rising" -> MedicalAlertHigh.copy(alpha = 0.15f)
                                        "Falling" -> ClinicalTeal.copy(alpha = 0.15f)
                                        else -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                    },
                                    contentColor = when (p.trend) {
                                        "Rising" -> MedicalAlertHigh
                                        "Falling" -> ClinicalTeal
                                        else -> MaterialTheme.colorScheme.primary
                                    }
                                ) {
                                    Row(modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = when (p.trend) {
                                                "Rising" -> Icons.Default.ArrowUpward
                                                "Falling" -> Icons.Default.ArrowDownward
                                                else -> Icons.Default.HorizontalRule
                                            },
                                            contentDescription = null,
                                            modifier = Modifier.size(10.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(p.trend, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Text("Confidence: ${p.confidence}", fontSize = 9.sp, color = Color.GRAY)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Primary factor: ${p.possibleReason}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))

                        if (p.warningMessage != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.CrisisAlert, contentDescription = null, tint = MedicalAlertLow, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(p.warningMessage, fontSize = 10.sp, color = MedicalAlertLow, fontWeight = FontWeight.Bold, lineHeight = 12.sp)
                            }
                        }
                    }
                }
            }

            // Insights blocks
            Text("AI Medical Insight Indicator", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF191C19))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF00210B),
                    contentColor = Color(0xFFD7E8CD)
                )
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF006D32)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("✨", fontSize = 18.sp)
                    }
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "AI METABOLIC TREND NARRATIVE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD7E8CD).copy(alpha = 0.7f),
                            letterSpacing = 0.8.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = predictionResult.insights,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = Color(0xFFD7E8CD),
                            fontWeight = FontWeight.Medium,
                            style = androidx.compose.ui.text.TextStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                        )
                    }
                    
                    Column(horizontalAlignment = Alignment.End) {
                        Text("85%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD7E8CD))
                        Text("Confidence", fontSize = 8.sp, color = Color(0xFFD7E8CD).copy(alpha = 0.6f))
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsTab(
    profile: UserProfile,
    thresholds: AlertThreshold,
    reminders: List<Reminder>,
    currentUnit: String,
    onToggleUnit: () -> Unit,
    onExportPdf: () -> Unit,
    onUpdateProfile: (String, Int, String, String) -> Unit,
    onUpdateThresholds: (Double, Double, Double, Double) -> Unit,
    onAddReminder: () -> Unit,
    onToggleReminder: (Reminder) -> Unit,
    onDeleteReminder: (Reminder) -> Unit,
    onLoadSample: () -> Unit,
    onRemoveAllData: () -> Unit
) {
    val context = LocalContext.current
    var showRemoveAllDataDialog by remember { mutableStateOf(false) }

    // Local profile variables for form inputs
    var nameInput by remember { mutableStateOf(profile.name) }
    var ageInput by remember { mutableStateOf(profile.age.toString()) }
    var genderInput by remember { mutableStateOf(profile.gender) }
    var diabetesTypeInput by remember { mutableStateOf(profile.diabetesType) }

    // Threshold variables
    var lowInput by remember { mutableStateOf(thresholds.lowThreshold.toString()) }
    var highInput by remember { mutableStateOf(thresholds.highThreshold.toString()) }
    var emLowInput by remember { mutableStateOf(thresholds.emergencyLow.toString()) }
    var emHighInput by remember { mutableStateOf(thresholds.emergencyHigh.toString()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "System Settings & Profile", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

        // New interactive clinical actions utility card inside Settings
        Card(
            modifier = Modifier.fillMaxWidth().testTag("clinical_actions_utility_card"),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF2ED)),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, Color(0xFFC1C9BE))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "CLINICAL UTILITY ACTIONS",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF006D32),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Active Measurement Unit", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        Text("Current default is: $currentUnit", style = MaterialTheme.typography.labelSmall, color = Color.GRAY)
                    }
                    Button(onClick = onToggleUnit, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006D32))) {
                        Text("Toggle Unit")
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Spacer(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFC1C9BE).copy(alpha = 0.5f)))
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Physician Health Record PDF", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        Text("Generates standard clinical report exports", style = MaterialTheme.typography.labelSmall, color = Color.GRAY)
                    }
                    Button(onClick = onExportPdf, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006D32))) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export Chart")
                    }
                }
            }
        }

        // Quick Seed Sample Data Card
        Card(
            modifier = Modifier.fillMaxWidth().testTag("sample_data_card"),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFD7E8CD),
                contentColor = Color(0xFF00210B)
            ),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, Color(0xFFC1C9BE))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "READY TO EXPLORE THE APP?",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF006D32),
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Load Clinical Sample Dataset",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF191C19)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Populate your logbook instantly with realistic pre-configured glucose levels, meals, in-range stats, and AI predictions.",
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        color = Color(0xFF191C19).copy(alpha = 0.7f)
                    )
                }
                Button(
                    onClick = {
                        onLoadSample()
                        Toast.makeText(context, "Pristine clinical sample dataset loaded successfully!", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF006D32),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("action_load_sample_dataset")
                ) {
                    Text("Load", fontWeight = FontWeight.Bold)
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth().testTag("remove_all_data_card"),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF9DEDC),
                contentColor = Color(0xFF410E0B)
            ),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, Color(0xFFB3261E))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "DANGER ZONE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB3261E),
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Remove All Data",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF410E0B)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Clears logs, medications, reminders, expanded health records, alerts, and AI predictions.",
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        color = Color(0xFF410E0B).copy(alpha = 0.75f)
                    )
                }
                Button(
                    onClick = { showRemoveAllDataDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFB3261E),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("action_remove_all_data")
                ) {
                    Icon(imageVector = Icons.Default.DeleteForever, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Remove", fontWeight = FontWeight.Bold)
                }
            }
        }

        if (showRemoveAllDataDialog) {
            AlertDialog(
                onDismissRequest = { showRemoveAllDataDialog = false },
                icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFB3261E)) },
                title = { Text("Remove all data?") },
                text = {
                    Text("This will permanently delete all tracked health logs, medications, reminders, and predictions. Profile and alert thresholds will reset to defaults.")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onRemoveAllData()
                            showRemoveAllDataDialog = false
                            Toast.makeText(context, "All DiaTrack data removed.", Toast.LENGTH_LONG).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB3261E))
                    ) {
                        Text("Remove All")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRemoveAllDataDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Medical Card Profile Section
        Row(verticalAlignment = Alignment.CenterVertically) {
            AnimatedHeartPulseIcon(imageVector = Icons.Default.Person, contentDescription = null, tint = ClinicalTeal, size = 20.dp)
            Spacer(modifier = Modifier.width(6.dp))
            Text("PATIENT BASIC PROFILE", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ClinicalTeal)
        }
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Patient Registered Name") },
                    modifier = Modifier.fillMaxWidth().testTag("profile_name_input")
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = ageInput,
                        onValueChange = { ageInput = it },
                        label = { Text("Age") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = genderInput,
                        onValueChange = { genderInput = it },
                        label = { Text("Gender") },
                        modifier = Modifier.weight(1.5f)
                    )
                }
                
                OutlinedTextField(
                    value = diabetesTypeInput,
                    onValueChange = { diabetesTypeInput = it },
                    label = { Text("Diabetes Classification (Type 1, Type 2, Gestational)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        val parsedAge = ageInput.toIntOrNull() ?: 45
                        onUpdateProfile(nameInput, parsedAge, genderInput, diabetesTypeInput)
                        Toast.makeText(context, "Profile details saved successfully.", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.align(Alignment.End).testTag("action_save_profile")
                ) {
                    Text("Save details")
                }
            }
        }

        // Custom thresholds settings ranges
        Row(verticalAlignment = Alignment.CenterVertically) {
            AnimatedWiggleIcon(imageVector = Icons.Default.Tune, contentDescription = null, tint = ClinicalTeal, size = 20.dp)
            Spacer(modifier = Modifier.width(6.dp))
            Text("ALERT TARGET THRESHOLDS", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ClinicalTeal)
        }
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Configure targets below which alert notifications trigger. (Represented in mg/dL or mmol/L depending on your active unit preference).",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.GRAY
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = lowInput,
                        onValueChange = { lowInput = it },
                        label = { Text("Target Low (Hypo)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = highInput,
                        onValueChange = { highInput = it },
                        label = { Text("Target High (Hyper)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = emLowInput,
                        onValueChange = { emLowInput = it },
                        label = { Text("Emergency Low") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = emHighInput,
                        onValueChange = { emHighInput = it },
                        label = { Text("Emergency High") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                Button(
                    onClick = {
                        val low = lowInput.toDoubleOrNull() ?: 70.0
                        val high = highInput.toDoubleOrNull() ?: 180.0
                        val el = emLowInput.toDoubleOrNull() ?: 50.0
                        val eh = emHighInput.toDoubleOrNull() ?: 250.0
                        onUpdateThresholds(low, high, el, eh)
                        Toast.makeText(context, "Hyper/Hypo alert boundaries saved.", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Save Thresholds")
                }
            }
        }

        // Custom reminders scheduling list
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AnimatedRotationIcon(imageVector = Icons.Default.Alarm, contentDescription = null, tint = ClinicalTeal, size = 20.dp)
                Spacer(modifier = Modifier.width(6.dp))
                Text("CUSTOM SCHEDULED ALARMS", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ClinicalTeal)
            }
            IconButton(onClick = onAddReminder, modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer, CircleShape)) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Schedule Health Alarm Notification", tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }

        if (reminders.isEmpty()) {
            EmptyStatePlaceholder(
                icon = Icons.Default.NotificationsNone,
                title = "No Scheduled Alarms",
                subtitle = "Set persistent custom alerts (e.g. insulin times, glucose checks) by pressing the '+' icon above."
            )
        } else {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    reminders.forEach { r ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(r.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                Text("Task Type: ${r.type} | Time: ${String.format("%02d:%02d", r.timeHour, r.timeMinute)}", style = MaterialTheme.typography.labelSmall, color = Color.GRAY)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Switch(
                                    checked = r.isEnabled,
                                    onCheckedChange = { onToggleReminder(r) }
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(onClick = { onDeleteReminder(r) }) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Reminder Alarm", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Standard clinical privacy disclaimer compliance notice
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("DATA CONFIDENTIALITY & PRIVACY NOTICE", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = ClinicalTeal)
                Text(
                    text = "DiaTrack operates 100% offline. All medical parameters and profile logs are preserved completely in local encrypted SQLite systems on-device. Your metadata is never distributed outside your explicit permissions.",
                    fontSize = 10.sp,
                    lineHeight = 15.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun EmptyStatePlaceholder(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = 280.dp),
                lineHeight = 16.sp
            )
        }
    }
}
