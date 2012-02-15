package display;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.Random;

import javax.swing.JButton;

import serialtalk.ArduinoEvent;
import serialtalk.ArduinoSensor;
import serialtalk.TouchDirection;
import sl.shapes.StarPolygon;

public class ArduinoSensorButton extends JButton {
  private static final long serialVersionUID = -5603499266721585353L;
  public ArduinoSensor sensor;
  SensorShape.shapes shape = null;
  boolean locationChecked = false;
  
  private Point upperLeft;
  private int size;
  
  public ArduinoSensorButton(SensorShape.shapes shape) {
    this.shape = shape;
    addMouseListener(new MouseListener() {
      public void mousePressed(MouseEvent event) {
        ArduinoEvent triggered = new ArduinoEvent(((ArduinoSensorButton)event.getComponent()).sensor,
                                                  TouchDirection.RELEASE);
        SetUp.serialCommunication.handleCompleteEvent(triggered);
        activate();
      }

      public void mouseReleased(MouseEvent event) {
        ArduinoEvent triggered = new ArduinoEvent(((ArduinoSensorButton)event.getComponent()).sensor,
                                                  TouchDirection.TOUCH);
        SetUp.serialCommunication.handleCompleteEvent(triggered);
        deactivate();
      }
      
      public void mouseClicked(MouseEvent event) {
        mousePressed(event);
        mouseReleased(event);
      }

      public void mouseEntered(MouseEvent event) {
        //TODO:  consider whether hover here should be doing something nice like highlighting the appropriate interaction line on the right
      }
      public void mouseExited(MouseEvent event) {
        deactivate();
      }

    });
    Random random = new Random();
    upperLeft = new Point(random.nextInt(SetUp.CANVAS_X), random.nextInt(SetUp.CANVAS_Y));
    size = random.nextInt(10) * 10;
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
    setBackground(Color.orange);
  }
  public void deactivate() {
    setBackground(null);
  }
  
  public void changeShape(SensorShape.shapes newShape) {
    this.shape = newShape;
  }
  public boolean locationChecked() {
    return locationChecked;
  }
  
  public void paint(Graphics2D g) {
    Shape drawShape = getShape();
    g.setPaint(Color.gray);
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
    return new Ellipse2D.Double(upperLeft.x, upperLeft.y,size,size);
  }
  
  private Shape square() {
    return new Rectangle2D.Double(upperLeft.x, upperLeft.y,size,size);
  }
  
  private Shape star() {
    //I have no freaking clue why I have to put int int there.
    return new StarPolygon(upperLeft.x, upperLeft.y, size, (int)((int)size*.6), 5);
  }
  
  private Shape slider() {
    //TODO
    return square();
  }
  
  private Shape pad() {
    //TODO
    return square();
  }
}
