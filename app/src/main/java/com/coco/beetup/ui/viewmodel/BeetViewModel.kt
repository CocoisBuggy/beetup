package com.coco.beetup.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.coco.beetup.core.data.BeetActivityResistance
import com.coco.beetup.core.data.BeetExercise
import com.coco.beetup.core.data.BeetExerciseLog
import com.coco.beetup.core.data.BeetRepository
import com.coco.beetup.core.data.BeetResistance
import kotlinx.coroutines.launch

class BeetViewModel(
    private val repository: BeetRepository,
) : ViewModel() {
    val profile = repository.getProfile()
    val allExercises = repository.getAllExercises()
    val allLogs = repository.getAllLogs()
    val todaysLogs = repository.logsForDay()
    val allResistances = repository.allResistances()

    fun validResistancesFor(exercise: Int) = repository.validResistancesFor(exercise)

    fun exerciseLogs(
        day: Int,
        exercise: Int,
    ) = repository.exerciseLogs(day, exercise)

    fun magnitudeFor(id: Int) = repository.getMagnitude(id)

    fun insertExercise(exercise: BeetExercise) =
        viewModelScope.launch { repository.insertExercise(exercise) }

    fun insertActivity(activity: BeetExerciseLog) =
        viewModelScope.launch { repository.insertLog(activity) }

    fun deleteExercise(exercise: BeetExercise) =
        viewModelScope.launch { repository.deleteExercise(exercise) }

    fun deleteExercises(exercises: Collection<BeetExercise>) =
        viewModelScope.launch { repository.deleteExercises(exercises) }

    fun deleteActivity(exercises: Collection<BeetExerciseLog>) =
        viewModelScope.launch { repository.deleteLogEntries(exercises) }

    fun activityGroupsForDay(day: Int) = repository.activityGroupsForDay(day)

    fun insertActivityAndResistances(
        newExercise: BeetExerciseLog,
        selectedResistances: List<BeetActivityResistance>
    ) {
        viewModelScope.launch {

            repository.insertResistances(selectedResistances.map {
                it.copy(
                    activityId = repository.insertLog(
                        newExercise
                    )
                )
            })
        }
    }
}

class BeetViewModelFactory(
    private val repository: BeetRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BeetViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST") return BeetViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
