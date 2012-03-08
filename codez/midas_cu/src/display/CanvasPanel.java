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

import bridge.ArduinoToDisplayBridge;

public class CanvasPanel extends JPanel implements MouseListener, MouseMotionListener {
  private static final long serialVersionUID = 7046692110388368464L;
  
  public static final Color COPPER = new Color(184,115,51);
  
  List<SensorButtonGroup> displayedButtons;
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
      templateImage = ImageIO.read(new File(SetUp.PROJ_HOME + "display/images/iPhone_template.png"));
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
    
    for (SensorButtonGroup sbg : displayedButtons) {
      sbg.setIntersecting(false);
      for (SensorButtonGroup intersecting : displayedButtons) {
        if (sbg == intersecting) {
          continue;
        }
        if (sbg.intersects(intersecting.getBounds())) {
          sbg.setIntersecting(true);
          intersecting.setIntersecting(true);
        }
      }
      sbg.paint(g2);
    }
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
