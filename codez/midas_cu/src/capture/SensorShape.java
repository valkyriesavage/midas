package capture;

import java.awt.Graphics;


public class SensorShape {

  public static final SensorShape[] shapes = {new SensorShape("-----"),
                                              new SensorShape("circle"), new SensorShape("square"),
                                              new SensorShape("star"), new SensorShape("slider"),
                                              new SensorShape("pad")};
  private String shape;

  private SensorShape(String shape) {this.shape = shape;}
  public String toString() {
    return shape;
  }
  public void draw(Graphics g) {}
}
