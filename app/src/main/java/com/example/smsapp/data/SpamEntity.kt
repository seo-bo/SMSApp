package com.example.smsapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "spam")
data class SpamEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val address:  String,
    val body:     String,
    val timestamp: Long = System.currentTimeMillis()
)
