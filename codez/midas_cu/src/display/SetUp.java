package display;

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
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.Border;

import pathway.Pair;
import pathway.SVGPathwaysGenerator;
import serialtalk.ArduinoDispatcher;
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

  public static int CANVAS_X = 500;
  public static int CANVAS_Y = 600;

  public static final int MAX_OUTS_FOR_CHIP = 7;

  public static final Integer HELLA_SLIDER = 256;
  public static final Integer[] SLIDER_SENSITIVITIES = { 3, 4, 5, 6, 7, 8,
      HELLA_SLIDER };
  public static final Integer[] PAD_SENSITIVITIES = { 4, 9, 16, 25, 36 };

  private static final String HTML_HEAD = "<html><body width='200'>";
  private static final String HTML_TAIL = "</body></html>";

  SerialCommunication serialCommunication;

  JPanel buttonDisplayGrid = new JPanel();
  List<SensorButtonGroup> displayedButtons = new ArrayList<SensorButtonGroup>();
  CanvasPanel buttonCanvas = new CanvasPanel(this, displayedButtons);
  public List<ArduinoToDisplayBridge> bridgeObjects;
  JPanel buttonCreatorPanel = new JPanel();
  JPanel newButtonsPanel;
  JPanel listsOfThingsHappening = new JPanel();
  JPanel propertiesPane = new JPanel();
  JPanel tempButtonDisplay = new JPanel();
  JCheckBox generatePathways = new JCheckBox("generate traces", true);

  SVGPathwaysGenerator pathwaysGenerator = new SVGPathwaysGenerator(this);

  SensorShape.shapes queuedShape;

  public ArduinoToDisplayBridge currentBridge;

  Border blackline = BorderFactory.createLineBorder(Color.black);

  public SetUp(boolean test) throws AWTException {
    setSize(CANVAS_X + 350, CANVAS_Y + 135);
    setTitle("Midas");

    serialCommunication = new SerialCommunication();
    serialCommunication.initialize(test, null);
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

    // add(serialCommunication.whatISee(), BorderLayout.SOUTH);
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

    newButtonsPanel = new JPanel(new GridLayout(0, 1));
    JPanel holder = new JPanel();
    holder.add(buttonCanvas.templateButton());
    newButtonsPanel.add(holder);

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

        int buttonCount = 0;
        boolean hellaSliderFlag = false;
        for (ArduinoToDisplayBridge bridge : bridgeObjects) {
          if (bridge.isHellaSlider) {
            if (hellaSliderFlag) {
              JOptionPane
                  .showMessageDialog(
                      null,
                      HTML_HEAD
                          + "you may only have one continuous slider!<br/>please make one a discrete slider."
                          + HTML_TAIL, "too many continuous sliders",
                      JOptionPane.ERROR_MESSAGE);
              return;
            }
            hellaSliderFlag = true;
            continue;
          }
          buttonCount += bridge.interfacePiece.sensitivity;
        }
        if (buttonCount >= MAX_OUTS_FOR_CHIP) {
          JOptionPane.showMessageDialog(null, HTML_HEAD
              + "you have too many buttons.  this chip only supports "
              + MAX_OUTS_FOR_CHIP
              + ".<br/>please delete some buttons before you add more."
              + HTML_TAIL, "pad registration instructions",
              JOptionPane.INFORMATION_MESSAGE);
          return;
        }

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
        
        if (queuedShape == shapes.POLYGON) {
          JOptionPane
          .showMessageDialog(
              null,
              HTML_HEAD
                  + "define a polygon by clicking where you want vertices.<br/>finalize a polygon by clicking on the first point."
                  + HTML_TAIL, "polygon definition instructions",
              JOptionPane.INFORMATION_MESSAGE);
          buttonCanvas.polyDefinitionMode();
        }
      }
    });
    addStockButtonPanel.add(addStock);
    newButtonsPanel.add(addStockButtonPanel);

    JPanel addCustomButtonPanel = new JPanel();
    JButton addCustom = new JButton("custom sticker shape");
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
    newButtonsPanel.add(addCustomButtonPanel);

    JPanel printingPanel = new JPanel();
    JButton printSensors = new JButton("create stickers");
    printSensors.addActionListener(new ActionListener() {
      private void recursiveDisable(JComponent j) {
        for(int i=0; i < j.getComponents().length; i++) {
          j.getComponent(i).setEnabled(false);
          try {
            recursiveDisable((JComponent) j.getComponent(i));
          } catch (ClassCastException e) {
            continue;
          }
        }
      }
      
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
          
          recursiveDisable(newButtonsPanel);

        } else {
          JOptionPane.showMessageDialog(null,
              "there are no stickers to print!", "no stickers to print",
              JOptionPane.ERROR_MESSAGE);
        }
      }
    });
    printingPanel.add(printSensors);
    printingPanel.add(generatePathways);
    printingPanel
        .setBorder(BorderFactory.createTitledBorder("create stickers"));
    JPanel printingPanelContainer = new JPanel(new GridLayout(0, 1));
    printingPanelContainer.add(printingPanel);

    JButton reconnectDongle = new JButton("connect/reconnect dongle");
    reconnectDongle.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        ArduinoDispatcher dispatcher = serialCommunication.dispatcher;
        serialCommunication.close();
        serialCommunication = new SerialCommunication();
        try {
          serialCommunication.initialize(false, dispatcher);
        } catch (AWTException e) {
          e.printStackTrace();
        }
      }
    });
    JPanel dongleContainer = new JPanel(new GridLayout(0,1));
    dongleContainer.setBorder(BorderFactory.createTitledBorder("dongle"));
    JPanel dongleButtonContainer = new JPanel();
    dongleButtonContainer.add(reconnectDongle);
    JPanel stupidThingForGettingNewline = new JPanel();
    stupidThingForGettingNewline.add(serialCommunication.whatISee());
    dongleContainer.add(dongleButtonContainer);
    dongleContainer.add(stupidThingForGettingNewline);
    printingPanelContainer.add(dongleContainer);

    newButtonsPanel
        .setBorder(BorderFactory.createTitledBorder("design stickers"));

    buttonCreatorPanel.add(newButtonsPanel);
    buttonCreatorPanel.add(printingPanelContainer);
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
    List<SensorButtonGroup> obstacles = new ArrayList<SensorButtonGroup>();
    List<SensorButtonGroup> buttonsToRoute = new ArrayList<SensorButtonGroup>();

    for (SensorButtonGroup group : displayedButtons) {
      if (group.isObstacle()) {
        obstacles.add(group);
      } else {
        buttonsToRoute.add(group);
      }
    }
    boolean success = pathwaysGenerator.generatePathways(buttonsToRoute, obstacles,
        generatePathways.isSelected());

    if (generatePathways.isSelected() && success) {
      assignArduinoConnectionsFromSVG();
      buttonCanvas.isInteractive = false;
    }
  }

  public void assignArduinoConnectionsFromSVG() {
    // we are cheating; we know that the SVG just assigns based on the order in
    // which the buttons appear...
    Map<ArduinoSensorButton, Integer> buttonMap = pathwaysGenerator
        .getButtonMap();
    Map<ArduinoSensorButton, Pair<Integer, Integer>> buttonPadMap = pathwaysGenerator.getButtonPadMap();
    Map<ArduinoToDisplayBridge, List<ArduinoEvent>> sensorsToAssign = new HashMap<ArduinoToDisplayBridge, List<ArduinoEvent>>();

    // build a list of which sensors go to which bridges
    for (Map.Entry<ArduinoSensorButton, Integer> entry : buttonMap.entrySet()) {
      ArduinoSensorButton button = entry.getKey();
      int i = entry.getValue() - 1; // we need to 0-index here
      for (ArduinoToDisplayBridge bridge : bridgeObjects) {
        if (bridge.contains(button)) {
          ArduinoEvent event = new ArduinoEvent(ArduinoSetup.sensors[i],
              TouchDirection.TOUCH);
          if (sensorsToAssign.containsKey(bridge)) {
            sensorsToAssign.get(bridge).add(event);
          } else {
            List<ArduinoEvent> assignToBridge = new ArrayList<ArduinoEvent>();
            assignToBridge.add(event);
            sensorsToAssign.put(bridge, assignToBridge);
          }

        }
      }
    }
        
    for (Map.Entry<ArduinoSensorButton, Pair<Integer, Integer>> entry : buttonPadMap.entrySet()) {
      ArduinoSensorButton button = entry.getKey();
      int x = entry.getValue()._1 - 1; // we need to 0-index here
      int y = entry.getValue()._2 - 1; // we need to 0-index here
      
      if (!ArduinoSetup.griddedSensors.contains(x)) ArduinoSetup.griddedSensors.add(x);
      if (!ArduinoSetup.griddedSensors.contains(y)) ArduinoSetup.griddedSensors.add(y);
      
      for (ArduinoToDisplayBridge bridge : bridgeObjects) {
        if (bridge.contains(button)) {
          ArduinoEvent event = new ArduinoEvent(ArduinoSetup.gridSensors[x][y],
              TouchDirection.TOUCH);
          if (sensorsToAssign.containsKey(bridge)) {
            sensorsToAssign.get(bridge).add(event);
          } else {
            List<ArduinoEvent> assignToBridge = new ArrayList<ArduinoEvent>();
            assignToBridge.add(event);
            sensorsToAssign.put(bridge, assignToBridge);
          }
        }
      }
    }

    // assign those sensors to those bridges (because we have to do this in
    // chunks, sigh
    for (ArduinoToDisplayBridge bridge : bridgeObjects) {
      if (bridge.isHellaSlider) {
        bridge.updateColor();
        continue;
      }
      if (!sensorsToAssign.containsKey(bridge)) {
        // it is an obstacle
        continue;
      }
      bridge.setArduinoSequence(sensorsToAssign.get(bridge));
      bridge.updateColor();
    }
  }

  private URI generateInstructionsPage() {
    try {
      File temp = File.createTempFile("midas", ".html");
      temp.deleteOnExit();

      BufferedWriter out = new BufferedWriter(new FileWriter(temp));
      out.write(InstructionsGenerator.instructions(false,
          !generatePathways.isSelected()));
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

    JRadioButton sensor = new JRadioButton("sensor");
    sensor.setActionCommand("sensor");
    sensor.setSelected(!bridge.isObstacle());
    JRadioButton obstacle = new JRadioButton("obstacle");
    obstacle.setActionCommand("obstacle");
    obstacle.setSelected(bridge.isObstacle());
    
    ButtonGroup group = new ButtonGroup();
    group.add(sensor);
    group.add(obstacle);
    
    ActionListener changeType = new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        currentBridge.setIsObstacle(event.getActionCommand().equals("obstacle"));
        setSelectedBridge(currentBridge);
        repaint();
      }
    };
    
    sensor.addActionListener(changeType);
    obstacle.addActionListener(changeType);

    propertiesPane.add(sensor);
    propertiesPane.add(obstacle);
    
    if (bridge.isObstacle()) {
      JPanel deletePanel = new JPanel();
      JButton delete = bridge.interfacePiece.delete;
      delete.addActionListener(repainter());
      deletePanel.add(delete);
      propertiesPane.add(deletePanel);
    } else if (bridge.isCustom) {
      ArduinoToButtonBridge buttonBridge = (ArduinoToButtonBridge) bridge;
      propertiesPane.add(new JLabel("name"));
      propertiesPane.add(buttonBridge.interfacePiece.nameField);

      propertiesPane.add(new JLabel("interaction"));
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
    } else if (bridge.interfacePiece.isSlider) {
      ArduinoToSliderBridge sliderBridge = (ArduinoToSliderBridge) bridge;
      propertiesPane.add(new JLabel("name"));
      propertiesPane.add(sliderBridge.interfacePiece.nameField);

      propertiesPane.add(new JLabel("interaction"));
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

      propertiesPane.add(new JLabel("interaction"));
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

      propertiesPane.add(new JLabel("interaction"));
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
    propertiesPane.setLayout(new GridLayout(0, 2, 4, 4));
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
