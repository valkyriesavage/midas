package serialtalk;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;

import util.StorageJButton;

public class ArduinoTestWindow extends JFrame {

  private static final long serialVersionUID = 3277478961128936670L;

  private static ArduinoDispatcher dispatcher;
  
  public static void setDispatcher(ArduinoDispatcher newDispatcher) {
    dispatcher = newDispatcher;
  }
  
  public ArduinoTestWindow() {
    setSize(300,500);
    setLocation(900,0);
    setUpButtonsPerInput();
    setVisible(true);
  }
  
  public void setUpButtonsPerInput() {
    setLayout(new GridLayout(0,2));
    for (int i=0; i < 12; i++) {
      StorageJButton pinTouch = new StorageJButton("touch "+i);
      pinTouch.sensorData = ArduinoSetup.sensors[i];
      pinTouch.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent event) {
          ArduinoSensor activated = ((StorageJButton)event.getSource()).sensorData;
          ArduinoEvent touchEvent = new ArduinoEvent(activated, TouchDirection.TOUCH);
          dispatcher.handleEvent(touchEvent);
        }
      });
      /*
       * TODO: make this a reasonable thing to do.  right now these events are just deleted, anyway.
      StorageJButton pinRelease = new StorageJButton("release "+i);
      pinRelease.sensorData = ArduinoSetup.sensors[i];
      pinRelease.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent event) {
          ArduinoSensor activated = ((StorageJButton)event.getSource()).sensorData;
          ArduinoEvent releaseEvent = new ArduinoEvent(activated, TouchDirection.RELEASE);
          dispatcher.handleEvent(releaseEvent);
        }
      });*/
      
      add(pinTouch);
      //add(pinRelease);
    }
  }
}
