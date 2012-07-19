package serialtalk;

import java.util.ArrayList;
import java.util.List;

public class ArduinoSetup {
  public static final int NUM_TERMINALS = 7;
  
  public static List<Integer> griddedSensors = new ArrayList<Integer>();
  public static ArduinoSensor[][] gridSensors = new ArduinoSensor[NUM_TERMINALS][NUM_TERMINALS];
  public static ArduinoSensor[] sensors = new ArduinoSensor[NUM_TERMINALS];
  
  public static List<ArduinoSlider> sliders = new ArrayList<ArduinoSlider>();
  public static List<ArduinoPad> pads = new ArrayList<ArduinoPad>();
  
  public static ArduinoSlider hellaSlider;
  
  public static void initialize(boolean test) {
    if (sensors[0] == null) {
      for(int i=0; i<NUM_TERMINALS; i++) {
        sensors[i] = new ArduinoSensor(i, -1);
      }
    } if (gridSensors[0][0] == null) {
      for(int i=0; i<NUM_TERMINALS; i++) {
        for(int j=0; j<NUM_TERMINALS; j++) {
          gridSensors[i][j] = new ArduinoSensor(i, j);
        }
      }
    }
  }
  
  public static void addSlider(ArduinoSlider slider) {
    sliders.add(slider);
  }
  
  public static ArduinoSlider isPartOfSlider(ArduinoSensor sensor) {
    for(ArduinoSlider slider : sliders) {
      if(slider.isPartOfSlider(sensor)) {
        return slider;
      }
    }
    return null;
  }
  
  public static void resetSliders() {
    sliders = new ArrayList<ArduinoSlider>();
  }
  
  public static void addPad(ArduinoPad pad) {
    pads.add(pad);
    griddedSensors.addAll(pad.terminals());
  }
  
  public static ArduinoPad isPartOfPad(ArduinoSensor sensor) {
    for(ArduinoPad pad : pads) {
      if(pad.isPartOfPad(sensor)) {
        return pad;
      }
    }
    return null;
  }
  
  public static void resetPads() {
    pads = new ArrayList<ArduinoPad>();
  }
  
  public int whichSensor(ArduinoSensor sensor) {
    for (int i=0; i<NUM_TERMINALS; i++) {
      if (sensors[i] == sensor) {
        return i;
      }
    }
    return -1;
  }
  
  public boolean isGridSensor(ArduinoSensor sensor) {
    return griddedSensors.contains(sensor);
  }
}
