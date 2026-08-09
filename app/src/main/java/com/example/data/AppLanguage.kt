package com.example.data

// Faz 35: onceden tum gorsel metin isTr:Boolean bayragiyla (sadece TR/EN)
// yonetiliyordu. Kullanici Italyanca/Fransizca/Ispanyolca da istedi — bu
// enum ve pick() yardimcisi, mevcut "if (isTr) tr else en" mimarisini
// AYNEN koruyup 5 dile genisletiyor (davranis riski dusuk, sadece dal sayisi
// artiyor). Ses/TTS bu kapsamin DISINDA — TextToSpeechManager her zaman
// Ingilizce calar (Faz 34c), buraya dokunulmadi.
enum class AppLanguage(val code: String) {
    TR("tr"),
    EN("en"),
    IT("it"),
    FR("fr"),
    ES("es");

    companion object {
        fun fromCode(code: String?): AppLanguage = entries.find { it.code == code } ?: TR
    }
}

fun AppLanguage.pick(tr: String, en: String, it: String, fr: String, es: String): String =
    when (this) {
        AppLanguage.TR -> tr
        AppLanguage.EN -> en
        AppLanguage.IT -> it
        AppLanguage.FR -> fr
        AppLanguage.ES -> es
    }

// Faz 36b: Ayarlar'daki dil secici kaydirilabilir kart listesinden acilir
// menuye (dropdown) cevrilirken eklendi — her dil kendi bayragiyla gosteriliyor.
fun AppLanguage.flag(): String =
    when (this) {
        AppLanguage.TR -> "🇹🇷"
        AppLanguage.EN -> "🇬🇧"
        AppLanguage.IT -> "🇮🇹"
        AppLanguage.FR -> "🇫🇷"
        AppLanguage.ES -> "🇪🇸"
    }

fun AppLanguage.label(): String =
    pick(tr = "Türkçe", en = "English", it = "Italiano", fr = "Français", es = "Español")
