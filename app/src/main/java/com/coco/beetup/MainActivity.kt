package com.coco.beetup

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.coco.beetup.ui.components.AppDrawer
import com.coco.beetup.ui.components.BeetRaw
import com.coco.beetup.ui.components.ExerciseManager
import com.coco.beetup.ui.components.History
import com.coco.beetup.ui.components.Home
import com.coco.beetup.ui.components.Settings
import com.coco.beetup.ui.destinations.BeetExerciseManager
import com.coco.beetup.ui.destinations.BeetHistory
import com.coco.beetup.ui.destinations.BeetHome
import com.coco.beetup.ui.destinations.BeetRawView
import com.coco.beetup.ui.destinations.BeetSettings
import com.coco.beetup.ui.theme.AppTheme
import com.coco.beetup.ui.viewmodel.BeetViewModel
import com.coco.beetup.ui.viewmodel.BeetViewModelFactory

class MainActivity : ComponentActivity() {
  private val viewModel: BeetViewModel by viewModels {
    BeetViewModelFactory((application as BeetupApplication).repository)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      AppTheme(dynamicColor = false) {
        val navController = rememberNavController()
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val scope = rememberCoroutineScope()

        AppDrawer(drawerState = drawerState, scope = scope, navController = navController) {
          NavHost(navController = navController, startDestination = Home) {
            composable<Home> { BeetHome(navController, viewModel, drawerState) }
            composable<ExerciseManager> {
              BeetExerciseManager(navController, viewModel, drawerState)
            }
            composable<BeetRaw> { BeetRawView(navController, viewModel, drawerState) }
            composable<History> { BeetHistory() }
            composable<Settings> { BeetSettings(navController, viewModel, drawerState) }
          }
        }
      }
    }
  }
}
