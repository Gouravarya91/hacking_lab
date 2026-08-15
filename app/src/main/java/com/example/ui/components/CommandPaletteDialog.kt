package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*
import com.example.ui.viewmodel.CyberTab

data class PaletteAction(
    val title: String,
    val subtitle: String,
    val category: String,
    val onSelect: () -> Unit
)

@Composable
fun CommandPaletteDialog(
    onDismiss: () -> Unit,
    onNavigateTab: (CyberTab) -> Unit,
    onExecuteCommand: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val allActions = remember {
        listOf(
            PaletteAction("Sandbox CLI Terminal", "Jump to Linux simulation shell", "Navigation") { onNavigateTab(CyberTab.TERMINAL) },
            PaletteAction("Security Tools Directory", "Browse 50+ cybersecurity tools & cheatsheets", "Navigation") { onNavigateTab(CyberTab.TOOLS) },
            PaletteAction("Live Threat Map", "Inspect active cyber attacks and IOCs", "Navigation") { onNavigateTab(CyberTab.THREAT_MAP) },
            PaletteAction("Vulnerability & CVE Lab", "Audit targets & search CVSS 3.1 flaws", "Navigation") { onNavigateTab(CyberTab.VULN_LAB) },
            PaletteAction("CTF Challenges & Skill Progression", "Solve hacking challenges & earn XP", "Navigation") { onNavigateTab(CyberTab.CTF) },
            PaletteAction("Community Cyber Hub", "View researcher writeups & forum posts", "Navigation") { onNavigateTab(CyberTab.COMMUNITY) },
            PaletteAction("AI Cyber Copilot", "Ask questions about CVEs, scripts, and logs", "Navigation") { onNavigateTab(CyberTab.AI_ADVISOR) },

            PaletteAction("Run: nmap -sS -T4 192.168.1.100", "Execute fast SYN port scan in terminal", "Command") {
                onNavigateTab(CyberTab.TERMINAL)
                onExecuteCommand("nmap -sS -T4 192.168.1.100")
            },
            PaletteAction("Run: sqlmap -u \"http://10.10.10.5/item.php?id=1\"", "Simulate automated SQL injection exploit", "Command") {
                onNavigateTab(CyberTab.TERMINAL)
                onExecuteCommand("sqlmap -u \"http://10.10.10.5/item.php?id=1\" --batch")
            },
            PaletteAction("Run: hydra -l root -P passwords.txt 192.168.1.50 ssh", "Simulate multi-threaded SSH brute force", "Command") {
                onNavigateTab(CyberTab.TERMINAL)
                onExecuteCommand("hydra -l root -P /usr/share/wordlists/passwords.txt 192.168.1.50 ssh")
            },
            PaletteAction("Run: prowler aws --compliance cis_2.0_aws", "Run Cloud CIS benchmark compliance scan", "Command") {
                onNavigateTab(CyberTab.TERMINAL)
                onExecuteCommand("prowler aws --compliance cis_2.0_aws")
            },
            PaletteAction("Run: cat /var/log/auth.log", "Triage Linux authentication logs for brute-force attempts", "Command") {
                onNavigateTab(CyberTab.TERMINAL)
                onExecuteCommand("cat /var/log/auth.log")
            }
        )
    }

    val filteredActions = remember(searchQuery) {
        if (searchQuery.isBlank()) allActions
        else allActions.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.subtitle.contains(searchQuery, ignoreCase = true) ||
            it.category.contains(searchQuery, ignoreCase = true)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, NeonGreen, RoundedCornerShape(12.dp)),
            color = CyberSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = NeonGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "QUICK COMMAND PALETTE",
                        style = MaterialTheme.typography.titleMedium,
                        color = NeonGreen,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search tools, commands, CVEs, or tabs...", color = TextMuted) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("palette_search_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonGreen,
                        unfocusedBorderColor = CyberBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = CyberDark,
                        unfocusedContainerColor = CyberDark
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 350.dp)
                ) {
                    items(filteredActions) { action ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(CyberDark)
                                .border(1.dp, CyberBorder, RoundedCornerShape(6.dp))
                                .clickable {
                                    action.onSelect()
                                    onDismiss()
                                }
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = action.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (action.category == "Command") CyberCyan else NeonGreen
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(CyberSurfaceVariant)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = action.category,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextMuted
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = action.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}
