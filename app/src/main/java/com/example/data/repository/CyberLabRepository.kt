package com.example.data.repository

import android.content.Context
import com.example.data.local.CyberDatabase
import com.example.data.local.entity.ForumPostEntity
import com.example.data.local.entity.ScanReportEntity
import com.example.data.local.entity.TerminalHistoryEntity
import com.example.data.local.entity.UserStatsEntity
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class CyberLabRepository(private val context: Context) {
    private val database = CyberDatabase.getInstance(context)
    private val cyberDao = database.cyberDao()
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _terminalLines = MutableStateFlow<List<TerminalLine>>(emptyList())
    val terminalLines: Flow<List<TerminalLine>> = _terminalLines.asStateFlow()

    private val _tools = MutableStateFlow<List<SecurityTool>>(emptyList())
    val tools: Flow<List<SecurityTool>> = _tools.asStateFlow()

    private val _cves = MutableStateFlow<List<CVEItem>>(emptyList())
    val cves: Flow<List<CVEItem>> = _cves.asStateFlow()

    private val _threats = MutableStateFlow<List<ThreatIncident>>(emptyList())
    val threats: Flow<List<ThreatIncident>> = _threats.asStateFlow()

    private val _challenges = MutableStateFlow<List<CTFChallenge>>(emptyList())
    val challenges: Flow<List<CTFChallenge>> = _challenges.asStateFlow()

    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: Flow<UserProfile> = _userProfile.asStateFlow()

    init {
        initializeData()
        loadPersistedState()
    }

    private fun initializeData() {
        // Initialize Terminal Banner
        _terminalLines.value = listOf(
            TerminalLine(
                id = UUID.randomUUID().toString(),
                type = TerminalLine.LineType.BANNER,
                content = """
                    ==================================================================
                    [+] CYBER_LAB_PRO v3.4.0-RELEASE (x86_64-hardened-linux-gnu)
                    [+] KERNEL: 6.9.12-cyberlab-custom #1 SMP PREEMPT_DYNAMIC
                    [+] NODE: node-01.sandbox.cyberlab.internal (IP: 10.0.4.15)
                    [+] DEFENSE STATUS: ARMED | IDS/IPS: ACTIVE | AUDIT LOG: ON
                    ==================================================================
                    Type 'help' to view available commands, or tap quick touchbar actions.
                """.trimIndent()
            )
        )

        // Initialize 50+ Security Tools Directory
        _tools.value = SecurityToolsDataSource.getTools()

        // Initialize CVE Database
        _cves.value = CveDataSource.getCves()

        // Initialize Threat Intelligence Telemetry
        _threats.value = ThreatDataSource.getThreats()

        // Initialize CTF Challenges
        _challenges.value = CtfDataSource.getChallenges()
    }

    private fun loadPersistedState() {
        scope.launch {
            // Load user stats
            val existingStats = cyberDao.getUserStats()
            if (existingStats == null) {
                cyberDao.insertOrUpdateUserStats(
                    UserStatsEntity(
                        id = 1,
                        username = "OPERATOR_0x1",
                        level = 1,
                        rankTitle = "Script Kiddie",
                        currentXp = 150,
                        xpForNextLevel = 300,
                        streakDays = 4,
                        completedChallengesCsv = "",
                        bookmarkedToolsCsv = "nmap,sqlmap,hydra",
                        unlockedBadgesCsv = "FIRST_BREACH,TERMINAL_PRO"
                    )
                )
            }

            cyberDao.getUserStatsFlow().collect { stats ->
                if (stats != null) {
                    val completed = stats.completedChallengesCsv.split(",").filter { it.isNotBlank() }.toSet()
                    val bookmarks = stats.bookmarkedToolsCsv.split(",").filter { it.isNotBlank() }.toSet()
                    val badges = stats.unlockedBadgesCsv.split(",").filter { it.isNotBlank() }.toSet()

                    _userProfile.value = UserProfile(
                        username = stats.username,
                        level = stats.level,
                        rankTitle = stats.rankTitle,
                        currentXp = stats.currentXp,
                        xpForNextLevel = stats.xpForNextLevel,
                        streakDays = stats.streakDays,
                        completedChallengeIds = completed,
                        bookmarkedToolIds = bookmarks,
                        unlockedBadgeIds = badges
                    )

                    // Update tools bookmarked state
                    _tools.value = _tools.value.map { tool ->
                        tool.copy(isBookmarked = bookmarks.contains(tool.id))
                    }

                    // Update challenge completed state
                    _challenges.value = _challenges.value.map { ch ->
                        ch.copy(isCompleted = completed.contains(ch.id))
                    }
                }
            }
        }
    }

    fun toggleToolBookmark(toolId: String) {
        scope.launch {
            val current = _userProfile.value.bookmarkedToolIds.toMutableSet()
            if (current.contains(toolId)) {
                current.remove(toolId)
            } else {
                current.add(toolId)
            }
            val stats = cyberDao.getUserStats() ?: UserStatsEntity()
            cyberDao.insertOrUpdateUserStats(
                stats.copy(bookmarkedToolsCsv = current.joinToString(","))
            )
        }
    }

    fun awardXp(points: Int, challengeId: String? = null) {
        scope.launch {
            val current = _userProfile.value
            val newXp = current.currentXp + points
            var level = current.level
            var xpForNext = current.xpForNextLevel
            var rank = current.rankTitle
            val badges = current.unlockedBadgeIds.toMutableSet()
            val completed = current.completedChallengeIds.toMutableSet()

            if (challengeId != null) {
                completed.add(challengeId)
                if (completed.size >= 3) badges.add("CTF_NOVICE")
                if (completed.size >= 6) badges.add("FLAG_HUNTER")
            }

            while (newXp >= xpForNext) {
                level++
                xpForNext += level * 350
            }

            rank = when (level) {
                1 -> "Script Kiddie"
                2 -> "Junior Analyst"
                3 -> "SOC Defender"
                4 -> "Penetration Tester"
                5 -> "Red Team Operator"
                6 -> "Cyber Architect"
                else -> "Elite CISO"
            }

            if (level >= 3) badges.add("SOC_DEFENDER")
            if (level >= 5) badges.add("RED_TEAM_ELITE")

            val stats = cyberDao.getUserStats() ?: UserStatsEntity()
            cyberDao.insertOrUpdateUserStats(
                stats.copy(
                    level = level,
                    rankTitle = rank,
                    currentXp = newXp,
                    xpForNextLevel = xpForNext,
                    completedChallengesCsv = completed.joinToString(","),
                    unlockedBadgesCsv = badges.joinToString(",")
                )
            )
        }
    }

    // Community Forum
    fun getForumPosts(): Flow<List<ForumPost>> {
        return cyberDao.getAllForumPosts().map { entities ->
            if (entities.isEmpty()) {
                ForumDataSource.getDefaultPosts().map { it.toEntity() }.forEach {
                    cyberDao.insertForumPost(it)
                }
                ForumDataSource.getDefaultPosts()
            } else {
                entities.map { it.toModel() }
            }
        }
    }

    fun upvotePost(post: ForumPost) {
        scope.launch {
            val updated = post.copy(
                upvotes = if (post.isUpvoted) post.upvotes - 1 else post.upvotes + 1,
                isUpvoted = !post.isUpvoted
            )
            cyberDao.updateForumPost(updated.toEntity())
        }
    }

    fun createForumPost(title: String, category: String, content: String, codeSnippet: String?) {
        scope.launch {
            val user = _userProfile.value
            val timeStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date())
            val newPost = ForumPost(
                id = UUID.randomUUID().toString(),
                author = user.username,
                authorRole = user.rankTitle,
                verifiedBadge = user.level >= 4,
                title = title,
                category = category,
                content = content,
                codeSnippet = codeSnippet,
                upvotes = 1,
                timestamp = timeStr,
                isUpvoted = true
            )
            cyberDao.insertForumPost(newPost.toEntity())
            awardXp(50)
        }
    }

    // Vulnerability Reports
    fun getScanReports(): Flow<List<ScanResultReport>> {
        return cyberDao.getAllScanReports().map { entities ->
            entities.map { it.toModel() }
        }
    }

    fun saveScanReport(report: ScanResultReport) {
        scope.launch {
            cyberDao.insertScanReport(report.toEntity())
            awardXp(80)
        }
    }

    fun deleteScanReport(reportId: String) {
        scope.launch {
            cyberDao.deleteScanReport(reportId)
        }
    }

    // Terminal Operations
    fun executeCommand(commandInput: String): List<TerminalLine> {
        val trimmed = commandInput.trim()
        if (trimmed.isEmpty()) return emptyList()

        scope.launch {
            cyberDao.insertTerminalHistory(TerminalHistoryEntity(command = trimmed))
        }

        val inputLine = TerminalLine(
            id = UUID.randomUUID().toString(),
            type = TerminalLine.LineType.INPUT,
            content = "root@cyberlab-node-01:~$ $trimmed"
        )

        if (trimmed.equals("clear", ignoreCase = true)) {
            _terminalLines.value = emptyList()
            return emptyList()
        }

        val outputLines = TerminalSimulator.processCommand(trimmed)
        val combined = _terminalLines.value + inputLine + outputLines
        _terminalLines.value = combined
        return outputLines
    }

    fun clearTerminal() {
        _terminalLines.value = emptyList()
    }

    fun exportTerminalLogs(): String {
        return _terminalLines.value.joinToString("\n") { line ->
            "[${SimpleDateFormat("HH:mm:ss", Locale.US).format(Date(line.timestamp))}] ${line.content}"
        }
    }
}

// Extension mappers
fun ForumPostEntity.toModel() = ForumPost(
    id = id,
    author = author,
    authorRole = authorRole,
    verifiedBadge = verifiedBadge,
    title = title,
    category = category,
    content = content,
    codeSnippet = codeSnippet,
    upvotes = upvotes,
    timestamp = timestamp,
    isUpvoted = isUpvoted
)

fun ForumPost.toEntity() = ForumPostEntity(
    id = id,
    author = author,
    authorRole = authorRole,
    verifiedBadge = verifiedBadge,
    title = title,
    category = category,
    content = content,
    codeSnippet = codeSnippet,
    upvotes = upvotes,
    timestamp = timestamp,
    isUpvoted = isUpvoted
)

fun ScanReportEntity.toModel(): ScanResultReport {
    // Simple parser for findings
    val findingsList = parseFindings(findingsJson)
    return ScanResultReport(
        id = id,
        target = target,
        profile = profile,
        scanTime = scanTime,
        totalVulnerabilities = totalVulnerabilities,
        criticalCount = criticalCount,
        highCount = highCount,
        mediumCount = mediumCount,
        lowCount = lowCount,
        overallRiskScore = overallRiskScore,
        findings = findingsList,
        executiveSummary = executiveSummary
    )
}

fun ScanResultReport.toEntity() = ScanReportEntity(
    id = id,
    target = target,
    profile = profile,
    scanTime = scanTime,
    totalVulnerabilities = totalVulnerabilities,
    criticalCount = criticalCount,
    highCount = highCount,
    mediumCount = mediumCount,
    lowCount = lowCount,
    overallRiskScore = overallRiskScore,
    findingsJson = serializeFindings(findings),
    executiveSummary = executiveSummary
)

private fun serializeFindings(findings: List<ScanFinding>): String {
    return findings.joinToString("###FINDING###") {
        "${it.title}|||${it.severity.name}|||${it.portOrPath}|||${it.description}|||${it.remediation}"
    }
}

private fun parseFindings(json: String): List<ScanFinding> {
    if (json.isBlank()) return emptyList()
    return json.split("###FINDING###").mapNotNull { part ->
        val tokens = part.split("|||")
        if (tokens.size >= 5) {
            ScanFinding(
                title = tokens[0],
                severity = try { RiskLevel.valueOf(tokens[1]) } catch (e: Exception) { RiskLevel.MEDIUM },
                portOrPath = tokens[2],
                description = tokens[3],
                remediation = tokens[4]
            )
        } else null
    }
}
