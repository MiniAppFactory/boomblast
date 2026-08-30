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

    private fun render(name: String, theme: MapTheme, label: String, base: Int) {
        rule.setContent {
            ProgressionMapScreen(
                theme = theme,
                modeLabel = label,
                progress = PlayerProgress(),
                highestUnlockedLevel = 1,
                targetScoreForLevel = { base + (it - 1) * 5 },
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
        File(dir, "$name.png").outputStream().use {
            bmp.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        println("RENDER_OUT=" + File(dir, "$name.png").absolutePath)
    }

    @Test
    fun renderCareer() = render("v3_career", CareerMapTheme, "KARİYER İLERLEMESİ", 100)

    @Test
    fun renderEasy() = render("v3_easy", EasyMapTheme, "KOLAY MOD İLERLEMESİ", 100)

    @Test
    fun renderPro() = render("v3_pro", ProMapTheme, "PRO MOD İLERLEMESİ", 200)
}
