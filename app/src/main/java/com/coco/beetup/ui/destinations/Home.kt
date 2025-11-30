package com.coco.beetup.ui.destinations

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeetHome() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Today's Activity") }
            )
        }
    ) { innerPadding ->
        // Create a dummy list of items to display
        val itemList = (1..20).map { "Item #$it" }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            items(itemList) { item ->
                ExerciseEntry(text = item)
            }
        }
    }
}

@Composable
fun ExerciseEntry(text: String) {
    Card(
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        )
    }
}