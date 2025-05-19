package com.example.smsapp.repository

import com.example.smsapp.data.*

class SmsRepository(private val smsDao:  SmsDao, private val spamDao: SpamDao) {
    val conversations = smsDao.getConversations()
    fun messages(addr: String) = smsDao.getThread(addr)
    suspend fun addNormal(e: SmsEntity) = smsDao.insert(e)
    val spamList = spamDao.getAll()
    suspend fun addSpam(e: SpamEntity) = spamDao.insert(e)
}
