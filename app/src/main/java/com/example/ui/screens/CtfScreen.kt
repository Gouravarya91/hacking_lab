package com.example.ui.screens

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
import com.example.data.model.CTFChallenge
import com.example.data.model.Difficulty
import com.example.data.model.UserProfile
import com.example.ui.components.GlowCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.CyberLabViewModel

@Composable
fun CtfScreen(viewModel: CyberLabViewModel) {
    val challenges by viewModel.challenges.collectAsState(initial = emptyList())
    val userProfile by viewModel.userProfile.collectAsState(initial = UserProfile())
    val activeChallenge by viewModel.activeChallenge.collectAsState()
    val feedback by viewModel.challengeFeedback.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBlack)
            .padding(12.dp)
    ) {
        // Operator Profile Status HUD Card
        OperatorProfileCard(userProfile = userProfile)

        Spacer(modifier = Modifier.height(12.dp))

        // CTF Arena Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(CyberAmber))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "ACTIVE CTF CHALLENGES",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = CyberAmber
                )
            }
            Text(
                text = "${challenges.count { it.isCompleted }}/${challenges.size} SOLVED",
                style = MaterialTheme.typography.bodySmall,
                color = NeonGreen,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Challenge Cards
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(challenges, key = { it.id }) { challenge ->
                Box(modifier = Modifier.animateItem()) {
                    ChallengeCard(
                        challenge = challenge,
                        onClick = { viewModel.selectChallenge(challenge) }
                    )
                }
            }
        }
    }

    if (activeChallenge != null) {
        ChallengeSolveDialog(
            challenge = activeChallenge!!,
            feedback = feedback,
            onDismiss = { viewModel.selectChallenge(null) },
            onSubmitAnswer = { optIdx, flag -> viewModel.submitChallengeAnswer(optIdx, flag) }
        )
    }
}

@Composable
fun OperatorProfileCard(userProfile: UserProfile) {
    val progress = if (userProfile.xpForNextLevel > 0) {
        userProfile.currentXp.toFloat() / userProfile.xpForNextLevel.toFloat()
    } else 0f

    GlowCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = NeonGreen
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "OPERATOR: ${userProfile.username}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = NeonGreen
                )
                Text(
                    text = "Rank: ${userProfile.rankTitle} (Lvl ${userProfile.level})",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CyberCyan
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.LocalFireDepartment, contentDescription = "Streak", tint = CyberAmber, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${userProfile.streakDays} Day Streak",
                    style = MaterialTheme.typography.bodySmall,
                    color = CyberAmber,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "EXP: ${userProfile.currentXp} / ${userProfile.xpForNextLevel} XP",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall,
                color = NeonGreen,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = NeonGreen,
            trackColor = CyberDark
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Badges Row
        Text(
            text = "UNLOCKED DIGITAL BADGES:",
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
            fontSize = 9.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(userProfile.unlockedBadgeIds.toList()) { badge ->
                val badgeIcon = when (badge) {
                    "CTF_NOVICE" -> Icons.Default.Flag
                    "FLAG_HUNTER" -> Icons.Default.GpsFixed
                    "SOC_DEFENDER" -> Icons.Default.Shield
                    "RED_TEAM_ELITE" -> Icons.Default.Whatshot
                    else -> Icons.Default.EmojiEvents
                }
                val badgeColor = when (badge) {
                    "RED_TEAM_ELITE" -> CyberCrimson
                    "SOC_DEFENDER" -> NeonGreen
                    "FLAG_HUNTER" -> CyberCyan
                    else -> CyberAmber
                }
                
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(CyberDark)
                        .border(1.dp, badgeColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = badgeIcon,
                            contentDescription = badge,
                            tint = badgeColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = badge.replace("_", " "),
                            style = MaterialTheme.typography.labelSmall,
                            color = badgeColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChallengeCard(challenge: CTFChallenge, onClick: () -> Unit) {
    GlowCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("challenge_card_${challenge.id}"),
        borderColor = if (challenge.isCompleted) NeonGreen else CyberBorder
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = challenge.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(CyberSurfaceVariant)
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = challenge.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = CyberCyan
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(CyberDark)
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = challenge.difficulty.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = when (challenge.difficulty) {
                                Difficulty.BEGINNER -> NeonGreen
                                Difficulty.INTERMEDIATE -> CyberAmber
                                Difficulty.ADVANCED -> CyberCrimson
                            }
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (challenge.isCompleted) NeonGreenGlow else CyberAmberGlow)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (challenge.isCompleted) "SOLVED" else "+${challenge.points} XP",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (challenge.isCompleted) NeonGreen else CyberAmber
                    )
                }
            }
        }
    }
}

@Composable
fun ChallengeSolveDialog(
    challenge: CTFChallenge,
    feedback: String?,
    onDismiss: () -> Unit,
    onSubmitAnswer: (Int?, String?) -> Unit
) {
    var selectedOption by remember { mutableStateOf<Int?>(null) }
    var flagInput by remember { mutableStateOf("") }
    var showHint by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, CyberAmber, RoundedCornerShape(12.dp)),
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
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = challenge.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = CyberAmber)
                        Text(text = "Reward: +${challenge.points} XP | Category: ${challenge.category}", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(text = challenge.prompt, style = MaterialTheme.typography.bodySmall, color = TextPrimary)

                if (challenge.codeOrLogSnippet != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(CyberBlack)
                            .padding(8.dp)
                    ) {
                        Text(text = challenge.codeOrLogSnippet, style = MaterialTheme.typography.bodySmall, color = NeonGreen)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (challenge.options != null) {
                    Text(text = "SELECT CORRECT VECTOR:", style = MaterialTheme.typography.labelSmall, color = CyberCyan, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        challenge.options.forEachIndexed { index, option ->
                            val isSelected = selectedOption == index
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isSelected) CyberSurfaceVariant else CyberDark)
                                    .border(1.dp, if (isSelected) CyberCyan else CyberBorder, RoundedCornerShape(4.dp))
                                    .clickable { selectedOption = index }
                                    .padding(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { selectedOption = index },
                                    colors = RadioButtonDefaults.colors(selectedColor = CyberCyan)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = option, style = MaterialTheme.typography.bodySmall, color = if (isSelected) TextPrimary else TextSecondary)
                            }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = flagInput,
                        onValueChange = { flagInput = it },
                        label = { Text("Submit Flag (e.g., CYBER{...})", color = CyberCyan) },
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
                }

                if (feedback != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    val isSuccess = feedback.startsWith("SUCCESS")
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isSuccess) NeonGreenGlow else CyberCrimsonGlow)
                            .border(1.dp, if (isSuccess) NeonGreen else CyberCrimson, RoundedCornerShape(4.dp))
                            .padding(8.dp)
                    ) {
                        Column {
                            Text(
                                text = feedback,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isSuccess) NeonGreen else CyberCrimson,
                                fontWeight = FontWeight.Bold
                            )
                            if (isSuccess) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = challenge.explanation,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { showHint = !showHint }) {
                        Icon(imageVector = Icons.Default.Lightbulb, contentDescription = "Hint", tint = CyberAmber, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (showHint) "Hide Hint" else "View Hint", style = MaterialTheme.typography.labelSmall, color = CyberAmber)
                    }

                    Button(
                        onClick = { onSubmitAnswer(selectedOption, flagInput) },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.height(36.dp).testTag("submit_ctf_button")
                    ) {
                        Text("VERIFY & SUBMIT", style = MaterialTheme.typography.labelSmall, color = CyberBlack, fontWeight = FontWeight.Bold)
                    }
                }

                if (showHint) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(CyberDark)
                            .padding(6.dp)
                    ) {
                        Text(text = "💡 Hint: ${challenge.hint}", style = MaterialTheme.typography.labelSmall, color = CyberAmber)
                    }
                }
            }
        }
    }
}
