package capture;

import java.util.List;

import javax.swing.JButton;

import serialtalk.ArduinoEvent;
import serialtalk.ArduinoSlider;

public class ArduinoJButton extends JButton {

  public ArduinoSlider arduinoSlider = null;
  public List<ArduinoEvent> arduinoEvent = null;
  public UIAction uiAction = null;
  
  public ArduinoJButton(String buttonText, List<ArduinoEvent> arduinoEvent, UIAction uiAction) {
    super(buttonText);
    this.arduinoEvent = arduinoEvent;
    this.uiAction = uiAction;
  }
  
  public ArduinoJButton(String buttonText, ArduinoSlider arduinoSlider, UIAction uiAction) {
    super(buttonText);
    this.arduinoSlider = arduinoSlider;
    this.uiAction = uiAction;
  }
}
