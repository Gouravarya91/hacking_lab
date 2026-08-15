package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_stats")
data class UserStatsEntity(
    @PrimaryKey val id: Int = 1,
    val username: String = "OPERATOR_0x1",
    val level: Int = 1,
    val rankTitle: String = "Script Kiddie",
    val currentXp: Int = 150,
    val xpForNextLevel: Int = 300,
    val streakDays: Int = 4,
    val completedChallengesCsv: String = "",
    val bookmarkedToolsCsv: String = "",
    val unlockedBadgesCsv: String = "FIRST_BREACH,TERMINAL_PRO"
)

@Entity(tableName = "forum_posts")
data class ForumPostEntity(
    @PrimaryKey val id: String,
    val author: String,
    val authorRole: String,
    val verifiedBadge: Boolean,
    val title: String,
    val category: String,
    val content: String,
    val codeSnippet: String?,
    val upvotes: Int,
    val timestamp: String,
    val isUpvoted: Boolean = false
)

@Entity(tableName = "scan_reports")
data class ScanReportEntity(
    @PrimaryKey val id: String,
    val target: String,
    val profile: String,
    val scanTime: String,
    val totalVulnerabilities: Int,
    val criticalCount: Int,
    val highCount: Int,
    val mediumCount: Int,
    val lowCount: Int,
    val overallRiskScore: Float,
    val findingsJson: String,
    val executiveSummary: String
)

@Entity(tableName = "terminal_history")
data class TerminalHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val command: String,
    val timestamp: Long = System.currentTimeMillis()
)
