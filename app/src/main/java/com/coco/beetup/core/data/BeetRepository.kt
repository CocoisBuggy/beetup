package com.coco.beetup.core.data

import java.util.Date
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

private fun List<BeetResistance>.toMap(): Map<Int, BeetResistance> = this.associateBy { it.id }

private fun List<BeetMagnitude>.toMagMap(): Map<Int, BeetMagnitude> = this.associateBy { it.id }

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

  fun getMagnitude(id: Int) = beetExerciseDao.getMagnitude(id)

  fun activityGroupsForDay(day: Int): Flow<List<ActivityGroup>> =
      combine(
          exerciseLogDao.getAllFlatActivityDataForDay(day),
          beetExerciseDao.getAllResistances(),
          beetExerciseDao.getAllMagnitudes(),
      ) { flatRows, allResistances, allMagnitudes ->
        val extraResistancesMap = allResistances.toMap()
        val extraMagnitudes = allMagnitudes.toMagMap()
        val groupedByExercise =
            flatRows.groupBy {
              Triple(
                  it.exercise.id,
                  it.log.magnitude,
                  it.resistanceEntry,
              )
            }

        val exerciseByKey: MutableMap<ActivityKey, BeetExercise> = mutableMapOf()
        val activityGroups: MutableMap<ActivityKey, List<BeetExerciseLogWithResistances>> =
            mutableMapOf()

        groupedByExercise.values.forEach { rowsForExercise ->
          // This gives us logs collected by exercise and magnitude.
          // from here, we need to collect all resistances for each log.
          val exercise = rowsForExercise.first().exercise

          val key =
              ActivityKey(
                  exerciseId = exercise.id,
                  magValue = exercise.magnitudeKind,
                  resistances =
                      rowsForExercise
                          .flatMap { it.resistanceEntry }
                          .map { Pair(it.resistanceKind, it.resistanceValue) },
              )

          activityGroups[key] =
              activityGroups.getOrDefault(key, emptyList()) +
                  rowsForExercise.map { row ->
                    BeetExerciseLogWithResistances(
                        log = row.log,
                        resistances =
                            rowsForExercise
                                .flatMap { it.resistanceEntry }
                                .map { flatRow ->
                                  BeetExpandedResistance(
                                      entry = flatRow,
                                      extra = extraResistancesMap[flatRow.resistanceKind]!!,
                                  )
                                })
                  }
          exerciseByKey[key] = exercise
        }

        activityGroups.map { (key, value) ->
          ActivityGroup(
              exercise = exerciseByKey[key]!!,
              magnitude = extraMagnitudes[key.magValue]!!,
              logs = value,
          )
        }
      }

  fun allResistances() = beetExerciseDao.getAllResistances()

  suspend fun insertExercise(exercise: BeetExercise) {
    beetExerciseDao.insert(exercise)
  }

  suspend fun deleteExercise(exercise: BeetExercise) {
    beetExerciseDao.delete(exercise)
  }

  suspend fun deleteExercises(exercises: Collection<BeetExercise>) {
    beetExerciseDao.delete(exercises.toList())
  }

  suspend fun insertLog(log: BeetExerciseLog): Int {
    return exerciseLogDao.insert(log).toInt()
  }

  suspend fun insertResistances(resistances: List<BeetActivityResistance>) {
    exerciseLogDao.insertResistances(resistances)
  }

  fun logsForDay(day: Int = (Date().time / 86_400_000L).toInt()) = exerciseLogDao.getLogsForDay(day)

  fun exercisesForDay(day: Int = (Date().time / 86_400_000L).toInt()) =
      beetExerciseDao.getActiveExercisesForDay(day)

  suspend fun deleteLogEntries(exercises: List<BeetExerciseLog>) {
    exerciseLogDao.delete(exercises.toList())
  }

  fun validResistancesFor(exercise: Int) = beetExerciseDao.validResistancesFor(exercise)
}
