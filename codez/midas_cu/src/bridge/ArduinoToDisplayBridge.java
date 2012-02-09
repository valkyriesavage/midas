package bridge;

import java.awt.Graphics2D;

import serialtalk.ArduinoObject;
import display.SensorButtonGroup;

public interface ArduinoToDisplayBridge {
  public ArduinoObject arduinoPiece();
  public SensorButtonGroup interfacePiece();
  public void setInterfacePiece(SensorButtonGroup interfacePiece);
  public void paint(Graphics2D g);
  //public void executeScript();
  // we don't have that last one active because the execution of a script depends upon direction for the slider
  // and the pad...
}
