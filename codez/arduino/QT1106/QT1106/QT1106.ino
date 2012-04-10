#include <QT1106.h>

// TEENSY
int CHNG = 9;
int DRDY = 10;
int LED = 11;

// ARDUINO
/*int CHNG = 8;
int DRDY = 9;
// no LED use.. it makes the pins go crayzeee
int LED = 0;*/

int SLIDER = 1;

QT1106 QT(CHNG,DRDY,SLIDER);

byte lastKeys = 0;
byte lastSlide = 0;
byte newRes;
boolean slideOff = true;

void setup()
{
  Serial.begin(9600);
  
  QT.Setup();		// go establish default settings
  QT.DI(0x01);          // set it to be more noise-tolerant
  QT.RES(0x07);         // set resolution to 8 bytes (0...255)
  QT.AKS(1);            // turn on adjacent key suppression (only one key at a time)
  
  //turn the light on so we know when it's ready
  pinMode(LED, OUTPUT);
  digitalWrite(LED, HIGH);
}

void loop()
{
  QT.Check();
  if (QT.keyChange == true && QT.Keys != lastKeys){
      for(int i=0; i<7; i++) {
         if((QT.Keys >> i & 1) && !(lastKeys >> i & 1)){
           Serial.print("K:");
           Serial.print(i);
           Serial.print(" Dx");
         }
         if(!(QT.Keys >> i & 1) && (lastKeys >> i & 1)){
           Serial.print("K:");
           Serial.print(i);
           Serial.print(" Ux");
         }
      }

      lastKeys = QT.Keys;
    }
 
  if (QT.slideOn == true) {
    if (QT.slidePos != lastSlide) {
      Serial.print("S:");
      Serial.print(QT.slidePos);
      Serial.print(" Dx");
      lastSlide = QT.slidePos;
      slideOff = false;
    }
  } else {
    if (slideOff == false) {
      Serial.print("S:");
      Serial.print(QT.slidePos);
      Serial.print(" Ux");
      slideOff = true;
    }
  }
}

int pwr(int m, int n) {
  if (n == 1) { return m; }
  return m * pwr(m, n-1); 
}

void printNDigits(int toPrint, int n) {
  n = n-1;
  for (n; n>0; n--) {
    Serial.print(toPrint/pwr(10,n));
    toPrint = toPrint % pwr(10,n);
  }
  Serial.print(toPrint);
}
