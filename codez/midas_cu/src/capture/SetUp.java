package capture;

/**
 * 
 * Okay, VLKR, TODO:
 * hella updated interface
 * 	grid thing for layout of sensors
 * 	combo/slider/button differentiation
 */

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import serialtalk.ArduinoSensor;
import serialtalk.ArduinoSlider;
import serialtalk.SerialCommunication;

public class SetUp extends JFrame implements ActionListener {
	private static final long serialVersionUID = -7176602414855781819L;

	static SerialCommunication serialCommunication;
	
	JPanel buttonDisplayGrid = new JPanel();
	JPanel listsOfThingsHappening = new JPanel();

	public SetUp() throws AWTException {
		setSize(600, 600);
		setTitle("Midas Cu");

		serialCommunication = new SerialCommunication();
		serialCommunication.initialize();
		
		setLayout(new BorderLayout());

		cleanInterface();
		
		getContentPane().setVisible(true);
	}

	public void actionPerformed(ActionEvent evt) {

	}
	
	private void cleanInterface() {
	  JPanel grid = new JPanel();
	  grid.setSize(400,600);
	  grid.add(new JLabel("Hullo from THE GRID"));
	  add(grid, BorderLayout.WEST);
	  listsOfThingsHappening.setLayout(new GridLayout(4, 1));
	  setListsOfThingsHappening();
	  add(listsOfThingsHappening, BorderLayout.EAST);
	}

	private void setListsOfThingsHappening() {
    listsOfThingsHappening.removeAll();
    
		JPanel buttonSection = new JPanel();
		buttonSection.setLayout(new BorderLayout());
		buttonSection.add(new JLabel("buttons"), BorderLayout.NORTH);
		JPanel buttonMappings = new JPanel();
		buttonMappings.setLayout(new GridLayout(10, 3));
		for (Map.Entry<ArduinoSensor, UIScript> e : serialCommunication.buttonsToHandlers().entrySet()) {
	    buttonMappings.add(new JLabel(e.getKey().toString()));
	    buttonMappings.add(new JLabel(e.getValue().toString()));
	    buttonMappings.add(new JButton("x"));
		}
		buttonMappings.add(new JLabel("button"));
		buttonMappings.add(new JButton("action"));
		buttonSection.add(buttonMappings, BorderLayout.SOUTH);
		
		listsOfThingsHappening.add(buttonSection);

		JPanel sliderSection = new JPanel();
		sliderSection.setLayout(new BorderLayout());
		sliderSection.add(new JLabel("sliders"), BorderLayout.NORTH);
    JPanel sliderMappings = new JPanel();
    buttonMappings.setLayout(new GridLayout(10, 4));
    for (Map.Entry<ArduinoSlider, List<UIScript>> e : serialCommunication.slidersToHandlers().entrySet()) {
      sliderMappings.add(new JLabel(e.getKey().toString()));
      for(UIScript s : e.getValue()) {
        sliderMappings.add(new JLabel(s.toString()));
      }
      sliderMappings.add(new JButton("x"));
    }
    sliderMappings.add(new JLabel("slider"));
    sliderMappings.add(new JButton("up action"));
    sliderMappings.add(new JButton("down action"));
    sliderSection.add(sliderMappings, BorderLayout.SOUTH);
    
    listsOfThingsHappening.add(sliderSection);

    JPanel padSection = new JPanel();
    padSection.add(new JLabel("pads"));
    
    listsOfThingsHappening.add(padSection);
    
    JPanel comboSection = new JPanel();
    comboSection.add(new JLabel("combos"));
    
    listsOfThingsHappening.add(comboSection);
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
