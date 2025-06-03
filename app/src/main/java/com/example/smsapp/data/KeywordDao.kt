package com.example.smsapp.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface KeywordDao {

    /* ---------- CRUD ---------- */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(e: KeywordEntity)

    @Delete
    suspend fun delete(e: KeywordEntity)

    /* KeywordFragment 에서 쓰던 LiveData 버전 */
    @Query("SELECT * FROM keyword WHERE isWhitelist = :white ORDER BY word ASC")
    fun getKeywords(white: Boolean): LiveData<List<KeywordEntity>>

    /* 🔹  KeywordMatcher 초기화용 – 전체 목록을 즉시 반환 */
    @Query("SELECT * FROM keyword")
    suspend fun getAll(): List<KeywordEntity>
}
