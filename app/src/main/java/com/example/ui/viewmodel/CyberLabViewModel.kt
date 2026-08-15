package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.CyberAudioEngine
import com.example.data.ai.AiCopilotMode
import com.example.data.ai.CyberAiService
import com.example.data.ai.ExtractedCodeSnippet
import com.example.data.ai.ExtractedCommand
import com.example.data.ai.GeminiContent
import com.example.data.ai.GeminiPart
import com.example.data.model.*
import com.example.data.repository.CyberLabRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

enum class CyberTab(val label: String, val badge: String? = null) {
    TERMINAL("Sandbox CLI"),
    TOOLS("Tools Hub", "LIVE"),
    THREAT_MAP("Threat Intel"),
    FRAUD_INTEL("Cyber Fraud", "ALERT"),
    VULN_LAB("CVE & Scanner"),
    CTF("CTF Arena"),
    COMMUNITY("Cyber Hub"),
    AI_ADVISOR("AI Copilot"),
    SETTINGS("Settings")
}

data class AiMessage(
    val id: String = UUID.randomUUID().toString(),
    val isUser: Boolean,
    val text: String,
    val mode: AiCopilotMode = AiCopilotMode.GENERAL,
    val codeSnippets: List<ExtractedCodeSnippet> = emptyList(),
    val executableCommands: List<ExtractedCommand> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)

data class TargetScanState(
    val isScanning: Boolean = false,
    val progress: Float = 0f,
    val currentStepText: String = "",
    val target: String = "",
    val profile: String = "",
    val completedReport: ScanResultReport? = null
)

class CyberLabViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = CyberLabRepository(application)

    // Terminal ViewModel instance
    val terminalViewModel = TerminalViewModel(application, repository)

    // Current Navigation Tab
    private val _currentTab = MutableStateFlow(CyberTab.TERMINAL)
    val currentTab = _currentTab.asStateFlow()

    // Sound FX Mute state
    private val _isMuted = MutableStateFlow(false)
    val isMuted = _isMuted.asStateFlow()
    private val _apiKey = MutableStateFlow(com.example.data.local.SecurePreferencesManager.getApiKey("GEMINI_API_KEY") ?: "")
    val apiKey = _apiKey.asStateFlow()

    fun saveApiKey(key: String) {
        com.example.data.local.SecurePreferencesManager.saveApiKey("GEMINI_API_KEY", key)
        _apiKey.value = key
    }

    // Command Palette Dialog
    private val _showCommandPalette = MutableStateFlow(false)
    val showCommandPalette = _showCommandPalette.asStateFlow()

    // Theme state
    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme = _isDarkTheme.asStateFlow()

    // Terminal State
    val terminalLines = terminalViewModel.terminalLines
    val currentCommandInput = terminalViewModel.currentCommandInput

    private val commandHistoryList = mutableListOf<String>()
    private var historyIndex = -1

    // Tools State
    val tools = repository.tools
    private val _toolSearchQuery = MutableStateFlow("")
    val toolSearchQuery = _toolSearchQuery.asStateFlow()
    private val _selectedToolCategory = MutableStateFlow<ToolCategory?>(null)
    val selectedToolCategory = _selectedToolCategory.asStateFlow()
    private val _selectedToolDetail = MutableStateFlow<SecurityTool?>(null)
    val selectedToolDetail = _selectedToolDetail.asStateFlow()

    // Threat Intel State
    val threats = repository.threats
    private val _threatFilter = MutableStateFlow("ALL")
    val threatFilter = _threatFilter.asStateFlow()

    // CVE State
    val cves = repository.cves
    private val _cveSearchQuery = MutableStateFlow("")
    val cveSearchQuery = _cveSearchQuery.asStateFlow()
    private val _selectedCve = MutableStateFlow<CVEItem?>(null)
    val selectedCve = _selectedCve.asStateFlow()

    // Scanner State
    private val _scanState = MutableStateFlow(TargetScanState())
    val scanState = _scanState.asStateFlow()
    val savedReports = repository.getScanReports()
    private val _viewingReport = MutableStateFlow<ScanResultReport?>(null)
    val viewingReport = _viewingReport.asStateFlow()

    // CTF State
    val challenges = repository.challenges
    val userProfile = repository.userProfile
    private val _activeChallenge = MutableStateFlow<CTFChallenge?>(null)
    val activeChallenge = _activeChallenge.asStateFlow()
    private val _challengeFeedback = MutableStateFlow<String?>(null)
    val challengeFeedback = _challengeFeedback.asStateFlow()

    // Community Forum State
    val forumPosts = repository.getForumPosts()
    private val _showCreatePostDialog = MutableStateFlow(false)
    val showCreatePostDialog = _showCreatePostDialog.asStateFlow()

    // AI Advisor State
    private val _selectedAiMode = MutableStateFlow(AiCopilotMode.GENERAL)
    val selectedAiMode = _selectedAiMode.asStateFlow()

    private val _aiMessages = MutableStateFlow<List<AiMessage>>(
        listOf(
            AiMessage(
                isUser = false,
                text = "Greetings Operator. I am **CYBER_AI**, your cyber defense, penetration testing, and code development copilot.\n\n" +
                        "• **Code Developer**: Develop Python tools, Bash scripts, C exploits, YARA rules\n" +
                        "• **Command Builder**: Convert natural language intent into chained terminal pipelines\n" +
                        "• **Exploit & CTF**: Craft payloads, decode ciphers, solve challenge flags\n" +
                        "• **Vuln Review**: Scan code for CWE vulnerabilities & generate remediation patches\n\n" +
                        "Select a mode or tap a preset below to begin.",
                mode = AiCopilotMode.GENERAL
            )
        )
    )
    val aiMessages = _aiMessages.asStateFlow()
    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading = _isAiLoading.asStateFlow()

    fun setAiMode(mode: AiCopilotMode) {
        _selectedAiMode.value = mode
        CyberAudioEngine.playSound(CyberAudioEngine.SoundType.BUTTON_CLICK)
    }

    fun clearAiMessages() {
        _aiMessages.value = listOf(
            AiMessage(
                isUser = false,
                text = "CYBER_AI context cleared. Ready for new cyber operation in [${_selectedAiMode.value.title}] mode.",
                mode = _selectedAiMode.value
            )
        )
        CyberAudioEngine.playSound(CyberAudioEngine.SoundType.KEY_CLACK)
    }

    fun runCommandInTerminal(command: String) {
        _currentTab.value = CyberTab.TERMINAL
        terminalViewModel.executeTerminalCommand(command)
        CyberAudioEngine.playSound(CyberAudioEngine.SoundType.EXECUTE)
    }

    fun saveScriptToVfs(path: String, code: String): Boolean {
        val success = terminalViewModel.saveScriptToVfs(path, code)
        if (success) {
            CyberAudioEngine.playSound(CyberAudioEngine.SoundType.SUCCESS)
        } else {
            CyberAudioEngine.playSound(CyberAudioEngine.SoundType.ERROR)
        }
        return success
    }

    fun setTab(tab: CyberTab) {
        _currentTab.value = tab
        CyberAudioEngine.playSound(CyberAudioEngine.SoundType.TAB_SWITCH)
    }

    fun toggleSound() {
        _isMuted.value = !_isMuted.value
        CyberAudioEngine.isMuted = _isMuted.value
        if (!_isMuted.value) {
            CyberAudioEngine.playSound(CyberAudioEngine.SoundType.BUTTON_CLICK)
        }
    }

    fun toggleCommandPalette(show: Boolean) {
        _showCommandPalette.value = show
        if (show) CyberAudioEngine.playSound(CyberAudioEngine.SoundType.EXECUTE)
    }

    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
        CyberAudioEngine.playSound(CyberAudioEngine.SoundType.BUTTON_CLICK)
    }

    // Terminal Commands
    fun onCommandInputChange(text: String) {
        terminalViewModel.onCommandInputChange(text)
    }

    fun executeTerminalCommand(cmd: String? = null) {
        terminalViewModel.executeTerminalCommand(cmd)
    }

    fun navigateHistory(up: Boolean) {
        terminalViewModel.navigateHistory(up)
    }

    fun clearTerminal() {
        terminalViewModel.clearTerminal()
    }

    fun exportTerminalLogs(): String {
        return terminalViewModel.exportLogs()
    }

    fun runToolInTerminal(command: String) {
        setTab(CyberTab.TERMINAL)
        _selectedToolDetail.value = null
        executeTerminalCommand(command)
    }

    // Tools Actions
    fun setToolSearchQuery(q: String) {
        _toolSearchQuery.value = q
    }

    fun setToolCategory(cat: ToolCategory?) {
        _selectedToolCategory.value = cat
        CyberAudioEngine.playSound(CyberAudioEngine.SoundType.KEY_CLACK)
    }

    fun selectToolDetail(tool: SecurityTool?) {
        _selectedToolDetail.value = tool
        if (tool != null) CyberAudioEngine.playSound(CyberAudioEngine.SoundType.KEY_CLACK)
    }

    fun toggleBookmark(toolId: String) {
        repository.toggleToolBookmark(toolId)
        CyberAudioEngine.playSound(CyberAudioEngine.SoundType.SUCCESS)
    }

    // Threat Feed
    fun setThreatFilter(filter: String) {
        _threatFilter.value = filter
        CyberAudioEngine.playSound(CyberAudioEngine.SoundType.KEY_CLACK)
    }

    // CVE Explorer
    fun setCveSearchQuery(q: String) {
        _cveSearchQuery.value = q
    }

    fun selectCve(cve: CVEItem?) {
        _selectedCve.value = cve
        if (cve != null) CyberAudioEngine.playSound(CyberAudioEngine.SoundType.KEY_CLACK)
    }

    // Vulnerability Scanner
    private var scanJob: Job? = null
    fun startTargetScan(target: String, profile: String) {
        scanJob?.cancel()
        _scanState.value = TargetScanState(
            isScanning = true,
            progress = 0.05f,
            currentStepText = "Initializing stealth probes against $target...",
            target = target,
            profile = profile
        )
        CyberAudioEngine.playSound(CyberAudioEngine.SoundType.SCAN_PING)

        scanJob = viewModelScope.launch {
            val steps = listOf(
                Pair(0.20f, "Resolving DNS & Host routing vectors..."),
                Pair(0.40f, "Executing TCP/UDP SYN port matrix inspection..."),
                Pair(0.65f, "Fingerprinting service banners and SSL certificates..."),
                Pair(0.85f, "Cross-referencing 200,000+ CVE database signatures..."),
                Pair(0.95f, "Synthesizing executive vulnerability audit findings..."),
                Pair(1.0f, "Audit complete!")
            )

            for ((prog, text) in steps) {
                delay(700)
                _scanState.value = _scanState.value.copy(
                    progress = prog,
                    currentStepText = text
                )
                CyberAudioEngine.playSound(CyberAudioEngine.SoundType.SCAN_PING)
            }

            // Generate Simulated Report
            val findings = listOf(
                ScanFinding(
                    title = "Remote Code Execution via Outdated OpenSSH / liblzma",
                    severity = RiskLevel.CRITICAL,
                    portOrPath = "Port 22/TCP",
                    description = "Detected vulnerable OpenSSH 8.9p1 linked against untrusted liblzma 5.6.0 with potential backdoor hooks.",
                    remediation = "Downgrade xz-utils to 5.4.6 or patch to OpenSSH 9.7p1 immediately."
                ),
                ScanFinding(
                    title = "Public Database Management Interface Exposed",
                    severity = RiskLevel.HIGH,
                    portOrPath = "Port 3306/TCP (MySQL)",
                    description = "MySQL database listener is binding on 0.0.0.0 allowing unrestricted remote connection attempts.",
                    remediation = "Bind MySQL strictly to 127.0.0.1 and configure iptables firewall rules."
                ),
                ScanFinding(
                    title = "Missing Anti-Clickjacking X-Frame-Options Header",
                    severity = RiskLevel.MEDIUM,
                    portOrPath = "Port 80/443 (HTTP)",
                    description = "Web server does not enforce X-Frame-Options: DENY, rendering user sessions vulnerable to UI redress attacks.",
                    remediation = "Add 'Header always set X-Frame-Options SAMEORIGIN' in Apache / Nginx config."
                ),
                ScanFinding(
                    title = "Anonymous FTP Login Permitted",
                    severity = RiskLevel.LOW,
                    portOrPath = "Port 21/TCP (vsftpd)",
                    description = "FTP server allows login using username 'anonymous' without password verification.",
                    remediation = "Set anonymous_enable=NO in /etc/vsftpd.conf."
                )
            )

            val report = ScanResultReport(
                id = "REP-" + UUID.randomUUID().toString().take(8).uppercase(),
                target = target,
                profile = profile,
                scanTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date()),
                totalVulnerabilities = 4,
                criticalCount = 1,
                highCount = 1,
                mediumCount = 1,
                lowCount = 1,
                overallRiskScore = 84.5f,
                findings = findings,
                executiveSummary = "Target $target exhibits a CRITICAL overall risk score of 84.5/100 due to exposed administrative databases and remote code execution vulnerabilities in SSH/OpenSSH services."
            )

            _scanState.value = _scanState.value.copy(
                isScanning = false,
                completedReport = report
            )
            repository.saveScanReport(report)
            CyberAudioEngine.playSound(CyberAudioEngine.SoundType.ALERT_SIREN)
        }
    }

    fun viewReport(report: ScanResultReport?) {
        _viewingReport.value = report
        if (report != null) CyberAudioEngine.playSound(CyberAudioEngine.SoundType.KEY_CLACK)
    }

    fun deleteReport(reportId: String) {
        repository.deleteScanReport(reportId)
    }

    // CTF Challenges
    fun selectChallenge(ch: CTFChallenge?) {
        _activeChallenge.value = ch
        _challengeFeedback.value = null
        if (ch != null) CyberAudioEngine.playSound(CyberAudioEngine.SoundType.KEY_CLACK)
    }

    fun submitChallengeAnswer(optionIndex: Int? = null, flagText: String? = null) {
        val ch = _activeChallenge.value ?: return

        var isCorrect = false
        if (ch.options != null && optionIndex != null) {
            isCorrect = (optionIndex == ch.correctOptionIndex)
        } else if (ch.flagAnswer != null && flagText != null) {
            isCorrect = (flagText.trim().equals(ch.flagAnswer.trim(), ignoreCase = true))
        }

        if (isCorrect) {
            _challengeFeedback.value = "SUCCESS: Correct! Flag verified. +${ch.points} XP awarded."
            CyberAudioEngine.playSound(CyberAudioEngine.SoundType.CTF_FLAG_SOLVED)
            repository.awardXp(ch.points, ch.id)
        } else {
            _challengeFeedback.value = "FAILED: Incorrect answer or flag token. Check the hint and retry."
            CyberAudioEngine.playSound(CyberAudioEngine.SoundType.ERROR)
        }
    }

    // Community Forum
    fun setCreatePostDialog(show: Boolean) {
        _showCreatePostDialog.value = show
        if (show) CyberAudioEngine.playSound(CyberAudioEngine.SoundType.KEY_CLACK)
    }

    fun createPost(title: String, category: String, content: String, code: String?) {
        repository.createForumPost(title, category, content, code)
        _showCreatePostDialog.value = false
        CyberAudioEngine.playSound(CyberAudioEngine.SoundType.SUCCESS)
    }

    fun upvotePost(post: ForumPost) {
        repository.upvotePost(post)
        CyberAudioEngine.playSound(CyberAudioEngine.SoundType.KEY_CLACK)
    }

    // AI Advisor
    fun sendAiPrompt(prompt: String, explicitMode: AiCopilotMode? = null) {
        if (prompt.isBlank() || _isAiLoading.value) return

        val targetMode = explicitMode ?: _selectedAiMode.value
        val userMsg = AiMessage(
            isUser = true,
            text = prompt,
            mode = targetMode
        )
        _aiMessages.value = _aiMessages.value + userMsg
        _isAiLoading.value = true
        CyberAudioEngine.playSound(CyberAudioEngine.SoundType.EXECUTE)

        viewModelScope.launch {
            // Build history for multi-turn Gemini conversation
            val history = _aiMessages.value.dropLast(1).map { msg ->
                GeminiContent(
                    role = if (msg.isUser) "user" else "model",
                    parts = listOf(GeminiPart(text = msg.text))
                )
            }

            val parsedResponse = CyberAiService.askCyberAi(
                prompt = prompt,
                mode = targetMode,
                conversationHistory = history
            )

            val aiMsg = AiMessage(
                isUser = false,
                text = parsedResponse.fullText,
                mode = targetMode,
                codeSnippets = parsedResponse.codeSnippets,
                executableCommands = parsedResponse.executableCommands
            )

            _aiMessages.value = _aiMessages.value + aiMsg
            _isAiLoading.value = false
            CyberAudioEngine.playSound(CyberAudioEngine.SoundType.SUCCESS)
        }
    }
}
