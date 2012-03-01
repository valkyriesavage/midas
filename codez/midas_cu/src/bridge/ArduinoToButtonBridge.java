package bridge;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JOptionPane;

import serialtalk.ArduinoEvent;
import serialtalk.ArduinoSensor;
import capture.UIScript;
import display.ArduinoSensorButton;

public class ArduinoToButtonBridge extends ArduinoToDisplayBridge {
  private static final ArduinoSensor nullSensor = new ArduinoSensor(-1,-1);
  
  public UIScript interactivePiece = new UIScript();
    
  public ArduinoToButtonBridge() {}
    
  public String toString() {
    if (interfacePiece.name != null) {
      return interfacePiece.name;
    }
    return "unnamed";
  }
  
  public void executeScript() {
    interactivePiece.execute();
  }
  
  public void execute(ArduinoSensorButton button) {
    if (this.contains(button)) {
      executeScript();
    }
  }
  
  public JButton interactionButton() {
    JButton change;
    if (interactivePiece.actions.size() > 0) {
      change = new JButton(interactivePiece.toString());
    }
    else {
      change = new JButton("record interaction");
    }
    change.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        if (!interactivePiece.isRecording) {
          JOptionPane.showMessageDialog(null, "click and type as you like,\nyour actions will be recorded and saved",
              "capture instructions", JOptionPane.INFORMATION_MESSAGE);
          interactivePiece.record();
          ((JButton)event.getSource()).setText("stop recording");
        } else {
          interactivePiece.stopRecording();
          ((JButton)event.getSource()).setText("");
          ((JButton)event.getSource()).setIcon(interactivePiece.icon());
        }
      }
    });
    return change;
  }
  
  public JButton goButton() {
    JButton change = new JButton("replay");
    change.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        interactivePiece.execute();
      }
    });
    return change;
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
  
  public void execute(ArduinoSensor sensor) {
    interactivePiece.execute();
  }
  
  public void setArduinoSequence(List<ArduinoEvent> events) {
    // since this is a single button, we will just take the first button that was pushed or released
    arduinoPiece = events.get(0).whichSensor;
    ((ArduinoSensor)arduinoPiece).setName(this.toString());
  }
}
