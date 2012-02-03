package display;

import java.awt.Point;

public class ConnectorJunction {
  
  public Point location;
  public boolean connectRight;
  public boolean connectLeft;
  public boolean connectDown;
  public boolean connectUp;
  
  public ConnectorJunction(Point location, boolean connectRight, boolean connectLeft, boolean connectUp, boolean connectDown) {
    this.location = location;
    this.connectRight = connectRight;
    this.connectLeft = connectLeft;
    this.connectUp = connectUp;
    this.connectDown = connectDown;
  }
}
