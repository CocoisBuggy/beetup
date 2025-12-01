package com.coco.beetup

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.coco.beetup.ui.destinations.BeetHome
import com.coco.beetup.ui.theme.BeetupTheme
import com.coco.beetup.ui.viewmodel.BeetViewModel
import com.coco.beetup.ui.viewmodel.BeetViewModelFactory
import kotlinx.serialization.Serializable

@Serializable
object Home

class MainActivity : ComponentActivity() {
    private val viewModel: BeetViewModel by viewModels {
        BeetViewModelFactory((application as BeetupApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BeetupTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = Home) {
                    composable<Home> { BeetHome(navController, viewModel) }
                }
            }
        }
    }
}
