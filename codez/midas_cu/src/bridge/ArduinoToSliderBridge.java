package bridge;

import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;

import serialtalk.ArduinoObject;
import serialtalk.ArduinoSensor;
import serialtalk.ArduinoSlider;
import capture.UISlider;
import display.SensorButtonGroup;
import display.SetUp;

public class ArduinoToSliderBridge implements ArduinoToDisplayBridge {
  
  private static final ArduinoSlider nullSlider = new ArduinoSlider(new ArduinoSensor[0]);
  
  public SensorButtonGroup interfacePiece;
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
  
  public ArduinoObject arduinoPiece() {
    return arduinoPiece;
  }
  
  public SensorButtonGroup interfacePiece() {
    return interfacePiece;
  }
  
  public void paint(Graphics2D g) {
    interfacePiece.paint(g);
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
}
