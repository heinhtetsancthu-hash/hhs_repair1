package com.example

import android.content.Context
import com.example.data.SettingsManager
import com.example.data.local.AppDatabase
import com.example.data.remote.CloudMockDatabase
import com.example.data.repository.TicketRepository
import com.example.data.repository.UserRepository

object ServiceLocator {
    private var database: AppDatabase? = null
    private var settingsManager: SettingsManager? = null
    private var mockCloudDatabase: CloudMockDatabase? = null
    private var userRepository: UserRepository? = null
    private var ticketRepository: TicketRepository? = null

    fun initialize(context: Context) {
        if (database == null) {
            database = AppDatabase.getDatabase(context)
        }
        if (settingsManager == null) {
            settingsManager = SettingsManager(context)
        }
        if (mockCloudDatabase == null) {
            mockCloudDatabase = CloudMockDatabase(context)
        }
        if (userRepository == null) {
            userRepository = UserRepository(
                userDao = database!!.userDao(),
                mockCloud = mockCloudDatabase!!.apply {},
                settings = settingsManager!!
            )
        }
        if (ticketRepository == null) {
            ticketRepository = TicketRepository(
                ticketDao = database!!.ticketDao(),
                mockCloud = mockCloudDatabase!!,
                settings = settingsManager!!
            )
        }
    }

    fun getSettingsManager(context: Context): SettingsManager {
        initialize(context)
        return settingsManager!!
    }

    fun getUserRepository(context: Context): UserRepository {
        initialize(context)
        return userRepository!!
    }

    fun getTicketRepository(context: Context): TicketRepository {
        initialize(context)
        return ticketRepository!!
    }

    fun getMockCloudDatabase(context: Context): CloudMockDatabase {
        initialize(context)
        return mockCloudDatabase!!
    }
}
