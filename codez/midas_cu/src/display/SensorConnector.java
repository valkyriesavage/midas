package display;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public class SensorConnector {

  private ConnectorJunction start;

  public static final int CONNECTOR_WIDTH = 3;

  public SensorConnector(ConnectorJunction start) {
    this.start = start;
  }

  public void paint(Graphics2D g) {
    g.setPaint(Color.gray);
    paintJunction(g, start);
  }

  public void paintJunction(Graphics2D g, ConnectorJunction junction) {
    if (junction.isTerminal) {
      return;
    }
    if (junction.connectRight != null) {
      g.fill(new Rectangle(junction.location.x, junction.location.y,
                           CONNECTOR_WIDTH, (junction.connectRight.location.x - junction.location.x)));
      paintJunction(g, junction.connectRight);
    }
    if (junction.connectUp != null) {
      g.fill(new Rectangle(junction.location.x, junction.location.y,
                           (junction.connectRight.location.y - junction.location.y), CONNECTOR_WIDTH));
      paintJunction(g, junction.connectRight);
    }
  }
}
