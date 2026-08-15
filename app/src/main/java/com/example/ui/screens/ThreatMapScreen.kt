package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RiskLevel
import com.example.data.model.ThreatIncident
import com.example.ui.components.GlowCard
import com.example.ui.components.SeverityBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.CyberLabViewModel

@Composable
fun ThreatMapScreen(viewModel: CyberLabViewModel) {
    val threats by viewModel.threats.collectAsState(initial = emptyList())
    val selectedFilter by viewModel.threatFilter.collectAsState()

    val filteredThreats = remember(threats, selectedFilter) {
        if (selectedFilter == "ALL") threats
        else threats.filter { it.severity.name == selectedFilter }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBlack)
            .padding(12.dp)
    ) {
        // Attack Map Visualizer Canvas
        CyberAttackMapCanvas(threats = filteredThreats)

        Spacer(modifier = Modifier.height(12.dp))

        // Severity Filter Chips & Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(CyberCrimson)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "LIVE THREAT TELEMETRY FEED",
                    style = MaterialTheme.typography.titleMedium,
                    color = NeonGreen,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val filters = listOf("ALL", "CRITICAL", "HIGH", "MEDIUM")
            items(filters) { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { viewModel.setThreatFilter(filter) },
                    label = { Text(filter, style = MaterialTheme.typography.labelSmall) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = if (filter == "CRITICAL") CyberCrimsonGlow else NeonGreenGlow,
                        selectedLabelColor = if (filter == "CRITICAL") CyberCrimson else NeonGreen,
                        containerColor = CyberDark,
                        labelColor = TextSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = if (selectedFilter == filter) NeonGreen else CyberBorder,
                        enabled = true,
                        selected = selectedFilter == filter
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Live Incident List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredThreats, key = { it.id }) { incident ->
                Box(modifier = Modifier.animateItem()) {
                    ThreatIncidentCard(incident)
                }
            }
        }
    }
}

@Composable
fun CyberAttackMapCanvas(threats: List<ThreatIncident>) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse"
    )

    val fastPulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "fastPulse"
    )

    // Generate static server nodes for background telemetry
    val backgroundNodes = remember {
        List(80) {
            Pair(
                (0..100).random() / 100f,
                (0..100).random() / 100f
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(CyberDark)
            .border(1.dp, CyberBorder, RoundedCornerShape(8.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // 1. Draw World Matrix Grid Lines (Radar effect)
            val gridCols = 12
            val gridRows = 6
            for (i in 0..gridCols) {
                val x = (w / gridCols) * i
                drawLine(
                    color = CyberBorderBright.copy(alpha = 0.1f),
                    start = Offset(x, 0f),
                    end = Offset(x, h),
                    strokeWidth = 1f
                )
            }
            for (j in 0..gridRows) {
                val y = (h / gridRows) * j
                drawLine(
                    color = CyberBorderBright.copy(alpha = 0.1f),
                    start = Offset(0f, y),
                    end = Offset(w, y),
                    strokeWidth = 1f
                )
            }

            // 2. Draw global telemetry nodes (simulated world data centers)
            backgroundNodes.forEachIndexed { index, (px, py) ->
                val nodeOffset = Offset(w * px, h * py)
                val isHot = index % 7 == 0
                val radius = if (isHot) 3f else 1.5f
                val color = if (isHot) CyberCyanDim.copy(alpha = 0.8f * (1f - fastPulse)) else TextMuted.copy(alpha = 0.3f)
                drawCircle(color = color, radius = radius, center = nodeOffset)
            }

            // 3. Draw Cyber Attack Arc Vectors
            threats.forEachIndexed { index, threat ->
                val start = Offset(w * threat.sourceCoords.first, h * threat.sourceCoords.second)
                val end = Offset(w * threat.targetCoords.first, h * threat.targetCoords.second)

                val attackColor = if (threat.severity == RiskLevel.CRITICAL) CyberCrimson else CyberAmber
                val offsetPulse = (pulseProgress + (index * 0.2f)) % 1f

                // Source Node (Attacker)
                drawCircle(color = attackColor, radius = 4f, center = start)
                
                // Pulsing Target Node
                val targetRadius = 4f + (offsetPulse * 16f)
                drawCircle(
                    color = attackColor.copy(alpha = 1f - offsetPulse),
                    radius = targetRadius,
                    center = end,
                    style = Stroke(width = 2f)
                )
                drawCircle(color = NeonGreen, radius = 3f, center = end)

                // Curved Attack Path
                val path = Path().apply {
                    moveTo(start.x, start.y)
                    val midX = (start.x + end.x) / 2
                    val midY = ((start.y + end.y) / 2) - 40f - (index * 10f)
                    quadraticBezierTo(midX, midY, end.x, end.y)
                }

                // Path Trail
                drawPath(
                    path = path,
                    color = attackColor.copy(alpha = 0.4f),
                    style = Stroke(width = 1.5f)
                )

                // Animated Packets (Multiple payloads per vector)
                val packet1X = start.x + (end.x - start.x) * offsetPulse
                val packet1Y = start.y + (end.y - start.y) * offsetPulse
                val packet2Pulse = (offsetPulse + 0.1f) % 1f
                val packet2X = start.x + (end.x - start.x) * packet2Pulse
                val packet2Y = start.y + (end.y - start.y) * packet2Pulse

                drawCircle(color = Color.White, radius = 2.5f, center = Offset(packet1X, packet1Y))
                drawCircle(color = attackColor, radius = 1.5f, center = Offset(packet2X, packet2Y))
            }
        }

        // Overlay status tags
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(CyberBlack.copy(alpha = 0.8f))
                .border(1.dp, CyberCyan.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 3.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(CyberCrimson)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "GLOBAL THREAT TELEMETRY MAP [NATIVE ENGINE]",
                    style = MaterialTheme.typography.labelSmall,
                    color = CyberCyan,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp)
        ) {
            Text(
                text = "ACTIVE SESSIONS: ${backgroundNodes.size + (threats.size * 14)}",
                style = MaterialTheme.typography.labelSmall,
                color = NeonGreen,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ThreatIncidentCard(incident: ThreatIncident) {
    val context = LocalContext.current

    GlowCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("threat_card_${incident.id}"),
        borderColor = if (incident.severity == RiskLevel.CRITICAL) CyberCrimsonGlow else CyberBorder
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = incident.threatType,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Actor/Campaign: ${incident.actorOrCampaign}",
                    style = MaterialTheme.typography.bodySmall,
                    color = CyberCyan
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                SeverityBadge(incident.severity)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = incident.timestamp,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Vector Path Route
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(CyberDark)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${incident.sourceCountry}  ➔  ${incident.targetCountry}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = if (incident.severity == RiskLevel.CRITICAL) CyberCrimson else CyberAmber
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "Target: ${incident.targetSector}",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = incident.summary,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(8.dp))

        // MITRE ATT&CK Badges
        Text(
            text = "MITRE ATT&CK TECHNIQUES:",
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
            fontSize = 9.sp
        )
        Spacer(modifier = Modifier.height(3.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            incident.mitreTechniques.forEach { technique ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(CyberSurfaceVariant)
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = technique,
                        style = MaterialTheme.typography.labelSmall,
                        color = CyberCyan,
                        fontSize = 9.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // IOCs & Copy Action
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "INDICATOR OF COMPROMISE (IOC):",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontSize = 9.sp
                )
                Text(
                    text = incident.iocs.firstOrNull() ?: "None",
                    style = MaterialTheme.typography.bodySmall,
                    color = NeonGreen
                )
            }

            IconButton(
                onClick = {
                    val iocText = incident.iocs.joinToString("\n")
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("IOCs", iocText))
                    Toast.makeText(context, "IOCs copied to clipboard!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy IOCs",
                    tint = TextMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
