package display;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

public class SVGPathwaysGenerator {
  
  private List<SensorConnector> sensorConnectors = new ArrayList<SensorConnector>();

  public SVGPathwaysGenerator(List<SensorButtonGroup> displayedButtons) {}
  
  public void paint(Graphics2D g) {
    for (SensorConnector connector : sensorConnectors) {
      connector.paint(g);
    }
  }
  
  public void generatePathways(List<SensorButtonGroup> buttonsToConnect) {
    List<ArduinoSensorButton> buttons = new ArrayList<ArduinoSensorButton>();
    
    for(SensorButtonGroup sbg : buttonsToConnect) {
      for(ArduinoSensorButton button : sbg.triggerButtons) {
        buttons.add(button);
      }
    }
    
    // here !!!
  }
}
