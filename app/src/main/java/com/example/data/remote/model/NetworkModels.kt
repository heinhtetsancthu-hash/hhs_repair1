package com.example.data.remote.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LoginRequest(
    val email: String,
    val password: String
)

@JsonClass(generateAdapter = true)
data class RegisterRequest(
    val email: String,
    val password: String,
    val fullName: String
)

@JsonClass(generateAdapter = true)
data class AuthResponse(
    val id: String,
    val email: String,
    val fullName: String,
    val token: String
)

@JsonClass(generateAdapter = true)
data class NetworkTicket(
    val id: String,
    val userId: String,
    val customerName: String,
    val phoneNumber: String,
    val deviceBrand: String,
    val deviceModel: String,
    val imei: String?,
    val errorType: String,
    val estimatedCost: Double,
    val screenLockType: String,
    val includedAccessories: String,
    val serviceNotes: String,
    val status: String,
    val createdAt: Long,
    val updatedAt: Long
)

@JsonClass(generateAdapter = true)
data class SyncTicketsRequest(
    val userId: String,
    val ticketsToUpload: List<NetworkTicket>,
    val lastSyncTimestamp: Long
)

@JsonClass(generateAdapter = true)
data class SyncTicketsResponse(
    val downloadedTickets: List<NetworkTicket>,
    val uploadedIds: List<String>,
    val serverTime: Long
)
