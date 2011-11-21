package serialtalk;

import java.util.List;

public class ArduinoSlider {
  private List<ArduinoSensor> sensors;
  
  public ArduinoSlider(List<ArduinoSensor> sensors) {
    this.sensors = sensors;
  }
  
  public boolean isPartOfSlider(ArduinoSensor sensor) {
    return sensors.contains(sensor);
  }
  
  public Direction upOrDown(List<ArduinoSensor> sensorsTouched) {
    if (sensors.indexOf(sensorsTouched.get(0)) > sensors.indexOf(sensors.get(1))) {
      return Direction.ASCENDING;
    }
    return Direction.DESCENDING;
  }
  
  public int howFar(List<ArduinoSensor> sensorsTouched) {
    return Math.abs(sensors.indexOf(sensorsTouched.get(0)) - sensors.indexOf(sensorsTouched.get(sensorsTouched.size())));
  }
  
  public String toString() {
    return "slider : " + sensors.toString();
  }
}
