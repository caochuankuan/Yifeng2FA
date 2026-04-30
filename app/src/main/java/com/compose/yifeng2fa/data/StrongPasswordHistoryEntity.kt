package com.compose.yifeng2fa.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "strong_password_history")
data class StrongPasswordHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val password: String,
    val keyword1: String = "",
    val keyword2: String = "",
    val length: Int = 16,
    val useUppercase: Boolean = true,
    val useLowercase: Boolean = true,
    val useNumbers: Boolean = true,
    val useSymbols: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
