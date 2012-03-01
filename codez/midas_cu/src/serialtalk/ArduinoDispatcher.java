package serialtalk;

import java.awt.AWTException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTextField;

import bridge.ArduinoToDisplayBridge;
import display.ArduinoSensorButton;


public class ArduinoDispatcher {
  private List<ArduinoEvent> recentEvents = new ArrayList<ArduinoEvent>();
  
  public List<ArduinoToDisplayBridge> bridgeObjects = new ArrayList<ArduinoToDisplayBridge>();
  
  public ArduinoEvent lastEvent;

  // we want to phase out old events since they won't be part of the same gesture
  private static final int TIMEOUT_FOR_INSTRUCTION = 8000;
  
  private boolean isCapturing = false;
  private boolean isPaused = false;
  
  private List<ArduinoEvent> capturedEvents;
  
  public JTextField whatISee = new JTextField("I see...");
  
  public ArduinoDispatcher() throws AWTException { }
  
  public void beginCapturing() {
    isCapturing = true;
    capturedEvents = new ArrayList<ArduinoEvent>();
  }
  
  public List<ArduinoEvent> endCaptureAndReport() {
    isCapturing = false;
    return capturedEvents;
  }
  
  public void handleFakeEvent(ArduinoSensorButton button) {
    for (ArduinoToDisplayBridge bridge : bridgeObjects) {
      if (bridge.contains(button)) {
        bridge.execute(button);
      }
    }
  }

  public void handleEvent(ArduinoEvent e) {
    if (isPaused) {
      // drop the event on the floor
      return;
    }
    if (isCapturing) {
      capturedEvents.add(e);
      return;
    }
    
    // phase out old events
    int newestReasonableEvent;
    for (newestReasonableEvent=0; newestReasonableEvent<recentEvents.size(); newestReasonableEvent++) {
      if (recentEvents.get(newestReasonableEvent).timestamp >= System.currentTimeMillis() - TIMEOUT_FOR_INSTRUCTION) {
        break;
      }
    }
    recentEvents = recentEvents.subList(newestReasonableEvent, recentEvents.size());
    lastEvent = e;
    
    recentEvents.add(e);

    ArduinoSensor sensor = e.whichSensor;
    
    setWhatISee();
    
    if(e.touchDirection == TouchDirection.RELEASE) {
      //ignore these for right now...
      return;
    }
    
    for (ArduinoToDisplayBridge bridge : bridgeObjects) {
      if (bridge.contains(sensor)) {
        bridge.execute(sensor);
      }
    }
  }
  
  void setWhatISee() {
    whatISee.setText((recentEvents.toString()).substring(1, recentEvents.toString().length()-1));
  }
  
  void clearRecentEvents() {
	  recentEvents = new ArrayList<ArduinoEvent>();
  }
  
  public List<ArduinoEvent> lastNEvents(int n) {
    if(recentEvents.size() <= n) { return recentEvents; }
    return recentEvents.subList(recentEvents.size() - n - 1, recentEvents.size() - 1);
  }
}
