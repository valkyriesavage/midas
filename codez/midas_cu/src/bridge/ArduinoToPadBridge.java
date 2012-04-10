package bridge;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import serialtalk.ArduinoEvent;
import serialtalk.ArduinoPad;
import serialtalk.ArduinoSensor;
import serialtalk.ArduinoSetup;
import serialtalk.TouchDirection;
import actions.SocketTalkAction;
import capture.UIPad;
import display.ArduinoSensorButton;
import display.SensorButtonGroup;
import display.SetUp;

public class ArduinoToPadBridge extends ArduinoToDisplayBridge {

  public static final ArduinoPad nullPad = new ArduinoPad(
      new ArduinoSensor[0][0]);

  public UIPad interactivePiece;
  
  private JButton show = new JButton();

  public Integer sensitivity;
  JComboBox padSensitivity = new JComboBox(SetUp.PAD_SENSITIVITIES);

  public ArduinoToPadBridge(int sensitivity) {
    initWebsocketField();
    this.sensitivity = sensitivity;
    arduinoPiece = nullPad;
    interactivePiece = new UIPad(sensitivity);

    padSensitivity.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        JComboBox sensitivityBox = (JComboBox) event.getSource();
        Integer newSensitivity = (Integer) sensitivityBox.getSelectedItem();
        setSensitivity(newSensitivity);
      }
    });
  }

  public void setSensitivity(Integer sensitivity) {
    this.sensitivity = sensitivity;
    this.interactivePiece.sensitivity = sensitivity;
    this.interfacePiece.setSensitivity(sensitivity);
    ((ArduinoPad) this.arduinoPiece).setSensitivity(sensitivity);
  }

  public void setInterfacePiece(SensorButtonGroup interfacePiece) {
    this.interfacePiece = interfacePiece;
    interfacePiece.isPad = true;
    this.interfacePiece.setSensitivity(this.sensitivity);
  }

  public JComponent interactionSetter() {
    if (websocketing()) {
      return websocketField;
    } else {
      JButton capturePad;
      if (interactivePiece.icon() != null) {
        capturePad = new JButton(interactivePiece.icon());
        capturePad.setToolTipText(interactivePiece.toString());
      } else {
        capturePad = new JButton("record pad");
      }
      capturePad.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent event) {
          if (!interactivePiece.isRecording) {
            JOptionPane
                .showMessageDialog(
                    null,
                    "click in two opposite corners of the pad area you'd like to control",
                    "pad capture instructions", JOptionPane.INFORMATION_MESSAGE);
            interactivePiece.record();
            ((JButton) event.getSource()).setText("done");
          } else {
            interactivePiece.stopRecording();
            ((JButton) event.getSource()).setText("record pad");
            setUpDisplay();
          }
        }
      });
      return capturePad;
    }
  }
  
  private void setUpDisplay() {
    if (interactivePiece == null) {
      show.setText("none");
    }
    show.setIcon(interactivePiece.icon());
    show.setToolTipText(interactivePiece.toString());
    show.setEnabled(false);
  }
  
  public JComponent interactionDisplay() {
    setUpDisplay();
    return show;
  }

  public JComboBox padSensitivityBox() {
    return padSensitivity;
  }

  public JButton goButton() {
    JButton show = new JButton("replay positions");
    show.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        for (int i = (int) Math.sqrt(sensitivity) - 1; i >= 0; i--) {
          for (int j = 0; j < Math.sqrt(sensitivity); j++) {
            execute(((ArduinoPad)arduinoPiece).sensorAt(j, i), TouchDirection.TOUCH);
            execute(((ArduinoPad)arduinoPiece).sensorAt(j, i), TouchDirection.RELEASE);
          }
        }
      }
    });
    return show;
  }

  public void execute(ArduinoSensor sensor, TouchDirection direction) {
    if (websocketing()) {
      new SocketTalkAction(websocketField().getText()).doAction();
      return;
    }
    
    if (direction == TouchDirection.TOUCH) {
      interactivePiece.execute(((ArduinoPad) arduinoPiece).locationOnPad(sensor));
    }
  }

  public void execute(ArduinoSensorButton button) {
    if (this.contains(button)) {
      // we might not need to do this at the moment... we aren't triggering on
      // click
      // this.interactivePiece.execute(whichPad)
    }
  }
  
  private void initWebsocketField() {
    websocketField.getDocument().addDocumentListener(new DocumentListener() {
      @Override
      public void changedUpdate(DocumentEvent event) {
        JTextField target = (JTextField) event.getDocument();
        websocket = target.getText();
      }

      @Override
      public void insertUpdate(DocumentEvent event) {
      }

      @Override
      public void removeUpdate(DocumentEvent event) {
      }
    });
  }

  @Override
  public void setArduinoSequence(List<ArduinoEvent> events) {
    // for a pad, we want to take them in order l->r t->b
    List<ArduinoSensor> sensors = new ArrayList<ArduinoSensor>();
    for (ArduinoEvent e : events) {
      if (e.touchDirection == TouchDirection.TOUCH && !sensors.contains(e.whichSensor)) {
        sensors.add(e.whichSensor);
      }
    }

    // now organize them
    if (sensitivity.intValue() != sensors.size()) {
      System.out.println("wrong number of sensors registered");
      arduinoPiece = null;
      return;
    }
    
    int sideOfPad = (int) Math.floor(Math.sqrt(sensitivity));
    ArduinoSensor[][] newPad = new ArduinoSensor[sideOfPad][sideOfPad];

    for (int i = 0; i < sideOfPad; i++) {
      for (int j = 0; j < sideOfPad; j++) {
        newPad[i][j] = sensors.get(i * sideOfPad + j);
      }
    }
    
    arduinoPiece = new ArduinoPad(newPad);
    ArduinoSetup.addPad((ArduinoPad) arduinoPiece);
  }
}
