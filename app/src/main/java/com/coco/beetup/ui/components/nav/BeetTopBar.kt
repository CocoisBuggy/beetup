package com.coco.beetup.ui.components.nav

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeetTopBar(
    scope: CoroutineScope,
    drawerState: DrawerState,
) {

  TopAppBar(
      title = {},
      navigationIcon = {
        IconButton(onClick = { scope.launch { drawerState.open() } }) {
          Icon(Icons.Default.Menu, "Nav Menu")
        }
      },
  )
}
