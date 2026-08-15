package com.example.data.repository

import com.example.data.model.RiskLevel
import com.example.data.model.ThreatIncident

object ThreatDataSource {
    fun getThreats(): List<ThreatIncident> = listOf(
        ThreatIncident(
            id = "THREAT-2026-8941",
            threatType = "Ransomware Outbreak",
            actorOrCampaign = "LockBit 4.0 (Green variant)",
            sourceCountry = "Eastern Europe",
            sourceCoords = Pair(0.62f, 0.28f),
            targetCountry = "United States",
            targetCoords = Pair(0.24f, 0.36f),
            targetSector = "Healthcare & Hospital Systems",
            severity = RiskLevel.CRITICAL,
            mitreTechniques = listOf("T1486 Data Encrypted for Impact", "T1078 Valid Accounts", "T1562 Impair Defenses"),
            iocs = listOf("185.220.101.44", "d41d8cd98f00b204e9800998ecf8427e", "c2-sync.lockbit4-onion.to"),
            timestamp = "Just Now",
            summary = "High volume double-extortion ransomware campaign actively encrypting hospital ESXi clusters via compromised VPN credentials."
        ),
        ThreatIncident(
            id = "THREAT-2026-7712",
            threatType = "APT Nation-State Infiltration",
            actorOrCampaign = "Volt Typhoon (UNC3236)",
            sourceCountry = "East Asia",
            sourceCoords = Pair(0.78f, 0.38f),
            targetCountry = "Guam / United States",
            targetCoords = Pair(0.85f, 0.48f),
            targetSector = "Critical Infrastructure (Power & Water)",
            severity = RiskLevel.CRITICAL,
            mitreTechniques = listOf("T1059.001 PowerShell", "T1046 Network Discovery", "T1070 Indicator Removal"),
            iocs = listOf("103.145.13.9", "router-vpn.living-off-the-land.net", "sha256:7f83b1657ff1fc53b92dc18148a1d65dfc2d4b1fa3d677284addd200126d9069"),
            timestamp = "4 min ago",
            summary = "Living-off-the-land (LotL) stealth infiltration targeting OT industrial routers and SOHO network appliances to pre-position for disruption."
        ),
        ThreatIncident(
            id = "THREAT-2026-6503",
            threatType = "DDoS Volumetric Swarm",
            actorOrCampaign = "Mirai / Gorilla Botnet Variant",
            sourceCountry = "South America",
            sourceCoords = Pair(0.32f, 0.72f),
            targetCountry = "Germany / EU",
            targetCoords = Pair(0.52f, 0.30f),
            targetSector = "Financial Services & Banking",
            severity = RiskLevel.HIGH,
            mitreTechniques = listOf("T1498 Network Denial of Service", "T1499 Endpoint DoS"),
            iocs = listOf("45.154.255.81", "194.26.29.112", "udp-flood-target.bank.de"),
            timestamp = "12 min ago",
            summary = "Over 4.2 Tbps HTTPS and UDP amplification attack flooding tier-1 European financial payment gateways."
        ),
        ThreatIncident(
            id = "THREAT-2026-5120",
            threatType = "Zero-Day Exploitation",
            actorOrCampaign = "Lazarus Group (Hidden Cobra)",
            sourceCountry = "East Asia",
            sourceCoords = Pair(0.82f, 0.32f),
            targetCountry = "Singapore / Global",
            targetCoords = Pair(0.79f, 0.58f),
            targetSector = "Cryptocurrency & Web3 Exchanges",
            severity = RiskLevel.CRITICAL,
            mitreTechniques = listOf("T1190 Exploit Public-Facing App", "T1003 OS Credential Dumping", "T1027 Obfuscated Files"),
            iocs = listOf("91.240.118.230", "blockchain-bridge-validator.xyz", "md5:c4ca4238a0b923820dcc509a6f75849b"),
            timestamp = "26 min ago",
            summary = "Weaponized cross-chain bridge zero-day exploit targeting decentralized liquidity pools via smart contract reentrancy."
        ),
        ThreatIncident(
            id = "THREAT-2026-4409",
            threatType = "Supply Chain Backdoor",
            actorOrCampaign = "APT29 (Cozy Bear / Nobelium)",
            sourceCountry = "Eastern Europe",
            sourceCoords = Pair(0.66f, 0.22f),
            targetCountry = "United Kingdom",
            targetCoords = Pair(0.48f, 0.28f),
            targetSector = "Government & Foreign Affairs",
            severity = RiskLevel.CRITICAL,
            mitreTechniques = listOf("T1195 Supply Chain Compromise", "T1134 Access Token Manipulation"),
            iocs = listOf("193.106.191.17", "cloud-token-verify.azure-auth.org", "sha256:e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"),
            timestamp = "45 min ago",
            summary = "Compromised OAuth tenant applications abused to bypass MFA and exfiltrate cloud mailbox archives."
        ),
        ThreatIncident(
            id = "THREAT-2026-3101",
            threatType = "Cloud IAM Credential Leak",
            actorOrCampaign = "Automated Shodan Botnet",
            sourceCountry = "Global / Distributed",
            sourceCoords = Pair(0.40f, 0.45f),
            targetCountry = "Australia",
            targetCoords = Pair(0.86f, 0.78f),
            targetSector = "Telecommunications",
            severity = RiskLevel.MEDIUM,
            mitreTechniques = listOf("T1552 Unsecured Credentials", "T1530 Data from Cloud Storage"),
            iocs = listOf("198.51.100.77", "s3-public-bucket-telecom.ap-southeast-2.amazonaws.com"),
            timestamp = "1 hr ago",
            summary = "Continuous scanning bots scraping public GitHub commits for hardcoded AWS session tokens and Kubernetes secrets."
        )
    )
}
