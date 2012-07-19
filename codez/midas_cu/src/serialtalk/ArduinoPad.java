package serialtalk;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class ArduinoPad implements ArduinoObject {
  private ArduinoSensor[][] sensors;
  public int sensitivity;
  
  public ArduinoPad(ArduinoSensor[][] sensors) {
    this.sensors = sensors;
  }
  
  public boolean isPartOfPad(ArduinoSensor sensor) {
    return locationOnPad(sensor) != null;
  }
  
  public ArduinoSensor sensorAt(int x, int y) {
    return sensors[y][x];
  }
  
  public Point locationOnPad(ArduinoSensor sensor) {
    for (int i=0; i<sensors.length; i++) {
      for (int j=0; j<sensors[i].length; j++) {
        if (sensors[i][j].equals(sensor)) {
          return new Point(j, i);
        }
      }
    }
    return null;
  }
  
  public double positionXInPad(ArduinoSensor sensor) {
    return locationOnPad(sensor).x / (1.0*sensors.length - 1);
  }
 
  public double positionYInPad(ArduinoSensor sensor) {
    return locationOnPad(sensor).y / (1.0*sensors[0].length - 1);
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
  
  public int[] sensor() {
    sensitivity = sensors.length * sensors[0].length;
    int[] retSensors = new int[sensitivity];
    List<Integer> terminals = terminals();
    for (Integer i : terminals) {
      retSensors[terminals.indexOf(i)] = i.intValue();
    }
    return retSensors;
  }
  
  public List<Integer> terminals() {
    List<Integer> terminals = new ArrayList<Integer>();
    for (int i=0; i<sensors.length; i++) {
      for (int j=0; j<sensors[i].length; j++) {
        if(!terminals.contains(sensors[i][j].location.x)) {
          terminals.add(sensors[i][j].location.x);
        }
        if(!terminals.contains(sensors[i][j].location.y)) {
          terminals.add(sensors[i][j].location.y);
        }
      }
    }
    Collections.sort(terminals);
    return terminals;
  }
}
