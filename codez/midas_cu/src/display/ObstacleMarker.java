package display;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.util.List;

public class ObstacleMarker {
  
  public static final int REASONABLE_DISTANCE_TO_CLOSE_OBSTACLE = 7;
  private static final Color OBSTACLE_COLOR = Color.GRAY;
  
  private Polygon myShape = new Polygon();
  
  public ObstacleMarker(List<Point> vertices) {
    for (Point p : vertices) {
      myShape.addPoint(p.x, p.y);
    }
  }
  
  public void paint(Graphics2D g) {
    g.setColor(OBSTACLE_COLOR);
    g.fillPolygon(myShape);
  }
  
  // what else do we need to draw this into the SVGPathwaysGenerator?
}
