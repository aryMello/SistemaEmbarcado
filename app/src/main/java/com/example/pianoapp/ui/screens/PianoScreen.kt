package com.yourpackage.pianoapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yourpackage.pianoapp.ui.components.ConnectionStatusBar
import com.yourpackage.pianoapp.ui.components.PianoKeyboard
import com.yourpackage.pianoapp.viewmodel.PianoViewModel

@Composable
fun PianoScreen() {
    val pianoViewModel: PianoViewModel = viewModel()
    val activeNotes by pianoViewModel.activeNotes
    val velocities by pianoViewModel.velocities

    val context = LocalContext.current
    LaunchedEffect(key1 = Unit) {
        pianoViewModel.initialize(context)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Piano Virtual",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            textAlign = TextAlign.Center
        )

        // Interface do piano
        PianoKeyboard(
            activeNotes = activeNotes,
            velocities = velocities,
            onKeyPressed = { note ->
                pianoViewModel.playNote(note, 100)
            }
        )

        ConnectionStatusBar(isConnected = pianoViewModel.isConnected.value)
    }
}