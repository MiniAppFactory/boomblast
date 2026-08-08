package com.example.ui.navigation

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ads.BannerAdView
import com.example.ads.RewardedAdManager
import com.example.game.LevelGenerator
import com.example.ui.BlastViewModel
import com.example.ui.consent.TermsAcceptScreen
import com.example.ui.games.blockblast.BlockBlastGame
import com.example.ui.levels.LevelMapScreen
import com.example.ui.missions.MissionsScreen
import com.example.ui.modeselect.ModeSelectScreen
import com.example.ui.onboarding.OnboardingScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.shop.LoadoutScreen

object Routes {
    const val MODE_SELECT = "mode_select"
    const val LEVEL_MAP = "level_map"
    const val LOADOUT = "loadout/{level}"
    const val GAME = "game/{level}"
    const val ENDLESS_GAME = "endless_game"
    const val MISSIONS = "missions"
    const val SETTINGS = "settings"

    fun loadout(level: Int) = "loadout/$level"
    fun game(level: Int) = "game/$level"
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
fun AppNavigation(viewModel: BlastViewModel, adsConsentResolved: Boolean) {
    val navController = rememberNavController()
    val progress by viewModel.playerProgress.collectAsStateWithLifecycle()
    val missions by viewModel.weeklyMissions.collectAsStateWithLifecycle()
    val context = LocalContext.current

    NavHost(navController = navController, startDestination = Routes.MODE_SELECT) {
        composable(Routes.MODE_SELECT) {
            // Banner sadece menu ekraninda — oyun/izgara alanina asla eklenmiyor
            // (bkz. plan: "Banner ads only in places where they do not damage gameplay").
            Box(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.weight(1f)) {
                        ModeSelectScreen(
                            isTr = progress.isTr,
                            darkMode = progress.darkMode,
                            tokens = progress.tokens,
                            endlessBestScore = progress.endlessHighScore,
                            highestUnlockedLevel = progress.highestUnlockedLevel,
                            onOpenLevels = { navController.navigate(Routes.LEVEL_MAP) },
                            onOpenEndless = { navController.navigate(Routes.ENDLESS_GAME) },
                            onOpenMissions = { navController.navigate(Routes.MISSIONS) },
                            onOpenSettings = { navController.navigate(Routes.SETTINGS) }
                        )
                    }
                    if (adsConsentResolved) {
                        BannerAdView()
                    }
                }

                if (!progress.hasAcceptedTerms) {
                    TermsAcceptScreen(
                        isTr = progress.isTr,
                        darkMode = progress.darkMode,
                        onAccept = { viewModel.markTermsAccepted() }
                    )
                } else if (!progress.hasSeenOnboarding) {
                    OnboardingScreen(
                        isTr = progress.isTr,
                        darkMode = progress.darkMode,
                        onFinish = { viewModel.markOnboardingSeen() }
                    )
                }
            }
        }

        composable(Routes.LEVEL_MAP) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.weight(1f)) {
                    LevelMapScreen(
                        progress = progress,
                        isTr = progress.isTr,
                        darkMode = progress.darkMode,
                        onSelectLevel = { level -> navController.navigate(Routes.loadout(level)) },
                        onOpenMissions = { navController.navigate(Routes.MISSIONS) },
                        onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                        onBack = { navController.popBackStack() }
                    )
                }
                if (adsConsentResolved) {
                    BannerAdView()
                }
            }
        }

        composable(
            Routes.LOADOUT,
            arguments = listOf(navArgument("level") { type = NavType.IntType })
        ) { backStackEntry ->
            val level = backStackEntry.arguments?.getInt("level") ?: 1
            val definition = LevelGenerator.forLevel(level)
            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.weight(1f)) {
                    LoadoutScreen(
                        levelNumber = level,
                        targetScore = definition.targetScore,
                        progress = progress,
                        isTr = progress.isTr,
                        darkMode = progress.darkMode,
                        onBuyBooster = { type -> viewModel.buyBooster(type) },
                        onWatchAdForTokens = {
                            val activity = context.findActivity()
                            if (activity != null) {
                                RewardedAdManager.loadAndShow(
                                    context = context,
                                    activity = activity,
                                    onRewardEarned = { viewModel.watchAdForTokens() },
                                    onFailure = { /* odul verilmez, oyun akisi bloklanmaz */ }
                                )
                            }
                        },
                        onStartLevel = { navController.navigate(Routes.game(level)) },
                        onBack = { navController.popBackStack() }
                    )
                }
                if (adsConsentResolved) {
                    BannerAdView()
                }
            }
        }

        composable(
            Routes.GAME,
            arguments = listOf(navArgument("level") { type = NavType.IntType })
        ) { backStackEntry ->
            val level = backStackEntry.arguments?.getInt("level") ?: 1
            val definition = LevelGenerator.forLevel(level)
            BlockBlastGame(
                levelNumber = level,
                targetScore = definition.targetScore,
                shapePoolTier = definition.shapePoolTier,
                currentTheme = progress.blockTheme,
                isTr = progress.isTr,
                soundEnabled = progress.soundEnabled,
                darkMode = progress.darkMode,
                initialBoosterCounts = progress.ownedBoosters,
                onSelectTheme = { theme -> viewModel.setBlockTheme(theme) },
                onUseBooster = { type -> viewModel.consumeBoosterFromInventory(type) },
                onLinesCleared = { count -> viewModel.recordLinesCleared(count) },
                onBack = { navController.popBackStack(Routes.LEVEL_MAP, inclusive = false) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                onLevelComplete = { score, stars -> viewModel.recordLevelComplete(level, score, stars) },
                onLevelFailed = { /* skor kaybedildi, oyuncu "TEKRAR DENE"/"HARİTAYA DÖN" ile devam eder */ }
            )
        }

        composable(Routes.ENDLESS_GAME) {
            // Sonsuz Mod oturumlari seviye oyunlarindan cok daha uzun surebiliyor —
            // level GAME ekraninin aksine burada banner alt cubukta gosteriliyor
            // (kullanici geri bildirimi: uzun oturumlarda hic reklam gorunmuyordu).
            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.weight(1f)) {
                    BlockBlastGame(
                        levelNumber = 0,
                        targetScore = Int.MAX_VALUE,
                        isEndless = true,
                        bestScore = progress.endlessHighScore,
                        currentTheme = progress.blockTheme,
                        isTr = progress.isTr,
                        soundEnabled = progress.soundEnabled,
                        darkMode = progress.darkMode,
                        initialBoosterCounts = progress.ownedBoosters,
                        onSelectTheme = { theme -> viewModel.setBlockTheme(theme) },
                        onUseBooster = { type -> viewModel.consumeBoosterFromInventory(type) },
                        onLinesCleared = { count -> viewModel.recordLinesCleared(count) },
                        onBack = { navController.popBackStack() },
                        onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                        onEndlessGameOver = { score -> viewModel.recordEndlessScore(score) },
                        onRequestContinueAd = { onGranted, onDenied ->
                            val activity = context.findActivity()
                            if (activity != null) {
                                RewardedAdManager.loadAndShow(
                                    context = context,
                                    activity = activity,
                                    onRewardEarned = onGranted,
                                    onFailure = onDenied
                                )
                            } else {
                                onDenied()
                            }
                        }
                    )
                }
                if (adsConsentResolved) {
                    BannerAdView()
                }
            }
        }

        composable(Routes.MISSIONS) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.weight(1f)) {
                    MissionsScreen(
                        missionProgress = missions,
                        isTr = progress.isTr,
                        darkMode = progress.darkMode,
                        onClaim = { id -> viewModel.claimMission(id) },
                        onBack = { navController.popBackStack() }
                    )
                }
                if (adsConsentResolved) {
                    BannerAdView()
                }
            }
        }

        composable(Routes.SETTINGS) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.weight(1f)) {
                    SettingsScreen(
                        soundEnabled = progress.soundEnabled,
                        musicEnabled = progress.musicEnabled,
                        darkMode = progress.darkMode,
                        isTr = progress.isTr,
                        onToggleSound = { viewModel.setSoundEnabled(it) },
                        onToggleMusic = { viewModel.setMusicEnabled(it) },
                        onToggleDarkMode = { viewModel.setDarkMode(it) },
                        onSelectLanguage = { viewModel.setLanguage(it) },
                        onBack = { navController.popBackStack() }
                    )
                }
                if (adsConsentResolved) {
                    BannerAdView()
                }
            }
        }
    }
}
