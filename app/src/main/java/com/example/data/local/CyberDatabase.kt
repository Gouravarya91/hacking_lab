package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.CyberDao
import com.example.data.local.entity.ForumPostEntity
import com.example.data.local.entity.ScanReportEntity
import com.example.data.local.entity.TerminalHistoryEntity
import com.example.data.local.entity.UserStatsEntity

@Database(
    entities = [
        UserStatsEntity::class,
        ForumPostEntity::class,
        ScanReportEntity::class,
        TerminalHistoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class CyberDatabase : RoomDatabase() {
    abstract fun cyberDao(): CyberDao

    companion object {
        @Volatile
        private var INSTANCE: CyberDatabase? = null

        fun getInstance(context: Context): CyberDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CyberDatabase::class.java,
                    "cyber_lab_pro.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
