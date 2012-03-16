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
import display.ArduinoSensorButton;
import display.SensorButtonGroup;

public abstract class ArduinoToDisplayBridge {
  public ArduinoObject arduinoPiece;
  public SensorButtonGroup interfacePiece;
  
  public boolean isCustom = false;
  
  private static ArduinoDispatcher dispatcher;
  
  public static final String[] possibleInteractions = {"screen script", "web script"};
  
  public static void setDispatcher(ArduinoDispatcher newDispatcher) {
    dispatcher = newDispatcher;
  }
  
  public void setInterfacePiece(SensorButtonGroup interfacePiece) {
    this.interfacePiece = interfacePiece;
  }
  
  public void paint(Graphics2D g) {
    interfacePiece.paint(g);
  }
  
  public abstract void execute(ArduinoSensor sensor);
  
  public abstract void execute(ArduinoSensorButton button);
  
  public boolean contains(ArduinoSensor sensor) {
    return arduinoPiece != null && arduinoPiece.contains(sensor);
  }
  
  public boolean contains(ArduinoSensorButton button) {
    return interfacePiece.contains(button);
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
                "slider registration instructions", JOptionPane.INFORMATION_MESSAGE);
          } else if(interfacePiece.isPad) {
            JOptionPane.showMessageDialog(null, "slide your finger from left to right along each row,\nbeginning on the top row",
                "pad registration instructions", JOptionPane.INFORMATION_MESSAGE);
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
