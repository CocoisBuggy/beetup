package com.coco.beetup.core.data

import java.util.Date
import kotlinx.coroutines.flow.Flow

class BeetRepository(
    private val beetProfileDao: BeetProfileDao,
    private val beetExerciseDao: BeetExerciseDao,
    private val exerciseLogDao: ExerciseLogDao,
) {
  fun getProfile(): Flow<BeetProfile> = beetProfileDao.getProfile()

  fun getAllExercises(): Flow<List<BeetExercise>> = beetExerciseDao.getAllExercises()

  fun getAllLogs(): Flow<List<BeetExerciseLog>> = exerciseLogDao.getAllLogs()

  fun exerciseLogs(
      day: Int,
      exercise: Int,
  ) = exerciseLogDao.exerciseLogsFor(day, exercise)

  fun activityGroupsForDay(day: Int) = exerciseLogDao.activityGroupsForDay(day)

  suspend fun insertExercise(exercise: BeetExercise) {
    beetExerciseDao.insert(exercise)
  }

  suspend fun deleteExercise(exercise: BeetExercise) {
    beetExerciseDao.delete(exercise)
  }

  suspend fun deleteExercises(exercises: Collection<BeetExercise>) {
    beetExerciseDao.delete(exercises.toList())
  }

  suspend fun insertLog(log: BeetExerciseLog) {
    exerciseLogDao.insert(log)
  }

  fun logsForDay(day: Int = (Date().time / 86_400_000L).toInt()) = exerciseLogDao.getLogsForDay(day)

  fun exercisesForDay(day: Int = (Date().time / 86_400_000L).toInt()) =
      beetExerciseDao.getActiveExercisesForDay(day)

  suspend fun deleteLogEntries(exercises: Collection<BeetExerciseLog>) {
    exerciseLogDao.delete(exercises.toList())
  }
}
