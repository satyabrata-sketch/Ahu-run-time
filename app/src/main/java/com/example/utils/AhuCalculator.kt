package com.example.utils

import kotlin.math.pow

object AhuCalculator {
    // Default Schedule Constants
    const val DEFAULT_SCHEDULE_LABEL = "Mon-Fri 8:00 AM - 8:00 PM, Sat 8:00 AM - 2:00 PM"
    const val DEFAULT_WEEKDAY_HOURS = 12.0 // 8am to 8pm = 12 hours * 5 days = 60 hours
    const val DEFAULT_WEEKEND_HOURS = 6.0  // Sat 8am to 2pm = 6 hours * 1 day = 6 hours
    const val DEFAULT_OPTIMIZED_WEEKLY_HOURS = 66.0 // 60 + 6 = 66 hrs/week
    const val DEFAULT_STANDARD_WEEKLY_HOURS = 168.0 // 24/7 operation = 168 hrs/week
    const val DEFAULT_ELECTRICITY_RATE = 0.14 // $0.14 / kWh average commercial rate
    const val DEFAULT_VFD_SPEED_PERCENT = 85.0 // 85% airflow modulation
    const val CO2_FACTOR_TONS_PER_KWH = 0.000707 // EPA US average emission factor

    data class CalculationResult(
        val baselineAnnualKwh: Double,
        val optimizedAnnualKwh: Double,
        val annualKwhSaved: Double,
        val annualCostSaved: Double,
        val co2SavedTons: Double,
        val hoursSavedPerYear: Double
    )

    fun calculateSavings(
        fanPowerKw: Double,
        standardWeeklyHours: Double,
        optimizedWeeklyHours: Double,
        electricityRate: Double,
        vfdSpeedPercent: Double
    ): CalculationResult {
        val safeFanKw = maxOf(0.0, fanPowerKw)
        val safeStdHours = maxOf(0.0, minOf(168.0, standardWeeklyHours))
        val safeOptHours = maxOf(0.0, minOf(safeStdHours, optimizedWeeklyHours))
        val safeRate = maxOf(0.0, electricityRate)
        val safeVfd = maxOf(10.0, minOf(100.0, vfdSpeedPercent)) / 100.0

        // Fan affinity law: Power ratio = (Speed ratio)^3
        val vfdPowerFactor = safeVfd.pow(3)

        val baselineAnnualKwh = safeFanKw * safeStdHours * 52.0
        val optimizedAnnualKwh = safeFanKw * vfdPowerFactor * safeOptHours * 52.0

        val annualKwhSaved = maxOf(0.0, baselineAnnualKwh - optimizedAnnualKwh)
        val annualCostSaved = annualKwhSaved * safeRate
        val co2SavedTons = annualKwhSaved * CO2_FACTOR_TONS_PER_KWH
        val hoursSavedPerYear = maxOf(0.0, (safeStdHours - safeOptHours) * 52.0)

        return CalculationResult(
            baselineAnnualKwh = baselineAnnualKwh,
            optimizedAnnualKwh = optimizedAnnualKwh,
            annualKwhSaved = annualKwhSaved,
            annualCostSaved = annualCostSaved,
            co2SavedTons = co2SavedTons,
            hoursSavedPerYear = hoursSavedPerYear
        )
    }
}
