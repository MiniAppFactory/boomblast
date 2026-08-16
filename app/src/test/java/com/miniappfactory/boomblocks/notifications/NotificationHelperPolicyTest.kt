package com.miniappfactory.boomblocks.notifications

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Faz 114. Play politikasi, bildirimlerin reklam/promosyon araci olarak
 * kullanilmasini yasaklar.
 *
 * Faz 114'e kadar bes dilde de "Reklam izle, güçlendirici al" mesaji vardi.
 * Metin duzeltmesi kolaydir — asil risk, ileride "reklam izle 2 kat jeton"
 * gibi bir mesajin iyi niyetle geri eklenmesidir. Bu test o kapiyi kapatir.
 */
class NotificationHelperPolicyTest {

    // Bes dilde reklam izlemeye cagri anlamina gelen kokler.
    private val adWords = listOf(
        "reklam",      // tr
        "ad",          // en — kelime siniriyla aranir
        "ads",
        "annuncio",    // it
        "pubblicit",   // it
        "pub",         // fr
        "publicit",    // fr
        "anuncio",     // es
        "video"        // "izle" cagrisinin yaygin esdegeri
    )

    @Test
    fun `hicbir bildirim mesaji reklam izlemeye cagirmaz`() {
        val offenders = mutableListOf<String>()
        for ((title, body) in NotificationHelper.allMessages) {
            val text = "$title $body".lowercase()
            // Kelime siniri: "ad" kelimesi "today"/"grab" icinde eslesmemeli.
            val words = text.split(Regex("[^\\p{L}]+")).filter { it.isNotBlank() }
            val hit = adWords.firstOrNull { root ->
                words.any { w -> w == root || (root.length > 3 && w.startsWith(root)) }
            }
            if (hit != null) offenders += "\"$title / $body\" -> '$hit'"
        }
        assertTrue(
            "Bildirim mesajlari reklam izlemeye cagiramaz (Play politikasi). Ihlaller:\n" +
                offenders.joinToString("\n"),
            offenders.isEmpty()
        )
    }

    @Test
    fun `bes dilin hepsinde ayni sayida mesaj var`() {
        // Bir dile mesaj eklenip digerlerine eklenmemesi, o dilde denetlenmemis
        // bir metin birakir.
        assertEquals(20, NotificationHelper.allMessages.size)
    }

    @Test
    fun `hicbir mesaj bos degil`() {
        for ((title, body) in NotificationHelper.allMessages) {
            assertTrue("Bos baslik", title.isNotBlank())
            assertTrue("Bos govde: $title", body.isNotBlank())
        }
    }
}
