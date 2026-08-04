package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.TelegramBlue
import kotlinx.coroutines.delay

@Composable
fun VoiceMessagePlayer(
    durationSeconds: Int = 14,
    isOutgoing: Boolean = false,
    modifier: Modifier = Modifier
) {
    var isPlaying by remember { mutableStateOf(false) }
    var currentProgress by remember { mutableStateOf(0f) }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (currentProgress < 1f) {
                delay(100)
                currentProgress += 0.03f
            }
            isPlaying = false
            currentProgress = 0f
        }
    }

    val waveHeights = listOf(0.4f, 0.8f, 0.5f, 0.9f, 0.3f, 0.7f, 1.0f, 0.6f, 0.4f, 0.8f, 0.5f, 0.9f, 0.2f, 0.7f, 0.5f)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        // Play / Pause Circle
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(if (isOutgoing) Color.White.copy(alpha = 0.2f) else TelegramBlue)
                .clickable { isPlaying = !isPlaying },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            // Waveform bars
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
            ) {
                waveHeights.forEachIndexed { index, heightFactor ->
                    val isPlayed = (index.toFloat() / waveHeights.size) <= currentProgress
                    val barHeight = (20 * heightFactor).dp
                    val color = if (isPlayed) {
                        if (isOutgoing) Color.White else TelegramBlue
                    } else {
                        if (isOutgoing) Color.White.copy(alpha = 0.4f) else Color.Gray.copy(alpha = 0.4f)
                    }

                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(barHeight)
                            .clip(RoundedCornerShape(1.5.dp))
                            .background(color)
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            val playedSecs = (durationSeconds * currentProgress).toInt()
            Text(
                text = if (isPlaying) String.format("%02d:%02d", playedSecs / 60, playedSecs % 60)
                       else String.format("%02d:%02d", durationSeconds / 60, durationSeconds % 60),
                fontSize = 11.sp,
                color = if (isOutgoing) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
