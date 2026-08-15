package com.example.ui.screens

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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.ForumPost
import com.example.ui.components.GlowCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.CyberLabViewModel

@Composable
fun CommunityHubScreen(viewModel: CyberLabViewModel) {
    val posts by viewModel.forumPosts.collectAsState(initial = emptyList())
    val showCreateDialog by viewModel.showCreatePostDialog.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBlack)
            .padding(12.dp)
    ) {
        // Header & Create Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "CYBER RESEARCH HUB",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = CyberCyan
                )
                Text(
                    text = "Intelligence bulletins, exploit writeups & threat discussions",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            Button(
                onClick = { viewModel.setCreatePostDialog(true) },
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.height(34.dp).testTag("create_post_button"),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "New Post", tint = CyberBlack, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("POST INTEL", style = MaterialTheme.typography.labelSmall, color = CyberBlack, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Forum Post Feed
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(posts) { post ->
                ForumPostCard(
                    post = post,
                    onUpvote = { viewModel.upvotePost(post) }
                )
            }
        }
    }

    if (showCreateDialog) {
        CreatePostDialog(
            onDismiss = { viewModel.setCreatePostDialog(false) },
            onCreatePost = { title, cat, content, code ->
                viewModel.createPost(title, cat, content, code)
            }
        )
    }
}

@Composable
fun ForumPostCard(
    post: ForumPost,
    onUpvote: () -> Unit
) {
    GlowCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("post_card_${post.id}"),
        borderColor = CyberBorder
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(CyberSurfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Person, contentDescription = "Author", tint = CyberCyan, modifier = Modifier.size(14.dp))
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = post.author, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                        if (post.verifiedBadge) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(imageVector = Icons.Default.Verified, contentDescription = "Verified", tint = NeonGreen, modifier = Modifier.size(12.dp))
                        }
                    }
                    Text(text = "${post.authorRole} • ${post.timestamp}", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 9.sp)
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(3.dp))
                    .background(CyberDark)
                    .border(1.dp, CyberBorder, RoundedCornerShape(3.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(text = post.category, style = MaterialTheme.typography.labelSmall, color = CyberAmber, fontSize = 9.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = post.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = post.content,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )

        if (!post.codeSnippet.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .background(CyberBlack)
                    .padding(8.dp)
            ) {
                Text(
                    text = post.codeSnippet,
                    style = MaterialTheme.typography.bodySmall,
                    color = NeonGreen
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onUpvote,
                colors = ButtonDefaults.buttonColors(containerColor = if (post.isUpvoted) NeonGreenGlow else CyberDark),
                border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(if (post.isUpvoted) NeonGreen else CyberBorder)),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.height(30.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ThumbUp,
                    contentDescription = "Upvote",
                    tint = if (post.isUpvoted) NeonGreen else TextMuted,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${post.upvotes} Upvotes",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (post.isUpvoted) NeonGreen else TextMuted
                )
            }

            Text(
                text = "Community Verified",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                fontSize = 9.sp
            )
        }
    }
}

@Composable
fun CreatePostDialog(
    onDismiss: () -> Unit,
    onCreatePost: (String, String, String, String?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Vulnerability") }
    var content by remember { mutableStateOf("") }
    var codeSnippet by remember { mutableStateOf("") }

    val categories = listOf("Zero-Day", "Vulnerability", "Malware Analysis", "CTF", "Blue Team", "Cloud Security")

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
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "PUBLISH INTEL REPORT", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = NeonGreen)
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Report Title", color = CyberCyan) },
                    modifier = Modifier.fillMaxWidth(),
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

                Spacer(modifier = Modifier.height(6.dp))

                Text(text = "CATEGORY:", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    categories.take(3).forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyberCyanGlow,
                                selectedLabelColor = CyberCyan,
                                containerColor = CyberDark,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Investigation Details & Findings", color = CyberCyan) },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonGreen,
                        unfocusedBorderColor = CyberBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = CyberDark,
                        unfocusedContainerColor = CyberDark
                    ),
                    maxLines = 4
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = codeSnippet,
                    onValueChange = { codeSnippet = it },
                    label = { Text("Code / PoC / Splunk Query (Optional)", color = TextMuted) },
                    modifier = Modifier.fillMaxWidth().height(70.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = CyberBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = CyberBlack,
                        unfocusedContainerColor = CyberBlack
                    ),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        if (title.isNotBlank() && content.isNotBlank()) {
                            onCreatePost(title, category, content, codeSnippet.ifBlank { null })
                        }
                    },
                    enabled = title.isNotBlank() && content.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                    modifier = Modifier.fillMaxWidth().height(38.dp),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("BROADCAST TO COMMUNITY (+50 XP)", style = MaterialTheme.typography.labelSmall, color = CyberBlack, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
