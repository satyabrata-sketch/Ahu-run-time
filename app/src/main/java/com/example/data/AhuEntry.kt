package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ahu_entries")
data class AhuEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val userEmail: String,
    val ahuTag: String,
    val buildingName: String,
    val fanPowerKw: Double,
    val standardWeeklyHours: Double,
    val optimizedWeeklyHours: Double,
    val scheduleNotes: String,
    val electricityRate: Double, // $ / kWh
    val vfdSpeedPercent: Double, // 100% = full speed, e.g. 80% = 80
    val annualKwhSaved: Double,
    val annualCostSaved: Double,
    val co2SavedTons: Double,
    val timestamp: Long = System.currentTimeMillis()
)
