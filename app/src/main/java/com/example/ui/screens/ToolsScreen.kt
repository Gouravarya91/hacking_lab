package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Difficulty
import com.example.data.model.SecurityTool
import com.example.data.model.ToolCategory
import com.example.ui.components.GlowCard
import com.example.ui.components.SeverityBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.CyberLabViewModel

@Composable
fun ToolsScreen(viewModel: CyberLabViewModel) {
    val tools by viewModel.tools.collectAsState(initial = emptyList())
    val searchQuery by viewModel.toolSearchQuery.collectAsState()
    val selectedCategory by viewModel.selectedToolCategory.collectAsState()
    val selectedToolDetail by viewModel.selectedToolDetail.collectAsState()

    val filteredTools = remember(tools, searchQuery, selectedCategory) {
        tools.filter { tool ->
            val matchesCategory = selectedCategory == null || tool.category == selectedCategory
            val matchesSearch = searchQuery.isBlank() ||
                    tool.name.contains(searchQuery, ignoreCase = true) ||
                    tool.description.contains(searchQuery, ignoreCase = true) ||
                    tool.syntax.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBlack)
            .padding(12.dp)
    ) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setToolSearchQuery(it) },
            placeholder = { Text("Search 50+ tools, syntax, flags...", color = TextMuted) },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = NeonGreen)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setToolSearchQuery("") }) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", tint = TextMuted)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("tools_search_bar"),
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

        Spacer(modifier = Modifier.height(10.dp))

        // Category Filter Chips
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { viewModel.setToolCategory(null) },
                    label = { Text("ALL (${tools.size})", style = MaterialTheme.typography.labelSmall) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NeonGreenGlow,
                        selectedLabelColor = NeonGreen,
                        containerColor = CyberDark,
                        labelColor = TextSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = if (selectedCategory == null) NeonGreen else CyberBorder,
                        enabled = true,
                        selected = selectedCategory == null
                    )
                )
            }

            items(ToolCategory.values()) { category ->
                val count = tools.count { it.category == category }
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { viewModel.setToolCategory(category) },
                    label = { Text("${category.displayName} ($count)", style = MaterialTheme.typography.labelSmall) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CyberCyanGlow,
                        selectedLabelColor = CyberCyan,
                        containerColor = CyberDark,
                        labelColor = TextSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = if (selectedCategory == category) CyberCyan else CyberBorder,
                        enabled = true,
                        selected = selectedCategory == category
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        
        // Live Updates Ticker
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(CyberDark)
                .border(1.dp, CyberCyan.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Campaign,
                contentDescription = "Live Updates",
                tint = CyberCyan,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "LIVE TOOLS UPDATE",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = CyberCyan,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Nmap 7.94 released | Metasploit 6.3.32 patch available | Trivy adds new AWS checks",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Tools List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredTools, key = { it.id }) { tool ->
                Box(modifier = Modifier.animateItem()) {
                    ToolCard(
                        tool = tool,
                        onClick = { viewModel.selectToolDetail(tool) },
                        onToggleBookmark = { viewModel.toggleBookmark(tool.id) }
                    )
                }
            }
        }
    }

    // Tool Detail Modal
    if (selectedToolDetail != null) {
        ToolDetailDialog(
            tool = selectedToolDetail!!,
            onDismiss = { viewModel.selectToolDetail(null) },
            onToggleBookmark = { viewModel.toggleBookmark(selectedToolDetail!!.id) },
            onRunCommand = { cmd -> viewModel.runToolInTerminal(cmd) }
        )
    }
}

@Composable
fun ToolCard(
    tool: SecurityTool,
    onClick: () -> Unit,
    onToggleBookmark: () -> Unit
) {
    GlowCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("tool_card_${tool.id}"),
        borderColor = if (tool.isBookmarked) NeonGreen else CyberBorder
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = tool.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(CyberSurfaceVariant)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = tool.category.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            color = CyberCyan
                        )
                    }

                    SeverityBadge(tool.riskLevel)

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(CyberDark)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = tool.difficulty.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = when (tool.difficulty) {
                                Difficulty.BEGINNER -> NeonGreen
                                Difficulty.INTERMEDIATE -> CyberAmber
                                Difficulty.ADVANCED -> CyberCrimson
                            }
                        )
                    }
                }
            }

            IconButton(onClick = onToggleBookmark) {
                Icon(
                    imageVector = if (tool.isBookmarked) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = "Bookmark",
                    tint = if (tool.isBookmarked) NeonGreen else TextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = tool.description,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(CyberDark)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = "$ ${tool.syntax}",
                style = MaterialTheme.typography.bodySmall,
                color = NeonGreen,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun ToolDetailDialog(
    tool: SecurityTool,
    onDismiss: () -> Unit,
    onToggleBookmark: () -> Unit,
    onRunCommand: (String) -> Unit
) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, CyberCyan, RoundedCornerShape(12.dp)),
            color = CyberSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Dialog Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = tool.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CyberCyan
                        )
                        Text(
                            text = tool.category.displayName,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }

                    Row {
                        IconButton(onClick = onToggleBookmark) {
                            Icon(
                                imageVector = if (tool.isBookmarked) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                                contentDescription = "Bookmark",
                                tint = if (tool.isBookmarked) NeonGreen else TextMuted
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = tool.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "OFFICIAL SYNTAX",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeonGreen,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(CyberBlack)
                        .padding(8.dp)
                ) {
                    Text(
                        text = tool.syntax,
                        style = MaterialTheme.typography.bodySmall,
                        color = NeonGreen
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "REAL-WORLD COMMAND EXAMPLES (${tool.examples.size})",
                    style = MaterialTheme.typography.labelSmall,
                    color = CyberCyan,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 280.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(tool.examples) { ex ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(CyberDark)
                                .border(1.dp, CyberBorder, RoundedCornerShape(6.dp))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = ex.title,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = ex.description,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(CyberBlack)
                                    .padding(6.dp)
                            ) {
                                Text(
                                    text = "$ ${ex.command}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CyberCyan
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("Command", ex.command))
                                        Toast.makeText(context, "Command copied!", Toast.LENGTH_SHORT).show()
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(14.dp), tint = TextMuted)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Copy", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                Button(
                                    onClick = {
                                        onRunCommand(ex.command)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Run", modifier = Modifier.size(14.dp), tint = CyberBlack)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Run in Sandbox", style = MaterialTheme.typography.labelSmall, color = CyberBlack, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
