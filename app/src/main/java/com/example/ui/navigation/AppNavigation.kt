package com.example.ui.navigation

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ads.BannerAdView
import com.example.ads.InterstitialAdManager
import com.example.ads.RewardedAdManager
import com.example.data.AppLanguage
import com.example.data.pick
import com.example.game.LevelGenerator
import com.example.ui.BlastViewModel
import com.example.ui.consent.TermsAcceptScreen
import com.example.ui.games.boomblocks.BlastTheBlocksGame
import com.example.ui.games.retro.RetroGameScreen
import com.example.ui.games.retro.RetroHighScoreScreen
import com.example.ui.games.retro.RetroMenuScreen
import com.example.ui.games.retro.RetroSettingsScreen
import com.example.ui.help.HowToPlayScreen
import com.example.ui.levels.LevelMapScreen
import com.example.ui.missions.MissionsScreen
import com.example.ui.modeselect.ModeSelectScreen
import com.example.ui.onboarding.OnboardingScreen
import com.example.ui.retro.RetroViewModel
import com.example.ui.settings.SettingsScreen
import com.example.ui.shop.LoadoutScreen
import com.example.ui.theme.BlastSkin
import com.example.ui.theme.retro.getThemePalette
import com.example.utils.SoundManager

object Routes {
    const val MODE_SELECT = "mode_select"
    const val LEVEL_MAP = "level_map"
    const val LOADOUT = "loadout/{level}"
    const val GAME = "game/{level}"
    const val ENDLESS_GAME = "endless_game"
    const val MISSIONS = "missions"
    const val SETTINGS = "settings"
    // Faz 81: Ayarlar altina "Nasil Oynanir / Modlar" sayfasi (Gorev #21).
    const val HOW_TO_PLAY = "how_to_play"

    // Faz 77: Pro Mode (eski "Challenge") — Seviyeli Mod'un AYNI harita/loadout/
    // oyun akisini, kendi ilerlemesi + can sistemiyle kullanir.
    const val CHALLENGE_MAP = "challenge_map"
    const val CHALLENGE_LOADOUT = "challenge_loadout/{level}"
    const val CHALLENGE_GAME = "challenge_game/{level}"

    // Faz 78: Retro Modu — AI Studio'da uretilen bagimsiz Tetris motoru/UI'sinin
    // Boom Blocks'a route olarak entegrasyonu (kendi ic ScreenState/Crossfade
    // navigasyonu yerine).
    const val RETRO_MENU = "retro_menu"
    const val RETRO_GAME = "retro_game"
    const val RETRO_HIGH_SCORES = "retro_high_scores"
    const val RETRO_SETTINGS = "retro_settings"

    fun loadout(level: Int) = "loadout/$level"
    fun game(level: Int) = "game/$level"
    fun challengeLoadout(level: Int) = "challenge_loadout/$level"
    fun challengeGame(level: Int) = "challenge_game/$level"
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

// Faz 65: kullanici video kanitiyla bildirdi — reklam butonlarina basinca
// yukleniyor gostergesi cikiyor, ama reklam gelmeyince (no-fill, GERCEK
// AdMob envanteri sorunudur, kod hatasi degil — S8 test cihazinda ayni kod
// yolu guvenilir sekilde calisiyor) buton SESSIZCE eski haline donuyor,
// kullaniciya "neden hicbir sey olmadi" konusunda hicbir sinyal verilmiyordu.
// Butonun "bozuk" hissi vermemesi icin kisa bir Toast ile aciklama eklendi.
private fun showAdUnavailableToast(context: Context, language: AppLanguage) {
    Toast.makeText(
        context,
        language.pick(
            tr = "Şu anda reklam yok, birazdan tekrar dene",
            en = "No ad available right now, try again shortly",
            it = "Nessun annuncio disponibile ora, riprova a breve",
            fr = "Aucune pub disponible pour le moment, réessayez bientôt",
            es = "No hay anuncios disponibles ahora, inténtalo en un momento"
        ),
        Toast.LENGTH_SHORT
    ).show()
}

@Composable
fun AppNavigation(viewModel: BlastViewModel, retroViewModel: RetroViewModel, adsConsentResolved: Boolean) {
    val navController = rememberNavController()
    val progress by viewModel.playerProgress.collectAsStateWithLifecycle()
    val missions by viewModel.weeklyMissions.collectAsStateWithLifecycle()
    val retroHighScoreState by retroViewModel.highestScoreEver.collectAsStateWithLifecycle()
    val retroHighScore = retroHighScoreState ?: 0
    val context = LocalContext.current
    val skin = BlastSkin.fromId(progress.uiSkin)
    val onSelectSkin: (BlastSkin) -> Unit = { viewModel.setUiSkin(it.name) }

    // Faz 27: ses siddeti degistikce SoundManager'in calma-anindaki hacmini
    // guncelle — tum ekranlarda tek noktadan uygulanir.
    LaunchedEffect(progress.soundVolume) {
        SoundManager.setVolume(progress.soundVolume)
    }

    NavHost(navController = navController, startDestination = Routes.MODE_SELECT) {
        composable(Routes.MODE_SELECT) {
            // Banner sadece menu ekraninda — oyun/izgara alanina asla eklenmiyor
            // (bkz. plan: "Banner ads only in places where they do not damage gameplay").
            Box(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.weight(1f)) {
                        ModeSelectScreen(
                            language = progress.language,
                            darkMode = progress.darkMode,
                            skin = skin,
                            tokens = progress.tokens,
                            endlessBestScore = progress.endlessHighScore,
                            highestUnlockedLevel = progress.highestUnlockedLevel,
                            highestChallengeLevel = progress.challengeHighestUnlockedLevel,
                            retroHighScore = retroHighScore,
                            onOpenLevels = { navController.navigate(Routes.LEVEL_MAP) },
                            onOpenEndless = { navController.navigate(Routes.ENDLESS_GAME) },
                            onOpenChallenge = { navController.navigate(Routes.CHALLENGE_MAP) },
                            onOpenRetro = { navController.navigate(Routes.RETRO_MENU) },
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
                        language = progress.language,
                        darkMode = progress.darkMode,
                        skin = skin,
                        onAccept = { viewModel.markTermsAccepted() }
                    )
                } else if (!progress.hasSeenOnboarding) {
                    OnboardingScreen(
                        language = progress.language,
                        onSelectLanguage = { viewModel.setLanguage(it) },
                        darkMode = progress.darkMode,
                        skin = skin,
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
                        highestUnlockedLevel = progress.highestUnlockedLevel,
                        levelStars = progress.levelStars,
                        targetScoreForLevel = { level -> LevelGenerator.forLevel(level).targetScore },
                        language = progress.language,
                        darkMode = progress.darkMode,
                        skin = skin,
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
            // Faz 43: RewardedAd.load() 3-8 saniye surebiliyor, kullanici "watch
            // butonuna tıklayamadım" dedi — aslinda tiklama calisiyordu ama sessiz
            // bekleme suresi butonu bozuk gosteriyordu. Artik yukleme durumu
            // LoadoutScreen'e bildirilip buton bir spinner'a donusuyor.
            var isWatchAdLoading by remember { mutableStateOf(false) }
            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.weight(1f)) {
                    LoadoutScreen(
                        levelNumber = level,
                        targetScore = definition.targetScore,
                        progress = progress,
                        language = progress.language,
                        darkMode = progress.darkMode,
                        skin = skin,
                        onBuyBooster = { type -> viewModel.buyBooster(type) },
                        isWatchAdLoading = isWatchAdLoading,
                        onWatchAdForTokens = {
                            val activity = context.findActivity()
                            if (activity != null && !isWatchAdLoading) {
                                isWatchAdLoading = true
                                RewardedAdManager.loadAndShow(
                                    context = context,
                                    activity = activity,
                                    onRewardEarned = { viewModel.watchAdForTokens() },
                                    onFailure = { showAdUnavailableToast(context, progress.language) },
                                    onAdClosed = { isWatchAdLoading = false }
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
            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.weight(1f)) {
                    BlastTheBlocksGame(
                        levelNumber = level,
                        targetScore = definition.targetScore,
                        shapePoolTier = definition.shapePoolTier,
                        currentTheme = progress.blockTheme,
                        language = progress.language,
                        soundEnabled = progress.soundEnabled,
                        darkMode = progress.darkMode,
                        initialBoosterCounts = progress.ownedBoosters,
                        onSelectTheme = { theme -> viewModel.setBlockTheme(theme) },
                        onUseBooster = { type -> viewModel.consumeBoosterFromInventory(type) },
                        onLinesCleared = { count -> viewModel.recordLinesCleared(count) },
                        onMultiClear = { viewModel.recordMultiClear() },
                        onBack = { navController.popBackStack(Routes.LEVEL_MAP, inclusive = false) },
                        musicEnabled = progress.musicEnabled,
                        soundVolume = progress.soundVolume,
                        onToggleSound = { viewModel.setSoundEnabled(it) },
                        onSoundVolumeChange = { viewModel.setSoundVolume(it) },
                        onToggleMusic = { viewModel.setMusicEnabled(it) },
                        onToggleDarkMode = { viewModel.setDarkMode(it) },
                        onSelectLanguage = { viewModel.setLanguage(it) },
                        uiSkin = skin,
                        onSelectSkin = onSelectSkin,
                        notificationsEnabled = progress.notificationsEnabled,
                        onToggleNotifications = { viewModel.setNotificationsEnabled(it) },
                        hasMadeFirstMove = progress.hasMadeFirstMove,
                        onFirstMoveMade = { viewModel.markFirstMoveMade() },
                        onLevelComplete = { score, stars -> viewModel.recordLevelComplete(level, score, stars) },
                        onLevelCompleteContinue = {
                            // Faz 39: zorunlu gecis reklami — yeterli reklam gosterimi
                            // olmadan oyuncu seviyeleri ucretsiz geçebiliyordu, bu da
                            // reklam gelirini cok dusuruyordu. Faz 42: kullanici istegiyle
                            // "her 2 bolumde bir" -> "her bolum sonrasi" (esik 2 -> 1).
                            val activity = context.findActivity()
                            if (progress.levelsCompletedSinceInterstitial >= 1 && activity != null) {
                                viewModel.resetLevelsSinceInterstitial()
                                InterstitialAdManager.loadAndShow(
                                    context = context,
                                    activity = activity,
                                    onProceed = {
                                        navController.popBackStack(Routes.LEVEL_MAP, inclusive = false)
                                    }
                                )
                            } else {
                                navController.popBackStack(Routes.LEVEL_MAP, inclusive = false)
                            }
                        },
                        onLevelFailed = { /* skor kaybedildi, oyuncu "TEKRAR DENE"/"HARİTAYA DÖN" ile devam eder */ },
                        // Faz 61: bu kanca daha once HIC baglanmamisti — varsayilan deger
                        // aninda onDenied() cagirdigi icin Seviyeli Mod'daki "REKLAM İZLE,
                        // DEVAM ET" butonu gercekte HICBIR ZAMAN reklam yuklemiyordu
                        // (kullanici: "hala reklam çağıramıyor"). Routes.ENDLESS_GAME'deki
                        // ile BIREBIR ayni RewardedAdManager kablolamasi.
                        onRequestContinueAd = { onGranted, onDenied ->
                            val activity = context.findActivity()
                            if (activity != null) {
                                var handled = false
                                RewardedAdManager.loadAndShow(
                                    context = context,
                                    activity = activity,
                                    onRewardEarned = { handled = true; onGranted() },
                                    onFailure = { handled = true; showAdUnavailableToast(context, progress.language); onDenied() },
                                    onAdClosed = { if (!handled) { handled = true; onDenied() } }
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

        // Faz 77/79: Pro Mode (Challenge) — Seviyeli Mod'un AYNI ekranlarini
        // (LevelMapScreen/LoadoutScreen/BlastTheBlocksGame) kendi ilerleme +
        // hedef egrisi + can sistemiyle kullanir. Can artik bolume GIRERKEN
        // degil, kaybedip "YENIDEN BASLA" secilince harcaniyor (bkz. Game Over
        // modalindeki Pro Mode'a ozel buton, BoomBlocksGame.kt).
        composable(Routes.CHALLENGE_MAP) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.weight(1f)) {
                    LevelMapScreen(
                        progress = progress,
                        highestUnlockedLevel = progress.challengeHighestUnlockedLevel,
                        levelStars = progress.challengeLevelStars,
                        targetScoreForLevel = { level -> LevelGenerator.forChallengeLevel(level).targetScore },
                        isChallengeMode = true,
                        language = progress.language,
                        darkMode = progress.darkMode,
                        skin = skin,
                        onSelectLevel = { level -> navController.navigate(Routes.challengeLoadout(level)) },
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
            Routes.CHALLENGE_LOADOUT,
            arguments = listOf(navArgument("level") { type = NavType.IntType })
        ) { backStackEntry ->
            val level = backStackEntry.arguments?.getInt("level") ?: 1
            val definition = LevelGenerator.forChallengeLevel(level)
            var isWatchAdLoading by remember { mutableStateOf(false) }
            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.weight(1f)) {
                    LoadoutScreen(
                        levelNumber = level,
                        targetScore = definition.targetScore,
                        progress = progress,
                        language = progress.language,
                        darkMode = progress.darkMode,
                        skin = skin,
                        onBuyBooster = { type -> viewModel.buyBooster(type) },
                        isWatchAdLoading = isWatchAdLoading,
                        onWatchAdForTokens = {
                            val activity = context.findActivity()
                            if (activity != null && !isWatchAdLoading) {
                                isWatchAdLoading = true
                                RewardedAdManager.loadAndShow(
                                    context = context,
                                    activity = activity,
                                    onRewardEarned = { viewModel.watchAdForTokens() },
                                    onFailure = { showAdUnavailableToast(context, progress.language) },
                                    onAdClosed = { isWatchAdLoading = false }
                                )
                            }
                        },
                        onStartLevel = { navController.navigate(Routes.challengeGame(level)) },
                        onBack = { navController.popBackStack() }
                    )
                }
                if (adsConsentResolved) {
                    BannerAdView()
                }
            }
        }

        composable(
            Routes.CHALLENGE_GAME,
            arguments = listOf(navArgument("level") { type = NavType.IntType })
        ) { backStackEntry ->
            val level = backStackEntry.arguments?.getInt("level") ?: 1
            val definition = LevelGenerator.forChallengeLevel(level)
            var isWatchAdForLifeLoading by remember { mutableStateOf(false) }
            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.weight(1f)) {
                    BlastTheBlocksGame(
                        levelNumber = level,
                        targetScore = definition.targetScore,
                        shapePoolTier = definition.shapePoolTier,
                        scoreMultiplier = definition.scoreMultiplier,
                        isChallengeMode = true,
                        challengeLives = progress.challengeLives,
                        onChallengeRestart = { viewModel.spendChallengeLifeForRestart() },
                        onChallengeWatchAdForLife = {
                            val activity = context.findActivity()
                            if (activity != null && !isWatchAdForLifeLoading) {
                                isWatchAdForLifeLoading = true
                                RewardedAdManager.loadAndShow(
                                    context = context,
                                    activity = activity,
                                    onRewardEarned = { viewModel.grantChallengeLife() },
                                    onFailure = { showAdUnavailableToast(context, progress.language) },
                                    onAdClosed = { isWatchAdForLifeLoading = false }
                                )
                            }
                        },
                        currentTheme = progress.blockTheme,
                        language = progress.language,
                        soundEnabled = progress.soundEnabled,
                        darkMode = progress.darkMode,
                        initialBoosterCounts = progress.ownedBoosters,
                        onSelectTheme = { theme -> viewModel.setBlockTheme(theme) },
                        onUseBooster = { type -> viewModel.consumeBoosterFromInventory(type) },
                        onLinesCleared = { count -> viewModel.recordLinesCleared(count) },
                        onMultiClear = { viewModel.recordMultiClear() },
                        onBack = { navController.popBackStack(Routes.CHALLENGE_MAP, inclusive = false) },
                        musicEnabled = progress.musicEnabled,
                        soundVolume = progress.soundVolume,
                        onToggleSound = { viewModel.setSoundEnabled(it) },
                        onSoundVolumeChange = { viewModel.setSoundVolume(it) },
                        onToggleMusic = { viewModel.setMusicEnabled(it) },
                        onToggleDarkMode = { viewModel.setDarkMode(it) },
                        onSelectLanguage = { viewModel.setLanguage(it) },
                        uiSkin = skin,
                        onSelectSkin = onSelectSkin,
                        notificationsEnabled = progress.notificationsEnabled,
                        onToggleNotifications = { viewModel.setNotificationsEnabled(it) },
                        hasMadeFirstMove = progress.hasMadeFirstMove,
                        onFirstMoveMade = { viewModel.markFirstMoveMade() },
                        onLevelComplete = { score, stars -> viewModel.recordChallengeLevelComplete(level, score, stars) },
                        onLevelCompleteContinue = {
                            // Faz 77: basitlik icin Seviyeli Mod'daki AYNI
                            // "levelsCompletedSinceInterstitial" sayacini
                            // paylasiyor — iki modda birlesik "her bolum"
                            // esigi, ayri bir sayac eklemeye gerek yok.
                            val activity = context.findActivity()
                            if (progress.levelsCompletedSinceInterstitial >= 1 && activity != null) {
                                viewModel.resetLevelsSinceInterstitial()
                                InterstitialAdManager.loadAndShow(
                                    context = context,
                                    activity = activity,
                                    onProceed = {
                                        navController.popBackStack(Routes.CHALLENGE_MAP, inclusive = false)
                                    }
                                )
                            } else {
                                navController.popBackStack(Routes.CHALLENGE_MAP, inclusive = false)
                            }
                        },
                        onLevelFailed = { /* skor kaybedildi, oyuncu "TEKRAR DENE"/"HARİTAYA DÖN" ile devam eder */ },
                        onRequestContinueAd = { onGranted, onDenied ->
                            val activity = context.findActivity()
                            if (activity != null) {
                                var handled = false
                                RewardedAdManager.loadAndShow(
                                    context = context,
                                    activity = activity,
                                    onRewardEarned = { handled = true; onGranted() },
                                    onFailure = { handled = true; showAdUnavailableToast(context, progress.language); onDenied() },
                                    onAdClosed = { if (!handled) { handled = true; onDenied() } }
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

        // Faz 78: Retro Modu — AI Studio'da uretilen Tetris motoru/UI'si.
        // retroViewModel Activity-omurlu TEK instance (bkz. MainActivity),
        // route'lar arasi state (motor/ayarlar) burada KAYBOLMUYOR.
        composable(Routes.RETRO_MENU) {
            val retroSettings by retroViewModel.settings.collectAsStateWithLifecycle()
            val retroHighest by retroViewModel.highestScoreEver.collectAsStateWithLifecycle()
            val retroPalette = getThemePalette(retroSettings.theme)
            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.weight(1f)) {
                    RetroMenuScreen(
                        settings = retroSettings,
                        highestScoreEver = retroHighest ?: 0,
                        palette = retroPalette,
                        language = progress.language,
                        onStartGame = {
                            retroViewModel.startNewGame()
                            navController.navigate(Routes.RETRO_GAME)
                        },
                        onSelectDifficulty = { diff -> retroViewModel.updateSettings(retroSettings.copy(difficultyPreset = diff)) },
                        onOpenHighScores = { navController.navigate(Routes.RETRO_HIGH_SCORES) },
                        onOpenSettings = { navController.navigate(Routes.RETRO_SETTINGS) },
                        onQuitToMain = { navController.popBackStack() }
                    )
                }
                if (adsConsentResolved) {
                    BannerAdView()
                }
            }
        }

        composable(Routes.RETRO_GAME) {
            val retroSettings by retroViewModel.settings.collectAsStateWithLifecycle()
            val retroHighest by retroViewModel.highestScoreEver.collectAsStateWithLifecycle()
            val retroPalette = getThemePalette(retroSettings.theme)
            Box(modifier = Modifier.fillMaxSize()) {
                RetroGameScreen(
                    gameEngine = retroViewModel.gameEngine,
                    settings = retroSettings,
                    palette = retroPalette,
                    highestScoreEver = retroHighest ?: 0,
                    language = progress.language,
                    onSaveHighScore = { name -> retroViewModel.saveHighScore(name) },
                    onRestartGame = { retroViewModel.startNewGame() },
                    onOpenSettings = { navController.navigate(Routes.RETRO_SETTINGS) },
                    onResume = { retroViewModel.resumeGame() },
                    onReturnToMenu = {
                        // Kullanicinin kendi istegi: her Retro oturumu (bir "game
                        // over") sonunda menuye donuste bir gecis reklami —
                        // Seviyeli Mod'daki gibi bir "her N bolum" esigi YOK,
                        // her donuste bir kez.
                        retroViewModel.pauseGame()
                        val activity = context.findActivity()
                        if (activity != null) {
                            InterstitialAdManager.loadAndShow(
                                context = context,
                                activity = activity,
                                onProceed = {
                                    navController.popBackStack(Routes.RETRO_MENU, inclusive = false)
                                }
                            )
                        } else {
                            navController.popBackStack(Routes.RETRO_MENU, inclusive = false)
                        }
                    }
                )
            }
        }

        composable(Routes.RETRO_HIGH_SCORES) {
            val retroSettings by retroViewModel.settings.collectAsStateWithLifecycle()
            val retroScores by retroViewModel.topScores.collectAsStateWithLifecycle()
            val retroTotalGames by retroViewModel.totalGamesPlayed.collectAsStateWithLifecycle()
            val retroTotalLines by retroViewModel.totalLinesCleared.collectAsStateWithLifecycle()
            val retroHighest by retroViewModel.highestScoreEver.collectAsStateWithLifecycle()
            val retroSelectedDiff by retroViewModel.selectedLeaderboardDifficulty.collectAsStateWithLifecycle()
            val retroPalette = getThemePalette(retroSettings.theme)
            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.weight(1f)) {
                    RetroHighScoreScreen(
                        scores = retroScores,
                        totalGamesPlayed = retroTotalGames,
                        totalLinesCleared = retroTotalLines ?: 0,
                        highestScoreEver = retroHighest ?: 0,
                        selectedDifficulty = retroSelectedDiff,
                        palette = retroPalette,
                        language = progress.language,
                        onSelectDifficultyFilter = { diff -> retroViewModel.selectLeaderboardDifficulty(diff) },
                        onBackToMenu = { navController.popBackStack() }
                    )
                }
                if (adsConsentResolved) {
                    BannerAdView()
                }
            }
        }

        composable(Routes.RETRO_SETTINGS) {
            val retroSettings by retroViewModel.settings.collectAsStateWithLifecycle()
            val retroPalette = getThemePalette(retroSettings.theme)
            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.weight(1f)) {
                    RetroSettingsScreen(
                        settings = retroSettings,
                        palette = retroPalette,
                        language = progress.language,
                        onUpdateSettings = { newSettings -> retroViewModel.updateSettings(newSettings) },
                        onBackToMenu = { navController.popBackStack() }
                    )
                }
                if (adsConsentResolved) {
                    BannerAdView()
                }
            }
        }

        composable(Routes.ENDLESS_GAME) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.weight(1f)) {
                    BlastTheBlocksGame(
                        levelNumber = 0,
                        targetScore = Int.MAX_VALUE,
                        isEndless = true,
                        bestScore = progress.endlessHighScore,
                        currentTheme = progress.blockTheme,
                        language = progress.language,
                        soundEnabled = progress.soundEnabled,
                        darkMode = progress.darkMode,
                        initialBoosterCounts = progress.ownedBoosters,
                        onSelectTheme = { theme -> viewModel.setBlockTheme(theme) },
                        onUseBooster = { type -> viewModel.consumeBoosterFromInventory(type) },
                        onLinesCleared = { count -> viewModel.recordLinesCleared(count) },
                        onMultiClear = { viewModel.recordMultiClear() },
                        onBack = { navController.popBackStack() },
                        musicEnabled = progress.musicEnabled,
                        soundVolume = progress.soundVolume,
                        onToggleSound = { viewModel.setSoundEnabled(it) },
                        onSoundVolumeChange = { viewModel.setSoundVolume(it) },
                        onToggleMusic = { viewModel.setMusicEnabled(it) },
                        onToggleDarkMode = { viewModel.setDarkMode(it) },
                        onSelectLanguage = { viewModel.setLanguage(it) },
                        uiSkin = skin,
                        onSelectSkin = onSelectSkin,
                        notificationsEnabled = progress.notificationsEnabled,
                        onToggleNotifications = { viewModel.setNotificationsEnabled(it) },
                        hasMadeFirstMove = progress.hasMadeFirstMove,
                        onFirstMoveMade = { viewModel.markFirstMoveMade() },
                        onEndlessGameOver = { score -> viewModel.recordEndlessScore(score) },
                        onRequestContinueAd = { onGranted, onDenied ->
                            val activity = context.findActivity()
                            if (activity != null) {
                                // Faz 48: kullanici "hamle kalmayınca reklam izle 3 hamle
                                // kazan mekanizması çalışmadı" dedi. Kok neden: kullanici
                                // reklami ODUL KAZANMADAN (video bitmeden) kapatirsa ne
                                // onRewardEarned ne onFailure hic tetiklenmiyordu —
                                // isRequestingContinueAd sonsuza kadar true kalip butonu
                                // KALICI OLARAK devre disi birakiyordu (bir daha hic
                                // tiklanamiyordu). `handled` bayragi + onAdClosed guvenlik
                                // agi ile artik reklam NASIL kapanirsa kapansin (odullu/
                                // odulsuz) en gec kapaninca onDenied() tetiklenip oturum
                                // duzgunce sonlandiriliyor, buton sonsuza dek kilitli kalmiyor.
                                var handled = false
                                RewardedAdManager.loadAndShow(
                                    context = context,
                                    activity = activity,
                                    onRewardEarned = { handled = true; onGranted() },
                                    onFailure = { handled = true; showAdUnavailableToast(context, progress.language); onDenied() },
                                    onAdClosed = { if (!handled) { handled = true; onDenied() } }
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
                        language = progress.language,
                        darkMode = progress.darkMode,
                        skin = skin,
                        onClaim = { id, tierIndex -> viewModel.claimMission(id, tierIndex) },
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
                        soundVolume = progress.soundVolume,
                        musicEnabled = progress.musicEnabled,
                        darkMode = progress.darkMode,
                        language = progress.language,
                        skin = skin,
                        onToggleSound = { viewModel.setSoundEnabled(it) },
                        onSoundVolumeChange = { viewModel.setSoundVolume(it) },
                        onToggleMusic = { viewModel.setMusicEnabled(it) },
                        onToggleDarkMode = { viewModel.setDarkMode(it) },
                        onSelectLanguage = { viewModel.setLanguage(it) },
                        onSelectSkin = onSelectSkin,
                        notificationsEnabled = progress.notificationsEnabled,
                        onToggleNotifications = { viewModel.setNotificationsEnabled(it) },
                        onOpenHowToPlay = { navController.navigate(Routes.HOW_TO_PLAY) },
                        onBack = { navController.popBackStack() }
                    )
                }
                if (adsConsentResolved) {
                    BannerAdView()
                }
            }
        }

        composable(Routes.HOW_TO_PLAY) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.weight(1f)) {
                    HowToPlayScreen(
                        language = progress.language,
                        darkMode = progress.darkMode,
                        skin = skin,
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
