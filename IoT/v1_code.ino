#include <Wire.h>
#include <MPU6050_tockn.h>  // Biblioteca do MPU-6050
#include <MozziGuts.h>
#include <Oscil.h>
#include <tables/sin2048_int8.h>
#include <WiFi.h>
#include <PubSubClient.h>

// Configuração do IMU e Sensor de Força
MPU6050 mpu6050(Wire);
const int forceSensor = A1;

// Configuração de Som Mozzi
Oscil<SIN2048_NUM_CELLS, AUDIO_RATE> osc;

// Mapeamento de teclas do piano (32 teclas: C3 até G5)
const int pianoKeys[32] = {
    48, 50, 52, 53, 55, 57, 59,  // C3 - B3 (Brancas)
    60, 62, 64, 65, 67, 69, 71,  // C4 - B4 (Brancas)
    72, 74, 76, 77, 79, 81, 83,  // C5 - B5 (Brancas)
    49, 51, 54, 56, 58,          // Pretas (C#3, D#3, F#3, G#3, A#3)
    61, 63, 66, 68, 70,          // Pretas (C#4, D#4, F#4, G#4, A#4)
    73, 75, 78, 80, 82           // Pretas (C#5, D#5, F#5, G#5, A#5)
};

// Variáveis para suavização dos dados do IMU
float smoothedAngleX = 0;
const float alpha = 0.1; // Fator de suavização (filtro passa-baixa)

// Configuração de Wi-Fi e MQTT
const char* ssid = "your-SSID";  // Substitua pelo nome da sua rede Wi-Fi
const char* password = "your-PASSWORD";  // Substitua pela senha da sua rede Wi-Fi
const char* mqttServer = "broker.hivemq.com";  // Endereço do broker MQTT
const int mqttPort = 1883;  // Porta do broker MQTT
const char* mqttUser = "your-username";  // Seu nome de usuário MQTT (se necessário)
const char* mqttPassword = "your-password";  // Sua senha MQTT (se necessário)
const char* mqttTopic = "piano/sensor";  // Tópico MQTT para enviar os dados

WiFiClient wifiClient;
PubSubClient mqttClient(wifiClient);

// Função de callback MQTT para reconexão
void callback(char* topic, byte* payload, unsigned int length) {
  // Função de callback para gerenciar as mensagens recebidas (não usada aqui)
}

void setup() {
    Serial.begin(9600);
    Wire.begin();
    mpu6050.begin();
    mpu6050.calcGyroOffsets(true);
    
    startMozzi();

    // Conectar à rede Wi-Fi
    WiFi.begin(ssid, password);
    while (WiFi.status() != WL_CONNECTED) {
        delay(1000);
        Serial.println("Connecting to WiFi...");
    }
    Serial.println("Connected to WiFi");

    // Configurar o cliente MQTT
    mqttClient.setServer(mqttServer, mqttPort);
    mqttClient.setCallback(callback);
}

void reconnectMQTT() {
    // Reconnecta ao MQTT broker
    while (!mqttClient.connected()) {
        if (mqttClient.connect("ESP32Client", mqttUser, mqttPassword)) {
            Serial.println("Connected to MQTT Broker");
        } else {
            delay(5000);
            Serial.println("Retrying MQTT connection...");
        }
    }
}

void updateControl() {
    mpu6050.update();
    float rawAngleX = mpu6050.getAngleX();

    // Suaviza os dados do IMU para evitar saltos de valores
    smoothedAngleX = (alpha * rawAngleX) + ((1 - alpha) * smoothedAngleX);

    // Mapeia o ângulo para uma tecla do piano
    int index = map(smoothedAngleX, -90, 90, 0, 31);
    index = constrain(index, 0, 31);

    // Converte a tecla para frequência
    int midiNote = pianoKeys[index];
    float freq = pow(2.0, (midiNote - 69) / 12.0) * 440.0;
    osc.setFreq(freq);

    // Mapeia o sensor de força para o volume
    int forceValue = analogRead(forceSensor);
    int amplitude = map(forceValue, 0, 1023, 0, 255);
    osc.setAmplitude(amplitude);

    // Publica os dados via MQTT
    if (!mqttClient.connected()) {
        reconnectMQTT();
    }

    mqttClient.loop();
    
    String data = String(smoothedAngleX) + "," + String(forceValue);
    mqttClient.publish(mqttTopic, data.c_str());

    // Exibe os dados no Serial Monitor
    Serial.print("Roll Angle: "); Serial.print(smoothedAngleX);
    Serial.print(" | Piano Key: "); Serial.print(midiNote);
    Serial.print(" (Freq: "); Serial.print(freq); Serial.print(" Hz)");
    Serial.print(" | Force: "); Serial.println(amplitude);
}

AudioOutput_t updateAudio() {
    return MonoOutput::from8Bit(osc.next());
}

void loop() {
    audioHook(); // Mantém o Mozzi ativo
    updateControl(); // Atualiza os controles do IMU e publica os dados via MQTT
}

