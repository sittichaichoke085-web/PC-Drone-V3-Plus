package com.pcdrone.v3plus

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class HistoryReportActivity : Activity() {

    private fun dp(v: Int): Int =
        (v * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val scroll = ScrollView(this)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(40), dp(18), dp(24))
            setBackgroundColor(Color.WHITE)
        }

        val back = Button(this).apply {
            text = "← ย้อนกลับ"
            isAllCaps = false
            setOnClickListener { finish() }
        }
        root.addView(back)

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
            setPadding(0, 0, 0, dp(20))
        })

        fun addDateSection(title: String) {
            root.addView(TextView(this).apply {
                text = title
                textSize = 18f
                setTextColor(Color.BLACK)
                setPadding(0, dp(8), 0, dp(6))
            })

            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
            }

            fun field(hintText: String, max: Int, weight: Float): EditText =
                EditText(this).apply {
                    hint = hintText
                    gravity = Gravity.CENTER
                    isSingleLine = true
                    inputType = InputType.TYPE_CLASS_NUMBER
                    filters = arrayOf(InputFilter.LengthFilter(max))
                    textSize = 16f
                    layoutParams = LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        weight
                    ).apply {
                        marginEnd = dp(6)
                    }
                }

            row.addView(field("วัน", 2, 0.8f))
            row.addView(field("เดือน", 2, 0.9f))
            row.addView(field("ปี", 4, 1.2f))

            root.addView(row)
        }

        addDateSection("จากวันที่")
        addDateSection("ถึงวันที่")

        val search = Button(this).apply {
            text = "ค้นหาประวัติงาน"
            isAllCaps = false
            textSize = 17f
            setTextColor(Color.WHITE)

            background =
                android.graphics.drawable.GradientDrawable().apply {
                    setColor(Color.rgb(0, 105, 55))
                    cornerRadius = dp(12).toFloat()
                    setStroke(dp(2), Color.rgb(0, 70, 35))
                }
        }

        root.addView(
            search,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(22)
            }
        )

        root.addView(TextView(this).apply {
            text = "ตารางประวัติงานจะแสดงบริเวณนี้"
            gravity = Gravity.CENTER
            textSize = 15f
            setTextColor(Color.GRAY)
            setPadding(0, dp(30), 0, dp(20))
        })

        scroll.addView(root)
        setContentView(scroll)
    }
}
