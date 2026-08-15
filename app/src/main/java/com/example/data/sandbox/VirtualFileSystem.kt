package com.example.data.sandbox

data class VfsNode(
    val name: String,
    val isDirectory: Boolean,
    var content: String = "",
    val permissions: String = if (isDirectory) "drwxr-xr-x" else "-rw-r--r--",
    val owner: String = "root",
    val group: String = "root",
    val size: Int = content.length,
    val children: MutableMap<String, VfsNode> = mutableMapOf()
)

class VirtualFileSystem {
    private val root = VfsNode(name = "/", isDirectory = true)
    var currentPath: String = "/root"
        private set

    init {
        setupDefaultFileSystem()
    }

    private fun setupDefaultFileSystem() {
        // Create standard hierarchy
        mkdirp("/root")
        mkdirp("/home/operator")
        mkdirp("/etc/security")
        mkdirp("/var/log/nginx")
        mkdirp("/var/log/suricata")
        mkdirp("/opt/wordlists")
        mkdirp("/opt/exploits")
        mkdirp("/challenges")
        mkdirp("/tmp")

        // Root home files
        writeFile("/root/notes.txt", """
            [!] CONFIDENTIAL - TARGET INFRASTRUCTURE NOTES
            - Primary Gateway: 10.0.4.1 (Cisco ASA Firewall)
            - Web DMZ Server: 10.10.10.50 (Apache 2.4.52 / PHP 8.1.2)
            - Production DB: 10.10.10.60 (MySQL 8.0.35 on port 3306)
            - Cloud VPC: AWS Account 123456789012 (us-east-1)
            - Open CTF flags located in /challenges/ directory
        """.trimIndent())

        writeFile("/root/.bash_history", """
            nmap -sS -T4 10.10.10.50
            gobuster dir -u http://10.10.10.50/ -w /opt/wordlists/common.txt
            sqlmap -u "http://10.10.10.50/item.php?id=1" --batch
            cat /var/log/auth.log | grep Failed
            prowler aws --compliance cis_2.0_aws
        """.trimIndent())

        writeFile("/root/exploit_poc.py", """
            #!/usr/bin/env python3
            # POC: Buffer Overflow Demonstration
            import socket

            target_host = "10.10.10.50"
            target_port = 9999

            payload = b"A" * 524 + b"\xef\xbe\xad\xde" # EIP overwrite
            print(f"[*] Sending payload ({len(payload)} bytes) to {target_host}:{target_port}...")
            # s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            # s.connect((target_host, target_port))
            # s.send(payload)
            print("[+] Payload delivered successfully!")
        """.trimIndent())

        // Operator home files
        writeFile("/home/operator/readme.txt", """
            Welcome Operator.
            Your terminal sandbox has full root privileges in this isolated node.
            Use 'help' to view available security tools, or explore the filesystem.
            Submit CTF flags using 'submit FLAG{...}' or 'ctf submit <flag>'.
        """.trimIndent())

        writeFile("/home/operator/passwords.txt", """
            root:Summer2024!
            admin:password123
            operator:shadowmaster
            guest:guest1234
            service_account:CyberWarrior2026!
        """.trimIndent())

        // System Configuration
        writeFile("/etc/passwd", """
            root:x:0:0:root:/root:/bin/bash
            daemon:x:1:1:daemon:/usr/sbin:/usr/sbin/nologin
            bin:x:2:2:bin:/bin:/usr/sbin/nologin
            sys:x:3:3:sys:/dev:/usr/sbin/nologin
            operator:x:1000:1000:Operator,,,:/home/operator:/bin/bash
            sshd:x:121:65534::/run/sshd:/usr/sbin/nologin
            mysql:x:122:128:MySQL Server,,,:/nonexistent:/bin/false
            www-data:x:33:33:www-data:/var/www:/usr/sbin/nologin
        """.trimIndent())

        writeFile("/etc/hosts", """
            127.0.0.1   localhost
            127.0.1.1   cyberlab-node-01
            10.0.4.15   cyberlab-node-01.sandbox.cyberlab.internal
            10.10.10.50 target-web.internal.corp
            10.10.10.60 target-db.internal.corp
        """.trimIndent())

        writeFile("/etc/os-release", """
            NAME="CyberLab Hardened Linux"
            VERSION="2026.1 (Antigravity-Edition)"
            ID=cyberlab-linux
            ID_LIKE=debian
            PRETTY_NAME="CyberLab Security OS v3.4 x86_64"
            VERSION_ID="3.4"
            HOME_URL="https://cyberlab.security"
        """.trimIndent())

        writeFile("/etc/shadow", """
            root:${'$'}6${'$'}rounds=50000${'$'}xyz99872${'$'}9b23f8c8a14d5e:19800:0:99999:7:::
            operator:${'$'}6${'$'}rounds=50000${'$'}abc11223${'$'}e4d3c2b1a09876:19800:0:99999:7:::
            admin:${'$'}6${'$'}rounds=50000${'$'}pass4455${'$'}1a2b3c4d5e6f70:19800:0:99999:7:::
        """.trimIndent())

        // Log Files
        writeFile("/var/log/auth.log", """
            Aug 13 21:45:10 cyberlab sshd[2841]: Failed password for invalid user admin from 185.220.101.5 port 44821 ssh2
            Aug 13 21:45:12 cyberlab sshd[2845]: Failed password for invalid user root from 185.220.101.5 port 44824 ssh2
            Aug 13 21:45:15 cyberlab sshd[2850]: Failed password for invalid user deploy from 185.220.101.5 port 44830 ssh2
            Aug 13 21:48:02 cyberlab sudo:   operator : TTY=pts/0 ; PWD=/home/operator ; USER=root ; COMMAND=/bin/su -
            Aug 13 21:48:02 cyberlab sudo: pam_unix(sudo:session): session opened for user root(uid=0) by operator(uid=1000)
            Aug 13 21:50:44 cyberlab sshd[2910]: Accepted password for root from 192.168.1.50 port 51230 ssh2
        """.trimIndent())

        writeFile("/var/log/nginx/access.log", """
            185.220.101.5 - - [13/Aug/2026:21:40:12 +0000] "GET /robots.txt HTTP/1.1" 200 85 "-" "Mozilla/5.0"
            185.220.101.5 - - [13/Aug/2026:21:40:15 +0000] "GET /item.php?id=1%27%20OR%201=1-- HTTP/1.1" 200 4512 "-" "sqlmap/1.7.11"
            185.220.101.5 - - [13/Aug/2026:21:40:22 +0000] "GET /admin/config.php.bak HTTP/1.1" 200 840 "-" "gobuster/3.6"
            10.0.4.15 - - [13/Aug/2026:22:01:05 +0000] "GET /api/v1/health HTTP/1.1" 200 42 "-" "curl/7.88.1"
        """.trimIndent())

        // Wordlists & Tools
        writeFile("/opt/wordlists/common.txt", """
            admin
            api
            config.php.bak
            dashboard
            images
            login
            robots.txt
            server-status
            secret
            backup.zip
            .env
            .git
        """.trimIndent())

        // CTF Directory
        writeFile("/challenges/instructions.txt", """
            ================ CTF CHALLENGE SANDBOX ================
            Available local challenge artifacts:
            1. flag1_crypto.txt      - Base64 & XOR cipher challenge
            2. flag2_stego.txt       - Hidden forensic ASCII metadata
            3. flag3_privesc.txt     - Misconfigured SUID binary artifact
            
            Submit flags using:
              submit FLAG{...}
              or
              ctf submit FLAG{...}
        """.trimIndent())

        writeFile("/challenges/flag1_crypto.txt", """
            Ciphertext (Base64 encoded XOR with key '0x42'):
            RkxBR3t4b3JfY2lwaGVyc19hcmVfZWFzeV90b19icmVha30=
            
            Decoded hint: The flag format is standard FLAG{...}
        """.trimIndent())

        writeFile("/challenges/flag2_stego.txt", """
            Steganography / Hidden Metadata:
            [+] EXIF Artist: Operator_Anonymous
            [+] Comment: FLAG{h1dd3n_1n_pl41n_s1ght_f0r3ns1cs}
        """.trimIndent())
    }

    fun resolvePath(path: String): String {
        var clean = path.trim()
        if (clean == "~" || clean.isEmpty()) return "/root"
        if (clean.startsWith("~/")) clean = "/root" + clean.substring(1)
        if (!clean.startsWith("/")) {
            clean = if (currentPath == "/") "/$clean" else "$currentPath/$clean"
        }

        // Normalize . and ..
        val segments = clean.split("/").filter { it.isNotEmpty() }
        val stack = mutableListOf<String>()
        for (seg in segments) {
            if (seg == ".") continue
            if (seg == "..") {
                if (stack.isNotEmpty()) stack.removeAt(stack.size - 1)
            } else {
                stack.add(seg)
            }
        }
        return "/" + stack.joinToString("/")
    }

    private fun getNode(path: String): VfsNode? {
        val resolved = resolvePath(path)
        if (resolved == "/" || resolved.isEmpty()) return root
        val parts = resolved.split("/").filter { it.isNotEmpty() }
        var current = root
        for (part in parts) {
            current = current.children[part] ?: return null
        }
        return current
    }

    fun changeDirectory(path: String): Boolean {
        val target = resolvePath(path)
        val node = getNode(target)
        return if (node != null && node.isDirectory) {
            currentPath = target
            true
        } else {
            false
        }
    }

    fun listDirectory(path: String = currentPath): List<VfsNode>? {
        val node = getNode(path) ?: return null
        return if (node.isDirectory) {
            node.children.values.sortedWith(compareBy({ !it.isDirectory }, { it.name }))
        } else {
            null
        }
    }

    fun readFile(path: String): String? {
        val node = getNode(path) ?: return null
        return if (!node.isDirectory) node.content else null
    }

    fun writeFile(path: String, content: String, append: Boolean = false): Boolean {
        val resolved = resolvePath(path)
        val parentPath = if (resolved.lastIndexOf('/') <= 0) "/" else resolved.substring(0, resolved.lastIndexOf('/'))
        val fileName = resolved.substring(resolved.lastIndexOf('/') + 1)
        if (fileName.isEmpty()) return false

        val parentNode = getNode(parentPath) ?: return false
        if (!parentNode.isDirectory) return false

        val existing = parentNode.children[fileName]
        if (existing != null) {
            if (existing.isDirectory) return false
            existing.content = if (append) existing.content + "\n" + content else content
        } else {
            parentNode.children[fileName] = VfsNode(
                name = fileName,
                isDirectory = false,
                content = content
            )
        }
        return true
    }

    fun mkdirp(path: String): Boolean {
        val resolved = resolvePath(path)
        val parts = resolved.split("/").filter { it.isNotEmpty() }
        var current = root
        for (part in parts) {
            if (!current.children.containsKey(part)) {
                val newNode = VfsNode(name = part, isDirectory = true)
                current.children[part] = newNode
                current = newNode
            } else {
                val existing = current.children[part]!!
                if (!existing.isDirectory) return false
                current = existing
            }
        }
        return true
    }

    fun deleteNode(path: String): Boolean {
        val resolved = resolvePath(path)
        if (resolved == "/" || resolved == "/root") return false
        val parentPath = if (resolved.lastIndexOf('/') <= 0) "/" else resolved.substring(0, resolved.lastIndexOf('/'))
        val fileName = resolved.substring(resolved.lastIndexOf('/') + 1)

        val parentNode = getNode(parentPath) ?: return false
        return parentNode.children.remove(fileName) != null
    }

    fun getPromptPath(): String {
        return when {
            currentPath == "/root" -> "~"
            currentPath.startsWith("/root/") -> "~" + currentPath.removePrefix("/root")
            else -> currentPath
        }
    }
}
