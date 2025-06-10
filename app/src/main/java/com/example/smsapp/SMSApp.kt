package com.example.smsapp

import android.app.Application
import com.example.smsapp.util.KeywordMatcher
import com.example.smsapp.util.SpamClassifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SMSApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // TFLite
        SpamClassifier.init(applicationContext)

        // 키워드 매칭(아호-코라식)
        CoroutineScope(Dispatchers.IO).launch {
            KeywordMatcher.init(applicationContext)
        }
    }
}
