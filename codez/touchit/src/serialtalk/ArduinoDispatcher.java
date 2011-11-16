package serialtalk;

import java.awt.AWTException;
import java.awt.Robot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import capture.SikuliScript;

public class ArduinoDispatcher {
  HashMap<List<ArduinoEvent>, List<SikuliScript>> eventsToHandlers;
  List<ArduinoEvent> recentEvents;
  
  // this is the maximum number of Arduino events that can be tied to a 
  // UIAction.  we don't want any kind of infinite shit happening because that is slow.
  private static final int MAX_LENGTH_OF_INSTRUCTION = 6;
  
  public ArduinoDispatcher() throws AWTException {
    this.eventsToHandlers = new HashMap<List<ArduinoEvent>, List<SikuliScript>>();
    this.recentEvents = new ArrayList<ArduinoEvent>();
  }
  
  void handleEvent(ArduinoEvent e) {
    recentEvents.add(e);
    for (int i=0; i<MAX_LENGTH_OF_INSTRUCTION; i++) {
      if (i > recentEvents.size()) {
        return;
      }
      List<ArduinoEvent> iLengthList = recentEvents.subList(recentEvents.size()-i,recentEvents.size());
      if (eventsToHandlers.containsKey(iLengthList)) {
        for (SikuliScript s : eventsToHandlers.get(iLengthList)) {
          s.doAction();
        }
      }
    }
  }
  
  void registerEvent(List<ArduinoEvent> l, SikuliScript s) {
    if (!eventsToHandlers.containsKey(l)) {
      eventsToHandlers.put(l, new ArrayList<SikuliScript>());
    }
    eventsToHandlers.get(l).add(s);
  }
}
