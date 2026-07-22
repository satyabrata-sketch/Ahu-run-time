package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AhuDatabase
import com.example.data.AhuEntry
import com.example.data.AhuRepository
import com.example.data.UserRole
import com.example.data.UserSession
import com.example.utils.AhuCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AhuViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AhuRepository

    init {
        val dao = AhuDatabase.getDatabase(application).ahuDao()
        repository = AhuRepository(dao)
    }

    // Auth & Role state
    private val _userSession = MutableStateFlow(
        UserSession(uid = "user_101", email = "engineer@building.com", role = UserRole.USER)
    )
    val userSession: StateFlow<UserSession> = _userSession.asStateFlow()

    // Form Inputs
    val ahuTag = MutableStateFlow("AHU-01 (Main North)")
    val buildingName = MutableStateFlow("Tech Park Tower A")
    val fanPowerKw = MutableStateFlow("15.0")
    val standardWeeklyHours = MutableStateFlow("168.0") // 24/7 continuous
    val optimizedWeeklyHours = MutableStateFlow("66.0")  // Mon-Fri 8-8, Sat 8-2
    val scheduleNotes = MutableStateFlow("Mon-Fri 8am-8pm, Sat 8am-2pm default HVAC setback schedule")
    val electricityRate = MutableStateFlow("0.14")
    val vfdSpeedPercent = MutableStateFlow("85.0")

    // UI Toast or SnackBar message
    private val _messageEvent = MutableStateFlow<String?>(null)
    val messageEvent: StateFlow<String?> = _messageEvent.asStateFlow()

    // Flow of entries depending on active user or admin
    val entries: StateFlow<List<AhuEntry>> = _userSession.flatMapLatest { session ->
        if (session.role == UserRole.ADMIN) {
            repository.allEntries
        } else {
            repository.getEntriesForUser(session.uid)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // All entries flow (for admin stats)
    val allEntries: StateFlow<List<AhuEntry>> = repository.allEntries.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        // Seed initial mock entries if database is empty on first launch
        viewModelScope.launch {
            repository.allEntries.collect { list ->
                if (list.isEmpty()) {
                    seedInitialEntries()
                }
            }
        }
    }

    fun login(email: String, role: UserRole) {
        val sanitizedEmail = email.ifBlank { if (role == UserRole.ADMIN) "admin@hvac-solutions.com" else "user@building.com" }
        val uid = if (role == UserRole.ADMIN) "admin_001" else "user_" + sanitizedEmail.hashCode().coerceAtLeast(100)
        _userSession.value = UserSession(uid = uid, email = sanitizedEmail, role = role)
        _messageEvent.value = "Signed in as ${role.label} (${sanitizedEmail})"
    }

    fun logout() {
        _userSession.value = UserSession(uid = "user_101", email = "user@building.com", role = UserRole.USER)
        _messageEvent.value = "Logged out"
    }

    fun switchRole(newRole: UserRole) {
        val current = _userSession.value
        val email = if (newRole == UserRole.ADMIN) "admin@hvac-solutions.com" else "user@building.com"
        val uid = if (newRole == UserRole.ADMIN) "admin_001" else "user_101"
        _userSession.value = UserSession(uid = uid, email = email, role = newRole)
        _messageEvent.value = "Switched active view to ${newRole.label} mode"
    }

    fun applyDefaultSchedule() {
        standardWeeklyHours.value = "168.0"
        optimizedWeeklyHours.value = AhuCalculator.DEFAULT_OPTIMIZED_WEEKLY_HOURS.toString()
        scheduleNotes.value = "Default: Mon-Fri 8:00 AM - 8:00 PM (60h) + Sat 8:00 AM - 2:00 PM (6h)"
        if (electricityRate.value.isBlank() || electricityRate.value == "0.0") {
            electricityRate.value = "0.14"
        }
        if (vfdSpeedPercent.value.isBlank()) {
            vfdSpeedPercent.value = "85.0"
        }
        _messageEvent.value = "Applied default Mon-Fri 8-8 & Sat 8-2 schedule (66 hrs/week)"
    }

    fun submitEntry() {
        val kw = fanPowerKw.value.toDoubleOrNull() ?: 0.0
        val stdHrs = standardWeeklyHours.value.toDoubleOrNull() ?: 168.0
        val optHrs = optimizedWeeklyHours.value.toDoubleOrNull() ?: 66.0
        val rate = electricityRate.value.toDoubleOrNull() ?: 0.14
        val vfd = vfdSpeedPercent.value.toDoubleOrNull() ?: 85.0

        if (kw <= 0) {
            _messageEvent.value = "Please enter a valid fan power (kW > 0)"
            return
        }

        val calculation = AhuCalculator.calculateSavings(
            fanPowerKw = kw,
            standardWeeklyHours = stdHrs,
            optimizedWeeklyHours = optHrs,
            electricityRate = rate,
            vfdSpeedPercent = vfd
        )

        val currentSession = _userSession.value

        val newEntry = AhuEntry(
            userId = currentSession.uid,
            userEmail = currentSession.email,
            ahuTag = ahuTag.value.ifBlank { "AHU-Unit" },
            buildingName = buildingName.value.ifBlank { "Facility Center" },
            fanPowerKw = kw,
            standardWeeklyHours = stdHrs,
            optimizedWeeklyHours = optHrs,
            scheduleNotes = scheduleNotes.value,
            electricityRate = rate,
            vfdSpeedPercent = vfd,
            annualKwhSaved = calculation.annualKwhSaved,
            annualCostSaved = calculation.annualCostSaved,
            co2SavedTons = calculation.co2SavedTons
        )

        viewModelScope.launch {
            repository.insertEntry(newEntry)
            _messageEvent.value = "AHU Entry saved! Annual Savings: \$${String.format("%.2f", calculation.annualCostSaved)}"
        }
    }

    fun deleteEntry(entry: AhuEntry) {
        viewModelScope.launch {
            repository.deleteEntry(entry)
            _messageEvent.value = "Entry deleted"
        }
    }

    fun clearMessage() {
        _messageEvent.value = null
    }

    fun generateCsvData(entriesList: List<AhuEntry>): String {
        val sb = StringBuilder()
        sb.append("ID,User,AHU Tag,Building,Fan (kW),Std Hrs/Wk,Opt Hrs/Wk,VFD %,Rate ($/kWh),Annual kWh Saved,Annual Savings ($),CO2 Saved (Tons),Date\n")
        entriesList.forEach { item ->
            val dateStr = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.US)
                .format(java.util.Date(item.timestamp))
            sb.append("${item.id},\"${item.userEmail}\",\"${item.ahuTag}\",\"${item.buildingName}\",${item.fanPowerKw},${item.standardWeeklyHours},${item.optimizedWeeklyHours},${item.vfdSpeedPercent},${item.electricityRate},${item.annualKwhSaved.toInt()},${item.annualCostSaved.toInt()},${String.format("%.2f", item.co2SavedTons)},\"$dateStr\"\n")
        }
        return sb.toString()
    }

    private suspend fun seedInitialEntries() {
        val sample1 = AhuEntry(
            userId = "user_101",
            userEmail = "engineer@building.com",
            ahuTag = "AHU-01 (Executive Floor)",
            buildingName = "Financial Center Tower",
            fanPowerKw = 22.0,
            standardWeeklyHours = 168.0,
            optimizedWeeklyHours = 66.0,
            scheduleNotes = "Mon-Fri 8:00 AM - 8:00 PM, Sat 8:00 AM - 2:00 PM",
            electricityRate = 0.15,
            vfdSpeedPercent = 80.0,
            annualKwhSaved = 142100.0,
            annualCostSaved = 21315.0,
            co2SavedTons = 100.4
        )

        val sample2 = AhuEntry(
            userId = "user_102",
            userEmail = "facility_mgr@hospital.org",
            ahuTag = "AHU-04 (Outpatient Wing)",
            buildingName = "St. Jude Medical Center",
            fanPowerKw = 30.0,
            standardWeeklyHours = 168.0,
            optimizedWeeklyHours = 90.0,
            scheduleNotes = "Extended medical clinic hours setback",
            electricityRate = 0.14,
            vfdSpeedPercent = 85.0,
            annualKwhSaved = 158400.0,
            annualCostSaved = 22176.0,
            co2SavedTons = 111.9
        )

        val sample3 = AhuEntry(
            userId = "admin_001",
            userEmail = "admin@hvac-solutions.com",
            ahuTag = "AHU-12 (Data Hall Air Handler)",
            buildingName = "Metro Data Hub",
            fanPowerKw = 45.0,
            standardWeeklyHours = 168.0,
            optimizedWeeklyHours = 120.0,
            scheduleNotes = "Night economizer free cooling mode",
            electricityRate = 0.12,
            vfdSpeedPercent = 75.0,
            annualKwhSaved = 224600.0,
            annualCostSaved = 26952.0,
            co2SavedTons = 158.8
        )

        repository.insertEntry(sample1)
        repository.insertEntry(sample2)
        repository.insertEntry(sample3)
    }
}
