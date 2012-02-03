package serialtalk;

import java.util.List;

import util.Direction;

public class ArduinoSlider implements ArduinoObject {
  private List<ArduinoSensor> sensors;
  
  public ArduinoSlider(List<ArduinoSensor> sensors) {
    this.sensors = sensors;
  }
  
  public boolean isPartOfSlider(ArduinoSensor sensor) {
    return sensors.contains(sensor);
  }
  
  public Direction ascOrDesc(ArduinoSensor previous, ArduinoSensor current) {
    if (sensors.indexOf(previous) > sensors.indexOf(current)) {
      return Direction.ASCENDING;
    }
    return Direction.DESCENDING;
  }
  
  public int howFar(List<ArduinoSensor> sensorsTouched) {
    return Math.abs(sensors.indexOf(sensorsTouched.get(0)) - sensors.indexOf(sensorsTouched.get(sensorsTouched.size())));
  }
  
  public int hashCode() {
    String allSensors = "";
    for (ArduinoSensor as : sensors) {
      allSensors += as.location;
    }
    return Integer.parseInt(allSensors);
  }
  
  public boolean equals(Object o) {
    if (o.getClass() == ArduinoSlider.class && o.hashCode() == this.hashCode()) {
      return true;
    }
    return false;
  }
  
  public String backwardsToString() {
	  String ret = "slider : [";
	  for(int i=sensors.size() - 1; i >= 0; i--) {
		  ret += sensors.get(i) + ", ";
	  }
	  ret = ret.substring(0, ret.length() - 2); //get rid of trailing comma
	  ret += "]";
	  return ret;
  }
  
  public String toString() {
    return "slider : " + sensors.toString();
  }
}
