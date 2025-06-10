package com.example.smsapp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.example.smsapp.data.SmsDatabase
import com.example.smsapp.data.SmsEntity
import com.example.smsapp.data.SpamEntity
import com.example.smsapp.util.AppPrefs
import com.example.smsapp.util.KeywordMatcher
import com.example.smsapp.util.SpamClassifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SMSReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        // 조각 합치기
        val parts = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (parts.isEmpty()) return
        val address   = parts[0].originatingAddress ?: return
        val body      = buildString { parts.forEach { append(it.messageBody) } }
        val timestamp = System.currentTimeMillis()

        val db      = SmsDatabase.get(ctx)
        val smsDao  = db.smsDao()
        val spamDao = db.spamDao()

        // 코루틴
        CoroutineScope(Dispatchers.IO).launch {
            KeywordMatcher.init(ctx)

            // priority 체크
            val res      = KeywordMatcher.match(body)
            val localOn  = AppPrefs.isLocalFilterEnabled(ctx)
            val isSpam   = when {
                res.hasWhite      -> false
                res.hasBlack      -> true
                localOn && SpamClassifier.isSpam(body) -> true
                else               -> false
            }

            // DB
            if (isSpam) {
                spamDao.insert(SpamEntity(address = address, body = body, timestamp = timestamp))
            } else {
                smsDao.insert(SmsEntity(address = address, body = body, timestamp = timestamp, type = 1))
            }
        }
    }
}
