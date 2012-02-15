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
import display.SensorShape;

public class ArduinoToSliderBridge implements ArduinoToDisplayBridge {
  
  private static final SensorButtonGroup nullInterface = new SensorButtonGroup(SensorShape.shapes.SQUARE);
  private static final ArduinoSlider nullSlider = new ArduinoSlider(new ArduinoSensor[0]);
  
  public SensorButtonGroup interfacePiece = nullInterface;
  public ArduinoObject arduinoPiece = nullSlider;
  public UISlider interactivePiece = new UISlider();
    
  public ArduinoToSliderBridge() {
    nullInterface.isSlider = true;
  }
    
  public String toString() {
    if (interfacePiece.name != null) {
      return interfacePiece.name;
    }
    return "unknown";
  }
  
  public ArduinoObject arduinoPiece() {
    return arduinoPiece;
  }
  
  public SensorButtonGroup interfacePiece() {
    return interfacePiece;
  }
  
  public void paint(Graphics2D g) {
    if (interfacePiece != nullInterface) {
      interfacePiece.paint(g);
    }
  }
  
  public void setInterfacePiece(SensorButtonGroup interfacePiece) {
    this.interfacePiece = interfacePiece;
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
}
