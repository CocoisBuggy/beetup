package com.coco.beetup.core.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BeetDaoTest {

  private lateinit var database: BeetData
  private lateinit var beetProfileDao: BeetProfileDao
  private lateinit var beetExerciseDao: BeetExerciseDao
  private lateinit var exerciseLogDao: ExerciseLogDao
  private lateinit var exerciseNoteDao: ExerciseNoteDao
  private lateinit var beetExerciseScheduleDao: BeetExerciseScheduleDao

  private val testDay = 19500

  @Before
  fun setup() {
    database =
        Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(), BeetData::class.java)
            .allowMainThreadQueries()
            .build()

    beetProfileDao = database.beetProfileDao()
    beetExerciseDao = database.beetExerciseDao()
    exerciseLogDao = database.exerciseLogDao()
    exerciseNoteDao = database.exerciseNoteDao()
    beetExerciseScheduleDao = database.beetExerciseScheduleDao()
  }

  @After
  fun tearDown() {
    database.close()
  }

  @Test
  fun `insertAndGetExercise should work correctly`() = runTest {
    // Arrange
    val exercise =
        BeetExercise(
            id = 1,
            exerciseName = "Test Exercise",
            exerciseDescription = "Test Description",
            magnitudeKind = 1)

    // Act
    beetExerciseDao.insert(exercise)
    val result = beetExerciseDao.getExercise(1).first()

    // Assert
    assertNotNull(result)
    assertEquals("Test Exercise", result.exerciseName)
    assertEquals("Test Description", result.exerciseDescription)
    assertEquals(1, result.magnitudeKind)
  }

  @Test
  fun `getAllExercises should return all exercises`() = runTest {
    // Arrange
    val exercises =
        listOf(
            BeetExercise(1, "Exercise 1", "Description 1", 1),
            BeetExercise(2, "Exercise 2", "Description 2", 2),
            BeetExercise(3, "Exercise 3", "Description 3", 1))
    beetExerciseDao.insert(*exercises.toTypedArray())

    // Act
    val result = beetExerciseDao.getAllExercises().first()

    // Assert
    assertEquals(3, result.size)
    assertTrue(result.all { it.exerciseName.startsWith("Exercise") })
  }

  @Test
  fun `insertAndGetMagnitude should work correctly`() = runTest {
    // Arrange
    val magnitude =
        BeetMagnitude(
            id = 1, name = "Repetitions", description = "Number of repetitions", unit = "reps")

    // Act
    beetExerciseDao.insert(magnitude)
    val result = beetExerciseDao.getMagnitude(1).first()

    // Assert
    assertNotNull(result)
    assertEquals("Repetitions", result.name)
    assertEquals("Number of repetitions", result.description)
    assertEquals("reps", result.unit)
  }

  @Test
  fun `insertAndGetResistance should work correctly`() = runTest {
    // Arrange
    val resistance =
        BeetResistance(
            id = 1, name = "Body Weight", description = "Body weight resistance", unit = "kg")

    // Act
    beetExerciseDao.insert(resistance)
    val result = beetExerciseDao.getResistance(1).first()

    // Assert
    assertNotNull(result)
    assertEquals("Body Weight", result.name)
    assertEquals("Body weight resistance", result.description)
    assertEquals("kg", result.unit)
  }

  @Test
  fun `insertActivityAndResistances should work correctly`() = runTest {
    // Arrange
    val exercise =
        BeetExercise(
            id = 1,
            exerciseName = "Push-ups",
            exerciseDescription = "Upper body exercise",
            magnitudeKind = 1)
    val log = BeetExerciseLog(id = 1, exerciseId = 1, magnitude = 10, logDay = testDay)
    val resistances =
        listOf(
            BeetActivityResistance(activityId = 1, resistanceKind = 1, resistanceValue = 70),
            BeetActivityResistance(activityId = 1, resistanceKind = 2, resistanceValue = 20))

    beetExerciseDao.insert(exercise)

    // Act
    exerciseLogDao.insertActivityAndResistances(log, resistances)

    // Assert
    val logsForDay = exerciseLogDao.getLogsForDay(testDay).first()
    assertEquals(1, logsForDay.size)
    assertEquals(10, logsForDay.first().magnitude)
  }

  @Test
  fun `getAllFlatActivityDataForDay should return correctly grouped data`() = runTest {
    // Arrange
    val exercise =
        BeetExercise(
            id = 1,
            exerciseName = "Bench Press",
            exerciseDescription = "Upper body exercise",
            magnitudeKind = 1)
    val resistance =
        BeetResistance(id = 1, name = "Barbell", description = "Barbell resistance", unit = "kg")

    val log1 = BeetExerciseLog(id = 1, exerciseId = 1, magnitude = 10, logDay = testDay)

    val log2 = BeetExerciseLog(id = 2, exerciseId = 1, magnitude = 12, logDay = testDay)

    val resistance1 =
        BeetActivityResistance(activityId = 1, resistanceKind = 1, resistanceValue = 60)

    val resistance2 =
        BeetActivityResistance(activityId = 2, resistanceKind = 1, resistanceValue = 80)

    beetExerciseDao.insert(exercise, resistance)
    exerciseLogDao.insertActivityAndResistances(log1, listOf(resistance1))
    exerciseLogDao.insertActivityAndResistances(log2, listOf(resistance2))

    // Act
    val flatData = exerciseLogDao.getAllFlatActivityDataForDay(testDay).first()

    // Assert
    assertEquals(2, flatData.size)
    assertTrue(flatData.all { it.exercise.id == 1 })
    assertEquals(1, flatData.count { it.log.magnitude == 10 })
    assertEquals(1, flatData.count { it.log.magnitude == 12 })
  }

  @Test
  fun `validResistancesFor should return correct resistances for exercise`() = runTest {
    // Arrange
    val exercise =
        BeetExercise(
            id = 1,
            exerciseName = "Squats",
            exerciseDescription = "Lower body exercise",
            magnitudeKind = 1)

    val resistance1 =
        BeetResistance(id = 1, name = "Barbell", description = "Barbell resistance", unit = "kg")

    val resistance2 =
        BeetResistance(id = 2, name = "Dumbbell", description = "Dumbbell resistance", unit = "kg")

    beetExerciseDao.insert(exercise, resistance1, resistance2)
    beetExerciseDao.insertResistanceReference(1, 1)
    beetExerciseDao.insertResistanceReference(1, 2)

    // Act
    val validResistances = beetExerciseDao.validResistancesFor(1).first()

    // Assert
    assertEquals(2, validResistances.size)
    assertTrue(validResistances.any { it.id == 1 })
    assertTrue(validResistances.any { it.id == 2 })
  }

  @Test
  fun `insertAndGetNote should work correctly`() = runTest {
    // Arrange
    val note = ExerciseNote(id = 1, noteDay = testDay, noteText = "Test note for the day")

    // Act
    val noteId = exerciseNoteDao.insertNote(note)
    val result = exerciseNoteDao.getNoteForDay(testDay).first()

    // Assert
    assertNotNull(result)
    assertEquals("Test note for the day", result.noteText)
    assertEquals(testDay, result.noteDay)
  }

  @Test
  fun `insertAndGetSchedule should work correctly`() = runTest {
    // Arrange
    val schedule =
        BeetExerciseSchedule(
            id = 1, exerciseId = 1, scheduleTime = LocalDateTime.now(), enabled = true)

    // Act
    val scheduleId = beetExerciseScheduleDao.insertSchedule(schedule)
    val result = beetExerciseScheduleDao.getSchedule(1).first()

    // Assert
    assertNotNull(result)
    assertEquals(1, result.exerciseId)
    assertEquals(true, result.enabled)
  }

  @Test
  fun `updateLogAndResistances should update log and replace resistances`() = runTest {
    // Arrange
    val exercise =
        BeetExercise(
            id = 1,
            exerciseName = "Test Exercise",
            exerciseDescription = "Test Description",
            magnitudeKind = 1)
    val resistance =
        BeetResistance(
            id = 1, name = "Test Resistance", description = "Test resistance", unit = "kg")

    val log = BeetExerciseLog(id = 1, exerciseId = 1, magnitude = 10, logDay = testDay)

    val initialResistances =
        listOf(BeetActivityResistance(activityId = 1, resistanceKind = 1, resistanceValue = 50))

    beetExerciseDao.insert(exercise, resistance)
    exerciseLogDao.insertActivityAndResistances(log, initialResistances)

    // Act - update the log and resistances
    val updatedLog = log.copy(magnitude = 15)
    val updatedResistances =
        androidx.compose.runtime.snapshots.SnapshotStateMap<Int, Int>().apply {
          put(1, 75) // Update resistance value
        }

    exerciseLogDao.updateLogAndResistances(updatedLog, updatedResistances)

    // Assert
    val logsForDay = exerciseLogDao.getLogsForDay(testDay).first()
    assertEquals(1, logsForDay.size)
    assertEquals(15, logsForDay.first().magnitude)

    // Verify resistances were updated
    val flatData = exerciseLogDao.getAllFlatActivityDataForDay(testDay).first()
    assertEquals(1, flatData.size)
    assertEquals(75, flatData.first().resistanceEntry.first().resistanceValue)
  }

  @Test
  fun `removeResistanceReference should remove valid resistance for exercise`() = runTest {
    // Arrange
    val exercise =
        BeetExercise(
            id = 1,
            exerciseName = "Test Exercise",
            exerciseDescription = "Test Description",
            magnitudeKind = 1)
    val resistance =
        BeetResistance(
            id = 1, name = "Test Resistance", description = "Test resistance", unit = "kg")

    beetExerciseDao.insert(exercise, resistance)
    beetExerciseDao.insertResistanceReference(1, 1)

    // Verify resistance is initially valid
    val initialValidResistances = beetExerciseDao.validResistancesFor(1).first()
    assertEquals(1, initialValidResistances.size)

    // Act
    beetExerciseDao.removeResistanceReference(1, 1)

    // Assert
    val finalValidResistances = beetExerciseDao.validResistancesFor(1).first()
    assertEquals(0, finalValidResistances.size)
  }
}
