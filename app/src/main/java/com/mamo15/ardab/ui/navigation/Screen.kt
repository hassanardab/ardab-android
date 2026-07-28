package com.mamo15.ardab.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person

import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Home)
    object Reports : Screen("reports", "Reports", Icons.Default.List)
    object Profile : Screen("profile", "Profile", Icons.Default.Person)
    object Messages : Screen("messages", "Messages", Icons.Default.Email)
}