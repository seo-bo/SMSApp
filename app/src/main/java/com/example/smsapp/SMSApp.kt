package com.example.smsapp

import android.app.Application
import com.example.smsapp.util.SpamClassifier

class SMSApp : Application() {
    override fun onCreate() {
        super.onCreate()
        SpamClassifier.init(this)
    }
}
