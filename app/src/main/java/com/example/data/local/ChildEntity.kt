package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "children")
data class ChildEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val age: Int,
    val mentalAge: Int,
    val notes: String,
    val country: String = "",
    val avatarColor: Int, // Hex color or simple index for selection
    val createdAt: Long = System.currentTimeMillis()
)
