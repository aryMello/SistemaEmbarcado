#include <Wire.h>
#include <MozziGuts.h>
#include <Oscil.h>
#include <tables/sin2048_int8.h>
#include <WiFi.h>
#include <PubSubClient.h>
#include <Adafruit_MPU6050.h>
#include <Adafruit_Sensor.h>

// ========== Sensor Configuration ==========
// ADC1 pins (safe with Wi-Fi)
const int flexPins[5] = {36, 39, 34, 35, 32}; // Thumb to Pinky
const int forceSensor = 33; // Thumb pressure sensor

int flexValues[5];  // Stores flex sensor readings

// ========== IMU ==========
Adafruit_MPU6050 mpu;
float accelX = 0, accelY = 0, accelZ = 0;

// ========== Mozzi Audio ==========
Oscil<SIN2048_NUM_CELLS, AUDIO_RATE> osc(SIN2048_DATA);
int amplitude = 255;  // Manual amplitude control

// Piano MIDI note map (C3–G5 + black keys)
const int pianoKeys[34] = {
  48, 50, 52, 53, 55, 57, 59,
  60, 62, 64, 65, 67, 69, 71,
  72, 74, 76, 77, 79, 81, 83,
  49, 51, 54, 56, 58,
  61, 63, 66, 68, 70,
  73, 75
};

// ========== WiFi & MQTT ==========
const char* ssid = "Mobile_Mello";
const char* password = "#Bobesponja$02";

const char* mqttServer = "192.168.197.7"; // Use actual IP of your MQTT broker
const int mqttPort = 1883;
const char* mqttUser = "mello";     
const char* mqttPassword = "Bobesponja02";
const char* mqttTopic = "piano/sensor";

WiFiClient wifiClient;
PubSubClient mqttClient(wifiClient);

void callback(char* topic, byte* payload, unsigned int length) {
  // Not used
}

void reconnectMQTT() {
  while (!mqttClient.connected()) {
    Serial.print("Connecting to MQTT...");
    if (mqttClient.connect("ESP32Client", mqttUser, mqttPassword)) {
      Serial.println("connected");
    } else {
      Serial.print("failed, rc=");
      Serial.print(mqttClient.state());
      Serial.println(" try again in 5 seconds");
      delay(5000);
    }
  }
}

void setup() {
  Serial.begin(115200);

  Wire.begin(); // Needed if you use any I2C sensor later

  startMozzi();

  // Connect Wi-Fi
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    delay(1000);
    Serial.println("Connecting to WiFi...");
  }
  Serial.println("Connected to WiFi");

  // Connect MQTT
  mqttClient.setServer(mqttServer, mqttPort);
  mqttClient.setCallback(callback);

  if (!mpu.begin()) {
      Serial.println("Failed to find MPU6050 chip");
      while (1) delay(10);
    }

  mpu.setAccelerometerRange(MPU6050_RANGE_8_G);
  mpu.setGyroRange(MPU6050_RANGE_500_DEG);
  mpu.setFilterBandwidth(MPU6050_BAND_21_HZ);

  Serial.println("MPU6050 ready");
}

void updateControl() {
  // Read pressure sensor
  int forceValue = analogRead(forceSensor);
  amplitude = map(forceValue, 0, 4095, 0, 255);  // update global amplitude

  // Read flex sensors
  for (int i = 0; i < 5; i++) {
    flexValues[i] = analogRead(flexPins[i]);
  }

  // === Read IMU ===
  sensors_event_t a, g, temp;
  mpu.getEvent(&a, &g, &temp);
  accelX = a.acceleration.x;
  accelY = a.acceleration.y;
  accelZ = a.acceleration.z;

  // Use flex sensor 0 to pick MIDI note
  int index = map(flexValues[0], 200, 3000, 0, 33); // tune range as needed
  index = constrain(index, 0, 33);
  int midiNote = pianoKeys[index];
  float freq = pow(2.0, (midiNote - 69) / 12.0) * 440.0;
  osc.setFreq(freq);

  // === Only update note if it changed ===
  static int prevNote = -1;
  if (midiNote != prevNote) {
    osc.setFreq(freq);
    prevNote = midiNote;
    Serial.print("Note changed: ");
    Serial.print(midiNote);
    Serial.print(" | Freq: ");
    Serial.println(freq);
  }

  // === Create CSV payload ===
  int scaledForce = map(forceValue, 0, 4095, 0, 100);
  scaledForce = constrain(scaledForce, 0, 100);

  String payload = String(scaledForce);
  for (int i = 0; i < 5; i++) {
    int scaledFlex = map(flexValues[i], 600, 2000, 0, 100);
    scaledFlex = constrain(scaledFlex, 0, 100);
    payload += "," + String(scaledFlex);
  }
  payload += "," + String(accelX, 2);
  payload += "," + String(accelY, 2);
  payload += "," + String(accelZ, 2);

  mqttClient.publish(mqttTopic, payload.c_str());

  // Debug
  Serial.println(payload);
}

AudioOutput_t updateAudio() {
  uint8_t sample = osc.next();
  sample = (sample * amplitude) / 255;
  return MonoOutput::from8Bit(sample);
}

void loop() {
  audioHook();  // Handles Mozzi engine and calls updateControl()
}
