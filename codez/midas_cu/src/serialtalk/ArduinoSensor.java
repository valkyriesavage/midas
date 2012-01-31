package serialtalk;

import java.awt.Dimension;

public class ArduinoSensor implements ArduinoObject {
  public Dimension which;
  
  private String name;
  
  public ArduinoSensor(Dimension which) {
    this.which = which;
    this.name = "" + which;
  }
  
  public ArduinoSensor(int x, int y) {
    this.which = new Dimension(x,y);
    this.name = "" + which;
  }
  
  public void setName(String name) {
    this.name = name;
  }
  
  public String toString() {
    return name;
  }
}
