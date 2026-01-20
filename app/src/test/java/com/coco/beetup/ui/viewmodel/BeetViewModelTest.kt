package com.coco.beetup.ui.viewmodel

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.coco.beetup.core.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

@ExperimentalCoroutinesApi
class BeetViewModelTest {

  @get:Rule val instantTaskExecutorRule = InstantTaskExecutorRule()

  private val testDispatcher = StandardTestDispatcher()

  @Mock private lateinit var repository: BeetRepository

  @Mock private lateinit var context: Context

  private lateinit var viewModel: BeetViewModel

  private val testDay = 19500

  @Before
  fun setup() {
    MockitoAnnotations.openMocks(this)
    Dispatchers.setMain(testDispatcher)
    viewModel = BeetViewModel(repository, context)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun activityGroupsForDayShouldReturnResult() = runTest {
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

    val log = BeetExerciseLog(id = 1, exerciseId = 1, magnitude = 10, logDay = testDay)

    val logWithResistances = BeetExerciseLogWithResistances(log = log, resistances = emptyList())

    val expectedActivityGroups =
        listOf(
            ActivityGroup(
                exercise = exercise, magnitude = magnitude, logs = listOf(logWithResistances)))

    `when`(repository.activityGroupsForDay(testDay)).thenReturn(flowOf(expectedActivityGroups))

    // Act
    val result = viewModel.activityGroupsForDay(testDay).first()

    // Assert
    assertEquals("Activity groups should match", expectedActivityGroups, result)
    verify(repository).activityGroupsForDay(testDay)
  }

  @Test
  fun insertActivityAndResistancesShouldCallRepository() = runTest {
    // Arrange
    val newExercise = BeetExerciseLog(id = 0, exerciseId = 1, magnitude = 10, logDay = testDay)

    val selectedResistances =
        listOf(BeetActivityResistance(activityId = 0, resistanceKind = 1, resistanceValue = 70))

    // Act
    viewModel.insertActivityAndResistances(newExercise, selectedResistances)
    testDispatcher.scheduler.advanceUntilIdle()

    // Assert
    verify(repository).insertActivityAndResistances(newExercise, selectedResistances)
  }

  @Test
  fun deleteActivityShouldCallRepository() = runTest {
    // Arrange
    val logsToDelete =
        listOf(
            BeetExerciseLog(id = 1, exerciseId = 1, magnitude = 10, logDay = testDay),
            BeetExerciseLog(id = 2, exerciseId = 1, magnitude = 15, logDay = testDay))

    // Act
    viewModel.deleteActivity(logsToDelete)
    testDispatcher.scheduler.advanceUntilIdle()

    // Assert
    verify(repository).deleteLogEntries(logsToDelete)
  }

  @Test
  fun updateLogEntryShouldCallRepository() = runTest {
    // Arrange
    val updatedLog = BeetExerciseLog(id = 1, exerciseId = 1, magnitude = 12, logDay = testDay)

    // Act
    viewModel.updateLogEntry(updatedLog)
    testDispatcher.scheduler.advanceUntilIdle()

    // Assert
    verify(repository).updateLogEntry(updatedLog)
  }

  @Test
  fun insertExerciseShouldCallRepository() = runTest {
    // Arrange
    val newExercise =
        BeetExercise(
            id = 0,
            exerciseName = "New Exercise",
            exerciseDescription = "Test exercise",
            magnitudeKind = 1)

    // Act
    viewModel.insertExercise(newExercise)
    testDispatcher.scheduler.advanceUntilIdle()

    // Assert
    verify(repository).insertExercise(newExercise)
  }

  @Test
  fun deleteExerciseShouldCallRepository() = runTest {
    // Arrange
    val exerciseToDelete =
        BeetExercise(
            id = 1,
            exerciseName = "Exercise to Delete",
            exerciseDescription = "Test exercise",
            magnitudeKind = 1)

    // Act
    viewModel.deleteExercise(exerciseToDelete)
    testDispatcher.scheduler.advanceUntilIdle()

    // Assert
    verify(repository).deleteExercise(exerciseToDelete)
  }
}
