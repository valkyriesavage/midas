package serialtalk;

public class ArduinoSensor implements ArduinoObject {
  public int which;
  
  public ArduinoSensor(int which) {
    this.which = which;
  }
  
  public String toString() {
    return "" + which;
  }
}
