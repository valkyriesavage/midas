package serialtalk;

import java.awt.Point;

public class ArduinoSensor implements ArduinoObject {
  public Point location;
  // a y coordinate of -1 implies that this is a non-gridded sensor on pin #x
  
  private String name;
  
  public ArduinoSensor(Point location) {
    register(location);
  }
  
  public ArduinoSensor(int x, int y) {
    this.location = new Point(x,y);
    this.name = "" + location;
  }
  
  public ArduinoSensor(String name) {
    this.name = name;
    this.location = new Point(-1,-1);
  }
  
  public void setName(String name) {
    this.name = name;
  }
  
  public String toString() {
    return name;
  }
  
  public int hashCode() {
    return Integer.parseInt(String.format("%02d%02d", location.x, location.y));
  }
  
  public boolean equals(Object o) {
    return o instanceof ArduinoSensor && this.location.equals(((ArduinoSensor)o).location);
  }

  public boolean contains(ArduinoSensor sensor) {
    return sensor.location.x == this.location.x && sensor.location.y == this.location.y;
  }
  
  public void register(Point newLocation) {
    this.location = newLocation;
    this.name = ""+ location;
  }
}
