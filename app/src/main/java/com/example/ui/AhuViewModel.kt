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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SingleAhuFormState(
    val ahuTag: String = "AHU-1",
    val fanPowerKw: String = "18.5",
    val onTime: String = "08:00 AM",
    val offTime: String = "08:00 PM",
    val kwhReading: String = "1250.0",
    val btuReading: String = "350000.0",
    val vfdSpeedPercent: String = "85.0",
    val standardWeeklyHours: String = "168.0",
    val optimizedWeeklyHours: String = "66.0",
    val scheduleNotes: String = "NAB-DT3 3rd Fl Schedule: Mon-Fri 8am-8pm, Sat 8am-2pm"
)

class AhuViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AhuRepository

    init {
        val dao = AhuDatabase.getDatabase(application).ahuDao()
        repository = AhuRepository(dao)
    }

    // Auth & Role state
    private val _userSession = MutableStateFlow(
        UserSession(uid = "user_101", email = "engineer@nab-dt3.com", role = UserRole.USER)
    )
    val userSession: StateFlow<UserSession> = _userSession.asStateFlow()

    // Global Site Location & Electricity Rate
    val buildingName = MutableStateFlow("NAB-DT3 3rd Floor")
    val electricityRate = MutableStateFlow("0.14")
    val selectedAhuIndex = MutableStateFlow(0) // 0: AHU-1, 1: AHU-2, 2: AHU-3, 3: AHU-4

    // State for all 4 AHUs on site NAB-DT3 3rd floor
    val ahuStates = listOf(
        MutableStateFlow(SingleAhuFormState(ahuTag = "AHU-1", fanPowerKw = "18.5", onTime = "08:00 AM", offTime = "08:00 PM", kwhReading = "1250.0", btuReading = "350000.0", vfdSpeedPercent = "85.0", standardWeeklyHours = "168.0", optimizedWeeklyHours = "66.0", scheduleNotes = "NAB-DT3 3rd Fl Zone 1 (Executive)")),
        MutableStateFlow(SingleAhuFormState(ahuTag = "AHU-2", fanPowerKw = "22.0", onTime = "08:00 AM", offTime = "08:00 PM", kwhReading = "1480.0", btuReading = "420000.0", vfdSpeedPercent = "80.0", standardWeeklyHours = "168.0", optimizedWeeklyHours = "66.0", scheduleNotes = "NAB-DT3 3rd Fl Zone 2 (Open Office)")),
        MutableStateFlow(SingleAhuFormState(ahuTag = "AHU-3", fanPowerKw = "15.0", onTime = "08:00 AM", offTime = "06:00 PM", kwhReading = "980.0", btuReading = "280000.0", vfdSpeedPercent = "85.0", standardWeeklyHours = "168.0", optimizedWeeklyHours = "56.0", scheduleNotes = "NAB-DT3 3rd Fl Zone 3 (Conference Labs)")),
        MutableStateFlow(SingleAhuFormState(ahuTag = "AHU-4", fanPowerKw = "20.0", onTime = "08:00 AM", offTime = "08:00 PM", kwhReading = "1350.0", btuReading = "390000.0", vfdSpeedPercent = "80.0", standardWeeklyHours = "168.0", optimizedWeeklyHours = "66.0", scheduleNotes = "NAB-DT3 3rd Fl Zone 4 (Server & Training)"))
    )

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

    fun updateSelectedAhuState(update: (SingleAhuFormState) -> SingleAhuFormState) {
        val idx = selectedAhuIndex.value
        if (idx in 0..3) {
            val current = ahuStates[idx].value
            ahuStates[idx].value = update(current)
        }
    }

    fun login(email: String, role: UserRole) {
        val sanitizedEmail = email.ifBlank { if (role == UserRole.ADMIN) "admin@hvac-solutions.com" else "engineer@nab-dt3.com" }
        val uid = if (role == UserRole.ADMIN) "admin_001" else "user_" + sanitizedEmail.hashCode().coerceAtLeast(100)
        _userSession.value = UserSession(uid = uid, email = sanitizedEmail, role = role)
        _messageEvent.value = "Signed in as ${role.label} (${sanitizedEmail})"
    }

    fun logout() {
        _userSession.value = UserSession(uid = "user_101", email = "engineer@nab-dt3.com", role = UserRole.USER)
        _messageEvent.value = "Logged out"
    }

    fun switchRole(newRole: UserRole) {
        val current = _userSession.value
        val email = if (newRole == UserRole.ADMIN) "admin@hvac-solutions.com" else "engineer@nab-dt3.com"
        val uid = if (newRole == UserRole.ADMIN) "admin_001" else "user_101"
        _userSession.value = UserSession(uid = uid, email = email, role = newRole)
        _messageEvent.value = "Switched active view to ${newRole.label} mode"
    }

    fun applyNabDt3Defaults() {
        buildingName.value = "NAB-DT3 3rd Floor"
        electricityRate.value = "0.14"

        ahuStates[0].value = SingleAhuFormState("AHU-1", "18.5", "08:00 AM", "08:00 PM", "1250.0", "350000.0", "85.0", "168.0", "66.0", "NAB-DT3 3rd Fl Zone 1 - Mon-Fri 8am-8pm, Sat 8am-2pm")
        ahuStates[1].value = SingleAhuFormState("AHU-2", "22.0", "08:00 AM", "08:00 PM", "1480.0", "420000.0", "80.0", "168.0", "66.0", "NAB-DT3 3rd Fl Zone 2 - Mon-Fri 8am-8pm, Sat 8am-2pm")
        ahuStates[2].value = SingleAhuFormState("AHU-3", "15.0", "08:00 AM", "06:00 PM", "980.0", "280000.0", "85.0", "168.0", "56.0", "NAB-DT3 3rd Fl Zone 3 - Mon-Fri 8am-6pm, Sat Off")
        ahuStates[3].value = SingleAhuFormState("AHU-4", "20.0", "08:00 AM", "08:00 PM", "1350.0", "390000.0", "80.0", "168.0", "66.0", "NAB-DT3 3rd Fl Zone 4 - Mon-Fri 8am-8pm, Sat 8am-2pm")

        _messageEvent.value = "Applied NAB-DT3 3rd Floor default schedules & specs for all 4 AHUs!"
    }

    fun submitSingleEntry(ahuIndex: Int = selectedAhuIndex.value) {
        if (ahuIndex !in 0..3) return
        val form = ahuStates[ahuIndex].value
        val kw = form.fanPowerKw.toDoubleOrNull() ?: 0.0
        val stdHrs = form.standardWeeklyHours.toDoubleOrNull() ?: 168.0
        val optHrs = form.optimizedWeeklyHours.toDoubleOrNull() ?: 66.0
        val rate = electricityRate.value.toDoubleOrNull() ?: 0.14
        val vfd = form.vfdSpeedPercent.toDoubleOrNull() ?: 85.0
        val kwh = form.kwhReading.toDoubleOrNull() ?: 0.0
        val btu = form.btuReading.toDoubleOrNull() ?: 0.0

        if (kw <= 0) {
            _messageEvent.value = "Please enter a valid fan power (kW > 0) for ${form.ahuTag}"
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
            ahuTag = form.ahuTag,
            buildingName = buildingName.value.ifBlank { "NAB-DT3 3rd Floor" },
            fanPowerKw = kw,
            standardWeeklyHours = stdHrs,
            optimizedWeeklyHours = optHrs,
            scheduleNotes = form.scheduleNotes,
            electricityRate = rate,
            vfdSpeedPercent = vfd,
            onTime = form.onTime,
            offTime = form.offTime,
            kwhReading = kwh,
            btuReading = btu,
            annualKwhSaved = calculation.annualKwhSaved,
            annualCostSaved = calculation.annualCostSaved,
            co2SavedTons = calculation.co2SavedTons
        )

        viewModelScope.launch {
            repository.insertEntry(newEntry)
            _messageEvent.value = "Saved ${form.ahuTag} for ${buildingName.value}! Annual Savings: \$${String.format("%.2f", calculation.annualCostSaved)}"
        }
    }

    fun submitAll4AhuEntries() {
        val currentSession = _userSession.value
        val bName = buildingName.value.ifBlank { "NAB-DT3 3rd Floor" }
        val rate = electricityRate.value.toDoubleOrNull() ?: 0.14

        viewModelScope.launch {
            var totalSavedCost = 0.0
            for (form in ahuStates.map { it.value }) {
                val kw = form.fanPowerKw.toDoubleOrNull() ?: 0.0
                val stdHrs = form.standardWeeklyHours.toDoubleOrNull() ?: 168.0
                val optHrs = form.optimizedWeeklyHours.toDoubleOrNull() ?: 66.0
                val vfd = form.vfdSpeedPercent.toDoubleOrNull() ?: 85.0
                val kwh = form.kwhReading.toDoubleOrNull() ?: 0.0
                val btu = form.btuReading.toDoubleOrNull() ?: 0.0

                if (kw > 0) {
                    val calc = AhuCalculator.calculateSavings(
                        fanPowerKw = kw,
                        standardWeeklyHours = stdHrs,
                        optimizedWeeklyHours = optHrs,
                        electricityRate = rate,
                        vfdSpeedPercent = vfd
                    )
                    val entry = AhuEntry(
                        userId = currentSession.uid,
                        userEmail = currentSession.email,
                        ahuTag = form.ahuTag,
                        buildingName = bName,
                        fanPowerKw = kw,
                        standardWeeklyHours = stdHrs,
                        optimizedWeeklyHours = optHrs,
                        scheduleNotes = form.scheduleNotes,
                        electricityRate = rate,
                        vfdSpeedPercent = vfd,
                        onTime = form.onTime,
                        offTime = form.offTime,
                        kwhReading = kwh,
                        btuReading = btu,
                        annualKwhSaved = calc.annualKwhSaved,
                        annualCostSaved = calc.annualCostSaved,
                        co2SavedTons = calc.co2SavedTons
                    )
                    repository.insertEntry(entry)
                    totalSavedCost += calc.annualCostSaved
                }
            }
            _messageEvent.value = "All 4 AHUs saved for $bName! Combined Savings: \$${String.format("%,.2f", totalSavedCost)}"
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
        sb.append("ID,User,AHU Tag,Building / Site,ON Time,OFF Time,kWh Reading,BTU Reading,Fan (kW),Std Hrs/Wk,Opt Hrs/Wk,VFD %,Rate ($/kWh),Annual kWh Saved,Annual Savings ($),CO2 Saved (Tons),Date\n")
        entriesList.forEach { item ->
            val dateStr = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.US)
                .format(java.util.Date(item.timestamp))
            sb.append("${item.id},\"${item.userEmail}\",\"${item.ahuTag}\",\"${item.buildingName}\",\"${item.onTime}\",\"${item.offTime}\",${item.kwhReading},${item.btuReading},${item.fanPowerKw},${item.standardWeeklyHours},${item.optimizedWeeklyHours},${item.vfdSpeedPercent},${item.electricityRate},${item.annualKwhSaved.toInt()},${item.annualCostSaved.toInt()},${String.format("%.2f", item.co2SavedTons)},\"$dateStr\"\n")
        }
        return sb.toString()
    }

    private suspend fun seedInitialEntries() {
        val site = "NAB-DT3 3rd Floor"

        val ahus = listOf(
            Triple("AHU-1", 18.5, Pair("08:00 AM", "08:00 PM")) to Pair(1250.0, 350000.0),
            Triple("AHU-2", 22.0, Pair("08:00 AM", "08:00 PM")) to Pair(1480.0, 420000.0),
            Triple("AHU-3", 15.0, Pair("08:00 AM", "06:00 PM")) to Pair(980.0, 280000.0),
            Triple("AHU-4", 20.0, Pair("08:00 AM", "08:00 PM")) to Pair(1350.0, 390000.0)
        )

        ahus.forEachIndexed { i, item ->
            val tag = item.first.first
            val kw = item.first.second
            val onTime = item.first.third.first
            val offTime = item.first.third.second
            val kwh = item.second.first
            val btu = item.second.second

            val calc = AhuCalculator.calculateSavings(
                fanPowerKw = kw,
                standardWeeklyHours = 168.0,
                optimizedWeeklyHours = if (tag == "AHU-3") 56.0 else 66.0,
                electricityRate = 0.14,
                vfdSpeedPercent = 80.0 + (i % 2) * 5.0
            )

            repository.insertEntry(
                AhuEntry(
                    userId = "user_101",
                    userEmail = "engineer@nab-dt3.com",
                    ahuTag = tag,
                    buildingName = site,
                    fanPowerKw = kw,
                    standardWeeklyHours = 168.0,
                    optimizedWeeklyHours = if (tag == "AHU-3") 56.0 else 66.0,
                    scheduleNotes = "$site $tag - Mon-Fri setback schedule",
                    electricityRate = 0.14,
                    vfdSpeedPercent = 80.0 + (i % 2) * 5.0,
                    onTime = onTime,
                    offTime = offTime,
                    kwhReading = kwh,
                    btuReading = btu,
                    annualKwhSaved = calc.annualKwhSaved,
                    annualCostSaved = calc.annualCostSaved,
                    co2SavedTons = calc.co2SavedTons
                )
            )
        }
    }
}

