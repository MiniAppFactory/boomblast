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
import com.miniappfactory.boomblocks.ui.theme.ComfortTeal
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
                // Faz 104: mod adi "SEVİYELİ" -> "KARİYER" (bkz. ModeSelectScreen).
                title = language.pick(tr = "KARİYER MODU", en = "CAREER MODE", it = "MODALITÀ CARRIERA", fr = "MODE CARRIÈRE", es = "MODO CARRERA"),
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
                    // Faz 103: bu metin Faz 96'dan beri YANLIStI — can sistemi (4 can,
                    // zamanla yenilenme, "Yeniden Başla 1 can harcar") o fazda tamamen
                    // KALDIRILMISTI (bypass edilebiliyordu: haritaya donup tekrar girmek
                    // can harcamadan ayni sonucu veriyordu), ama kilavuz guncellenmemisti.
                    // Oyuncu var olmayan bir mekanigi okuyordu. Artik gercek davranis
                    // anlatiliyor; devam/reklam detaylari asagidaki ayri kartta.
                    tr = "Kariyer Modu'nun daha zorlu hali: puanlar 1.5 kat sayılır, 1x1 parça hiç gelmez ve zor parçalar çok daha erken açılır. Can sistemi yoktur — kaybettiğinde reklam izleyip kaldığın yerden devam edebilir ya da bölümü baştan başlatabilirsin.",
                    en = "A tougher version of Career Mode: scores count 1.5x, 1x1 pieces never appear, and hard pieces unlock much earlier. There are no lives — when you fail, you can watch an ad to continue where you left off, or restart the level.",
                    it = "Una versione più difficile della Modalità Carriera: i punteggi contano 1.5x, i pezzi 1x1 non compaiono mai e i pezzi difficili si sbloccano molto prima. Non ci sono vite — quando perdi, puoi guardare un annuncio per continuare da dove eri, oppure ricominciare il livello.",
                    fr = "Une version plus difficile du Mode Carrière : les scores comptent 1.5x, les pièces 1x1 n'apparaissent jamais et les pièces difficiles se débloquent bien plus tôt. Il n'y a pas de vies — quand tu échoues, tu peux regarder une pub pour reprendre où tu en étais, ou recommencer le niveau.",
                    es = "Una versión más difícil del Modo Carrera: las puntuaciones cuentan 1.5x, las piezas 1x1 nunca aparecen y las piezas difíciles se desbloquean mucho antes. No hay vidas — cuando pierdes, puedes ver un anuncio para continuar donde lo dejaste, o reiniciar el nivel."
                ),
                palette = palette
            )

            // Faz 128: RETRO MOD karti kaldirildi (mod menuden cikarildi), yerine
            // COMFORT MODE (TR "KOLAY MOD") anlatimi geldi.
            ModeInfoCard(
                accentColor = ComfortTeal,
                title = language.pick(tr = "KOLAY MOD", en = "COMFORT MODE", it = "MODALITÀ COMFORT", fr = "MODE CONFORT", es = "MODO CONFORT"),
                body = language.pick(
                    tr = "Kariyer Modu'nun rahat hali. Hedef puan 100'den başlar ve her bölümde yalnızca 1 puan artar — bölümler hızlı geçer, baskı yoktur. Parçalar da sana yardım eder: tahtada o an bir satırı ya da sütunu tamamlayabilecek bir parça varsa, tepsindeki üç yuvadan biri o parçalardan seçilir. Bölüm haritası, yıldızlar ve güçlendiriciler Kariyer'deki gibidir; ilerlemen Kariyer'den ayrı tutulur.",
                    en = "The relaxed side of Career Mode. The target score starts at 100 and rises by just 1 point per level — levels go by fast and there is no pressure. The pieces help you too: if a piece could complete a row or column on the board right now, one of your three tray slots is drawn from those. The level map, stars and boosters work just like Career; your progress is tracked separately.",
                    it = "Il lato rilassato della Modalità Carriera. Il punteggio obiettivo parte da 100 e sale di appena 1 punto per livello — i livelli passano in fretta e non c'è pressione. Anche i pezzi ti aiutano: se un pezzo può completare una riga o una colonna sul tabellone in questo momento, uno dei tre slot del vassoio viene scelto tra quelli. Mappa dei livelli, stelle e potenziamenti funzionano come in Carriera; i progressi sono separati.",
                    fr = "Le côté détendu du Mode Carrière. Le score cible démarre à 100 et n'augmente que de 1 point par niveau — les niveaux défilent vite et sans pression. Les pièces t'aident aussi : si une pièce peut compléter une ligne ou une colonne sur le plateau à cet instant, l'un de tes trois emplacements est tiré parmi celles-ci. La carte des niveaux, les étoiles et les boosters fonctionnent comme en Carrière ; ta progression est séparée.",
                    es = "El lado relajado del Modo Carrera. La puntuación objetivo empieza en 100 y sube solo 1 punto por nivel — los niveles pasan rápido y sin presión. Las piezas también te ayudan: si una pieza puede completar una fila o columna en el tablero ahora mismo, una de tus tres ranuras se elige entre ellas. El mapa de niveles, las estrellas y los potenciadores funcionan como en Carrera; tu progreso se guarda por separado."
                ),
                palette = palette
            )

            // Faz 103: kilavuzda devam etme/reklam kurallari hicbir yerde anlatilmiyordu —
            // tek deginen yer Pro Mod kartindaki (artik silinen) yanlis can metniydi.
            // Oyuncu "kac hakkim var", "reklam gelmezse ne olur", "geri alma ne yapar"
            // sorularinin cevabini oyun ici hicbir yerden ogrenemiyordu. Reklam ekonomisi
            // Faz 96-103 arasinda epey degistigi icin tek ve net bir yere yazildi.
            ModeInfoCard(
                accentColor = NeonCyan,
                title = language.pick(
                    tr = "DEVAM ETME VE REKLAMLAR", en = "CONTINUES & ADS",
                    it = "CONTINUA E ANNUNCI", fr = "CONTINUER ET PUBS", es = "CONTINUAR Y ANUNCIOS"
                ),
                body = language.pick(
                    tr = "Kaybettiğinde \"Reklam İzle, Devam Et\" son 3 hamleni geri alır ve tahtada sana yer açar — Kariyer ve Pro Mod'da deneme başına 3, Sonsuz Mod'da oturum başına 4 hakkın var. Haklar bitince bölümü baştan başlatabilirsin. Reklam yüklenemezse oyun seni asla bekletmez, yine devam edersin.",
                    en = "When you fail, \"Watch Ad, Continue\" undoes your last 3 moves and clears space on the board — you get 3 per attempt in Career and Pro Mode, and 4 per session in Endless Mode. Once they run out you can restart. If an ad can't load, the game never blocks you — you continue anyway.",
                    it = "Quando perdi, \"Guarda Annuncio, Continua\" annulla le tue ultime 3 mosse e libera spazio sulla griglia — ne hai 3 per tentativo in Modalità Carriera e Pro, e 4 per sessione in Modalità Infinita. Esauriti, puoi ricominciare. Se un annuncio non si carica, il gioco non ti blocca mai — continui comunque.",
                    fr = "Quand tu échoues, « Regarder Pub, Continuer » annule tes 3 derniers coups et libère de la place sur le plateau — tu en as 3 par tentative en Mode Carrière et Pro, et 4 par session en Mode Infini. Une fois épuisés, tu peux recommencer. Si une pub ne se charge pas, le jeu ne te bloque jamais — tu continues quand même.",
                    es = "Cuando pierdes, \"Ver Anuncio, Continuar\" deshace tus últimos 3 movimientos y libera espacio en el tablero — tienes 3 por intento en Modo Carrera y Pro, y 4 por sesión en Modo Infinito. Cuando se acaban, puedes reiniciar. Si un anuncio no carga, el juego nunca te bloquea — continúas igualmente."
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
