package com.example.smsapp.ui.viewmodel

import android.app.Application
import android.telephony.SmsManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.smsapp.data.SmsEntity
import com.example.smsapp.data.SmsDatabase
import com.example.smsapp.repository.SmsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ConvViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = SmsRepository(SmsDatabase.get(app).smsDao())
    val rooms = repo.conversations

    fun sendSms(phone: String, body: String) {
        viewModelScope.launch(Dispatchers.IO) {
            SmsManager.getDefault().sendTextMessage(phone, null, body, null, null)
            repo.add(SmsEntity(address = phone, body = body, type = 2))
        }
    }
}

