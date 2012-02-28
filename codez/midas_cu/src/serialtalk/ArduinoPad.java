package serialtalk;

import java.awt.Point;


public class ArduinoPad implements ArduinoObject {
  private ArduinoSensor[][] sensors;
  public int sensitivity;
  
  public ArduinoPad(ArduinoSensor[][] sensors) {
    this.sensors = sensors;
  }
  
  public boolean isPartOfPad(ArduinoSensor sensor) {
    return locationOnPad(sensor) != null;
  }
  
  public Point locationOnPad(ArduinoSensor sensor) {
    for (int i=0; i<sensors.length; i++) {
      for (int j=0; j<sensors[i].length; j++) {
        if (sensors[i][j].equals(sensor)) {
          return new Point(i, j);
        }
      }
    }
    return null;
  }
  
  public int hashCode() {
    String allSensors = "";
    for (int i=0; i<sensors.length; i++) {
      for (int j=0; j<sensors[i].length; j++) {
        allSensors += sensors[i][j].location;
      }
    }
    return Integer.parseInt(allSensors);
  }
  
  public boolean equals(Object o) {
    return (o.getClass() == ArduinoPad.class && o.hashCode() == this.hashCode());
  }
  
  public String toString() {
    return "pad : " + sensors.toString();
  }
  
  public void setSensitivity(int sensitivity) {
    this.sensitivity = sensitivity;
  }

  public boolean contains(ArduinoSensor sensor) {
    return isPartOfPad(sensor);
  }
}
