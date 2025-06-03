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
        SmsDatabase.get(app).spamDao()
    )

    /** 일반 or 스팸 스레드 선택 */
    val messages: LiveData<List<SmsEntity>> = if (!isSpam) {
        repo.messages(address)
    } else {
        // spamDao.getThread → map to SmsEntity(type=1)
        val source = repo.spamThread(address)
        val result = MediatorLiveData<List<SmsEntity>>()
        result.addSource(source) { list ->
            result.value = list.map { se ->
                SmsEntity(
                    id        = se.id,
                    address   = se.address,
                    body      = se.body,
                    timestamp = se.timestamp,
                    type      = 1
                )
            }
        }
        result
    }

    /** 일반 메시지 전송 only */
    fun send(body: String) = viewModelScope.launch(Dispatchers.IO) {
        if (!isSpam) {
            repo.addNormal(
                SmsEntity(address = address, body = body, type = 2)
            )
        }
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
