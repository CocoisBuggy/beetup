package com.coco.beetup.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun WelcomeCard(nav: NavHostController) {
  Card(
      modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
      colors =
          CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.tertiaryContainer,
          ),
      elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Text(
          text = "Welcome back, Beet!",
          style = MaterialTheme.typography.titleLarge,
          color = MaterialTheme.colorScheme.onTertiaryContainer,
      )

      Text(
          text = "Here are the exercises you've logged today.",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onTertiaryContainer,
          modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
      )

      // Use a Row to arrange the buttons with spacing
      Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.fillMaxWidth(),
      ) {
        FilledTonalButton(
            onClick = { /*TODO*/ },
            modifier = Modifier.weight(1f),
        ) {
          Icon(
              Icons.Default.History,
              contentDescription = "Recent",
              modifier = Modifier.size(ButtonDefaults.IconSize),
          )
          Spacer(Modifier.size(ButtonDefaults.IconSpacing))
          Text("Recent")
        }

        Button(
            onClick = { /*TODO*/ },
            modifier = Modifier.weight(1f),
        ) {
          Icon(
              Icons.Default.Add,
              contentDescription = "Add Exercise",
              modifier = Modifier.size(ButtonDefaults.IconSize),
          )
          Spacer(Modifier.size(ButtonDefaults.IconSpacing))
          Text("Add Exercise")
        }
      }
    }
  }
}
