package serialtalk;

import java.awt.AWTException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JTextArea;

import capture.UIScript;

public class ArduinoDispatcher {
  private List<ArduinoEvent> recentEvents = new ArrayList<ArduinoEvent>();
  
  public Map<ArduinoSensor, UIScript> buttonsToHandlers = new HashMap<ArduinoSensor, UIScript>();
  public Map<ArduinoSlider, UIScript> slidersToAscHandlers = new HashMap<ArduinoSlider, UIScript>();
  public Map<ArduinoSlider, UIScript> slidersToDescHandlers = new HashMap<ArduinoSlider, UIScript>();
  public Map<ArduinoSensor, UIScript> padsToHandlers = new HashMap<ArduinoSensor, UIScript>();
  public Map<List<ArduinoEvent>, UIScript> combosToHandlers = new HashMap<List<ArduinoEvent>, UIScript>();


  // we want to phase out old events since they won't be part of the same gesture
  private static final int TIMEOUT_FOR_INSTRUCTION = 2000;
  
  public JTextArea whatISee = new JTextArea("what it sees is...");

  public ArduinoDispatcher() throws AWTException {

    whatISee.setEditable(false);
    whatISee.setSize(100, 5);
  }
  
  public void clearAllInteractions() {
    this.buttonsToHandlers = new HashMap<ArduinoSensor, UIScript>();
    this.slidersToAscHandlers = new HashMap<ArduinoSlider, UIScript>();
    this.slidersToDescHandlers = new HashMap<ArduinoSlider, UIScript>();
    ArduinoSetup.resetSliders();
  }

  void handleEvent(ArduinoEvent e) {
    // phase out old events
    if (recentEvents.size() > 0
        && recentEvents.get(recentEvents.size() - 1).timestamp < System.currentTimeMillis() - TIMEOUT_FOR_INSTRUCTION) {
      recentEvents = new ArrayList<ArduinoEvent>();
    }

    recentEvents.add(e);
    ArduinoSlider slider;
    ArduinoSensor current = e.whichSensor;

    if ((slider = ArduinoSetup.isPartOfSlider(current)) != null) {
      if (e.touchDirection == TouchDirection.UP) { 
    	  // ignore the release events, since they won't necessarily be in order
    	  recentEvents.remove(e);
    	  return;
      }
      if (recentEvents.size() > 1) {
    	  ArduinoSensor previous = recentEvents.get(recentEvents.size() - 2).whichSensor;
        if (ArduinoSetup.isPartOfSlider(previous) == slider) {
          if (slider.ascOrDesc(previous, current) == Direction.ASCENDING) {
            UIScript toDo = this.slidersToAscHandlers.get(slider);
            System.out.println("ascending...");
            toDo.execute();
          } else {
            UIScript toDo = this.slidersToDescHandlers.get(slider);
            System.out.println("descending...");
            toDo.execute();
          }
        }
      }
    } else {
      for (int i = 1; i <= recentEvents.size(); i++) {
        List<ArduinoEvent> iLengthList = recentEvents.subList(
            recentEvents.size() - i, recentEvents.size());
        if (buttonsToHandlers.containsKey(iLengthList)) {
          UIScript toDo = combosToHandlers.get(iLengthList);
          System.out.println("combo!");
          toDo.execute();
        }
      }
    }
  }
  
  void clearRecentEvents() {
	  recentEvents = new ArrayList<ArduinoEvent>();
  }
}
