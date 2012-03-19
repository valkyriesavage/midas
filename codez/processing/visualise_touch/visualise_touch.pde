/**
 * Visualise touch
 */


import processing.serial.*;

Serial myPort;  // Create object from Serial class
int val;      // Data received from the serial port
int lf = 10;    // Linefeed in ASCII
String myString = null;
int low=0;
int high=10000;
int pos=0;
int linesReceived=0;
int lastRedDiff=0;
int lastBlueDiff=0;
int lastYellowDiff=0;
int lastGreenDiff=0;

final int THRESHOLD = 30;
final int SCALAR = 3;

ArrayList previousDiffs;
final int WINDOW = 10;

int baseline = 0;

void setup() 
{
  size(1000, 1000);
  blank_Canvas();
  // I know that the first port in the serial list on my mac
  // is always my  FTDI adaptor, so I open Serial.list()[0].
  // On Windows machines, this generally opens COM1.
  // Open whatever port is the one you're using.
  // List all the available serial ports
  println(Serial.list());

  String portName = Serial.list()[0];
  myPort = new Serial(this, portName, 57600);

  previousDiffs = new ArrayList();
  previousDiffs.add(0);
}

void blank_Canvas() {
  background(0);
  stroke(255);
  line(0, height/2 - THRESHOLD/SCALAR, width, height/2 - THRESHOLD/SCALAR);
  line(0, height/2 + THRESHOLD/SCALAR, width, height/2 + THRESHOLD/SCALAR);
  pos=0;
}

int reasonablate(int diff) {  
  previousDiffs.add(diff);
  int runningTotal = 0;
  for (int i=0; i < previousDiffs.size(); i++) {
    runningTotal += (Integer)previousDiffs.get(i);
  }

  int reasonablated = runningTotal/WINDOW - baseline;
  boolean allWithinThreshold = true;
  for (Object someDiff : previousDiffs) {
    Integer thatDiff = (Integer) someDiff - baseline;
    allWithinThreshold &= (thatDiff <  reasonablated + THRESHOLD); 
    allWithinThreshold &= (thatDiff > reasonablated - THRESHOLD);
    if (!allWithinThreshold) {
      println("foiled! " + thatDiff + " is not within " + THRESHOLD + " of " + reasonablated);
      break;
    }
  }
  
  if (allWithinThreshold) {
    baseline = diff;
    println("****reset baseline to " + baseline + "****");
  }
  
 if (previousDiffs.size() > WINDOW) {
    previousDiffs.remove(0);
  }

  return reasonablated;
}

void draw() {
  while (myPort.available () > 0) {
    myString = myPort.readStringUntil(lf);
    if (myString != null) {
      //draw next data point
      //println(myString);
      String[] splits = myString.split("\t");
      if (splits.length>3) {
        int redDiff = reasonablate(Integer.parseInt(splits[3].trim()));
        plotPoints(redDiff, lastRedDiff, 255, 0, 0);
        lastRedDiff = redDiff;
      }
      if (lastRedDiff < THRESHOLD && lastRedDiff > -THRESHOLD) {
        print("no touch");
      } 
      else if (lastRedDiff < -THRESHOLD) {
        print("more on the left");
      } 
      else {
        print("more on the right");
      }
      println(" (" + lastRedDiff + ")");
    }
  }
}

void plotPoints(int diff, int lastDiff, int r, int g, int b) {
  stroke(r, g, b, 127);
  line(pos-1, lastDiff/SCALAR+height/2, pos, diff/SCALAR+height/2);
  if (pos++>width) {
    blank_Canvas();
  }
}

