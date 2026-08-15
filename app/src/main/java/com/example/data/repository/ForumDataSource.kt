package com.example.data.repository

import com.example.data.model.ForumPost

object ForumDataSource {
    fun getDefaultPosts(): List<ForumPost> = listOf(
        ForumPost(
            id = "POST-01",
            author = "RedTeam_Lead_0x",
            authorRole = "Lead Red Teamer",
            verifiedBadge = true,
            title = "Analysis: Bypassing Modern EDR Hooks via Direct System Calls",
            category = "Zero-Day",
            content = "Modern Endpoint Detection and Response (EDR) solutions place inline userland hooks in `ntdll.dll` functions like `NtCreateThreadEx` and `NtAllocateVirtualMemory`. In our latest simulation, we bypassed these detections by dynamically extracting SSNs (System Service Numbers) from disk and executing the `syscall` assembly opcode directly in memory.",
            codeSnippet = """
                // Direct Syscall Assembly Stub
                mov r10, rcx
                mov eax, [ssn_NtAllocateVirtualMemory]
                syscall
                ret
            """.trimIndent(),
            upvotes = 42,
            timestamp = "2026-08-13 18:40",
            isUpvoted = false
        ),
        ForumPost(
            id = "POST-02",
            author = "CyberSec_Sarah",
            authorRole = "SOC Level 3 Analyst",
            verifiedBadge = true,
            title = "Hunting Volt Typhoon Activity: Practical Sigma & Splunk Queries",
            category = "Malware Analysis",
            content = "Volt Typhoon heavily relies on Living-off-the-Land (LotL) tools rather than custom binaries. We created high-fidelity detection rules focusing on anomalous `wmic process get`, `netsh interface portproxy`, and `vssadmin delete shadows` execution sequences within 60-second windows.",
            codeSnippet = """
                index=sysmon EventCode=1 Image="*\\wmic.exe" 
                CommandLine="*process call create*" 
                | stats count by host, User, ParentImage
            """.trimIndent(),
            upvotes = 38,
            timestamp = "2026-08-13 14:15",
            isUpvoted = true
        ),
        ForumPost(
            id = "POST-03",
            author = "Binary_Exploit_God",
            authorRole = "CTF Champion",
            verifiedBadge = true,
            title = "DEFCON CTF Qualification Walkthrough: Heap Feng Shui & tcache Poisoning",
            category = "CTF",
            content = "Here is the writeup for the glibc 2.35 heap challenge. By exploiting a single 1-byte null overflow (off-by-null), we consolidated small chunks across borders, overwrote the `tcache_entry` pointer with the target `__free_hook`, and popped a shell.",
            codeSnippet = """
                p.sendlineafter(b"> ", b"1") # Allocate 0x80
                p.sendlineafter(b"> ", b"2") # Poison tcache forward pointer
                p.sendafter(b"Data: ", p64(target_address))
            """.trimIndent(),
            upvotes = 65,
            timestamp = "2026-08-12 21:00",
            isUpvoted = false
        ),
        ForumPost(
            id = "POST-04",
            author = "Cloud_Defender_Alex",
            authorRole = "DevSecOps Architect",
            verifiedBadge = false,
            title = "Hardening Kubernetes Clusters Against Container Escapes (CVE-2024-21626)",
            category = "Blue Team",
            content = "The runc file descriptor leak (CVE-2024-21626) allowed attackers to access host filesystem paths from inside a container. Make sure you are enforcing AppArmor/SELinux profiles, running non-root user namespaces, and disabling `CAP_SYS_ADMIN` in all production pods.",
            codeSnippet = """
                securityContext:
                  readOnlyRootFilesystem: true
                  allowPrivilegeEscalation: false
                  runAsNonRoot: true
                  capabilities:
                    drop: ["ALL"]
            """.trimIndent(),
            upvotes = 29,
            timestamp = "2026-08-11 11:30",
            isUpvoted = false
        )
    )
}
