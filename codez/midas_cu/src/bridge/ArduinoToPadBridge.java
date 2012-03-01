package bridge;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;

import serialtalk.ArduinoEvent;
import serialtalk.ArduinoPad;
import serialtalk.ArduinoSensor;
import serialtalk.ArduinoSetup;
import serialtalk.TouchDirection;
import capture.UIPad;
import display.ArduinoSensorButton;
import display.SensorButtonGroup;
import display.SetUp;

public class ArduinoToPadBridge extends ArduinoToDisplayBridge {
  
  private static final ArduinoPad nullPad = new ArduinoPad(new ArduinoSensor[0][0]);
  
  public UIPad interactivePiece;
  
  public Integer sensitivity;
  JComboBox padSensitivity = new JComboBox(SetUp.PAD_SENSITIVITIES);
    
  public ArduinoToPadBridge(int sensitivity) {
    this.sensitivity = sensitivity;
    arduinoPiece = nullPad;
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
          JOptionPane.showMessageDialog(null, "click in two opposite corners of the pad area you'd like to control",
              "pad capture instructions", JOptionPane.INFORMATION_MESSAGE);
          interactivePiece.record();
          ((JButton)event.getSource()).setText("stop recording");
        } else {
          interactivePiece.stopRecording();
          ((JButton)event.getSource()).setText("");
          ((JButton)event.getSource()).setIcon(interactivePiece.icon());
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
  
  public void execute(ArduinoSensorButton button) {
    if(this.contains(button)) {
      // TODO !!
      // I think we need to split up sensorbuttons into multiple sensors so that this is a bit easier...      
    }
  }
  
  public void setSequence(List<ArduinoEvent> events) {
    // for a pad, we want to know all the bits.  we need to tell this to the ArduinoSetup, too
    List<ArduinoSensor> sensorsInPad = new ArrayList<ArduinoSensor>();
    for(ArduinoEvent e : events) {
      if (e.touchDirection == TouchDirection.RELEASE || sensorsInPad.contains(e.whichSensor)) {
        continue;
      }
      sensorsInPad.add(e.whichSensor);
    }
    int arrayDim = (int) Math.sqrt(sensorsInPad.size());
    ArduinoSensor[][] sensors = new ArduinoSensor[arrayDim][arrayDim];
    for(int i=0; i<arrayDim; i++) {
      for (int j=0; j<arrayDim; j++) {
        sensors[i][j] = sensorsInPad.get(i*arrayDim + j);
      }
    }
    arduinoPiece = new ArduinoPad(sensors);
    ArduinoSetup.addPad((ArduinoPad)arduinoPiece);
  }

  @Override
  public void setArduinoSequence(List<ArduinoEvent> events) {
    // for a pad, we want to take them in order l->r t->b
    List<ArduinoSensor> sensors = new ArrayList<ArduinoSensor>();
    for (ArduinoEvent e : events) {
      if (!sensors.contains(e.whichSensor)) {
        continue;
      }
      sensors.add(e.whichSensor);
    }
    
    // now organize them
    if (sensitivity.intValue() != sensors.size()) {
      System.out.println("wrong number of sensors registered");
      return;
    }
    int sideOfPad = (int) Math.floor(Math.sqrt(sensitivity));
    ArduinoSensor[][] newPad = new ArduinoSensor[sideOfPad][sideOfPad];
    
    for(int i=0; i<sideOfPad; i++) {
      for(int j=0; j<sideOfPad; j++) {
        newPad[i][j] = sensors.get(i*sideOfPad + j);
      }
    }
    arduinoPiece = new ArduinoPad(newPad);
    ArduinoSetup.addPad((ArduinoPad)arduinoPiece);
  }
}
