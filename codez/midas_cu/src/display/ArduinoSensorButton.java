package display;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.Random;

import javax.swing.JButton;

import serialtalk.ArduinoDispatcher;
import serialtalk.ArduinoSensor;
import sl.shapes.StarPolygon;

public class ArduinoSensorButton extends JButton {
  private static final long serialVersionUID = -5603499266721585353L;
  private static ArduinoDispatcher dispatcher;
  
  public ArduinoSensor sensor;
  SensorShape.shapes shape = null;
  boolean locationChecked = false;
  
  private Point upperLeft;
  private int size;
  
  private Color relevantColor = CanvasPanel.COPPER;
  
  public static void setDispatcher(ArduinoDispatcher newDispatcher) {
    dispatcher = newDispatcher;
  }
  
  public ArduinoSensorButton(SensorShape.shapes shape) {
    this.shape = shape;

    Random random = new Random();
    upperLeft = new Point(random.nextInt(SetUp.CANVAS_X - 50), random.nextInt(SetUp.CANVAS_Y - 50));
    size = (random.nextInt(10)+4) * 8;
  }
  
  public void setSensor(ArduinoSensor sensor) {
    this.sensor = sensor;
  }
  
  public ArduinoSensor getSensor() {
    return sensor;
  }
  
  public void rotateLeft() {
    
  }
  public void rotateRight() {
    
  }
  public void smaller() {
    size -= 4;
    upperLeft.x += 2;
    upperLeft.y += 2;
  }
  public void larger() {
    size += 4;
    upperLeft.x -= 2;
    upperLeft.y -= 2;
  }
  
  public void activate() {
    relevantColor = Color.PINK;
    dispatcher.handleFakeEvent(this);
  }

  public void deactivate() {
    relevantColor = CanvasPanel.COPPER;
  }
  
  public void changeShape(SensorShape.shapes newShape) {
    this.shape = newShape;
  }
  public boolean locationChecked() {
    return locationChecked;
  }
  
  public void paint(Graphics2D g) {
    Shape drawShape = getShape();
    g.setColor(relevantColor);
    g.fill(drawShape);
  }
  
  private Shape getShape() {
    if (shape.equals(SensorShape.shapes.CIRCLE)) {
      return circle();
    } if (shape.equals(SensorShape.shapes.STAR)) {
      return star();
    } if (shape.equals(SensorShape.shapes.SLIDER)) {
      return slider();
    } if (shape.equals(SensorShape.shapes.PAD)) {
      return pad();
    }
    return square();
  }
  
  private Shape circle() {
    return new Ellipse2D.Double(upperLeft.x, upperLeft.y, size, size);
  }
  
  private Shape square() {
    return new Rectangle2D.Double(upperLeft.x, upperLeft.y, size, size);
  }
  
  private Shape star() {
    return new StarPolygon(upperLeft.x, upperLeft.y, size, (int)(size*.6), 5);
  }
  
  private Shape slider() {
    //TODO
    return square();
  }

  private Shape pad() {
    //TODO
    return square();
  }

  @Override
  public boolean contains(Point p) {
    return getShape().contains(p);
  }
  
  public void moveTo(Point upperLeft) {
    this.upperLeft = upperLeft;
  }
}
