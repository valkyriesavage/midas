package display;

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
    HELLA_SLIDER,
  }
  public shapes shape = null;

  private SensorShape() {}
  private SensorShape(shapes shape) {this.shape = shape;}
  public String toString() {
    if (shape == null) {
      return "------";
    }
    return shape.name();
  }
}
