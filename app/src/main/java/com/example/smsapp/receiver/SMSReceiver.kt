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

        /* ─── 1. 여러 segment 합치기 ─── */
        val parts = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (parts.isEmpty()) return
        val address   = parts[0].originatingAddress ?: return
        val body      = buildString { parts.forEach { append(it.messageBody) } }
        val timestamp = System.currentTimeMillis()

        val db      = SmsDatabase.get(ctx)
        val smsDao  = db.smsDao()
        val spamDao = db.spamDao()

        /* ─── 2. IO 코루틴 ─── */
        CoroutineScope(Dispatchers.IO).launch {
            /* 2-1) 키워드 매처 초기화 보장 (중복 호출 OK) */
            KeywordMatcher.init(ctx)

            /* 2-2) 화이트/블랙 우선순위 검사 */
            val res      = KeywordMatcher.match(body)
            val localOn  = AppPrefs.isLocalFilterEnabled(ctx)
            val isSpam   = when {
                res.hasWhite      -> false
                res.hasBlack      -> true
                localOn && SpamClassifier.isSpam(body) -> true
                else               -> false
            }

            /* 2-3) DB 저장 */
            if (isSpam) {
                spamDao.insert(SpamEntity(address = address, body = body, timestamp = timestamp))
            } else {
                smsDao.insert(SmsEntity(address = address, body = body, timestamp = timestamp, type = 1))
            }
        }
    }
}
