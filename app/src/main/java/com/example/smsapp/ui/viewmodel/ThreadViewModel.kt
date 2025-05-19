package com.example.smsapp.ui.viewmodel

import android.app.Application
import android.telephony.SmsManager
import androidx.lifecycle.*
import com.example.smsapp.data.*
import com.example.smsapp.repository.SmsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ThreadViewModel(
    app: Application,
    private val address: String
) : AndroidViewModel(app) {

    private val repo = SmsRepository(
        SmsDatabase.get(app).smsDao(),
        SmsDatabase.get(app).spamDao()
    )
    val messages = repo.messages(address)

    fun send(body: String) = viewModelScope.launch(Dispatchers.IO) {
        SmsManager.getDefault().sendTextMessage(address, null, body, null, null)
        repo.addNormal(SmsEntity(address = address, body = body, type = 2))
    }

    class Factory(
        private val app: Application,
        private val addr: String
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(cls: Class<T>): T =
            ThreadViewModel(app, addr) as T
    }
}
