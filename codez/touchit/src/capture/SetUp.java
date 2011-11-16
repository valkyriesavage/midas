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
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

import serialtalk.SerialCommunication;

public class SetUp extends JFrame implements ActionListener {
  private static final long serialVersionUID = -7176602414855781819L;

  SerialCommunication serialCommunication;

  JTextArea listOfThingsHappening;
  JTextField whenIDo = new JTextField("when i do...");
  SikuliScript outputAction;
  JTextField itDoes = new JTextField("it does...");
  
  Container contentPane = getContentPane();
      
  public SetUp() throws AWTException {
    setSize(530, 480);

    serialCommunication = new SerialCommunication();
    serialCommunication.initialize();

    JPanel input = new JPanel();
    JButton captureIn = new JButton("capture Arduino interaction");
    captureIn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent A) {
        if (serialCommunication.isCapturing()) {
          ((JButton)A.getSource()).setText("capture Arduino interaction");
          whenIDo.setText(serialCommunication.currentCaptureToString());
        } else {
          ((JButton)A.getSource()).setText("done capturing");
        }
        serialCommunication.toggleCapturing();
      }
    });
    whenIDo.setEditable(false);
    input.setLayout(new BorderLayout());
    input.add(whenIDo, BorderLayout.NORTH);
    input.add(captureIn, BorderLayout.SOUTH);
    
    JPanel output = new JPanel();
    JButton captureOut = new JButton("create sikuli script (launch sikuli)");
    captureOut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent A) {
          try {
           Runtime.getRuntime().exec(SikuliScript.SIKULI);
          } catch (IOException e) {
            e.printStackTrace();
            JDialog errorPop = new JDialog();
            errorPop.add(new JLabel("there was a problem with sikuli. is it in your path?"));
          }
      }
    });
    JButton selectSikuliScript = new JButton("select sikuli script");
    selectSikuliScript.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent A) {
    	  JFileChooser chooser = new JFileChooser(SikuliScript.SIKULI_SCRIPT_DIRECTORY);
    	  FileNameExtensionFilter filter = new FileNameExtensionFilter(
    	        "Sikuli Scripts", "py");
    	  chooser.setFileFilter(filter);
    	  int returnVal = chooser.showOpenDialog(((JButton)A.getSource()).getParent());
    	  if(returnVal == JFileChooser.APPROVE_OPTION) {
    		  outputAction = new SikuliScript(chooser.getCurrentDirectory().getAbsolutePath());
    		  itDoes.setText(outputAction.toString());
    	  }
      }
    });
    itDoes.setEditable(false);
    output.setLayout(new BorderLayout());
    output.add(captureOut, BorderLayout.NORTH);
    output.add(itDoes, BorderLayout.CENTER);
    output.add(selectSikuliScript, BorderLayout.SOUTH);
    
    JButton saveInteraction = new JButton("save interaction");
    saveInteraction.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent A) {
    	  whenIDo.setText("when i do...");
    	  itDoes.setText("it does...");
          serialCommunication.registerCurrentCapture(outputAction);
          outputAction = null;
          listOfThingsHappening.setText(serialCommunication.listOfRegisteredEvents());
      }
    });
    
    listOfThingsHappening  = new JTextArea(25, 15);
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

