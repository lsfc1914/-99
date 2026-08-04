package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Chat
import com.example.data.model.ChatCategory
import com.example.data.model.UserProfile
import com.example.ui.components.ChatListItem
import com.example.ui.components.CreateChatDialog
import com.example.ui.components.TelegramDrawer
import com.example.ui.components.TelegramTopBar
import com.example.ui.theme.TelegramBlue
import kotlinx.coroutines.launch

@Composable
fun ChatListScreen(
    chats: List<Chat>,
    userProfile: UserProfile,
    onChatClick: (Chat) -> Unit,
    onPinToggle: (chatId: String) -> Unit,
    onMuteToggle: (chatId: String) -> Unit,
    onDeleteChat: (chatId: String) -> Unit,
    onCreateChat: (title: String, category: ChatCategory, handle: String, bio: String) -> Unit,
    onDarkModeToggle: (Boolean) -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var selectedCategory by remember { mutableStateOf(ChatCategory.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var showCreateChatDialog by remember { mutableStateOf(false) }

    // Filter chats based on selected folder/tab and search query
    val filteredChats = remember(chats, selectedCategory, searchQuery) {
        chats.filter { chat ->
            val matchesCategory = when (selectedCategory) {
                ChatCategory.ALL -> true
                ChatCategory.DIRECT -> !chat.isChannel && !chat.isBot && chat.id != "chat_saved"
                ChatCategory.GROUPS -> chat.memberCount > 1 && !chat.isChannel
                ChatCategory.CHANNELS -> chat.isChannel
                ChatCategory.BOTS -> chat.isBot
                ChatCategory.FAVORITES -> chat.isPinned || chat.id == "chat_saved"
            }

            val matchesSearch = searchQuery.isBlank() ||
                    chat.title.contains(searchQuery, ignoreCase = true) ||
                    chat.lastMessage.contains(searchQuery, ignoreCase = true) ||
                    chat.handle.contains(searchQuery, ignoreCase = true)

            matchesCategory && matchesSearch
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            TelegramDrawer(
                userProfile = userProfile,
                onSavedMessagesClick = {
                    val savedChat = chats.firstOrNull { it.id == "chat_saved" }
                    if (savedChat != null) onChatClick(savedChat)
                },
                onNewGroupClick = { showCreateChatDialog = true },
                onNewChannelClick = { showCreateChatDialog = true },
                onContactsClick = { },
                onCallsClick = { },
                onSettingsClick = onOpenSettings,
                onDarkModeToggle = onDarkModeToggle,
                onCloseDrawer = { scope.launch { drawerState.close() } }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TelegramTopBar(
                    selectedCategory = selectedCategory,
                    onCategorySelected = { selectedCategory = it },
                    onMenuClick = { scope.launch { drawerState.open() } },
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    isSearchActive = isSearchActive,
                    onSearchActiveToggle = { isSearchActive = it }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showCreateChatDialog = true },
                    containerColor = TelegramBlue,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.testTag("fab_new_chat")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "New Chat"
                    )
                }
            },
            modifier = modifier
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                if (filteredChats.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Text(
                                text = "Няма намерени чатове 💬",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Използвайте '+' бутона за започване на нов разговор, създаване на канал или група.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 80.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("chat_list_lazy_column")
                    ) {
                        items(
                            items = filteredChats,
                            key = { it.id }
                        ) { chat ->
                            ChatListItem(
                                chat = chat,
                                onClick = { onChatClick(chat) },
                                onPinToggle = { onPinToggle(chat.id) },
                                onMuteToggle = { onMuteToggle(chat.id) },
                                onDelete = { onDeleteChat(chat.id) }
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 76.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }

            // Create New Chat Modal Dialog
            if (showCreateChatDialog) {
                CreateChatDialog(
                    initialCategory = selectedCategory,
                    onDismiss = { showCreateChatDialog = false },
                    onCreateChat = { title, category, handle, bio ->
                        onCreateChat(title, category, handle, bio)
                    }
                )
            }
        }
    }
}
