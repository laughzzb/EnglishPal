package com.`is`.englishpal.room

import androidx.room.Entity
import androidx.room.PrimaryKey
//@Entity → 表（table）
@Entity(tableName = "messages")
data class ChatMessageEntity(
    //	id 是主键，每条记录的唯一标识
    @PrimaryKey val id: Long,
    val content: String,
    val isUser: Boolean,
    val timestamp: Long
)
