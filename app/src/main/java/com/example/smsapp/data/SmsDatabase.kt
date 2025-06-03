// SmsDatabase.kt
package com.example.smsapp.data

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [SmsEntity::class, SpamEntity::class],
    /* ▼ (Fix) 기존 2 → 3으로 복구 */
    version = 3,
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
                    // v1→v2, v2→v3 모두 등록
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
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

        /* ▼ (Fix) 새로 추가 – 버전 2 → 3 로직이 실제로 없으면 빈 migration이라도 등록 */
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                /* 현재 스키마 변화 없음 → NO-OP */
            }
        }
    }
}
