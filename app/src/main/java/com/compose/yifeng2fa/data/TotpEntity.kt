package com.compose.yifeng2fa.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "totp_accounts")
data class TotpEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val issuer: String,
    val accountName: String,
    val secret: String,
    val addedAt: Long = System.currentTimeMillis()
)
