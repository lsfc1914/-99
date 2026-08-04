package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PollData
import com.example.data.model.PollOption
import com.example.ui.theme.TelegramBlue
import java.util.UUID

@Composable
fun CreatePollDialog(
    onDismiss: () -> Unit,
    onCreatePoll: (PollData) -> Unit
) {
    var question by remember { mutableStateOf("") }
    val options = remember { mutableStateListOf("", "") }
    var isAnonymous by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Нова анкета 📊",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = question,
                    onValueChange = { question = it },
                    label = { Text("Задайте въпрос") },
                    placeholder = { Text("напр. Къде ще обядваме днес?") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("poll_question_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Опции за гласуване",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                options.forEachIndexed { index, optionText ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        OutlinedTextField(
                            value = optionText,
                            onValueChange = { options[index] = it },
                            placeholder = { Text("Опция ${index + 1}") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("poll_option_input_$index")
                        )

                        if (options.size > 2) {
                            IconButton(
                                onClick = { options.removeAt(index) }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove option"
                                )
                            }
                        }
                    }
                }

                if (options.size < 8) {
                    TextButton(
                        onClick = { options.add("") },
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .testTag("add_poll_option_button")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Добави опция")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Анонимно гласуване",
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = isAnonymous,
                        onCheckedChange = { isAnonymous = it }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val validOptions = options.filter { it.isNotBlank() }
                    if (question.isNotBlank() && validOptions.size >= 2) {
                        val pollData = PollData(
                            id = "poll_${UUID.randomUUID()}",
                            question = question,
                            options = validOptions.mapIndexed { idx, txt -> PollOption(idx, txt, 0) },
                            totalVotes = 0,
                            isAnonymous = isAnonymous
                        )
                        onCreatePoll(pollData)
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = TelegramBlue),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.testTag("submit_poll_button")
            ) {
                Text("Създай")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отказ")
            }
        }
    )
}
