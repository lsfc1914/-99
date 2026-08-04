package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Chat
import com.example.data.model.ChatCategory
import com.example.data.model.Message
import com.example.data.model.MessageType
import com.example.data.model.PollData
import com.example.data.model.UserProfile
import com.example.data.repository.TelegramRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class TelegramViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = TelegramRepository.getInstance(application)

    val chats: StateFlow<List<Chat>> = repository.getAllChats()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val userProfile: StateFlow<UserProfile> = repository.userProfile

    private val _selectedChatId = MutableStateFlow<String?>(null)
    val selectedChatId: StateFlow<String?> = _selectedChatId.asStateFlow()

    val activeChat: StateFlow<Chat?> = _selectedChatId.flatMapLatest { id ->
        if (id != null) repository.getChatById(id) else flowOf(null)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val activeMessages: StateFlow<List<Message>> = _selectedChatId.flatMapLatest { id ->
        if (id != null) repository.getMessagesForChat(id) else flowOf(emptyList())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val activePinnedMessage: StateFlow<Message?> = _selectedChatId.flatMapLatest { id ->
        if (id != null) repository.getPinnedMessageForChat(id) else flowOf(null)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    fun selectChat(chatId: String?) {
        _selectedChatId.value = chatId
        if (chatId != null) {
            viewModelScope.launch {
                repository.clearUnreadCount(chatId)
            }
        }
    }

    fun sendMessage(
        chatId: String,
        content: String,
        type: MessageType = MessageType.TEXT,
        mediaUrl: String = "",
        pollData: PollData? = null,
        replyToMessage: Message? = null
    ) {
        viewModelScope.launch {
            repository.sendMessage(chatId, content, type, mediaUrl, pollData, replyToMessage)
        }
    }

    fun togglePinChat(chatId: String) {
        viewModelScope.launch {
            repository.togglePinChat(chatId)
        }
    }

    fun toggleMuteChat(chatId: String) {
        viewModelScope.launch {
            repository.toggleMuteChat(chatId)
        }
    }

    fun deleteChat(chatId: String) {
        viewModelScope.launch {
            repository.deleteChat(chatId)
            if (_selectedChatId.value == chatId) {
                _selectedChatId.value = null
            }
        }
    }

    fun togglePinMessage(messageId: String) {
        viewModelScope.launch {
            repository.togglePinMessage(messageId)
        }
    }

    fun addReaction(messageId: String, emoji: String) {
        viewModelScope.launch {
            repository.addReaction(messageId, emoji)
        }
    }

    fun votePoll(messageId: String, optionIndex: Int) {
        viewModelScope.launch {
            repository.votePoll(messageId, optionIndex)
        }
    }

    fun deleteMessage(messageId: String) {
        viewModelScope.launch {
            repository.deleteMessage(messageId)
        }
    }

    fun createNewChat(title: String, category: ChatCategory, handle: String, bio: String) {
        viewModelScope.launch {
            val newChatId = repository.createNewChat(title, category, handle, bio)
            selectChat(newChatId)
        }
    }

    fun updateUserProfile(profile: UserProfile) {
        repository.updateUserProfile(profile)
    }
}
