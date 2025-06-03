package com.example.smsapp.data

import androidx.lifecycle.LiveData
import androidx.room.*

data class SpamSummary(
    val address:  String,
    val lastBody: String,
    val lastTime: Long,
    val total:    Int
)

@Dao
interface SpamDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(e: SpamEntity)

    @Query(
        """
        SELECT address,
               MAX(timestamp)                                AS lastTime,
               (SELECT body FROM spam s2
                 WHERE s2.address = spam.address
                 ORDER BY timestamp DESC LIMIT 1)            AS lastBody,
               COUNT(*)                                      AS total
        FROM spam
        GROUP BY address
        ORDER BY lastTime DESC
        """
    )
    fun getSpamConversations(): LiveData<List<SpamSummary>>

    @Query("SELECT * FROM spam WHERE address = :addr ORDER BY timestamp DESC")
    fun getThread(addr: String): LiveData<List<SpamEntity>>

    @Query("DELETE FROM spam WHERE address = :addr")
    suspend fun deleteConversation(addr: String)

    /* ───── Settings 화면용 스팸 건수 ───── */
    @Query("SELECT COUNT(*) FROM spam")
    fun spamCount(): LiveData<Int>
}
