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
 * FAZ 171 — KARIYER HARITASINI CIHAZ OLMADAN RENDER ET.
 *
 * Kullanicinin kurali, kendi sozleriyle:
 *
 *   "APK yapip bana test yaptirmak yerine APK yapmadan once render et,
 *    ben senin testcin degilim."
 *
 * Bu bir TEST degil ARAC: assertion yok, `build/screen-renders/` altina PNG
 * yazar. Uc modun ucunu birden cikarir, cunku fark yalnizca renkte degil
 * METINDE de var (Faz 171'e kadar Pro ve Kolay haritalarinda da "KARİYER
 * İLERLEMESİ" yaziyordu -- bu hata tam olarak ucunu yan yana koymadigim icin
 * uzun sure gorulmedi).
 *
 * Calistirmak icin:
 *   gradlew :app:testDebugUnitTest --tests "*LevelMapRenderTest*"
 *
 * NOT: `captureToImage()` KULLANILAMIYOR -- PixelCopy tabanli ve Robolectric'te
 * zaman asimina ugruyor. decorView dogrudan bir Canvas'a ciziliyor.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w411dp-h891dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class LevelMapRenderTest {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private fun save(name: String) {
        val view = rule.activity.window.decorView
        val bmp = createBitmap(view.width.coerceAtLeast(1), view.height.coerceAtLeast(1))
        view.draw(android.graphics.Canvas(bmp))
        val dir = File("build/screen-renders").apply { mkdirs() }
        File(dir, "$name.png").outputStream().use {
            bmp.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        println("RENDER_OUT=" + File(dir, "$name.png").absolutePath)
    }

    private fun render(name: String, challenge: Boolean, comfort: Boolean, targetBase: Int) {
        rule.setContent {
            LevelMapScreen(
                progress = PlayerProgress(),
                highestUnlockedLevel = 1,
                levelStars = emptyMap(),
                targetScoreForLevel = { level -> targetBase + (level - 1) * 5 },
                language = AppLanguage.TR,
                darkMode = true,
                skin = BlastSkin.DEFAULT,
                isChallengeMode = challenge,
                isComfortMode = comfort,
                onSelectLevel = {},
                onOpenMissions = {},
                onOpenSettings = {},
                onBack = {}
            )
        }
        rule.waitForIdle()
        save(name)
    }

    @Test
    fun renderCareerMap() = render("map_career", challenge = false, comfort = false, targetBase = 100)

    @Test
    fun renderProMap() = render("map_pro", challenge = true, comfort = false, targetBase = 200)

    @Test
    fun renderComfortMap() = render("map_comfort", challenge = false, comfort = true, targetBase = 100)
}
