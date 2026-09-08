package com.pcdrone.v3plus

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object HistoryPdfExporter {

    private data class Row(
        val date: Long,
        val customer: String,
        val service: String,
        val rai: Double,
        val total: Double,
        val status: String
    )

    fun createAndShare(
        context: Context,
        start: Long,
        end: Long
    ) {
        try {
            val prefs = context.getSharedPreferences(
                "pc_drone_v3_data",
                Context.MODE_PRIVATE
            )

            val jobs = prefs
                .getStringSet("flight_jobs", emptySet())
                ?.toList()
                ?: emptyList()

            val appointments = prefs
                .getStringSet("job_appointments", emptySet())
                ?.mapNotNull {
                    val p = it.split("|||")
                    if (p.size >= 2) {
                        val id = p[0].toLongOrNull()
                        val time = p[1].toLongOrNull()
                        if (id != null && time != null) {
                            id to time
                        } else null
                    } else null
                }
                ?.toMap()
                ?: emptyMap()

            val rows = jobs.mapNotNull { raw ->
                val p = raw.split(
                    "|||",
                    ignoreCase = false,
                    limit = 9
                )

                if (p.size < 9) return@mapNotNull null

                val id =
                    p[0].toLongOrNull()
                        ?: return@mapNotNull null

                val effectiveDate =
                    appointments[id] ?: id

                if (effectiveDate !in start..end) {
                    return@mapNotNull null
                }

                Row(
                    date = effectiveDate,
                    customer = p[1],
                    service = p[2],
                    rai = p[4].toDoubleOrNull() ?: 0.0,
                    total = p[6].toDoubleOrNull() ?: 0.0,
                    status = p[7]
                )
            }.sortedBy { it.date }

            if (rows.isEmpty()) {
                android.widget.Toast.makeText(
                    context,
                    "ไม่มีข้อมูลสำหรับสร้าง PDF",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
                return
            }

            val tz = TimeZone.getTimeZone("Asia/Bangkok")

            val df = SimpleDateFormat(
                "dd/MM/yyyy",
                Locale.getDefault()
            ).apply {
                timeZone = tz
            }

            val nf = NumberFormat.getNumberInstance(
                Locale.getDefault()
            ).apply {
                maximumFractionDigits = 2
            }

            val pdf = PdfDocument()

            // A4 portrait 595 x 842
            val pageWidth = 595
            val pageHeight = 842

            val left = 28f
            val right = 567f
            val bottomLimit = 805f

            val normal = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.BLACK
                textSize = 8f
                typeface = Typeface.create(
                    "sans-serif",
                    Typeface.NORMAL
                )
            }

            val bold = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.BLACK
                textSize = 9f
                typeface = Typeface.create(
                    "sans-serif",
                    Typeface.BOLD
                )
            }

            val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(0, 90, 45)
                textSize = 9f
                typeface = Typeface.create(
                    "sans-serif",
                    Typeface.BOLD
                )
            }

            val whiteBold = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.BLACK
                textSize = 8f
                typeface = Typeface.create(
                    "sans-serif",
                    Typeface.NORMAL
                )
            }

            val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(180, 180, 180)
                strokeWidth = 0.7f
                style = Paint.Style.STROKE
            }

            val headerFill = Paint().apply {
                color = Color.WHITE
                style = Paint.Style.FILL
            }

            val altFill = Paint().apply {
                color = Color.rgb(245, 245, 245)
                style = Paint.Style.FILL
            }

            val columnX = floatArrayOf(
                28f,   // วันที่
                100f,  // ลูกค้า
                220f,  // งาน
                335f,  // ไร่
                390f,  // รวม
                480f,  // สถานะ
                567f
            )

            fun fitText(
                text: String,
                paint: Paint,
                maxWidth: Float
            ): String {
                if (paint.measureText(text) <= maxWidth) {
                    return text
                }

                var value = text

                while (
                    value.length > 1 &&
                    paint.measureText("$value…") > maxWidth
                ) {
                    value = value.dropLast(1)
                }

                return "$value…"
            }

            var pageNumber = 0
            var page: PdfDocument.Page? = null
            var y = 0f

            fun finishCurrentPage() {
                page?.let {
                    val canvas = it.canvas

                    normal.textSize = 8f
                    canvas.drawText(
                        "หน้า $pageNumber",
                        520f,
                        825f,
                        normal
                    )
                    normal.textSize = 9f

                    pdf.finishPage(it)
                }

                page = null
            }

            fun drawTableHeader() {
                val canvas = page!!.canvas
                val top = y
                val height = 25f
                val bottom = top + height

                canvas.drawRect(
                    left,
                    top,
                    right,
                    bottom,
                    headerFill
                )

                val headers = arrayOf(
                    "วันที่",
                    "ลูกค้า",
                    "งาน",
                    "ไร่",
                    "รวม",
                    "สถานะ"
                )

                for (i in headers.indices) {
                    canvas.drawText(
                        headers[i],
                        columnX[i] + 4f,
                        top + 16f,
                        whiteBold
                    )
                }

                // กรอบบนสุด
                canvas.drawLine(
                    left,
                    top,
                    right,
                    top,
                    linePaint
                )

                // กรอบล่างของหัวตาราง
                canvas.drawLine(
                    left,
                    bottom,
                    right,
                    bottom,
                    linePaint
                )

                // เส้นแบ่งทุกคอลัมน์ รวมขอบซ้ายและขวา
                for (x in columnX) {
                    canvas.drawLine(
                        x,
                        top,
                        x,
                        bottom,
                        linePaint
                    )
                }

                y = bottom
            }

            fun startPage(firstPage: Boolean) {
                pageNumber++

                page = pdf.startPage(
                    PdfDocument.PageInfo.Builder(
                        pageWidth,
                        pageHeight,
                        pageNumber
                    ).create()
                )

                val canvas = page!!.canvas

                y = 38f

                canvas.drawText(
                    "PC Drone",
                    left,
                    y,
                    titlePaint
                )

                y += 22f

                val reportTitlePaint =
                    Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = Color.BLACK
                        textSize = 17f
                        typeface = Typeface.create(
                            "sans-serif",
                            Typeface.NORMAL
                        )
                        textAlign = Paint.Align.CENTER
                    }

                canvas.drawText(
                    "รายงานประวัติงาน",
                    pageWidth / 2f,
                    y,
                    reportTitlePaint
                )

                y += 22f

                canvas.drawText(
                    "ช่วงวันที่ ${df.format(Date(start))} - ${df.format(Date(end))}",
                    left,
                    y,
                    normal
                )

                y += 20f

                drawTableHeader()
            }

            startPage(true)

            rows.forEachIndexed { index, r ->

                val rowHeight = 24f

                if (y + rowHeight > bottomLimit) {
                    finishCurrentPage()
                    startPage(false)
                }

                val canvas = page!!.canvas

                // ตารางขาวดำ ไม่มีสีพื้นสลับ

                val values = arrayOf(
                    df.format(Date(r.date)),
                    r.customer,
                    r.service,
                    nf.format(r.rai),
                    nf.format(r.total),
                    r.status
                )

                for (i in values.indices) {
                    val available =
                        columnX[i + 1] -
                        columnX[i] -
                        8f

                    val value = fitText(
                        values[i],
                        normal,
                        available
                    )

                    canvas.drawText(
                        value,
                        columnX[i] + 4f,
                        y + 16f,
                        normal
                    )
                }

                for (x in columnX) {
                    canvas.drawLine(
                        x,
                        y,
                        x,
                        y + rowHeight,
                        linePaint
                    )
                }

                canvas.drawLine(
                    left,
                    y + rowHeight,
                    right,
                    y + rowHeight,
                    linePaint
                )

                y += rowHeight
            }

            val totalRai = rows.sumOf { it.rai }
            val totalValue = rows.sumOf { it.total }

            if (y + 42f > bottomLimit) {
                finishCurrentPage()
                startPage(false)
            }

            y += 12f

            val summaryCanvas = page!!.canvas

            summaryCanvas.drawLine(
                left,
                y,
                right,
                y,
                linePaint
            )

            y += 18f

            summaryCanvas.drawText(
                "สรุป  จำนวน ${rows.size} งาน   รวม ${nf.format(totalRai)} ไร่   มูลค่ารวม ${nf.format(totalValue)} บาท",
                left,
                y,
                normal
            )

            finishCurrentPage()

            val dir = File(
                context.cacheDir,
                "history_reports"
            )

            if (!dir.exists()) {
                dir.mkdirs()
            }

            dir.listFiles()?.forEach {
                if (it.isFile) it.delete()
            }

            val fileName =
                "PC-Drone-History-" +
                System.currentTimeMillis() +
                ".pdf"

            val file = File(dir, fileName)

            FileOutputStream(file).use {
                pdf.writeTo(it)
            }

            pdf.close()

            val uri = Uri.parse(
                "content://${context.packageName}.history-reports/$fileName"
            )

            val shareIntent = Intent(
                Intent.ACTION_SEND
            ).apply {
                type = "application/pdf"
                putExtra(
                    Intent.EXTRA_STREAM,
                    uri
                )
                addFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }

            context.startActivity(
                Intent.createChooser(
                    shareIntent,
                    "แชร์รายงานประวัติงาน PDF"
                )
            )

        } catch (e: Exception) {
            android.widget.Toast.makeText(
                context,
                "สร้าง PDF ไม่สำเร็จ: ${e.message ?: "ไม่ทราบสาเหตุ"}",
                android.widget.Toast.LENGTH_LONG
            ).show()
        }
    }
}
