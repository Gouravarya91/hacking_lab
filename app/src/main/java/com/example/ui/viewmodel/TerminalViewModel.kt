package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.CyberAudioEngine
import com.example.data.model.TerminalLine
import com.example.data.repository.CyberLabRepository
import com.example.data.sandbox.TerminalSandboxEngine
import com.example.data.sandbox.VirtualFileSystem
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class QuickCommand(
    val label: String,
    val command: String,
    val category: String,
    val description: String
)

class TerminalViewModel(
    application: Application,
    private val repository: CyberLabRepository = CyberLabRepository(application)
) : AndroidViewModel(application) {

    private val sandboxEngine = TerminalSandboxEngine(
        onXpAwarded = { points, flag ->
            repository.awardXp(points, flag)
        }
    )

    private val _terminalLines = MutableStateFlow<List<TerminalLine>>(emptyList())
    val terminalLines = _terminalLines.asStateFlow()

    private val _currentCommandInput = MutableStateFlow("")
    val currentCommandInput = _currentCommandInput.asStateFlow()

    private val _promptString = MutableStateFlow(sandboxEngine.getPrompt())
    val promptString = _promptString.asStateFlow()

    private val _currentWorkingDirectory = MutableStateFlow(sandboxEngine.vfs.currentPath)
    val currentWorkingDirectory = _currentWorkingDirectory.asStateFlow()

    private val _autocompleteSuggestions = MutableStateFlow<List<String>>(emptyList())
    val autocompleteSuggestions = _autocompleteSuggestions.asStateFlow()

    private val _isExecuting = MutableStateFlow(false)
    val isExecuting = _isExecuting.asStateFlow()

    private val _selectedCategory = MutableStateFlow("ALL")
    val selectedCategory = _selectedCategory.asStateFlow()

    private val commandHistoryList = mutableListOf<String>()
    private var historyIndex = -1

    val quickCommands = listOf(
        // Recon & Discovery
        QuickCommand("NMAP SYN", "nmap -sS -T4 10.10.10.50", "RECON", "Stealth TCP port scan"),
        QuickCommand("NMAP VULN", "nmap -sV --script vuln 10.10.10.50", "RECON", "Vulnerability discovery scan"),
        QuickCommand("GOBUSTER", "gobuster dir -u http://10.10.10.50/ -w /opt/wordlists/common.txt", "WEB", "Web route brute-forcing"),
        QuickCommand("NIKTO", "nikto -h 10.10.10.50", "WEB", "Web server misconfiguration scan"),
        QuickCommand("SQLMAP", "sqlmap -u \"http://10.10.10.50/item.php?id=1\" --batch", "WEB", "Automated SQL injection audit"),
        QuickCommand("HYDRA SSH", "hydra -l root -P /home/operator/passwords.txt 10.10.10.50 ssh", "EXPLOIT", "SSH credential brute-force"),
        QuickCommand("JOHN CRACK", "john /etc/shadow", "EXPLOIT", "Password hash recovery"),
        QuickCommand("HASHCAT", "hashcat -m 1000 32ed87b2490fedba7556e1b12f020bc5", "EXPLOIT", "GPU NTLM hash cracking"),
        QuickCommand("METASPLOIT", "msfconsole", "EXPLOIT", "Metasploit exploit session"),
        QuickCommand("PROWLER AWS", "prowler aws --compliance cis_2.0_aws", "CLOUD", "AWS CIS Benchmark audit"),
        QuickCommand("TRIVY", "trivy image alpine:3.18", "CLOUD", "Container CVE scanner"),
        QuickCommand("AUTH LOGS", "cat /var/log/auth.log", "FORENSICS", "Triage SSH auth events"),
        QuickCommand("NETSTAT", "netstat -tulnp", "SYSTEM", "List active listening ports"),
        QuickCommand("PS AUX", "ps aux", "SYSTEM", "View active running processes"),
        QuickCommand("LS -LA", "ls -la", "SYSTEM", "List files with permissions"),
        QuickCommand("NEOFETCH", "neofetch", "SYSTEM", "Show system hacker telemetry"),
        QuickCommand("AI COPILOT", "ai explain CVE-2024-3094 backdoor", "AI", "Query AI threat intelligence"),
        QuickCommand("SUBMIT FLAG", "submit FLAG{x0r_c1phers_4re_e4sy_t0_br3ak}", "CTF", "Submit CTF flag in terminal")
    )

    init {
        insertBanner()
    }

    private fun insertBanner() {
        val banner = TerminalLine(
            id = UUID.randomUUID().toString(),
            type = TerminalLine.LineType.BANNER,
            content = """
                ==================================================================
                [+] CYBER_LAB_PRO SANDBOX CLI v3.4 (x86_64-hardened-linux)
                [+] KERNEL: 6.9.12-cyberlab-custom | NODE: cyberlab-node-01
                [+] DEFENSE STATUS: ARMED | IDS/IPS: ACTIVE | AUDIT LOG: ON
                ==================================================================
                Type 'help' for command reference, or tap quick touchbar actions.
            """.trimIndent()
        )
        _terminalLines.value = listOf(banner)
    }

    fun onCommandInputChange(text: String) {
        if (text != _currentCommandInput.value && text.isNotEmpty()) {
            CyberAudioEngine.playSound(CyberAudioEngine.SoundType.KEY_CLACK)
        }
        _currentCommandInput.value = text
        _autocompleteSuggestions.value = sandboxEngine.getCompletions(text)
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
        CyberAudioEngine.playSound(CyberAudioEngine.SoundType.BUTTON_CLICK)
    }

    fun applyAutocomplete(completion: String) {
        val current = _currentCommandInput.value
        val isTrailingSpace = current.endsWith(" ")
        val tokens = current.trimEnd().split("\\s+".toRegex())
        
        val updated = if (isTrailingSpace) {
            // They typed a space, meaning the last token is complete, append directly
            "$current$completion "
        } else {
            // Replace the last partially typed token
            if (tokens.size <= 1) {
                "$completion "
            } else {
                val prefix = tokens.dropLast(1).joinToString(" ")
                "$prefix $completion "
            }
        }
        _currentCommandInput.value = updated
        _autocompleteSuggestions.value = emptyList()
        CyberAudioEngine.playSound(CyberAudioEngine.SoundType.KEY_CLACK)
    }

    fun executeTerminalCommand(cmd: String? = null) {
        val toRun = cmd ?: _currentCommandInput.value
        val trimmed = toRun.trim()
        if (trimmed.isEmpty()) return

        commandHistoryList.add(trimmed)
        historyIndex = commandHistoryList.size

        val inputLine = TerminalLine(
            id = UUID.randomUUID().toString(),
            type = TerminalLine.LineType.INPUT,
            content = "${sandboxEngine.getPrompt()} $trimmed"
        )

        val newLines = _terminalLines.value + inputLine
        _terminalLines.value = newLines.takeLast(1000)
        _currentCommandInput.value = ""
        _autocompleteSuggestions.value = emptyList()
        CyberAudioEngine.playSound(CyberAudioEngine.SoundType.EXECUTE)

        viewModelScope.launch {
            _isExecuting.value = true

            // Provide realistic simulated execution delay for tools
            val commandName = trimmed.split("\\s+".toRegex()).first().lowercase()
            val simulatedDelay = when (commandName) {
                "nmap", "sqlmap", "hydra", "gobuster", "prowler", "trivy" -> 400L
                "hashcat", "john", "msfconsole", "ai", "cyber-ai" -> 350L
                else -> 50L
            }
            delay(simulatedDelay)

            val outputLines = sandboxEngine.processCommand(trimmed)

            if (outputLines.any { it.content == "__CLEAR__" }) {
                _terminalLines.value = emptyList()
            } else {
                val newOutputLines = _terminalLines.value + outputLines
                _terminalLines.value = newOutputLines.takeLast(1000)

                // Trigger distinct sound effect based on command result
                val hasError = outputLines.any { it.type == TerminalLine.LineType.ERROR }
                val hasSuccess = outputLines.any { it.type == TerminalLine.LineType.SUCCESS }
                val hasCtfSolved = outputLines.any { it.content.contains("FLAG CAPTURED", ignoreCase = true) || it.content.contains("FLAG SUBMITTED", ignoreCase = true) }

                when {
                    hasCtfSolved -> CyberAudioEngine.playSound(CyberAudioEngine.SoundType.CTF_FLAG_SOLVED)
                    commandName == "cmatrix" || commandName == "matrix" -> CyberAudioEngine.playSound(CyberAudioEngine.SoundType.MATRIX_CASCADE)
                    commandName == "ai" || commandName == "cyber-ai" -> CyberAudioEngine.playSound(CyberAudioEngine.SoundType.AI_CHIRP)
                    hasError -> CyberAudioEngine.playSound(CyberAudioEngine.SoundType.ERROR)
                    hasSuccess -> CyberAudioEngine.playSound(CyberAudioEngine.SoundType.SUCCESS)
                }
            }

            _promptString.value = sandboxEngine.getPrompt()
            _currentWorkingDirectory.value = sandboxEngine.vfs.currentPath
            _isExecuting.value = false
        }
    }

    fun navigateHistory(up: Boolean) {
        if (commandHistoryList.isEmpty()) return
        if (up) {
            if (historyIndex > 0) {
                historyIndex--
                _currentCommandInput.value = commandHistoryList[historyIndex]
            }
        } else {
            if (historyIndex < commandHistoryList.size - 1) {
                historyIndex++
                _currentCommandInput.value = commandHistoryList[historyIndex]
            } else {
                historyIndex = commandHistoryList.size
                _currentCommandInput.value = ""
            }
        }
        CyberAudioEngine.playSound(CyberAudioEngine.SoundType.KEY_CLACK)
    }

    fun clearTerminal() {
        _terminalLines.value = emptyList()
        CyberAudioEngine.playSound(CyberAudioEngine.SoundType.KEY_CLACK)
    }

    val vfs: VirtualFileSystem
        get() = sandboxEngine.vfs

    fun saveScriptToVfs(path: String, code: String): Boolean {
        return sandboxEngine.vfs.writeFile(path, code)
    }

    fun exportTerminalLogs(): String {
        return _terminalLines.value.joinToString("\n") { line ->
            val time = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date(line.timestamp))
            "[$time] ${line.content}"
        }
    }

    fun exportLogs(): String = exportTerminalLogs()
}
