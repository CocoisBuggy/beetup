package com.coco.beetup.ui.components.exercise

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Note
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.coco.beetup.core.data.ExerciseNote
import com.coco.beetup.ui.viewmodel.BeetViewModel
import java.time.LocalDate
import java.time.LocalDateTime

@Composable
fun ExerciseNoteCard(viewModel: BeetViewModel, day: Int, modifier: Modifier = Modifier) {
  val note by viewModel.getNoteForDay(day).collectAsState(initial = null)
  var showEditDialog by remember { mutableStateOf(false) }

  Card(modifier = modifier.fillMaxWidth()) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
      Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  Icon(
                      imageVector = Icons.Default.Note,
                      contentDescription = "Note",
                      tint = MaterialTheme.colorScheme.primary)
                  Text(
                      text = "Daily Note",
                      style = MaterialTheme.typography.titleMedium,
                      fontWeight = FontWeight.Medium)
                }

            IconButton(onClick = { showEditDialog = true }) {
              Icon(
                  imageVector = Icons.Default.Edit,
                  contentDescription = "Edit Note",
                  tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }

      Spacer(modifier = Modifier.height(8.dp))

      if (note != null) {
        Text(
            text = note!!.noteText,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.fillMaxWidth())
      } else {
        Text(
            text = "No note for today. Tap the edit button to add one.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth())
      }
    }
  }

  if (showEditDialog) {
    ExerciseNoteEditDialog(
        viewModel = viewModel,
        day = day,
        existingNote = note,
        onDismiss = { showEditDialog = false })
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExerciseNoteEditDialog(
    viewModel: BeetViewModel,
    day: Int,
    existingNote: ExerciseNote?,
    onDismiss: () -> Unit
) {
  var noteText by remember { mutableStateOf(existingNote?.noteText ?: "") }
  val scrollState = rememberScrollState()

  AlertDialog(
      onDismissRequest = onDismiss,
      modifier = Modifier.widthIn(max = 400.dp),
      properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(
            modifier = Modifier.fillMaxWidth().verticalScroll(scrollState),
            shape = MaterialTheme.shapes.large) {
              Column(
                  modifier = Modifier.fillMaxWidth().padding(24.dp),
                  verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = if (existingNote != null) "Edit Note" else "Add Note",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Medium)

                    val date = LocalDate.ofEpochDay(day.toLong())
                    Text(
                        text = "Date: ${date.month.name} ${date.dayOfMonth}, ${date.year}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)

                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        label = { Text("Note") },
                        placeholder = { Text("Enter your thoughts for today...") },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        maxLines = 5)

                    AnimatedVisibility(
                        visible = noteText.isNotBlank(),
                        enter = fadeIn() + slideInVertically(),
                        exit = fadeOut() + slideOutVertically()) {
                          Row(
                              modifier = Modifier.fillMaxWidth(),
                              horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)) {
                                TextButton(onClick = onDismiss) { Text("Cancel") }

                                if (existingNote != null && noteText.isBlank()) {
                                  Button(
                                      onClick = {
                                        viewModel.deleteNoteForDay(day)
                                        onDismiss()
                                      }) {
                                        Text("Delete")
                                      }
                                }

                                Button(
                                    onClick = {
                                      if (existingNote != null) {
                                        val updatedNote =
                                            existingNote.copy(
                                                noteText = noteText, noteDate = LocalDateTime.now())
                                        viewModel.updateNote(updatedNote)
                                      } else {
                                        val newNote =
                                            ExerciseNote(
                                                noteText = noteText,
                                                noteDay = day,
                                                noteDate = LocalDateTime.now())
                                        viewModel.insertNote(newNote)
                                      }
                                      onDismiss()
                                    },
                                    enabled = noteText.isNotBlank()) {
                                      Text(if (existingNote != null) "Update" else "Save")
                                    }
                              }
                        }

                    if (noteText.isBlank()) {
                      Row(
                          modifier = Modifier.fillMaxWidth(),
                          horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)) {
                            TextButton(onClick = onDismiss) { Text("Cancel") }
                          }
                    }
                  }
            }
      }
}
