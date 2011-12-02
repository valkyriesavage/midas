package capture;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

import serialtalk.ArduinoEvent;
import serialtalk.ArduinoSlider;
import serialtalk.SerialCommunication;

public class SetUp extends JFrame implements ActionListener {
  private static final long serialVersionUID = -7176602414855781819L;

  SerialCommunication serialCommunication;

  JPanel listOfThingsHappening;

  JPanel input = new JPanel();
  JTextField whenIDo = new JTextField("when i do...");
  JButton captureIn = new JButton("capture touch interaction");
  JButton registerSlider = new JButton("register a slider");

  JPanel output = new JPanel();
  JPanel selectSliderActionsPanel = new JPanel();
  SikuliScript outputAction;
  SikuliScript ascendingAction;
  SikuliScript descendingAction;
  JTextField itDoes = new JTextField("it does...");
  JButton selectOutputAction = new JButton("select sikuli script");
  JButton selectAscendingAction = new JButton("select ascending action");
  JButton selectDescendingAction = new JButton("select descending action");

  Container contentPane = getContentPane();

  public SetUp() throws AWTException {
    setSize(530, 480);

    serialCommunication = new SerialCommunication();
    serialCommunication.initialize();

    captureIn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent A) {
        if (serialCommunication.isCapturing()) {
          ((JButton) A.getSource()).setText("capture touch interaction");
          whenIDo.setText(serialCommunication.currentCaptureToString());
          output.add(itDoes, BorderLayout.CENTER);
          output.add(selectOutputAction, BorderLayout.SOUTH);
        } else {
          ((JButton) A.getSource()).setText("done capturing");
          registerSlider.setEnabled(false);
        }
        serialCommunication.toggleCapturing();
      }
    });
    whenIDo.setEditable(false);
    registerSlider.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent A) {
        if (serialCommunication.isCapturingSlider()) {
          ((JButton) A.getSource()).setText("register a slider");
          whenIDo.setText(serialCommunication.currentSliderCaptureToString());
          output.add(selectSliderActionsPanel, BorderLayout.SOUTH);
        } else {
          ((JButton) A.getSource()).setText("done");
          captureIn.setEnabled(false);
        }
        serialCommunication.toggleCapturingSlider();
      }
    });
    input.setLayout(new BorderLayout());
    input.add(whenIDo, BorderLayout.NORTH);
    input.add(captureIn, BorderLayout.CENTER);
    input.add(registerSlider, BorderLayout.SOUTH);

    JButton captureOut = new JButton("create sikuli script (launch sikuli)");
    captureOut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent A) {
        try {
          Runtime.getRuntime().exec(SikuliScript.SIKULI);
        } catch (IOException e) {
          e.printStackTrace();
          JDialog errorPop = new JDialog();
          errorPop.add(new JLabel(
              "there was a problem with sikuli. is it in your path?"));
        }
      }
    });

    selectOutputAction.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent A) {
        JFileChooser chooser = new JFileChooser(
            SikuliScript.SIKULI_SCRIPT_DIRECTORY);
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
            "Sikuli Scripts", "py");
        chooser.setFileFilter(filter);
        int returnVal = chooser.showOpenDialog(((JButton) A.getSource())
            .getParent());
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            if(chooser.getCurrentDirectory().getAbsolutePath().endsWith(".sikuli")) {
          	  outputAction = new SikuliScript(chooser.getCurrentDirectory()
          		.getAbsolutePath());
          	}
          	else {
          	  outputAction = new SikuliScript(chooser.getSelectedFile().getAbsolutePath());
            }
          itDoes.setText(outputAction.toString());
        }
      }
    });
    selectAscendingAction.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent A) {
        JFileChooser chooser = new JFileChooser(
            SikuliScript.SIKULI_SCRIPT_DIRECTORY);
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
            "Sikuli Scripts", "py");
        chooser.setFileFilter(filter);
        int returnVal = chooser.showOpenDialog(((JButton) A.getSource())
            .getParent());
        if (returnVal == JFileChooser.APPROVE_OPTION) {
        	if(chooser.getCurrentDirectory().getAbsolutePath().endsWith(".sikuli")) {
          	  ascendingAction = new SikuliScript(chooser.getCurrentDirectory()
          		.getAbsolutePath());
          	}
          	else {
          	  ascendingAction = new SikuliScript(chooser.getSelectedFile().getAbsolutePath());
            }
          itDoes.setText(ascendingAndDescending());
        }
      }
    });
    selectDescendingAction.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent A) {
        JFileChooser chooser = new JFileChooser(
            SikuliScript.SIKULI_SCRIPT_DIRECTORY);
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
            "Sikuli Scripts", "py");
        chooser.setFileFilter(filter);
        int returnVal = chooser.showOpenDialog(((JButton) A.getSource())
            .getParent());
        if (returnVal == JFileChooser.APPROVE_OPTION) {
        	if(chooser.getCurrentDirectory().getAbsolutePath().endsWith(".sikuli")) {
        	  descendingAction = new SikuliScript(chooser.getCurrentDirectory()
        		.getAbsolutePath());
        	}
        	else {
        	  descendingAction = new SikuliScript(chooser.getSelectedFile().getAbsolutePath());
            }
          itDoes.setText(ascendingAndDescending());
        }
      }
    });
    itDoes.setEditable(false);
    output.setLayout(new BorderLayout());
    output.add(captureOut, BorderLayout.NORTH);
    output.add(itDoes, BorderLayout.CENTER);

    selectSliderActionsPanel.setLayout(new BorderLayout());
    selectSliderActionsPanel.add(selectAscendingAction, BorderLayout.WEST);
    selectSliderActionsPanel.add(selectDescendingAction, BorderLayout.EAST);

    JButton saveInteraction = new JButton("save interaction");
    saveInteraction.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent A) {
        whenIDo.setText("when i do...");
        itDoes.setText("it does...");
        if (ascendingAction != null && descendingAction != null) {
          serialCommunication.registerCurrentCapture(ascendingAction,
              descendingAction);
          ascendingAction = null;
          descendingAction = null;
          output.remove(selectSliderActionsPanel);
        } else if (outputAction != null) {
          serialCommunication.registerCurrentCapture(outputAction);
          outputAction = null;
          output.remove(selectOutputAction);
        }
        setListOfThingsHappening();
        captureIn.setEnabled(true);
        registerSlider.setEnabled(true);
      }
    });
    JButton clearInteractions = new JButton("clear all interactions");
    clearInteractions.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent A) {
        serialCommunication.clearAllInteractions();
        setListOfThingsHappening();
      }
    });

    listOfThingsHappening = new JPanel();
    listOfThingsHappening.setLayout(new BoxLayout(listOfThingsHappening, BoxLayout.PAGE_AXIS));
    setListOfThingsHappening();

    contentPane.setLayout(new FlowLayout());
    contentPane.add(input);
    contentPane.add(output);
    contentPane.add(saveInteraction);
    contentPane.add(clearInteractions);
    contentPane.add(listOfThingsHappening);

    contentPane.setVisible(true);
  }

  public void actionPerformed(ActionEvent evt) {

  }

  private void setListOfThingsHappening() {
    listOfThingsHappening.setVisible(false);
    listOfThingsHappening.removeAll();
    for (Entry<List<ArduinoEvent>, List<UIAction>> interaction : serialCommunication
        .eventsToHandlers().entrySet()) {
      for (UIAction uia : interaction.getValue()) {
        String label = new String(interaction.getKey().toString() + " -> "
            + interaction.getValue().toString() + "\n");
        JPanel holder = new JPanel();
        holder.setLayout(new BorderLayout());
        holder.add(new JLabel(label), BorderLayout.WEST);
        JButton remove = new ArduinoJButton("remove", interaction.getKey(), uia);
        remove.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent A) {
            ArduinoJButton button = (ArduinoJButton) A.getSource();
            serialCommunication.unregisterEvent(button.arduinoEvent,
                button.uiAction);
            setListOfThingsHappening();
          }
        });
        holder.add(remove, BorderLayout.EAST);
        listOfThingsHappening.add(holder);
      }
    }
    for (Entry<ArduinoSlider, List<UIAction>> interaction : serialCommunication
        .slidersToAscHandlers().entrySet()) {
      ArduinoSlider slider = interaction.getKey();
      for (UIAction uia : interaction.getValue()) {
        String label = new String(slider + " -> " + uia + "\n");
        JPanel holder = new JPanel();
        holder.setLayout(new BorderLayout());
        holder.add(new JLabel(label), BorderLayout.WEST);
        JButton remove = new ArduinoJButton("remove", slider, uia);
        remove.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent A) {
            ArduinoJButton button = (ArduinoJButton) A.getSource();
            serialCommunication.unregisterSliderAscEvent(button.arduinoSlider, button.uiAction);
            setListOfThingsHappening();
          }
        });
        holder.add(remove, BorderLayout.EAST);
        listOfThingsHappening.add(holder);
      }
      // now do the descending one right underneath it
      for (UIAction uia : serialCommunication.slidersToDescHandlers().get(slider)) {
        String label = new String(slider.backwardsToString() + " -> " + uia + "\n");
        JPanel holder = new JPanel();
        holder.setLayout(new BorderLayout());
        holder.add(new JLabel(label), BorderLayout.WEST);
        JButton remove = new ArduinoJButton("remove", slider, uia);
        remove.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent A) {
            ArduinoJButton button = (ArduinoJButton) A.getSource();
            serialCommunication.unregisterSliderDescEvent(button.arduinoSlider, button.uiAction);
            setListOfThingsHappening();
          }
        });
        holder.add(remove, BorderLayout.EAST);
        listOfThingsHappening.add(holder);
      }
    }
    listOfThingsHappening.setVisible(true);
  }

  private String ascendingAndDescending() {
    if (ascendingAction != null && descendingAction != null) {
      return "asc : " + ascendingAction.toString() + " ; desc : "
          + descendingAction.toString();
    }
    if (ascendingAction != null) {
      return "asc : " + ascendingAction.toString() + " ; desc : ?";
    }
    return "asc : ? ; desc : " + descendingAction.toString();
  }

  public static void main(String[] args) {
    SetUp setup;
    try {
      setup = new SetUp();
    } catch (AWTException e) {
      e.printStackTrace();
      return;
    }
    setup.setVisible(true);
  }
}
