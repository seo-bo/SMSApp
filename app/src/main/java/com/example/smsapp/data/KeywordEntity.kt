package com.example.smsapp.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "keyword",
    indices   = [Index(value = ["word", "isWhitelist"], unique = true)]
)
data class KeywordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val word: String,
    /** true  → 화이트리스트,  false → 블랙리스트 */
    val isWhitelist: Boolean
)
