package display;

import java.awt.Graphics;


public class SensorShape {

  public static final SensorShape[] shapesList = {new SensorShape(),
                                                  new SensorShape(shapes.CIRCLE), new SensorShape(shapes.SQUARE),
                                                  new SensorShape(shapes.STAR), new SensorShape(shapes.SLIDER),
                                                  new SensorShape(shapes.PAD)};
  
  public static enum shapes {
    CIRCLE,
    SQUARE,
    STAR,
    SLIDER,
    PAD,
  }
  public shapes shape = null;

  private SensorShape() {}
  private SensorShape(shapes shape) {this.shape = shape;}
  public String toString() {
    return shape.name();
  }
}
