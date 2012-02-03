package serialtalk;

import java.awt.AWTException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import util.Direction;

import capture.UIScript;


public class ArduinoDispatcher {
  private List<ArduinoEvent> recentEvents = new ArrayList<ArduinoEvent>();
  
  public Map<ArduinoSensor, UIScript> buttonsToHandlers;
  public Map<ArduinoSlider, UIScript> slidersToAscHandlers;
  public Map<ArduinoSlider, UIScript> slidersToDescHandlers;
  public Map<ArduinoSensor, UIScript> padsToHandlers;
  public Map<List<ArduinoEvent>, UIScript> combosToHandlers;

  // we want to phase out old events since they won't be part of the same gesture
  private static final int TIMEOUT_FOR_INSTRUCTION = 2000;
  
  public ArduinoDispatcher() throws AWTException {
     clearAllInteractions();
  }
  
  public void clearAllInteractions() {
    buttonsToHandlers = new HashMap<ArduinoSensor, UIScript>();
    slidersToAscHandlers = new HashMap<ArduinoSlider, UIScript>();
    slidersToDescHandlers = new HashMap<ArduinoSlider, UIScript>();
    padsToHandlers = new HashMap<ArduinoSensor, UIScript>();
    combosToHandlers = new HashMap<List<ArduinoEvent>, UIScript>();
    ArduinoSetup.resetSliders();
  }

  void handleEvent(ArduinoEvent e) {
    // phase out old events
    int newestReasonableEvent;
    for (newestReasonableEvent=0; newestReasonableEvent<recentEvents.size(); newestReasonableEvent++) {
      if (recentEvents.get(newestReasonableEvent).timestamp >= System.currentTimeMillis() - TIMEOUT_FOR_INSTRUCTION) {
        break;
      }
    }
    recentEvents = recentEvents.subList(newestReasonableEvent, recentEvents.size()-1);

    recentEvents.add(e);
    ArduinoSlider slider;
    //ArduinoPad pad;
    ArduinoSensor current = e.whichSensor;

    if ((slider = ArduinoSetup.isPartOfSlider(current)) != null) {
      if (e.touchDirection == TouchDirection.TOUCH) { 
    	  // ignore the release events, since they won't necessarily be in order
    	  recentEvents.remove(e);
    	  return;
      }
      if (recentEvents.size() > 1) {
    	  ArduinoSensor previous = recentEvents.get(recentEvents.size() - 2).whichSensor;
        if (ArduinoSetup.isPartOfSlider(previous) == slider) {
          if (slider.ascOrDesc(previous, current) == Direction.ASCENDING) {
            System.out.println("ascending...");
            slidersToAscHandlers.get(slider).execute();
          } else {
            System.out.println("descending...");
            slidersToDescHandlers.get(slider).execute();
          }
        }
      }
    } else {
      for (int i = 1; i <= recentEvents.size(); i++) {
        List<ArduinoEvent> iLengthList = recentEvents.subList(
            recentEvents.size() - i, recentEvents.size());
        if (combosToHandlers.containsKey(iLengthList)) {
          System.out.println("combo!");
          combosToHandlers.get(iLengthList).execute();
        }
        else if (i==1) {
          ArduinoEvent singleEvent = iLengthList.get(0);
          //we know that it's not part of a combo, it's just a plain single button being pushed
          if(buttonsToHandlers.containsKey(singleEvent.whichSensor)) {
            System.out.println("single button");
            buttonsToHandlers.get(singleEvent.whichSensor).execute();
          }
        }
      }
    }
  }
  
  void clearRecentEvents() {
	  recentEvents = new ArrayList<ArduinoEvent>();
  }
}
