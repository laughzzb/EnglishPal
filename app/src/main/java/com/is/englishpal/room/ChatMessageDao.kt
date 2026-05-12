package com.`is`.englishpal.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

//@Dao   → 增删改查（CRUD）
@Dao
interface ChatMessageDao {

    @Query("SELECT * FROM messages ORDER BY id ASC")
    suspend fun getAllMessages(): List<ChatMessageEntity>

    //OnConflictStrategy.REPLACE 的意思是：如果 id 已存在就覆盖，不存在就插入。
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: ChatMessageEntity)

    @Query("DELETE FROM messages")
    suspend fun deleteAll()
}
