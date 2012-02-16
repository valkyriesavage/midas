package bridge;

import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import serialtalk.ArduinoObject;
import serialtalk.ArduinoSensor;
import serialtalk.ArduinoSlider;
import capture.UISlider;
import display.SensorButtonGroup;

public class ArduinoToSliderBridge implements ArduinoToDisplayBridge {
  
  private static final ArduinoSlider nullSlider = new ArduinoSlider(new ArduinoSensor[0]);
  
  public SensorButtonGroup interfacePiece;
  public ArduinoObject arduinoPiece = nullSlider;
  public UISlider interactivePiece;
  
  public Integer sensitivity = 1;
    
  public ArduinoToSliderBridge(int sensitivity) {
    this.sensitivity = sensitivity;
    interactivePiece = new UISlider(sensitivity);
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
          ((JButton)event.getSource()).setText(interactivePiece.toString() + " (change)");
        }
      }
    });
    return captureSlider;
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
