package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Co2
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AhuEntry
import com.example.data.UserRole
import com.example.ui.AhuViewModel
import com.example.ui.theme.DarkNavyBackground
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EnergyTeal

@Composable
fun AdminDashboardScreen(
    viewModel: AhuViewModel,
    modifier: Modifier = Modifier
) {
    val allEntries by viewModel.allEntries.collectAsState()
    val userSession by viewModel.userSession.collectAsState()
    val context = LocalContext.current

    val totalAnnualCostSaved = allEntries.sumOf { it.annualCostSaved }
    val totalAnnualKwhSaved = allEntries.sumOf { it.annualKwhSaved }
    val totalCo2SavedTons = allEntries.sumOf { it.co2SavedTons }
    val uniqueUsers = allEntries.map { it.userEmail }.distinct().size

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Admin Header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("admin_header_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkNavyBackground)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Surface(
                        shape = CircleShape,
                        color = EmeraldGreen.copy(alpha = 0.2f),
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = EmeraldGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Admin Portal Control",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldGreen
                            )
                        }
                    }

                    Text(
                        text = "System Analytics & Overview",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )

                    Text(
                        text = "Managing all ${allEntries.size} entries across $uniqueUsers registered users",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }

                IconButton(
                    onClick = {
                        val csvData = viewModel.generateCsvData(allEntries)
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/csv"
                            putExtra(Intent.EXTRA_SUBJECT, "Admin AHU Master Energy Audit")
                            putExtra(Intent.EXTRA_TEXT, csvData)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Export Master CSV"))
                    },
                    modifier = Modifier.testTag("admin_export_all_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Export All CSV",
                        tint = EmeraldGreen,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Aggregate KPI Metrics Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AdminKpiCard(
                title = "Total Savings",
                value = "\$${String.format("%,.0f", totalAnnualCostSaved)}",
                subtitle = "/ year saved",
                icon = Icons.Default.AttachMoney,
                color = EmeraldGreen,
                modifier = Modifier.weight(1f)
            )

            AdminKpiCard(
                title = "Total Energy",
                value = "${String.format("%,d", (totalAnnualKwhSaved / 1000).toInt())} MWh",
                subtitle = "annual reduction",
                icon = Icons.Default.ElectricBolt,
                color = ElectricCyan,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AdminKpiCard(
                title = "Carbon Offset",
                value = "${String.format("%.1f", totalCo2SavedTons)} T",
                subtitle = "CO₂ avoided",
                icon = Icons.Default.Co2,
                color = EnergyTeal,
                modifier = Modifier.weight(1f)
            )

            AdminKpiCard(
                title = "Active Users",
                value = "$uniqueUsers",
                subtitle = "engineers logged",
                icon = Icons.Default.Group,
                color = Color(0xFF8B5CF6),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // All User Entries Master List Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Master Entry Logs (All Users)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            OutlinedButton(
                onClick = { viewModel.switchRole(UserRole.USER) },
                modifier = Modifier.testTag("switch_to_user_button")
            ) {
                Text("Switch to User View", fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(allEntries, key = { it.id }) { entry ->
                AhuEntryCard(
                    entry = entry,
                    onDelete = { viewModel.deleteEntry(entry) }
                )
            }
        }
    }
}

@Composable
fun AdminKpiCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.testTag("admin_kpi_${title.lowercase().replace(" ", "_")}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )

            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
