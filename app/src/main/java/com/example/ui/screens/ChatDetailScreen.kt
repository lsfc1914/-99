package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SentimentSatisfiedAlt
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Chat
import com.example.data.model.Message
import com.example.data.model.MessageType
import com.example.data.model.PollData
import com.example.ui.components.AttachmentPickerSheet
import com.example.ui.components.AvatarView
import com.example.ui.components.CreatePollDialog
import com.example.ui.components.MessageBubble
import com.example.ui.theme.OnlineGreen
import com.example.ui.theme.TelegramBlue
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    chat: Chat,
    messages: List<Message>,
    pinnedMessage: Message?,
    onBackClick: () -> Unit,
    onSendMessage: (text: String, type: MessageType, mediaUrl: String, pollData: PollData?, replyTo: Message?) -> Unit,
    onReactionSelect: (messageId: String, emoji: String) -> Unit,
    onPinMessage: (messageId: String) -> Unit,
    onDeleteMessage: (messageId: String) -> Unit,
    onVotePoll: (messageId: String, optionIndex: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var textInput by remember { mutableStateOf("") }
    var replyingToMessage by remember { mutableStateOf<Message?>(null) }
    var showAttachmentSheet by remember { mutableStateOf(false) }
    var showPollDialog by remember { mutableStateOf(false) }
    var isRecordingVoice by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Auto scroll to bottom when messages update
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val smartReplies = listOf("Супер! 👍", "Добре, благодаря!", "Разбрах 👌", "Изпрати линка")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { }
                    ) {
                        AvatarView(chat = chat, size = 42.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = chat.title,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = when {
                                    chat.isChannel -> "${chat.memberCount} абоната"
                                    chat.isBot -> "бот"
                                    chat.isOnline -> "онлайн"
                                    else -> chat.lastSeenText
                                },
                                fontSize = 12.sp,
                                color = if (chat.isOnline) OnlineGreen else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("chat_detail_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(imageVector = Icons.Default.Call, contentDescription = "Call")
                    }
                    IconButton(onClick = { }) {
                        Icon(imageVector = Icons.Default.Videocam, contentDescription = "Video Call")
                    }
                    IconButton(onClick = { }) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Pinned Message Banner Header
            if (pinnedMessage != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PushPin,
                        contentDescription = "Pinned",
                        tint = TelegramBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Забодено съобщение",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TelegramBlue
                        )
                        Text(
                            text = pinnedMessage.content,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                    }
                }
            }

            // Message Feed
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 8.dp)
                        .testTag("message_feed_lazy_column")
                ) {
                    items(
                        items = messages,
                        key = { it.id }
                    ) { message ->
                        MessageBubble(
                            message = message,
                            onReactionSelect = { emoji -> onReactionSelect(message.id, emoji) },
                            onPinMessage = { onPinMessage(message.id) },
                            onReplyToMessage = { replyingToMessage = message },
                            onDeleteMessage = { onDeleteMessage(message.id) },
                            onVotePoll = { optIdx -> onVotePoll(message.id, optIdx) }
                        )
                    }
                }
            }

            // Smart Reply Chips Bar
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                smartReplies.forEach { replyText ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable {
                                onSendMessage(replyText, MessageType.TEXT, "", null, replyingToMessage)
                                replyingToMessage = null
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = replyText,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Reply Banner Box if Replying
            if (replyingToMessage != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(32.dp)
                            .background(TelegramBlue)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Отговор на ${replyingToMessage?.senderName}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TelegramBlue
                        )
                        Text(
                            text = replyingToMessage?.content ?: "",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                    }
                    IconButton(onClick = { replyingToMessage = null }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Cancel reply")
                    }
                }
            }

            // Bottom Input Bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.Default.SentimentSatisfiedAlt,
                        contentDescription = "Emojis",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Text Input Box
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (isRecordingVoice) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = null,
                                tint = Color.Red,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Записване на гласово съобщение...",
                                color = Color.Red,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        BasicTextField(
                            value = textInput,
                            onValueChange = { textInput = it },
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 15.sp
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("message_input_field"),
                            decorationBox = { inner ->
                                if (textInput.isEmpty()) {
                                    Text(
                                        text = "Съобщение...",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                }
                                inner()
                            }
                        )
                    }
                }

                IconButton(
                    onClick = { showAttachmentSheet = true },
                    modifier = Modifier.testTag("attachment_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AttachFile,
                        contentDescription = "Attach",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Send or Record Voice Button
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(TelegramBlue)
                        .clickable {
                            if (textInput.isNotBlank()) {
                                onSendMessage(
                                    textInput,
                                    MessageType.TEXT,
                                    "",
                                    null,
                                    replyingToMessage
                                )
                                textInput = ""
                                replyingToMessage = null
                            } else if (isRecordingVoice) {
                                isRecordingVoice = false
                                onSendMessage(
                                    "🎤 Гласово съобщение (0:18)",
                                    MessageType.VOICE,
                                    "",
                                    null,
                                    replyingToMessage
                                )
                                replyingToMessage = null
                            } else {
                                isRecordingVoice = true
                            }
                        }
                        .testTag("send_message_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (textInput.isNotBlank()) Icons.Default.Send else Icons.Default.Mic,
                        contentDescription = if (textInput.isNotBlank()) "Send" else "Record Voice",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // Attachment Sheet
        if (showAttachmentSheet) {
            AttachmentPickerSheet(
                onDismiss = { showAttachmentSheet = false },
                onSendImage = { imageUrl ->
                    onSendMessage("Снимка от Рила 🏔️", MessageType.IMAGE, imageUrl, null, replyingToMessage)
                    replyingToMessage = null
                },
                onSendFile = {
                    onSendMessage("📄 Документ_презентация.pdf", MessageType.FILE, "", null, replyingToMessage)
                    replyingToMessage = null
                },
                onSendLocation = {
                    onSendMessage("📍 София, бул. Витоша 100", MessageType.LOCATION, "", null, replyingToMessage)
                    replyingToMessage = null
                },
                onCreatePoll = { showPollDialog = true }
            )
        }

        // Poll Creation Dialog
        if (showPollDialog) {
            CreatePollDialog(
                onDismiss = { showPollDialog = false },
                onCreatePoll = { pollData ->
                    onSendMessage("Анкета: ${pollData.question}", MessageType.POLL, "", pollData, replyingToMessage)
                    replyingToMessage = null
                }
            )
        }
    }
}
