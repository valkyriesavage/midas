package bridge;

import java.util.ArrayList;

import serialtalk.ArduinoObject;
import serialtalk.ArduinoPad;
import serialtalk.ArduinoSensor;
import capture.UIScript;
import display.SensorButtonGroup;
import display.SensorShape;

public class ArduinoToPadBridge {
  private static final SensorButtonGroup nullInterface = new SensorButtonGroup(SensorShape.shapes.SQUARE);
  private static final ArduinoPad nullPad = new ArduinoPad(new ArrayList<ArduinoSensor>());
  private static final UIScript nullScript = new UIScript();
  
  public SensorButtonGroup interfacePiece = nullInterface;
  public ArduinoObject arduinoPiece = nullPad;
  public UIScript interactivePiece = nullScript;
    
  public ArduinoToPadBridge() {}
    
  public String toString() {
    if (interfacePiece.name != null) {
      return interfacePiece.name;
    }
    return "unknown";
  }
}
