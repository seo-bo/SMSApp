package com.example.smsapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [SmsEntity::class], version = 1, exportSchema = false)
abstract class SmsDatabase : RoomDatabase() {
    abstract fun smsDao(): SmsDao
    companion object {
        @Volatile private var INSTANCE: SmsDatabase? = null
        fun get(ctx: Context): SmsDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    ctx.applicationContext,
                    SmsDatabase::class.java, "sms.db"
                ).build().also { INSTANCE = it }
            }
    }
}
