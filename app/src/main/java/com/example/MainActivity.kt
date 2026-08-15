package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.ui.components.CommandPaletteDialog
import com.example.ui.components.CyberBottomNav
import com.example.ui.components.CyberTopBar
import com.example.ui.screens.*
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.CyberLabViewModel
import com.example.ui.viewmodel.CyberTab

class MainActivity : ComponentActivity() {
    private val viewModel: CyberLabViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        com.example.audio.CyberAudioEngine.initialize(this)
        enableEdgeToEdge()
        setContent {
            val isDarkTheme by viewModel.isDarkTheme.collectAsState()
            MyApplicationTheme(darkTheme = isDarkTheme) {
                CyberLabApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun CyberLabApp(viewModel: CyberLabViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    val isMuted by viewModel.isMuted.collectAsState()
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val showCommandPalette by viewModel.showCommandPalette.collectAsState()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        topBar = {
            CyberTopBar(
                isMuted = isMuted,
                isDarkTheme = isDarkTheme,
                onToggleSound = { viewModel.toggleSound() },
                onToggleTheme = { viewModel.toggleTheme() },
                onOpenCommandPalette = { viewModel.toggleCommandPalette(true) }
            )
        },
        bottomBar = {
            CyberBottomNav(
                selectedTab = currentTab,
                onSelectTab = { viewModel.setTab(it) }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            androidx.compose.animation.AnimatedContent(
                targetState = currentTab,
                transitionSpec = {
                    androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(300)).togetherWith(
                        androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(300))
                    )
                },
                label = "tab_transition"
            ) { targetTab ->
                when (targetTab) {
                    CyberTab.TERMINAL -> TerminalScreen(viewModel = viewModel)
                    CyberTab.TOOLS -> ToolsScreen(viewModel = viewModel)
                    CyberTab.THREAT_MAP -> ThreatMapScreen(viewModel = viewModel)
                    CyberTab.FRAUD_INTEL -> CyberFraudScreen(viewModel = viewModel)
                    CyberTab.VULN_LAB -> VulnerabilityLabScreen(viewModel = viewModel)
                    CyberTab.CTF -> CtfScreen(viewModel = viewModel)
                    CyberTab.COMMUNITY -> CommunityHubScreen(viewModel = viewModel)
                    CyberTab.AI_ADVISOR -> AiAdvisorScreen(viewModel = viewModel)
                    CyberTab.SETTINGS -> SettingsScreen(viewModel = viewModel)
                }
            }

            if (showCommandPalette) {
                CommandPaletteDialog(
                    onDismiss = { viewModel.toggleCommandPalette(false) },
                    onNavigateTab = { tab -> viewModel.setTab(tab) },
                    onExecuteCommand = { cmd -> viewModel.executeTerminalCommand(cmd) }
                )
            }
        }
    }
}
