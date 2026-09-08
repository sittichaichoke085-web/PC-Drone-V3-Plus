package com.pcdrone.v3plus

import android.content.Context
import android.net.Uri
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

    fun restoreBackup(
        context: Context,
        jsonText: String
    ) {

        val root =
            JSONObject(jsonText)

        if (
            root.optString("format") !=
            "PC_DRONE_BACKUP"
        ) {
            throw IllegalArgumentException(
                "ไฟล์นี้ไม่ใช่ Backup ของ PC Drone"
            )
        }

        val version =
            root.optInt(
                "version",
                -1
            )

        if (
            version != BACKUP_VERSION
        ) {
            throw IllegalArgumentException(
                "เวอร์ชันไฟล์ Backup ไม่รองรับ"
            )
        }

        val preferences =
            root.optJSONObject(
                "preferences"
            )
                ?: throw IllegalArgumentException(
                    "ไม่พบข้อมูล preferences"
                )

        /*
         * ตรวจทุกอย่างก่อนแตะข้อมูลจริง
         */
        val validated =
            mutableMapOf<
                String,
                MutableMap<String, Pair<String, Any>>
            >()

        preferenceNames.forEach { prefName ->

            val prefObject =
                preferences.optJSONObject(
                    prefName
                )
                    ?: throw IllegalArgumentException(
                        "Backup ขาดข้อมูล $prefName"
                    )

            val values =
                mutableMapOf<
                    String,
                    Pair<String, Any>
                >()

            val keys =
                prefObject.keys()

            while (
                keys.hasNext()
            ) {

                val key =
                    keys.next()

                val item =
                    prefObject.optJSONObject(
                        key
                    )
                        ?: throw IllegalArgumentException(
                            "ข้อมูล $key ไม่ถูกต้อง"
                        )

                val type =
                    item.optString(
                        "type"
                    )

                if (
                    !item.has("value")
                ) {
                    throw IllegalArgumentException(
                        "ข้อมูล $key ไม่มี value"
                    )
                }

                val value: Any =
                    when (type) {

                        "string" ->
                            item.getString(
                                "value"
                            )

                        "boolean" ->
                            item.getBoolean(
                                "value"
                            )

                        "int" ->
                            item.getInt(
                                "value"
                            )

                        "long" ->
                            item.getLong(
                                "value"
                            )

                        "float" ->
                            item.getDouble(
                                "value"
                            ).toFloat()

                        "string_set" -> {

                            val array =
                                item.getJSONArray(
                                    "value"
                                )

                            val set =
                                mutableSetOf<String>()

                            for (
                                i in 0 until
                                    array.length()
                            ) {

                                val element =
                                    array.get(i)

                                if (
                                    element !is String
                                ) {
                                    throw IllegalArgumentException(
                                        "ข้อมูล $key มีค่าที่ไม่ใช่ข้อความ"
                                    )
                                }

                                set.add(
                                    element
                                )
                            }

                            set
                        }

                        else ->
                            throw IllegalArgumentException(
                                "ชนิดข้อมูล $type ไม่รองรับ"
                            )
                    }

                values[key] =
                    type to value
            }

            validated[prefName] =
                values
        }

        /*
         * ผ่าน validation แล้วเท่านั้น
         * จึง snapshot ข้อมูลเดิมไว้ก่อน Restore
         */
        val safetySnapshot =
            createBackup(
                context
            )

        try {

            preferenceNames.forEach { prefName ->

                val prefs =
                    context.getSharedPreferences(
                        prefName,
                        Context.MODE_PRIVATE
                    )

                val editor =
                    prefs.edit()
                        .clear()

                validated[
                    prefName
                ]!!.forEach {
                    (key, pair) ->

                    val type =
                        pair.first

                    val value =
                        pair.second

                    when (type) {

                        "string" ->
                            editor.putString(
                                key,
                                value as String
                            )

                        "boolean" ->
                            editor.putBoolean(
                                key,
                                value as Boolean
                            )

                        "int" ->
                            editor.putInt(
                                key,
                                value as Int
                            )

                        "long" ->
                            editor.putLong(
                                key,
                                value as Long
                            )

                        "float" ->
                            editor.putFloat(
                                key,
                                value as Float
                            )

                        "string_set" -> {
                            @Suppress("UNCHECKED_CAST")
                            editor.putStringSet(
                                key,
                                value as Set<String>
                            )
                        }
                    }
                }

                if (
                    !editor.commit()
                ) {
                    throw IllegalStateException(
                        "เขียนข้อมูล $prefName ไม่สำเร็จ"
                    )
                }
            }

        } catch (
            e: Exception
        ) {

            /*
             * ถ้า Restore มีปัญหาระหว่างทาง
             * ย้อนกลับจาก snapshot เดิม
             */
            try {

                restoreBackupInternal(
                    context,
                    safetySnapshot.readText(
                        Charsets.UTF_8
                    )
                )

            } catch (
                _: Exception
            ) {
            }

            throw e
        }
    }


    private fun restoreBackupInternal(
        context: Context,
        jsonText: String
    ) {

        val root =
            JSONObject(jsonText)

        val preferences =
            root.getJSONObject(
                "preferences"
            )

        preferenceNames.forEach { prefName ->

            val prefObject =
                preferences.getJSONObject(
                    prefName
                )

            val editor =
                context.getSharedPreferences(
                    prefName,
                    Context.MODE_PRIVATE
                )
                    .edit()
                    .clear()

            val keys =
                prefObject.keys()

            while (
                keys.hasNext()
            ) {

                val key =
                    keys.next()

                val item =
                    prefObject.getJSONObject(
                        key
                    )

                when (
                    item.getString(
                        "type"
                    )
                ) {

                    "string" ->
                        editor.putString(
                            key,
                            item.getString(
                                "value"
                            )
                        )

                    "boolean" ->
                        editor.putBoolean(
                            key,
                            item.getBoolean(
                                "value"
                            )
                        )

                    "int" ->
                        editor.putInt(
                            key,
                            item.getInt(
                                "value"
                            )
                        )

                    "long" ->
                        editor.putLong(
                            key,
                            item.getLong(
                                "value"
                            )
                        )

                    "float" ->
                        editor.putFloat(
                            key,
                            item.getDouble(
                                "value"
                            ).toFloat()
                        )

                    "string_set" -> {

                        val array =
                            item.getJSONArray(
                                "value"
                            )

                        val set =
                            mutableSetOf<String>()

                        for (
                            i in 0 until
                                array.length()
                        ) {
                            set.add(
                                array.getString(i)
                            )
                        }

                        editor.putStringSet(
                            key,
                            set
                        )
                    }
                }
            }

            editor.commit()
        }
    }



    fun shareBackup(
        context: Context
    ) {

        val file =
            createBackup(context)

        val uri =
            Uri.Builder()
                .scheme(
                    android.content.ContentResolver
                        .SCHEME_CONTENT
                )
                .authority(
                    context.packageName +
                        ".backup-files"
                )
                .appendPath(
                    file.name
                )
                .build()

        val intent =
            android.content.Intent(
                android.content.Intent.ACTION_SEND
            ).apply {

                type =
                    "application/octet-stream"

                putExtra(
                    android.content.Intent.EXTRA_STREAM,
                    uri
                )

                addFlags(
                    android.content.Intent
                        .FLAG_GRANT_READ_URI_PERMISSION
                )
            }

        context.startActivity(
            android.content.Intent.createChooser(
                intent,
                "แชร์ไฟล์สำรอง PC Drone"
            )
        )
    }
}
