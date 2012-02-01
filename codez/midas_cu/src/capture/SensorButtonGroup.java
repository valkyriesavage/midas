package capture;

import java.awt.BorderLayout;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import util.ImageIconUtil;

public class SensorButtonGroup extends JPanel {
  private static final long serialVersionUID = -3154036436928212098L;
  private ArduinoSensorButton triggerButton;
  private JButton rotateLeft = new JButton("<");
  private JButton rotateRight = new JButton(">");
  private JButton larger = new JButton("+");
  private JButton smaller = new JButton("-");
  private JButton delete = new JButton("x");
  private JButton name = new JButton("set name");
  private JCheckBox verified = new JCheckBox("location?");
  
  public boolean deleteMe = false;
  
  public SensorButtonGroup(String shape) {
    Icon icon = ImageIconUtil.createImageIcon("/images/"+shape+".png", shape);
    triggerButton = new ArduinoSensorButton(icon);
    this.setLayout(new BorderLayout());
    
    initializeButtons();
    
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
    triggerButton.paint(g);
  }
  
  public String getName() {
    return triggerButton.name;
  }
  public void setName(String name) {
    triggerButton.name(name);
  }
}
