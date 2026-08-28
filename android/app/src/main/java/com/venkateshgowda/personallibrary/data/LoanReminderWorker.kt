package com.venkateshgowda.personallibrary.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.room.Room
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

class LoanReminderWorker(context: Context, parameters: WorkerParameters) : CoroutineWorker(context, parameters) {
    override suspend fun doWork(): Result = runCatching {
        val database = Room.databaseBuilder(applicationContext, LibraryDatabase::class.java, "library.db")
            .addMigrations(LibraryDatabase.MIGRATION_5_6, LibraryDatabase.MIGRATION_6_7, LibraryDatabase.MIGRATION_7_8, LibraryDatabase.MIGRATION_8_9, LibraryDatabase.MIGRATION_9_10, LibraryDatabase.MIGRATION_10_11, LibraryDatabase.MIGRATION_11_12, LibraryDatabase.MIGRATION_12_13)
            .build()
        val today = LocalDate.now()
        val activeLoans = database.loanDao().allLoans().filter { it.actualReturnDate == null && it.expectedReturnDate != null }
        val relevant = activeLoans.filter { LoanReminderPolicy.needsReminder(it.expectedReturnDate, today) }
        if (relevant.isNotEmpty()) notify(relevant.size)
        database.close()
    }.fold(onSuccess = { Result.success() }, onFailure = { Result.retry() })

    private suspend fun notify(count: Int) {
        val settings = AppSettings(applicationContext)
        val detailed = settings.detailedReminders.first()
        val manager = applicationContext.getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) manager.createNotificationChannel(NotificationChannel(CHANNEL, "Loan reminders", NotificationManager.IMPORTANCE_DEFAULT))
        val message = if (detailed) "$count loan reminder${if (count == 1) "" else "s"} need attention." else "You have personal library reminders."
        manager.notify(NOTIFICATION_ID, NotificationCompat.Builder(applicationContext, CHANNEL).setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle("Personal Library").setContentText(message).setStyle(NotificationCompat.BigTextStyle().bigText(message)).setAutoCancel(true).setVisibility(NotificationCompat.VISIBILITY_PRIVATE).build())
    }

    companion object {
        private const val CHANNEL = "loan_reminders"
        private const val NOTIFICATION_ID = 2001
    }
}

object LoanReminderScheduler {
    private const val WORK_NAME = "loan_reminders_daily"
    fun schedule(context: Context) {
        val now = LocalDateTime.now()
        var next = now.toLocalDate().atTime(LocalTime.of(9, 0))
        if (!next.isAfter(now)) next = next.plusDays(1)
        val request = PeriodicWorkRequestBuilder<LoanReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(Duration.between(now, next).toMillis(), TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.UPDATE, request)
    }
    fun cancel(context: Context) = WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
}