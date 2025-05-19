package com.example.smsapp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsMessage
import com.example.smsapp.data.SmsDatabase
import com.example.smsapp.data.SmsEntity
import com.example.smsapp.data.SpamEntity
import com.example.smsapp.util.SpamClassifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SMSReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, intent: Intent) {
        if (intent.action != "android.provider.Telephony.SMS_RECEIVED") return

        val pdus   = intent.extras?.get("pdus") as? Array<*> ?: return
        val format = intent.extras?.getString("format")

        val db      = SmsDatabase.get(ctx)
        val smsDao  = db.smsDao()
        val spamDao = db.spamDao()

        pdus.forEach { raw ->
            val msg = if (format != null) {
                SmsMessage.createFromPdu(raw as ByteArray, format)
            } else {
                SmsMessage.createFromPdu(raw as ByteArray) ?: return@forEach
            }

            val addr = msg.originatingAddress ?: "Unknown"
            val body = msg.messageBody
            val ts   = System.currentTimeMillis()

            CoroutineScope(Dispatchers.IO).launch {
                if (SpamClassifier.isSpam(body)) {
                    spamDao.insert(
                        SpamEntity(
                            address   = addr,
                            body      = body,
                            timestamp = ts
                        )
                    )
                } else {
                    smsDao.insert(
                        SmsEntity(
                            address   = addr,
                            body      = body,
                            timestamp = ts,
                            type      = 1
                        )
                    )
                }
            }
        }
    }
}
