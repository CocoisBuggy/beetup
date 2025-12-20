package com.coco.beetup.ui.destinations

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring.DampingRatioMediumBouncy
import androidx.compose.animation.core.Spring.StiffnessMedium
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialShapes.Companion.Sunny
import androidx.compose.material3.Scaffold
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.coco.beetup.ui.components.exercise.CreateExerciseDialog
import com.coco.beetup.ui.components.exercise.ExerciseListItem
import com.coco.beetup.ui.components.exercise.ExerciseManagerHero
import com.coco.beetup.ui.components.exercise.ResistanceManagementDialog
import com.coco.beetup.ui.components.nav.BeetTopBar
import com.coco.beetup.ui.viewmodel.BeetViewModel
import kotlinx.coroutines.CoroutineScope

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BeetExerciseManager(
    nav: NavHostController,
    viewModel: BeetViewModel,
    drawerState: DrawerState,
    scope: CoroutineScope = rememberCoroutineScope(),
) {
  val resistances by viewModel.allResistances.collectAsState(emptyList())
  var showExerciseDialog by remember { mutableStateOf(false) }

  val exerciseCategories by viewModel.allExercises.collectAsState(initial = null)
  val scrollState = rememberScrollState()

  val isVisible = scrollState.value > 0
  val targetAngle = if (isVisible) 0f else 180f
  val animationTime = 400

  val rotationAngle by
      animateFloatAsState(
          targetValue = targetAngle,
          animationSpec =
              spring(dampingRatio = DampingRatioMediumBouncy, stiffness = StiffnessMedium),
          label = "FAB Rotation Animation")

  val scaleEnterTransition = remember {
    scaleIn(
        animationSpec =
            spring(dampingRatio = DampingRatioMediumBouncy, stiffness = StiffnessMedium),
        initialScale = 0.0f) + fadeIn(animationSpec = tween(durationMillis = animationTime))
  }

  val scaleExitTransition = remember {
    scaleOut(animationSpec = tween(durationMillis = animationTime)) +
        fadeOut(animationSpec = tween(durationMillis = animationTime))
  }

  CreateExerciseDialog(
      viewModel,
      showExerciseDialog,
      onDismiss = { showExerciseDialog = false },
  )

  Scaffold(
      topBar = { BeetTopBar(scope, drawerState) },
      floatingActionButton = {
        AnimatedVisibility(
            visible = isVisible,
            enter = scaleEnterTransition,
            exit = scaleExitTransition,
        ) {
          FloatingActionButton(
              onClick = { showExerciseDialog = true },
              shape = Sunny.toShape(),
              modifier = Modifier.graphicsLayer { rotationZ = rotationAngle }) {
                Icon(Icons.Default.Add, "Add Icon")
              }
        }
      },
  ) { innerPadding ->
    Column(
        Modifier.fillMaxSize().padding(innerPadding).padding(12.dp).verticalScroll(scrollState),
    ) {
      ExerciseManagerHero(onAddClick = { showExerciseDialog = true })
      Spacer(Modifier.size(24.dp))

      if (exerciseCategories == null) {
        LoadingIndicator()
      } else {
        var selectedExercise by remember { mutableStateOf<Int?>(null) }
        var showResistanceDialog by remember { mutableStateOf(false) }
        ResistanceManagementDialog(
            selectedExercise,
            showResistanceDialog,
            viewModel,
            resistances,
            onDismiss = { showResistanceDialog = false })

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          for (exercise in exerciseCategories) {
            val isSelected = selectedExercise == exercise.id

            val animatedPadding by
                animateDpAsState(
                    targetValue = if (isSelected) 16.dp else 0.dp,
                    animationSpec =
                        spring(
                            dampingRatio = DampingRatioMediumBouncy, stiffness = StiffnessMedium),
                    label = "padding")

            ExerciseListItem(
                isSelected,
                exercise,
                animatedPadding.coerceAtLeast(0.dp),
                viewModel,
                onClick = { selectedExercise = it },
                onRemoveResistanceReference = {
                  viewModel.removeResistanceReference(exercise.id, it)
                },
                onAddResistance = { showResistanceDialog = true })
          }
        }
      }
    }
  }
}
