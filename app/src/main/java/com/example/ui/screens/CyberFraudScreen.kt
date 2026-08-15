package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.CyberLabViewModel

data class FraudIntel(
    val title: String,
    val description: String,
    val category: String,
    val severity: String,
    val prevention: String,
    val targetSectors: List<String>,
    val mitreTTPs: List<String>
)

val mockFraudIntelData = listOf(
    FraudIntel(
        title = "Phishing Campaigns via SMS (Smishing)",
        description = "Attackers send SMS messages posing as legitimate entities (e.g., banks, delivery services) to steal credentials or install malware.",
        category = "Social Engineering",
        severity = "HIGH",
        prevention = "Never click links in unsolicited SMS. Verify via official app/website. Use SMS filtering apps.",
        targetSectors = listOf("Retail", "Banking", "Logistics"),
        mitreTTPs = listOf("T1566.002", "T1059.005")
    ),
    FraudIntel(
        title = "Business Email Compromise (BEC)",
        description = "Scammers spoof or compromise executive email accounts to request unauthorized wire transfers or sensitive data.",
        category = "Financial Fraud",
        severity = "CRITICAL",
        prevention = "Implement strict multi-factor authentication (MFA) and secondary verification channels for financial transactions.",
        targetSectors = listOf("Finance", "Corporate", "Real Estate"),
        mitreTTPs = listOf("T1566.001", "T1114.002")
    ),
    FraudIntel(
        title = "Ransomware as a Service (RaaS)",
        description = "Cybercriminals distribute ransomware toolkits on the dark web, allowing affiliates to deploy attacks and extort victims.",
        category = "Malware / Extortion",
        severity = "CRITICAL",
        prevention = "Maintain offline encrypted backups, update systems regularly, and implement Zero Trust architecture.",
        targetSectors = listOf("Healthcare", "Education", "Government"),
        mitreTTPs = listOf("T1486", "T1078")
    ),
    FraudIntel(
        title = "Investment / Crypto Scams (Pig Butchering)",
        description = "Scammers build trust with victims over time through dating apps or social media, eventually luring them into fake cryptocurrency investments.",
        category = "Confidence Fraud",
        severity = "HIGH",
        prevention = "Be skeptical of unsolicited investment advice. Verify exchanges and never send funds to unknown entities.",
        targetSectors = listOf("Individuals", "Crypto Investors"),
        mitreTTPs = listOf("T1583", "T1584")
    ),
    FraudIntel(
        title = "Tech Support Scams",
        description = "Fraudsters use pop-ups or cold calls claiming the victim's computer is infected, demanding payment for fake support services.",
        category = "Social Engineering",
        severity = "MEDIUM",
        prevention = "Recognize that legitimate tech companies will not cold-call you. Use ad-blockers and avoid dialing pop-up numbers.",
        targetSectors = listOf("Consumers", "Elderly"),
        mitreTTPs = listOf("T1566.003", "T1499")
    )
)

@Composable
fun CyberFraudScreen(viewModel: CyberLabViewModel) {
    var searchQuery by remember { mutableStateOf("") }
    
    val filteredData = remember(searchQuery) {
        if (searchQuery.isBlank()) mockFraudIntelData
        else mockFraudIntelData.filter { 
            it.title.contains(searchQuery, ignoreCase = true) || 
            it.category.contains(searchQuery, ignoreCase = true) ||
            it.description.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBlack)
            .padding(12.dp)
    ) {
        // Advanced Header with Live Stats
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(CyberDark)
                .border(1.dp, CyberBorder, RoundedCornerShape(6.dp))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Fraud Intel",
                tint = CyberAmber,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "CYBER FRAUD INTELLIGENCE",
                    style = MaterialTheme.typography.titleMedium,
                    color = CyberAmber,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Live threat vectors & prevention strategies",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }
            // Small stat box
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "ACTIVE CAMPAIGNS",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "42,891",
                    style = MaterialTheme.typography.titleMedium,
                    color = CyberCrimson,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Filter
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .border(1.dp, CyberBorder, RoundedCornerShape(6.dp)),
            placeholder = { Text("Search vectors, sectors, or TTPs...", color = TextMuted, fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = CyberCyan) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = CyberDark,
                unfocusedContainerColor = CyberDark,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                cursorColor = CyberCyan,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Content List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredData, key = { it.title }) { intel ->
                Box(modifier = Modifier.animateItem()) {
                    FraudIntelCard(intel)
                }
            }
        }
    }
}

@Composable
fun FraudIntelCard(intel: FraudIntel) {
    val severityColor = when (intel.severity.uppercase()) {
        "CRITICAL" -> CyberCrimson
        "HIGH" -> CyberAmber
        "MEDIUM" -> NeonGreen
        else -> CyberCyan
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(CyberDark)
            .border(1.dp, CyberBorder, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = intel.category.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = CyberCyan,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(severityColor.copy(alpha = 0.15f))
                    .border(1.dp, severityColor.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "SEVERITY: ${intel.severity}",
                    style = MaterialTheme.typography.labelSmall,
                    color = severityColor,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
        
        Spacer(modifier = Modifier.height(6.dp))
        
        Text(
            text = intel.title,
            style = MaterialTheme.typography.titleSmall,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = intel.description,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            lineHeight = 16.sp
        )
        
        Spacer(modifier = Modifier.height(8.dp))

        // Meta Tags (Targets & TTPs)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            intel.targetSectors.forEach { sector ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(CyberSurfaceVariant)
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = sector.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
            intel.mitreTTPs.forEach { ttp ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(CyberCrimson.copy(alpha = 0.1f))
                        .border(0.5.dp, CyberCrimson.copy(alpha = 0.4f), RoundedCornerShape(3.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = ttp,
                        style = MaterialTheme.typography.labelSmall,
                        color = CyberCrimson,
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(10.dp))
        
        // Prevention Block
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(CyberBlack)
                .border(1.dp, NeonGreen.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                .padding(8.dp)
        ) {
            Column {
                Text(
                    text = "PREVENTION STRATEGY",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeonGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = intel.prevention,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextPrimary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }
    }
}
