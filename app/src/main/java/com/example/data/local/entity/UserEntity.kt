package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String, // email or UUID
    val email: String,
    val fullName: String,
    val sessionToken: String?,
    val isLoggedIn: Boolean,
    val createdAt: Long = System.currentTimeMillis()
)
