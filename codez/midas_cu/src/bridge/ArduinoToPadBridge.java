package bridge;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;

import serialtalk.ArduinoObject;
import serialtalk.ArduinoPad;
import serialtalk.ArduinoSensor;
import capture.UIPad;
import display.SensorButtonGroup;
import display.SetUp;

public class ArduinoToPadBridge extends ArduinoToDisplayBridge {
  
  private static final ArduinoPad nullPad = new ArduinoPad(new ArduinoSensor[0][0]);
  
  public ArduinoObject arduinoPiece = nullPad;
  public UIPad interactivePiece;
  
  public Integer sensitivity;
  JComboBox padSensitivity = new JComboBox(SetUp.PAD_SENSITIVITIES);
    
  public ArduinoToPadBridge(int sensitivity) {
    this.sensitivity = sensitivity;
    interactivePiece = new UIPad(sensitivity);
    
    padSensitivity.addActionListener(new ActionListener() {
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
    ((ArduinoPad)this.arduinoPiece).setSensitivity(sensitivity);
  }
  
  public void setInterfacePiece(SensorButtonGroup interfacePiece) {
    this.interfacePiece = interfacePiece;
    interfacePiece.isPad = true;
    this.interfacePiece.setSensitivity(this.sensitivity);
  }
  
  public JButton capturePadButton() {
    JButton capturePad = new JButton(interactivePiece.toString());
    capturePad.addActionListener(new ActionListener() {
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
    return capturePad;
  }
  
  public JComboBox padSensitivityBox() {
    return padSensitivity;
  }
  
  public JButton showTestPositionsButton() {
    JButton show = new JButton("test positions");
    show.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        for(int i=(int)Math.sqrt(sensitivity)-1; i>=0; i--) {
          for(int j=0; j<Math.sqrt(sensitivity); j++) {
            interactivePiece.execute(new Point(j, i));
          }
        }
      }
    });
    return show;
  }
  
  public void execute(ArduinoSensor sensor) {
    interactivePiece.execute(((ArduinoPad)arduinoPiece).locationOnPad(sensor));
  }
}
