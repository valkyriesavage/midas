package serialtalk;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class ArduinoSetup {
  public static ArduinoSensor[][] gridSensors = new ArduinoSensor[12][12];
  public static ArduinoSensor[] sensors = new ArduinoSensor[12];
  
  public static List<ArduinoSlider> sliders = new ArrayList<ArduinoSlider>();
  public static List<ArduinoPad> pads = new ArrayList<ArduinoPad>();
  
  public static void initialize(boolean test) {
    if (gridSensors[0][0] == null) {
      for(int i=0; i<12; i++) {
        for(int j=0; j<12; j++) {
          gridSensors[i][j] = new ArduinoSensor(i, j);
        }
      }
    }
    if (sensors[0] == null) {
      for(int i=0; i<12; i++) {
        sensors[i] = new ArduinoSensor(i, -1);
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
    for (int i=0; i<12; i++) {
      if (sensors[i] == sensor) {
        return i;
      }
    }
    return -1;
  }
  
  public Point whichGridSensor(ArduinoSensor sensor) {
    for (int i=0; i<12; i++) {
      for (int j=0; j<12; j++) {
        if (gridSensors[i][j] == sensor) {
          return new Point(i, j);
        }
      }
    }
    return new Point(-1,-1);
  }
}
