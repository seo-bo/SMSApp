package com.example.smsapp.data

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        SmsEntity::class,
        SpamEntity::class,
        KeywordEntity::class
    ],
    version = 5,                      // 4 → 5
    exportSchema = false
)
abstract class SmsDatabase : RoomDatabase() {

    abstract fun smsDao():     SmsDao
    abstract fun spamDao():    SpamDao
    abstract fun keywordDao(): KeywordDao

    companion object {
        @Volatile private var INSTANCE: SmsDatabase? = null

        fun get(ctx: Context): SmsDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    ctx.applicationContext,
                    SmsDatabase::class.java, "sms.db"
                )
                    .addMigrations(
                        MIGRATION_1_2,
                        MIGRATION_2_3,
                        MIGRATION_3_4,
                        MIGRATION_4_5
                    )
                    .build().also { INSTANCE = it }
            }

        // v1 ~ v2
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

        // v2 ~ v3
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {}
        }

        // v3 ~ v4
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS keyword (
                        id          INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        word        TEXT NOT NULL,
                        isWhitelist INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_keyword_word_isWhitelist " +
                            "ON keyword(word,isWhitelist)"
                )
            }
        }

        // v4 ~ v5
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "DROP INDEX IF EXISTS index_keyword_word_isWhitelist"
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS " +
                            "index_keyword_word_isWhitelist " +
                            "ON keyword(word,isWhitelist)"
                )
            }
        }
    }
}
