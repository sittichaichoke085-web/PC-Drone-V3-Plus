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


    // PC_DRONE_RINGTONE_PICKER_RESULT
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: android.content.Intent?
    ) {

        super.onActivityResult(
            requestCode,
            resultCode,
            data
        )

        if (
            requestCode == 7601 &&
            resultCode ==
                android.app.Activity.RESULT_OK
        ) {

            @Suppress("DEPRECATION")
            val uri =
                data?.getParcelableExtra(
                    android.media.RingtoneManager
                        .EXTRA_RINGTONE_PICKED_URI
                ) as? android.net.Uri

            if (
                uri != null
            ) {

                getSharedPreferences(
                    "pc_drone_settings",
                    android.content.Context.MODE_PRIVATE
                )
                    .edit()
                    .putString(
                        "notification_sound_uri",
                        uri.toString()
                    )
                    .apply()

                android.widget.Toast
                    .makeText(
                        this,
                        "เลือกเสียงแจ้งเตือนแล้ว",
                        android.widget.Toast.LENGTH_SHORT
                    )
                    .show()

                showScreen(
                    AppRoute.SETTINGS
                )
            }
        }
    }


    private fun showScreen(route: AppRoute) {
        when (route) {
            AppRoute.DASHBOARD -> showDashboard()
            AppRoute.MONEY_MANAGER -> showMoneyManager()
            else -> showPlaceholder(route)
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
            menuRow(
                menuCard(
                    "฿",
                    "บริหารเงิน",
                    "กระเป๋าและเงินทุน",
                    AppRoute.MONEY_MANAGER
                ),
                menuCard(
                    "⚙",
                    "ตั้งค่า",
                    "ข้อมูลและระบบ",
                    AppRoute.SETTINGS
                )
            )

        row3.setPadding(
            0,
            dp(12),
            0,
            0
        )

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





    // PC_DRONE_MONTHLY_EXPENSE_REPORT
    private fun showMonthlyExpenseReport() {

        val prefs =
            getSharedPreferences(
                "pc_drone_v3_data",
                MODE_PRIVATE
            )

        val dark = android.graphics.Color.rgb(17, 17, 17)
        val pageBg = android.graphics.Color.rgb(245, 248, 246)
        val red = android.graphics.Color.rgb(180, 35, 35)
        val orange = android.graphics.Color.rgb(239, 108, 0)

        val monthlyMoneyFormat =
            java.text.NumberFormat.getNumberInstance(
                java.util.Locale("th", "TH")
            ).apply {
                minimumFractionDigits = 2
                maximumFractionDigits = 2
            }

        fun rounded(
            color: Int,
            radius: Int
        ): android.graphics.drawable.GradientDrawable {
            return android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                setColor(color)
                cornerRadius = dp(radius).toFloat()
            }
        }

        val scroll = android.widget.ScrollView(this).apply {
            setBackgroundColor(pageBg)
        }

        val root = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(dp(16), dp(24), dp(16), dp(32))
        }

        root.addView(
            android.widget.TextView(this).apply {
                text = "รายจ่ายต่อเดือน"
                textSize = 26f
                setTextColor(dark)
                setTypeface(typeface, android.graphics.Typeface.BOLD)
            }
        )

        // PC_DRONE_MONTHLY_DATE_RANGE_STEP1
        fun createDateInput(
            hintText: String,
            maxLength: Int
        ) = android.widget.EditText(this).apply {
            hint = hintText
            textSize = 14f
            isSingleLine = true
            gravity = android.view.Gravity.CENTER
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            filters = arrayOf(
                android.text.InputFilter.LengthFilter(maxLength)
            )
        }

        val cal = java.util.Calendar.getInstance()

        val firstDay = cal.clone() as java.util.Calendar
        firstDay.set(java.util.Calendar.DAY_OF_MONTH, 1)
        firstDay.set(java.util.Calendar.HOUR_OF_DAY, 0)
        firstDay.set(java.util.Calendar.MINUTE, 0)
        firstDay.set(java.util.Calendar.SECOND, 0)
        firstDay.set(java.util.Calendar.MILLISECOND, 0)

        val lastDay = cal.clone() as java.util.Calendar
        lastDay.set(
            java.util.Calendar.DAY_OF_MONTH,
            lastDay.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
        )
        lastDay.set(java.util.Calendar.HOUR_OF_DAY, 23)
        lastDay.set(java.util.Calendar.MINUTE, 59)
        lastDay.set(java.util.Calendar.SECOND, 59)
        lastDay.set(java.util.Calendar.MILLISECOND, 999)

        // PC_DRONE_MONTHLY_DATE_RANGE_STEP2
        val fromDay = createDateInput("วัน", 2)
        val fromMonth = createDateInput("เดือน", 2)
        val fromYear = createDateInput("ปี", 4)

        val toDay = createDateInput("วัน", 2)
        val toMonth = createDateInput("เดือน", 2)
        val toYear = createDateInput("ปี", 4)

        fromDay.setText(
            "%02d".format(firstDay.get(java.util.Calendar.DAY_OF_MONTH))
        )
        fromMonth.setText(
            "%02d".format(firstDay.get(java.util.Calendar.MONTH) + 1)
        )
        fromYear.setText(
            firstDay.get(java.util.Calendar.YEAR).toString()
        )

        toDay.setText(
            "%02d".format(lastDay.get(java.util.Calendar.DAY_OF_MONTH))
        )
        toMonth.setText(
            "%02d".format(lastDay.get(java.util.Calendar.MONTH) + 1)
        )
        toYear.setText(
            lastDay.get(java.util.Calendar.YEAR).toString()
        )

        val dateOnly =
            java.text.SimpleDateFormat(
                "dd/MM/yyyy",
                java.util.Locale.getDefault()
            )

        val header =
            android.widget.TextView(this).apply {
                text =
                    "จากวันที่ ${dateOnly.format(firstDay.time)} ถึง ${dateOnly.format(lastDay.time)}"
                textSize = 15f
                setTextColor(dark)
                setPadding(0, dp(8), 0, dp(14))
            }

        root.addView(header)

        // PC_DRONE_MONTHLY_DATE_RANGE_STEP4
        fun setupDateAutoJump(
            day: android.widget.EditText,
            month: android.widget.EditText,
            year: android.widget.EditText
        ) {
            day.addTextChangedListener(
                object : android.text.TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence?, start: Int, count: Int, after: Int
                    ) {}
                    override fun onTextChanged(
                        s: CharSequence?, start: Int, before: Int, count: Int
                    ) {
                        if (s?.length == 2) month.requestFocus()
                    }
                    override fun afterTextChanged(
                        s: android.text.Editable?
                    ) {}
                }
            )

            month.addTextChangedListener(
                object : android.text.TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence?, start: Int, count: Int, after: Int
                    ) {}
                    override fun onTextChanged(
                        s: CharSequence?, start: Int, before: Int, count: Int
                    ) {
                        if (s?.length == 2) year.requestFocus()
                    }
                    override fun afterTextChanged(
                        s: android.text.Editable?
                    ) {}
                }
            )

            day.imeOptions =
                android.view.inputmethod.EditorInfo.IME_ACTION_NEXT
            month.imeOptions =
                android.view.inputmethod.EditorInfo.IME_ACTION_NEXT
            year.imeOptions =
                android.view.inputmethod.EditorInfo.IME_ACTION_DONE

            day.setOnEditorActionListener { _, actionId, _ ->
                if (
                    actionId ==
                    android.view.inputmethod.EditorInfo.IME_ACTION_NEXT
                ) {
                    if (day.text.length == 1) {
                        day.setText(day.text.toString().padStart(2, '0'))
                    }
                    month.requestFocus()
                    true
                } else false
            }

            month.setOnEditorActionListener { _, actionId, _ ->
                if (
                    actionId ==
                    android.view.inputmethod.EditorInfo.IME_ACTION_NEXT
                ) {
                    if (month.text.length == 1) {
                        month.setText(month.text.toString().padStart(2, '0'))
                    }
                    year.requestFocus()
                    true
                } else false
            }
        }

        setupDateAutoJump(fromDay, fromMonth, fromYear)
        setupDateAutoJump(toDay, toMonth, toYear)

        // PC_DRONE_MONTHLY_DATE_RANGE_STEP5
        fun readDateRange(
            day: android.widget.EditText,
            month: android.widget.EditText,
            year: android.widget.EditText,
            endOfDay: Boolean
        ): java.util.Calendar? {
            val d = day.text.toString().trim().toIntOrNull()
                ?: return null
            val m = month.text.toString().trim().toIntOrNull()
                ?: return null
            val y = year.text.toString().trim().toIntOrNull()
                ?: return null

            if (y !in 1900..2500) return null
            if (m !in 1..12) return null
            if (d !in 1..31) return null

            return try {
                java.util.Calendar.getInstance().apply {
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
                    set(
                        java.util.Calendar.MILLISECOND,
                        if (endOfDay) 999 else 0
                    )

                    timeInMillis
                }
            } catch (_: Exception) {
                null
            }
        }

        // PC_DRONE_MONTHLY_DATE_RANGE_STEP3
        fun addDateInputRow(
            label: String,
            day: android.widget.EditText,
            month: android.widget.EditText,
            year: android.widget.EditText
        ) {
            val row = android.widget.LinearLayout(this).apply {
                orientation = android.widget.LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_VERTICAL
                setPadding(0, dp(4), 0, dp(4))
            }

            row.addView(
                android.widget.TextView(this).apply {
                    text = label
                    textSize = 15f
                    setTextColor(dark)
                },
                android.widget.LinearLayout.LayoutParams(
                    dp(80),
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                )
            )

            row.addView(
                day,
                android.widget.LinearLayout.LayoutParams(0, dp(52), 1f)
            )
            row.addView(
                month,
                android.widget.LinearLayout.LayoutParams(0, dp(52), 1f)
            )
            row.addView(
                year,
                android.widget.LinearLayout.LayoutParams(0, dp(52), 1.4f)
            )

            root.addView(row)
        }

        addDateInputRow(
            "จากวันที่",
            fromDay,
            fromMonth,
            fromYear
        )

        addDateInputRow(
            "ถึงวันที่",
            toDay,
            toMonth,
            toYear
        )

        // PC_DRONE_MONTHLY_DATE_RANGE_STEP6C
        val resultArea = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
        }

        fun renderReport(
            from: java.util.Calendar,
            to: java.util.Calendar
        ) {
            // PC_DRONE_MONTHLY_DYNAMIC_HEADER
            header.text =
                "จากวันที่ ${dateOnly.format(from.time)} ถึง ${dateOnly.format(to.time)}"

            resultArea.removeAllViews()

            val rows =
                prefs.getStringSet(
                    "finance_transactions",
                    emptySet()
                ).orEmpty()
                    .mapNotNull { record ->
                        val parts = record.split(
                            "|||",
                            ignoreCase = false,
                            limit = 5
                        )

                        val time =
                            parts.getOrNull(0)
                                ?.trim()
                                ?.toLongOrNull()
                                ?: return@mapNotNull null

                        val type =
                            parts.getOrNull(1)
                                ?.trim()
                                .orEmpty()

                        if (type != "EXPENSE") {
                            return@mapNotNull null
                        }

                        if (
                            time < from.timeInMillis ||
                            time > to.timeInMillis
                        ) {
                            return@mapNotNull null
                        }

                        val category =
                            parts.getOrNull(2)
                                ?.trim()
                                .orEmpty()

                        val amount =
                            parts.getOrNull(3)
                                ?.replace(",", "")
                                ?.trim()
                                ?.toBigDecimalOrNull()
                                ?: java.math.BigDecimal.ZERO

                        Triple(time, category, amount)
                    }
                    .sortedByDescending { it.first }

            val total =
                rows.fold(java.math.BigDecimal.ZERO) { sum, row ->
                    sum.add(row.third)
                }

            resultArea.addView(
                android.widget.TextView(this).apply {
                    text =
                        "รายจ่ายรวม ${monthlyMoneyFormat.format(total)} บาท   •   ${rows.size} รายการ"
                    textSize = 18f
                    setTextColor(red)
                    setTypeface(
                        typeface,
                        android.graphics.Typeface.BOLD
                    )
                    setPadding(0, 0, 0, dp(14))
                }
            )

            val table =
                android.widget.LinearLayout(this).apply {
                    orientation =
                        android.widget.LinearLayout.VERTICAL
                    background =
                        rounded(android.graphics.Color.WHITE, 14)
                    setPadding(
                        dp(12), dp(12), dp(12), dp(12)
                    )
                }

            table.addView(
                android.widget.TextView(this).apply {
                    text =
                        "วันที่ / เวลา        รายการ        จำนวนเงิน"
                    textSize = 14f
                    setTextColor(dark)
                    setTypeface(
                        typeface,
                        android.graphics.Typeface.BOLD
                    )
                    setPadding(0, 0, 0, dp(8))
                }
            )

            val fullDate =
                java.text.SimpleDateFormat(
                    "dd/MM/yyyy HH:mm",
                    java.util.Locale.getDefault()
                )

            if (rows.isEmpty()) {
                table.addView(
                    android.widget.TextView(this).apply {
                        text =
                            "ไม่พบรายการรายจ่ายในช่วงนี้"
                        textSize = 14f
                        setTextColor(
                            android.graphics.Color.GRAY
                        )
                        setPadding(
                            0, dp(12), 0, dp(12)
                        )
                    }
                )
            } else {
                rows.forEach { row ->
                    table.addView(
                        android.widget.TextView(this).apply {
                            text =
                                "${fullDate.format(java.util.Date(row.first))}   ${row.second}   ${monthlyMoneyFormat.format(row.third)} บาท"
                            textSize = 14f
                            setTextColor(dark)
                            setPadding(
                                0, dp(8), 0, dp(8)
                            )
                        }
                    )
                }
            }

            resultArea.addView(table)

            resultArea.addView(
                android.widget.TextView(this).apply {
                    text =
                        "รวมทั้งหมด ${monthlyMoneyFormat.format(total)} บาท"
                    textSize = 18f
                    setTextColor(red)
                    setTypeface(
                        typeface,
                        android.graphics.Typeface.BOLD
                    )
                    setPadding(
                        0, dp(14), 0, dp(8)
                    )
                }
            )
        }


        // PC_DRONE_MONTHLY_DATE_RANGE_STEP6D
        val searchButton =
            android.widget.Button(this).apply {
                text = "ค้นหา"
                isAllCaps = false
                setTextColor(android.graphics.Color.WHITE)
                background = rounded(orange, 12)

                setOnClickListener {
                    val from =
                        readDateRange(
                            fromDay,
                            fromMonth,
                            fromYear,
                            false
                        )

                    val to =
                        readDateRange(
                            toDay,
                            toMonth,
                            toYear,
                            true
                        )

                    when {
                        from == null || to == null -> {
                            android.widget.Toast.makeText(
                                this@MainActivity,
                                "กรุณากรอกวันที่ให้ถูกต้อง",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }

                        from.timeInMillis > to.timeInMillis -> {
                            android.widget.Toast.makeText(
                                this@MainActivity,
                                "วันที่เริ่มต้นต้องไม่เกินวันที่สิ้นสุด",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }

                        else -> {
                            renderReport(from, to)
                        }
                    }
                }
            }

        root.addView(
            searchButton,
            android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                dp(52)
            ).apply {
                setMargins(0, dp(6), 0, dp(12))
            }
        )

        root.addView(resultArea)

        // แสดงเดือนปัจจุบันทันทีเมื่อเปิดหน้า
        renderReport(firstDay, lastDay)

        root.addView(
            android.widget.Button(this).apply {
                text = "ย้อนกลับ"
                isAllCaps = false
                setTextColor(android.graphics.Color.WHITE)
                background = rounded(orange, 12)
                setOnClickListener {
                    showMoneyManager()
                }
            }
        )

        scroll.addView(
            root,
            android.view.ViewGroup.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        setContentView(scroll)
    }


    private fun showMoneyManager() {

        // ============================================================
        // PC DRONE V3.0.1 - MONEY MANAGER PHASE 2A
        //
        // IMPORTANT:
        // - Wallet data is stored separately in money_manager_wallets
        // - finance_transactions is NOT modified in Phase 2A
        // - Wallet balances here are opening/manual wallet balances only
        // ============================================================

        val dark = android.graphics.Color.rgb(17, 17, 17)
        val green = android.graphics.Color.rgb(0, 145, 70)
        val greenDark = android.graphics.Color.rgb(0, 91, 45)
        val pageBg = android.graphics.Color.rgb(245, 248, 246)
        val gray = android.graphics.Color.rgb(100, 105, 102)
        val red = android.graphics.Color.rgb(180, 35, 35)

        val prefs =
            getSharedPreferences(
                "pc_drone_v3_data",
                MODE_PRIVATE
            )

        val walletKey = "money_manager_wallets"
        val delimiter = "|||"

        fun clean(value: String): String {
            return value
                .replace(delimiter, " ")
                .replace("\n", " ")
                .trim()
        }

        fun rounded(
            color: Int,
            radius: Int
        ): android.graphics.drawable.GradientDrawable {

            return android.graphics.drawable.GradientDrawable().apply {
                shape =
                    android.graphics.drawable.GradientDrawable.RECTANGLE
                setColor(color)
                cornerRadius = dp(radius).toFloat()
            }
        }

        // ------------------------------------------------------------
        // SEED DEFAULT WALLETS ONLY ON FIRST USE
        // ------------------------------------------------------------

        if (!prefs.contains(walletKey)) {

            val defaults =
                linkedSetOf(
                    "001${delimiter}เงินสด${delimiter}เงินสด${delimiter}0${delimiter}เงินสดที่ถืออยู่",
                    "002${delimiter}บัญชีธนาคาร${delimiter}ธนาคาร${delimiter}0${delimiter}เงินในบัญชี",
                    "003${delimiter}ค่าน้ำมัน${delimiter}ค่าใช้จ่าย${delimiter}0${delimiter}งบเชื้อเพลิง",
                    "004${delimiter}ซ่อมบำรุง${delimiter}ค่าใช้จ่าย${delimiter}0${delimiter}โดรน รถ และอุปกรณ์",
                    "005${delimiter}ค่าแรง${delimiter}ค่าใช้จ่าย${delimiter}0${delimiter}ลูกน้องและทีมงาน",
                    "006${delimiter}เงินงานลูกค้า${delimiter}รายรับ${delimiter}0${delimiter}เงินจากงานบิน",
                    "007${delimiter}เงินเก็บ / ลงทุน${delimiter}เงินสำรอง${delimiter}0${delimiter}เงินสำรองธุรกิจ",
                    "008${delimiter}อื่น ๆ${delimiter}อื่น ๆ${delimiter}0${delimiter}ค่าใช้จ่ายหรือเงินกองอื่น"
                )

            prefs.edit()
                .putStringSet(
                    walletKey,
                    defaults
                )
                .apply()
        }

        fun loadWallets(): MutableList<List<String>> {

            return prefs
                .getStringSet(
                    walletKey,
                    emptySet()
                )
                .orEmpty()
                .mapNotNull { record ->

                    val parts =
                        record.split(
                            delimiter
                        )

                    if (parts.size >= 5) {
                        listOf(
                            parts[0],
                            parts[1],
                            parts[2],
                            parts[3],
                            parts.subList(
                                4,
                                parts.size
                            ).joinToString(" ")
                        )
                    } else {
                        null
                    }
                }
                .sortedBy { it[0] }
                .toMutableList()
        }

        fun saveWallets(
            wallets: List<List<String>>
        ) {

            val encoded =
                wallets.map { item ->
                    listOf(
                        clean(item[0]),
                        clean(item[1]),
                        clean(item[2]),
                        clean(item[3]),
                        clean(item[4])
                    ).joinToString(delimiter)
                }.toSet()

            prefs.edit()
                .putStringSet(
                    walletKey,
                    encoded
                )
                .apply()
        }

        fun parseMoney(
            text: String
        ): java.math.BigDecimal {

            return try {
                java.math.BigDecimal(
                    text
                        .replace(",", "")
                        .trim()
                        .ifBlank { "0" }
                )
            } catch (_: Exception) {
                java.math.BigDecimal.ZERO
            }
        }

        val moneyFormat =
            java.text.DecimalFormat(
                "#,##0.00"
            )

        val walletTypes =
            arrayOf(
                "เงินสด",
                "ธนาคาร",
                "รายรับ",
                "ค่าใช้จ่าย",
                "เงินสำรอง",
                "อื่น ๆ"
            )

        // ------------------------------------------------------------
        // ADD / EDIT WALLET DIALOG
        // ------------------------------------------------------------

        fun showWalletEditor(
            existingId: String? = null
        ) {

            val currentWallets =
                loadWallets()

            val current =
                existingId?.let { id ->
                    currentWallets.firstOrNull {
                        it[0] == id
                    }
                }

            val form =
                android.widget.LinearLayout(this).apply {
                    orientation =
                        android.widget.LinearLayout.VERTICAL

                    setPadding(
                        dp(20),
                        dp(8),
                        dp(20),
                        0
                    )
                }

            val nameInput =
                android.widget.EditText(this).apply {
                    hint = "ชื่อกระเป๋า"
                    setText(
                        current?.getOrNull(1)
                            ?: ""
                    )
                }

            form.addView(nameInput)

            val typeLabel =
                android.widget.TextView(this).apply {
                    text = "ประเภทกระเป๋า"
                    textSize = 14f
                    setTextColor(gray)

                    setPadding(
                        0,
                        dp(14),
                        0,
                        dp(4)
                    )
                }

            form.addView(typeLabel)

            val typeSpinner =
                android.widget.Spinner(this)

            val typeAdapter =
                android.widget.ArrayAdapter(
                    this,
                    android.R.layout.simple_spinner_item,
                    walletTypes
                ).apply {
                    setDropDownViewResource(
                        android.R.layout.simple_spinner_dropdown_item
                    )
                }

            typeSpinner.adapter =
                typeAdapter

            val currentType =
                current?.getOrNull(2)

            val currentTypeIndex =
                walletTypes.indexOf(
                    currentType
                )

            if (currentTypeIndex >= 0) {
                typeSpinner.setSelection(
                    currentTypeIndex
                )
            }

            form.addView(typeSpinner)

            val balanceInput =
                android.widget.EditText(this).apply {
                    hint = "ยอดตั้งต้น เช่น 5000"

                    inputType =
                        android.text.InputType.TYPE_CLASS_NUMBER or
                        android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL or
                        android.text.InputType.TYPE_NUMBER_FLAG_SIGNED

                    setText(
                        current?.getOrNull(3)
                            ?: "0"
                    )
                }

            form.addView(balanceInput)

            val noteInput =
                android.widget.EditText(this).apply {
                    hint = "รายละเอียดเพิ่มเติม (ไม่บังคับ)"

                    setText(
                        current?.getOrNull(4)
                            ?: ""
                    )
                }

            form.addView(noteInput)

            val dialog =
                android.app.AlertDialog.Builder(this)
                    .setTitle(
                        if (current == null) {
                            "เพิ่มกระเป๋า"
                        } else {
                            "แก้ไขกระเป๋า"
                        }
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

                dialog
                    .getButton(
                        android.app.AlertDialog.BUTTON_POSITIVE
                    )
                    .setOnClickListener {

                        val walletName =
                            clean(
                                nameInput.text
                                    .toString()
                            )

                        if (walletName.isBlank()) {

                            nameInput.error =
                                "กรุณาใส่ชื่อกระเป๋า"

                            return@setOnClickListener
                        }

                        val rawBalance =
                            balanceInput.text
                                .toString()
                                .replace(",", "")
                                .trim()

                        val balance =
                            try {
                                java.math.BigDecimal(
                                    rawBalance.ifBlank {
                                        "0"
                                    }
                                )
                            } catch (_: Exception) {

                                balanceInput.error =
                                    "ยอดเงินไม่ถูกต้อง"

                                return@setOnClickListener
                            }

                        val type =
                            clean(
                                typeSpinner.selectedItem
                                    ?.toString()
                                    ?: "อื่น ๆ"
                            )

                        val note =
                            clean(
                                noteInput.text
                                    .toString()
                            )

                        val id =
                            current?.getOrNull(0)
                                ?: System.currentTimeMillis()
                                    .toString()

                        val duplicateName =
                            currentWallets.any {
                                it[0] != id &&
                                it[1].equals(
                                    walletName,
                                    ignoreCase = true
                                )
                            }

                        if (duplicateName) {

                            nameInput.error =
                                "มีกระเป๋าชื่อนี้แล้ว"

                            return@setOnClickListener
                        }

                        val updated =
                            currentWallets
                                .filterNot {
                                    it[0] == id
                                }
                                .toMutableList()

                        updated.add(
                            listOf(
                                id,
                                walletName,
                                type,
                                balance
                                    .stripTrailingZeros()
                                    .toPlainString(),
                                note
                            )
                        )

                        saveWallets(updated)

                        dialog.dismiss()

                        // Redraw from persisted data
                        showMoneyManager()
                    }
            }

            dialog.show()
        }

        // ------------------------------------------------------------
        // DELETE WALLET
        // ------------------------------------------------------------

        fun confirmDeleteWallet(
            walletId: String,
            walletName: String
        ) {
            // PC_DRONE_PHASE_2FA_DELETE_GUARD
            val hasFinanceLink =
                prefs.getStringSet("money_manager_links", emptySet()).orEmpty().any { link ->
                    link.split("|||", ignoreCase = false, limit = 2)
                        .getOrNull(1)?.trim() == walletId
                }

            val hasJobLink =
                prefs.getStringSet("money_manager_job_links", emptySet()).orEmpty().any { link ->
                    link.split("|||", ignoreCase = false, limit = 2)
                        .getOrNull(1)?.trim() == walletId
                }

            val hasTransferHistory =
                prefs.getStringSet("money_manager_transfers", emptySet()).orEmpty().any { record ->
                    val parts = record.split("|||", ignoreCase = false, limit = 5)
                    parts.getOrNull(1)?.trim() == walletId ||
                        parts.getOrNull(2)?.trim() == walletId
                }

            if (hasFinanceLink || hasJobLink || hasTransferHistory) {
                android.app.AlertDialog.Builder(this)
                    .setTitle("ยังลบกระเป๋านี้ไม่ได้")
                    .setMessage(
                        "กระเป๋า \"$walletName\" มีประวัติหรือหลักฐานทางการเงินเชื่อมอยู่\n\n" +
                        "ระบบจึงป้องกันการลบ เพื่อไม่ให้รายการบัญชี งานบิน หรือประวัติโอนเงินขาดการเชื่อมโยง"
                    )
                    .setPositiveButton("ตกลง", null)
                    .show()
                return
            }

            android.app.AlertDialog.Builder(this)
                .setTitle("ลบกระเป๋า")
                .setMessage(
                    "ต้องการลบ \"$walletName\" ใช่หรือไม่?\n\n" +
                    "กระเป๋านี้ไม่มีรายการทางการเงินหรืองานบินเชื่อมอยู่"
                )
                .setNegativeButton("ยกเลิก", null)
                .setPositiveButton("ลบ") { _, _ ->
                    val updated =
                        loadWallets().filterNot {
                            it[0] == walletId
                        }

                    saveWallets(updated)
                    showMoneyManager()
                }
                .show()
        }

        // ------------------------------------------------------------
        // PAGE
        // ------------------------------------------------------------

        val wallets =
            loadWallets()

        fun computedWalletBalance(
            walletId: String,
            openingBalance: java.math.BigDecimal
        ): java.math.BigDecimal {

            val links =
                prefs.getStringSet(
                    "money_manager_links",
                    emptySet()
                )?.toList()
                    ?: emptyList()

            val transactionIds =
                links.mapNotNull { link ->

                    val p =
                        link.split(
                            "|||",
                            ignoreCase = false,
                            limit = 2
                        )

                    val txId =
                        p.getOrNull(0)
                            ?.trim()
                            .orEmpty()

                    val linkedWalletId =
                        p.getOrNull(1)
                            ?.trim()
                            .orEmpty()

                    if (
                        txId.isNotBlank() &&
                        linkedWalletId == walletId
                    ) {
                        txId
                    } else {
                        null
                    }
                }.toSet()

            var movement =
                java.math.BigDecimal.ZERO

            val financeRecords =
                prefs.getStringSet(
                    "finance_transactions",
                    emptySet()
                )?.toList()
                    ?: emptyList()

            financeRecords.forEach { record ->

                val p =
                    record.split(
                        "|||",
                        ignoreCase = false,
                        limit = 5
                    )

                val txId =
                    p.getOrNull(0)
                        ?.trim()
                        .orEmpty()

                if (txId in transactionIds) {

                    val type =
                        p.getOrNull(1)
                            ?.trim()
                            .orEmpty()

                    val amount =
                        parseMoney(
                            p.getOrNull(3)
                                ?: "0"
                        )

                    when (type) {

                        "INCOME" ->
                            movement =
                                movement.add(amount)

                        "EXPENSE" ->
                            movement =
                                movement.subtract(amount)
                    }
                }
            }

            // =============================================
            // PHASE 2C - JOB INCOME LINKED TO WALLET
            // =============================================

            val jobLinks =
                prefs.getStringSet(
                    "money_manager_job_links",
                    emptySet()
                )?.toList()
                    ?: emptyList()

            val linkedJobIds =
                jobLinks.mapNotNull { link ->

                    val p =
                        link.split(
                            "|||",
                            ignoreCase = false,
                            limit = 2
                        )

                    val jobId =
                        p.getOrNull(0)
                            ?.trim()
                            .orEmpty()

                    val linkedWalletId =
                        p.getOrNull(1)
                            ?.trim()
                            .orEmpty()

                    if (
                        jobId.isNotBlank() &&
                        linkedWalletId == walletId
                    ) {
                        jobId
                    } else {
                        null
                    }

                }.toSet()

            var jobIncomeMovement =
                java.math.BigDecimal.ZERO

            val jobs =
                prefs.getStringSet(
                    "flight_jobs",
                    emptySet()
                )?.toList()
                    ?: emptyList()

            jobs.forEach { record ->

                val p =
                    record.split(
                        "|||",
                        ignoreCase = false,
                        limit = 9
                    )

                val jobId =
                    p.getOrNull(0)
                        ?.trim()
                        .orEmpty()

                val total =
                    parseMoney(
                        p.getOrNull(6)
                            ?: "0"
                    )

                val status =
                    p.getOrNull(7)
                        ?.trim()
                        .orEmpty()

                if (
                    jobId in linkedJobIds &&
                    status == "เสร็จแล้ว"
                ) {
                    jobIncomeMovement =
                        jobIncomeMovement.add(
                            total
                        )
                }
            }

            // =============================================
            // PC_DRONE_PHASE_2E_WALLET_TRANSFER
            // Transfer = movement between wallets only.
            // It must NOT become business income/expense.
            // =============================================

            var transferMovement =
                java.math.BigDecimal.ZERO

            val transfers =
                prefs.getStringSet(
                    "money_manager_transfers",
                    emptySet()
                )?.toList()
                    ?: emptyList()

            transfers.forEach { record ->

                val p =
                    record.split(
                        "|||",
                        ignoreCase = false,
                        limit = 5
                    )

                val fromWalletId =
                    p.getOrNull(1)
                        ?.trim()
                        .orEmpty()

                val toWalletId =
                    p.getOrNull(2)
                        ?.trim()
                        .orEmpty()

                val amount =
                    parseMoney(
                        p.getOrNull(3)
                            ?: "0"
                    )

                if (
                    fromWalletId == walletId
                ) {
                    transferMovement =
                        transferMovement.subtract(
                            amount
                        )
                }

                if (
                    toWalletId == walletId
                ) {
                    transferMovement =
                        transferMovement.add(
                            amount
                        )
                }
            }

            return openingBalance
                .add(movement)
                .add(jobIncomeMovement)
                .add(transferMovement)
        }

        // ------------------------------------------------------------
        // PHASE 2E - TRANSFER BETWEEN WALLETS
        // ------------------------------------------------------------

        fun showWalletTransferDialog() {

            if (wallets.size < 2) {

                android.widget.Toast.makeText(
                    this,
                    "ต้องมีกระเป๋าอย่างน้อย 2 ใบจึงจะโอนเงินได้",
                    android.widget.Toast.LENGTH_LONG
                ).show()

                return
            }

            val walletNames =
                wallets.map {
                    it.getOrNull(1)
                        ?: "ไม่ระบุชื่อ"
                }

            val container =
                android.widget.LinearLayout(this).apply {

                    orientation =
                        android.widget.LinearLayout.VERTICAL

                    setPadding(
                        dp(20),
                        dp(8),
                        dp(20),
                        0
                    )
                }

            container.addView(
                android.widget.TextView(this).apply {
                    text = "จากกระเป๋า"
                    textSize = 13f
                    setTextColor(gray)
                    setPadding(
                        0,
                        dp(5),
                        0,
                        dp(4)
                    )
                }
            )

            val fromSpinner =
                android.widget.Spinner(this).apply {

                    adapter =
                        android.widget.ArrayAdapter(
                            this@MainActivity,
                            android.R.layout.simple_spinner_dropdown_item,
                            walletNames
                        )
                }

            container.addView(fromSpinner)

            container.addView(
                android.widget.TextView(this).apply {
                    text = "ไปยังกระเป๋า"
                    textSize = 13f
                    setTextColor(gray)
                    setPadding(
                        0,
                        dp(14),
                        0,
                        dp(4)
                    )
                }
            )

            val toSpinner =
                android.widget.Spinner(this).apply {

                    adapter =
                        android.widget.ArrayAdapter(
                            this@MainActivity,
                            android.R.layout.simple_spinner_dropdown_item,
                            walletNames
                        )

                    if (wallets.size > 1) {
                        setSelection(1)
                    }
                }

            container.addView(toSpinner)

            val amountInput =
                android.widget.EditText(this).apply {

                    hint = "จำนวนเงิน"

                    inputType =
                        android.text.InputType.TYPE_CLASS_NUMBER or
                        android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL

                    setPadding(
                        0,
                        dp(16),
                        0,
                        dp(8)
                    )
                }

            container.addView(amountInput)

            val noteInput =
                android.widget.EditText(this).apply {

                    hint = "หมายเหตุ เช่น ฝากเข้าธนาคาร"

                    setPadding(
                        0,
                        dp(8),
                        0,
                        dp(8)
                    )
                }

            container.addView(noteInput)

            val dialog =
                android.app.AlertDialog.Builder(this)
                    .setTitle("โอนเงินระหว่างกระเป๋า")
                    .setView(container)
                    .setNegativeButton(
                        "ยกเลิก",
                        null
                    )
                    .setPositiveButton(
                        "โอนเงิน",
                        null
                    )
                    .create()

            dialog.setOnShowListener {

                val saveButton =
                    dialog.getButton(
                        android.app.AlertDialog.BUTTON_POSITIVE
                    )

                saveButton.setOnClickListener {

                    val fromIndex =
                        fromSpinner.selectedItemPosition

                    val toIndex =
                        toSpinner.selectedItemPosition

                    if (
                        fromIndex !in wallets.indices ||
                        toIndex !in wallets.indices
                    ) {
                        return@setOnClickListener
                    }

                    if (
                        fromIndex == toIndex
                    ) {

                        android.widget.Toast.makeText(
                            this,
                            "กระเป๋าต้นทางและปลายทางต้องไม่ใช่ใบเดียวกัน",
                            android.widget.Toast.LENGTH_LONG
                        ).show()

                        return@setOnClickListener
                    }

                    val amount =
                        parseMoney(
                            amountInput.text
                                ?.toString()
                                .orEmpty()
                        )

                    if (
                        amount.compareTo(
                            java.math.BigDecimal.ZERO
                        ) <= 0
                    ) {

                        android.widget.Toast.makeText(
                            this,
                            "กรุณาใส่จำนวนเงินมากกว่า 0",
                            android.widget.Toast.LENGTH_LONG
                        ).show()

                        return@setOnClickListener
                    }

                    val fromWallet =
                        wallets[fromIndex]

                    val toWallet =
                        wallets[toIndex]

                    val fromWalletId =
                        fromWallet.getOrNull(0)
                            .orEmpty()

                    val toWalletId =
                        toWallet.getOrNull(0)
                            .orEmpty()

                    val fromOpening =
                        parseMoney(
                            fromWallet.getOrNull(3)
                                ?: "0"
                        )

                    val sourceBalance =
                        computedWalletBalance(
                            fromWalletId,
                            fromOpening
                        )

                    if (
                        sourceBalance.compareTo(
                            amount
                        ) < 0
                    ) {

                        android.widget.Toast.makeText(
                            this,
                            "ยอดเงินในกระเป๋าต้นทางไม่เพียงพอ",
                            android.widget.Toast.LENGTH_LONG
                        ).show()

                        return@setOnClickListener
                    }

                    val transferId =
                        System.currentTimeMillis()
                            .toString()

                    val note =
                        noteInput.text
                            ?.toString()
                            ?.trim()
                            .orEmpty()

                    val transferRecord =
                        listOf(
                            transferId,
                            fromWalletId,
                            toWalletId,
                            amount
                                .stripTrailingZeros()
                                .toPlainString(),
                            note
                        ).joinToString("|||")

                    val current =
                        prefs.getStringSet(
                            "money_manager_transfers",
                            emptySet()
                        )?.toMutableSet()
                            ?: mutableSetOf()

                    current.add(
                        transferRecord
                    )

                    prefs.edit()
                        .putStringSet(
                            "money_manager_transfers",
                            current
                        )
                        .apply()

                    dialog.dismiss()

                    showMoneyManager()
                }
            }

            dialog.show()
        }

        val totalBalance =
            wallets.fold(
                java.math.BigDecimal.ZERO
            ) { total, wallet ->

                val openingBalance =
                    parseMoney(
                        wallet.getOrNull(3)
                            ?: "0"
                    )

                val walletId =
                    wallet.getOrNull(0)
                        .orEmpty()

                total.add(
                    computedWalletBalance(
                        walletId,
                        openingBalance
                    )
                )
            }

        // PC_DRONE_PHASE_2FA_STEP1A
        var auditManualIncome = java.math.BigDecimal.ZERO
        var auditManualExpense = java.math.BigDecimal.ZERO
        var auditCompletedJobIncome = java.math.BigDecimal.ZERO

        prefs.getStringSet("finance_transactions", emptySet()).orEmpty().forEach { record ->
            val parts = record.split("|||", ignoreCase = false, limit = 5)
            val type = parts.getOrNull(1)?.trim().orEmpty()
            val amount = parseMoney(parts.getOrNull(3) ?: "0")
            if (type == "INCOME") auditManualIncome = auditManualIncome.add(amount)
            if (type == "EXPENSE") auditManualExpense = auditManualExpense.add(amount)
        }

        prefs.getStringSet("flight_jobs", emptySet()).orEmpty().forEach { record ->
            val parts = record.split("|||", ignoreCase = false, limit = 9)
            val amount = parseMoney(parts.getOrNull(6) ?: "0")
            val status = parts.getOrNull(7)?.trim().orEmpty()
            if (status == "เสร็จแล้ว") auditCompletedJobIncome = auditCompletedJobIncome.add(amount)
        }

        val auditBusinessNet = auditCompletedJobIncome.add(auditManualIncome).subtract(auditManualExpense)

        // PC_DRONE_PHASE_2FA_STEP1B
        val auditFinanceLinks = prefs.getStringSet("money_manager_links", emptySet()).orEmpty()
        val auditLinkedFinanceIds = auditFinanceLinks.mapNotNull { link ->
            link.split("|||", ignoreCase = false, limit = 2).getOrNull(0)?.trim()?.takeIf { it.isNotEmpty() }
        }.toSet()

        var auditUnlinkedIncomeCount = 0
        var auditUnlinkedIncomeAmount = java.math.BigDecimal.ZERO
        var auditUnlinkedExpenseCount = 0
        var auditUnlinkedExpenseAmount = java.math.BigDecimal.ZERO

        prefs.getStringSet("finance_transactions", emptySet()).orEmpty().forEach { record ->
            val parts = record.split("|||", ignoreCase = false, limit = 5)
            val transactionId = parts.getOrNull(0)?.trim().orEmpty()
            val type = parts.getOrNull(1)?.trim().orEmpty()
            val amount = parseMoney(parts.getOrNull(3) ?: "0")
            if (transactionId.isNotEmpty() && transactionId !in auditLinkedFinanceIds) {
                if (type == "INCOME") {
                    auditUnlinkedIncomeCount++
                    auditUnlinkedIncomeAmount = auditUnlinkedIncomeAmount.add(amount)
                }
                if (type == "EXPENSE") {
                    auditUnlinkedExpenseCount++
                    auditUnlinkedExpenseAmount = auditUnlinkedExpenseAmount.add(amount)
                }
            }
        }

        // PC_DRONE_PHASE_2FA_STEP1C
        val auditJobLinks = prefs.getStringSet("money_manager_job_links", emptySet()).orEmpty()
        val auditLinkedJobIds = auditJobLinks.mapNotNull { link ->
            link.split("|||", ignoreCase = false, limit = 2).getOrNull(0)?.trim()?.takeIf { it.isNotEmpty() }
        }.toSet()

        var auditUnlinkedCompletedJobCount = 0
        var auditUnlinkedCompletedJobAmount = java.math.BigDecimal.ZERO

        prefs.getStringSet("flight_jobs", emptySet()).orEmpty().forEach { record ->
            val parts = record.split("|||", ignoreCase = false, limit = 9)
            val jobId = parts.getOrNull(0)?.trim().orEmpty()
            val amount = parseMoney(parts.getOrNull(6) ?: "0")
            val status = parts.getOrNull(7)?.trim().orEmpty()

            if (status == "เสร็จแล้ว" && jobId.isNotEmpty() && jobId !in auditLinkedJobIds) {
                auditUnlinkedCompletedJobCount++
                auditUnlinkedCompletedJobAmount = auditUnlinkedCompletedJobAmount.add(amount)
            }
        }

        // PC_DRONE_PHASE_2FA_STEP1D
        val auditValidWalletIds = wallets.map { it[0] }.toSet()

        val auditOpeningTotal = wallets.fold(java.math.BigDecimal.ZERO) { sum, wallet ->
            sum.add(parseMoney(wallet[3]))
        }

        val auditFinanceTransactionIds =
            prefs.getStringSet("finance_transactions", emptySet()).orEmpty().mapNotNull { record ->
                record.split("|||", ignoreCase = false, limit = 5)
                    .getOrNull(0)?.trim()?.takeIf { it.isNotEmpty() }
            }.toSet()

        var auditOrphanFinanceLinkCount = 0

        prefs.getStringSet("money_manager_links", emptySet()).orEmpty().forEach { link ->
            val parts = link.split("|||", ignoreCase = false, limit = 2)
            val transactionId = parts.getOrNull(0)?.trim().orEmpty()
            val walletId = parts.getOrNull(1)?.trim().orEmpty()

            if (
                transactionId.isEmpty() ||
                walletId.isEmpty() ||
                transactionId !in auditFinanceTransactionIds ||
                walletId !in auditValidWalletIds
            ) {
                auditOrphanFinanceLinkCount++
            }
        }

        // PC_DRONE_PHASE_2FA_STEP1E
        val auditFlightJobStatusById =
            prefs.getStringSet("flight_jobs", emptySet()).orEmpty().mapNotNull { record ->
                val p = record.split("|||", ignoreCase = false, limit = 9)
                val id = p.getOrNull(0)?.trim().orEmpty()
                if (id.isNotEmpty()) id to p.getOrNull(7)?.trim().orEmpty() else null
            }.toMap()

        var auditOrphanJobLinkCount = 0
        val auditJobLinkCounts = mutableMapOf<String, Int>()

        auditJobLinks.forEach { link ->
            val p = link.split("|||", ignoreCase = false, limit = 2)
            val jobId = p.getOrNull(0)?.trim().orEmpty()
            val walletId = p.getOrNull(1)?.trim().orEmpty()

            if (jobId.isNotEmpty()) {
                auditJobLinkCounts[jobId] = (auditJobLinkCounts[jobId] ?: 0) + 1
            }

            if (
                jobId.isEmpty() ||
                walletId.isEmpty() ||
                jobId !in auditFlightJobStatusById ||
                walletId !in auditValidWalletIds ||
                auditFlightJobStatusById[jobId] != "เสร็จแล้ว"
            ) {
                auditOrphanJobLinkCount++
            }
        }

        val auditDuplicateJobLinkCount =
            auditJobLinkCounts.values.count { it > 1 }

        val auditFinanceLinkCounts = mutableMapOf<String, Int>()
        prefs.getStringSet("money_manager_links", emptySet()).orEmpty().forEach { link ->
            val id = link.split("|||", ignoreCase = false, limit = 2)
                .getOrNull(0)?.trim().orEmpty()
            if (id.isNotEmpty()) {
                auditFinanceLinkCounts[id] = (auditFinanceLinkCounts[id] ?: 0) + 1
            }
        }

        val auditDuplicateFinanceLinkCount =
            auditFinanceLinkCounts.values.count { it > 1 }

        var auditInvalidTransferCount = 0

        prefs.getStringSet("money_manager_transfers", emptySet()).orEmpty().forEach { record ->
            val p = record.split("|||", ignoreCase = false, limit = 5)
            val transferId = p.getOrNull(0)?.trim().orEmpty()
            val fromWalletId = p.getOrNull(1)?.trim().orEmpty()
            val toWalletId = p.getOrNull(2)?.trim().orEmpty()
            val amount = parseMoney(p.getOrNull(3) ?: "0")

            if (
                transferId.isEmpty() ||
                fromWalletId.isEmpty() ||
                toWalletId.isEmpty() ||
                fromWalletId !in auditValidWalletIds ||
                toWalletId !in auditValidWalletIds ||
                fromWalletId == toWalletId ||
                amount <= java.math.BigDecimal.ZERO
            ) {
                auditInvalidTransferCount++
            }
        }

        // PC_DRONE_PHASE_2FA_STEP1F
        val auditDifference =
            auditBusinessNet.subtract(totalBalance)

        val auditAnomalyCount =
            auditOrphanFinanceLinkCount +
            auditOrphanJobLinkCount +
            auditDuplicateFinanceLinkCount +
            auditDuplicateJobLinkCount +
            auditInvalidTransferCount

        val auditPass =
            auditOpeningTotal.compareTo(java.math.BigDecimal.ZERO) == 0 &&
            auditUnlinkedIncomeCount == 0 &&
            auditUnlinkedExpenseCount == 0 &&
            auditUnlinkedCompletedJobCount == 0 &&
            auditAnomalyCount == 0 &&
            auditDifference.compareTo(java.math.BigDecimal.ZERO) == 0

        val root =
            android.widget.LinearLayout(this).apply {
                orientation =
                    android.widget.LinearLayout.VERTICAL

                setPadding(
                    dp(16),
                    dp(18),
                    dp(16),
                    dp(32)
                )

                setBackgroundColor(pageBg)
            }

        root.addView(
            android.widget.TextView(this).apply {
                text = "‹  กลับ"
                textSize = 17f
                setTextColor(greenDark)

                setTypeface(
                    typeface,
                    android.graphics.Typeface.BOLD
                )

                setPadding(
                    dp(4),
                    dp(8),
                    dp(8),
                    dp(12)
                )

                isClickable = true
                isFocusable = true

                setOnClickListener {
                    showScreen(
                        AppRoute.DASHBOARD
                    )
                }
            }
        )

        root.addView(
            android.widget.TextView(this).apply {
                text = "บริหารเงิน"
                textSize = 28f
                setTextColor(dark)

                setTypeface(
                    typeface,
                    android.graphics.Typeface.BOLD
                )
            }
        )

        root.addView(
            android.widget.TextView(this).apply {
                text =
                    "จัดการกระเป๋าเงินและเงินทุนของธุรกิจ"

                textSize = 14f
                setTextColor(gray)

                setPadding(
                    0,
                    dp(4),
                    0,
                    dp(16)
                )
            }
        )

        // PC_DRONE_PHASE_2FB_SUMMARY_CARDS
        val summaryContainer =
            android.widget.LinearLayout(this).apply {
                orientation = android.widget.LinearLayout.VERTICAL
            }

        fun addSummaryRow(
            leftTitle: String,
            leftValue: String,
            rightTitle: String,
            rightValue: String
        ) {
            val row =
                android.widget.LinearLayout(this).apply {
                    orientation = android.widget.LinearLayout.HORIZONTAL
                    weightSum = 2f
                }

            fun card(title: String, value: String) =
                android.widget.LinearLayout(this).apply {
                    orientation = android.widget.LinearLayout.VERTICAL
                    setPadding(dp(14), dp(14), dp(14), dp(14))
                    background = rounded(greenDark, dp(16))

                    addView(
                        android.widget.TextView(this@MainActivity).apply {
                            text = title
                            textSize = 13f
                            setTextColor(android.graphics.Color.WHITE)
                        }
                    )

                    addView(
                        android.widget.TextView(this@MainActivity).apply {
                            text = value
                            textSize = 20f
                            setTextColor(android.graphics.Color.WHITE)
                            setTypeface(
                                typeface,
                                android.graphics.Typeface.BOLD
                            )
                            setPadding(0, dp(6), 0, 0)
                        }
                    )
                }

            row.addView(
                card(leftTitle, leftValue),
                android.widget.LinearLayout.LayoutParams(
                    0,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                ).apply {
                    rightMargin = dp(5)
                }
            )

            // PC_DRONE_MONTHLY_EXPENSE_CARD
            val rightCard =
                if (rightTitle == "รายจ่ายต่อเดือน") {
                    android.widget.LinearLayout(this).apply {
                        orientation = android.widget.LinearLayout.VERTICAL
                        setPadding(dp(14), dp(14), dp(14), dp(12))
                        background = rounded(red, dp(16))

                        addView(
                            android.widget.TextView(this@MainActivity).apply {
                                text = rightTitle
                                textSize = 13f
                                setTextColor(android.graphics.Color.WHITE)
                            }
                        )

                        addView(
                            android.widget.TextView(this@MainActivity).apply {
                                text = rightValue
                                textSize = 20f
                                setTextColor(android.graphics.Color.WHITE)
                                setTypeface(
                                    typeface,
                                    android.graphics.Typeface.BOLD
                                )
                                setPadding(0, dp(6), 0, dp(8))
                            }
                        )

                        addView(
                            android.widget.Button(this@MainActivity).apply {
                                text = "รายละเอียด"
                                isAllCaps = false
                                textSize = 13f
                                setTextColor(android.graphics.Color.WHITE)
                                background =
                                    rounded(
                                        android.graphics.Color.rgb(239, 108, 0),
                                        dp(10)
                                    )
                                setOnClickListener {
                                    showMonthlyExpenseReport()
                                }
                            },
                            android.widget.LinearLayout.LayoutParams(
                                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                                dp(42)
                            )
                        )
                    }
                } else {
                    card(rightTitle, rightValue)
                }

            row.addView(
                rightCard,
                android.widget.LinearLayout.LayoutParams(
                    0,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                ).apply {
                    leftMargin = dp(5)
                }
            )

            summaryContainer.addView(
                row,
                android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = dp(10)
                }
            )
        }

        val nowCalendar = java.util.Calendar.getInstance()
        val currentMonth = nowCalendar.get(java.util.Calendar.MONTH)
        val currentYear = nowCalendar.get(java.util.Calendar.YEAR)

        var monthlyExpenseTotal = java.math.BigDecimal.ZERO

        prefs.getStringSet("finance_transactions", emptySet()).orEmpty().forEach { record ->
            val parts = record.split("|||", ignoreCase = false, limit = 5)
            val txId = parts.getOrNull(0)?.trim().orEmpty()
            val type = parts.getOrNull(1)?.trim().orEmpty()
            val amount = parseMoney(parts.getOrNull(3) ?: "0")

            val time = txId.toLongOrNull() ?: 0L

            if (type == "EXPENSE" && time > 0L) {
                val cal = java.util.Calendar.getInstance().apply {
                    timeInMillis = time
                }

                if (
                    cal.get(java.util.Calendar.MONTH) == currentMonth &&
                    cal.get(java.util.Calendar.YEAR) == currentYear
                ) {
                    monthlyExpenseTotal = monthlyExpenseTotal.add(amount)
                }
            }
        }

        addSummaryRow(
            "ธุรกิจมีเงินเท่าไร",
            "${moneyFormat.format(auditBusinessNet)} บาท",
            "รายจ่ายต่อเดือน",
            "${moneyFormat.format(monthlyExpenseTotal)} บาท"
        )

        addSummaryRow(
            "เงินพร้อมใช้จริง",
            "${moneyFormat.format(totalBalance)} บาท",
            "กำไรจริง",
            "กำลังคำนวณ"
        )

        root.addView(
            summaryContainer,
            android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(6)
            }
        )

        // PC_DRONE_MONTHLY_OBLIGATIONS_ENTRY
        val monthlyObligationButton =
            android.widget.Button(this).apply {
                text = "รายการที่ต้องจ่ายประจำเดือน"
                isAllCaps = false
                textSize = 16f
                setTextColor(android.graphics.Color.WHITE)
                background = rounded(
                    android.graphics.Color.rgb(0, 121, 107),
                    dp(14)
                )
                setPadding(
                    dp(12),
                    dp(10),
                    dp(12),
                    dp(10)
                )
                setOnClickListener {
                    showMonthlyObligations()
                }
            }

        root.addView(
            monthlyObligationButton,
            android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                dp(52)
            ).apply {
                bottomMargin = dp(10)
            }
        )

        // PC_DRONE_PHASE_2FA_STEP2_AUDIT_UI
        val auditBox =
            android.widget.LinearLayout(this).apply {
                orientation = android.widget.LinearLayout.VERTICAL
                setPadding(dp(16), dp(16), dp(16), dp(16))
                background = rounded(
                    if (auditPass) android.graphics.Color.rgb(232, 245, 233)
                    else android.graphics.Color.rgb(255, 243, 224),
                    dp(16)
                )
            }

        auditBox.addView(
            android.widget.TextView(this).apply {
                text = "ศูนย์ตรวจสอบการเงิน"
                textSize = 20f
                setTextColor(dark)
                setTypeface(typeface, android.graphics.Typeface.BOLD)
            }
        )

        auditBox.addView(
            android.widget.TextView(this).apply {
                text =
                    if (auditPass) "✓ บัญชีและกระเป๋าเงินตรงกัน"
                    else "⚠ พบรายการที่ต้องตรวจสอบ"
                textSize = 16f
                setTextColor(if (auditPass) greenDark else red)
                setTypeface(typeface, android.graphics.Typeface.BOLD)
                setPadding(0, dp(6), 0, dp(10))
            }
        )

        val auditSummary =
            buildString {
                append("ยอดตามบัญชี: ฿")
                append(moneyFormat.format(auditBusinessNet))
                append("\nยอดรวมทุกกระเป๋า: ฿")
                append(moneyFormat.format(totalBalance))
                append("\nผลต่างบัญชี-กระเป๋า: ฿")
                append(moneyFormat.format(auditDifference))

                append("\n\nรายรับยังไม่ระบุกระเป๋า: ")
                append(auditUnlinkedIncomeCount)
                append(" รายการ  ฿")
                append(moneyFormat.format(auditUnlinkedIncomeAmount))

                append("\nรายจ่ายยังไม่ระบุกระเป๋าที่จ่าย: ")
                append(auditUnlinkedExpenseCount)
                append(" รายการ  ฿")
                append(moneyFormat.format(auditUnlinkedExpenseAmount))

                append("\nงานบินเสร็จแล้วยังไม่ระบุกระเป๋ารับเงิน: ")
                append(auditUnlinkedCompletedJobCount)
                append(" รายการ  ฿")
                append(moneyFormat.format(auditUnlinkedCompletedJobAmount))

                append("\nรายการเชื่อมโยง/โอนเงินผิดปกติ: ")
                append(auditAnomalyCount)
                append(" รายการ")

                if (auditOpeningTotal.compareTo(java.math.BigDecimal.ZERO) != 0) {
                    append("\nยอดตั้งต้นกระเป๋า: ฿")
                    append(moneyFormat.format(auditOpeningTotal))
                    append("  (ต้องตรวจสอบ)")
                }
            }

        auditBox.addView(
            android.widget.TextView(this).apply {
                text = auditSummary
                textSize = 14f
                setTextColor(dark)
                setLineSpacing(0f, 1.18f)
            }
        )

        root.addView(
            auditBox,
            android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(16)
            }
        )

        val totalBox =
            android.widget.LinearLayout(this).apply {
                orientation =
                    android.widget.LinearLayout.VERTICAL

                setPadding(
                    dp(18),
                    dp(18),
                    dp(18),
                    dp(18)
                )

                background =
                    rounded(
                        if (
                            totalBalance.compareTo(
                                java.math.BigDecimal.ZERO
                            ) >= 0
                        ) {
                            greenDark
                        } else {
                            red
                        },
                        18
                    )
            }

        totalBox.addView(
            android.widget.TextView(this).apply {
                text = "ยอดรวมทุกกระเป๋า"
                textSize = 15f
                setTextColor(
                    android.graphics.Color.WHITE
                )
            }
        )

        totalBox.addView(
            android.widget.TextView(this).apply {
                text =
                    "${moneyFormat.format(totalBalance)} บาท"

                textSize = 28f

                setTextColor(
                    android.graphics.Color.WHITE
                )

                setTypeface(
                    typeface,
                    android.graphics.Typeface.BOLD
                )

                setPadding(
                    0,
                    dp(5),
                    0,
                    0
                )
            }
        )

        root.addView(totalBox)

        root.addView(
            android.widget.TextView(this).apply {
                text = "กระเป๋าเงิน"
                textSize = 20f
                setTextColor(dark)

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

        if (wallets.isEmpty()) {

            root.addView(
                android.widget.TextView(this).apply {
                    text =
                        "ยังไม่มีกระเป๋าเงิน\nกด + เพิ่มกระเป๋า เพื่อเริ่มใช้งาน"

                    textSize = 15f
                    gravity =
                        android.view.Gravity.CENTER

                    setTextColor(gray)

                    setPadding(
                        dp(16),
                        dp(24),
                        dp(16),
                        dp(24)
                    )
                }
            )
        }

        wallets.forEach { wallet ->

            val id =
                wallet.getOrNull(0)
                    ?: return@forEach

            val name =
                wallet.getOrNull(1)
                    ?: "ไม่ระบุชื่อ"

            val type =
                wallet.getOrNull(2)
                    ?: "อื่น ๆ"

            val openingBalance =
                parseMoney(
                    wallet.getOrNull(3)
                        ?: "0"
                )

            val balance =
                computedWalletBalance(
                    id,
                    openingBalance
                )

            val note =
                wallet.getOrNull(4)
                    .orEmpty()

            val icon =
                when (type) {
                    "เงินสด" -> "฿"
                    "ธนาคาร" -> "▣"
                    "รายรับ" -> "✓"
                    "ค่าใช้จ่าย" -> "⚙"
                    "เงินสำรอง" -> "★"
                    else -> "•"
                }

            val card =
                android.widget.LinearLayout(this).apply {

                    orientation =
                        android.widget.LinearLayout.HORIZONTAL

                    gravity =
                        android.view.Gravity.CENTER_VERTICAL

                    setPadding(
                        dp(14),
                        dp(14),
                        dp(10),
                        dp(14)
                    )

                    background =
                        rounded(
                            android.graphics.Color.WHITE,
                            16
                        )

                    elevation =
                        dp(2).toFloat()

                    isClickable = true
                    isFocusable = true

                    setOnClickListener {
                        showWalletEditor(id)
                    }

                    setOnLongClickListener {
                        confirmDeleteWallet(
                            id,
                            name
                        )

                        true
                    }
                }

            card.addView(
                android.widget.TextView(this).apply {
                    text = icon
                    textSize = 24f

                    gravity =
                        android.view.Gravity.CENTER
                },
                android.widget.LinearLayout.LayoutParams(
                    dp(45),
                    dp(48)
                )
            )

            val textBox =
                android.widget.LinearLayout(this).apply {
                    orientation =
                        android.widget.LinearLayout.VERTICAL

                    setPadding(
                        dp(9),
                        0,
                        dp(8),
                        0
                    )
                }

            textBox.addView(
                android.widget.TextView(this).apply {
                    text = name
                    textSize = 17f
                    setTextColor(dark)

                    setTypeface(
                        typeface,
                        android.graphics.Typeface.BOLD
                    )
                }
            )

            textBox.addView(
                android.widget.TextView(this).apply {

                    text =
                        if (note.isBlank()) {
                            type
                        } else {
                            "$type • $note"
                        }

                    textSize = 12f
                    setTextColor(gray)
                }
            )

            card.addView(
                textBox,
                android.widget.LinearLayout.LayoutParams(
                    0,
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
                )
            )

            val rightBox =
                android.widget.LinearLayout(this).apply {
                    orientation =
                        android.widget.LinearLayout.VERTICAL

                    gravity =
                        android.view.Gravity.END
                }

            rightBox.addView(
                android.widget.TextView(this).apply {

                    text =
                        "${moneyFormat.format(balance)} บาท"

                    textSize = 15f

                    setTextColor(
                        if (
                            balance.compareTo(
                                java.math.BigDecimal.ZERO
                            ) >= 0
                        ) {
                            greenDark
                        } else {
                            red
                        }
                    )

                    setTypeface(
                        typeface,
                        android.graphics.Typeface.BOLD
                    )
                }
            )

            rightBox.addView(
                android.widget.TextView(this).apply {
                    text = "แตะเพื่อแก้ไข"
                    textSize = 10f
                    setTextColor(gray)
                }
            )

            // PC_DRONE_PHASE_2D_WALLET_DETAIL
            rightBox.addView(
                android.widget.TextView(this).apply {

                    text = "รายละเอียด"
                    textSize = 12f
                    gravity =
                        android.view.Gravity.CENTER

                    setTextColor(
                        android.graphics.Color.WHITE
                    )

                    setTypeface(
                        typeface,
                        android.graphics.Typeface.BOLD
                    )

                    setPadding(
                        dp(10),
                        dp(6),
                        dp(10),
                        dp(6)
                    )

                    background =
                        rounded(
                            greenDark,
                            10
                        )

                    isClickable = true
                    isFocusable = true

                    setOnClickListener {
                        showWalletDetail(
                            walletId = id,
                            walletName = name,
                            openingBalance = openingBalance
                        )
                    }
                },
                android.widget.LinearLayout.LayoutParams(
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = dp(7)
                }
            )

            card.addView(rightBox)

            root.addView(
                card,
                android.widget.LinearLayout.LayoutParams(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin =
                        dp(10)
                }
            )
        }

        root.addView(
            android.widget.TextView(this).apply {

                text =
                    "แตะกระเป๋า = แก้ไข   •   กดค้าง = ลบ"

                textSize = 11f
                gravity =
                    android.view.Gravity.CENTER

                setTextColor(gray)

                setPadding(
                    dp(4),
                    dp(2),
                    dp(4),
                    dp(10)
                )
            }
        )

        val addWallet =
            android.widget.TextView(this).apply {
                text = "+  เพิ่มกระเป๋า"
                textSize = 17f

                gravity =
                    android.view.Gravity.CENTER

                setTextColor(
                    android.graphics.Color.WHITE
                )

                setTypeface(
                    typeface,
                    android.graphics.Typeface.BOLD
                )

                setPadding(
                    dp(12),
                    dp(15),
                    dp(12),
                    dp(15)
                )

                background =
                    rounded(
                        green,
                        14
                    )

                isClickable = true
                isFocusable = true

                setOnClickListener {
                    showWalletEditor()
                }
            }

        root.addView(
            addWallet,
            android.widget.LinearLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        val transferWallet =
            android.widget.TextView(this).apply {

                text = "⇄  โอนเงินระหว่างกระเป๋า"
                textSize = 16f

                gravity =
                    android.view.Gravity.CENTER

                setTextColor(
                    greenDark
                )

                setTypeface(
                    typeface,
                    android.graphics.Typeface.BOLD
                )

                setPadding(
                    dp(12),
                    dp(14),
                    dp(12),
                    dp(14)
                )

                background =
                    rounded(
                        android.graphics.Color.WHITE,
                        14
                    )

                elevation =
                    dp(1).toFloat()

                isClickable = true
                isFocusable = true

                setOnClickListener {
                    showWalletTransferDialog()
                }
            }

        root.addView(
            transferWallet,
            android.widget.LinearLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(10)
            }
        )

        val phaseNote =
            android.widget.TextView(this).apply {

                text =
                    "Phase 2B: ยอดกระเป๋า = ยอดตั้งต้น + รายรับ - รายจ่าย " +
                    "ที่เลือกผูกกับกระเป๋านั้น"

                textSize = 11f
                gravity =
                    android.view.Gravity.CENTER

                setTextColor(gray)

                setPadding(
                    dp(8),
                    dp(12),
                    dp(8),
                    0
                )
            }

        root.addView(phaseNote)

        val scroll =
            android.widget.ScrollView(this).apply {

                isFillViewport = true

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



    // PC_DRONE_MONTHLY_OBLIGATIONS_SCREEN
    private fun showMonthlyObligations() {
        val pageBg = android.graphics.Color.rgb(245, 248, 246)
        val dark = android.graphics.Color.rgb(17, 17, 17)
        val green = android.graphics.Color.rgb(0, 121, 107)
        val gray = android.graphics.Color.rgb(100, 105, 102)

        // PC_DRONE_MONTHLY_OBLIGATIONS_TEMPLATE_STORAGE
        val prefs =
            getSharedPreferences(
                "pc_drone_v3_data",
                MODE_PRIVATE
            )

        val obligationTemplateKey =
            "money_manager_obligation_templates"

        val obligationDelimiter = "|||"

        fun loadObligationTemplates(): MutableList<List<String>> {
            return prefs
                .getStringSet(
                    obligationTemplateKey,
                    emptySet()
                )
                .orEmpty()
                .mapNotNull { record ->
                    val parts =
                        record.split(obligationDelimiter)

                    if (parts.size == 7) parts
                    else null
                }
                .sortedBy { it[6].toLongOrNull() ?: 0L }
                .toMutableList()
        }

        fun saveObligationTemplates(
            templates: List<List<String>>
        ) {
            val records =
                templates.map { parts ->
                    parts.joinToString(obligationDelimiter)
                }.toSet()

            prefs.edit()
                .putStringSet(
                    obligationTemplateKey,
                    records
                )
                .apply()
        }

        // PC_DRONE_MONTHLY_OBLIGATIONS_MONTH_STORAGE
        val obligationMonthKey =
            "money_manager_obligation_months"

        fun loadObligationMonths(): MutableList<List<String>> {
            return prefs
                .getStringSet(
                    obligationMonthKey,
                    emptySet()
                )
                .orEmpty()
                .mapNotNull { record ->
                    val parts =
                        record.split(obligationDelimiter)

                    if (parts.size == 7) parts
                    else null
                }
                .sortedBy { it[6].toLongOrNull() ?: 0L }
                .toMutableList()
        }

        fun saveObligationMonths(
            items: List<List<String>>
        ) {
            val records =
                items.map { parts ->
                    parts.joinToString(obligationDelimiter)
                }.toSet()

            prefs.edit()
                .putStringSet(
                    obligationMonthKey,
                    records
                )
                .apply()
        }

        // PC_DRONE_MONTHLY_OBLIGATIONS_PAYMENT_STORAGE
        val obligationPaymentKey =
            "money_manager_obligation_payments"

        fun loadObligationPayments(): MutableList<List<String>> {
            return prefs
                .getStringSet(
                    obligationPaymentKey,
                    emptySet()
                )
                .orEmpty()
                .mapNotNull { record ->
                    val parts =
                        record.split(obligationDelimiter)

                    if (parts.size == 5) parts
                    else null
                }
                .sortedBy { it[3].toLongOrNull() ?: 0L }
                .toMutableList()
        }

        fun saveObligationPayments(
            payments: List<List<String>>
        ) {
            val records =
                payments.map { parts ->
                    parts.joinToString(obligationDelimiter)
                }.toSet()

            prefs.edit()
                .putStringSet(
                    obligationPaymentKey,
                    records
                )
                .apply()
        }

        val now = java.util.Calendar.getInstance()
        // PC_DRONE_MONTHLY_OBLIGATIONS_CURRENT_SNAPSHOT
        val currentMonthKey =
            String.format(
                java.util.Locale.US,
                "%04d-%02d",
                now.get(java.util.Calendar.YEAR),
                now.get(java.util.Calendar.MONTH) + 1
            )

        val obligationMonths = loadObligationMonths()
        val existingMonthPairs =
            obligationMonths
                .map { it[1] to it[2] }
                .toMutableSet()

        var obligationMonthsChanged = false

        loadObligationTemplates()
            .filter { it[5] == "true" }
            .forEach { template ->
                val templateId = template[0]
                val pair = templateId to currentMonthKey

                if (pair !in existingMonthPairs) {
                    val createdAt =
                        System.currentTimeMillis().toString()

                    val monthItemId =
                        "obligation_month_" +
                        currentMonthKey +
                        "_" +
                        templateId

                    obligationMonths.add(
                        listOf(
                            monthItemId,
                            templateId,
                            currentMonthKey,
                            template[1],
                            template[2],
                            template[3],
                            createdAt
                        )
                    )

                    existingMonthPairs.add(pair)
                    obligationMonthsChanged = true
                }
            }

        if (obligationMonthsChanged) {
            saveObligationMonths(obligationMonths)
        }

        val monthNames = arrayOf(
            "มกราคม", "กุมภาพันธ์", "มีนาคม", "เมษายน",
            "พฤษภาคม", "มิถุนายน", "กรกฎาคม", "สิงหาคม",
            "กันยายน", "ตุลาคม", "พฤศจิกายน", "ธันวาคม"
        )
        val monthText =
            monthNames[now.get(java.util.Calendar.MONTH)] +
            " " +
            (now.get(java.util.Calendar.YEAR) + 543)

        val root = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(dp(16), dp(18), dp(16), dp(24))
            setBackgroundColor(pageBg)
        }

        root.addView(
            android.widget.Button(this).apply {
                text = "← กลับ"
                isAllCaps = false
                setOnClickListener {
                    showMoneyManager()
                }
            }
        )

        root.addView(
            android.widget.TextView(this).apply {
                text = "รายการที่ต้องจ่ายประจำเดือน"
                textSize = 24f
                setTextColor(dark)
                setTypeface(typeface, android.graphics.Typeface.BOLD)
                setPadding(0, dp(20), 0, dp(6))
            }
        )

        root.addView(
            android.widget.TextView(this).apply {
                text = monthText
                textSize = 18f
                setTextColor(green)
                setTypeface(typeface, android.graphics.Typeface.BOLD)
            }
        )

        root.addView(
            android.widget.TextView(this).apply {
                text = "ติดตามยอดที่ต้องจ่าย • จ่ายแล้ว • คงเหลือ • ประวัติการชำระ"
                textSize = 14f
                setTextColor(gray)
                setPadding(0, dp(8), 0, dp(20))
            }
        )

        // PC_DRONE_MONTHLY_OBLIGATIONS_HISTORY_BUTTON
        root.addView(
            android.widget.Button(this).apply {
                text = "ประวัติรายเดือน"
                isAllCaps = false
                textSize = 16f
                setOnClickListener {
                    showMonthlyObligationHistory()
                }
            },
            android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                dp(52)
            ).apply {
                bottomMargin = dp(12)
            }
        )

        // PC_DRONE_MONTHLY_OBLIGATIONS_ADD_BUTTON
        root.addView(
            android.widget.Button(this).apply {
                text = "+ เพิ่มรายการประจำ"
                isAllCaps = false
                textSize = 16f
                setTextColor(android.graphics.Color.WHITE)
                background =
                    android.graphics.drawable.GradientDrawable().apply {
                        setColor(android.graphics.Color.rgb(0, 121, 107))
                        cornerRadius = dp(14).toFloat()
                    }
                setOnClickListener {
                    // PC_DRONE_MONTHLY_OBLIGATIONS_ADD_FORM
                    val form = android.widget.LinearLayout(this@MainActivity).apply {
                        orientation = android.widget.LinearLayout.VERTICAL
                        setPadding(dp(20), dp(8), dp(20), 0)
                    }

                    val nameInput = android.widget.EditText(this@MainActivity).apply {
                        hint = "ชื่อรายการ เช่น ค่าผ่อนโดรน"
                        inputType = android.text.InputType.TYPE_CLASS_TEXT
                    }

                    val amountInput = android.widget.EditText(this@MainActivity).apply {
                        hint = "จำนวนเงิน เช่น 2000"
                        inputType =
                            android.text.InputType.TYPE_CLASS_NUMBER or
                            android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
                    }

                    val dueDayInput = android.widget.EditText(this@MainActivity).apply {
                        hint = "วันที่ต้องจ่าย 1-31"
                        inputType = android.text.InputType.TYPE_CLASS_NUMBER
                    }

                    val noteInput = android.widget.EditText(this@MainActivity).apply {
                        hint = "หมายเหตุ (ถ้ามี)"
                        inputType = android.text.InputType.TYPE_CLASS_TEXT
                    }

                    form.addView(nameInput)
                    form.addView(amountInput)
                    form.addView(dueDayInput)
                    form.addView(noteInput)

                    android.app.AlertDialog.Builder(this@MainActivity)
                        .setTitle("เพิ่มรายการประจำ")
                        .setView(form)
                        .setPositiveButton("บันทึก") { _, _ ->
                            val name =
                                nameInput.text.toString().replace(obligationDelimiter, " ").trim()

                            val amount =
                                amountInput.text.toString()
                                    .trim()
                                    .toBigDecimalOrNull()

                            val dueDay =
                                dueDayInput.text.toString()
                                    .trim()
                                    .toIntOrNull()

                            val note =
                                noteInput.text.toString().replace(obligationDelimiter, " ").trim()

                            when {
                                name.isBlank() -> {
                                    android.widget.Toast.makeText(
                                        this@MainActivity,
                                        "กรุณากรอกชื่อรายการ",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                }

                                amount == null ||
                                    amount.compareTo(java.math.BigDecimal.ZERO) <= 0 -> {
                                    android.widget.Toast.makeText(
                                        this@MainActivity,
                                        "จำนวนเงินต้องมากกว่า 0",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                }

                                dueDay == null ||
                                    dueDay !in 1..31 -> {
                                    android.widget.Toast.makeText(
                                        this@MainActivity,
                                        "วันที่ต้องจ่ายต้องอยู่ระหว่าง 1-31",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                }

                                else -> {
                                    // PC_DRONE_MONTHLY_OBLIGATIONS_TEMPLATE_SAVE
                                    val createdAt =
                                        System.currentTimeMillis()

                                    val templateId =
                                        "obligation_" +
                                        java.util.UUID.randomUUID().toString()

                                    val templates =
                                        loadObligationTemplates()

                                    templates.add(
                                        listOf(
                                            templateId,
                                            name,
                                            amount.stripTrailingZeros()
                                                .toPlainString(),
                                            dueDay.toString(),
                                            note,
                                            "true",
                                            createdAt.toString()
                                        )
                                    )

                                    saveObligationTemplates(templates)

                                    android.widget.Toast.makeText(
                                        this@MainActivity,
                                        "บันทึกรายการประจำแล้ว",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()

                                    showMonthlyObligations()
                                }
                            }
                        }
                        .setNegativeButton("ยกเลิก", null)
                        .show()
                }
            },
            android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                dp(52)
            ).apply {
                bottomMargin = dp(12)
            }
        )

        // PC_DRONE_MONTHLY_OBLIGATIONS_CURRENT_LIST
        val currentMonthItems =
            obligationMonths
                .filter { it[2] == currentMonthKey }
                .sortedBy { it[5].toIntOrNull() ?: 31 }

        if (currentMonthItems.isEmpty()) {
            root.addView(
                android.widget.TextView(this).apply {
                    text = "ยังไม่มีรายการที่ต้องจ่ายในเดือนนี้"
                    textSize = 16f
                    setTextColor(gray)
                    setPadding(dp(12), dp(20), dp(12), dp(20))
                    gravity = android.view.Gravity.CENTER
                }
            )
        } else {
            currentMonthItems.forEach { item ->
                val name = item[3]
                val planned =
                    item[4].toBigDecimalOrNull()
                        ?: java.math.BigDecimal.ZERO
                val dueDay =
                    item[5].toIntOrNull() ?: 0

                // PC_DRONE_MONTHLY_OBLIGATIONS_PAYMENT_TOTALS
                val monthItemId = item[0]
                val paid =
                    loadObligationPayments()
                        .filter { payment ->
                            payment[1] == monthItemId
                        }
                        .fold(java.math.BigDecimal.ZERO) { total, payment ->
                            total.add(
                                payment[2].toBigDecimalOrNull()
                                    ?: java.math.BigDecimal.ZERO
                            )
                        }

                val remainingRaw =
                    planned.subtract(paid)

                val remaining =
                    if (remainingRaw.compareTo(java.math.BigDecimal.ZERO) < 0)
                        java.math.BigDecimal.ZERO
                    else
                        remainingRaw

                // PC_DRONE_MONTHLY_OBLIGATIONS_PAYMENT_STATUS
                val monthParts =
                    currentMonthKey.split("-")

                val statusCalendar =
                    java.util.Calendar.getInstance().apply {
                        clear()
                        set(
                            monthParts[0].toInt(),
                            monthParts[1].toInt() - 1,
                            1,
                            23,
                            59,
                            59
                        )

                        val actualLastDay =
                            getActualMaximum(java.util.Calendar.DAY_OF_MONTH)

                        set(
                            java.util.Calendar.DAY_OF_MONTH,
                            kotlin.math.min(
                                dueDay.coerceAtLeast(1),
                                actualLastDay
                            )
                        )
                    }

                val dueTimeMillis =
                    statusCalendar.timeInMillis

                val nowMillis =
                    System.currentTimeMillis()

                val paymentStatus =
                    when {
                        remaining.compareTo(java.math.BigDecimal.ZERO) == 0 ->
                            "จ่ายแล้ว"

                        nowMillis > dueTimeMillis ->
                            "เกินกำหนด"

                        paid.compareTo(java.math.BigDecimal.ZERO) > 0 ->
                            "จ่ายบางส่วน"

                        else ->
                            "ยังไม่จ่าย"
                    }

                val card =
                    android.widget.LinearLayout(this).apply {
                        orientation = android.widget.LinearLayout.VERTICAL
                        setPadding(dp(16), dp(14), dp(16), dp(14))
                        background =
                            android.graphics.drawable.GradientDrawable().apply {
                                setColor(android.graphics.Color.WHITE)
                                cornerRadius = dp(14).toFloat()
                                setStroke(
                                    dp(1),
                                    android.graphics.Color.rgb(220, 226, 222)
                                )
                            }
                    }

                card.addView(
                    android.widget.TextView(this).apply {
                        text = name
                        textSize = 18f
                        setTextColor(dark)
                        setTypeface(
                            typeface,
                            android.graphics.Typeface.BOLD
                        )
                    }
                )

                card.addView(
                    android.widget.TextView(this).apply {
                        text =
                            "ยอดที่ต้องจ่าย " +
                            java.text.NumberFormat.getNumberInstance(java.util.Locale("th", "TH")).format(planned) +
                            " บาท"
                        textSize = 16f
                        setTextColor(dark)
                        setPadding(0, dp(8), 0, 0)
                    }
                )

                card.addView(
                    android.widget.TextView(this).apply {
                        text =
                            "จ่ายแล้ว " +
                            java.text.NumberFormat.getNumberInstance(java.util.Locale("th", "TH")).format(paid) +
                            " บาท\n" +
                            "คงเหลือ " +
                            java.text.NumberFormat.getNumberInstance(java.util.Locale("th", "TH")).format(remaining) +
                            " บาท"
                        textSize = 15f
                        setTextColor(gray)
                        setPadding(0, dp(6), 0, 0)
                    }
                )

                card.addView(
                    android.widget.TextView(this).apply {
                        text =
                            if (dueDay > 0)
                                "กำหนดจ่ายวันที่ $dueDay"
                            else
                                "ไม่พบวันที่กำหนดจ่าย"
                        textSize = 14f
                        setTextColor(green)
                        setPadding(0, dp(8), 0, 0)
                    }
                )

                
                // PC_DRONE_MONTHLY_OBLIGATIONS_PAYMENT_STATUS_UI
                card.addView(
                    android.widget.TextView(this).apply {
                        text = "สถานะ: " + paymentStatus
                        textSize = 15f
                        setTypeface(
                            typeface,
                            android.graphics.Typeface.BOLD
                        )
                        setTextColor(
                            when (paymentStatus) {
                                "จ่ายแล้ว" -> green
                                "เกินกำหนด" -> android.graphics.Color.rgb(180, 35, 35)
                                "จ่ายบางส่วน" -> dark
                                else -> gray
                            }
                        )
                        setPadding(0, dp(6), 0, dp(2))
                    }
                )

                // PC_DRONE_MONTHLY_OBLIGATIONS_PAYMENT_HISTORY
                val paymentHistory =
                    loadObligationPayments()
                        .filter { payment ->
                            payment[1] == monthItemId
                        }
                        .sortedByDescending { payment ->
                            payment[3].toLongOrNull() ?: 0L
                        }

                if (paymentHistory.isNotEmpty()) {
                    card.addView(
                        android.widget.TextView(this).apply {
                            text = "ประวัติการชำระ"
                            textSize = 15f
                            setTextColor(dark)
                            setTypeface(
                                typeface,
                                android.graphics.Typeface.BOLD
                            )
                            setPadding(0, dp(12), 0, dp(4))
                        }
                    )

                    paymentHistory.forEach { payment ->
                        val paymentAmount =
                            payment[2].toBigDecimalOrNull()
                                ?: java.math.BigDecimal.ZERO

                        val paidAt =
                            payment[3].toLongOrNull() ?: 0L

                        val paymentNote =
                            payment[4]

                        val paymentDateText =
                            if (paidAt > 0L) {
                                val calendar =
                                    java.util.Calendar.getInstance().apply {
                                        timeInMillis = paidAt
                                    }

                                String.format(
                                    java.util.Locale.US,
                                    "%02d/%02d/%04d %02d:%02d",
                                    calendar.get(java.util.Calendar.DAY_OF_MONTH),
                                    calendar.get(java.util.Calendar.MONTH) + 1,
                                    calendar.get(java.util.Calendar.YEAR) + 543,
                                    calendar.get(java.util.Calendar.HOUR_OF_DAY),
                                    calendar.get(java.util.Calendar.MINUTE)
                                )
                            } else {
                                "ไม่พบวันเวลา"
                            }

                        card.addView(
                            android.widget.TextView(this).apply {
                                val noteSuffix =
                                    if (paymentNote.isNotBlank()) {
                                        System.lineSeparator() +
                                            "หมายเหตุ: " +
                                            paymentNote
                                    } else {
                                        ""
                                    }

                                text =
                                    paymentDateText +
                                    " • " +
                                    java.text.NumberFormat.getNumberInstance(
                                        java.util.Locale("th", "TH")
                                    ).format(paymentAmount) +
                                    " บาท" +
                                    noteSuffix

                                textSize = 14f
                                setTextColor(gray)
                                setPadding(
                                    dp(8),
                                    dp(5),
                                    dp(4),
                                    dp(5)
                                )
                            }
                        )
                    }
                }

                // PC_DRONE_MONTHLY_OBLIGATIONS_PAYMENT_BUTTON
                if (remaining.compareTo(java.math.BigDecimal.ZERO) > 0) {
                    card.addView(
                        android.widget.Button(this).apply {
                            text = "บันทึกการชำระ"
                            isAllCaps = false
                            setOnClickListener {
                                val form =
                                    android.widget.LinearLayout(this@MainActivity).apply {
                                        orientation = android.widget.LinearLayout.VERTICAL
                                        setPadding(dp(20), dp(8), dp(20), 0)
                                    }

                                val amountInput =
                                    android.widget.EditText(this@MainActivity).apply {
                                        hint = "จำนวนเงินที่จ่าย"
                                        inputType =
                                            android.text.InputType.TYPE_CLASS_NUMBER or
                                            android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
                                    }

                                val noteInput =
                                    android.widget.EditText(this@MainActivity).apply {
                                        hint = "หมายเหตุ (ถ้ามี)"
                                        inputType =
                                            android.text.InputType.TYPE_CLASS_TEXT
                                    }

                                form.addView(amountInput)
                                form.addView(noteInput)

                                val dialog =
                                    android.app.AlertDialog.Builder(this@MainActivity)
                                        .setTitle("บันทึกการชำระ")
                                        .setView(form)
                                        .setPositiveButton("บันทึก", null)
                                        .setNegativeButton("ยกเลิก", null)
                                        .create()

                                dialog.setOnShowListener {
                                    dialog.getButton(
                                        android.app.AlertDialog.BUTTON_POSITIVE
                                    ).setOnClickListener {
                                        val amount =
                                            amountInput.text.toString()
                                                .trim()
                                                .toBigDecimalOrNull()

                                        val note =
                                            noteInput.text.toString()
                                                .replace(obligationDelimiter, " ")
                                                .trim()

                                        when {
                                            amount == null ||
                                                amount.compareTo(java.math.BigDecimal.ZERO) <= 0 -> {
                                                amountInput.error =
                                                    "จำนวนเงินต้องมากกว่า 0"
                                            }

                                            amount.compareTo(remaining) > 0 -> {
                                                amountInput.error =
                                                    "จำนวนเงินเกินยอดคงเหลือ"
                                            }

                                            else -> {
                                                // PC_DRONE_MONTHLY_OBLIGATIONS_PAYMENT_SAVE
                                                val payments =
                                                    loadObligationPayments()

                                                payments.add(
                                                    listOf(
                                                        "obligation_payment_" +
                                                            java.util.UUID.randomUUID().toString(),
                                                        monthItemId,
                                                        amount.stripTrailingZeros()
                                                            .toPlainString(),
                                                        System.currentTimeMillis().toString(),
                                                        note
                                                    )
                                                )

                                                saveObligationPayments(payments)
                                                dialog.dismiss()

                                                android.widget.Toast.makeText(
                                                    this@MainActivity,
                                                    "บันทึกการชำระแล้ว",
                                                    android.widget.Toast.LENGTH_SHORT
                                                ).show()

                                                showMonthlyObligations()
                                            }
                                        }
                                    }
                                }

                                dialog.show()
                            }
                        },
                        android.widget.LinearLayout.LayoutParams(
                            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                            dp(48)
                        ).apply {
                            topMargin = dp(10)
                        }
                    )
                }

                root.addView(
                    card,
                    android.widget.LinearLayout.LayoutParams(
                        android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                        android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        bottomMargin = dp(10)
                    }
                )
            }
        }

        val scroll = android.widget.ScrollView(this).apply {
            isFillViewport = true
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


    // PC_DRONE_MONTHLY_OBLIGATIONS_HISTORY_SCREEN
    private fun showMonthlyObligationHistory() {
        val root =
            android.widget.LinearLayout(this).apply {
                orientation = android.widget.LinearLayout.VERTICAL
                setPadding(dp(16), dp(16), dp(16), dp(16))
                setBackgroundColor(
                    android.graphics.Color.rgb(245, 248, 246)
                )
            }

        root.addView(
            android.widget.Button(this).apply {
                text = "← กลับ"
                isAllCaps = false
                setOnClickListener {
                    showMonthlyObligations()
                }
            }
        )

        root.addView(
            android.widget.TextView(this).apply {
                text = "ประวัติรายเดือน"
                textSize = 24f
                setTypeface(
                    typeface,
                    android.graphics.Typeface.BOLD
                )
                setTextColor(
                    android.graphics.Color.rgb(17, 17, 17)
                )
                setPadding(0, dp(18), 0, dp(8))
            }
        )

        root.addView(
            android.widget.TextView(this).apply {
                text = "สรุปรายการที่ต้องจ่ายย้อนหลัง แยกตามเดือน"
                textSize = 15f
                setTextColor(
                    android.graphics.Color.rgb(100, 105, 102)
                )
                setPadding(0, 0, 0, dp(16))
            }
        )

        val content =
            android.widget.LinearLayout(this).apply {
                orientation = android.widget.LinearLayout.VERTICAL
            }

        root.addView(
            android.widget.ScrollView(this).apply {
                addView(content)
            },
            android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        // PC_DRONE_MONTHLY_OBLIGATIONS_HISTORY_SUMMARY
        val historyMonths =
            getSharedPreferences(
                "pc_drone_v3_data",
                MODE_PRIVATE
            ).getStringSet(
                "money_manager_obligation_months",
                emptySet()
            ).orEmpty()
                .map { it.split("|||") }
                .filter { it.size == 7 }
                .toMutableList()
                .filter { it.size >= 7 }
                .groupBy { it[2] }
                .filterKeys { monthKey ->
                    val parts = monthKey.split("-")
                    val year = parts.getOrNull(0)?.toIntOrNull()
                    val month = parts.getOrNull(1)?.toIntOrNull()
                    parts.size == 2 &&
                        year != null &&
                        month != null &&
                        month in 1..12
                }
                .toList()
                .sortedByDescending { it.first }

        val historyPayments =
            getSharedPreferences(
                "pc_drone_v3_data",
                MODE_PRIVATE
            ).getStringSet(
                "money_manager_obligation_payments",
                emptySet()
            ).orEmpty()
                .map { it.split("|||") }
                .filter { it.size == 5 }
                .toMutableList()
                .filter { it.size >= 5 }

        if (historyMonths.isEmpty()) {
            content.addView(
                android.widget.TextView(this).apply {
                    text = "ยังไม่มีประวัติรายเดือน"
                    textSize = 16f
                    setTextColor(
                        android.graphics.Color.rgb(100, 105, 102)
                    )
                    setPadding(0, dp(20), 0, dp(20))
                }
            )
        } else {
            val thaiMonths =
                listOf(
                    "มกราคม", "กุมภาพันธ์", "มีนาคม",
                    "เมษายน", "พฤษภาคม", "มิถุนายน",
                    "กรกฎาคม", "สิงหาคม", "กันยายน",
                    "ตุลาคม", "พฤศจิกายน", "ธันวาคม"
                )

            val moneyFormat =
                java.text.NumberFormat.getNumberInstance(
                    java.util.Locale("th", "TH")
                )

            historyMonths.forEach { monthEntry ->
                val monthKey = monthEntry.first
                val monthItems = monthEntry.second

                val parts = monthKey.split("-")
                val year = parts[0].toInt()
                val month = parts[1].toInt()

                var plannedTotal = java.math.BigDecimal.ZERO
                var paidTotal = java.math.BigDecimal.ZERO
                var remainingTotal = java.math.BigDecimal.ZERO

                monthItems.forEach { item ->
                    val planned =
                        item[4].toBigDecimalOrNull()
                            ?: java.math.BigDecimal.ZERO

                    val itemId = item[0]

                    val paid =
                        historyPayments
                            .filter { payment ->
                                payment[1] == itemId
                            }
                            .fold(java.math.BigDecimal.ZERO) { total, payment ->
                                total.add(
                                    payment[2].toBigDecimalOrNull()
                                        ?: java.math.BigDecimal.ZERO
                                )
                            }

                    val rawRemaining =
                        planned.subtract(paid)

                    val remaining =
                        if (
                            rawRemaining.compareTo(
                                java.math.BigDecimal.ZERO
                            ) < 0
                        ) {
                            java.math.BigDecimal.ZERO
                        } else {
                            rawRemaining
                        }

                    plannedTotal = plannedTotal.add(planned)
                    paidTotal = paidTotal.add(paid)
                    remainingTotal = remainingTotal.add(remaining)
                }

                val card =
                    android.widget.LinearLayout(this).apply {
                        orientation = android.widget.LinearLayout.VERTICAL
                        setPadding(dp(16), dp(14), dp(16), dp(14))
                        background =
                            android.graphics.drawable.GradientDrawable().apply {
                                setColor(android.graphics.Color.WHITE)
                                cornerRadius = dp(14).toFloat()
                                setStroke(
                                    dp(1),
                                    android.graphics.Color.rgb(220, 226, 222)
                                )
                            }
                    }

                card.addView(
                    android.widget.TextView(this).apply {
                        text =
                            thaiMonths[month - 1] +
                            " " +
                            (year + 543)
                        textSize = 19f
                        setTypeface(
                            typeface,
                            android.graphics.Typeface.BOLD
                        )
                        setTextColor(
                            android.graphics.Color.rgb(17, 17, 17)
                        )
                    }
                )

                card.addView(
                    android.widget.TextView(this).apply {
                        text =
                            "ยอดที่ต้องจ่าย " +
                            moneyFormat.format(plannedTotal) +
                            " บาท" +
                            System.lineSeparator() +
                            "จ่ายแล้ว " +
                            moneyFormat.format(paidTotal) +
                            " บาท" +
                            System.lineSeparator() +
                            "คงเหลือ " +
                            moneyFormat.format(remainingTotal) +
                            " บาท" +
                            System.lineSeparator() +
                            "จำนวน " +
                            monthItems.size +
                            " รายการ"
                        textSize = 15f
                        setTextColor(
                            android.graphics.Color.rgb(70, 75, 72)
                        )
                        setPadding(0, dp(8), 0, 0)
                    }
                )

                // PC_DRONE_MONTHLY_OBLIGATIONS_HISTORY_MONTH_CLICK
                card.isClickable = true
                card.isFocusable = true
                card.setOnClickListener {
                    showMonthlyObligationHistoryDetail(monthKey)
                }

                content.addView(
                    card,
                    android.widget.LinearLayout.LayoutParams(
                        android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                        android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        bottomMargin = dp(10)
                    }
                )
            }
        }

        setContentView(root)
    }


    // PC_DRONE_MONTHLY_OBLIGATIONS_HISTORY_DETAIL_SCREEN
    private fun showMonthlyObligationHistoryDetail(monthKey: String) {
        val parts = monthKey.split("-")
        val year = parts.getOrNull(0)?.toIntOrNull()
        val month = parts.getOrNull(1)?.toIntOrNull()

        if (
            parts.size != 2 ||
            year == null ||
            month == null ||
            month !in 1..12
        ) {
            android.widget.Toast.makeText(
                this,
                "ข้อมูลเดือนไม่ถูกต้อง",
                android.widget.Toast.LENGTH_SHORT
            ).show()
            showMonthlyObligationHistory()
            return
        }

        val thaiMonths =
            listOf(
                "มกราคม", "กุมภาพันธ์", "มีนาคม",
                "เมษายน", "พฤษภาคม", "มิถุนายน",
                "กรกฎาคม", "สิงหาคม", "กันยายน",
                "ตุลาคม", "พฤศจิกายน", "ธันวาคม"
            )

        val root =
            android.widget.LinearLayout(this).apply {
                orientation = android.widget.LinearLayout.VERTICAL
                setPadding(dp(16), dp(16), dp(16), dp(16))
                setBackgroundColor(
                    android.graphics.Color.rgb(245, 248, 246)
                )
            }

        root.addView(
            android.widget.Button(this).apply {
                text = "← กลับ"
                isAllCaps = false
                setOnClickListener {
                    showMonthlyObligationHistory()
                }
            }
        )

        root.addView(
            android.widget.TextView(this).apply {
                text =
                    "รายละเอียด " +
                    thaiMonths[month - 1] +
                    " " +
                    (year + 543)
                textSize = 23f
                setTypeface(
                    typeface,
                    android.graphics.Typeface.BOLD
                )
                setTextColor(
                    android.graphics.Color.rgb(17, 17, 17)
                )
                setPadding(0, dp(18), 0, dp(8))
            }
        )

        root.addView(
            android.widget.TextView(this).apply {
                text = "รายการที่ต้องจ่ายและประวัติการชำระของเดือนนี้"
                textSize = 15f
                setTextColor(
                    android.graphics.Color.rgb(100, 105, 102)
                )
                setPadding(0, 0, 0, dp(16))
            }
        )

        val content =
            android.widget.LinearLayout(this).apply {
                orientation = android.widget.LinearLayout.VERTICAL
            }

        // PC_DRONE_MONTHLY_OBLIGATIONS_HISTORY_DETAIL_DATA
        val detailItems =
            getSharedPreferences(
                "pc_drone_v3_data",
                MODE_PRIVATE
            ).getStringSet(
                "money_manager_obligation_months",
                emptySet()
            ).orEmpty()
                .map { it.split("|||") }
                .filter { it.size == 7 && it[2] == monthKey }
                .sortedBy { it[5].toIntOrNull() ?: 31 }

        val detailPayments =
            getSharedPreferences(
                "pc_drone_v3_data",
                MODE_PRIVATE
            ).getStringSet(
                "money_manager_obligation_payments",
                emptySet()
            ).orEmpty()
                .map { it.split("|||") }
                .filter { it.size == 5 }

        // PC_DRONE_MONTHLY_OBLIGATIONS_HISTORY_DETAIL_CARDS
        if (detailItems.isEmpty()) {
            content.addView(
                android.widget.TextView(this).apply {
                    text = "ไม่พบรายการของเดือนนี้"
                    textSize = 16f
                    setTextColor(
                        android.graphics.Color.rgb(100, 105, 102)
                    )
                    setPadding(0, dp(20), 0, dp(20))
                }
            )
        } else {
            val moneyFormat =
                java.text.NumberFormat.getNumberInstance(
                    java.util.Locale("th", "TH")
                )

            detailItems.forEach { item ->
                val itemId = item[0]
                val name = item[3]
                val planned =
                    item[4].toBigDecimalOrNull()
                        ?: java.math.BigDecimal.ZERO
                val dueDay = item[5].toIntOrNull() ?: 1

                val paid =
                    detailPayments
                        .filter { it[1] == itemId }
                        .fold(java.math.BigDecimal.ZERO) { total, payment ->
                            total.add(
                                payment[2].toBigDecimalOrNull()
                                    ?: java.math.BigDecimal.ZERO
                            )
                        }

                val remaining =
                    planned.subtract(paid).max(
                        java.math.BigDecimal.ZERO
                    )

                // PC_DRONE_MONTHLY_OBLIGATIONS_HISTORY_DETAIL_OVERDUE
                val dueCalendar =
                    java.util.Calendar.getInstance().apply {
                        clear()
                        set(
                            year,
                            month - 1,
                            1,
                            23,
                            59,
                            59
                        )
                        val lastDay =
                            getActualMaximum(
                                java.util.Calendar.DAY_OF_MONTH
                            )
                        set(
                            java.util.Calendar.DAY_OF_MONTH,
                            dueDay.coerceIn(1, lastDay)
                        )
                        set(
                            java.util.Calendar.MILLISECOND,
                            999
                        )
                    }

                val isOverdue =
                    System.currentTimeMillis() >
                        dueCalendar.timeInMillis

                val status =
                    when {
                        remaining.compareTo(
                            java.math.BigDecimal.ZERO
                        ) == 0 -> "จ่ายแล้ว"

                        isOverdue -> "เกินกำหนด"

                        paid.compareTo(
                            java.math.BigDecimal.ZERO
                        ) > 0 -> "จ่ายบางส่วน"

                        else -> "ยังไม่จ่าย"
                    }

                val card =
                    android.widget.LinearLayout(this).apply {
                        orientation =
                            android.widget.LinearLayout.VERTICAL
                        setPadding(
                            dp(14), dp(14), dp(14), dp(14)
                        )
                        background =
                            android.graphics.drawable.GradientDrawable().apply {
                                setColor(
                                    android.graphics.Color.WHITE
                                )
                                cornerRadius = dp(14).toFloat()
                            }
                    }

                card.addView(
                    android.widget.TextView(this).apply {
                        text = name
                        textSize = 18f
                        setTypeface(
                            typeface,
                            android.graphics.Typeface.BOLD
                        )
                        setTextColor(
                            android.graphics.Color.rgb(20, 20, 20)
                        )
                    }
                )

                card.addView(
                    android.widget.TextView(this).apply {
                        text =
                            "ยอดที่ต้องจ่าย: " +
                            moneyFormat.format(planned) +
                            " บาท" +
                            System.lineSeparator() +
                            "จ่ายแล้ว: " +
                            moneyFormat.format(paid) +
                            " บาท" +
                            System.lineSeparator() +
                            "คงเหลือ: " +
                            moneyFormat.format(remaining) +
                            " บาท" +
                            System.lineSeparator() +
                            "ครบกำหนดวันที่: " +
                            dueDay +
                            System.lineSeparator() +
                            "สถานะ: " +
                            status
                        textSize = 15f
                        setTextColor(
                            android.graphics.Color.rgb(65, 70, 67)
                        )
                        setPadding(0, dp(8), 0, 0)
                    }
                )

                content.addView(
                    card,
                    android.widget.LinearLayout.LayoutParams(
                        android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                        android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        bottomMargin = dp(10)
                    }
                )
            }
        }

        root.addView(
            android.widget.ScrollView(this).apply {
                addView(content)
            },
            android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        setContentView(root)
    }


    // =====================================================
    // PHASE 2D - WALLET DETAIL / WALLET HISTORY
    // =====================================================
    private fun showWalletDetail(
        walletId: String,
        walletName: String,
        openingBalance: java.math.BigDecimal
    ) {

        val prefs =
            getSharedPreferences(
                "pc_drone_v3_data",
                MODE_PRIVATE
            )

        val greenDark =
            android.graphics.Color.rgb(
                0, 105, 50
            )

        val green =
            android.graphics.Color.rgb(
                46, 125, 50
            )

        val red =
            android.graphics.Color.rgb(
                198, 40, 40
            )

        val dark =
            android.graphics.Color.rgb(
                35, 35, 35
            )

        val gray =
            android.graphics.Color.rgb(
                100, 100, 100
            )

        val pageBg =
            android.graphics.Color.rgb(
                247, 249, 247
            )

        val moneyFormat =
            java.text.DecimalFormat(
                "#,##0.00"
            )

        fun rounded(
            color: Int,
            radiusDp: Int
        ): android.graphics.drawable.GradientDrawable {

            return android.graphics.drawable.GradientDrawable().apply {
                setColor(color)
                cornerRadius =
                    dp(radiusDp).toFloat()
            }
        }

        fun parseMoney(
            value: String
        ): java.math.BigDecimal {

            return try {
                java.math.BigDecimal(
                    value.trim()
                )
            } catch (_: Exception) {
                java.math.BigDecimal.ZERO
            }
        }

        data class WalletHistoryRow(
            val time: Long,
            val title: String,
            val detail: String,
            val amount: java.math.BigDecimal,
            val isIncome: Boolean
        )

        val history =
            mutableListOf<WalletHistoryRow>()

        var totalIncome =
            java.math.BigDecimal.ZERO

        var totalExpense =
            java.math.BigDecimal.ZERO

        // PC_DRONE_PHASE_2E_FIX1_NET_TRANSFER
        // แยกการโอนออกจากรายรับ/รายจ่ายจริงของธุรกิจ
        var transferIn =
            java.math.BigDecimal.ZERO

        var transferOut =
            java.math.BigDecimal.ZERO

        // ==========================================
        // MANUAL FINANCE LINKS
        // ==========================================

        val financeLinks =
            prefs.getStringSet(
                "money_manager_links",
                emptySet()
            )?.toList()
                ?: emptyList()

        val linkedTransactionIds =
            financeLinks.mapNotNull { link ->

                val p =
                    link.split(
                        "|||",
                        ignoreCase = false,
                        limit = 2
                    )

                val transactionId =
                    p.getOrNull(0)
                        ?.trim()
                        .orEmpty()

                val linkedWalletId =
                    p.getOrNull(1)
                        ?.trim()
                        .orEmpty()

                if (
                    transactionId.isNotBlank() &&
                    linkedWalletId == walletId
                ) {
                    transactionId
                } else {
                    null
                }

            }.toSet()

        val financeTransactions =
            prefs.getStringSet(
                "finance_transactions",
                emptySet()
            )?.toList()
                ?: emptyList()

        financeTransactions.forEach { record ->

            val p =
                record.split(
                    "|||",
                    ignoreCase = false,
                    limit = 5
                )

            val transactionId =
                p.getOrNull(0)
                    ?.trim()
                    .orEmpty()

            if (
                transactionId !in linkedTransactionIds
            ) {
                return@forEach
            }

            val time =
                transactionId.toLongOrNull()
                    ?: 0L

            val type =
                p.getOrNull(1)
                    ?.trim()
                    .orEmpty()

            val category =
                p.getOrNull(2)
                    ?.trim()
                    .orEmpty()

            val amount =
                parseMoney(
                    p.getOrNull(3)
                        ?: "0"
                )

            val note =
                p.getOrNull(4)
                    ?.trim()
                    .orEmpty()

            if (type == "INCOME") {

                totalIncome =
                    totalIncome.add(
                        amount
                    )

                history.add(
                    WalletHistoryRow(
                        time = time,
                        title =
                            if (category.isBlank()) {
                                "รายรับ"
                            } else {
                                category
                            },
                        detail = note,
                        amount = amount,
                        isIncome = true
                    )
                )

            } else if (
                type == "EXPENSE"
            ) {

                totalExpense =
                    totalExpense.add(
                        amount
                    )

                history.add(
                    WalletHistoryRow(
                        time = time,
                        title =
                            if (category.isBlank()) {
                                "รายจ่าย"
                            } else {
                                category
                            },
                        detail = note,
                        amount = amount,
                        isIncome = false
                    )
                )
            }
        }

        // ==========================================
        // FLIGHT JOB LINKS
        // ==========================================

        val jobLinks =
            prefs.getStringSet(
                "money_manager_job_links",
                emptySet()
            )?.toList()
                ?: emptyList()

        val linkedJobIds =
            jobLinks.mapNotNull { link ->

                val p =
                    link.split(
                        "|||",
                        ignoreCase = false,
                        limit = 2
                    )

                val jobId =
                    p.getOrNull(0)
                        ?.trim()
                        .orEmpty()

                val linkedWalletId =
                    p.getOrNull(1)
                        ?.trim()
                        .orEmpty()

                if (
                    jobId.isNotBlank() &&
                    linkedWalletId == walletId
                ) {
                    jobId
                } else {
                    null
                }

            }.toSet()

        val jobs =
            prefs.getStringSet(
                "flight_jobs",
                emptySet()
            )?.toList()
                ?: emptyList()

        jobs.forEach { record ->

            val p =
                record.split(
                    "|||",
                    ignoreCase = false,
                    limit = 9
                )

            val jobId =
                p.getOrNull(0)
                    ?.trim()
                    .orEmpty()

            if (
                jobId !in linkedJobIds
            ) {
                return@forEach
            }

            val status =
                p.getOrNull(7)
                    ?.trim()
                    .orEmpty()

            if (
                status != "เสร็จแล้ว"
            ) {
                return@forEach
            }

            val time =
                jobId.toLongOrNull()
                    ?: 0L

            val customer =
                p.getOrNull(1)
                    ?.trim()
                    .orEmpty()

            val service =
                p.getOrNull(2)
                    ?.trim()
                    .orEmpty()

            val amount =
                parseMoney(
                    p.getOrNull(6)
                        ?: "0"
                )

            totalIncome =
                totalIncome.add(
                    amount
                )

            val description =
                buildString {

                    append("งานบิน")

                    if (
                        customer.isNotBlank()
                    ) {
                        append(" - ")
                        append(customer)
                    }

                    if (
                        service.isNotBlank()
                    ) {
                        append(" • ")
                        append(service)
                    }
                }

            history.add(
                WalletHistoryRow(
                    time = time,
                    title = description,
                    detail = "",
                    amount = amount,
                    isIncome = true
                )
            )
        }

        // ==========================================
        // PHASE 2E - TRANSFER HISTORY
        // Transfer appears in wallet history but is NOT
        // business income/expense in Finance/Reports.
        // ==========================================

        val transfers =
            prefs.getStringSet(
                "money_manager_transfers",
                emptySet()
            )?.toList()
                ?: emptyList()

        val walletRecords =
            prefs.getStringSet(
                "money_manager_wallets",
                emptySet()
            )?.toList()
                ?: emptyList()

        fun walletNameById(
            id: String
        ): String {

            walletRecords.forEach { record ->

                val p =
                    record.split(
                        "|||",
                        ignoreCase = false,
                        limit = 5
                    )

                if (
                    p.getOrNull(0)
                        ?.trim() == id
                ) {

                    return p.getOrNull(1)
                        ?.trim()
                        .orEmpty()
                }
            }

            return "กระเป๋า"
        }

        transfers.forEach { record ->

            val p =
                record.split(
                    "|||",
                    ignoreCase = false,
                    limit = 5
                )

            val transferId =
                p.getOrNull(0)
                    ?.trim()
                    .orEmpty()

            val fromWalletId =
                p.getOrNull(1)
                    ?.trim()
                    .orEmpty()

            val toWalletId =
                p.getOrNull(2)
                    ?.trim()
                    .orEmpty()

            val amount =
                parseMoney(
                    p.getOrNull(3)
                        ?: "0"
                )

            val note =
                p.getOrNull(4)
                    ?.trim()
                    .orEmpty()

            val time =
                transferId.toLongOrNull()
                    ?: 0L

            if (
                toWalletId == walletId
            ) {

                transferIn =
                    transferIn.add(
                        amount
                    )

                history.add(
                    WalletHistoryRow(
                        time = time,
                        title =
                            "รับโอนจาก ${walletNameById(fromWalletId)}",
                        detail = note,
                        amount = amount,
                        isIncome = true
                    )
                )
            }

            if (
                fromWalletId == walletId
            ) {

                transferOut =
                    transferOut.add(
                        amount
                    )

                history.add(
                    WalletHistoryRow(
                        time = time,
                        title =
                            "โอนไป ${walletNameById(toWalletId)}",
                        detail = note,
                        amount = amount,
                        isIncome = false
                    )
                )
            }
        }

        history.sortByDescending {
            it.time
        }

        // การโอนต้องสรุปแบบสุทธิ ณ ตอนนั้น
        //
        // ตัวอย่าง:
        // ออก 200 + ออก 200 + รับกลับ 200
        // = โอนออกสุทธิ 200
        val netTransfer =
            transferIn.subtract(
                transferOut
            )

        val netTransferIn =
            if (
                netTransfer.compareTo(
                    java.math.BigDecimal.ZERO
                ) > 0
            ) {
                netTransfer
            } else {
                java.math.BigDecimal.ZERO
            }

        val netTransferOut =
            if (
                netTransfer.compareTo(
                    java.math.BigDecimal.ZERO
                ) < 0
            ) {
                netTransfer.abs()
            } else {
                java.math.BigDecimal.ZERO
            }

        val summarizedMoneyIn =
            totalIncome.add(
                netTransferIn
            )

        val summarizedMoneyOut =
            totalExpense.add(
                netTransferOut
            )

        val currentBalance =
            openingBalance
                .add(totalIncome)
                .subtract(totalExpense)
                .add(transferIn)
                .subtract(transferOut)

        // ==========================================
        // UI
        // ==========================================

        val root =
            android.widget.LinearLayout(this).apply {

                orientation =
                    android.widget.LinearLayout.VERTICAL

                setPadding(
                    dp(16),
                    dp(18),
                    dp(16),
                    dp(32)
                )

                setBackgroundColor(
                    pageBg
                )
            }

        root.addView(
            android.widget.TextView(this).apply {

                text = "‹  กลับ"
                textSize = 17f
                setTextColor(
                    greenDark
                )

                setTypeface(
                    typeface,
                    android.graphics.Typeface.BOLD
                )

                setPadding(
                    dp(4),
                    dp(8),
                    dp(8),
                    dp(12)
                )

                isClickable = true

                setOnClickListener {
                    showMoneyManager()
                }
            }
        )

        root.addView(
            android.widget.TextView(this).apply {

                text = walletName
                textSize = 27f
                setTextColor(dark)

                setTypeface(
                    typeface,
                    android.graphics.Typeface.BOLD
                )
            }
        )

        root.addView(
            android.widget.TextView(this).apply {

                text =
                    "รายละเอียดและประวัติกระเป๋า"

                textSize = 14f
                setTextColor(gray)

                setPadding(
                    0,
                    dp(3),
                    0,
                    dp(15)
                )
            }
        )

        val balanceBox =
            android.widget.LinearLayout(this).apply {

                orientation =
                    android.widget.LinearLayout.VERTICAL

                setPadding(
                    dp(18),
                    dp(16),
                    dp(18),
                    dp(16)
                )

                background =
                    rounded(
                        if (
                            currentBalance.compareTo(
                                java.math.BigDecimal.ZERO
                            ) >= 0
                        ) {
                            greenDark
                        } else {
                            red
                        },
                        16
                    )
            }

        balanceBox.addView(
            android.widget.TextView(this).apply {

                text =
                    "ยอดคงเหลือปัจจุบัน"

                textSize = 14f

                setTextColor(
                    android.graphics.Color.WHITE
                )
            }
        )

        balanceBox.addView(
            android.widget.TextView(this).apply {

                text =
                    "${moneyFormat.format(currentBalance)} บาท"

                textSize = 27f

                setTextColor(
                    android.graphics.Color.WHITE
                )

                setTypeface(
                    typeface,
                    android.graphics.Typeface.BOLD
                )
            }
        )

        root.addView(balanceBox)

        val summary =
            android.widget.LinearLayout(this).apply {

                orientation =
                    android.widget.LinearLayout.HORIZONTAL

                setPadding(
                    0,
                    dp(12),
                    0,
                    dp(14)
                )
            }

        fun summaryBox(
            title: String,
            amount: java.math.BigDecimal,
            textColor: Int
        ): android.widget.LinearLayout {

            return android.widget.LinearLayout(this).apply {

                orientation =
                    android.widget.LinearLayout.VERTICAL

                gravity =
                    android.view.Gravity.CENTER

                setPadding(
                    dp(8),
                    dp(12),
                    dp(8),
                    dp(12)
                )

                background =
                    rounded(
                        android.graphics.Color.WHITE,
                        14
                    )

                addView(
                    android.widget.TextView(
                        this@MainActivity
                    ).apply {

                        text = title
                        textSize = 12f
                        setTextColor(gray)
                    }
                )

                addView(
                    android.widget.TextView(
                        this@MainActivity
                    ).apply {

                        text =
                            "${moneyFormat.format(amount)} บาท"

                        textSize = 17f
                        setTextColor(
                            textColor
                        )

                        setTypeface(
                            typeface,
                            android.graphics.Typeface.BOLD
                        )
                    }
                )
            }
        }

        summary.addView(
            summaryBox(
                "เงินเข้าสุทธิ",
                summarizedMoneyIn,
                green
            ),
            android.widget.LinearLayout.LayoutParams(
                0,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                rightMargin = dp(5)
            }
        )

        summary.addView(
            summaryBox(
                "เงินออกสุทธิ",
                summarizedMoneyOut,
                red
            ),
            android.widget.LinearLayout.LayoutParams(
                0,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                leftMargin = dp(5)
            }
        )

        root.addView(summary)

        root.addView(
            android.widget.TextView(this).apply {

                text = "ประวัติรายการ"
                textSize = 19f
                setTextColor(dark)

                setTypeface(
                    typeface,
                    android.graphics.Typeface.BOLD
                )

                setPadding(
                    dp(2),
                    dp(5),
                    0,
                    dp(10)
                )
            }
        )

        if (
            history.isEmpty()
        ) {

            root.addView(
                android.widget.TextView(this).apply {

                    text =
                        "ยังไม่มีรายการรับ-จ่ายในกระเป๋านี้"

                    textSize = 14f

                    gravity =
                        android.view.Gravity.CENTER

                    setTextColor(gray)

                    setPadding(
                        dp(10),
                        dp(24),
                        dp(10),
                        dp(24)
                    )
                }
            )

        } else {

            val dateFormat =
                java.text.SimpleDateFormat(
                    "dd/MM/yyyy HH:mm",
                    java.util.Locale.getDefault()
                )

            history.forEach { row ->

                val card =
                    android.widget.LinearLayout(this).apply {

                        orientation =
                            android.widget.LinearLayout.HORIZONTAL

                        gravity =
                            android.view.Gravity.CENTER_VERTICAL

                        setPadding(
                            dp(14),
                            dp(12),
                            dp(14),
                            dp(12)
                        )

                        background =
                            rounded(
                                android.graphics.Color.WHITE,
                                14
                            )
                    }

                val left =
                    android.widget.LinearLayout(this).apply {

                        orientation =
                            android.widget.LinearLayout.VERTICAL
                    }

                left.addView(
                    android.widget.TextView(this).apply {

                        text = row.title
                        textSize = 15f
                        setTextColor(dark)

                        setTypeface(
                            typeface,
                            android.graphics.Typeface.BOLD
                        )
                    }
                )

                if (
                    row.detail.isNotBlank()
                ) {

                    left.addView(
                        android.widget.TextView(this).apply {

                            text = row.detail
                            textSize = 12f
                            setTextColor(gray)
                        }
                    )
                }

                left.addView(
                    android.widget.TextView(this).apply {

                        text =
                            if (
                                row.time > 0L
                            ) {
                                dateFormat.format(
                                    java.util.Date(
                                        row.time
                                    )
                                )
                            } else {
                                "-"
                            }

                        textSize = 11f
                        setTextColor(gray)
                    }
                )

                card.addView(
                    left,
                    android.widget.LinearLayout.LayoutParams(
                        0,
                        android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                )

                card.addView(
                    android.widget.TextView(this).apply {

                        text =
                            (
                                if (
                                    row.isIncome
                                ) {
                                    "+"
                                } else {
                                    "-"
                                }
                            ) +
                            moneyFormat.format(
                                row.amount
                            )

                        textSize = 16f

                        setTextColor(
                            if (
                                row.isIncome
                            ) {
                                green
                            } else {
                                red
                            }
                        )

                        setTypeface(
                            typeface,
                            android.graphics.Typeface.BOLD
                        )
                    }
                )

                root.addView(
                    card,
                    android.widget.LinearLayout.LayoutParams(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                    ).apply {
                        bottomMargin =
                            dp(8)
                    }
                )
            }
        }

        val scroll =
            android.widget.ScrollView(this).apply {

                isFillViewport = true

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


    private fun showReports() {

        // PC_DRONE_REPORT_STEP3B

        val root = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(dp(20), dp(20), dp(20), dp(36))
            setBackgroundColor(android.graphics.Color.WHITE)
        }

        root.addView(createBackButton())

        root.addView(
            android.widget.TextView(this).apply {
                text = "รายงาน"
                textSize = 30f
                setTextColor(
                    android.graphics.Color.rgb(0, 105, 50)
                )
                setTypeface(
                    typeface,
                    android.graphics.Typeface.BOLD
                )
                setPadding(0, dp(18), 0, dp(2))
            }
        )

        root.addView(
            android.widget.TextView(this).apply {
                text = "ค้นหา ตรวจสอบ และสรุปรายรับ-รายจ่าย"
                textSize = 16f
                setTextColor(android.graphics.Color.DKGRAY)
                setPadding(0, 0, 0, dp(18))
            }
        )

        val tz =
            java.util.TimeZone.getTimeZone("Asia/Bangkok")

        fun sectionLabel(value: String) =
            android.widget.TextView(this).apply {
                text = value
                textSize = 18f
                setTextColor(android.graphics.Color.BLACK)
                setTypeface(
                    typeface,
                    android.graphics.Typeface.BOLD
                )
                setPadding(0, dp(10), 0, dp(6))
            }

        fun dateBox(
            hintValue: String,
            maxLength: Int
        ): android.widget.EditText {

            return android.widget.EditText(this).apply {

                hint = hintValue
                gravity = android.view.Gravity.CENTER
                textSize = 20f

                inputType =
                    android.text.InputType.TYPE_CLASS_NUMBER

                filters = arrayOf(
                    android.text.InputFilter.LengthFilter(maxLength)
                )

                setSelectAllOnFocus(true)
            }
        }

        fun dateRow():
            Pair<
                android.widget.LinearLayout,
                Triple<
                    android.widget.EditText,
                    android.widget.EditText,
                    android.widget.EditText
                >
            > {

            val row =
                android.widget.LinearLayout(this).apply {
                    orientation =
                        android.widget.LinearLayout.HORIZONTAL
                    gravity =
                        android.view.Gravity.CENTER_VERTICAL
                }

            val day = dateBox("00", 2)
            val month = dateBox("00", 2)
            val year = dateBox("0000", 4)

            val shortParams =
                android.widget.LinearLayout.LayoutParams(
                    0,
                    dp(58),
                    1f
                )

            val yearParams =
                android.widget.LinearLayout.LayoutParams(
                    0,
                    dp(58),
                    1.7f
                )

            fun slash() =
                android.widget.TextView(this).apply {
                    text = "/"
                    textSize = 24f
                    gravity = android.view.Gravity.CENTER
                }

            row.addView(day, shortParams)

            row.addView(
                slash(),
                android.widget.LinearLayout.LayoutParams(
                    dp(30),
                    dp(58)
                )
            )

            row.addView(month, shortParams)

            row.addView(
                slash(),
                android.widget.LinearLayout.LayoutParams(
                    dp(30),
                    dp(58)
                )
            )

            row.addView(year, yearParams)

            day.addTextChangedListener(
                object : android.text.TextWatcher {

                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {}

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                        if (s?.length == 2) {
                            month.requestFocus()
                        }
                    }

                    override fun afterTextChanged(
                        s: android.text.Editable?
                    ) {}
                }
            )

            month.addTextChangedListener(
                object : android.text.TextWatcher {

                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {}

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                        if (s?.length == 2) {
                            year.requestFocus()
                        }
                    }

                    override fun afterTextChanged(
                        s: android.text.Editable?
                    ) {}
                }
            )

            return Pair(
                row,
                Triple(day, month, year)
            )
        }

        root.addView(sectionLabel("จากวันที่"))

        val from = dateRow()
        root.addView(from.first)

        root.addView(sectionLabel("ถึงวันที่"))

        val to = dateRow()
        root.addView(to.first)

        val now =
            java.util.Calendar.getInstance(tz)

        val todayDay =
            now.get(java.util.Calendar.DAY_OF_MONTH)

        val todayMonth =
            now.get(java.util.Calendar.MONTH) + 1

        val todayYear =
            now.get(java.util.Calendar.YEAR) + 543

        fun setToday(
            fields: Triple<
                android.widget.EditText,
                android.widget.EditText,
                android.widget.EditText
            >
        ) {

            fields.first.setText(
                "%02d".format(todayDay)
            )

            fields.second.setText(
                "%02d".format(todayMonth)
            )

            fields.third.setText(
                todayYear.toString()
            )
        }

        setToday(from.second)
        setToday(to.second)

        fun parseDate(
            fields: Triple<
                android.widget.EditText,
                android.widget.EditText,
                android.widget.EditText
            >,
            endOfDay: Boolean
        ): Long? {

            val day =
                fields.first.text.toString()
                    .toIntOrNull()
                    ?: return null

            val month =
                fields.second.text.toString()
                    .toIntOrNull()
                    ?: return null

            var year =
                fields.third.text.toString()
                    .toIntOrNull()
                    ?: return null

            // รองรับ พ.ศ. และ ค.ศ.
            if (year >= 2400) {
                year -= 543
            }

            if (
                day !in 1..31 ||
                month !in 1..12 ||
                year !in 1900..2200
            ) {
                return null
            }

            val cal =
                java.util.Calendar.getInstance(tz).apply {

                    isLenient = false
                    clear()

                    set(
                        year,
                        month - 1,
                        day,
                        if (endOfDay) 23 else 0,
                        if (endOfDay) 59 else 0,
                        if (endOfDay) 59 else 0
                    )

                    set(
                        java.util.Calendar.MILLISECOND,
                        if (endOfDay) 999 else 0
                    )
                }

            return try {
                cal.timeInMillis
            } catch (_: Exception) {
                null
            }
        }

        fun thaiDate(ms: Long): String {

            val c =
                java.util.Calendar.getInstance(tz)

            c.timeInMillis = ms

            return "%02d/%02d/%04d".format(
                c.get(
                    java.util.Calendar.DAY_OF_MONTH
                ),
                c.get(
                    java.util.Calendar.MONTH
                ) + 1,
                c.get(
                    java.util.Calendar.YEAR
                ) + 543
            )
        }

        data class ReportTx(
            val time: Long,
            val type: String,
            val item: String,
            val amount: Double,
            val note: String
        )

        fun parseTx(raw: String): ReportTx? {

            val parts =
                when {
                    raw.contains("|||") ->
                        raw.split("|||", limit = 5)

                    raw.contains("\t") ->
                        raw.split("\t")

                    raw.contains("|") ->
                        raw.split("|")

                    else ->
                        return null
                }

            val time =
                parts.getOrNull(0)
                    ?.trim()
                    ?.toLongOrNull()
                    ?: return null

            val type =
                parts.getOrNull(1)
                    ?.trim()
                    ?.uppercase()
                    ?: ""

            val item =
                parts.getOrNull(2)
                    ?.trim()
                    .orEmpty()

            val amount =
                parts.getOrNull(3)
                    ?.trim()
                    ?.replace(",", "")
                    ?.toDoubleOrNull()
                    ?: 0.0

            val note =
                parts.getOrNull(4)
                    ?.trim()
                    .orEmpty()

            return ReportTx(
                time,
                type,
                item,
                amount,
                note
            )
        }

        fun isIncome(type: String): Boolean {

            val t = type.trim().uppercase()

            return (
                t == "INCOME" ||
                t == "RECEIVE" ||
                t == "REVENUE" ||
                type.contains("รายรับ") ||
                type.contains("รับเงิน")
            )
        }

        val summaryText =
            android.widget.TextView(this).apply {

                text =
                    "กรอกวันที่แล้วกดค้นหาและประมวลผลรายงาน"

                textSize = 16f
                setTextColor(android.graphics.Color.DKGRAY)

                setPadding(
                    0,
                    dp(18),
                    0,
                    dp(12)
                )
            }

        root.addView(summaryText)

        val table =
            android.widget.TableLayout(this).apply {

                isStretchAllColumns = true

                setPadding(
                    dp(4),
                    dp(4),
                    dp(4),
                    dp(8)
                )
            }

        val horizontal =
            android.widget.HorizontalScrollView(this).apply {

                isFillViewport = true

                addView(
                    table,
                    android.widget.FrameLayout.LayoutParams(
                        android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                        android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                )
            }

        fun cell(
            value: String,
            bold: Boolean = false,
            gravityValue: Int =
                android.view.Gravity.START
        ): android.widget.TextView {

            return android.widget.TextView(this).apply {

                text = value
                textSize = 14f
                gravity = gravityValue

                setTextColor(android.graphics.Color.BLACK)

                setPadding(
                    dp(10),
                    dp(10),
                    dp(10),
                    dp(10)
                )

                if (bold) {
                    setTypeface(
                        typeface,
                        android.graphics.Typeface.BOLD
                    )
                }

                minWidth = dp(110)
            }
        }

        fun addHeader() {

            val row =
                android.widget.TableRow(this)

            row.addView(cell("วันที่", true))
            row.addView(cell("รายการ", true))
            row.addView(
                cell(
                    "รายรับ",
                    true,
                    android.view.Gravity.END
                )
            )
            row.addView(
                cell(
                    "รายจ่าย",
                    true,
                    android.view.Gravity.END
                )
            )
            row.addView(
                cell(
                    "คงเหลือ",
                    true,
                    android.view.Gravity.END
                )
            )

            table.addView(row)
        }

        fun money(v: Double): String =
            java.text.NumberFormat
                .getNumberInstance(
                    java.util.Locale("th", "TH")
                )
                .apply {
                    minimumFractionDigits = 2
                    maximumFractionDigits = 2
                }
                .format(v)

        /*
         * ข้อมูลรายงานล่าสุดสำหรับสร้าง PDF
         * เก็บเฉพาะผลที่ผ่านการคำนวณจากหน้ารายงานแล้ว
         */
        var pdfFromMillis: Long? = null
        var pdfToMillis: Long? = null
        var pdfRows: List<Array<String>> = emptyList()
        var pdfTotalIncome = 0.0
        var pdfTotalExpense = 0.0
        var pdfEndingBalance = 0.0

        val searchButton =
            android.widget.Button(this).apply {

                text = "ค้นหาและประมวลผลรายงาน"
                textSize = 18f
                isAllCaps = false

                setOnClickListener {

                    val fromMillis =
                        parseDate(
                            from.second,
                            false
                        )

                    val toMillis =
                        parseDate(
                            to.second,
                            true
                        )

                    if (
                        fromMillis == null ||
                        toMillis == null
                    ) {

                        android.widget.Toast.makeText(
                            this@MainActivity,
                            "กรุณากรอกวันที่ให้ถูกต้อง",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()

                        return@setOnClickListener
                    }

                    if (fromMillis > toMillis) {

                        android.widget.Toast.makeText(
                            this@MainActivity,
                            "วันที่เริ่มต้นต้องไม่เกินวันที่สิ้นสุด",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()

                        return@setOnClickListener
                    }

                    /*
                     * REPORT FINANCE SOURCE
                     * ฐานข้อมูลจริงของ PC Drone V3 Plus
                     */
                    val financePrefs =
                        getSharedPreferences(
                            "pc_drone_v3_data",
                            MODE_PRIVATE
                        )


                    // FINANCE_SCHEMA:
                    // 0=timestamp
                    // 1=typeCode
                    // 2=category
                    // 3=amount
                    // 4=note
                    val rawSet: Set<String> =
                        financePrefs.getStringSet(
                            "finance_transactions",
                            emptySet()
                        )?.toSet()
                            ?: emptySet()

                    val all =
                        rawSet.mapNotNull {
                            parseTx(it)
                        }.sortedBy {
                            it.time
                        }

                    /*
                     * รวมข้อมูลการเงิน + รายได้จากงานบิน
                     *
                     * flight_jobs schema:
                     * 0 timestamp
                     * 1 customer
                     * 2 service
                     * 3 location
                     * 4 rai
                     * 5 rate
                     * 6 total
                     * 7 status
                     * 8 note
                     */

                    data class ReportRow(
                        val time: Long,
                        val item: String,
                        val income: Double,
                        val expense: Double
                    )

                    val financeRows =
                        all.map { tx ->

                            val income =
                                if (isIncome(tx.type)) {
                                    tx.amount
                                } else {
                                    0.0
                                }

                            val expense =
                                if (isIncome(tx.type)) {
                                    0.0
                                } else {
                                    tx.amount
                                }

                            val itemText =
                                if (tx.note.isNotBlank()) {
                                    tx.item + " - " + tx.note
                                } else {
                                    tx.item
                                }

                            ReportRow(
                                time = tx.time,
                                item = itemText.ifBlank { "-" },
                                income = income,
                                expense = expense
                            )
                        }

                    val rawJobs =
                        financePrefs.getStringSet(
                            "flight_jobs",
                            emptySet()
                        )?.toSet()
                            ?: emptySet()

                    val flightRows =
                        rawJobs.mapNotNull { record ->

                            val parts =
                                record.split(
                                    "|||",
                                    ignoreCase = false,
                                    limit = 9
                                )

                            if (parts.size < 8) {
                                null
                            } else {

                                val time =
                                    parts.getOrNull(0)
                                        ?.toLongOrNull()

                                val customer =
                                    parts.getOrNull(1)
                                        ?.trim()
                                        .orEmpty()

                                val service =
                                    parts.getOrNull(2)
                                        ?.trim()
                                        .orEmpty()

                                val total =
                                    parts.getOrNull(6)
                                        ?.trim()
                                        ?.toDoubleOrNull()

                                val status =
                                    parts.getOrNull(7)
                                        ?.trim()
                                        .orEmpty()

                                if (
                                    time == null ||
                                    total == null ||
                                    total < 0.0 ||
                                    status != "เสร็จแล้ว"
                                ) {
                                    null
                                } else {

                                    val description =
                                        buildString {

                                            append("งานบิน")

                                            if (customer.isNotBlank()) {
                                                append(" - ")
                                                append(customer)
                                            }

                                            if (service.isNotBlank()) {
                                                append(" - ")
                                                append(service)
                                            }
                                        }

                                    ReportRow(
                                        time = time,
                                        item = description,
                                        income = total,
                                        expense = 0.0
                                    )
                                }
                            }
                        }

                    /*
                     * รวมทุกการเคลื่อนไหวทางการเงิน
                     * แล้วเรียงตามเวลา
                     */
                    val reportRows =
                        (financeRows + flightRows)
                            .sortedBy { it.time }

                    /*
                     * ยอดยกมาก่อนวันเริ่มรายงาน
                     */
                    var runningBalance = 0.0

                    reportRows
                        .filter {
                            it.time < fromMillis
                        }
                        .forEach { row ->

                            runningBalance +=
                                row.income - row.expense
                        }

                    /*
                     * รายการที่อยู่ในช่วงวันที่เลือก
                     */
                    val selected =
                        reportRows.filter {
                            it.time in fromMillis..toMillis
                        }

                    table.removeAllViews()
                    addHeader()

                    var totalIncome = 0.0
                    var totalExpense = 0.0

                    selected.forEach { reportRow ->

                        val income =
                            reportRow.income

                        val expense =
                            reportRow.expense

                        totalIncome += income
                        totalExpense += expense

                        runningBalance +=
                            income - expense

                        val row =
                            android.widget.TableRow(
                                this@MainActivity
                            )

                        row.addView(
                            cell(
                                thaiDate(
                                    reportRow.time
                                )
                            )
                        )

                        row.addView(
                            cell(
                                reportRow.item
                                    .ifBlank { "-" }
                            )
                        )

                        row.addView(
                            cell(
                                if (income > 0.0) {
                                    money(income)
                                } else {
                                    "-"
                                },
                                false,
                                android.view.Gravity.END
                            )
                        )

                        row.addView(
                            cell(
                                if (expense > 0.0) {
                                    money(expense)
                                } else {
                                    "-"
                                },
                                false,
                                android.view.Gravity.END
                            )
                        )

                        row.addView(
                            cell(
                                money(runningBalance),
                                false,
                                android.view.Gravity.END
                            )
                        )

                        table.addView(row)
                    }

                    val totalRow =
                        android.widget.TableRow(
                            this@MainActivity
                        )

                    totalRow.addView(
                        cell("", true)
                    )

                    totalRow.addView(
                        cell("รวม", true)
                    )

                    totalRow.addView(
                        cell(
                            money(totalIncome),
                            true,
                            android.view.Gravity.END
                        )
                    )

                    totalRow.addView(
                        cell(
                            money(totalExpense),
                            true,
                            android.view.Gravity.END
                        )
                    )

                    totalRow.addView(
                        cell(
                            money(runningBalance),
                            true,
                            android.view.Gravity.END
                        )
                    )

                    /*
                     * ส่งผลรายงานชุดเดียวกับหน้าจอไปให้ PDF
                     */
                    var pdfRunningBalance = 0.0

                    reportRows
                        .filter { it.time < fromMillis }
                        .forEach { row ->
                            pdfRunningBalance +=
                                row.income - row.expense
                        }

                    val preparedPdfRows =
                        mutableListOf<Array<String>>()

                    selected.forEachIndexed { index, reportRow ->

                        pdfRunningBalance +=
                            reportRow.income -
                                reportRow.expense

                        preparedPdfRows.add(
                            arrayOf(
                                (index + 1).toString(),
                                thaiDate(reportRow.time) + " " +
                                    java.text.SimpleDateFormat(
                                        "HH:mm",
                                        java.util.Locale("th", "TH")
                                    ).apply {
                                        timeZone =
                                            java.util.TimeZone.getTimeZone(
                                                "Asia/Bangkok"
                                            )
                                    }.format(
                                        java.util.Date(reportRow.time)
                                    ),
                                reportRow.item.ifBlank { "-" },
                                if (reportRow.income > 0.0) {
                                    money(reportRow.income)
                                } else {
                                    "-"
                                },
                                if (reportRow.expense > 0.0) {
                                    money(reportRow.expense)
                                } else {
                                    "-"
                                },
                                money(pdfRunningBalance)
                            )
                        )
                    }

                    pdfFromMillis = fromMillis
                    pdfToMillis = toMillis
                    pdfRows = preparedPdfRows.toList()
                    pdfTotalIncome = totalIncome
                    pdfTotalExpense = totalExpense
                    pdfEndingBalance = runningBalance

                    table.addView(totalRow)

                    summaryText.text =
                        "รายงาน " +
                        thaiDate(fromMillis) +
                        " ถึง " +
                        thaiDate(toMillis) +
                        "\n" +
                        "จำนวนรายการ: " +
                        selected.size +
                        "\n" +
                        "รายรับ: " +
                        money(totalIncome) +
                        " บาท" +
                        "\n" +
                        "รายจ่าย: " +
                        money(totalExpense) +
                        " บาท" +
                        "\n" +
                        "คงเหลือ: " +
                        money(runningBalance) +
                        " บาท"

                    if (selected.isEmpty()) {

                        android.widget.Toast.makeText(
                            this@MainActivity,
                            "ไม่พบรายการในช่วงวันที่ที่เลือก",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

        root.addView(
            searchButton,
            android.widget.LinearLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                dp(62)
            ).apply {
                topMargin = dp(14)
            }
        )

        val pdfButton =
            android.widget.Button(this).apply {

                text = "สร้างและแชร์ PDF"
                textSize = 18f
                isAllCaps = false

                setOnClickListener {

                    val fromMillis = pdfFromMillis
                    val toMillis = pdfToMillis

                    if (
                        fromMillis == null ||
                        toMillis == null
                    ) {
                        android.widget.Toast.makeText(
                            this@MainActivity,
                            "กรุณากดค้นหาและประมวลผลรายงานก่อน",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()

                        return@setOnClickListener
                    }

                    try {
                        val reportDir =
                            java.io.File(
                                cacheDir,
                                "finance_reports"
                            ).apply {
                                mkdirs()
                            }

                        val pdfFile =
                            java.io.File(
                                reportDir,
                                "PC-Drone-Finance-Report.pdf"
                            )

                        val document =
                            android.graphics.pdf.PdfDocument()

                        val pageWidth = 595
                        val pageHeight = 842

                        val rowsPerPage = 17

                        val pageCount =
                            maxOf(
                                1,
                                (pdfRows.size + rowsPerPage - 1) /
                                    rowsPerPage
                            )

                        val now =
                            java.util.Date()

                        val printDate =
                            java.text.SimpleDateFormat(
                                "dd/MM/yyyy",
                                java.util.Locale("th", "TH")
                            ).apply {
                                timeZone =
                                    java.util.TimeZone.getTimeZone(
                                        "Asia/Bangkok"
                                    )
                            }.format(now)

                        val printTime =
                            java.text.SimpleDateFormat(
                                "HH:mm",
                                java.util.Locale("th", "TH")
                            ).apply {
                                timeZone =
                                    java.util.TimeZone.getTimeZone(
                                        "Asia/Bangkok"
                                    )
                            }.format(now)

                        val paint =
                            android.graphics.Paint(
                                android.graphics.Paint.ANTI_ALIAS_FLAG
                            ).apply {
                                color =
                                    android.graphics.Color.BLACK
                            }

                        fun drawText(
                            canvas: android.graphics.Canvas,
                            text: String,
                            x: Float,
                            y: Float,
                            size: Float,
                            bold: Boolean = false,
                            align: android.graphics.Paint.Align =
                                android.graphics.Paint.Align.LEFT
                        ) {
                            paint.textSize = size
                            paint.textAlign = align
                            paint.style =
                                android.graphics.Paint.Style.FILL
                            paint.typeface =
                                if (bold) {
                                    android.graphics.Typeface.DEFAULT_BOLD
                                } else {
                                    android.graphics.Typeface.DEFAULT
                                }

                            canvas.drawText(
                                text,
                                x,
                                y,
                                paint
                            )
                        }

                        fun line(
                            canvas: android.graphics.Canvas,
                            x1: Float,
                            y1: Float,
                            x2: Float,
                            y2: Float
                        ) {
                            paint.style =
                                android.graphics.Paint.Style.STROKE
                            paint.strokeWidth = 0.8f

                            canvas.drawLine(
                                x1,
                                y1,
                                x2,
                                y2,
                                paint
                            )
                        }

                        for (pageNumber in 1..pageCount) {

                            val pageInfo =
                                android.graphics.pdf.PdfDocument.PageInfo
                                    .Builder(
                                        pageWidth,
                                        pageHeight,
                                        pageNumber
                                    )
                                    .create()

                            val page =
                                document.startPage(pageInfo)

                            val canvas =
                                page.canvas

                            drawText(
                                canvas,
                                "PC DRONE",
                                42f,
                                45f,
                                13f,
                                true
                            )

                            drawText(
                                canvas,
                                "วันที่พิมพ์รายงาน : $printDate",
                                553f,
                                35f,
                                9f,
                                false,
                                android.graphics.Paint.Align.RIGHT
                            )

                            drawText(
                                canvas,
                                "เวลา : $printTime น.",
                                553f,
                                49f,
                                9f,
                                false,
                                android.graphics.Paint.Align.RIGHT
                            )

                            drawText(
                                canvas,
                                "หน้า : $pageNumber / $pageCount",
                                553f,
                                63f,
                                9f,
                                false,
                                android.graphics.Paint.Align.RIGHT
                            )

                            drawText(
                                canvas,
                                "รายงานสรุปการเงิน",
                                297.5f,
                                92f,
                                17f,
                                true,
                                android.graphics.Paint.Align.CENTER
                            )

                            drawText(
                                canvas,
                                "ประจำวันที่ " +
                                    thaiDate(fromMillis) +
                                    " ถึง " +
                                    thaiDate(toMillis),
                                297.5f,
                                111f,
                                10f,
                                false,
                                android.graphics.Paint.Align.CENTER
                            )

                            val left = 35f
                            val top = 132f
                            val rowHeight = 28f

                            val xs =
                                floatArrayOf(
                                    35f,
                                    70f,
                                    165f,
                                    335f,
                                    405f,
                                    475f,
                                    560f
                                )

                            val headers =
                                arrayOf(
                                    "ลำดับ",
                                    "วันที่ / เวลา",
                                    "รายการ",
                                    "รายรับ",
                                    "รายจ่าย",
                                    "คงเหลือ"
                                )

                            val headerBottom =
                                top + rowHeight

                            for (x in xs) {
                                line(
                                    canvas,
                                    x,
                                    top,
                                    x,
                                    headerBottom
                                )
                            }

                            line(
                                canvas,
                                left,
                                top,
                                560f,
                                top
                            )

                            line(
                                canvas,
                                left,
                                headerBottom,
                                560f,
                                headerBottom
                            )

                            headers.forEachIndexed { i, title ->

                                drawText(
                                    canvas,
                                    title,
                                    (xs[i] + xs[i + 1]) / 2f,
                                    top + 18f,
                                    8.5f,
                                    true,
                                    android.graphics.Paint.Align.CENTER
                                )
                            }

                            val startIndex =
                                (pageNumber - 1) * rowsPerPage

                            val endIndex =
                                minOf(
                                    startIndex + rowsPerPage,
                                    pdfRows.size
                                )

                            var y =
                                headerBottom

                            for (i in startIndex until endIndex) {

                                val row =
                                    pdfRows[i]

                                val bottom =
                                    y + rowHeight

                                for (x in xs) {
                                    line(
                                        canvas,
                                        x,
                                        y,
                                        x,
                                        bottom
                                    )
                                }

                                line(
                                    canvas,
                                    left,
                                    bottom,
                                    560f,
                                    bottom
                                )

                                drawText(
                                    canvas,
                                    row[0],
                                    52.5f,
                                    y + 18f,
                                    8f,
                                    false,
                                    android.graphics.Paint.Align.CENTER
                                )

                                drawText(
                                    canvas,
                                    row[1],
                                    117.5f,
                                    y + 18f,
                                    7.5f,
                                    false,
                                    android.graphics.Paint.Align.CENTER
                                )

                                /*
                                 * รายการต้องแสดงครบทุกตัวอักษร
                                 * ห้ามตัดข้อความด้วย ...
                                 * ถ้ายาวให้ขึ้นบรรทัดใหม่ภายในช่องรายการ
                                 */
                                val itemText =
                                    row[2]

                                val itemPaint =
                                    android.text.TextPaint(
                                        android.graphics.Paint.ANTI_ALIAS_FLAG
                                    ).apply {
                                        color =
                                            android.graphics.Color.BLACK
                                        textSize = 8f
                                        typeface =
                                            android.graphics.Typeface.DEFAULT
                                    }

                                var itemLayout =
                                    android.text.StaticLayout.Builder
                                        .obtain(
                                            itemText,
                                            0,
                                            itemText.length,
                                            itemPaint,
                                            160
                                        )
                                        .setAlignment(
                                            android.text.Layout.Alignment
                                                .ALIGN_NORMAL
                                        )
                                        .setIncludePad(false)
                                        .setLineSpacing(
                                            0f,
                                            1f
                                        )
                                        .build()

                                /*
                                 * ถ้าข้อความยาวมากจนเกินความสูงแถว
                                 * ลดขนาดอักษรทีละน้อย แต่ไม่ตัดข้อความ
                                 */
                                var itemSize = 8f

                                while (
                                    itemLayout.height > 24 &&
                                    itemSize > 5f
                                ) {
                                    itemSize -= 0.5f
                                    itemPaint.textSize =
                                        itemSize

                                    itemLayout =
                                        android.text.StaticLayout.Builder
                                            .obtain(
                                                itemText,
                                                0,
                                                itemText.length,
                                                itemPaint,
                                                160
                                            )
                                            .setAlignment(
                                                android.text.Layout.Alignment
                                                    .ALIGN_NORMAL
                                            )
                                            .setIncludePad(false)
                                            .setLineSpacing(
                                                0f,
                                                1f
                                            )
                                            .build()
                                }

                                canvas.save()

                                canvas.translate(
                                    170f,
                                    y + (
                                        rowHeight -
                                            itemLayout.height
                                        ) / 2f
                                )

                                itemLayout.draw(
                                    canvas
                                )

                                canvas.restore()

                                drawText(
                                    canvas,
                                    row[3],
                                    400f,
                                    y + 18f,
                                    8f,
                                    false,
                                    android.graphics.Paint.Align.RIGHT
                                )

                                drawText(
                                    canvas,
                                    row[4],
                                    470f,
                                    y + 18f,
                                    8f,
                                    false,
                                    android.graphics.Paint.Align.RIGHT
                                )

                                drawText(
                                    canvas,
                                    row[5],
                                    555f,
                                    y + 18f,
                                    8f,
                                    false,
                                    android.graphics.Paint.Align.RIGHT
                                )

                                y = bottom
                            }

                            if (pageNumber == pageCount) {

                                val totalTop =
                                    y

                                val totalBottom =
                                    totalTop + 30f

                                line(
                                    canvas,
                                    35f,
                                    totalTop,
                                    560f,
                                    totalTop
                                )

                                line(
                                    canvas,
                                    35f,
                                    totalBottom,
                                    560f,
                                    totalBottom
                                )

                                for (x in xs) {
                                    line(
                                        canvas,
                                        x,
                                        totalTop,
                                        x,
                                        totalBottom
                                    )
                                }

                                drawText(
                                    canvas,
                                    "รวมทั้งหมด",
                                    250f,
                                    totalTop + 20f,
                                    9f,
                                    true,
                                    android.graphics.Paint.Align.CENTER
                                )

                                drawText(
                                    canvas,
                                    money(pdfTotalIncome),
                                    400f,
                                    totalTop + 20f,
                                    9f,
                                    true,
                                    android.graphics.Paint.Align.RIGHT
                                )

                                drawText(
                                    canvas,
                                    money(pdfTotalExpense),
                                    470f,
                                    totalTop + 20f,
                                    9f,
                                    true,
                                    android.graphics.Paint.Align.RIGHT
                                )

                                drawText(
                                    canvas,
                                    money(pdfEndingBalance),
                                    555f,
                                    totalTop + 20f,
                                    9f,
                                    true,
                                    android.graphics.Paint.Align.RIGHT
                                )

                                drawText(
                                    canvas,
                                    "ลงชื่อ ........................................",
                                    540f,
                                    710f,
                                    10f,
                                    false,
                                    android.graphics.Paint.Align.RIGHT
                                )

                                drawText(
                                    canvas,
                                    "( ........................................ )",
                                    540f,
                                    730f,
                                    10f,
                                    false,
                                    android.graphics.Paint.Align.RIGHT
                                )

                                drawText(
                                    canvas,
                                    "วันที่ ........................................",
                                    540f,
                                    750f,
                                    10f,
                                    false,
                                    android.graphics.Paint.Align.RIGHT
                                )
                            }

                            document.finishPage(page)
                        }

                        java.io.FileOutputStream(pdfFile).use {
                            document.writeTo(it)
                        }

                        document.close()

                        val uri =
                            android.net.Uri.Builder()
                                .scheme(
                                    android.content.ContentResolver
                                        .SCHEME_CONTENT
                                )
                                .authority(
                                    packageName +
                                        ".finance-reports"
                                )
                                .appendPath(
                                    pdfFile.name
                                )
                                .build()

                        val shareIntent =
                            android.content.Intent(
                                android.content.Intent.ACTION_SEND
                            ).apply {

                                type = "application/pdf"

                                putExtra(
                                    android.content.Intent.EXTRA_STREAM,
                                    uri
                                )

                                addFlags(
                                    android.content.Intent
                                        .FLAG_GRANT_READ_URI_PERMISSION
                                )
                            }

                        startActivity(
                            android.content.Intent.createChooser(
                                shareIntent,
                                "แชร์รายงาน PDF"
                            )
                        )

                    } catch (e: Exception) {

                        android.widget.Toast.makeText(
                            this@MainActivity,
                            "สร้าง PDF ไม่สำเร็จ: " +
                                (e.message ?: "ไม่ทราบสาเหตุ"),
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }

        root.addView(
            pdfButton,
            android.widget.LinearLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                dp(62)
            ).apply {
                topMargin = dp(10)
            }
        )


        root.addView(
            android.widget.TextView(this).apply {
                text = "ตารางรายงาน"
                textSize = 22f
                setTypeface(
                    typeface,
                    android.graphics.Typeface.BOLD
                )
                setTextColor(
                    android.graphics.Color.rgb(0, 105, 50)
                )
                setPadding(0, dp(22), 0, dp(8))
            }
        )

        root.addView(horizontal)

        root.addView(
            android.widget.TextView(this).apply {

                text =
                    "เขตเวลา: Asia/Bangkok • ICT (UTC+7)"

                textSize = 13f
                gravity = android.view.Gravity.CENTER
                setTextColor(android.graphics.Color.GRAY)
                setPadding(0, dp(20), 0, 0)
            }
        )

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


    private fun showSettings() {

        val prefs =
            getSharedPreferences(
                "pc_drone_settings",
                android.content.Context.MODE_PRIVATE
            )

        val dark =
            android.graphics.Color.rgb(17, 17, 17)

        val green =
            android.graphics.Color.rgb(0, 145, 70)

        val greenDark =
            android.graphics.Color.rgb(0, 91, 45)

        val pageBg =
            android.graphics.Color.rgb(247, 249, 248)

        val lineColor =
            android.graphics.Color.rgb(225, 229, 226)

        val gray =
            android.graphics.Color.rgb(100, 105, 102)

        fun rounded(
            color: Int,
            radius: Int,
            strokeColor: Int? = null
        ): android.graphics.drawable.GradientDrawable {

            return android.graphics.drawable.GradientDrawable().apply {

                shape =
                    android.graphics.drawable.GradientDrawable.RECTANGLE

                setColor(color)

                cornerRadius =
                    dp(radius).toFloat()

                if (strokeColor != null) {
                    setStroke(
                        dp(1),
                        strokeColor
                    )
                }
            }
        }

        window.statusBarColor =
            android.graphics.Color.WHITE

        if (
            android.os.Build.VERSION.SDK_INT >=
            android.os.Build.VERSION_CODES.M
        ) {
            window.decorView.systemUiVisibility =
                android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        val root =
            android.widget.LinearLayout(this).apply {

                orientation =
                    android.widget.LinearLayout.VERTICAL

                setBackgroundColor(pageBg)
            }

        // =========================================
        // HEADER
        // =========================================

        val header =
            android.widget.LinearLayout(this).apply {

                orientation =
                    android.widget.LinearLayout.VERTICAL

                setPadding(
                    dp(16),
                    dp(18),
                    dp(16),
                    dp(18)
                )

                setBackgroundColor(
                    android.graphics.Color.WHITE
                )
            }

        if (
            android.os.Build.VERSION.SDK_INT >=
            android.os.Build.VERSION_CODES.KITKAT_WATCH
        ) {
            header.setOnApplyWindowInsetsListener { _, insets ->

                header.setPadding(
                    dp(16),
                    dp(18) + insets.systemWindowInsetTop,
                    dp(16),
                    dp(18)
                )

                insets
            }

            header.requestApplyInsets()
        }

        val backButton =
            android.widget.TextView(this).apply {

                text = "‹  กลับ"

                textSize = 17f

                setTextColor(greenDark)

                setTypeface(
                    typeface,
                    android.graphics.Typeface.BOLD
                )

                setPadding(
                    dp(4),
                    dp(8),
                    dp(8),
                    dp(8)
                )

                isClickable = true
                isFocusable = true

                setOnClickListener {
                    showScreen(AppRoute.DASHBOARD)
                }
            }

        header.addView(backButton)

        header.addView(
            android.widget.TextView(this).apply {

                text = "ตั้งค่า"

                textSize = 28f

                setTextColor(dark)

                setTypeface(
                    typeface,
                    android.graphics.Typeface.BOLD
                )

                setPadding(
                    dp(4),
                    dp(8),
                    0,
                    0
                )
            }
        )

        header.addView(
            android.widget.TextView(this).apply {

                text =
                    "ปรับการทำงานของ PC Drone"

                textSize = 14f

                setTextColor(gray)

                setPadding(
                    dp(4),
                    dp(3),
                    0,
                    0
                )
            }
        )

        root.addView(header)

        // =========================================
        // CONTENT
        // =========================================

        val content =
            android.widget.LinearLayout(this).apply {

                orientation =
                    android.widget.LinearLayout.VERTICAL

                setPadding(
                    dp(14),
                    dp(16),
                    dp(14),
                    dp(30)
                )
            }

        fun sectionTitle(
            title: String
        ): android.widget.TextView {

            return android.widget.TextView(this).apply {

                text = title

                textSize = 18f

                setTextColor(dark)

                setTypeface(
                    typeface,
                    android.graphics.Typeface.BOLD
                )

                setPadding(
                    dp(4),
                    dp(14),
                    0,
                    dp(8)
                )
            }
        }

        fun card():
            android.widget.LinearLayout {

            return android.widget.LinearLayout(this).apply {

                orientation =
                    android.widget.LinearLayout.VERTICAL

                setPadding(
                    dp(16),
                    dp(8),
                    dp(16),
                    dp(8)
                )

                background =
                    rounded(
                        android.graphics.Color.WHITE,
                        16,
                        lineColor
                    )

                elevation =
                    dp(1).toFloat()
            }
        }

        fun divider():
            android.view.View {

            return android.view.View(this).apply {

                setBackgroundColor(lineColor)
            }
        }

        fun addDivider(
            parent: android.widget.LinearLayout
        ) {
            parent.addView(
                divider(),
                android.widget.LinearLayout.LayoutParams(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    dp(1)
                ).apply {
                    marginStart = dp(4)
                    marginEnd = dp(4)
                }
            )
        }

        fun toggleRow(
            title: String,
            subtitle: String,
            key: String,
            defaultValue: Boolean
        ): android.widget.LinearLayout {

            val row =
                android.widget.LinearLayout(this).apply {

                    orientation =
                        android.widget.LinearLayout.HORIZONTAL

                    gravity =
                        android.view.Gravity.CENTER_VERTICAL

                    setPadding(
                        dp(2),
                        dp(12),
                        dp(2),
                        dp(12)
                    )
                }

            val texts =
                android.widget.LinearLayout(this).apply {

                    orientation =
                        android.widget.LinearLayout.VERTICAL
                }

            texts.addView(
                android.widget.TextView(this).apply {

                    text = title
                    textSize = 16f

                    setTextColor(dark)

                    setTypeface(
                        typeface,
                        android.graphics.Typeface.BOLD
                    )
                }
            )

            if (subtitle.isNotBlank()) {
                texts.addView(
                    android.widget.TextView(this).apply {

                        text = subtitle
                        textSize = 12f

                        setTextColor(gray)

                        setPadding(
                            0,
                            dp(2),
                            0,
                            0
                        )
                    }
                )
            }

            val switch =
                android.widget.Switch(this).apply {

                    isChecked =
                        prefs.getBoolean(
                            key,
                            defaultValue
                        )

                    setOnCheckedChangeListener { _, checked ->

                        prefs.edit()
                            .putBoolean(
                                key,
                                checked
                            )
                            .apply()
                    }
                }

            row.addView(
                texts,
                android.widget.LinearLayout.LayoutParams(
                    0,
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
                )
            )

            row.addView(switch)

            return row
        }

        fun editField(
            label: String,
            key: String,
            hintText: String
        ): android.widget.LinearLayout {

            val wrap =
                android.widget.LinearLayout(this).apply {

                    orientation =
                        android.widget.LinearLayout.VERTICAL

                    setPadding(
                        dp(2),
                        dp(10),
                        dp(2),
                        dp(10)
                    )
                }

            wrap.addView(
                android.widget.TextView(this).apply {

                    text = label

                    textSize = 14f

                    setTextColor(dark)

                    setTypeface(
                        typeface,
                        android.graphics.Typeface.BOLD
                    )
                }
            )

            val edit =
                android.widget.EditText(this).apply {

                    setText(
                        prefs.getString(
                            key,
                            ""
                        ).orEmpty()
                    )

                    hint = hintText

                    textSize = 16f

                    setTextColor(dark)
                    setHintTextColor(gray)

                    setPadding(
                        dp(12),
                        dp(10),
                        dp(12),
                        dp(10)
                    )

                    background =
                        rounded(
                            android.graphics.Color.rgb(
                                248,
                                249,
                                248
                            ),
                            10,
                            lineColor
                        )

                    setSingleLine(
                        key != "profile_address"
                    )

                    setOnFocusChangeListener { _, hasFocus ->

                        if (!hasFocus) {
                            prefs.edit()
                                .putString(
                                    key,
                                    text.toString().trim()
                                )
                                .apply()
                        }
                    }
                }

            wrap.addView(
                edit,
                android.widget.LinearLayout.LayoutParams(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = dp(6)
                }
            )

            return wrap
        }

        // =========================================
        // GENERAL
        // =========================================

        content.addView(
            sectionTitle("ทั่วไป")
        )

        val generalCard = card()

        val yearRow =
            android.widget.LinearLayout(this).apply {

                orientation =
                    android.widget.LinearLayout.HORIZONTAL

                gravity =
                    android.view.Gravity.CENTER_VERTICAL

                setPadding(
                    dp(2),
                    dp(12),
                    dp(2),
                    dp(12)
                )
            }

        yearRow.addView(
            android.widget.TextView(this).apply {

                text = "รูปแบบปี"

                textSize = 16f

                setTextColor(dark)

                setTypeface(
                    typeface,
                    android.graphics.Typeface.BOLD
                )
            },
            android.widget.LinearLayout.LayoutParams(
                0,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        val yearButton =
            android.widget.Button(this).apply {

                val current =
                    prefs.getString(
                        "year_format",
                        "BE"
                    ) ?: "BE"

                text =
                    if (current == "BE") {
                        "พ.ศ."
                    } else {
                        "ค.ศ."
                    }

                textSize = 14f

                setTextColor(
                    android.graphics.Color.WHITE
                )

                background =
                    rounded(
                        green,
                        10
                    )

                setOnClickListener {

                    val newValue =
                        if (
                            prefs.getString(
                                "year_format",
                                "BE"
                            ) == "BE"
                        ) {
                            "CE"
                        } else {
                            "BE"
                        }

                    prefs.edit()
                        .putString(
                            "year_format",
                            newValue
                        )
                        .apply()

                    text =
                        if (newValue == "BE") {
                            "พ.ศ."
                        } else {
                            "ค.ศ."
                        }
                }
            }

        yearRow.addView(
            yearButton,
            android.widget.LinearLayout.LayoutParams(
                dp(82),
                dp(44)
            )
        )

        generalCard.addView(yearRow)

        addDivider(generalCard)

        generalCard.addView(
            toggleRow(
                "เวลา 24 ชั่วโมง",
                "แสดงเวลา เช่น 18:30 น.",
                "time_24h",
                true
            )
        )

        addDivider(generalCard)

        generalCard.addView(
            toggleRow(
                "ยืนยันก่อนลบข้อมูล",
                "ลดการลบข้อมูลโดยไม่ได้ตั้งใจ",
                "confirm_delete",
                true
            )
        )

        content.addView(generalCard)

        // =========================================
        // NOTIFICATION
        // =========================================

        content.addView(
            sectionTitle("การแจ้งเตือน")
        )

        val notifyCard = card()

        notifyCard.addView(
            toggleRow(
                "เปิดการแจ้งเตือน",
                "อนุญาตการเตือนภายใน PC Drone",
                "notifications_enabled",
                true
            )
        )

        addDivider(notifyCard)

        val advanceWrap =
            android.widget.LinearLayout(this).apply {

                orientation =
                    android.widget.LinearLayout.HORIZONTAL

                gravity =
                    android.view.Gravity.CENTER_VERTICAL

                setPadding(
                    dp(2),
                    dp(12),
                    dp(2),
                    dp(12)
                )
            }

        advanceWrap.addView(
            android.widget.TextView(this).apply {

                text =
                    "แจ้งเตือนงานบินล่วงหน้า"

                textSize = 16f

                setTextColor(dark)

                setTypeface(
                    typeface,
                    android.graphics.Typeface.BOLD
                )
            },
            android.widget.LinearLayout.LayoutParams(
                0,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        val advanceButton =
            android.widget.Button(this).apply {

                fun label(
                    value: Int
                ): String {

                    return when (value) {
                        15 -> "15 นาที"
                        30 -> "30 นาที"
                        60 -> "1 ชม."
                        else -> "30 นาที"
                    }
                }

                var current =
                    prefs.getInt(
                        "notify_advance_minutes",
                        30
                    )

                text = label(current)

                textSize = 13f

                setOnClickListener {

                    current =
                        when (current) {
                            15 -> 30
                            30 -> 60
                            else -> 15
                        }

                    prefs.edit()
                        .putInt(
                            "notify_advance_minutes",
                            current
                        )
                        .apply()

                    text = label(current)
                }
            }

        advanceWrap.addView(
            advanceButton,
            android.widget.LinearLayout.LayoutParams(
                dp(105),
                dp(46)
            )
        )

        notifyCard.addView(advanceWrap)

        addDivider(notifyCard)

        notifyCard.addView(
            editField(
                "เวลาแจ้งเตือนเริ่มต้น",
                "notification_default_time",
                "เช่น 07:00"
            )
        )

        content.addView(notifyCard)

        // =========================================
        // SOUND
        // =========================================

        content.addView(
            sectionTitle("เสียง")
        )

        val soundCard = card()

        soundCard.addView(
            toggleRow(
                "เสียงในแอป",
                "เปิดหรือปิดเสียงทั้งหมดของแอป",
                "sound_enabled",
                true
            )
        )

        addDivider(soundCard)

        soundCard.addView(
            toggleRow(
                "เสียงเมื่อบันทึกสำเร็จ",
                "",
                "sound_save_success",
                true
            )
        )

        addDivider(soundCard)

        soundCard.addView(
            toggleRow(
                "เสียงแจ้งเตือนงาน",
                "",
                "sound_job_notification",
                true
            )
        )

        addDivider(soundCard)


        // PC_DRONE_SOUND_PICKER_SETTING

        val soundPickerWrap =
            android.widget.LinearLayout(this).apply {

                orientation =
                    android.widget.LinearLayout.VERTICAL

                setPadding(
                    dp(2),
                    dp(12),
                    dp(2),
                    dp(12)
                )
            }

        val savedSoundUri =
            prefs.getString(
                "notification_sound_uri",
                null
            )

        val currentSoundUri =
            if (
                savedSoundUri.isNullOrBlank()
            ) {
                android.provider.Settings.System
                    .DEFAULT_NOTIFICATION_URI
            } else {
                android.net.Uri.parse(
                    savedSoundUri
                )
            }

        val currentSoundName =
            try {

                android.media.RingtoneManager
                    .getRingtone(
                        this,
                        currentSoundUri
                    )
                    ?.getTitle(
                        this
                    )
                    ?: "เสียงแจ้งเตือนเริ่มต้น"

            } catch (
                _: Exception
            ) {

                "เสียงแจ้งเตือนเริ่มต้น"
            }

        soundPickerWrap.addView(
            android.widget.TextView(this).apply {

                text =
                    "เสียงแจ้งเตือนงาน"

                textSize =
                    16f

                setTextColor(
                    dark
                )

                setTypeface(
                    typeface,
                    android.graphics.Typeface.BOLD
                )
            }
        )

        soundPickerWrap.addView(
            android.widget.TextView(this).apply {

                text =
                    "เสียงปัจจุบัน: $currentSoundName"

                textSize =
                    13f

                setTextColor(
                    gray
                )

                setPadding(
                    0,
                    dp(4),
                    0,
                    dp(8)
                )
            }
        )

        soundPickerWrap.addView(
            android.widget.Button(this).apply {

                text =
                    "เลือกเสียงจาก Android"

                textSize =
                    15f

                setTextColor(
                    android.graphics.Color.WHITE
                )

                background =
                    rounded(
                        green,
                        10
                    )

                setOnClickListener {

                    val picker =
                        android.content.Intent(
                            android.media.RingtoneManager
                                .ACTION_RINGTONE_PICKER
                        ).apply {

                            putExtra(
                                android.media.RingtoneManager
                                    .EXTRA_RINGTONE_TYPE,
                                android.media.RingtoneManager
                                    .TYPE_NOTIFICATION or
                                android.media.RingtoneManager
                                    .TYPE_RINGTONE
                            )

                            putExtra(
                                android.media.RingtoneManager
                                    .EXTRA_RINGTONE_TITLE,
                                "เลือกเสียงแจ้งเตือน PC Drone"
                            )

                            putExtra(
                                android.media.RingtoneManager
                                    .EXTRA_RINGTONE_SHOW_DEFAULT,
                                true
                            )

                            putExtra(
                                android.media.RingtoneManager
                                    .EXTRA_RINGTONE_SHOW_SILENT,
                                false
                            )

                            putExtra(
                                android.media.RingtoneManager
                                    .EXTRA_RINGTONE_EXISTING_URI,
                                currentSoundUri
                            )
                        }

                    startActivityForResult(
                        picker,
                        7601
                    )
                }
            },
            android.widget.LinearLayout.LayoutParams(
                android.view.ViewGroup
                    .LayoutParams.MATCH_PARENT,
                dp(50)
            )
        )

        soundCard.addView(
            soundPickerWrap
        )

        addDivider(
            soundCard
        )

        soundCard.addView(
            toggleRow(
                "การสั่นเมื่อแจ้งเตือน",
                "ให้โทรศัพท์สั่นเมื่อมีการเตือนงานบิน",
                "notification_vibration",
                true
            )
        )

        addDivider(
            soundCard
        )



        // =========================================
        // REPEAT NOTIFICATION
        // =========================================

        val repeatWrap =
            android.widget.LinearLayout(this).apply {

                orientation =
                    android.widget.LinearLayout.HORIZONTAL

                gravity =
                    android.view.Gravity.CENTER_VERTICAL

                setPadding(
                    dp(2),
                    dp(12),
                    dp(2),
                    dp(12)
                )
            }

        repeatWrap.addView(
            android.widget.TextView(this).apply {

                text =
                    "แจ้งเตือนซ้ำ"

                textSize =
                    16f

                setTextColor(dark)

                setTypeface(
                    typeface,
                    android.graphics.Typeface.BOLD
                )
            },
            android.widget.LinearLayout.LayoutParams(
                0,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        val repeatButton =
            android.widget.Button(this).apply {

                fun repeatLabel(
                    value: Int
                ): String {

                    return when (value) {
                        3 -> "3 นาที"
                        5 -> "5 นาที"
                        10 -> "10 นาที"
                        15 -> "15 นาที"
                        else -> "ปิด"
                    }
                }

                var current =
                    prefs.getInt(
                        "notification_repeat_minutes",
                        0
                    )

                text =
                    repeatLabel(current)

                textSize =
                    13f

                setOnClickListener {

                    current =
                        when (current) {
                            0 -> 3
                            3 -> 5
                            5 -> 10
                            10 -> 15
                            else -> 0
                        }

                    prefs.edit()
                        .putInt(
                            "notification_repeat_minutes",
                            current
                        )
                        .apply()

                    text =
                        repeatLabel(current)
                }
            }

        repeatWrap.addView(
            repeatButton,
            android.widget.LinearLayout.LayoutParams(
                dp(105),
                dp(46)
            )
        )

        soundCard.addView(
            repeatWrap
        )

        addDivider(
            soundCard
        )


        val volumeWrap =
            android.widget.LinearLayout(this).apply {

                orientation =
                    android.widget.LinearLayout.VERTICAL

                setPadding(
                    dp(2),
                    dp(12),
                    dp(2),
                    dp(12)
                )
            }

        val volumeLabel =
            android.widget.TextView(this).apply {

                val value =
                    prefs.getInt(
                        "sound_volume",
                        70
                    )

                text =
                    "ระดับเสียงในแอป  $value%"

                textSize = 16f

                setTextColor(dark)

                setTypeface(
                    typeface,
                    android.graphics.Typeface.BOLD
                )
            }

        val seek =
            android.widget.SeekBar(this).apply {

                max = 100

                progress =
                    prefs.getInt(
                        "sound_volume",
                        70
                    )

                setOnSeekBarChangeListener(
                    object :
                        android.widget.SeekBar.OnSeekBarChangeListener {

                        override fun onProgressChanged(
                            seekBar: android.widget.SeekBar?,
                            progress: Int,
                            fromUser: Boolean
                        ) {
                            volumeLabel.text =
                                "ระดับเสียงในแอป  $progress%"

                            if (fromUser) {
                                prefs.edit()
                                    .putInt(
                                        "sound_volume",
                                        progress
                                    )
                                    .apply()
                            }
                        }

                        override fun onStartTrackingTouch(
                            seekBar: android.widget.SeekBar?
                        ) {
                        }

                        override fun onStopTrackingTouch(
                            seekBar: android.widget.SeekBar?
                        ) {
                        }
                    }
                )
            }

        volumeWrap.addView(volumeLabel)

        volumeWrap.addView(
            seek,
            android.widget.LinearLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(8)
            }
        )

        soundCard.addView(volumeWrap)

        content.addView(soundCard)

        // =========================================
        // REPORT & EXPORT
        // =========================================

        content.addView(
            sectionTitle("รายงานและการส่งออก")
        )

        val reportCard = card()

        reportCard.addView(
            android.widget.LinearLayout(this).apply {

                orientation =
                    android.widget.LinearLayout.VERTICAL

                setPadding(
                    dp(4),
                    dp(14),
                    dp(4),
                    dp(14)
                )

                addView(
                    android.widget.TextView(
                        this@MainActivity
                    ).apply {

                        text = "สรุปรายงานการเงิน"

                        textSize = 17f

                        setTextColor(dark)

                        setTypeface(
                            typeface,
                            android.graphics.Typeface.BOLD
                        )
                    }
                )

                addView(
                    android.widget.TextView(
                        this@MainActivity
                    ).apply {

                        text =
                            "ประมวลผลรายรับ รายจ่าย งานบิน และแชร์รายงาน PDF"

                        textSize = 13f

                        setTextColor(gray)

                        setPadding(
                            0,
                            dp(4),
                            0,
                            dp(12)
                        )
                    }
                )

                addView(
                    android.widget.Button(
                        this@MainActivity
                    ).apply {

                        text =
                            "เปิดรายงานและแชร์ PDF"

                        textSize = 16f

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
                                12
                            )

                        setOnClickListener {
                            showScreen(
                                AppRoute.REPORTS
                            )
                        }
                    },
                    android.widget.LinearLayout.LayoutParams(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(52)
                    )
                )
            }
        )

        content.addView(reportCard)

        content.addView(
            sectionTitle("สำรองและกู้คืนข้อมูล")
        )

        val backupCard = card()

        backupCard.addView(
            android.widget.Button(this).apply {

                text = "เปิดเมนูสำรองข้อมูล"

                textSize = 16f

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
                        12
                    )

                setOnClickListener {

                    startActivity(
                        android.content.Intent(
                            this@MainActivity,
                            BackupActivity::class.java
                        )
                    )
                }
            },
            android.widget.LinearLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                dp(54)
            )
        )

        content.addView(backupCard)

        // =========================================
        // PROFILE
        // =========================================

        content.addView(
            sectionTitle("ข้อมูลผู้ใช้งาน")
        )

        val profileCard = card()

        profileCard.addView(
            editField(
                "ชื่อผู้ให้บริการ / ชื่อกิจการ",
                "profile_name",
                "กรอกชื่อ"
            )
        )

        addDivider(profileCard)

        profileCard.addView(
            editField(
                "เบอร์โทร",
                "profile_phone",
                "กรอกเบอร์โทร"
            )
        )

        addDivider(profileCard)

        profileCard.addView(
            editField(
                "พื้นที่ให้บริการ",
                "profile_service_area",
                "เช่น ทองผาภูมิ และพื้นที่ใกล้เคียง"
            )
        )

        addDivider(profileCard)

        profileCard.addView(
            editField(
                "ที่อยู่",
                "profile_address",
                "กรอกที่อยู่"
            )
        )

        addDivider(profileCard)

        profileCard.addView(
            editField(
                "เลขประจำตัวผู้เสียภาษี (ไม่บังคับ)",
                "profile_tax_id",
                "เว้นว่างได้"
            )
        )

        content.addView(profileCard)

        val saveButton =
            android.widget.Button(this).apply {

                text = "บันทึกการตั้งค่า"

                textSize = 17f

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
                        14
                    )

                setOnClickListener {

                    val focus =
                        currentFocus

                    focus?.clearFocus()

                    android.widget.Toast.makeText(
                        this@MainActivity,
                        "บันทึกการตั้งค่าแล้ว",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            }

        content.addView(
            saveButton,
            android.widget.LinearLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                dp(54)
            ).apply {
                topMargin = dp(18)
            }
        )

        // =========================================
        // SYSTEM INFO
        // =========================================

        content.addView(
            sectionTitle("ข้อมูลระบบ")
        )

        val systemCard = card()

        fun infoRow(
            label: String,
            value: String
        ): android.widget.LinearLayout {

            return android.widget.LinearLayout(this).apply {

                orientation =
                    android.widget.LinearLayout.HORIZONTAL

                gravity =
                    android.view.Gravity.CENTER_VERTICAL

                setPadding(
                    dp(2),
                    dp(13),
                    dp(2),
                    dp(13)
                )

                addView(
                    android.widget.TextView(
                        this@MainActivity
                    ).apply {

                        text = label

                        textSize = 15f

                        setTextColor(dark)

                        setTypeface(
                            typeface,
                            android.graphics.Typeface.BOLD
                        )
                    },
                    android.widget.LinearLayout.LayoutParams(
                        0,
                        android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                )

                addView(
                    android.widget.TextView(
                        this@MainActivity
                    ).apply {

                        text = value

                        textSize = 14f

                        setTextColor(gray)

                        gravity =
                            android.view.Gravity.END
                    }
                )
            }
        }

        val versionName =
            try {
                packageManager
                    .getPackageInfo(
                        packageName,
                        0
                    )
                    .versionName ?: "-"
            } catch (_: Exception) {
                "-"
            }

        systemCard.addView(
            infoRow(
                "เขตเวลา",
                "Asia/Bangkok"
            )
        )

        addDivider(systemCard)

        systemCard.addView(
            infoRow(
                "รูปแบบวันที่",
                "DD/MM/YYYY"
            )
        )

        addDivider(systemCard)

        systemCard.addView(
            infoRow(
                "ชื่อแอป",
                "PC Drone V3 Plus"
            )
        )

        addDivider(systemCard)

        systemCard.addView(
            infoRow(
                "เวอร์ชันแอป",
                versionName
            )
        )

        content.addView(systemCard)

        val scroll =
            android.widget.ScrollView(this).apply {

                isFillViewport = true

                addView(
                    content,
                    android.view.ViewGroup.LayoutParams(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                )
            }

        root.addView(
            scroll,
            android.widget.LinearLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        setContentView(root)
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

                                    val saved =
                                        prefs.edit()
                                            .putStringSet(
                                                "customers",
                                                current.toSet()
                                            )
                                            .commit()

                                    if (!saved) {
                                        android.widget.Toast
                                            .makeText(
                                                this@MainActivity,
                                                "บันทึกข้อมูลลูกค้าไม่สำเร็จ",
                                                android.widget.Toast.LENGTH_LONG
                                            )
                                            .show()
                                        return@setOnClickListener
                                    }

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

                                    val saved =
                                        prefs.edit()
                                            .putStringSet(
                                                "customers",
                                                current.toSet()
                                            )
                                            .commit()

                                    if (!saved) {
                                        android.widget.Toast
                                            .makeText(
                                                this@MainActivity,
                                                "ลบข้อมูลลูกค้าไม่สำเร็จ",
                                                android.widget.Toast.LENGTH_LONG
                                            )
                                            .show()
                                    } else {
                                        android.widget.Toast
                                            .makeText(
                                                this@MainActivity,
                                                "ลบข้อมูลลูกค้าแล้ว",
                                                android.widget.Toast.LENGTH_SHORT
                                            )
                                            .show()

                                        reloadCustomers()
                                    }
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

                    val saved =
                        prefs.edit()
                            .putStringSet(
                                "customers",
                                current.toSet()
                            )
                            .commit()

                    if (!saved) {
                        android.widget.Toast
                            .makeText(
                                this@MainActivity,
                                "บันทึกข้อมูลลูกค้าไม่สำเร็จ",
                                android.widget.Toast.LENGTH_LONG
                            )
                            .show()
                        return@setOnClickListener
                    }

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


    private fun scheduleJobReminder(
        jobId: Long,
        appointmentMillis: Long,
        customer: String,
        service: String,
        location: String,
        rai: Double
    ) {

        val settingsPrefs =
            getSharedPreferences(
                "pc_drone_settings",
                android.content.Context.MODE_PRIVATE
            )

        val notificationsEnabled =
            settingsPrefs.getBoolean(
                "notifications_enabled",
                true
            )

        if (!notificationsEnabled) {
            return
        }

        /*
         * รองรับทั้ง Int / Long / String
         * เผื่อ Settings รุ่นก่อนบันทึกคนละชนิด
         */
        val advanceValue =
            settingsPrefs.all[
                "notify_advance_minutes"
            ]

        val advanceMinutes =
            when (advanceValue) {

                is Int ->
                    advanceValue

                is Long ->
                    advanceValue.toInt()

                is Float ->
                    advanceValue.toInt()

                is String ->
                    advanceValue
                        .toIntOrNull()
                        ?: 120

                else ->
                    120
            }.coerceAtLeast(0)

        var triggerAt =
            appointmentMillis -
                (
                    advanceMinutes *
                        60_000L
                )

        val now =
            System.currentTimeMillis()

        /*
         * ถ้าเลยเวลาเตือนล่วงหน้าแล้ว
         * ให้เตือนภายในประมาณ 5 วินาที
         * แทนที่จะไม่เตือนเลย
         */
        if (triggerAt <= now) {
            triggerAt =
                now + 5_000L
        }

        val intent =
            android.content.Intent(
                this,
                JobNotificationReceiver::class.java
            ).apply {

                putExtra(
                    "job_id",
                    jobId
                )

                putExtra(
                    "appointment_millis",
                    appointmentMillis
                )

                putExtra(
                    "customer",
                    customer
                )

                putExtra(
                    "service",
                    service
                )

                putExtra(
                    "location",
                    location
                )

                putExtra(
                    "rai",
                    rai
                )
            }

        val requestCode =
            (
                jobId xor
                    (jobId ushr 32)
                ).toInt() and 0x7fffffff

        val pendingIntent =
            android.app.PendingIntent
                .getBroadcast(
                    this,
                    requestCode,
                    intent,
                    android.app.PendingIntent
                        .FLAG_UPDATE_CURRENT or
                        android.app.PendingIntent
                            .FLAG_IMMUTABLE
                )

        val alarmManager =
            getSystemService(
                android.content.Context.ALARM_SERVICE
            ) as android.app.AlarmManager

        if (
            android.os.Build.VERSION.SDK_INT >=
            android.os.Build.VERSION_CODES.S
        ) {

            if (
                alarmManager
                    .canScheduleExactAlarms()
            ) {

                alarmManager
                    .setExactAndAllowWhileIdle(
                        android.app.AlarmManager
                            .RTC_WAKEUP,
                        triggerAt,
                        pendingIntent
                    )

            } else {

                /*
                 * ไม่มีสิทธิ์ Exact Alarm
                 * ใช้ alarm ปกติแทน
                 */
                alarmManager
                    .setAndAllowWhileIdle(
                        android.app.AlarmManager
                            .RTC_WAKEUP,
                        triggerAt,
                        pendingIntent
                    )
            }

        } else {

            alarmManager
                .setExactAndAllowWhileIdle(
                    android.app.AlarmManager
                        .RTC_WAKEUP,
                    triggerAt,
                    pendingIntent
                )
        }

        /*
         * Android 13+
         * ขอสิทธิ์ Notification จากผู้ใช้
         */
        if (
            android.os.Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(
                "android.permission.POST_NOTIFICATIONS"
            ) !=
            android.content.pm.PackageManager
                .PERMISSION_GRANTED
        ) {

            requestPermissions(
                arrayOf(
                    "android.permission.POST_NOTIFICATIONS"
                ),
                5501
            )
        }
    }


    private fun cancelJobReminder(
        jobId: Long
    ) {

        val intent =
            android.content.Intent(
                this,
                JobNotificationReceiver::class.java
            )

        val requestCode =
            (
                jobId xor
                    (jobId ushr 32)
                ).toInt() and 0x7fffffff

        val pendingIntent =
            android.app.PendingIntent
                .getBroadcast(
                    this,
                    requestCode,
                    intent,
                    android.app.PendingIntent
                        .FLAG_NO_CREATE or
                        android.app.PendingIntent
                            .FLAG_IMMUTABLE
                )

        if (pendingIntent != null) {

            val alarmManager =
                getSystemService(
                    android.content.Context.ALARM_SERVICE
                ) as android.app.AlarmManager

            alarmManager.cancel(
                pendingIntent
            )

            pendingIntent.cancel()
        }

        val repeatPendingIntent =
            android.app.PendingIntent.getBroadcast(
                this,
                requestCode + 100000,
                intent,
                android.app.PendingIntent.FLAG_NO_CREATE or
                    android.app.PendingIntent.FLAG_IMMUTABLE
            )

        if (repeatPendingIntent != null) {

            val alarmManager =
                getSystemService(
                    android.content.Context.ALARM_SERVICE
                ) as android.app.AlarmManager

            alarmManager.cancel(
                repeatPendingIntent
            )

            repeatPendingIntent.cancel()
        }
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

        // ---------- APPOINTMENT ----------
        val appointmentLabel =
            android.widget.TextView(this).apply {
                text = "วันและเวลานัดหมาย"
                textSize = 17f
                setTextColor(android.graphics.Color.BLACK)
                setTypeface(
                    typeface,
                    android.graphics.Typeface.BOLD
                )
                setPadding(0, dp(18), 0, dp(6))
            }

        val appointmentTz =
            java.util.TimeZone.getTimeZone(
                "Asia/Bangkok"
            )

        val appointmentCal =
            java.util.Calendar
                .getInstance(appointmentTz)
                .apply {
                    add(
                        java.util.Calendar.DAY_OF_MONTH,
                        1
                    )
                    set(
                        java.util.Calendar.HOUR_OF_DAY,
                        8
                    )
                    set(
                        java.util.Calendar.MINUTE,
                        0
                    )
                    set(
                        java.util.Calendar.SECOND,
                        0
                    )
                    set(
                        java.util.Calendar.MILLISECOND,
                        0
                    )
                }

        var appointmentDateSelected = false

        val appointmentDateButton =
            android.widget.Button(this).apply {
                text = "เลือกวันที่นัดหมาย"
                textSize = 16f
                isAllCaps = false
            }

        val appointmentTimeButton =
            android.widget.Button(this).apply {
                text = "เวลา 08:00 น."
                textSize = 16f
                isAllCaps = false
            }

        fun updateAppointmentDateText() {
            appointmentDateButton.text =
                "%02d/%02d/%04d".format(
                    appointmentCal.get(
                        java.util.Calendar.DAY_OF_MONTH
                    ),
                    appointmentCal.get(
                        java.util.Calendar.MONTH
                    ) + 1,
                    appointmentCal.get(
                        java.util.Calendar.YEAR
                    ) + 543
                )
        }

        fun updateAppointmentTimeText() {
            appointmentTimeButton.text =
                "เวลา %02d:%02d น.".format(
                    appointmentCal.get(
                        java.util.Calendar.HOUR_OF_DAY
                    ),
                    appointmentCal.get(
                        java.util.Calendar.MINUTE
                    )
                )
        }

        appointmentDateButton.setOnClickListener {

            android.app.DatePickerDialog(
                this,
                { _, year, month, day ->

                    appointmentCal.set(
                        java.util.Calendar.YEAR,
                        year
                    )
                    appointmentCal.set(
                        java.util.Calendar.MONTH,
                        month
                    )
                    appointmentCal.set(
                        java.util.Calendar.DAY_OF_MONTH,
                        day
                    )

                    appointmentDateSelected = true
                    updateAppointmentDateText()
                },
                appointmentCal.get(
                    java.util.Calendar.YEAR
                ),
                appointmentCal.get(
                    java.util.Calendar.MONTH
                ),
                appointmentCal.get(
                    java.util.Calendar.DAY_OF_MONTH
                )
            ).show()
        }

        appointmentTimeButton.setOnClickListener {

            android.app.TimePickerDialog(
                this,
                { _, hour, minute ->

                    appointmentCal.set(
                        java.util.Calendar.HOUR_OF_DAY,
                        hour
                    )
                    appointmentCal.set(
                        java.util.Calendar.MINUTE,
                        minute
                    )

                    updateAppointmentTimeText()
                },
                appointmentCal.get(
                    java.util.Calendar.HOUR_OF_DAY
                ),
                appointmentCal.get(
                    java.util.Calendar.MINUTE
                ),
                true
            ).show()
        }

        val reminderSwitch =
            android.widget.Switch(this).apply {

                text =
                    "เปิดการแจ้งเตือนสำหรับงานนี้"

                textSize = 16f

                val settingsPrefs =
                    getSharedPreferences(
                        "pc_drone_settings",
                        android.content.Context.MODE_PRIVATE
                    )

                isChecked =
                    settingsPrefs.getBoolean(
                        "notifications_enabled",
                        true
                    )

                setPadding(
                    0,
                    dp(8),
                    0,
                    dp(10)
                )
            }

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

                    if (!appointmentDateSelected) {

                        android.widget.Toast.makeText(
                            this@MainActivity,
                            "กรุณาเลือกวันที่นัดหมาย",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()

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

                    /*
                     * APPOINTMENT_SCHEMA
                     * 0 = jobId (ใช้ timestamp เดียวกับ flight_jobs)
                     * 1 = appointmentMillis
                     * 2 = reminderEnabled
                     */
                    val appointmentRecord =
                        listOf(
                            time.toString(),
                            appointmentCal.timeInMillis
                                .toString(),
                            reminderSwitch.isChecked
                                .toString()
                        ).joinToString("|||")

                    val appointmentSet =
                        prefs.getStringSet(
                            "job_appointments",
                            emptySet()
                        )?.toMutableSet()
                            ?: mutableSetOf()

                    appointmentSet.add(
                        appointmentRecord
                    )

                    prefs.edit()
                        .putStringSet(
                            "job_appointments",
                            appointmentSet
                        )
                        .apply()

                    // PC_DRONE_NOTIFICATION_SCHEDULED
                    if (
                        reminderSwitch.isChecked &&
                        status != "เสร็จแล้ว" &&
                        status != "ยกเลิก"
                    ) {

                        scheduleJobReminder(
                            jobId = time,
                            appointmentMillis =
                                appointmentCal.timeInMillis,
                            customer = customer,
                            service = service,
                            location = location,
                            rai = rai
                        )

                    } else {

                        cancelJobReminder(
                            time
                        )
                    }

                    val appointmentText =
                        "%02d/%02d/%04d %02d:%02d".format(
                            appointmentCal.get(
                                java.util.Calendar.DAY_OF_MONTH
                            ),
                            appointmentCal.get(
                                java.util.Calendar.MONTH
                            ) + 1,
                            appointmentCal.get(
                                java.util.Calendar.YEAR
                            ) + 543,
                            appointmentCal.get(
                                java.util.Calendar.HOUR_OF_DAY
                            ),
                            appointmentCal.get(
                                java.util.Calendar.MINUTE
                            )
                        )

                    resultText.text =
                        "บันทึกสำเร็จ\n" +
                        "ลูกค้า: $customer\n" +
                        "งาน: $service\n" +
                        "จำนวน: %.2f ไร่\n".format(rai) +
                        "ราคา: %.2f บาท/ไร่\n".format(rate) +
                        "รวม: %.2f บาท\n".format(total) +
                        "สถานะ: $status\n" +
                        "นัดหมาย: $appointmentText\n" +
                        "แจ้งเตือน: " +
                        if (reminderSwitch.isChecked) {
                            "เปิด"
                        } else {
                            "ปิด"
                        }

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

        root.addView(appointmentLabel)

        val appointmentRow =
            android.widget.LinearLayout(this).apply {
                orientation =
                    android.widget.LinearLayout.HORIZONTAL
            }

        appointmentRow.addView(
            appointmentDateButton,
            android.widget.LinearLayout.LayoutParams(
                0,
                dp(54),
                1f
            ).apply {
                marginEnd = dp(5)
            }
        )

        appointmentRow.addView(
            appointmentTimeButton,
            android.widget.LinearLayout.LayoutParams(
                0,
                dp(54),
                1f
            ).apply {
                marginStart = dp(5)
            }
        )

        root.addView(appointmentRow)
        root.addView(reminderSwitch)

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

        val searchRow =
            android.widget.LinearLayout(this).apply {
                orientation =
                    android.widget.LinearLayout.HORIZONTAL

                setPadding(
                    0,
                    dp(4),
                    0,
                    dp(12)
                )
            }

        val daySearchInput =
            android.widget.EditText(this).apply {

                hint = "วัน"
                textSize = 15f
                isSingleLine = true

                gravity =
                    android.view.Gravity.CENTER

                inputType =
                    android.text.InputType.TYPE_CLASS_NUMBER

                filters =
                    arrayOf(
                        android.text.InputFilter.LengthFilter(2)
                    )
            }

        val monthSearchInput =
            android.widget.EditText(this).apply {

                hint = "เดือน"
                textSize = 15f
                isSingleLine = true

                gravity =
                    android.view.Gravity.CENTER

                inputType =
                    android.text.InputType.TYPE_CLASS_NUMBER

                filters =
                    arrayOf(
                        android.text.InputFilter.LengthFilter(2)
                    )
            }

        val yearSearchInput =
            android.widget.EditText(this).apply {

                hint = "ปี"
                textSize = 15f
                isSingleLine = true

                gravity =
                    android.view.Gravity.CENTER

                inputType =
                    android.text.InputType.TYPE_CLASS_NUMBER

                filters =
                    arrayOf(
                        android.text.InputFilter.LengthFilter(4)
                    )
            }

        // Auto jump: วัน -> เดือน -> ปี
        daySearchInput.addTextChangedListener(
            object : android.text.TextWatcher {

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) = Unit

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {

                    if (s?.length == 2) {
                        monthSearchInput.requestFocus()
                    }
                }

                override fun afterTextChanged(
                    s: android.text.Editable?
                ) = Unit
            }
        )

        monthSearchInput.addTextChangedListener(
            object : android.text.TextWatcher {

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) = Unit

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {

                    if (s?.length == 2) {
                        yearSearchInput.requestFocus()
                    }
                }

                override fun afterTextChanged(
                    s: android.text.Editable?
                ) = Unit
            }
        )

        // ถ้ากรอกวัน/เดือนหลักเดียวแล้วกด Next
        // เติม 0 ด้านหน้าให้อัตโนมัติ
        daySearchInput.imeOptions =
            android.view.inputmethod.EditorInfo.IME_ACTION_NEXT

        monthSearchInput.imeOptions =
            android.view.inputmethod.EditorInfo.IME_ACTION_NEXT

        yearSearchInput.imeOptions =
            android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH

        daySearchInput.setOnEditorActionListener {
            _, actionId, _ ->

            if (
                actionId ==
                android.view.inputmethod.EditorInfo.IME_ACTION_NEXT
            ) {

                val value =
                    daySearchInput.text
                        .toString()
                        .trim()

                if (value.length == 1) {
                    daySearchInput.setText(
                        value.padStart(2, '0')
                    )
                }

                monthSearchInput.requestFocus()
                true

            } else {
                false
            }
        }

        monthSearchInput.setOnEditorActionListener {
            _, actionId, _ ->

            if (
                actionId ==
                android.view.inputmethod.EditorInfo.IME_ACTION_NEXT
            ) {

                val value =
                    monthSearchInput.text
                        .toString()
                        .trim()

                if (value.length == 1) {
                    monthSearchInput.setText(
                        value.padStart(2, '0')
                    )
                }

                yearSearchInput.requestFocus()
                true

            } else {
                false
            }
        }

        val searchButton =
            android.widget.Button(this).apply {

                text = "ค้นหา"
                isAllCaps = false

                setTextColor(
                    android.graphics.Color.WHITE
                )

                background =
                    android.graphics.drawable.GradientDrawable().apply {

                        setColor(
                            android.graphics.Color.rgb(
                                0,
                                105,
                                55
                            )
                        )

                        cornerRadius =
                            dp(10).toFloat()

                        setStroke(
                            dp(2),
                            android.graphics.Color.rgb(
                                0,
                                70,
                                35
                            )
                        )
                    }

                elevation =
                    dp(5).toFloat()
            }

        searchRow.addView(
            daySearchInput,
            android.widget.LinearLayout.LayoutParams(
                0,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                0.8f
            ).apply {
                marginEnd = dp(4)
            }
        )

        searchRow.addView(
            monthSearchInput,
            android.widget.LinearLayout.LayoutParams(
                0,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                0.9f
            ).apply {
                marginEnd = dp(4)
            }
        )

        searchRow.addView(
            yearSearchInput,
            android.widget.LinearLayout.LayoutParams(
                0,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                1.2f
            ).apply {
                marginEnd = dp(6)
            }
        )

        searchRow.addView(
            searchButton,
            android.widget.LinearLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        root.addView(searchRow)

        val refreshHistoryButton =
            android.widget.Button(this).apply {

                text = "↻ กลับงานล่าสุด"
                isAllCaps = false
                textSize = 16f

                setTextColor(
                    android.graphics.Color.WHITE
                )

                background =
                    android.graphics.drawable.GradientDrawable().apply {

                        setColor(
                            android.graphics.Color.rgb(
                                65,
                                65,
                                65
                            )
                        )

                        cornerRadius =
                            dp(12).toFloat()

                        setStroke(
                            dp(2),
                            android.graphics.Color.rgb(
                                30,
                                30,
                                30
                            )
                        )
                    }

                elevation = dp(5).toFloat()

                setPadding(
                    dp(16),
                    dp(8),
                    dp(16),
                    dp(8)
                )
            }

        root.addView(
            refreshHistoryButton,
            android.widget.LinearLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(12)
            }
        )

        val historyProcessButton =
            android.widget.Button(this).apply {
                text = "ประมวลผล / แชร์ PDF"
                isAllCaps = false
                textSize = 16f

                setTextColor(android.graphics.Color.WHITE)

                background =
                    android.graphics.drawable.GradientDrawable().apply {
                        setColor(
                            android.graphics.Color.rgb(0, 105, 55)
                        )
                        cornerRadius = dp(12).toFloat()
                        setStroke(
                            dp(2),
                            android.graphics.Color.rgb(0, 70, 35)
                        )
                    }

                elevation = dp(5).toFloat()
            }

        historyProcessButton.setOnClickListener {
            startActivity(
                android.content.Intent(
                    this,
                    HistoryReportActivity::class.java
                )
            )
        }

        root.addView(
            historyProcessButton,
            android.widget.LinearLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(12)
            }
        )

        val historyModeText =
            android.widget.TextView(this).apply {
                text =
                    "แสดง 10 งานล่าสุด • ค้นหาวันที่เพื่อดูงานย้อนหลัง"

                textSize = 14f

                setTextColor(
                    android.graphics.Color.DKGRAY
                )

                setPadding(
                    0,
                    0,
                    0,
                    dp(10)
                )
            }

        root.addView(historyModeText)

        var historySearchDate: String? = null

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

            val appointmentRecords =
                prefs.getStringSet(
                    "job_appointments",
                    emptySet()
                )?.toList()
                    ?: emptyList()

            val appointmentMap =
                appointmentRecords
                    .mapNotNull { appointmentRecord ->

                        val appointmentParts =
                            appointmentRecord.split(
                                "|||",
                                ignoreCase = false,
                                limit = 3
                            )

                        val appointmentJobId =
                            appointmentParts
                                .getOrNull(0)
                                ?.toLongOrNull()

                        if (appointmentJobId != null) {
                            appointmentJobId to appointmentParts
                        } else {
                            null
                        }
                    }
                    .toMap()

            val allSortedRecords =
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

            fun normalizedSearchDate(
                millis: Long
            ): String {

                val calendar =
                    java.util.Calendar.getInstance(
                        java.util.TimeZone.getTimeZone(
                            "Asia/Bangkok"
                        )
                    ).apply {
                        timeInMillis = millis
                    }

                return "%02d/%02d/%04d".format(
                    calendar.get(
                        java.util.Calendar.DAY_OF_MONTH
                    ),
                    calendar.get(
                        java.util.Calendar.MONTH
                    ) + 1,
                    calendar.get(
                        java.util.Calendar.YEAR
                    ) + 543
                )
            }

            val searchDate =
                historySearchDate

            val sortedRecords =
                if (searchDate.isNullOrBlank()) {

                    allSortedRecords.take(10)

                } else {

                    allSortedRecords.filter { record ->

                        val searchParts =
                            record.split(
                                "|||",
                                ignoreCase = false,
                                limit = 9
                            )

                        val jobId =
                            searchParts
                                .getOrNull(0)
                                ?.toLongOrNull()
                                ?: 0L

                        val appointmentMillis =
                            appointmentMap[jobId]
                                ?.getOrNull(1)
                                ?.toLongOrNull()
                                ?: 0L

                        val dateMillis =
                            if (appointmentMillis > 0L) {
                                appointmentMillis
                            } else {
                                jobId
                            }

                        dateMillis > 0L &&
                            normalizedSearchDate(
                                dateMillis
                            ) == searchDate
                    }
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

                val appointmentParts =
                    appointmentMap[time]

                val appointmentMillis =
                    appointmentParts
                        ?.getOrNull(1)
                        ?.toLongOrNull()
                        ?: 0L

                val reminderEnabled =
                    appointmentParts
                        ?.getOrNull(2)
                        ?.toBooleanStrictOrNull()
                        ?: false

                val appointmentText =
                    if (appointmentMillis > 0L) {

                        val appointmentCalendar =
                            java.util.Calendar.getInstance(
                                java.util.TimeZone.getTimeZone(
                                    "Asia/Bangkok"
                                )
                            ).apply {
                                timeInMillis =
                                    appointmentMillis
                            }

                        "%02d/%02d/%04d %02d:%02d น.".format(
                            appointmentCalendar.get(
                                java.util.Calendar.DAY_OF_MONTH
                            ),
                            appointmentCalendar.get(
                                java.util.Calendar.MONTH
                            ) + 1,
                            appointmentCalendar.get(
                                java.util.Calendar.YEAR
                            ) + 543,
                            appointmentCalendar.get(
                                java.util.Calendar.HOUR_OF_DAY
                            ),
                            appointmentCalendar.get(
                                java.util.Calendar.MINUTE
                            )
                        )

                    } else {
                        "ไม่มีข้อมูลนัดหมาย"
                    }

                val reminderText =
                    if (reminderEnabled) {
                        "เปิด"
                    } else {
                        "ปิด"
                    }

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

                        val cardColor =
                            when (status) {
                                "เสร็จแล้ว" ->
                                    android.graphics.Color.rgb(76, 175, 80)

                                "รอทำงาน" ->
                                    android.graphics.Color.rgb(255, 193, 7)

                                "กำลังดำเนินงาน" ->
                                    android.graphics.Color.rgb(239, 83, 80)

                                else ->
                                    android.graphics.Color.rgb(224, 224, 224)
                            }

                        val cardBorderColor =
                            when (status) {
                                "เสร็จแล้ว" ->
                                    android.graphics.Color.rgb(27, 94, 32)

                                "รอทำงาน" ->
                                    android.graphics.Color.rgb(245, 127, 23)

                                "กำลังดำเนินงาน" ->
                                    android.graphics.Color.rgb(183, 28, 28)

                                else ->
                                    android.graphics.Color.rgb(97, 97, 97)
                            }

                        val cardTextColor =
                            when (status) {
                                "เสร็จแล้ว",
                                "กำลังดำเนินงาน" ->
                                    android.graphics.Color.WHITE

                                else ->
                                    android.graphics.Color.BLACK
                            }

                        background =
                            android.graphics.drawable.GradientDrawable().apply {
                                setColor(cardColor)

                                cornerRadius =
                                    dp(14).toFloat()

                                setStroke(
                                    dp(2),
                                    cardBorderColor
                                )
                            }

                        elevation = dp(6).toFloat()

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
                            when (status) {
                                "เสร็จแล้ว",
                                "กำลังดำเนินงาน" ->
                                    android.graphics.Color.WHITE

                                else ->
                                    android.graphics.Color.BLACK
                            }
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
                            "วันที่บันทึก: $dateText\n" +
                            "วันนัดหมาย: $appointmentText\n" +
                            "แจ้งเตือน: $reminderText\n" +
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
                            when (status) {
                                "เสร็จแล้ว",
                                "กำลังดำเนินงาน" ->
                                    android.graphics.Color.WHITE

                                else ->
                                    android.graphics.Color.BLACK
                            }
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

                val completeButton =
                    android.widget.Button(
                        this@MainActivity
                    ).apply {

                        text = "เสร็จแล้ว"
                        isAllCaps = false

                        setTextColor(
                            android.graphics.Color.WHITE
                        )

                        background =
                            android.graphics.drawable.GradientDrawable().apply {

                                setColor(
                                    android.graphics.Color.rgb(
                                        0,
                                        120,
                                        55
                                    )
                                )

                                cornerRadius =
                                    dp(12).toFloat()

                                setStroke(
                                    dp(2),
                                    android.graphics.Color.rgb(
                                        0,
                                        80,
                                        38
                                    )
                                )
                            }

                        elevation =
                            dp(8).toFloat()

                        stateListAnimator = null

                        setOnClickListener {

                            val current =
                                prefs.getStringSet(
                                    "flight_jobs",
                                    emptySet()
                                )?.toMutableSet()
                                    ?: mutableSetOf()

                            val updatedParts =
                                parts.toMutableList()

                            if (updatedParts.size >= 8) {

                                updatedParts[7] =
                                    "เสร็จแล้ว"

                                val completedRecord =
                                    updatedParts.joinToString(
                                        "|||"
                                    )

                                current.remove(record)
                                current.add(completedRecord)

                                prefs.edit()
                                    .putStringSet(
                                        "flight_jobs",
                                        current
                                    )
                                    .apply()

                                // =====================================
                                // PHASE 2C - LINK COMPLETED JOB TO WALLET
                                // =====================================

                                val completedJobId =
                                    updatedParts
                                        .getOrNull(0)
                                        ?.trim()
                                        .orEmpty()

                                val walletRecords =
                                    prefs.getStringSet(
                                        "money_manager_wallets",
                                        emptySet()
                                    )?.toList()
                                        ?: emptyList()

                                val walletPairs =
                                    walletRecords.mapNotNull { walletRecord ->

                                        val walletParts =
                                            walletRecord.split(
                                                "|||",
                                                ignoreCase = false,
                                                limit = 5
                                            )

                                        val walletId =
                                            walletParts.getOrNull(0)
                                                ?.trim()
                                                .orEmpty()

                                        val walletName =
                                            walletParts.getOrNull(1)
                                                ?.trim()
                                                .orEmpty()

                                        if (
                                            walletId.isBlank() ||
                                            walletName.isBlank()
                                        ) {
                                            null
                                        } else {
                                            walletId to walletName
                                        }
                                    }.sortedBy {
                                        it.second
                                    }

                                if (
                                    completedJobId.isNotBlank() &&
                                    walletPairs.isNotEmpty()
                                ) {

                                    val walletNames =
                                        walletPairs.map {
                                            it.second
                                        }.toTypedArray()

                                    android.app.AlertDialog.Builder(
                                        this@MainActivity
                                    )
                                        .setTitle(
                                            "รับเงินงานบินเข้ากระเป๋า"
                                        )
                                        .setItems(
                                            walletNames
                                        ) { _, which ->

                                            val selectedWalletId =
                                                walletPairs
                                                    .getOrNull(which)
                                                    ?.first
                                                    .orEmpty()

                                            if (
                                                selectedWalletId.isNotBlank()
                                            ) {

                                                val links =
                                                    prefs.getStringSet(
                                                        "money_manager_job_links",
                                                        emptySet()
                                                    )?.toMutableSet()
                                                        ?: mutableSetOf()

                                                links.removeAll { link ->

                                                    link.split(
                                                        "|||",
                                                        ignoreCase = false,
                                                        limit = 2
                                                    ).getOrNull(0)
                                                        ?.trim() ==
                                                        completedJobId
                                                }

                                                links.add(
                                                    completedJobId +
                                                        "|||" +
                                                        selectedWalletId
                                                )

                                                prefs.edit()
                                                    .putStringSet(
                                                        "money_manager_job_links",
                                                        links.toSet()
                                                    )
                                                    .apply()

                                                android.widget.Toast
                                                    .makeText(
                                                        this@MainActivity,
                                                        "เชื่อมรายรับงานบินเข้ากระเป๋าแล้ว",
                                                        android.widget.Toast.LENGTH_SHORT
                                                    )
                                                    .show()
                                            }
                                        }
                                        .setNegativeButton(
                                            "ยังไม่เลือก",
                                            null
                                        )
                                        .show()
                                }

                                val appointmentCurrent =
                                    prefs.getStringSet(
                                        "job_appointments",
                                        emptySet()
                                    )?.toMutableSet()
                                        ?: mutableSetOf()

                                appointmentCurrent.removeAll {
                                    appointmentRecord ->

                                    appointmentRecord
                                        .split(
                                            "|||",
                                            ignoreCase = false,
                                            limit = 3
                                        )
                                        .getOrNull(0)
                                        ?.toLongOrNull() ==
                                        time
                                }

                                if (appointmentMillis > 0L) {
                                    appointmentCurrent.add(
                                        listOf(
                                            time.toString(),
                                            appointmentMillis.toString(),
                                            "false"
                                        ).joinToString("|||")
                                    )
                                }

                                prefs.edit()
                                    .putStringSet(
                                        "job_appointments",
                                        appointmentCurrent
                                    )
                                    .apply()

                                cancelJobReminder(time)

                                android.widget.Toast.makeText(
                                    this@MainActivity,
                                    "บันทึกงานเป็นเสร็จแล้ว",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()

                                reloadHistory()
                            }
                        }
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

                                    // PHASE 2C - remove wallet link
                                    val deletedJobId =
                                        parts.getOrNull(0)
                                            ?.trim()
                                            .orEmpty()

                                    if (
                                        deletedJobId.isNotBlank()
                                    ) {

                                        val jobLinks =
                                            prefs.getStringSet(
                                                "money_manager_job_links",
                                                emptySet()
                                            )?.toMutableSet()
                                                ?: mutableSetOf()

                                        jobLinks.removeAll { link ->

                                            link.split(
                                                "|||",
                                                ignoreCase = false,
                                                limit = 2
                                            ).getOrNull(0)
                                                ?.trim() ==
                                                deletedJobId
                                        }

                                        prefs.edit()
                                            .putStringSet(
                                                "money_manager_job_links",
                                                jobLinks.toSet()
                                            )
                                            .apply()
                                    }

                                    val appointmentCurrent =
                                        prefs.getStringSet(
                                            "job_appointments",
                                            emptySet()
                                        )?.toMutableSet()
                                            ?: mutableSetOf()

                                    appointmentCurrent.removeAll {
                                        appointmentRecord ->

                                        appointmentRecord
                                            .split(
                                                "|||",
                                                ignoreCase = false,
                                                limit = 3
                                            )
                                            .getOrNull(0)
                                            ?.toLongOrNull() ==
                                            time
                                    }

                                    prefs.edit()
                                        .putStringSet(
                                            "job_appointments",
                                            appointmentCurrent
                                        )
                                        .apply()

                                    cancelJobReminder(time)

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

                if (
                    status != "เสร็จแล้ว" &&
                    status != "ยกเลิก"
                ) {
                    buttonRow.addView(
                        completeButton,
                        android.widget.LinearLayout.LayoutParams(
                            0,
                            android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                            1f
                        ).apply {
                            marginEnd = dp(4)
                        }
                    )
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
                if (historySearchDate.isNullOrBlank()) {
                    "แสดง ${sortedRecords.size} จาก ${allSortedRecords.size} งาน\n" +
                    "พื้นที่รวม %.2f ไร่\n".format(totalRai) +
                    "มูลค่างานรวม %.2f บาท".format(totalValue)
                } else {
                    "ผลการค้นหา ${historySearchDate}\n" +
                    "พบ ${sortedRecords.size} งาน\n" +
                    "พื้นที่รวม %.2f ไร่\n".format(totalRai) +
                    "มูลค่างานรวม %.2f บาท".format(totalValue)
                }

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

        refreshHistoryButton.setOnClickListener {

            daySearchInput.text.clear()
            monthSearchInput.text.clear()
            yearSearchInput.text.clear()

            historySearchDate = null

            historyModeText.text =
                "แสดง 10 งานล่าสุด • ค้นหาวันที่เพื่อดูงานย้อนหลัง"

            daySearchInput.clearFocus()
            monthSearchInput.clearFocus()
            yearSearchInput.clearFocus()

            val inputMethodManager =
                getSystemService(
                    android.content.Context.INPUT_METHOD_SERVICE
                ) as android.view.inputmethod.InputMethodManager

            inputMethodManager.hideSoftInputFromWindow(
                refreshHistoryButton.windowToken,
                0
            )

            reloadHistory()

            android.widget.Toast.makeText(
                this,
                "กลับมาแสดง 10 งานล่าสุดแล้ว",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }

        searchButton.setOnClickListener {

            val dayText =
                daySearchInput.text
                    .toString()
                    .trim()

            val monthText =
                monthSearchInput.text
                    .toString()
                    .trim()

            val yearText =
                yearSearchInput.text
                    .toString()
                    .trim()

            if (
                dayText.isBlank() &&
                monthText.isBlank() &&
                yearText.isBlank()
            ) {

                historySearchDate = null

                historyModeText.text =
                    "แสดง 10 งานล่าสุด • ค้นหาวันที่เพื่อดูงานย้อนหลัง"

                reloadHistory()

                return@setOnClickListener
            }

            if (
                dayText.isBlank() ||
                monthText.isBlank() ||
                yearText.isBlank()
            ) {

                android.widget.Toast.makeText(
                    this,
                    "กรุณากรอก วัน เดือน และ ปี ให้ครบ",
                    android.widget.Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            val day =
                dayText.toIntOrNull()

            val month =
                monthText.toIntOrNull()

            var year =
                yearText.toIntOrNull()

            if (
                day == null ||
                month == null ||
                year == null
            ) {

                android.widget.Toast.makeText(
                    this,
                    "วันที่ไม่ถูกต้อง",
                    android.widget.Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            if (year < 2400) {
                year += 543
            }

            if (
                day !in 1..31 ||
                month !in 1..12
            ) {

                android.widget.Toast.makeText(
                    this,
                    "วันที่ไม่ถูกต้อง",
                    android.widget.Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            try {

                val validateCalendar =
                    java.util.Calendar.getInstance(
                        java.util.TimeZone.getTimeZone(
                            "Asia/Bangkok"
                        )
                    )

                validateCalendar.isLenient = false

                validateCalendar.set(
                    year - 543,
                    month - 1,
                    day,
                    12,
                    0,
                    0
                )

                validateCalendar.set(
                    java.util.Calendar.MILLISECOND,
                    0
                )

                validateCalendar.timeInMillis

            } catch (
                e: IllegalArgumentException
            ) {

                android.widget.Toast.makeText(
                    this,
                    "วันที่นี้ไม่มีอยู่จริง",
                    android.widget.Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            historySearchDate =
                "%02d/%02d/%04d".format(
                    day,
                    month,
                    year
                )

            historyModeText.text =
                "กำลังแสดงงานวันที่ ${historySearchDate}"

            reloadHistory()
        }

        yearSearchInput.setOnEditorActionListener {
            _, _, _ ->

            searchButton.performClick()
            true
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

        val prefs =
            getSharedPreferences(
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

        // ==========================================
        // โหลดข้อมูลนัดหมายเดิม
        // ==========================================

        val appointmentSet =
            prefs.getStringSet(
                "job_appointments",
                emptySet()
            )?.toMutableSet()
                ?: mutableSetOf()

        val oldAppointmentRecord =
            appointmentSet.firstOrNull {
                record ->

                record.split(
                    "|||",
                    ignoreCase = false,
                    limit = 3
                )
                    .getOrNull(0)
                    ?.toLongOrNull() ==
                    originalTime
            }

        val appointmentParts =
            oldAppointmentRecord
                ?.split(
                    "|||",
                    ignoreCase = false,
                    limit = 3
                )

        val oldAppointmentMillis =
            appointmentParts
                ?.getOrNull(1)
                ?.toLongOrNull()
                ?: 0L

        val oldReminderEnabled =
            appointmentParts
                ?.getOrNull(2)
                ?.toBooleanStrictOrNull()
                ?: false

        val appointmentTz =
            java.util.TimeZone.getTimeZone(
                "Asia/Bangkok"
            )

        val appointmentCal =
            java.util.Calendar
                .getInstance(appointmentTz)
                .apply {

                    if (oldAppointmentMillis > 0L) {

                        timeInMillis =
                            oldAppointmentMillis

                    } else {

                        add(
                            java.util.Calendar.DAY_OF_MONTH,
                            1
                        )

                        set(
                            java.util.Calendar.HOUR_OF_DAY,
                            8
                        )

                        set(
                            java.util.Calendar.MINUTE,
                            0
                        )

                        set(
                            java.util.Calendar.SECOND,
                            0
                        )

                        set(
                            java.util.Calendar.MILLISECOND,
                            0
                        )
                    }
                }

        var appointmentDateSelected =
            oldAppointmentMillis > 0L

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

                setText(
                    parts.getOrNull(1) ?: ""
                )
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
                android.R.layout
                    .simple_spinner_dropdown_item,
                services
            )

        val oldService =
            parts.getOrNull(2) ?: ""

        val serviceIndex =
            services.indexOf(oldService)

        if (serviceIndex >= 0) {
            serviceSpinner.setSelection(
                serviceIndex
            )
        }

        val locationInput =
            android.widget.EditText(this).apply {

                hint = "พื้นที่ / สวน"

                setText(
                    parts.getOrNull(3) ?: ""
                )
            }

        val raiInput =
            android.widget.EditText(this).apply {

                hint = "จำนวนไร่"

                inputType =
                    android.text.InputType
                        .TYPE_CLASS_NUMBER or
                    android.text.InputType
                        .TYPE_NUMBER_FLAG_DECIMAL

                setText(
                    parts.getOrNull(4) ?: ""
                )
            }

        val rateInput =
            android.widget.EditText(this).apply {

                hint = "ราคาต่อไร่"

                inputType =
                    android.text.InputType
                        .TYPE_CLASS_NUMBER or
                    android.text.InputType
                        .TYPE_NUMBER_FLAG_DECIMAL

                setText(
                    parts.getOrNull(5) ?: ""
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
                android.R.layout
                    .simple_spinner_dropdown_item,
                statuses
            )

        val oldStatus =
            parts.getOrNull(7) ?: ""

        val statusIndex =
            statuses.indexOf(oldStatus)

        if (statusIndex >= 0) {

            statusSpinner.setSelection(
                statusIndex
            )
        }

        // ==========================================
        // วัน / เวลา นัดหมาย
        // ==========================================

        val appointmentLabel =
            android.widget.TextView(this).apply {

                text =
                    "วันและเวลานัดหมาย"

                textSize = 17f

                setTypeface(
                    typeface,
                    android.graphics.Typeface.BOLD
                )

                setPadding(
                    0,
                    dp(16),
                    0,
                    dp(6)
                )
            }

        val appointmentDateButton =
            android.widget.Button(this).apply {

                isAllCaps = false
                textSize = 15f
            }

        val appointmentTimeButton =
            android.widget.Button(this).apply {

                isAllCaps = false
                textSize = 15f
            }

        fun updateDateButton() {

            appointmentDateButton.text =
                if (appointmentDateSelected) {

                    "%02d/%02d/%04d".format(
                        appointmentCal.get(
                            java.util.Calendar
                                .DAY_OF_MONTH
                        ),
                        appointmentCal.get(
                            java.util.Calendar.MONTH
                        ) + 1,
                        appointmentCal.get(
                            java.util.Calendar.YEAR
                        ) + 543
                    )

                } else {

                    "เลือกวันที่นัดหมาย"
                }
        }

        fun updateTimeButton() {

            appointmentTimeButton.text =
                "เวลา %02d:%02d น.".format(
                    appointmentCal.get(
                        java.util.Calendar
                            .HOUR_OF_DAY
                    ),
                    appointmentCal.get(
                        java.util.Calendar.MINUTE
                    )
                )
        }

        updateDateButton()
        updateTimeButton()

        appointmentDateButton
            .setOnClickListener {

                android.app.DatePickerDialog(
                    this,
                    { _, year, month, day ->

                        appointmentCal.set(
                            java.util.Calendar.YEAR,
                            year
                        )

                        appointmentCal.set(
                            java.util.Calendar.MONTH,
                            month
                        )

                        appointmentCal.set(
                            java.util.Calendar
                                .DAY_OF_MONTH,
                            day
                        )

                        appointmentDateSelected =
                            true

                        updateDateButton()
                    },
                    appointmentCal.get(
                        java.util.Calendar.YEAR
                    ),
                    appointmentCal.get(
                        java.util.Calendar.MONTH
                    ),
                    appointmentCal.get(
                        java.util.Calendar
                            .DAY_OF_MONTH
                    )
                ).show()
            }

        appointmentTimeButton
            .setOnClickListener {

                android.app.TimePickerDialog(
                    this,
                    { _, hour, minute ->

                        appointmentCal.set(
                            java.util.Calendar
                                .HOUR_OF_DAY,
                            hour
                        )

                        appointmentCal.set(
                            java.util.Calendar.MINUTE,
                            minute
                        )

                        appointmentCal.set(
                            java.util.Calendar.SECOND,
                            0
                        )

                        appointmentCal.set(
                            java.util.Calendar.MILLISECOND,
                            0
                        )

                        updateTimeButton()
                    },
                    appointmentCal.get(
                        java.util.Calendar
                            .HOUR_OF_DAY
                    ),
                    appointmentCal.get(
                        java.util.Calendar.MINUTE
                    ),
                    true
                ).show()
            }

        val appointmentButtonRow =
            android.widget.LinearLayout(this).apply {

                orientation =
                    android.widget.LinearLayout.HORIZONTAL
            }

        appointmentButtonRow.addView(
            appointmentDateButton,
            android.widget.LinearLayout.LayoutParams(
                0,
                dp(52),
                1f
            ).apply {
                marginEnd = dp(4)
            }
        )

        appointmentButtonRow.addView(
            appointmentTimeButton,
            android.widget.LinearLayout.LayoutParams(
                0,
                dp(52),
                1f
            ).apply {
                marginStart = dp(4)
            }
        )

        val reminderSwitch =
            android.widget.Switch(this).apply {

                text =
                    "เปิดการแจ้งเตือนสำหรับงานนี้"

                textSize = 16f

                isChecked =
                    oldReminderEnabled

                setPadding(
                    0,
                    dp(8),
                    0,
                    dp(8)
                )
            }

        val noteInput =
            android.widget.EditText(this).apply {

                hint = "หมายเหตุ"

                minLines = 2

                setText(
                    parts.getOrNull(8) ?: ""
                )
            }

        form.addView(customerInput)
        form.addView(serviceSpinner)
        form.addView(locationInput)
        form.addView(raiInput)
        form.addView(rateInput)
        form.addView(statusSpinner)

        form.addView(
            appointmentLabel
        )

        form.addView(
            appointmentButtonRow
        )

        form.addView(
            reminderSwitch
        )

        form.addView(
            noteInput
        )

        val scroll =
            android.widget.ScrollView(this).apply {

                addView(form)
            }

        val dialog =
            android.app.AlertDialog.Builder(this)
                .setTitle(
                    "แก้ไขงาน"
                )
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
                    android.app.AlertDialog
                        .BUTTON_POSITIVE
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

                if (
                    rai == null ||
                    rai <= 0.0
                ) {

                    raiInput.error =
                        "จำนวนไร่ไม่ถูกต้อง"

                    return@setOnClickListener
                }

                if (
                    rate == null ||
                    rate < 0.0
                ) {

                    rateInput.error =
                        "ราคาต่อไร่ไม่ถูกต้อง"

                    return@setOnClickListener
                }

                if (!appointmentDateSelected) {

                    android.widget.Toast
                        .makeText(
                            this@MainActivity,
                            "กรุณาเลือกวันที่นัดหมาย",
                            android.widget.Toast.LENGTH_SHORT
                        )
                        .show()

                    return@setOnClickListener
                }

                val service =
                    serviceSpinner
                        .selectedItem
                        .toString()

                val status =
                    statusSpinner
                        .selectedItem
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
                    ).joinToString(
                        "|||"
                    )

                val currentJobs =
                    prefs.getStringSet(
                        "flight_jobs",
                        emptySet()
                    )?.toMutableSet()
                        ?: mutableSetOf()

                currentJobs.remove(
                    originalRecord
                )

                currentJobs.add(
                    updatedRecord
                )

                prefs.edit()
                    .putStringSet(
                        "flight_jobs",
                        currentJobs
                    )
                    .apply()

                // ======================================
                // อัปเดต appointment metadata
                // ======================================

                val currentAppointments =
                    prefs.getStringSet(
                        "job_appointments",
                        emptySet()
                    )?.toMutableSet()
                        ?: mutableSetOf()

                currentAppointments.removeAll {
                    appointmentRecord ->

                    appointmentRecord
                        .split(
                            "|||",
                            ignoreCase = false,
                            limit = 3
                        )
                        .getOrNull(0)
                        ?.toLongOrNull() ==
                        originalTime
                }

                val newAppointmentRecord =
                    listOf(
                        originalTime.toString(),
                        appointmentCal
                            .timeInMillis
                            .toString(),
                        reminderSwitch
                            .isChecked
                            .toString()
                    ).joinToString(
                        "|||"
                    )

                currentAppointments.add(
                    newAppointmentRecord
                )

                prefs.edit()
                    .putStringSet(
                        "job_appointments",
                        currentAppointments
                    )
                    .apply()

                // ======================================
                // ยกเลิก Alarm เก่า
                // แล้วตั้ง Alarm ใหม่
                // ======================================

                cancelJobReminder(
                    originalTime
                )

                if (
                    reminderSwitch.isChecked &&
                    status != "เสร็จแล้ว" &&
                    status != "ยกเลิก"
                ) {

                    scheduleJobReminder(
                        jobId =
                            originalTime,
                        appointmentMillis =
                            appointmentCal
                                .timeInMillis,
                        customer =
                            customer,
                        service =
                            service,
                        location =
                            location,
                        rai =
                            rai
                    )
                }

                android.widget.Toast
                    .makeText(
                        this@MainActivity,
                        "แก้ไขงานและนัดหมายเรียบร้อย",
                        android.widget.Toast.LENGTH_SHORT
                    )
                    .show()

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


        // =================================================
        // PHASE 2B - WALLET SELECTOR
        // =================================================

        data class FinanceWallet(
            val id: String,
            val name: String
        )

        fun loadFinanceWallets(): List<FinanceWallet> {

            val raw =
                prefs.getStringSet(
                    "money_manager_wallets",
                    emptySet()
                )?.toList()
                    ?: emptyList()

            return raw.mapNotNull { record ->

                val parts =
                    record.split(
                        "|||",
                        ignoreCase = false,
                        limit = 5
                    )

                val id =
                    parts.getOrNull(0)
                        ?.trim()
                        .orEmpty()

                val name =
                    parts.getOrNull(1)
                        ?.trim()
                        .orEmpty()

                if (
                    id.isBlank() ||
                    name.isBlank()
                ) {
                    null
                } else {
                    FinanceWallet(
                        id = id,
                        name = name
                    )
                }
            }.sortedBy {
                it.name
            }
        }

        val financeWallets =
            loadFinanceWallets()

        val walletSpinner =
            android.widget.Spinner(this)

        val walletNames =
            if (financeWallets.isEmpty()) {
                listOf("ยังไม่มีกระเป๋า")
            } else {
                financeWallets.map {
                    it.name
                }
            }

        walletSpinner.adapter =
            android.widget.ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                walletNames
            )

        val walletLabel =
            android.widget.TextView(this).apply {

                text = "กระเป๋าเงิน"

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
        root.addView(walletLabel)
        root.addView(walletSpinner)
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

                                    val deletedTransactionId =
                                        record.split(
                                            "|||",
                                            ignoreCase = false,
                                            limit = 2
                                        ).getOrNull(0)
                                            ?.trim()
                                            .orEmpty()

                                    if (
                                        deletedTransactionId.isNotBlank()
                                    ) {

                                        val links =
                                            prefs.getStringSet(
                                                "money_manager_links",
                                                emptySet()
                                            )?.toMutableSet()
                                                ?: mutableSetOf()

                                        links.removeAll { link ->

                                            link.split(
                                                "|||",
                                                ignoreCase = false,
                                                limit = 2
                                            ).getOrNull(0)
                                                ?.trim() ==
                                                deletedTransactionId
                                        }

                                        prefs.edit()
                                            .putStringSet(
                                                "money_manager_links",
                                                links.toSet()
                                            )
                                            .apply()
                                    }

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

                    // PHASE 2B:
                    // ผูกรายการเงินกับกระเป๋า โดยไม่แก้ schema เดิม
                    if (financeWallets.isNotEmpty()) {

                        val walletIndex =
                            walletSpinner.selectedItemPosition
                                .coerceIn(
                                    0,
                                    financeWallets.lastIndex
                                )

                        val walletId =
                            financeWallets[
                                walletIndex
                            ].id

                        val transactionId =
                            record.split(
                                "|||",
                                ignoreCase = false,
                                limit = 2
                            ).getOrNull(0)
                                ?.trim()
                                .orEmpty()

                        if (
                            transactionId.isNotBlank() &&
                            walletId.isNotBlank()
                        ) {

                            val links =
                                prefs.getStringSet(
                                    "money_manager_links",
                                    emptySet()
                                )?.toMutableSet()
                                    ?: mutableSetOf()

                            links.removeAll { link ->

                                link.split(
                                    "|||",
                                    ignoreCase = false,
                                    limit = 2
                                ).getOrNull(0)
                                    ?.trim() ==
                                    transactionId
                            }

                            links.add(
                                transactionId +
                                    "|||" +
                                    walletId
                            )

                            prefs.edit()
                                .putStringSet(
                                    "money_manager_links",
                                    links.toSet()
                                )
                                .apply()
                        }
                    }

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
