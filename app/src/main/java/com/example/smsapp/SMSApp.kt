package com.example.smsapp

import android.app.Application
import com.example.smsapp.util.SpamClassifier

/**
 * Application 클래스에서는 오직 스팸 모델 초기화만 수행합니다.
 * SMSImporter.importAll() 은 MainActivity 에서 퍼미션 허용 후에만 호출됩니다.
 */
class SMSApp : Application() {
    override fun onCreate() {
        super.onCreate()
        SpamClassifier.init(applicationContext)
    }
}
