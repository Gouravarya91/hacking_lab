package com.example.data.repository

import com.example.data.model.CVEItem
import com.example.data.model.RiskLevel

object CveDataSource {
    fun getCves(): List<CVEItem> = listOf(
        CVEItem(
            id = "CVE-2024-3094",
            title = "XZ Utils liblzma SSH Backdoor Remote Code Execution",
            cvssScore = 10.0f,
            severity = RiskLevel.CRITICAL,
            affectedSoftware = "XZ Utils versions 5.6.0 and 5.6.1 (liblzma)",
            attackVector = "Network (AV:N)",
            complexity = "Low (AC:L)",
            privilegesRequired = "None (PR:N)",
            patchStatus = "Patched (Downgrade to 5.4.x)",
            description = "A sophisticated multi-stage supply chain backdoor was intentionally injected into upstream tarballs of XZ Utils by a compromised maintainer account. The backdoor hooks into OpenSSH daemon (sshd) via RSA public key decryption routines to allow unauthenticated remote attackers to execute arbitrary system commands with root privileges.",
            remediation = "Downgrade xz-utils to 5.4.6 or update to verified 5.6.2+. Audit all build pipelines and verify artifact GPG signatures.",
            references = listOf("https://nvd.nist.gov/vuln/detail/CVE-2024-3094", "https://www.cisa.gov/news-events/alerts/2024/03/29/open-source-xz-utils-backdoor")
        ),
        CVEItem(
            id = "CVE-2024-1709",
            title = "ConnectWise ScreenConnect Authentication Bypass",
            cvssScore = 10.0f,
            severity = RiskLevel.CRITICAL,
            affectedSoftware = "ConnectWise ScreenConnect <= 23.9.7",
            attackVector = "Network (AV:N)",
            complexity = "Low (AC:L)",
            privilegesRequired = "None (PR:N)",
            patchStatus = "Patched in 23.9.8+",
            description = "An authentication bypass vulnerability allowing an unauthenticated remote attacker to access the initial setup wizard (/SetupWizard.aspx) on already-configured instances, create administrative accounts, and achieve full remote code execution.",
            remediation = "Immediately upgrade ScreenConnect servers to version 23.9.8 or higher. Review user management logs for newly spawned local administrative accounts.",
            references = listOf("https://www.huntress.com/blog/slashandgrab-connectwise-screenconnect-rce")
        ),
        CVEItem(
            id = "CVE-2023-48788",
            title = "Fortinet FortiClient EMS SQL Injection Remote Code Execution",
            cvssScore = 9.8f,
            severity = RiskLevel.CRITICAL,
            affectedSoftware = "FortiClientEMS 7.0.1 - 7.0.10, 7.2.0 - 7.2.2",
            attackVector = "Network (AV:N)",
            complexity = "Low (AC:L)",
            privilegesRequired = "None (PR:N)",
            patchStatus = "Patched in 7.0.11+ and 7.2.3+",
            description = "Improper neutralization of special elements in SQL commands within the FCMDaemon component allows an unauthenticated remote attacker to execute arbitrary SQL commands and trigger remote code execution through xp_cmdshell.",
            remediation = "Upgrade to FortiClientEMS 7.2.3 or 7.0.11. Disable MSSQL xp_cmdshell procedure and restrict TCP port 8013.",
            references = listOf("https://www.fortiguard.com/psirt/FG-IR-24-007")
        ),
        CVEItem(
            id = "CVE-2021-44228",
            title = "Apache Log4j2 JNDI Injection (Log4Shell)",
            cvssScore = 10.0f,
            severity = RiskLevel.CRITICAL,
            affectedSoftware = "Apache Log4j versions 2.0-beta9 to 2.14.1",
            attackVector = "Network (AV:N)",
            complexity = "Low (AC:L)",
            privilegesRequired = "None (PR:N)",
            patchStatus = "Patched in 2.17.1+",
            description = "Apache Log4j2 JNDI lookup features did not protect against attacker-controlled LDAP and RMI endpoints. An attacker who can log arbitrary strings (e.g. User-Agent or search inputs) can trigger remote class loading and arbitrary code execution on the server.",
            remediation = "Update Log4j2 dependencies to 2.17.1 or higher. For legacy deployments, set system property `log4j2.formatMsgNoLookups=true` or remove JndiLookup class from classpath.",
            references = listOf("https://logging.apache.org/log4j/2.x/security.html")
        ),
        CVEItem(
            id = "CVE-2023-34362",
            title = "MOVEit Transfer SQL Injection / RCE",
            cvssScore = 9.8f,
            severity = RiskLevel.CRITICAL,
            affectedSoftware = "Progress MOVEit Transfer before 2023.0.1",
            attackVector = "Network (AV:N)",
            complexity = "Low (AC:L)",
            privilegesRequired = "None (PR:N)",
            patchStatus = "Patched",
            description = "SQL injection vulnerability in MOVEit Transfer web application that allows unauthenticated remote attackers to gain unauthorized access to databases, alter database elements, and drop web shells (LEMURLOOT) for data exfiltration.",
            remediation = "Apply manufacturer security updates immediately. Block external inbound traffic on ports 80 and 443 until patched.",
            references = listOf("https://www.progress.com/security/moveit-transfer-vulnerability")
        ),
        CVEItem(
            id = "CVE-2023-38606",
            title = "Operation Triangulation iOS Kernel Memory Corruption",
            cvssScore = 8.8f,
            severity = RiskLevel.HIGH,
            affectedSoftware = "Apple iOS before 16.6, iPadOS before 16.6, macOS Ventura before 13.5",
            attackVector = "Local (AV:L)",
            complexity = "Low (AC:L)",
            privilegesRequired = "Low (PR:L)",
            patchStatus = "Patched",
            description = "An app may be able to modify sensitive kernel state. Attackers utilized undocumented Apple hardware MMIO registers to bypass hardware memory page protection.",
            remediation = "Update Apple devices to iOS 16.6 / macOS 13.5 or later.",
            references = listOf("https://securelist.com/operation-triangulation-the-last-hardware-mystery/111160/")
        ),
        CVEItem(
            id = "CVE-2024-21413",
            title = "Microsoft Outlook Moniker Link Remote Code Execution (#MonikerLink)",
            cvssScore = 9.8f,
            severity = RiskLevel.CRITICAL,
            affectedSoftware = "Microsoft Office 2016, 2019, LTSC 2021, Microsoft 365 Apps",
            attackVector = "Network (AV:N)",
            complexity = "Low (AC:L)",
            privilegesRequired = "None (PR:N)",
            patchStatus = "Patched in Feb 2024 Patch Tuesday",
            description = "An attacker who successfully exploited this vulnerability could bypass Protected View and trigger local NTLM credential leaks or arbitrary code execution by crafting a malicious file:// link with an exclamation point moniker tag.",
            remediation = "Apply Microsoft February 2024 security updates across all Office client installations.",
            references = listOf("https://msrc.microsoft.com/update-guide/vulnerability/CVE-2024-21413")
        )
    )
}
