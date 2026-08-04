package com.example.data.repository

import android.content.Context
import com.example.BuildConfig
import com.example.data.db.ChatDao
import com.example.data.db.MessageDao
import com.example.data.db.TelegramDatabase
import com.example.data.model.Chat
import com.example.data.model.ChatCategory
import com.example.data.model.Message
import com.example.data.model.MessageType
import com.example.data.model.PollData
import com.example.data.model.PollOption
import com.example.data.model.UserProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.TimeUnit

class TelegramRepository private constructor(
    private val chatDao: ChatDao,
    private val messageDao: MessageDao
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    init {
        scope.launch {
            seedInitialDataIfNeeded()
        }
    }

    fun getAllChats(): Flow<List<Chat>> = chatDao.getAllChats()

    fun getChatById(chatId: String): Flow<Chat?> = chatDao.getChatById(chatId)

    fun getMessagesForChat(chatId: String): Flow<List<Message>> = messageDao.getMessagesForChat(chatId)

    fun getPinnedMessageForChat(chatId: String): Flow<Message?> = messageDao.getPinnedMessageForChat(chatId)

    fun searchMessages(query: String): Flow<List<Message>> = messageDao.searchMessages(query)

    suspend fun sendMessage(
        chatId: String,
        content: String,
        type: MessageType = MessageType.TEXT,
        mediaUrl: String = "",
        pollData: PollData? = null,
        replyToMessage: Message? = null
    ) {
        val now = System.currentTimeMillis()
        val msgId = "msg_${UUID.randomUUID()}"
        val user = _userProfile.value

        val message = Message(
            id = msgId,
            chatId = chatId,
            senderId = user.id,
            senderName = user.name,
            senderAvatar = user.avatarUrl,
            content = content,
            timestamp = now,
            isOutgoing = true,
            messageType = type,
            mediaUrl = mediaUrl,
            pollData = pollData,
            replyToMessageId = replyToMessage?.id,
            replyToSender = replyToMessage?.senderName,
            replyToText = replyToMessage?.content
        )

        messageDao.insertMessage(message)

        // Update chat preview
        val chat = chatDao.getChatByIdOneShot(chatId)
        if (chat != null) {
            val previewText = when (type) {
                MessageType.POLL -> "📊 Анкета: ${pollData?.question}"
                MessageType.IMAGE -> "📷 Снимка: $content"
                MessageType.VOICE -> "🎤 Гласово съобщение"
                MessageType.FILE -> "📁 Файл"
                MessageType.LOCATION -> "📍 Локация"
                else -> content
            }
            val updatedChat = chat.copy(
                lastMessage = previewText,
                lastMessageTime = now
            )
            chatDao.updateChat(updatedChat)

            // Trigger AI Bot reply if chatting with TeleBot AI
            if (chat.isBot || chatId == "bot_telebot") {
                triggerAiBotResponse(chatId, content)
            }
        }
    }

    private suspend fun triggerAiBotResponse(chatId: String, userPrompt: String) {
        val now = System.currentTimeMillis() + 1000
        val botMsgId = "msg_bot_${UUID.randomUUID()}"

        // Generate response using Gemini API or smart fallback
        val aiReplyText = fetchGeminiResponse(userPrompt)

        val botMessage = Message(
            id = botMsgId,
            chatId = chatId,
            senderId = "bot_telebot",
            senderName = "TeleBot AI 🤖",
            senderAvatar = "",
            content = aiReplyText,
            timestamp = now,
            isOutgoing = false,
            messageType = MessageType.TEXT
        )

        messageDao.insertMessage(botMessage)

        val chat = chatDao.getChatByIdOneShot(chatId)
        if (chat != null) {
            chatDao.updateChat(chat.copy(lastMessage = aiReplyText, lastMessageTime = now))
        }
    }

    private suspend fun fetchGeminiResponse(prompt: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isNotEmpty() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
                val systemPrompt = "Ти си полезен, приятелски асистент в Telegram (TeleBot AI). Отговаряй на български език ясно, с добро настроение и подходящи емотикони. Поддържай форматиране с Markdown за яснота."
                
                val json = JSONObject().apply {
                    put("contents", JSONArray().apply {
                        put(JSONObject().apply {
                            put("parts", JSONArray().apply {
                                put(JSONObject().put("text", "$systemPrompt\n\nПотребител: $prompt"))
                            })
                        })
                    })
                }

                val body = json.toString().toRequestBody("application/json".toMediaType())
                val request = Request.Builder().url(url).post(body).build()

                httpClient.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val responseStr = response.body?.string() ?: ""
                        val rootJson = JSONObject(responseStr)
                        val candidates = rootJson.optJSONArray("candidates")
                        if (candidates != null && candidates.length() > 0) {
                            val candidate = candidates.getJSONObject(0)
                            val content = candidate.optJSONObject("content")
                            val parts = content?.optJSONArray("parts")
                            if (parts != null && parts.length() > 0) {
                                val text = parts.getJSONObject(0).optString("text")
                                if (text.isNotBlank()) return@withContext text
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // Fallback to offline assistant
            }
        }

        // Smart offline fallback response generator
        val cleanPrompt = prompt.lowercase()
        return@withContext when {
            cleanPrompt.contains("здравей") || cleanPrompt.contains("здрасти") || cleanPrompt.contains("привет") ->
                "Здравей! 😊 Радвам се да се свържем в Telegram. С какво мога да ти помогна днес?"
            cleanPrompt.contains("как си") ->
                "Отлично! 🤖 Всички системи функционират на 100%. А ти как прекарваш деня си?"
            cleanPrompt.contains("погода") || cleanPrompt.contains("времето") ->
                "🌤️ Времето в България днес е слънчево и приятно за разходка или кодене! Температурите са около 24°C."
            cleanPrompt.contains("канал") || cleanPrompt.contains("група") ->
                "В Telegram можеш да създаваш публични канали за излъчване и групи с до 200 000 участника! Натисни '+' бутона, за да опиташ."
            cleanPrompt.contains("виц") || cleanPrompt.contains("шега") ->
                "😂 Защо програматорите предпочитат тъмната тема?\nЗащото светлината привлича бъгове!"
            else ->
                "🤖 **TeleBot AI**: Анализирах вашето запитване: *\"$prompt\"*\n\nTelegram предлага висока скорост, сигурност, реакция с емотикони, гласови съобщения и бързо споделяне на файлове. Можете да опитате да пуснете анкета или да изпратите снимка!"
        }
    }

    suspend fun togglePinChat(chatId: String) {
        val chat = chatDao.getChatByIdOneShot(chatId) ?: return
        chatDao.updatePinStatus(chatId, !chat.isPinned)
    }

    suspend fun toggleMuteChat(chatId: String) {
        val chat = chatDao.getChatByIdOneShot(chatId) ?: return
        chatDao.updateMuteStatus(chatId, !chat.isMuted)
    }

    suspend fun clearUnreadCount(chatId: String) {
        chatDao.clearUnreadCount(chatId)
    }

    suspend fun togglePinMessage(messageId: String) {
        val message = messageDao.getMessageById(messageId) ?: return
        val updated = message.copy(isPinned = !message.isPinned)
        messageDao.updateMessage(updated)
    }

    suspend fun addReaction(messageId: String, emoji: String) {
        val message = messageDao.getMessageById(messageId) ?: return
        val map = message.reactions.toMutableMap()
        val prevUserReaction = message.userReaction

        if (prevUserReaction == emoji) {
            // Remove reaction
            val currentCount = map[emoji] ?: 1
            if (currentCount <= 1) map.remove(emoji) else map[emoji] = currentCount - 1
            messageDao.updateMessage(message.copy(reactions = map, userReaction = null))
        } else {
            // If user reacted with another emoji previously, decrease it
            if (prevUserReaction != null) {
                val count = map[prevUserReaction] ?: 1
                if (count <= 1) map.remove(prevUserReaction) else map[prevUserReaction] = count - 1
            }
            map[emoji] = (map[emoji] ?: 0) + 1
            messageDao.updateMessage(message.copy(reactions = map, userReaction = emoji))
        }
    }

    suspend fun votePoll(messageId: String, optionIndex: Int) {
        val message = messageDao.getMessageById(messageId) ?: return
        val poll = message.pollData ?: return
        if (poll.userVotedOptionIndex != null) return // Already voted

        val updatedOptions = poll.options.mapIndexed { idx, opt ->
            if (idx == optionIndex) opt.copy(voteCount = opt.voteCount + 1) else opt
        }
        val updatedPoll = poll.copy(
            options = updatedOptions,
            totalVotes = poll.totalVotes + 1,
            userVotedOptionIndex = optionIndex
        )
        messageDao.updateMessage(message.copy(pollData = updatedPoll))
    }

    suspend fun createNewChat(title: String, category: ChatCategory, handle: String = "", bio: String = ""): String {
        val id = "chat_${UUID.randomUUID()}"
        val chat = Chat(
            id = id,
            title = title,
            category = category,
            lastMessage = "Чатът е създаден",
            lastMessageTime = System.currentTimeMillis(),
            isChannel = (category == ChatCategory.CHANNELS),
            isBot = (category == ChatCategory.BOTS),
            handle = if (handle.startsWith("@")) handle else if (handle.isNotEmpty()) "@$handle" else "",
            bio = bio
        )
        chatDao.insertChat(chat)

        // System message
        val sysMessage = Message(
            id = "msg_${UUID.randomUUID()}",
            chatId = id,
            senderId = "system",
            senderName = "Система",
            content = "Чатът бе успешно създаден.",
            timestamp = System.currentTimeMillis(),
            isOutgoing = false,
            messageType = MessageType.SYSTEM
        )
        messageDao.insertMessage(sysMessage)
        return id
    }

    suspend fun deleteMessage(messageId: String) {
        messageDao.deleteMessage(messageId)
    }

    suspend fun deleteChat(chatId: String) {
        messageDao.deleteAllMessagesForChat(chatId)
        chatDao.deleteChat(chatId)
    }

    fun updateUserProfile(profile: UserProfile) {
        _userProfile.value = profile
    }

    private suspend fun seedInitialDataIfNeeded() {
        val existingChats = chatDao.getAllChats().first()
        if (existingChats.isNotEmpty()) return

        val now = System.currentTimeMillis()

        val sampleChats = listOf(
            Chat(
                id = "bot_telebot",
                title = "TeleBot AI 🤖",
                category = ChatCategory.BOTS,
                lastMessage = "Здравей! Аз съм твоят AI асистент. Попитай ме каквото и да е!",
                lastMessageTime = now - 60000,
                unreadCount = 1,
                isPinned = true,
                isVerified = true,
                isBot = true,
                handle = "@telebot_ai",
                bio = "Официален интелигентен бот в Telegram. Задвижван от Gemini AI."
            ),
            Chat(
                id = "chat_saved",
                title = "Запазени съобщения 🔖",
                category = ChatCategory.FAVORITES,
                lastMessage = "Лично пространство за бележки, линкове и медия",
                lastMessageTime = now - 120000,
                isPinned = true,
                handle = "@me",
                bio = "Вашите запазени съобщения и файлове"
            ),
            Chat(
                id = "channel_news",
                title = "Телеграм Новини 🇧🇬",
                category = ChatCategory.CHANNELS,
                lastMessage = "🚀 Нова версия на Telegram! Поддръжка на анимирани стикери и HD видео.",
                lastMessageTime = now - 300000,
                unreadCount = 3,
                isPinned = true,
                isVerified = true,
                isChannel = true,
                memberCount = 28450,
                handle = "@telegram_news_bg",
                bio = "Официален канал за новини и актуализации на Telegram за България."
            ),
            Chat(
                id = "user_ivan",
                title = "Иван Георгиев",
                category = ChatCategory.DIRECT,
                lastMessage = "Чудесно! Изпращам ти снимките от Рила.",
                lastMessageTime = now - 600000,
                isOnline = true,
                lastSeenText = "онлайн",
                isVerified = true,
                handle = "@ivan_g",
                bio = "Мобилен разработчик | Фотография и планинарство 🏔️"
            ),
            Chat(
                id = "group_android_devs",
                title = "Android Developers BG 🤖",
                category = ChatCategory.GROUPS,
                lastMessage = "Елена: Кой ползва Jetpack Compose с Room flow?",
                lastMessageTime = now - 1800000,
                unreadCount = 5,
                memberCount = 1420,
                handle = "@android_devs_bg",
                bio = "Общност на Android разработчиците в България."
            ),
            Chat(
                id = "user_maria",
                title = "Мария Стоянова",
                category = ChatCategory.DIRECT,
                lastMessage = "Благодаря за проекта, изглежда супер! 👍",
                lastMessageTime = now - 3600000,
                isOnline = false,
                lastSeenText = "последно виждана днес в 18:15",
                handle = "@maria_st",
                bio = "UI/UX Designer @ TechStudio"
            ),
            Chat(
                id = "channel_travel",
                title = "Пътешествия и Приключения 🏔️",
                category = ChatCategory.CHANNELS,
                lastMessage = "Залез над Пирин - величествена гледка! 🌅",
                lastMessageTime = now - 86400000,
                isChannel = true,
                memberCount = 12500,
                handle = "@bg_travel_pics",
                bio = "Красиви места и природни забележителности от България."
            )
        )

        chatDao.insertChats(sampleChats)

        // Sample Messages
        val sampleMessages = listOf(
            // Saved Messages
            Message(
                id = "m_saved_1",
                chatId = "chat_saved",
                senderId = "user_me",
                senderName = "Александър",
                content = "📌 Идеи за новия Telegram клиент:\n1. Тъмна и светла тема\n2. AI асистент бот\n3. Реакции и гласови съобщения\n4. Анкети и викторини",
                timestamp = now - 120000,
                isOutgoing = true,
                isPinned = true
            ),
            // TeleBot AI
            Message(
                id = "m_bot_1",
                chatId = "bot_telebot",
                senderId = "bot_telebot",
                senderName = "TeleBot AI 🤖",
                content = "Здравей, Александър! 👋 Аз съм TeleBot AI. Мога да отговарям на въпроси, да генерирам идеи или да ти помогна с писане на текст. Напиши нещо и опитай!",
                timestamp = now - 60000,
                isOutgoing = false
            ),
            // Telegram News Channel
            Message(
                id = "m_news_1",
                chatId = "channel_news",
                senderId = "channel_news",
                senderName = "Телеграм Новини 🇧🇬",
                content = "🚀 **Обновление на Telegram v10.5**\n\n• Изключително бързи гласови съобщения\n• Гъвкави анкети с викторина\n• Персонализирани теми и тапети\n• Интеграция с AI асистенти",
                timestamp = now - 300000,
                isOutgoing = false,
                isPinned = true,
                reactions = mapOf("🔥" to 142, "❤️" to 89, "👏" to 56)
            ),
            // Poll in news channel
            Message(
                id = "m_news_poll",
                chatId = "channel_news",
                senderId = "channel_news",
                senderName = "Телеграм Новини 🇧🇬",
                content = "Каква функция използвате най-често в Telegram?",
                timestamp = now - 250000,
                isOutgoing = false,
                messageType = MessageType.POLL,
                pollData = PollData(
                    id = "poll_1",
                    question = "Коя функция в Telegram използвате най-често?",
                    options = listOf(
                        PollOption(0, "Гласови съобщения 🎤", 412),
                        PollOption(1, "Канали за новини 📰", 850),
                        PollOption(2, "Групови чатове 👥", 620),
                        PollOption(3, "AI Ботове 🤖", 310)
                    ),
                    totalVotes = 2192,
                    isAnonymous = true
                )
            ),
            // Ivan Georgiev
            Message(
                id = "m_ivan_1",
                chatId = "user_ivan",
                senderId = "user_ivan",
                senderName = "Иван Георгиев",
                content = "Здравей, Алекс! Свободен ли си за кафе утре?",
                timestamp = now - 1200000,
                isOutgoing = false
            ),
            Message(
                id = "m_ivan_2",
                chatId = "user_ivan",
                senderId = "user_me",
                senderName = "Александър",
                content = "Да, разбира се! Около 14:00 часа добре ли ти е?",
                timestamp = now - 900000,
                isOutgoing = true
            ),
            Message(
                id = "m_ivan_3",
                chatId = "user_ivan",
                senderId = "user_ivan",
                senderName = "Иван Георгиев",
                content = "Чудесно! Изпращам ти снимките от Рила.",
                timestamp = now - 600000,
                isOutgoing = false,
                reactions = mapOf("👍" to 1)
            ),
            // Android Devs Group
            Message(
                id = "m_group_1",
                chatId = "group_android_devs",
                senderId = "user_elena",
                senderName = "Елена Николова",
                content = "Кой ползва Jetpack Compose с Room flow?",
                timestamp = now - 1800000,
                isOutgoing = false
            )
        )

        messageDao.insertMessages(sampleMessages)
    }

    companion object {
        @Volatile
        private var INSTANCE: TelegramRepository? = null

        fun getInstance(context: Context): TelegramRepository {
            return INSTANCE ?: synchronized(this) {
                val db = TelegramDatabase.getInstance(context)
                val instance = TelegramRepository(db.chatDao(), db.messageDao())
                INSTANCE = instance
                instance
            }
        }
    }
}
