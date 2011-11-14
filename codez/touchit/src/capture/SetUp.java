package capture;

import java.awt.AWTException;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import serialtalk.SerialCommunication;
import util.FriendlyGlassPane;

public class SetUp extends JFrame implements ActionListener {
  private static final long serialVersionUID = -7176602414855781819L;

  SerialCommunication serialCommunication;

  JTextArea listOfThingsHappening;
  JTextField whenIDo = new JTextField("when i do...");
  JTextField itDoes = new JTextField("it does...");
  
  Container contentPane = getContentPane();
  
  JFrame glassPane = new JFrame("move me over what you interact with so i can capture it");
  JPanel glassPaneGlass = new FriendlyGlassPane(this);
      
  @SuppressWarnings("restriction")
  public SetUp() throws AWTException {
    setSize(280, 480);

    serialCommunication = new SerialCommunication();
    serialCommunication.initialize();

    JPanel input = new JPanel();
    JButton captureIn = new JButton("capture");
    captureIn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent A) {
        if (serialCommunication.isCapturing()) {
          ((JButton)A.getSource()).setText("capture");
          whenIDo.setText(serialCommunication.currentCaptureToString());
        } else {
          ((JButton)A.getSource()).setText("done");
        }
        serialCommunication.toggleCapturing();
      }
    });
    input.add(whenIDo);
    input.add(captureIn);
    
    JButton doneButton = new JButton("done capturing");
    doneButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent A) {
        toFront();
        setVisible(true);
        glassPane.setVisible(false);
        System.out.println("blah, done capturing");
      }
    });
    glassPane.setLayout(new FlowLayout());
    glassPane.add(doneButton);
    
    JPanel output = new JPanel();
    JButton captureOut = new JButton("record interaction");
    captureOut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent A) {
          try {
           Runtime.getRuntime().exec(SikuliScript.SIKULI);
          } catch (IOException e) {
            e.printStackTrace();
            JFrame errorPop = new JFrame("problems!");
            errorPop.add(new JLabel("there was a problem with sikuli. is it in your path?"));
          }
      }
    });
    JButton selectSikuliScript = new JButton("select sikuli script");
    captureOut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent A) {
          
      }
    });
    output.add(itDoes);
    output.add(selectSikuliScript);
    output.add(captureOut);
    
    listOfThingsHappening  = new JTextArea(25, 15);
    listOfThingsHappening.setEditable(false);
    listOfThingsHappening.setText("recorded interactions will appear here");

    contentPane.setLayout(new FlowLayout());
    contentPane.add(input);
    contentPane.add(output);
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

