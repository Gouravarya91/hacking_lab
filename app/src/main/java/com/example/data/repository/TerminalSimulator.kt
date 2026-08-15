package com.example.data.repository

import com.example.data.model.TerminalLine
import java.util.UUID

object TerminalSimulator {
    fun processCommand(cmd: String): List<TerminalLine> {
        val trimmed = cmd.trim()
        val parts = trimmed.split("\\s+".toRegex())
        val binary = parts.firstOrNull()?.lowercase() ?: ""

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

        when (binary) {
            "help", "?" -> {
                add(TerminalLine.LineType.BANNER, "[!] CYBER_LAB_PRO VIRTUAL SANDBOX CLI COMMAND REFERENCE")
                add(TerminalLine.LineType.OUTPUT, """
                    Core Security Tools:
                      nmap [options] <target>       - Network port & service vulnerability scanner
                      sqlmap -u <url> [options]    - Automatic SQL injection and DB enumeration
                      hydra -l <user> -P <list>... - Network authentication brute-forcing tool
                      gobuster dir -u <url>...     - Fast web directory & subdomain brute-forcer
                      nikto -h <target>            - Web server misconfiguration and CGI scanner
                      john <hashfile>              - John the Ripper password hash recovery
                      hashcat -m <mode> ...        - GPU accelerated hash cracker simulation
                      prowler aws/gcp              - Cloud infrastructure CIS benchmark auditor
                      trivy image/repo <target>    - Container & artifact vulnerability scanner
                      sherlock <username>          - OSINT social media username footprinting
                      theharvester -d <domain>     - Email, subdomain, and employee scraper
                      whois / dig / traceroute     - Network reconnaissance utilities

                    System & Forensics:
                      cat /var/log/auth.log        - Linux SSH authentication triage logs
                      ps aux                       - Active processes and memory telemetry
                      netstat -tulnp               - Listening ports and active sockets
                      iptables -L                  - Firewall filter chains and rules
                      whoami / id / uname -a       - User privileges and kernel identification
                      clear                        - Clear sandbox terminal buffer
                """.trimIndent())
            }

            "whoami" -> {
                add(TerminalLine.LineType.SUCCESS, "root (Operator UID=0 GID=0 Groups=0(root),27(sudo),44(video))")
            }

            "id" -> {
                add(TerminalLine.LineType.OUTPUT, "uid=0(root) gid=0(root) groups=0(root),4(adm),24(cdrom),27(sudo),30(dip),46(plugdev)")
            }

            "uname" -> {
                if (parts.contains("-a")) {
                    add(TerminalLine.LineType.OUTPUT, "Linux cyberlab-node-01 6.9.12-cyberlab-custom #1 SMP PREEMPT_DYNAMIC x86_64 GNU/Linux")
                } else {
                    add(TerminalLine.LineType.OUTPUT, "Linux")
                }
            }

            "nmap" -> {
                val target = parts.lastOrNull { !it.startsWith("-") && it != "nmap" } ?: "192.168.1.100"
                add(TerminalLine.LineType.SYSTEM, "Starting Nmap 7.94 ( https://nmap.org ) at 2026-08-13 22:00 UTC")
                add(TerminalLine.LineType.SYSTEM, "Nmap scan report for $target (host is up: 0.0024s latency).")
                add(TerminalLine.LineType.SYSTEM, "Not shown: 993 closed tcp ports (reset)")
                add(TerminalLine.LineType.TABLE, """
PORT     STATE SERVICE     VERSION
21/tcp   open  ftp         vsftpd 3.0.3 (Anonymous FTP Allowed)
22/tcp   open  ssh         OpenSSH 8.9p1 Ubuntu (protocol 2.0)
80/tcp   open  http        Apache httpd 2.4.52 ((Ubuntu) PHP/8.1.2)
139/tcp  open  netbios-ssn Samba smbd 4.6.2
443/tcp  open  ssl/http    Apache/2.4.52 (SSL-Cert: self-signed)
445/tcp  open  netbios-ssn Samba smbd 4.6.2 (Workgroup: WORKGROUP)
3306/tcp open  mysql       MySQL 8.0.35-0ubuntu0.22.04.1
                """.trimIndent())
                if (parts.contains("--script") || parts.contains("vuln") || parts.contains("-A")) {
                    add(TerminalLine.LineType.ERROR, """
| vulners:
|   cpe:/a:apache:httpd:2.4.52:
|     CVE-2023-25690  9.8  https://vulners.com/cve/CVE-2023-25690
|     CVE-2022-22720  9.8  https://vulners.com/cve/CVE-2022-22720
|_smb-vuln-ms17-010: Remote Code Execution vulnerability in Microsoft SMBv1 (CLEAN)
                    """.trimIndent())
                }
                add(TerminalLine.LineType.SUCCESS, "Nmap done: 1 IP address (1 host up) scanned in 2.81 seconds.")
            }

            "sqlmap" -> {
                add(TerminalLine.LineType.BANNER, """
    ___ ___| |_____ ___ ___  {1.7.11#stable}
   |_ -| . | |     | .'| . |
   |___|_  |_|_|_|_|__,|  _| http://sqlmap.org
         |_|           |_|
                """.trimIndent())
                add(TerminalLine.LineType.SYSTEM, "[*] starting @ 22:01:14 /2026-08-13/")
                add(TerminalLine.LineType.SYSTEM, "[INFO] testing connection to the target URL")
                add(TerminalLine.LineType.SYSTEM, "[INFO] checking if the target is protected by some kind of WAF/IPS")
                add(TerminalLine.LineType.SUCCESS, "[+] heuristic (basic) test shows that GET parameter 'id' might be injectable (possible DBMS: 'MySQL')")
                add(TerminalLine.LineType.SUCCESS, "[+] GET parameter 'id' is vulnerable. Do you want to keep testing the others? [y/N] N")
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
            }

            "hydra" -> {
                add(TerminalLine.LineType.BANNER, "Hydra v9.5 (c) 2023 by van Hauser / THC & David Maciejak - Online Pass Crack")
                add(TerminalLine.LineType.SYSTEM, "[DATA] max 16 tasks per target, 500 login tries, testing service: ssh")
                add(TerminalLine.LineType.SYSTEM, "[ATTACK] targeting 192.168.1.50:22 (SSH)...")
                add(TerminalLine.LineType.OUTPUT, "[22][ssh] host: 192.168.1.50   login: root   password: password123 [FAILED]")
                add(TerminalLine.LineType.OUTPUT, "[22][ssh] host: 192.168.1.50   login: root   password: admin [FAILED]")
                add(TerminalLine.LineType.OUTPUT, "[22][ssh] host: 192.168.1.50   login: root   password: toor [FAILED]")
                add(TerminalLine.LineType.SUCCESS, "[22][ssh] host: 192.168.1.50   login: root   password: Summer2024! [VALID CREDENTIALS]")
                add(TerminalLine.LineType.SUCCESS, "1 of 1 target completed, 1 valid password found.")
            }

            "gobuster" -> {
                add(TerminalLine.LineType.BANNER, "===============================================================\nGobuster v3.6 - Written by OJ Reeves (@The колобок) & Christian Mehlmauer\n===============================================================")
                add(TerminalLine.LineType.SYSTEM, "[+] Url:                     http://10.10.10.50/")
                add(TerminalLine.LineType.SYSTEM, "[+] Method:                  GET")
                add(TerminalLine.LineType.SYSTEM, "[+] Threads:                 10")
                add(TerminalLine.LineType.SYSTEM, "[+] Wordlist:                /usr/share/wordlists/dirb/common.txt")
                add(TerminalLine.LineType.TABLE, """
/admin                (Status: 301) [Size: 312] [--> http://10.10.10.50/admin/]
/api                  (Status: 200) [Size: 1420]
/config.php.bak       (Status: 200) [Size: 840] [CRITICAL SENSITIVE FILE]
/dashboard            (Status: 302) [Size: 0] [--> /login.php]
/images               (Status: 301) [Size: 314]
/robots.txt           (Status: 200) [Size: 85]
/server-status        (Status: 403) [Size: 277]
                """.trimIndent())
                add(TerminalLine.LineType.SUCCESS, "===============================================================\nFinished in 1.45s. Total URLs checked: 4614")
            }

            "nikto" -> {
                add(TerminalLine.LineType.SYSTEM, "- Nikto v2.5.0\n---------------------------------------------------------------------------")
                add(TerminalLine.LineType.SYSTEM, "+ Target IP:          192.168.1.100\n+ Target Hostname:    web01.internal.corp\n+ Target Port:        80\n+ Start Time:         2026-08-13 22:04:12")
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
                add(TerminalLine.LineType.SYSTEM, "Press 'q' or Ctrl-C to abort, almost any other key for status")
                add(TerminalLine.LineType.SUCCESS, "dragon           (user_admin)")
                add(TerminalLine.LineType.SUCCESS, "shadowmaster     (operator_02)")
                add(TerminalLine.LineType.OUTPUT, "2g 0:00:00:01 DONE (2026-08-13 22:05) 1.818g/s 4520p/s 4520c/s 4520C/s")
                add(TerminalLine.LineType.SUCCESS, "Use the \"--show\" option to display all of the cracked passwords reliably.")
            }

            "hashcat" -> {
                add(TerminalLine.LineType.SYSTEM, "hashcat (v6.2.6) starting in benchmark / attack mode...")
                add(TerminalLine.LineType.SYSTEM, "OpenCL Platform #1 [NVIDIA Corporation] OpenCL 3.0 CUDA 12.4")
                add(TerminalLine.LineType.OUTPUT, "Hash.Mode........: 1000 (NTLM)")
                add(TerminalLine.LineType.OUTPUT, "Speed.#1.........: 38450.4 MH/s (38.45 GH/s)")
                add(TerminalLine.LineType.SUCCESS, "32ed87b2490fedba7556e1b12f020bc5:CyberWarrior2026!")
                add(TerminalLine.LineType.SUCCESS, "Status...........: Cracked")
                add(TerminalLine.LineType.SUCCESS, "Candidates.#1....: CyberWarrior2024! -> CyberWarrior2026!")
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
                add(TerminalLine.LineType.SYSTEM, "Compliance Summary: 42 Passed | 3 Failed | Score: 93.3% CIS AWS Benchmark v2.0")
            }

            "trivy" -> {
                add(TerminalLine.LineType.SYSTEM, "2026-08-13T22:06:40.120Z INFO Need to update DB")
                add(TerminalLine.LineType.SYSTEM, "2026-08-13T22:06:41.054Z INFO Vulnerability scanning image: alpine:3.18")
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
                val username = parts.getOrNull(1) ?: "cyber_operator"
                add(TerminalLine.LineType.SYSTEM, "[*] Checking username '$username' on 300+ social sites...")
                add(TerminalLine.LineType.SUCCESS, "[+] GitHub: https://github.com/$username")
                add(TerminalLine.LineType.SUCCESS, "[+] Twitter/X: https://x.com/$username")
                add(TerminalLine.LineType.SUCCESS, "[+] Reddit: https://www.reddit.com/user/$username")
                add(TerminalLine.LineType.SUCCESS, "[+] DockerHub: https://hub.docker.com/u/$username")
                add(TerminalLine.LineType.SUCCESS, "[+] Medium: https://medium.com/@$username")
                add(TerminalLine.LineType.OUTPUT, "[*] Search completed. Found 5 active accounts.")
            }

            "theharvester" -> {
                add(TerminalLine.LineType.SYSTEM, "[*] theHarvester 4.4.0 - Gathering accounts for domain target.corp")
                add(TerminalLine.LineType.OUTPUT, "[*] Harvesting search engines: Google, Bing, LinkedIn...")
                add(TerminalLine.LineType.TABLE, """
[*] Emails found (4):
  - admin@target.corp
  - ciso-security@target.corp
  - john.doe.dev@target.corp
  - hr-recruiting@target.corp

[*] Hosts / Subdomains found (5):
  - vpn.target.corp (198.51.100.22)
  - api.target.corp (198.51.100.25)
  - mail.target.corp (198.51.100.10)
  - gitlab.internal.target.corp (10.0.8.4)
  - staging.target.corp (198.51.100.99)
                """.trimIndent())
            }

            "cat" -> {
                val file = parts.getOrNull(1) ?: ""
                if (file.contains("auth.log")) {
                    add(TerminalLine.LineType.SYSTEM, "--- Displaying /var/log/auth.log (Triage Sample) ---")
                    add(TerminalLine.LineType.TABLE, """
Aug 13 21:45:10 cyberlab sshd[2841]: Failed password for invalid user admin from 185.220.101.5 port 44821 ssh2
Aug 13 21:45:12 cyberlab sshd[2845]: Failed password for invalid user root from 185.220.101.5 port 44824 ssh2
Aug 13 21:45:15 cyberlab sshd[2850]: Failed password for invalid user deploy from 185.220.101.5 port 44830 ssh2
Aug 13 21:48:02 cyberlab sudo:   operator : TTY=pts/0 ; PWD=/home/operator ; USER=root ; COMMAND=/bin/su -
Aug 13 21:48:02 cyberlab sudo: pam_unix(sudo:session): session opened for user root(uid=0) by operator(uid=1000)
Aug 13 21:50:44 cyberlab sshd[2910]: Accepted publickey for root from 10.0.4.15 port 51230 ssh2: RSA SHA256:4kKj...
                    """.trimIndent())
                } else if (file.contains("passwd")) {
                    add(TerminalLine.LineType.OUTPUT, """
root:x:0:0:root:/root:/bin/bash
daemon:x:1:1:daemon:/usr/sbin:/usr/sbin/nologin
operator:x:1000:1000:Operator,,,:/home/operator:/bin/bash
sshd:x:121:65534::/run/sshd:/usr/sbin/nologin
                    """.trimIndent())
                } else {
                    add(TerminalLine.LineType.OUTPUT, "File content: ($file) empty or simulated.")
                }
            }

            "ps" -> {
                add(TerminalLine.LineType.TABLE, """
USER       PID %CPU %MEM    VSZ   RSS TTY      STAT START   TIME COMMAND
root         1  0.0  0.1 169420 12840 ?        Ss   20:00   0:01 /sbin/init
root       412  0.0  0.2  72480 18200 ?        Ss   20:00   0:00 /usr/sbin/sshd -D
root       640  0.1  0.8 450120 68400 ?        Ssl  20:00   0:04 /usr/sbin/suricata -c /etc/suricata/suricata.yaml
www-data   820  0.0  0.4 224100 34100 ?        S    20:01   0:00 /usr/sbin/apache2 -k start
root      1420  0.0  0.1  24120  4120 pts/0    Ss   21:48   0:00 -bash
root      2840  0.0  0.1  18900  3200 pts/0    R+   22:08   0:00 ps aux
                """.trimIndent())
            }

            "netstat" -> {
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

            "whois" -> {
                val target = parts.getOrNull(1) ?: "example.com"
                add(TerminalLine.LineType.OUTPUT, """
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
                val target = parts.getOrNull(1) ?: "8.8.8.8"
                add(TerminalLine.LineType.SYSTEM, "traceroute to $target (8.8.8.8), 30 hops max, 60 byte packets")
                add(TerminalLine.LineType.TABLE, """
 1  gateway.cyberlab.internal (10.0.4.1)  0.342 ms  0.312 ms  0.298 ms
 2  198.51.100.1 (198.51.100.1)  1.421 ms  1.390 ms  1.355 ms
 3  core-router-01.isp.net (203.0.113.45)  4.812 ms  4.790 ms  4.760 ms
 4  dns.google (8.8.8.8)  8.214 ms  8.190 ms  8.160 ms
                """.trimIndent())
            }

            else -> {
                add(TerminalLine.LineType.ERROR, "bash: $binary: command simulated or unrecognized. Type 'help' to see available tools or select from the quick touchbar.")
            }
        }

        return lines
    }
}
