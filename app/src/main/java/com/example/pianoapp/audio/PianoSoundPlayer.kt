package com.yourdomain.pianoapp.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log
import com.yourdomain.pianoapp.R  // MANTEREMOS esta importação

class PianoSoundPlayer(private val context: Context) {
    private val TAG = "PianoSoundPlayer"

    private val soundPool: SoundPool by lazy {
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()

        SoundPool.Builder()
            .setMaxStreams(10)
            .setAudioAttributes(attributes)
            .build()
    }

    private val soundMap = mutableMapOf<String, Int>()

    // Lista de notas disponíveis - CÓDIGO TEMPORÁRIO, REMOVEREMOS quando tiver os arquivos de áudio
    private val availableNotes = listOf(
        // Oitava 3
        "C3", "C#3", "D3", "D#3", "E3", "F3", "F#3", "G3", "G#3", "A3", "A#3", "B3",
        // Oitava 4
        "C4", "C#4", "D4", "D#4", "E4", "F4", "F#4", "G4", "G#4", "A4", "A#4", "B4",
        // Oitava 5
        "C5", "C#5", "D5", "D#5", "E5", "F5", "F#5", "G5", "G#5", "A5", "A#5", "B5"
    )

    fun loadSounds() {
        // CÓDIGO TEMPORÁRIO - REMOVEREMOS ESTE BLOCO quando tiver os arquivos de áudio
        Log.d(TAG, "Os arquivos de áudio ainda não foram adicionados ao projeto.")
        Log.d(TAG, "Quando estiverem disponíveis, coloque-os na pasta res/raw/")

        for (note in availableNotes) {
            soundMap[note] = 0  // ID temporário
            Log.d(TAG, "Registrada nota: $note com ID temporário")
        }

        // CÓDIGO REAL - DESCOMENTAR quando arrumarmos o projeto e tivermos os arquivos de áudio
        /*
        // Mapear notas para IDs de recursos
        val noteResourceMap = mapOf(
            // Oitava 3
            "C3" to R.raw.piano_c3,
            "C#3" to R.raw.piano_cs3,
            "D3" to R.raw.piano_d3,
            "D#3" to R.raw.piano_ds3,
            "E3" to R.raw.piano_e3,
            "F3" to R.raw.piano_f3,
            "F#3" to R.raw.piano_fs3,
            "G3" to R.raw.piano_g3,
            "G#3" to R.raw.piano_gs3,
            "A3" to R.raw.piano_a3,
            "A#3" to R.raw.piano_as3,
            "B3" to R.raw.piano_b3,

            // Oitava 4
            "C4" to R.raw.piano_c4,
            "C#4" to R.raw.piano_cs4,
            "D4" to R.raw.piano_d4,
            "D#4" to R.raw.piano_ds4,
            "E4" to R.raw.piano_e4,
            "F4" to R.raw.piano_f4,
            "F#4" to R.raw.piano_fs4,
            "G4" to R.raw.piano_g4,
            "G#4" to R.raw.piano_gs4,
            "A4" to R.raw.piano_a4,
            "A#4" to R.raw.piano_as4,
            "B4" to R.raw.piano_b4,

            // Oitava 5
            "C5" to R.raw.piano_c5,
            "C#5" to R.raw.piano_cs5,
            "D5" to R.raw.piano_d5,
            "D#5" to R.raw.piano_ds5,
            "E5" to R.raw.piano_e5,
            "F5" to R.raw.piano_f5,
            "F#5" to R.raw.piano_fs5,
            "G5" to R.raw.piano_g5,
            "G#5" to R.raw.piano_gs5,
            "A5" to R.raw.piano_a5,
            "A#5" to R.raw.piano_as5,
            "B5" to R.raw.piano_b5
        )

        // Carregar cada som no SoundPool
        for ((note, resourceId) in noteResourceMap) {
            try {
                val soundId = soundPool.load(context, resourceId, 1)
                soundMap[note] = soundId
                Log.d(TAG, "Carregado som para nota: $note")
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao carregar som para nota $note", e)
            }
        }
        */
    }

    fun playNote(note: String, velocity: Int) {
        if (!soundMap.containsKey(note)) {
            Log.d(TAG, "Nota $note não está disponível")
            return
        }

        // Mapear velocidade (0-127) para volume SoundPool (0.0-1.0)
        val volume = velocity / 127f

        // CÓDIGO TEMPORÁRIO - REMOVEREMOS esta linha quando tivermos os arquivos de áudio
        Log.d(TAG, "Simulando tocar nota: $note com volume: $volume")

        // CÓDIGO REAL - DESCOMENTAR
        // soundPool.play(soundMap[note]!!, volume, volume, 1, 0, 1.0f)
    }

    fun release() {
        soundPool.release()
        Log.d(TAG, "SoundPool liberado")
    }

    // CÓDIGO TEMPORÁRIO - REMOVEREMOS
    fun checkResourcesAvailable(resourceId: Int): Boolean {
        return try {
            context.resources.getResourceName(resourceId)
            true
        } catch (e: Exception) {
            false
        }
    }
}