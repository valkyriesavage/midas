package bridge;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import serialtalk.ArduinoDispatcher;
import serialtalk.ArduinoEvent;
import serialtalk.ArduinoObject;
import serialtalk.ArduinoSensor;
import serialtalk.TouchDirection;
import util.ColorBar;
import actions.UIAction;
import display.ArduinoSensorButton;
import display.CanvasPanel;
import display.SensorButtonGroup;
import display.SetUp;

public abstract class ArduinoToDisplayBridge {
  public ArduinoObject arduinoPiece;
  public SensorButtonGroup interfacePiece;

  public boolean isCustom = false;
  public boolean isHellaSlider = false;

  private static ArduinoDispatcher dispatcher;

  protected String interactionType = UIAction.POSSIBLE_INTERACTIONS[0];
  protected JTextField websocketField = new JTextField("socket address");
  protected String websocket = "";

  protected static SetUp repainter;
  
  protected ColorBar myColorBar = new ColorBar(null);

  public static void setDispatcher(ArduinoDispatcher newDispatcher) {
    dispatcher = newDispatcher;
  }

  public static void setRepainter(SetUp newRepainter) {
    repainter = newRepainter;
  }

  public void setInterfacePiece(SensorButtonGroup interfacePiece) {
    this.interfacePiece = interfacePiece;
    isHellaSlider = (interfacePiece.sensitivity == SetUp.HELLA_SLIDER);
  }

  public void paint(Graphics2D g) {
    interfacePiece.paint(g);
  }

  public abstract void execute(ArduinoSensor sensor, TouchDirection direction);

  public abstract void execute(ArduinoSensorButton button);

  public boolean contains(ArduinoSensor sensor) {
    return arduinoPiece != null && arduinoPiece.contains(sensor);
  }

  public boolean contains(ArduinoSensorButton button) {
    return interfacePiece.contains(button);
  }

  public abstract void setArduinoSequence(List<ArduinoEvent> events);

  public Color[] arduinoTies() {
    if (arduinoPiece == null
        || arduinoPiece == ArduinoToSliderBridge.nullSlider
        || arduinoPiece == ArduinoToPadBridge.nullPad
        || arduinoPiece.sensor() == null) {
      return null;
    }

    Color[] colors = { CanvasPanel.DARK_COPPER, Color.RED, Color.ORANGE,
        Color.YELLOW, Color.GREEN, Color.BLUE, new Color(255, 0, 255) };

    Color[] used = new Color[arduinoPiece.sensor().length];

    for (int i = 0; i < arduinoPiece.sensor().length; i++) {
      used[i] = colors[arduinoPiece.sensor()[i]];
    }

    return used;
  }

  public JButton setArduinoSequenceButton() {
    JButton sequenceButton;
    sequenceButton = new JButton("tie to stickers");
    sequenceButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        JButton src = (JButton) event.getSource();
        if (!src.getText().equals("done")) { // we want to start capturing
          if (interfacePiece.isSlider) {
            JOptionPane.showMessageDialog(null,
                "slide your finger from top to bottom or left to right",
                "slider registration instructions",
                JOptionPane.INFORMATION_MESSAGE);
          } else if (interfacePiece.isPad) {
            JOptionPane
                .showMessageDialog(
                    null,
                    "slide your finger from left to right along each row,\nbeginning on the top row",
                    "pad registration instructions",
                    JOptionPane.INFORMATION_MESSAGE);
          }
          src.setText("done");
          dispatcher.beginCapturing();
        } else { // src.getText().equals("done") , i.e. we are done capturing
          List<ArduinoEvent> report = dispatcher.endCaptureAndReport();
          List<ArduinoEvent> sendAlong = new ArrayList<ArduinoEvent>();
          for (ArduinoEvent e : report) {
            if (e.touchDirection == TouchDirection.TOUCH) {
              // keep it
              sendAlong.add(e);
            }
          }
          setArduinoSequence(sendAlong);
          if (arduinoPiece == null) {
            src.setText("try again");
          } else {
            src.setText("tie to stickers");
            myColorBar.setColor(arduinoTies());
          }
        }
      }
    });
    return sequenceButton;
  }

  public JComboBox chooseInteractionType() {
    JComboBox choose = new JComboBox(UIAction.POSSIBLE_INTERACTIONS);
    choose.setSelectedItem(interactionType);
    choose.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        String selected = (String) ((JComboBox) event.getSource())
            .getSelectedItem();
        interactionType = selected;
        repainter.setSelectedBridge(repainter.currentBridge);
        repainter.repaint();
      }
    });
    return choose;
  }

  public JTextField websocketField() {
    return websocketField;
  }

  protected boolean websocketing() {
    return interactionType.equals(UIAction.POSSIBLE_INTERACTIONS[1]);
  }

  protected boolean screenScripting() {
    return interactionType.equals(UIAction.POSSIBLE_INTERACTIONS[0]);
  }

  public String toString() {
    return interfacePiece.name;
  }
  
  public JComponent colorBar() {
    return myColorBar;
  }
}
