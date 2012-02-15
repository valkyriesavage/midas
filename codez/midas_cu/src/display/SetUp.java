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
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.swing.JSVGCanvas;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGDocument;

import serialtalk.ArduinoEvent;
import serialtalk.SerialCommunication;
import bridge.ArduinoToButtonBridge;
import bridge.ArduinoToDisplayBridge;
import bridge.ArduinoToPadBridge;
import bridge.ArduinoToSliderBridge;
import capture.UIScript;
import display.SensorShape.shapes;

public class SetUp extends JFrame implements ActionListener {
	private static final long serialVersionUID = -7176602414855781819L;
	
	public static final int CANVAS_X = 150;
	public static final int CANVAS_Y = 150;
	
	private static final Integer[] SLIDER_SENSITIVITIES = {3,4,5,6,7,8};
	private static final Integer[] PAD_SENSITIVITIES = {4,9,16,25,36};

	static SerialCommunication serialCommunication;
	public static final String PROJ_HOME = "/Users/valkyrie/projects/midas_cu/codez/midas_cu/src/";
	
	JPanel buttonDisplayGrid = new JPanel();
	JComboBox sliderSensitivity = new JComboBox(SLIDER_SENSITIVITIES);
	private boolean doingSlider = false;
	JComboBox padSensitivity = new JComboBox(PAD_SENSITIVITIES);
	private boolean doingPad = false;
  JSVGCanvas svgCanvas = new JSVGCanvas();
  SVGGraphics2D g;
  SVGDocument doc;
	List<SensorButtonGroup> displayedButtons = new ArrayList<SensorButtonGroup>();
	List<ArduinoToDisplayBridge> bridgeObjects = new ArrayList<ArduinoToDisplayBridge>();
	JPanel buttonCreatorPanel = new JPanel();
	JPanel listsOfThingsHappening = new JPanel();
	
	SVGPathwaysGenerator pathwaysGenerator = new SVGPathwaysGenerator();
	
	SensorShape.shapes queuedShape;

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
	  setUpSVGCanvas();
	  
    buttonDisplayGrid.setPreferredSize(new Dimension(300,600));
    buttonDisplayGrid.setLayout(new BorderLayout());
    buttonDisplayGrid.add(svgCanvas, BorderLayout.NORTH);
    
    setUpButtonCreator();
    buttonDisplayGrid.add(buttonCreatorPanel, BorderLayout.SOUTH);
	}
	
	private void setUpSVGCanvas() {
	   svgCanvas.setDocumentState(JSVGCanvas.ALWAYS_DYNAMIC);
	   svgCanvas.setSize(CANVAS_X,CANVAS_Y);
	   DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
     String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
     doc = (SVGDocument) impl.createDocument(svgNS, "svg", null);
     g = new SVGGraphics2D(doc);
     g.setSVGCanvasSize(new Dimension(300, 300));
     svgCanvas.setSVGDocument(doc);
	}
	
	public void paint() {
	  for (SensorButtonGroup sbg : displayedButtons) {
	    sbg.paint(g);
	  }
	  pathwaysGenerator.paint(g);

    Element root = doc.getDocumentElement();
    g.getRoot(root);
    
    setListsOfThingsHappening();
    
    repaint();
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
          doingSlider = false;
          doingPad = false;
	        if (queuedShape.equals("slider")) {
	          doingSlider = true;
	          setUpButtonCreator();
	        } else if (queuedShape.equals("pad")) {
	          doingPad = true;
	          setUpButtonCreator();
	        }
	      }
	    }
	  });
	  if(doingSlider) { addStockButtonPanel.add(sliderSensitivity); }
	  if(doingPad) { addStockButtonPanel.add(padSensitivity); }
	  addStockButtonPanel.add(shapeChooser);
	  JButton addStock = new JButton("+");
	  addStock.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent event) {
	      cleanUpDeletions();
	      SensorButtonGroup newButton = new SensorButtonGroup(queuedShape);
	      ArduinoToDisplayBridge newBridge;
	      if (queuedShape == shapes.SLIDER) {
	        newButton.isSlider = true;
          newBridge = new ArduinoToSliderBridge();
          newButton.setSensitivity(((Integer)sliderSensitivity.getSelectedItem()).intValue());
	      }
	      else if (queuedShape == shapes.PAD) {
	        newButton.isPad = true;
	        newBridge = new ArduinoToPadBridge();
          newButton.setSensitivity(((Integer)padSensitivity.getSelectedItem()).intValue());
	      }
	      else { // it is a button
	        newBridge = new ArduinoToButtonBridge();
	      }
	      displayedButtons.add(newButton);
	      newBridge.setInterfacePiece(newButton);
	      bridgeObjects.add(newBridge);
	      paint();
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
	  printCopper.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent event) {
	      cleanUpDeletions();
        if(displayedButtons.size() > 0) {
          //TODO
          System.out.println("once this is hooked up for it, we will print copper shapes here!");
        } else {
          JOptionPane.showMessageDialog(buttonDisplayGrid, "there are no shapes to print!");
        }
      }
	  });
	  JButton printVinyl = new JButton("print vinyl");
	  printVinyl.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent event) {
	      cleanUpDeletions();
	      if(displayedButtons.size() > 0) {
	        //TODO
	        System.out.println("once this is hooked up for it, we will print vinyl shapes here!");
	      } else {
	        JOptionPane.showMessageDialog(buttonDisplayGrid, "there are no shapes to print!");
	      }
	    }
	  });
	  printingPanel.add(printCopper);
	  printingPanel.add(printVinyl);
	  buttonCreatorPanel.add(printingPanel);
	}
	
	private void cleanUpDeletions() {
	  List<SensorButtonGroup> toDelete = new ArrayList<SensorButtonGroup>();
	  for (SensorButtonGroup sbg : displayedButtons) {
	    if (sbg.deleteMe) {
	      toDelete.add(sbg);
	    }
	  }
	  for (SensorButtonGroup deleteable : toDelete) {
	    displayedButtons.remove(deleteable);
	  }
	  generatePathways();
	  paint();
	}
	
	private void generatePathways() {
	  pathwaysGenerator.generatePathways(displayedButtons);
	}
	
	private void setListsOfThingsHappening() {
    listsOfThingsHappening.setLayout(new GridLayout(0, 1));

	  listsOfThingsHappening.setVisible(false);
    listsOfThingsHappening.removeAll();
    
		JPanel buttonSection = new JPanel();
		buttonSection.setLayout(new BorderLayout());
		buttonSection.add(new JLabel("buttons"), BorderLayout.NORTH);
		JPanel buttonMappings = new JPanel();
		buttonMappings.setLayout(new GridLayout(0, 2));
		for (ArduinoToDisplayBridge genericBridge : bridgeObjects) {
		  if (genericBridge.interfacePiece().isSlider || genericBridge.interfacePiece().isPad) { continue; }
		  ArduinoToButtonBridge bridge = (ArduinoToButtonBridge) genericBridge;
	    buttonMappings.add(new JTextField(bridge.interfacePiece.name));
	    buttonMappings.add(bridge.interactionButton());
	    buttonMappings.add(bridge.goButton());
		}
		buttonSection.add(buttonMappings, BorderLayout.SOUTH);

		JPanel sliderSection = new JPanel();
		sliderSection.setLayout(new BorderLayout());
		sliderSection.add(new JLabel("sliders"), BorderLayout.NORTH);
    JPanel sliderMappings = new JPanel();
    sliderMappings.setLayout(new GridLayout(0, 3));
    for (ArduinoToDisplayBridge genericBridge : bridgeObjects) {
      if (!(genericBridge.interfacePiece().isSlider)) { continue; }
      ArduinoToSliderBridge bridge = (ArduinoToSliderBridge) genericBridge;
      sliderMappings.add(new JTextField(bridge.interfacePiece.name));
      sliderMappings.add(bridge.captureSliderButton());
    }
    sliderSection.add(sliderMappings, BorderLayout.SOUTH);

    JPanel padSection = new JPanel();
    padSection.setLayout(new BorderLayout());
    padSection.add(new JLabel("pads"), BorderLayout.NORTH);
    JPanel padMappings = new JPanel();
    padMappings.setLayout(new GridLayout(0, 3));
    for (ArduinoToDisplayBridge genericBridge : bridgeObjects) {
      if (!(genericBridge.interfacePiece().isPad)) { continue; }
      ArduinoToPadBridge bridge = (ArduinoToPadBridge) genericBridge;
      padMappings.add(new JTextField(bridge.interfacePiece.name));
      padMappings.add(new JLabel(bridge.interactivePiece.toString()));
    }
    padSection.add(padMappings, BorderLayout.SOUTH);    
    
    JPanel comboSection = new JPanel();
    comboSection.setLayout(new BorderLayout());
    comboSection.add(new JLabel("combos"), BorderLayout.NORTH);
    JPanel comboMappings = new JPanel();
    comboMappings.setLayout(new GridLayout(0, 4));
    for (Map.Entry<List<ArduinoEvent>, UIScript> e : serialCommunication.combosToHandlers().entrySet()) {
      //FIXME : does this make sense?  should combos be treated differently from sliders and buttons entirely?
      // ???????  since they involve a button combo, and it seems silly to make a combo an ArduinoObject, maybe so...
      comboMappings.add(new JLabel(e.getKey().toString()));
      comboMappings.add(new JButton("change"));
      comboMappings.add(new JLabel(e.getValue().toString()));
      comboMappings.add(new JButton("x"));
    }
    comboMappings.add(new JButton("capture combo"));
    comboMappings.add(new JButton("select action"));
    comboSection.add(comboMappings, BorderLayout.SOUTH);
    
    listsOfThingsHappening.add(serialCommunication.whatItSees());
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
