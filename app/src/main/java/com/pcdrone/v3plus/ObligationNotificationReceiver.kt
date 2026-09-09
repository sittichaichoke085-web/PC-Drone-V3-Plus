package com.pcdrone.v3plus

class ObligationNotificationReceiver :
    android.content.BroadcastReceiver() {

    override fun onReceive(
        context: android.content.Context,
        intent: android.content.Intent
    ) {
        // PC_DRONE_OBLIGATION_NOTIFICATION_SETTINGS
        val settings =
            context.getSharedPreferences(
                "pc_drone_settings",
                android.content.Context.MODE_PRIVATE
            )

        if (
            !settings.getBoolean(
                "notifications_enabled",
                true
            )
        ) {
            return
        }

        val soundEnabled =
            settings.getBoolean(
                "sound_enabled",
                true
            ) &&
            settings.getBoolean(
                "sound_job_notification",
                true
            )

        val vibrationEnabled =
            settings.getBoolean(
                "notification_vibration",
                true
            )

        val savedSound =
            settings.getString(
                "notification_sound_uri",
                null
            )

        val soundUri =
            if (soundEnabled) {
                if (savedSound.isNullOrBlank()) {
                    android.provider.Settings.System
                        .DEFAULT_NOTIFICATION_URI
                } else {
                    android.net.Uri.parse(savedSound)
                }
            } else {
                null
            }

        val monthItemId =
            intent.getStringExtra("month_item_id")
                ?: return

        val name =
            intent.getStringExtra("name")
                ?: "รายการที่ต้องชำระ"

        val planned =
            intent.getStringExtra("planned")
                ?.toBigDecimalOrNull()
                ?: return

        val data =
            context.getSharedPreferences(
                "pc_drone_v3_data",
                android.content.Context.MODE_PRIVATE
            )

        val payments =
            data.getStringSet(
                "money_manager_obligation_payments",
                emptySet()
            ).orEmpty()

        val paid =
            payments.fold(
                java.math.BigDecimal.ZERO
            ) { total, raw ->
                val p = raw.split(
                    "|||",
                    ignoreCase = false,
                    limit = 5
                )

                if (
                    p.size == 5 &&
                    p[1] == monthItemId
                ) {
                    total.add(
                        p[2].toBigDecimalOrNull()
                            ?: java.math.BigDecimal.ZERO
                    )
                } else {
                    total
                }
            }

        val remaining =
            planned.subtract(paid).max(
                java.math.BigDecimal.ZERO
            )

        // PC_DRONE_OBLIGATION_NOTIFICATION_PAID_GUARD
        if (
            remaining.compareTo(
                java.math.BigDecimal.ZERO
            ) <= 0
        ) {
            return
        }

        val kind =
            intent.getStringExtra("notification_kind")
                ?: "DUE"

        val money =
            java.text.NumberFormat.getNumberInstance(
                java.util.Locale("th", "TH")
            ).format(remaining)

        val title =
            when (kind) {
                "BEFORE" -> "พรุ่งนี้ครบกำหนดชำระ"
                "OVERDUE" -> "รายการค้างชำระ"
                else -> "วันนี้ครบกำหนดชำระ"
            }

        val message =
            "$name • คงเหลือ $money บาท"

        val channelIdentity =
            (soundUri?.toString() ?: "silent") +
                "_" +
                vibrationEnabled

        val channelId =
            "pc_drone_obligation_" +
                channelIdentity.hashCode()
                    .toUInt()
                    .toString()

        val manager =
            context.getSystemService(
                android.content.Context.NOTIFICATION_SERVICE
            ) as android.app.NotificationManager

        if (
            android.os.Build.VERSION.SDK_INT >=
            android.os.Build.VERSION_CODES.O
        ) {
            val channel =
                android.app.NotificationChannel(
                    channelId,
                    "แจ้งเตือนรายการที่ต้องจ่าย",
                    android.app.NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description =
                        "แจ้งเตือนวันครบกำหนดและรายการค้างชำระ"

                    if (vibrationEnabled) {
                        enableVibration(true)
                    } else {
                        enableVibration(false)
                    }

                    if (soundUri != null) {
                        val audioAttributes =
                            android.media.AudioAttributes
                                .Builder()
                                .setUsage(
                                    android.media.AudioAttributes
                                        .USAGE_NOTIFICATION
                                )
                                .setContentType(
                                    android.media.AudioAttributes
                                        .CONTENT_TYPE_SONIFICATION
                                )
                                .build()

                        setSound(
                            soundUri,
                            audioAttributes
                        )
                    } else {
                        setSound(
                            null,
                            null
                        )
                    }
                }

            manager.createNotificationChannel(channel)
        }

        val openIntent =
            android.content.Intent(
                context,
                MainActivity::class.java
            ).apply {
                flags =
                    android.content.Intent.FLAG_ACTIVITY_NEW_TASK or
                    android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

        val contentIntent =
            android.app.PendingIntent.getActivity(
                context,
                monthItemId.hashCode() and 0x7fffffff,
                openIntent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or
                    android.app.PendingIntent.FLAG_IMMUTABLE
            )

        val notification =
            android.app.Notification.Builder(
                context,
                channelId
            )
                .setSmallIcon(
                    android.R.drawable.ic_popup_reminder
                )
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(
                    android.app.Notification.BigTextStyle()
                        .bigText(message)
                )
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .setCategory(
                    android.app.Notification.CATEGORY_REMINDER
                )
                .build()

        manager.notify(
            (
                "obligation_" +
                    monthItemId +
                    "_" +
                    kind
            ).hashCode() and 0x7fffffff,
            notification
        )
    }
}
