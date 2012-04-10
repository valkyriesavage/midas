package display;

/**
 * TODO
 *  make sure gridded registration works
 *  enforce single hellaslider
 */

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.Border;

import pathway.SVGPathwaysGenerator;
import serialtalk.ArduinoEvent;
import serialtalk.ArduinoSetup;
import serialtalk.SerialCommunication;
import serialtalk.TouchDirection;
import util.ExtensionFileFilter;
import bridge.ArduinoToButtonBridge;
import bridge.ArduinoToDisplayBridge;
import bridge.ArduinoToPadBridge;
import bridge.ArduinoToSliderBridge;
import display.SensorShape.shapes;

public class SetUp extends JFrame {
  private static final long serialVersionUID = -7176602414855781819L;

  public static final int CANVAS_X = 300;
  public static final int CANVAS_Y = 520;

  public static final Integer HELLA_SLIDER = 256;
  public static final Integer[] SLIDER_SENSITIVITIES = { 3, 4, 5, 6, 7, 8,
      HELLA_SLIDER };
  public static final Integer[] PAD_SENSITIVITIES = { 4, 9, 16, 25, 36 };

  SerialCommunication serialCommunication;

  JPanel buttonDisplayGrid = new JPanel();
  List<SensorButtonGroup> displayedButtons = new ArrayList<SensorButtonGroup>();
  CanvasPanel buttonCanvas = new CanvasPanel(this, displayedButtons);
  public List<ArduinoToDisplayBridge> bridgeObjects;
  JPanel buttonCreatorPanel = new JPanel();
  JPanel listsOfThingsHappening = new JPanel();
  JPanel propertiesPane = new JPanel();
  JPanel tempButtonDisplay = new JPanel();
  JCheckBox generatePathways = new JCheckBox("generate traces", true);

  SVGPathwaysGenerator pathwaysGenerator = new SVGPathwaysGenerator(this);

  SensorShape.shapes queuedShape;

  public ArduinoToDisplayBridge currentBridge;
  
  Border blackline = BorderFactory.createLineBorder(Color.black);

  public SetUp(boolean test) throws AWTException {
    setSize(CANVAS_X + 350, CANVAS_Y + 100);
    setTitle("Midas");

    serialCommunication = new SerialCommunication();
    serialCommunication.initialize(test);
    bridgeObjects = serialCommunication.bridgeObjects;
    ArduinoToDisplayBridge.setRepainter(this);

    setLayout(new BorderLayout());

    cleanInterface();

    getContentPane().setVisible(true);
  }

  private void cleanInterface() {
    setUpTheGrid();
    add(buttonDisplayGrid, BorderLayout.WEST);

    setUpButtonCreator();
    prepPropertiesPane();
    JPanel holder = new JPanel(new BorderLayout());
    holder.add(propertiesPane, BorderLayout.NORTH);
    holder.add(buttonCreatorPanel, BorderLayout.SOUTH);
    add(holder, BorderLayout.EAST);

    //add(serialCommunication.whatISee(), BorderLayout.SOUTH);
  }

  private void setUpTheGrid() {
    buttonDisplayGrid.setVisible(false);
    buttonDisplayGrid.setSize(CANVAS_X, 600);
    buttonDisplayGrid.setLayout(new BorderLayout());
    buttonDisplayGrid.add(buttonCanvas, BorderLayout.NORTH);
    buttonDisplayGrid.setVisible(true);
  }

  public void paintComponent(Graphics2D g) {
    super.paint(g);
    buttonCanvas.paint(g);
    pathwaysGenerator.paint(g);
  }

  private void setUpButtonCreator() {
    buttonCreatorPanel.removeAll();
    buttonCreatorPanel.setLayout(new GridLayout(0, 1));
    
    JPanel templatePanel = new JPanel(new GridLayout(0,1));
    JPanel holder = new JPanel();
    JButton templateButton = new JButton("load new template image");
    holder.add(templateButton);
    templatePanel.add(holder);

    JPanel addStockButtonPanel = new JPanel();
    JComboBox shapeChooser = new JComboBox(SensorShape.shapesList);
    shapeChooser.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent event) {
        if (event.getStateChange() == ItemEvent.SELECTED) {
          queuedShape = ((SensorShape) ((JComboBox) event.getSource())
              .getSelectedItem()).shape;
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
        } else if (queuedShape == shapes.PAD) {
          newButton.isPad = true;
          newBridge = new ArduinoToPadBridge(PAD_SENSITIVITIES[0]);
        } else { // it is a button
          newBridge = new ArduinoToButtonBridge();
        }
        displayedButtons.add(newButton);
        newBridge.setInterfacePiece(newButton);
        if (queuedShape != shapes.PAD && queuedShape != shapes.SLIDER) {
          // this is definitely bad coding practice, but we have to get the
          // single buttons drawing still
          newBridge.interfacePiece.setSensitivity(1);
        }
        bridgeObjects.add(newBridge);
        setSelectedBridge(newBridge);
        repaint();
      }
    });
    addStockButtonPanel.add(addStock);
    templatePanel.add(addStockButtonPanel);

    JPanel addCustomButtonPanel = new JPanel();
    JButton addCustom = new JButton("add custom button");
    addCustom.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new ExtensionFileFilter("PNG images", new String[] {
            "PNG", "png" }));
        int returnVal = fc.showOpenDialog(SetUp.this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
          File customImage = fc.getSelectedFile();
          cleanUpDeletions();
          SensorButtonGroup newButton;
          try {
            newButton = new SensorButtonGroup(ImageIO.read(customImage),
                customImage.getName());
          } catch (IOException ioe) {
            newButton = new SensorButtonGroup(shapes.SQUARE);
            ioe.printStackTrace();
          }
          ArduinoToDisplayBridge newBridge = new ArduinoToButtonBridge();
          newBridge.isCustom = true;
          displayedButtons.add(newButton);
          newBridge.setInterfacePiece(newButton);
          newBridge.interfacePiece.setSensitivity(1);
          bridgeObjects.add(newBridge);
          setSelectedBridge(newBridge);
          repaint();
        }
      }
    });
    addCustomButtonPanel.add(addCustom);
    templatePanel.add(addCustomButtonPanel);

    JPanel printingPanel = new JPanel();
    JButton printSensors = new JButton("print sensors");
    printSensors.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        cleanUpDeletions();
        if (displayedButtons.size() > 0) {
        	generatePathways();
          // code related to desktop inspired by johnbokma.com
          if (!Desktop.isDesktopSupported()) {
            System.err.println("Can't get browser opener");
          }
          Desktop desktop = Desktop.getDesktop();
          if (!desktop.isSupported(Desktop.Action.BROWSE)) {
            System.err.println("Can't open browser");
          }

          try {
            desktop.browse(generateInstructionsPage());
          } catch (IOException e) {
            e.printStackTrace();
          }
          
          setSelectedBridge(currentBridge);

        } else {
          JOptionPane.showMessageDialog(null, "there are no buttons to print!",
              "no buttons to print", JOptionPane.ERROR_MESSAGE);
        }
      }
    });
    printingPanel.add(printSensors);
    printingPanel.add(generatePathways);
    printingPanel.setBorder(BorderFactory.createTitledBorder("print"));
    
    templatePanel.setBorder(BorderFactory.createTitledBorder("sensors"));

    buttonCreatorPanel.add(templatePanel);
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
  }

  public void generatePathways() {
    pathwaysGenerator.generatePathways(displayedButtons, generatePathways.isSelected());
    
    if(generatePathways.isSelected()) {
      assignArduinoConnectionsFromSVG();
      buttonCanvas.isInteractive = false;
    }
  }
  
  public void assignArduinoConnectionsFromSVG() {
    // we are cheating; we know that the SVG just assigns based on the order in which the buttons appear...
    Map<ArduinoSensorButton, Integer> buttonMap = pathwaysGenerator.getButtonMap();
    Map<ArduinoToDisplayBridge, List<ArduinoEvent>> sensorsToAssign = new HashMap<ArduinoToDisplayBridge, List<ArduinoEvent>>();

    // build a list of which sensors go to which bridges
    for (Map.Entry<ArduinoSensorButton, Integer> entry : buttonMap.entrySet()) {
    	ArduinoSensorButton button = entry.getKey();
    	int i = entry.getValue();
      for (ArduinoToDisplayBridge bridge : bridgeObjects) {
        if (bridge.contains(button)) {
          ArduinoEvent event = new ArduinoEvent(ArduinoSetup.sensors[i], TouchDirection.TOUCH);
          
          if (sensorsToAssign.containsKey(bridge)) {
            sensorsToAssign.get(bridge).add(event);
          }
          else {
            List<ArduinoEvent> assignToBridge = new ArrayList<ArduinoEvent>();
            assignToBridge.add(event);
            sensorsToAssign.put(bridge, assignToBridge);
          }
          
        }
      }
    }
    
    // assign those sensors to those bridges (because we have to do this in chunks, sigh
    for (ArduinoToDisplayBridge bridge : bridgeObjects) {
      bridge.setArduinoSequence(sensorsToAssign.get(bridge));
    }
  }

  private URI generateInstructionsPage() {
    try {
      File temp = File.createTempFile("midas_cu", ".html");
      temp.deleteOnExit();

      BufferedWriter out = new BufferedWriter(new FileWriter(temp));
      out.write(InstructionsGenerator.instructions(false, !generatePathways.isSelected()));
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

  private ActionListener refreshSelected() {
    return new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        setSelectedBridge(currentBridge);
      }
    };
  }

  private JComponent placeholder() {
    return new JLabel("");
  }

  public void setSelectedBridge(ArduinoToDisplayBridge bridge) {
    currentBridge = bridge;

    for (ArduinoToDisplayBridge notSelected : bridgeObjects) {
      notSelected.interfacePiece.setSelected(false);
    }
    bridge.interfacePiece.setSelected(true);
    repaint();

    propertiesPane.setVisible(false);
    propertiesPane.removeAll();
    JComboBox interactionType = bridge.chooseInteractionType();
    interactionType.addActionListener(refreshSelected());
    
    if (bridge.isCustom) {
      ArduinoToButtonBridge buttonBridge = (ArduinoToButtonBridge) bridge;
      propertiesPane.add(new JLabel("name"));
      propertiesPane.add(buttonBridge.interfacePiece.nameField);

      propertiesPane.add(new JLabel("interaction type"));
      propertiesPane.add(interactionType);

      propertiesPane.add(placeholder());
      propertiesPane.add(buttonBridge.interactionSetter());
      
      propertiesPane.add(placeholder());
      propertiesPane.add(buttonBridge.goButton());

      propertiesPane.add(buttonBridge.setArduinoSequenceButton());
      JButton delete = buttonBridge.interfacePiece.delete;
      delete.addActionListener(repainter());
      propertiesPane.add(delete);
    } else if (bridge.interfacePiece.isSlider) {
      ArduinoToSliderBridge sliderBridge = (ArduinoToSliderBridge) bridge;
      propertiesPane.add(new JLabel("name"));
      propertiesPane.add(sliderBridge.interfacePiece.nameField);
      
      propertiesPane.add(new JLabel("interaction type"));
      propertiesPane.add(interactionType);

      propertiesPane.add(placeholder());
      propertiesPane.add(sliderBridge.interactionSetter());
      propertiesPane.add(placeholder());
      propertiesPane.add(new JLabel("current:"));
      propertiesPane.add(placeholder());
      propertiesPane.add(sliderBridge.interactionDisplay());
      propertiesPane.add(placeholder());
      propertiesPane.add(sliderBridge.goButton());

      propertiesPane.add(new JLabel("sensitivity"));
      JComboBox sensitivityBox = sliderBridge.sliderSensitivityBox();
      sensitivityBox.addActionListener(repainter());
      propertiesPane.add(sensitivityBox);

      propertiesPane.add(new JLabel("orientation"));
      JButton orientationFlip = sliderBridge.interfacePiece.orientationFlip;
      orientationFlip.addActionListener(repainter());
      propertiesPane.add(orientationFlip);

      propertiesPane.add(new JLabel("registration"));
      JButton seqButton = sliderBridge.setArduinoSequenceButton();
      seqButton.addActionListener(repainter());
      propertiesPane.add(seqButton);
      propertiesPane.add(placeholder());
      propertiesPane.add(new JLabel("current:"));
      propertiesPane.add(placeholder());
      propertiesPane.add(sliderBridge.colorBar());
      
      propertiesPane.add(placeholder());
      JButton delete = sliderBridge.interfacePiece.delete;
      delete.addActionListener(repainter());
      propertiesPane.add(delete);
    } else if (bridge.interfacePiece.isPad) {
      ArduinoToPadBridge padBridge = (ArduinoToPadBridge) bridge;
      propertiesPane.add(new JLabel("name"));
      propertiesPane.add(padBridge.interfacePiece.nameField);

      propertiesPane.add(new JLabel("interaction type"));
      propertiesPane.add(interactionType);

      propertiesPane.add(placeholder());
      propertiesPane.add(padBridge.interactionSetter());
      propertiesPane.add(placeholder());
      propertiesPane.add(new JLabel("current:"));
      propertiesPane.add(placeholder());
      propertiesPane.add(padBridge.interactionDisplay());
      
      propertiesPane.add(placeholder());
      propertiesPane.add(padBridge.goButton());

      propertiesPane.add(new JLabel("sensitivity"));
      JComboBox sensitivityBox = padBridge.padSensitivityBox();
      sensitivityBox.addActionListener(repainter());
      propertiesPane.add(sensitivityBox);

      propertiesPane.add(new JLabel("registration"));
      JButton seqButton = padBridge.setArduinoSequenceButton();
      seqButton.addActionListener(repainter());
      propertiesPane.add(seqButton);
      propertiesPane.add(placeholder());
      propertiesPane.add(new JLabel("current:"));
      propertiesPane.add(placeholder());
      propertiesPane.add(padBridge.colorBar());
      
      propertiesPane.add(placeholder());
      JButton delete = padBridge.interfacePiece.delete;
      delete.addActionListener(repainter());
      propertiesPane.add(delete);
    } else {
      ArduinoToButtonBridge buttonBridge = (ArduinoToButtonBridge) bridge;
      propertiesPane.add(new JLabel("name"));
      propertiesPane.add(buttonBridge.interfacePiece.nameField);

      propertiesPane.add(new JLabel("interaction type"));
      propertiesPane.add(interactionType);

      propertiesPane.add(placeholder());
      propertiesPane.add(buttonBridge.interactionSetter());
      propertiesPane.add(placeholder());
      propertiesPane.add(new JLabel("current:"));
      propertiesPane.add(placeholder());
      propertiesPane.add(buttonBridge.interactionDisplay());
      
      propertiesPane.add(placeholder());
      propertiesPane.add(buttonBridge.goButton());

      propertiesPane.add(new JLabel("registration"));
      JButton seqButton = buttonBridge.setArduinoSequenceButton();
      seqButton.addActionListener(repainter());
      propertiesPane.add(seqButton);
      propertiesPane.add(placeholder());
      propertiesPane.add(new JLabel("current:"));
      propertiesPane.add(placeholder());
      propertiesPane.add(buttonBridge.colorBar());
      
      propertiesPane.add(placeholder());
      JButton delete = buttonBridge.interfacePiece.delete;
      delete.addActionListener(repainter());
      propertiesPane.add(delete);
    }
    propertiesPane.setVisible(true);
  }

  private void prepPropertiesPane() {
    propertiesPane.setVisible(false);
    propertiesPane.removeAll();
    propertiesPane.setSize(300, 300);
    propertiesPane.setPreferredSize(new Dimension(300, 300));
    propertiesPane.setLayout(new GridLayout(0, 2,4,4));
    propertiesPane.setVisible(true);
    propertiesPane.setBorder(BorderFactory.createTitledBorder("properties"));
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
