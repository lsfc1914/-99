package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PollData
import com.example.ui.theme.TelegramBlue

@Composable
fun PollCard(
    poll: PollData,
    onVote: (optionIndex: Int) -> Unit,
    isOutgoing: Boolean = false,
    modifier: Modifier = Modifier
) {
    val hasVoted = poll.userVotedOptionIndex != null

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp)
    ) {
        // Poll Title / Question
        Text(
            text = poll.question,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            ),
            color = if (isOutgoing) Color.White else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        // Poll Options
        poll.options.forEachIndexed { index, option ->
            val isUserChoice = poll.userVotedOptionIndex == index
            val percent = if (poll.totalVotes > 0) (option.voteCount.toFloat() / poll.totalVotes) else 0f
            val animatedPercent by animateFloatAsState(targetValue = if (hasVoted) percent else 0f)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(enabled = !hasVoted) { onVote(index) }
                    .testTag("poll_option_$index")
                    .background(
                        if (hasVoted) {
                            if (isOutgoing) Color.White.copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.surfaceVariant
                        } else Color.Transparent
                    )
            ) {
                // Background percentage fill
                if (hasVoted) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedPercent)
                            .height(42.dp)
                            .background(
                                if (isUserChoice) TelegramBlue.copy(alpha = 0.35f)
                                else Color.Gray.copy(alpha = 0.2f)
                            )
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 10.dp)
                ) {
                    if (!hasVoted) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .border(
                                    2.dp,
                                    if (isOutgoing) Color.White.copy(alpha = 0.7f) else TelegramBlue,
                                    CircleShape
                                )
                        )
                    } else if (isUserChoice) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(TelegramBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Voted",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = option.text,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                        color = if (isOutgoing) Color.White else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )

                    if (hasVoted) {
                        Text(
                            text = "${(percent * 100).toInt()}%",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isOutgoing) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Total Votes Summary
        Text(
            text = if (poll.isAnonymous) "${poll.totalVotes} гласа • Анонимна анкета"
                   else "${poll.totalVotes} гласа",
            fontSize = 12.sp,
            color = if (isOutgoing) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
