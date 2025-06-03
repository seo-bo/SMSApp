package com.example.smsapp.data

import androidx.lifecycle.LiveData
import androidx.room.*

/** 대화 목록 요약 DTO */
data class ConversationSummary(
    val address:  String,
    val lastBody: String,
    val lastTime: Long,
    val total:    Int
)

@Dao
interface SmsDao {

    /* ───── ① 대화 목록 ───── */
    @Query(
        """
        SELECT address,
               MAX(timestamp) AS lastTime,
               (SELECT body FROM sms s2
                 WHERE s2.address = sms.address
                 ORDER BY timestamp DESC LIMIT 1) AS lastBody,
               COUNT(*) AS total
        FROM sms
        GROUP BY address
        ORDER BY lastTime DESC
        """
    )
    fun getConversations(): LiveData<List<ConversationSummary>>

    /* ───── ② 스레드 ───── */
    @Query("SELECT * FROM sms WHERE address = :addr ORDER BY timestamp DESC")
    fun getThread(addr: String): LiveData<List<SmsEntity>>

    /* ───── ③ 삽입/삭제 ───── */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(e: SmsEntity)

    @Query("DELETE FROM sms WHERE id = :msgId")
    suspend fun deleteMessage(msgId: Long)

    @Query("DELETE FROM sms WHERE address = :addr")
    suspend fun deleteConversation(addr: String)

    /* ───── ④ 전체 건수 (Settings 화면용) ───── */
    @Query("SELECT COUNT(*) FROM sms")
    fun totalCount(): LiveData<Int>
}
