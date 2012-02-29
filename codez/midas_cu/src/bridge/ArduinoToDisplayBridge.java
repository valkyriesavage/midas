package bridge;

import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JOptionPane;

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
    JButton sequenceButton = new JButton("register sensors");
    sequenceButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        JButton src = (JButton)event.getSource();
        if (!src.getText().equals("done")) {
          if(interfacePiece.isSlider) {
            JOptionPane.showMessageDialog(null, "slide your finger from top to bottom or left to right",
                "slider capture instructions", JOptionPane.INFORMATION_MESSAGE);
          } else if(interfacePiece.isPad) {
            JOptionPane.showMessageDialog(null, "slide your finger from left to right along each row,\nbeginning on the top row",
                "slider capture instructions", JOptionPane.INFORMATION_MESSAGE);
          }
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
