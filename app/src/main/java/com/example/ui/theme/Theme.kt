package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class TelegramChatColors(
    val bubbleOutgoing: Color,
    val bubbleIncoming: Color,
    val bubbleOutgoingText: Color,
    val bubbleIncomingText: Color,
    val chatBackground: Color,
    val unreadBadge: Color
)

val LocalTelegramColors = staticCompositionLocalOf {
    TelegramChatColors(
        bubbleOutgoing = TelegramLightBubbleOut,
        bubbleIncoming = TelegramLightBubbleIn,
        bubbleOutgoingText = Color.Black,
        bubbleIncomingText = Color.Black,
        chatBackground = TelegramLightBg,
        unreadBadge = UnreadBadgeColor
    )
}

private val LightColorScheme = lightColorScheme(
    primary = TelegramBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6EEFF),
    onPrimaryContainer = Color(0xFF001E30),
    secondary = UnreadBadgeColor,
    onSecondary = Color.White,
    background = TelegramLightBg,
    onBackground = Color(0xFF1C1B1F),
    surface = TelegramLightSurface,
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFF0F2F5),
    onSurfaceVariant = Color(0xFF44474E)
)

private val DarkColorScheme = darkColorScheme(
    primary = TelegramDarkPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF2B5278),
    onPrimaryContainer = Color.White,
    secondary = UnreadBadgeColor,
    onSecondary = Color.White,
    background = TelegramDarkBg,
    onBackground = Color.White,
    surface = TelegramDarkSurface,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF232E3C),
    onSurfaceVariant = Color(0xFFC4C7C5)
)

private val EmeraldColorScheme = darkColorScheme(
    primary = EmeraldPrimary,
    onPrimary = Color.White,
    primaryContainer = EmeraldBubbleOut,
    onPrimaryContainer = Color.White,
    background = EmeraldBg,
    onBackground = Color.White,
    surface = EmeraldSurface,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF14362D)
)

private val SunsetColorScheme = darkColorScheme(
    primary = SunsetPrimary,
    onPrimary = Color.White,
    primaryContainer = SunsetBubbleOut,
    onPrimaryContainer = Color.White,
    background = SunsetBg,
    onBackground = Color.White,
    surface = SunsetSurface,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF331B42)
)

@Composable
fun TelegramTheme(
    isDarkMode: Boolean = isSystemInDarkTheme(),
    themeName: String = "Classic Blue",
    content: @Composable () -> Unit
) {
    val (colorScheme, chatColors) = when {
        !isDarkMode -> Pair(
            LightColorScheme,
            TelegramChatColors(
                bubbleOutgoing = TelegramLightBubbleOut,
                bubbleIncoming = TelegramLightBubbleIn,
                bubbleOutgoingText = Color(0xFF0F1419),
                bubbleIncomingText = Color(0xFF0F1419),
                chatBackground = TelegramLightBg,
                unreadBadge = UnreadBadgeColor
            )
        )
        themeName == "Emerald" -> Pair(
            EmeraldColorScheme,
            TelegramChatColors(
                bubbleOutgoing = EmeraldBubbleOut,
                bubbleIncoming = EmeraldBubbleIn,
                bubbleOutgoingText = Color.White,
                bubbleIncomingText = Color.White,
                chatBackground = EmeraldBg,
                unreadBadge = EmeraldPrimary
            )
        )
        themeName == "Sunset" -> Pair(
            SunsetColorScheme,
            TelegramChatColors(
                bubbleOutgoing = SunsetBubbleOut,
                bubbleIncoming = SunsetBubbleIn,
                bubbleOutgoingText = Color.White,
                bubbleIncomingText = Color.White,
                chatBackground = SunsetBg,
                unreadBadge = SunsetPrimary
            )
        )
        else -> Pair(
            DarkColorScheme,
            TelegramChatColors(
                bubbleOutgoing = TelegramDarkBubbleOut,
                bubbleIncoming = TelegramDarkBubbleIn,
                bubbleOutgoingText = Color.White,
                bubbleIncomingText = Color.White,
                chatBackground = TelegramDarkBg,
                unreadBadge = TelegramDarkPrimary
            )
        )
    }

    CompositionLocalProvider(LocalTelegramColors provides chatColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
