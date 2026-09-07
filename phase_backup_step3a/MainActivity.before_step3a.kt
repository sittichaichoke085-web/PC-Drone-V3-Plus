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

        val prefs = getSharedPreferences(
            "pc_drone_v3_data",
            android.content.Context.MODE_PRIVATE
        )

        val moneyFormat =
            java.text.DecimalFormat("#,##0.00")

        fun money(value: String?): java.math.BigDecimal {
            return try {
                if (value.isNullOrBlank()) {
                    java.math.BigDecimal.ZERO
                } else {
                    java.math.BigDecimal(value.trim())
                }
            } catch (_: Exception) {
                java.math.BigDecimal.ZERO
            }
        }

        // =========================================
        // คำนวณข้อมูลงานบิน
        // =========================================

        val jobs =
            prefs.getStringSet(
                "flight_jobs",
                emptySet()
            )?.toList() ?: emptyList()

        var completedJobs = 0
        var waitingJobs = 0
        var totalRai = 0.0

        var jobIncome =
            java.math.BigDecimal.ZERO

        jobs.forEach { record ->

            val parts =
                record.split(
                    "|||",
                    ignoreCase = false,
                    limit = 9
                )

            if (parts.size >= 8) {

                val rai =
                    parts.getOrNull(4)
                        ?.toDoubleOrNull() ?: 0.0

                val total =
                    money(
                        parts.getOrNull(6)
                    )

                val status =
                    parts.getOrNull(7)
                        ?.trim() ?: ""

                when (status) {

                    "เสร็จแล้ว" -> {
                        completedJobs++
                        totalRai += rai
                        jobIncome =
                            jobIncome.add(total)
                    }

                    "ยกเลิก" -> {
                        // ไม่นับ
                    }

                    else -> {
                        waitingJobs++
                    }
                }
            }
        }

        // =========================================
        // คำนวณการเงิน
        // =========================================

        val transactions =
            prefs.getStringSet(
                "finance_transactions",
                emptySet()
            )?.toList() ?: emptyList()

        var otherIncome =
            java.math.BigDecimal.ZERO

        var expense =
            java.math.BigDecimal.ZERO

        transactions.forEach { record ->

            val parts =
                record.split(
                    "|||",
                    ignoreCase = false,
                    limit = 5
                )

            if (parts.size >= 4) {

                val type =
                    parts.getOrNull(1)
                        ?.trim() ?: ""

                val amount =
                    money(
                        parts.getOrNull(3)
                    )

                if (type == "INCOME") {
                    otherIncome =
                        otherIncome.add(amount)
                }

                if (type == "EXPENSE") {
                    expense =
                        expense.add(amount)
                }
            }
        }

        val totalIncome =
            jobIncome.add(otherIncome)

        val balance =
            totalIncome.subtract(expense)

        // =========================================
        // สีหลัก
        // =========================================

        val dark =
            android.graphics.Color.rgb(
                12, 18, 14
            )

        val deepGreen =
            android.graphics.Color.rgb(
                0, 91, 45
            )

        val green =
            android.graphics.Color.rgb(
                0, 145, 70
            )

        val softGreen =
            android.graphics.Color.rgb(
                235, 247, 239
            )

        val lightGray =
            android.graphics.Color.rgb(
                245, 247, 246
            )

        window.statusBarColor =
            dark

        if (
            android.os.Build.VERSION.SDK_INT >=
            android.os.Build.VERSION_CODES.M
        ) {
            window.decorView.systemUiVisibility =
                window.decorView.systemUiVisibility and
                android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        }

        fun rounded(
            color: Int,
            radius: Float
        ): android.graphics.drawable.GradientDrawable {

            return android.graphics.drawable.GradientDrawable().apply {
                shape =
                    android.graphics.drawable.GradientDrawable.RECTANGLE

                setColor(color)

                cornerRadius =
                    dp(radius.toInt()).toFloat()
            }
        }

        // =========================================
        // ROOT
        // =========================================

        val root =
            android.widget.LinearLayout(this).apply {

                orientation =
                    android.widget.LinearLayout.VERTICAL

                setBackgroundColor(
                    softGreen
                )
            }

        // =========================================
        // HEADER
        // =========================================

        val header =
            android.widget.LinearLayout(this).apply {

                orientation =
                    android.widget.LinearLayout.VERTICAL

                setPadding(
                    dp(20),
                    dp(22),
                    dp(20),
                    dp(20)
                )

                background =
                    rounded(
                        dark,
                        0f
                    )
            }

        if (
            android.os.Build.VERSION.SDK_INT >=
            android.os.Build.VERSION_CODES.KITKAT_WATCH
        ) {

            header.setOnApplyWindowInsetsListener { _, insets ->

                header.setPadding(
                    dp(20),
                    dp(22) + insets.systemWindowInsetTop,
                    dp(20),
                    dp(20)
                )

                insets
            }

            header.requestApplyInsets()
        }

        val brandRow =
            android.widget.LinearLayout(this).apply {

                orientation =
                    android.widget.LinearLayout.HORIZONTAL

                gravity =
                    android.view.Gravity.CENTER_VERTICAL
            }

        val logoText =
            android.widget.TextView(this).apply {

                text = "PC"

                textSize = 26f

                gravity =
                    android.view.Gravity.CENTER

                setTextColor(
                    android.graphics.Color.WHITE
                )

                setTypeface(
                    typeface,
                    android.graphics.Typeface.BOLD
                )

                background =
                    rounded(
                        green,
                        16f
                    )

                setPadding(
                    dp(12),
                    dp(8),
                    dp(12),
                    dp(8)
                )
            }

        val brandText =
            android.widget.LinearLayout(this).apply {

                orientation =
                    android.widget.LinearLayout.VERTICAL

                setPadding(
                    dp(14),
                    0,
                    0,
                    0
                )
            }

        brandText.addView(
            android.widget.TextView(this).apply {

                text = "PC-Drone V3 Plus"

                textSize = 24f

                setTextColor(
                    android.graphics.Color.WHITE
                )

                setTypeface(
                    typeface,
                    android.graphics.Typeface.BOLD
                )
            }
        )

        brandText.addView(
            android.widget.TextView(this).apply {

                text =
                    "จัดการงานโดรนเกษตร ครบ จบ ในแอปเดียว"

                textSize = 13f

                setTextColor(
                    android.graphics.Color.LTGRAY
                )
            }
        )

        brandRow.addView(
            logoText
        )

        brandRow.addView(
            brandText,
            android.widget.LinearLayout.LayoutParams(
                0,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        header.addView(
            brandRow
        )

        // =========================================
        // HERO
        // =========================================

        val hero =
            android.widget.LinearLayout(this).apply {

                orientation =
                    android.widget.LinearLayout.VERTICAL

                gravity =
                    android.view.Gravity.CENTER

                setPadding(
                    dp(16),
                    dp(18),
                    dp(16),
                    dp(18)
                )

                background =
                    rounded(
                        deepGreen,
                        20f
                    )
            }

        hero.addView(
            android.widget.TextView(this).apply {

                text = "✦  PC DRONE  ✦"

                textSize = 27f

                gravity =
                    android.view.Gravity.CENTER

                setTextColor(
                    android.graphics.Color.WHITE
                )

                setTypeface(
                    typeface,
                    android.graphics.Typeface.BOLD
                )
            }
        )

        hero.addView(
            android.widget.TextView(this).apply {

                text =
                    "พ่นยา • หว่านปุ๋ย • พืชไร่ • พืชสวน"

                textSize = 15f

                gravity =
                    android.view.Gravity.CENTER

                setTextColor(
                    android.graphics.Color.WHITE
                )

                setPadding(
                    0,
                    dp(6),
                    0,
                    0
                )
            }
        )

        val heroParams =
            android.widget.LinearLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {

                topMargin =
                    dp(18)
            }

        header.addView(
            hero,
            heroParams
        )

        root.addView(
            header
        )

        // =========================================
        // CONTENT
        // =========================================

        val content =
            android.widget.LinearLayout(this).apply {

                orientation =
                    android.widget.LinearLayout.VERTICAL

                setPadding(
                    dp(16),
                    dp(18),
                    dp(16),
                    dp(30)
                )

                setBackgroundColor(
                    softGreen
                )
            }

        content.addView(
            android.widget.TextView(this).apply {

                text = "เมนูหลัก"

                textSize = 20f

                setTextColor(
                    dark
                )

                setTypeface(
                    typeface,
                    android.graphics.Typeface.BOLD
                )

                setPadding(
                    dp(2),
                    0,
                    0,
                    dp(10)
                )
            }
        )

        fun menuCard(
            icon: String,
            title: String,
            subtitle: String,
            route: AppRoute
        ): android.widget.LinearLayout {

            return android.widget.LinearLayout(this).apply {

                orientation =
                    android.widget.LinearLayout.VERTICAL

                gravity =
                    android.view.Gravity.CENTER

                setPadding(
                    dp(8),
                    dp(14),
                    dp(8),
                    dp(14)
                )

                background =
                    rounded(
                        android.graphics.Color.WHITE,
                        16f
                    )

                elevation =
                    dp(3).toFloat()

                isClickable = true
                isFocusable = true

                addView(
                    android.widget.TextView(
                        this@MainActivity
                    ).apply {

                        text = icon

                        textSize = 30f

                        gravity =
                            android.view.Gravity.CENTER
                    }
                )

                addView(
                    android.widget.TextView(
                        this@MainActivity
                    ).apply {

                        text = title

                        textSize = 17f

                        gravity =
                            android.view.Gravity.CENTER

                        setTextColor(
                            dark
                        )

                        setTypeface(
                            typeface,
                            android.graphics.Typeface.BOLD
                        )

                        setPadding(
                            0,
                            dp(5),
                            0,
                            dp(2)
                        )
                    }
                )

                addView(
                    android.widget.TextView(
                        this@MainActivity
                    ).apply {

                        text = subtitle

                        textSize = 12f

                        gravity =
                            android.view.Gravity.CENTER

                        setTextColor(
                            android.graphics.Color.DKGRAY
                        )
                    }
                )

                setOnClickListener {
                    showScreen(route)
                }
            }
        }

        fun menuRow(
            left: android.view.View,
            right: android.view.View
        ): android.widget.LinearLayout {

            return android.widget.LinearLayout(this).apply {

                orientation =
                    android.widget.LinearLayout.HORIZONTAL

                val lp1 =
                    android.widget.LinearLayout.LayoutParams(
                        0,
                        dp(145),
                        1f
                    ).apply {
                        marginEnd =
                            dp(6)
                    }

                val lp2 =
                    android.widget.LinearLayout.LayoutParams(
                        0,
                        dp(145),
                        1f
                    ).apply {
                        marginStart =
                            dp(6)
                    }

                addView(left, lp1)
                addView(right, lp2)
            }
        }

        content.addView(
            menuRow(
                menuCard(
                    "✈",
                    "งานของฉัน",
                    "จัดการงานบิน",
                    AppRoute.JOBS
                ),
                menuCard(
                    "👤",
                    "ลูกค้า",
                    "ข้อมูลลูกค้า",
                    AppRoute.CUSTOMERS
                )
            )
        )

        val row2 =
            menuRow(
                menuCard(
                    "฿",
                    "การเงิน",
                    "รายรับ–รายจ่าย",
                    AppRoute.FINANCE
                ),
                menuCard(
                    "☰",
                    "ประวัติงาน",
                    "ดูงานที่ผ่านมา",
                    AppRoute.HISTORY
                )
            )

        row2.setPadding(
            0,
            dp(12),
            0,
            0
        )

        content.addView(
            row2
        )

        val row3 =
            android.widget.LinearLayout(this).apply {

                orientation =
                    android.widget.LinearLayout.HORIZONTAL

                setPadding(
                    0,
                    dp(12),
                    0,
                    0
                )

                val settingsCard =
                    menuCard(
                        "⚙",
                        "ตั้งค่า",
                        "ข้อมูลและระบบ",
                        AppRoute.SETTINGS
                    )

                addView(
                    settingsCard,
                    android.widget.LinearLayout.LayoutParams(
                        0,
                        dp(145),
                        1f
                    )
                )
            }

        content.addView(
            row3
        )

        // =========================================
        // TODAY SUMMARY
        // =========================================

        content.addView(
            android.widget.TextView(this).apply {

                text = "สรุปภาพรวม"

                textSize = 20f

                setTextColor(
                    dark
                )

                setTypeface(
                    typeface,
                    android.graphics.Typeface.BOLD
                )

                setPadding(
                    dp(2),
                    dp(22),
                    0,
                    dp(10)
                )
            }
        )

        val summary =
            android.widget.LinearLayout(this).apply {

                orientation =
                    android.widget.LinearLayout.VERTICAL

                setPadding(
                    dp(16),
                    dp(16),
                    dp(16),
                    dp(16)
                )

                background =
                    rounded(
                        lightGray,
                        18f
                    )
            }

        fun summaryRow(
            label: String,
            value: String
        ): android.widget.TextView {

            return android.widget.TextView(this).apply {

                text =
                    "$label     $value"

                textSize = 16f

                setTextColor(
                    dark
                )

                setPadding(
                    0,
                    dp(5),
                    0,
                    dp(5)
                )
            }
        }

        summary.addView(
            summaryRow(
                "พื้นที่บิน",
                "%.2f ไร่".format(totalRai)
            )
        )

        summary.addView(
            summaryRow(
                "งานเสร็จแล้ว",
                "$completedJobs งาน"
            )
        )

        summary.addView(
            summaryRow(
                "งานรอดำเนินการ",
                "$waitingJobs งาน"
            )
        )

        summary.addView(
            summaryRow(
                "รายรับรวม",
                "${moneyFormat.format(totalIncome)} บาท"
            )
        )

        summary.addView(
            summaryRow(
                "รายจ่ายรวม",
                "${moneyFormat.format(expense)} บาท"
            )
        )

        val balanceBox =
            android.widget.TextView(this).apply {

                text =
                    "เงินคงเหลือ  ${moneyFormat.format(balance)} บาท"

                textSize = 20f

                gravity =
                    android.view.Gravity.CENTER

                setTypeface(
                    typeface,
                    android.graphics.Typeface.BOLD
                )

                setTextColor(
                    android.graphics.Color.WHITE
                )

                setPadding(
                    dp(12),
                    dp(13),
                    dp(12),
                    dp(13)
                )

                background =
                    rounded(
                        if (
                            balance.compareTo(
                                java.math.BigDecimal.ZERO
                            ) >= 0
                        ) {
                            green
                        } else {
                            android.graphics.Color.rgb(
                                180,
                                35,
                                35
                            )
                        },
                        14f
                    )
            }

        val balanceParams =
            android.widget.LinearLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {

                topMargin =
                    dp(12)
            }

        summary.addView(
            balanceBox,
            balanceParams
        )

        content.addView(
            summary
        )

        content.addView(
            android.widget.TextView(this).apply {

                text =
                    "PC Drone • Agriculture Drone Service"

                textSize = 12f

                gravity =
                    android.view.Gravity.CENTER

                setTextColor(
                    android.graphics.Color.GRAY
                )

                setPadding(
                    0,
                    dp(24),
                    0,
                    0
                )
            }
        )

        root.addView(
            content
        )

        val scroll =
            android.widget.ScrollView(this).apply {

                setBackgroundColor(
                    softGreen
                )

                addView(
                    root,
                    android.view.ViewGroup.LayoutParams(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                )
            }

        setContentView(
            scroll
        )
    }



    private fun showReports() {

        val prefs = getSharedPreferences(
            "pc_drone_v3_data",
            android.content.Context.MODE_PRIVATE
        )

        val root = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(dp(20), dp(8), dp(20), dp(30))
            setBackgroundColor(android.graphics.Color.WHITE)
        }

        root.addView(createBackButton())

        root.addView(
            android.widget.TextView(this).apply {
                text = "รายงาน"
                textSize = 30f
                setTextColor(greenDark)
                setTypeface(
                    typeface,
                    android.graphics.Typeface.BOLD
                )
            }
        )

        root.addView(
            android.widget.TextView(this).apply {
                text = "สรุปรายวัน รายเดือน รายปี และแชร์เอกสาร"
                textSize = 15f
                setTextColor(android.graphics.Color.DKGRAY)
                setPadding(0, dp(4), 0, dp(14))
            }
        )

        val periodSpinner =
            android.widget.Spinner(this)

        val periodOptions =
            arrayOf(
                "วันนี้",
                "เดือนนี้",
                "ปีนี้"
            )

        periodSpinner.adapter =
            android.widget.ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                periodOptions
            )

        root.addView(
            android.widget.TextView(this).apply {
                text = "ช่วงรายงาน"
                textSize = 16f
                setTextColor(android.graphics.Color.BLACK)
                setPadding(0, dp(6), 0, dp(4))
            }
        )

        root.addView(periodSpinner)

        val reportText =
            android.widget.TextView(this).apply {
                textSize = 15f
                setTextColor(android.graphics.Color.BLACK)
                setPadding(
                    dp(10),
                    dp(14),
                    dp(10),
                    dp(14)
                )
            }

        fun inPeriod(
            time: Long,
            mode: String
        ): Boolean {

            val now =
                java.util.Calendar.getInstance()

            val item =
                java.util.Calendar.getInstance().apply {
                    timeInMillis = time
                }

            return when (mode) {

                "วันนี้" ->
                    now.get(java.util.Calendar.YEAR) ==
                        item.get(java.util.Calendar.YEAR) &&
                    now.get(java.util.Calendar.DAY_OF_YEAR) ==
                        item.get(java.util.Calendar.DAY_OF_YEAR)

                "เดือนนี้" ->
                    now.get(java.util.Calendar.YEAR) ==
                        item.get(java.util.Calendar.YEAR) &&
                    now.get(java.util.Calendar.MONTH) ==
                        item.get(java.util.Calendar.MONTH)

                else ->
                    now.get(java.util.Calendar.YEAR) ==
                        item.get(java.util.Calendar.YEAR)
            }
        }

        
        // PC_DRONE_DATE_RANGE_V1
        val reportDateFormat =
            java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale("th", "TH")).apply {
                isLenient = false
            }

        val todayForReport =
            reportDateFormat.format(java.util.Date())

        val reportFromDate =
            android.widget.EditText(this).apply {
                hint = "วว/ดด/ปปปป"
                setText(todayForReport)
                textSize = 17f
                inputType = android.text.InputType.TYPE_CLASS_DATETIME
                setPadding(dp(14), dp(10), dp(14), dp(10))
            }

        val reportToDate =
            android.widget.EditText(this).apply {
                hint = "วว/ดด/ปปปป"
                setText(todayForReport)
                textSize = 17f
                inputType = android.text.InputType.TYPE_CLASS_DATETIME
                setPadding(dp(14), dp(10), dp(14), dp(10))
            }

        fun selectedReportRange(): Pair<Long, Long>? {
            return try {
                val fromText = reportFromDate.text.toString().trim()
                val toText = reportToDate.text.toString().trim()

                val fromDate = reportDateFormat.parse(fromText)
                    ?: return null

                val toDate = reportDateFormat.parse(toText)
                    ?: return null

                val fromCal = java.util.Calendar.getInstance().apply {
                    time = fromDate
                    set(java.util.Calendar.HOUR_OF_DAY, 0)
                    set(java.util.Calendar.MINUTE, 0)
                    set(java.util.Calendar.SECOND, 0)
                    set(java.util.Calendar.MILLISECOND, 0)
                }

                val toCal = java.util.Calendar.getInstance().apply {
                    time = toDate
                    set(java.util.Calendar.HOUR_OF_DAY, 23)
                    set(java.util.Calendar.MINUTE, 59)
                    set(java.util.Calendar.SECOND, 59)
                    set(java.util.Calendar.MILLISECOND, 999)
                }

                if (fromCal.timeInMillis > toCal.timeInMillis) {
                    null
                } else {
                    Pair(fromCal.timeInMillis, toCal.timeInMillis)
                }

            } catch (_: Exception) {
                null
            }
        }

        fun buildReport(): String {

            val mode =
                periodSpinner.selectedItem.toString()

            val business =
                prefs.getString(
                    "setting_business_name",
                    "PC DRONE"
                ) ?: "PC DRONE"

            val title =
                prefs.getString(
                    "setting_report_title",
                    "สรุปรายรับ - รายจ่าย"
                ) ?: "สรุปรายรับ - รายจ่าย"

            val onlyCompleted =
                prefs.getBoolean(
                    "setting_only_completed_jobs",
                    true
                )

            val dateFormat =
                java.text.SimpleDateFormat(
                    "dd/MM/yyyy",
                    java.util.Locale.getDefault()
                )

            val jobs =
                prefs.getStringSet(
                    "flight_jobs",
                    emptySet()
                )?.toList() ?: emptyList()

            val finance =
                prefs.getStringSet(
                    "finance_transactions",
                    emptySet()
                )?.toList() ?: emptyList()

            val rows =
                mutableListOf<
                    Triple<Long, String, Pair<Double, Double>>
                >()

            var totalIncome = 0.0
            var totalExpense = 0.0
            var totalRai = 0.0
            var jobCount = 0

            jobs.forEach { record ->

                val p =
                    record.split(
                        "|||",
                        ignoreCase = false,
                        limit = 9
                    )

                if (p.size >= 8) {

                    val time =
                        p.getOrNull(0)
                            ?.toLongOrNull() ?: 0L

                    if (time <= 0L ||
                        !inPeriod(time, mode)
                    ) {
                        return@forEach
                    }

                    val customer =
                        p.getOrNull(1) ?: "-"

                    val service =
                        p.getOrNull(2) ?: "-"

                    val rai =
                        p.getOrNull(4)
                            ?.toDoubleOrNull() ?: 0.0

                    val total =
                        p.getOrNull(6)
                            ?.toDoubleOrNull() ?: 0.0

                    val status =
                        p.getOrNull(7) ?: ""

                    if (!onlyCompleted ||
                        status == "เสร็จแล้ว"
                    ) {

                        totalIncome += total
                        totalRai += rai
                        jobCount++

                        rows.add(
                            Triple(
                                time,
                                "$service - $customer",
                                Pair(total, 0.0)
                            )
                        )
                    }
                }
            }

            finance.forEach { record ->

                val p =
                    record.split(
                        "|||",
                        ignoreCase = false,
                        limit = 5
                    )

                if (p.size >= 4) {

                    val time =
                        p.getOrNull(0)
                            ?.toLongOrNull() ?: 0L

                    if (time <= 0L ||
                        !inPeriod(time, mode)
                    ) {
                        return@forEach
                    }

                    val type =
                        p.getOrNull(1) ?: "EXPENSE"

                    val category =
                        p.getOrNull(2) ?: "-"

                    val amount =
                        p.getOrNull(3)
                            ?.toDoubleOrNull() ?: 0.0

                    val note =
                        p.getOrNull(4) ?: ""

                    if (type == "INCOME") {
                        totalIncome += amount

                        rows.add(
                            Triple(
                                time,
                                if (note.isBlank())
                                    category
                                else
                                    "$category - $note",
                                Pair(amount, 0.0)
                            )
                        )

                    } else {

                        totalExpense += amount

                        rows.add(
                            Triple(
                                time,
                                if (note.isBlank())
                                    category
                                else
                                    "$category - $note",
                                Pair(0.0, amount)
                            )
                        )
                    }
                }
            }

            val openingBalance =
                prefs.getString(
                    "setting_opening_balance",
                    "0"
                )?.toDoubleOrNull() ?: 0.0

            val net =
                totalIncome - totalExpense

            val balance =
                openingBalance + net

            val sb =
                StringBuilder()

            sb.append("$business\n")
            sb.append("$title\n")
            sb.append("ช่วง: $mode\n")
            sb.append("--------------------------------\n")
            sb.append("วันที่ | รายการ | รายรับ | รายจ่าย\n")
            sb.append("--------------------------------\n")

            rows.sortedBy { it.first }
                .forEach { row ->

                    val date =
                        dateFormat.format(
                            java.util.Date(row.first)
                        )

                    sb.append(date)
                    sb.append(" | ")
                    sb.append(row.second)
                    sb.append(" | ")

                    if (row.third.first > 0.0) {
                        sb.append(
                            "%.2f".format(
                                row.third.first
                            )
                        )
                    } else {
                        sb.append("-")
                    }

                    sb.append(" | ")

                    if (row.third.second > 0.0) {
                        sb.append(
                            "%.2f".format(
                                row.third.second
                            )
                        )
                    } else {
                        sb.append("-")
                    }

                    sb.append("\n")
                }

            sb.append("--------------------------------\n")
            sb.append(
                "รวมรายรับ: %.2f บาท\n".format(
                    totalIncome
                )
            )

            sb.append(
                "รวมรายจ่าย: %.2f บาท\n".format(
                    totalExpense
                )
            )

            sb.append(
                "กำไรสุทธิ: %.2f บาท\n".format(
                    net
                )
            )

            sb.append(
                "ยอดคงเหลือ: %.2f บาท\n".format(
                    balance
                )
            )

            sb.append(
                "จำนวนงาน: $jobCount งาน\n"
            )

            sb.append(
                "พื้นที่บินรวม: %.2f ไร่\n".format(
                    totalRai
                )
            )

            return sb.toString()
        }

        fun refreshReport() {

            reportText.text =
                buildReport()
        }

        
        val reportDateTitle =
            android.widget.TextView(this).apply {
                text = "ช่วงวันที่รายงาน"
                textSize = 20f
                setTypeface(typeface, android.graphics.Typeface.BOLD)
                setPadding(0, dp(16), 0, dp(8))
            }

        val reportFromLabel =
            android.widget.TextView(this).apply {
                text = "จากวันที่"
                textSize = 16f
                setPadding(0, dp(6), 0, dp(4))
            }

        val reportToLabel =
            android.widget.TextView(this).apply {
                text = "ถึงวันที่"
                textSize = 16f
                setPadding(0, dp(10), 0, dp(4))
            }

        root.addView(reportDateTitle)
        root.addView(reportFromLabel)
        root.addView(reportFromDate)
        root.addView(reportToLabel)
        root.addView(reportToDate)

        val calculateButton =
            android.widget.Button(this).apply {

                text = "ประมวลผลรายงาน"
                textSize = 17f
                isAllCaps = false

                setOnClickListener {
                    refreshReport()
                }
            }

        root.addView(
            calculateButton,
            android.widget.LinearLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                dp(56)
            ).apply {
                topMargin = dp(12)
            }
        )

        root.addView(reportText)

        val shareButton =
            android.widget.Button(this).apply {

                text = "แชร์รายงานไปยังแอปอื่น"
                textSize = 17f
                isAllCaps = false

                setOnClickListener {

                    val report =
                        buildReport()

                    val intent =
                        android.content.Intent(
                            android.content.Intent.ACTION_SEND
                        ).apply {

                            type = "text/plain"

                            putExtra(
                                android.content.Intent.EXTRA_SUBJECT,
                                "รายงาน PC DRONE"
                            )

                            putExtra(
                                android.content.Intent.EXTRA_TEXT,
                                report
                            )
                        }

                    startActivity(
                        android.content.Intent.createChooser(
                            intent,
                            "แชร์รายงาน"
                        )
                    )
                }
            }

        root.addView(
            shareButton,
            android.widget.LinearLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                dp(56)
            ).apply {
                topMargin = dp(8)
            }
        )

        refreshReport()

        val scroll =
            android.widget.ScrollView(this).apply {

                addView(
                    root,
                    android.view.ViewGroup.LayoutParams(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                )
            }

        setContentView(scroll)
    }



    private fun showSettings() {

        val root =
            android.widget.LinearLayout(this).apply {

                orientation =
                    android.widget.LinearLayout.VERTICAL

                setPadding(
                    dp(18),
                    dp(8),
                    dp(18),
                    dp(32)
                )

                setBackgroundColor(
                    android.graphics.Color.WHITE
                )
            }

        root.addView(
            createBackButton()
        )

        root.addView(
            android.widget.TextView(this).apply {

                text = "ตั้งค่า"

                textSize = 30f

                setTypeface(
                    typeface,
                    android.graphics.Typeface.BOLD
                )

                setTextColor(
                    greenDark
                )

                setPadding(
                    0,
                    dp(8),
                    0,
                    dp(4)
                )
            }
        )

        root.addView(
            android.widget.TextView(this).apply {

                text = "ศูนย์ตั้งค่ารายงาน PC DRONE"

                textSize = 17f

                setTextColor(
                    android.graphics.Color.DKGRAY
                )

                setPadding(
                    0,
                    0,
                    0,
                    dp(18)
                )
            }
        )

        fun sectionTitle(
            title: String
        ): android.widget.TextView {

            return android.widget.TextView(this).apply {

                text = title

                textSize = 21f

                setTypeface(
                    typeface,
                    android.graphics.Typeface.BOLD
                )

                setTextColor(
                    android.graphics.Color.BLACK
                )

                setPadding(
                    dp(4),
                    dp(12),
                    dp(4),
                    dp(10)
                )
            }
        }

        root.addView(
            sectionTitle(
                "ตั้งค่ารายงาน"
            )
        )

        val reportCard =
            android.widget.LinearLayout(this).apply {

                orientation =
                    android.widget.LinearLayout.VERTICAL

                setPadding(
                    dp(16),
                    dp(16),
                    dp(16),
                    dp(16)
                )

                background =
                    android.graphics.drawable.GradientDrawable()
                        .apply {

                            setColor(
                                android.graphics.Color.rgb(
                                    246,
                                    250,
                                    247
                                )
                            )

                            setStroke(
                                dp(1),
                                android.graphics.Color.rgb(
                                    175,
                                    195,
                                    180
                                )
                            )

                            cornerRadius =
                                dp(12).toFloat()
                        }
            }

        reportCard.addView(
            android.widget.TextView(this).apply {

                text = "รูปแบบเอกสาร"

                textSize = 16f

                setTypeface(
                    typeface,
                    android.graphics.Typeface.BOLD
                )

                setTextColor(
                    android.graphics.Color.BLACK
                )
            }
        )

        reportCard.addView(
            android.widget.TextView(this).apply {

                text = "A4 • แนวตั้ง"

                textSize = 17f

                setTextColor(
                    greenDark
                )

                setPadding(
                    0,
                    dp(5),
                    0,
                    dp(16)
                )
            }
        )

        reportCard.addView(
            android.widget.TextView(this).apply {

                text = "ช่วงเวลาสรุป"

                textSize = 16f

                setTypeface(
                    typeface,
                    android.graphics.Typeface.BOLD
                )

                setTextColor(
                    android.graphics.Color.BLACK
                )
            }
        )

        val periodSpinner =
            android.widget.Spinner(this)

        periodSpinner.adapter =
            android.widget.ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                arrayOf(
                    "รายวัน",
                    "รายเดือน",
                    "รายปี"
                )
            )

        reportCard.addView(
            periodSpinner
        )

        val includeIncome =
            android.widget.CheckBox(this).apply {

                text = "แสดงรายรับ"

                isChecked = true

                textSize = 16f

                setTextColor(
                    android.graphics.Color.BLACK
                )
            }

        val includeExpense =
            android.widget.CheckBox(this).apply {

                text = "แสดงรายจ่าย"

                isChecked = true

                textSize = 16f

                setTextColor(
                    android.graphics.Color.BLACK
                )
            }

        val includeBalance =
            android.widget.CheckBox(this).apply {

                text = "แสดงยอดคงเหลือ"

                isChecked = true

                textSize = 16f

                setTextColor(
                    android.graphics.Color.BLACK
                )
            }

        reportCard.addView(
            includeIncome
        )

        reportCard.addView(
            includeExpense
        )

        reportCard.addView(
            includeBalance
        )

        root.addView(
            reportCard
        )

        root.addView(
            sectionTitle(
                "รูปแบบตาราง A4"
            )
        )

        val preview =
            android.widget.LinearLayout(this).apply {

                orientation =
                    android.widget.LinearLayout.VERTICAL

                setPadding(
                    dp(12),
                    dp(12),
                    dp(12),
                    dp(12)
                )

                background =
                    android.graphics.drawable.GradientDrawable()
                        .apply {

                            setColor(
                                android.graphics.Color.WHITE
                            )

                            setStroke(
                                dp(2),
                                android.graphics.Color.rgb(
                                    210,
                                    210,
                                    210
                                )
                            )

                            cornerRadius =
                                dp(5).toFloat()
                        }
            }

        preview.addView(
            android.widget.TextView(this).apply {

                text = "PC DRONE"

                gravity =
                    android.view.Gravity.CENTER

                textSize = 20f

                setTypeface(
                    typeface,
                    android.graphics.Typeface.BOLD
                )

                setTextColor(
                    android.graphics.Color.BLACK
                )
            }
        )

        preview.addView(
            android.widget.TextView(this).apply {

                text = "รายงานสรุปการเงิน"

                gravity =
                    android.view.Gravity.CENTER

                textSize = 16f

                setTextColor(
                    android.graphics.Color.BLACK
                )

                setPadding(
                    0,
                    dp(2),
                    0,
                    dp(14)
                )
            }
        )

        val tableHeader =
            android.widget.TextView(this).apply {

                text =
                    "วันที่   |   รายการ   |   รายรับ   |   รายจ่าย   |   คงเหลือ"

                textSize = 13f

                setTypeface(
                    typeface,
                    android.graphics.Typeface.BOLD
                )

                setTextColor(
                    android.graphics.Color.BLACK
                )

                setPadding(
                    dp(5),
                    dp(9),
                    dp(5),
                    dp(9)
                )

                background =
                    android.graphics.drawable.GradientDrawable()
                        .apply {

                            setColor(
                                android.graphics.Color.rgb(
                                    235,
                                    240,
                                    236
                                )
                            )
                        }
            }

        preview.addView(
            tableHeader
        )

        preview.addView(
            android.widget.TextView(this).apply {

                text =
                    "\nรายการการเงินจริงจะถูกนำมาแสดงในตารางนี้\n" +
                    "ตามช่วงเวลาที่เลือก\n"

                gravity =
                    android.view.Gravity.CENTER

                textSize = 14f

                setTextColor(
                    android.graphics.Color.GRAY
                )
            }
        )

        preview.addView(
            android.widget.TextView(this).apply {

                text =
                    "รวมรายรับ        0.00 บาท\n" +
                    "รวมรายจ่าย       0.00 บาท\n" +
                    "ยอดคงเหลือ       0.00 บาท"

                gravity =
                    android.view.Gravity.END

                textSize = 15f

                setTypeface(
                    typeface,
                    android.graphics.Typeface.BOLD
                )

                setTextColor(
                    android.graphics.Color.BLACK
                )

                setPadding(
                    dp(5),
                    dp(12),
                    dp(5),
                    dp(8)
                )
            }
        )

        val horizontal =
            android.widget.HorizontalScrollView(this).apply {

                isFillViewport = true

                addView(
                    preview,
                    android.widget.FrameLayout.LayoutParams(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                )
            }

        root.addView(
            horizontal
        )

        fun greenButton(
            label: String,
            action: () -> Unit
        ): android.widget.Button {

            return android.widget.Button(this).apply {

                text = label

                textSize = 17f

                isAllCaps = false

                setTextColor(
                    android.graphics.Color.WHITE
                )

                elevation =
                    dp(5).toFloat()

                background =
                    android.graphics.drawable.GradientDrawable()
                        .apply {

                            setColor(
                                greenDark
                            )

                            setStroke(
                                dp(2),
                                android.graphics.Color.rgb(
                                    4,
                                    75,
                                    30
                                )
                            )

                            cornerRadius =
                                dp(10).toFloat()
                        }

                setOnClickListener {
                    action()
                }
            }
        }

        root.addView(
            greenButton(
                "บันทึกการตั้งค่ารายงาน"
            ) {

                val prefs =
                    getSharedPreferences(
                        "pc_drone_settings",
                        android.content.Context.MODE_PRIVATE
                    )

                prefs.edit()
                    .putInt(
                        "report_period",
                        periodSpinner.selectedItemPosition
                    )
                    .putBoolean(
                        "report_income",
                        includeIncome.isChecked
                    )
                    .putBoolean(
                        "report_expense",
                        includeExpense.isChecked
                    )
                    .putBoolean(
                        "report_balance",
                        includeBalance.isChecked
                    )
                    .apply()

                android.widget.Toast.makeText(
                    this,
                    "บันทึกการตั้งค่ารายงานแล้ว",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            },
            android.widget.LinearLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                dp(58)
            ).apply {

                topMargin =
                    dp(18)
            }
        )

        /*
         * Phase 2.3 มี showReports() อยู่แล้ว
         * ปุ่มนี้เรียกฟังก์ชันโดยตรง
         * ไม่ผ่าน AppRoute จึงไม่เกิดปัญหา route เดิม
         */
        root.addView(
            greenButton(
                "เปิดตารางสรุปและแชร์เอกสาร"
            ) {

                showReports()
            },
            android.widget.LinearLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                dp(62)
            ).apply {

                topMargin =
                    dp(10)
            }
        )

        root.addView(
            android.widget.TextView(this).apply {

                text =
                    "รายงานใช้ข้อมูลจากรายการการเงินที่บันทึกอยู่ในแอป\n" +
                    "สามารถสร้าง PDF / CSV และแชร์ไปยังแอปอื่นได้"

                textSize = 14f

                gravity =
                    android.view.Gravity.CENTER

                setTextColor(
                    android.graphics.Color.DKGRAY
                )

                setPadding(
                    dp(4),
                    dp(15),
                    dp(4),
                    dp(8)
                )
            }
        )

        val scroll =
            android.widget.ScrollView(this).apply {

                addView(
                    root,
                    android.view.ViewGroup.LayoutParams(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                )
            }

        setContentView(
            scroll
        )
    }


    private fun showPlaceholder(route: AppRoute) {

        if (route == AppRoute.REPORTS) {
            showReports()
            return
        }


        if (route == AppRoute.SETTINGS) {
            showSettings()
            return
        }

        if (route == AppRoute.FINANCE) {
            showFinance()
            return
        }

        if (route == AppRoute.HISTORY) {
            showJobHistory()
            return
        }

        if (route == AppRoute.JOBS) {
            showJobs()
            return
        }

        if (route == AppRoute.CUSTOMERS) {
            showCustomers()
            return
        }
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


    private fun showCustomers() {

        val prefs = getSharedPreferences(
            "pc_drone_v3_data",
            android.content.Context.MODE_PRIVATE
        )

        val root = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(dp(20), dp(8), dp(20), dp(28))
            setBackgroundColor(android.graphics.Color.WHITE)
        }

        root.addView(createBackButton())

        root.addView(
            android.widget.TextView(this).apply {
                text = "ประวัติลูกค้า"
                textSize = 28f
                setTextColor(android.graphics.Color.BLACK)
                setTypeface(
                    typeface,
                    android.graphics.Typeface.BOLD
                )
            }
        )

        root.addView(
            android.widget.TextView(this).apply {
                text = "ข้อมูลจะถูกเก็บไว้จนกว่าจะกดแก้ไขหรือลบ"
                textSize = 15f
                setTextColor(android.graphics.Color.DKGRAY)
                setPadding(0, dp(5), 0, dp(16))
            }
        )

        val nameInput =
            android.widget.EditText(this).apply {
                hint = "ชื่อลูกค้า"
                textSize = 17f
                setTextColor(android.graphics.Color.BLACK)
                setHintTextColor(android.graphics.Color.GRAY)
            }

        val phoneInput =
            android.widget.EditText(this).apply {
                hint = "เบอร์โทร"
                textSize = 17f
                inputType =
                    android.text.InputType.TYPE_CLASS_PHONE
                setTextColor(android.graphics.Color.BLACK)
                setHintTextColor(android.graphics.Color.GRAY)
            }

        val areaInput =
            android.widget.EditText(this).apply {
                hint = "พื้นที่ / สวน / ตำบล / อำเภอ"
                textSize = 17f
                setTextColor(android.graphics.Color.BLACK)
                setHintTextColor(android.graphics.Color.GRAY)
            }

        root.addView(nameInput)
        root.addView(phoneInput)
        root.addView(areaInput)

        val customerList =
            android.widget.LinearLayout(this).apply {
                orientation =
                    android.widget.LinearLayout.VERTICAL
                setPadding(0, dp(18), 0, 0)
            }

        fun reloadCustomers() {

            customerList.removeAllViews()

            val records =
                prefs.getStringSet(
                    "customers",
                    emptySet()
                )?.toList()
                    ?: emptyList()

            val sorted =
                records.sortedByDescending { record ->
                    record.split(
                        "|||",
                        ignoreCase = false,
                        limit = 4
                    ).getOrNull(0)
                        ?.toLongOrNull()
                        ?: 0L
                }

            if (sorted.isEmpty()) {

                customerList.addView(
                    android.widget.TextView(
                        this@MainActivity
                    ).apply {

                        text = "ยังไม่มีประวัติลูกค้า"
                        textSize = 17f
                        setTextColor(
                            android.graphics.Color.GRAY
                        )
                        setPadding(
                            dp(4),
                            dp(20),
                            dp(4),
                            dp(20)
                        )
                    }
                )

                return
            }

            sorted.forEach { record ->

                val parts =
                    record.split(
                        "|||",
                        ignoreCase = false,
                        limit = 4
                    )

                if (parts.size < 2) {
                    return@forEach
                }

                val time =
                    parts.getOrNull(0)
                        ?.toLongOrNull()
                        ?: System.currentTimeMillis()

                val name =
                    parts.getOrNull(1)
                        ?: "-"

                val phone =
                    parts.getOrNull(2)
                        ?: ""

                val area =
                    parts.getOrNull(3)
                        ?: ""

                val card =
                    android.widget.LinearLayout(
                        this@MainActivity
                    ).apply {

                        orientation =
                            android.widget.LinearLayout.VERTICAL

                        setPadding(
                            dp(15),
                            dp(14),
                            dp(15),
                            dp(14)
                        )

                        background =
                            android.graphics.drawable.GradientDrawable().apply {
                                setColor(
                                    android.graphics.Color.rgb(
                                        248,
                                        250,
                                        248
                                    )
                                )
                                setStroke(
                                    dp(1),
                                    android.graphics.Color.rgb(
                                        190,
                                        205,
                                        193
                                    )
                                )
                                cornerRadius =
                                    dp(10).toFloat()
                            }

                        elevation = dp(3).toFloat()

                        layoutParams =
                            android.widget.LinearLayout.LayoutParams(
                                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                            ).apply {
                                bottomMargin = dp(14)
                            }
                    }

                card.addView(
                    android.widget.TextView(
                        this@MainActivity
                    ).apply {

                        text = name
                        textSize = 20f
                        setTextColor(
                            android.graphics.Color.BLACK
                        )
                        setTypeface(
                            typeface,
                            android.graphics.Typeface.BOLD
                        )
                    }
                )

                card.addView(
                    android.widget.TextView(
                        this@MainActivity
                    ).apply {

                        text =
                            "เบอร์โทร: ${if (phone.isBlank()) "-" else phone}\n" +
                            "พื้นที่: ${if (area.isBlank()) "-" else area}"

                        textSize = 16f
                        setTextColor(
                            android.graphics.Color.DKGRAY
                        )

                        setPadding(
                            0,
                            dp(6),
                            0,
                            dp(10)
                        )
                    }
                )

                val buttonRow =
                    android.widget.LinearLayout(
                        this@MainActivity
                    ).apply {
                        orientation =
                            android.widget.LinearLayout.HORIZONTAL
                    }

                val editButton =
                    android.widget.Button(
                        this@MainActivity
                    ).apply {

                        text = "แก้ไข"
                        isAllCaps = false

                        setOnClickListener {

                            val form =
                                android.widget.LinearLayout(
                                    this@MainActivity
                                ).apply {

                                    orientation =
                                        android.widget.LinearLayout.VERTICAL

                                    setPadding(
                                        dp(18),
                                        dp(8),
                                        dp(18),
                                        dp(4)
                                    )
                                }

                            val editName =
                                android.widget.EditText(
                                    this@MainActivity
                                ).apply {
                                    hint = "ชื่อลูกค้า"
                                    setText(name)
                                }

                            val editPhone =
                                android.widget.EditText(
                                    this@MainActivity
                                ).apply {
                                    hint = "เบอร์โทร"
                                    setText(phone)
                                    inputType =
                                        android.text.InputType.TYPE_CLASS_PHONE
                                }

                            val editArea =
                                android.widget.EditText(
                                    this@MainActivity
                                ).apply {
                                    hint =
                                        "พื้นที่ / สวน / ตำบล / อำเภอ"
                                    setText(area)
                                }

                            form.addView(editName)
                            form.addView(editPhone)
                            form.addView(editArea)

                            val dialog =
                                android.app.AlertDialog.Builder(
                                    this@MainActivity
                                )
                                    .setTitle(
                                        "แก้ไขข้อมูลลูกค้า"
                                    )
                                    .setView(form)
                                    .setNegativeButton(
                                        "ยกเลิก",
                                        null
                                    )
                                    .setPositiveButton(
                                        "บันทึก",
                                        null
                                    )
                                    .create()

                            dialog.setOnShowListener {

                                dialog.getButton(
                                    android.app.AlertDialog.BUTTON_POSITIVE
                                ).setOnClickListener {

                                    val newName =
                                        editName.text
                                            .toString()
                                            .trim()

                                    val newPhone =
                                        editPhone.text
                                            .toString()
                                            .trim()

                                    val newArea =
                                        editArea.text
                                            .toString()
                                            .trim()

                                    if (newName.isBlank()) {
                                        editName.error =
                                            "กรุณากรอกชื่อลูกค้า"
                                        return@setOnClickListener
                                    }

                                    val current =
                                        prefs.getStringSet(
                                            "customers",
                                            emptySet()
                                        )?.toMutableSet()
                                            ?: mutableSetOf()

                                    val updated =
                                        listOf(
                                            time.toString(),
                                            newName,
                                            newPhone,
                                            newArea
                                        ).joinToString(
                                            "|||"
                                        )

                                    current.remove(record)
                                    current.add(updated)

                                    prefs.edit()
                                        .putStringSet(
                                            "customers",
                                            current
                                        )
                                        .apply()

                                    dialog.dismiss()

                                    android.widget.Toast
                                        .makeText(
                                            this@MainActivity,
                                            "แก้ไขข้อมูลลูกค้าแล้ว",
                                            android.widget.Toast.LENGTH_SHORT
                                        )
                                        .show()

                                    reloadCustomers()
                                }
                            }

                            dialog.show()
                        }
                    }

                val deleteButton =
                    android.widget.Button(
                        this@MainActivity
                    ).apply {

                        text = "ลบ"
                        isAllCaps = false

                        setOnClickListener {

                            android.app.AlertDialog.Builder(
                                this@MainActivity
                            )
                                .setTitle(
                                    "ยืนยันการลบ"
                                )
                                .setMessage(
                                    "ต้องการลบข้อมูลลูกค้า $name ใช่หรือไม่?"
                                )
                                .setNegativeButton(
                                    "ยกเลิก",
                                    null
                                )
                                .setPositiveButton(
                                    "ลบ"
                                ) { _, _ ->

                                    val current =
                                        prefs.getStringSet(
                                            "customers",
                                            emptySet()
                                        )?.toMutableSet()
                                            ?: mutableSetOf()

                                    current.remove(record)

                                    prefs.edit()
                                        .putStringSet(
                                            "customers",
                                            current
                                        )
                                        .apply()

                                    android.widget.Toast
                                        .makeText(
                                            this@MainActivity,
                                            "ลบข้อมูลลูกค้าแล้ว",
                                            android.widget.Toast.LENGTH_SHORT
                                        )
                                        .show()

                                    reloadCustomers()
                                }
                                .show()
                        }
                    }

                buttonRow.addView(
                    editButton,
                    android.widget.LinearLayout.LayoutParams(
                        0,
                        dp(50),
                        1f
                    ).apply {
                        marginEnd = dp(5)
                    }
                )

                buttonRow.addView(
                    deleteButton,
                    android.widget.LinearLayout.LayoutParams(
                        0,
                        dp(50),
                        1f
                    ).apply {
                        marginStart = dp(5)
                    }
                )

                card.addView(buttonRow)
                customerList.addView(card)
            }
        }

        val addButton =
            android.widget.Button(this).apply {

                text = "บันทึกลูกค้า"
                textSize = 18f
                isAllCaps = false
                elevation = dp(4).toFloat()

                setOnClickListener {

                    val name =
                        nameInput.text
                            .toString()
                            .trim()

                    val phone =
                        phoneInput.text
                            .toString()
                            .trim()

                    val area =
                        areaInput.text
                            .toString()
                            .trim()

                    if (name.isBlank()) {
                        nameInput.error =
                            "กรุณากรอกชื่อลูกค้า"
                        return@setOnClickListener
                    }

                    val record =
                        listOf(
                            System.currentTimeMillis()
                                .toString(),
                            name,
                            phone,
                            area
                        ).joinToString(
                            "|||"
                        )

                    val current =
                        prefs.getStringSet(
                            "customers",
                            emptySet()
                        )?.toMutableSet()
                            ?: mutableSetOf()

                    current.add(record)

                    prefs.edit()
                        .putStringSet(
                            "customers",
                            current
                        )
                        .apply()

                    nameInput.text.clear()
                    phoneInput.text.clear()
                    areaInput.text.clear()

                    android.widget.Toast
                        .makeText(
                            this@MainActivity,
                            "บันทึกลูกค้าเรียบร้อย",
                            android.widget.Toast.LENGTH_SHORT
                        )
                        .show()

                    reloadCustomers()
                }
            }

        root.addView(
            addButton,
            android.widget.LinearLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                dp(56)
            ).apply {
                topMargin = dp(14)
            }
        )

        root.addView(customerList)

        reloadCustomers()

        val scrollView =
            android.widget.ScrollView(this).apply {

                addView(
                    root,
                    android.view.ViewGroup.LayoutParams(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                )
            }

        setContentView(scrollView)
    }


    private fun showJobs() {

        val prefs = getSharedPreferences(
            "pc_drone_v3_data",
            android.content.Context.MODE_PRIVATE
        )

        val root = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(dp(20), dp(20), dp(20), dp(28))
            setBackgroundColor(android.graphics.Color.WHITE)
        }

        // ---------- TITLE ----------
        root.addView(createBackButton())

        root.addView(
            android.widget.TextView(this).apply {
                text = "บันทึกงานบิน"
                textSize = 28f
                setTextColor(android.graphics.Color.BLACK)
                setTypeface(
                    typeface,
                    android.graphics.Typeface.BOLD
                )
            }
        )

        root.addView(
            android.widget.TextView(this).apply {
                text = "กรอกรายละเอียดงานให้ครบก่อนบันทึก"
                textSize = 15f
                setTextColor(android.graphics.Color.DKGRAY)
                setPadding(0, dp(4), 0, dp(16))
            }
        )

        // ---------- CUSTOMER ----------
        val customerInput = android.widget.EditText(this).apply {
            hint = "ชื่อลูกค้า"
            textSize = 17f
            setTextColor(android.graphics.Color.BLACK)
            setHintTextColor(android.graphics.Color.GRAY)
        }

        // ---------- SERVICE ----------
        val serviceLabel = android.widget.TextView(this).apply {
            text = "ประเภทงาน"
            textSize = 16f
            setTextColor(android.graphics.Color.BLACK)
            setPadding(0, dp(12), 0, dp(4))
        }

        val serviceSpinner = android.widget.Spinner(this)

        val services = arrayOf(
            "พ่นยา",
            "หว่านปุ๋ย",
            "หว่านเมล็ด",
            "พ่นสวนผลไม้",
            "งานอื่น ๆ"
        )

        serviceSpinner.adapter =
            android.widget.ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                services
            )

        // ---------- LOCATION ----------
        val locationInput = android.widget.EditText(this).apply {
            hint = "พื้นที่ / สวน / ตำบล / อำเภอ"
            textSize = 17f
            setTextColor(android.graphics.Color.BLACK)
            setHintTextColor(android.graphics.Color.GRAY)
        }

        // ---------- RAI ----------
        val raiInput = android.widget.EditText(this).apply {
            hint = "จำนวนไร่"
            textSize = 17f
            inputType =
                android.text.InputType.TYPE_CLASS_NUMBER or
                android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            setTextColor(android.graphics.Color.BLACK)
            setHintTextColor(android.graphics.Color.GRAY)
        }

        // ---------- RATE ----------
        val rateInput = android.widget.EditText(this).apply {
            hint = "ค่าบริการต่อไร่ เช่น 350"
            textSize = 17f
            inputType =
                android.text.InputType.TYPE_CLASS_NUMBER or
                android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            setTextColor(android.graphics.Color.BLACK)
            setHintTextColor(android.graphics.Color.GRAY)
        }

        // ---------- STATUS ----------
        val statusLabel = android.widget.TextView(this).apply {
            text = "สถานะงาน"
            textSize = 16f
            setTextColor(android.graphics.Color.BLACK)
            setPadding(0, dp(12), 0, dp(4))
        }

        val statusSpinner = android.widget.Spinner(this)

        val statuses = arrayOf(
            "รอทำงาน",
            "กำลังดำเนินงาน",
            "เสร็จแล้ว",
            "ยกเลิก"
        )

        statusSpinner.adapter =
            android.widget.ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                statuses
            )

        // ---------- NOTES ----------
        val noteInput = android.widget.EditText(this).apply {
            hint = "หมายเหตุ เช่น ชนิดพืช ยาที่ใช้ หรือรายละเอียดเพิ่มเติม"
            textSize = 16f
            minLines = 2
            setTextColor(android.graphics.Color.BLACK)
            setHintTextColor(android.graphics.Color.GRAY)
        }

        // ---------- TOTAL ----------
        val totalText = android.widget.TextView(this).apply {
            text = "ยอดค่าบริการ: 0.00 บาท"
            textSize = 22f
            setTextColor(android.graphics.Color.rgb(0, 110, 55))
            setTypeface(
                typeface,
                android.graphics.Typeface.BOLD
            )
            setPadding(0, dp(18), 0, dp(12))
        }

        fun calculateTotal(): Double {

            val rai =
                raiInput.text.toString()
                    .trim()
                    .toDoubleOrNull() ?: 0.0

            val rate =
                rateInput.text.toString()
                    .trim()
                    .toDoubleOrNull() ?: 0.0

            val total = rai * rate

            totalText.text =
                "ยอดค่าบริการ: %.2f บาท".format(total)

            return total
        }

        val calculateButton =
            android.widget.Button(this).apply {

                text = "คำนวณค่าบริการ"
                textSize = 17f

                setOnClickListener {
                    calculateTotal()
                }
            }

        // ---------- SAVED RESULT ----------
        val resultText = android.widget.TextView(this).apply {
            text = ""
            textSize = 15f
            setTextColor(android.graphics.Color.DKGRAY)
            setPadding(0, dp(12), 0, dp(12))
        }

        // ---------- SAVE ----------
        val saveButton =
            android.widget.Button(this).apply {

                text = "บันทึกงาน"
                textSize = 18f

                setOnClickListener {

                    val customer =
                        customerInput.text.toString().trim()

                    val location =
                        locationInput.text.toString().trim()

                    val rai =
                        raiInput.text.toString()
                            .trim()
                            .toDoubleOrNull()

                    val rate =
                        rateInput.text.toString()
                            .trim()
                            .toDoubleOrNull()

                    if (customer.isEmpty()) {
                        customerInput.error =
                            "กรุณากรอกชื่อลูกค้า"
                        return@setOnClickListener
                    }

                    if (rai == null || rai <= 0.0) {
                        raiInput.error =
                            "กรุณากรอกจำนวนไร่ให้ถูกต้อง"
                        return@setOnClickListener
                    }

                    if (rate == null || rate < 0.0) {
                        rateInput.error =
                            "กรุณากรอกราคาต่อไร่ให้ถูกต้อง"
                        return@setOnClickListener
                    }

                    val service =
                        serviceSpinner.selectedItem.toString()

                    val status =
                        statusSpinner.selectedItem.toString()

                    val note =
                        noteInput.text.toString().trim()

                    val total = calculateTotal()

                    val time =
                        System.currentTimeMillis()

                    /*
                     * ใช้ตัวคั่นที่ควบคุมเอง
                     * เพื่อให้ชุดถัดไปสามารถอ่านข้อมูล
                     * และเชื่อมเข้าประวัติ/การเงินได้
                     */
                    val record =
                        listOf(
                            time.toString(),
                            customer,
                            service,
                            location,
                            rai.toString(),
                            rate.toString(),
                            total.toString(),
                            status,
                            note
                        ).joinToString("|||")

                    val oldSet =
                        prefs.getStringSet(
                            "flight_jobs",
                            emptySet()
                        )?.toMutableSet()
                            ?: mutableSetOf()

                    oldSet.add(record)

                    prefs.edit()
                        .putStringSet(
                            "flight_jobs",
                            oldSet
                        )
                        .apply()

                    resultText.text =
                        "บันทึกสำเร็จ\n" +
                        "ลูกค้า: $customer\n" +
                        "งาน: $service\n" +
                        "จำนวน: %.2f ไร่\n".format(rai) +
                        "ราคา: %.2f บาท/ไร่\n".format(rate) +
                        "รวม: %.2f บาท\n".format(total) +
                        "สถานะ: $status"

                    android.widget.Toast.makeText(
                        this@MainActivity,
                        "บันทึกงานเรียบร้อย",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()

                    customerInput.text.clear()
                    locationInput.text.clear()
                    raiInput.text.clear()
                    rateInput.text.clear()
                    noteInput.text.clear()

                    totalText.text =
                        "ยอดค่าบริการ: 0.00 บาท"
                }
            }

        root.addView(customerInput)

        root.addView(serviceLabel)
        root.addView(serviceSpinner)

        root.addView(locationInput)
        root.addView(raiInput)
        root.addView(rateInput)

        root.addView(statusLabel)
        root.addView(statusSpinner)

        root.addView(noteInput)

        root.addView(totalText)
        root.addView(calculateButton)
        root.addView(saveButton)
        root.addView(resultText)

        val scrollView =
            android.widget.ScrollView(this).apply {

                addView(
                    root,
                    android.view.ViewGroup.LayoutParams(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                )
            }

        setContentView(scrollView)
    }


    private fun showJobHistory() {

        val prefs = getSharedPreferences(
            "pc_drone_v3_data",
            android.content.Context.MODE_PRIVATE
        )

        val root = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(dp(18), dp(18), dp(18), dp(28))
            setBackgroundColor(android.graphics.Color.WHITE)
        }

        root.addView(createBackButton())

        root.addView(
            android.widget.TextView(this).apply {
                text = "ประวัติงาน"
                textSize = 28f
                setTextColor(android.graphics.Color.BLACK)
                setTypeface(
                    typeface,
                    android.graphics.Typeface.BOLD
                )
            }
        )

        val summaryText =
            android.widget.TextView(this).apply {
                textSize = 17f
                setTextColor(
                    android.graphics.Color.rgb(
                        0,
                        105,
                        55
                    )
                )
                setPadding(
                    0,
                    dp(6),
                    0,
                    dp(16)
                )
            }

        root.addView(summaryText)

        val listContainer =
            android.widget.LinearLayout(this).apply {
                orientation =
                    android.widget.LinearLayout.VERTICAL
            }

        root.addView(listContainer)

        fun reloadHistory() {

            listContainer.removeAllViews()

            val records =
                prefs.getStringSet(
                    "flight_jobs",
                    emptySet()
                )?.toList()
                    ?: emptyList()

            val sortedRecords =
                records.sortedByDescending { record ->

                    val parts =
                        record.split(
                            "|||",
                            ignoreCase = false,
                            limit = 9
                        )

                    parts.getOrNull(0)
                        ?.toLongOrNull()
                        ?: 0L
                }

            var totalRai = 0.0
            var totalValue = 0.0

            sortedRecords.forEach { record ->

                val parts =
                    record.split(
                        "|||",
                        ignoreCase = false,
                        limit = 9
                    )

                if (parts.size < 8) {
                    return@forEach
                }

                val time =
                    parts.getOrNull(0)
                        ?.toLongOrNull()
                        ?: 0L

                val customer =
                    parts.getOrNull(1)
                        ?: "-"

                val service =
                    parts.getOrNull(2)
                        ?: "-"

                val location =
                    parts.getOrNull(3)
                        ?: "-"

                val rai =
                    parts.getOrNull(4)
                        ?.toDoubleOrNull()
                        ?: 0.0

                val rate =
                    parts.getOrNull(5)
                        ?.toDoubleOrNull()
                        ?: 0.0

                val total =
                    parts.getOrNull(6)
                        ?.toDoubleOrNull()
                        ?: 0.0

                val status =
                    parts.getOrNull(7)
                        ?: "-"

                val note =
                    parts.getOrNull(8)
                        ?: ""

                totalRai += rai
                totalValue += total

                val dateText =
                    if (time > 0L) {
                        java.text.SimpleDateFormat(
                            "dd/MM/yyyy HH:mm",
                            java.util.Locale.getDefault()
                        ).format(
                            java.util.Date(time)
                        )
                    } else {
                        "-"
                    }

                val card =
                    android.widget.LinearLayout(
                        this@MainActivity
                    ).apply {

                        orientation =
                            android.widget.LinearLayout.VERTICAL

                        setPadding(
                            dp(14),
                            dp(14),
                            dp(14),
                            dp(14)
                        )

                        setBackgroundColor(
                            android.graphics.Color.rgb(
                                245,
                                247,
                                245
                            )
                        )

                        val params =
                            android.widget.LinearLayout.LayoutParams(
                                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                            )

                        params.setMargins(
                            0,
                            0,
                            0,
                            dp(12)
                        )

                        layoutParams = params
                    }

                card.addView(
                    android.widget.TextView(
                        this@MainActivity
                    ).apply {

                        text = customer
                        textSize = 20f

                        setTextColor(
                            android.graphics.Color.BLACK
                        )

                        setTypeface(
                            typeface,
                            android.graphics.Typeface.BOLD
                        )
                    }
                )

                card.addView(
                    android.widget.TextView(
                        this@MainActivity
                    ).apply {

                        text =
                            "วันที่: $dateText\n" +
                            "งาน: $service\n" +
                            "พื้นที่: $location\n" +
                            "จำนวน: %.2f ไร่\n".format(rai) +
                            "ราคา: %.2f บาท/ไร่\n".format(rate) +
                            "ยอดงาน: %.2f บาท\n".format(total) +
                            "สถานะ: $status" +
                            if (note.isNotBlank()) {
                                "\nหมายเหตุ: $note"
                            } else {
                                ""
                            }

                        textSize = 16f

                        setTextColor(
                            android.graphics.Color.DKGRAY
                        )

                        setPadding(
                            0,
                            dp(6),
                            0,
                            dp(10)
                        )
                    }
                )

                val buttonRow =
                    android.widget.LinearLayout(
                        this@MainActivity
                    ).apply {
                        orientation =
                            android.widget.LinearLayout.HORIZONTAL
                    }

                val editButton =
                    android.widget.Button(
                        this@MainActivity
                    ).apply {

                        text = "แก้ไข"

                        setOnClickListener {
                            showEditJobDialog(
                                record
                            ) {
                                reloadHistory()
                            }
                        }
                    }

                val deleteButton =
                    android.widget.Button(
                        this@MainActivity
                    ).apply {

                        text = "ลบ"

                        setOnClickListener {

                            android.app.AlertDialog.Builder(
                                this@MainActivity
                            )
                                .setTitle("ยืนยันการลบ")
                                .setMessage(
                                    "ต้องการลบงานของ $customer ใช่หรือไม่?"
                                )
                                .setNegativeButton(
                                    "ยกเลิก",
                                    null
                                )
                                .setPositiveButton(
                                    "ลบ"
                                ) { _, _ ->

                                    val current =
                                        prefs.getStringSet(
                                            "flight_jobs",
                                            emptySet()
                                        )?.toMutableSet()
                                            ?: mutableSetOf()

                                    current.remove(record)

                                    prefs.edit()
                                        .putStringSet(
                                            "flight_jobs",
                                            current
                                        )
                                        .apply()

                                    android.widget.Toast.makeText(
                                        this@MainActivity,
                                        "ลบรายการแล้ว",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()

                                    reloadHistory()
                                }
                                .show()
                        }
                    }

                buttonRow.addView(
                    editButton,
                    android.widget.LinearLayout.LayoutParams(
                        0,
                        android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                )

                buttonRow.addView(
                    deleteButton,
                    android.widget.LinearLayout.LayoutParams(
                        0,
                        android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                )

                card.addView(buttonRow)

                listContainer.addView(card)
            }

            summaryText.text =
                "ทั้งหมด ${sortedRecords.size} งาน\n" +
                "พื้นที่รวม %.2f ไร่\n".format(totalRai) +
                "มูลค่างานรวม %.2f บาท".format(totalValue)

            if (sortedRecords.isEmpty()) {

                listContainer.addView(
                    android.widget.TextView(
                        this@MainActivity
                    ).apply {

                        text =
                            "ยังไม่มีประวัติงาน\n" +
                            "ให้บันทึกงานจากเมนูบันทึกงานบินก่อน"

                        textSize = 17f

                        setTextColor(
                            android.graphics.Color.GRAY
                        )

                        setPadding(
                            0,
                            dp(20),
                            0,
                            dp(20)
                        )
                    }
                )
            }
        }

        reloadHistory()

        val scrollView =
            android.widget.ScrollView(this).apply {

                addView(
                    root,
                    android.view.ViewGroup.LayoutParams(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                )
            }

        setContentView(scrollView)
    }


    private fun showEditJobDialog(
        originalRecord: String,
        onSaved: () -> Unit
    ) {

        val prefs = getSharedPreferences(
            "pc_drone_v3_data",
            android.content.Context.MODE_PRIVATE
        )

        val parts =
            originalRecord.split(
                "|||",
                ignoreCase = false,
                limit = 9
            )

        if (parts.size < 8) {

            android.widget.Toast.makeText(
                this,
                "ไม่สามารถอ่านข้อมูลรายการนี้ได้",
                android.widget.Toast.LENGTH_SHORT
            ).show()

            return
        }

        val originalTime =
            parts.getOrNull(0)
                ?.toLongOrNull()
                ?: System.currentTimeMillis()

        val form =
            android.widget.LinearLayout(this).apply {

                orientation =
                    android.widget.LinearLayout.VERTICAL

                setPadding(
                    dp(20),
                    dp(8),
                    dp(20),
                    dp(8)
                )
            }

        val customerInput =
            android.widget.EditText(this).apply {
                hint = "ชื่อลูกค้า"
                setText(parts.getOrNull(1) ?: "")
            }

        val serviceSpinner =
            android.widget.Spinner(this)

        val services =
            arrayOf(
                "พ่นยา",
                "หว่านปุ๋ย",
                "หว่านเมล็ด",
                "พ่นสวนผลไม้",
                "งานอื่น ๆ"
            )

        serviceSpinner.adapter =
            android.widget.ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                services
            )

        val oldService =
            parts.getOrNull(2)
                ?: ""

        val serviceIndex =
            services.indexOf(oldService)

        if (serviceIndex >= 0) {
            serviceSpinner.setSelection(serviceIndex)
        }

        val locationInput =
            android.widget.EditText(this).apply {
                hint = "พื้นที่ / สวน"
                setText(parts.getOrNull(3) ?: "")
            }

        val raiInput =
            android.widget.EditText(this).apply {

                hint = "จำนวนไร่"

                inputType =
                    android.text.InputType.TYPE_CLASS_NUMBER or
                    android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL

                setText(
                    parts.getOrNull(4)
                        ?: ""
                )
            }

        val rateInput =
            android.widget.EditText(this).apply {

                hint = "ราคาต่อไร่"

                inputType =
                    android.text.InputType.TYPE_CLASS_NUMBER or
                    android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL

                setText(
                    parts.getOrNull(5)
                        ?: ""
                )
            }

        val statusSpinner =
            android.widget.Spinner(this)

        val statuses =
            arrayOf(
                "รอทำงาน",
                "กำลังดำเนินงาน",
                "เสร็จแล้ว",
                "ยกเลิก"
            )

        statusSpinner.adapter =
            android.widget.ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                statuses
            )

        val oldStatus =
            parts.getOrNull(7)
                ?: ""

        val statusIndex =
            statuses.indexOf(oldStatus)

        if (statusIndex >= 0) {
            statusSpinner.setSelection(statusIndex)
        }

        val noteInput =
            android.widget.EditText(this).apply {

                hint = "หมายเหตุ"

                minLines = 2

                setText(
                    parts.getOrNull(8)
                        ?: ""
                )
            }

        form.addView(customerInput)
        form.addView(serviceSpinner)
        form.addView(locationInput)
        form.addView(raiInput)
        form.addView(rateInput)
        form.addView(statusSpinner)
        form.addView(noteInput)

        val scroll =
            android.widget.ScrollView(this).apply {
                addView(form)
            }

        val dialog =
            android.app.AlertDialog.Builder(this)
                .setTitle("แก้ไขงาน")
                .setView(scroll)
                .setNegativeButton(
                    "ยกเลิก",
                    null
                )
                .setPositiveButton(
                    "บันทึก",
                    null
                )
                .create()

        dialog.setOnShowListener {

            val saveButton =
                dialog.getButton(
                    android.app.AlertDialog.BUTTON_POSITIVE
                )

            saveButton.setOnClickListener {

                val customer =
                    customerInput.text
                        .toString()
                        .trim()

                val location =
                    locationInput.text
                        .toString()
                        .trim()

                val rai =
                    raiInput.text
                        .toString()
                        .trim()
                        .toDoubleOrNull()

                val rate =
                    rateInput.text
                        .toString()
                        .trim()
                        .toDoubleOrNull()

                if (customer.isEmpty()) {
                    customerInput.error =
                        "กรุณากรอกชื่อลูกค้า"
                    return@setOnClickListener
                }

                if (rai == null || rai <= 0.0) {
                    raiInput.error =
                        "จำนวนไร่ไม่ถูกต้อง"
                    return@setOnClickListener
                }

                if (rate == null || rate < 0.0) {
                    rateInput.error =
                        "ราคาต่อไร่ไม่ถูกต้อง"
                    return@setOnClickListener
                }

                val service =
                    serviceSpinner.selectedItem
                        .toString()

                val status =
                    statusSpinner.selectedItem
                        .toString()

                val note =
                    noteInput.text
                        .toString()
                        .trim()

                val total =
                    rai * rate

                val updatedRecord =
                    listOf(
                        originalTime.toString(),
                        customer,
                        service,
                        location,
                        rai.toString(),
                        rate.toString(),
                        total.toString(),
                        status,
                        note
                    ).joinToString("|||")

                val current =
                    prefs.getStringSet(
                        "flight_jobs",
                        emptySet()
                    )?.toMutableSet()
                        ?: mutableSetOf()

                current.remove(originalRecord)
                current.add(updatedRecord)

                prefs.edit()
                    .putStringSet(
                        "flight_jobs",
                        current
                    )
                    .apply()

                android.widget.Toast.makeText(
                    this@MainActivity,
                    "แก้ไขรายการเรียบร้อย",
                    android.widget.Toast.LENGTH_SHORT
                ).show()

                dialog.dismiss()

                onSaved()
            }
        }

        dialog.show()
    }


    private fun showFinance() {

        val prefs = getSharedPreferences(
            "pc_drone_v3_data",
            android.content.Context.MODE_PRIVATE
        )

        val moneyFormat =
            java.text.DecimalFormat("#,##0.00")

        fun parseMoney(value: String?): java.math.BigDecimal {
            return try {
                if (value.isNullOrBlank()) {
                    java.math.BigDecimal.ZERO
                } else {
                    java.math.BigDecimal(value.trim())
                }
            } catch (_: Exception) {
                java.math.BigDecimal.ZERO
            }
        }

        fun safeText(value: String): String {
            return value
                .replace("|||", " ")
                .replace("\n", " ")
                .trim()
        }

        val root =
            android.widget.LinearLayout(this).apply {

                orientation =
                    android.widget.LinearLayout.VERTICAL

                setPadding(
                    dp(18),
                    dp(18),
                    dp(18),
                    dp(28)
                )

                setBackgroundColor(
                    android.graphics.Color.WHITE
                )
            }

        root.addView(createBackButton())

        root.addView(
            android.widget.TextView(this).apply {

                text = "การเงิน"

                textSize = 28f

                setTextColor(
                    android.graphics.Color.BLACK
                )

                setTypeface(
                    typeface,
                    android.graphics.Typeface.BOLD
                )
            }
        )

        root.addView(
            android.widget.TextView(this).apply {

                text =
                    "รายรับและรายจ่ายทั้งหมดเชื่อมกับยอดคงเหลือกลาง"

                textSize = 15f

                setTextColor(
                    android.graphics.Color.DKGRAY
                )

                setPadding(
                    0,
                    dp(4),
                    0,
                    dp(16)
                )
            }
        )

        // =================================================
        // SUMMARY BOX
        // =================================================

        val summaryBox =
            android.widget.LinearLayout(this).apply {

                orientation =
                    android.widget.LinearLayout.VERTICAL

                setPadding(
                    dp(14),
                    dp(14),
                    dp(14),
                    dp(14)
                )

                setBackgroundColor(
                    android.graphics.Color.rgb(
                        241,
                        247,
                        242
                    )
                )
            }

        val jobIncomeText =
            android.widget.TextView(this).apply {
                textSize = 17f
                setTextColor(
                    android.graphics.Color.BLACK
                )
            }

        val manualIncomeText =
            android.widget.TextView(this).apply {
                textSize = 17f
                setTextColor(
                    android.graphics.Color.BLACK
                )
            }

        val expenseText =
            android.widget.TextView(this).apply {
                textSize = 17f
                setTextColor(
                    android.graphics.Color.BLACK
                )
            }

        val totalIncomeText =
            android.widget.TextView(this).apply {

                textSize = 18f

                setTextColor(
                    android.graphics.Color.rgb(
                        0,
                        115,
                        55
                    )
                )

                setTypeface(
                    typeface,
                    android.graphics.Typeface.BOLD
                )
            }

        val balanceText =
            android.widget.TextView(this).apply {

                textSize = 24f

                setTypeface(
                    typeface,
                    android.graphics.Typeface.BOLD
                )

                setPadding(
                    0,
                    dp(10),
                    0,
                    0
                )
            }

        summaryBox.addView(jobIncomeText)
        summaryBox.addView(manualIncomeText)
        summaryBox.addView(totalIncomeText)
        summaryBox.addView(expenseText)
        summaryBox.addView(balanceText)

        root.addView(summaryBox)

        // =================================================
        // ADD TRANSACTION FORM
        // =================================================

        root.addView(
            android.widget.TextView(this).apply {

                text = "บันทึกรายรับ / รายจ่าย"

                textSize = 20f

                setTypeface(
                    typeface,
                    android.graphics.Typeface.BOLD
                )

                setTextColor(
                    android.graphics.Color.BLACK
                )

                setPadding(
                    0,
                    dp(22),
                    0,
                    dp(6)
                )
            }
        )

        val typeSpinner =
            android.widget.Spinner(this)

        val transactionTypes =
            arrayOf(
                "รายจ่าย",
                "รายรับอื่น"
            )

        typeSpinner.adapter =
            android.widget.ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                transactionTypes
            )

        val categorySpinner =
            android.widget.Spinner(this)

        val categories =
            arrayOf(
                "ค่าน้ำมัน",
                "ค่าอาหาร",
                "ค่าแรง",
                "ค่าซ่อมบำรุง",
                "ค่าอะไหล่",
                "ค่างวดโดรน",
                "ค่างวด/หนี้",
                "ค่าเดินทาง",
                "ซื้ออุปกรณ์",
                "เงินทุนเข้า",
                "รับเงินอื่น",
                "อื่น ๆ"
            )

        categorySpinner.adapter =
            android.widget.ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                categories
            )

        val amountInput =
            android.widget.EditText(this).apply {

                hint = "จำนวนเงิน"

                textSize = 17f

                inputType =
                    android.text.InputType.TYPE_CLASS_NUMBER or
                    android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL

                setTextColor(
                    android.graphics.Color.BLACK
                )

                setHintTextColor(
                    android.graphics.Color.GRAY
                )
            }

        val noteInput =
            android.widget.EditText(this).apply {

                hint =
                    "รายละเอียด เช่น เติมน้ำมันรถ 1,000 บาท"

                textSize = 16f

                minLines = 2

                setTextColor(
                    android.graphics.Color.BLACK
                )

                setHintTextColor(
                    android.graphics.Color.GRAY
                )
            }

        root.addView(typeSpinner)
        root.addView(categorySpinner)
        root.addView(amountInput)
        root.addView(noteInput)

        // =================================================
        // TRANSACTION HISTORY
        // =================================================

        root.addView(
            android.widget.TextView(this).apply {

                text = "รายการเงิน"

                textSize = 20f

                setTypeface(
                    typeface,
                    android.graphics.Typeface.BOLD
                )

                setTextColor(
                    android.graphics.Color.BLACK
                )

                setPadding(
                    0,
                    dp(22),
                    0,
                    dp(8)
                )
            }
        )

        val transactionContainer =
            android.widget.LinearLayout(this).apply {

                orientation =
                    android.widget.LinearLayout.VERTICAL
            }

        fun reloadFinance() {

            transactionContainer.removeAllViews()

            // =============================================
            // 1. JOB INCOME
            // เฉพาะงานสถานะ "เสร็จแล้ว"
            // =============================================

            val jobs =
                prefs.getStringSet(
                    "flight_jobs",
                    emptySet()
                )?.toList()
                    ?: emptyList()

            var jobIncome =
                java.math.BigDecimal.ZERO

            var completedJobCount = 0

            jobs.forEach { record ->

                val parts =
                    record.split(
                        "|||",
                        ignoreCase = false,
                        limit = 9
                    )

                if (parts.size >= 8) {

                    val total =
                        parseMoney(
                            parts.getOrNull(6)
                        )

                    val status =
                        parts.getOrNull(7)
                            ?.trim()
                            ?: ""

                    if (status == "เสร็จแล้ว") {

                        jobIncome =
                            jobIncome.add(total)

                        completedJobCount++
                    }
                }
            }

            // =============================================
            // 2. MANUAL TRANSACTIONS
            // =============================================

            val transactions =
                prefs.getStringSet(
                    "finance_transactions",
                    emptySet()
                )?.toList()
                    ?: emptyList()

            val sortedTransactions =
                transactions.sortedByDescending { record ->

                    record
                        .split(
                            "|||",
                            ignoreCase = false,
                            limit = 5
                        )
                        .getOrNull(0)
                        ?.toLongOrNull()
                        ?: 0L
                }

            var manualIncome =
                java.math.BigDecimal.ZERO

            var totalExpense =
                java.math.BigDecimal.ZERO

            sortedTransactions.forEach { record ->

                val parts =
                    record.split(
                        "|||",
                        ignoreCase = false,
                        limit = 5
                    )

                if (parts.size < 4) {
                    return@forEach
                }

                val time =
                    parts.getOrNull(0)
                        ?.toLongOrNull()
                        ?: 0L

                val type =
                    parts.getOrNull(1)
                        ?: ""

                val category =
                    parts.getOrNull(2)
                        ?: "-"

                val amount =
                    parseMoney(
                        parts.getOrNull(3)
                    )

                val note =
                    parts.getOrNull(4)
                        ?: ""

                if (type == "INCOME") {

                    manualIncome =
                        manualIncome.add(amount)

                } else if (type == "EXPENSE") {

                    totalExpense =
                        totalExpense.add(amount)
                }

                val dateText =
                    if (time > 0L) {

                        java.text.SimpleDateFormat(
                            "dd/MM/yyyy HH:mm",
                            java.util.Locale.getDefault()
                        ).format(
                            java.util.Date(time)
                        )

                    } else {
                        "-"
                    }

                val card =
                    android.widget.LinearLayout(
                        this@MainActivity
                    ).apply {

                        orientation =
                            android.widget.LinearLayout.VERTICAL

                        setPadding(
                            dp(12),
                            dp(12),
                            dp(12),
                            dp(12)
                        )

                        setBackgroundColor(
                            android.graphics.Color.rgb(
                                247,
                                247,
                                247
                            )
                        )

                        val lp =
                            android.widget.LinearLayout.LayoutParams(
                                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                            )

                        lp.setMargins(
                            0,
                            0,
                            0,
                            dp(10)
                        )

                        layoutParams = lp
                    }

                val isIncome =
                    type == "INCOME"

                card.addView(
                    android.widget.TextView(
                        this@MainActivity
                    ).apply {

                        text =
                            if (isIncome) {
                                "รายรับ +${moneyFormat.format(amount)} บาท"
                            } else {
                                "รายจ่าย -${moneyFormat.format(amount)} บาท"
                            }

                        textSize = 18f

                        setTypeface(
                            typeface,
                            android.graphics.Typeface.BOLD
                        )

                        setTextColor(
                            if (isIncome) {
                                android.graphics.Color.rgb(
                                    0,
                                    115,
                                    55
                                )
                            } else {
                                android.graphics.Color.rgb(
                                    180,
                                    30,
                                    30
                                )
                            }
                        )
                    }
                )

                card.addView(
                    android.widget.TextView(
                        this@MainActivity
                    ).apply {

                        text =
                            "หมวด: $category\n" +
                            "วันที่: $dateText" +
                            if (note.isNotBlank()) {
                                "\nรายละเอียด: $note"
                            } else {
                                ""
                            }

                        textSize = 15f

                        setTextColor(
                            android.graphics.Color.DKGRAY
                        )
                    }
                )

                val editButton =
                    android.widget.Button(
                        this@MainActivity
                    ).apply {

                        text = "แก้ไขรายการ"

                        setOnClickListener {

                            showEditFinanceTransaction(
                                record
                            ) {
                                reloadFinance()
                            }
                        }
                    }

                val deleteButton =
                    android.widget.Button(
                        this@MainActivity
                    ).apply {

                        text = "ลบรายการ"

                        setOnClickListener {

                            android.app.AlertDialog.Builder(
                                this@MainActivity
                            )
                                .setTitle(
                                    "ยืนยันการลบ"
                                )
                                .setMessage(
                                    "ต้องการลบรายการเงินนี้หรือไม่?"
                                )
                                .setNegativeButton(
                                    "ยกเลิก",
                                    null
                                )
                                .setPositiveButton(
                                    "ลบ"
                                ) { _, _ ->

                                    val current =
                                        prefs.getStringSet(
                                            "finance_transactions",
                                            emptySet()
                                        )?.toMutableSet()
                                            ?: mutableSetOf()

                                    current.remove(
                                        record
                                    )

                                    prefs.edit()
                                        .putStringSet(
                                            "finance_transactions",
                                            current
                                        )
                                        .apply()

                                    android.widget.Toast.makeText(
                                        this@MainActivity,
                                        "ลบรายการแล้ว",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()

                                    reloadFinance()
                                }
                                .show()
                        }
                    }

                val financeActionRow =
                    android.widget.LinearLayout(
                        this@MainActivity
                    ).apply {

                        orientation =
                            android.widget.LinearLayout.HORIZONTAL
                    }

                financeActionRow.addView(
                    editButton,
                    android.widget.LinearLayout.LayoutParams(
                        0,
                        android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                )

                financeActionRow.addView(
                    deleteButton,
                    android.widget.LinearLayout.LayoutParams(
                        0,
                        android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                )

                card.addView(
                    financeActionRow
                )

                transactionContainer.addView(
                    card
                )
            }

            val totalIncome =
                jobIncome.add(
                    manualIncome
                )

            val balance =
                totalIncome.subtract(
                    totalExpense
                )

            jobIncomeText.text =
                "รายรับจากงานบิน ($completedJobCount งาน): " +
                "${moneyFormat.format(jobIncome)} บาท"

            manualIncomeText.text =
                "รายรับอื่น: " +
                "${moneyFormat.format(manualIncome)} บาท"

            totalIncomeText.text =
                "รายรับรวม: " +
                "${moneyFormat.format(totalIncome)} บาท"

            expenseText.text =
                "รายจ่ายรวม: " +
                "${moneyFormat.format(totalExpense)} บาท"

            balanceText.text =
                "คงเหลือ: " +
                "${moneyFormat.format(balance)} บาท"

            balanceText.setTextColor(
                if (
                    balance.compareTo(
                        java.math.BigDecimal.ZERO
                    ) >= 0
                ) {
                    android.graphics.Color.rgb(
                        0,
                        120,
                        55
                    )
                } else {
                    android.graphics.Color.rgb(
                        190,
                        25,
                        25
                    )
                }
            )

            if (sortedTransactions.isEmpty()) {

                transactionContainer.addView(
                    android.widget.TextView(
                        this@MainActivity
                    ).apply {

                        text =
                            "ยังไม่มีรายรับ/รายจ่ายที่บันทึกเอง"

                        textSize = 16f

                        setTextColor(
                            android.graphics.Color.GRAY
                        )

                        setPadding(
                            0,
                            dp(10),
                            0,
                            dp(10)
                        )
                    }
                )
            }
        }

        val saveButton =
            android.widget.Button(this).apply {

                text = "บันทึกรายการ"

                textSize = 18f

                setOnClickListener {

                    val amount =
                        parseMoney(
                            amountInput.text
                                .toString()
                        )

                    if (
                        amount.compareTo(
                            java.math.BigDecimal.ZERO
                        ) <= 0
                    ) {

                        amountInput.error =
                            "กรุณากรอกจำนวนเงินมากกว่า 0"

                        return@setOnClickListener
                    }

                    val selectedType =
                        typeSpinner.selectedItem
                            .toString()

                    val typeCode =
                        if (
                            selectedType ==
                            "รายรับอื่น"
                        ) {
                            "INCOME"
                        } else {
                            "EXPENSE"
                        }

                    val category =
                        safeText(
                            categorySpinner
                                .selectedItem
                                .toString()
                        )

                    val note =
                        safeText(
                            noteInput.text
                                .toString()
                        )

                    val record =
                        listOf(
                            System.currentTimeMillis()
                                .toString(),
                            typeCode,
                            category,
                            amount
                                .stripTrailingZeros()
                                .toPlainString(),
                            note
                        ).joinToString(
                            "|||"
                        )

                    val current =
                        prefs.getStringSet(
                            "finance_transactions",
                            emptySet()
                        )?.toMutableSet()
                            ?: mutableSetOf()

                    current.add(
                        record
                    )

                    prefs.edit()
                        .putStringSet(
                            "finance_transactions",
                            current
                        )
                        .apply()

                    amountInput.text.clear()
                    noteInput.text.clear()

                    android.widget.Toast.makeText(
                        this@MainActivity,
                        "บันทึกรายการเงินเรียบร้อย",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()

                    reloadFinance()
                }
            }

        root.addView(
            saveButton
        )

        root.addView(
            transactionContainer
        )

        reloadFinance()

        val scrollView =
            android.widget.ScrollView(this).apply {

                addView(
                    root,
                    android.view.ViewGroup.LayoutParams(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                )
            }

        setContentView(
            scrollView
        )
    }


    private fun showEditFinanceTransaction(
        originalRecord: String,
        onSaved: () -> Unit
    ) {

        val prefs = getSharedPreferences(
            "pc_drone_v3_data",
            android.content.Context.MODE_PRIVATE
        )

        val parts =
            originalRecord.split(
                "|||",
                ignoreCase = false,
                limit = 5
            )

        if (parts.size < 4) {

            android.widget.Toast.makeText(
                this,
                "ไม่สามารถอ่านรายการเงินนี้ได้",
                android.widget.Toast.LENGTH_SHORT
            ).show()

            return
        }

        val originalTime =
            parts.getOrNull(0)
                ?.toLongOrNull()
                ?: System.currentTimeMillis()

        val originalType =
            parts.getOrNull(1)
                ?: "EXPENSE"

        val originalCategory =
            parts.getOrNull(2)
                ?: "อื่น ๆ"

        val originalAmount =
            parts.getOrNull(3)
                ?: ""

        val originalNote =
            parts.getOrNull(4)
                ?: ""

        val form =
            android.widget.LinearLayout(this).apply {

                orientation =
                    android.widget.LinearLayout.VERTICAL

                setPadding(
                    dp(20),
                    dp(8),
                    dp(20),
                    dp(8)
                )
            }

        // --------------------------------------------------
        // TYPE
        // --------------------------------------------------

        val typeLabel =
            android.widget.TextView(this).apply {

                text = "ประเภทรายการ"

                textSize = 15f

                setTextColor(
                    android.graphics.Color.BLACK
                )

                setPadding(
                    0,
                    dp(4),
                    0,
                    dp(4)
                )
            }

        val typeSpinner =
            android.widget.Spinner(this)

        val types =
            arrayOf(
                "รายจ่าย",
                "รายรับอื่น"
            )

        typeSpinner.adapter =
            android.widget.ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                types
            )

        if (originalType == "INCOME") {
            typeSpinner.setSelection(1)
        } else {
            typeSpinner.setSelection(0)
        }

        // --------------------------------------------------
        // CATEGORY
        // --------------------------------------------------

        val categoryLabel =
            android.widget.TextView(this).apply {

                text = "หมวดหมู่"

                textSize = 15f

                setTextColor(
                    android.graphics.Color.BLACK
                )

                setPadding(
                    0,
                    dp(10),
                    0,
                    dp(4)
                )
            }

        val categorySpinner =
            android.widget.Spinner(this)

        val categories =
            arrayOf(
                "ค่าน้ำมัน",
                "ค่าอาหาร",
                "ค่าแรง",
                "ค่าซ่อมบำรุง",
                "ค่าอะไหล่",
                "ค่างวดโดรน",
                "ค่างวด/หนี้",
                "ค่าเดินทาง",
                "ซื้ออุปกรณ์",
                "เงินทุนเข้า",
                "รับเงินอื่น",
                "อื่น ๆ"
            )

        categorySpinner.adapter =
            android.widget.ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                categories
            )

        val categoryIndex =
            categories.indexOf(
                originalCategory
            )

        if (categoryIndex >= 0) {
            categorySpinner.setSelection(
                categoryIndex
            )
        } else {
            categorySpinner.setSelection(
                categories.lastIndex
            )
        }

        // --------------------------------------------------
        // AMOUNT
        // --------------------------------------------------

        val amountInput =
            android.widget.EditText(this).apply {

                hint = "จำนวนเงิน"

                textSize = 17f

                inputType =
                    android.text.InputType.TYPE_CLASS_NUMBER or
                    android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL

                setText(
                    originalAmount
                )

                setTextColor(
                    android.graphics.Color.BLACK
                )

                setHintTextColor(
                    android.graphics.Color.GRAY
                )
            }

        // --------------------------------------------------
        // NOTE
        // --------------------------------------------------

        val noteInput =
            android.widget.EditText(this).apply {

                hint = "รายละเอียด"

                textSize = 16f

                minLines = 2

                setText(
                    originalNote
                )

                setTextColor(
                    android.graphics.Color.BLACK
                )

                setHintTextColor(
                    android.graphics.Color.GRAY
                )
            }

        form.addView(typeLabel)
        form.addView(typeSpinner)

        form.addView(categoryLabel)
        form.addView(categorySpinner)

        form.addView(amountInput)
        form.addView(noteInput)

        val scroll =
            android.widget.ScrollView(this).apply {

                addView(
                    form
                )
            }

        val dialog =
            android.app.AlertDialog.Builder(this)
                .setTitle(
                    "แก้ไขรายการเงิน"
                )
                .setView(
                    scroll
                )
                .setNegativeButton(
                    "ยกเลิก",
                    null
                )
                .setPositiveButton(
                    "บันทึก",
                    null
                )
                .create()

        dialog.setOnShowListener {

            val positiveButton =
                dialog.getButton(
                    android.app.AlertDialog.BUTTON_POSITIVE
                )

            positiveButton.setOnClickListener {

                val amountText =
                    amountInput.text
                        .toString()
                        .trim()

                val amount =
                    try {

                        java.math.BigDecimal(
                            amountText
                        )

                    } catch (_: Exception) {

                        java.math.BigDecimal.ZERO
                    }

                if (
                    amount.compareTo(
                        java.math.BigDecimal.ZERO
                    ) <= 0
                ) {

                    amountInput.error =
                        "กรุณากรอกจำนวนเงินมากกว่า 0"

                    return@setOnClickListener
                }

                val typeCode =
                    if (
                        typeSpinner.selectedItem
                            .toString() ==
                        "รายรับอื่น"
                    ) {
                        "INCOME"
                    } else {
                        "EXPENSE"
                    }

                val category =
                    categorySpinner
                        .selectedItem
                        .toString()
                        .replace(
                            "|||",
                            " "
                        )
                        .replace(
                            "\n",
                            " "
                        )
                        .trim()

                val note =
                    noteInput.text
                        .toString()
                        .replace(
                            "|||",
                            " "
                        )
                        .replace(
                            "\n",
                            " "
                        )
                        .trim()

                val updatedRecord =
                    listOf(
                        originalTime.toString(),
                        typeCode,
                        category,
                        amount
                            .stripTrailingZeros()
                            .toPlainString(),
                        note
                    ).joinToString(
                        "|||"
                    )

                val current =
                    prefs.getStringSet(
                        "finance_transactions",
                        emptySet()
                    )?.toMutableSet()
                        ?: mutableSetOf()

                /*
                 * ลบรายการเดิมก่อน
                 * แล้วเพิ่มรายการที่แก้ไข
                 *
                 * จึงไม่เกิดยอดซ้ำ
                 */
                current.remove(
                    originalRecord
                )

                current.add(
                    updatedRecord
                )

                prefs.edit()
                    .putStringSet(
                        "finance_transactions",
                        current
                    )
                    .apply()

                android.widget.Toast.makeText(
                    this@MainActivity,
                    "แก้ไขรายการเงินเรียบร้อย",
                    android.widget.Toast.LENGTH_SHORT
                ).show()

                dialog.dismiss()

                onSaved()
            }
        }

        dialog.show()
    }



    private fun createBackButton(): android.widget.Button {

        return android.widget.Button(this).apply {

            text = "←  ย้อนกลับ"
            textSize = 18f
            isAllCaps = false

            setTextColor(
                android.graphics.Color.WHITE
            )

            background =
                android.graphics.drawable.GradientDrawable().apply {

                    setColor(
                        greenDark
                    )

                    setStroke(
                        dp(2),
                        android.graphics.Color.rgb(
                            4,
                            65,
                            27
                        )
                    )

                    cornerRadius =
                        dp(9).toFloat()
                }

            elevation =
                dp(7).toFloat()

            setOnClickListener {
                showDashboard()
            }

            layoutParams =
                android.widget.LinearLayout.LayoutParams(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    dp(58)
                ).apply {

                    /*
                     * เว้นพื้นที่ Status Bar
                     * ไม่ให้ปุ่มชนเวลา / สัญญาณ / แบตเตอรี่
                     */
                    topMargin =
                        statusBarHeight() + dp(10)

                    bottomMargin =
                        dp(18)
                }
        }
    }


    private fun statusBarHeight(): Int {

        val resourceId =
            resources.getIdentifier(
                "status_bar_height",
                "dimen",
                "android"
            )

        return if (resourceId > 0) {
            resources.getDimensionPixelSize(
                resourceId
            )
        } else {
            dp(28)
        }
    }


    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}
