package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.ForumPostEntity
import com.example.data.local.entity.ScanReportEntity
import com.example.data.local.entity.TerminalHistoryEntity
import com.example.data.local.entity.UserStatsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CyberDao {
    // User Stats
    @Query("SELECT * FROM user_stats WHERE id = 1")
    fun getUserStatsFlow(): Flow<UserStatsEntity?>

    @Query("SELECT * FROM user_stats WHERE id = 1")
    suspend fun getUserStats(): UserStatsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUserStats(stats: UserStatsEntity)

    // Forum Posts
    @Query("SELECT * FROM forum_posts ORDER BY timestamp DESC")
    fun getAllForumPosts(): Flow<List<ForumPostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertForumPost(post: ForumPostEntity)

    @Update
    suspend fun updateForumPost(post: ForumPostEntity)

    // Scan Reports
    @Query("SELECT * FROM scan_reports ORDER BY scanTime DESC")
    fun getAllScanReports(): Flow<List<ScanReportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScanReport(report: ScanReportEntity)

    @Query("DELETE FROM scan_reports WHERE id = :reportId")
    suspend fun deleteScanReport(reportId: String)

    // Terminal History
    @Query("SELECT * FROM terminal_history ORDER BY id DESC LIMIT 50")
    fun getRecentTerminalHistory(): Flow<List<TerminalHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTerminalHistory(item: TerminalHistoryEntity)

    @Query("DELETE FROM terminal_history")
    suspend fun clearTerminalHistory()
}
