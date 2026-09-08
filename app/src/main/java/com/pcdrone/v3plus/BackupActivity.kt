package com.pcdrone.v3plus

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class BackupActivity : Activity() {

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density)
            .toInt()

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(savedInstanceState)

        val dark =
            Color.rgb(17, 17, 17)

        val green =
            Color.rgb(0, 145, 70)

        val root =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL

                setPadding(
                    dp(20),
                    dp(24),
                    dp(20),
                    dp(30)
                )

                setBackgroundColor(
                    Color.WHITE
                )
            }

        root.addView(
            TextView(this).apply {

                text = "‹  กลับ"

                textSize = 17f

                setTextColor(green)

                setPadding(
                    0,
                    dp(10),
                    0,
                    dp(14)
                )

                setOnClickListener {
                    finish()
                }
            }
        )

        root.addView(
            TextView(this).apply {

                text =
                    "สำรองข้อมูล"

                textSize = 28f

                setTextColor(dark)

                setTypeface(
                    typeface,
                    android.graphics.Typeface.BOLD
                )
            }
        )

        root.addView(
            TextView(this).apply {

                text =
                    "เก็บข้อมูลลูกค้า งานบิน นัดหมาย การเงิน และการตั้งค่าของ PC Drone ไว้ในไฟล์เดียว"

                textSize = 15f

                setTextColor(
                    Color.DKGRAY
                )

                setPadding(
                    0,
                    dp(8),
                    0,
                    dp(24)
                )
            }
        )

        val backupButton =
            Button(this).apply {

                text =
                    "สร้างและแชร์ไฟล์สำรอง"

                textSize = 17f

                setTextColor(
                    Color.WHITE
                )

                setBackgroundColor(
                    green
                )

                setOnClickListener {

                    try {

                        BackupManager
                            .shareBackup(
                                this@BackupActivity
                            )

                    } catch (
                        e: Exception
                    ) {

                        Toast.makeText(
                            this@BackupActivity,
                            "สำรองข้อมูลไม่สำเร็จ: " +
                                (e.message ?: "ไม่ทราบสาเหตุ"),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }

        root.addView(
            backupButton,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(58)
            )
        )

        root.addView(
            TextView(this).apply {

                text =
                    "ไฟล์นามสกุล .pcdrone สามารถเก็บไว้ในโทรศัพท์ ไดรฟ์ หรือส่งไปยังอุปกรณ์อื่นได้"

                textSize = 13f

                gravity =
                    Gravity.CENTER

                setTextColor(
                    Color.GRAY
                )

                setPadding(
                    0,
                    dp(18),
                    0,
                    0
                )
            }
        )

        setContentView(root)
    }
}
