package com.example.data.model

data class TerminalLine(
    val id: String,
    val type: LineType,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
) {
    enum class LineType {
        INPUT,
        OUTPUT,
        ERROR,
        SUCCESS,
        SYSTEM,
        BANNER,
        TABLE
    }
}

enum class ToolCategory(val displayName: String, val iconName: String) {
    RECON("Recon & OSINT", "Radar"),
    WEB_APP("Web PenTesting", "Globe"),
    NETWORK("Network & Wireless", "Wifi"),
    EXPLOITATION("Exploitation & Payloads", "Bomb"),
    CLOUD_DEVSECOPS("Cloud & DevSecOps", "Cloud"),
    FORENSICS("Forensics & Incident Response", "Search")
}

enum class RiskLevel {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

enum class Difficulty {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED
}

data class ToolExample(
    val title: String,
    val command: String,
    val description: String
)

data class SecurityTool(
    val id: String,
    val name: String,
    val category: ToolCategory,
    val description: String,
    val syntax: String,
    val riskLevel: RiskLevel,
    val difficulty: Difficulty,
    val examples: List<ToolExample>,
    val defaultTarget: String = "192.168.1.100",
    val isBookmarked: Boolean = false
)

data class CVEItem(
    val id: String, // e.g. CVE-2024-3094
    val title: String,
    val cvssScore: Float, // e.g. 10.0
    val severity: RiskLevel,
    val affectedSoftware: String,
    val attackVector: String, // e.g. Network, Local
    val complexity: String, // Low, High
    val privilegesRequired: String, // None, Low
    val patchStatus: String, // Patched, Zero-Day, Workaround
    val description: String,
    val remediation: String,
    val references: List<String>
)

data class ThreatIncident(
    val id: String,
    val threatType: String, // Ransomware, DDoS, APT Campaign, Zero-Day, Supply Chain
    val actorOrCampaign: String, // e.g. LockBit 4.0, Volt Typhoon, Lazarus Group
    val sourceCountry: String,
    val sourceCoords: Pair<Float, Float>, // Normalized 0..1 for map
    val targetCountry: String,
    val targetCoords: Pair<Float, Float>,
    val targetSector: String, // Finance, Healthcare, Gov, Critical Infra
    val severity: RiskLevel,
    val mitreTechniques: List<String>, // T1059.001, T1190, T1078
    val iocs: List<String>, // IPs, MD5/SHA256, Domains
    val timestamp: String,
    val summary: String
)

data class CTFChallenge(
    val id: String,
    val title: String,
    val category: String, // Web, Crypto, Forensics, Reverse, Cloud, OSINT
    val points: Int,
    val difficulty: Difficulty,
    val prompt: String,
    val codeOrLogSnippet: String? = null,
    val options: List<String>? = null, // for multiple choice
    val correctOptionIndex: Int? = null,
    val flagAnswer: String? = null, // for text flag input
    val hint: String,
    val explanation: String,
    val isCompleted: Boolean = false
)

data class ForumPost(
    val id: String,
    val author: String,
    val authorRole: String, // e.g. "Lead Red Teamer", "SOC Level 3", "Bug Hunter"
    val verifiedBadge: Boolean,
    val title: String,
    val category: String, // CTF, Zero-Day, Malware Analysis, Career, Blue Team
    val content: String,
    val codeSnippet: String? = null,
    val upvotes: Int,
    val timestamp: String,
    val isUpvoted: Boolean = false
)

data class ScanResultReport(
    val id: String,
    val target: String,
    val profile: String,
    val scanTime: String,
    val totalVulnerabilities: Int,
    val criticalCount: Int,
    val highCount: Int,
    val mediumCount: Int,
    val lowCount: Int,
    val overallRiskScore: Float, // 0..100
    val findings: List<ScanFinding>,
    val executiveSummary: String
)

data class ScanFinding(
    val title: String,
    val severity: RiskLevel,
    val portOrPath: String,
    val description: String,
    val remediation: String
)

data class UserProfile(
    val username: String = "OPERATOR_0x1",
    val level: Int = 1,
    val rankTitle: String = "Script Kiddie",
    val currentXp: Int = 150,
    val xpForNextLevel: Int = 300,
    val streakDays: Int = 4,
    val completedChallengeIds: Set<String> = emptySet(),
    val bookmarkedToolIds: Set<String> = emptySet(),
    val unlockedBadgeIds: Set<String> = setOf("FIRST_BREACH", "TERMINAL_PRO")
)

data class AchievementBadge(
    val id: String,
    val name: String,
    val description: String,
    val icon: String,
    val unlocked: Boolean
)
