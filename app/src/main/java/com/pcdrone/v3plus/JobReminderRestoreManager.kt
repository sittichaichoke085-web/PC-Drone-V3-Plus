package com.pcdrone.v3plus

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

object JobReminderRestoreManager {

    fun currentJobIds(context: Context): Set<Long> {
        val prefs = context.getSharedPreferences(
            "pc_drone_v3_data",
            Context.MODE_PRIVATE
        )

        return prefs
            .getStringSet("flight_jobs", emptySet())
            .orEmpty()
            .mapNotNull { record ->
                record
                    .split("|||", ignoreCase = false, limit = 9)
                    .firstOrNull()
                    ?.toLongOrNull()
            }
            .toSet()
    }

    fun rescheduleAfterRestore(
        context: Context,
        previousJobIds: Set<Long>
    ) {
        val dataPrefs = context.getSharedPreferences(
            "pc_drone_v3_data",
            Context.MODE_PRIVATE
        )

        val settingsPrefs = context.getSharedPreferences(
            "pc_drone_settings",
            Context.MODE_PRIVATE
        )

        val jobs = dataPrefs
            .getStringSet("flight_jobs", emptySet())
            .orEmpty()
            .mapNotNull { raw ->
                val p = raw.split(
                    "|||",
                    ignoreCase = false,
                    limit = 9
                )

                if (p.size < 9) {
                    null
                } else {
                    val id = p[0].toLongOrNull()
                        ?: return@mapNotNull null

                    Job(
                        id = id,
                        customer = p[1],
                        service = p[2],
                        location = p[3],
                        rai = p[4].toDoubleOrNull() ?: 0.0,
                        status = p[7]
                    )
                }
            }
            .associateBy { it.id }

        val restoredIds = jobs.keys

        /*
         * ล้าง Alarm ทั้งของข้อมูลก่อน Restore
         * และของข้อมูลที่เพิ่ง Restore เพื่อกัน Alarm ซ้ำ/ค้าง
         */
        (previousJobIds + restoredIds).forEach { jobId ->
            cancelReminder(context, jobId)
        }

        if (!settingsPrefs.getBoolean(
                "notifications_enabled",
                true
            )
        ) {
            return
        }

        val appointments = dataPrefs
            .getStringSet("job_appointments", emptySet())
            .orEmpty()

        val now = System.currentTimeMillis()

        appointments.forEach { raw ->
            val p = raw.split(
                "|||",
                ignoreCase = false,
                limit = 3
            )

            if (p.size < 3) {
                return@forEach
            }

            val jobId = p[0].toLongOrNull()
                ?: return@forEach

            val appointmentMillis = p[1].toLongOrNull()
                ?: return@forEach

            val reminderEnabled =
                p[2].equals("true", ignoreCase = true)

            if (!reminderEnabled) {
                return@forEach
            }

            /*
             * Restore แล้วไม่ยิงแจ้งเตือนย้อนหลัง
             */
            if (appointmentMillis <= now) {
                return@forEach
            }

            val job = jobs[jobId]
                ?: return@forEach

            if (
                job.status == "เสร็จแล้ว" ||
                job.status == "ยกเลิก"
            ) {
                return@forEach
            }

            scheduleReminder(
                context = context,
                settingsPrefs = settingsPrefs,
                job = job,
                appointmentMillis = appointmentMillis
            )
        }
    }

    private fun scheduleReminder(
        context: Context,
        settingsPrefs: android.content.SharedPreferences,
        job: Job,
        appointmentMillis: Long
    ) {
        val advanceValue =
            settingsPrefs.all["notify_advance_minutes"]

        val advanceMinutes =
            when (advanceValue) {
                is Int -> advanceValue
                is Long -> advanceValue.toInt()
                is Float -> advanceValue.toInt()
                is String ->
                    advanceValue.toIntOrNull() ?: 120
                else -> 120
            }.coerceAtLeast(0)

        var triggerAt =
            appointmentMillis -
                advanceMinutes * 60_000L

        val now = System.currentTimeMillis()

        /*
         * นัดยังไม่ผ่าน แต่เลยช่วงเตือนล่วงหน้าแล้ว
         * ใช้พฤติกรรมเดียวกับระบบเดิม
         */
        if (triggerAt <= now) {
            triggerAt = now + 5_000L
        }

        val intent = Intent(
            context,
            JobNotificationReceiver::class.java
        ).apply {
            putExtra("job_id", job.id)
            putExtra(
                "appointment_millis",
                appointmentMillis
            )
            putExtra("customer", job.customer)
            putExtra("service", job.service)
            putExtra("location", job.location)
            putExtra("rai", job.rai)
        }

        val requestCode = requestCode(job.id)

        val pendingIntent =
            PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
            )

        val alarmManager =
            context.getSystemService(
                Context.ALARM_SERVICE
            ) as AlarmManager

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.S
        ) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAt,
                    pendingIntent
                )
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAt,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAt,
                pendingIntent
            )
        }
    }

    private fun cancelReminder(
        context: Context,
        jobId: Long
    ) {
        val alarmManager =
            context.getSystemService(
                Context.ALARM_SERVICE
            ) as AlarmManager

        val baseIntent = Intent(
            context,
            JobNotificationReceiver::class.java
        )

        val code = requestCode(jobId)

        val mainPending =
            PendingIntent.getBroadcast(
                context,
                code,
                baseIntent,
                PendingIntent.FLAG_NO_CREATE or
                    PendingIntent.FLAG_IMMUTABLE
            )

        if (mainPending != null) {
            alarmManager.cancel(mainPending)
            mainPending.cancel()
        }

        val repeatPending =
            PendingIntent.getBroadcast(
                context,
                code + 100000,
                baseIntent,
                PendingIntent.FLAG_NO_CREATE or
                    PendingIntent.FLAG_IMMUTABLE
            )

        if (repeatPending != null) {
            alarmManager.cancel(repeatPending)
            repeatPending.cancel()
        }
    }

    private fun requestCode(jobId: Long): Int =
        (
            jobId xor
                (jobId ushr 32)
            ).toInt() and 0x7fffffff

    private data class Job(
        val id: Long,
        val customer: String,
        val service: String,
        val location: String,
        val rai: Double,
        val status: String
    )
}
