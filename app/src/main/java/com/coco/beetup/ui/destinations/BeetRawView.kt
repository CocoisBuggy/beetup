package com.coco.beetup.ui.destinations

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItem
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.coco.beetup.ui.components.nav.BeetTopBar
import com.coco.beetup.ui.viewmodel.BeetViewModel
import kotlinx.coroutines.CoroutineScope

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BeetRawView(
    nav: NavHostController,
    viewModel: BeetViewModel,
    drawerState: DrawerState,
    scope: CoroutineScope = rememberCoroutineScope(),
) {
  val allLogs by viewModel.allLogs.collectAsState(initial = null)

  Scaffold(
      topBar = { BeetTopBar(scope, drawerState) },
      content = { innerPadding ->
        if (allLogs == null) {
          LoadingIndicator()
        }
        allLogs?.let { logs ->
          LazyColumn(Modifier.padding(innerPadding)) {
            items(
                count = logs.size,
                key = { it },
                itemContent = { ListItem(headlineContent = { Text("${logs[it]}") }) },
            )
          }
        }
      },
  )
}
