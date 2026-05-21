package com.example.data.remote

import android.content.Context
import android.content.SharedPreferences
import com.example.data.remote.model.AuthResponse
import com.example.data.remote.model.NetworkTicket
import com.example.data.remote.model.SyncTicketsResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.delay
import java.util.UUID

/**
 * A highly-engineered persistent mock secure cloud database.
 * This simulates actual remote server databases using separate SharedPreferences,
 * enabling multi-device synchronization simulation on a single emulator device!
 * If the user logs in, creates tickets, synchronizes, and wipes local DB ("Simulate Changing Device"),
 * they can download their secure data back onto the device.
 */
class CloudMockDatabase(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("mock_cloud_storage", Context.MODE_PRIVATE)
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()

    private val userAdapter = moshi.adapter(MockUser::class.java)
    private val ticketListAdapter = moshi.adapter<List<NetworkTicket>>(
        Types.newParameterizedType(List::class.java, NetworkTicket::class.java)
    )

    data class MockUser(
        val id: String,
        val email: String,
        val fullName: String,
        val passwordHashed: String,
        val token: String
    )

    suspend fun login(email: String, password: String): AuthResponse {
        delay(1000) // Simulate latency
        val normalizedEmail = email.lowercase().trim()
        val userJson = prefs.getString("user_$normalizedEmail", null)
            ?: throw Exception("Invalid email or password. User not found.")

        val mockUser = userAdapter.fromJson(userJson)
            ?: throw Exception("Corrupted user profile in cloud database.")

        if (mockUser.passwordHashed != hashSimple(password)) {
            throw Exception("Incorrect password. Please try again.")
        }

        return AuthResponse(
            id = mockUser.id,
            email = mockUser.email,
            fullName = mockUser.fullName,
            token = mockUser.token
        )
    }

    suspend fun register(email: String, fullName: String, password: String): AuthResponse {
        delay(1200) // Simulate slightly longer sign up latency
        val normalizedEmail = email.lowercase().trim()
        if (prefs.contains("user_$normalizedEmail")) {
            throw Exception("Email already registered in the cloud database!")
        }

        val userId = UUID.randomUUID().toString()
        val token = "jwt_mock_token_${UUID.randomUUID()}"
        val mockUser = MockUser(
            id = userId,
            email = normalizedEmail,
            fullName = fullName,
            passwordHashed = hashSimple(password),
            token = token
        )

        prefs.edit().putString("user_$normalizedEmail", userAdapter.toJson(mockUser)).apply()
        return AuthResponse(
            id = mockUser.id,
            email = mockUser.email,
            fullName = mockUser.fullName,
            token = mockUser.token
        )
    }

    suspend fun syncTickets(userId: String, ticketsToUpload: List<NetworkTicket>, lastSyncTimestamp: Long): SyncTicketsResponse {
        delay(1500) // Simulate real-world synchronization latency

        val cloudKey = "tickets_$userId"
        val existingCloudJson = prefs.getString(cloudKey, "[]")
        val cloudTickets = ticketListAdapter.fromJson(existingCloudJson)?.toMutableList() ?: mutableListOf()

        val uploadedIds = mutableListOf<String>()

        // 1. Process Uploads
        for (upload in ticketsToUpload) {
            val idx = cloudTickets.indexOfFirst { it.id == upload.id }
            if (idx == -1) {
                // Not in cloud, insert
                cloudTickets.add(upload)
                uploadedIds.add(upload.id)
            } else {
                val existing = cloudTickets[idx]
                // Conflict resolution: latest updatedAt wins
                if (upload.updatedAt > existing.updatedAt) {
                    cloudTickets[idx] = upload
                    uploadedIds.add(upload.id)
                } else {
                    // Cloud version is newer, upload not accepted (or keep cloud version)
                    uploadedIds.add(upload.id) // Still let local client know we resolved it
                }
            }
        }

        // Save back to mock cloud
        prefs.edit().putString(cloudKey, ticketListAdapter.toJson(cloudTickets)).apply()

        // 2. Fetch Downloads (all tickets in cloud belonging to client, modified after lastSyncTimestamp,
        // and not exclusively part of the uploaded set or newer on the server)
        val downloadedTickets = cloudTickets.filter { cloudTicket ->
            // Created/updated by other device, or was not in upload set but has newer timestamp
            val localUpload = ticketsToUpload.find { it.id == cloudTicket.id }
            if (localUpload == null) {
                cloudTicket.updatedAt > lastSyncTimestamp
            } else {
                cloudTicket.updatedAt > localUpload.updatedAt
            }
        }

        return SyncTicketsResponse(
            downloadedTickets = downloadedTickets,
            uploadedIds = uploadedIds,
            serverTime = System.currentTimeMillis()
        )
    }

    private fun hashSimple(input: String): String {
        // Simple hash simulation for security representation
        return "secure_salt_${input.hashCode()}_hash"
    }
}
