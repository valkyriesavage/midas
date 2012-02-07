package bridge;

import serialtalk.ArduinoObject;
import serialtalk.ArduinoSensor;
import serialtalk.ArduinoSlider;
import capture.UIScript;
import display.SensorButtonGroup;
import display.SensorShape;

public class ArduinoToSliderBridge {
  private static final SensorButtonGroup nullInterface = new SensorButtonGroup(SensorShape.shapes.SQUARE);
  private static final ArduinoSlider nullSlider = new ArduinoSlider(new ArduinoSensor[0]);
  private static final UIScript nullScript = new UIScript();
  
  public SensorButtonGroup interfacePiece = nullInterface;
  public ArduinoObject arduinoPiece = nullSlider;
  public UIScript interactivePieceAsc = nullScript;
  public UIScript interactivePieceDesc = nullScript;
    
  public ArduinoToSliderBridge() {}
    
  public String toString() {
    if (interfacePiece.name != null) {
      return interfacePiece.name;
    }
    return "unknown";
  }
}
