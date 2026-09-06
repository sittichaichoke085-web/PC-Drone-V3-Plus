package com.pcdrone.v3plus

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.pcdrone.v3plus.navigation.AppRoute

class MainActivity : Activity() {

    private val greenDark = Color.rgb(7, 91, 36)
    private val green = Color.rgb(11, 122, 48)
    private val black = Color.rgb(17, 17, 17)
    private val white = Color.WHITE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        showScreen(AppRoute.DASHBOARD)
    }

    private fun showScreen(route: AppRoute) {
        if (route == AppRoute.DASHBOARD) {
            showDashboard()
        } else {
            showPlaceholder(route)
        }
    }

    private fun showDashboard() {
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(24), dp(20), dp(28))
            setBackgroundColor(white)
        }

        content.addView(TextView(this).apply {
            text = "PC DRONE"
            textSize = 32f
            setTextColor(greenDark)
            gravity = Gravity.CENTER
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        })

        content.addView(TextView(this).apply {
            text = "PC Drone V3 Plus"
            textSize = 18f
            setTextColor(black)
            gravity = Gravity.CENTER
            setPadding(0, dp(4), 0, dp(24))
        })

        content.addView(summaryCard(
            "เงินคงเหลือ",
            "0.00 บาท"
        ))

        content.addView(summaryCard(
            "ยอดค้างรับ",
            "0.00 บาท"
        ))

        content.addView(summaryCard(
            "งานวันนี้",
            "0 งาน"
        ))

        addMenuButton(content, AppRoute.JOBS)
        addMenuButton(content, AppRoute.CUSTOMERS)
        addMenuButton(content, AppRoute.FINANCE)
        addMenuButton(content, AppRoute.HISTORY)
        addMenuButton(content, AppRoute.SETTINGS)

        val scrollView = ScrollView(this).apply {
            addView(
                content,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )
        }

        setContentView(scrollView)
    }

    private fun showPlaceholder(route: AppRoute) {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(24), dp(24), dp(24), dp(24))
            setBackgroundColor(white)
        }

        root.addView(TextView(this).apply {
            text = route.title
            textSize = 28f
            setTextColor(greenDark)
            gravity = Gravity.CENTER
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        })

        root.addView(TextView(this).apply {
            text = "กำลังเตรียมระบบใน Phase ถัดไป"
            textSize = 17f
            setTextColor(black)
            gravity = Gravity.CENTER
            setPadding(0, dp(16), 0, dp(28))
        })

        root.addView(Button(this).apply {
            text = "กลับหน้าหลัก"
            setTextColor(white)
            setBackgroundColor(green)
            setOnClickListener {
                showScreen(AppRoute.DASHBOARD)
            }
        })

        setContentView(root)
    }

    private fun addMenuButton(
        parent: LinearLayout,
        route: AppRoute
    ) {
        parent.addView(Button(this).apply {
            text = route.title
            textSize = 18f
            setTextColor(white)
            setBackgroundColor(green)
            isAllCaps = false

            setOnClickListener {
                showScreen(route)
            }
        }, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            dp(58)
        ).apply {
            topMargin = dp(10)
        })
    }

    private fun summaryCard(
        title: String,
        value: String
    ): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(14), dp(18), dp(14))
            setBackgroundColor(Color.rgb(232, 247, 235))

            addView(TextView(this@MainActivity).apply {
                text = title
                textSize = 16f
                setTextColor(black)
            })

            addView(TextView(this@MainActivity).apply {
                text = value
                textSize = 24f
                setTextColor(greenDark)
                setTypeface(typeface, android.graphics.Typeface.BOLD)
            })
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}
