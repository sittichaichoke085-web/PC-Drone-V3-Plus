package com.pcdrone.v3plus

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object BackupManager {

    private const val BACKUP_VERSION = 1

    private val preferenceNames = listOf(
        "pc_drone_v3_data",
        "pc_drone_settings"
    )

    fun createBackup(context: Context): File {

        val root = JSONObject()
        root.put("format", "PC_DRONE_BACKUP")
        root.put("version", BACKUP_VERSION)
        root.put("createdAt", System.currentTimeMillis())

        val preferencesObject = JSONObject()

        preferenceNames.forEach { prefName ->

            val prefs =
                context.getSharedPreferences(
                    prefName,
                    Context.MODE_PRIVATE
                )

            val prefObject = JSONObject()

            prefs.all.forEach { (key, value) ->

                val item = JSONObject()

                when (value) {

                    is String -> {
                        item.put("type", "string")
                        item.put("value", value)
                    }

                    is Boolean -> {
                        item.put("type", "boolean")
                        item.put("value", value)
                    }

                    is Int -> {
                        item.put("type", "int")
                        item.put("value", value)
                    }

                    is Long -> {
                        item.put("type", "long")
                        item.put("value", value)
                    }

                    is Float -> {
                        item.put("type", "float")
                        item.put("value", value.toDouble())
                    }

                    is Set<*> -> {
                        item.put("type", "string_set")

                        val array = JSONArray()

                        value
                            .filterIsInstance<String>()
                            .sorted()
                            .forEach {
                                array.put(it)
                            }

                        item.put("value", array)
                    }

                    else -> return@forEach
                }

                prefObject.put(key, item)
            }

            preferencesObject.put(
                prefName,
                prefObject
            )
        }

        root.put(
            "preferences",
            preferencesObject
        )

        val dir =
            File(
                context.cacheDir,
                "pc_drone_backups"
            ).apply {
                mkdirs()
            }

        val formatter =
            SimpleDateFormat(
                "yyyyMMdd-HHmmss",
                Locale.US
            ).apply {
                timeZone =
                    TimeZone.getTimeZone(
                        "Asia/Bangkok"
                    )
            }

        val file =
            File(
                dir,
                "PC-Drone-Backup-${
                    formatter.format(Date())
                }.pcdrone"
            )

        file.writeText(
            root.toString(2),
            Charsets.UTF_8
        )

        return file
    }
}
