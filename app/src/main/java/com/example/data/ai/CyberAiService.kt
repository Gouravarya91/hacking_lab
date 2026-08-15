package com.example.data.ai

import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GeminiPart(
    @Json(name = "text") val text: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    @Json(name = "role") val role: String? = "user",
    @Json(name = "parts") val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiGenerationConfig(
    @Json(name = "temperature") val temperature: Float? = 0.7f,
    @Json(name = "topP") val topP: Float? = 0.95f,
    @Json(name = "topK") val topK: Int? = 40
)

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    @Json(name = "contents") val contents: List<GeminiContent>,
    @Json(name = "systemInstruction") val systemInstruction: GeminiContent? = null,
    @Json(name = "generationConfig") val generationConfig: GeminiGenerationConfig? = null
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    @Json(name = "content") val content: GeminiContent?
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    @Json(name = "candidates") val candidates: List<GeminiCandidate>?
)

interface GeminiApi {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

enum class AiCopilotMode(val title: String, val badge: String, val description: String) {
    GENERAL("General Intel", "INTEL", "Cybersecurity advisory, MITRE ATT&CK analysis & incident triage"),
    CODE_DEVELOPER("Code Developer", "DEV", "Develops complete Python, Bash, C, Go, YARA & SQL scripts"),
    COMMAND_BUILDER("Command Builder", "CLI", "Translates natural language tasks into chained terminal pipelines"),
    EXPLOIT_LAB("Exploit & CTF", "EXPLOIT", "Payload crafting, CTF solvers, crypto decoders & reverse engineering"),
    VULN_PATCH("Vuln Review", "PATCH", "Source code vulnerability review (CWE) & secure remediation patches")
}

data class ExtractedCodeSnippet(
    val language: String,
    val code: String,
    val suggestedFileName: String
)

data class ExtractedCommand(
    val command: String,
    val explanation: String
)

data class ParsedAiResponse(
    val fullText: String,
    val codeSnippets: List<ExtractedCodeSnippet>,
    val executableCommands: List<ExtractedCommand>
)

object CyberAiService {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    // 60-second timeouts as required for Gemini API
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val api: GeminiApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApi::class.java)
    }

    private fun getSystemInstruction(mode: AiCopilotMode): GeminiContent {
        val base = "You are CYBER_AI, an elite cybersecurity engineer, penetration tester, and exploit/defense developer integrated into CYBER_LAB_PRO. "
        val modeDirective = when (mode) {
            AiCopilotMode.CODE_DEVELOPER ->
                "Focus on writing clean, production-ready, highly effective code (Python 3, Bash, C, Go, YARA rules, Dockerfile, SQL). " +
                "Always place filename in code block comment (e.g. ```python\n# filename: /root/scanner.py\n...). " +
                "Provide detailed code explanations, dependencies, and execution instructions."

            AiCopilotMode.COMMAND_BUILDER ->
                "Focus on crafting precise Linux, penetration testing, and security terminal command pipelines (using nmap, sqlmap, hydra, curl, grep, awk, sed, iptables, trivy, prowler, openssl, tcpdump). " +
                "Highlight every single command in code blocks with a brief explanation of each flag and pipe."

            AiCopilotMode.EXPLOIT_LAB ->
                "Focus on educational proof-of-concepts, CTF challenge solving (Base64/XOR decoding, steganography, regex extraction, buffer overflow offset calculations, SQLi payloads). " +
                "Provide step-by-step solving code scripts and verify flags."

            AiCopilotMode.VULN_PATCH ->
                "Analyze source code for security flaws (CWE, OWASP Top 10, memory corruption, logic bugs). " +
                "Provide the vulnerable code snippet followed immediately by the secure fixed code with detailed remediation guidance."

            AiCopilotMode.GENERAL ->
                "Provide deep technical advisory, incident response triage, network defense architectures, forensic analysis of logs, and MITRE ATT&CK mapping."
        }

        return GeminiContent(
            role = "system",
            parts = listOf(GeminiPart(text = "$base $modeDirective Format output cleanly with markdown headers, concise explanations, and code blocks."))
        )
    }

    suspend fun askCyberAi(
        prompt: String,
        mode: AiCopilotMode = AiCopilotMode.GENERAL,
        conversationHistory: List<GeminiContent> = emptyList()
    ): ParsedAiResponse = withContext(Dispatchers.IO) {
        val apiKey = try {
            val secureKey = com.example.data.local.SecurePreferencesManager.getApiKey("GEMINI_API_KEY")
            if (!secureKey.isNullOrBlank()) secureKey else 
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        var rawResponseText: String? = null

        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val fullContents = mutableListOf<GeminiContent>()
                fullContents.addAll(conversationHistory.takeLast(10)) // Send last 10 turns for context
                fullContents.add(
                    GeminiContent(
                        role = "user",
                        parts = listOf(GeminiPart(text = prompt))
                    )
                )

                val request = GeminiRequest(
                    contents = fullContents,
                    systemInstruction = getSystemInstruction(mode),
                    generationConfig = GeminiGenerationConfig(
                        temperature = if (mode == AiCopilotMode.CODE_DEVELOPER || mode == AiCopilotMode.COMMAND_BUILDER) 0.3f else 0.7f
                    )
                )

                val response = api.generateContent(apiKey = apiKey, request = request)
                rawResponseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            } catch (e: Exception) {
                // Fall back to offline cyber intelligence
            }
        }

        val finalText = if (!rawResponseText.isNullOrBlank()) {
            rawResponseText
        } else {
            generateOfflineCyberResponse(prompt, mode)
        }

        parseAiResponse(finalText, mode)
    }

    fun parseAiResponse(rawText: String, mode: AiCopilotMode): ParsedAiResponse {
        val codeSnippets = mutableListOf<ExtractedCodeSnippet>()
        val executableCommands = mutableListOf<ExtractedCommand>()

        // Extract code blocks with regex ```(language)?\n(content)```
        val codeBlockRegex = "```([a-zA-Z0-9_-]*)\n([\\s\\S]*?)```".toRegex()
        val matches = codeBlockRegex.findAll(rawText)

        for (match in matches) {
            val lang = match.groupValues[1].lowercase().trim().ifEmpty { "bash" }
            val code = match.groupValues[2].trim()

            // Look for suggested filename in comments
            val fileNameRegex = "(?:#|//|/\\*)\\s*(?:filename|file|save as):?\\s*([a-zA-Z0-9_./-]+)".toRegex(RegexOption.IGNORE_CASE)
            val fileNameMatch = fileNameRegex.find(code)

            val suggestedFileName = fileNameMatch?.groupValues?.get(1)?.trim() ?: when (lang) {
                "python", "py" -> "/root/ai_script.py"
                "bash", "sh" -> "/root/ai_tool.sh"
                "c", "cpp" -> "/root/exploit_poc.c"
                "yara", "yar" -> "/root/rules.yara"
                "sql" -> "/root/query.sql"
                "json" -> "/root/config.json"
                "yaml", "yml" -> "/root/pipeline.yml"
                else -> "/root/script.sh"
            }

            codeSnippets.add(
                ExtractedCodeSnippet(
                    language = lang,
                    code = code,
                    suggestedFileName = suggestedFileName
                )
            )

            // If it's a bash/sh block or short command block, extract individual lines as runnable commands
            if (lang in listOf("bash", "sh", "shell", "terminal", "zsh")) {
                val lines = code.lines()
                for (line in lines) {
                    val cleanLine = line.trim()
                    if (cleanLine.isNotEmpty() && !cleanLine.startsWith("#") && !cleanLine.startsWith("//")) {
                        val cmdName = cleanLine.split("\\s+".toRegex()).firstOrNull() ?: ""
                        if (isRecognizedCyberCommand(cmdName)) {
                            executableCommands.add(
                                ExtractedCommand(
                                    command = cleanLine,
                                    explanation = "Command from $suggestedFileName"
                                )
                            )
                        }
                    }
                }
            }
        }

        // If no commands extracted from code blocks, look for standalone command lines
        if (executableCommands.isEmpty()) {
            val commandPatterns = listOf(
                "nmap", "sqlmap", "hydra", "gobuster", "nikto", "john", "hashcat",
                "curl", "prowler", "trivy", "sherlock", "theharvester", "grep",
                "cat", "ls", "cd", "python", "python3", "whois", "traceroute", "ping", "cmatrix"
            )
            for (pattern in commandPatterns) {
                val regex = "(?:`|\\\$ )($pattern [^`\n\r]+)".toRegex()
                for (m in regex.findAll(rawText)) {
                    val cmd = m.groupValues[1].trim()
                    if (cmd.isNotEmpty() && executableCommands.none { it.command == cmd }) {
                        executableCommands.add(ExtractedCommand(command = cmd, explanation = "Extracted CLI task"))
                    }
                }
            }
        }

        return ParsedAiResponse(
            fullText = rawText,
            codeSnippets = codeSnippets,
            executableCommands = executableCommands
        )
    }

    private fun isRecognizedCyberCommand(cmd: String): Boolean {
        val recognized = setOf(
            "nmap", "sqlmap", "hydra", "gobuster", "nikto", "john", "hashcat",
            "curl", "wget", "prowler", "trivy", "sherlock", "theharvester", "whois",
            "traceroute", "ping", "ps", "netstat", "ss", "iptables", "whoami", "id",
            "uname", "env", "history", "clear", "neofetch", "cmatrix", "ai", "submit",
            "cat", "ls", "cd", "pwd", "mkdir", "touch", "rm", "grep", "python", "python3",
            "bash", "sh", "chmod", "gcc", "msfconsole", "nc"
        )
        return recognized.contains(cmd.lowercase())
    }

    private fun generateOfflineCyberResponse(prompt: String, mode: AiCopilotMode): String {
        val lower = prompt.lowercase()

        return when {
            // Mode-specific specialized generations
            mode == AiCopilotMode.CODE_DEVELOPER || "python" in lower || "script" in lower || "develop" in lower || "code" in lower -> {
                when {
                    "port" in lower || "scan" in lower -> """
                        ### [CODE DEV] Multi-Threaded TCP Port Scanner (Python 3)
                        
                        This high-performance socket scanner inspects open network services with thread pool concurrency.
                        
                        ```python
                        # filename: /root/port_scanner.py
                        import socket
                        from concurrent.futures import ThreadPoolExecutor
                        import sys
                        import time
                        
                        TARGET_HOST = "10.10.10.50"
                        COMMON_PORTS = [21, 22, 25, 53, 80, 110, 143, 443, 3306, 8080, 8443]
                        
                        def scan_port(host, port):
                            try:
                                s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
                                s.settimeout(0.6)
                                result = s.connect_ex((host, port))
                                if result == 0:
                                    service = socket.getservbyport(port, 'tcp') if port < 1024 else "custom"
                                    print(f"  [+] Port {port:5d}/tcp OPEN  ({service})")
                                s.close()
                            except Exception:
                                pass
                        
                        def main():
                            target = sys.argv[1] if len(sys.argv) > 1 else TARGET_HOST
                            print(f"[*] Starting TCP Banner Probe against {target}...")
                            start_time = time.time()
                            
                            with ThreadPoolExecutor(max_workers=50) as executor:
                                for port in COMMON_PORTS:
                                    executor.submit(scan_port, target, port)
                                    
                            elapsed = time.time() - start_time
                            print(f"[+] Scan completed in {elapsed:.2f} seconds.")
                        
                        if __name__ == "__main__":
                            main()
                        ```
                        
                        **Execution in Sandbox:**
                        ```bash
                        python3 /root/port_scanner.py 10.10.10.50
                        ```
                    """.trimIndent()

                    "xor" in lower || "crypto" in lower || "decoder" in lower || "ctf" in lower -> """
                        ### [CODE DEV] Automated Base64 & XOR Cipher Decoder
                        
                        Solves CTF cryptographic challenges by stripping Base64 encoding and brute-forcing 1-byte XOR keys.
                        
                        ```python
                        # filename: /root/ctf_xor_solver.py
                        import base64
                        
                        CIPHERTEXT_B64 = "RkxBR3t4b3JfY2lwaGVyc19hcmVfZWFzeV90b19icmVha30="
                        
                        def solve_ctf_cipher():
                            # Step 1: Decode Base64
                            raw_bytes = base64.b64decode(CIPHERTEXT_B64)
                            print(f"[*] Decoded {len(raw_bytes)} bytes from Base64")
                            
                            # Step 2: Test plain output
                            decoded_str = raw_bytes.decode('utf-8', errors='ignore')
                            print(f"[+] Extracted Token: {decoded_str}")
                            
                            if "FLAG{" in decoded_str:
                                print(f"[SUCCESS] Flag found: {decoded_str}")
                                return decoded_str
                                
                            # Step 3: Brute force single-byte XOR key 0x00 to 0xFF
                            print("[*] Brute-forcing 256 XOR keys...")
                            for key in range(256):
                                xored = bytes([b ^ key for b in raw_bytes])
                                text = xored.decode('utf-8', errors='ignore')
                                if "FLAG{" in text:
                                    print(f"[+] Found Key 0x{key:02X}: {text}")
                                    return text
                        
                        if __name__ == "__main__":
                            solve_ctf_cipher()
                        ```
                        
                        **Execution in Sandbox:**
                        ```bash
                        python3 /root/ctf_xor_solver.py
                        ```
                    """.trimIndent()

                    "log" in lower || "triage" in lower || "forensic" in lower -> """
                        ### [CODE DEV] Linux Auth Log Forensic Analyzer (Bash)
                        
                        Parses `/var/log/auth.log` to identify automated SSH brute force attacks and malicious IP addresses.
                        
                        ```bash
                        # filename: /root/auth_triage.sh
                        #!/bin/bash
                        LOG_FILE="/var/log/auth.log"
                        
                        echo "=================================================="
                        echo "[*] LINUX AUTH.LOG INCIDENT RESPONSE TRIAGE"
                        echo "=================================================="
                        
                        if [ ! -f "${'$'}LOG_FILE" ]; then
                            echo "[-] Error: ${'$'}LOG_FILE not found."
                            exit 1
                        fi
                        
                        echo -e "\n[+] Top 5 Attacking IP Addresses (Failed Passwords):"
                        grep "Failed password" "${'$'}LOG_FILE" | awk '{print ${'$'}11}' | sort | uniq -c | sort -nr | head -5
                        
                        echo -e "\n[+] Targeted Usernames:"
                        grep "Failed password" "${'$'}LOG_FILE" | awk '{for(i=1;i<=NF;i++) if(${'$'}i=="user") print ${'$'}(i+1)}' | sort | uniq -c | sort -nr
                        
                        echo -e "\n[+] Sudo Privilege Escalation Events:"
                        grep "sudo:" "${'$'}LOG_FILE" | tail -3
                        
                        echo "=================================================="
                        ```
                        
                        **Execution in Sandbox:**
                        ```bash
                        bash /root/auth_triage.sh
                        ```
                    """.trimIndent()

                    "yara" in lower || "rule" in lower -> """
                        ### [CODE DEV] YARA Malware Detection Rule
                        
                        Detects obfuscated PHP webshell backdoors and malicious eval hooks.
                        
                        ```yara
                        # filename: /root/webshell_detector.yara
                        rule Malicious_PHP_Webshell_Backdoor {
                            meta:
                                author = "CYBER_LAB_PRO AI"
                                description = "Detects hidden PHP webshells and command execution payloads"
                                severity = "HIGH"
                                date = "2026-08-13"
                            strings:
                                ${'$'}tag = "<?php" nocase
                                ${'$'}eval1 = "eval(base64_decode(" nocase
                                ${'$'}eval2 = "assert(" nocase
                                ${'$'}exec1 = "passthru(" nocase
                                ${'$'}exec2 = "system(" nocase
                                ${'$'}exec3 = "shell_exec(" nocase
                                ${'$'}input1 = "${'$'}_POST"
                                ${'$'}input2 = "${'$'}_GET"
                                ${'$'}input3 = "${'$'}_REQUEST"
                            condition:
                                ${'$'}tag at 0 and (any of (${'$'}eval*) or any of (${'$'}exec*)) and any of (${'$'}input*)
                        }
                        ```
                    """.trimIndent()

                    else -> """
                        ### [CODE DEV] Security Automation Utility (Python 3)
                        
                        ```python
                        # filename: /root/cyber_util.py
                        import os
                        import sys
                        import hashlib
                        
                        def calculate_file_hash(filepath):
                            sha256 = hashlib.sha256()
                            with open(filepath, "rb") as f:
                                while chunk := f.read(4096):
                                    sha256.update(chunk)
                            return sha256.hexdigest()
                        
                        def main():
                            target_dir = "/etc"
                            print(f"[*] Auditing critical config hashes in {target_dir}...")
                            for root, _, files in os.walk(target_dir):
                                for file in files[:5]:
                                    path = os.path.join(root, file)
                                    try:
                                        print(f"  {file:20s} -> {calculate_file_hash(path)[:16]}...")
                                    except Exception:
                                        pass
                        
                        if __name__ == "__main__":
                            main()
                        ```
                        
                        **Execution in Sandbox:**
                        ```bash
                        python3 /root/cyber_util.py
                        ```
                    """.trimIndent()
                }
            }

            mode == AiCopilotMode.COMMAND_BUILDER || "command" in lower || "nmap" in lower || "sqlmap" in lower || "hydra" in lower -> {
                when {
                    "recon" in lower || "nmap" in lower || "scan" in lower -> """
                        ### [COMMAND CRAFTER] Full-Spectrum Reconnaissance Pipeline
                        
                        **1. Stealth TCP SYN + Version & Vulnerability Scan:**
                        ```bash
                        nmap -sS -sV -O -p- --min-rate 1000 -T4 10.10.10.50
                        ```
                        
                        **2. Web Directory Discovery with Gobuster:**
                        ```bash
                        gobuster dir -u http://10.10.10.50/ -w /opt/wordlists/common.txt -t 25 -x php,txt,bak
                        ```
                        
                        **3. Web Server Misconfiguration & CGI Audit:**
                        ```bash
                        nikto -h 10.10.10.50 -Tuning 123b
                        ```
                    """.trimIndent()

                    "crack" in lower || "hydra" in lower || "brute" in lower -> """
                        ### [COMMAND CRAFTER] Automated Authentication Brute-Force
                        
                        **1. SSH Credential Attack:**
                        ```bash
                        hydra -l root -P /home/operator/passwords.txt ssh://10.10.10.50 -t 4 -vV
                        ```
                        
                        **2. FTP Service Dictionary Probe:**
                        ```bash
                        hydra -L /etc/passwd -P /home/operator/passwords.txt ftp://10.10.10.50
                        ```
                        
                        **3. Shadow Hash Cracking with John The Ripper:**
                        ```bash
                        john --wordlist=/home/operator/passwords.txt /etc/shadow
                        ```
                    """.trimIndent()

                    "cloud" in lower || "aws" in lower || "trivy" in lower || "prowler" in lower -> """
                        ### [COMMAND CRAFTER] Cloud & Container Security Audit
                        
                        **1. AWS CIS Benchmark Compliance Scan:**
                        ```bash
                        prowler aws --compliance cis_2.0_aws --severity critical high
                        ```
                        
                        **2. Container Image CVE Vulnerability Audit:**
                        ```bash
                        trivy image nginx:alpine --severity HIGH,CRITICAL
                        ```
                    """.trimIndent()

                    else -> """
                        ### [COMMAND CRAFTER] Essential Linux Security Diagnostics
                        
                        ```bash
                        netstat -tulnp
                        ps aux | grep -v "root"
                        iptables -L -n -v
                        cat /var/log/auth.log | grep Failed
                        neofetch
                        ```
                    """.trimIndent()
                }
            }

            mode == AiCopilotMode.EXPLOIT_LAB || "buffer overflow" in lower || "exploit" in lower || "payload" in lower -> """
                ### [EXPLOIT LAB] Buffer Overflow & Memory Corruption Mechanics
                
                **Vulnerability Concept (CWE-121 Stack-based Buffer Overflow):**
                When boundary checking is omitted (`strcpy`, `gets`, `sprintf`), user input overwrites the stack frame:
                
                ```
                [ Local Buffer 64B ] -> [ Saved EBP/RBP ] -> [ Return Address (EIP/RIP) ]
                ```
                
                **Proof of Concept Exploit Script:**
                ```python
                # filename: /root/bof_exploit.py
                import struct
                
                OFFSET = 524
                # Target memory address (e.g. jmp esp in non-ASLR module)
                RET_ADDR = struct.pack("<I", 0x08041337)
                
                # NOP Sled + Shellcode payload
                NOPS = b"\x90" * 32
                SHELLCODE = b"\xcc\xcc\xcc\xcc" # INT3 Breakpoint trap
                
                payload = b"A" * OFFSET + RET_ADDR + NOPS + SHELLCODE
                print(f"[+] Generated PoC Payload of {len(payload)} bytes")
                print(f"[+] Overwriting Return Address with: {RET_ADDR}")
                ```
                
                **Defensive Countermeasures:**
                1. Compile with `-fstack-protector-all` (Stack Canaries).
                2. Enforce DEP/NX (Non-Executable Stack via `W^X`).
                3. Enable full ASLR (`echo 2 > /proc/sys/kernel/randomize_va_space`).
            """.trimIndent()

            mode == AiCopilotMode.VULN_PATCH || "patch" in lower || "fix" in lower -> """
                ### [VULN REVIEW & PATCH] SQL Injection (CWE-89) Remediation
                
                **❌ Vulnerable Code (Direct String Concatenation):**
                ```php
                // INSECURE: User input concatenated into SQL statement
                ${'$'}id = ${'$'}_GET['id'];
                ${'$'}query = "SELECT * FROM users WHERE id = '" . ${'$'}id . "'";
                ${'$'}result = mysqli_query(${'$'}conn, ${'$'}query);
                ```
                
                **✅ Secure Remediation Patch (Parameterized Query / PDO):**
                ```php
                # filename: /root/secure_user_query.php
                <?php
                // SECURE: Parameterized Prepared Statement prevents SQLi
                ${'$'}stmt = ${'$'}pdo->prepare("SELECT id, username, email FROM users WHERE id = :id");
                ${'$'}stmt->execute(['id' => ${'$'}_GET['id']]);
                ${'$'}user = ${'$'}stmt->fetch(PDO::FETCH_ASSOC);
                ?>
                ```
                
                **Remediation Steps:**
                1. Separate SQL code from user-supplied data using database parameter binding.
                2. Enforce strict input validation (e.g. `filter_var(${'$'}id, FILTER_VALIDATE_INT)`).
                3. Restrict database user privileges to `SELECT` on required tables only.
            """.trimIndent()

            else -> """
                ### [CYBER_AI INTEL] Threat Intelligence & Offensive/Defensive Briefing
                
                **Topic**: **$prompt**
                
                1. **Threat Vectors & Attack Surface:**
                   - Assess publicly exposed ports (`80`, `443`, `22`, `3306`) and untrusted input points.
                   - Monitor unauthorized privilege escalation hooks and unpatched libraries.
                
                2. **Actionable Commands in Sandbox:**
                   ```bash
                   nmap -sS -p 80,443,22 10.10.10.50
                   cat /etc/passwd | grep -v "/nologin"
                   prowler aws --compliance cis_2.0_aws
                   ```
                
                3. **Recommended Next Steps:**
                   - Tap **"CODE DEVELOPER"** to generate an automated script.
                   - Tap **"COMMAND BUILDER"** to craft a piped terminal pipeline.
                   - Or execute any command directly in the **Sandbox CLI** tab!
            """.trimIndent()
        }
    }
}
