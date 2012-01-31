package serialtalk;


public class ArduinoEvent implements ArduinoObject {
  TouchDirection touchDirection;
  ArduinoSensor whichSensor;
  long timestamp = System.currentTimeMillis();
  
  public ArduinoEvent(ArduinoSensor whichSensor, TouchDirection touchDirection) {
	    this.whichSensor = whichSensor;
	    this.touchDirection = touchDirection;
  }
  
  public ArduinoEvent(ArduinoSensor whichSensor) {
		this.whichSensor = whichSensor;
  }
  
  public void setDirection(TouchDirection touchDirection) {
	  this.touchDirection = touchDirection;
  }
  
  public boolean isComplete() {
	  return (this.touchDirection != null);
  }
  
  public int hashCode() {
    return this.whichSensor.location.x + 100*this.whichSensor.location.y + 1000*this.touchDirection.ordinal();
  }
  
  @Override
  public String toString() {
    String retVal = new String();
    if (touchDirection == TouchDirection.TOUCH){
      retVal += "release ";
    } else if (touchDirection == TouchDirection.RELEASE) {
      retVal += "touch ";
    }
    retVal += whichSensor.location;
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
