package com.coco.beetup.core.data

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

class BeetRepositorySimpleTest {

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
  fun activityGroupsForDayShouldReturnEmptyList() = runTest {
    // Arrange
    `when`(exerciseLogDao.getAllFlatActivityDataForDay(testDay)).thenReturn(flowOf(emptyList()))
    `when`(beetExerciseDao.getAllResistances()).thenReturn(flowOf(emptyList()))
    `when`(beetExerciseDao.getAllMagnitudes()).thenReturn(flowOf(emptyList()))

    // Act
    val result = repository.activityGroupsForDay(testDay).first()

    // Assert
    assertEquals(0, result.size)
  }
}
