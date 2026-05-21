package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ServiceLocator
import com.example.data.local.entity.TicketEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TicketViewModel(application: Application) : AndroidViewModel(application) {
    private val ticketRepository = ServiceLocator.getTicketRepository(application)
    private val settings = ServiceLocator.getSettingsManager(application)

    // Current UserId used to watch tickets
    private val _currentUserId = MutableStateFlow("")
    
    @OptIn(ExperimentalCoroutinesApi::class)
    val tickets: StateFlow<List<TicketEntity>> = _currentUserId
        .flatMapLatest { userId ->
            if (userId.isEmpty()) {
                flowOf(emptyList())
            } else {
                ticketRepository.getAllTickets(userId)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // UI state for form
    var customerName = MutableStateFlow("")
    var phoneNumber = MutableStateFlow("")
    var deviceBrand = MutableStateFlow("")
    var deviceModel = MutableStateFlow("")
    var imei = MutableStateFlow("")
    var errorType = MutableStateFlow("Screen Damage") // default
    var estimatedCost = MutableStateFlow("")
    var screenLockType = MutableStateFlow("None") // None, Pin, Password, Pattern
    var screenLockValue = MutableStateFlow("") // Stores pin code, passwords, or pattern sequences (e.g., "1-2-5")
    var includedAccessories = MutableStateFlow(setOf<String>()) // Charger, Battery, Memory Card, Sim Card
    var serviceNotes = MutableStateFlow("")

    // List search & filters
    var searchQuery = MutableStateFlow("")
    var filterStatus = MutableStateFlow("All") // All, Pending, In Progress, Completed, Delivered

    private val _formError = MutableStateFlow<String?>(null)
    val formError: StateFlow<String?> = _formError.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()

    fun setUserId(userId: String) {
        _currentUserId.value = userId
    }

    fun toggleAccessory(accessory: String) {
        val currentSet = includedAccessories.value.toMutableSet()
        if (currentSet.contains(accessory)) {
            currentSet.remove(accessory)
        } else {
            currentSet.add(accessory)
        }
        includedAccessories.value = currentSet
    }

    fun saveTicket(userId: String) {
        _formError.value = null
        val name = customerName.value.trim()
        val phone = phoneNumber.value.trim()
        val brand = deviceBrand.value.trim()
        val model = deviceModel.value.trim()
        val lockType = screenLockType.value
        val lockValue = screenLockValue.value.trim()
        val error = errorType.value
        val costStr = estimatedCost.value.trim()
        val note = serviceNotes.value.trim()
        val imeiVal = imei.value.trim().ifEmpty { null }

        // Validations
        if (name.isEmpty()) {
            _formError.value = "Customer Name is required."
            return
        }
        if (phone.isEmpty()) {
            _formError.value = "Phone Number is required."
            return
        }
        if (brand.isEmpty()) {
            _formError.value = "Device Brand is required."
            return
        }
        if (model.isEmpty()) {
            _formError.value = "Device Model is required."
            return
        }
        val cost = costStr.toDoubleOrNull()
        if (cost == null || cost < 0) {
            _formError.value = "Please enter a valid non-negative Estimated Cost."
            return
        }

        val accessoriesString = includedAccessories.value.joinToString(",")

        // Generate ID like TKT-YYYYMMDDHHMMSS
        val timestamp = System.currentTimeMillis()
        val ticketId = "TKT-${timestamp % 1000000000}"

        val newTicket = TicketEntity(
            id = ticketId,
            userId = userId,
            customerName = name,
            phoneNumber = phone,
            deviceBrand = brand,
            deviceModel = model,
            imei = imeiVal,
            errorType = error,
            estimatedCost = cost,
            screenLockType = lockType,
            screenLockValue = lockValue,
            includedAccessories = accessoriesString,
            serviceNotes = note,
            status = "Pending",
            isSynced = false,
            createdAt = timestamp,
            updatedAt = timestamp
        )

        viewModelScope.launch {
            try {
                ticketRepository.saveTicket(newTicket)
                _saveSuccess.value = true
                clearForm()
            } catch (e: Exception) {
                _formError.value = e.message ?: "Failed to save repair ticket."
            }
        }
    }

    fun updateTicketStatus(ticket: TicketEntity, newStatus: String) {
        viewModelScope.launch {
            val updated = ticket.copy(
                status = newStatus,
                isSynced = false, // mark unsynced so that it synchronizes to cloud
                updatedAt = System.currentTimeMillis()
            )
            ticketRepository.saveTicket(updated)
        }
    }

    fun deleteTicket(ticketId: String) {
        viewModelScope.launch {
            ticketRepository.deleteTicket(ticketId)
        }
    }

    fun clearForm() {
        customerName.value = ""
        phoneNumber.value = ""
        deviceBrand.value = ""
        deviceModel.value = ""
        imei.value = ""
        errorType.value = "Screen Damage"
        estimatedCost.value = ""
        screenLockType.value = "None"
        screenLockValue.value = ""
        includedAccessories.value = emptySet()
        serviceNotes.value = ""
        _formError.value = null
    }

    fun resetSuccess() {
        _saveSuccess.value = false
    }
}
