package com.example.smsapp.data

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [SmsEntity::class, SpamEntity::class],
    version  = 2,
    exportSchema = false
)
abstract class SmsDatabase : RoomDatabase() {
    abstract fun smsDao():  SmsDao
    abstract fun spamDao(): SpamDao

    companion object {
        @Volatile private var INSTANCE: SmsDatabase? = null

        fun get(ctx: Context): SmsDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    ctx.applicationContext,
                    SmsDatabase::class.java, "sms.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build().also { INSTANCE = it }
            }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS spam (
                        id        INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        address   TEXT NOT NULL,
                        body      TEXT NOT NULL,
                        timestamp INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }
    }
}
