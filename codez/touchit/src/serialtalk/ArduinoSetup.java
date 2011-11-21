package serialtalk;

import java.util.ArrayList;
import java.util.List;

public class ArduinoSetup {
  public static ArduinoSensor[] sensors = {new ArduinoSensor(0), new ArduinoSensor(1), 
                                           new ArduinoSensor(2), new ArduinoSensor(3),
                                           new ArduinoSensor(4), new ArduinoSensor(5),
                                           new ArduinoSensor(6), new ArduinoSensor(7),
                                           new ArduinoSensor(8), new ArduinoSensor(9),
                                           new ArduinoSensor(10), new ArduinoSensor(11)};
  
  public static final List<ArduinoSlider> sliders = new ArrayList<ArduinoSlider>();
  
  private ArduinoSetup() {}
  
  public static void addSlider(ArduinoSlider slider) {
    sliders.add(slider);
  }
  
  public static ArduinoSlider isPartOfSlider(ArduinoSensor sensor) {
    for(int i=0; i<sliders.size(); i++) {
      if(sliders.get(i).isPartOfSlider(sensor)) {
        return sliders.get(i);
      }
    }
    return null;
  }
}
