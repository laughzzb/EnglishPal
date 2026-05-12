package com.`is`.englishpal.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
//@Database → 数据库本身
@Database(entities = [ChatMessageEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    //	Room 自动生成 DAO 实现
    abstract fun chatMessageDao(): ChatMessageDao

    //整个 App 只创建一个数据库实例，避免重复创建
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "englishpal_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
