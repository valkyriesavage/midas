package display;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

public class SVGPathwaysGenerator {
  
  private List<SensorConnector> sensorConnectors = new ArrayList<SensorConnector>();

  public SVGPathwaysGenerator() {}
  
  public void paint(Graphics2D g) {
    for (SensorConnector connector : sensorConnectors) {
      connector.paint(g);
    }
  }
  
  public void generatePathways(List<SensorButtonGroup> buttonsToConnect) {
    //TODO
    System.out.println("we would be generating pathways now.  :)");
  }
}
