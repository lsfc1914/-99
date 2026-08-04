package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.ChatCategory
import com.example.ui.theme.TelegramBlue

@Composable
fun CreateChatDialog(
    initialCategory: ChatCategory = ChatCategory.DIRECT,
    onDismiss: () -> Unit,
    onCreateChat: (title: String, category: ChatCategory, handle: String, bio: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var handle by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(initialCategory) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = when (selectedCategory) {
                    ChatCategory.CHANNELS -> "Нов канал 📢"
                    ChatCategory.GROUPS -> "Нова група 👥"
                    ChatCategory.BOTS -> "Нов AI Бот 🤖"
                    else -> "Нов разговор 💬"
                },
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Category Filter Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    FilterChip(
                        selected = selectedCategory == ChatCategory.DIRECT,
                        onClick = { selectedCategory = ChatCategory.DIRECT },
                        label = { Text("Личен") },
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    FilterChip(
                        selected = selectedCategory == ChatCategory.GROUPS,
                        onClick = { selectedCategory = ChatCategory.GROUPS },
                        label = { Text("Група") },
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    FilterChip(
                        selected = selectedCategory == ChatCategory.CHANNELS,
                        onClick = { selectedCategory = ChatCategory.CHANNELS },
                        label = { Text("Канал") }
                    )
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(if (selectedCategory == ChatCategory.CHANNELS) "Име на канала" else "Име / Заглавие") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_chat_title_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = handle,
                    onValueChange = { handle = it },
                    label = { Text("Потребителско име (напр. @my_channel)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_chat_handle_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Описание (незадължително)") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onCreateChat(title, selectedCategory, handle, bio)
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = TelegramBlue),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.testTag("submit_create_chat")
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
