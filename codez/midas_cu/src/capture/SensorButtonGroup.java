package capture;

import java.awt.BorderLayout;

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
  private JButton delete = new JButton("x");
  private JButton name = new JButton("set name");
  private JCheckBox verified = new JCheckBox("Verified location?");
  
  public SensorButtonGroup(Icon icon, ArduinoSensor sensor) {
    triggerButton = new ArduinoJButton(icon, sensor);
    this.setLayout(new BorderLayout());
    this.add(name, BorderLayout.NORTH);
    this.add(rotateLeft, BorderLayout.WEST);
    this.add(triggerButton, BorderLayout.CENTER);
    this.add(rotateRight, BorderLayout.EAST);
    
    JPanel verifyAndDeletePanel = new JPanel();
    verifyAndDeletePanel.add(verified);
    verifyAndDeletePanel.add(delete);
    this.add(verifyAndDeletePanel, BorderLayout.SOUTH);
    
    setButtonActions();
  }
  
  private void setButtonActions() {
    
  }
}
