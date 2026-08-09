package com.example.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.data.GameStateRepository
import java.util.concurrent.TimeUnit
import kotlin.random.Random
import kotlinx.coroutines.flow.first

// Faz 27: sabit periyodik bir WorkManager istegi yerine, her calisma bir
// SONRAKI calismayi 20-32 saat arasinda RASTGELE bir gecikmeyle kendisi
// zamanliyor — kullanicinin istedigi "random bir sekilde push yolluyor"
// hissini verir (tamamen ongorulebilir sabit saatli bildirimler yerine).
class ReminderWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val repository = GameStateRepository(applicationContext)
        val progress = repository.playerProgress.first()
        if (progress.notificationsEnabled) {
            NotificationHelper.showReminder(applicationContext, progress.language)
        }
        scheduleNext(applicationContext, ExistingWorkPolicy.REPLACE)
        return Result.success()
    }

    companion object {
        private const val UNIQUE_WORK_NAME = "blast_reminder_chain"

        private fun scheduleNext(context: Context, policy: ExistingWorkPolicy) {
            val delayHours = Random.nextInt(20, 33)
            val request = OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInitialDelay(delayHours.toLong(), TimeUnit.HOURS)
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork(UNIQUE_WORK_NAME, policy, request)
        }

        // Uygulama her acildiginda cagrilir — zaten bekleyen bir zincir varsa
        // dokunmaz (KEEP), yoksa ilk halkayi baslatir.
        fun ensureScheduled(context: Context) {
            scheduleNext(context, ExistingWorkPolicy.KEEP)
        }
    }
}
