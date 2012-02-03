package display;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

public class SensorConnector {
  
  private List<ConnectorJunction> junctions = new ArrayList<ConnectorJunction>();
  
  public static final int CONNECTOR_WIDTH = 3;

  public SensorConnector() {}
  
  public void paint(Graphics2D g) {
    g.setPaint(Color.gray);
    for (ConnectorJunction junction : junctions) {
      g.drawRect(junction.location.x, junction.location.y, CONNECTOR_WIDTH, CONNECTOR_WIDTH);
    }
  }
}
