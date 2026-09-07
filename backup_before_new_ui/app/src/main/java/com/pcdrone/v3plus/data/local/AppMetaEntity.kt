package com.pcdrone.v3plus.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_meta")
data class AppMetaEntity(
    @PrimaryKey
    val id: Int = 1,
    val schemaVersion: Int = 1,
    val createdAt: Long = System.currentTimeMillis()
)
