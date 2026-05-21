package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tickets")
data class TicketEntity(
    @PrimaryKey val id: String, // local format "TKT-<timestamp>" or UUID
    val userId: String, // Owner's email or user id
    val customerName: String,
    val phoneNumber: String,
    val deviceBrand: String,
    val deviceModel: String,
    val imei: String?,
    val errorType: String,
    val estimatedCost: Double,
    val screenLockType: String, // None, Pin, Password, Pattern
    val includedAccessories: String, // Comma separated string or JSON
    val serviceNotes: String,
    val status: String, // Pending, In Progress, Completed, Delivered
    val isSynced: Boolean,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
