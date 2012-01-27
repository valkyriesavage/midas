package capture;

import java.awt.AWTException;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;

import serialtalk.ArduinoEvent;
import serialtalk.ArduinoSetup;
import serialtalk.TouchDirection;

public class TestSetUp extends SetUp {

  JFrame arduinoButtons = new JFrame();

  public TestSetUp() throws AWTException {
    super();

    arduinoButtons.setSize(280, 400);
    arduinoButtons.setLayout(new FlowLayout());
    arduinoButtons.setLocation(500, 20);

    for (int i = 0; i < 12; i++) {
      JButton touchi = new JButton("touch " + i);
      JButton untouchi = new JButton("release " + i);
      touchi.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent A) {
          ArduinoEvent event = new ArduinoEvent(ArduinoSetup.sensors[Integer.parseInt((((JButton) A
              .getSource()).getText().split(" ")[1]))], TouchDirection.DOWN);
          serialCommunication.handleCompleteEvent(event);
        }
      });
      untouchi.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent A) {
          ArduinoEvent event = new ArduinoEvent(ArduinoSetup.sensors[Integer.parseInt((((JButton) A
              .getSource()).getText().split(" ")[1]))], TouchDirection.UP);
          serialCommunication.handleCompleteEvent(event);
        }
      });
      arduinoButtons.add(touchi);
      arduinoButtons.add(untouchi);
    }

    arduinoButtons.setVisible(true);
  }

  public static void main(String[] args) {
    TestSetUp setup;
    try {
      setup = new TestSetUp();
    } catch (AWTException e) {
      e.printStackTrace();
      return;
    }
    setup.setVisible(true);
  }
}
