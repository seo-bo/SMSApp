package com.example.smsapp.repository

import com.example.smsapp.data.*

class SmsRepository(private val dao: SmsDao) {
    val conversations = dao.getConversations()
    fun messages(addr: String) = dao.getThread(addr)
    suspend fun add(e: SmsEntity) = dao.insert(e)
}
