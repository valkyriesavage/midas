package serialtalk;

import java.util.ArrayList;
import java.util.List;

import util.Direction;

public class ArduinoPad implements ArduinoObject {
  private List<ArduinoSensor> sensors;
  
  public ArduinoPad(List<ArduinoSensor> sensors) {
    this.sensors = sensors;
  }
  
  public ArduinoPad(ArduinoSensor[] sensors) {
    this.sensors = new ArrayList<ArduinoSensor>();
    for(int i=0; i<sensors.length; i++) {
      this.sensors.add(sensors[i]);
    }
  }
  
  public boolean isPartOfPad(ArduinoSensor sensor) {
    return sensors.contains(sensor);
  }
  
  public int hashCode() {
    String allSensors = "";
    for (ArduinoSensor as : sensors) {
      allSensors += as.location;
    }
    return Integer.parseInt(allSensors);
  }
  
  public boolean equals(Object o) {
    return (o.getClass() == ArduinoPad.class && o.hashCode() == this.hashCode());
  }
  
  public String toString() {
    return "pad : " + sensors.toString();
  }
}
