package display;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JButton;

import serialtalk.ArduinoEvent;
import serialtalk.ArduinoSensor;
import serialtalk.TouchDirection;

public class ArduinoSensorButton extends JButton {
  private static final long serialVersionUID = -5603499266721585353L;
  public ArduinoSensor sensor;
  SensorShape.shapes shape = null;
  boolean locationChecked = false;
  
  private Point upperLeft;
  
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

      public void mouseEntered(MouseEvent event) {}
      public void mouseExited(MouseEvent event) {}

    });
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
    
  }
  public void larger() {
    
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
    g.translate(60, 0);
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
    return new Ellipse2D.Double(0,0,50,50);
  }
  
  private Shape square() {
    return new Rectangle2D.Double(0,0,50,50);
  }
  
  private Shape star() {
    //TODO
    return square();
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
