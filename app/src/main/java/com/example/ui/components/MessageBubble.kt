package com.example.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.Message
import com.example.data.model.MessageType
import com.example.ui.theme.LocalTelegramColors
import com.example.ui.theme.TelegramBlue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun MessageBubble(
    message: Message,
    onReactionSelect: (emoji: String) -> Unit,
    onPinMessage: () -> Unit,
    onReplyToMessage: () -> Unit,
    onDeleteMessage: () -> Unit,
    onVotePoll: (optionIndex: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val chatColors = LocalTelegramColors.current
    var showMenu by remember { mutableStateOf(false) }
    var showReactionPicker by remember { mutableStateOf(false) }

    val isOutgoing = message.isOutgoing
    val isSystem = message.messageType == MessageType.SYSTEM

    if (isSystem) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.4f))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = message.content,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        return
    }

    val bubbleShape = if (isOutgoing) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 2.dp)
    } else {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 2.dp, bottomEnd = 16.dp)
    }

    val bubbleBg = if (isOutgoing) chatColors.bubbleOutgoing else chatColors.bubbleIncoming
    val textColor = if (isOutgoing) chatColors.bubbleOutgoingText else chatColors.bubbleIncomingText

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 3.dp),
        contentAlignment = if (isOutgoing) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Column(
            horizontalAlignment = if (isOutgoing) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 320.dp)
        ) {
            // Reaction Picker Popup Bar when long pressed
            if (showReactionPicker) {
                ReactionPicker(
                    onSelectEmoji = { emoji ->
                        showReactionPicker = false
                        onReactionSelect(emoji)
                    },
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }

            Box(
                modifier = Modifier
                    .clip(bubbleShape)
                    .background(bubbleBg)
                    .combinedClickable(
                        onClick = { },
                        onLongClick = {
                            showReactionPicker = !showReactionPicker
                            showMenu = true
                        }
                    )
                    .testTag("message_bubble_${message.id}")
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Column {
                    // Sender Name if in group/incoming
                    if (!isOutgoing && message.senderName.isNotEmpty()) {
                        Text(
                            text = message.senderName,
                            color = TelegramBlue,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }

                    // Reply Preview Header Box
                    if (!message.replyToText.isNullOrEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (isOutgoing) Color.White.copy(alpha = 0.2f)
                                    else TelegramBlue.copy(alpha = 0.12f)
                                )
                                .padding(start = 8.dp, top = 4.dp, bottom = 4.dp, end = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .height(28.dp)
                                    .background(TelegramBlue)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = message.replyToSender ?: "Отговор",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TelegramBlue
                                )
                                Text(
                                    text = message.replyToText,
                                    fontSize = 12.sp,
                                    color = textColor.copy(alpha = 0.8f),
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    // Media Content (Image)
                    if (message.messageType == MessageType.IMAGE || message.mediaUrl.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .padding(bottom = 6.dp)
                                .clip(RoundedCornerShape(8.dp))
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(message.mediaUrl.ifEmpty { "https://images.unsplash.com/photo-1506744038136-46273834b3fb" })
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Media",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // Voice Message
                    if (message.messageType == MessageType.VOICE) {
                        VoiceMessagePlayer(
                            durationSeconds = 18,
                            isOutgoing = isOutgoing
                        )
                    }

                    // Poll Content
                    if (message.messageType == MessageType.POLL && message.pollData != null) {
                        PollCard(
                            poll = message.pollData,
                            onVote = onVotePoll,
                            isOutgoing = isOutgoing
                        )
                    }

                    // Text Content
                    if (message.content.isNotEmpty() && message.messageType != MessageType.POLL) {
                        Text(
                            text = message.content,
                            fontSize = 15.sp,
                            color = textColor,
                            lineHeight = 20.sp
                        )
                    }

                    // Timestamp & Read Indicators Row at bottom
                    Row(
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 4.dp)
                    ) {
                        if (message.isPinned) {
                            Icon(
                                imageVector = Icons.Default.PushPin,
                                contentDescription = "Pinned",
                                tint = textColor.copy(alpha = 0.6f),
                                modifier = Modifier
                                    .size(12.dp)
                                    .padding(end = 2.dp)
                            )
                        }

                        Text(
                            text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp)),
                            fontSize = 11.sp,
                            color = textColor.copy(alpha = 0.65f)
                        )

                        if (isOutgoing) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = "Read",
                                tint = if (message.isRead) TelegramBlue else textColor.copy(alpha = 0.65f),
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }

            // Reaction Chips Bar below message bubble
            if (message.reactions.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    message.reactions.forEach { (emoji, count) ->
                        val isUserReacted = message.userReaction == emoji
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isUserReacted) TelegramBlue.copy(alpha = 0.25f)
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable { onReactionSelect(emoji) }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = emoji, fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = count.toString(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // Dropdown Menu for message actions
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text("Отговори (Reply)") },
                onClick = {
                    showMenu = false
                    onReplyToMessage()
                }
            )
            DropdownMenuItem(
                text = { Text(if (message.isPinned) "Отбодеж" else "Забодеж (Pin)") },
                onClick = {
                    showMenu = false
                    onPinMessage()
                }
            )
            DropdownMenuItem(
                text = { Text("Изтрий съобщението", color = MaterialTheme.colorScheme.error) },
                onClick = {
                    showMenu = false
                    onDeleteMessage()
                }
            )
        }
    }
}
