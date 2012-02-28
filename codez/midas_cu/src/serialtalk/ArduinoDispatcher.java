package serialtalk;

import java.awt.AWTException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTextField;

import bridge.ArduinoToDisplayBridge;


public class ArduinoDispatcher {
  private List<ArduinoEvent> recentEvents = new ArrayList<ArduinoEvent>();
  
  public List<ArduinoToDisplayBridge> bridgeObjects = new ArrayList<ArduinoToDisplayBridge>();
  
  public ArduinoEvent lastEvent;

  // we want to phase out old events since they won't be part of the same gesture
  private static final int TIMEOUT_FOR_INSTRUCTION = 2000;
  
  public JTextField whatISee = new JTextField("I see...");
  
  public ArduinoDispatcher() throws AWTException { }

  void handleEvent(ArduinoEvent e) {
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

    if (e.touchDirection == TouchDirection.RELEASE) { 
    	// ignore the release events, since they aren't relevant until we make combos happen
      // TODO : Make combos happen!
      recentEvents.remove(e);
   	  return;
    }
    
    setWhatISee();
    
    for (ArduinoToDisplayBridge bridge : bridgeObjects) {
      if (bridge.contains(sensor)) {
        bridge.execute(sensor);
      }
    }
  }
  
  void setWhatISee() {
    whatISee.setText(recentEvents.toString());
  }
  
  void clearRecentEvents() {
	  recentEvents = new ArrayList<ArduinoEvent>();
  }
  
  public List<ArduinoEvent> lastNEvents(int n) {
    if(recentEvents.size() <= n) { return recentEvents; }
    return recentEvents.subList(recentEvents.size() - n - 1, recentEvents.size() - 1);
  }
}
