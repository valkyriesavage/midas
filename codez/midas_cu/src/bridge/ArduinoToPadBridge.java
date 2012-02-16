package bridge;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;

import serialtalk.ArduinoObject;
import serialtalk.ArduinoPad;
import serialtalk.ArduinoSensor;
import capture.UIPad;
import display.SensorButtonGroup;
import display.SensorShape;

public class ArduinoToPadBridge implements ArduinoToDisplayBridge {
  /* TODO
   * pads should have one UIAction per square of sensor
   * they should correspond to absolute location on a pad
   * the pad should be determined by clicking in the four corners of the pad thing the user wants controlled
   * 
   * TODO also: we need an example of this to test it for the user study!!
   */
  
  private static final SensorButtonGroup nullInterface = new SensorButtonGroup(SensorShape.shapes.SQUARE);
  private static final ArduinoPad nullPad = new ArduinoPad(new ArrayList<ArduinoSensor>());
  
  public SensorButtonGroup interfacePiece = nullInterface;
  public ArduinoObject arduinoPiece = nullPad;
  public UIPad interactivePiece;
  
  public Integer sensitivity = 0;
    
  public ArduinoToPadBridge(int sensitivity) {
    nullInterface.isPad = true;
    this.sensitivity = sensitivity;
    interactivePiece = new UIPad(sensitivity);
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
          ((JButton)event.getSource()).setText(interactivePiece.toString() + " (change)");
        }
      }
    });
    return capturePad;
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
}
