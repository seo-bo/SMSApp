package com.example.smsapp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsMessage
import com.example.smsapp.data.SmsDatabase
import com.example.smsapp.data.SmsEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SMSReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, intent: Intent) {
        if (intent.action != "android.provider.Telephony.SMS_RECEIVED") return
        val pdus = intent.extras?.get("pdus") as? Array<*> ?: return
        val format = intent.extras?.getString("format")
        val dao = SmsDatabase.get(ctx).smsDao()

        pdus.forEach { raw ->
            val msg = if (format != null)
                SmsMessage.createFromPdu(raw as ByteArray, format)
            else SmsMessage.createFromPdu(raw as ByteArray) ?: return@forEach

            CoroutineScope(Dispatchers.IO).launch {
                dao.insert(
                    SmsEntity(
                        address = msg.originatingAddress ?: "Unknown",
                        body = msg.messageBody,
                        type = 1
                    )
                )
            }
        }
    }
}
