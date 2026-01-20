package com.coco.beetup.core.data

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

class BeetRepositoryTest {

  @get:Rule val instantTaskExecutorRule = InstantTaskExecutorRule()

  @Mock private lateinit var database: BeetData

  @Mock private lateinit var beetProfileDao: BeetProfileDao

  @Mock private lateinit var beetExerciseDao: BeetExerciseDao

  @Mock private lateinit var exerciseLogDao: ExerciseLogDao

  @Mock private lateinit var exerciseNoteDao: ExerciseNoteDao

  @Mock private lateinit var beetExerciseScheduleDao: BeetExerciseScheduleDao

  private lateinit var repository: BeetRepository

  private val testDay = 19500

  @Before
  fun setup() {
    MockitoAnnotations.openMocks(this)
    repository =
        BeetRepository(
            database = database,
            beetProfileDao = beetProfileDao,
            beetExerciseDao = beetExerciseDao,
            exerciseLogDao = exerciseLogDao,
            exerciseNoteDao = exerciseNoteDao,
            beetExerciseScheduleDao = beetExerciseScheduleDao)
  }

  @Test
  fun activityGroupsForDayShouldGroupLogs() = runTest {
    // Arrange
    val exercise =
        BeetExercise(
            id = 1,
            exerciseName = "Push-ups",
            exerciseDescription = "Upper body exercise",
            magnitudeKind = 1)

    val magnitude =
        BeetMagnitude(
            id = 1, name = "Repetitions", description = "Number of repetitions", unit = "reps")

    val resistance =
        BeetResistance(
            id = 1, name = "Body Weight", description = "Body weight resistance", unit = "kg")

    val log1 = BeetExerciseLog(id = 1, exerciseId = 1, magnitude = 10, logDay = testDay)

    val log2 = BeetExerciseLog(id = 2, exerciseId = 1, magnitude = 10, logDay = testDay)

    val log3 = BeetExerciseLog(id = 3, exerciseId = 1, magnitude = 15, logDay = testDay)

    val resistance1 =
        BeetActivityResistance(activityId = 1, resistanceKind = 1, resistanceValue = 70)

    val resistance2 =
        BeetActivityResistance(activityId = 2, resistanceKind = 1, resistanceValue = 70)

    val resistance3 =
        BeetActivityResistance(activityId = 3, resistanceKind = 1, resistanceValue = 70)

    val flatRows =
        listOf(
            ActivityGroupFlatRow(
                log = log1, exercise = exercise, resistanceEntry = listOf(resistance1)),
            ActivityGroupFlatRow(
                log = log2, exercise = exercise, resistanceEntry = listOf(resistance2)),
            ActivityGroupFlatRow(
                log = log3, exercise = exercise, resistanceEntry = listOf(resistance3)))

    `when`(exerciseLogDao.getAllFlatActivityDataForDay(testDay)).thenReturn(flowOf(flatRows))
    `when`(beetExerciseDao.getAllResistances()).thenReturn(flowOf(listOf(resistance)))
    `when`(beetExerciseDao.getAllMagnitudes()).thenReturn(flowOf(listOf(magnitude)))

    // Act
    val result = repository.activityGroupsForDay(testDay).first()

    // Assert
    assertEquals(
        "Should create 2 activity groups - one for magnitude 10, one for magnitude 15",
        2,
        result.size)

    val groupForMag10 = result.find { it.logs.any { it.log.magnitude == 10 } }
    val groupForMag15 = result.find { it.logs.any { it.log.magnitude == 15 } }

    assertEquals("Group with magnitude 10 should have 2 logs", 2, groupForMag10?.logs?.size)
    assertEquals("Group with magnitude 15 should have 1 log", 1, groupForMag15?.logs?.size)

    // Verify that logs with same magnitude are grouped together
    assertTrue(
        "Group with magnitude 10 should only contain logs with magnitude 10",
        groupForMag10?.logs?.all { it.log.magnitude == 10 } == true)
    assertTrue(
        "Group with magnitude 15 should only contain logs with magnitude 15",
        groupForMag15?.logs?.all { it.log.magnitude == 15 } == true)
  }

  @Test
  fun activityGroupsForDayShouldHandleEmptyData() = runTest {
    // Arrange
    `when`(exerciseLogDao.getAllFlatActivityDataForDay(testDay)).thenReturn(flowOf(emptyList()))
    `when`(beetExerciseDao.getAllResistances()).thenReturn(flowOf(emptyList()))
    `when`(beetExerciseDao.getAllMagnitudes()).thenReturn(flowOf(emptyList()))

    // Act
    val result = repository.activityGroupsForDay(testDay).first()

    // Assert
    assertEquals("Should return empty list for no data", 0, result.size)
  }
}
