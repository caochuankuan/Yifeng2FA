package com.compose.yifeng2fa

import android.R.attr.type
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.compose.yifeng2fa.ui.AddScreen
import com.compose.yifeng2fa.ui.HomeScreen
import com.compose.yifeng2fa.ui.ScanScreen
import com.compose.yifeng2fa.ui.ScanResultScreen
import com.compose.yifeng2fa.ui.ItemDetailScreen
import com.compose.yifeng2fa.ui.Screen
import com.compose.yifeng2fa.ui.theme.Yifeng2FATheme
import com.compose.yifeng2fa.viewmodel.TotpViewModel

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

                    NavHost(
                        navController = navController,
                        startDestination = Screen.Home.route
                    ) {
                        composable(Screen.Home.route) {
                            HomeScreen(
                                viewModel = viewModel,
                                onNavigateToAdd = { navController.navigate(Screen.Add.route) },
                                onNavigateToScan = { navController.navigate(Screen.Scan.route) },
                                onNavigateToDetail = { id -> navController.navigate(Screen.ItemDetail.createRoute(id)) }
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