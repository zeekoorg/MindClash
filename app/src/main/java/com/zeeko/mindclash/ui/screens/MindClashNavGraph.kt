package com.zeeko.mindclash.ui.screens

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.zeeko.mindclash.ads.AdManager

@Composable
fun MindClashNavGraph(adManager: AdManager) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        
        composable("home") {
            HomeScreen(
                onNavigateToGame = { level ->
                    navController.navigate("game/$level")
                }
            )
        }

        composable(
            route = "game/{level}",
            arguments = listOf(navArgument("level") { type = NavType.IntType })
        ) { backStackEntry ->
            val level = backStackEntry.arguments?.getInt("level") ?: 1
            GameScreen(
                level = level,
                adManager = adManager,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
