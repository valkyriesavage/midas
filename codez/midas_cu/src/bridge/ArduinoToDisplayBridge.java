package bridge;

import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;

import serialtalk.ArduinoDispatcher;
import serialtalk.ArduinoEvent;
import serialtalk.ArduinoObject;
import serialtalk.ArduinoSensor;
import display.SensorButtonGroup;

public abstract class ArduinoToDisplayBridge {
  public ArduinoObject arduinoPiece;
  public SensorButtonGroup interfacePiece;
  
  private static ArduinoDispatcher dispatcher;
  
  public static void setDispatcher(ArduinoDispatcher newDispatcher) {
    dispatcher = newDispatcher;
  }
  
  public void setInterfacePiece(SensorButtonGroup interfacePiece) {
    this.interfacePiece = interfacePiece;
  }
  
  public void paint(Graphics2D g) {
    interfacePiece.paint(g);
  }
  
  public void execute(ArduinoSensor sensor) {};
  
  public boolean contains(ArduinoSensor sensor) {
    return arduinoPiece != null && arduinoPiece.contains(sensor);
  }
  
  public abstract void setArduinoSequence(List<ArduinoEvent> events);
  
  public JButton setArduinoSequenceButton() {
    JButton sequenceButton = new JButton("register sensor");
    sequenceButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        JButton src = (JButton)event.getSource();
        if (!src.getText().equals("done")) {
          src.setText("done");
          dispatcher.beginCapturing();
        } else { // src.getText().equals("done")
          setArduinoSequence(dispatcher.endCaptureAndReport());
          src.setText("registered (change)");
        }
      }
    });
    return sequenceButton;
  }
}
