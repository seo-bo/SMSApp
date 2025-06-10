package com.example.smsapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.example.smsapp.data.SmsDatabase
import com.example.smsapp.data.SmsEntity
import com.example.smsapp.repository.SmsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ThreadViewModel(
    app: Application,
    private val address: String,
    private val isSpam: Boolean
) : AndroidViewModel(app) {

    private val repo = SmsRepository(
        SmsDatabase.get(app).smsDao(),
        SmsDatabase.get(app).spamDao(),
        SmsDatabase.get(app).keywordDao()
    )

    // 스레드 LiveData
    val messages: LiveData<List<SmsEntity>> = if (!isSpam) {
        repo.messages(address)
    } else {
        repo.spamThread(address).map { list ->
            list.map { s -> SmsEntity(s.id, s.address, s.body, s.timestamp, 1) }
        }
    }

    // 전송하기
    fun send(body: String) = viewModelScope.launch(Dispatchers.IO) {
        if (!isSpam) {
            repo.addNormal(SmsEntity(address = address, body = body, type = 2))
        }
    }

    // 개별 메시지 삭제
    fun delete(id: Long) = viewModelScope.launch(Dispatchers.IO) {
        if (!isSpam) repo.deleteMessage(id)
    }

    class Factory(
        private val app: Application,
        private val address: String,
        private val isSpam: Boolean
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return ThreadViewModel(app, address, isSpam) as T
        }
    }
}
