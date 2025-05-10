package com.yourdomain.pianoapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.yourdomain.pianoapp.ui.theme.PianoAppTheme
import com.yourpackage.pianoapp.ui.screens.PianoScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PianoAppTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    PianoScreen()
                }
            }
        }
    }
}