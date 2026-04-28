package com.compose.yifeng2fa.ui

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
}
