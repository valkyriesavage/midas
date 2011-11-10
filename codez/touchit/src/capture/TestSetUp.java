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
  
  List<Pair<List<ArduinoEvent>, UIAction>> l = new ArrayList<Pair<List<ArduinoEvent>, UIAction>>();

  public TestSetUp() throws AWTException {
    super();
    setSize(280, 500);
    List<ArduinoEvent> zeroUp = new ArrayList<ArduinoEvent>();
    zeroUp.add(new ArduinoEvent(0, TouchDirection.UP));
    List<ArduinoEvent> oneUp = new ArrayList<ArduinoEvent>();
    oneUp.add(new ArduinoEvent(1, TouchDirection.UP));
    List<ArduinoEvent> twoUp = new ArrayList<ArduinoEvent>();
    twoUp.add(new ArduinoEvent(2, TouchDirection.UP));
    List<ArduinoEvent> threeUp = new ArrayList<ArduinoEvent>();
    threeUp.add(new ArduinoEvent(3, TouchDirection.UP));
    
    l.add(new Pair<List<ArduinoEvent>, UIAction>(zeroUp, new KeyAction(KeyEvent.VK_H)));
    l.add(new Pair<List<ArduinoEvent>, UIAction>(oneUp, new KeyAction(KeyEvent.VK_E)));
    l.add(new Pair<List<ArduinoEvent>, UIAction>(twoUp, new KeyAction(KeyEvent.VK_L)));
    l.add(new Pair<List<ArduinoEvent>, UIAction>(threeUp, new KeyAction(KeyEvent.VK_L)));
    l.add(new Pair<List<ArduinoEvent>, UIAction>(zeroUp, new KeyAction(KeyEvent.VK_O)));

    Container contentPane = getContentPane();
    contentPane.setLayout(new FlowLayout());
    
    JButton startB = new JButton("Set a bunch of useless defaults!");
    startB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent A) {
        for(Pair<List<ArduinoEvent>, UIAction> p: l) {
          serialCommunication.registerSerialEvent((List<ArduinoEvent>)p.getLeft(),
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
