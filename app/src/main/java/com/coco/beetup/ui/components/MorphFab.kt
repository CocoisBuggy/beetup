package com.coco.beetup.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.coco.beetup.R
import com.coco.beetup.core.data.ActivityGroup

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MorphFab(
    selectedItems: Set<ActivityGroup>,
    editMode: Boolean,
    multiSelectionEnabled: Boolean,
    onAddActivityClick: () -> Unit,
    onEditModeToggle: () -> Unit,
    onDeleteSelected: () -> Unit,
    onBifurcate: () -> Unit,
    onMinus: () -> Unit,
) {
  FloatingActionButton({
    if (!multiSelectionEnabled && selectedItems.isEmpty()) onAddActivityClick()
  }) {
    AnimatedContent(
        targetState = !multiSelectionEnabled && selectedItems.isNotEmpty(),
        label = "Morphing FAB",
        transitionSpec = {
          (slideInHorizontally { height -> height } + fadeIn())
              .togetherWith(slideOutHorizontally { height -> -height } + fadeOut())
              .using(SizeTransform(clip = true))
        }) { isExpanded ->
          if (isExpanded) {
            FloatingActivityToolbar(
                editMode,
                onEditModeToggle,
                onDeleteSelected,
                onBifurcate,
                onMinus,
            )
          } else {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(id = R.string.add_exercise_entry),
            )
          }
        }
  }
}
