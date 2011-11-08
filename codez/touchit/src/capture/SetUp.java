package capture;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import serialtalk.ArduinoEvent;
import serialtalk.SerialCommunication;
import util.Pair;

public class SetUp extends JFrame implements ActionListener {
  private static final long serialVersionUID = -7176602414855781819L;

  SerialCommunication serialCommunication;
  JLabel padding = new JLabel();
  JProgressBar waiter = new JProgressBar(0, 100);
  
  @SuppressWarnings("unchecked")
  List<Pair> l = new ArrayList<Pair>();
  
  public SetUp() throws AWTException {
    serialCommunication = new SerialCommunication();
    serialCommunication.initialize();
    
    l.add(new Pair<ArduinoEvent, UIAction>(new ArduinoEvent(0), new KeyAction(KeyEvent.VK_H)));
    l.add(new Pair<ArduinoEvent, UIAction>(new ArduinoEvent(1), new KeyAction(KeyEvent.VK_E)));
    l.add(new Pair<ArduinoEvent, UIAction>(new ArduinoEvent(2), new KeyAction(KeyEvent.VK_L)));
    l.add(new Pair<ArduinoEvent, UIAction>(new ArduinoEvent(3), new KeyAction(KeyEvent.VK_L)));
    l.add(new Pair<ArduinoEvent, UIAction>(new ArduinoEvent(0), new KeyAction(KeyEvent.VK_O)));
  }
  
  public void actionPerformed(ActionEvent evt) {
    
  }
  
  public void run() {
    setSize(300, 300);
    setDefaultCloseOperation(EXIT_ON_CLOSE);

    JPanel controlPane = new JPanel(new GridLayout(2, 1));
    controlPane.setOpaque(false);
    controlPane.add(new JLabel("Please wait..."));
    controlPane.add(waiter);

    JButton startB = new JButton("Set a bunch of useless defaults!");
 
    startB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent A) {
        for(Pair<ArduinoEvent, UIAction> p: l) {
          serialCommunication.registerSerialEvent((ArduinoEvent)p.getLeft(),
                                                  (UIAction)p.getRight());
        }
      }
    });
    
    JTextField whenIDo = new JTextField("When I do...");
    JTextField itDoes = new JTextField("It does...");
    
    JTextArea listOfThingsHappening = new JTextArea();
    listOfThingsHappening.setEditable(false);
    
    this.add(whenIDo);
    this.add(itDoes);
    this.add(listOfThingsHappening);
    this.pack();
    
    Container contentPane = getContentPane();
    contentPane.add(startB, BorderLayout.SOUTH);
    contentPane.setVisible(true);
  }

  public static void main(String[] args) {
    SetUp setup;
    try {
      setup = new SetUp();
    } catch (AWTException e) {
      e.printStackTrace();
      return;
    }
    setup.run();
  }
}

