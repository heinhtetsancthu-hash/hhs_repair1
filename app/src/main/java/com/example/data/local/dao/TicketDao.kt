package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.TicketEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TicketDao {
    @Query("SELECT * FROM tickets WHERE userId = :userId ORDER BY createdAt DESC")
    fun getAllTickets(userId: String): Flow<List<TicketEntity>>

    @Query("SELECT * FROM tickets WHERE userId = :userId AND id = :ticketId")
    suspend fun getTicketById(userId: String, ticketId: String): TicketEntity?

    @Query("SELECT * FROM tickets WHERE userId = :userId AND isSynced = 0")
    suspend fun getUnsyncedTickets(userId: String): List<TicketEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTicket(ticket: TicketEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTickets(tickets: List<TicketEntity>)

    @Query("UPDATE tickets SET isSynced = :isSynced, updatedAt = :updatedAt WHERE id = :ticketId")
    suspend fun updateSyncStatus(ticketId: String, isSynced: Boolean, updatedAt: Long)

    @Query("DELETE FROM tickets WHERE id = :ticketId")
    suspend fun deleteTicket(ticketId: String)

    @Query("DELETE FROM tickets")
    suspend fun deleteAllTickets()
}
