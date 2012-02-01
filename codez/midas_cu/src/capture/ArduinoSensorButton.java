package capture;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Ellipse2D;

import javax.swing.Icon;
import javax.swing.JButton;

import serialtalk.ArduinoEvent;
import serialtalk.ArduinoSensor;
import serialtalk.TouchDirection;

public class ArduinoSensorButton extends JButton {
  private static final long serialVersionUID = -5603499266721585353L;
  public ArduinoSensor sensor;
  String name = null;
  boolean locationChecked = false;
  
  public ArduinoSensorButton(Icon shape) {
    super(shape);
    this.name = shape.toString();
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
  
  public void name(String name) {
    this.name = name;
  }
  public boolean locationChecked() {
    return locationChecked;
  }
  
  public void paint(Graphics2D g) {
    Shape circle = new Ellipse2D.Double(0, 0, 50, 50);
    g.setPaint(Color.gray);
    g.fill(circle);
    g.translate(60, 0);
  }
}
