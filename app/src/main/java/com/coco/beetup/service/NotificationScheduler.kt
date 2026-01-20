package com.coco.beetup.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
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
import kotlinx.coroutines.runBlocking

class NotificationScheduler : BroadcastReceiver() {
  val channelId = "exercise_reminders"

  override fun onReceive(context: Context, intent: Intent) {
    val scheduleId = intent.getIntExtra("schedule_id", -1)
    val exerciseId = intent.getIntExtra("exercise_id", -1)
    val message = intent.getStringExtra("message") ?: "Time to exercise!"
    val reminderType = intent.getStringExtra("reminder_type")
    val permitted =
        ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

    if (!permitted) {
      Log.d("NotificationScheduler", "Notification permission not granted")
    }

    if (reminderType != ReminderStrength.IN_APP.name && permitted) {
      showNotification(context, exerciseId, message)
    }

    // Schedule the next notification
    scheduleNextNotification(context, scheduleId)
  }

  @RequiresPermission("android.permission.POST_NOTIFICATIONS")
  fun showNotification(context: Context, exerciseId: Int, message: String) {
    // Check for notification permission on Android 13+
    val notificationManager = NotificationManagerCompat.from(context)
    if (!notificationManager.areNotificationsEnabled()) {
      return // Don't show notification if permission is denied
    }

    val channel =
        NotificationChannel(channelId, "Exercise Reminders", NotificationManager.IMPORTANCE_DEFAULT)
            .apply { description = "Reminders for scheduled exercises" }

    notificationManager.createNotificationChannel(channel)

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

  fun scheduleNextNotification(context: Context, scheduleId: Int) {
    // Get the schedule from database and reschedule it
    // This will need to be implemented with proper database access
    // For now, we'll use ExerciseNotificationManager to handle rescheduling
    // TODO: Implement proper schedule retrieval and rescheduling
  }
}

class ScheduleNotificationWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

  override fun doWork(): Result {
    val context = applicationContext
    val scheduleId = inputData.getInt("schedule_id", -1)
    val exerciseId = inputData.getInt("exercise_id", -1)
    val message = inputData.getString("message") ?: "Time to exercise!"
    val reminderType = inputData.getString("reminder_type")
    val permitted =
        ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

    if (reminderType != ReminderStrength.IN_APP.name && permitted) {
      NotificationScheduler().showNotification(context, exerciseId, message)
    }

    // Schedule the next notification
    NotificationScheduler().scheduleNextNotification(context, scheduleId)

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
    val delay = runBlocking {
      calculateMonotonicDelay(context, schedule.activityId, schedule.monotonicDays ?: 1)
    }

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
    val workManager = WorkManager.getInstance(context)

    val data =
        Data.Builder()
            .putInt("schedule_id", schedule.id)
            .putInt("exercise_id", schedule.activityId)
            .putString("message", schedule.message ?: "Time to exercise!")
            .putString("reminder_type", schedule.reminder?.name)
            .putInt("follows_exercise", schedule.followsExercise ?: -1)
            .build()

    // Schedule notification 1 hour after the target exercise is typically completed
    // For now, we'll use a daily check approach
    val workRequest =
        OneTimeWorkRequest.Builder(ScheduleNotificationWorker::class.java)
            .setInitialDelay(24, TimeUnit.HOURS) // Check daily
            .setInputData(data)
            .addTag("exercise_schedule_${schedule.id}")
            .addTag("follows_exercise")
            .build()

    workManager.enqueueUniqueWork(
        "exercise_schedule_${schedule.id}", ExistingWorkPolicy.REPLACE, workRequest)
  }

  private suspend fun calculateMonotonicDelay(
      context: Context,
      exerciseId: Int,
      daysBetween: Int
  ): Long {
    val application = context.applicationContext as com.coco.beetup.BeetupApplication
    val repository = application.repository

    // Get the last exercise date for this exercise
    val lastExerciseDate = repository.getLastExerciseDate(exerciseId)
    val nextExerciseDate =
        if (lastExerciseDate != null) {
          lastExerciseDate.plusDays(daysBetween.toLong())
        } else {
          LocalDate.now().plusDays(daysBetween.toLong())
        }

    val targetTime = LocalTime.of(9, 0) // Default to 9 AM
    val targetDateTime = nextExerciseDate.atTime(targetTime)

    return ChronoUnit.MILLIS.between(LocalDateTime.now(), targetDateTime)
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

  suspend fun getScheduledNotificationTime(context: Context, scheduleId: Int): LocalDateTime? {
    val workManager = WorkManager.getInstance(context)
    return try {
      val workInfos = workManager.getWorkInfosByTag("exercise_schedule_$scheduleId").get()

      // Find the next scheduled work that is not finished
      val nextWork =
          workInfos
              .filter { work ->
                work.state != WorkInfo.State.CANCELLED &&
                    work.state != WorkInfo.State.SUCCEEDED &&
                    work.state != WorkInfo.State.FAILED
              }
              .minByOrNull { work -> work.nextScheduleTimeMillis }

      nextWork?.nextScheduleTimeMillis?.let { timestamp ->
        val instant = java.time.Instant.ofEpochMilli(timestamp)
        LocalDateTime.ofInstant(instant, java.time.ZoneId.systemDefault())
      }
    } catch (e: Exception) {
      null
    }
  }

  suspend fun getNextNotificationTime(
      context: Context,
      schedule: BeetExerciseSchedule
  ): LocalDateTime? {
    // Check if notifications are enabled
    val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
    val notificationsEnabled = prefs.getBoolean("notifications_enabled", true)

    if (!notificationsEnabled || schedule.reminder == ReminderStrength.IN_APP) {
      return null
    }

    return when (schedule.kind) {
      ScheduleKind.MONOTONIC -> getNextMonotonicTime(context, schedule)
      ScheduleKind.DAY_OF_WEEK -> getNextDayOfWeekTime(schedule)
      ScheduleKind.FOLLOWS_EXERCISE -> getNextFollowsExerciseTime(context, schedule)
    }
  }

  private suspend fun getNextMonotonicTime(
      context: Context,
      schedule: BeetExerciseSchedule
  ): LocalDateTime? {
    val application = context.applicationContext as com.coco.beetup.BeetupApplication
    val repository = application.repository

    val lastExerciseDate = repository.getLastExerciseDate(schedule.activityId)
    val nextExerciseDate =
        if (lastExerciseDate != null) {
          lastExerciseDate.plusDays(schedule.monotonicDays?.toLong() ?: 1L)
        } else {
          LocalDate.now().plusDays(schedule.monotonicDays?.toLong() ?: 1L)
        }

    val targetTime = LocalTime.of(9, 0) // Default to 9 AM
    return nextExerciseDate.atTime(targetTime)
  }

  private fun getNextDayOfWeekTime(schedule: BeetExerciseSchedule): LocalDateTime? {
    val now = LocalDate.now()
    val currentDay = now.dayOfWeek.value % 7 // Convert to 0-6 format where 0=Sunday

    var daysUntilTarget = (schedule.dayOfWeek ?: 1) - currentDay
    if (daysUntilTarget <= 0) {
      daysUntilTarget += 7
    }

    val targetDate = now.plusDays(daysUntilTarget.toLong())
    val targetTime = LocalTime.of(9, 0) // Default to 9 AM
    return targetDate.atTime(targetTime)
  }

  private suspend fun getNextFollowsExerciseTime(
      context: Context,
      schedule: BeetExerciseSchedule
  ): LocalDateTime? {
    val application = context.applicationContext as com.coco.beetup.BeetupApplication
    val repository = application.repository

    val followsExerciseId = schedule.followsExercise ?: return null
    val lastTargetExerciseDate = repository.getLastExerciseDate(followsExerciseId)

    return if (lastTargetExerciseDate != null) {
      val nextNotificationDate = lastTargetExerciseDate.plusDays(1) // Day after target exercise
      val targetTime = LocalTime.of(10, 0) // 10 AM to give time for exercise completion
      nextNotificationDate.atTime(targetTime)
    } else {
      null // No target exercise completed yet
    }
  }
}
