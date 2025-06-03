package com.example.smsapp.ui.keyword

import android.app.Application
import androidx.lifecycle.*
import com.example.smsapp.data.KeywordEntity
import com.example.smsapp.data.SmsDatabase
import com.example.smsapp.repository.SmsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class KeywordViewModel(
    app: Application,
    private val white: Boolean
) : AndroidViewModel(app) {

    private val repo = SmsRepository(
        SmsDatabase.get(app).smsDao(),
        SmsDatabase.get(app).spamDao(),
        SmsDatabase.get(app).keywordDao()
    )

    val words = repo.keywords(white)

    fun add(w: String) = viewModelScope.launch(Dispatchers.IO) {
        repo.addKeyword(w, white)
    }

    fun delete(e: KeywordEntity) = viewModelScope.launch(Dispatchers.IO) {
        repo.removeKeyword(e)
    }

    class Factory(
        private val app: Application,
        private val white: Boolean
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            KeywordViewModel(app, white) as T
    }
}
