package com.pcdrone.v3plus.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        AppMetaEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun appMetaDao(): AppMetaDao
}
