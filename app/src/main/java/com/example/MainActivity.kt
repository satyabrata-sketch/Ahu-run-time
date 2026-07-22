package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AddCard
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserRole
import com.example.ui.AhuViewModel
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.EntryHistoryScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.UserEntryScreen
import com.example.ui.theme.AhuSavingsTheme
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EnergyTeal

class MainActivity : ComponentActivity() {

    private val viewModel: AhuViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AhuSavingsTheme {
                val userSession by viewModel.userSession.collectAsState()
                val messageEvent by viewModel.messageEvent.collectAsState()
                val entries by viewModel.entries.collectAsState()
                val snackbarHostState = remember { SnackbarHostState() }

                var isAuthenticated by remember { mutableStateOf(true) }
                var selectedTab by remember { mutableIntStateOf(0) }

                LaunchedEffect(messageEvent) {
                    messageEvent?.let { msg ->
                        snackbarHostState.showSnackbar(msg)
                        viewModel.clearMessage()
                    }
                }

                if (!isAuthenticated) {
                    LoginScreen(
                        onLoginSuccess = { email, role ->
                            viewModel.login(email, role)
                            isAuthenticated = true
                        }
                    )
                } else {
                    Scaffold(
                        topBar = {
                            TopAppBar(
                                title = {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = if (userSession.role == UserRole.ADMIN) "AHU Savings • Admin" else "AHU Savings • User",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = if (userSession.role == UserRole.ADMIN) EmeraldGreen else EnergyTeal
                                            )
                                        }
                                    }
                                },
                                actions = {
                                    // Quick Role Switcher Button
                                    IconButton(
                                        onClick = {
                                            val nextRole = if (userSession.role == UserRole.ADMIN) UserRole.USER else UserRole.ADMIN
                                            viewModel.switchRole(nextRole)
                                        },
                                        modifier = Modifier.testTag("top_role_switch_button")
                                    ) {
                                        Icon(
                                            imageVector = if (userSession.role == UserRole.ADMIN) Icons.Default.AdminPanelSettings else Icons.Default.Person,
                                            contentDescription = "Switch Role",
                                            tint = if (userSession.role == UserRole.ADMIN) EmeraldGreen else ElectricCyan
                                        )
                                    }

                                    // Logout Button
                                    IconButton(
                                        onClick = {
                                            viewModel.logout()
                                            isAuthenticated = false
                                        },
                                        modifier = Modifier.testTag("logout_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                            contentDescription = "Logout",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            )
                        },
                        bottomBar = {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.testTag("bottom_nav")
                            ) {
                                NavigationBarItem(
                                    selected = selectedTab == 0,
                                    onClick = { selectedTab = 0 },
                                    icon = {
                                        Icon(
                                            imageVector = if (selectedTab == 0) Icons.Filled.Calculate else Icons.Outlined.Calculate,
                                            contentDescription = "Energy Entry"
                                        )
                                    },
                                    label = { Text("Entry") },
                                    modifier = Modifier.testTag("tab_entry"),
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = EnergyTeal,
                                        indicatorColor = EnergyTeal.copy(alpha = 0.15f)
                                    )
                                )

                                NavigationBarItem(
                                    selected = selectedTab == 1,
                                    onClick = { selectedTab = 1 },
                                    icon = {
                                        BadgedBox(
                                            badge = {
                                                if (entries.isNotEmpty()) {
                                                    Badge { Text("${entries.size}") }
                                                }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = if (selectedTab == 1) Icons.Filled.History else Icons.Outlined.History,
                                                contentDescription = "Entry History"
                                            )
                                        }
                                    },
                                    label = { Text("History") },
                                    modifier = Modifier.testTag("tab_history"),
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = ElectricCyan,
                                        indicatorColor = ElectricCyan.copy(alpha = 0.15f)
                                    )
                                )

                                NavigationBarItem(
                                    selected = selectedTab == 2,
                                    onClick = { selectedTab = 2 },
                                    icon = {
                                        Icon(
                                            imageVector = if (selectedTab == 2) Icons.Filled.Dashboard else Icons.Outlined.Dashboard,
                                            contentDescription = "Admin Dashboard"
                                        )
                                    },
                                    label = { Text(if (userSession.role == UserRole.ADMIN) "Admin Portal" else "Dashboard") },
                                    modifier = Modifier.testTag("tab_admin"),
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = EmeraldGreen,
                                        indicatorColor = EmeraldGreen.copy(alpha = 0.15f)
                                    )
                                )
                            }
                        },
                        snackbarHost = { SnackbarHost(snackbarHostState) },
                        modifier = Modifier.fillMaxSize()
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            when (selectedTab) {
                                0 -> UserEntryScreen(viewModel = viewModel)
                                1 -> EntryHistoryScreen(viewModel = viewModel)
                                2 -> AdminDashboardScreen(viewModel = viewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}
