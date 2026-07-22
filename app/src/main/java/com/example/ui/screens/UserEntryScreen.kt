package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Co2
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Flare
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AhuViewModel
import com.example.ui.components.AhuHeroHeader
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EnergyTeal
import com.example.utils.AhuCalculator

@Composable
fun UserEntryScreen(
    viewModel: AhuViewModel,
    modifier: Modifier = Modifier
) {
    val userSession by viewModel.userSession.collectAsState()
    val buildingName by viewModel.buildingName.collectAsState()
    val electricityRate by viewModel.electricityRate.collectAsState()
    val selectedIndex by viewModel.selectedAhuIndex.collectAsState()

    val ahu1Form by viewModel.ahuStates[0].collectAsState()
    val ahu2Form by viewModel.ahuStates[1].collectAsState()
    val ahu3Form by viewModel.ahuStates[2].collectAsState()
    val ahu4Form by viewModel.ahuStates[3].collectAsState()

    val allAhuForms = listOf(ahu1Form, ahu2Form, ahu3Form, ahu4Form)
    val activeForm = allAhuForms[selectedIndex]

    val scrollState = rememberScrollState()

    // Combined site calculations across all 4 AHUs
    val rateVal = electricityRate.toDoubleOrNull() ?: 0.14
    var combinedAnnualCostSaved = 0.0
    var combinedAnnualKwhSaved = 0.0
    var combinedCo2SavedTons = 0.0
    var combinedKwhReadings = 0.0
    var combinedBtuReadings = 0.0

    allAhuForms.forEach { form ->
        val kw = form.fanPowerKw.toDoubleOrNull() ?: 0.0
        val stdHrs = form.standardWeeklyHours.toDoubleOrNull() ?: 168.0
        val optHrs = form.optimizedWeeklyHours.toDoubleOrNull() ?: 66.0
        val vfd = form.vfdSpeedPercent.toDoubleOrNull() ?: 85.0

        combinedKwhReadings += form.kwhReading.toDoubleOrNull() ?: 0.0
        combinedBtuReadings += form.btuReading.toDoubleOrNull() ?: 0.0

        if (kw > 0) {
            val calc = AhuCalculator.calculateSavings(
                fanPowerKw = kw,
                standardWeeklyHours = stdHrs,
                optimizedWeeklyHours = optHrs,
                electricityRate = rateVal,
                vfdSpeedPercent = vfd
            )
            combinedAnnualCostSaved += calc.annualCostSaved
            combinedAnnualKwhSaved += calc.annualKwhSaved
            combinedCo2SavedTons += calc.co2SavedTons
        }
    }

    // Active single AHU calculation
    val activeKw = activeForm.fanPowerKw.toDoubleOrNull() ?: 0.0
    val activeStdHrs = activeForm.standardWeeklyHours.toDoubleOrNull() ?: 168.0
    val activeOptHrs = activeForm.optimizedWeeklyHours.toDoubleOrNull() ?: 66.0
    val activeVfd = activeForm.vfdSpeedPercent.toDoubleOrNull() ?: 85.0
    val activeSavings = AhuCalculator.calculateSavings(
        fanPowerKw = activeKw,
        standardWeeklyHours = activeStdHrs,
        optimizedWeeklyHours = activeOptHrs,
        electricityRate = rateVal,
        vfdSpeedPercent = activeVfd
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AhuHeroHeader(
            title = "AHU Energy Entry & Metering",
            subtitle = "Site: $buildingName (4 Active AHUs)",
            activeRole = userSession.role,
            userEmail = userSession.email
        )

        // Site Header & Preset Selection Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("site_header_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = EnergyTeal,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Site Location & Rate",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Button(
                        onClick = { viewModel.applyNabDt3Defaults() },
                        modifier = Modifier.testTag("apply_nab_dt3_preset_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("NAB-DT3 3rd Fl Defaults", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = buildingName,
                        onValueChange = { viewModel.buildingName.value = it },
                        label = { Text("Site / Building Floor") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                        modifier = Modifier
                            .weight(1.4f)
                            .testTag("site_name_input"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = electricityRate,
                        onValueChange = { viewModel.electricityRate.value = it },
                        label = { Text("Rate (\$/kWh)") },
                        leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(0.8f)
                            .testTag("electricity_rate_input"),
                        singleLine = true
                    )
                }
            }
        }

        // Live Combined Site Savings & Total Readings Banner (NAB-DT3 3rd Floor)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("combined_site_summary_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                EmeraldGreen.copy(alpha = 0.15f),
                                ElectricCyan.copy(alpha = 0.15f)
                            )
                        )
                    )
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = EmeraldGreen,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AttachMoney,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Combined Site Annual Savings (4 AHUs)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "\$${String.format("%,.2f", combinedAnnualCostSaved)} / year",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = EmeraldGreen
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = EmeraldGreen.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "${String.format("%,d", combinedAnnualKwhSaved.toInt())} kWh Saved",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldGreen,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = EmeraldGreen.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(12.dp))

                // Live total kWh and BTU readings across all 4 AHUs on 3rd floor
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MetricChip(
                        icon = Icons.Default.ElectricBolt,
                        label = "Total kWh Readings",
                        value = "${String.format("%,.1f", combinedKwhReadings)} kWh",
                        color = ElectricCyan
                    )

                    MetricChip(
                        icon = Icons.Default.Thermostat,
                        label = "Total BTU Load",
                        value = "${String.format("%,.0f", combinedBtuReadings)} BTU",
                        color = EnergyTeal
                    )

                    MetricChip(
                        icon = Icons.Default.Co2,
                        label = "CO₂ Reduction",
                        value = "${String.format("%.1f", combinedCo2SavedTons)} T",
                        color = Color(0xFF10B981)
                    )
                }
            }
        }

        // AHU Tab Selector (AHU-1, AHU-2, AHU-3, AHU-4)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("ahu_selector_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Select AHU Unit to View & Edit Readings:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("AHU-1", "AHU-2", "AHU-3", "AHU-4").forEachIndexed { index, tag ->
                        val isSelected = selectedIndex == index
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.selectedAhuIndex.value = index }
                                .testTag("ahu_tab_$index"),
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) EnergyTeal else MaterialTheme.colorScheme.surfaceContainerHigh,
                            border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Air,
                                    contentDescription = null,
                                    tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = tag,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${allAhuForms[index].fanPowerKw} kW",
                                    fontSize = 10.sp,
                                    color = if (isSelected) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Selected AHU Detailed Form Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("ahu_active_form_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = EnergyTeal.copy(alpha = 0.15f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Air,
                                    contentDescription = null,
                                    tint = EnergyTeal,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Readings & Specs: ${activeForm.ahuTag}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Site: $buildingName • Est. Savings: \$${String.format("%,.0f", activeSavings.annualCostSaved)}/yr",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // AHU Tag & Fan Rating
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = activeForm.ahuTag,
                        onValueChange = { tag ->
                            viewModel.updateSelectedAhuState { it.copy(ahuTag = tag) }
                        },
                        label = { Text("AHU Tag / ID") },
                        leadingIcon = { Icon(Icons.Default.Air, contentDescription = null) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("ahu_active_tag_input"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = activeForm.fanPowerKw,
                        onValueChange = { kw ->
                            viewModel.updateSelectedAhuState { it.copy(fanPowerKw = kw) }
                        },
                        label = { Text("Fan Rating (kW)") },
                        leadingIcon = { Icon(Icons.Default.ElectricBolt, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("fan_power_input"),
                        singleLine = true
                    )
                }

                // AHU ON / OFF Time (Explicitly requested!)
                Text(
                    text = "AHU Operating Hours (ON / OFF Time)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = activeForm.onTime,
                        onValueChange = { time ->
                            viewModel.updateSelectedAhuState { it.copy(onTime = time) }
                        },
                        label = { Text("AHU ON Time") },
                        leadingIcon = { Icon(Icons.Default.Timer, contentDescription = null) },
                        placeholder = { Text("08:00 AM") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("ahu_on_time_input"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = activeForm.offTime,
                        onValueChange = { time ->
                            viewModel.updateSelectedAhuState { it.copy(offTime = time) }
                        },
                        label = { Text("AHU OFF Time") },
                        leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null) },
                        placeholder = { Text("08:00 PM") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("ahu_off_time_input"),
                        singleLine = true
                    )
                }

                // Readings: kWh and BTU (Explicitly requested!)
                Text(
                    text = "Individual AHU Meter Readings (kWh & BTU)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = activeForm.kwhReading,
                        onValueChange = { kwh ->
                            viewModel.updateSelectedAhuState { it.copy(kwhReading = kwh) }
                        },
                        label = { Text("kWh Reading") },
                        leadingIcon = { Icon(Icons.Default.ElectricBolt, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        placeholder = { Text("1250.0") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("kwh_reading_input"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = activeForm.btuReading,
                        onValueChange = { btu ->
                            viewModel.updateSelectedAhuState { it.copy(btuReading = btu) }
                        },
                        label = { Text("BTU Reading") },
                        leadingIcon = { Icon(Icons.Default.Thermostat, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        placeholder = { Text("350000.0") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btu_reading_input"),
                        singleLine = true
                    )
                }

                // VFD Speed & Runtime Hours
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = activeForm.vfdSpeedPercent,
                        onValueChange = { vfd ->
                            viewModel.updateSelectedAhuState { it.copy(vfdSpeedPercent = vfd) }
                        },
                        label = { Text("VFD Speed (%)") },
                        leadingIcon = { Icon(Icons.Default.Speed, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("vfd_speed_input"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = activeForm.optimizedWeeklyHours,
                        onValueChange = { opt ->
                            viewModel.updateSelectedAhuState { it.copy(optimizedWeeklyHours = opt) }
                        },
                        label = { Text("Opt Hrs/Wk") },
                        leadingIcon = { Icon(Icons.Default.Eco, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("optimized_hours_input"),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = activeForm.scheduleNotes,
                    onValueChange = { notes ->
                        viewModel.updateSelectedAhuState { it.copy(scheduleNotes = notes) }
                    },
                    label = { Text("Zone / Schedule Notes") },
                    leadingIcon = { Icon(Icons.Default.Build, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("schedule_notes_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Single & Batch Save Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.submitSingleEntry(selectedIndex) },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("save_single_ahu_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Save ${activeForm.ahuTag}", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { viewModel.submitAll4AhuEntries() },
                        modifier = Modifier
                            .weight(1.3f)
                            .height(50.dp)
                            .testTag("save_all_4_ahus_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EnergyTeal)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Save All 4 AHUs", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun MetricChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.12f)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = label,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
        }
    }
}

