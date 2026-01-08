package com.coco.beetup.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.coco.beetup.MainActivity
import com.coco.beetup.R
import com.coco.beetup.core.data.BeetExerciseSchedule
import com.coco.beetup.core.data.ReminderStrength
import com.coco.beetup.core.data.ScheduleKind
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

class NotificationScheduler : BroadcastReceiver() {

  override fun onReceive(context: Context, intent: Intent) {
    val scheduleId = intent.getIntExtra("schedule_id", -1)
    val exerciseId = intent.getIntExtra("exercise_id", -1)
    val message = intent.getStringExtra("message") ?: "Time to exercise!"
    val reminderType = intent.getStringExtra("reminder_type")

    if (reminderType != ReminderStrength.IN_APP.name) {
      showNotification(context, exerciseId, message)
    }

    // Schedule the next notification
    scheduleNextNotification(context, scheduleId)
  }

  private fun showNotification(context: Context, exerciseId: Int, message: String) {
    val channelId = "exercise_reminders"

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel =
          NotificationChannel(
                  channelId, "Exercise Reminders", NotificationManager.IMPORTANCE_DEFAULT)
              .apply { description = "Reminders for scheduled exercises" }

      val notificationManager =
          context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
      notificationManager.createNotificationChannel(channel)
    }

    val intent =
        Intent(context, MainActivity::class.java).apply {
          flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

    val pendingIntent =
        PendingIntent.getActivity(
            context,
            exerciseId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

    val notification =
        NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Exercise Reminder")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

    with(NotificationManagerCompat.from(context)) { notify(exerciseId, notification) }
  }

  private fun scheduleNextNotification(context: Context, scheduleId: Int) {
    // This would be implemented to reschedule based on the schedule type
    // For now, we'll just leave it as is
  }
}

class ScheduleNotificationWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

  override fun doWork(): Result {
    val context = applicationContext
    val scheduleId = inputData.getInt("schedule_id", -1)
    val exerciseId = inputData.getInt("exercise_id", -1)
    val message = inputData.getString("message") ?: "Time to exercise!"

    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel =
          NotificationChannel(
              "exercise_reminders", "Exercise Reminders", NotificationManager.IMPORTANCE_DEFAULT)
      notificationManager.createNotificationChannel(channel)
    }

    // This would handle the actual notification logic
    return Result.success()
  }
}

object ExerciseNotificationManager {

  fun scheduleNotification(context: Context, schedule: BeetExerciseSchedule) {
    when (schedule.kind) {
      ScheduleKind.MONOTONIC -> scheduleMonotonicNotification(context, schedule)
      ScheduleKind.DAY_OF_WEEK -> scheduleDayOfWeekNotification(context, schedule)
      ScheduleKind.FOLLOWS_EXERCISE -> scheduleFollowsExerciseNotification(context, schedule)
    }
  }

  private fun scheduleMonotonicNotification(context: Context, schedule: BeetExerciseSchedule) {
    // Get the last exercise date for this exercise
    val workManager = WorkManager.getInstance(context)

    val data =
        Data.Builder()
            .putInt("schedule_id", schedule.id)
            .putInt("exercise_id", schedule.activityId)
            .putString("message", schedule.message ?: "Time to exercise!")
            .putString("reminder_type", schedule.reminder?.name)
            .build()

    // Calculate delay until next notification
    val delay = calculateMonotonicDelay(context, schedule.activityId, schedule.monotonicDays ?: 1)

    val workRequest =
        OneTimeWorkRequest.Builder(ScheduleNotificationWorker::class.java)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .addTag("exercise_schedule_${schedule.id}")
            .build()

    workManager.enqueueUniqueWork(
        "exercise_schedule_${schedule.id}", ExistingWorkPolicy.REPLACE, workRequest)
  }

  private fun scheduleDayOfWeekNotification(context: Context, schedule: BeetExerciseSchedule) {
    val workManager = WorkManager.getInstance(context)

    val data =
        Data.Builder()
            .putInt("schedule_id", schedule.id)
            .putInt("exercise_id", schedule.activityId)
            .putString("message", schedule.message ?: "Time to exercise!")
            .putString("reminder_type", schedule.reminder?.name)
            .build()

    // Calculate delay until next occurrence of the specified day
    val delay = calculateDayOfWeekDelay(schedule.dayOfWeek ?: 1)

    val workRequest =
        OneTimeWorkRequest.Builder(ScheduleNotificationWorker::class.java)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .addTag("exercise_schedule_${schedule.id}")
            .build()

    workManager.enqueueUniqueWork(
        "exercise_schedule_${schedule.id}", ExistingWorkPolicy.REPLACE, workRequest)
  }

  private fun scheduleFollowsExerciseNotification(
      context: Context,
      schedule: BeetExerciseSchedule
  ) {
    // This would monitor when the target exercise is completed
    // For now, we'll skip implementation
  }

  private fun calculateMonotonicDelay(context: Context, exerciseId: Int, daysBetween: Int): Long {
    // This would query the repository to get the last exercise date
    // and calculate the delay until the next one
    // For now, return a default value
    return TimeUnit.DAYS.toMillis(daysBetween.toLong())
  }

  private fun calculateDayOfWeekDelay(targetDay: Int): Long {
    val now = LocalDate.now()
    val currentDay = now.dayOfWeek.value % 7 // Convert to 0-6 format where 0=Sunday

    var daysUntilTarget = targetDay - currentDay
    if (daysUntilTarget <= 0) {
      daysUntilTarget += 7
    }

    val targetDate = now.plusDays(daysUntilTarget.toLong())
    val targetTime = LocalTime.of(9, 0) // Default to 9 AM
    val targetDateTime = targetDate.atTime(targetTime)

    return ChronoUnit.MILLIS.between(LocalDateTime.now(), targetDateTime)
  }

  fun cancelNotification(context: Context, scheduleId: Int) {
    val workManager = WorkManager.getInstance(context)
    workManager.cancelUniqueWork("exercise_schedule_$scheduleId")
  }

  fun cancelAllNotifications(context: Context) {
    val workManager = WorkManager.getInstance(context)
    workManager.cancelAllWorkByTag("exercise_schedule")
  }
}
