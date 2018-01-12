
#include <ESP8266WiFi.h>
#include <SoftwareSerial.h>

#define ssid "ufinder"
#define pass "u-finder"

const char* host = "77.172.10.240";
const int poort = 8378;

WiFiClient client;
boolean started=false;
String stringIn="";
void setup() {
  Serial.begin(115200);
  WiFi.begin(ssid, pass);

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("connected");
  
}
void sendToServer(){
  Serial.println(stringIn);
  
  client.print(stringIn);

  if(!client.connect(host, poort)){
    Serial.println("connection failed");
  }
 
}

void loop() {
  byte byteIn = Serial.read();
  if(byteIn!=255)
  {
    char charIn=char(byteIn);
    if(charIn=='{')
    {
      stringIn="";
      started=true;
    }
    if(started)
    {
      stringIn+=charIn;
    }
    if(charIn=='}')
    {
      started=false;
      Serial.println(stringIn);
      sendToServer();
      
    }
    
    
  }
}



