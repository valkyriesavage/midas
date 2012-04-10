package serialtalk;


public class ArduinoEvent implements ArduinoObject {
  public TouchDirection touchDirection;
  public ArduinoSensor whichSensor;
  long timestamp = System.currentTimeMillis();
  
  public boolean isHellaSlider = false;
  public int hellaSliderLocation = -1;
  
  public ArduinoEvent(ArduinoSensor whichSensor, TouchDirection touchDirection) {
	    this.whichSensor = whichSensor;
	    this.touchDirection = touchDirection;
  }
  
  public ArduinoEvent(int location, TouchDirection touchDirection) {
    isHellaSlider = true;
    hellaSliderLocation = location;
    this.touchDirection = touchDirection;
  }
  
  public ArduinoEvent(ArduinoSensor whichSensor) {
		this.whichSensor = whichSensor;
  }
  
  public void setDirection(TouchDirection touchDirection) {
	  this.touchDirection = touchDirection;
  }
  
  public boolean isComplete() {
	  return (touchDirection != null && (whichSensor != null || isHellaSlider));
  }
  
  public int hashCode() {
    return whichSensor.location.x + 100*whichSensor.location.y + 1000*touchDirection.ordinal() + 10000*hellaSliderLocation;
  }
  
  @Override
  public String toString() {
    String retStr = "";
    if (touchDirection == TouchDirection.TOUCH){
      retStr += "touch ";
    } else if (touchDirection == TouchDirection.RELEASE) {
      retStr += "release ";
    }
    if (whichSensor != null && whichSensor.location != null) {
      retStr += whichSensor.location.x;
      if (whichSensor.location.y >= 0) {
        retStr += ", " + whichSensor.location.y;
      }
    } else if (hellaSliderLocation >= 0) {
      retStr += " slider " + hellaSliderLocation;
    }

    return retStr;
  }
  
  @Override
  public boolean equals(Object o) {
    if ((o.getClass() == ArduinoEvent.class) && (this.hashCode() == o.hashCode())) {
      return true;
    }
    return false;
  }

  public boolean contains(ArduinoSensor sensor) {
    return !isHellaSlider && sensor.equals(whichSensor);
  }
  
  public int[] sensor() {
    int[] ret = {whichSensor.location.x};
    return ret;
  }
}
