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

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}
