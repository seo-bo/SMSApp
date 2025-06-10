package com.example.smsapp.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.example.smsapp.data.SmsDatabase
import com.example.smsapp.repository.SmsRepository

class SettingsViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = SmsRepository(
        SmsDatabase.get(app).smsDao(),
        SmsDatabase.get(app).spamDao(),
        SmsDatabase.get(app).keywordDao()
    )

    // 스팸 / 일반 건수
    val spam:   LiveData<Int> = repo.totalSpamCount
    val normal: LiveData<Int> = repo.totalNormalCount

    // total
    val total: LiveData<Int> = MediatorLiveData<Int>().apply {
        var s = 0
        var n = 0
        fun update() { value = s + n }
        addSource(spam)   { s = it; update() }
        addSource(normal) { n = it; update() }
    }
}
