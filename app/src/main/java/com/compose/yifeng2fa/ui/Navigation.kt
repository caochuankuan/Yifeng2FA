package com.compose.yifeng2fa.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Add : Screen("add")
    object Scan : Screen("scan")
    object ItemDetail : Screen("item_detail/{id}") {
        fun createRoute(id: Long) = "item_detail/$id"
    }
    object ScanResult : Screen("scan_result/{uri}") {
        fun createRoute(uri: String) = "scan_result/${android.net.Uri.encode(uri)}"
    }
    object Tools : Screen("tools")
    object Settings : Screen("settings")
}

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Home : BottomNavItem(Screen.Home.route, "首页", Icons.Default.Home)
    object Tools : BottomNavItem(Screen.Tools.route, "工具", Icons.Default.Construction)
    object Settings : BottomNavItem(Screen.Settings.route, "设置", Icons.Default.Settings)
}

val bottomNavItems = listOf(
    BottomNavItem.Home,
    BottomNavItem.Tools,
    BottomNavItem.Settings
)
