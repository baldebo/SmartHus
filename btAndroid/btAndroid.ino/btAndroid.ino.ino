/* Av Emil Baldebo 04/12/2016
Komponenter:
4 LED(Röd,Gul, Blå och Grön
4 Resistorer 220 Ohm
1 Resistor 2K Ohm
1 Resistor 1,7K Ohm
1 Piezosummer
1 Bluetoothmodul HC-06
*/
//Bibliotek så jag kan använda digitala 3 och 5
//istället för TX RX
#include <SoftwareSerial.h>

//Bluetoothmodulen kopplad till TX -> 5 och RX ->3
SoftwareSerial BT(3, 5);

//Ge lamporna en färg.
const byte red = 13;
const byte yellow = 12;
const byte blue = 11;
const byte green = 10;
const byte ljud = 9;

/*Använder mig utav timers här för att undvika att använda
delay() vilket gör att man kan t ex tända och släcka en lampa
medan en annan blinkar*/
const unsigned long blinkInterval = 500;
const unsigned long asynkInterval = 200;

unsigned long yellowTimer;
unsigned long blueTimer;
unsigned long greenTimer;

void setup() {
  //Lampa som ska lysa
  pinMode(red, OUTPUT);
  //Lampa som ska blinka
  pinMode(yellow, OUTPUT);
  //Lampor som ska blinka asynkront
  pinMode(blue, OUTPUT);
  pinMode(green, OUTPUT);
  //Piezosummer för ljud
  pinMode(ljud, OUTPUT);

  //Sätt timers till nuvarande millisekundsvärde.
  yellowTimer = millis();
  blueTimer   = millis();
  greenTimer  = millis();

  //Initiera Bluetooth
  BT.begin(9600);
  BT.println("Bluetooth ansluten!");
}

//Function för att blinka gula lampan.
void yellowBlink () {
  if (digitalRead (yellow) == LOW)
      digitalWrite (yellow, HIGH);
   else
      digitalWrite (yellow, LOW);

  // Komma ihåg när vi startade
  yellowTimer = millis ();  
  }

//Function för att blinka blå lampan.
void blueBlink () {
  if (digitalRead (blue) == LOW)
      digitalWrite (blue, HIGH);
   else
      digitalWrite (blue, LOW);

  // Komma ihåg när vi startade
  blueTimer = millis ();  
  }

//Function för att blinka gröna lampan.
void greenBlink () {
  if (digitalRead (green) == LOW)
      digitalWrite (green, HIGH);
   else
      digitalWrite (green, LOW);

  // Komma ihåg när vi startade
  greenTimer = millis ();  
  }
  
//Kommandot som skickas från Android sparas i command.
char buffer;

//Booleans för att släcka/tända och ljud på/av
boolean blinkaGul = false;
boolean blinkaBla = false;
boolean blinkaGron = false;

void loop() {

  //Om nuvarande millis() är större eller lika med intervallet uppdateras lampans output.
  if ( (millis() - yellowTimer) >= blinkInterval) {
        if(blinkaGul){
        yellowBlink();}
        }
  if ( (millis() - blueTimer) >= blinkInterval) {
        if(blinkaBla){
        blueBlink();}
        }
  if ( (millis() - greenTimer) >= asynkInterval) {
        if(blinkaGron){
        greenBlink();}
        }

  //Alla olika kommandon som kan skickas till arduinon        
  if (BT.available()){
   buffer = (BT.read());

      //Tänd Röd
      if(buffer == '1') {
        digitalWrite(red, HIGH);
      }
      //Släck Röd
      if(buffer == '2') {
        digitalWrite(red, LOW);
      }
      //Blinka Gul
      if(buffer == '3') {
        blinkaGul = true;
      }
      //Släck Gul
      if(buffer == '4') {
        blinkaGul = false;
        digitalWrite(yellow, LOW);
      }
      // Blinka Blå och Grön
      if(buffer == '5') {
        blinkaBla = true;
        blinkaGron = true;        
      }
      //Släck Blå och Grön
      if(buffer == '6') {
        blinkaBla = false;
        blinkaGron = false;
        digitalWrite(blue, LOW);
        digitalWrite(green, LOW);
      }
      //Starta ljud
      if(buffer == '7') {
        //Spelar ljud med frekvens på 31Hz
        tone(ljud, 31);
      }
      //Stäng av ljud
      if(buffer == '8') {
        noTone(ljud);
      }

  }

}
