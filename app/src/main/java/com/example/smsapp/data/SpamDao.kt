package com.example.smsapp.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface SpamDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(e: SpamEntity)

    @Query("SELECT * FROM spam ORDER BY timestamp DESC")
    fun getAll(): LiveData<List<SpamEntity>>
}
