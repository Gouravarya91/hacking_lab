package com.example.data.repository

import com.example.data.model.CTFChallenge
import com.example.data.model.Difficulty

object CtfDataSource {
    fun getChallenges(): List<CTFChallenge> = listOf(
        CTFChallenge(
            id = "CTF-001",
            title = "Web Security: SQL Injection Auth Bypass",
            category = "Web Exploitation",
            points = 100,
            difficulty = Difficulty.BEGINNER,
            prompt = "An authentication backend uses the following SQL query:\n`SELECT * FROM users WHERE username = 'INPUT_USER' AND password = 'INPUT_PASS'`\n\nWhich payload in the username field will force the SQL engine to evaluate TRUE without requiring a valid password?",
            codeOrLogSnippet = "SELECT * FROM users WHERE username = 'admin' OR '1'='1' --' AND password = 'xxx'",
            options = listOf(
                "admin' OR '1'='1' --",
                "<script>alert(1)</script>",
                "../../etc/passwd",
                "admin; DROP TABLE users;"
            ),
            correctOptionIndex = 0,
            hint = "Look for the classic tautology operator that comments out the remainder of the SQL query.",
            explanation = "The payload `admin' OR '1'='1' --` breaks out of the single quote string, injects an always-true boolean statement `OR '1'='1'`, and uses `--` to comment out the subsequent password evaluation clause."
        ),
        CTFChallenge(
            id = "CTF-002",
            title = "Network Forensics: Identifying Port Scanning Type",
            category = "Network Forensics",
            points = 120,
            difficulty = Difficulty.BEGINNER,
            prompt = "In a Wireshark capture, an external IP sends a TCP packet with `SYN=1, ACK=0`. The target server responds with `SYN=1, ACK=1`. Immediately, the external IP sends `RST=1`.\n\nWhat type of scan is this?",
            codeOrLogSnippet = "Attacker -> Target: [SYN]\nTarget -> Attacker: [SYN, ACK]\nAttacker -> Target: [RST]",
            options = listOf(
                "TCP Connect Full Handshake Scan (-sT)",
                "TCP SYN Stealth / Half-Open Scan (-sS)",
                "UDP Null Probe Scan (-sN)",
                "FIN / Xmas Tree Scan (-sF)"
            ),
            correctOptionIndex = 1,
            hint = "The 3-way handshake is never completed; it is terminated early with a RST flag to avoid full socket creation.",
            explanation = "A TCP SYN Stealth scan (Nmap `-sS`) sends a SYN packet, waits for SYN-ACK to verify the port is open, and immediately transmits a RST packet instead of the final ACK, preventing application-level connection logging."
        ),
        CTFChallenge(
            id = "CTF-003",
            title = "Cryptography: Base64 & Rot13 Decoding",
            category = "Cryptography",
            points = 150,
            difficulty = Difficulty.INTERMEDIATE,
            prompt = "Analyze the intercepted encoded string below. It has been encoded in Base64 and then shifted with ROT13.\n\nEncoded Token: `Q1lCRVJ7cjAwN19iM3kwbmRfYjF0c30=`\n\nWhat is the decrypted plaintext flag?",
            codeOrLogSnippet = "Token: Q1lCRVJ7cjAwN19iM3kwbmRfYjF0c30=\nBase64 Decoded: CYBER{r007_b3y0nd_b1ts}",
            flagAnswer = "CYBER{r007_b3y0nd_b1ts}",
            options = listOf(
                "CYBER{r007_b3y0nd_b1ts}",
                "CYBER{h4ck_th3_pl4n3t}",
                "FLAG{d3c0d3_m4st3r}",
                "CYBER{s3cur1ty_l4b_pr0}"
            ),
            correctOptionIndex = 0,
            hint = "First decode the Base64 padding (`=`) to retrieve the ASCII characters.",
            explanation = "Decoding the Base64 string `Q1lCRVJ7cjAwN19iM3kwbmRfYjF0c30=` directly yields the flag `CYBER{r007_b3y0nd_b1ts}`."
        ),
        CTFChallenge(
            id = "CTF-004",
            title = "Linux Incident Response: Rogue SUID Binary",
            category = "Forensics",
            points = 200,
            difficulty = Difficulty.INTERMEDIATE,
            prompt = "An attacker established persistence on a Linux server by setting the SUID bit on a binary. Which command will locate all files with SUID bit set (`4000`) owned by root?",
            codeOrLogSnippet = "-rwsr-xr-x 1 root root 124968 Jan 10 14:22 /tmp/.backdoor",
            options = listOf(
                "find / -perm -4000 -user root -type f 2>/dev/null",
                "ls -la /root/suid",
                "chmod 4755 /bin/*",
                "grep -r \"SUID\" /etc/passwd"
            ),
            correctOptionIndex = 0,
            hint = "Use the `find` utility with permission mask `-perm -4000` and discard permission denied errors.",
            explanation = "`find / -perm -4000 -user root -type f 2>/dev/null` searches the entire filesystem for files with the SUID bit set that are owned by root, redirecting error messages to `/dev/null`."
        ),
        CTFChallenge(
            id = "CTF-005",
            title = "Cloud Security: AWS IAM Privilege Escalation",
            category = "Cloud & DevSecOps",
            points = 250,
            difficulty = Difficulty.ADVANCED,
            prompt = "A compromised AWS IAM user has the permission `iam:CreateAccessKey` on arbitrary users. How can the attacker escalate to full AdministratorAccess?",
            codeOrLogSnippet = "{\n  \"Effect\": \"Allow\",\n  \"Action\": \"iam:CreateAccessKey\",\n  \"Resource\": \"arn:aws:iam::*:user/admin*\"\n}",
            options = listOf(
                "Generate a new Access Key ID & Secret for an existing Administrator IAM user",
                "Delete the CloudTrail logging bucket",
                "Reboot the EC2 instance with AWS CLI",
                "Modify the VPC route table"
            ),
            correctOptionIndex = 0,
            hint = "If you can create credentials for another user, you can assume their permissions.",
            explanation = "With `iam:CreateAccessKey` permissions targeted at an administrative IAM user account, an attacker can generate a new API key pair for the admin user and authenticate as full Administrator."
        ),
        CTFChallenge(
            id = "CTF-006",
            title = "Reverse Engineering: Buffer Overflow Instruction Pointer",
            category = "Reverse Engineering",
            points = 300,
            difficulty = Difficulty.ADVANCED,
            prompt = "In x86-64 architecture, what CPU register holds the address of the next instruction to be executed, and is the primary target in standard stack buffer overflow control flow hijacking?",
            codeOrLogSnippet = "RIP: 0x00007fffffffe420 ('AAAAAAAAAAAAAAAA')",
            options = listOf(
                "RIP (Instruction Pointer)",
                "RSP (Stack Pointer)",
                "RBP (Base Pointer)",
                "RAX (Accumulator Register)"
            ),
            correctOptionIndex = 0,
            hint = "In 32-bit it is EIP, in 64-bit it starts with R.",
            explanation = "The `RIP` register in 64-bit x86-64 stores the instruction pointer. Overwriting the saved return address on the stack causes `RIP` to jump to attacker-controlled shellcode or a ROP chain when the function returns."
        ),
        CTFChallenge(
            id = "CTF-007",
            title = "Malware Analysis: Process Injection",
            category = "Malware Analysis",
            points = 350,
            difficulty = Difficulty.ADVANCED,
            prompt = "A suspected malware sample calls 'VirtualAllocEx' followed by 'WriteProcessMemory' and 'CreateRemoteThread' targeting 'explorer.exe'. What technique is being utilized?",
            codeOrLogSnippet = "hProcess = OpenProcess(PROCESS_ALL_ACCESS, FALSE, 1337);\npRemoteCode = VirtualAllocEx(hProcess, NULL, size, MEM_COMMIT, PAGE_EXECUTE_READWRITE);",
            options = listOf(
                "DLL Search Order Hijacking",
                "Classic DLL Injection / Process Injection",
                "Process Hollowing",
                "Hooking (SetWindowsHookEx)"
            ),
            correctOptionIndex = 1,
            hint = "The sequence of allocating memory in a remote process and executing a remote thread is the classic hallmark of this injection type.",
            explanation = "This sequence of Windows APIs (VirtualAllocEx -> WriteProcessMemory -> CreateRemoteThread) is the standard method for classic Process Injection (often DLL Injection), allowing malware to run its code within the memory space of a legitimate process like explorer.exe."
        ),
        CTFChallenge(
            id = "CTF-008",
            title = "Web Exploitation: Directory Traversal",
            category = "Web Exploitation",
            points = 150,
            difficulty = Difficulty.BEGINNER,
            prompt = "An image viewer application loads files via a parameter: `http://target.com/view.php?file=image1.png`. An attacker wants to read the Linux password file. Which payload should be appended?",
            codeOrLogSnippet = "view.php?file=../../../../../../etc/passwd",
            options = listOf(
                "?file=' OR 1=1--",
                "?file=../../../../../../etc/passwd",
                "?file=http://evil.com/malware.sh",
                "?file=<script>alert('XSS')</script>"
            ),
            correctOptionIndex = 1,
            hint = "Use dot-dot-slash sequence to navigate up to the root directory, then access /etc/passwd.",
            explanation = "The payload `../../../../../../etc/passwd` uses relative pathing (Directory Traversal) to escape the web root and read the sensitive /etc/passwd file from the underlying OS."
        )
    )
}
