package bridge;

import serialtalk.ArduinoObject;
import serialtalk.ArduinoSensor;
import capture.UIScript;
import display.SensorButtonGroup;
import display.SensorShape;

public class ArduinoToInterfaceBridge {
  private static final SensorButtonGroup nullInterface = new SensorButtonGroup(SensorShape.shapes.SQUARE);
  private static final ArduinoSensor nullSensor = new ArduinoSensor(-1,-1);
  //FIXME: private static final ArduinoSlider nullSlider = new ArduinoSlider([nullSensor]);
  //TODO: deal with pads and combos?
  private static final UIScript nullScript = new UIScript();
  
  public SensorButtonGroup interfacePiece = nullInterface;
  public ArduinoObject arduinoPiece = nullSensor;
  public UIScript interactivePiece = nullScript;
    
  public ArduinoToInterfaceBridge() {}
    
  public String toString() {
    if (interfacePiece.name != null) {
      return interfacePiece.name;
    }
    return "unknown";
  }
}
