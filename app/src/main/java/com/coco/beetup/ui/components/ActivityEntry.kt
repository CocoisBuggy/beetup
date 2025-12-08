package com.coco.beetup.ui.components

import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.coco.beetup.core.data.ActivityGroup
import com.coco.beetup.core.data.ResistanceConversion
import com.coco.beetup.ui.components.grain.ResistanceIcon
import com.coco.beetup.ui.viewmodel.BeetViewModel
import java.util.Date
import java.util.UUID
import kotlinx.coroutines.delay

private data class PlusOneAnimation(val id: String = UUID.randomUUID().toString())

@Composable
private fun PlusOne(onFinished: () -> Unit) {
  var visible by remember { mutableStateOf(true) }

  LaunchedEffect(Unit) {
    visible = false
    delay(500)
    onFinished()
  }

  val animatedOffsetY by
      animateDpAsState(
          targetValue = if (visible) 0.dp else (-24).dp,
          animationSpec =
              tween(
                  durationMillis = 500,
                  easing = FastOutLinearInEasing,
              ),
          label = "offsetY")
  val animatedAlpha by
      animateFloatAsState(
          targetValue = if (visible) 1f else 0f,
          animationSpec =
              tween(
                  durationMillis = 500,
                  easing = FastOutLinearInEasing,
              ),
          label = "alpha")

  Text(
      text = "+1",
      color = MaterialTheme.colorScheme.primary,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.offset(y = animatedOffsetY).alpha(animatedAlpha))
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ActivityEntry(
    viewModel: BeetViewModel,
    activity: ActivityGroup,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
  var plusOneAnimations by remember { mutableStateOf<List<PlusOneAnimation>>(emptyList()) }

  if (activity.logs.isEmpty()) {
    ListItem(
        leadingContent = { LoadingIndicator() },
        headlineContent = { Text("${activity.exercise.exerciseName}") },
    )
  } else {

    ListItem(
        headlineContent = { Text("${activity.exercise.exerciseName}") },
        supportingContent = {
          Column {
            val text = mutableListOf<String>()

            activity.logs.firstOrNull()?.let {
              text.add("${it.log.magnitude} ${activity.magnitude.unit}")
            }

            text.add("${activity.logs.size} sets")
            Row {
              Text(text.joinToString(", "))
              Spacer(Modifier.width(4.dp))

              if (plusOneAnimations.isNotEmpty()) {
                Box(modifier = Modifier.width(32.dp)) {
                  plusOneAnimations.forEach { animation ->
                    key(animation.id) {
                      PlusOne(
                          onFinished = {
                            plusOneAnimations =
                                plusOneAnimations.filterNot { it.id == animation.id }
                          })
                    }
                  }
                }
              }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
              activity.logs.first().resistances.forEach { res ->
                AssistChip(
                    {},
                    leadingIcon = {
                      ResistanceIcon[res.extra.id]?.let { icon ->
                        Icon(
                            icon,
                            contentDescription = "Icon for ${res.extra.name}",
                        )
                      }
                    },
                    label = {
                      Text(
                          ResistanceConversion[res.extra.id]?.invoke(res)
                              ?: "${res.entry.resistanceValue}${res.extra.unit}")
                    },
                    enabled = false)
              }

              if (activity.logs.first().resistances.isEmpty()) {
                AssistChip({}, label = { Text("No Resistance") }, enabled = false)
              }
            }
          }
        },
        leadingContent = {
          Icon(Icons.Default.FitnessCenter, contentDescription = "Exercise Icon")
        },
        trailingContent = {
          FilledTonalButton(
              onClick = {
                plusOneAnimations = plusOneAnimations + PlusOneAnimation()
                viewModel.insertActivityAndResistances(
                    activity.logs.last().log.copy(id = 0, logDate = Date()),
                    activity.logs.last().resistances.map { it.entry })
              }) {
                Text("+1")
              }
        },
        colors =
            ListItemDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        modifier =
            modifier
                .padding(vertical = 4.dp)
                .clip(MaterialTheme.shapes.medium)
                .border(
                    width = 1.dp,
                    color =
                        if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    shape = MaterialTheme.shapes.medium))
  }
}
