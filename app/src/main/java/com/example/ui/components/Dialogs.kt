package com.example.ui.components

import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.WatchLater
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlucoseLogDialog(
    unit: String,
    onDismiss: () -> Unit,
    onSave: (value: Double, type: String, notes: String, symptoms: String, mealRelation: String) -> Unit
) {
    var valueInput by remember { mutableStateOf("") }
    var notesInput by remember { mutableStateOf("") }
    var symptomsInput by remember { mutableStateOf("") }
    
    // Type selection dropdown simulation
    val types = listOf("Fasting", "Before Meal", "After Meal", "Bedtime", "Random")
    var selectedType by remember { mutableStateOf(types.first()) }
    var typeDropdownExpanded by remember { mutableStateOf(false) }

    // Meal relation selection dropdown simulation
    val mealRelations = listOf("Before Breakfast", "After Breakfast", "Before Lunch", "After Lunch", "Before Dinner", "After Dinner", "None")
    var selectedMealRelation by remember { mutableStateOf(mealRelations.last()) }
    var mealRelationDropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Blood Sugar reading", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = valueInput,
                    onValueChange = { valueInput = it },
                    label = { Text("Sugar Level value ($unit)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("add_glucose_input")
                )

                // Type selector
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Reading type category") },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.clickable { typeDropdownExpanded = !typeDropdownExpanded }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { typeDropdownExpanded = !typeDropdownExpanded }
                    )
                    DropdownMenu(
                        expanded = typeDropdownExpanded,
                        onDismissRequest = { typeDropdownExpanded = false }
                    ) {
                        types.forEach { t ->
                            DropdownMenuItem(
                                text = { Text(t) },
                                onClick = {
                                    selectedType = t
                                    typeDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Food relation selector
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedMealRelation,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Meal timing association") },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.clickable { mealRelationDropdownExpanded = !mealRelationDropdownExpanded }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { mealRelationDropdownExpanded = !mealRelationDropdownExpanded }
                    )
                    DropdownMenu(
                        expanded = mealRelationDropdownExpanded,
                        onDismissRequest = { mealRelationDropdownExpanded = false }
                    ) {
                        mealRelations.forEach { mr ->
                            DropdownMenuItem(
                                text = { Text(mr) },
                                onClick = {
                                    selectedMealRelation = mr
                                    mealRelationDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = symptomsInput,
                    onValueChange = { symptomsInput = it },
                    label = { Text("Symptoms (e.g., headache, shaky, none)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notesInput,
                    onValueChange = { notesInput = it },
                    label = { Text("Notes & metadata") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val doubleVal = valueInput.toDoubleOrNull() ?: 100.0
                    onSave(doubleVal, selectedType, notesInput, symptomsInput, selectedMealRelation)
                },
                modifier = Modifier.testTag("submit_glucose_button")
            ) {
                Text("Log reading")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealLogDialog(
    onDismiss: () -> Unit,
    onSave: (mealType: String, foodName: String, carbsGrams: Double, notes: String) -> Unit
) {
    var foodName by remember { mutableStateOf("") }
    var carbsInput by remember { mutableStateOf("") }
    var notesInput by remember { mutableStateOf("") }

    val mealTypes = listOf("Breakfast", "Lunch", "Dinner", "Snack")
    var selectedMealType by remember { mutableStateOf(mealTypes.first()) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log carbs & meals", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedMealType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Meal type category") },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.clickable { dropdownExpanded = !dropdownExpanded }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { dropdownExpanded = !dropdownExpanded }
                    )
                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false }
                    ) {
                        mealTypes.forEach { mt ->
                            DropdownMenuItem(
                                text = { Text(mt) },
                                onClick = {
                                    selectedMealType = mt
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = foodName,
                    onValueChange = { foodName = it },
                    label = { Text("Food description / Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = carbsInput,
                    onValueChange = { carbsInput = it },
                    label = { Text("Carbohydratesgrams (g)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notesInput,
                    onValueChange = { notesInput = it },
                    label = { Text("Nutrition notes") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val carbs = carbsInput.toDoubleOrNull() ?: 15.0
                    onSave(selectedMealType, foodName, carbs, notesInput)
                }
            ) {
                Text("Log carbs")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, dosage: String, timeOfDay: String, frequency: String, notes: String) -> Unit
) {
    var nameInput by remember { mutableStateOf("") }
    var dosageInput by remember { mutableStateOf("") }
    var timeInput by remember { mutableStateOf("08:00 AM") }
    var freqInput by remember { mutableStateOf("Daily") }
    var notesInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Register Prescription oral medicine", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Medicine name (e.g. Metformin)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = dosageInput,
                    onValueChange = { dosageInput = it },
                    label = { Text("Dosage description (e.g. 500mg, 1 tablet)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = timeInput,
                    onValueChange = { timeInput = it },
                    label = { Text("Standard scheduled Hour (e.g. Morning, Night)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = freqInput,
                    onValueChange = { freqInput = it },
                    label = { Text("Frequency scale (e.g. Every 12h, Daily)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notesInput,
                    onValueChange = { notesInput = it },
                    label = { Text("Special notes / instructions") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(nameInput, dosageInput, timeInput, freqInput, notesInput)
                }
            ) {
                Text("Register Medication")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsulinLogDialog(
    onDismiss: () -> Unit,
    onSave: (insulinType: String, units: Double, mealRelation: String, notes: String) -> Unit
) {
    var unitsInput by remember { mutableStateOf("") }
    var notesInput by remember { mutableStateOf("") }

    val insulinTypes = listOf("Rapid-acting", "Long-acting", "Ultra Rapid-acting", "Regular/Short-acting", "Intermediate-acting NPH")
    var selectedInsulinType by remember { mutableStateOf(insulinTypes.first()) }
    var insulinTypeExpanded by remember { mutableStateOf(false) }

    val mealsRelation = listOf("Before Breakfast", "Before Lunch", "Before Dinner", "Bedtime", "None")
    var selectedRelation by remember { mutableStateOf(mealsRelation.last()) }
    var relationExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Log Insulin Taken", fontWeight = FontWeight.Bold)
                Text("SAFETY DIRECTION: Records manually logged only.", style = MaterialTheme.typography.labelSmall, color = Color.Red)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedInsulinType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Insulin Action Type classification") },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.clickable { insulinTypeExpanded = !insulinTypeExpanded }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { insulinTypeExpanded = !insulinTypeExpanded }
                    )
                    DropdownMenu(
                        expanded = insulinTypeExpanded,
                        onDismissRequest = { insulinTypeExpanded = false }
                    ) {
                        insulinTypes.forEach { t ->
                            DropdownMenuItem(
                                text = { Text(t) },
                                onClick = {
                                    selectedInsulinType = t
                                    insulinTypeExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = unitsInput,
                    onValueChange = { unitsInput = it },
                    label = { Text("Insulin Units administered (U)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("add_insulin_input")
                )

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedRelation,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Timing relation association") },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.clickable { relationExpanded = !relationExpanded }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { relationExpanded = !relationExpanded }
                    )
                    DropdownMenu(
                        expanded = relationExpanded,
                        onDismissRequest = { relationExpanded = false }
                    ) {
                        mealsRelation.forEach { mr ->
                            DropdownMenuItem(
                                text = { Text(mr) },
                                onClick = {
                                    selectedRelation = mr
                                    relationExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = notesInput,
                    onValueChange = { notesInput = it },
                    label = { Text("Clinical Notes / site details") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Medical safety advisory checklist inside layout
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "Disclaimer: Under no circumstances does this module prescribe or suggest insulin dose sizes. Verify your correction ratios and physical targets with your physician.",
                        fontSize = 9.sp,
                        lineHeight = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val doubleUnits = unitsInput.toDoubleOrNull() ?: 2.0
                    onSave(selectedInsulinType, doubleUnits, selectedRelation, notesInput)
                },
                modifier = Modifier.testTag("submit_insulin_button")
            ) {
                Text("Record Units")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderDialog(
    onDismiss: () -> Unit,
    onSave: (title: String, hour: Int, minute: Int, type: String) -> Unit
) {
    var titleInput by remember { mutableStateOf("") }
    
    val reminderTypes = listOf("Blood Sugar", "Insulin", "Medication", "Meal", "Appointment")
    var selectedType by remember { mutableStateOf(reminderTypes.first()) }
    var typeExpanded by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    var selectedHour by remember { mutableStateOf(calendar.get(Calendar.HOUR_OF_DAY)) }
    var selectedMinute by remember { mutableStateOf(calendar.get(Calendar.MINUTE)) }

    val timePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            selectedHour = hourOfDay
            selectedMinute = minute
        },
        selectedHour,
        selectedMinute,
        true
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Schedule Health remind Alarm", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = titleInput,
                    onValueChange = { titleInput = it },
                    label = { Text("Alarm title label") },
                    modifier = Modifier.fillMaxWidth()
                )

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Reminder action Type") },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.clickable { typeExpanded = !typeExpanded }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { typeExpanded = !typeExpanded }
                    )
                    DropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false }
                    ) {
                        reminderTypes.forEach { rt ->
                            DropdownMenuItem(
                                text = { Text(rt) },
                                onClick = {
                                    selectedType = rt
                                    typeExpanded = false
                                }
                            )
                        }
                    }
                }

                // Time picker visual activation row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { timePickerDialog.show() }
                        .padding(vertical = 12.dp, horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.WatchLater, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Select time alarm details", fontWeight = FontWeight.Bold)
                    }
                    Text(
                        text = String.format("%02d:%02d", selectedHour, selectedMinute),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(titleInput.ifEmpty { "Clinical scheduled reminder" }, selectedHour, selectedMinute, selectedType)
                }
            ) {
                Text("Schedule Alarm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun WaterLogDialog(
    onDismiss: () -> Unit,
    onSave: (amountMl: Double, notes: String) -> Unit
) {
    var amountInput by remember { mutableStateOf("250") }
    var notesInput by remember { mutableStateOf("") }
    
    val quickAmounts = listOf("150", "250", "330", "500", "750")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Water Intake", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Select standard milliliter volume or type custom:", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    quickAmounts.forEach { amt ->
                        SuggestionChip(
                            onClick = { amountInput = amt },
                            label = { Text(amt) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = if (amountInput == amt) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it },
                    label = { Text("Water Volume (mL)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("add_water_input")
                )

                OutlinedTextField(
                    value = notesInput,
                    onValueChange = { notesInput = it },
                    label = { Text("Notes (e.g. cold water, after run)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val doubleVal = amountInput.toDoubleOrNull() ?: 250.0
                    onSave(doubleVal, notesInput)
                },
                modifier = Modifier.testTag("submit_water_button")
            ) {
                Text("Log Water")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun ExerciseLogDialog(
    onDismiss: () -> Unit,
    onSave: (activityType: String, durationMinutes: Int, intensity: String, caloriesBurned: Double, notes: String) -> Unit
) {
    var activityInput by remember { mutableStateOf("") }
    var durationInput by remember { mutableStateOf("30") }
    var caloriesInput by remember { mutableStateOf("150") }
    var notesInput by remember { mutableStateOf("") }

    val intensities = listOf("Low", "Moderate", "High")
    var selectedIntensity by remember { mutableStateOf(intensities[1]) }
    var intensityExpanded by remember { mutableStateOf(false) }

    val standardActivities = listOf("Walking", "Running", "Jogging", "Cycling", "Swimming", "Yoga", "Gym Gym")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Physical Activity", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = activityInput,
                    onValueChange = { activityInput = it },
                    label = { Text("Activity Type") },
                    placeholder = { Text("e.g. Walking, Hiking, Cycling...") },
                    modifier = Modifier.fillMaxWidth().testTag("add_exercise_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    standardActivities.take(4).forEach { act ->
                        SuggestionChip(
                            onClick = { activityInput = act },
                            label = { Text(act, fontSize = 10.sp) }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = durationInput,
                        onValueChange = { durationInput = it },
                        label = { Text("Duration (min)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = caloriesInput,
                        onValueChange = { caloriesInput = it },
                        label = { Text("Est. Calories") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Intensity selector
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedIntensity,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Workout Intensity") },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.clickable { intensityExpanded = !intensityExpanded }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { intensityExpanded = !intensityExpanded }
                    )
                    DropdownMenu(
                        expanded = intensityExpanded,
                        onDismissRequest = { intensityExpanded = false }
                    ) {
                        intensities.forEach { i ->
                            DropdownMenuItem(
                                text = { Text(i) },
                                onClick = {
                                    selectedIntensity = i
                                    intensityExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = notesInput,
                    onValueChange = { notesInput = it },
                    label = { Text("Workout comments or notes") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val duration = durationInput.toIntOrNull() ?: 30
                    val calories = caloriesInput.toDoubleOrNull() ?: (duration * 5.0)
                    onSave(activityInput.ifEmpty { "Workout" }, duration, selectedIntensity, calories, notesInput)
                },
                modifier = Modifier.testTag("submit_exercise_button")
            ) {
                Text("Log Workout")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun BloodPressureLogDialog(
    onDismiss: () -> Unit,
    onSave: (systolic: Int, diastolic: Int, pulse: Int, symptoms: String, notes: String) -> Unit
) {
    var systolicInput by remember { mutableStateOf("120") }
    var diastolicInput by remember { mutableStateOf("80") }
    var pulseInput by remember { mutableStateOf("72") }
    var symptomsInput by remember { mutableStateOf("") }
    var notesInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Blood Pressure Vitals", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = systolicInput,
                        onValueChange = { systolicInput = it },
                        label = { Text("Systolic (mmHg)") },
                        placeholder = { Text("e.g. 120") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("bp_systolic_input")
                    )

                    OutlinedTextField(
                        value = diastolicInput,
                        onValueChange = { diastolicInput = it },
                        label = { Text("Diastolic (mmHg)") },
                        placeholder = { Text("e.g. 80") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("bp_diastolic_input")
                    )
                }

                OutlinedTextField(
                    value = pulseInput,
                    onValueChange = { pulseInput = it },
                    label = { Text("Pulse / Heart Rate (bpm)") },
                    placeholder = { Text("e.g. 72") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = symptomsInput,
                    onValueChange = { symptomsInput = it },
                    label = { Text("Associated Symptoms") },
                    placeholder = { Text("none, dizzy, headache...") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notesInput,
                    onValueChange = { notesInput = it },
                    label = { Text("Measurement notes") },
                    placeholder = { Text("e.g. Sitting on chair, resting") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val sys = systolicInput.toIntOrNull() ?: 120
                    val dia = diastolicInput.toIntOrNull() ?: 80
                    val pulse = pulseInput.toIntOrNull() ?: 72
                    onSave(sys, dia, pulse, symptomsInput, notesInput)
                },
                modifier = Modifier.testTag("submit_bp_button")
            ) {
                Text("Log Vitals")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun SleepLogDialog(onDismiss: () -> Unit, onSave: (Double, String, Double, String) -> Unit) {
    var hours by remember { mutableStateOf("7.0") }
    var quality by remember { mutableStateOf("Good") }
    var wakeGlucose by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    val qualities = listOf("Poor", "Fair", "Good", "Excellent")
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Sleep", fontWeight = FontWeight.Bold) },
        text = { SimpleFormColumn {
            OutlinedTextField(hours, { hours = it }, label = { Text("Sleep duration (hours)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
            ChipRow(qualities, quality) { quality = it }
            OutlinedTextField(wakeGlucose, { wakeGlucose = it }, label = { Text("Wake glucose optional") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
            OutlinedTextField(notes, { notes = it }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth())
        } },
        confirmButton = { Button(onClick = { onSave(hours.toDoubleOrNull() ?: 7.0, quality, wakeGlucose.toDoubleOrNull() ?: 0.0, notes) }) { Text("Log Sleep") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun StressMoodDialog(onDismiss: () -> Unit, onSave: (Int, String, String, String) -> Unit) {
    var stress by remember { mutableStateOf("5") }
    var mood by remember { mutableStateOf("Steady") }
    var symptoms by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Stress & Mood", fontWeight = FontWeight.Bold) },
        text = { SimpleFormColumn {
            OutlinedTextField(stress, { stress = it }, label = { Text("Stress level 1-10") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
            ChipRow(listOf("Calm", "Steady", "Anxious", "Low", "Irritable"), mood) { mood = it }
            OutlinedTextField(symptoms, { symptoms = it }, label = { Text("Symptoms") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(notes, { notes = it }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth())
        } },
        confirmButton = { Button(onClick = { onSave((stress.toIntOrNull() ?: 5).coerceIn(1, 10), mood, symptoms, notes) }) { Text("Log Mood") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun WeightLogDialog(onDismiss: () -> Unit, onSave: (Double, Double, Double, String) -> Unit) {
    var weight by remember { mutableStateOf("") }
    var waist by remember { mutableStateOf("") }
    var bmi by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Weight", fontWeight = FontWeight.Bold) },
        text = { SimpleFormColumn {
            OutlinedTextField(weight, { weight = it }, label = { Text("Weight (kg)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
            OutlinedTextField(waist, { waist = it }, label = { Text("Waist (cm)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
            OutlinedTextField(bmi, { bmi = it }, label = { Text("BMI optional") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
            OutlinedTextField(notes, { notes = it }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth())
        } },
        confirmButton = { Button(onClick = { onSave(weight.toDoubleOrNull() ?: 0.0, waist.toDoubleOrNull() ?: 0.0, bmi.toDoubleOrNull() ?: 0.0, notes) }) { Text("Log Weight") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun LabResultDialog(onDismiss: () -> Unit, onSave: (Double, Double, Double, Double, Double, Double, Double, String, String) -> Unit) {
    var a1c by remember { mutableStateOf("") }
    var ldl by remember { mutableStateOf("") }
    var hdl by remember { mutableStateOf("") }
    var trig by remember { mutableStateOf("") }
    var creatinine by remember { mutableStateOf("") }
    var egfr by remember { mutableStateOf("") }
    var urine by remember { mutableStateOf("") }
    var ketones by remember { mutableStateOf("Negative") }
    var notes by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Labs", fontWeight = FontWeight.Bold) },
        text = { SimpleFormColumn {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(a1c, { a1c = it }, label = { Text("HbA1c %") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                OutlinedTextField(egfr, { egfr = it }, label = { Text("eGFR") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(ldl, { ldl = it }, label = { Text("LDL") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                OutlinedTextField(hdl, { hdl = it }, label = { Text("HDL") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
            }
            OutlinedTextField(trig, { trig = it }, label = { Text("Triglycerides") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
            OutlinedTextField(creatinine, { creatinine = it }, label = { Text("Creatinine") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
            OutlinedTextField(urine, { urine = it }, label = { Text("Urine albumin") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
            ChipRow(listOf("Negative", "Trace", "Small", "Moderate", "Large"), ketones) { ketones = it }
            OutlinedTextField(notes, { notes = it }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth())
        } },
        confirmButton = { Button(onClick = { onSave(a1c.toDoubleOrNull() ?: 0.0, ldl.toDoubleOrNull() ?: 0.0, hdl.toDoubleOrNull() ?: 0.0, trig.toDoubleOrNull() ?: 0.0, creatinine.toDoubleOrNull() ?: 0.0, egfr.toDoubleOrNull() ?: 0.0, urine.toDoubleOrNull() ?: 0.0, ketones, notes) }) { Text("Log Labs") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun SickDayDialog(onDismiss: () -> Unit, onSave: (Double, String, String, Boolean, Boolean, String) -> Unit) {
    var temp by remember { mutableStateOf("") }
    var ketones by remember { mutableStateOf("Negative") }
    var appetite by remember { mutableStateOf("Normal") }
    var vomiting by remember { mutableStateOf(false) }
    var hydration by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Sick Day", fontWeight = FontWeight.Bold) },
        text = { SimpleFormColumn {
            OutlinedTextField(temp, { temp = it }, label = { Text("Temperature C") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
            ChipRow(listOf("Negative", "Trace", "Small", "Moderate", "Large"), ketones) { ketones = it }
            ChipRow(listOf("Normal", "Low", "None"), appetite) { appetite = it }
            Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(vomiting, { vomiting = it }); Text("Vomiting") }
            Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(hydration, { hydration = it }); Text("Hydration concern") }
            OutlinedTextField(notes, { notes = it }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth())
        } },
        confirmButton = { Button(onClick = { onSave(temp.toDoubleOrNull() ?: 0.0, ketones, appetite, vomiting, hydration, notes) }) { Text("Log Sick Day") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun FoodEstimateDialog(onDismiss: () -> Unit, onSave: (String, String) -> Unit) {
    var description by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("AI Food Estimate", fontWeight = FontWeight.Bold) },
        text = { SimpleFormColumn {
            OutlinedTextField(description, { description = it }, label = { Text("Describe meal or photo") }, placeholder = { Text("rice, chicken curry, salad") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(notes, { notes = it }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth())
            Text("This estimates macros from text/photo notes. Verify portions before treatment decisions.", fontSize = 11.sp, color = Color.Gray)
        } },
        confirmButton = { Button(onClick = { onSave(description, notes) }) { Text("Estimate") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun WearableSnapshotDialog(onDismiss: () -> Unit, onSave: (String, Int, Int, Double, Double, String) -> Unit) {
    var source by remember { mutableStateOf("Health Connect") }
    var steps by remember { mutableStateOf("") }
    var heart by remember { mutableStateOf("") }
    var sleep by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Wearable Snapshot", fontWeight = FontWeight.Bold) },
        text = { SimpleFormColumn {
            OutlinedTextField(source, { source = it }, label = { Text("Source") }, modifier = Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(steps, { steps = it }, label = { Text("Steps") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                OutlinedTextField(heart, { heart = it }, label = { Text("Heart") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(sleep, { sleep = it }, label = { Text("Sleep h") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                OutlinedTextField(calories, { calories = it }, label = { Text("Active cal") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
            }
            OutlinedTextField(notes, { notes = it }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth())
        } },
        confirmButton = { Button(onClick = { onSave(source, steps.toIntOrNull() ?: 0, heart.toIntOrNull() ?: 0, sleep.toDoubleOrNull() ?: 0.0, calories.toDoubleOrNull() ?: 0.0, notes) }) { Text("Log Snapshot") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun SimpleFormColumn(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        content = content
    )
}

@Composable
private fun ChipRow(options: List<String>, selected: String, onSelect: (String) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        options.take(5).forEach {
            SuggestionChip(
                onClick = { onSelect(it) },
                label = { Text(it, fontSize = 10.sp) },
                colors = SuggestionChipDefaults.suggestionChipColors(
                    containerColor = if (selected == it) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                )
            )
        }
    }
}
