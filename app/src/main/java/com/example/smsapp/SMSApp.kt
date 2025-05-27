package com.example.smsapp

import android.app.Application
import android.content.Context
import com.example.smsapp.util.SmsImporter
import com.example.smsapp.util.SpamClassifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SMSApp : Application() {
    override fun onCreate() {
        super.onCreate()
        SpamClassifier.init(this)

        // 2) 기존 메시지 import (한 번만)
        val prefs = getSharedPreferences("smsapp_prefs", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("import_done", false)) {
            CoroutineScope(Dispatchers.IO).launch {
                SmsImporter.importAll(this@SMSApp)
                prefs.edit().putBoolean("import_done", true).apply()
            }
        }
    }
}