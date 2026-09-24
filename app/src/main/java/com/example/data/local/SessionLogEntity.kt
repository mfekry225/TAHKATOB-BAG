package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "session_logs")
data class SessionLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val childId: Int,
    val activityName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val score: Int = 0,
    val notes: String = ""
)
