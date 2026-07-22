package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
    val ahuTag by viewModel.ahuTag.collectAsState()
    val buildingName by viewModel.buildingName.collectAsState()
    val fanPowerKw by viewModel.fanPowerKw.collectAsState()
    val standardWeeklyHours by viewModel.standardWeeklyHours.collectAsState()
    val optimizedWeeklyHours by viewModel.optimizedWeeklyHours.collectAsState()
    val scheduleNotes by viewModel.scheduleNotes.collectAsState()
    val electricityRate by viewModel.electricityRate.collectAsState()
    val vfdSpeedPercent by viewModel.vfdSpeedPercent.collectAsState()

    val scrollState = rememberScrollState()

    // Live calculation values
    val kwVal = fanPowerKw.toDoubleOrNull() ?: 0.0
    val stdVal = standardWeeklyHours.toDoubleOrNull() ?: 168.0
    val optVal = optimizedWeeklyHours.toDoubleOrNull() ?: 66.0
    val rateVal = electricityRate.toDoubleOrNull() ?: 0.14
    val vfdVal = vfdSpeedPercent.toDoubleOrNull() ?: 85.0

    val liveSavings = AhuCalculator.calculateSavings(
        fanPowerKw = kwVal,
        standardWeeklyHours = stdVal,
        optimizedWeeklyHours = optVal,
        electricityRate = rateVal,
        vfdSpeedPercent = vfdVal
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
            title = "AHU Energy Entry",
            subtitle = "Calculate air handler power savings & setback schedules",
            activeRole = userSession.role,
            userEmail = userSession.email
        )

        // Live Real-Time Savings Summary Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("savings_summary_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                EmeraldGreen.copy(alpha = 0.12f),
                                ElectricCyan.copy(alpha = 0.12f)
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
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AttachMoney,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Estimated Annual Savings",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "\$${String.format("%,.2f", liveSavings.annualCostSaved)} / year",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = EmeraldGreen
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = EmeraldGreen.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "${liveSavings.hoursSavedPerYear.toInt()} hrs saved/yr",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldGreen,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MetricChip(
                        icon = Icons.Default.ElectricBolt,
                        label = "Energy Saved",
                        value = "${String.format("%,d", liveSavings.annualKwhSaved.toInt())} kWh",
                        color = ElectricCyan
                    )

                    MetricChip(
                        icon = Icons.Default.Co2,
                        label = "Carbon Offset",
                        value = "${String.format("%.1f", liveSavings.co2SavedTons)} Tons CO₂",
                        color = EnergyTeal
                    )
                }
            }
        }

        // AHU System Form Inputs Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("ahu_entry_form"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "AHU Specifications & Operating Schedule",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Unit Tag & Building Name
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = ahuTag,
                        onValueChange = { viewModel.ahuTag.value = it },
                        label = { Text("AHU Tag / ID") },
                        leadingIcon = { Icon(Icons.Default.Air, contentDescription = null) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("ahu_tag_input"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = buildingName,
                        onValueChange = { viewModel.buildingName.value = it },
                        label = { Text("Building / Zone") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("building_input"),
                        singleLine = true
                    )
                }

                // Fan kW & VFD Speed
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = fanPowerKw,
                        onValueChange = { viewModel.fanPowerKw.value = it },
                        label = { Text("Fan Rating (kW)") },
                        leadingIcon = { Icon(Icons.Default.ElectricBolt, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("fan_power_input"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = vfdSpeedPercent,
                        onValueChange = { viewModel.vfdSpeedPercent.value = it },
                        label = { Text("VFD Speed (%)") },
                        leadingIcon = { Icon(Icons.Default.Speed, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("vfd_speed_input"),
                        singleLine = true
                    )
                }

                // Schedule Pre-fill Preset Button
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = EnergyTeal.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(16.dp)
                        )
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = EnergyTeal,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Preset Schedule",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            // Use Default Schedule Button (Requested in prompt!)
                            Button(
                                onClick = { viewModel.applyDefaultSchedule() },
                                modifier = Modifier.testTag("use_default_schedule_button"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Use Default",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Text(
                            text = "Pre-fills: Mon-Fri 8am-8pm (60h) + Sat 8am-2pm (6h) = 66 hrs/wk vs 24/7 (168h/wk)",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }

                // Hours Inputs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = standardWeeklyHours,
                        onValueChange = { viewModel.standardWeeklyHours.value = it },
                        label = { Text("Standard Hrs/Wk") },
                        leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("standard_hours_input"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = optimizedWeeklyHours,
                        onValueChange = { viewModel.optimizedWeeklyHours.value = it },
                        label = { Text("Optimized Hrs/Wk") },
                        leadingIcon = { Icon(Icons.Default.Eco, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("optimized_hours_input"),
                        singleLine = true
                    )
                }

                // Electricity Rate & Remarks
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
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

                    OutlinedTextField(
                        value = scheduleNotes,
                        onValueChange = { viewModel.scheduleNotes.value = it },
                        label = { Text("Schedule Notes / Description") },
                        leadingIcon = { Icon(Icons.Default.Build, contentDescription = null) },
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag("schedule_notes_input"),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Button(
                    onClick = { viewModel.submitEntry() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("submit_entry_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EnergyTeal)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Calculate & Save AHU Entry",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
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
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
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
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
        }
    }
}
