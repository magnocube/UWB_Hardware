#include <ESP8266WiFi.h>
#include <ArduinoJson.h>

#define ssid "somewifinetworkname"
#define pass "thepasswordofthatnetwork"

const char* host = "77.172.10.240";
const int poort = 8378;

void setup() {
  Serial.begin(115200);
  WiFi.begin(ssid, pass);

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

}

void loop() {
  delay(500);
  String lines[]Serial.readString();
  
  
  client.write();
  
  WiFiClient client;
  if (!client.connect(host, poort)) {
    Serial.println("connection failed");
    return;
  }
  
  unsigned long timeout = millis();
  while (client.available() == 0) {
    if (millis() - timeout > 5000) {
      Serial.println(">>> Client Timeout !");
      client.stop();
      return;
    }
  }
}
