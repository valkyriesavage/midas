package capture;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

import serialtalk.SerialCommunication;

public class SetUp extends JFrame implements ActionListener {
  private static final long serialVersionUID = -7176602414855781819L;

  SerialCommunication serialCommunication;

  JTextArea listOfThingsHappening;
  
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
          outputAction = new SikuliScript(chooser.getCurrentDirectory()
              .getAbsolutePath());
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
          ascendingAction = new SikuliScript(chooser.getCurrentDirectory()
              .getAbsolutePath());
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
          descendingAction = new SikuliScript(chooser.getCurrentDirectory()
              .getAbsolutePath());
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
        serialCommunication.registerCurrentCapture(outputAction);
        outputAction = null;
        ascendingAction = null;
        descendingAction = null;
        listOfThingsHappening.setText(serialCommunication
            .listOfRegisteredEvents());
        captureIn.setEnabled(true);
        registerSlider.setEnabled(true);
        output.remove(selectSliderActionsPanel);
        output.remove(selectOutputAction);
      }
    });

    listOfThingsHappening = new JTextArea(25, 15);
    listOfThingsHappening.setEditable(false);
    listOfThingsHappening.setText("recorded interactions will appear here");

    contentPane.setLayout(new FlowLayout());
    contentPane.add(input);
    contentPane.add(output);
    contentPane.add(saveInteraction);
    contentPane.add(listOfThingsHappening);

    contentPane.setVisible(true);
  }

  public void actionPerformed(ActionEvent evt) {

  }
  
  private String ascendingAndDescending() {
    if (ascendingAction != null && descendingAction != null) {
      return "asc : " + ascendingAction.toString() + " ; desc : " + descendingAction.toString();
    } if (ascendingAction != null) {
      return "asc : " + ascendingAction.toString() + " ; desc : ?";
    } return "asc : ? ; desc : " + descendingAction.toString();
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
