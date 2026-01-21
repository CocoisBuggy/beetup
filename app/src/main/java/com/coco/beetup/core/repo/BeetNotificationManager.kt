package com.coco.beetup.core.repo

import com.coco.beetup.core.data.BeetExerciseSchedule
import com.coco.beetup.core.data.BeetExerciseScheduleDao
import com.coco.beetup.core.data.ExerciseLogDao
import com.coco.beetup.core.data.ScheduleKind
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class BeetNotificationManager(
    private val exerciseLogDao: ExerciseLogDao,
    private val scheduleDao: BeetExerciseScheduleDao,
) {
  val schedule = scheduleDao.getAllSchedules()
  val notifications =
      schedule.map { list ->
        list.map { item ->
          item.enabled &&
              !item.dismissed &&
              isActive(item, LocalDate.now()) &&
              !hasBeenFulfilled(item, LocalDate.now())
        }
      }

  suspend fun isActive(item: BeetExerciseSchedule, forDay: LocalDate): Boolean =
      when (item.kind) {
        // When the schedule is based on monotonic firing, we can easily
        // determine if the item is active based on the present time
        ScheduleKind.MONOTONIC -> item.showAfter != null && item.showAfter >= forDay
        // When a schedule is supposed to fire on a day of the week, then we only
        // care if today is that day.
        ScheduleKind.DAY_OF_WEEK -> item.dayOfWeek == forDay.dayOfWeek
        ScheduleKind.FOLLOWS_EXERCISE -> {
          // When we combine the monotonic days with the LAST time we saw
          // the exercise we are supposed to follow, we can determine if this schedule
          // item is supposed to be active
          item.followsExercise?.let { followsExercise ->
            exerciseLogDao.getLatestLogForExercise(followsExercise)?.let { happened ->
              happened.logDate.toLocalDate().plusDays((item.monotonicDays ?: 0).toLong()) <= forDay
            }
          } ?: false
        }
      }

  suspend fun hasBeenFulfilled(item: BeetExerciseSchedule, forDate: LocalDate): Boolean {
    exerciseLogDao.getLatestLogForExercise(item.exerciseId)?.let { scheduleExercise ->
      if (scheduleExercise.logDate.toLocalDate() <= forDate) {
        // In this case, we have an exercise log proving that we did this exercise
        // on or before the date
        return true
      }
    }

    return false
  }

  suspend fun nextSchedule(): List<BeetExerciseSchedule> {
    val today = LocalDate.now()
    val active = mutableListOf<BeetExerciseSchedule>()
    for (item in schedule.first().filter { it.enabled && !it.dismissed }) {
      if (isActive(item, today) && hasBeenFulfilled(item, today).not()) {
        active.add(item)
      }
    }

    return active
  }

  suspend fun dismiss(scheduleId: Int) {
    scheduleDao.dismiss(scheduleId)
  }

  /** When an exercise happens, we bump all of the schedules for that exercise. */
  suspend fun reschedule(exercise: Int) {
    val schedules = scheduleDao.getSchedulesForExercise(exercise).first()
    val now = LocalDate.now()

    for (schedule in schedules) {
      when (schedule.kind) {
        ScheduleKind.MONOTONIC -> {
          scheduleDao.updateSchedule(
              schedule.copy(
                  dismissed = false,
                  showAfter = now.plusDays((schedule.monotonicDays ?: 0).toLong())))
        }
        ScheduleKind.DAY_OF_WEEK -> {}
        ScheduleKind.FOLLOWS_EXERCISE -> {}
      }
    }
  }
}
