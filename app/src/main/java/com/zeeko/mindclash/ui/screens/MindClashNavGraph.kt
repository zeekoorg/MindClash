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
        
        // 1. شاشة البداية (Splash)
        composable("splash") {
            SplashScreen(onNavigateToHome = {
                navController.navigate("home") { popUpTo("splash") { inclusive = true } }
            })
        }

        // 2. الشاشة الرئيسية (تعديل ليدعم العجلة) ✨
        composable("home") {
            HomeScreen(
                onNavigateToGame = { level -> navController.navigate("game/$level") },
                onNavigateToStore = { navController.navigate("store") },
                onNavigateToSurvival = { navController.navigate("survival") },
                onNavigateToWheel = { navController.navigate("wheel") } // ✨ إضافة التنقل للعجلة
            )
        }

        // 3. المتجر (السوق السوداء)
        composable("store") {
            StoreScreen(
                adManager = adManager,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 4. عجلة الحظ (عجلة المصير) ✨ شاشة جديدة
        composable("wheel") {
            WheelScreen(
                adManager = adManager,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 5. طور صراع الزمن (تحدي النجاة)
        composable("survival") {
            SurvivalScreen(
                adManager = adManager,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 6. شاشة اللعب الأساسية
        composable(
            route = "game/{level}", 
            arguments = listOf(navArgument("level") { type = NavType.IntType })
        ) { backStackEntry ->
            val level = backStackEntry.arguments?.getInt("level") ?: 1
            GameScreen(
                level = level, 
                adManager = adManager, 
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
