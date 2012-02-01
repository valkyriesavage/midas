package capture;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import serialtalk.ArduinoSensor;

public class SensorButtonGroup extends JPanel {
  private static final long serialVersionUID = -3154036436928212098L;
  private ArduinoJButton triggerButton;
  private JButton rotateLeft = new JButton("rotate left");
  private JButton rotateRight = new JButton("rotate right");
  private JButton larger = new JButton("+");
  private JButton smaller = new JButton("-");
  private JButton delete = new JButton("x");
  private JButton name = new JButton("set name");
  private JCheckBox verified = new JCheckBox("location?");
  
  public SensorButtonGroup(Icon icon) {
    triggerButton = new ArduinoJButton(icon);
    this.setLayout(new BorderLayout());
    
    add(name, BorderLayout.NORTH);
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
    this.add(verifyAndDeletePanel, BorderLayout.SOUTH);
    
  }
  
  public String getName() {
    return triggerButton.name;
  }
  public void setName(String name) {
    triggerButton.name(name);
  }
}
