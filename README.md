# CYBER_LAB_PRO 🛡️⚡

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=flat-square&logo=android&logoColor=white)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-7F52FF?style=flat-square&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20(M3)-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Room Database](https://img.shields.io/badge/Storage-Room%20DB-FFA000?style=flat-square&logo=sqlite&logoColor=white)](https://developer.android.com/training/data-storage/room)
[![Gemini AI](https://img.shields.io/badge/AI-Google%20Gemini%20API-4E79A7?style=flat-square&logo=google&logoColor=white)](https://ai.google.dev/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg?style=flat-square)](LICENSE)

**CYBER_LAB_PRO** is a comprehensive, production-grade cybersecurity and ethical hacking training lab built natively for Android using Jetpack Compose and Kotlin. Designed for security researchers, penetration testers, CTF competitors, and students, the application delivers a hands-on cybersecurity toolkit, interactive sandbox CLI, threat intelligence visualizers, and an AI-powered security copilot.

---

## 🌟 Key Features

### 🖥️ Interactive Sandbox CLI & Terminal
* **Simulated Terminal Shell**: Realistic security command-line interface with interactive execution, output streaming, and syntax formatting.
* **Security Command Suite**: Built-in commands including `nmap`, `ping`, `whois`, `hash`, `cve`, `scan`, `decrypt`, `exploit`, `systeminfo`, `clear`, and more.
* **Command History & Quick Bar**: Persistent command buffer with instant replay and dedicated quick-action chips.
* **Command Palette**: Fast modal launcher (`Ctrl`/`⌘` style) for searching and executing tools rapidly.

### 🛠️ Security Tools Hub
* **Network & Port Diagnostic Scanners**: Simulate host discovery, service enumeration, and open-port probing.
* **Cryptographic Suite**: Hash calculation and cracking for MD5, SHA-1, SHA-256, SHA-512, and HMAC.
* **Encoder / Decoder Playground**: Multi-format converter supporting Base64, Hexadecimal, URL Encoding, and ROT13.
* **Password Strength & Entropy Analyzer**: Real-time evaluation of password complexity, entropy bits, and estimated crack times.

### 🗺️ Live Threat Intelligence Map
* **Interactive Attack Visualizer**: Dynamic visual representation of global cyber attacks, telemetry vectors, and geo-coordinates.
* **Attack Vector Classification**: Real-time monitoring for DDoS strikes, Ransomware outbreaks, Phishing campaigns, and Zero-Day exploits.
* **Telemetry Feeds**: Live chronological threat log with severity tags, protocol details, and geographic attribution.

### 🕵️ Cyber Fraud & Financial Threat Intel
* **Fraud Vector Library**: In-depth analysis of SIM swapping, banking Trojans, social engineering schemes, and cryptocurrency fraud.
* **IOC & Scam Indicator Explorer**: Database of malicious signatures, suspicious domain structures, and rogue wallet patterns.
* **Mitigation Playbooks**: Step-by-step incident response and remediation checklists for compromised credentials.

### 🧪 CVE Explorer & Vulnerability Lab
* **Vulnerability Database**: Searchable CVE repository with CVSS v3.1 severity metrics, attack vectors, and exploit availability status.
* **Automated Audit Simulator**: Run mock configuration scans against target profiles to uncover configuration weaknesses.
* **Remediation Guides**: Technical patch summaries and defensive mitigation strategies.

### 🚩 CTF (Capture The Flag) Arena
* **Hands-on Challenges**: Practice challenges categorized across:
  * 🌐 Web Exploitation (SQLi, XSS, CSRF, Header Injection)
  * 🔐 Cryptography (Cipher cracking, RSA flaws, Substitution)
  * ⚙️ Reverse Engineering & Binary Analysis
  * 🔍 Forensics & Steganography
  * 🌐 OSINT & Reconnaissance
* **Scoreboard & Badges**: Real-time point tracking, hint unlock system, and challenge solve tracking.

### 🤖 AI Cyber Copilot (Powered by Google Gemini)
* **Intelligent Threat Advisory**: Context-aware AI assistant tailored for offensive and defensive security queries.
* **Code & Payload Analysis**: Inspect decompiled code, audit script security, and extract executable terminal commands directly into the CLI.
* **Secure API Key Management**: On-device key storage encrypted via Android Keystore & `EncryptedSharedPreferences`.

### 👥 Cyber Community Hub
* **Security Advisories**: Community-driven discussions, incident write-ups, and vulnerability disclosures.
* **Discussion Threads**: Collaborative thread creation with upvotes, tags, and category filtering.

---

## 🏗️ Architecture & Tech Stack

The project follows modern Android architecture guidelines using **MVVM (Model-View-ViewModel)** and **Unidirectional Data Flow (UDF)**:

```
┌────────────────────────────────────────────────────────┐
│                   Jetpack Compose UI                   │
│   (TerminalScreen, ToolsScreen, ThreatMap, CTF, etc.)  │
└───────────────────────────▲────────────────────────────┘
                            │ UI State & Events
┌───────────────────────────┴────────────────────────────┐
│                  CyberLabViewModel                     │
│         (StateFlow, Coroutines, Business Logic)        │
└───────────────────────────▲────────────────────────────┘
                            │ Data Flow
┌───────────────────────────┴────────────────────────────┐
│                 CyberLabRepository                     │
└──────────────┬──────────────────────────┬──────────────┘
               │                          │
┌──────────────▼──────────────┐ ┌─────────▼──────────────┐
│        Room Database        │ │    Network / AI API    │
│  (CyberDao, Local SQLite)   │ │ (Retrofit, Gemini SDK) │
└─────────────────────────────┘ └────────────────────────┘
```

* **Language**: Kotlin 2.2.10
* **UI Toolkit**: Jetpack Compose with Material Design 3 (M3)
* **Asynchronous Flow**: Kotlin Coroutines & `StateFlow`
* **Local Persistence**: Android Room Database (`CyberDatabase`)
* **Security & Encryption**: `androidx.security:security-crypto` (`EncryptedSharedPreferences`, AES-256 GCM)
* **Networking & JSON**: Retrofit 2, OkHttp 3, Moshi
* **Audio Engine**: Custom synthesizer for tactile audio feedback
* **Code Obfuscation**: ProGuard / R8 with tailored keep rules

---

## 🚀 Getting Started

### Prerequisites
* **Android Studio**: Ladybug (2024.2.1) or newer
* **JDK**: Version 11 or 17
* **Android SDK**: `minSdk = 24`, `targetSdk = 36`

### Cloning and Building

1. **Clone the repository**:
   ```bash
   git clone https://github.com/your-username/cyber-lab-pro.git
   cd cyber-lab-pro
   ```

2. **Configure Gemini API Key (Optional)**:
   * Copy the example environment file:
     ```bash
     cp .env.example .env
     ```
   * Add your Gemini API key from [Google AI Studio](https://aistudio.google.com/):
     ```properties
     GEMINI_API_KEY=your_gemini_api_key_here
     ```
   * *Note: You can also enter and securely save your API key directly inside the app under **Settings > Gemini API Key**.*

3. **Build the Debug APK**:
   ```bash
   ./gradlew assembleDebug
   ```

4. **Run Unit Tests**:
   ```bash
   ./gradlew testDebugUnitTest
   ```

---

## 🔒 Security & Privacy

* **Local-First Architecture**: All scan logs, CTF progress, forum posts, and terminal command history are stored locally in an on-device SQLite database via Room.
* **Encrypted Secrets**: Sensitive keys are protected using hardware-backed Android Keystore cryptography.
* **Obfuscation**: Release builds have `isMinifyEnabled` and `isShrinkResources` enabled with strict R8 optimization rules.
* **Educational Disclaimer**: *CYBER_LAB_PRO is intended solely for educational purposes, authorized security research, and defensive training. Unauthorized testing of systems without prior written consent is strictly prohibited.*

---

## 🤝 Contributing

Contributions are welcome! To contribute:
1. Fork the repository.
2. Create a feature branch (`git checkout -b feature/amazing-feature`).
3. Commit your changes (`git commit -m 'Add some amazing feature'`).
4. Push to the branch (`git push origin feature/amazing-feature`).
5. Open a Pull Request.

---

## 📄 License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.
