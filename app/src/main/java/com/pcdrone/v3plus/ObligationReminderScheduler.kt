package com.pcdrone.v3plus

object ObligationReminderScheduler {

    private const val REQUEST_CODE_BASE = 1200000000

    fun schedule(
        context: android.content.Context,
        monthItemId: String,
        monthKey: String,
        name: String,
        planned: java.math.BigDecimal,
        dueDay: Int
    ) {
        val parts = monthKey.split("-")
        if (parts.size != 2) return

        val year = parts[0].toIntOrNull() ?: return
        val month = parts[1].toIntOrNull() ?: return
        if (month !in 1..12) return

        val due =
            java.util.Calendar.getInstance(
                java.util.TimeZone.getTimeZone("Asia/Bangkok")
            ).apply {
                clear()
                set(java.util.Calendar.YEAR, year)
                set(java.util.Calendar.MONTH, month - 1)

                val maxDay =
                    getActualMaximum(java.util.Calendar.DAY_OF_MONTH)

                set(
                    java.util.Calendar.DAY_OF_MONTH,
                    dueDay.coerceIn(1, maxDay)
                )
                set(java.util.Calendar.HOUR_OF_DAY, 9)
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }

        val before =
            due.clone() as java.util.Calendar
        before.add(java.util.Calendar.DAY_OF_MONTH, -1)

        val overdue =
            due.clone() as java.util.Calendar
        overdue.add(java.util.Calendar.DAY_OF_MONTH, 1)

        scheduleOne(
            context,
            monthItemId,
            name,
            planned,
            "BEFORE",
            before.timeInMillis
        )

        scheduleOne(
            context,
            monthItemId,
            name,
            planned,
            "DUE",
            due.timeInMillis
        )

        scheduleOne(
            context,
            monthItemId,
            name,
            planned,
            "OVERDUE",
            overdue.timeInMillis
        )
    }

    private fun scheduleOne(
        context: android.content.Context,
        monthItemId: String,
        name: String,
        planned: java.math.BigDecimal,
        kind: String,
        triggerAt: Long
    ) {
        if (triggerAt <= System.currentTimeMillis()) {
            return
        }

        val intent =
            android.content.Intent(
                context,
                ObligationNotificationReceiver::class.java
            ).apply {
                putExtra("month_item_id", monthItemId)
                putExtra("name", name)
                putExtra("planned", planned.toPlainString())
                putExtra("notification_kind", kind)
            }

        val pendingIntent =
            android.app.PendingIntent.getBroadcast(
                context,
                requestCode(monthItemId, kind),
                intent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or
                    android.app.PendingIntent.FLAG_IMMUTABLE
            )

        val alarmManager =
            context.getSystemService(
                android.content.Context.ALARM_SERVICE
            ) as android.app.AlarmManager

        if (
            android.os.Build.VERSION.SDK_INT >=
            android.os.Build.VERSION_CODES.S &&
            !alarmManager.canScheduleExactAlarms()
        ) {
            alarmManager.setAndAllowWhileIdle(
                android.app.AlarmManager.RTC_WAKEUP,
                triggerAt,
                pendingIntent
            )
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                android.app.AlarmManager.RTC_WAKEUP,
                triggerAt,
                pendingIntent
            )
        }
    }

    // PC_DRONE_OBLIGATION_NOTIFICATION_CANCEL
    fun cancel(
        context: android.content.Context,
        monthItemId: String
    ) {
        val alarmManager =
            context.getSystemService(
                android.content.Context.ALARM_SERVICE
            ) as android.app.AlarmManager

        listOf(
            "BEFORE",
            "DUE",
            "OVERDUE"
        ).forEach { kind ->
            val intent =
                android.content.Intent(
                    context,
                    ObligationNotificationReceiver::class.java
                )

            val pendingIntent =
                android.app.PendingIntent.getBroadcast(
                    context,
                    requestCode(monthItemId, kind),
                    intent,
                    android.app.PendingIntent.FLAG_NO_CREATE or
                        android.app.PendingIntent.FLAG_IMMUTABLE
                )

            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        }
    }

    // PC_DRONE_OBLIGATION_NOTIFICATION_REQUEST_CODE
    private fun requestCode(
        monthItemId: String,
        kind: String
    ): Int {
        val hash =
            ("obligation|" + monthItemId + "|" + kind)
                .hashCode() and 0x0fffffff

        return REQUEST_CODE_BASE + hash
    }
}
