package com.example.smsapp.util

import android.content.Context
import android.net.Uri
import com.example.smsapp.data.SmsDatabase
import com.example.smsapp.data.SmsEntity
import com.example.smsapp.data.SpamEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object SmsImporter {
    suspend fun importAll(ctx: Context) = withContext(Dispatchers.IO) {
        val db      = SmsDatabase.get(ctx)
        val smsDao  = db.smsDao()
        val spamDao = db.spamDao()

        val uri  = Uri.parse("content://sms")
        val cols = arrayOf("_id","address","body","date","type")
        ctx.contentResolver.query(uri, cols, null, null, null)?.use { cursor ->
            val ad  = cursor.getColumnIndex("address")
            val bo  = cursor.getColumnIndex("body")
            val da  = cursor.getColumnIndex("date")
            val ty  = cursor.getColumnIndex("type")
            while (cursor.moveToNext()) {
                val address = cursor.getString(ad) ?: "Unknown"
                val body    = cursor.getString(bo) ?: ""
                val ts      = cursor.getLong(da)
                val type    = cursor.getInt(ty)

                if (SpamClassifier.isSpam(body)) {
                    spamDao.insert(SpamEntity(address = address, body = body, timestamp = ts))
                } else {
                    smsDao.insert(SmsEntity(address = address, body = body, timestamp = ts, type = type))
                }
            }
        }
    }
}
