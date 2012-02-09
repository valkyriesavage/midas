package bridge;

import java.awt.Graphics2D;

import serialtalk.ArduinoObject;
import serialtalk.ArduinoSensor;
import capture.UIScript;
import display.SensorButtonGroup;
import display.SensorShape;

public class ArduinoToButtonBridge implements ArduinoToDisplayBridge {
  private static final SensorButtonGroup nullInterface = new SensorButtonGroup(SensorShape.shapes.SQUARE);
  private static final ArduinoSensor nullSensor = new ArduinoSensor(-1,-1);
  private static final UIScript nullScript = new UIScript();
  
  public SensorButtonGroup interfacePiece = nullInterface;
  public ArduinoObject arduinoPiece = nullSensor;
  public UIScript interactivePiece = nullScript;
    
  public ArduinoToButtonBridge() {System.out.println("made a button bridge");}
    
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
  
  public void executeScript() {
    if (interactivePiece != nullScript) {
      interactivePiece.execute();
    }
  }
  
  public void setInterfacePiece(SensorButtonGroup interfacePiece) {
    this.interfacePiece = interfacePiece;
  }
}
