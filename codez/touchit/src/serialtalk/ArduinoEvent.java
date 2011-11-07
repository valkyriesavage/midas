package serialtalk;

public class ArduinoEvent {
  int whichSensor;
  public ArduinoEvent(int whichSensor) {
    this.whichSensor = whichSensor;
  }
  
  public int hashCode() {
     return whichSensor;
  }
}
