package com.coco.beetup.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallSplit
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Divider
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel

@Composable
fun FloatingActivityToolbar() {
    ExtendedFloatingActionButton(
        onClick = { /* No action needed on the container itself */ }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { /* TODO: Handle Edit */ }) {
                Icon(Icons.Default.Edit, "Edit")
            }
            IconButton(onClick = { /* TODO: Handle Edit */ }) {
                Icon(Icons.AutoMirrored.Filled.CallSplit, "Bifurcate")
            }
            Spacer(Modifier.width(16.dp))
            VerticalDivider(
                modifier = Modifier
                    .height(32.dp)
                    .width(1.dp),
                color = MaterialTheme.colorScheme.onTertiary
            )
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = { /* TODO: Handle Delete */ }) {
                Icon(Icons.Default.Delete, "Delete")
            }
        }
    }
}