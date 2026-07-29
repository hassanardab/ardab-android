package com.mamo15.ardab.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.mamo15.ardab.ui.navigation.Screen
import com.mamo15.ardab.ui.screens.DashboardScreen
import com.mamo15.ardab.ui.screens.MessagesScreen
import com.mamo15.ardab.ui.screens.ProfileScreen
import com.mamo15.ardab.ui.screens.ReportsScreen

@Composable
fun MainScreen() {
    val items = listOf(
        Screen.Dashboard,
        Screen.Reports,
        Screen.Profile,
        Screen.Messages
    )

    var selectedRoute by rememberSaveable { mutableStateOf(Screen.Dashboard.route) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = stringResource(screen.titleRes)
                            )
                        },
                        label = {
                            Text(stringResource(screen.titleRes))
                        },
                        selected = selectedRoute == screen.route,
                        onClick = {
                            selectedRoute = screen.route
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedRoute) {
                Screen.Dashboard.route -> DashboardScreen()
                Screen.Reports.route -> ReportsScreen()
                Screen.Profile.route -> ProfileScreen()
                Screen.Messages.route -> MessagesScreen()
            }
        }
    }
}