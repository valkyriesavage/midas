package serialtalk;

public interface ArduinoObject {
  public boolean contains(ArduinoSensor sensor);
  
  public int[] sensor();
}
