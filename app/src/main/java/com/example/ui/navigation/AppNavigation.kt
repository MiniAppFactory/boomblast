package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.game.LevelGenerator
import com.example.ui.BlastViewModel
import com.example.ui.games.blockblast.BlockBlastGame
import com.example.ui.levels.LevelMapScreen
import com.example.ui.missions.MissionsScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.shop.LoadoutScreen

object Routes {
    const val LEVEL_MAP = "level_map"
    const val LOADOUT = "loadout/{level}"
    const val GAME = "game/{level}"
    const val MISSIONS = "missions"
    const val SETTINGS = "settings"

    fun loadout(level: Int) = "loadout/$level"
    fun game(level: Int) = "game/$level"
}

@Composable
fun AppNavigation(viewModel: BlastViewModel) {
    val navController = rememberNavController()
    val progress by viewModel.playerProgress.collectAsStateWithLifecycle()
    val missions by viewModel.weeklyMissions.collectAsStateWithLifecycle()

    NavHost(navController = navController, startDestination = Routes.LEVEL_MAP) {
        composable(Routes.LEVEL_MAP) {
            LevelMapScreen(
                progress = progress,
                isTr = progress.isTr,
                onSelectLevel = { level -> navController.navigate(Routes.loadout(level)) },
                onOpenMissions = { navController.navigate(Routes.MISSIONS) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }

        composable(
            Routes.LOADOUT,
            arguments = listOf(navArgument("level") { type = NavType.IntType })
        ) { backStackEntry ->
            val level = backStackEntry.arguments?.getInt("level") ?: 1
            val definition = LevelGenerator.forLevel(level)
            LoadoutScreen(
                levelNumber = level,
                targetScore = definition.targetScore,
                progress = progress,
                isTr = progress.isTr,
                onBuyBooster = { type -> viewModel.buyBooster(type) },
                onWatchAdForTokens = { viewModel.watchAdForTokens() },
                onStartLevel = { navController.navigate(Routes.game(level)) },
                onBack = { navController.popBackStack() }
            )
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
                initialBoosterCounts = progress.ownedBoosters,
                onSelectTheme = { theme -> viewModel.setBlockTheme(theme) },
                onUseBooster = { type -> viewModel.consumeBoosterFromInventory(type) },
                onLinesCleared = { count -> viewModel.recordLinesCleared(count) },
                onBack = { navController.popBackStack(Routes.LEVEL_MAP, inclusive = false) },
                onLevelComplete = { score, stars -> viewModel.recordLevelComplete(level, score, stars) },
                onLevelFailed = { /* skor kaybedildi, oyuncu "TEKRAR DENE"/"HARİTAYA DÖN" ile devam eder */ }
            )
        }

        composable(Routes.MISSIONS) {
            MissionsScreen(
                missionProgress = missions,
                isTr = progress.isTr,
                onClaim = { id -> viewModel.claimMission(id) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SETTINGS) {
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
    }
}
