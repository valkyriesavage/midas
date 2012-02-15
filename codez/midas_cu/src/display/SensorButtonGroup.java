package display;

import java.awt.BorderLayout;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

public class SensorButtonGroup extends JPanel {
  private static final long serialVersionUID = -3154036436928212098L;
  private ArduinoSensorButton triggerButton;
  private JButton rotateLeft = new JButton("<");
  private JButton rotateRight = new JButton(">");
  private JButton larger = new JButton("+");
  private JButton smaller = new JButton("-");
  private JButton delete = new JButton("x");
  private JButton nameIt = new JButton("set name");
  private JCheckBox verified = new JCheckBox("location?");
  
  public String name;
  
  public boolean deleteMe = false;
  public boolean isSlider;
  public boolean isPad;
  public int sensitivity = 1; // TODO (xiaohan) : sensitivity here means the number of buttons that make it up
  
  public SensorButtonGroup(SensorShape.shapes shape) {
    triggerButton = new ArduinoSensorButton(shape);
    isSlider = (shape == SensorShape.shapes.SLIDER);
    isPad = (shape == SensorShape.shapes.PAD);
    
    setLayout(new BorderLayout());
    
    initializeButtons();
    
    add(nameIt, BorderLayout.NORTH);
    add(triggerButton, BorderLayout.CENTER);
  
    JPanel turnAndSize = new JPanel();
    turnAndSize.setLayout(new GridLayout(2,2));
    turnAndSize.add(rotateLeft);
    turnAndSize.add(rotateRight);
    turnAndSize.add(smaller);
    turnAndSize.add(larger);
    add(turnAndSize, BorderLayout.EAST);
    
    JPanel verifyAndDeletePanel = new JPanel();
    verifyAndDeletePanel.add(verified);
    verifyAndDeletePanel.add(delete);
    add(verifyAndDeletePanel, BorderLayout.SOUTH);
    
    name = shape.name().toLowerCase();
  }
  
  public void setSensitivity(int sensitivity) {
    this.sensitivity = sensitivity;
    name = name + "(" + sensitivity + ")";
  }
  
  private void initializeButtons() {
    verified.setEnabled(false);
    rotateLeft.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        triggerButton.rotateLeft();
      }
    });
    rotateRight.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        triggerButton.rotateRight();
      }
    });
    smaller.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        triggerButton.smaller();
      }
    });
    larger.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        triggerButton.larger();
      }
    });
    delete.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        setVisible(false);
        
        deleteMe = true;
      }
    });
  }
  
  public void paint(Graphics2D g) {
    if (!deleteMe) {
      triggerButton.paint(g);
    }
  }
  
  public String toString() {
    return name;
  }
}
