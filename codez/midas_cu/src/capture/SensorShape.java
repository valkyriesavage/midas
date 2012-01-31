package capture;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.geom.Point2D;

public class SensorShape {

  public static final SensorShape[] shapes = {new SensorShape("circle"), new SensorShape("square"),
                                              new SensorShape("star"), new SensorShape("slider", 2),
                                              new SensorShape("pad")};
  
  public String shape;
  public int sliderSensors = 0;
  public Point2D upperLeft;
  public Dimension size;
  
  private SensorShape(String shape) {this.shape = shape;}
  private SensorShape(String shape, int sliderSensors) {
    this.shape = shape;
    this.sliderSensors = sliderSensors;
  }
  
  public void draw(Graphics g) {
    
  }
  
  public String toString() {
    return shape;
  }
}
