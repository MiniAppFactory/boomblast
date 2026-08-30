package com.miniappfactory.boomblocks.ui.levels

import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.core.graphics.createBitmap
import com.miniappfactory.boomblocks.data.AppLanguage
import com.miniappfactory.boomblocks.data.PlayerProgress
import com.miniappfactory.boomblocks.ui.theme.BlastSkin
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

/**
 * FAZ 173: yeni varlik tabanli Kariyer haritasini cihaza dokunmadan render eder.
 * Kullanicinin kurali: "APK yapmadan once render et."
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w411dp-h891dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ProgressionMapRenderTest {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun renderCareer() {
        rule.setContent {
            ProgressionMapScreen(
                theme = CareerMapTheme,
                modeLabel = "KARİYER İLERLEMESİ",
                progress = PlayerProgress(),
                highestUnlockedLevel = 1,
                targetScoreForLevel = { 100 + (it - 1) * 5 },
                language = AppLanguage.TR,
                darkMode = true,
                skin = BlastSkin.DEFAULT,
                onSelectLevel = {},
                onOpenMissions = {},
                onOpenSettings = {},
                onBack = {}
            )
        }
        rule.waitForIdle()
        val view = rule.activity.window.decorView
        val bmp = createBitmap(view.width.coerceAtLeast(1), view.height.coerceAtLeast(1))
        view.draw(android.graphics.Canvas(bmp))
        val dir = File("build/screen-renders").apply { mkdirs() }
        File(dir, "career_v2.png").outputStream().use {
            bmp.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        println("RENDER_OUT=" + File(dir, "career_v2.png").absolutePath)
    }
}
