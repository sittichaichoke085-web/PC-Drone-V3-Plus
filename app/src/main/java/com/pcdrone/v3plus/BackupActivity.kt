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

    @Deprecated("ใช้เพื่อรองรับโครงสร้าง Activity ปัจจุบัน")
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
            requestCode == 8402 &&
            resultCode == RESULT_OK
        ) {

            val saveUri =
                data?.data
                    ?: return

            try {

                BackupManager
                    .writeBackupToUri(
                        this,
                        saveUri
                    )

                Toast.makeText(
                    this,
                    "สำรองข้อมูลทั้งแอปสำเร็จ",
                    Toast.LENGTH_LONG
                ).show()

            } catch (
                e: Exception
            ) {

                Toast.makeText(
                    this,
                    "สำรองข้อมูลไม่สำเร็จ: " +
                        (
                            e.message
                                ?: "ไม่ทราบสาเหตุ"
                        ),
                    Toast.LENGTH_LONG
                ).show()
            }

            return
        }

        if (
            requestCode != 8401 ||
            resultCode != RESULT_OK
        ) {
            return
        }

        val uri =
            data?.data
                ?: return

        try {

            val jsonText =
                contentResolver
                    .openInputStream(
                        uri
                    )
                    ?.bufferedReader(
                        Charsets.UTF_8
                    )
                    ?.use {
                        it.readText()
                    }
                    ?: throw IllegalArgumentException(
                        "เปิดไฟล์ไม่ได้"
                    )

            android.app.AlertDialog.Builder(
                this
            )
                .setTitle(
                    "ยืนยันกู้คืนข้อมูล"
                )
                .setMessage(
                    "ข้อมูลในแอปปัจจุบันจะถูกแทนที่ด้วยข้อมูลจากไฟล์สำรอง\\n\\nระบบจะตรวจสอบไฟล์ก่อน และสร้างสำเนาข้อมูลปัจจุบันไว้เพื่อความปลอดภัย"
                )
                .setNegativeButton(
                    "ยกเลิก",
                    null
                )
                .setPositiveButton(
                    "กู้คืน"
                ) { _, _ ->

                    try {

                        val previousJobIds =
                            JobReminderRestoreManager
                                .currentJobIds(this)

                        BackupManager
                            .restoreBackup(
                                this,
                                jsonText
                            )

                        JobReminderRestoreManager
                            .rescheduleAfterRestore(
                                this,
                                previousJobIds
                            )

                        Toast.makeText(
                            this,
                            "กู้คืนข้อมูลสำเร็จ",
                            Toast.LENGTH_LONG
                        ).show()

                        val intent =
                            android.content.Intent(
                                this,
                                MainActivity::class.java
                            ).apply {

                                addFlags(
                                    android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                        android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                                )
                            }

                        startActivity(
                            intent
                        )

                        finish()

                    } catch (
                        e: Exception
                    ) {

                        Toast.makeText(
                            this,
                            "กู้คืนไม่สำเร็จ: " +
                                (
                                    e.message
                                        ?: "ไฟล์ไม่ถูกต้อง"
                                    ),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
                .show()

        } catch (
            e: Exception
        ) {

            Toast.makeText(
                this,
                "อ่านไฟล์ไม่สำเร็จ: " +
                    (
                        e.message
                            ?: "ไฟล์ไม่ถูกต้อง"
                        ),
                Toast.LENGTH_LONG
            ).show()
        }
    }



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
                    "สำรองข้อมูลทั้งหมดของ PC Drone ไว้ในไฟล์เดียว ทั้งลูกค้า งานบิน ประวัติ นัดหมาย การเงิน และการตั้งค่า"

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
                    "สำรองข้อมูลทั้งแอป"

                textSize = 17f

                setTextColor(
                    Color.WHITE
                )

                setBackgroundColor(
                    green
                )

                setOnClickListener {

                    val fileName =
                        "PC-Drone-Full-Backup-" +
                            java.text.SimpleDateFormat(
                                "yyyyMMdd-HHmmss",
                                java.util.Locale.US
                            ).apply {
                                timeZone =
                                    java.util.TimeZone.getTimeZone(
                                        "Asia/Bangkok"
                                    )
                            }.format(
                                java.util.Date()
                            ) +
                            ".pcdrone"

                    val intent =
                        android.content.Intent(
                            android.content.Intent.ACTION_CREATE_DOCUMENT
                        ).apply {

                            addCategory(
                                android.content.Intent.CATEGORY_OPENABLE
                            )

                            type =
                                "application/octet-stream"

                            putExtra(
                                android.content.Intent.EXTRA_TITLE,
                                fileName
                            )
                        }

                    startActivityForResult(
                        intent,
                        8402
                    )
                }
            }

        root.addView(
            backupButton,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(58)
            )
        )

        val restoreButton =
            Button(this).apply {

                text =
                    "กู้คืนจากไฟล์สำรอง"

                textSize = 17f

                setTextColor(
                    Color.WHITE
                )

                setBackgroundColor(
                    Color.rgb(
                        65,
                        65,
                        65
                    )
                )

                setOnClickListener {

                    val intent =
                        android.content.Intent(
                            android.content.Intent.ACTION_OPEN_DOCUMENT
                        ).apply {

                            addCategory(
                                android.content.Intent.CATEGORY_OPENABLE
                            )

                            type =
                                "application/octet-stream"

                            putExtra(
                                android.content.Intent.EXTRA_MIME_TYPES,
                                arrayOf(
                                    "application/octet-stream",
                                    "application/json",
                                    "text/plain"
                                )
                            )
                        }

                    startActivityForResult(
                        intent,
                        8401
                    )
                }
            }

        root.addView(
            restoreButton,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(58)
            ).apply {
                topMargin =
                    dp(12)
            }
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
