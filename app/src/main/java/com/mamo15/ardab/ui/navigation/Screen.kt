package com.mamo15.ardab.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector
import com.mamo15.ardab.R

sealed class Screen(
    val route: String,
    @StringRes val titleRes: Int,
    val icon: ImageVector
) {

    object Dashboard : Screen(
        "dashboard",
        R.string.dashboard,
        Icons.Default.Home
    )

    object Reports : Screen(
        "reports",
        R.string.reports,
        Icons.Default.List
    )

    object Profile : Screen(
        "profile",
        R.string.profile,
        Icons.Default.Person
    )

    object Messages : Screen(
        "messages",
        R.string.messages,
        Icons.Default.Email
    )
}