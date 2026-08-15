package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.CyberAudioEngine
import com.example.data.ai.AiCopilotMode
import com.example.data.ai.ExtractedCodeSnippet
import com.example.data.ai.ExtractedCommand
import com.example.ui.theme.*
import com.example.ui.viewmodel.AiMessage
import com.example.ui.viewmodel.CyberLabViewModel

@Composable
fun AiAdvisorScreen(viewModel: CyberLabViewModel) {
    val messages by viewModel.aiMessages.collectAsState()
    val isLoading by viewModel.isAiLoading.collectAsState()
    val selectedMode by viewModel.selectedAiMode.collectAsState()
    var inputPrompt by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val modePresets = when (selectedMode) {
        AiCopilotMode.CODE_DEVELOPER -> listOf(
            "Build Python Multi-threaded TCP Port Scanner",
            "Write Bash Auth.log Incident Triage Script",
            "Create Python Base64 & XOR CTF Cipher Solver",
            "Generate YARA Rule for Obfuscated PHP Webshell",
            "Write Python Buffer Overflow Offset Exploit PoC",
            "Create Linux System Hardening Audit Script"
        )
        AiCopilotMode.COMMAND_BUILDER -> listOf(
            "Nmap Full Port Stealth SYN Scan Pipeline",
            "Gobuster Web Directory Brute-Force Command",
            "Hydra SSH Password Dictionary Attack",
            "Sqlmap Automated Database Extraction",
            "Find All SUID Executable Binaries on Linux",
            "Prowler AWS CIS Benchmark Security Audit"
        )
        AiCopilotMode.EXPLOIT_LAB -> listOf(
            "Decode XOR Flag from /challenges/flag1_crypto.txt",
            "Extract EXIF Steganography Flag from flag2_stego.txt",
            "Craft Buffer Overflow EIP Overwrite Payload",
            "Build SQL Injection Auth Bypass Payload",
            "Generate Linux Bash Reverse Shell One-Liner"
        )
        AiCopilotMode.VULN_PATCH -> listOf(
            "Review SQLi Vulnerability in PHP & Give Prepared Fix",
            "Explain XZ CVE-2024-3094 Backdoor & Remediation",
            "Patch Insecure Direct Object Reference (IDOR)",
            "Fix Command Injection in Python subprocess.call()",
            "Remediate Cross-Site Scripting (XSS) in HTML Output"
        )
        AiCopilotMode.GENERAL -> listOf(
            "Explain MITRE ATT&CK T1059 Interpreter Tactics",
            "Analyze Linux auth.log Brute-Force Signatures",
            "Explain Stack Canaries & ASLR Defenses",
            "What is Zero Trust Micro-Segmentation?",
            "How to Perform Active Directory Kerberoasting Triage"
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBlack)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // High Density AI Header HUD
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(CyberDark)
                .border(1.dp, CyberBorder, RoundedCornerShape(6.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(CyberCyanGlow)
                    .border(1.dp, CyberCyan, RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = "AI",
                    tint = CyberCyan,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "CYBER_AI COPILOT // DEV & THREAT INTEL",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = CyberCyan,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(NeonGreen.copy(alpha = 0.15f))
                            .border(0.5.dp, NeonGreen.copy(alpha = 0.5f), RoundedCornerShape(3.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "LIVE",
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonGreen,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 7.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(
                    text = "ENGINE: GEMINI-3.5-FLASH • MODE: ${selectedMode.title.uppercase()} • CONTEXT: ONLINE",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 8.sp
                )
            }

            IconButton(
                onClick = { viewModel.clearAiMessages() },
                modifier = Modifier.size(26.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Clear Chat",
                    tint = TextMuted,
                    modifier = Modifier.size(15.dp)
                )
            }
        }

        // Mode Selector Bar
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(AiCopilotMode.values()) { mode ->
                val isSelected = selectedMode == mode
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isSelected) CyberCyan.copy(alpha = 0.2f) else CyberDark)
                        .border(
                            1.dp,
                            if (isSelected) CyberCyan else CyberBorder,
                            RoundedCornerShape(4.dp)
                        )
                        .clickable { viewModel.setAiMode(mode) }
                        .padding(horizontal = 7.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "[${mode.badge}]",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) CyberCyan else TextMuted,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 8.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = mode.title,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) TextPrimary else TextSecondary,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp
                        )
                    }
                }
            }
        }

        // High Density Actionable Presets
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            items(modePresets) { query ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(CyberDark)
                        .border(1.dp, CyberBorder, RoundedCornerShape(4.dp))
                        .clickable(enabled = !isLoading) { viewModel.sendAiPrompt(query) }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = CyberCyanDim,
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = query,
                            style = MaterialTheme.typography.labelSmall,
                            color = CyberCyanDim,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 8.5.sp
                        )
                    }
                }
            }
        }

        // Message List Stream
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(Color.Black.copy(alpha = 0.6f))
                .border(1.dp, CyberBorder, RoundedCornerShape(6.dp))
                .padding(8.dp)
        ) {
            SelectionContainer {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(messages) { msg ->
                        AiMessageBubble(
                            msg = msg,
                            onRunCommand = { cmd ->
                                viewModel.runCommandInTerminal(cmd)
                                Toast.makeText(context, "Executing in Sandbox CLI: $cmd", Toast.LENGTH_SHORT).show()
                            },
                            onSaveScript = { path, code ->
                                val success = viewModel.saveScriptToVfs(path, code)
                                if (success) {
                                    Toast.makeText(context, "Saved script to $path", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Failed to write $path", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onCopyText = { text, label ->
                                clipboardManager.setText(AnnotatedString(text))
                                CyberAudioEngine.playSound(CyberAudioEngine.SoundType.KEY_CLACK)
                                Toast.makeText(context, "Copied $label to clipboard", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }

                    if (isLoading) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(CyberDark)
                                    .border(1.dp, CyberCyan.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    color = CyberCyan,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "CYBER_AI: Generating code & cyber intelligence...",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = CyberCyan,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 9.sp
                                    )
                                    Text(
                                        text = "Querying Gemini API & parsing executable blocks...",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextMuted,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 7.5.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Prompt Input Field
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(CyberDark)
                .border(1.dp, CyberCyan.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = inputPrompt,
                onValueChange = { inputPrompt = it },
                modifier = Modifier
                    .weight(1f)
                    .testTag("ai_prompt_input"),
                placeholder = {
                    Text(
                        when (selectedMode) {
                            AiCopilotMode.CODE_DEVELOPER -> "Ask to develop Python tools, YARA rules, Bash scripts..."
                            AiCopilotMode.COMMAND_BUILDER -> "Describe task to generate piped terminal commands..."
                            AiCopilotMode.EXPLOIT_LAB -> "Ask for CTF cipher decoders, payloads, buffer overflow..."
                            AiCopilotMode.VULN_PATCH -> "Paste code to analyze security flaws & get patch..."
                            AiCopilotMode.GENERAL -> "Ask about CVEs, threat intel, penetration testing..."
                        },
                        color = TextMuted,
                        fontSize = 10.5.sp,
                        fontFamily = FontFamily.Monospace
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = CyberCyan,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    if (inputPrompt.isNotBlank() && !isLoading) {
                        viewModel.sendAiPrompt(inputPrompt)
                        inputPrompt = ""
                    }
                }),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                )
            )

            IconButton(
                onClick = {
                    if (inputPrompt.isNotBlank() && !isLoading) {
                        viewModel.sendAiPrompt(inputPrompt)
                        inputPrompt = ""
                    }
                },
                enabled = inputPrompt.isNotBlank() && !isLoading,
                modifier = Modifier
                    .size(32.dp)
                    .testTag("ai_send_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    tint = if (inputPrompt.isNotBlank() && !isLoading) CyberCyan else TextMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun AiMessageBubble(
    msg: AiMessage,
    onRunCommand: (String) -> Unit,
    onSaveScript: (String, String) -> Unit,
    onCopyText: (String, String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (msg.isUser) Alignment.End else Alignment.Start
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (msg.isUser) "[OPERATOR]" else "[CYBER_AI // ${msg.mode.badge}]",
                style = MaterialTheme.typography.labelSmall,
                color = if (msg.isUser) NeonGreen else CyberCyan,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                fontSize = 8.5.sp
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth(if (msg.isUser) 0.90f else 1.0f)
                .clip(RoundedCornerShape(4.dp))
                .background(if (msg.isUser) CyberSurfaceVariant else CyberDark)
                .border(
                    1.dp,
                    if (msg.isUser) NeonGreen.copy(alpha = 0.5f) else CyberBorder,
                    RoundedCornerShape(4.dp)
                )
                .padding(8.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                // Main Text
                Text(
                    text = msg.text,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 15.sp,
                        fontSize = 10.sp
                    ),
                    color = TextPrimary
                )

                // Extracted Executable Commands Action Cards
                if (msg.executableCommands.isNotEmpty()) {
                    Text(
                        text = "⚡ ACTIONABLE CLI COMMANDS:",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonGreen,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 8.sp
                    )

                    for (cmd in msg.executableCommands.take(3)) {
                        CommandActionCard(
                            command = cmd.command,
                            onRun = { onRunCommand(cmd.command) },
                            onCopy = { onCopyText(cmd.command, "Command") }
                        )
                    }
                }

                // Extracted Code Snippet Action Cards
                if (msg.codeSnippets.isNotEmpty()) {
                    Text(
                        text = "🛠️ DEVELOPED CODE ARTIFACTS:",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyberCyan,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 8.sp
                    )

                    for (snippet in msg.codeSnippets) {
                        CodeArtifactCard(
                            snippet = snippet,
                            onRun = {
                                val runCmd = when (snippet.language) {
                                    "python", "py" -> "python3 ${snippet.suggestedFileName}"
                                    "bash", "sh" -> "bash ${snippet.suggestedFileName}"
                                    "c", "cpp" -> "gcc ${snippet.suggestedFileName} -o /root/exploit && /root/exploit"
                                    "yara", "yar" -> "yara ${snippet.suggestedFileName} /var/log/nginx/access.log"
                                    else -> "cat ${snippet.suggestedFileName}"
                                }
                                onSaveScript(snippet.suggestedFileName, snippet.code)
                                onRunCommand(runCmd)
                            },
                            onSave = { onSaveScript(snippet.suggestedFileName, snippet.code) },
                            onCopy = { onCopyText(snippet.code, "Code Script") }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CommandActionCard(
    command: String,
    onRun: () -> Unit,
    onCopy: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(Color.Black.copy(alpha = 0.5f))
            .border(1.dp, NeonGreen.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$",
            color = NeonGreen,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = command,
            modifier = Modifier.weight(1f),
            color = TextPrimary,
            fontFamily = FontFamily.Monospace,
            fontSize = 9.sp,
            maxLines = 2
        )
        Spacer(modifier = Modifier.width(4.dp))

        // Copy button
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(3.dp))
                .background(CyberSurfaceVariant)
                .clickable { onCopy() }
                .padding(horizontal = 5.dp, vertical = 2.dp)
        ) {
            Text(
                text = "COPY",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                fontFamily = FontFamily.Monospace,
                fontSize = 7.5.sp
            )
        }

        Spacer(modifier = Modifier.width(4.dp))

        // Run in Terminal button
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(3.dp))
                .background(NeonGreen.copy(alpha = 0.2f))
                .border(0.5.dp, NeonGreen, RoundedCornerShape(3.dp))
                .clickable { onRun() }
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Run",
                    tint = NeonGreen,
                    modifier = Modifier.size(10.dp)
                )
                Text(
                    text = "RUN",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeonGreen,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 8.sp
                )
            }
        }
    }
}

@Composable
fun CodeArtifactCard(
    snippet: ExtractedCodeSnippet,
    onRun: () -> Unit,
    onSave: () -> Unit,
    onCopy: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(Color.Black.copy(alpha = 0.7f))
            .border(1.dp, CyberCyan.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
            .padding(6.dp)
    ) {
        // Code Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(3.dp))
                    .background(CyberCyan.copy(alpha = 0.2f))
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            ) {
                Text(
                    text = snippet.language.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = CyberCyan,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 7.5.sp
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = snippet.suggestedFileName,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                fontFamily = FontFamily.Monospace,
                fontSize = 8.sp,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${snippet.code.lines().size} lines",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                fontFamily = FontFamily.Monospace,
                fontSize = 7.5.sp
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Code Body Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(3.dp))
                .background(CyberBlack)
                .border(0.5.dp, CyberBorder, RoundedCornerShape(3.dp))
                .padding(6.dp)
                .horizontalScroll(scrollState)
        ) {
            Text(
                text = snippet.code,
                fontFamily = FontFamily.Monospace,
                fontSize = 8.5.sp,
                lineHeight = 12.sp,
                color = TextPrimary
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Action Toolbar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Copy
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(3.dp))
                    .background(CyberDark)
                    .border(0.5.dp, CyberBorder, RoundedCornerShape(3.dp))
                    .clickable { onCopy() }
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "COPY",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 8.sp
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Save to VFS
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(3.dp))
                    .background(CyberDark)
                    .border(0.5.dp, CyberCyan.copy(alpha = 0.5f), RoundedCornerShape(3.dp))
                    .clickable { onSave() }
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = "Save",
                        tint = CyberCyan,
                        modifier = Modifier.size(10.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "SAVE TO VFS",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyberCyan,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 8.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Run in Terminal
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(3.dp))
                    .background(NeonGreen.copy(alpha = 0.2f))
                    .border(0.5.dp, NeonGreen, RoundedCornerShape(3.dp))
                    .clickable { onRun() }
                    .padding(horizontal = 7.dp, vertical = 3.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Terminal,
                        contentDescription = "Run in Terminal",
                        tint = NeonGreen,
                        modifier = Modifier.size(10.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "RUN IN CLI",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonGreen,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 8.sp
                    )
                }
            }
        }
    }
}
