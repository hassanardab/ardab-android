package com.mamo15.ardab.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector
import com.mamo15.ardab.R

sealed class Screen(
    val route: String,
    @StringRes val title: Int,
    val icon: ImageVector
) {
    object Dashboard : Screen(
        route = "dashboard",
        title = R.string.dashboard,
        icon = Icons.Default.Home
    )
    object Reports : Screen(
        route = "reports",
        title = R.string.reports,
        icon = Icons.Default.Assessment
    )
    object Messages : Screen(
        route = "messages",
        title = R.string.messages,
        icon = Icons.Default.Email
    )
    object Profile : Screen(
        route = "profile",
        title = R.string.profile,
        icon = Icons.Default.Person
    )
}