package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.TelegramViewModel
import com.example.ui.screens.ChatDetailScreen
import com.example.ui.screens.ChatListScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.TelegramTheme

enum class ScreenState {
    CHAT_LIST, CHAT_DETAIL, SETTINGS
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: TelegramViewModel = viewModel()
            val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()

            TelegramTheme(
                isDarkMode = userProfile.isDarkMode,
                themeName = userProfile.themeName
            ) {
                TelegramApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun TelegramApp(viewModel: TelegramViewModel) {
    var screenState by remember { mutableStateOf(ScreenState.CHAT_LIST) }

    val chats by viewModel.chats.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val activeChat by viewModel.activeChat.collectAsStateWithLifecycle()
    val activeMessages by viewModel.activeMessages.collectAsStateWithLifecycle()
    val activePinnedMessage by viewModel.activePinnedMessage.collectAsStateWithLifecycle()

    // Handle system back button press
    BackHandler(enabled = screenState != ScreenState.CHAT_LIST) {
        when (screenState) {
            ScreenState.CHAT_DETAIL -> {
                viewModel.selectChat(null)
                screenState = ScreenState.CHAT_LIST
            }
            ScreenState.SETTINGS -> {
                screenState = ScreenState.CHAT_LIST
            }
            else -> {}
        }
    }

    when (screenState) {
        ScreenState.CHAT_LIST -> {
            ChatListScreen(
                chats = chats,
                userProfile = userProfile,
                onChatClick = { chat ->
                    viewModel.selectChat(chat.id)
                    screenState = ScreenState.CHAT_DETAIL
                },
                onPinToggle = { chatId -> viewModel.togglePinChat(chatId) },
                onMuteToggle = { chatId -> viewModel.toggleMuteChat(chatId) },
                onDeleteChat = { chatId -> viewModel.deleteChat(chatId) },
                onCreateChat = { title, category, handle, bio ->
                    viewModel.createNewChat(title, category, handle, bio)
                    screenState = ScreenState.CHAT_DETAIL
                },
                onDarkModeToggle = { isDark ->
                    viewModel.updateUserProfile(userProfile.copy(isDarkMode = isDark))
                },
                onOpenSettings = {
                    screenState = ScreenState.SETTINGS
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        ScreenState.CHAT_DETAIL -> {
            val chat = activeChat
            if (chat != null) {
                ChatDetailScreen(
                    chat = chat,
                    messages = activeMessages,
                    pinnedMessage = activePinnedMessage,
                    onBackClick = {
                        viewModel.selectChat(null)
                        screenState = ScreenState.CHAT_LIST
                    },
                    onSendMessage = { text, type, mediaUrl, pollData, replyTo ->
                        viewModel.sendMessage(chat.id, text, type, mediaUrl, pollData, replyTo)
                    },
                    onReactionSelect = { msgId, emoji ->
                        viewModel.addReaction(msgId, emoji)
                    },
                    onPinMessage = { msgId ->
                        viewModel.togglePinMessage(msgId)
                    },
                    onDeleteMessage = { msgId ->
                        viewModel.deleteMessage(msgId)
                    },
                    onVotePoll = { msgId, optIdx ->
                        viewModel.votePoll(msgId, optIdx)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                screenState = ScreenState.CHAT_LIST
            }
        }

        ScreenState.SETTINGS -> {
            SettingsScreen(
                userProfile = userProfile,
                onSaveProfile = { updatedProfile ->
                    viewModel.updateUserProfile(updatedProfile)
                },
                onBackClick = {
                    screenState = ScreenState.CHAT_LIST
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
