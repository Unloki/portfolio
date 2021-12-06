/*
  DUBOIS Louis
  simple client firebase Realtime Database, Send Data to the Database and listen data change
  using the Firebase-ESP-Client library by mobizt
*/

#include <Arduino.h>
#include <WiFi.h>
#include <FirebaseESP32.h>

//Provide the token generation process info.
#include "addons/TokenHelper.h"
//Provide the RTDB payload printing info and other helper functions.
#include "addons/RTDBHelper.h"

//  network credentials setup
#define WIFI_SSID "xxxxxxxx"
#define WIFI_PASSWORD "xxxxxxxxx"

// Firebase project API Key
#define API_KEY "AIzaSyCbDnqQ91Wb365oABLWZaL_QbYiiw-4e6U"

// RTDB URL
#define DATABASE_URL "https://beer-tap-esp32-default-rtdb.europe-west1.firebasedatabase.app/" 

//Define Firebase Data object
FirebaseData stream;
FirebaseData fbdo;

FirebaseAuth auth;
FirebaseConfig config;

unsigned long sendDataPrevMilisListen = 0;
unsigned long sendDataPrevMilisWriting = 0;
int count = 0;
bool signupOK = false;

void streamCallback(StreamData data)
{
  printResult(data); //print the json data, see addons/RTDBHelper.h
  Serial.println();
}
/* use if timeout  */
void streamTimeoutCallback(bool timeout)
{
  if (timeout)
    Serial.println("stream timed out, resuming...\n");

  if (!stream.httpConnected())
    Serial.printf("error code: %d, reason: %s\n\n", stream.httpCode(), stream.errorReason().c_str());
}

void setup(){
  Serial.begin(115200);
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  Serial.print("Connecting to Wi-Fi");
  while (WiFi.status() != WL_CONNECTED)
  {
    Serial.print(".");
    delay(300);
  }
  Serial.println();
  Serial.print("Connected with IP: ");
  Serial.println(WiFi.localIP());
  Serial.println();

  Serial.printf("Firebase Client v%s\n\n", FIREBASE_CLIENT_VERSION);

  /* Assign the api key (required) */
  config.api_key = API_KEY;

  /* Assign the RTDB URL (required) */
  config.database_url = DATABASE_URL;

    /* Sign up, no user and password use (config firebase) */
  if (Firebase.signUp(&config, &auth, "", "")){
    Serial.println("Sign Up");
    signupOK = true;
  }
  else{
    Serial.printf("%s\n", config.signer.signupError.message.c_str());
  }

  /* Assign the callback function for the long running token generation task */
  config.token_status_callback = tokenStatusCallback; //see addons/TokenHelper.h

  Firebase.begin(&config, &auth);

  Firebase.reconnectWiFi(true);

  if (!Firebase.beginStream(stream, "/ESP32/order"))
    Serial.printf("sream begin error, %s\n\n", stream.errorReason().c_str());
  // For set handler on data change
  Firebase.setStreamCallback(stream, streamCallback, streamTimeoutCallback);  
  Serial.printf("All Setup");  
  Serial.println();
}
void loop(){
  
  // PART LISTEN CHANGE DATA 
  // Send data for testing the listen data process, for simulating an order from the user
  if (Firebase.ready() && signupOK && (millis() - sendDataPrevMilisListen > 15000 || sendDataPrevMilisListen == 0))
  {
    sendDataPrevMilisListen = millis();
    count++;
    FirebaseJson json;
    json.add("title", "New order by user");
    json.add("data", count++);
    Serial.printf("Insert new json at /ESP32/order/json... %s\n\n", Firebase.setJSON(fbdo, "/ESP32/order/json", json) ? "complete" : fbdo.errorReason().c_str());
  }
  count++;


  // PART Writing DATA
  //wait 1 minutes before update data
  if (Firebase.ready() && signupOK && (millis() - sendDataPrevMilisWriting > 1 * 60000ul || sendDataPrevMilisWriting == 0)){
    sendDataPrevMilisWriting = millis();
    // Write an Int number on the database path ESP32/temp/biere, here for the temperature of the drink
    if (Firebase.setInt(fbdo, "ESP32/temperature/Machine1/beer", 10)){
      Serial.println("Data successful insert");
      Serial.println("PATH: " + fbdo.dataPath());
      Serial.println("TYPE: " + fbdo.dataType());
    }
    else {
      Serial.println("Echec data insert");
      Serial.println("REASON: " + fbdo.errorReason());
    }
    
    // Write an int on the database path ESP32/temp/piece, here for the room temperature
    if (Firebase.setInt(fbdo, "ESP32/temperature/Machine1/room", 20)){
      Serial.println("Data successful insert");
      Serial.println("PATH: " + fbdo.dataPath());
      Serial.println("TYPE: " + fbdo.dataType());
    }
    else {
      Serial.println("Echec data insert");
      Serial.println("REASON: " + fbdo.errorReason());
    }
  }
}
