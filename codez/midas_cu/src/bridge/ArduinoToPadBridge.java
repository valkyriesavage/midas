package bridge;

import java.awt.Graphics2D;
import java.util.ArrayList;

import serialtalk.ArduinoObject;
import serialtalk.ArduinoPad;
import serialtalk.ArduinoSensor;
import capture.UIScript;
import display.SensorButtonGroup;
import display.SensorShape;

public class ArduinoToPadBridge implements ArduinoToDisplayBridge {
  private static final SensorButtonGroup nullInterface = new SensorButtonGroup(SensorShape.shapes.SQUARE);
  private static final ArduinoPad nullPad = new ArduinoPad(new ArrayList<ArduinoSensor>());
  private static final UIScript nullScript = new UIScript();
  
  public SensorButtonGroup interfacePiece = nullInterface;
  public ArduinoObject arduinoPiece = nullPad;
  public UIScript interactivePiece = nullScript;
    
  public ArduinoToPadBridge() {
    nullInterface.isPad = true;
  }
    
  public String toString() {
    if (interfacePiece.name != null) {
      return interfacePiece.name;
    }
    return "unknown";
  }
  
  public ArduinoObject arduinoPiece() {
    return arduinoPiece;
  }
  
  public SensorButtonGroup interfacePiece() {
    return interfacePiece;
  }
  
  public void paint(Graphics2D g) {
    if (interfacePiece != nullInterface) {
      interfacePiece.paint(g);
    }
  }
  
  public void setInterfacePiece(SensorButtonGroup interfacePiece) {
    this.interfacePiece = interfacePiece;
  }
}
