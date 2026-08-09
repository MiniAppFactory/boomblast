package com.example.notifications

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
import com.example.MainActivity
import com.example.data.AppLanguage
import kotlin.random.Random

// Faz 27: rakip oyunlardaki gibi ("Şimdi oyna, eğlenceli bir tur bekliyor")
// ara sira, rastgele araliklarla gonderilen "geri gel" hatirlatma bildirimi.
// Ayarlar'dan kapatilabilir, gonderilmeden once hem izin hem sistem duzeyinde
// bildirimlerin acik olup olmadigi kontrol edilir.
object NotificationHelper {
    const val CHANNEL_ID = "blast_reminders"
    private const val NOTIFICATION_ID = 4001

    private val messagesTr = listOf(
        "🧩 Eğlenceli bir tur seni bekliyor!" to "Şimdi oyna ve en yüksek skorunu geç 🚀",
        "🔥 Serini kaybetme!" to "Bugün birkaç satır patlatmaya ne dersin?",
        "🎁 Ücretsiz jeton kazanmanın tam zamanı" to "Reklam izle, güçlendirici al",
        "🏆 Haftalık görevlerin seni bekliyor" to "Ödülünü almayı unutma"
    )
    private val messagesEn = listOf(
        "🧩 A fun round is waiting!" to "Play now and beat your high score 🚀",
        "🔥 Don't lose your streak!" to "How about clearing a few lines today?",
        "🎁 Time to grab free tokens" to "Watch an ad, get a booster",
        "🏆 Your weekly missions are waiting" to "Don't forget to claim your reward"
    )
    // Faz 35: 5 dile cikarilirken eklendi.
    private val messagesIt = listOf(
        "🧩 Un turno divertente ti aspetta!" to "Gioca ora e batti il tuo record 🚀",
        "🔥 Non perdere la tua serie!" to "Che ne dici di eliminare qualche linea oggi?",
        "🎁 È ora di prendere token gratis" to "Guarda un annuncio, ottieni un potenziamento",
        "🏆 Le tue missioni settimanali ti aspettano" to "Non dimenticare di riscuotere la tua ricompensa"
    )
    private val messagesFr = listOf(
        "🧩 Une partie amusante t'attend !" to "Joue maintenant et bats ton record 🚀",
        "🔥 Ne perds pas ta série !" to "Et si tu effaçais quelques lignes aujourd'hui ?",
        "🎁 C'est le moment de récupérer des jetons gratuits" to "Regarde une pub, obtiens un boost",
        "🏆 Tes missions hebdomadaires t'attendent" to "N'oublie pas de réclamer ta récompense"
    )
    private val messagesEs = listOf(
        "🧩 ¡Una ronda divertida te espera!" to "Juega ahora y supera tu récord 🚀",
        "🔥 ¡No pierdas tu racha!" to "¿Qué tal si limpias unas líneas hoy?",
        "🎁 Es hora de conseguir fichas gratis" to "Mira un anuncio, obtén un potenciador",
        "🏆 Tus misiones semanales te esperan" to "No olvides reclamar tu recompensa"
    )

    private fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java) ?: return
            if (manager.getNotificationChannel(CHANNEL_ID) == null) {
                manager.createNotificationChannel(
                    NotificationChannel(CHANNEL_ID, "Blast the Blocks", NotificationManager.IMPORTANCE_DEFAULT)
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
