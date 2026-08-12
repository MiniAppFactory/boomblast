package com.example.data

// Faz 73: kullanici "her haftalik gorev icinde 3 milestone olsun, bes tane
// haftalik gorev olsun" dedi — 5 MissionType = 5 sabit gorev, POOL/shuffle
// rotasyonu kaldirildi (5 tip zaten 5 gorev demek, rotasyona gerek yok).
// Hafta siniri sadece progress/claim'i sifirlar (bkz. GameStateRepository),
// hangi gorevlerin gosterildigini degil.
object WeeklyMissionGenerator {

    private val MISSIONS = listOf(
        WeeklyMissionDef(
            "complete_levels", "{count} Seviye Tamamla", "Complete {count} Levels", "Completa {count} Livelli", "Terminer {count} Niveaux", "Completa {count} Niveles",
            tiers = listOf(MissionTier(3, 20), MissionTier(8, 30), MissionTier(15, 50)),
            type = MissionType.COMPLETE_LEVELS
        ),
        WeeklyMissionDef(
            "clear_lines", "{count} Satır/Sütun Patlat", "Clear {count} Lines", "Elimina {count} Linee", "Effacer {count} Lignes", "Elimina {count} Líneas",
            tiers = listOf(MissionTier(50, 20), MissionTier(150, 30), MissionTier(350, 50)),
            type = MissionType.CLEAR_LINES
        ),
        WeeklyMissionDef(
            "use_boosters", "{count} Güçlendirici Kullan", "Use {count} Boosters", "Usa {count} Potenziamenti", "Utiliser {count} Boosts", "Usa {count} Potenciadores",
            tiers = listOf(MissionTier(5, 20), MissionTier(10, 30), MissionTier(15, 50)),
            type = MissionType.USE_BOOSTERS
        ),
        WeeklyMissionDef(
            "score_points", "Toplamda {count} Puan Topla", "Score {count} Total Points", "Totalizza {count} Punti", "Marquer {count} Points au Total", "Consigue {count} Puntos en Total",
            tiers = listOf(MissionTier(400, 20), MissionTier(1000, 30), MissionTier(2500, 50)),
            type = MissionType.SCORE_POINTS
        ),
        WeeklyMissionDef(
            "multi_clears", "{count} Kez Çoklu Patlama Yap", "Make {count} Multi-Clears", "Esegui {count} Cancellazioni Multiple", "Faire {count} Effacements Multiples", "Haz {count} Eliminaciones Múltiples",
            tiers = listOf(MissionTier(5, 20), MissionTier(15, 30), MissionTier(30, 50)),
            type = MissionType.MULTI_CLEARS
        )
    )

    fun forWeek(weekId: String): List<WeeklyMissionDef> = MISSIONS
}
