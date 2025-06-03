package com.example.smsapp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.example.smsapp.data.SmsDatabase
import com.example.smsapp.data.SmsEntity
import com.example.smsapp.data.SpamEntity
import com.example.smsapp.util.SpamClassifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SMSReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        // ① 여러 segment가 있을 수 있는 SMS를 통합
        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isEmpty()) return

        val address   = messages[0].originatingAddress ?: return
        val body      = buildString { messages.forEach { append(it.messageBody) } }
        val timestamp = System.currentTimeMillis()

        val db      = SmsDatabase.get(ctx)
        val smsDao  = db.smsDao()
        val spamDao = db.spamDao()

        CoroutineScope(Dispatchers.IO).launch {
            if (SpamClassifier.isSpam(body)) {
                // Named arguments 로 id→address→body→timestamp 순서 오류 방지
                spamDao.insert(
                    SpamEntity(
                        address   = address,
                        body      = body,
                        timestamp = timestamp
                    )
                )
            } else {
                smsDao.insert(
                    SmsEntity(
                        address   = address,
                        body      = body,
                        timestamp = timestamp,
                        type      = 1
                    )
                )
            }
        }
    }
}
