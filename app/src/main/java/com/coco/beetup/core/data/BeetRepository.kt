package com.coco.beetup.core.data

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import androidx.compose.runtime.snapshots.SnapshotStateMap
import com.coco.beetup.R
import java.io.BufferedReader
import java.io.InputStreamReader
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.concurrent.TimeUnit
import kotlin.sequences.forEach
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

private fun List<BeetResistance>.toMap(): Map<Int, BeetResistance> = this.associateBy { it.id }

private fun List<BeetMagnitude>.toMagMap(): Map<Int, BeetMagnitude> = this.associateBy { it.id }

data class ActivityOverview(val date: LocalDate, val count: Int)

fun Date.toLocalDate(): LocalDate {
  return this.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
}

fun Date.unixDay(): Int {
  return (this.time / 86_400_000L).toInt()
}

fun LocalDate.unixDay(): Int {
  return this.toEpochDay().toInt()
}

fun Date.daysSince(): Long {
  val diff = Date().time - this.time
  return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS)
}

class BeetRepository(
    private val database: BeetData,
    private val beetProfileDao: BeetProfileDao,
    private val beetExerciseDao: BeetExerciseDao,
    private val exerciseLogDao: ExerciseLogDao,
    private val exerciseNoteDao: ExerciseNoteDao,
    private val beetExerciseScheduleDao: BeetExerciseScheduleDao,
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
          beetExerciseDao.getAllExercises()
      ) { flatRows, allResistances, allMagnitudes, allExercises ->
        val extraResistancesMap = allResistances.toMap()
        val extraMagnitudes = allMagnitudes.toMagMap()
          val exerciseToMagnitude = allExercises.associate { it.id to it.magnitudeKind }

        val groupedByExercise =
            flatRows.groupBy { flat ->
              Triple(
                  flat.exercise.id,
                  flat.log.magnitude,
                  flat.resistanceEntry
                      .map { Pair(it.resistanceKind, it.resistanceValue) }
                      .sortedBy { it.first }
                      .distinct())
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
                  magValue = rowsForExercise.first().log.magnitude,
                  resistances =
                      rowsForExercise
                          .flatMap { it.resistanceEntry }
                          .map { Pair(it.resistanceKind, it.resistanceValue) }
                          .distinct(),
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
                                }
                                .distinctBy { Pair(it.extra.id, it.entry.resistanceValue) })
                  }
          exerciseByKey[key] = exercise
        }

        activityGroups.map { (key, value) ->
          ActivityGroup(
              exercise = exerciseByKey[key]!!,
              magnitude = extraMagnitudes[exerciseToMagnitude[key.exerciseId]!!]!!,
              logs = value.distinctBy { it.log.id },
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

  suspend fun insertResistances(resistances: List<BeetActivityResistance>) {
    exerciseLogDao.insertResistances(resistances)
  }

  fun logsForDay(day: Int = Date().unixDay()) = exerciseLogDao.getLogsForDay(day)

  suspend fun deleteLogEntries(exercises: List<BeetExerciseLog>) {
    exerciseLogDao.delete(exercises.toList())
  }

  fun validResistancesFor(exercise: Int) = beetExerciseDao.validResistancesFor(exercise)

  suspend fun insertActivityAndResistances(
      newExercise: BeetExerciseLog,
      selectedResistances: List<BeetActivityResistance>
  ) {
    exerciseLogDao.insertActivityAndResistances(newExercise, selectedResistances)
  }

  fun activityOverview(untilDate: Date = Date(), daysAgo: Int = 30): Flow<List<ActivityOverview>> {
    val minDate = untilDate.unixDay() - daysAgo
    return exerciseLogDao.activityOverview(minDate).map {
      it.map { item -> ActivityOverview(item.date.toLocalDate(), item.count) }
    }
  }

  fun exerciseDateOverview(
      untilDate: Date = Date(),
      daysAgo: Int = 30
  ): Flow<Map<Int, List<LocalDate>>> {
    val minDate = untilDate.unixDay() - daysAgo
    val map = mutableMapOf<Int, List<LocalDate>>()

    return exerciseLogDao.exerciseDateOverview(minDate).map {
      it.forEach { item ->
        map[item.exercise] = map.getOrDefault(item.exercise, emptyList()) + item.date.toLocalDate()
      }
      return@map map
    }
  }

  fun getExerciseUsageCounts(): Flow<List<ExerciseUsagePrimitive>> =
      exerciseLogDao.getExerciseUsageCounts()

  suspend fun updateLogAndResistances(
      log: BeetExerciseLog,
      selectedResistances: SnapshotStateMap<Int, Int>
  ) {
    exerciseLogDao.updateLogAndResistances(log, selectedResistances)
  }

  fun allMagnitudes(): Flow<List<BeetMagnitude>> = beetExerciseDao.getAllMagnitudes()

  suspend fun removeResistanceReference(exerciseId: Int, resistanceId: Int) =
      beetExerciseDao.removeResistanceReference(exerciseId, resistanceId)

  suspend fun insertResistanceReference(exerciseId: Int, resistanceId: Int) =
      beetExerciseDao.insertResistanceReference(exerciseId, resistanceId)

  suspend fun clearAllData(context: Context) {
    withContext(Dispatchers.IO) {
      database.clearAllTables()
      installDefaults(context)
    }
  }

  suspend fun installDefaults(context: Context, allowOverwrite: Boolean = false) {
    withContext(Dispatchers.IO) {
      val inputStream = context.resources.openRawResource(R.raw.defaults)
      val reader = BufferedReader(InputStreamReader(inputStream))

      reader.useLines { lines ->
        lines.forEach { line ->
          if (line.isNotBlank() && !line.trim().startsWith("--")) {
            Log.d("Database", line)
            try {
              database.openHelper.writableDatabase.execSQL(line)
            } catch (exc: SQLiteConstraintException) {
              if (exc.message?.contains("UNIQUE constraint failed") ?: false && allowOverwrite) {
                return@forEach
              }

              throw exc
            }
          }
        }
      }
    }
  }

  fun checkpoint() {
    database.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)").close()
  }

  fun close() {
    if (database.isOpen) {
      database.close()
    }
  }

  suspend fun getExerciseHistory(
      exerciseId: Int,
      since: Int,
      before: Int
  ): List<ActivityGroupFlatRow> {
    return exerciseLogDao.getFlatActivityDataForExerciseSince(exerciseId, since, before)
  }

  fun getNoteForDay(day: Int): Flow<ExerciseNote?> = exerciseNoteDao.getNoteForDay(day)

  suspend fun insertNote(note: ExerciseNote): Long = exerciseNoteDao.insertNote(note)

  suspend fun updateNote(note: ExerciseNote) = exerciseNoteDao.updateNote(note)

  suspend fun deleteNote(note: ExerciseNote) = exerciseNoteDao.deleteNote(note)

  suspend fun deleteNoteForDay(day: Int) = exerciseNoteDao.deleteNoteForDay(day)

  suspend fun updateLogEntry(log: BeetExerciseLog) {
    exerciseLogDao.update(log)
  }

  fun getBannerDates(): Flow<List<Int>> = exerciseLogDao.getBannerDates()

  // Schedule operations
  fun getAllSchedules(): Flow<List<BeetExerciseSchedule>> =
      beetExerciseScheduleDao.getAllSchedules()

  fun getSchedule(id: Int): Flow<BeetExerciseSchedule> = beetExerciseScheduleDao.getSchedule(id)

  fun getSchedulesForExercise(exerciseId: Int): Flow<List<BeetExerciseSchedule>> =
      beetExerciseScheduleDao.getSchedulesForExercise(exerciseId)

  fun getScheduleForExercise(exerciseId: Int): Flow<BeetExerciseSchedule?> =
      beetExerciseScheduleDao.getScheduleForExercise(exerciseId)

  suspend fun insertSchedule(schedule: BeetExerciseSchedule): Long =
      beetExerciseScheduleDao.insertSchedule(schedule)

  suspend fun updateSchedule(schedule: BeetExerciseSchedule) =
      beetExerciseScheduleDao.updateSchedule(schedule)

  suspend fun deleteSchedule(schedule: BeetExerciseSchedule) =
      beetExerciseScheduleDao.deleteSchedule(schedule)

  suspend fun deleteSchedulesForExercise(exerciseId: Int) =
      beetExerciseScheduleDao.deleteSchedulesForExercise(exerciseId)

  suspend fun getLastExerciseDate(exerciseId: Int): LocalDate? {
    val latestLog = exerciseLogDao.getLatestLogForExercise(exerciseId)
    return latestLog?.logDate?.toLocalDate()
  }
}
