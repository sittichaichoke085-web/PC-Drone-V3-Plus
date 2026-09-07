package com.pcdrone.v3plus

class JobNotificationReceiver :
    android.content.BroadcastReceiver() {

    override fun onReceive(
        context: android.content.Context,
        intent: android.content.Intent
    ) {

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
            if (
                soundEnabled
            ) {
                if (
                    savedSound.isNullOrBlank()
                ) {
                    android.provider.Settings.System
                        .DEFAULT_NOTIFICATION_URI
                } else {
                    android.net.Uri.parse(
                        savedSound
                    )
                }
            } else {
                null
            }

        val channelId =
            createChannel(
                context,
                soundUri,
                vibrationEnabled
            )

        val jobId =
            intent.getLongExtra(
                "job_id",
                System.currentTimeMillis()
            )

        val customer =
            intent.getStringExtra(
                "customer"
            ) ?: "-"

        val service =
            intent.getStringExtra(
                "service"
            ) ?: "-"

        val location =
            intent.getStringExtra(
                "location"
            ) ?: "-"

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

        val format =
            java.text.SimpleDateFormat(
                "dd/MM/yyyy HH:mm",
                java.util.Locale(
                    "th",
                    "TH"
                )
            ).apply {
                timeZone = tz
            }

        val appointmentText =
            if (
                appointmentMillis > 0L
            ) {
                format.format(
                    java.util.Date(
                        appointmentMillis
                    )
                )
            } else {
                "-"
            }

        val detail =
            buildString {

                append(
                    "ลูกค้า: $customer"
                )

                append(
                    "\nงาน: $service"
                )

                append(
                    "\nนัดหมาย: $appointmentText น."
                )

                if (
                    location.isNotBlank()
                ) {
                    append(
                        "\nพื้นที่: $location"
                    )
                }

                if (
                    rai > 0.0
                ) {
                    append(
                        "\nจำนวน: %.2f ไร่"
                            .format(rai)
                    )
                }
            }

        val openIntent =
            android.content.Intent(
                context,
                MainActivity::class.java
            ).apply {

                flags =
                    android.content.Intent
                        .FLAG_ACTIVITY_NEW_TASK or
                    android.content.Intent
                        .FLAG_ACTIVITY_CLEAR_TOP
            }

        val contentIntent =
            android.app.PendingIntent
                .getActivity(
                    context,
                    notificationId(
                        jobId
                    ),
                    openIntent,
                    android.app.PendingIntent
                        .FLAG_UPDATE_CURRENT or
                    android.app.PendingIntent
                        .FLAG_IMMUTABLE
                )

        val notification =
            android.app.Notification
                .Builder(
                    context,
                    channelId
                )
                .setSmallIcon(
                    android.R.drawable
                        .ic_popup_reminder
                )
                .setContentTitle(
                    "PC Drone • เตือนงานบิน"
                )
                .setContentText(
                    "$customer • $appointmentText"
                )
                .setStyle(
                    android.app.Notification
                        .BigTextStyle()
                        .bigText(
                            detail
                        )
                )
                .setContentIntent(
                    contentIntent
                )
                .setAutoCancel(
                    true
                )
                .setCategory(
                    android.app.Notification
                        .CATEGORY_REMINDER
                )
                .setVisibility(
                    android.app.Notification
                        .VISIBILITY_PUBLIC
                )
                .build()

        val manager =
            context.getSystemService(
                android.content.Context
                    .NOTIFICATION_SERVICE
            ) as android.app.NotificationManager

        manager.notify(
            notificationId(
                jobId
            ),
            notification
        )
    }


    private fun createChannel(
        context: android.content.Context,
        soundUri: android.net.Uri?,
        vibrationEnabled: Boolean
    ): String {

        /*
         * Channel ของ Android เปลี่ยนเสียงหลังสร้างแล้ว
         * ไม่ได้อย่างน่าเชื่อถือ
         *
         * จึงสร้าง Channel ID ตามเสียง + สั่น
         * เมื่อผู้ใช้เปลี่ยนเสียงจะได้ Channel ใหม่ทันที
         */

        val identity =
            (
                soundUri?.toString()
                    ?: "silent"
            ) +
            "_" +
            vibrationEnabled

        val channelId =
            "pc_drone_job_" +
            identity.hashCode()
                .toUInt()
                .toString()

        val manager =
            context.getSystemService(
                android.content.Context
                    .NOTIFICATION_SERVICE
            ) as android.app.NotificationManager

        if (
            manager.getNotificationChannel(
                channelId
            ) != null
        ) {
            return channelId
        }

        val channel =
            android.app.NotificationChannel(
                channelId,
                "แจ้งเตือนงานบิน PC Drone",
                android.app.NotificationManager
                    .IMPORTANCE_HIGH
            )

        channel.description =
            "เสียงและการสั่นสำหรับแจ้งเตือนงานบิน"

        channel.lockscreenVisibility =
            android.app.Notification
                .VISIBILITY_PUBLIC

        if (
            vibrationEnabled
        ) {

            channel.enableVibration(
                true
            )

            channel.vibrationPattern =
                longArrayOf(
                    0,
                    500,
                    250,
                    500
                )

        } else {

            channel.enableVibration(
                false
            )
        }

        if (
            soundUri != null
        ) {

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

            channel.setSound(
                soundUri,
                audioAttributes
            )

        } else {

            channel.setSound(
                null,
                null
            )
        }

        manager.createNotificationChannel(
            channel
        )

        return channelId
    }


    private fun notificationId(
        jobId: Long
    ): Int {

        return (
            jobId xor
                (
                    jobId ushr 32
                )
            )
            .toInt() and
            0x7fffffff
    }
}
