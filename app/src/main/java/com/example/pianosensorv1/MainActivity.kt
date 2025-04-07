package com.example.pianosensorv1

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.MqttException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken

class MainActivity : AppCompatActivity() {

    private lateinit var mqttClient: MqttClient
    private val mqttBroker = "tcp://your_mqtt_broker_address:1883"
    private val topic = "piano/keys"
    private lateinit var pianoKeys: Array<Button>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

// Initialize buttons for 32 piano keys
        pianoKeys = arrayOf(
            //(R.id.key_C3), findViewById(R.id.key_D3), findViewById(R.id.key_E3),
            //findViewById(R.id.key_F3), findViewById(R.id.key_G3), findViewById(R.id.key_A3),
            //findViewById(R.id.key_B3), findViewById(R.id.key_C4), findViewById(R.id.key_D4),
            findViewById(R.id.key_E4), findViewById(R.id.key_F4), findViewById(R.id.key_G4),
            findViewById(R.id.key_A4), findViewById(R.id.key_B4), findViewById(R.id.key_C5),
            findViewById(R.id.key_D5), findViewById(R.id.key_E5), findViewById(R.id.key_F5),
            findViewById(R.id.key_G5), findViewById(R.id.key_A5), findViewById(R.id.key_B5),
            //findViewById(R.id.key_Cs3), findViewById(R.id.key_Ds3), findViewById(R.id.key_Fs3),
            //findViewById(R.id.key_Gs3), findViewById(R.id.key_As3), findViewById(R.id.key_Cs4),
            findViewById(R.id.key_Ds4), findViewById(R.id.key_Fs4), findViewById(R.id.key_Gs4),
            findViewById(R.id.key_As4), findViewById(R.id.key_Cs5), findViewById(R.id.key_Ds5),
            findViewById(R.id.key_Fs5), findViewById(R.id.key_Gs5), findViewById(R.id.key_As5)
        )


        // Connect to MQTT broker
        connectToMqttBroker()

        // Set up the subscription
        setupMqttSubscription()

        // Example of the piano key press functionality (just toggling color for visual effect)
        pianoKeys.forEachIndexed { index, button ->
            button.setOnClickListener {
                // Handle piano key press logic
                toggleKeyPress(index)
            }
        }
    }

    private fun connectToMqttBroker() {
        try {
            mqttClient = MqttClient(mqttBroker, MqttClient.generateClientId(), null)
            mqttClient.connect()
            mqttClient.setCallback(object : MqttCallback {
                override fun connectionLost(cause: Throwable?) {
                    // Handle connection loss
                    println("Connection lost: ${cause?.message}")
                }

                override fun messageArrived(topic: String?, message: MqttMessage?) {
                    // Handle incoming MQTT message
                    val data = message?.payload?.toString(Charsets.UTF_8) ?: return
                    val (angle, force) = data.split(",").map { it.toFloat() }
                    updatePianoKeys(angle, force)
                }

                override fun deliveryComplete(token: IMqttDeliveryToken?) {
                    // Handle message delivery completion
                    println("Delivery complete: $token")
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    private fun setupMqttSubscription() {
        try {
            mqttClient.subscribe(topic)
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    private fun updatePianoKeys(angle: Float, force: Float) {
        lifecycleScope.launch(Dispatchers.Main) {
            val index = mapAngleToKey(angle)
            // Visual representation of pressing a key
            pianoKeys.forEachIndexed { i, button ->
                if (i == index) {
                    button.setBackgroundColor(resources.getColor(R.color.pressedKey))
                } else {
                    button.setBackgroundColor(resources.getColor(R.color.defaultKey))
                }
            }
        }
    }

    private fun mapAngleToKey(angle: Float): Int {
        return ((angle + 90) / 180 * 31).toInt()
    }

    private fun toggleKeyPress(index: Int) {
        // Toggle color for key press (for visual feedback)
        val button = pianoKeys[index]
        button.setBackgroundColor(
            if (button.currentTextColor == resources.getColor(R.color.pressedKey))
                resources.getColor(R.color.defaultKey)
            else
                resources.getColor(R.color.pressedKey)
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            mqttClient.disconnect()
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }
}
