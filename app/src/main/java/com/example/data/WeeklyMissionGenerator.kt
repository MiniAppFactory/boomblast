package com.example.data

// Faz 73: kullanici "her haftalik gorev icinde 3 milestone olsun, bes tane
// haftalik gorev olsun" dedi — 5 MissionType = 5 sabit gorev, POOL/shuffle
// rotasyonu kaldirildi (5 tip zaten 5 gorev demek, rotasyona gerek yok).
// Hafta siniri sadece progress/claim'i sifirlar (bkz. GameStateRepository),
// hangi gorevlerin gosterildigini degil.
object WeeklyMissionGenerator {

    private val MISSIONS = listOf(
        WeeklyMissionDef(
            "complete_levels", "Seviye Tamamla", "Complete Levels", "Completa Livelli", "Terminer des Niveaux", "Completa Niveles",
            tiers = listOf(MissionTier(3, 20), MissionTier(8, 30), MissionTier(15, 50)),
            type = MissionType.COMPLETE_LEVELS
        ),
        WeeklyMissionDef(
            "clear_lines", "Satır/Sütun Patlat", "Clear Lines", "Elimina Linee", "Effacer des Lignes", "Elimina Líneas",
            tiers = listOf(MissionTier(50, 20), MissionTier(150, 30), MissionTier(350, 50)),
            type = MissionType.CLEAR_LINES
        ),
        WeeklyMissionDef(
            "use_boosters", "Güçlendirici Kullan", "Use Boosters", "Usa Potenziamenti", "Utiliser des Boosts", "Usa Potenciadores",
            tiers = listOf(MissionTier(5, 20), MissionTier(10, 30), MissionTier(15, 50)),
            type = MissionType.USE_BOOSTERS
        ),
        WeeklyMissionDef(
            "score_points", "Toplamda Puan Topla", "Score Total Points", "Totalizza Punti", "Marquer des Points au Total", "Consigue Puntos en Total",
            tiers = listOf(MissionTier(400, 20), MissionTier(1000, 30), MissionTier(2500, 50)),
            type = MissionType.SCORE_POINTS
        ),
        WeeklyMissionDef(
            "multi_clears", "Çoklu Patlama Yap", "Make Multi-Clears", "Esegui Cancellazioni Multiple", "Faire des Effacements Multiples", "Haz Eliminaciones Múltiples",
            tiers = listOf(MissionTier(5, 20), MissionTier(15, 30), MissionTier(30, 50)),
            type = MissionType.MULTI_CLEARS
        )
    )

    fun forWeek(weekId: String): List<WeeklyMissionDef> = MISSIONS
}
