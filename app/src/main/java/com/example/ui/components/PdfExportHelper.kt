package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max
import kotlin.math.min

object PdfExportHelper {
    private const val TAG = "PdfExportHelper"

    fun exportReport(
        context: Context,
        profile: UserProfile,
        thresholds: AlertThreshold,
        readings: List<GlucoseReading>,
        meals: List<MealLog>,
        medications: List<Medication>,
        medLogs: List<MedicationLog>,
        insulinLogs: List<InsulinLog>,
        unit: String
    ): File? {
        val pdfDocument = PdfDocument()

        // Page setup: Standard letter/A4 resolution (595 x 842 pt for A4)
        val pageWidth = 595
        val pageHeight = 842

        // Create page 1
        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        // Paint setups
        val textPaint = Paint().apply {
            color = Color.BLACK
            textSize = 10f
            isAntiAlias = true
        }

        val headerPaint = Paint().apply {
            color = Color.rgb(20, 110, 120) // Deep Teal clinical style
            isAntiAlias = true
        }

        val footerTextPaint = Paint().apply {
            color = Color.GRAY
            textSize = 8f
            isAntiAlias = true
        }

        val borderPaint = Paint().apply {
            color = Color.rgb(220, 220, 220)
            style = Paint.Style.STROKE
            strokeWidth = 1f
            isAntiAlias = true
        }

        val rowPaint = Paint().apply {
            color = Color.rgb(248, 248, 250)
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val accentPaint = Paint().apply {
            color = Color.rgb(235, 245, 245)
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        var y = 40f
        val margin = 40f

        // --- PAGE 1: HEADER & STATS SUMMARY ---
        // Header clinical styling text
        canvas.drawRect(margin, y, pageWidth - margin, y + 60f, headerPaint)
        
        val titlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("DIATRACK CLINICAL REPORT", margin + 15f, y + 28f, titlePaint)

        val subTitlePaint = Paint().apply {
            color = Color.rgb(220, 240, 240)
            textSize = 9f
            isAntiAlias = true
        }
        val lastUpdate = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        canvas.drawText("Comprehensive Diabetes Self-Monitoring Summary | Generated: $lastUpdate", margin + 15f, y + 45f, subTitlePaint)

        y += 80f

        // Basic Profile section
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("PATIENT PROFILE", margin, y, textPaint)
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        
        y += 15f
        canvas.drawRect(margin, y - 5f, pageWidth - margin, y + 35f, accentPaint)
        canvas.drawText("Name: ${profile.name}", margin + 10f, y + 10f, textPaint)
        canvas.drawText("Age: ${profile.age}", margin + 150f, y + 10f, textPaint)
        canvas.drawText("Gender: ${profile.gender}", margin + 250f, y + 10f, textPaint)
        canvas.drawText("Type: ${profile.diabetesType}", margin + 350f, y + 10f, textPaint)
        
        canvas.drawText("Blood Sugar Unit: $unit", margin + 10f, y + 25f, textPaint)
        canvas.drawText("Target: ${thresholds.lowThreshold} - ${thresholds.highThreshold} $unit", margin + 150f, y + 25f, textPaint)
        canvas.drawText("Emergency Bounds: < ${thresholds.emergencyLow} or > ${thresholds.emergencyHigh} $unit", margin + 300f, y + 25f, textPaint)

        y += 55f

        // Metrics calculations
        val totalReadings = readings.size
        val avgSugar = if (totalReadings > 0) readings.map { it.value }.average() else 0.0
        val maxSugar = if (totalReadings > 0) readings.maxOf { it.value } else 0.0
        val minSugar = if (totalReadings > 0) readings.minOf { it.value } else 0.0
        val fastingReadings = readings.filter { it.type == "Fasting" }
        val avgFasting = if (fastingReadings.isNotEmpty()) fastingReadings.map { it.value }.average() else 0.0

        // Summary Boxes
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("GLUCOSE METRICS SUMMARY", margin, y, textPaint)
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

        y += 12f
        val boxWidth = 110f
        val boxHeight = 50f
        val boxGap = 15f

        fun drawMetricBox(cx: Float, cy: Float, title: String, value: String, isWarning: Boolean = false) {
            val bgPaint = Paint().apply {
                color = if (isWarning) Color.rgb(255, 240, 240) else Color.rgb(245, 248, 248)
                style = Paint.Style.FILL
                isAntiAlias = true
            }
            val borderBoxPaint = Paint().apply {
                color = if (isWarning) Color.rgb(240, 100, 100) else Color.rgb(200, 210, 210)
                style = Paint.Style.STROKE
                strokeWidth = 1f
                isAntiAlias = true
            }
            canvas.drawRect(cx, cy, cx + boxWidth, cy + boxHeight, bgPaint)
            canvas.drawRect(cx, cy, cx + boxWidth, cy + boxHeight, borderBoxPaint)

            val boxTitlePaint = Paint().apply {
                color = Color.DKGRAY
                textSize = 7.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                isAntiAlias = true
            }
            canvas.drawText(title, cx + 8f, cy + 15f, boxTitlePaint)

            val boxValuePaint = Paint().apply {
                color = if (isWarning) Color.RED else Color.rgb(10, 80, 90)
                textSize = 13f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            canvas.drawText(value, cx + 8f, cy + 35f, boxValuePaint)
        }

        drawMetricBox(margin, y, "AVERAGE SUGAR", String.format("%.1f", avgSugar), false)
        drawMetricBox(margin + boxWidth + boxGap, y, "HIGHEST READING", String.format("%.1f", maxSugar), maxSugar > thresholds.highThreshold)
        drawMetricBox(margin + (boxWidth + boxGap) * 2, y, "LOWEST READING", String.format("%.1f", minSugar), minSugar > 0.0 && minSugar < thresholds.lowThreshold)
        drawMetricBox(margin + (boxWidth + boxGap) * 3, y, "AVG FASTING", if (avgFasting > 0) String.format("%.1f", avgFasting) else "N/A", false)

        y += 75f

        // Draw simple inline visual trend chart on the PDF page!
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("BLOOD GLUCOSE RECENT TREND VISUALIZATION", margin, y, textPaint)
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

        y += 10f
        val chartHeight = 100f
        val chartWidth = pageWidth - margin * 2
        val chartBgPaint = Paint().apply {
            color = Color.rgb(250, 252, 252)
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        // Draw chart frame background bounded area
        canvas.drawRect(margin, y, margin + chartWidth, y + chartHeight, chartBgPaint)
        canvas.drawRect(margin, y, margin + chartWidth, y + chartHeight, borderPaint)

        // Draw normal Target threshold lines
        val linePaint = Paint().apply {
            color = Color.rgb(150, 220, 150)
            style = Paint.Style.STROKE
            strokeWidth = 1f
            isAntiAlias = true
        }
        val highLineY = y + chartHeight - ((thresholds.highThreshold / 300.0) * chartHeight).toFloat()
        val lowLineY = y + chartHeight - ((thresholds.lowThreshold / 300.0) * chartHeight).toFloat()
        
        // Ensure lines stay inside box boundaries
        val hLineClamped = max(y, min(y + chartHeight, highLineY))
        val lLineClamped = max(y, min(y + chartHeight, lowLineY))

        canvas.drawLine(margin, hLineClamped, margin + chartWidth, hLineClamped, linePaint)
        canvas.drawLine(margin, lLineClamped, margin + chartWidth, lLineClamped, linePaint)
        
        val lineLabelPaint = Paint().apply {
            color = Color.rgb(100, 160, 100)
            textSize = 7f
            isAntiAlias = true
        }
        canvas.drawText("Target High: ${thresholds.highThreshold}", margin + 5f, hLineClamped - 2f, lineLabelPaint)
        canvas.drawText("Target Low: ${thresholds.lowThreshold}", margin + 5f, lLineClamped + 8f, lineLabelPaint)

        // Plot recent points
        val plottedReadings = readings.take(20).reversed()
        if (plottedReadings.size > 1) {
            val plotPaint = Paint().apply {
                color = Color.rgb(20, 120, 130)
                style = Paint.Style.FILL_AND_STROKE
                strokeWidth = 2.5f
                isAntiAlias = true
            }
            val pointPaint = Paint().apply {
                color = Color.rgb(230, 80, 80)
                style = Paint.Style.FILL
                isAntiAlias = true
            }

            val xInterval = chartWidth / (plottedReadings.size - 1)
            var prevX = 0f
            var prevY = 0f

            plottedReadings.forEachIndexed { index, r ->
                val rx = margin + (index * xInterval)
                // Normalize glucose values in scale to fit 300 mg/dl total grid height
                val rawValue = if (r.unit == "mmol/L") r.value * 18.0 else r.value
                val normValue = max(0.0, min(300.0, rawValue))
                val ry = y + chartHeight - ((normValue / 300.0) * chartHeight).toFloat()
                
                // Draw connecting line
                if (index > 0) {
                    canvas.drawLine(prevX, prevY, rx, ry, plotPaint)
                }
                
                // Draw point circle
                canvas.drawCircle(rx, ry, 3.5f, pointPaint)
                prevX = rx
                prevY = ry
            }
            
            // X bounds timestamp labels
            val dateLabelPaint = Paint().apply {
                color = Color.GRAY
                textSize = 7f
                isAntiAlias = true
            }
            val dateStart = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(plottedReadings.first().timestamp)
            val dateEnd = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(plottedReadings.last().timestamp)
            canvas.drawText(dateStart, margin, y + chartHeight + 10f, dateStartLabelPaint(Date()))
            canvas.drawText(dateEnd, margin + chartWidth - 60f, y + chartHeight + 10f, dateStartLabelPaint(Date()))
        } else {
            // Draw placeholder in trend chart
            val centerTextPaint = Paint().apply {
                color = Color.GRAY
                textSize = 9f
                isAntiAlias = true
            }
            canvas.drawText("Add at least 2 glucose readings to plot your trend curve here.", margin + 110f, y + 55f, centerTextPaint)
        }

        y += 135f

        // Blood sugar logging history table on Page 1
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("GLUCOSE READING HISTORY (RECENT LOGS)", margin, y, textPaint)
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

        y += 12f

        // Table headers
        val tableHeadPaint = Paint().apply {
            color = Color.rgb(60, 140, 150)
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawRect(margin, y, pageWidth - margin, y + 20f, tableHeadPaint)
        
        val headTextPaint = Paint().apply {
            color = Color.WHITE
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        canvas.drawText("Date & Time", margin + 10f, y + 13f, headTextPaint)
        canvas.drawText("Sugar Level", margin + 110f, y + 13f, headTextPaint)
        canvas.drawText("Period Type", margin + 180f, y + 13f, headTextPaint)
        canvas.drawText("Symptoms Reported", margin + 260f, y + 13f, headTextPaint)
        canvas.drawText("Notes", margin + 380f, y + 13f, headTextPaint)

        y += 20f

        // Print rows (let's print up to 10 rows on Page 1)
        val tableRowHeight = 22f
        val readingsPage1 = readings.take(13)

        readingsPage1.forEachIndexed { idx, r ->
            if (idx % 2 == 1) {
                canvas.drawRect(margin, y, pageWidth - margin, y + tableRowHeight, rowPaint)
            }
            canvas.drawRect(margin, y, pageWidth - margin, y + tableRowHeight, borderPaint)

            val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(r.timestamp)
            canvas.drawText(dateStr, margin + 5f, y + 14f, textPaint)

            // Dynamic color coding for out-of-range readings
            val textValuePaint = Paint().apply {
                color = if (r.value < thresholds.lowThreshold || r.value > thresholds.highThreshold) Color.RED else Color.rgb(10, 80, 90)
                textSize = 9.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            canvas.drawText("${r.value} ${r.unit}", margin + 110f, y + 14f, textValuePaint)
            canvas.drawText(r.type, margin + 180f, y + 14f, textPaint)
            
            val symStr = if (r.symptoms.isNotEmpty()) r.symptoms else "No Symptoms"
            val symTrunc = if (symStr.length > 22) symStr.substring(0, 19) + "..." else symStr
            canvas.drawText(symTrunc, margin + 260f, y + 14f, textPaint)

            val notesStr = if (r.notes.isNotEmpty()) r.notes else "N/A"
            val notesTrunc = if (notesStr.length > 25) notesStr.substring(0, 22) + "..." else notesStr
            canvas.drawText(notesTrunc, margin + 380f, y + 14f, textPaint)

            y += tableRowHeight
        }

        if (readingsPage1.isEmpty()) {
            canvas.drawText("No blood glucose readings found on the system database.", margin + 10f, y + 25f, textPaint)
            y += 40f
        }

        // Draw Footer for Page 1
        canvas.drawText("DiaTrack Patient Export report | Page 1", margin, pageHeight - 20f, footerTextPaint)
        pdfDocument.finishPage(page)

        // --- PAGE 2: MEAL LOGGING & MEDICATION LOGS ---
        pageNumber = 2
        pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        page = pdfDocument.startPage(pageInfo)
        canvas = page.canvas
        y = 40f

        // Page Header Banner
        canvas.drawRect(margin, y, pageWidth - margin, y + 35f, headerPaint)
        canvas.drawText("DIABETIC NUTRITION & MEDICATION THERAPY", margin + 15f, y + 22f, titleHeader2Paint())
        y += 55f

        // Meal logging section
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("MEAL MANAGEMENT & CARBOHYDRATE INTAKE (RECENT)", margin, y, textPaint)
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        y += 12f

        // Table headers for meals
        canvas.drawRect(margin, y, pageWidth - margin, y + 20f, tableHeadPaint)
        canvas.drawText("Time Logged", margin + 10f, y + 13f, headTextPaint)
        canvas.drawText("Meal Type", margin + 110f, y + 13f, headTextPaint)
        canvas.drawText("Food Description", margin + 180f, y + 13f, headTextPaint)
        canvas.drawText("Est. Carbs (g)", margin + 340f, y + 13f, headTextPaint)
        canvas.drawText("Nutrition Notes", margin + 420f, y + 13f, headTextPaint)
        y += 20f

        val mealsPage2 = meals.take(15)
        mealsPage2.forEachIndexed { idx, m ->
            if (idx % 2 == 1) {
                canvas.drawRect(margin, y, pageWidth - margin, y + tableRowHeight, rowPaint)
            }
            canvas.drawRect(margin, y, pageWidth - margin, y + tableRowHeight, borderPaint)

            val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(m.timestamp)
            canvas.drawText(dateStr, margin + 5f, y + 14f, textPaint)
            canvas.drawText(m.mealType, margin + 110f, y + 14f, textPaint)

            val foodTrunc = if (m.foodName.length > 25) m.foodName.substring(0, 22) + "..." else m.foodName
            canvas.drawText(foodTrunc, margin + 180f, y + 14f, textPaint)

            canvas.drawText("${m.carbsGrams}g", margin + 340f, y + 14f, textPaint)

            val notesStr = if (m.notes.isNotEmpty()) m.notes else "N/A"
            val notesTrunc = if (notesStr.length > 26) notesStr.substring(0, 23) + "..." else notesStr
            canvas.drawText(notesTrunc, margin + 420f, y + 14f, textPaint)

            y += tableRowHeight
        }

        if (mealsPage2.isEmpty()) {
            canvas.drawText("No meals recorded in the current active trackers.", margin + 10f, y + 25f, textPaint)
            y += 40f
        }

        y += 35f

        // Medication History section on Page 2
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("ACTIVE MEDICATIONS & GLYCAP-INDEPENDENT THERAPY", margin, y, textPaint)
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        y += 12f

        canvas.drawRect(margin, y, pageWidth - margin, y + 20f, tableHeadPaint)
        canvas.drawText("Medication Name", margin + 10f, y + 13f, headTextPaint)
        canvas.drawText("Prescription Dosage", margin + 150f, y + 13f, headTextPaint)
        canvas.drawText("Time Scheduled", margin + 280f, y + 13f, headTextPaint)
        canvas.drawText("Frequency Details", margin + 400f, y + 13f, headTextPaint)
        y += 20f

        val medsPage2 = medications
        medsPage2.forEachIndexed { idx, med ->
            if (idx % 2 == 1) {
                canvas.drawRect(margin, y, pageWidth - margin, y + tableRowHeight, rowPaint)
            }
            canvas.drawRect(margin, y, pageWidth - margin, y + tableRowHeight, borderPaint)

            canvas.drawText(med.name, margin + 5f, y + 14f, textPaint)
            canvas.drawText(med.dosage, margin + 150f, y + 14f, textPaint)
            canvas.drawText(med.timeOfDay, margin + 280f, y + 14f, textPaint)
            canvas.drawText(med.frequency, margin + 400f, y + 14f, textPaint)

            y += tableRowHeight
        }

        if (medsPage2.isEmpty()) {
            canvas.drawText("No active oral medications registered under settings.", margin + 10f, y + 25f, textPaint)
            y += 40f
        }

        canvas.drawText("DiaTrack Patient Export report | Page 2", margin, pageHeight - 20f, footerTextPaint)
        pdfDocument.finishPage(page)


        // --- PAGE 3: INSULIN DOSINGS & CLINICAL RECOMMENDATIONS ---
        pageNumber = 3
        pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        page = pdfDocument.startPage(pageInfo)
        canvas = page.canvas
        y = 40f

        // Page Header Banner
        canvas.drawRect(margin, y, pageWidth - margin, y + 35f, headerPaint)
        canvas.drawText("INSULIN THERAPY LOG & EMERGENCY PROTOCOLS", margin + 15f, y + 22f, titleHeader2Paint())
        y += 55f

        // Insulin history table
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("INSULIN INJECTION LOGS (CHRONOLOGICAL TRACKER)", margin, y, textPaint)
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        y += 12f

        canvas.drawRect(margin, y, pageWidth - margin, y + 20f, tableHeadPaint)
        canvas.drawText("Logged Time", margin + 10f, y + 13f, headTextPaint)
        canvas.drawText("Insulin Type", margin + 150f, y + 13f, headTextPaint)
        canvas.drawText("Units Administered", margin + 280f, y + 13f, headTextPaint)
        canvas.drawText("Meal Relation State", margin + 400f, y + 13f, headTextPaint)
        y += 20f

        val insulinPage3 = insulinLogs.take(15)
        insulinPage3.forEachIndexed { idx, ins ->
            if (idx % 2 == 1) {
                canvas.drawRect(margin, y, pageWidth - margin, y + tableRowHeight, rowPaint)
            }
            canvas.drawRect(margin, y, pageWidth - margin, y + tableRowHeight, borderPaint)

            val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(ins.timestamp)
            canvas.drawText(dateStr, margin + 5f, y + 14f, textPaint)
            canvas.drawText(ins.insulinType, margin + 150f, y + 14f, textPaint)
            
            val doubleUnitsPaint = Paint().apply {
                color = Color.rgb(20, 110, 120)
                textSize = 9.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            canvas.drawText("${ins.units} Units", margin + 280f, y + 14f, doubleUnitsPaint)
            canvas.drawText(ins.mealRelation, margin + 400f, y + 14f, textPaint)

            y += tableRowHeight
        }

        if (insulinPage3.isEmpty()) {
            canvas.drawText("No recorded insulin injection rows found in DB logs.", margin + 10f, y + 25f, textPaint)
            y += 40f
        }

        y += 45f

        // Safety Warnings & Instructions
        val safetyBoxPaint = Paint().apply {
            color = Color.rgb(255, 248, 245)
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val safetyBorderPaint = Paint().apply {
            color = Color.rgb(240, 150, 80)
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
            isAntiAlias = true
        }
        canvas.drawRect(margin, y, pageWidth - margin, y + 180f, safetyBoxPaint)
        canvas.drawRect(margin, y, pageWidth - margin, y + 180f, safetyBorderPaint)

        val warningHeaderPaint = Paint().apply {
            color = Color.rgb(180, 50, 10)
            textSize = 10.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("CRITICAL MEDICAL SAFETY & DATA CLINICAL INTERPRETATION ASSISTANCE", margin + 15f, y + 22f, warningHeaderPaint)

        val descPaint = Paint().apply {
            color = Color.BLACK
            textSize = 8.5f
            isAntiAlias = true
        }
        val bulletPaint = Paint().apply {
            color = Color.rgb(180, 50, 10)
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        var bulletY = y + 45f
        
        fun drawBulletText(textLine: String) {
            canvas.drawCircle(margin + 20f, bulletY - 3f, 2.5f, bulletPaint)
            canvas.drawText(textLine, margin + 32f, bulletY, descPaint)
            bulletY += 16f
        }

        drawBulletText("DISCLAIMER: DiaTrack is a metabolic self-monitoring log book and analytical record tracker.")
        drawBulletText("It IS NOT a diagnostic advisor, insulin calculator, prescription manager, or replacement for professional advice.")
        drawBulletText("The insulin injection logs above are strictly record histories entered manually by the patient on-device.")
        drawBulletText("DO NOT adjust dosing routines, ratios, or correction scales without consulting your endocrinology counselor first.")
        drawBulletText("GLUCOSE CRITICAL HIGHS/LOWS: In cases of severe hypoglycemia (< 55 mg/dL) or hyperosmolar spikes, seek ER care immediately.")
        drawBulletText("Doctor Consultation Note: The physical parameters and summaries listed are derived completely from logged telemetry.")
        drawBulletText("Please double check these entries against lab assays (e.g., HbA1c panels) before making therapeutic variations.")

        y += 205f

        // Doctor Sign off block
        val lineSignPaint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 0.8f
            isAntiAlias = true
        }
        canvas.drawLine(margin + 10f, y + 30f, margin + 180f, y + 30f, lineSignPaint)
        canvas.drawText("Patient Signature / Date", margin + 10f, y + 45f, textPaint)

        canvas.drawLine(pageWidth - margin - 180f, y + 30f, pageWidth - margin, y + 30f, lineSignPaint)
        canvas.drawText("Physician / Healthcare Signature", pageWidth - margin - 180f, y + 45f, textPaint)

        canvas.drawText("DiaTrack Patient Export report | Page 3", margin, pageHeight - 20f, footerTextPaint)
        pdfDocument.finishPage(page)

        // Write document to File Provider Cache
        return try {
            val reportsDir = File(context.cacheDir, "medical_reports")
            if (!reportsDir.exists()) {
                reportsDir.mkdirs()
            }
            val pdfFile = File(reportsDir, "DiaTrack_Doctor_Report_${System.currentTimeMillis()}.pdf")
            val outputStream = FileOutputStream(pdfFile)
            pdfDocument.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            pdfDocument.close()
            pdfFile
        } catch (e: IOException) {
            Log.e(TAG, "Error writing Clinical report PDF document", e)
            pdfDocument.close()
            null
        }
    }

    private fun titleHeader2Paint(): Paint {
        return Paint().apply {
            color = Color.WHITE
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
    }

    private fun dateStartLabelPaint(d: Date): Paint {
        return Paint().apply {
            color = Color.GRAY
            textSize = 7.5f
            isAntiAlias = true
        }
    }

    /**
     * Launch shares native sheets on-device for direct distribution (e.g., Email to Dr, Bluetooth print, safe-keep google drive).
     */
    fun sharePdf(context: Context, file: File) {
        try {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "DiaTrack Clinical Report - Diabetes Self-Monitoring")
                putExtra(Intent.EXTRA_TEXT, "Hello Doctor, please find attached my detailed diabetes monitoring profile table, glucose trends, medication log, and insulin therapy logs from DiaTrack.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Email or Export report to Doctor"))
        } catch (e: Exception) {
            Log.e(TAG, "Failed sharing PDF report file document provider", e)
            Toast.makeText(context, "Export sharing failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
