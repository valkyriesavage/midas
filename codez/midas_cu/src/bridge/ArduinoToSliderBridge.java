package bridge;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;

import serialtalk.ArduinoEvent;
import serialtalk.ArduinoSensor;
import serialtalk.ArduinoSetup;
import serialtalk.ArduinoSlider;
import capture.UISlider;
import display.ArduinoSensorButton;
import display.SensorButtonGroup;
import display.SetUp;

public class ArduinoToSliderBridge extends ArduinoToDisplayBridge {
  
  private static final ArduinoSlider nullSlider = new ArduinoSlider(new ArduinoSensor[0]);
  
  public UISlider interactivePiece;
  
  public Integer sensitivity;
  private JComboBox sliderSensitivity = new JComboBox(SetUp.SLIDER_SENSITIVITIES);
    
  public ArduinoToSliderBridge(int sensitivity) {
    this.sensitivity = sensitivity;
    arduinoPiece = nullSlider;
    interactivePiece = new UISlider(sensitivity);
    
    sliderSensitivity.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        JComboBox sensitivityBox = (JComboBox)event.getSource();
        Integer newSensitivity = (Integer) sensitivityBox.getSelectedItem();
        setSensitivity(newSensitivity);
      }
    });
  }
    
  public String toString() {
    return interfacePiece.name;
  }
  
  public void setSensitivity(Integer sensitivity) {
    this.sensitivity = sensitivity;
    this.interactivePiece.sensitivity = sensitivity;
    this.interfacePiece.setSensitivity(sensitivity);
  }
  
  public void setInterfacePiece(SensorButtonGroup interfacePiece) {
    this.interfacePiece = interfacePiece;
    interfacePiece.isSlider = true;
    this.interfacePiece.setSensitivity(this.sensitivity);
  }
  
  public JButton captureSliderButton() {
    JButton captureSlider = new JButton(interactivePiece.icon());
    captureSlider.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        if (!interactivePiece.isRecording) {
          JOptionPane.showMessageDialog(null, "click at the top and bottom or\nleft and right ends of the slider",
              "slider capture instructions", JOptionPane.INFORMATION_MESSAGE);
          interactivePiece.record();
          ((JButton)event.getSource()).setText("stop recording");
        } else {
          interactivePiece.stopRecording();
          ((JButton)event.getSource()).setText("");
          ((JButton)event.getSource()).setIcon(interactivePiece.icon());
        }
      }
    });
    return captureSlider;
  }
  
  public JComboBox sliderSensitivityBox() {
    return sliderSensitivity;
  }
  
  public JButton showTestPositionsButton() {
    JButton show = new JButton("test positions");
    show.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        for(int i=0; i<sensitivity; i++) {
          interactivePiece.execute(i);
        }
      }
    });
    return show;
  }
  
  public void execute(ArduinoSensor sensor) {
    interactivePiece.execute(((ArduinoSlider)arduinoPiece).whichInSlider(sensor));
  }
  
  public void execute(ArduinoSensorButton button) {
    if(this.contains(button)) {
      // TODO !!
      // I think we need to split up sensorbuttons into multiple sensors so that this is a bit easier...      
    }
  }

  @Override
  public void setArduinoSequence(List<ArduinoEvent> events) {
    //for a slider, we want t->b
    List<ArduinoSensor> sensors = new ArrayList<ArduinoSensor>();
    for (ArduinoEvent e : events) {
      if(!sensors.contains(e.whichSensor)) {
        sensors.add(e.whichSensor);
      }
    }
    if(sensors.size() != sensitivity.intValue()) {
      System.out.println("wrong number of sensors registered");
      return;
    }    
    arduinoPiece = new ArduinoSlider(sensors);
    ArduinoSetup.addSlider((ArduinoSlider)arduinoPiece);
  }
}
