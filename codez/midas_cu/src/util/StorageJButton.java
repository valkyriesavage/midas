package util;

import javax.swing.JButton;

import serialtalk.ArduinoSensor;

public class StorageJButton extends JButton {
  private static final long serialVersionUID = -9002966128949738728L;

  public StorageJButton(String label) {
    super(label);
  }
  
  public String stringData;
  public ArduinoSensor sensorData;
  public int intData;
}
