package com.miniappfactory.boomblocks.data

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

        // Faz 37: kullanici "ilk girişte dil telefon dili default mu geliyor"
        // diye sordu — onceden gelmiyordu, hep sabit TR idi. Artik GERCEKTEN
        // ilk kurulumda (DataStore'da ne "language" ne eski "is_tr" kaydi
        // varsa) cihazin sistem diline gore akilli bir varsayilan seciliyor.
        // Desteklenmeyen bir sistem dili icin TR yerine EN'e dusuluyor — daha
        // uluslararasi/notr bir varsayilan.
        fun fromSystemLocale(): AppLanguage {
            val systemCode = java.util.Locale.getDefault().language
            return entries.find { it.code == systemCode } ?: EN
        }
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
