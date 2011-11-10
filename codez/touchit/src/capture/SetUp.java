package capture;

import java.awt.AWTException;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import serialtalk.SerialCommunication;

public class SetUp extends JFrame implements ActionListener {
  private static final long serialVersionUID = -7176602414855781819L;

  SerialCommunication serialCommunication;

  JTextArea listOfThingsHappening;
  
  public SetUp() throws AWTException {
    setSize(280, 280);
    
    serialCommunication = new SerialCommunication();
    serialCommunication.initialize();

    JTextField whenIDo = new JTextField("When I do...");
    JTextField itDoes = new JTextField("It does...");
    listOfThingsHappening  = new JTextArea();
    listOfThingsHappening.setEditable(false);
    listOfThingsHappening.setText("What's up, doc");

    Container contentPane = getContentPane();
    contentPane.add(whenIDo);
    contentPane.add(itDoes);
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

