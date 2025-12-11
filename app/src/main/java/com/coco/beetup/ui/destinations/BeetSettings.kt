package com.coco.beetup.ui.destinations

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.coco.beetup.ui.components.nav.BeetTopBar
import com.coco.beetup.ui.viewmodel.BeetViewModel
import kotlinx.coroutines.CoroutineScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeetSettings(
    nav: NavHostController,
    viewModel: BeetViewModel,
    drawerState: DrawerState,
    scope: CoroutineScope = rememberCoroutineScope(),
) {
  Scaffold(
      topBar = { BeetTopBar(scope, drawerState) },
  ) { innerPadding ->
    Column(Modifier.padding(innerPadding)) {}
  }
}
