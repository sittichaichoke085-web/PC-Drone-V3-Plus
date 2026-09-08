package com.pcdrone.v3plus

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class HistoryReportActivity : Activity() {

    private fun dp(v: Int) =
        (v * resources.displayMetrics.density).toInt()

    private lateinit var resultBox: LinearLayout

    private data class DateFields(
        val day: EditText,
        val month: EditText,
        val year: EditText
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val scroll = ScrollView(this)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(40), dp(18), dp(30))
            setBackgroundColor(Color.WHITE)
        }

        root.addView(Button(this).apply {
            text = "← ย้อนกลับ"
            isAllCaps = false
            setOnClickListener { finish() }
        })

        root.addView(TextView(this).apply {
            text = "ประมวลผล / แชร์ประวัติงาน"
            textSize = 24f
            setTextColor(Color.BLACK)
            setPadding(0, dp(18), 0, dp(6))
        })

        root.addView(TextView(this).apply {
            text = "เลือกช่วงวันที่ที่ต้องการดูประวัติงาน"
            textSize = 15f
            setTextColor(Color.DKGRAY)
            setPadding(0, 0, 0, dp(18))
        })

        fun dateSection(title: String): DateFields {
            root.addView(TextView(this).apply {
                text = title
                textSize = 18f
                setTextColor(Color.BLACK)
                setPadding(0, dp(8), 0, dp(5))
            })

            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
            }

            fun field(hint: String, max: Int, weight: Float) =
                EditText(this).apply {
                    this.hint = hint
                    gravity = Gravity.CENTER
                    isSingleLine = true
                    inputType = InputType.TYPE_CLASS_NUMBER
                    filters = arrayOf(InputFilter.LengthFilter(max))
                    textSize = 16f
                    layoutParams = LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        weight
                    ).apply { marginEnd = dp(6) }
                }

            val d = field("วัน", 2, .8f)
            val m = field("เดือน", 2, .9f)
            val y = field("ปี", 4, 1.2f)

            row.addView(d)
            row.addView(m)
            row.addView(y)
            root.addView(row)

            return DateFields(d, m, y)
        }

        val from = dateSection("จากวันที่")
        val to = dateSection("ถึงวันที่")

        val search = Button(this).apply {
            text = "ค้นหาประวัติงาน"
            isAllCaps = false
            textSize = 17f
            setTextColor(Color.WHITE)

            background =
                android.graphics.drawable.GradientDrawable().apply {
                    setColor(Color.rgb(0,105,55))
                    cornerRadius = dp(12).toFloat()
                    setStroke(dp(2), Color.rgb(0,70,35))
                }
        }

        root.addView(
            search,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dp(20) }
        )

        resultBox = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(18), 0, 0)
        }
        root.addView(resultBox)

        search.setOnClickListener {
            val start = parseDate(from, false)
            val end = parseDate(to, true)

            if (start == null || end == null) {
                Toast.makeText(
                    this,
                    "กรุณากรอกวันที่ให้ถูกต้อง",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (start > end) {
                Toast.makeText(
                    this,
                    "วันที่เริ่มต้นต้องไม่เกินวันที่สิ้นสุด",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            showJobs(start, end)
        }

        scroll.addView(root)
        setContentView(scroll)
    }

    private fun parseDate(
        f: DateFields,
        endOfDay: Boolean
    ): Long? {
        val d = f.day.text.toString().toIntOrNull() ?: return null
        val m = f.month.text.toString().toIntOrNull() ?: return null
        var y = f.year.text.toString().toIntOrNull() ?: return null

        if (y >= 2400) y -= 543

        if (d !in 1..31 || m !in 1..12 || y !in 1900..2200)
            return null

        return try {
            Calendar.getInstance(TimeZone.getTimeZone("Asia/Bangkok")).apply {
                isLenient = false
                clear()
                set(
                    y,
                    m - 1,
                    d,
                    if (endOfDay) 23 else 0,
                    if (endOfDay) 59 else 0,
                    if (endOfDay) 59 else 0
                )
            }.timeInMillis
        } catch (_: Exception) {
            null
        }
    }

    private fun showJobs(start: Long, end: Long) {
        resultBox.removeAllViews()

        val prefs =
            getSharedPreferences("pc_drone_v3_data", MODE_PRIVATE)

        val jobs =
            prefs.getStringSet("flight_jobs", emptySet())
                ?.toList()
                ?: emptyList()

        val appointments =
            prefs.getStringSet("job_appointments", emptySet())
                ?.mapNotNull {
                    val p = it.split("|||")
                    if (p.size >= 2) {
                        val id = p[0].toLongOrNull()
                        val time = p[1].toLongOrNull()
                        if (id != null && time != null) id to time else null
                    } else null
                }?.toMap()
                ?: emptyMap()

        data class Row(
            val date: Long,
            val customer: String,
            val service: String,
            val location: String,
            val rai: Double,
            val rate: Double,
            val total: Double,
            val status: String
        )

        val rows = jobs.mapNotNull { raw ->
            val p = raw.split("|||", ignoreCase = false, limit = 9)
            if (p.size < 9) return@mapNotNull null

            val id = p[0].toLongOrNull() ?: return@mapNotNull null
            val effectiveDate = appointments[id] ?: id

            if (effectiveDate !in start..end)
                return@mapNotNull null

            Row(
                effectiveDate,
                p[1],
                p[2],
                p[3],
                p[4].toDoubleOrNull() ?: 0.0,
                p[5].toDoubleOrNull() ?: 0.0,
                p[6].toDoubleOrNull() ?: 0.0,
                p[7]
            )
        }.sortedBy { it.date }

        if (rows.isEmpty()) {
            resultBox.addView(TextView(this).apply {
                text = "ไม่พบประวัติงานในช่วงวันที่นี้"
                gravity = Gravity.CENTER
                textSize = 16f
                setTextColor(Color.DKGRAY)
                setPadding(0, dp(24), 0, dp(24))
            })
            return
        }

        val nf = NumberFormat.getNumberInstance(Locale("th", "TH")).apply {
            maximumFractionDigits = 2
        }

        val df = SimpleDateFormat("dd/MM/yyyy", Locale("th", "TH")).apply {
            timeZone = TimeZone.getTimeZone("Asia/Bangkok")
        }

        val totalRai = rows.sumOf { it.rai }
        val totalValue = rows.sumOf { it.total }

        resultBox.addView(TextView(this).apply {
            text =
                "สรุป ${rows.size} งาน  •  ${nf.format(totalRai)} ไร่\n" +
                "มูลค่างานรวม ${nf.format(totalValue)} บาท"
            textSize = 17f
            setTextColor(Color.BLACK)
            setPadding(dp(12), dp(12), dp(12), dp(12))

            background =
                android.graphics.drawable.GradientDrawable().apply {
                    setColor(Color.rgb(232,245,233))
                    cornerRadius = dp(12).toFloat()
                    setStroke(dp(2), Color.rgb(0,105,55))
                }
        })

        val horizontal = HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = true
        }

        val table = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(10), 0, dp(10))
        }

        fun cell(
            value: String,
            width: Int,
            header: Boolean = false,
            alignRight: Boolean = false
        ): TextView =
            TextView(this).apply {
                text = value
                textSize = if (header) 14f else 13f
                setTextColor(
                    if (header) Color.WHITE else Color.BLACK
                )
                gravity =
                    if (alignRight)
                        Gravity.CENTER_VERTICAL or Gravity.END
                    else
                        Gravity.CENTER_VERTICAL
                setPadding(dp(7), dp(8), dp(7), dp(8))
                layoutParams =
                    LinearLayout.LayoutParams(
                        dp(width),
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
            }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(Color.rgb(0, 105, 55))
        }

        header.addView(cell("วันที่", 90, true))
        header.addView(cell("ลูกค้า", 125, true))
        header.addView(cell("งาน", 120, true))
        header.addView(cell("พื้นที่", 140, true))
        header.addView(cell("ไร่", 70, true, true))
        header.addView(cell("ราคา/ไร่", 90, true, true))
        header.addView(cell("รวม", 100, true, true))
        header.addView(cell("สถานะ", 110, true))

        table.addView(header)

        rows.forEachIndexed { index, r ->

            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL

                setBackgroundColor(
                    if (index % 2 == 0)
                        Color.WHITE
                    else
                        Color.rgb(245, 245, 245)
                )
            }

            row.addView(cell(df.format(Date(r.date)), 90))
            row.addView(cell(r.customer, 125))
            row.addView(cell(r.service, 120))
            row.addView(cell(r.location, 140))
            row.addView(cell(nf.format(r.rai), 70, alignRight = true))
            row.addView(cell(nf.format(r.rate), 90, alignRight = true))
            row.addView(cell(nf.format(r.total), 100, alignRight = true))
            row.addView(cell(r.status, 110))

            table.addView(row)

            table.addView(
                android.view.View(this).apply {
                    setBackgroundColor(Color.rgb(220,220,220))
                    layoutParams =
                        LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            dp(1)
                        )
                }
            )
        }

        horizontal.addView(table)

        resultBox.addView(
            horizontal,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(8)
            }
        )

        val sharePdfButton = Button(this).apply {
            text = "แชร์ PDF A4"
            isAllCaps = false
            textSize = 17f
            setTextColor(Color.WHITE)

            background =
                android.graphics.drawable.GradientDrawable().apply {
                    setColor(Color.rgb(0, 105, 55))
                    cornerRadius = dp(12).toFloat()
                    setStroke(
                        dp(2),
                        Color.rgb(0, 70, 35)
                    )
                }

            elevation = dp(5).toFloat()

            setOnClickListener {
                HistoryPdfExporter.createAndShare(
                    this@HistoryReportActivity,
                    start,
                    end
                )
            }
        }

        resultBox.addView(
            sharePdfButton,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(14)
                bottomMargin = dp(20)
            }
        )
    }
}
