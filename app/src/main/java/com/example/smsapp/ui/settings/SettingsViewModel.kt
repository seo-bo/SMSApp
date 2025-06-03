package com.example.smsapp.ui.settings

import android.app.Application
import androidx.lifecycle.*
import com.example.smsapp.data.SmsDatabase
import com.example.smsapp.repository.SmsRepository

class SettingsViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = SmsRepository(
        SmsDatabase.get(app).smsDao(),
        SmsDatabase.get(app).spamDao()
    )

    /** 스팸·일반 개수 */
    val spam: LiveData<Int>   = repo.totalSpamCount
    val normal: LiveData<Int> = repo.totalNormalCount

    /** 전체 = 스팸 + 일반 */
    val total: LiveData<Int> = MediatorLiveData<Int>().apply {
        var s = 0; var n = 0
        fun update() { value = s + n }
        addSource(spam ) { s = it; update() }
        addSource(normal) { n = it; update() }
    }
}
