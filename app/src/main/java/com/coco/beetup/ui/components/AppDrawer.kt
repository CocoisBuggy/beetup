package com.coco.beetup.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerState
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun AppDrawer(
    drawerState: DrawerState,
    scope: CoroutineScope,
    navController: NavHostController,
    content: @Composable () -> Unit
) {
  var currentRoute by remember { mutableStateOf("home") }

  val navItems =
      listOf(
          Triple("home", "Home", Icons.Default.Home),
          Triple("history", "History", Icons.Default.History),
          Triple("stats", "Stats", Icons.Default.QueryStats),
          Triple("settings", "Settings", Icons.Default.Settings),
      )

  ModalNavigationDrawer(
      drawerState = drawerState,
      drawerContent = {
        ModalDrawerSheet(modifier = Modifier.fillMaxWidth(0.75f)) {
          Text("Beetup", modifier = Modifier.padding(16.dp))
          navItems.forEach { (route, label, icon) ->
            NavigationDrawerItem(
                icon = { Icon(icon, label) },
                label = { Text(text = label) },
                selected = route == currentRoute,
                onClick = {
                  if (route != "Home") return@NavigationDrawerItem
                  scope.launch { drawerState.close() }
                  navController.navigate(route)
                  currentRoute = route
                })
          }
        }
      },
      content = content)
}
