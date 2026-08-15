package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.CyberLabViewModel

@Composable
fun SettingsScreen(viewModel: CyberLabViewModel) {
    val isMuted by viewModel.isMuted.collectAsState()
    val apiKey by viewModel.apiKey.collectAsState()
    var showApiKey by remember { mutableStateOf(false) }
    var editingApiKey by remember(apiKey) { mutableStateOf(apiKey) }
    var isEditing by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "SYSTEM CONFIGURATION",
                color = CyberCyan,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        item {
            SettingsCard(
                title = "Gemini API Key",
                description = "Configure your AI Copilot API key securely. Encrypted locally using EncryptedSharedPreferences.",
                icon = Icons.Default.VpnKey,
                onClick = { isEditing = !isEditing },
                actionContent = {
                    TextButton(onClick = { isEditing = !isEditing }) {
                        Text(if (isEditing) "CANCEL" else "EDIT", color = NeonGreen)
                    }
                }
            )
            if (isEditing) {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = editingApiKey,
                        onValueChange = { editingApiKey = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("AIzaSy...") },
                        visualTransformation = if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showApiKey = !showApiKey }) {
                                Icon(if (showApiKey) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = null, tint = NeonGreen)
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGreen,
                            unfocusedBorderColor = CyberBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = NeonGreen
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { viewModel.saveApiKey(editingApiKey); isEditing = false }, colors = ButtonDefaults.buttonColors(containerColor = CyberCyan)) {
                        Text("SAVE", color = CyberBlack, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            SettingsCard(
                title = "Haptic Feedback & Audio",
                description = "Toggle system sounds and haptic vibrations for terminal actions.",
                icon = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                onClick = { viewModel.toggleSound() },
                actionContent = {
                    Switch(
                        checked = !isMuted,
                        onCheckedChange = { viewModel.toggleSound() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CyberBlack,
                            checkedTrackColor = NeonGreen,
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = CyberDark
                        )
                    )
                }
            )
        }

        item {
            SettingsCard(
                title = "Clear Terminal History",
                description = "Purge all local terminal logs and buffer history.",
                icon = Icons.Default.Delete,
                onClick = { viewModel.executeTerminalCommand("clear") },
                actionContent = {
                    Button(
                        onClick = { viewModel.executeTerminalCommand("clear") },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCrimson)
                    ) {
                        Text("PURGE", color = CyberBlack, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        item {
            SettingsCard(
                title = "System Information",
                description = "OS: Android (CyberLab Sandbox)\nVersion: v2.1.4_stable\nCodename: NEON_PHOENIX",
                icon = Icons.Default.Info,
                onClick = {}
            )
        }
    }
}

@Composable
fun SettingsCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    actionContent: @Composable () -> Unit = {}
) {
    Surface(
        color = CyberDark.copy(alpha = 0.5f),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CyberBorder),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = NeonGreen,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    color = TextMuted,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            actionContent()
        }
    }
}
