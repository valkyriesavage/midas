package bridge;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;

import serialtalk.ArduinoEvent;
import serialtalk.ArduinoObject;
import serialtalk.ArduinoSensor;
import serialtalk.ArduinoSlider;
import capture.UISlider;
import display.SensorButtonGroup;
import display.SetUp;

public class ArduinoToSliderBridge extends ArduinoToDisplayBridge {
  
  private static final ArduinoSlider nullSlider = new ArduinoSlider(new ArduinoSensor[0]);
  
  public ArduinoObject arduinoPiece = nullSlider;
  public UISlider interactivePiece;
  
  public Integer sensitivity;
  private JComboBox sliderSensitivity = new JComboBox(SetUp.SLIDER_SENSITIVITIES);
    
  public ArduinoToSliderBridge(int sensitivity) {
    this.sensitivity = sensitivity;
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
    JButton captureSlider = new JButton(interactivePiece.toString());
    captureSlider.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        if (!interactivePiece.isRecording) {
          interactivePiece.record();
          ((JButton)event.getSource()).setText("stop recording");
        } else {
          interactivePiece.stopRecording();
          ((JButton)event.getSource()).setText(interactivePiece.toString());
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
  
  public void setSequence(List<ArduinoEvent> events) {
    // for a slider, we want to 
  }
}
