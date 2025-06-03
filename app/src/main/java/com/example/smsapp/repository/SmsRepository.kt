package com.example.smsapp.repository

import com.example.smsapp.data.*

class SmsRepository(
    private val smsDao:  SmsDao,
    private val spamDao: SpamDao
) {
    /* ─────────── 일반 ─────────── */
    val conversations                  = smsDao.getConversations()
    fun messages(addr: String)         = smsDao.getThread(addr)
    suspend fun addNormal(e: SmsEntity)          = smsDao.insert(e)
    suspend fun deleteMessage(id: Long)          = smsDao.deleteMessage(id)
    suspend fun deleteConversation(addr: String) = smsDao.deleteConversation(addr)

    /* ─────────── 스팸 ─────────── */
    val spamRooms                      = spamDao.getSpamConversations()
    fun spamThread(addr: String)       = spamDao.getThread(addr)
    suspend fun addSpam(e: SpamEntity)             = spamDao.insert(e)
    suspend fun deleteSpamConversation(addr: String) = spamDao.deleteConversation(addr)

    /* ─────────── Settings 용 통계 ─────────── */
    val totalNormalCount = smsDao.totalCount()
    val totalSpamCount   = spamDao.spamCount()
}
