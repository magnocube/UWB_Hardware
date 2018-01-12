/*
   Copyright (c) 2015 by Thomas Trojer <thomas@trojer.net>
   Decawave DW1000 library for arduino.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

   @file RangingTag.ino
   Use this to test two-way ranging functionality with two DW1000. This is
   the tag component's code which polls for range computation. Addressing and
   frame filtering is currently done in a custom way, as no MAC features are
   implemented yet.

   Complements the "RangingAnchor" example sketch.

   @todo
    - use enum instead of define
    - move strings to flash (less RAM consumption)
*/

#include <SPI.h>
#include <DW1000.h>

// connection pins
const uint8_t PIN_RST = 9; // reset pin
const uint8_t PIN_IRQ = 2; // irq pin
const uint8_t PIN_SS = SS; // spi select pin

// messages used in the ranging protocol
#define POLL 0
#define POLL_ACK 1
#define RANGE 2
#define RANGE_REPORT 3

#define RANGE_FAILED 6

String messageName[] = {"poll", "poll_ack", "range", "range_rapport", "blink", "ranging_init", "error"};

// message flow state
volatile byte expectedMsgId = POLL_ACK;
// message sent/received state
volatile boolean sentAck = false;
volatile boolean receivedAck = false;
// timestamps to remember
DW1000Time timePollSent;
DW1000Time timePollAckReceived;
DW1000Time timeRangeSent;
// data buffer
#define LEN_DATA 32
byte data[LEN_DATA];

// reply times (same on both sides for symm. ranging)
uint16_t replyDelayTimeUS = 3000;

boolean rangeDone = false;
byte addresses[2][2] = {{0xFF, 0xF0}, {0xFF, 0xF1}};
byte destinationAdress = 0;
float distances[2];
void setup() {
  // DEBUG monitoring
  Serial.begin(115200);
  Serial.println(F("### DW1000-arduino-ranging-tag ###"));
  // initialize the driver
  DW1000.begin(PIN_IRQ, PIN_RST);
  DW1000.select(PIN_SS);
  Serial.println("DW1000 initialized ...");
  // general configuration
  DW1000.newConfiguration();
  DW1000.setDefaults();
  DW1000.setDeviceAddress(2);
  DW1000.setNetworkId(10);
  DW1000.enableMode(DW1000.MODE_LONGDATA_RANGE_ACCURACY);
  DW1000.commitConfiguration();
  Serial.println(F("Committed configuration ..."));
  // DEBUG chip info and registers pretty printed
  char msg[128];
  DW1000.getPrintableDeviceIdentifier(msg);
  Serial.print("Device ID: "); Serial.println(msg);
  DW1000.getPrintableExtendedUniqueIdentifier(msg);
  Serial.print("Unique ID: "); Serial.println(msg);
  DW1000.getPrintableNetworkIdAndShortAddress(msg);
  Serial.print("Network ID & Device Address: "); Serial.println(msg);
  DW1000.getPrintableDeviceMode(msg);
  Serial.print("Device mode: "); Serial.println(msg);
  // attach callback for (successfully) sent and received messages
  DW1000.attachSentHandler(handleSent);
  DW1000.attachReceivedHandler(handleReceived);
  // anchor starts by transmitting a POLL message
  receiver();
  transmitPoll();
}

void handleSent() {
  // status change on sent success
  sentAck = true;
}

void handleReceived() {
  // status change on received success
  receivedAck = true;
}

void transmitPoll() {
  clearData();
  DW1000.newTransmit();
  DW1000.setDefaults();
  if (destinationAdress == 0)
  {
    memcpy(data + 5, &distances[1], 4);
  }
  else
  {
    memcpy(data + 5, &distances[0], 4);
  }
  data[0] = addresses[destinationAdress][0];
  data[1] = addresses[destinationAdress][1];
  data[4] = POLL;


  DW1000.setData(data, LEN_DATA);
  DW1000.startTransmit();
}

void transmitRange() {
  clearData();
  DW1000.newTransmit();
  DW1000.setDefaults();
  data[0] = addresses[destinationAdress][0];
  data[1] = addresses[destinationAdress][1];
  data[4] = RANGE;
  // delay sending the message and remember expected future sent timestamp
  DW1000Time deltaTime = DW1000Time(replyDelayTimeUS, DW1000Time::MICROSECONDS);
  timeRangeSent = DW1000.setDelay(deltaTime);
  timePollSent.getTimestamp(data + 5);
  timePollAckReceived.getTimestamp(data + 10);
  timeRangeSent.getTimestamp(data + 15);
  DW1000.setData(data, LEN_DATA);
  DW1000.startTransmit();
  //Serial.print("Expect RANGE to be sent @ "); Serial.println(timeRangeSent.getAsFloat());
}

void receiver() {
  DW1000.newReceive();
  DW1000.setDefaults();
  // so we don't need to restart the receiver manually
  DW1000.receivePermanently(true);
  DW1000.startReceive();
}
long lastTime = 0;

void pollNext()
{
  destinationAdress++;
  if (destinationAdress == 2)
  {
    destinationAdress = 0;
  }
  transmitPoll();
  lastTime = millis();
  rangeDone = false;
}
void loop() {
  if (!sentAck && !receivedAck) {
    // check if inactive
    if (millis() - lastTime > 250) {
      pollNext();
    }
    if (rangeDone)
    {
      delay(500);
      pollNext();
    }

  }
  // continue on any success confirmation
  if (sentAck) {
    sentAck = false;
    byte msgId = data[4];
    if (msgId == POLL) {
      DW1000.getTransmitTimestamp(timePollSent);
      //Serial.print("Sent POLL @ "); Serial.println(timePollSent.getAsFloat());
    } else if (msgId == RANGE) {
      DW1000.getTransmitTimestamp(timeRangeSent);
    }
    clearData();
  }
  if (receivedAck) {
    clearData();
    receivedAck = false;
    // get message and parse
    DW1000.getData(data, LEN_DATA);
    byte msgId = data[4];
    if (msgId != expectedMsgId) {
      // unexpected message, start over again
      //Serial.print("Received wrong message # "); Serial.println(msgId);
      expectedMsgId = POLL_ACK;
      transmitPoll();
      return;
    }
    if (msgId == POLL_ACK) {
      DW1000.getReceiveTimestamp(timePollAckReceived);
      expectedMsgId = RANGE_REPORT;
      transmitRange();
    } else if (msgId == RANGE_REPORT) {
      expectedMsgId = POLL_ACK;
      float curRange;
      memcpy(&curRange, data + 5, 4);
      distances[destinationAdress] = curRange;
      Serial.print("   ");
      Serial.println(curRange);
      Serial.print("\t RX power: "); Serial.print(DW1000.getReceivePower()); Serial.print(" dBm");
      rangeDone = true;
    } else if (msgId == RANGE_FAILED) {
      expectedMsgId = POLL_ACK;
      transmitPoll();
      pollNext();
    }

  }
}
void visualizeDatas(byte datas[]) {
  /* char string[60];
    sprintf(string, "%02X:%02X:%02X:%02X:%02X:%02X:%02X:%02X:%02X:%02X:%02X:%02X:%02X:%02X:%02X:%02X",
           datas[0], datas[1], datas[2], datas[3], datas[4], datas[5], datas[6], datas[7], datas[8], datas[9], datas[10], datas[11], datas[12], datas[13], datas[14], datas[15]);
    Serial.println(string);*/
  for (int i = 0; i < LEN_DATA; i++)
  {
    Serial.print(datas[i], HEX);
  }
  Serial.println();
}



void clearData()
{
  for (int i = 0; i < LEN_DATA; i++)
  {
    data[i] = 0;
  }
}

