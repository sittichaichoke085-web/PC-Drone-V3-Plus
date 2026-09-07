package com.pcdrone.v3plus.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AppMetaDao {

    @Query("SELECT * FROM app_meta WHERE id = 1 LIMIT 1")
    suspend fun getAppMeta(): AppMetaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAppMeta(meta: AppMetaEntity)
}
