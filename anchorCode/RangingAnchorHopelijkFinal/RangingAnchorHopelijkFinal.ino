#include <SPI.h>
#include <DW1000.h>

// connection pins
const uint8_t PIN_RST = 9; // reset pin
const uint8_t PIN_IRQ = 2; // irq pin
const uint8_t PIN_SS = SS; // spi select pin

// messages used in the ranging protocol
// TODO replace by enum
#define POLL 0
#define POLL_ACK 1
#define RANGE 2
#define RANGE_REPORT 3

#define RANGE_FAILED 6

float otherDistance;
String messageName[] = {"poll", "poll_ack", "range", "range_rapport", "blink", "ranging_init", "error"};
byte address[2] = {0xFF, 0xF1};
// message flow state
volatile byte expectedMsgId = POLL;
// message sent/received state
volatile boolean sentAck = false;
volatile boolean receivedAck = false;
// protocol error state
boolean protocolFailed = false;
// timestamps to remember
DW1000Time timePollSent;
DW1000Time timePollReceived;
DW1000Time timePollAckSent;
DW1000Time timePollAckReceived;
DW1000Time timeRangeSent;
DW1000Time timeRangeReceived;
// last computed range/time
DW1000Time timeComputedRange;
// data buffer
#define LEN_DATA 32
byte data[LEN_DATA];
// watchdog and reset period
uint32_t lastActivity;
uint32_t resetPeriod = 250;
// reply times (same on both sides for symm. ranging)
uint16_t replyDelayTimeUS = 3000;
// ranging counter (per second)
uint16_t successRangingCount = 0;
uint32_t rangingCountPeriod = 0;
float samplingRate = 0;
boolean debug = true;
void setup() {
  // DEBUG monitoring
  
    Serial.begin(115200);
  
  
  
  // initialize the driver
  DW1000.begin(PIN_IRQ, PIN_RST);
  DW1000.select(PIN_SS);
  // general configuration
  DW1000.newConfiguration();
  DW1000.setDefaults();
  DW1000.setDeviceAddress(1);
  DW1000.setNetworkId(10);
  DW1000.enableMode(DW1000.MODE_LONGDATA_RANGE_ACCURACY);
  DW1000.commitConfiguration();
  if(debug)
  {
    Serial.println(F("### DW1000-arduino-ranging-anchor ###"));
    delay(1000);
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
  }
  // attach callback for (successfully) sent and received messages
  DW1000.attachSentHandler(handleSent);
  DW1000.attachReceivedHandler(handleReceived);
  // anchor starts in receiving mode, awaiting a ranging poll message
  receiver();
  noteActivity();
  // for first time ranging frequency computation
  rangingCountPeriod = millis();
 
}

void noteActivity() {
  // update activity timestamp, so that we do not reach "resetPeriod"
  lastActivity = millis();
}

void resetInactive() {
  // anchor listens for POLL
  expectedMsgId = POLL;
  receiver();
  noteActivity();
}

void handleSent() {
  // status change on sent success
  sentAck = true;
}

void handleReceived() {
  // status change on received success
  receivedAck = true;
}

void transmitPollAck() {
  clearData();
  DW1000.newTransmit();
  DW1000.setDefaults();
  data[4] = POLL_ACK;
  // delay the same amount as ranging tag
  DW1000Time deltaTime = DW1000Time(replyDelayTimeUS, DW1000Time::MICROSECONDS);
  DW1000.setDelay(deltaTime);
  DW1000.setData(data, LEN_DATA);
  DW1000.startTransmit();
}

void transmitRangeReport(float curRange) {
  clearData();
  DW1000.newTransmit();
  DW1000.setDefaults();
  data[4] = RANGE_REPORT;
  // write final ranging result
  memcpy(data + 5, &curRange, 4);
  DW1000.setData(data, LEN_DATA);
  DW1000.startTransmit();
}

void transmitRangeFailed() {
  clearData();
  DW1000.newTransmit();
  DW1000.setDefaults();
  data[4] = RANGE_FAILED;
  DW1000.setData(data, LEN_DATA);
  DW1000.startTransmit();
}

void receiver() {
  DW1000.newReceive();
  DW1000.setDefaults();
  // so we don't need to restart the receiver manually
  DW1000.receivePermanently(true);
  DW1000.startReceive();
}

/*
   RANGING ALGORITHMS
   ------------------
   Either of the below functions can be used for range computation (see line "CHOSEN
   RANGING ALGORITHM" in the code).
   - Asymmetric is more computation intense but least error prone
   - Symmetric is less computation intense but more error prone to clock drifts

   The anchors and tags of this reference example use the same reply delay times, hence
   are capable of symmetric ranging (and of asymmetric ranging anyway).
*/

void computeRangeAsymmetric() {
  // asymmetric two-way ranging (more computation intense, less error prone)
  DW1000Time round1 = (timePollAckReceived - timePollSent).wrap();
  DW1000Time reply1 = (timePollAckSent - timePollReceived).wrap();
  DW1000Time round2 = (timeRangeReceived - timePollAckSent).wrap();
  DW1000Time reply2 = (timeRangeSent - timePollAckReceived).wrap();
  DW1000Time tof = (round1 * round2 - reply1 * reply2) / (round1 + round2 + reply1 + reply2);
  // set tof timestamp
  timeComputedRange.setTimestamp(tof);
}

/*
   END RANGING ALGORITHMS
   ----------------------
*/

void loop() {
  int32_t curMillis = millis();
  if (!sentAck && !receivedAck) {
    // check if inactive
    if (curMillis - lastActivity > resetPeriod) {
      resetInactive();
    }
    return;
  }
  // continue on any success confirmation
  if (sentAck) {
    sentAck = false;
    byte msgId = data[4];
    if (debug) {
      Serial.print("->");
      Serial.print(messageName[msgId]);
      Serial.print(" ");
      visualizeDatas(data);
    }
    if (msgId == POLL_ACK) {
      DW1000.getTransmitTimestamp(timePollAckSent);
      noteActivity();
    }
    clearData();
  }
  if (receivedAck) {


    clearData();
    receivedAck = false;
    // get message and parse
    DW1000.getData(data, LEN_DATA);
    byte msgId = data[4];

    if (address[0] != data[0])
    {
      return;
    }
    if (address[1] != data[1])
    {
      return;
    }

    if (debug) {
      Serial.print("<-");
      Serial.print(messageName[msgId]);
      Serial.print(" ");
      visualizeDatas(data);
    }
    if (msgId != expectedMsgId) {
      // unexpected message, start over again (except if already POLL)
      protocolFailed = true;
    }
    if (msgId == POLL) {
      // on POLL we (re-)start, so no protocol failure
      memcpy(&otherDistance, data + 5, 4);
      /*Serial.print("{\"anchor\":\"FFF1\",\"tag\":\"AAA0\",\"distance\":\"");
        Serial.print(otherDistance);
        Serial.println("\"}");*/
      
      protocolFailed = false;
      DW1000.getReceiveTimestamp(timePollReceived);
      expectedMsgId = RANGE;
      transmitPollAck();
      noteActivity();
    }
    else if (msgId == RANGE) {
      DW1000.getReceiveTimestamp(timeRangeReceived);
      expectedMsgId = POLL;
      if (!protocolFailed) {
        timePollSent.setTimestamp(data + 5);
        timePollAckReceived.setTimestamp(data + 10);
        timeRangeSent.setTimestamp(data + 15);
        // (re-)compute range as two-way ranging is done
        computeRangeAsymmetric(); // CHOSEN RANGING ALGORITHM
        //transmitRangeReport(timeComputedRange.getAsMicroSeconds());
        transmitRangeReport(timeComputedRange.getAsMeters());
        float distance = timeComputedRange.getAsMeters();
       /* Serial.print("Range: "); Serial.print(distance); Serial.print(" m");
        Serial.print("  otherrange:"); Serial.print(otherDistance); Serial.print(" m");
        Serial.print("\t RX power: "); Serial.print(DW1000.getReceivePower()); Serial.print(" dBm");
        Serial.print("\t Sampling: "); Serial.print(samplingRate); Serial.println(" Hz");
        //Serial.print("FP power is [dBm]: "); Serial.print(DW1000.getFirstPathPower());
        //Serial.print("RX power is [dBm]: "); Serial.println(DW1000.getReceivePower());
        //Serial.print("Receive quality: "); Serial.println(DW1000.getReceiveQuality());
        */
        Serial.print("{");
        Serial.print("\"anchor\":\"FFF0\",\"tag\":\"AAA0\",\"distance\":\"");
         Serial.print(distance-0.45);
         Serial.print("\",");
          Serial.print("\"anchor2\":\"FFF1\",\"tag2\":\"AAA0\",\"distance2\":\"");
         Serial.print(otherDistance-0.50);
        Serial.println("\" }");
        
        
       

        
        // update sampling rate (each second)
        successRangingCount++;
        if (curMillis - rangingCountPeriod > 1000) {
          samplingRate = (1000.0f * successRangingCount) / (curMillis - rangingCountPeriod);
          rangingCountPeriod = curMillis;
          successRangingCount = 0;
        }
      }
      else {
        transmitRangeFailed();
      }

      noteActivity();
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

