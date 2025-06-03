package com.example.smsapp.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "spam",
    indices = [Index(value = ["address", "timestamp"], unique = true)]
)
data class SpamEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val address:  String,
    val body:     String,
    val timestamp: Long = System.currentTimeMillis()
)
