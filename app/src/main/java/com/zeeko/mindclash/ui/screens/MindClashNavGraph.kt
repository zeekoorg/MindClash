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

    NavHost(navController = navController, startDestination = "splash") {
        
        composable("splash") {
            SplashScreen(onNavigateToHome = {
                navController.navigate("home") { popUpTo("splash") { inclusive = true } }
            })
        }

        composable("home") {
            HomeScreen(
                onNavigateToGame = { level -> navController.navigate("game/$level") },
                onNavigateToStore = { navController.navigate("store") },
                onNavigateToSurvival = { navController.navigate("survival") } // ✨ هنا حل الخطأ الأول!
            )
        }

        composable("store") {
            StoreScreen(
                adManager = adManager,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // مسار طور النجاة (التحدي بالزمن)
        composable("survival") {
            SurvivalScreen(
                adManager = adManager,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = "game/{level}", arguments = listOf(navArgument("level") { type = NavType.IntType })) { backStackEntry ->
            val level = backStackEntry.arguments?.getInt("level") ?: 1
            GameScreen(level = level, adManager = adManager, onNavigateBack = { navController.popBackStack() })
        }
    }
}
