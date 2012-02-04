package display;

import java.awt.Point;

public class ConnectorJunction {
  
  //note that we only move right and up, because if we were able to move in all 4 directions from each point
  //we would find ourselves in nasty infinite loops
  
  public Point location;
  public ConnectorJunction connectRight = null;
  public ConnectorJunction connectUp = null;
  
  public boolean isTerminal = true;
  
  public ConnectorJunction(Point location) {
    this.location = location;
  }
  
  public void connectJunctionRight(ConnectorJunction junction) {
    connectRight = junction;
  }
  public void connectJunctionUp(ConnectorJunction junction) {
    connectUp = junction;
  }
}
