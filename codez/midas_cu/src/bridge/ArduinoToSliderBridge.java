package bridge;

import java.awt.Graphics2D;

import serialtalk.ArduinoObject;
import serialtalk.ArduinoSensor;
import serialtalk.ArduinoSlider;
import capture.UIScript;
import display.SensorButtonGroup;
import display.SensorShape;

public class ArduinoToSliderBridge implements ArduinoToDisplayBridge {
  private static final SensorButtonGroup nullInterface = new SensorButtonGroup(SensorShape.shapes.SQUARE);
  private static final ArduinoSlider nullSlider = new ArduinoSlider(new ArduinoSensor[0]);
  private static final UIScript nullScript = new UIScript();
  
  public SensorButtonGroup interfacePiece = nullInterface;
  public ArduinoObject arduinoPiece = nullSlider;
  public UIScript interactivePieceAsc = nullScript;
  public UIScript interactivePieceDesc = nullScript;
    
  public ArduinoToSliderBridge() {
    nullInterface.isSlider = true;
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
