package com.compose.yifeng2fa.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "password_accounts")
data class PasswordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val websiteName: String,
    val websiteUrl: String = "",
    val account: String = "",
    val password: String,
    val note: String = "",
    val category: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
