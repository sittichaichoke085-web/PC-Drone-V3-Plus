package com.pcdrone.v3plus.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface CustomerDao {

    @Insert
    suspend fun insert(customer: CustomerEntity): Long

    @Update
    suspend fun update(customer: CustomerEntity)

    @Query("""
        SELECT *
        FROM customers
        ORDER BY name ASC
    """)
    suspend fun getAll(): List<CustomerEntity>

    @Query("""
        SELECT *
        FROM customers
        WHERE id = :id
        LIMIT 1
    """)
    suspend fun getById(id: Long): CustomerEntity?

    @Query("""
        SELECT *
        FROM customers
        WHERE name LIKE '%' || :query || '%'
           OR phone LIKE '%' || :query || '%'
           OR gardenName LIKE '%' || :query || '%'
        ORDER BY name ASC
    """)
    suspend fun search(query: String): List<CustomerEntity>
}
