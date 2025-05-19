package com.example.smsapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.smsapp.data.SmsDatabase
import com.example.smsapp.repository.SmsRepository

class SpamViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = SmsRepository(
        SmsDatabase.get(app).smsDao(),
        SmsDatabase.get(app).spamDao()
    )
    val spam = repo.spamList
}
