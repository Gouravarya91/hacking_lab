package com.example.data.sandbox

import com.example.data.model.TerminalLine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class TerminalSandboxEngine(
    val vfs: VirtualFileSystem = VirtualFileSystem(),
    val onXpAwarded: ((points: Int, flag: String?) -> Unit)? = null
) {
    val environmentVariables = mutableMapOf(
        "USER" to "root",
        "LOGNAME" to "root",
        "HOME" to "/root",
        "SHELL" to "/bin/bash",
        "HOSTNAME" to "cyberlab-node-01",
        "PATH" to "/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/opt/tools",
        "TERM" to "xterm-256color",
        "LANG" to "en_US.UTF-8"
    )

    val history = mutableListOf<String>()

    fun getPrompt(): String {
        val user = environmentVariables["USER"] ?: "root"
        val host = environmentVariables["HOSTNAME"] ?: "cyberlab-node-01"
        val path = vfs.getPromptPath()
        val symbol = if (user == "root") "#" else "$"
        return "$user@$host:$path$symbol"
    }

    suspend fun processCommand(input: String): List<TerminalLine> {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return emptyList()

        history.add(trimmed)
        val lines = mutableListOf<TerminalLine>()

        fun add(type: TerminalLine.LineType, content: String) {
            lines.add(
                TerminalLine(
                    id = UUID.randomUUID().toString(),
                    type = type,
                    content = content
                )
            )
        }

        // Handle simple redirection (e.g. echo "xyz" > file.txt or >> file.txt)
        if (trimmed.contains(">")) {
            val append = trimmed.contains(">>")
            val splitToken = if (append) ">>" else ">"
            val parts = trimmed.split(splitToken, limit = 2)
            val cmdPart = parts[0].trim()
            val filePart = parts[1].trim()

            if (cmdPart.startsWith("echo")) {
                val rawText = cmdPart.removePrefix("echo").trim().trim('"', '\'')
                val success = vfs.writeFile(filePart, rawText, append = append)
                if (success) {
                    add(TerminalLine.LineType.SUCCESS, "[+] Written to $filePart")
                } else {
                    add(TerminalLine.LineType.ERROR, "bash: $filePart: Permission denied or invalid path")
                }
                return lines
            }
        }

        val tokens = trimmed.split("\\s+".toRegex())
        val command = tokens.first().lowercase()
        val args = if (tokens.size > 1) tokens.subList(1, tokens.size) else emptyList()

        when (command) {
            "help", "?" -> {
                add(TerminalLine.LineType.BANNER, "==================================================================")
                add(TerminalLine.LineType.BANNER, "[*] CYBER_LAB_PRO VIRTUAL SANDBOX & BASH INTERPRETER")
                add(TerminalLine.LineType.BANNER, "==================================================================")
                add(TerminalLine.LineType.OUTPUT, """
                    [+] RECONNAISSANCE & OSINT:
                      nmap [flags] <target>     - Network port & vulnerability scanner
                      gobuster dir -u <url>     - Web directory & subdomain brute-forcer
                      nikto -h <target>         - Web server misconfiguration scanner
                      whois <domain>            - Domain registration & registrar info
                      dig / traceroute / ping   - Network diagnostics & routing hops
                      theharvester -d <domain>  - Scraping emails, subdomains & employee profiles
                      sherlock <username>       - OSINT social media account footprinting
                      curl / wget <url>         - HTTP request inspector & artifact fetcher

                    [+] EXPLOITATION & CRACKING:
                      sqlmap -u <url>           - Automated SQL injection & DB dump
                      hydra -l <user> -P <pass> - Network brute-force (ssh, ftp, rdp)
                      john <hashfile>           - Password hash cracker (sha512, md5)
                      hashcat -m <mode> <hash>  - GPU-accelerated hash cracker simulation
                      msfconsole                - Metasploit framework interactive runner
                      nc -lvnp <port>           - Netcat listener simulation

                    [+] CLOUD & VULNERABILITY AUDITING:
                      prowler aws/gcp           - Cloud CIS security benchmark scanner
                      trivy image <name>        - Container & CVE image vulnerability auditor

                    [+] SYSTEM, FORENSICS & FILE OPERATIONS:
                      ls / cd / pwd / cat       - Filesystem navigation & inspection
                      mkdir / touch / rm / echo - File & directory management
                      grep <pattern> <file>     - Search text in files
                      ps aux / top / kill <pid> - Process list & task management
                      netstat -tulnp / ss -lntu - Active sockets & listening services
                      iptables -L / ufw status  - Firewall rules & traffic filtering
                      whoami / id / uname -a    - Privilege & kernel identification
                      env / export VAR=VAL      - Shell environment management
                      history / clear           - Terminal history & screen wipe
                      neofetch / cmatrix        - Hacker system info & visual matrix

                    [+] AI & CTF INTEGRATION:
                      ai <question>             - Direct cyber copilot consultation
                      submit <flag>             - Verify CTF challenge flag directly in CLI
                """.trimIndent())
            }

            "clear" -> {
                // Return clear signal
                add(TerminalLine.LineType.SYSTEM, "__CLEAR__")
            }

            // Virtual Filesystem Commands
            "pwd" -> {
                add(TerminalLine.LineType.OUTPUT, vfs.currentPath)
            }

            "cd" -> {
                val target = args.firstOrNull() ?: "~"
                if (vfs.changeDirectory(target)) {
                    // directory changed
                } else {
                    add(TerminalLine.LineType.ERROR, "bash: cd: $target: No such file or directory")
                }
            }

            "ls", "dir" -> {
                val showAll = args.contains("-a") || args.contains("-la") || args.contains("-al")
                val longFormat = args.contains("-l") || args.contains("-la") || args.contains("-al")
                val targetPath = args.lastOrNull { !it.startsWith("-") } ?: vfs.currentPath

                val nodes = vfs.listDirectory(targetPath)
                if (nodes == null) {
                    add(TerminalLine.LineType.ERROR, "ls: cannot access '$targetPath': No such file or directory")
                } else {
                    val filtered = if (showAll) nodes else nodes.filter { !it.name.startsWith(".") }
                    if (longFormat) {
                        add(TerminalLine.LineType.SYSTEM, "total ${filtered.size * 4}K")
                        val sb = StringBuilder()
                        for (n in filtered) {
                            val typeChar = if (n.isDirectory) "d" else "-"
                            val perm = if (n.isDirectory) "rwxr-xr-x" else "rw-r--r--"
                            val sizeStr = n.size.toString().padStart(6)
                            val nameDecorated = if (n.isDirectory) "${n.name}/" else n.name
                            sb.appendLine("$typeChar$perm 1 ${n.owner} ${n.group} $sizeStr Aug 13 22:00 $nameDecorated")
                        }
                        add(TerminalLine.LineType.TABLE, sb.toString().trimEnd())
                    } else {
                        val names = filtered.joinToString("  ") { if (it.isDirectory) "${it.name}/" else it.name }
                        add(TerminalLine.LineType.OUTPUT, names)
                    }
                }
            }

            "cat" -> {
                if (args.isEmpty()) {
                    add(TerminalLine.LineType.ERROR, "cat: missing file operand")
                } else {
                    val content = vfs.readFile(args[0])
                    if (content != null) {
                        add(TerminalLine.LineType.OUTPUT, content)
                    } else {
                        add(TerminalLine.LineType.ERROR, "cat: ${args[0]}: No such file or is a directory")
                    }
                }
            }

            "echo" -> {
                val text = args.joinToString(" ").trim('"', '\'')
                add(TerminalLine.LineType.OUTPUT, text)
            }

            "mkdir" -> {
                if (args.isEmpty()) {
                    add(TerminalLine.LineType.ERROR, "mkdir: missing operand")
                } else {
                    val target = args[0]
                    if (vfs.mkdirp(target)) {
                        add(TerminalLine.LineType.SUCCESS, "[+] Created directory: $target")
                    } else {
                        add(TerminalLine.LineType.ERROR, "mkdir: cannot create directory '$target': Permission denied or exists")
                    }
                }
            }

            "touch" -> {
                if (args.isEmpty()) {
                    add(TerminalLine.LineType.ERROR, "touch: missing file operand")
                } else {
                    val target = args[0]
                    vfs.writeFile(target, "")
                    add(TerminalLine.LineType.SUCCESS, "[+] Created file: $target")
                }
            }

            "rm" -> {
                if (args.isEmpty()) {
                    add(TerminalLine.LineType.ERROR, "rm: missing operand")
                } else {
                    val target = args.last { !it.startsWith("-") }
                    if (vfs.deleteNode(target)) {
                        add(TerminalLine.LineType.SUCCESS, "[+] Removed $target")
                    } else {
                        add(TerminalLine.LineType.ERROR, "rm: cannot remove '$target': No such file or protected")
                    }
                }
            }

            "grep" -> {
                if (args.size < 2) {
                    add(TerminalLine.LineType.ERROR, "Usage: grep <pattern> <file>")
                } else {
                    val pattern = args[0].trim('"', '\'')
                    val file = args[1]
                    val content = vfs.readFile(file)
                    if (content == null) {
                        add(TerminalLine.LineType.ERROR, "grep: $file: No such file")
                    } else {
                        val matches = content.lines().filter { it.contains(pattern, ignoreCase = true) }
                        if (matches.isNotEmpty()) {
                            add(TerminalLine.LineType.TABLE, matches.joinToString("\n"))
                        } else {
                            add(TerminalLine.LineType.OUTPUT, "(no matching lines found for '$pattern')")
                        }
                    }
                }
            }

            "whoami" -> {
                add(TerminalLine.LineType.SUCCESS, environmentVariables["USER"] ?: "root")
            }

            "id" -> {
                add(TerminalLine.LineType.OUTPUT, "uid=0(root) gid=0(root) groups=0(root),4(adm),24(cdrom),27(sudo),30(dip),46(plugdev)")
            }

            "uname" -> {
                if (args.contains("-a")) {
                    add(TerminalLine.LineType.OUTPUT, "Linux cyberlab-node-01 6.9.12-cyberlab-custom #1 SMP PREEMPT_DYNAMIC x86_64 GNU/Linux")
                } else {
                    add(TerminalLine.LineType.OUTPUT, "Linux")
                }
            }

            "hostname" -> {
                add(TerminalLine.LineType.OUTPUT, environmentVariables["HOSTNAME"] ?: "cyberlab-node-01")
            }

            "date" -> {
                val now = SimpleDateFormat("EEE MMM dd HH:mm:ss 'UTC' yyyy", Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }.format(Date())
                add(TerminalLine.LineType.OUTPUT, now)
            }

            "uptime" -> {
                add(TerminalLine.LineType.OUTPUT, " 22:15:00 up 42 days,  3:18,  1 user,  load average: 0.12, 0.08, 0.05")
            }

            "env" -> {
                val sb = StringBuilder()
                for ((k, v) in environmentVariables) {
                    sb.appendLine("$k=$v")
                }
                add(TerminalLine.LineType.TABLE, sb.toString().trimEnd())
            }

            "export" -> {
                if (args.isEmpty()) {
                    val sb = StringBuilder()
                    for ((k, v) in environmentVariables) {
                        sb.appendLine("declare -x $k=\"$v\"")
                    }
                    add(TerminalLine.LineType.TABLE, sb.toString().trimEnd())
                } else {
                    val assignment = args[0]
                    if (assignment.contains("=")) {
                        val (k, v) = assignment.split("=", limit = 2)
                        environmentVariables[k.trim()] = v.trim('"', '\'')
                        add(TerminalLine.LineType.SUCCESS, "[+] Exported ${k.trim()}")
                    }
                }
            }

            "history" -> {
                val sb = StringBuilder()
                history.forEachIndexed { index, cmd ->
                    sb.appendLine("  ${(index + 1).toString().padStart(4)}  $cmd")
                }
                add(TerminalLine.LineType.OUTPUT, sb.toString().trimEnd())
            }

            // Security Tools Implementations
            "nmap" -> {
                val target = args.lastOrNull { !it.startsWith("-") && it != "nmap" } ?: "10.10.10.50"
                add(TerminalLine.LineType.SYSTEM, "Starting Nmap 7.94 ( https://nmap.org ) at 2026-08-13 22:15 UTC")
                add(TerminalLine.LineType.SYSTEM, "Initiating SYN Stealth Scan against $target")
                add(TerminalLine.LineType.SYSTEM, "Scanning $target [1000 ports]")
                add(TerminalLine.LineType.SYSTEM, "Discovered open port 80/tcp on $target")
                add(TerminalLine.LineType.SYSTEM, "Discovered open port 22/tcp on $target")
                add(TerminalLine.LineType.SYSTEM, "Discovered open port 3306/tcp on $target")
                add(TerminalLine.LineType.TABLE, """
PORT     STATE SERVICE     VERSION
22/tcp   open  ssh         OpenSSH 8.9p1 Ubuntu 3ubuntu0.6 (Ubuntu Linux; protocol 2.0)
80/tcp   open  http        Apache httpd 2.4.52 ((Ubuntu) PHP/8.1.2)
139/tcp  open  netbios-ssn Samba smbd 4.6.2
443/tcp  open  ssl/http    Apache/2.4.52 (SSL-Cert: self-signed)
445/tcp  open  netbios-ssn Samba smbd 4.6.2 (Workgroup: WORKGROUP)
3306/tcp open  mysql       MySQL 8.0.35-0ubuntu0.22.04.1
                """.trimIndent())
                if (args.contains("--script") || args.contains("vuln") || args.contains("-A")) {
                    add(TerminalLine.LineType.ERROR, """
| vulners:
|   cpe:/a:apache:httpd:2.4.52:
|     CVE-2023-25690  9.8  https://vulners.com/cve/CVE-2023-25690 (HTTP Request Smuggling)
|     CVE-2022-22720  9.8  https://vulners.com/cve/CVE-2022-22720 (HTTP Request smuggling in mod_proxy)
|_smb-vuln-ms17-010: Remote Code Execution vulnerability in Microsoft SMBv1 (CLEAN)
                    """.trimIndent())
                }
                add(TerminalLine.LineType.SUCCESS, "Nmap done: 1 IP address (1 host up) scanned in 2.34 seconds.")
            }

            "sqlmap" -> {
                add(TerminalLine.LineType.BANNER, """
    ___ ___| |_____ ___ ___  {1.7.11#stable}
   |_ -| . | |     | .'| . |
   |___|_  |_|_|_|_|__,|  _| http://sqlmap.org
         |_|           |_|
                """.trimIndent())
                add(TerminalLine.LineType.SYSTEM, "[*] starting @ 22:15:10 /2026-08-13/")
                add(TerminalLine.LineType.SYSTEM, "[INFO] testing connection to the target URL: http://10.10.10.50/item.php?id=1")
                add(TerminalLine.LineType.SYSTEM, "[INFO] checking if the target is protected by some kind of WAF/IPS")
                add(TerminalLine.LineType.SUCCESS, "[+] heuristic (basic) test shows that GET parameter 'id' might be injectable (possible DBMS: 'MySQL')")
                add(TerminalLine.LineType.TABLE, """
sqlmap identified the following injection point(s) with a total of 48 HTTP(s) requests:
---
Parameter: id (GET)
    Type: boolean-based blind
    Title: AND boolean-based blind - WHERE or HAVING clause
    Payload: id=1 AND 8821=8821

    Type: time-based blind
    Title: MySQL >= 5.0.12 AND time-based blind (query SLEEP)
    Payload: id=1 AND (SELECT 6942 FROM (SELECT(SLEEP(5)))xyz)

    Type: UNION query
    Title: Generic UNION query (NULL) - 3 columns
    Payload: id=-1 UNION ALL SELECT NULL,CONCAT(0x7170626271,username,0x7178766a71),NULL FROM users-- -
---
[INFO] the back-end DBMS is MySQL 8.0.35
available databases [3]:
[*] cyberlab_production
[*] information_schema
[*] mysql
                """.trimIndent())
                add(TerminalLine.LineType.SUCCESS, "[*] fetched 3 database names successfully.")
            }

            "hydra" -> {
                add(TerminalLine.LineType.BANNER, "Hydra v9.5 (c) 2023 by van Hauser / THC & David Maciejak - Online Pass Crack")
                add(TerminalLine.LineType.SYSTEM, "[DATA] max 16 tasks per target, 500 login tries, testing service: ssh")
                add(TerminalLine.LineType.SYSTEM, "[ATTACK] targeting 10.10.10.50:22 (SSH)...")
                add(TerminalLine.LineType.OUTPUT, "[22][ssh] host: 10.10.10.50   login: root   password: password123 [FAILED]")
                add(TerminalLine.LineType.OUTPUT, "[22][ssh] host: 10.10.10.50   login: root   password: admin [FAILED]")
                add(TerminalLine.LineType.OUTPUT, "[22][ssh] host: 10.10.10.50   login: root   password: toor [FAILED]")
                add(TerminalLine.LineType.SUCCESS, "[22][ssh] host: 10.10.10.50   login: root   password: Summer2024! [VALID CREDENTIALS]")
                add(TerminalLine.LineType.SUCCESS, "[+] 1 of 1 target completed, 1 valid password found.")
            }

            "gobuster" -> {
                add(TerminalLine.LineType.BANNER, "===============================================================\nGobuster v3.6 - Fast Directory & DNS Enumerator\n===============================================================")
                add(TerminalLine.LineType.SYSTEM, "[+] Url:                     http://10.10.10.50/")
                add(TerminalLine.LineType.SYSTEM, "[+] Method:                  GET")
                add(TerminalLine.LineType.SYSTEM, "[+] Wordlist:                /opt/wordlists/common.txt")
                add(TerminalLine.LineType.TABLE, """
/admin                (Status: 301) [Size: 312] [--> http://10.10.10.50/admin/]
/api                  (Status: 200) [Size: 1420]
/config.php.bak       (Status: 200) [Size: 840] [CRITICAL SENSITIVE BACKUP]
/dashboard            (Status: 302) [Size: 0] [--> /login.php]
/images               (Status: 301) [Size: 314]
/robots.txt           (Status: 200) [Size: 85]
/server-status        (Status: 403) [Size: 277]
                """.trimIndent())
                add(TerminalLine.LineType.SUCCESS, "===============================================================\nFinished in 1.15s. Total URLs checked: 4614")
            }

            "nikto" -> {
                add(TerminalLine.LineType.SYSTEM, "- Nikto v2.5.0\n---------------------------------------------------------------------------")
                add(TerminalLine.LineType.SYSTEM, "+ Target IP:          10.10.10.50\n+ Target Hostname:    web01.internal.corp\n+ Target Port:        80")
                add(TerminalLine.LineType.TABLE, """
+ Server: Apache/2.4.52 (Ubuntu)
+ [!] /admin/phpinfo.php: Output from phpinfo() was found (Exposes internal environment variables).
+ [!] /robots.txt: Contains 3 disallow entries: /private/, /backup/, /admin/
+ [!] The anti-clickjacking X-Frame-Options header is not present.
+ [!] The X-Content-Type-Options header is not set.
+ [+] Retrieved x-powered-by header: PHP/8.1.2
+ [!] Cookie PHPSESSID created without the httponly flag.
+ [+] 7918 requests: 0 error(s) and 6 item(s) reported on remote host
                """.trimIndent())
            }

            "john" -> {
                add(TerminalLine.LineType.SYSTEM, "Loaded 2 password hashes (sha512crypt, crypt(3) ${'$'}6${'$'} [SHA512 256/256 AVX2 4x])")
                add(TerminalLine.LineType.SUCCESS, "dragon           (user_admin)")
                add(TerminalLine.LineType.SUCCESS, "shadowmaster     (operator)")
                add(TerminalLine.LineType.OUTPUT, "2g 0:00:00:01 DONE (2026-08-13 22:05) 1.818g/s 4520p/s 4520c/s 4520C/s")
                add(TerminalLine.LineType.SUCCESS, "Use the \"--show\" option to display all cracked hashes.")
            }

            "hashcat" -> {
                add(TerminalLine.LineType.SYSTEM, "hashcat (v6.2.6) starting in attack mode 0 (Straight)...")
                add(TerminalLine.LineType.SYSTEM, "Device #1: NVIDIA GeForce RTX 4090, 24564/24564 MB, 128MCU")
                add(TerminalLine.LineType.OUTPUT, "Hash.Mode........: 1000 (NTLM)")
                add(TerminalLine.LineType.OUTPUT, "Speed.#1.........: 38450.4 MH/s (38.45 GH/s)")
                add(TerminalLine.LineType.SUCCESS, "32ed87b2490fedba7556e1b12f020bc5:CyberWarrior2026!")
                add(TerminalLine.LineType.SUCCESS, "Status...........: Cracked [Time.Estimated: 00:00:02]")
            }

            "msfconsole", "metasploit" -> {
                add(TerminalLine.LineType.BANNER, """
       =[ metasploit v6.3.40-dev                          ]
+ -- --=[ 2380 exploits - 1230 auxiliary - 410 post       ]
+ -- --=[ 1385 payloads - 46 encoders - 11 nops           ]
                """.trimIndent())
                add(TerminalLine.LineType.SYSTEM, "[*] Launching multi/handler on 10.0.4.15:4444")
                add(TerminalLine.LineType.SYSTEM, "[*] Started reverse TCP handler on 10.0.4.15:4444")
                add(TerminalLine.LineType.SUCCESS, "[*] Sending stage (175686 bytes) to 10.10.10.50")
                add(TerminalLine.LineType.SUCCESS, "[*] Meterpreter session 1 opened (10.0.4.15:4444 -> 10.10.10.50:51280)")
                add(TerminalLine.LineType.OUTPUT, "meterpreter > sysinfo")
                add(TerminalLine.LineType.OUTPUT, "Computer     : web01.internal.corp\nOS           : Linux 5.15.0-89-generic (x86_64)\nArchitecture : x64\nMeterpreter  : x64/linux")
            }

            "prowler" -> {
                add(TerminalLine.LineType.BANNER, "PROWLER v4.2.0 - Open Source Security Tool for AWS/Azure/GCP")
                add(TerminalLine.LineType.SYSTEM, "[*] Scanning AWS Account: 123456789012 (Production-VPC-East)")
                add(TerminalLine.LineType.TABLE, """
[PASS] s3_bucket_default_encryption: S3 Bucket [finance-records-prod] has AES-256 enabled.
[FAIL] s3_bucket_public_access: S3 Bucket [public-assets-static] has public ACL permissions! [CRITICAL]
[FAIL] iam_root_mfa_enabled: Root account does not have Hardware MFA enforced! [CRITICAL]
[PASS] cloudtrail_multi_region_enabled: CloudTrail logging is enabled across all active regions.
[FAIL] securitygroup_ssh_world_open: Security Group [sg-0a4f9] allows 0.0.0.0/0 on port 22. [HIGH]
                """.trimIndent())
                add(TerminalLine.LineType.SUCCESS, "Compliance Summary: 42 Passed | 3 Failed | Score: 93.3% CIS AWS Benchmark v2.0")
            }

            "trivy" -> {
                add(TerminalLine.LineType.SYSTEM, "2026-08-13T22:15:40.120Z INFO Vulnerability scanning image: alpine:3.18")
                add(TerminalLine.LineType.TABLE, """
Target: alpine:3.18 (alpine 3.18.4)
==================================
Total: 2 (UNKNOWN: 0, LOW: 0, MEDIUM: 1, HIGH: 0, CRITICAL: 1)

┌────────────┬────────────────┬──────────┬──────────────┬───────────────────┬────────────────────────────────────────────┐
│  Library   │ Vulnerability  │ Severity │ Installed    │    Fixed Version  │                   Title                    │
├────────────┼────────────────┼──────────┼──────────────┼───────────────────┼────────────────────────────────────────────┤
│ libcrypto3 │ CVE-2023-5363  │ MEDIUM   │ 3.1.2-r0     │ 3.1.3-r0          │ openssl: Incorrect cipher key length check │
│ libssl3    │ CVE-2024-3094  │ CRITICAL │ 5.6.0-r1     │ 5.4.6-r0 (downgd) │ xz-utils: liblzma sshd backdoor inject    │
└────────────┴────────────────┴──────────┴──────────────┴───────────────────┴────────────────────────────────────────────┘
                """.trimIndent())
            }

            "sherlock" -> {
                val username = args.firstOrNull() ?: "operator_0x"
                add(TerminalLine.LineType.SYSTEM, "[*] Checking username '$username' on 300+ social sites...")
                add(TerminalLine.LineType.SUCCESS, "[+] GitHub: https://github.com/$username")
                add(TerminalLine.LineType.SUCCESS, "[+] Twitter/X: https://x.com/$username")
                add(TerminalLine.LineType.SUCCESS, "[+] Reddit: https://www.reddit.com/user/$username")
                add(TerminalLine.LineType.SUCCESS, "[+] DockerHub: https://hub.docker.com/u/$username")
                add(TerminalLine.LineType.OUTPUT, "[*] Search completed. Found 4 active accounts.")
            }

            "theharvester" -> {
                val domain = args.lastOrNull { !it.startsWith("-") && it != "theharvester" } ?: "target.corp"
                add(TerminalLine.LineType.SYSTEM, "[*] theHarvester 4.4.0 - Gathering accounts for domain $domain")
                add(TerminalLine.LineType.TABLE, """
[*] Emails found (4):
  - admin@$domain
  - ciso-security@$domain
  - john.doe.dev@$domain
  - hr-recruiting@$domain

[*] Hosts / Subdomains found (4):
  - vpn.$domain (198.51.100.22)
  - api.$domain (198.51.100.25)
  - mail.$domain (198.51.100.10)
  - gitlab.internal.$domain (10.0.8.4)
                """.trimIndent())
            }

            "whois" -> {
                val target = args.firstOrNull() ?: "example.com"
                add(TerminalLine.LineType.TABLE, """
Domain Name: ${target.uppercase()}
Registry Domain ID: 2138514_DOMAIN_COM-VRSN
Registrar WHOIS Server: whois.iana.org
Updated Date: 2026-08-10T07:11:42Z
Creation Date: 1995-08-14T04:00:00Z
Registry Expiry Date: 2027-08-13T04:00:00Z
Registrar: RESERVED-Internet Assigned Numbers Authority
Name Server: A.IANA-SERVERS.NET
DNSSEC: signedDelegation
                """.trimIndent())
            }

            "traceroute" -> {
                val target = args.firstOrNull() ?: "8.8.8.8"
                add(TerminalLine.LineType.SYSTEM, "traceroute to $target ($target), 30 hops max, 60 byte packets")
                add(TerminalLine.LineType.TABLE, """
 1  gateway.cyberlab.internal (10.0.4.1)  0.342 ms  0.312 ms  0.298 ms
 2  198.51.100.1 (198.51.100.1)  1.421 ms  1.390 ms  1.355 ms
 3  core-router-01.isp.net (203.0.113.45)  4.812 ms  4.790 ms  4.760 ms
 4  dns.google (8.8.8.8)  8.214 ms  8.190 ms  8.160 ms
                """.trimIndent())
            }

            "ping" -> {
                val target = args.lastOrNull { !it.startsWith("-") && it != "ping" } ?: "10.0.4.1"
                add(TerminalLine.LineType.SYSTEM, "PING $target ($target) 56(84) bytes of data.")
                add(TerminalLine.LineType.OUTPUT, "64 bytes from $target: icmp_seq=1 ttl=64 time=0.341 ms")
                add(TerminalLine.LineType.OUTPUT, "64 bytes from $target: icmp_seq=2 ttl=64 time=0.298 ms")
                add(TerminalLine.LineType.OUTPUT, "64 bytes from $target: icmp_seq=3 ttl=64 time=0.315 ms")
                add(TerminalLine.LineType.OUTPUT, "64 bytes from $target: icmp_seq=4 ttl=64 time=0.289 ms")
                add(TerminalLine.LineType.SUCCESS, "--- $target ping statistics ---")
                add(TerminalLine.LineType.SUCCESS, "4 packets transmitted, 4 received, 0% packet loss, time 3004ms")
            }

            "curl" -> {
                val url = args.lastOrNull { !it.startsWith("-") && it != "curl" } ?: "http://10.10.10.50/api/v1/health"
                add(TerminalLine.LineType.SYSTEM, "HTTP/1.1 200 OK\nDate: Thu, 13 Aug 2026 22:15:00 GMT\nServer: Apache/2.4.52\nContent-Type: application/json\n")
                add(TerminalLine.LineType.TABLE, """{"status":"HEALTHY","node":"web01","uptime":364210,"env":"production"}""")
            }

            "ps" -> {
                add(TerminalLine.LineType.TABLE, """
USER       PID %CPU %MEM    VSZ   RSS TTY      STAT START   TIME COMMAND
root         1  0.0  0.1 169420 12840 ?        Ss   20:00   0:01 /sbin/init
root       412  0.0  0.2  72480 18200 ?        Ss   20:00   0:00 /usr/sbin/sshd -D
root       640  0.1  0.8 450120 68400 ?        Ssl  20:00   0:04 /usr/sbin/suricata -c /etc/suricata/suricata.yaml
www-data   820  0.0  0.4 224100 34100 ?        S    20:01   0:00 /usr/sbin/apache2 -k start
root      1420  0.0  0.1  24120  4120 pts/0    Ss   21:48   0:00 -bash
root      2840  0.0  0.1  18900  3200 pts/0    R+   22:15   0:00 ps aux
                """.trimIndent())
            }

            "netstat", "ss" -> {
                add(TerminalLine.LineType.TABLE, """
Active Internet connections (only servers)
Proto Recv-Q Send-Q Local Address           Foreign Address         State       PID/Program name    
tcp        0      0 0.0.0.0:22              0.0.0.0:*               LISTEN      412/sshd: /usr/sbin 
tcp        0      0 0.0.0.0:80              0.0.0.0:*               LISTEN      820/apache2         
tcp        0      0 127.0.0.1:3306          0.0.0.0:*               LISTEN      590/mysqld          
tcp6       0      0 :::443                  :::*                    LISTEN      820/apache2         
                """.trimIndent())
            }

            "iptables" -> {
                add(TerminalLine.LineType.TABLE, """
Chain INPUT (policy DROP 0 packets, 0 bytes)
 pkts bytes target     prot opt in     out     source               destination         
 142K   18M ACCEPT     all  --  lo     *       0.0.0.0/0            0.0.0.0/0           
  89K   12M ACCEPT     all  --  *      *       0.0.0.0/0            0.0.0.0/0            state RELATED,ESTABLISHED
 1240  148K ACCEPT     tcp  --  eth0   *       10.0.0.0/8           0.0.0.0/0            tcp dpt:22
 5420  320K ACCEPT     tcp  --  eth0   *       0.0.0.0/0            0.0.0.0/0            tcp dpt:80
 8910  510K ACCEPT     tcp  --  eth0   *       0.0.0.0/0            0.0.0.0/0            tcp dpt:443
  420 18400 LOG        all  --  *      *       0.0.0.0/0            0.0.0.0/0            LOG level warning prefix "IPTABLES-DROP: "
                """.trimIndent())
            }

            "neofetch", "fastfetch" -> {
                add(TerminalLine.LineType.BANNER, """
       /\         root@cyberlab-node-01
      /  \        ---------------------
     / /\ \       OS: CyberLab Linux Hardened v3.4 x86_64
    / /__\ \      Host: KVM Virtual Cloud Node (ID: 0x992)
   / /____\ \     Kernel: 6.9.12-cyberlab-custom
  /_/      \_\    Uptime: 42 days, 3 hours, 18 mins
                  Packages: 1420 (dpkg), 12 (cargo)
                  Shell: bash 5.2.21
                  Terminal: xterm-256color (80x24)
                  CPU: AMD EPYC 9654 (16) @ 3.500GHz
                  GPU: NVIDIA RTX 4090 24GB [Pass-through]
                  Memory: 3410MiB / 16384MiB (20.8%)
                """.trimIndent())
            }

            "cmatrix", "matrix" -> {
                add(TerminalLine.LineType.BANNER, """
0 1 0 1 1 0 0 1 0 1 0 1 1 0 0 1 0 1 0 1 1 0 0 1
1 0 0 1 0 1 1 0 1 0 0 1 0 1 1 0 1 0 0 1 0 1 1 0
0 1 1 0 1 0 0 1 0 1 1 0 1 0 0 1 0 1 1 0 1 0 0 1
[+] MATRIX DATA STREAM ACTIVE // ACCESS GRANTED
0 1 0 1 1 0 0 1 0 1 0 1 1 0 0 1 0 1 0 1 1 0 0 1
                """.trimIndent())
            }

            "python", "python3" -> {
                if (args.isEmpty()) {
                    add(TerminalLine.LineType.SYSTEM, "Python 3.11.8 (main, Feb 12 2026, 14:02:18) [GCC 13.2.0] on linux\nType \"help\", \"copyright\", \"credits\" or \"license\" for more information.")
                } else if (args[0] == "-c") {
                    val code = args.drop(1).joinToString(" ").trim('"', '\'')
                    add(TerminalLine.LineType.OUTPUT, ">>> Executing Python snippet:")
                    if (code.contains("print")) {
                        val toPrint = code.substringAfter("print(").substringBeforeLast(")").trim('"', '\'')
                        add(TerminalLine.LineType.SUCCESS, toPrint)
                    } else {
                        add(TerminalLine.LineType.SUCCESS, "[+] Script executed successfully (exit code 0)")
                    }
                } else {
                    val scriptPath = args[0]
                    val content = vfs.readFile(scriptPath)
                    if (content == null) {
                        add(TerminalLine.LineType.ERROR, "python3: can't open file '$scriptPath': [Errno 2] No such file or directory")
                    } else {
                        add(TerminalLine.LineType.SYSTEM, "[*] Executing Python runtime on $scriptPath...")
                        // Simulate intelligent execution based on script content
                        when {
                            content.contains("port_scanner") || content.contains("socket") -> {
                                add(TerminalLine.LineType.SYSTEM, "[*] Starting TCP Banner Probe against 10.10.10.50...")
                                add(TerminalLine.LineType.SUCCESS, "  [+] Port    22/tcp OPEN  (ssh - OpenSSH 8.9p1)")
                                add(TerminalLine.LineType.SUCCESS, "  [+] Port    80/tcp OPEN  (http - Apache 2.4.52)")
                                add(TerminalLine.LineType.SUCCESS, "  [+] Port   443/tcp OPEN  (https - SSL/TLS 1.3)")
                                add(TerminalLine.LineType.SUCCESS, "  [+] Port  3306/tcp OPEN  (mysql - MySQL 8.0.35)")
                                add(TerminalLine.LineType.OUTPUT, "[+] Scan completed: 4 open ports identified in 0.42s")
                            }
                            content.contains("xor") || content.contains("base64") || content.contains("FLAG{") -> {
                                add(TerminalLine.LineType.SYSTEM, "[*] Decoded 44 bytes from Base64")
                                add(TerminalLine.LineType.SUCCESS, "[+] Extracted Token: FLAG{x0r_c1phers_4re_e4sy_t0_br3ak}")
                                add(TerminalLine.LineType.SUCCESS, "[SUCCESS] Solved CTF Cryptographic Flag: FLAG{x0r_c1phers_4re_e4sy_t0_br3ak}")
                                onXpAwarded?.invoke(100, "FLAG{x0r_c1phers_4re_e4sy_t0_br3ak}")
                            }
                            content.contains("exploit") || content.contains("payload") || content.contains("bof") -> {
                                add(TerminalLine.LineType.SYSTEM, "[*] Constructing 528-byte Buffer Overflow payload...")
                                add(TerminalLine.LineType.SUCCESS, "[+] Overwriting EIP with 0x08041337 (JMP ESP) + 32-byte NOP sled")
                                add(TerminalLine.LineType.SUCCESS, "[+] Payload delivered to target 10.10.10.50:9999")
                                add(TerminalLine.LineType.OUTPUT, "[+] Target process hijacked. Shellcode executed successfully.")
                            }
                            else -> {
                                // Generic execution output
                                val printLines = content.lines().filter { it.trim().startsWith("print(") }
                                if (printLines.isNotEmpty()) {
                                    for (pl in printLines.take(5)) {
                                        val text = pl.trim().removePrefix("print(").removeSuffix(")").trim('"', '\'', 'f')
                                        add(TerminalLine.LineType.OUTPUT, text)
                                    }
                                } else {
                                    add(TerminalLine.LineType.SUCCESS, "[+] Execution completed successfully with exit code 0.")
                                }
                            }
                        }
                    }
                }
            }

            "bash", "sh" -> {
                if (args.isEmpty()) {
                    add(TerminalLine.LineType.OUTPUT, "GNU bash, version 5.2.21(1)-release (x86_64-pc-linux-gnu)")
                } else {
                    val scriptPath = args[0]
                    val content = vfs.readFile(scriptPath)
                    if (content == null) {
                        add(TerminalLine.LineType.ERROR, "bash: $scriptPath: No such file or directory")
                    } else {
                        add(TerminalLine.LineType.SYSTEM, "[*] Running bash script $scriptPath...")
                        when {
                            content.contains("auth.log") || content.contains("Failed password") -> {
                                add(TerminalLine.LineType.TABLE, """
==================================================
[*] LINUX AUTH.LOG INCIDENT RESPONSE TRIAGE
==================================================

[+] Top Attacking IP Addresses (Failed Passwords):
     142 185.220.101.5
      84 194.26.29.112
      32 45.155.205.233

[+] Targeted Usernames:
     120 root
      95 admin
      14 operator
      10 test

[+] Sudo Privilege Escalation Events:
Aug 13 21:48:02 cyberlab sudo: operator : USER=root ; COMMAND=/bin/su -
==================================================
                                """.trimIndent())
                            }
                            else -> {
                                add(TerminalLine.LineType.SUCCESS, "[+] Shell script $scriptPath finished execution.")
                            }
                        }
                    }
                }
            }

            "gcc" -> {
                if (args.isEmpty()) {
                    add(TerminalLine.LineType.ERROR, "gcc: fatal error: no input files\ncompilation terminated.")
                } else {
                    val srcFile = args.firstOrNull { it.endsWith(".c") } ?: args[0]
                    val outFile = if (args.contains("-o")) args.getOrNull(args.indexOf("-o") + 1) ?: "a.out" else "a.out"
                    val content = vfs.readFile(srcFile)
                    if (content == null) {
                        add(TerminalLine.LineType.ERROR, "gcc: error: $srcFile: No such file or directory")
                    } else {
                        vfs.writeFile(outFile, "#!/bin/elf-binary\nCompiled from $srcFile")
                        add(TerminalLine.LineType.SUCCESS, "[+] Compiled $srcFile -> $outFile (ELF 64-bit LSB pie executable, x86-64)")
                    }
                }
            }

            "yara" -> {
                if (args.size < 2) {
                    add(TerminalLine.LineType.ERROR, "Usage: yara [RULE_FILE] [TARGET_PATH]")
                } else {
                    val ruleFile = args[0]
                    val targetPath = args[1]
                    val ruleContent = vfs.readFile(ruleFile)
                    if (ruleContent == null) {
                        add(TerminalLine.LineType.ERROR, "yara: error opening file '$ruleFile'")
                    } else {
                        add(TerminalLine.LineType.SYSTEM, "[*] Scanning '$targetPath' with YARA rules in '$ruleFile'...")
                        add(TerminalLine.LineType.SUCCESS, "CyberLab_Webshell_Detector /var/log/nginx/access.log (Matched: \$tag, \$exec_fn1, \$cmd_param)")
                        add(TerminalLine.LineType.OUTPUT, "[+] YARA Scan Finished: 1 positive signature detection.")
                    }
                }
            }

            "ai", "cyber-ai", "copilot", "ask" -> {
                val query = args.joinToString(" ")
                if (query.isBlank()) {
                    add(TerminalLine.LineType.ERROR, "Usage: ai <your question, code request, or command request>")
                } else {
                    add(TerminalLine.LineType.SYSTEM, "[*] Querying CYBER_AI Copilot (Gemini Threat Intelligence Engine)...")
                    val parsed = com.example.data.ai.CyberAiService.parseAiResponse(
                        rawText = getSimulatedAiResponse(query),
                        mode = com.example.data.ai.AiCopilotMode.GENERAL
                    )
                    add(TerminalLine.LineType.SUCCESS, "[+] CYBER_AI Response:\n${parsed.fullText}")
                    
                    // Auto-save any generated scripts to VFS for direct execution
                    for (snippet in parsed.codeSnippets) {
                        vfs.writeFile(snippet.suggestedFileName, snippet.code)
                        add(TerminalLine.LineType.BANNER, "[+] Auto-saved script to: ${snippet.suggestedFileName} (Run with: python3 ${snippet.suggestedFileName})")
                    }
                }
            }

            "submit", "flag", "ctf" -> {
                val submittedFlag = args.lastOrNull() ?: ""
                if (submittedFlag.isBlank()) {
                    add(TerminalLine.LineType.ERROR, "Usage: submit FLAG{...}")
                } else {
                    val validFlags = mapOf(
                        "FLAG{x0r_c1phers_4re_e4sy_t0_br3ak}" to Pair("Crypto 101", 100),
                        "FLAG{h1dd3n_1n_pl41n_s1ght_f0r3ns1cs}" to Pair("Forensics Alpha", 150),
                        "FLAG{sql_1nj3ct10n_m4st3r_2026}" to Pair("SQLi Bypass", 200),
                        "FLAG{buff3r_0v3rfl0w_pwn3d_31337}" to Pair("Binary Exploitation", 300)
                    )

                    val match = validFlags.entries.find { it.key.equals(submittedFlag.trim(), ignoreCase = true) }
                    if (match != null) {
                        add(TerminalLine.LineType.SUCCESS, """
[+] ==========================================
[+] FLAG CAPTURED! CHALLENGE SOLVED!
[+] Challenge: ${match.value.first}
[+] Reward   : +${match.value.second} XP
[+] ==========================================
                        """.trimIndent())
                        onXpAwarded?.invoke(match.value.second, match.key)
                    } else {
                        add(TerminalLine.LineType.ERROR, "[-] [INCORRECT FLAG] The submitted flag does not match any active challenges.")
                    }
                }
            }

            else -> {
                try {
                    withContext(Dispatchers.IO) {
                        val process = ProcessBuilder("sh", "-c", trimmed)
                            .redirectErrorStream(true)
                            .start()
                        
                        val reader = process.inputStream.bufferedReader()
                        val output = StringBuilder()
                        var line: String? = null
                        var linesRead = 0
                        
                        val startTime = System.currentTimeMillis()
                        while (System.currentTimeMillis() - startTime < 3000) {
                            if (reader.ready()) {
                                line = reader.readLine()
                                if (line == null) break
                                output.append(line).append("\n")
                                linesRead++
                                if (linesRead > 500) break
                            } else {
                                try {
                                    process.exitValue()
                                    // Make sure we read any remaining output before breaking
                                    while (reader.ready() && reader.readLine().also { line = it } != null) {
                                        output.append(line).append("\n")
                                        linesRead++
                                        if (linesRead > 500) break
                                    }
                                    break
                                } catch (e: IllegalThreadStateException) {
                                    Thread.sleep(50)
                                }
                            }
                        }
                        
                        try {
                            val exitVal = process.exitValue()
                            if (exitVal != 0 && output.isEmpty()) {
                                add(TerminalLine.LineType.ERROR, "bash: $command: returned error $exitVal (command not found or failed)")
                            }
                        } catch (e: IllegalThreadStateException) {
                            process.destroy()
                            output.append("\n[Process terminated due to 3-second timeout]")
                        }
                        
                        val resultText = output.toString().trimEnd()
                        if (resultText.isNotEmpty()) {
                            add(TerminalLine.LineType.OUTPUT, resultText)
                        }
                    }
                } catch (e: Exception) {
                    add(TerminalLine.LineType.ERROR, "bash: $command: execution failed: ${e.message}")
                }
            }
        }

        return lines
    }

    private fun getSimulatedAiResponse(query: String): String {
        val q = query.lowercase()
        return when {
            q.contains("xz") || q.contains("3094") -> {
                "CVE-2024-3094 Backdoor in XZ Utils / liblzma:\n" +
                "- An attacker inserted obfuscated malicious test files in the upstream release tarball.\n" +
                "- During compilation, it patches `RSA_public_decrypt` in OpenSSH to enable unauthorized pre-auth remote code execution."
            }
            q.contains("sql") || q.contains("injection") -> {
                "SQL Injection Mitigation:\n" +
                "- Always use Prepared Statements (Parameterized Queries) via PDO or ORM.\n" +
                "- Implement Least Privilege database credentials and strict input validation."
            }
            q.contains("nmap") || q.contains("scan") -> {
                "Nmap Essential Scans:\n" +
                "- Stealth SYN Scan: `nmap -sS -T4 -p- <target>`\n" +
                "- Service Detection & NSE Vulns: `nmap -sV -sC --script vuln <target>`\n" +
                "- UDP Scan: `nmap -sU -top-ports 100 <target>`"
            }
            q.contains("buffer overflow") || q.contains("bof") -> {
                "Buffer Overflow Mechanics:\n" +
                "- Overflowing an unchecked buffer (e.g. `strcpy`) overwrites the Saved Frame Pointer (EBP/RBP) and Return Address (EIP/RIP).\n" +
                "- Protections: Stack Canaries (`-fstack-protector`), ASLR, and Non-Executable Stack (`NX`/`DEP`)."
            }
            else -> {
                "CYBER_AI Analysis for '$query':\n" +
                "- Recommended defense strategy: enforce zero-trust isolation, multi-factor authentication, and automated vulnerability scanning."
            }
        }
    }

    fun getCompletions(partial: String): List<String> {
        val trimmed = partial.trimStart()
        if (trimmed.isEmpty()) return emptyList()

        val builtins = listOf(
            "help", "clear", "ls", "cd", "pwd", "cat", "echo", "mkdir", "touch", "rm", "grep",
            "whoami", "id", "uname", "hostname", "date", "uptime", "env", "export", "history",
            "nmap", "sqlmap", "hydra", "gobuster", "nikto", "john", "hashcat", "msfconsole",
            "prowler", "trivy", "sherlock", "theharvester", "whois", "traceroute", "ping",
            "curl", "ps", "netstat", "iptables", "neofetch", "cmatrix", "ai", "submit"
        )

        // If typing the command itself
        if (!trimmed.contains(" ")) {
            return builtins.filter { it.startsWith(trimmed.lowercase()) }
        }

        // If typing after a command
        val isTrailingSpace = partial.endsWith(" ")
        val tokens = trimmed.trimEnd().split("\\s+".toRegex())
        val command = tokens.first().lowercase()
        val lastToken = if (isTrailingSpace) "" else tokens.last()

        // Check tool flags if typing a flag or already typed a space after the command
        if (lastToken.startsWith("-") || isTrailingSpace) {
            val flags = when (command) {
                "nmap" -> listOf("-sS", "-sV", "-sC", "-O", "-A", "-T4", "-p-", "--script=vuln")
                "sqlmap" -> listOf("-u", "--batch", "--dbs", "--tables", "--dump", "--random-agent", "--level=", "--risk=")
                "gobuster" -> listOf("dir", "dns", "vhost", "-u", "-w", "-t", "-x")
                "hydra" -> listOf("-l", "-L", "-p", "-P", "-s", "-t", "ssh", "ftp", "http-get")
                "john" -> listOf("--wordlist=", "--format=", "--show")
                "hashcat" -> listOf("-m", "-a", "--show", "--force")
                "prowler" -> listOf("aws", "gcp", "azure", "--compliance")
                "trivy" -> listOf("image", "repo", "fs", "--severity")
                "ls" -> listOf("-la", "-l", "-a", "-lh")
                "grep" -> listOf("-i", "-r", "-n", "-v")
                "ping" -> listOf("-c", "-i", "-s")
                "curl" -> listOf("-X", "-d", "-H", "-I", "-O", "-s")
                "netstat" -> listOf("-tulnp", "-anp", "-rn")
                else -> emptyList()
            }
            if (lastToken.isNotEmpty()) {
                val matches = flags.filter { it.startsWith(lastToken, ignoreCase = true) }
                if (matches.isNotEmpty()) return matches
            } else {
                return flags
            }
        }

        // Subcommands that don't start with '-'
        if (command == "gobuster" && tokens.size == 2 && !lastToken.startsWith("-")) {
             val gobusterModes = listOf("dir", "dns", "vhost")
             if (gobusterModes.any { it.startsWith(lastToken, ignoreCase = true) }) {
                 return gobusterModes.filter { it.startsWith(lastToken, ignoreCase = true) }
             }
        }
        if (command == "prowler" && tokens.size == 2 && !lastToken.startsWith("-")) {
             val prowlerModes = listOf("aws", "gcp", "azure")
             if (prowlerModes.any { it.startsWith(lastToken, ignoreCase = true) }) {
                 return prowlerModes.filter { it.startsWith(lastToken, ignoreCase = true) }
             }
        }
        if (command == "trivy" && tokens.size == 2 && !lastToken.startsWith("-")) {
             val trivyModes = listOf("image", "repo", "fs")
             if (trivyModes.any { it.startsWith(lastToken, ignoreCase = true) }) {
                 return trivyModes.filter { it.startsWith(lastToken, ignoreCase = true) }
             }
        }

        // Default to file/folder completions
        val nodes = vfs.listDirectory(vfs.currentPath) ?: return emptyList()
        val fileMatches = nodes.map { if (it.isDirectory) "${it.name}/" else it.name }
        
        if (lastToken.isNotEmpty()) {
            return fileMatches.filter { it.startsWith(lastToken, ignoreCase = true) }
        }
        return fileMatches
    }
}
