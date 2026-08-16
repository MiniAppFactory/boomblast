package com.miniappfactory.boomblocks.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.miniappfactory.boomblocks.MainActivity
import com.miniappfactory.boomblocks.data.AppLanguage
import kotlin.random.Random

// Faz 27: rakip oyunlardaki gibi ("Şimdi oyna, eğlenceli bir tur bekliyor")
// ara sira, rastgele araliklarla gonderilen "geri gel" hatirlatma bildirimi.
// Ayarlar'dan kapatilabilir, gonderilmeden once hem izin hem sistem duzeyinde
// bildirimlerin acik olup olmadigi kontrol edilir.
//
// Faz 114 — POLITIKA KURALI, bu listeye mesaj eklerken oku:
// Bu bildirimler oyuna geri cagri icindir. Play politikasi, bildirimlerin
// reklam/promosyon araci olarak kullanilmasini ayrica yasaklar. Bu yuzden
// mesajlarin higbiri REKLAM IZLEMEYE CAGIRMAZ.
//
// Faz 114'e kadar 3. mesaj bes dilde de "Reklam izle, güçlendirici al" /
// "Watch an ad, get a booster" diyordu — yani amaci reklam gosterimi uretmek
// olan bir push'ti. Siklik ihlalinden farkli olarak bu, TEK bir bildirimde bile
// manuel incelemede goze carpar. Mesaj, oyuncuyu oyuna cagiran notr bir
// hatirlatmaya cevrildi; oyuncu oyuna girdiginde rewarded butonunu zaten
// goruyor, yani gelir yolu kapanmiyor — sadece cagri bildirimden cikiyor.
object NotificationHelper {
    const val CHANNEL_ID = "blast_reminders"
    private const val NOTIFICATION_ID = 4001

    private val messagesTr = listOf(
        "🧩 Eğlenceli bir tur seni bekliyor!" to "Şimdi oyna ve en yüksek skorunu geç 🚀",
        "🔥 Serini kaybetme!" to "Bugün birkaç satır patlatmaya ne dersin?",
        "🎁 Güçlendiricilerin seni bekliyor" to "Bir sonraki bölümü kolaylaştır",
        "🏆 Haftalık görevlerin seni bekliyor" to "Ödülünü almayı unutma"
    )
    private val messagesEn = listOf(
        "🧩 A fun round is waiting!" to "Play now and beat your high score 🚀",
        "🔥 Don't lose your streak!" to "How about clearing a few lines today?",
        "🎁 Your boosters are waiting" to "Make the next level easier",
        "🏆 Your weekly missions are waiting" to "Don't forget to claim your reward"
    )
    // Faz 35: 5 dile cikarilirken eklendi.
    private val messagesIt = listOf(
        "🧩 Un turno divertente ti aspetta!" to "Gioca ora e batti il tuo record 🚀",
        "🔥 Non perdere la tua serie!" to "Che ne dici di eliminare qualche linea oggi?",
        "🎁 I tuoi potenziamenti ti aspettano" to "Rendi più facile il prossimo livello",
        "🏆 Le tue missioni settimanali ti aspettano" to "Non dimenticare di riscuotere la tua ricompensa"
    )
    private val messagesFr = listOf(
        "🧩 Une partie amusante t'attend !" to "Joue maintenant et bats ton record 🚀",
        "🔥 Ne perds pas ta série !" to "Et si tu effaçais quelques lignes aujourd'hui ?",
        "🎁 Tes boosts t'attendent" to "Facilite le prochain niveau",
        "🏆 Tes missions hebdomadaires t'attendent" to "N'oublie pas de réclamer ta récompense"
    )
    private val messagesEs = listOf(
        "🧩 ¡Una ronda divertida te espera!" to "Juega ahora y supera tu récord 🚀",
        "🔥 ¡No pierdas tu racha!" to "¿Qué tal si limpias unas líneas hoy?",
        "🎁 Tus potenciadores te esperan" to "Haz más fácil el próximo nivel",
        "🏆 Tus misiones semanales te esperan" to "No olvides reclamar tu recompensa"
    )

    // Faz 114: yukaridaki politika kuralini testin gorebilmesi icin tek liste.
    // NotificationHelperPolicyTest bunu tarar; yeni bir dil ya da mesaj eklenince
    // buraya da eklenmeli, yoksa test o mesaji denetlemez.
    internal val allMessages: List<Pair<String, String>>
        get() = messagesTr + messagesEn + messagesIt + messagesFr + messagesEs

    private fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java) ?: return
            if (manager.getNotificationChannel(CHANNEL_ID) == null) {
                manager.createNotificationChannel(
                    NotificationChannel(CHANNEL_ID, "Kaboom Blocks", NotificationManager.IMPORTANCE_DEFAULT)
                )
            }
        }
    }

    fun showReminder(context: Context, language: AppLanguage) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return

        ensureChannel(context)
        val pool = when (language) {
            AppLanguage.TR -> messagesTr
            AppLanguage.EN -> messagesEn
            AppLanguage.IT -> messagesIt
            AppLanguage.FR -> messagesFr
            AppLanguage.ES -> messagesEs
        }
        val (title, body) = pool[Random.nextInt(pool.size)]

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            // Izin son anda geri alinmis olabilir — sessizce gec.
        }
    }
}
