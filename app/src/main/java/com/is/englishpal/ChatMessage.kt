package com.`is`.englishpal

import com.`is`.englishpal.room.ChatMessageEntity

data class ChatMessage(
    val id: Long = System.currentTimeMillis(),
    val content: String,
    val isUser: Boolean,
    //true → 显示打字动画 ... false → 动画消失，回复完成
    val isStreaming: Boolean = false,
    val timestamp: Long = id
) {
    // ChatMessage → Entity（存库时调用）
    fun toEntity() = ChatMessageEntity(
        id = id, content = content, isUser = isUser, timestamp = timestamp
    )

    companion object {
        // Entity → ChatMessage（读取时调用）
        fun fromEntity(entity: ChatMessageEntity) = ChatMessage(
            id = entity.id,
            content = entity.content,
            isUser = entity.isUser,
            isStreaming = false,
            timestamp = entity.timestamp
        )
    }
}
