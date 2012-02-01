package capture;

/**
 * 
 * TODO:
 *  hella updated interface
 *    add functionality for updating button names/actions
 * 	  grid thing for layout of sensors
 */

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import serialtalk.ArduinoEvent;
import serialtalk.ArduinoSensor;
import serialtalk.ArduinoSlider;
import serialtalk.SerialCommunication;

public class SetUp extends JFrame implements ActionListener {
	private static final long serialVersionUID = -7176602414855781819L;

	static SerialCommunication serialCommunication;
	public static String PROJ_HOME = "/Users/valkyriesavage/projects/midas_cu/codez/midas_cu/src/";
	
	JPanel buttonDisplayGrid = new JPanel();
	JPanel buttonCreatorPanel = new JPanel();
	JPanel listsOfThingsHappening = new JPanel();
	
	String queuedIconLocation;

	public SetUp() throws AWTException {
		setSize(880, 600);
		setTitle("Midas Cu");

		serialCommunication = new SerialCommunication();
		serialCommunication.initialize();
		
		setLayout(new BorderLayout());

		cleanInterface();
		
		getContentPane().setVisible(true);
	}

	public void actionPerformed(ActionEvent evt) {}
	
	private void cleanInterface() {
	  setUpTheGrid();
	  add(buttonDisplayGrid, BorderLayout.WEST);
	  
	  setListsOfThingsHappening();
	  add(listsOfThingsHappening, BorderLayout.EAST);
	}
	
	private void setUpTheGrid() {
    buttonDisplayGrid.setPreferredSize(new Dimension(300,600));
    buttonDisplayGrid.setLayout(new BorderLayout());
    buttonDisplayGrid.add(new JLabel("Hullo from THE GRID"), BorderLayout.NORTH);
    
    setUpButtonCreator();
    buttonDisplayGrid.add(buttonCreatorPanel, BorderLayout.SOUTH);
	}

	private void setUpButtonCreator() {
	  buttonCreatorPanel.setLayout(new GridLayout(3,2));
	  
	  JPanel addStockButtonPanel = new JPanel();
	  JComboBox shapeChooser = new JComboBox(SensorShape.shapes);
	  shapeChooser.addItemListener(new ItemListener() {
	    public void itemStateChanged(ItemEvent event) {
	      if (event.getStateChange() == ItemEvent.SELECTED) {
	        queuedIconLocation = ((JComboBox)event.getSource()).getSelectedItem().toString();
	      }
	    }
	  });
	  addStockButtonPanel.add(shapeChooser);
	  JButton addStock = new JButton("+");
	  addStock.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent event) {
	      SensorButtonGroup newButton = new SensorButtonGroup(queuedIconLocation);
	      System.out.println(newButton.getName());
	    }
	  });
	  addStockButtonPanel.add(addStock);
	  buttonCreatorPanel.add(addStockButtonPanel);
	  
	  JPanel addCustomButtonPanel = new JPanel();
	  JButton addCustom = new JButton("draw custom button");
	  addCustomButtonPanel.add(addCustom);
	  buttonCreatorPanel.add(addCustomButtonPanel);
	  
	  JPanel printingPanel = new JPanel();
	  JButton printCopper = new JButton("print copper");
	  JButton printVinyl = new JButton("print vinyl");
	  printingPanel.add(printCopper);
	  printingPanel.add(printVinyl);
	  buttonCreatorPanel.add(printingPanel);
	}
	
	private void setListsOfThingsHappening() {
    listsOfThingsHappening.setLayout(new GridLayout(4, 1));

	  listsOfThingsHappening.setVisible(false);
    listsOfThingsHappening.removeAll();
    
		JPanel buttonSection = new JPanel();
		buttonSection.setLayout(new BorderLayout());
		buttonSection.add(new JLabel("buttons"), BorderLayout.NORTH);
		JPanel buttonMappings = new JPanel();
		ArduinoSensor[] listOfButtons = {};
		buttonMappings.setLayout(new GridLayout(0, 3));
		for (Map.Entry<ArduinoSensor, UIScript> e : serialCommunication.buttonsToHandlers().entrySet()) {
	    buttonMappings.add(new JTextField(e.getKey().toString()));
	    listOfButtons[listOfButtons.length] = e.getKey();
	    buttonMappings.add(new JLabel(e.getValue().toString()));
	    buttonMappings.add(new JButton("x"));
		}
		buttonMappings.add(new JComboBox(listOfButtons));
		buttonMappings.add(new JButton("action"));
		buttonSection.add(buttonMappings, BorderLayout.SOUTH);

		JPanel sliderSection = new JPanel();
		sliderSection.setLayout(new BorderLayout());
		sliderSection.add(new JLabel("sliders"), BorderLayout.NORTH);
    JPanel sliderMappings = new JPanel();
    ArduinoSlider[] listOfSliders = {};
    sliderMappings.setLayout(new GridLayout(0, 4));
    for (Map.Entry<ArduinoSlider, List<UIScript>> e : serialCommunication.slidersToHandlers().entrySet()) {
      sliderMappings.add(new JTextField(e.getKey().toString()));
      listOfSliders[listOfSliders.length] = e.getKey(); 
      for(UIScript s : e.getValue()) {
        sliderMappings.add(new JLabel(s.toString()));
      }
      sliderMappings.add(new JButton("x"));
    }
    sliderMappings.add(new JComboBox(listOfSliders));
    sliderMappings.add(new JButton("up action"));
    sliderMappings.add(new JButton("down action"));
    sliderSection.add(sliderMappings, BorderLayout.SOUTH);    

    JPanel padSection = new JPanel();
    padSection.setLayout(new BorderLayout());
    padSection.add(new JLabel("pads"), BorderLayout.NORTH);  
    
    JPanel comboSection = new JPanel();
    comboSection.setLayout(new BorderLayout());
    comboSection.add(new JLabel("combos"), BorderLayout.NORTH);
    JPanel comboMappings = new JPanel();
    comboMappings.setLayout(new GridLayout(0, 4));
    for (Map.Entry<List<ArduinoEvent>, UIScript> e : serialCommunication.combosToHandlers().entrySet()) {
      comboMappings.add(new JLabel(e.getKey().toString()));
      comboMappings.add(new JButton("change"));
      comboMappings.add(new JLabel(e.getValue().toString()));
      comboMappings.add(new JButton("x"));
    }
    comboMappings.add(new JButton("capture combo"));
    comboMappings.add(new JButton("select action"));
    comboSection.add(comboMappings, BorderLayout.SOUTH);
    
    listsOfThingsHappening.add(buttonSection);
    listsOfThingsHappening.add(sliderSection);
    listsOfThingsHappening.add(padSection);
    listsOfThingsHappening.add(comboSection);

    listsOfThingsHappening.setVisible(true);
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
