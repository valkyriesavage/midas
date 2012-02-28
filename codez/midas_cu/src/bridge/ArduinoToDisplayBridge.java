package bridge;

import java.awt.Graphics2D;
import java.util.List;

import serialtalk.ArduinoEvent;
import serialtalk.ArduinoObject;
import serialtalk.ArduinoSensor;
import display.SensorButtonGroup;

public abstract class ArduinoToDisplayBridge {
  public ArduinoObject arduinoPiece;
  public SensorButtonGroup interfacePiece;
  
  public void setInterfacePiece(SensorButtonGroup interfacePiece) {
    this.interfacePiece = interfacePiece;
  }
  
  public void paint(Graphics2D g) {
    interfacePiece.paint(g);
  }
  
  public void execute(ArduinoSensor sensor) {};
  
  public boolean contains(ArduinoSensor sensor) {
    return arduinoPiece.contains(sensor);
  }
  
  public void setArduinoSequence(List<ArduinoEvent> events) {};
}
