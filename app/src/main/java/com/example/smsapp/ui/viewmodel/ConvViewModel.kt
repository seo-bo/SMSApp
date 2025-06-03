package com.example.smsapp.ui.viewmodel

import android.app.Application
import android.telephony.SmsManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.smsapp.data.SmsDatabase
import com.example.smsapp.data.SmsEntity
import com.example.smsapp.repository.SmsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ConvViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = SmsRepository(
        SmsDatabase.get(app).smsDao(),
        SmsDatabase.get(app).spamDao()
    )

    /** 대화방 목록 LiveData */
    val rooms = repo.conversations

    /** SMS 보내기 */
    fun sendSms(phone: String, body: String) = viewModelScope.launch(Dispatchers.IO) {
        SmsManager.getDefault().sendTextMessage(phone, null, body, null, null)
        repo.addNormal(SmsEntity(address = phone, body = body, type = 2))
    }

    /** 대화방 삭제 */
    fun deleteConversation(addr: String) = viewModelScope.launch(Dispatchers.IO) {
        repo.deleteConversation(addr)
    }

    /* ───────────────────────────────────────────────
       ⬇️  Undo(복원)용:  기존 Repo 의 addNormal 래퍼
       ─────────────────────────────────────────────── */
    fun addNormal(e: SmsEntity) = viewModelScope.launch(Dispatchers.IO) {
        repo.addNormal(e)
    }
}
