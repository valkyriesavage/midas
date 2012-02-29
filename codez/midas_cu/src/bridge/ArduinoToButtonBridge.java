package bridge;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;

import serialtalk.ArduinoEvent;
import serialtalk.ArduinoSensor;
import capture.UIScript;

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
          interactivePiece.record();
          ((JButton)event.getSource()).setText("stop recording");
        } else {
          interactivePiece.stopRecording();
          ((JButton)event.getSource()).setText(interactivePiece.toString());
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
