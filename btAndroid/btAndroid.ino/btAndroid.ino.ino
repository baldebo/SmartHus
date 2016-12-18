#include <SoftwareSerial.h>
#include <math.h>

SoftwareSerial BT(3, 5);

const byte red = 13;
const byte yellow = 12;
const byte blue = 11;
const byte window = 7;
const byte larm = 6;
int tempPin = 0;

long redTimer;
long yellowTimer;
long blueTimer;

int larmState = 0;
long larmTime = 0;
long larmDelay = 200;
boolean larmOn = false;


int windowState = 0;
long tempTime = 0;
long tempDelay = 1000;
int current = HIGH;
int previous = LOW;

int values[6] = {0, 0, 0, 0, 0, 0};
long valuesTime = 0;
long valuesDelay = 100;


void setup() {
  pinMode(red, OUTPUT);
  pinMode(yellow, OUTPUT);
  pinMode(blue, OUTPUT);

  redTimer = millis();
  yellowTimer = millis();
  blueTimer = millis();

  BT.begin(9600);
}

char buffer;

void loop() {
  if(BT.available()) {
    buffer = (BT.read());
    switch(buffer) {
      case '1' : {
        if(digitalRead(red) == LOW){
          digitalWrite(red, HIGH);
          values[0] = 1;
        }
        else {
          digitalWrite(red, LOW);
          values[0] = 0;
        }
        break;
      }
      case '2' : {
        if(digitalRead(yellow) == LOW) {
          digitalWrite(yellow, HIGH);
          values[1] = 1;
        }
        else {
          digitalWrite(yellow, LOW);
          values[1] = 0;
        }
        break;
      }
      case '3' : {
        if(digitalRead(blue) == LOW) {
          digitalWrite(blue, HIGH);
          values[2] = 1;
        }
        else {
          digitalWrite(blue, LOW);
          values[2] = 0;
        }
        break;
      }
    }
  }
  
  checkWindow();
  checkLarm();
  checkTemp();
  sendAndroidValues();
}

void checkTemp() {

  if(millis() - tempTime > tempDelay) {
  int sensor = analogRead(tempPin);  
  float tempVal = (sensor * 5.0 / 1024.0 - 0.5) * 100;
  round(tempVal);
  int roundTemp = tempVal;
  values[3] = roundTemp;
  tempTime = millis();
  }
    
}

void checkWindow() {
  windowState = digitalRead(window);
  if(windowState == HIGH) {
    values[4] = 1;
  } else {
    values[4] = 0; 
  }
    
}

void checkLarm() {
  larmState = digitalRead(larm);
  if(larmState == HIGH && previous == LOW && millis() - larmTime > larmDelay) {
    if (current == HIGH) { current = LOW; values[5] = 1;}
    else{ current = HIGH; values[5] = 0; }
    larmTime = millis();
  }
  
  
  previous = larmState; 
  
}

void readAndroidValues() {
  
}

void sendAndroidValues() {
  if(millis() - valuesTime > valuesDelay) {
  BT.print('P');
  for(int k = 0; k<7; k++) {
    BT.print(values[k]);
  }
  BT.print('Q');
  BT.println();
  valuesTime = millis();
  }
  
}

