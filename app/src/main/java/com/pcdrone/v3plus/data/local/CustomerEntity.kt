package com.pcdrone.v3plus.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "customers",
    indices = [
        Index(value = ["name"]),
        Index(value = ["phone"]),
        Index(value = ["gardenName"])
    ]
)
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true)
    val customerId: Long = 0L,

    val name: String,
    val phone: String,

    val lineId: String? = null,

    val address: String = "",
    val subdistrict: String = "",
    val district: String = "",
    val province: String = "",

    val gardenName: String = "",

    val latitude: Double? = null,
    val longitude: Double? = null,

    val note: String = "",

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),

    val isDeleted: Boolean = false
)
