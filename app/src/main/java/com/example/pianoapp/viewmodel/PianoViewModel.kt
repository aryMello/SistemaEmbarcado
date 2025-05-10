package com.yourpackage.pianoapp.viewmodel

import android.content.Context
import android.media.SoundPool
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourdomain.pianoapp.audio.PianoSoundPlayer
import com.yourpackage.pianoapp.mqtt.MqttManager
import kotlinx.coroutines.launch
import org.json.JSONObject

class PianoViewModel : ViewModel() {
    private val _activeNotes = mutableStateOf<Set<String>>(emptySet())
    val activeNotes: State<Set<String>> = _activeNotes

    private val _velocities = mutableStateOf<Map<String, Int>>(emptyMap())
    val velocities: State<Map<String, Int>> = _velocities

    private val _isConnected = mutableStateOf(false)
    val isConnected: State<Boolean> = _isConnected

    private lateinit var soundPlayer: PianoSoundPlayer
    private lateinit var mqttManager: MqttManager

    fun initialize(context: Context) {
        // Inicializar o reprodutor de som
        soundPlayer = PianoSoundPlayer(context)
        soundPlayer.loadSounds()

        // Inicializar o gerenciador MQTT
        mqttManager = MqttManager(
            brokerUrl = "tcp://seu-servidor-linux.com:1883",
            clientId = "AndroidPianoApp-${java.util.UUID.randomUUID()}",
            onConnectionChanged = { connected ->
                _isConnected.value = connected
            },
            onMessageReceived = { message ->
                processMessage(message)
            }
        )

        // Conectar ao broker MQTT
        viewModelScope.launch {
            mqttManager.connect()
        }
    }

    private fun processMessage(message: String) {
        try {
            val jsonData = JSONObject(message)

            // Processar notas ativas
            val notes = mutableSetOf<String>()
            val noteVelocities = mutableMapOf<String, Int>()

            if (jsonData.has("notes")) {
                val notesArray = jsonData.getJSONArray("notes")
                for (i in 0 until notesArray.length()) {
                    val note = notesArray.getString(i)
                    notes.add(note)

                    // Obter a velocidade/volume da nota
                    val velocity = if (jsonData.has(note)) jsonData.getInt(note) else 127
                    noteVelocities[note] = velocity

                    // Tocar a nota
                    playNote(note, velocity)
                }
            }

            // Atualizar o estado com as notas ativas
            _activeNotes.value = notes
            _velocities.value = noteVelocities

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playNote(note: String, velocity: Int) {
        soundPlayer.playNote(note, velocity)
    }

    override fun onCleared() {
        super.onCleared()
        // Limpar recursos
        mqttManager.disconnect()
        soundPlayer.release()
    }
}