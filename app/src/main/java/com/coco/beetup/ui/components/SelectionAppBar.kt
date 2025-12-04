package com.coco.beetup.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionAppBar(
    selectedItemCount: Int,
    onClearSelection: () -> Unit,
    onDeleteSelected: () -> Unit,
) {
  TopAppBar(
      title = { Text("$selectedItemCount selected") },
      navigationIcon = {
        IconButton(onClick = onClearSelection) {
          Icon(Icons.Default.Close, contentDescription = "Clear selection")
        }
      },
      actions = {
        IconButton(onClick = onDeleteSelected) {
          Icon(Icons.Default.Delete, contentDescription = "Delete selected items")
        }
      },
      colors =
          TopAppBarDefaults.topAppBarColors(
              containerColor = MaterialTheme.colorScheme.primaryContainer,
              titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
              actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
              navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
          ),
  )
}
