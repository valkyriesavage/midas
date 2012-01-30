package serialtalk;

public class ArduinoSensor implements ArduinoObject {
  public int which;
  private String name;
  
  public ArduinoSensor(int which) {
    this.which = which;
    this.name = "" + which;
  }
  
  public void setName(String name) {
    this.name = name;
  }
  
  public String toString() {
    return name;
  }
}
