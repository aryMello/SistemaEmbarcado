#include <Wire.h>
#include <MozziGuts.h>
#include <Oscil.h>
#include <tables/sin2048_int8.h>
#include <WiFi.h>
#include <PubSubClient.h>
#include <Adafruit_MPU6050.h>
#include <Adafruit_Sensor.h>

// ========== Sensor Configuration ==========
const int flexPins[5] = {36, 39, 34, 35, 32}; // Thumb to Pinky (ADC1)
const int forceSensor = 33; // Thumb pressure sensor

int flexValues[5];  // Raw flex sensor readings
int forceValue = 0;

// ========== IMU ==========
Adafruit_MPU6050 mpu;
float accelX = 0, accelY = 0, accelZ = 0;

// ========== Mozzi Audio ==========
Oscil<SIN2048_NUM_CELLS, AUDIO_RATE> osc(SIN2048_DATA);
int amplitude = 255;  // Volume

// MIDI notes (C3–G5 with black keys)
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

const char* mqttServer = "172.171.228.166";
const int mqttPort = 1883;
const char* mqttUser = "admin";
const char* mqttPassword = "admin123";
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
  Wire.begin();
  startMozzi(128);  // Mozzi control rate

  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    delay(1000);
    Serial.println("Connecting to WiFi...");
  }
  Serial.println("Connected to WiFi");

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

// === Move IMU read to non-interrupt loop ===
unsigned long lastIMURead = 0;
void updateIMU() {
  if (millis() - lastIMURead > 50) { // 20Hz
    lastIMURead = millis();
    sensors_event_t a, g, temp;
    mpu.getEvent(&a, &g, &temp);
    accelX = a.acceleration.x;
    accelY = a.acceleration.y;
    accelZ = a.acceleration.z;
  }
}

void updateControl() {
  // === Read force sensor ===
  forceValue = analogRead(forceSensor);
  amplitude = map(forceValue, 0, 4095, 0, 255);

  // === Read flex sensors ===
  for (int i = 0; i < 5; i++) {
    flexValues[i] = analogRead(flexPins[i]);
  }

  // === Calculate Hand Angles using IMU ===
  float pitch = atan2(accelY, accelZ) * 180.0 / PI;
  float roll = atan2(accelX, accelZ) * 180.0 / PI;

  // === Map thumb flex to MIDI note ===
  int index = map(flexValues[0], 600, 2000, 0, 33);  // Thumb flex to MIDI note index
  index = constrain(index, 0, 33);
  int midiNote = pianoKeys[index];

  // === Modify MIDI note with hand angle adjustments ===
  if (pitch > 10) {
    midiNote += 1;
  } else if (pitch < -10) {
    midiNote -= 1;
  }
  midiNote = constrain(midiNote, 0, 127);  // MIDI range check

  float freq = pow(2.0, (midiNote - 69) / 12.0) * 440.0;

  // === Only update note if changed ===
  static int prevNote = -1;
  if (midiNote != prevNote) {
    osc.setFreq(freq);
    prevNote = midiNote;
    Serial.print("Note changed: ");
    Serial.print(midiNote);
    Serial.print(" | Freq: ");
    Serial.println(freq);
  }

  // === MQTT Connection Check ===
  if (!mqttClient.connected()) {
    reconnectMQTT();
  }
  mqttClient.loop();

  // === Prepare payloads ===
  int scaledForce = constrain(map(forceValue, 0, 4095, 0, 100), 0, 100);
  int scaledFlex[5];
  for (int i = 0; i < 5; i++) {
    scaledFlex[i] = constrain(map(flexValues[i], 600, 2000, 0, 100), 0, 100);
  }

  // --- First payload: just MIDI note number ---
  char midiPayload[32];
  snprintf(midiPayload, sizeof(midiPayload), "midi:%d", midiNote);
  mqttClient.publish(mqttTopic, midiPayload);
  Serial.println(midiPayload);

  // --- Second payload: force, flex, IMU data ---
  char sensorPayload[128];
  snprintf(sensorPayload, sizeof(sensorPayload), "f:%d fx:%d,%d,%d,%d,%d imu:%.2f,%.2f,%.2f",
           scaledForce,
           scaledFlex[0], scaledFlex[1], scaledFlex[2], scaledFlex[3], scaledFlex[4],
           accelX, accelY, accelZ);
  mqttClient.publish(mqttTopic, sensorPayload);
  Serial.println(sensorPayload);

  // === Delay 5 seconds before next update ===
  delay(5000);
}


AudioOutput_t updateAudio() {
  uint8_t sample = osc.next();
  sample = (sample * amplitude) / 255;
  return MonoOutput::from8Bit(sample);
}

void loop() {
  audioHook();  // Mozzi ISR
  updateIMU();  // Safe to call outside interrupt
  updateControl();  // send data
}