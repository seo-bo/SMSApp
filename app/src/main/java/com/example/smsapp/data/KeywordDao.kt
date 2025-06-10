package com.example.smsapp.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface KeywordDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(e: KeywordEntity)

    @Delete
    suspend fun delete(e: KeywordEntity)

    @Query("SELECT * FROM keyword WHERE isWhitelist = :white ORDER BY word ASC")
    fun getKeywords(white: Boolean): LiveData<List<KeywordEntity>>

    @Query("SELECT * FROM keyword")
    suspend fun getAll(): List<KeywordEntity>
}
