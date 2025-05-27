package com.example.smsapp.data

import androidx.lifecycle.LiveData
import androidx.room.*

data class ConversationSummary(
    val address: String,
    val lastBody: String,
    val lastTime: Long,
    val total: Int
)
@Dao
interface SmsDao {
    @Query("""
        SELECT address,
               MAX(timestamp) AS lastTime,
               (SELECT body FROM sms s2
                   WHERE s2.address = sms.address
                   ORDER BY timestamp DESC LIMIT 1) AS lastBody,
               COUNT(*) AS total
        FROM sms
        GROUP BY address
        ORDER BY lastTime DESC
    """)
    fun getConversations(): LiveData<List<ConversationSummary>>

    @Query("SELECT * FROM sms WHERE address = :addr ORDER BY timestamp DESC")
    fun getThread(addr: String): LiveData<List<SmsEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(e: SmsEntity)
}
//asd