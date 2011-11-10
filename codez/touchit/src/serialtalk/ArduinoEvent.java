package serialtalk;

import java.util.List;

public class ArduinoEvent {
  TouchDirection touchDirection;
  int whichSensor;
  
  public ArduinoEvent(int whichSensor, TouchDirection touchDirection) {
    this.whichSensor = whichSensor;
    this.touchDirection = touchDirection;
  }
  
  public int hashCode() {
    return this.whichSensor + 100*this.touchDirection.ordinal();
  }
  
  @Override
  public String toString() {
    String retVal = new String();
    if (touchDirection == TouchDirection.UP){
      retVal += "release ";
    } else {
      retVal += "press ";
    }
    retVal += whichSensor;
    return retVal;
  }
  
  @Override
  public boolean equals(Object o) {
    if ((o.getClass() == ArduinoEvent.class) && (this.hashCode() == o.hashCode())) {
      return true;
    }
    return false;
  }
}
