package serialtalk;

import java.awt.AWTException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTextField;

import util.EventType;

import actions.SocketTalkAction;
import bridge.ArduinoToDisplayBridge;
import bridge.ArduinoToSliderBridge;
import display.ArduinoSensorButton;

public class ArduinoDispatcher {
  private List<ArduinoEvent> recentEvents = new ArrayList<ArduinoEvent>();

  public List<ArduinoToDisplayBridge> bridgeObjects = new ArrayList<ArduinoToDisplayBridge>();

  public ArduinoEvent lastEvent;

  // we want to phase out old events since they won't be part of the same
  // gesture
  private static final int TIMEOUT_FOR_INSTRUCTION = 8000;

  private boolean isCapturing = false;
  private boolean isPaused = false;

  private List<ArduinoEvent> capturedEvents;
  
  private SocketTalkAction socketAction = new SocketTalkAction("http://localhost:8080");
  
  public JTextField whatISee = new JTextField("I see...");

  public ArduinoDispatcher() throws AWTException {
  }

  public void beginCapturing() {
    isCapturing = true;
    capturedEvents = new ArrayList<ArduinoEvent>();
  }

  public boolean isCapturing() {
    return isCapturing;
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

  public void handleFakeEvent(int hellaSliderValue) {
    for (ArduinoToDisplayBridge bridge : bridgeObjects) {
      if (bridge.isHellaSlider) {
        ((ArduinoToSliderBridge) bridge).execute(hellaSliderValue);
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

    /*
    // phase out old events
    int newestReasonableEvent;
    for (newestReasonableEvent = 0; newestReasonableEvent < recentEvents.size(); newestReasonableEvent++) {
      if (recentEvents.get(newestReasonableEvent).timestamp >= System
          .currentTimeMillis() - TIMEOUT_FOR_INSTRUCTION) {
        break;
      }
    }
    recentEvents = recentEvents.subList(newestReasonableEvent,
        recentEvents.size());*/
    lastEvent = e;

    addRecentEvent(e);
    
    if (e.touchDirection == TouchDirection.RELEASE){
      // we don't need to activate the button, but we do need to tell it to release
        for (ArduinoToDisplayBridge bridge : bridgeObjects) {
          if (bridge.contains(e.whichSensor)) {
            bridge.release(e.whichSensor);
          }
        }
      // don't go execute it now.  just be done.
      return;
    }
    
    if (e.isHellaSlider) {
      for (ArduinoToDisplayBridge bridge : bridgeObjects) {
        if (bridge.isHellaSlider) {
          ((ArduinoToSliderBridge) bridge).execute(e.hellaSliderLocation);
          ((ArduinoToSliderBridge) bridge).touch(e.hellaSliderLocation);
        }
      }
    } else {
      for (ArduinoToDisplayBridge bridge : bridgeObjects) {
        if (bridge.contains(e.whichSensor)) {
          bridge.execute(e.whichSensor, e.touchDirection);
          bridge.touch(e.whichSensor);
        }
      }
    }

    // now we just push everything out to a websocket
    socketAction.doAction();
  }

  void setWhatISee() {
    whatISee.setText((recentEvents.toString()).substring("[".length(),
        recentEvents.toString().length() - "]".length()));
  }
  
  private void addRecentEvent(ArduinoEvent e) {
    recentEvents.add(e);
    String test = "";
    for (ArduinoEvent event : recentEvents) {
      test += event.regexableString();
    }
    System.out.println(test);
    System.out.println("\t" + FaultyConnectionRegexMatcher.containsFaultyConnection(test));
    if (FaultyConnectionRegexMatcher.containsFaultyConnection(test) != FaultyConnectionType.OK) {
      System.out.println(FaultyConnectionRegexMatcher.containsFaultyConnection(test));
    }
  }

  void clearRecentEvents() {
    recentEvents = new ArrayList<ArduinoEvent>();
  }

  public List<ArduinoEvent> lastNEvents(int n) {
    if (recentEvents.size() <= n) {
      return recentEvents;
    }
    return recentEvents.subList(recentEvents.size() - n - 1,
        recentEvents.size() - 1);
  }

  public EventType getType(ArduinoSensor sensor) {
    for (ArduinoToDisplayBridge bridge : bridgeObjects) {
      if (bridge.contains(sensor)) {
        if (bridge.interfacePiece.isPad) {
          return EventType.PAD;
        }
        if (bridge.interfacePiece.isSlider) {
          return EventType.SLIDER;
        }
        return EventType.BUTTON;
      }
    }
    return null;
  }

  public ArduinoToDisplayBridge getBridgeForSensor(ArduinoSensor sensor) {
    for (ArduinoToDisplayBridge bridge : bridgeObjects) {
      if (bridge.contains(sensor)) {
        return bridge;
      }
    }
    return null;
  }
  
  public ArduinoToDisplayBridge getHellaSliderBridge() {
    for (ArduinoToDisplayBridge bridge : bridgeObjects) {
      if (bridge.isHellaSlider) {
        return bridge;
      }
    }
    return null;
  }
}
