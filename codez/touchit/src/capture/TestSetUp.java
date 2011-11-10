package capture;

import java.awt.AWTException;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;

import serialtalk.ArduinoEvent;
import serialtalk.TouchDirection;
import util.Pair;

public class TestSetUp extends SetUp {
  
  @SuppressWarnings("unchecked")
  List<Pair> l = new ArrayList<Pair>();

  public TestSetUp() throws AWTException {
    super();
    setSize(280, 500);
    
    l.add(new Pair<ArduinoEvent, UIAction>(new ArduinoEvent(0, TouchDirection.UP), new KeyAction(KeyEvent.VK_H)));
    l.add(new Pair<ArduinoEvent, UIAction>(new ArduinoEvent(1, TouchDirection.UP), new KeyAction(KeyEvent.VK_E)));
    l.add(new Pair<ArduinoEvent, UIAction>(new ArduinoEvent(2, TouchDirection.UP), new KeyAction(KeyEvent.VK_L)));
    l.add(new Pair<ArduinoEvent, UIAction>(new ArduinoEvent(6, TouchDirection.UP), new KeyAction(KeyEvent.VK_L)));
    l.add(new Pair<ArduinoEvent, UIAction>(new ArduinoEvent(0, TouchDirection.UP), new KeyAction(KeyEvent.VK_O)));

    Container contentPane = getContentPane();
    contentPane.setLayout(new FlowLayout());
    
    JButton startB = new JButton("Set a bunch of useless defaults!");
    startB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent A) {
        for(Pair<ArduinoEvent, UIAction> p: l) {
          serialCommunication.registerSerialEvent((ArduinoEvent)p.getLeft(),
                                                  (UIAction)p.getRight());
          listOfThingsHappening.setText(serialCommunication.listOfRegisteredEvents());
        }
      }
    });

    for(int i=0; i<12; i++) {
      JButton touchi = new JButton("touch " + i);
      JButton untouchi = new JButton("release " + i);
      touchi.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent A) {
          serialCommunication.handleEvent_forTestingOnly(new ArduinoEvent(Integer.parseInt((((JButton)A.getSource()).getText().split(" ")[1])), TouchDirection.DOWN));
        }
      });
      untouchi.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent A) {
          serialCommunication.handleEvent_forTestingOnly(new ArduinoEvent(Integer.parseInt((((JButton)A.getSource()).getText().split(" ")[1])), TouchDirection.UP));
        }
      });
      contentPane.add(touchi);
      contentPane.add(untouchi);
    }

    contentPane.add(startB);
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
