package com.yourpackage.pianoapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.Alignment

@Composable
fun PianoKeyboard(
    activeNotes: Set<String>,
    velocities: Map<String, Int>,
    onKeyPressed: (String) -> Unit
) {
    val whiteNotes = listOf(
        // Oitava 3
        "C3", "D3", "E3", "F3", "G3", "A3", "B3",
        // Oitava 4
        "C4", "D4", "E4", "F4", "G4", "A4", "B4",
        // Oitava 5
        "C5", "D5", "E5", "F5", "G5", "A5", "B5"
    )

    val blackNotesPattern = listOf(
        // Oitava 3
        "C#3", "D#3", null, "F#3", "G#3", "A#3", null,
        // Oitava 4
        "C#4", "D#4", null, "F#4", "G#4", "A#4", null,
        // Oitava 5
        "C#5", "D#5", null, "F#5", "G#5", "A#5", null
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        val scrollState = rememberScrollState()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(scrollState)
        ) {
            Box(
                modifier = Modifier
                    .width((whiteNotes.size * 48).dp)
                    .height(300.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    whiteNotes.forEach { note ->
                        val isActive = note in activeNotes
                        val velocity = velocities[note] ?: 100

                        WhiteKey(
                            note = note,
                            isActive = isActive,
                            velocity = velocity,
                            onClick = { onKeyPressed(note) }
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    Spacer(modifier = Modifier.width(30.dp))

                    var currentPos = 0
                    blackNotesPattern.forEach { note ->
                        if (note != null) {
                            val isActive = note in activeNotes
                            val velocity = velocities[note] ?: 100

                            Box(
                                modifier = Modifier.width(48.dp),
                                contentAlignment = Alignment.TopCenter
                            ) {
                                BlackKey(
                                    note = note,
                                    isActive = isActive,
                                    velocity = velocity,
                                    onClick = { onKeyPressed(note) }
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.width(48.dp))
                        }
                        currentPos++
                    }
                }
            }
        }
    }
}