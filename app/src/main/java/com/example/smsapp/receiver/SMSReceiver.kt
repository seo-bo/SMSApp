package com.example.smsapp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.example.smsapp.data.SmsDatabase
import com.example.smsapp.data.SmsEntity
import com.example.smsapp.data.SpamEntity
import com.example.smsapp.util.AppPrefs
import com.example.smsapp.util.SpamClassifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SMSReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        /* ───────── ① 여러 segment 통합 ───────── */
        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isEmpty()) return

        val address   = messages[0].originatingAddress ?: return
        val body      = buildString { messages.forEach { append(it.messageBody) } }
        val timestamp = System.currentTimeMillis()

        val db      = SmsDatabase.get(ctx)
        val smsDao  = db.smsDao()
        val spamDao = db.spamDao()

        CoroutineScope(Dispatchers.IO).launch {
            /* ───────── ② 설정: 로컬 필터 ON/OFF ───────── */
            val localFilter = AppPrefs.isLocalFilterEnabled(ctx)

            if (localFilter && SpamClassifier.isSpam(body)) {
                spamDao.insert(
                    SpamEntity(address = address, body = body, timestamp = timestamp)
                )
            } else {
                smsDao.insert(
                    SmsEntity(address = address, body = body,
                        timestamp = timestamp, type = 1)
                )
            }
        }
    }
}
