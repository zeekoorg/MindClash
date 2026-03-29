package com.zeeko.mindclash.ui.screens

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.zeeko.mindclash.ads.AdManager

@Composable
fun MindClashNavGraph(adManager: AdManager) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val sharedPreferences = remember { context.getSharedPreferences("MindClashPrefs", Context.MODE_PRIVATE) }
    
    // ✨ التحقق هل لعب اللاعب الافتتاحية السينمائية من قبل؟
    val isIntroComplete = sharedPreferences.getBoolean("IntroOverloadComplete", false)

    NavHost(navController = navController, startDestination = "splash") {
        
        // 1. شاشة البداية (Splash)
        composable("splash") {
            SplashScreen(onNavigateToHome = {
                // توجيه ذكي: إذا أكمل الافتتاحية يذهب للرئيسية، وإلا يذهب لشاشة الأكشن!
                val nextScreen = if (isIntroComplete) "home" else "intro_cinematic"
                navController.navigate(nextScreen) { popUpTo("splash") { inclusive = true } }
            })
        }

        // ✨ 2. شاشة الأكشن السينمائية الجديدة (تظهر مرة واحدة فقط)
        composable("intro_cinematic") {
            IntroNeuralOverloadScreen(
                onComplete = {
                    navController.navigate("home") { 
                        popUpTo("intro_cinematic") { inclusive = true } 
                    }
                }
            )
        }

        // 3. الشاشة الرئيسية
        composable("home") {
            HomeScreen(
                onNavigateToGame = { level -> navController.navigate("game/$level") },
                onNavigateToStore = { navController.navigate("store") },
                onNavigateToSurvival = { navController.navigate("survival") },
                onNavigateToWheel = { navController.navigate("wheel") } 
            )
        }

        // 4. المتجر (السوق السوداء)
        composable("store") {
            StoreScreen(
                adManager = adManager,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 5. عجلة الحظ (عجلة المصير)
        composable("wheel") {
            WheelScreen(
                adManager = adManager,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 6. طور صراع الزمن (تحدي النجاة)
        composable("survival") {
            SurvivalScreen(
                adManager = adManager,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 7. شاشة اللعب الأساسية
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
