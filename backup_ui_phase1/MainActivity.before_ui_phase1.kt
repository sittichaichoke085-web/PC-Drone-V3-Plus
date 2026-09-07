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

        // =================================================
        // JOB DATA
        // =================================================

        val jobs =
            prefs.getStringSet(
                "flight_jobs",
                emptySet()
            )?.toList()
                ?: emptyList()

        var completedJobIncome =
            java.math.BigDecimal.ZERO

        var completedJobs = 0
        var waitingJobs = 0
        var cancelledJobs = 0

        jobs.forEach { record ->

            val parts =
                record.split(
                    "|||",
                    ignoreCase = false,
                    limit = 9
                )

            if (parts.size >= 8) {

                val total =
                    money(
                        parts.getOrNull(6)
                    )

                val status =
                    parts.getOrNull(7)
                        ?.trim()
                        ?: ""

                when (status) {

                    "เสร็จแล้ว" -> {

                        completedJobs++

                        completedJobIncome =
                            completedJobIncome.add(
                                total
                            )
                    }

                    "ยกเลิก" -> {
                        cancelledJobs++
                    }

                    else -> {
                        waitingJobs++
                    }
                }
            }
        }

        // =================================================
        // FINANCE DATA
        // =================================================

        val transactions =
            prefs.getStringSet(
                "finance_transactions",
                emptySet()
            )?.toList()
                ?: emptyList()

        var otherIncome =
            java.math.BigDecimal.ZERO

        var expenses =
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
                        ?.trim()
                        ?: ""

                val amount =
                    money(
                        parts.getOrNull(3)
                    )

                when (type) {

                    "INCOME" -> {
                        otherIncome =
                            otherIncome.add(
                                amount
                            )
                    }

                    "EXPENSE" -> {
                        expenses =
                            expenses.add(
                                amount
                            )
                    }
                }
            }
        }

        val totalIncome =
            completedJobIncome.add(
                otherIncome
            )

        val balance =
            totalIncome.subtract(
                expenses
            )

        // =================================================
        // UI
        // =================================================

        val content =
            android.widget.LinearLayout(this).apply {

                orientation =
                    android.widget.LinearLayout.VERTICAL

                setPadding(
                    dp(20),
                    dp(24),
                    dp(20),
                    dp(28)
                )

                setBackgroundColor(
                    android.graphics.Color.WHITE
                )
            }

        content.addView(
            android.widget.TextView(this).apply {

                text = "PC DRONE"

                textSize = 32f

                setTextColor(
                    greenDark
                )

                gravity =
                    android.view.Gravity.CENTER

                setTypeface(
                    typeface,
                    android.graphics.Typeface.BOLD
                )
            }
        )

        content.addView(
            android.widget.TextView(this).apply {

                text = "PC Drone V3 Plus"

                textSize = 18f

                setTextColor(
                    black
                )

                gravity =
                    android.view.Gravity.CENTER

                setPadding(
                    0,
                    dp(4),
                    0,
                    dp(20)
                )
            }
        )

        // =================================================
        // BALANCE
        // =================================================

        val balanceCard =
            summaryCard(
                "เงินคงเหลือ",
                "${moneyFormat.format(balance)} บาท"
            )

        content.addView(
            balanceCard
        )

        // =================================================
        // JOB INCOME
        // =================================================

        content.addView(
            summaryCard(
                "รายรับจากงานที่เสร็จแล้ว",
                "${moneyFormat.format(completedJobIncome)} บาท"
            )
        )

        // =================================================
        // EXPENSE
        // =================================================

        content.addView(
            summaryCard(
                "รายจ่ายรวม",
                "${moneyFormat.format(expenses)} บาท"
            )
        )

        // =================================================
        // OTHER INCOME
        // =================================================

        content.addView(
            summaryCard(
                "รายรับอื่น",
                "${moneyFormat.format(otherIncome)} บาท"
            )
        )

        // =================================================
        // JOB STATUS
        // =================================================

        content.addView(
            summaryCard(
                "งานรอดำเนินการ",
                "$waitingJobs งาน"
            )
        )

        content.addView(
            summaryCard(
                "งานเสร็จแล้ว",
                "$completedJobs งาน"
            )
        )

        content.addView(
            summaryCard(
                "งานทั้งหมด",
                "${jobs.size} งาน"
            )
        )

        if (cancelledJobs > 0) {

            content.addView(
                android.widget.TextView(this).apply {

                    text =
                        "งานยกเลิก: $cancelledJobs งาน"

                    textSize = 14f

                    setTextColor(
                        android.graphics.Color.DKGRAY
                    )

                    setPadding(
                        dp(4),
                        dp(8),
                        dp(4),
                        dp(4)
                    )
                }
            )
        }

        // =================================================
        // SYSTEM RELATION INFO
        // =================================================

        content.addView(
            android.widget.TextView(this).apply {

                text =
                    "รายรับรวม = งานที่เสร็จแล้ว + รายรับอื่น\n" +
                    "เงินคงเหลือ = รายรับรวม - รายจ่าย"

                textSize = 14f

                setTextColor(
                    android.graphics.Color.DKGRAY
                )

                setPadding(
                    dp(4),
                    dp(14),
                    dp(4),
                    dp(8)
                )
            }
        )

        // =================================================
        // MENU
        // =================================================

        addMenuButton(
            content,
            AppRoute.JOBS
        )

        addMenuButton(
            content,
            AppRoute.CUSTOMERS
        )

        addMenuButton(
            content,
            AppRoute.FINANCE
        )

        addMenuButton(
            content,
            AppRoute.HISTORY
        )

        addMenuButton(
            content,
            AppRoute.SETTINGS
        )

        val scrollView =
            android.widget.ScrollView(this).apply {

                addView(
                    content,
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

    private fun showPlaceholder(route: AppRoute) {
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
        val root = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(dp(20), dp(20), dp(20), dp(20))
            setBackgroundColor(android.graphics.Color.WHITE)
        }

        root.addView(createBackButton())

        root.addView(android.widget.TextView(this).apply {
            text = "ลูกค้า"
            textSize = 28f
            setTextColor(android.graphics.Color.BLACK)
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        })

        root.addView(android.widget.TextView(this).apply {
            text = "เพิ่มและจัดการข้อมูลลูกค้าสำหรับงานบิน"
            textSize = 16f
            setTextColor(android.graphics.Color.DKGRAY)
            setPadding(0, dp(6), 0, dp(16))
        })

        val nameInput = android.widget.EditText(this).apply {
            hint = "ชื่อลูกค้า"
            setTextColor(android.graphics.Color.BLACK)
            setHintTextColor(android.graphics.Color.GRAY)
        }

        val phoneInput = android.widget.EditText(this).apply {
            hint = "เบอร์โทร"
            inputType = android.text.InputType.TYPE_CLASS_PHONE
            setTextColor(android.graphics.Color.BLACK)
            setHintTextColor(android.graphics.Color.GRAY)
        }

        val areaInput = android.widget.EditText(this).apply {
            hint = "พื้นที่ / สวน / ตำบล"
            setTextColor(android.graphics.Color.BLACK)
            setHintTextColor(android.graphics.Color.GRAY)
        }

        root.addView(nameInput)
        root.addView(phoneInput)
        root.addView(areaInput)

        val customerList = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(0, dp(16), 0, 0)
        }

        val addButton = android.widget.Button(this).apply {
            text = "บันทึกลูกค้า"
            textSize = 18f

            setOnClickListener {
                val name = nameInput.text.toString().trim()
                val phone = phoneInput.text.toString().trim()
                val area = areaInput.text.toString().trim()

                if (name.isEmpty()) {
                    nameInput.error = "กรุณากรอกชื่อลูกค้า"
                    return@setOnClickListener
                }

                customerList.addView(
                    android.widget.TextView(this@MainActivity).apply {
                        text = "ชื่อลูกค้า: $name\nเบอร์โทร: ${if (phone.isEmpty()) "-" else phone}\nพื้นที่: ${if (area.isEmpty()) "-" else area}"
                        textSize = 17f
                        setTextColor(android.graphics.Color.BLACK)
                        setPadding(dp(12), dp(12), dp(12), dp(12))
                    }
                )

                nameInput.text.clear()
                phoneInput.text.clear()
                areaInput.text.clear()
            }
        }

        root.addView(addButton)
        root.addView(customerList)

        val scrollView = android.widget.ScrollView(this).apply {
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

            text = "← ย้อนกลับ"
            textSize = 18f
            isAllCaps = false

            setTextColor(
                android.graphics.Color.WHITE
            )

            setBackgroundColor(
                greenDark
            )

            setOnClickListener {
                showDashboard()
            }

            layoutParams =
                android.widget.LinearLayout.LayoutParams(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    dp(54)
                ).apply {
                    bottomMargin = dp(14)
                }
        }
    }


    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}
