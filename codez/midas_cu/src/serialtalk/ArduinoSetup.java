package serialtalk;

import java.util.ArrayList;
import java.util.List;

public abstract class ArduinoSetup {
  public static ArduinoSensor[][] sensors = new ArduinoSensor[12][12];
  
  public static List<ArduinoSlider> sliders = new ArrayList<ArduinoSlider>();
  
  private ArduinoSetup() {}
  
  public static void initialize() {
    if (sensors == null) {
      for(int i=0; i<12; i++) {
        for(int j=0; j<12; j++) {
          sensors[i][j] = new ArduinoSensor(i, j);
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
}