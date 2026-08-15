package com.example.ui.screens

import androidx.compose.runtime.Composable
import com.example.ui.components.TerminalComponent
import com.example.ui.viewmodel.CyberLabViewModel
import com.example.ui.viewmodel.TerminalViewModel

@Composable
fun TerminalScreen(viewModel: CyberLabViewModel) {
    TerminalComponent(viewModel = viewModel.terminalViewModel)
}

@Composable
fun TerminalScreen(terminalViewModel: TerminalViewModel) {
    TerminalComponent(viewModel = terminalViewModel)
}
