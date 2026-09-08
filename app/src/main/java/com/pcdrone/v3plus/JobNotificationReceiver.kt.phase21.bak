package com.pcdrone.v3plus

class JobNotificationReceiver :
    android.content.BroadcastReceiver() {

    companion object {
        const val CHANNEL_SOUND =
            "pc_drone_job_sound_v1"

        const val CHANNEL_SILENT =
            "pc_drone_job_silent_v1"
    }

    override fun onReceive(
        context: android.content.Context,
        intent: android.content.Intent
    ) {

        val settings =
            context.getSharedPreferences(
                "pc_drone_settings",
                android.content.Context.MODE_PRIVATE
            )

        val notificationsEnabled =
            settings.getBoolean(
                "notifications_enabled",
                true
            )

        if (!notificationsEnabled) {
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

        createChannels(context)

        val jobId =
            intent.getLongExtra(
                "job_id",
                System.currentTimeMillis()
            )

        val customer =
            intent.getStringExtra("customer")
                ?: "-"

        val service =
            intent.getStringExtra("service")
                ?: "-"

        val location =
            intent.getStringExtra("location")
                ?: "-"

        val rai =
            intent.getDoubleExtra(
                "rai",
                0.0
            )

        val appointmentMillis =
            intent.getLongExtra(
                "appointment_millis",
                0L
            )

        val tz =
            java.util.TimeZone.getTimeZone(
                "Asia/Bangkok"
            )

        val dateFormat =
            java.text.SimpleDateFormat(
                "dd/MM/yyyy HH:mm",
                java.util.Locale("th", "TH")
            ).apply {
                timeZone = tz
            }

        val appointmentText =
            if (appointmentMillis > 0L) {
                dateFormat.format(
                    java.util.Date(
                        appointmentMillis
                    )
                )
            } else {
                "-"
            }

        val title =
            "PC Drone • เตือนงานบิน"

        val detail =
            buildString {
                append("ลูกค้า: ")
                append(customer)

                append("\nงาน: ")
                append(service)

                append("\nนัดหมาย: ")
                append(appointmentText)
                append(" น.")

                if (location.isNotBlank()) {
                    append("\nพื้นที่: ")
                    append(location)
                }

                if (rai > 0.0) {
                    append("\nจำนวน: ")
                    append(
                        "%.2f ไร่".format(rai)
                    )
                }
            }

        val openAppIntent =
            android.content.Intent(
                context,
                MainActivity::class.java
            ).apply {

                flags =
                    android.content.Intent.FLAG_ACTIVITY_NEW_TASK or
                    android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

        val contentPendingIntent =
            android.app.PendingIntent.getActivity(
                context,
                notificationId(jobId),
                openAppIntent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or
                    android.app.PendingIntent.FLAG_IMMUTABLE
            )

        val channelId =
            if (soundEnabled) {
                CHANNEL_SOUND
            } else {
                CHANNEL_SILENT
            }

        val notification =
            android.app.Notification.Builder(
                context,
                channelId
            )
                .setSmallIcon(
                    android.R.drawable.ic_popup_reminder
                )
                .setContentTitle(title)
                .setContentText(
                    "$customer • $appointmentText"
                )
                .setStyle(
                    android.app.Notification.BigTextStyle()
                        .bigText(detail)
                )
                .setContentIntent(
                    contentPendingIntent
                )
                .setAutoCancel(true)
                .setCategory(
                    android.app.Notification.CATEGORY_REMINDER
                )
                .setVisibility(
                    android.app.Notification.VISIBILITY_PUBLIC
                )
                .build()

        val manager =
            context.getSystemService(
                android.content.Context.NOTIFICATION_SERVICE
            ) as android.app.NotificationManager

        manager.notify(
            notificationId(jobId),
            notification
        )
    }

    private fun notificationId(
        jobId: Long
    ): Int {

        return (
            jobId xor
                (jobId ushr 32)
            ).toInt() and 0x7fffffff
    }

    private fun createChannels(
        context: android.content.Context
    ) {

        val manager =
            context.getSystemService(
                android.content.Context.NOTIFICATION_SERVICE
            ) as android.app.NotificationManager

        val audioAttributes =
            android.media.AudioAttributes.Builder()
                .setUsage(
                    android.media.AudioAttributes.USAGE_NOTIFICATION
                )
                .setContentType(
                    android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION
                )
                .build()

        val soundChannel =
            android.app.NotificationChannel(
                CHANNEL_SOUND,
                "แจ้งเตือนงานบิน",
                android.app.NotificationManager.IMPORTANCE_HIGH
            ).apply {

                description =
                    "แจ้งเตือนวันและเวลางานบิน PC Drone"

                enableVibration(true)

                vibrationPattern =
                    longArrayOf(
                        0,
                        350,
                        200,
                        350
                    )

                setSound(
                    android.provider.Settings.System
                        .DEFAULT_NOTIFICATION_URI,
                    audioAttributes
                )

                lockscreenVisibility =
                    android.app.Notification.VISIBILITY_PUBLIC
            }

        val silentChannel =
            android.app.NotificationChannel(
                CHANNEL_SILENT,
                "แจ้งเตือนงานบินแบบไม่มีเสียง",
                android.app.NotificationManager.IMPORTANCE_HIGH
            ).apply {

                description =
                    "แจ้งเตือนงานบินโดยไม่เล่นเสียง"

                setSound(
                    null,
                    null
                )

                // ถึงปิดเสียง แต่ยังให้สั่น
                enableVibration(true)

                vibrationPattern =
                    longArrayOf(
                        0,
                        300,
                        200,
                        300
                    )

                lockscreenVisibility =
                    android.app.Notification.VISIBILITY_PUBLIC
            }

        manager.createNotificationChannel(
            soundChannel
        )

        manager.createNotificationChannel(
            silentChannel
        )
    }
}
