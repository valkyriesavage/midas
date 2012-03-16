package display;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.ImageObserver;
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
  Image customImage = null;
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
  
  public ArduinoSensorButton(SensorShape.shapes shape, Point upperLeft, int size) {
    this.shape = shape;
    this.upperLeft = upperLeft;
    this.size = size;
  }
  
  public ArduinoSensorButton(Image customImage, Point upperLeft, int size) {
    this.customImage = customImage;
    this.upperLeft = upperLeft;
    this.size = size;
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
    size -= SensorButtonGroup.SIZE_CHANGE;
    upperLeft.x += SensorButtonGroup.SIZE_CHANGE/2;
    upperLeft.y += SensorButtonGroup.SIZE_CHANGE/2;
  }
  public void larger() {
    size += SensorButtonGroup.SIZE_CHANGE;
    upperLeft.x -= SensorButtonGroup.SIZE_CHANGE/2;
    upperLeft.y -= SensorButtonGroup.SIZE_CHANGE/2;
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
    this.customImage = null;
  }
  
  public void changeImage(Image customImage) {
    this.customImage = customImage;
    this.shape = null;
  }
  
  public boolean locationChecked() {
    return locationChecked;
  }
  
  public void paint(Graphics2D g) {
    if (shape != null) {
      Shape drawShape = getShape();
      g.setColor(relevantColor);
      g.fill(drawShape);
    } else if (customImage != null) {
      g.drawImage(customImage, upperLeft.x, upperLeft.y, null, new ImageObserver() {
        public boolean imageUpdate(Image img, int infoflags, int x, int y,
            int width, int height) {
          return false;
        }
      });
    }
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
    return new Ellipse2D.Double(upperLeft.x - size/2, upperLeft.y - size/2, size, size);
  }
  
  private Shape square() {
    return new Rectangle2D.Double(upperLeft.x - size/2, upperLeft.y - size/2, size, size);
  }
  
  private Shape star() {
    return new StarPolygon(upperLeft.x, upperLeft.y, size, (int)(size*.5), 5);
  }
  
  private Shape slider() {
    return square();
  }

  private Shape pad() {
    return square();
  }

  @Override
  public boolean contains(Point p) {
    if (shape == null && customImage == null) {
      return false;
    }
    if (shape != null) {
      return getShape().contains(p);
    }
    return (p.x > upperLeft.x && p.x < upperLeft.x + customImage.getWidth(null) &&
            p.y > upperLeft.y && p.y < upperLeft.y + customImage.getHeight(null));
  }
  
  public void moveTo(Point upperLeft) {
    this.upperLeft = upperLeft;
  }
  
  @Override
  public void setSelected(boolean selected) {
    if(!selected) {
      relevantColor = CanvasPanel.COPPER;
    } else {
      relevantColor = Color.GREEN;
    }
  }
  
  public void setIntersecting(boolean intersecting) {
    if(intersecting) {
      relevantColor = Color.RED;
    }
  }
  
  public boolean intersects(Rectangle rectangle) {
    return getShape().getBounds().intersects(rectangle);
  }
  
  @Override
  public Rectangle getBounds() {
    Rectangle bounds = getShape().getBounds();
    bounds.grow(SensorButtonGroup.BUFFER, SensorButtonGroup.BUFFER);
    return bounds;
  }
}
