package serialtalk;

import java.util.ArrayList;
import java.util.List;

public abstract class ArduinoSetup {
  public static ArduinoSensor[] sensors = {new ArduinoSensor(0,0), new ArduinoSensor(1,0), 
                                           new ArduinoSensor(2,0), new ArduinoSensor(3,0),
                                           new ArduinoSensor(4,0), new ArduinoSensor(5,0),
                                           new ArduinoSensor(6,0), new ArduinoSensor(7,0),
                                           new ArduinoSensor(8,0), new ArduinoSensor(9,0),
                                           new ArduinoSensor(10,0), new ArduinoSensor(11,0)};
  
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
