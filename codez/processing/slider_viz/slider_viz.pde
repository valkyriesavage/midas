/**
 * Visualise the slider we're playing with
 */


import processing.serial.*;

Serial myPort;  // Create object from Serial class
int val;      // Data received from the serial port
int lf = 10;    // Linefeed in ASCII
String myString = null;
int low=0;
int high=10000;
int linesReceived=0;

final int THRESHOLD = 30;
final int SCALAR = 6;

ArrayList previousDiffs;
final int WINDOW = 10;

final int RADIUS = 10;
int baseline = 0;

void setup() 
{
  size(60, 400);
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
      println("foiled! "  + thatDiff + " is not within " + THRESHOLD + " of " + reasonablated + "!");
      break;
    }
  }
  
  if (allWithinThreshold) {
    println("threshold reset");
    baseline = diff;
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
      String[] splits = myString.split("\t");
      if (splits.length>3) {
        int redDiff = reasonablate(Integer.parseInt(splits[3].trim()));
        plotPoints(redDiff);
      }
    }
  }
}

void plotPoints(int diff) {
  blank_Canvas();
  if (diff > THRESHOLD || diff < -THRESHOLD) {
    stroke(255,0,0);
    ellipse(width/2 - RADIUS, diff/SCALAR+height/2 - RADIUS, 2*RADIUS, 2*RADIUS);
  }
}

