package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RiskLevel
import com.example.ui.theme.*
import com.example.ui.viewmodel.CyberTab
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CyberTopBar(
    isMuted: Boolean,
    isDarkTheme: Boolean,
    onToggleSound: () -> Unit,
    onToggleTheme: () -> Unit,
    onOpenCommandPalette: () -> Unit
) {
    var currentTimeString by remember { mutableStateOf("") }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    LaunchedEffect(Unit) {
        val sdf = SimpleDateFormat("HH:mm:ss 'UTC'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        while (true) {
            currentTimeString = sdf.format(Date())
            delay(1000)
        }
    }

    Surface(
        color = CyberDark,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = CyberBorder,
                shape = RoundedCornerShape(0.dp)
            )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // High Density Branding with pulse indicator
                Column {
                    Text(
                        text = "SYSTEM_STATUS",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyberCyan.copy(alpha = 0.8f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "CYBER_LAB_PRO",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = NeonGreen,
                            letterSpacing = 0.5.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.testTag("app_branding_title")
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CyberCrimson,
                            modifier = Modifier.alpha(pulseAlpha)
                        )
                    }
                }

                // High Density Telemetry Mini-Gauges & Clock Pill
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.padding(end = 6.dp)
                    ) {
                        // CPU Bar
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "CPU ",
                                style = MaterialTheme.typography.labelSmall,
                                color = CyberAmber,
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Box(
                                modifier = Modifier
                                    .width(36.dp)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(1.dp))
                                    .background(CyberBorder)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(0.45f)
                                        .background(NeonGreen)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        // MEM Bar
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "MEM ",
                                style = MaterialTheme.typography.labelSmall,
                                color = CyberCyan,
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Box(
                                modifier = Modifier
                                    .width(36.dp)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(1.dp))
                                    .background(CyberBorder)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(0.68f)
                                        .background(CyberCyan)
                                )
                            }
                        }
                    }

                    // UTC Clock Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(CyberSurfaceVariant)
                            .border(1.dp, NeonGreen.copy(alpha = 0.25f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = currentTimeString.ifEmpty { "14:22:09 UTC" },
                            style = MaterialTheme.typography.bodySmall,
                            color = NeonGreen,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Command Palette Trigger
                    IconButton(
                        onClick = onOpenCommandPalette,
                        modifier = Modifier
                            .size(30.dp)
                            .testTag("command_palette_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search / Command Palette",
                            tint = CyberCyan,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Theme Toggle
                    IconButton(
                        onClick = onToggleTheme,
                        modifier = Modifier
                            .size(30.dp)
                            .testTag("theme_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                            contentDescription = "Toggle Theme",
                            tint = if (isDarkTheme) NeonGreen else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Sound FX Toggle
                    IconButton(
                        onClick = onToggleSound,
                        modifier = Modifier
                            .size(30.dp)
                            .testTag("sound_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                            contentDescription = "Toggle Audio FX",
                            tint = if (isMuted) TextMuted else NeonGreen,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CyberBottomNav(
    selectedTab: CyberTab,
    onSelectTab: (CyberTab) -> Unit
) {
    Surface(
        color = CyberDark,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CyberBorder, RoundedCornerShape(0.dp))
    ) {
        ScrollableTabRow(
            selectedTabIndex = selectedTab.ordinal,
            containerColor = CyberDark,
            contentColor = NeonGreen,
            edgePadding = 4.dp,
            divider = {},
            indicator = {}
        ) {
            CyberTab.values().forEach { tab ->
                val isSelected = selectedTab == tab
                val icon = when (tab) {
                    CyberTab.TERMINAL -> Icons.Default.Terminal
                    CyberTab.TOOLS -> Icons.Default.Build
                    CyberTab.THREAT_MAP -> Icons.Default.Public
                    CyberTab.VULN_LAB -> Icons.Default.BugReport
                    CyberTab.FRAUD_INTEL -> Icons.Default.Warning
                    CyberTab.CTF -> Icons.Default.EmojiEvents
                    CyberTab.COMMUNITY -> Icons.Default.Forum
                    CyberTab.AI_ADVISOR -> Icons.Default.SmartToy
                    CyberTab.SETTINGS -> Icons.Default.Settings
                }

                val animatedBgColor by androidx.compose.animation.animateColorAsState(
                    targetValue = if (isSelected) CyberSurfaceVariant else Color.Transparent,
                    animationSpec = androidx.compose.animation.core.tween(300),
                    label = "tab_bg"
                )
                val animatedBorderColor by androidx.compose.animation.animateColorAsState(
                    targetValue = if (isSelected) NeonGreen.copy(alpha = 0.8f) else Color.Transparent,
                    animationSpec = androidx.compose.animation.core.tween(300),
                    label = "tab_border"
                )
                val animatedIconColor by androidx.compose.animation.animateColorAsState(
                    targetValue = if (isSelected) NeonGreen else TextMuted,
                    animationSpec = androidx.compose.animation.core.tween(300),
                    label = "tab_icon"
                )
                val animatedTextColor by androidx.compose.animation.animateColorAsState(
                    targetValue = if (isSelected) NeonGreen else TextSecondary,
                    animationSpec = androidx.compose.animation.core.tween(300),
                    label = "tab_text"
                )

                Tab(
                    selected = isSelected,
                    onClick = { onSelectTab(tab) },
                    modifier = Modifier
                        .padding(vertical = 3.dp, horizontal = 2.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(animatedBgColor)
                        .border(
                            width = 1.dp,
                            color = animatedBorderColor,
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("tab_${tab.name.lowercase()}"),
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = icon,
                                contentDescription = tab.label,
                                tint = animatedIconColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = tab.label,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = animatedTextColor,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (tab.badge != null) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(CyberCrimson)
                                        .padding(horizontal = 3.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = tab.badge,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        fontSize = 7.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun SeverityBadge(severity: RiskLevel) {
    val (color, bgColor) = when (severity) {
        RiskLevel.CRITICAL -> Pair(CyberCrimson, CyberCrimsonGlow)
        RiskLevel.HIGH -> Pair(CyberAmber, CyberAmberGlow)
        RiskLevel.MEDIUM -> Pair(CyberCyan, CyberCyanGlow)
        RiskLevel.LOW -> Pair(NeonGreen, NeonGreenGlow)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(3.dp))
            .background(bgColor)
            .border(1.dp, color, RoundedCornerShape(3.dp))
            .padding(horizontal = 5.dp, vertical = 1.dp)
    ) {
        Text(
            text = severity.name,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            fontSize = 8.sp
        )
    }
}

@Composable
fun GlowCard(
    modifier: Modifier = Modifier,
    borderColor: Color = CyberBorder,
    backgroundColor: Color = CyberDark,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .border(1.dp, borderColor, RoundedCornerShape(6.dp)),
        shape = RoundedCornerShape(6.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            content = content
        )
    }
}

