package serialtalk;


public class ArduinoEvent implements ArduinoObject {
  public TouchDirection touchDirection;
  public ArduinoSensor whichSensor;
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
    String retVal = "";
    if (touchDirection == TouchDirection.TOUCH){
      retVal += "touch ";
    } else if (touchDirection == TouchDirection.RELEASE) {
      retVal += "release ";
    }
    if (whichSensor != null && whichSensor.location != null) { 
      retVal += whichSensor.location.x + ", " + whichSensor.location.y;
    }

    return retVal;
  }
  
  @Override
  public boolean equals(Object o) {
    if ((o.getClass() == ArduinoEvent.class) && (this.hashCode() == o.hashCode())) {
      return true;
    }
    return false;
  }

  public boolean contains(ArduinoSensor sensor) {
    return sensor.equals(whichSensor);
  }
}
