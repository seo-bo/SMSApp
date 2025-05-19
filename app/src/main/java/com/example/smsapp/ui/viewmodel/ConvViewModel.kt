package com.example.smsapp.ui.viewmodel

import android.app.Application
import android.telephony.SmsManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.smsapp.data.*
import com.example.smsapp.repository.SmsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ConvViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = SmsRepository(
        SmsDatabase.get(app).smsDao(),
        SmsDatabase.get(app).spamDao()
    )
    val rooms = repo.conversations

    fun sendSms(phone: String, body: String) = viewModelScope.launch(Dispatchers.IO) {
        SmsManager.getDefault().sendTextMessage(phone, null, body, null, null)
        repo.addNormal(SmsEntity(address = phone, body = body, type = 2))
    }
}
