package ua.kpi.solarpractice6

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import java.io.File

object ReportPdf {
    fun create(context: Context, data: List<EnergyRecord>): File {
        val doc = PdfDocument()
        try {
            val page = doc.startPage(PdfDocument.PageInfo.Builder(595, 842, 1).create())
            val canvas = page.canvas
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 16f; isFakeBoldText = true }
            canvas.drawText("SolarPractice6 - Energy report", 45f, 60f, paint)
            paint.isFakeBoldText = false
            paint.textSize = 12f
            canvas.drawText("Demonstration data - solar energy forecast", 45f, 90f, paint)
            val maxPower = data.maxOfOrNull { it.power } ?: 0
            val total = data.sumOf { it.power }
            canvas.drawText("Records: ${data.size}   Peak: $maxPower W   Sum: $total Wh", 45f, 120f, paint)
            canvas.drawText("Hour", 45f, 158f, paint)
            canvas.drawText("Power (W)", 160f, 158f, paint)
            canvas.drawText("Temp (C)", 310f, 158f, paint)
            data.take(24).forEachIndexed { index, row ->
                val y = 181f + index * 24f
                canvas.drawText("%02d:00".format(row.hour), 45f, y, paint)
                canvas.drawText(row.power.toString(), 160f, y, paint)
                canvas.drawText("%.1f".format(java.util.Locale.US, row.temperature), 310f, y, paint)
            }
            canvas.drawText("Generated locally. No production measurements.", 45f, 790f, paint)
            doc.finishPage(page)
            val chartPage = doc.startPage(PdfDocument.PageInfo.Builder(595, 842, 2).create())
            val chartCanvas = chartPage.canvas
            paint.textSize = 16f
            paint.isFakeBoldText = true
            chartCanvas.drawText("Solar generation profile (24 h)", 45f, 60f, paint)
            paint.isFakeBoldText = false
            paint.textSize = 11f
            val bars = data.sortedBy { it.hour }.take(24)
            val peak = (bars.maxOfOrNull { it.power } ?: 1).coerceAtLeast(1)
            val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.rgb(102,80,164) }
            bars.forEachIndexed { i, r ->
                val x = 48f + i * 20f
                val barHeight = 380f * r.power / peak
                chartCanvas.drawRect(x, 550f - barHeight, x + 13f, 550f, barPaint)
                if(i % 3 == 0) chartCanvas.drawText(r.hour.toString(), x, 572f, paint)
            }
            chartCanvas.drawText("Power, W (relative scale). Peak = $peak W", 45f, 610f, paint)
            chartCanvas.drawText("All values are simulated educational data.", 45f, 640f, paint)
            doc.finishPage(chartPage)
            return File(context.filesDir, "solar_report.pdf").also { file ->
                file.outputStream().use { doc.writeTo(it) }
            }
        } finally { doc.close() }
    }
}
