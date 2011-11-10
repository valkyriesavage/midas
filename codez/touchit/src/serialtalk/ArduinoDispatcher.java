package serialtalk;

import java.awt.AWTException;
import java.awt.Robot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import capture.UIAction;

public class ArduinoDispatcher {
  HashMap<ArduinoEvent, List<UIAction>> eventsToHandlers;
  Robot screenRobot;
  
  public ArduinoDispatcher() throws AWTException {
    this.eventsToHandlers = new HashMap<ArduinoEvent, List<UIAction>>();
    this.screenRobot = new Robot();
  }
  
  void handleEvent(ArduinoEvent e) {
    System.out.println("***" + eventsToHandlers.toString());
    if (eventsToHandlers.containsKey(e)) {
      for (UIAction a : eventsToHandlers.get(e)) {
        a.performAction(screenRobot);
      }
    }
  }
  
  void registerEvent(ArduinoEvent e, UIAction a) {
    if (!eventsToHandlers.containsKey(e)) {
      eventsToHandlers.put(e, new ArrayList<UIAction>());
    }
    eventsToHandlers.get(e).add(a);
  }
}
