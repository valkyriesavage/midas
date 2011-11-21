package serialtalk;

public class ArduinoSensor {
  public int which;
  
  public ArduinoSensor(int which) {
    this.which = which;
  }
  
  public String toString() {
    return "sensor " + which;
  }
}
