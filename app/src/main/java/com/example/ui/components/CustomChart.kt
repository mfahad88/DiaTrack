package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GlucoseReading
import com.example.data.InsulinLog
import com.example.ui.theme.ClinicalTeal
import com.example.ui.theme.ClinicalSecondary
import com.example.ui.theme.MedicalAlertLow
import com.example.ui.theme.MedicalAlertHigh
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max
import kotlin.math.min

@Composable
fun GlucoseTrendLineChart(
    readings: List<GlucoseReading>,
    targetLow: Double,
    targetHigh: Double,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface, fontSize = 9.sp)
    val gridLineColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Chronological Glucose History",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (readings.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timeline,
                            contentDescription = null,
                            tint = ClinicalTeal,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Awaiting Glucose Readings",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Log blood sugar entries to map a live metabolic trend curve here.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.navigationBarsPadding().padding(horizontal = 24.dp)
                    )
                }
            } else {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height

                    val leftPadding = 50f
                    val rightPadding = 30f
                    val topPadding = 30f
                    val bottomPadding = 40f

                    val chartWidth = canvasWidth - leftPadding - rightPadding
                    val chartHeight = canvasHeight - topPadding - bottomPadding

                    // Determine scale bounds (glucose scale min of 30, max of 300)
                    val maxVal = max(300.0, readings.maxOfOrNull { if (it.unit == "mmol/L") it.value * 18.0 else it.value } ?: 200.0)
                    val minVal = min(30.0, readings.minOfOrNull { if (it.unit == "mmol/L") it.value * 18.0 else it.value } ?: 50.0)
                    val valueRange = maxVal - minVal

                    // Draw grid-lines
                    val horizontalLines = 4
                    for (i in 0..horizontalLines) {
                        val fraction = i.toFloat() / horizontalLines
                        val y = topPadding + chartHeight * (1f - fraction)
                        drawLine(
                            color = gridLineColor,
                            start = Offset(leftPadding, y),
                            end = Offset(canvasWidth - rightPadding, y),
                            strokeWidth = 1f
                        )

                        // Glucose values labels
                        val valueLabel = (minVal + fraction * valueRange).toInt()
                        drawText(
                            textMeasurer = textMeasurer,
                            text = valueLabel.toString(),
                            topLeft = Offset(5f, y - 15f),
                            style = textStyle
                        )
                    }

                    // Normalize target thresholds to canvas coordinates
                    val highY = topPadding + chartHeight * (1f - ((targetHigh - minVal) / valueRange).toFloat())
                    val lowY = topPadding + chartHeight * (1f - ((targetLow - minVal) / valueRange).toFloat())

                    // Draw High/Low warning target lines
                    drawLine(
                        color = MedicalAlertHigh.copy(alpha = 0.5f),
                        start = Offset(leftPadding, highY),
                        end = Offset(canvasWidth - rightPadding, highY),
                        strokeWidth = 2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )
                    drawLine(
                        color = MedicalAlertLow.copy(alpha = 0.5f),
                        start = Offset(leftPadding, lowY),
                        end = Offset(canvasWidth - rightPadding, lowY),
                        strokeWidth = 2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )

                    // Draw recent points chronologically
                    val sortedReadings = readings.take(15).reversed()
                    if (sortedReadings.size > 1) {
                        val points = sortedReadings.mapIndexed { index, reading ->
                            val rx = leftPadding + (index.toFloat() / (sortedReadings.size - 1)) * chartWidth
                            val rawValue = if (reading.unit == "mmol/L") reading.value * 18.0 else reading.value
                            val ry = topPadding + chartHeight * (1f - ((rawValue - minVal) / valueRange).toFloat())
                            Offset(rx, ry)
                        }

                        // Draw path line
                        val path = Path().apply {
                            moveTo(points.first().x, points.first().y)
                            for (i in 1 until points.size) {
                                lineTo(points[i].x, points[i].y)
                            }
                        }
                        drawPath(
                            path = path,
                            color = ClinicalTeal,
                            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                        )

                        // Draw background ambient gradient area under the curve
                        val areaPath = Path().apply {
                            moveTo(points.first().x, points.first().y)
                            for (i in 1 until points.size) {
                                lineTo(points[i].x, points[i].y)
                            }
                            lineTo(points.last().x, topPadding + chartHeight)
                            lineTo(points.first().x, topPadding + chartHeight)
                            close()
                        }
                        drawPath(
                            path = areaPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(ClinicalTeal.copy(alpha = 0.3f), Color.Transparent),
                                startY = topPadding,
                                endY = topPadding + chartHeight
                            )
                        )

                        // Draw individual circular highlights
                        points.forEachIndexed { index, p ->
                            val item = sortedReadings[index]
                            val rawValue = if (item.unit == "mmol/L") item.value * 18.0 else item.value
                            val circleColor = when {
                                rawValue > targetHigh -> MedicalAlertHigh
                                rawValue < targetLow -> MedicalAlertLow
                                else -> ClinicalSecondary
                            }
                            drawCircle(
                                color = circleColor,
                                radius = 5.dp.toPx(),
                                center = p
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 2.dp.toPx(),
                                center = p
                            )
                        }

                        // Label first and last timestamps
                        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                        val firstTime = sdf.format(sortedReadings.first().timestamp)
                        val lastTime = sdf.format(sortedReadings.last().timestamp)

                        drawText(
                            textMeasurer = textMeasurer,
                            text = firstTime,
                            topLeft = Offset(leftPadding - 10f, canvasHeight - bottomPadding + 5f),
                            style = textStyle
                        )
                        drawText(
                            textMeasurer = textMeasurer,
                            text = lastTime,
                            topLeft = Offset(canvasWidth - rightPadding - 30f, canvasHeight - bottomPadding + 5f),
                            style = textStyle
                        )
                    } else if (sortedReadings.size == 1) {
                        // Just one point
                        val rx = leftPadding + chartWidth / 2f
                        val rawValue = if (sortedReadings.first().unit == "mmol/L") sortedReadings.first().value * 18.0 else sortedReadings.first().value
                        val ry = topPadding + chartHeight * (1f - ((rawValue - minVal) / valueRange).toFloat())
                        drawCircle(
                            color = ClinicalTeal,
                            radius = 6.dp.toPx(),
                            center = Offset(rx, ry)
                        )
                        drawText(
                            textMeasurer = textMeasurer,
                            text = "Single reading logged. Keep logging details to draw trend flow.",
                            topLeft = Offset(leftPadding + 10f, canvasHeight - bottomPadding + 5f),
                            style = textStyle
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MealRelationGlucoseComparisonChart(
    readings: List<GlucoseReading>,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface, fontSize = 9.sp)

    val fastingVals = readings.filter { it.type == "Fasting" }.map { if (it.unit == "mmol/L") it.value * 18.0 else it.value }
    val beforeMealVals = readings.filter { it.type == "Before Meal" }.map { if (it.unit == "mmol/L") it.value * 18.0 else it.value }
    val afterMealVals = readings.filter { it.type == "After Meal" }.map { if (it.unit == "mmol/L") it.value * 18.0 else it.value }

    val avgFasting = if (fastingVals.isNotEmpty()) fastingVals.average() else 0.0
    val avgBefore = if (beforeMealVals.isNotEmpty()) beforeMealVals.average() else 0.0
    val avgAfter = if (afterMealVals.isNotEmpty()) afterMealVals.average() else 0.0

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Glucose Levels by Nutrition Phase (mg/dL)",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (avgFasting == 0.0 && avgBefore == 0.0 && avgAfter == 0.0) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Restaurant,
                            contentDescription = null,
                            tint = ClinicalTeal,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Nutrition Correlations Empty",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Add meal-relation timing attributes (e.g., Fasting, After meal) to logs.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
            } else {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                ) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height

                    val leftPadding = 50f
                    val topPadding = 20f
                    val bottomPadding = 35f

                    val chartWidth = canvasWidth - leftPadding
                    val chartHeight = canvasHeight - topPadding - bottomPadding

                    val maxLimit = 250f
                    val barWidth = 60f
                    val barSpacing = 80f

                    val bars = listOf(
                        Pair("Fasting", avgFasting),
                        Pair("Before Meal", avgBefore),
                        Pair("After Meal", avgAfter)
                    )

                    // Draw reference target levels grid-line at 140 mg/dl (standard target limits after dining)
                    val limitY = topPadding + chartHeight * (1f - (140f / maxLimit))
                    drawLine(
                        color = Color(0xFFB4B4B4),
                        start = Offset(leftPadding, limitY),
                        end = Offset(canvasWidth, limitY),
                        strokeWidth = 1f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f)
                    )
                    drawText(
                        textMeasurer = textMeasurer,
                        text = "PostMeal Target (140)",
                        topLeft = Offset(leftPadding + 10f, limitY - 14f),
                        style = textStyle.copy(color = Color.Gray, fontSize = 7.5.sp)
                    )

                    bars.forEachIndexed { index, (label, value) ->
                        val rx = leftPadding + (index * barSpacing) + (barSpacing / 2f) - (barWidth / 2f)
                        val valPercent = min(1.0, value / maxLimit)
                        val rBarHeight = (chartHeight * valPercent).toFloat()
                        val ry = topPadding + chartHeight - rBarHeight

                        // Column Fill Color Schemes (Teal for healthy bounds, Warning Amber for heavy postmeals)
                        val fillCol = if (value > 150.0) MedicalAlertHigh else ClinicalSecondary

                        // Draw Column
                        drawRect(
                            color = fillCol,
                            topLeft = Offset(rx, ry),
                            size = Size(barWidth, rBarHeight)
                        )

                        // Write average text inside of Column
                        if (value > 0) {
                            drawText(
                                textMeasurer = textMeasurer,
                                text = String.format("%.0f", value),
                                topLeft = Offset(rx + 15f, ry - 18f),
                                style = textStyle.copy(fontWeight = FontWeight.Bold, color = fillCol)
                            )
                        } else {
                            drawText(
                                textMeasurer = textMeasurer,
                                text = "N/A",
                                topLeft = Offset(rx + 18f, ry - 18f),
                                style = textStyle.copy(color = Color.Gray)
                            )
                        }

                        // Bottom Labels
                        drawText(
                            textMeasurer = textMeasurer,
                            text = label,
                            topLeft = Offset(rx - 8f, canvasHeight - bottomPadding + 5f),
                            style = textStyle
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InsulinGlucoseRelationshipChart(
    readings: List<GlucoseReading>,
    insulinLogs: List<InsulinLog>,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface, fontSize = 9.sp)
    val gridLineColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Insulin Dose vs Sugar Trends",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))

            val sortedSugar = readings.take(10).reversed()
            val sortedInsulin = insulinLogs.take(10).reversed()

            if (sortedSugar.isEmpty() && sortedInsulin.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Add blood glucose and insulin logs to overlay trends.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            } else {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                ) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height

                    val leftPadding = 50f
                    val rightPadding = 50f
                    val topPadding = 20f
                    val bottomPadding = 30f

                    val chartWidth = canvasWidth - leftPadding - rightPadding
                    val chartHeight = canvasHeight - topPadding - bottomPadding

                    // LEFT AXIS: Sugar level scale (0 to 300 mg/dL)
                    // RIGHT AXIS: Insulin Active Unit scale (0 to 30 Units)

                    // Draw reference grid
                    for (i in 0..3) {
                        val fraction = i.toFloat() / 3
                        val y = topPadding + chartHeight * (1f - fraction)
                        drawLine(
                            color = gridLineColor,
                            start = Offset(leftPadding, y),
                            end = Offset(canvasWidth - rightPadding, y),
                            strokeWidth = 1f
                        )

                        // Sugar values left labels
                        val sugarLabel = (fraction * 300).toInt()
                        drawText(
                            textMeasurer = textMeasurer,
                            text = sugarLabel.toString(),
                            topLeft = Offset(8f, y - 12f),
                            style = textStyle.copy(color = ClinicalTeal)
                        )

                        // Insulin values right labels
                        val insulinLabel = (fraction * 24).toInt()
                        drawText(
                            textMeasurer = textMeasurer,
                            text = "${insulinLabel}U",
                            topLeft = Offset(canvasWidth - rightPadding + 8f, y - 12f),
                            style = textStyle.copy(color = Color(0xFFB45064))
                        )
                    }

                    // Plot insulin doses as bars from bottom
                    if (sortedInsulin.isNotEmpty()) {
                        val maxDose = 24.0
                        val barSpacing = chartWidth / sortedInsulin.size
                        val iBarWidth = min(20f, (barSpacing / 2f))
                        
                        sortedInsulin.forEachIndexed { idx, ins ->
                            val rx = leftPadding + (idx * barSpacing) + (barSpacing / 4f)
                            val valPercent = min(1.0, ins.units / maxDose)
                            val iBarHeight = (chartHeight * valPercent).toFloat()
                            val ry = topPadding + chartHeight - iBarHeight

                            drawRect(
                                color = Color(0xFFE68C96),
                                topLeft = Offset(rx, ry),
                                size = Size(iBarWidth, iBarHeight)
                            )
                        }
                    }

                    // Plot sugar trajectory overlay line
                    if (sortedSugar.isNotEmpty()) {
                        val maxSugar = 300f
                        val pSpacing = chartWidth / sortedSugar.size
                        val points = sortedSugar.mapIndexed { idx, sugar ->
                            val rx = leftPadding + (idx * pSpacing) + (pSpacing / 2f)
                            val rawVal = if (sugar.unit == "mmol/L") sugar.value * 18.0 else sugar.value
                            val normVal = max(0.0, min(300.0, rawVal)).toFloat()
                            val ry = topPadding + chartHeight * (1f - (normVal / maxSugar))
                            Offset(rx, ry)
                        }

                        if (points.size > 1) {
                            val path = Path().apply {
                                moveTo(points.first().x, points.first().y)
                                for (i in 1 until points.size) {
                                    lineTo(points[i].x, points[i].y)
                                }
                            }
                            drawPath(
                                path = path,
                                color = ClinicalTeal,
                                style = Stroke(width = 2.5.dp.toPx())
                            )
                        }

                        points.forEach { p ->
                            drawCircle(
                                color = ClinicalTeal,
                                radius = 3.5.dp.toPx(),
                                center = p
                            )
                        }
                    }
                }
            }
        }
    }
}
