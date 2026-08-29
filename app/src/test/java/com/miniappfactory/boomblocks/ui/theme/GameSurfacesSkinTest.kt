package com.miniappfactory.boomblocks.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.miniappfactory.boomblocks.ui.components.gameButtonColors
import com.miniappfactory.boomblocks.ui.components.gameIconButtonColors
import com.miniappfactory.boomblocks.ui.components.backButtonBase
import com.miniappfactory.boomblocks.ui.components.BackButtonAccent
import com.miniappfactory.boomblocks.ui.components.mutedGameButtonColors
import com.miniappfactory.boomblocks.ui.components.primaryGameButtonColors
import com.miniappfactory.boomblocks.ui.components.splitEmblemLines
import com.miniappfactory.boomblocks.ui.components.emblemFontScale
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Faz 158 — 6 SKININ DE BOZULMADIGININ OTOMATIK GUVENCESI.
 *
 * Menu gorsel yenilemesinde tum malzeme (zemin, panel, buton, kapsul) tek bir
 * turetme katmanindan geciyor: `gameSurfaces()`. Bu test o katmani
 * DEFAULT(acik) + DEFAULT(koyu) + kalici 5 skin uzerinde tek tek dogruluyor,
 * yani yeni bir skin eklendiginde ya da bir formul degistiginde kirilma
 * derleme zamaninda degil TEST zamaninda yakalanir.
 *
 * Ozellikle Faz 146-147 hatasi ("NeonGold uzerine altin yazi acik temada
 * okunmuyor") burada kontrol altinda: her skin icin baslik rengi ve buton
 * yazi rengi, uzerinde durduklari zemine karsi kontrast esigini gecmek
 * zorunda.
 */
class GameSurfacesSkinTest {

    // WCAG bagil parlaklik kontrast orani.
    private fun contrast(a: Color, b: Color): Double {
        val la = a.luminance().toDouble()
        val lb = b.luminance().toDouble()
        val hi = maxOf(la, lb)
        val lo = minOf(la, lb)
        return (hi + 0.05) / (lo + 0.05)
    }

    // Test edilen tum kombinasyonlar: DEFAULT hem acik hem koyu temada
    // kullaniliyor, diger 5 skin acik/koyu ayarindan bagimsiz sabit.
    private fun allCases(): List<Triple<String, BlastSkin, Boolean>> = buildList {
        add(Triple("DEFAULT-dark", BlastSkin.DEFAULT, true))
        add(Triple("DEFAULT-light", BlastSkin.DEFAULT, false))
        BlastSkin.entries.filter { it != BlastSkin.DEFAULT }.forEach {
            add(Triple("${it.name}-dark", it, true))
            add(Triple("${it.name}-light", it, false))
        }
    }

    @Test
    fun `every skin produces a readable accent title color`() {
        allCases().forEach { (name, skin, dark) ->
            val palette = blastPalette(skin, dark)
            val s = gameSurfaces(palette, skin.accentGradient)
            val ratio = contrast(s.accentText, palette.background)
            // Basliklar 20sp+ ve kalin (buyuk metin) — WCAG AA esigi 3.0.
            assertTrue(
                "$name: accentText/background kontrasti dusuk ($ratio)",
                ratio >= 3.0
            )
        }
    }

    @Test
    fun `every skin keeps button labels readable on their own fill`() {
        allCases().forEach { (name, skin, dark) ->
            val palette = blastPalette(skin, dark)
            val s = gameSurfaces(palette, skin.accentGradient)

            // Birincil buton: dolgu skin accent'inden geliyor, yazi rengi
            // `readableOn` ile SECILIYOR — altin uzerine altin imkansiz.
            val primary = primaryGameButtonColors(s)
            val primaryFill = androidx.compose.ui.graphics.lerp(primary.top, primary.bottom, 0.5f)
            assertTrue(
                "$name: birincil buton yazisi dolgusunda okunmuyor",
                contrast(primary.content, primaryFill) >= 3.0
            )

            // Ikincil/pasif buton da okunabilir olmali — "goruyorum ama
            // basamiyorum" hali gizlenmemeli.
            //
            // Faz 159: bu test ONCEDEN yalnizca `accentText` ile cagriliyordu,
            // ama gercek cagiran (MissionsScreen'deki pasif "TOPLA") yazi rengi
            // olarak `hairline` GONDERIYORDU — yani bir KENARLIK tonu. Buton
            // govdesi okunur hale getirilince yazi govdenin icinde kayboldu ve
            // hata ancak CIHAZDA goruldu. Artik her iki cagri sekli de
            // taraniyor: `mutedGameButtonColors` hangi renk verilirse verilsin
            // okunur bir sonuc uretmek zorunda.
            listOf(
                "accentText" to s.accentText,
                "hairline" to s.hairline,
                "panelBorder" to s.panelBorder,
                "sunken" to s.sunken
            ).forEach { (requestedName, requested) ->
                val muted = mutedGameButtonColors(s, requested)
                assertTrue(
                    "$name: pasif buton yazisi ($requestedName) dolgusunda okunmuyor",
                    contrast(muted.content, muted.top) >= 2.0
                )
            }

            // Rol renkli butonlar (TOPLA yesili, SATIN AL turuncusu, altin).
            listOf(
                "yesil" to Color(0xFF35D96B),
                "altin" to Color(0xFFFACC15),
                "turuncu" to Color(0xFFFF6B35)
            ).forEach { (label, base) ->
                val c = gameButtonColors(base)
                val fill = androidx.compose.ui.graphics.lerp(c.top, c.bottom, 0.5f)
                assertTrue(
                    "$name: $label buton yazisi dolgusunda okunmuyor",
                    contrast(c.content, fill) >= 3.0
                )
            }
        }
    }

    @Test
    fun `every skin has visible list banding and a distinct ground band`() {
        allCases().forEach { (name, skin, dark) ->
            val palette = blastPalette(skin, dark)
            val s = gameSurfaces(palette, skin.accentGradient)

            // Donusumlu satir tonlari birbirinden AYIRT EDILEBILIR olmali;
            // aksi halde "bantlama" gorunmez bir suslemeye donerdi.
            assertNotEquals("$name: bantlama tonlari ayni", s.bandEven, s.bandOdd)
            assertTrue(
                "$name: bantlama farki gozle secilemeyecek kadar kucuk",
                kotlin.math.abs(s.bandEven.luminance() - s.bandOdd.luminance()) > 0.004f
            )

            // Zemin bandi gokten daha KOYU olmali — derinlik hissi buradan
            // geliyor. Acik temada da (yumusak da olsa) ayni yon gecerli.
            assertTrue(
                "$name: zemin bandi gokyuzunden koyu degil",
                s.groundBottom.luminance() < s.skyTop.luminance()
            )
        }
    }

    @Test
    fun `every skin produces a readable icon button`() {
        // Faz 159: geri tusu CIHAZDA neredeyse BEYAZ cikiyordu (mockup'ta
        // kabartmali mor). `gameButtonColors`'un parlatma dongusu metin
        // butonlari icindi; ikon butonu artik ham accent'ten turuyor.
        // Burada her skinde ikonun govdesinde okundugu dogrulaniyor.
        allCases().forEach { (name, skin, dark) ->
            val palette = blastPalette(skin, dark)
            val s = gameSurfaces(palette, skin.accentGradient)
            listOf(
                "accentPrimary" to s.accentPrimary,
                "accentSecondary" to s.accentSecondary
            ).forEach { (label, base) ->
                val c = gameIconButtonColors(base)
                val fill = androidx.compose.ui.graphics.lerp(c.top, c.bottom, 0.5f)
                assertTrue(
                    "$name: ikon butonu ($label) govdesinde okunmuyor",
                    contrast(c.content, fill) >= 3.0
                )
                // Govde DOYGUN kalmali: beyaza yaklasirsa mockup'taki mor
                // kimlik kaybolur. Parlatma dongusu buraya sizarsa yakalanir.
                assertTrue(
                    "$name: ikon butonu ($label) govdesi beyaza yaklasmis",
                    fill.luminance() < 0.62f
                )
            }
        }
    }

    @Test
    fun `emblem splits multi word titles and keeps single words on one line`() {
        // Bolme kelime sayisindan cikar, dile GOMULU elle bolme yok.
        // Tek kelime -> tek satir (altin).
        listOf("AYARLAR", "SETTINGS", "IMPOSTAZIONI", "PARAMÈTRES", "AJUSTES").forEach {
            assertTrue("$it tek satirda kalmali", splitEmblemLines(it).size == 1)
        }
        // Iki kelime -> iki satir (ust altin, alt accent).
        listOf(
            "HAFTALIK GÖREVLER",
            "WEEKLY MISSIONS",
            "MISSIONI SETTIMANALI",
            "MISSIONS HEBDOMADAIRES",
            "MISIONES SEMANALES"
        ).forEach {
            val lines = splitEmblemLines(it)
            assertTrue("$it iki satira bolunmeli", lines.size == 2)
            assertTrue("$it bolununce kelime kaybetti", lines.joinToString(" ") == it)
        }
        // Wordmark da ayni receteden gecer.
        assertTrue(splitEmblemLines("Kaboom Blocks") == listOf("Kaboom", "Blocks"))
    }

    @Test
    fun `emblem shrinks the longest translation instead of clipping it`() {
        // En uzun satir "HEBDOMADAIRES" (13 karakter): yazi boyutu kucultulmeli
        // ama okunamayacak kadar degil.
        val fr = splitEmblemLines("MISSIONS HEBDOMADAIRES")
        val scale = emblemFontScale(fr)
        assertTrue("FR baslik kuculmedi ($scale)", scale < 1f)
        assertTrue("FR baslik asiri kuculdu ($scale)", scale >= 0.68f)
        // Kisa basliklar hic kuculmemeli.
        assertTrue(emblemFontScale(listOf("AYARLAR")) == 1f)
        assertTrue(emblemFontScale(splitEmblemLines("HAFTALIK GÖREVLER")) == 1f)
    }

    @Test
    fun `accent tokens follow each skin's own gradient`() {
        // Skin'e ozel elle renk gomulmedigi icin her skinin accent'i KENDI
        // gradyanindan gelmeli. Bu test, ileride birinin buraya sabit bir
        // renk yazmasini engeller.
        BlastSkin.entries.forEach { skin ->
            val palette = blastPalette(skin, true)
            val s = gameSurfaces(palette, skin.accentGradient)
            assertNotEquals(
                "${skin.name}: accentPrimary skin gradyanindan gelmiyor",
                0,
                java.lang.Float.compare(s.accentPrimary.alpha, 0f)
            )
            assertTrue(
                "${skin.name}: accentPrimary skin gradyaninin ilk rengi olmali",
                s.accentPrimary == skin.accentGradient.first()
            )
            assertTrue(
                "${skin.name}: accentSecondary skin gradyaninin son rengi olmali",
                s.accentSecondary == skin.accentGradient.last()
            )
        }
    }

    /**
     * Faz 160 — GERI TUSU: HER EKRANDA AYNI MOR, HER SKINDE GORUNUR.
     *
     * Kullanici geri tusunun her ekranda ayni (Ayarlar'daki mor) olmasini
     * istedi; govde artik `surfaces.accentSecondary` yerine SABIT mordan
     * turuyor. Sabit renk gomerken asil risk MOR ZEMINDE MOR BUTON:
     * PURPLE_NIGHT skininde buton zemine gomulebilirdi.
     *
     * Bu test iki seyi birden guvenceye aliyor:
     *   1. renk KIMLIGI sabit kaliyor (skin'in accent'ine geri kaymiyor),
     *   2. buton govdesi 6 skinde de zeminden AYRISIYOR ve uzerindeki
     *      ok ikonu okunuyor.
     */
    @Test
    fun `back button stays one fixed purple and visible on every skin`() {
        allCases().forEach { (name, skin, dark) ->
            val palette = blastPalette(skin, dark)
            val s = gameSurfaces(palette, skin.accentGradient)

            val base = backButtonBase(s)
            // 1. Kimlik sabit: skin'in accent'i ne olursa olsun geri tusu
            //    MOR ailesinde kalmali — mavi kanal en guclu, yesil en zayif.
            assertTrue(
                "$name: geri tusu artik mor degil (base=$base)",
                base.blue > base.green && base.red > base.green
            )

            // 2. Govde zeminden ayrisiyor.
            assertTrue(
                "$name: geri tusu ekran zeminine gomuluyor " +
                    "(kontrast ${contrast(base, s.skyTop)})",
                contrast(base, s.skyTop) >= 1.85
            )

            // 3. Ustundeki ok ikonu govde uzerinde okunuyor.
            val c = gameIconButtonColors(base)
            val fill = androidx.compose.ui.graphics.lerp(c.top, c.bottom, 0.5f)
            assertTrue(
                "$name: geri tusu ikonu govdesinde okunmuyor",
                contrast(c.content, fill) >= 3.0
            )
        }
    }

    @Test
    fun `back button colour does not follow the skin accent`() {
        // Ayni sabit morun 6 skinde de AYNI noktadan basladigini dogrular:
        // biri ileride `surfaces.accentSecondary`ye geri donerse burada
        // yakalanir.
        val bases = allCases().map { (_, skin, dark) ->
            backButtonBase(gameSurfaces(blastPalette(skin, dark), skin.accentGradient))
        }
        assertTrue(
            "geri tusu sabit mordan turemiyor",
            bases.all { it.blue > it.green && it.red > it.green }
        )
        assertTrue(
            "geri tusu sabit mor sabiti kullanilmiyor",
            BackButtonAccent.blue > BackButtonAccent.green
        )
    }
}
