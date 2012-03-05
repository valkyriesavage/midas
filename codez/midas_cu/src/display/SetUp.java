package display;

/**
 * 
 * TODO:
 *  hella updated interface
 *    add functionality for updating button names/actions
 *    AUTOSAVE
 * 	  grid thing for layout of sensors
 *      get comfortable with batik
 *    
 */

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import serialtalk.SerialCommunication;
import bridge.ArduinoToButtonBridge;
import bridge.ArduinoToDisplayBridge;
import bridge.ArduinoToPadBridge;
import bridge.ArduinoToSliderBridge;
import display.SensorShape.shapes;

public class SetUp extends JFrame {
	private static final long serialVersionUID = -7176602414855781819L;
	
	public static final int CANVAS_X = 280;
	public static final int CANVAS_Y = 520;
	
	public static final Integer[] SLIDER_SENSITIVITIES = {3,4,5,6,7,8};
	public static final Integer[] PAD_SENSITIVITIES = {9,16,25,36};

	static SerialCommunication serialCommunication;
	public static final String PROJ_HOME = "/Users/valkyrie/projects/midas_cu/codez/midas_cu/src/";
	
	JPanel buttonDisplayGrid = new JPanel();
	List<SensorButtonGroup> displayedButtons = new ArrayList<SensorButtonGroup>();
	CanvasPanel buttonCanvas = new CanvasPanel(this, displayedButtons);
	List<ArduinoToDisplayBridge> bridgeObjects;
	JPanel buttonCreatorPanel = new JPanel();
	JPanel listsOfThingsHappening = new JPanel();
	JPanel propertiesPane = new JPanel();
	JPanel tempButtonDisplay = new JPanel();

	SVGPathwaysGenerator pathwaysGenerator = new SVGPathwaysGenerator(displayedButtons);
	
	SensorShape.shapes queuedShape;

	public SetUp(boolean test) throws AWTException {
		setSize(CANVAS_X + 350, CANVAS_Y + 180);
		setTitle("Midas Cu");

		serialCommunication = new SerialCommunication();
		serialCommunication.initialize(test);
		bridgeObjects = serialCommunication.bridgeObjects;
		
		setLayout(new BorderLayout());

		cleanInterface();
		
		getContentPane().setVisible(true);
	}
	
	private void cleanInterface() {
	  setUpTheGrid();
	  add(buttonDisplayGrid, BorderLayout.WEST);
	  
	  prepPropertiesPane();
	  add(propertiesPane, BorderLayout.EAST);
	  
	  add(serialCommunication.whatISee(), BorderLayout.SOUTH);
	}
	
	private void setUpTheGrid() {
	  buttonDisplayGrid.setVisible(false);
	  
    buttonDisplayGrid.setSize(CANVAS_X,600);
    buttonDisplayGrid.setLayout(new BorderLayout());
    buttonDisplayGrid.add(buttonCanvas, BorderLayout.NORTH);
    
    setUpButtonCreator();
    buttonDisplayGrid.add(buttonCreatorPanel, BorderLayout.SOUTH);
    buttonDisplayGrid.setVisible(true);
	}
	
	public void paintComponent(Graphics2D g) {
	  super.paint(g);
	  buttonCanvas.paint(g);
	  pathwaysGenerator.paint(g);
	}

	private void setUpButtonCreator() {
	  buttonCreatorPanel.removeAll();
	  buttonCreatorPanel.setLayout(new GridLayout(3,2));
	  
	  JPanel addStockButtonPanel = new JPanel();
	  JComboBox shapeChooser = new JComboBox(SensorShape.shapesList);
	  shapeChooser.addItemListener(new ItemListener() {
	    public void itemStateChanged(ItemEvent event) {
	      if (event.getStateChange() == ItemEvent.SELECTED) {
	        queuedShape = ((SensorShape)((JComboBox)event.getSource()).getSelectedItem()).shape;
	      }
	    }
	  });
	  
	  addStockButtonPanel.add(shapeChooser);
	  JButton addStock = new JButton("+");
	  addStock.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent event) {
	      cleanUpDeletions();
	      SensorButtonGroup newButton = new SensorButtonGroup(queuedShape);
	      ArduinoToDisplayBridge newBridge;
	      if (queuedShape == shapes.SLIDER) {
	        newButton.isSlider = true;
          newBridge = new ArduinoToSliderBridge(SLIDER_SENSITIVITIES[0]);
	      }
	      else if (queuedShape == shapes.PAD) {
	        newButton.isPad = true;
	        newBridge = new ArduinoToPadBridge(PAD_SENSITIVITIES[0]);
	      }
	      else { // it is a button
	        newBridge = new ArduinoToButtonBridge();
	      }
	      displayedButtons.add(newButton);
	      newBridge.setInterfacePiece(newButton);
	      if (queuedShape != shapes.PAD && queuedShape != shapes.SLIDER) {
	        // this is definitely bad coding practice, but we have to get the single buttons drawing still
	        newBridge.interfacePiece.setSensitivity(1);
	      }
	      bridgeObjects.add(newBridge);
	      setSelectedBridge(newBridge);
	      repaint();
	    }
	  });
	  addStockButtonPanel.add(addStock);
	  buttonCreatorPanel.add(addStockButtonPanel);
	  
	  JPanel addCustomButtonPanel = new JPanel();
	  JButton addCustom = new JButton("draw custom button");
	  addCustomButtonPanel.add(addCustom);
	  buttonCreatorPanel.add(addCustomButtonPanel);
	  
	  JPanel printingPanel = new JPanel();
	  JButton printSensors = new JButton("print sensors");
	  printSensors.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent event) {
	      cleanUpDeletions();
        if(displayedButtons.size() > 0) {
          // code related to desktop inspired by johnbokma.com
          if(!Desktop.isDesktopSupported()) {
            System.err.println( "Can't get browser opener" );
          }
          Desktop desktop = Desktop.getDesktop();
          if(!desktop.isSupported(Desktop.Action.BROWSE)) {
            System.err.println("Can't open browser");
          }
          
          try {
            desktop.browse(generateInstructionsPage());
          } catch (IOException e) {
            e.printStackTrace();
          }

        } else {
          JOptionPane.showMessageDialog(null, "there are no buttons to print!",
              "no buttons to print", JOptionPane.ERROR_MESSAGE);
        }
      }
	  });
	  printingPanel.add(printSensors);
	  buttonCreatorPanel.add(printingPanel);
	}
	
	private void cleanUpDeletions() {
	  List<SensorButtonGroup> toDelete = new ArrayList<SensorButtonGroup>();
	  List<ArduinoToDisplayBridge> bridgesToDelete = new ArrayList<ArduinoToDisplayBridge>();
	  for (SensorButtonGroup sbg : displayedButtons) {
	    if (sbg.deleteMe) {
	      toDelete.add(sbg);
	      for (ArduinoToDisplayBridge bridge : bridgeObjects) {
	        if (bridge.interfacePiece == sbg) {
	           bridgesToDelete.add(bridge);
	        }
	      }
	    }
	  }
	  for (SensorButtonGroup deleteable : toDelete) {
	    displayedButtons.remove(deleteable);
	  }
	  for (ArduinoToDisplayBridge deleteable : bridgesToDelete) {
	    bridgeObjects.remove(deleteable);
	  }
	  generatePathways();
	}
	
	private void generatePathways() {
	  pathwaysGenerator.generatePathways(displayedButtons);
	}
	
	private URI generateInstructionsPage() {
	  try {
	    File temp = File.createTempFile("midas_cu", ".html");
	    temp.deleteOnExit();

	    BufferedWriter out = new BufferedWriter(new FileWriter(temp));
	    out.write(InstructionsGenerator.instructions());
	    out.close();

	    return temp.toURI();
	  } catch (IOException e) {
	    return null;
	  }
	}
	
	private ActionListener repainter() {
	  return new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        repaint();
      }
    };
	}
	
	public void setSelectedBridge(ArduinoToDisplayBridge bridge) {
	  for (ArduinoToDisplayBridge notSelected : bridgeObjects) {
	    notSelected.interfacePiece.setSelected(false);
	  }
	  bridge.interfacePiece.setSelected(true);
	  repaint();

	  propertiesPane.setVisible(false);
	  propertiesPane.removeAll();
	  if (bridge.interfacePiece.isSlider) {
	    ArduinoToSliderBridge sliderBridge = (ArduinoToSliderBridge) bridge;
      propertiesPane.add(new JLabel("name"));
	    propertiesPane.add(bridge.interfacePiece.nameField);
	    propertiesPane.add(sliderBridge.captureSliderButton());
	    propertiesPane.add(sliderBridge.showTestPositionsButton());
      propertiesPane.add(new JLabel("sensitivity"));
      JComboBox sensitivityBox = sliderBridge.sliderSensitivityBox();
      sensitivityBox.addActionListener(repainter());
      propertiesPane.add(sensitivityBox);
      JButton larger = sliderBridge.interfacePiece.larger;
      larger.addActionListener(repainter());
      propertiesPane.add(larger);
      JButton smaller = sliderBridge.interfacePiece.smaller;
      smaller.addActionListener(repainter());
      propertiesPane.add(smaller);
      JButton orientationFlip = sliderBridge.interfacePiece.orientationFlip;
      orientationFlip.addActionListener(repainter());
      propertiesPane.add(orientationFlip);
      propertiesPane.add(new JLabel(""));  // placeholder
      propertiesPane.add(sliderBridge.setArduinoSequenceButton());
      JButton delete = sliderBridge.interfacePiece.delete;
      delete.addActionListener(repainter());
      propertiesPane.add(delete);
	  } else if (bridge.interfacePiece.isPad) {
	    ArduinoToPadBridge padBridge = (ArduinoToPadBridge) bridge;
      propertiesPane.add(new JLabel("name"));
      propertiesPane.add(bridge.interfacePiece.nameField);
      propertiesPane.add(padBridge.capturePadButton());
      propertiesPane.add(padBridge.showTestPositionsButton());
      propertiesPane.add(new JLabel("sensitivity"));
      JComboBox sensitivityBox = padBridge.padSensitivityBox();
      sensitivityBox.addActionListener(repainter());
      propertiesPane.add(sensitivityBox);
      JButton larger = padBridge.interfacePiece.larger;
      larger.addActionListener(repainter());
      propertiesPane.add(larger);
      JButton smaller = padBridge.interfacePiece.smaller;
      smaller.addActionListener(repainter());
      propertiesPane.add(smaller);   
      propertiesPane.add(padBridge.setArduinoSequenceButton());
      JButton delete = padBridge.interfacePiece.delete;
      delete.addActionListener(repainter());
      propertiesPane.add(delete);	  
    } else {
	    ArduinoToButtonBridge buttonBridge = (ArduinoToButtonBridge) bridge;
      propertiesPane.add(new JLabel("name"));
      propertiesPane.add(bridge.interfacePiece.nameField);
      propertiesPane.add(buttonBridge.interactionButton());
      propertiesPane.add(buttonBridge.goButton());
      JButton larger = buttonBridge.interfacePiece.larger;
      larger.addActionListener(repainter());
      propertiesPane.add(larger);
      JButton smaller = buttonBridge.interfacePiece.smaller;
      smaller.addActionListener(repainter());
      propertiesPane.add(smaller);
      propertiesPane.add(buttonBridge.setArduinoSequenceButton());
      JButton delete = buttonBridge.interfacePiece.delete;
      delete.addActionListener(repainter());
      propertiesPane.add(delete);
	  }
	  propertiesPane.setVisible(true);
	}
	
	private void prepPropertiesPane() {
	  propertiesPane.setVisible(false);
	  propertiesPane.removeAll();
	  propertiesPane.setSize(300, 600);
	  propertiesPane.setPreferredSize(new Dimension(300, 600));
	  propertiesPane.setLayout(new GridLayout(0, 2));
	  propertiesPane.setVisible(true);
	}
	
	public static void main(String[] args) {
		SetUp setup;
		boolean test = true;
		try {
			setup = new SetUp(test);
		} catch (AWTException e) {
			e.printStackTrace();
			return;
		}
		setup.setVisible(true);
	}
}
