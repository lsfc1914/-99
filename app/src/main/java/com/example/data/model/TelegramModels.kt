package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

enum class ChatCategory {
    ALL, DIRECT, GROUPS, CHANNELS, BOTS, FAVORITES
}

enum class MessageType {
    TEXT, IMAGE, VOICE, FILE, LOCATION, POLL, SYSTEM
}

@Entity(tableName = "chats")
data class Chat(
    @PrimaryKey val id: String,
    val title: String,
    val avatarUrl: String = "",
    val category: ChatCategory = ChatCategory.DIRECT,
    val lastMessage: String = "",
    val lastMessageTime: Long = System.currentTimeMillis(),
    val unreadCount: Int = 0,
    val isPinned: Boolean = false,
    val isMuted: Boolean = false,
    val isOnline: Boolean = false,
    val lastSeenText: String = "последно видян наскоро",
    val isVerified: Boolean = false,
    val isChannel: Boolean = false,
    val isBot: Boolean = false,
    val memberCount: Int = 1,
    val handle: String = "", // e.g. @telegram_news
    val bio: String = ""
)

@Entity(
    tableName = "messages",
    indices = [
        Index(value = ["chatId", "timestamp"]),
        Index(value = ["chatId", "isPinned", "timestamp"])
    ]
)
data class Message(
    @PrimaryKey val id: String,
    val chatId: String,
    val senderId: String,
    val senderName: String,
    val senderAvatar: String = "",
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isOutgoing: Boolean,
    val messageType: MessageType = MessageType.TEXT,
    val mediaUrl: String = "",
    val mediaCaption: String = "",
    val isRead: Boolean = true,
    val isPinned: Boolean = false,
    val reactions: Map<String, Int> = emptyMap(), // Emoji -> Count
    val userReaction: String? = null,
    val replyToMessageId: String? = null,
    val replyToSender: String? = null,
    val replyToText: String? = null,
    val pollData: PollData? = null
)

data class PollData(
    val id: String,
    val question: String,
    val options: List<PollOption>,
    val totalVotes: Int = 0,
    val userVotedOptionIndex: Int? = null,
    val isAnonymous: Boolean = true,
    val isQuiz: Boolean = false,
    val correctOptionIndex: Int? = null
)

data class PollOption(
    val id: Int,
    val text: String,
    val voteCount: Int = 0
)

data class UserProfile(
    val id: String = "user_me",
    val name: String = "Александър Петров",
    val username: String = "@alex_bg",
    val phone: String = "+359 88 812 3456",
    val bio: String = "🚀 Разработвам страхотни Android приложения | Jetpack Compose",
    val avatarUrl: String = "",
    val themeName: String = "Classic Blue",
    val isDarkMode: Boolean = false
)

class RoomConverters {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    @TypeConverter
    fun fromChatCategory(category: ChatCategory): String = category.name

    @TypeConverter
    fun toChatCategory(name: String): ChatCategory = try {
        ChatCategory.valueOf(name)
    } catch (e: Exception) {
        ChatCategory.ALL
    }

    @TypeConverter
    fun fromMessageType(type: MessageType): String = type.name

    @TypeConverter
    fun toMessageType(name: String): MessageType = try {
        MessageType.valueOf(name)
    } catch (e: Exception) {
        MessageType.TEXT
    }

    @TypeConverter
    fun fromReactionsMap(map: Map<String, Int>): String {
        val type = Types.newParameterizedType(Map::class.java, String::class.java, Int::class.javaObjectType)
        val adapter = moshi.adapter<Map<String, Int>>(type)
        return adapter.toJson(map)
    }

    @TypeConverter
    fun toReactionsMap(json: String): Map<String, Int> {
        if (json.isEmpty()) return emptyMap()
        return try {
            val type = Types.newParameterizedType(Map::class.java, String::class.java, Int::class.javaObjectType)
            val adapter = moshi.adapter<Map<String, Int>>(type)
            adapter.fromJson(json) ?: emptyMap()
        } catch (e: Exception) {
            emptyMap()
        }
    }

    @TypeConverter
    fun fromPollData(pollData: PollData?): String {
        if (pollData == null) return ""
        val adapter = moshi.adapter(PollData::class.java)
        return adapter.toJson(pollData)
    }

    @TypeConverter
    fun toPollData(json: String): PollData? {
        if (json.isEmpty()) return null
        return try {
            val adapter = moshi.adapter(PollData::class.java)
            adapter.fromJson(json)
        } catch (e: Exception) {
            null
        }
    }
}
