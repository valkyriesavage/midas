package serialtalk;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import util.StorageJButton;

public class ArduinoTestWindow extends JFrame {

  private static final long serialVersionUID = 3277478961128936670L;

  private ArduinoDispatcher dispatcher;
  
  public void setDispatcher(ArduinoDispatcher newDispatcher) {
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
    for (int i=0; i < ArduinoSetup.NUM_TERMINALS; i++) {
      StorageJButton pinTouch = new StorageJButton("touch "+i);
      pinTouch.sensorData = ArduinoSetup.sensors[i];
      pinTouch.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent event) {
          ArduinoSensor activated = ((StorageJButton)event.getSource()).sensorData;
          ArduinoEvent touchEvent = new ArduinoEvent(activated, TouchDirection.TOUCH);
          dispatcher.handleEvent(touchEvent);
        }
      });
      StorageJButton pinRelease = new StorageJButton("release "+i);
      pinRelease.sensorData = ArduinoSetup.sensors[i];
      pinRelease.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent event) {
          ArduinoSensor activated = ((StorageJButton)event.getSource()).sensorData;
          ArduinoEvent releaseEvent = new ArduinoEvent(activated, TouchDirection.RELEASE);
          dispatcher.handleEvent(releaseEvent);
        }
      });
      
      add(pinTouch);
      add(pinRelease);
    }
    JSlider hellaSlider = new JSlider(0, 255);
    hellaSlider.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent event) {
        JSlider hellaSlider = (JSlider)event.getSource();
        int value = hellaSlider.getValue();
        dispatcher.handleFakeEvent(value);
      }
    });
    add(hellaSlider);
  }
}
