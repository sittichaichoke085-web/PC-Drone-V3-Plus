package com.pcdrone.v3plus

class ObligationBootReceiver :
    android.content.BroadcastReceiver() {

    override fun onReceive(
        context: android.content.Context,
        intent: android.content.Intent
    ) {
        // PC_DRONE_OBLIGATION_BOOT_RESTORE
        if (
            intent.action !=
            android.content.Intent.ACTION_BOOT_COMPLETED
        ) {
            return
        }

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

        val data =
            context.getSharedPreferences(
                "pc_drone_v3_data",
                android.content.Context.MODE_PRIVATE
            )

        data.getStringSet(
            "money_manager_obligation_months",
            emptySet()
        ).orEmpty()
            .forEach { raw ->

                val item =
                    raw.split(
                        "|||",
                        ignoreCase = false,
                        limit = 7
                    )

                if (item.size != 7) {
                    return@forEach
                }

                val planned =
                    item[4].toBigDecimalOrNull()
                        ?: return@forEach

                val dueDay =
                    item[5].toIntOrNull()
                        ?: return@forEach

                if (
                    planned.compareTo(
                        java.math.BigDecimal.ZERO
                    ) <= 0 ||
                    dueDay !in 1..31
                ) {
                    return@forEach
                }

                ObligationReminderScheduler.schedule(
                    context = context,
                    monthItemId = item[0],
                    monthKey = item[2],
                    name = item[3],
                    planned = planned,
                    dueDay = dueDay
                )
            }
    }
}
