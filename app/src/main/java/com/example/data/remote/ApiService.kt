package com.example.data.remote

import com.example.data.remote.model.*
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {
    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): AuthResponse

    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): AuthResponse

    @POST("tickets/sync")
    suspend fun syncTickets(
        @Header("Authorization") token: String,
        @Body request: SyncTicketsRequest
    ): SyncTicketsResponse
}
