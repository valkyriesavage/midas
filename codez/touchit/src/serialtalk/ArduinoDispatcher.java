package serialtalk;

import java.awt.AWTException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import capture.UIAction;

public class ArduinoDispatcher {
  HashMap<List<ArduinoEvent>, List<UIAction>> eventsToHandlers;
  List<ArduinoEvent> recentEvents;
  HashMap<ArduinoSlider, List<UIAction>> slidersToHandlers;

  // this is the maximum number of Arduino events that can be tied to a
  // UIAction. we don't want any kind of infinite shit happening because that is
  // slow.
  private static final int MAX_LENGTH_OF_INSTRUCTION = 8;

  public ArduinoDispatcher() throws AWTException {
    this.eventsToHandlers = new HashMap<List<ArduinoEvent>, List<UIAction>>();
    this.slidersToHandlers = new HashMap<ArduinoSlider, List<UIAction>>();
    this.recentEvents = new ArrayList<ArduinoEvent>();
  }

  void handleEvent(ArduinoEvent e) {
    recentEvents.add(e);
    ArduinoSlider slider;

    if ((slider = ArduinoSetup.isPartOfSlider(e.whichSensor)) != null) {
      if (ArduinoSetup.isPartOfSlider(recentEvents.get(recentEvents.size()-1).whichSensor) == slider) {
        List<UIAction> toDo = this.slidersToHandlers.get(slider);
        for (UIAction action : toDo) {
          action.doAction(); // how do we know to go up or down? TODO
        }
      }
    } else {
      for (int i = 0; i < MAX_LENGTH_OF_INSTRUCTION; i++) {
        if (i > recentEvents.size()) {
          return;
        }
        List<ArduinoEvent> iLengthList = recentEvents.subList(
            recentEvents.size() - i, recentEvents.size());
        if (eventsToHandlers.containsKey(iLengthList)) {
          for (UIAction s : eventsToHandlers.get(iLengthList)) {
            s.doAction();
          }
        }
      }
    }
  }

  void registerEvent(List<ArduinoEvent> l, UIAction s) {
    if (!eventsToHandlers.containsKey(l)) {
      eventsToHandlers.put(l, new ArrayList<UIAction>());
    }
    eventsToHandlers.get(l).add(s);
  }

  void registerSliderEvent(List<ArduinoSensor> l, UIAction s) {
    ArduinoSlider slider = new ArduinoSlider(l);
    ArduinoSetup.addSlider(slider);
    if (!slidersToHandlers.containsKey(l)) {
      slidersToHandlers.put(slider, new ArrayList<UIAction>());
    }
    slidersToHandlers.get(slider).add(s);
  }
}
