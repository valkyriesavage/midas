/*

  Derived from the 7 Key Slider/Wheel example code at Rachel's Electronics
  
      PIN CONNECTIONS TO ARDUINO:
  BREAKOUT PIN        ARDUINO PIN
    CHNG                 8
    DRDY                 9
    SS                  10
    MOSI                11
    MISO                12
    SCLK                13
    
*/

#include <QT1106.h>
//QT1106 instanceName(CHNG pin, DRDY pin, 1 for slider/0 for wheel)
QT1106 QT(8,9,1);

byte lastKeys = 0;
byte lastSlide = 0;
byte newRes;
boolean slideOff = true;

void setup()
{
  Serial.begin(9600);
  
  QT.Setup();		// go establish default settings
  QT.DI(0x01);          // set it to be more noise-tolerant
  QT.RES(0x08);         // set resolution to 8 bytes (0...255)
  Serial.println("it's on!");
}

void loop()
{
  QT.Check();
  if (QT.keyChange == true && QT.Keys != lastKeys){
      for(int i=0; i<=7; i++) {
         if((QT.Keys >> i & 1) && !(lastKeys >> i & 1)){
           Serial.print("K:");
           Serial.print(i);
           Serial.println(" D");
         }
         if(!(QT.Keys >> i & 1) && (lastKeys >> i & 1)){
           Serial.print("K:");
           Serial.print(i);
           Serial.println(" U");
         }
      }

      lastKeys = QT.Keys;
    }
 
  if (QT.slideOn == true) {
    if (QT.slidePos != lastSlide) {
      Serial.print("S:");
      Serial.print(QT.slidePos);
      Serial.println(" D");
      lastSlide = QT.slidePos;
      slideOff = false;
    }
  } else {
    if (slideOff == false) {
      Serial.print("S:");
      Serial.print(QT.slidePos);
      Serial.println(" U");
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
