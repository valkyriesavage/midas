package bridge;

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
import serialtalk.ArduinoSensor;
import serialtalk.ArduinoSetup;
import serialtalk.ArduinoSlider;
import serialtalk.TouchDirection;
import actions.SocketTalkAction;
import capture.UISlider;
import display.ArduinoSensorButton;
import display.SensorButtonGroup;
import display.SetUp;

public class ArduinoToSliderBridge extends ArduinoToDisplayBridge {

  public static final ArduinoSlider nullSlider = new ArduinoSlider(
      new ArduinoSensor[0]);

  public UISlider interactivePiece;

  public Integer sensitivity;
  private JComboBox sliderSensitivity = new JComboBox(
      SetUp.SLIDER_SENSITIVITIES);

  private JButton show;

  public ArduinoToSliderBridge(int sensitivity) {
    initWebsocketField();
    this.sensitivity = sensitivity;
    arduinoPiece = nullSlider;
    interactivePiece = new UISlider(sensitivity);

    sliderSensitivity.addActionListener(new ActionListener() {
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

    isHellaSlider = (sensitivity == SetUp.HELLA_SLIDER);
    if (isHellaSlider) {
      arduinoPiece = ArduinoSetup.hellaSlider;
    }
  }

  public void setInterfacePiece(SensorButtonGroup interfacePiece) {
    this.interfacePiece = interfacePiece;
    interfacePiece.isSlider = true;
    this.interfacePiece.setSensitivity(this.sensitivity);
  }

  public JComponent interactionDisplay() {
    if (interactivePiece.icon() != null) {
      show = new JButton(interactivePiece.icon());
      show.setToolTipText(interactivePiece.toString());
    } else {
      show = new JButton("none");
    }
    show.setEnabled(false);

    return show;
  }

  public JComponent interactionSetter() {
    if (websocketing()) {
      return websocketField;
    } else {
      JButton captureSlider;
      if (interactivePiece.icon() != null) {
        captureSlider = new JButton(interactivePiece.icon());
        captureSlider.setToolTipText(interactivePiece.toString());
      } else {
        captureSlider = new JButton("record slider");
      }
      captureSlider.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent event) {
          if (!interactivePiece.isRecording) {
            JOptionPane
                .showMessageDialog(
                    null,
                    "click at the top and bottom or\nleft and right ends of the slider",
                    "slider capture instructions",
                    JOptionPane.INFORMATION_MESSAGE);
            interactivePiece.record();
            ((JButton) event.getSource()).setText("done");
          } else {
            interactivePiece.stopRecording();
            ((JButton) event.getSource()).setText("record slider");
            show.setIcon(interactivePiece.icon());
            show.setToolTipText(interactivePiece.toString());
          }
        }
      });
      return captureSlider;
    }
  }

  public JComboBox sliderSensitivityBox() {
    return sliderSensitivity;
  }

  public JButton goButton() {
    JButton show = new JButton("replay positions");
    show.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        for (int i = 0; i < sensitivity; i++) {
          execute(i);
        }
      }
    });
    return show;
  }

  public void execute(ArduinoSensor sensor, TouchDirection direction) {
    if (websocketing()) {
      new SocketTalkAction(websocketField().getText()).doAction();
    }
    if (direction == TouchDirection.TOUCH) {
      interactivePiece.execute(((ArduinoSlider) arduinoPiece)
          .whichInSlider(sensor));
    }
  }

  public void execute(ArduinoSensorButton button) {
    if (this.contains(button)) {
      // this does not work. we also don't need it because we're not activating
      // on click
      // this.interactivePiece.execute(button);
    }
  }

  public void execute(int hellaSliderValue) {
    if (websocketing()) {
      new SocketTalkAction(websocketField().getText()).doAction();
    } else {
      // just to make surezies
      interactivePiece.sensitivity = sensitivity;
      interactivePiece.execute(hellaSliderValue);
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
    // for a slider, we want t->b
    List<ArduinoSensor> sensors = new ArrayList<ArduinoSensor>();
    for (ArduinoEvent e : events) {
      if (!sensors.contains(e.whichSensor)) {
        sensors.add(e.whichSensor);
      }
    }
    if (sensors.size() != sensitivity.intValue()) {
      System.out.println("wrong number of sensors registered");
      return;
    }
    arduinoPiece = new ArduinoSlider(sensors);
    ArduinoSetup.addSlider((ArduinoSlider) arduinoPiece);
  }
}
