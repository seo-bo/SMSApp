package com.example.smsapp.repository

import com.example.smsapp.data.*
import com.example.smsapp.util.KeywordMatcher

class SmsRepository(
    private val smsDao:     SmsDao,
    private val spamDao:    SpamDao,
    private val keywordDao: KeywordDao
) {
    /* ─── 일반 메시지 영역 ─── */
    val conversations                  = smsDao.getConversations()
    fun messages(addr: String)         = smsDao.getThread(addr)
    suspend fun addNormal(e: SmsEntity)          = smsDao.insert(e)
    suspend fun deleteMessage(id: Long)          = smsDao.deleteMessage(id)
    suspend fun deleteConversation(addr: String) = smsDao.deleteConversation(addr)

    /* ─── 스팸 영역 ─── */
    val spamRooms                      = spamDao.getSpamConversations()
    fun spamThread(addr: String)       = spamDao.getThread(addr)
    suspend fun addSpam(e: SpamEntity)             = spamDao.insert(e)
    suspend fun deleteSpamConversation(addr: String) = spamDao.deleteConversation(addr)

    /* ─── 통계 ─── */
    val totalNormalCount = smsDao.totalCount()
    val totalSpamCount   = spamDao.spamCount()

    /* ─── 키워드 영역 ─── */
    fun keywords(white: Boolean)           = keywordDao.getKeywords(white)

    suspend fun addKeyword(word: String, white: Boolean) {
        keywordDao.insert(KeywordEntity(word = word, isWhitelist = white))
        KeywordMatcher.invalidate()                      // 🔹 즉시 재빌드 플래그
    }

    suspend fun removeKeyword(e: KeywordEntity) {
        keywordDao.delete(e)
        KeywordMatcher.invalidate()                      // 🔹 즉시 재빌드 플래그
    }
}
