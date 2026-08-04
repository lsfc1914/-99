package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.Chat
import com.example.ui.theme.OnlineGreen

private val AvatarColors = listOf(
    Pair(Color(0xFFFC5C7D), Color(0xFF6A82FB)), // Pinkish Blue
    Pair(Color(0xFF00B4DB), Color(0xFF0083B0)), // Cyan Blue
    Pair(Color(0xFF11998E), Color(0xFF38EF7D)), // Emerald
    Pair(Color(0xFFFF9966), Color(0xFFFF5E62)), // Orange Sunset
    Pair(Color(0xFF8E2DE2), Color(0xFF4A00E0)), // Violet Purple
    Pair(Color(0xFFF2994A), Color(0xFFF2C94C))  // Gold
)

@Composable
fun AvatarView(
    chat: Chat,
    size: Dp = 48.dp,
    modifier: Modifier = Modifier
) {
    AvatarView(
        title = chat.title,
        avatarUrl = chat.avatarUrl,
        isOnline = chat.isOnline,
        isChannel = chat.isChannel,
        isGroup = chat.memberCount > 1 && !chat.isChannel,
        isBot = chat.isBot,
        isSaved = chat.id == "chat_saved",
        size = size,
        modifier = modifier
    )
}

@Composable
fun AvatarView(
    title: String,
    avatarUrl: String = "",
    isOnline: Boolean = false,
    isChannel: Boolean = false,
    isGroup: Boolean = false,
    isBot: Boolean = false,
    isSaved: Boolean = false,
    size: Dp = 48.dp,
    modifier: Modifier = Modifier
) {
    val colorIndex = kotlin.math.abs(title.hashCode()) % AvatarColors.size
    val gradientColors = AvatarColors[colorIndex]

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        if (avatarUrl.isNotBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(avatarUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = title,
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(gradientColors.first, gradientColors.second)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isSaved -> {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = "Saved Messages",
                            tint = Color.White,
                            modifier = Modifier.size(size * 0.5f)
                        )
                    }
                    isBot -> {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = "Bot",
                            tint = Color.White,
                            modifier = Modifier.size(size * 0.5f)
                        )
                    }
                    isChannel -> {
                        Icon(
                            imageVector = Icons.Default.Campaign,
                            contentDescription = "Channel",
                            tint = Color.White,
                            modifier = Modifier.size(size * 0.5f)
                        )
                    }
                    isGroup -> {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = "Group",
                            tint = Color.White,
                            modifier = Modifier.size(size * 0.5f)
                        )
                    }
                    else -> {
                        val initials = getInitials(title)
                        Text(
                            text = initials,
                            color = Color.White,
                            fontSize = (size.value * 0.4f).sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Online Status Dot Indicator
        if (isOnline && !isChannel && !isBot) {
            val dotSize = size * 0.28f
            Box(
                modifier = Modifier
                    .size(dotSize)
                    .align(Alignment.BottomEnd)
                    .offset(x = 1.dp, y = 1.dp)
                    .clip(CircleShape)
                    .background(OnlineGreen)
                    .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape)
            )
        }
    }
}

private fun getInitials(name: String): String {
    if (name.isBlank()) return "?"
    val parts = name.trim().split(" ").filter { it.isNotBlank() }
    return if (parts.size >= 2) {
        "${parts[0].first().uppercaseChar()}${parts[1].first().uppercaseChar()}"
    } else {
        name.take(2).uppercase()
    }
}
