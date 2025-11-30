package com.coco.beetup

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.coco.beetup.ui.destinations.BeetHome
import com.coco.beetup.ui.theme.BeetupTheme
import kotlinx.serialization.Serializable

@Serializable
object Home

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BeetupTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = Home) {
                    composable<Home> { BeetHome(navController) }
                }
            }
        }
    }
}
