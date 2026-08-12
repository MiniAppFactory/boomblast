package com.miniappfactory.boomblocks.ui.help

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miniappfactory.boomblocks.data.AppLanguage
import com.miniappfactory.boomblocks.data.pick
import com.miniappfactory.boomblocks.ui.theme.BlastSkin
import com.miniappfactory.boomblocks.ui.theme.NeonCyan
import com.miniappfactory.boomblocks.ui.theme.NeonGold
import com.miniappfactory.boomblocks.ui.theme.NeonGreen
import com.miniappfactory.boomblocks.ui.theme.NeonPurple
import com.miniappfactory.boomblocks.ui.theme.blastPalette

// Faz 81: Ayarlar altina "Nasil Oynanir / Modlar" sayfasi (Gorev #21, uzun
// suredir ertelenmisti). Header deseni MissionsScreen'inkiyle ayni (geri
// IconButton + ortalanmis, tek-satir+ellipsis title + dengeleyici Spacer) —
// LevelMapScreen'de yasanan baslik kirpilma hatasini (rakip sag ikon grubu
// olmadigi icin) burada tekrarlamiyoruz. Her mod kendi Card'inda, kisa
// kural ozeti — 5 dilde.
@Composable
fun HowToPlayScreen(
    language: AppLanguage,
    darkMode: Boolean,
    skin: BlastSkin = BlastSkin.DEFAULT,
    onBack: () -> Unit
) {
    val palette = blastPalette(skin, darkMode)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.background)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("how_to_play_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = language.pick(tr = "Geri", en = "Back", it = "Indietro", fr = "Retour", es = "Atrás"),
                        tint = palette.textPrimary
                    )
                }

                Text(
                    text = language.pick(
                        tr = "NASIL OYNANIR / MODLAR", en = "HOW TO PLAY / MODES",
                        it = "COME GIOCARE / MODALITÀ", fr = "COMMENT JOUER / MODES", es = "CÓMO JUGAR / MODOS"
                    ),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = NeonCyan,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                Spacer(modifier = Modifier.width(48.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            ModeInfoCard(
                accentColor = NeonGreen,
                title = language.pick(tr = "SONSUZ MOD", en = "ENDLESS MODE", it = "MODALITÀ INFINITA", fr = "MODE INFINI", es = "MODO INFINITO"),
                body = language.pick(
                    tr = "Hedef yok — blokları yerleştirip satır/sütun patlatarak en yüksek skoru geçmeye çalış. Skorun arttıkça gelen parçaların zorluğu da artar. Oyun, tahtaya hiçbir parça sığmayınca biter.",
                    en = "No target — place blocks and clear rows/columns to beat your best score. Piece difficulty ramps up as your score grows. The game ends when no piece fits on the board anymore.",
                    it = "Nessun obiettivo — posiziona i blocchi ed elimina righe/colonne per battere il tuo record. La difficoltà dei pezzi aumenta con il punteggio. La partita finisce quando nessun pezzo entra più nella griglia.",
                    fr = "Aucun objectif — place des blocs et efface des lignes/colonnes pour battre ton meilleur score. La difficulté des pièces augmente avec le score. La partie se termine quand plus aucune pièce ne rentre sur le plateau.",
                    es = "Sin objetivo — coloca bloques y despeja filas/columnas para superar tu mejor puntuación. La dificultad de las piezas aumenta con la puntuación. La partida termina cuando ninguna pieza cabe ya en el tablero."
                ),
                palette = palette
            )

            ModeInfoCard(
                accentColor = com.miniappfactory.boomblocks.ui.theme.NeonCyan,
                title = language.pick(tr = "SEVİYELİ MOD", en = "LEVEL MODE", it = "MODALITÀ LIVELLI", fr = "MODE NIVEAUX", es = "MODO NIVELES"),
                body = language.pick(
                    tr = "Her bölümün bir hedef skoru vardır — blokları yerleştirip satır/sütun patlatarak hedefe ulaş. Hedefi ne kadar aştığına göre 1-3 yıldız kazanırsın. Bölüm ilerledikçe parça havuzu kademeli olarak genişler.",
                    en = "Each level has a target score — place blocks and clear rows/columns to reach it. You earn 1-3 stars depending on how much you exceed the target. The piece pool gradually widens as you progress.",
                    it = "Ogni livello ha un punteggio obiettivo — posiziona i blocchi ed elimina righe/colonne per raggiungerlo. Guadagni 1-3 stelle in base a quanto superi l'obiettivo. Il pool di pezzi si amplia gradualmente con i progressi.",
                    fr = "Chaque niveau a un score cible — place des blocs et efface des lignes/colonnes pour l'atteindre. Tu gagnes 1 à 3 étoiles selon combien tu dépasses la cible. Le pool de pièces s'élargit progressivement.",
                    es = "Cada nivel tiene una puntuación objetivo — coloca bloques y despeja filas/columnas para alcanzarla. Ganas de 1 a 3 estrellas según cuánto superes el objetivo. El conjunto de piezas se amplía gradualmente."
                ),
                palette = palette
            )

            ModeInfoCard(
                accentColor = NeonPurple,
                title = language.pick(tr = "PRO MOD", en = "PRO MODE", it = "MODALITÀ PRO", fr = "MODE PRO", es = "MODO PRO"),
                body = language.pick(
                    tr = "Seviyeli Mod'un daha zorlu hali: puanlar 1.5 kat sayılır, 1x1 parça hiç gelmez ve zor parçalar çok daha erken açılır. 4 canın var, zamanla yenilenir; bir bölümü kaybedip \"Yeniden Başla\"yı seçersen 1 can harcarsın (dilersen bunun yerine ücretsiz reklam izleyip devam edebilirsin).",
                    en = "A tougher version of Level Mode: scores count 1.5x, 1x1 pieces never appear, and hard pieces unlock much earlier. You have 4 lives that regenerate over time; choosing \"Restart\" after failing a level costs 1 life (or you can watch a free ad to continue instead).",
                    it = "Una versione più difficile della Modalità Livelli: i punteggi contano 1.5x, i pezzi 1x1 non compaiono mai e i pezzi difficili si sbloccano molto prima. Hai 4 vite che si rigenerano nel tempo; scegliere \"Ricomincia\" dopo aver fallito un livello costa 1 vita (oppure puoi guardare una pubblicità gratuita per continuare).",
                    fr = "Une version plus difficile du Mode Niveaux : les scores comptent 1.5x, les pièces 1x1 n'apparaissent jamais et les pièces difficiles se débloquent bien plus tôt. Tu as 4 vies qui se régénèrent avec le temps ; choisir \"Recommencer\" après un échec coûte 1 vie (ou tu peux regarder une pub gratuite pour continuer).",
                    es = "Una versión más difícil del Modo Niveles: las puntuaciones cuentan 1.5x, las piezas 1x1 nunca aparecen y las piezas difíciles se desbloquean mucho antes. Tienes 4 vidas que se regeneran con el tiempo; elegir \"Reiniciar\" tras fallar un nivel cuesta 1 vida (o puedes ver un anuncio gratis para continuar)."
                ),
                palette = palette
            )

            ModeInfoCard(
                accentColor = NeonGold,
                title = language.pick(tr = "RETRO MOD", en = "RETRO MODE", it = "MODALITÀ RETRO", fr = "MODE RÉTRO", es = "MODO RETRO"),
                body = language.pick(
                    tr = "Klasik Tetris! Düşen parçaları yönlendirip satırları tamamen doldurarak patlat. 1 satır=100, 2=300, 3=500, 4 satır (Tetris)=800 puan (×seviye) — art arda gelen Tetris'ler ve kombolar ekstra bonus verir. Parçayı bekletmek için \"Tut\", iniş noktasını görmek için hayalet parça önizlemesini kullanabilirsin.",
                    en = "Classic Tetris! Steer falling pieces and fill rows completely to clear them. 1 line=100, 2=300, 3=500, 4 lines (Tetris)=800 points (×level) — consecutive Tetrises and combos give extra bonuses. Use \"Hold\" to save a piece for later, and the ghost piece preview to see where it will land.",
                    it = "Tetris classico! Guida i pezzi che cadono e riempi completamente le righe per eliminarle. 1 riga=100, 2=300, 3=500, 4 righe (Tetris)=800 punti (×livello) — Tetris consecutivi e combo danno bonus extra. Usa \"Tieni\" per conservare un pezzo, e l'anteprima del pezzo fantasma per vedere dove atterrerà.",
                    fr = "Tetris classique ! Dirige les pièces qui tombent et remplis complètement les lignes pour les effacer. 1 ligne=100, 2=300, 3=500, 4 lignes (Tetris)=800 points (×niveau) — des Tetris consécutifs et des combos donnent des bonus. Utilise \"Garder\" pour réserver une pièce, et l'aperçu de la pièce fantôme pour voir où elle atterrira.",
                    es = "¡Tetris clásico! Dirige las piezas que caen y llena filas completas para eliminarlas. 1 línea=100, 2=300, 3=500, 4 líneas (Tetris)=800 puntos (×nivel) — Tetris consecutivos y combos dan bonus extra. Usa \"Guardar\" para reservar una pieza, y la vista previa de la pieza fantasma para ver dónde caerá."
                ),
                palette = palette
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ModeInfoCard(
    accentColor: androidx.compose.ui.graphics.Color,
    title: String,
    body: String,
    palette: com.miniappfactory.boomblocks.ui.theme.BlastPalette
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = palette.card),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                color = accentColor
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = body,
                fontSize = 13.sp,
                color = palette.textSecondary,
                lineHeight = 18.sp
            )
        }
    }
}
