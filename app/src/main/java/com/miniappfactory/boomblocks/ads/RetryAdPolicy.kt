package com.miniappfactory.boomblocks.ads

/**
 * Faz 164: "TEKRAR DENE" butonunun arkasindaki gecis reklaminin gosterilip
 * gosterilmeyecegine karar verir.
 *
 * SORUN (kullanici bildirdi, 2026-08-29):
 * Oyuncu kaybediyor, "REKLAM IZLE, DEVAM ET" ile haklarini tek tek kullaniyor
 * (Sonsuz Mod'da 4, Kariyer/Pro'da 3). Haklar bitince elinde **baska hicbir
 * secenek kalmiyor** — tek yol tahtayi sifirdan baslatmak. Ve o ZORUNLU yolun
 * uzerinde bir gecis reklami daha duruyordu.
 *
 * Kullanicinin ifadesi: *"orada zaten gerilmis oyuncuya tahtasini sifirdan
 * baslatmasi icin yine reklam dayatiyoruz ve bu cok fazla."*
 *
 * NEDEN ONEMLI: bu "istege bagli reklam" degil. Odullu reklamlar oyuncunun
 * SECIMI — onlara dokunulmadi, zaten sorun onlar degil. Sorun, secim
 * kalmadiginda cikisa konan gecis ucreti. Play'in "disruptive ads" politikasi
 * tam olarak bu deseni hedefler ve yaptirimi gelir kaybi degil, uygulamanin
 * askiya alinmasidir.
 *
 * KAPSAM BILEREK DAR: yalnizca haklari TUKENMIS oyuncunun bastan-baslama yolu.
 * Bolum-sonu reklami, HARITAYA DON, ve hakki KALAN oyuncunun tekrar-dene akisi
 * aynen calismaya devam eder — asiri duzeltme yapilmadi.
 *
 * Android'e bagimli DEGIL (saf fonksiyon), bu yuzden JVM birim testiyle
 * kilitlenebilir — `InterstitialFrequencyPolicy` ile ayni disiplin.
 */
object RetryAdPolicy {

    /**
     * @param continuesUsed bu oyunda/denemede kullanilan "reklam izle devam et" hakki
     * @param maxContinues o moddaki toplam hak (Sonsuz 4, Kariyer/Pro 3)
     * @return true ise TEKRAR DENE'de gecis reklami gosterilir
     */
    fun shouldShowInterstitialOnRetry(continuesUsed: Int, maxContinues: Int): Boolean {
        // Haklarini tuketen oyuncu zaten `maxContinues` kadar TAM EKRAN reklam
        // izledi ve baska secenegi kalmadi. Ustune bir tane daha koymuyoruz.
        if (maxContinues > 0 && continuesUsed >= maxContinues) return false
        // Hakki KALAN oyuncu (ornegin Sonsuz Mod'da devam teklifini reddedip
        // oyunu bitiren) bu muafiyetin disinda — normal davranis surer.
        return true
    }
}
