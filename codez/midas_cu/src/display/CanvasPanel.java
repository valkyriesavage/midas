package display;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import serialtalk.ArduinoSetup;

import bridge.ArduinoToDisplayBridge;

public class CanvasPanel extends JPanel implements MouseListener, MouseMotionListener {
  private static final long serialVersionUID = 7046692110388368464L;
  
  public static final Color COPPER = new Color(184,115,51);
  
  List<SensorButtonGroup> displayedButtons;
  List<File> displayedCustomButtons;
  SetUp setUp;
  
  private SensorButtonGroup draggingGroup;

  public CanvasPanel(SetUp setUp, List<SensorButtonGroup> buttonsToDisplay) {
    super();
    this.setUp = setUp;
    displayedButtons = buttonsToDisplay;
    setSize(SetUp.CANVAS_X, SetUp.CANVAS_Y);
    setPreferredSize(new Dimension(SetUp.CANVAS_X, SetUp.CANVAS_Y));
    setVisible(true);
    this.addMouseListener(this);
    this.addMouseMotionListener(this);
  }
  
  @Override
  public void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D) g;
    
    super.paintComponent(g2);

    BufferedImage templateImage;
    try {
      templateImage = ImageIO.read(new File("src/display/images/nexus_one_template.png"));
      g2.drawImage(templateImage, 0, 0, Color.BLACK, new ImageObserver() {
        public boolean imageUpdate(Image img, int infoflags, int x, int y,
            int width, int height) {
          return false;
        }
      });
    } catch (IOException e) {
      // well, poop
      e.printStackTrace();
    }

    // this is a dumb place to have to do this counting nonsense, but we'll do it anyway!
    int totalButtons = 0;
        
    for (SensorButtonGroup sbg : displayedButtons) {
      sbg.setIntersecting(false);
      
      // count the total number of buttons to see if we need grid sensing
      if (!sbg.deleteMe && !(sbg.sensitivity == SetUp.HELLA_SLIDER)) {
        totalButtons += sbg.sensitivity;
      }
      
      // test for intersections to see if we need to color differently
      for (SensorButtonGroup intersecting : displayedButtons) {
        if (sbg == intersecting || (sbg.isIntersecting() && intersecting.isIntersecting())) {
          continue;
        }
        if (sbg.intersects(intersecting.getBounds())) {
          sbg.setIntersecting(true);
          intersecting.setIntersecting(true);
        }
      }
      
      sbg.paint(g2);
    }
    
    setUp.pathwaysGenerator.paint(g2);

    // do we need grid sensing?
    setUp.serialCommunication.isGridded = (totalButtons > ArduinoSetup.NUM_TERMINALS);
  }
  
  SensorButtonGroup determineIntersection(Point pointClicked) {
    for(SensorButtonGroup sbg : displayedButtons) {
      if (sbg.contains(pointClicked)) {
        return sbg;
      }
    }
    return null;
  }
 
  @Override
  public void mouseClicked(MouseEvent event) {
    SensorButtonGroup intersectedGroup;
    if ((intersectedGroup = determineIntersection(event.getPoint())) != null) {
      // if the mouse clicks, we probably don't want to trigger the event, that would be annoying
      //intersectedGroup.triggerButton.activate();
      // we want to select that one
      for(ArduinoToDisplayBridge bridge : setUp.bridgeObjects) {
        if (bridge.interfacePiece == intersectedGroup) {
          setUp.setSelectedBridge(bridge);
        }
      }
    }
  }
  
  @Override
  public void mousePressed(MouseEvent event) {
    SensorButtonGroup intersectedGroup;
    if ((intersectedGroup = determineIntersection(event.getPoint())) != null) {
      draggingGroup = intersectedGroup;
    }
    for(ArduinoToDisplayBridge bridge : setUp.bridgeObjects) {
      if (bridge.interfacePiece == draggingGroup) {
        setUp.setSelectedBridge(bridge);
      }
    }
  }

  @Override
  public void mouseReleased(MouseEvent event) {
    draggingGroup = null;
  }
  
  @Override
  public void mouseEntered(MouseEvent event) { }

  @Override
  public void mouseExited(MouseEvent event) { }

  @Override
  public void mouseDragged(MouseEvent event) {
    if(draggingGroup != null) {
      draggingGroup.moveTo(event.getPoint());
      repaint();
//      setUp.generatePathways(); //regenerate because something just moved
    }
    for(ArduinoToDisplayBridge bridge : setUp.bridgeObjects) {
      if (bridge.interfacePiece == draggingGroup) {
        setUp.setSelectedBridge(bridge);
      }
    }
  }

  @Override
  public void mouseMoved(MouseEvent event) { }

}
