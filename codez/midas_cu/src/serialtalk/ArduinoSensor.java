package serialtalk;

import java.awt.Point;

public class ArduinoSensor implements ArduinoObject {
  public Point location;
  
  private String name;
  
  public ArduinoSensor(Point location) {
    this.location = location;
    this.name = "" + location;
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
}
