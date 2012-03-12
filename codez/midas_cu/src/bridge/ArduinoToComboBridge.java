package bridge;

import java.util.ArrayList;
import java.util.List;

import actions.UIScript;

import serialtalk.ArduinoEvent;

public class ArduinoToComboBridge {
  private static final List<ArduinoEvent> nullEventList = new ArrayList<ArduinoEvent>();
  private static final UIScript nullScript = new UIScript();
  
  public List<ArduinoEvent> arduinoPiece = nullEventList;
  public UIScript interactivePiece = nullScript;
    
  public ArduinoToComboBridge() {}
    
  public String toString() {
    if (arduinoPiece.size() > 0) {
      return arduinoPiece.toString();
    }
    return "unknown";
  }
  
  public void executeScript() {
    if (interactivePiece != nullScript) {
      interactivePiece.execute();
    }
  }
}
