package com.example.data.repository

import com.example.data.SettingsManager
import com.example.data.local.dao.UserDao
import com.example.data.local.entity.UserEntity
import com.example.data.remote.CloudMockDatabase
import com.example.data.remote.RetrofitClient
import com.example.data.remote.model.LoginRequest
import com.example.data.remote.model.RegisterRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class UserRepository(
    private val userDao: UserDao,
    private val mockCloud: CloudMockDatabase,
    private val settings: SettingsManager
) {
    val loggedInUser: Flow<UserEntity?> = userDao.getLoggedInUser()

    suspend fun getLoggedInUserSync(): UserEntity? = withContext(Dispatchers.IO) {
        userDao.getLoggedInUserSync()
    }

    suspend fun login(email: String, password: String): UserEntity = withContext(Dispatchers.IO) {
        val normalizedEmail = email.lowercase().trim()

        if (settings.useMockCloud) {
            // Simulated HTTP Secure Database Auth
            val authResponse = mockCloud.login(normalizedEmail, password)
            val user = UserEntity(
                id = authResponse.id,
                email = authResponse.email,
                fullName = authResponse.fullName,
                sessionToken = authResponse.token,
                isLoggedIn = true
            )
            // Save locally
            userDao.insertUser(user)
            return@withContext user
        } else {
            // Real Server Retrofit/OkHttp Client Flow
            val api = RetrofitClient.getApiService(settings.apiUrl)
            val authResponse = api.login(LoginRequest(normalizedEmail, password))
            val user = UserEntity(
                id = authResponse.id,
                email = authResponse.email,
                fullName = authResponse.fullName,
                sessionToken = authResponse.token,
                isLoggedIn = true
            )
            // Save locally
            userDao.insertUser(user)
            return@withContext user
        }
    }

    suspend fun register(email: String, fullName: String, password: String): UserEntity = withContext(Dispatchers.IO) {
        val normalizedEmail = email.lowercase().trim()

        if (settings.useMockCloud) {
            // Simulated HTTP Secure Database Auth
            val authResponse = mockCloud.register(normalizedEmail, fullName, password)
            val user = UserEntity(
                id = authResponse.id,
                email = authResponse.email,
                fullName = authResponse.fullName,
                sessionToken = authResponse.token,
                isLoggedIn = true
            )
            userDao.insertUser(user)
            return@withContext user
        } else {
            // Real Server Retrofit/OkHttp Client Flow
            val api = RetrofitClient.getApiService(settings.apiUrl)
            val authResponse = api.register(RegisterRequest(normalizedEmail, password, fullName))
            val user = UserEntity(
                id = authResponse.id,
                email = authResponse.email,
                fullName = authResponse.fullName,
                sessionToken = authResponse.token,
                isLoggedIn = true
            )
            userDao.insertUser(user)
            return@withContext user
        }
    }

    suspend fun logout() = withContext(Dispatchers.IO) {
        val current = userDao.getLoggedInUserSync()
        if (current != null) {
            userDao.logoutUser(current.id)
        }
        // Keep DB but set active user logged out so database doesn't hard delete data
        userDao.deleteAllUsers() // Logged out, can wipe cached user details
    }
}
