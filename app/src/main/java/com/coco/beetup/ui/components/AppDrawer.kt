package com.coco.beetup.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsTennis
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable sealed interface Screen

@Serializable object Home : Screen

@Serializable object History : Screen

@Serializable object Stats : Screen

@Serializable object Settings : Screen

@Serializable object ExerciseManager : Screen

@Serializable object BeetRaw : Screen

@Composable
fun AppDrawer(
    drawerState: DrawerState,
    scope: CoroutineScope,
    navController: NavHostController,
    content: @Composable () -> Unit
) {
  var currentRoute by remember { mutableStateOf<Screen>(Home) }

  val navItems: List<Triple<Screen, String, ImageVector>> =
      listOf(
          Triple(Home, "Home", Icons.Default.Home),
          Triple(ExerciseManager, "Exercise Manager", Icons.Default.SportsTennis),
          //          Triple(Stats, "Stats", Icons.Default.QueryStats),
          Triple(Settings, "Settings", Icons.Default.Settings),
          Triple(BeetRaw, "Raw", Icons.Default.DataObject),
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
                  scope.launch { drawerState.close() }
                  navController.navigate(route)
                  currentRoute = route
                })
          }
        }
      },
      content = content)
}
