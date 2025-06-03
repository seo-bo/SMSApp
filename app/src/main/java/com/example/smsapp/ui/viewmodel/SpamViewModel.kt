package com.example.smsapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.smsapp.data.SmsDatabase
import com.example.smsapp.repository.SmsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SpamViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = SmsRepository(
        SmsDatabase.get(app).smsDao(),
        SmsDatabase.get(app).spamDao(),
        SmsDatabase.get(app).keywordDao()
    )

    /** 스팸함 “방” 리스트 */
    val rooms = repo.spamRooms

    /** 스팸 대화(방) 삭제 */
    fun deleteConversation(addr: String) = viewModelScope.launch(Dispatchers.IO) {
        repo.deleteSpamConversation(addr)
    }
}
