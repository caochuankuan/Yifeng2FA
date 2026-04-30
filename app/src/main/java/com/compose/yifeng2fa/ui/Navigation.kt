package com.compose.yifeng2fa.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Password
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
    object Passwords : Screen("passwords")
    object AddPassword : Screen("add_password")
    object PasswordDetail : Screen("password_detail/{id}") {
        fun createRoute(id: Long) = "password_detail/$id"
    }
    object EditPassword : Screen("edit_password/{id}") {
        fun createRoute(id: Long) = "edit_password/$id"
    }
    object StrongPassword : Screen("strong_password")
    object StrongPasswordHistory : Screen("strong_password_history")
}

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Home : BottomNavItem(Screen.Home.route, "首页", Icons.Default.Home)
    object Passwords : BottomNavItem(Screen.Passwords.route, "密码", Icons.Default.Lock)
    object StrongPassword : BottomNavItem(Screen.StrongPassword.route, "强密码创建", Icons.Default.Password)
}

val bottomNavItems = listOf(
    BottomNavItem.Home,
    BottomNavItem.Passwords,
    BottomNavItem.StrongPassword
)
