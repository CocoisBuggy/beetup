package com.coco.beetup.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallSplit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.coco.beetup.core.data.ActivityGroup

@Composable
fun FloatingActivityToolbar(
    item: ActivityGroup?,
    editMode: Boolean,
    onEditModeToggle: () -> Unit,
    onDeleteSelected: () -> Unit,
    onBifurcate: () -> Unit,
    onMinus: () -> Unit,
    onBannerToggle: () -> Unit,
) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    if (editMode) {
      FilledTonalIconButton(onClick = onEditModeToggle) { Icon(Icons.Default.Edit, "Edit") }
    } else {
      IconButton(onClick = onEditModeToggle) { Icon(Icons.Default.Edit, "Edit") }
    }
    IconButton(onClick = onBifurcate) { Icon(Icons.AutoMirrored.Filled.CallSplit, "Bifurcate") }
    IconButton(onClick = onMinus, enabled = (item?.logs?.size ?: 0) > 1) {
      Icon(Icons.Default.RemoveCircleOutline, "Remove")
    }
    IconToggleButton(checked = item?.banner == true, onCheckedChange = { onBannerToggle() }) {
      Icon(
          if (item?.banner == true) Icons.Default.Flag else Icons.Default.Flag,
          "Toggle Banner",
      )
    }

    Spacer(Modifier.width(16.dp))
    VerticalDivider(
        modifier = Modifier.height(32.dp).width(1.dp),
        color = MaterialTheme.colorScheme.onTertiary,
    )
    Spacer(Modifier.width(8.dp))
    IconButton(onClick = onDeleteSelected) { Icon(Icons.Default.Delete, "Delete") }
  }
}
