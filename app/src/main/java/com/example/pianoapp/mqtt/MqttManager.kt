package com.yourpackage.pianoapp.mqtt

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import java.util.*

class MqttManager(
    private val brokerUrl: String,
    private val clientId: String,
    private val onConnectionChanged: (Boolean) -> Unit,
    private val onMessageReceived: (String) -> Unit
) {
    private val TAG = "MqttManager"
    private var mqttClient: MqttClient? = null
    private val topic = "piano/processado"

    suspend fun connect() = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Conectando ao broker MQTT: $brokerUrl")

            val persistence = MemoryPersistence()
            mqttClient = MqttClient(brokerUrl, clientId, persistence)

            val connOpts = MqttConnectOptions().apply {
                isCleanSession = true
                connectionTimeout = 60
                keepAliveInterval = 60
            }

            mqttClient?.setCallback(object : MqttCallback {
                override fun connectionLost(cause: Throwable?) {
                    Log.e(TAG, "Conexão MQTT perdida", cause)
                    onConnectionChanged(false)
                    reconnect()
                }

                override fun messageArrived(topic: String?, message: MqttMessage?) {
                    message?.let {
                        val payload = String(it.payload)
                        Log.d(TAG, "Mensagem recebida: $payload")
                        onMessageReceived(payload)
                    }
                }

                override fun deliveryComplete(token: IMqttDeliveryToken?) {
                }
            })

            mqttClient?.connect(connOpts)

            mqttClient?.subscribe(topic, 0)

            Log.d(TAG, "Conectado ao broker MQTT e inscrito no tópico: $topic")
            onConnectionChanged(true)

        } catch (e: MqttException) {
            Log.e(TAG, "Erro ao conectar ao broker MQTT", e)
            onConnectionChanged(false)
        }
    }

    private fun reconnect() {
        try {
            mqttClient?.connect()
            mqttClient?.subscribe(topic, 0)
            onConnectionChanged(true)
        } catch (e: MqttException) {
            Log.e(TAG, "Erro ao reconectar", e)
            onConnectionChanged(false)
        }
    }

    fun publish(topic: String, message: String) {
        try {
            val mqttMessage = MqttMessage(message.toByteArray())
            mqttMessage.qos = 0
            mqttClient?.publish(topic, mqttMessage)
        } catch (e: MqttException) {
            Log.e(TAG, "Erro ao publicar mensagem", e)
        }
    }

    fun disconnect() {
        try {
            mqttClient?.disconnect()
            Log.d(TAG, "Desconectado do broker MQTT")
            onConnectionChanged(false)
        } catch (e: MqttException) {
            Log.e(TAG, "Erro ao desconectar do broker MQTT", e)
        }
    }
}