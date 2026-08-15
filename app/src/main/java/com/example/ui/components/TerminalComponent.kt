package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.core.*
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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.CyberAudioEngine
import com.example.data.model.TerminalLine
import com.example.ui.theme.*
import com.example.ui.viewmodel.QuickCommand
import com.example.ui.viewmodel.TerminalViewModel

@Composable
fun TerminalComponent(
    viewModel: TerminalViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val terminalLines by viewModel.terminalLines.collectAsState()
    val currentInput by viewModel.currentCommandInput.collectAsState()
    val promptString by viewModel.promptString.collectAsState()
    val isExecuting by viewModel.isExecuting.collectAsState()
    val currentDirectory by viewModel.currentWorkingDirectory.collectAsState()
    val autocompleteSuggestions by viewModel.autocompleteSuggestions.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    val listState = rememberLazyListState()

    val infiniteTransition = rememberInfiniteTransition(label = "cursorPulse")
    val cursorAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursorAlpha"
    )

    LaunchedEffect(terminalLines.size) {
        if (terminalLines.isNotEmpty()) {
            listState.animateScrollToItem(terminalLines.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBlack)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // High-Density Terminal Window Header & Stream Console
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(Color.Black.copy(alpha = 0.8f))
                .border(1.dp, CyberBorder, RoundedCornerShape(6.dp))
        ) {
            // Window Titlebar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CyberDark)
                    .border(1.dp, CyberBorder, RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "bash — $promptString (80x24)",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyberCyan,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (isExecuting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(12.dp),
                            color = NeonGreen,
                            strokeWidth = 1.5.dp
                        )
                    }

                    IconButton(
                        onClick = {
                            val logs = viewModel.exportTerminalLogs()
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Terminal Logs", logs))
                            Toast.makeText(context, "Terminal logs copied to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(22.dp).testTag("copy_logs_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Terminal Logs",
                            tint = CyberCyan,
                            modifier = Modifier.size(13.dp)
                        )
                    }

                    IconButton(
                        onClick = { viewModel.clearTerminal() },
                        modifier = Modifier.size(22.dp).testTag("clear_terminal_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear Terminal",
                            tint = TextMuted,
                            modifier = Modifier.size(13.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(2.dp))
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(CyberCrimson))
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(CyberAmber))
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(NeonGreen))
                }
            }

            // Terminal Output Stream
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                SelectionContainer {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        items(
                            items = terminalLines,
                            key = { line -> line.id }
                        ) { line ->
                            TerminalLineItem(line)
                        }

                        // Active Prompt Cursor line inside terminal stream
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "$promptString ",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = TextPrimary
                                )
                                Text(
                                    text = if (currentInput.isNotEmpty()) currentInput else "_",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = NeonGreen,
                                    modifier = if (currentInput.isEmpty()) Modifier.alpha(cursorAlpha) else Modifier
                                )
                            }
                        }
                    }
                }
            }
        }

        // Dynamic Autocomplete Bar (shows when typing matching commands/files)
        if (autocompleteSuggestions.isNotEmpty()) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                item {
                    Text(
                        text = "TAB >>",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyberAmber,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(end = 2.dp)
                    )
                }
                items(autocompleteSuggestions) { suggestion ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(CyberSurfaceVariant)
                            .border(1.dp, CyberCyan.copy(alpha = 0.5f), RoundedCornerShape(3.dp))
                            .clickable { viewModel.applyAutocomplete(suggestion) }
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = suggestion,
                            style = MaterialTheme.typography.labelSmall,
                            color = CyberCyan,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.5.sp
                        )
                    }
                }
            }
        }

        // Category Filter Chips for Quick Actions
        val categories = listOf("ALL", "RECON", "WEB", "EXPLOIT", "CLOUD", "FORENSICS", "SYSTEM", "CTF")
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(categories) { cat ->
                val isSelected = cat == selectedCategory
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (isSelected) CyberSurfaceVariant else CyberDark)
                        .border(
                            1.dp,
                            if (isSelected) NeonGreen else CyberBorder,
                            RoundedCornerShape(3.dp)
                        )
                        .clickable { viewModel.selectCategory(cat) }
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = cat,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) NeonGreen else TextMuted,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 8.5.sp
                    )
                }
            }
        }

        // Quick Command Touchbar Actions
        val filteredQuickCommands = if (selectedCategory == "ALL") {
            viewModel.quickCommands
        } else {
            viewModel.quickCommands.filter { it.category == selectedCategory }
        }

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            // Navigation Helper Buttons
            item {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(CyberDark)
                        .border(1.dp, CyberBorderBright, RoundedCornerShape(4.dp))
                        .clickable { viewModel.navigateHistory(up = true) }
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                        .testTag("prev_history_button")
                ) {
                    Text(
                        text = "↑ PREV",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyberCyan,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.5.sp
                    )
                }
            }

            item {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(CyberDark)
                        .border(1.dp, CyberBorderBright, RoundedCornerShape(4.dp))
                        .clickable { viewModel.navigateHistory(up = false) }
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                        .testTag("next_history_button")
                ) {
                    Text(
                        text = "↓ NEXT",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyberCyan,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.5.sp
                    )
                }
            }

            items(filteredQuickCommands) { qc ->
                val chipColor = when (qc.category) {
                    "RECON" -> NeonGreen
                    "WEB" -> CyberCyan
                    "EXPLOIT" -> CyberCrimson
                    "CLOUD" -> CyberAmber
                    "FORENSICS" -> NeonGreen
                    "CTF" -> CyberAmber
                    else -> TextPrimary
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(CyberDark)
                        .border(1.dp, chipColor.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                        .clickable { viewModel.executeTerminalCommand(qc.command) }
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                        .testTag("quick_action_${qc.label}")
                ) {
                    Text(
                        text = qc.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = chipColor,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.5.sp
                    )
                }
            }
        }

        // Command Prompt Input Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(CyberDark)
                .border(1.dp, NeonGreen.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$promptString ",
                style = MaterialTheme.typography.bodySmall,
                color = NeonGreen,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 10.5.sp
            )

            TextField(
                value = currentInput,
                onValueChange = { 
                    if (it != currentInput) {
                        CyberAudioEngine.playSound(CyberAudioEngine.SoundType.KEY_CLACK)
                    }
                    viewModel.onCommandInputChange(it) 
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("terminal_command_input"),
                placeholder = {
                    Text(
                        "nmap, sqlmap, hydra, cd, ls, ai, submit...",
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
                    cursorColor = NeonGreen,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { viewModel.executeTerminalCommand() }),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.5.sp
                )
            )

            IconButton(
                onClick = { viewModel.executeTerminalCommand() },
                modifier = Modifier
                    .size(30.dp)
                    .testTag("terminal_execute_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Execute Command",
                    tint = NeonGreen,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun TerminalLineItem(line: TerminalLine) {
    val color = when (line.type) {
        TerminalLine.LineType.INPUT -> NeonGreen
        TerminalLine.LineType.OUTPUT -> TextPrimary
        TerminalLine.LineType.ERROR -> TerminalRed
        TerminalLine.LineType.SUCCESS -> TerminalGreen
        TerminalLine.LineType.SYSTEM -> TerminalCyan
        TerminalLine.LineType.BANNER -> TerminalYellow
        TerminalLine.LineType.TABLE -> TextPrimary
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = line.content,
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace,
                lineHeight = 15.sp,
                fontSize = 10.5.sp
            ),
            color = color
        )
    }
}
