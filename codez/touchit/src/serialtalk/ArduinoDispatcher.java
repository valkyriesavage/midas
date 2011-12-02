package serialtalk;

import java.awt.AWTException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.JTextField;

import capture.UIAction;

public class ArduinoDispatcher {
  private List<ArduinoEvent> recentEvents;

  public HashMap<List<ArduinoEvent>, List<UIAction>> eventsToHandlers;
  public HashMap<ArduinoSlider, List<UIAction>> slidersToAscHandlers;
  public HashMap<ArduinoSlider, List<UIAction>> slidersToDescHandlers;

  // we want to phase out old events since they won't be part of the same gesture
  private static final int TIMEOUT_FOR_INSTRUCTION = 2000;
  
  public JTextField whatISee = new JTextField("what i see is...                                                                        ");

  public ArduinoDispatcher() throws AWTException {
    this.eventsToHandlers = new HashMap<List<ArduinoEvent>, List<UIAction>>();
    this.slidersToAscHandlers = new HashMap<ArduinoSlider, List<UIAction>>();
    this.slidersToDescHandlers = new HashMap<ArduinoSlider, List<UIAction>>();
    this.recentEvents = new ArrayList<ArduinoEvent>();
    
    whatISee.setSize(120, 2);
  }
  
  public void clearAllInteractions() {
    this.eventsToHandlers = new HashMap<List<ArduinoEvent>, List<UIAction>>();
    this.slidersToAscHandlers = new HashMap<ArduinoSlider, List<UIAction>>();
    this.slidersToDescHandlers = new HashMap<ArduinoSlider, List<UIAction>>();
  }

  void handleEvent(ArduinoEvent e) {
    // phase out old events
    if (recentEvents.size() > 0
        && recentEvents.get(recentEvents.size() - 1).timestamp < System.currentTimeMillis() - TIMEOUT_FOR_INSTRUCTION) {
      recentEvents = new ArrayList<ArduinoEvent>();
    }

    recentEvents.add(e);
    updateWhatISee();
    ArduinoSlider slider;

    if ((slider = ArduinoSetup.isPartOfSlider(e.whichSensor)) != null) {
      if (recentEvents.size() > 1
          && ArduinoSetup
              .isPartOfSlider(recentEvents.get(recentEvents.size() - 1).whichSensor) == slider) {
        ArduinoSensor previous = recentEvents.get(recentEvents.size() - 1).whichSensor;
        if (slider.ascOrDesc(previous, e.whichSensor) == Direction.ASCENDING) {
          List<UIAction> toDo = this.slidersToAscHandlers.get(slider);
          for (UIAction action : toDo) {
            action.doAction();
          }
        } else {
          List<UIAction> toDo = this.slidersToDescHandlers.get(slider);
          for (UIAction action : toDo) {
            action.doAction();
          }
        }
      }
    } else {
      for (int i = 0; i <= recentEvents.size(); i++) {
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
  
  void unregisterEvent(List<ArduinoEvent> l, UIAction s) {
    if (!eventsToHandlers.containsKey(l)) {
       return;
    }
    eventsToHandlers.get(l).remove(s);
  }
  
  void unregisterAllForSensor(ArduinoEvent sensor) {
    if (!eventsToHandlers.containsKey(sensor)) {
      return;
   }
   eventsToHandlers.remove(sensor);
  }

  private ArduinoSlider registerSliderIfNecessary(List<ArduinoSensor> l) {
    ArduinoSlider slider = null;
    for (ArduinoSensor sensor : l) {
      if ((slider = ArduinoSetup.isPartOfSlider(sensor)) == null) {
        slider = new ArduinoSlider(l);
        ArduinoSetup.addSlider(slider);
      }
    }
    return slider;
  }

  void registerSliderAscendingEvent(List<ArduinoSensor> l, UIAction s) {
    ArduinoSlider slider = registerSliderIfNecessary(l);
    if (!slidersToAscHandlers.containsKey(slider)) {
      slidersToAscHandlers.put(slider, new ArrayList<UIAction>());
    }
    slidersToAscHandlers.get(slider).add(s);
  }
  
  void unregisterSliderAscendingEvent(ArduinoSlider slider, UIAction s) {
    if (!slidersToAscHandlers.containsKey(slider)) {
      return;
    }
    slidersToAscHandlers.get(slider).remove(s);
  }

  void registerSliderDescendingEvent(List<ArduinoSensor> l, UIAction s) {
    ArduinoSlider slider = registerSliderIfNecessary(l);
    if (!slidersToDescHandlers.containsKey(slider)) {
      slidersToDescHandlers.put(slider, new ArrayList<UIAction>());
    }
    slidersToDescHandlers.get(slider).add(s);
  }
  
  void unregisterSliderDescendingEvent(ArduinoSlider slider, UIAction s) {
    if (!slidersToDescHandlers.containsKey(slider)) {
      return;
    }
    slidersToDescHandlers.get(slider).remove(s);
  }
  
  void updateWhatISee() {
	  whatISee.setText("what i see is... " + recentEvents);
  }
}
