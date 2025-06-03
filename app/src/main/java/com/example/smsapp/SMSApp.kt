package com.example.smsapp

import android.app.Application
import com.example.smsapp.util.KeywordMatcher
import com.example.smsapp.util.SpamClassifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Application 시작 시
 * 1) 스팸 AI 모델 로드
 * 2) 키워드 오토마톤 초기화
 */
class SMSApp : Application() {
    override fun onCreate() {
        super.onCreate()

        /* 1 ─ TFLite 스팸 모델 */
        SpamClassifier.init(applicationContext)

        /* 2 ─ 키워드 매처 (IO 스레드) */
        CoroutineScope(Dispatchers.IO).launch {
            KeywordMatcher.init(applicationContext)
        }
    }
}
