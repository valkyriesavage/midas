package serialtalk;

import java.util.ArrayList;
import java.util.List;

public abstract class ArduinoSetup {
  public static ArduinoSensor[] sensors = {new ArduinoSensor(0), new ArduinoSensor(1), 
                                           new ArduinoSensor(2), new ArduinoSensor(3),
                                           new ArduinoSensor(4), new ArduinoSensor(5),
                                           new ArduinoSensor(6), new ArduinoSensor(7),
                                           new ArduinoSensor(8), new ArduinoSensor(9),
                                           new ArduinoSensor(10), new ArduinoSensor(11)};
  
  public static List<ArduinoSlider> sliders = new ArrayList<ArduinoSlider>();
  
  private ArduinoSetup() {}
  
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
