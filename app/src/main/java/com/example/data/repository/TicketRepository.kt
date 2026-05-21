package com.example.data.repository

import com.example.data.SettingsManager
import com.example.data.local.dao.TicketDao
import com.example.data.local.entity.TicketEntity
import com.example.data.remote.CloudMockDatabase
import com.example.data.remote.RetrofitClient
import com.example.data.remote.model.NetworkTicket
import com.example.data.remote.model.SyncTicketsRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class TicketRepository(
    private val ticketDao: TicketDao,
    private val mockCloud: CloudMockDatabase,
    private val settings: SettingsManager
) {
    fun getAllTickets(userId: String): Flow<List<TicketEntity>> = ticketDao.getAllTickets(userId)

    suspend fun getTicketById(userId: String, ticketId: String): TicketEntity? = withContext(Dispatchers.IO) {
        ticketDao.getTicketById(userId, ticketId)
    }

    suspend fun saveTicket(ticket: TicketEntity) = withContext(Dispatchers.IO) {
        ticketDao.insertTicket(ticket)
    }

    suspend fun deleteTicket(ticketId: String) = withContext(Dispatchers.IO) {
        ticketDao.deleteTicket(ticketId)
    }

    suspend fun clearAllLocalCachedTickets() = withContext(Dispatchers.IO) {
        ticketDao.deleteAllTickets()
    }

    /**
     * Synchronization Engine (Delta-Sync strategy)
     * Syncs unsynced local logs with remote server cloud.
     * Pulls newest records since last synchronized time.
     */
    suspend fun syncTickets(userId: String, token: String): SyncResult = withContext(Dispatchers.IO) {
        if (settings.isForceOffline) {
            throw Exception("Local device is in forced offline mode. Please disable in Settings to synchronize.")
        }

        // 1. Collect unsynced ticket records
        val unsyncedLocal = ticketDao.getUnsyncedTickets(userId)
        val networkUploads = unsyncedLocal.map { entity ->
            NetworkTicket(
                id = entity.id,
                userId = entity.userId,
                customerName = entity.customerName,
                phoneNumber = entity.phoneNumber,
                deviceBrand = entity.deviceBrand,
                deviceModel = entity.deviceModel,
                imei = entity.imei,
                errorType = entity.errorType,
                estimatedCost = entity.estimatedCost,
                screenLockType = entity.screenLockType,
                includedAccessories = entity.includedAccessories,
                serviceNotes = entity.serviceNotes,
                status = entity.status,
                createdAt = entity.createdAt,
                updatedAt = entity.updatedAt
            )
        }

        val lastSync = settings.lastSyncTime

        try {
            val response = if (settings.useMockCloud) {
                // Mock Cloud Storage Sync Path
                mockCloud.syncTickets(userId, networkUploads, lastSync)
            } else {
                // Live REST API Sync Path
                val api = RetrofitClient.getApiService(settings.apiUrl)
                api.syncTickets("Bearer $token", SyncTicketsRequest(userId, networkUploads, lastSync))
            }

            // 2. Mark local records verified updated on server
            for (uploadedId in response.uploadedIds) {
                ticketDao.updateSyncStatus(uploadedId, isSynced = true, updatedAt = response.serverTime)
            }

            // 3. Sync and write newly downloaded records locally
            if (response.downloadedTickets.isNotEmpty()) {
                val dbEntities = response.downloadedTickets.map { net ->
                    TicketEntity(
                        id = net.id,
                        userId = net.userId,
                        customerName = net.customerName,
                        phoneNumber = net.phoneNumber,
                        deviceBrand = net.deviceBrand,
                        deviceModel = net.deviceModel,
                        imei = net.imei,
                        errorType = net.errorType,
                        estimatedCost = net.estimatedCost,
                        screenLockType = net.screenLockType,
                        includedAccessories = net.includedAccessories,
                        serviceNotes = net.serviceNotes,
                        status = net.status,
                        isSynced = true,
                        createdAt = net.createdAt,
                        updatedAt = net.updatedAt
                    )
                }
                ticketDao.insertTickets(dbEntities)
            }

            // 4. Cache state sync time
            settings.lastSyncTime = response.serverTime

            return@withContext SyncResult(
                uploadedCount = response.uploadedIds.size,
                downloadedCount = response.downloadedTickets.size,
                success = true
            )
        } catch (e: Exception) {
            throw e
        }
    }
}

data class SyncResult(
    val uploadedCount: Int,
    val downloadedCount: Int,
    val success: Boolean
)
