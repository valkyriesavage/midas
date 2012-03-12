#include <CapSense.h>

/*
 * CapitiveSense Library Demo Sketch
 * Paul Badger 2008
 * Uses a high value resistor e.g. 10M between send pin and receive pin
 * Resistor effects sensitivity, experiment with values, 50K - 50M. Larger resistor values yield larger sensor values.
 * Receive pin is the sensor pin - try different amounts of foil/metal on this pin
 */


CapSense   cs_4_2 = CapSense(4,2);
CapSense   cs_4_6 = CapSense(4,6);
long baseline;

void setup()                    
{
  digitalWrite(0, HIGH); //We need to set it HIGH immediately on boot for our reset to work
  pinMode(0,OUTPUT); 

  // turning off auto-calibrate...
  cs_4_2.set_CS_AutocaL_Millis(0xFFFFFFFF);
  cs_4_6.set_CS_AutocaL_Millis(0xFFFFFFFF);

  Serial.begin(57600);

  long start = millis();
  long total1 =  cs_4_2.capSense(50);
  long total2 =  cs_4_6.capSense(50);

  baseline = total1 - total2;       // print difference of totals to see how constant it is

  received = 0;
  buffer[received] = '\0';
}

void loop()                    
{
  long start = millis();
  long total1 =  cs_4_2.capSense(30);
  long total2 =  cs_4_6.capSense(30) - baseline;  // figure in the difference in the two sensors

  Serial.print(millis() - start);        // check on performance in milliseconds
  Serial.print("\t");                    // tab character for debug windown spacing

  Serial.print(total1);                  // print sensor output 1
  Serial.print("\t");
  Serial.print(total2);                  // print sensor output 2
  Serial.print("\t");
  Serial.println(total1 - total2);       // print difference of totals to see how constant it is

  delay(10);                             // arbitrary delay to limit data to serial port

  // now let's check to see if we need to reset the Arduino
  // the java code will send us a 6 if we do
  if (Serial.available())
  {
    buffer[received++] = Serial.read();
    buffer[received] = '\0';
    if (received >= (sizeof(buffer)-1))
    {
      int instruction = atoi(buffer);
      if(instruction == 6) {
        // trigger the reset
        digitalWrite(1, LOW);
      }
      received = 0;
    }
  }
}

