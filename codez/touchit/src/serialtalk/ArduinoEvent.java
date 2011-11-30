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
    return this.whichSensor.which + 100*this.touchDirection.ordinal();
  }
  
  @Override
  public String toString() {
    String retVal = new String();
    if (touchDirection == TouchDirection.UP){
      retVal += "release ";
    } else if (touchDirection == TouchDirection.DOWN) {
      retVal += "touch ";
    }
    retVal += whichSensor.which;
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
