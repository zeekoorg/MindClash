package com.zeeko.mindclash

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.zeeko.mindclash.presentation.components.NoInternetScreen
import com.zeeko.mindclash.presentation.screens.*
import com.zeeko.mindclash.presentation.theme.MindClashTheme
import com.zeeko.mindclash.utils.LanguageManager
import com.zeeko.mindclash.utils.NetworkUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var adManager: AdManager
    
    @Inject
    lateinit var languageManager: LanguageManager
    
    private var isNetworkAvailable by mutableStateOf(true)
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // مراقبة حالة الاتصال
        NetworkUtils.observeNetworkConnectivity(this)
            .onEach { isConnected ->
                isNetworkAvailable = isConnected
            }
            .launchIn(lifecycleScope)
        
        setContent {
            MindClashTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    
                    if (!isNetworkAvailable) {
                        NoInternetScreen(
                            onRetryClick = {
                                // إعادة المحاولة
                            }
                        )
                    } else {
                        NavHost(
                            navController = navController,
                            startDestination = "splash"
                        ) {
                            composable("splash") {
                                SplashScreen(
                                    onTimeout = {
                                        navController.popBackStack()
                                        navController.navigate("home")
                                    }
                                )
                            }
                            
                            composable("home") {
                                HomeScreen(
                                    onPlayClick = { difficulty ->
                                        navController.navigate("level_select/$difficulty")
                                    },
                                    onNewSetClick = {
                                        // عرض إعلان مكافأة للسيت الجديد
                                        adManager.showRewardedAd(this@MainActivity, "https://zeekoorg.github.io/daily-set.html")
                                    }
                                )
                            }
                            
                            composable("level_select/{difficulty}") { backStackEntry ->
                                val difficulty = backStackEntry.arguments?.getString("difficulty")?.toInt() ?: 1
                                LevelSelectScreen(
                                    difficulty = difficulty,
                                    onLevelSelected = { levelId ->
                                        navController.navigate("game/$difficulty/$levelId")
                                    },
                                    onBackPressed = {
                                        navController.popBackStack()
                                    }
                                )
                            }
                            
                            composable("game/{difficulty}/{levelId}") { backStackEntry ->
                                val difficulty = backStackEntry.arguments?.getString("difficulty")?.toInt() ?: 1
                                val levelId = backStackEntry.arguments?.getString("levelId")?.toInt() ?: 1
                                GameScreen(
                                    difficulty = difficulty,
                                    levelId = levelId,
                                    onBackPressed = {
                                        navController.popBackStack()
                                    },
                                    onGameComplete = { points ->
                                        // العودة للشاشة الرئيسية
                                        navController.popBackStack("home", false)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BannerAd(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val activity = context as? MainActivity
    val adManager = activity?.adManager
    
    AndroidView(
        modifier = modifier,
        factory = {
            adManager?.createBannerAdView(activity) ?: android.view.View(context)
        }
    )
}
