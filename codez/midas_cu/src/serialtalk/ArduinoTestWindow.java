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
  
  private ArduinoSensor previous;
  
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
          if (ArduinoSetup.griddedSensors.contains(activated.location.x)) {
            if (previous != null) {
              ArduinoEvent touchEvent = new ArduinoEvent(ArduinoSetup.gridSensors[previous.location.x][activated.location.x], TouchDirection.TOUCH);
              dispatcher.handleEvent(touchEvent);
              // this... is a horrible hack.  :( we don't know what order we will get the sensors; x-first or y-first...
              touchEvent = new ArduinoEvent(ArduinoSetup.gridSensors[activated.location.x][previous.location.x], TouchDirection.TOUCH);
              dispatcher.handleEvent(touchEvent);
              previous = null;
            } else {
              previous = activated;
            }
          } else {
            ArduinoEvent touchEvent = new ArduinoEvent(activated, TouchDirection.TOUCH);
            dispatcher.handleEvent(touchEvent);
          }
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
        if (!hellaSlider.getValueIsAdjusting()) {
          int value = hellaSlider.getValue();
          dispatcher.handleEvent(new ArduinoEvent(value, TouchDirection.TOUCH));
          //dispatcher.handleEvent(new ArduinoEvent(value, TouchDirection.RELEASE));
        }
      }
    });
    add(hellaSlider);
  }
}
