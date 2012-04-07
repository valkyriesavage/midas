package bridge;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;

import serialtalk.ArduinoEvent;
import serialtalk.ArduinoSensor;
import serialtalk.TouchDirection;
import actions.SocketTalkAction;
import actions.UIScript;
import display.ArduinoSensorButton;

public class ArduinoToButtonBridge extends ArduinoToDisplayBridge {
  private static final ArduinoSensor nullSensor = new ArduinoSensor(-1, -1);

  private UIScript interactiveScript = new UIScript();

  public ArduinoToButtonBridge() { }

  public void executeScript() {
    if (websocketing()) {
      new SocketTalkAction(websocketField().getText()).doAction();
      return;
    }
    interactiveScript.doAction();
  }

  public void execute(ArduinoSensorButton button) {
    if (this.contains(button)) {
      executeScript();
    }
  }

  public JComponent interactionSetter() {
    if (websocketing()) {
      return websocketField;
    } else {
      JButton change;
      if (interactiveScript.actions.size() > 0) {
        change = new JButton(interactiveScript.icon());
        change.setToolTipText(interactiveScript.toString());
      } else {
        change = new JButton("record interaction");
      }
      change.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent event) {
          if (!interactiveScript.isRecording) {
            JOptionPane
                .showMessageDialog(
                    null,
                    "click and type as you like,\nyour actions will be recorded and saved",
                    "capture instructions", JOptionPane.INFORMATION_MESSAGE);
            interactiveScript.record();
            ((JButton) event.getSource()).setText("stop recording");
          } else {
            interactiveScript.stopRecording();
            ((JButton) event.getSource()).setText("");
            ((JButton) event.getSource()).setIcon(interactiveScript.icon());
            ((JButton) event.getSource()).setToolTipText(interactiveScript
                .toString());
          }
        }
      });
      return change;
    }
  }

  public JButton goButton() {
    JButton go = new JButton("test interaction");
    go.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        executeScript();
      }
    });
    return go;
  }

  public JButton registerButton() {
    JButton register;
    if (arduinoPiece == nullSensor) {
      register = new JButton("register");
    } else {
      register = new JButton("" + arduinoPiece);
    }
    register.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {

      }
    });

    return register;
  }

  public void execute(ArduinoSensor sensor, TouchDirection direction) {
    if (this.contains(sensor)
        && (websocketing() || direction == TouchDirection.TOUCH)) {
      executeScript();
    }
  }

  public void setArduinoSequence(List<ArduinoEvent> events) {
    // since this is a single button, we will just take the first button that
    // was pushed or released
    arduinoPiece = events.get(0).whichSensor;
    ((ArduinoSensor) arduinoPiece).setName(this.toString());
  }
}
