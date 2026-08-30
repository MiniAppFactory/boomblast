package com.miniappfactory.boomblocks.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.down
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import androidx.core.graphics.createBitmap
import com.miniappfactory.boomblocks.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

/**
 * Faz 167 — SONUC DIYALOGLARINI CIHAZ OLMADAN RENDER ET.
 *
 * Kullanicinin kurali: gorsel bir degisiklikte APK uretmeden ONCE ekran
 * goruntusu gosterip onay al. Cihaza dokunmadan bunu yapmanin yolu,
 * diyalogu URUNDEKI GERCEK bilesenlerle (ResultDialogTitle, ResultStatPanel,
 * GameButton) ve GERCEK varliklarla (kb_dlg_trophy, kb_dlg_heartbreak)
 * Robolectric'in native grafik modunda cizip PNG'ye almak.
 *
 * Bir TEST degil, bir ARAC: assertion yok, ciktiyi
 * `build/dialog-renders/` altina yazar. Calistirmak icin:
 *
 *   gradlew :app:testDebugUnitTest --tests "*ResultDialogRenderTest*"
 *
 * Diyalog govdesi `BoomBlocksGame` icinde gomulu oldugu icin burada
 * yeniden kuruluyor; ama malzemenin TAMAMI urunun kendi bilesenlerinden
 * geliyor, yani renk/kabartma/kontur birebir ayni.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w411dp-h891dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ResultDialogRenderTest {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private fun save(name: String, bitmap: Bitmap) {
        val dir = File("build/dialog-renders").apply { mkdirs() }
        File(dir, "$name.png").outputStream().use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        println("RENDER_OUT=" + File(dir, "$name.png").absolutePath)
    }

    /**
     * FAZ 181 — ARKA PLAN ARTIK CIHAZIN GERCEK OLCUSUNU DE ALABILIYOR.
     *
     * Varsayilan 411x760dp, kullanicinin SM-G965 (1080x2220 @480dpi = 360x740dp)
     * cihazinda "HARITAYA DON" butonunun YARIDAN KESILDIGI hatayi
     * UREMIYORDU: 411dp'de icerik zaten siğiyordu. Reklam banneri + durum
     * cubugu + gezinme cubugu dusulunce diyaloga kalan yer ~590dp; hatayi
     * goren olcu bu.
     */
    @Composable
    private fun Backdrop(
        accent: Color,
        widthDp: Int = 411,
        heightDp: Int = 760,
        content: @Composable () -> Unit
    ) {
        Box(
            modifier = Modifier
                .width(widthDp.dp)
                .height(heightDp.dp)
                .background(Color(0xFF05070E))
                .testTag("shot"),
            contentAlignment = Alignment.Center
        ) { content() }
    }

    @Composable
    private fun DialogCard(accent: Color, body: @Composable () -> Unit) {
        Card(
            colors = CardDefaults.cardColors(containerColor = ResultDialogSurface),
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(16.dp)
                .gameOuterGlow(accent = accent, cornerRadius = 28.dp, intensity = 1f)
                .border(3.dp, accent, RoundedCornerShape(28.dp))
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) { body() }
        }
    }

    @Test
    fun renderLevelFailed() {
        rule.setContent {
            Backdrop(ResultFailAccent) {
                DialogCard(ResultFailAccent) {
                    ResultDialogTitle("SEVİYE", "BAŞARISIZ", resultEmblemColors(ResultFailAccent))
                    Spacer(Modifier.height(10.dp))
                    Image(
                        painter = painterResource(R.drawable.kb_dlg_heartbreak),
                        contentDescription = null,
                        modifier = Modifier.size(96.dp)
                    )
                    Spacer(Modifier.height(14.dp))
                    ResultStatPanel(
                        leftLabel = "SKOR", leftValue = "205", leftColor = Color.White,
                        rightLabel = "HEDEF", rightValue = "220", rightColor = ResultFailAccent
                    )
                    Spacer(Modifier.height(16.dp))
                    GameButton(
                        text = "REKLAM İZLE, DEVAM ET",
                        onClick = {},
                        colors = resultButtonColors(ResultPrimaryBlue, ResultOnPrimary),
                        leadingPainter = painterResource(R.drawable.kb_btn_watchad),
                        iconSize = 28.dp,
                        minHeight = 54.dp, horizontalPadding = 10.dp,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    GameButton(
                        text = "YENİDEN BAŞLAT",
                        onClick = {},
                        colors = resultButtonColors(ResultSecondaryOrange, ResultOnSecondary),
                        leadingPainter = painterResource(R.drawable.kb_btn_retry),
                        iconSize = 28.dp,
                        fontSize = 14.sp, minHeight = 54.dp, horizontalPadding = 10.dp,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    GameButton(
                        text = "HARİTAYA DÖN",
                        onClick = {},
                        colors = resultButtonColors(ResultTertiarySlate, Color.White),
                        leadingPainter = painterResource(R.drawable.kb_btn_map),
                        iconSize = 28.dp,
                        fontSize = 14.sp, minHeight = 54.dp, horizontalPadding = 10.dp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        rule.waitForIdle()
        save("level_failed", rule.captureWindow())
    }

    /**
     * Kullanicinin cihazindaki dar/kisa yerlesim. Uc buton da TAM gorunmeli;
     * "HARITAYA DON" butonunun kirpilmasi burada yakalanir.
     */
    @Test
    fun renderLevelFailedNarrow() {
        rule.setContent {
            Backdrop(ResultFailAccent, widthDp = 360, heightDp = 590) {
                DialogCard(ResultFailAccent) {
                    ResultDialogTitle("SEVİYE", "BAŞARISIZ", resultEmblemColors(ResultFailAccent))
                    Spacer(Modifier.height(10.dp))
                    Image(
                        painter = painterResource(R.drawable.kb_dlg_heartbreak),
                        contentDescription = null,
                        modifier = Modifier.size(96.dp)
                    )
                    Spacer(Modifier.height(14.dp))
                    ResultStatPanel(
                        leftLabel = "SKOR", leftValue = "73", leftColor = Color.White,
                        rightLabel = "HEDEF", rightValue = "200", rightColor = ResultFailAccent
                    )
                    Spacer(Modifier.height(16.dp))
                    ResultActionButton(
                        text = "REKLAM İZLE, DEVAM ET",
                        onClick = {},
                        colors = resultButtonColors(ResultPrimaryBlue, ResultOnPrimary),
                        iconRes = R.drawable.kb_btn_watchad,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    ResultActionButton(
                        text = "YENİDEN BAŞLAT",
                        onClick = {},
                        colors = resultButtonColors(ResultSecondaryOrange, ResultOnSecondary),
                        iconRes = R.drawable.kb_btn_retry,
                        maxSizeSp = 15f,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    ResultActionButton(
                        text = "HARİTAYA DÖN",
                        onClick = {},
                        colors = resultButtonColors(ResultTertiarySlate, Color.White),
                        iconRes = R.drawable.kb_btn_map,
                        maxSizeSp = 15f,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        rule.waitForIdle()
        save("level_failed_narrow", rule.captureWindow())
    }

    @Test
    fun renderLevelComplete() {
        rule.setContent {
            Backdrop(ResultSuccessAccent) {
                DialogCard(ResultSuccessAccent) {
                    ResultDialogTitle("SEVİYE", "TAMAMLANDI!", resultEmblemColors(ResultSuccessAccent))
                    Spacer(Modifier.height(10.dp))
                    Box(contentAlignment = Alignment.Center) {
                        Canvas(modifier = Modifier.size(150.dp)) {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        ResultTrophyGlow.copy(alpha = 0.42f),
                                        Color.Transparent
                                    ),
                                    radius = size.minDimension / 2f
                                ),
                                radius = size.minDimension / 2f
                            )
                        }
                        Image(
                            painter = painterResource(R.drawable.kb_dlg_trophy),
                            contentDescription = null,
                            modifier = Modifier.size(104.dp)
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    ResultStatPanel(
                        leftLabel = "SKOR", leftValue = "222", leftColor = Color.White,
                        rightLabel = null, rightValue = null, rightColor = Color.White
                    )
                    Spacer(Modifier.height(18.dp))
                    GameButton(
                        text = "DEVAM ET",
                        onClick = {},
                        colors = resultButtonColors(ResultSuccessAccent, ResultOnSuccess),
                        fontSize = 17.sp, minHeight = 56.dp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        rule.waitForIdle()
        save("level_complete", rule.captureWindow())
    }

    /**
     * Faz 168 — BASMA YOLUNU GOZLE OLCTURMEK ICIN.
     *
     * Kullanici "basma hissi degil asagi kayma hissi cok net" dedi. Miktari
     * yargilayabilmesi icin butonun DINLENME ve BASILI hallerini yan yana
     * cikariyoruz. Basma taklit EDILMIYOR: saat durdurulup gercek bir dokunus
     * gonderiliyor, yani `GameButton`in kendi animasyonu calisiyor.
     */
    @Test
    fun renderButtonPressStates() {
        rule.mainClock.autoAdvance = false
        rule.setContent {
            Box(
                modifier = Modifier
                    .width(411.dp)
                    .height(260.dp)
                    .background(Color(0xFF0B1220)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    GameButton(
                        text = "BAŞLA",
                        onClick = {},
                        colors = resultButtonColors(ResultSuccessAccent, ResultOnSuccess),
                        fontSize = 17.sp,
                        minHeight = 56.dp,
                        modifier = Modifier.fillMaxWidth().testTag("btn_a")
                    )
                    Spacer(Modifier.height(22.dp))
                    GameButton(
                        text = "BAŞLA",
                        onClick = {},
                        colors = resultButtonColors(ResultSuccessAccent, ResultOnSuccess),
                        fontSize = 17.sp,
                        minHeight = 56.dp,
                        modifier = Modifier.fillMaxWidth().testTag("btn_b")
                    )
                }
            }
        }
        rule.mainClock.advanceTimeBy(300)
        save("button_rest", rule.captureWindow())

        // Alttaki butonu BASILI TUT: parmak kaldirilmadigi icin `pressed`
        // durumu kaliyor ve cokme animasyonu sonuna kadar gidiyor.
        rule.onNodeWithTag("btn_b").performTouchInput { down(center) }
        rule.mainClock.advanceTimeBy(300)
        save("button_pressed", rule.captureWindow())
    }
}

/**
 * Pencereyi dogrudan bitmap'e cizer.
 *
 * `captureToImage()` KULLANILAMIYOR: PixelCopy tabanli ve Robolectric'te
 * "Condition still not satisfied after 2000 ms" ile zaman asimina ugruyor.
 * View'i kendi elimizle bir Canvas'a cizmek native grafik modunda calisiyor.
 */
private fun androidx.compose.ui.test.junit4.AndroidComposeTestRule<*, ComponentActivity>.captureWindow(): Bitmap {
    val view = activity.window.decorView
    val bmp = createBitmap(view.width.coerceAtLeast(1), view.height.coerceAtLeast(1))
    view.draw(android.graphics.Canvas(bmp))
    return bmp
}
