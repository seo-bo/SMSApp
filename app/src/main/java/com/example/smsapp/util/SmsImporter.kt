package com.example.smsapp.util

import android.content.Context
import android.provider.Telephony
import com.example.smsapp.data.SmsDatabase
import com.example.smsapp.data.SmsEntity
import com.example.smsapp.data.SpamEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object SmsImporter {
    suspend fun importAll(ctx: Context) = withContext(Dispatchers.IO) {
        val cr      = ctx.contentResolver
        val msgs    = Telephony.Sms.Intents.getMessagesFromIntent(
            // dummy intent → getMessagesFromIntent only works in BroadcastReceiver,
            // 대신 content://sms 직접 조회
            android.content.Intent().apply { data = Telephony.Sms.CONTENT_URI }
        )
        // API 보장 안 되므로 fallback to contentResolver
        val cursor = cr.query(
            Telephony.Sms.CONTENT_URI,
            arrayOf("address","body","date","type"),
            null,null,
            Telephony.Sms.DEFAULT_SORT_ORDER
        ) ?: return@withContext

        val db      = SmsDatabase.get(ctx)
        val smsDao  = db.smsDao()
        val spamDao = db.spamDao()

        cursor.use { c ->
            val ciAddr = c.getColumnIndex("address")
            val ciBody = c.getColumnIndex("body")
            val ciDate = c.getColumnIndex("date")
            val ciType = c.getColumnIndex("type")
            while (c.moveToNext()) {
                val addr = c.getString(ciAddr) ?: "Unknown"
                val body = c.getString(ciBody) ?: ""
                val ts   = c.getLong(ciDate)
                val tp   = c.getInt(ciType)
                if (SpamClassifier.isSpam(body)) {
                    spamDao.insert(SpamEntity(address = addr, body = body, timestamp = ts))
                } else {
                    smsDao.insert(SmsEntity(address = addr, body = body, timestamp = ts, type = tp))
                }
            }
        }
    }
}
