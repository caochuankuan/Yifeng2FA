package com.compose.yifeng2fa

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.compose.yifeng2fa.ui.AddScreen
import com.compose.yifeng2fa.ui.HomeScreen
import com.compose.yifeng2fa.ui.ScanScreen
import com.compose.yifeng2fa.ui.ScanResultScreen
import com.compose.yifeng2fa.ui.ItemDetailScreen
import com.compose.yifeng2fa.ui.PasswordScreen
import com.compose.yifeng2fa.ui.AddPasswordScreen
import com.compose.yifeng2fa.ui.PasswordDetailScreen
import com.compose.yifeng2fa.ui.EditPasswordScreen
import com.compose.yifeng2fa.ui.StrongPasswordScreen
import com.compose.yifeng2fa.ui.StrongPasswordHistoryScreen
import com.compose.yifeng2fa.ui.Screen
import com.compose.yifeng2fa.ui.bottomNavItems
import com.compose.yifeng2fa.ui.theme.Yifeng2FATheme
import com.compose.yifeng2fa.viewmodel.TotpViewModel
import com.compose.yifeng2fa.viewmodel.PasswordViewModel

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Yifeng2FATheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val viewModel: TotpViewModel = viewModel()
                    val passwordViewModel: PasswordViewModel = viewModel()

                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route

                    val showBottomBar = bottomNavItems.any { it.route == currentRoute }

                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        bottomBar = {
                            if (showBottomBar) {
                                NavigationBar {
                                    bottomNavItems.forEach { item ->
                                        val selected = currentRoute == item.route
                                        NavigationBarItem(
                                            icon = {
                                                Icon(
                                                    imageVector = item.icon,
                                                    contentDescription = item.title
                                                )
                                            },
                                            label = { Text(item.title) },
                                            selected = selected,
                                            onClick = {
                                                if (currentRoute != item.route) {
                                                    navController.navigate(item.route) {
                                                        popUpTo(navController.graph.startDestinationId) {
                                                            saveState = true
                                                        }
                                                        launchSingleTop = true
                                                        restoreState = true
                                                    }
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    ) { innerPadding ->
                        NavHost(
                            navController = navController,
                            startDestination = Screen.Home.route,
                            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
                        ) {
                            composable(Screen.Home.route) {
                                HomeScreen(
                                    viewModel = viewModel,
                                    onNavigateToAdd = { navController.navigate(Screen.Add.route) },
                                    onNavigateToScan = { navController.navigate(Screen.Scan.route) },
                                    onNavigateToDetail = { id -> navController.navigate(Screen.ItemDetail.createRoute(id)) }
                                )
                            }

                            composable(Screen.Passwords.route) {
                                PasswordScreen(
                                    viewModel = passwordViewModel,
                                    onNavigateToAdd = { navController.navigate(Screen.AddPassword.route) },
                                    onNavigateToDetail = { id -> navController.navigate(Screen.PasswordDetail.createRoute(id)) }
                                )
                            }

                            composable(Screen.AddPassword.route) {
                                AddPasswordScreen(
                                    viewModel = passwordViewModel,
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            composable(
                                route = Screen.PasswordDetail.route,
                                arguments = listOf(navArgument("id") { type = NavType.LongType })
                            ) { backStackEntry ->
                                val id = backStackEntry.arguments?.getLong("id") ?: 0L
                                PasswordDetailScreen(
                                    id = id,
                                    viewModel = passwordViewModel,
                                    onBack = { navController.popBackStack() },
                                    onNavigateToEdit = { editId -> navController.navigate(Screen.EditPassword.createRoute(editId)) }
                                )
                            }

                            composable(
                                route = Screen.EditPassword.route,
                                arguments = listOf(navArgument("id") { type = NavType.LongType })
                            ) { backStackEntry ->
                                val id = backStackEntry.arguments?.getLong("id") ?: 0L
                                EditPasswordScreen(
                                    id = id,
                                    viewModel = passwordViewModel,
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            composable(Screen.StrongPassword.route) {
                                StrongPasswordScreen(
                                    onNavigateToHistory = { navController.navigate(Screen.StrongPasswordHistory.route) }
                                )
                            }

                            composable(Screen.StrongPasswordHistory.route) {
                                StrongPasswordHistoryScreen(
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            composable(Screen.Scan.route) {
                                ScanScreen(
                                    onScanResult = { uri ->
                                        navController.popBackStack()
                                        navController.navigate(Screen.ScanResult.createRoute(uri))
                                    },
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            composable(Screen.Add.route) {
                                AddScreen(
                                    viewModel = viewModel,
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            composable(
                                route = Screen.ScanResult.route,
                                arguments = listOf(navArgument("uri") { type = NavType.StringType })
                            ) { backStackEntry ->
                                val uri = backStackEntry.arguments?.getString("uri") ?: ""
                                ScanResultScreen(
                                    uri = uri,
                                    viewModel = viewModel,
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            composable(
                                route = Screen.ItemDetail.route,
                                arguments = listOf(navArgument("id") { type = NavType.LongType })
                            ) { backStackEntry ->
                                val id = backStackEntry.arguments?.getLong("id") ?: 0L
                                ItemDetailScreen(
                                    id = id,
                                    viewModel = viewModel,
                                    onBack = { navController.popBackStack() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}