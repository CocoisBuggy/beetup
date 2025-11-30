package com.coco.beetup.ui.destinations

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.coco.beetup.R
import com.coco.beetup.ui.components.DeletableExerciseEntry
import com.coco.beetup.ui.components.SelectionAppBar
import com.coco.beetup.ui.components.WelcomeCard
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeetHome(nav: NavHostController) {
    var selectedItems by remember { mutableStateOf<Set<String>>(emptySet()) }
    var multiSelectionEnabled by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            if (multiSelectionEnabled) {
                SelectionAppBar(
                    selectedItemCount = selectedItems.size,
                    onClearSelection = { selectedItems = emptySet() },
                    onDeleteSelected = {
                        // This will be passed down to the list to handle deletion
                    }
                )
            } else {
                TopAppBar(
                    title = { Text(stringResource(id = R.string.todays_activity)) }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { /* TODO: Handle new entry creation */ }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(id = R.string.add_exercise_entry)
                )
            }
        }
    ) { innerPadding ->
        var itemList by remember { mutableStateOf((1..20).map { "Item #$it" }) }

        val onDeleteSelected = {
            itemList = itemList.filterNot { it in selectedItems }
            selectedItems = emptySet() // Clear selection after deletion
        }

        if (multiSelectionEnabled) {
            SelectionAppBar(
                selectedItemCount = selectedItems.size,
                onClearSelection = { selectedItems = emptySet() },
                onDeleteSelected = onDeleteSelected
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            item {
                WelcomeCard(nav)
            }

            items(itemList, key = { it }) { item ->
                val isSelected = item in selectedItems

                DeletableExerciseEntry(
                    itemText = item,
                    isSelected = isSelected,
                    onDelete = {
                        itemList = itemList.filterNot { it == item }
                    },
                    // 3. Handle toggling selection
                    onToggleSelection = {
                        if (multiSelectionEnabled) {
                            selectedItems = if (isSelected) {
                                selectedItems - item
                            } else {
                                selectedItems + item
                            }
                        } else {
                            // When multiselection is prohibited, we expect that every toggle
                            // should UNSET all set, and then set the clicked item
                            selectedItems = setOf(item)
                        }
                    },
                    onToggleMultiSelection = { multiSelectionEnabled = !multiSelectionEnabled },
                    multiSelectionEnabled = multiSelectionEnabled
                )
            }
        }
    }
}





