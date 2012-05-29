package display;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;

import serialtalk.ArduinoSetup;
import util.ExtensionFileFilter;
import bridge.ArduinoToDisplayBridge;

public class CanvasPanel extends JPanel implements MouseListener, MouseMotionListener, KeyListener {
  private static final long serialVersionUID = 7046692110388368464L;
  
  public static final Color COPPER = new Color(204, 76, 22);
  public static final Color LIGHT_COPPER = new Color(236, 112, 20);
  public static final Color DARK_COPPER = new Color(140, 45, 4);
  
  List<SensorButtonGroup> displayedButtons;
  List<File> displayedCustomButtons;
  SetUp setUp;
  
  private SensorButtonGroup draggingGroup;
  
  private int keyCodePressed = 0;
  private Point prevPoint;
  
  public boolean isInteractive = true;
  
  BufferedImage templateImage;

  public CanvasPanel(SetUp setUp, List<SensorButtonGroup> buttonsToDisplay) {
    super();
    this.setUp = setUp;
    displayedButtons = buttonsToDisplay;
    setSize(SetUp.CANVAS_X, SetUp.CANVAS_Y);
    setPreferredSize(new Dimension(SetUp.CANVAS_X, SetUp.CANVAS_Y));
    setVisible(true);
    this.addMouseListener(this);
    this.addMouseMotionListener(this);
    this.setFocusable(true);
    this.addKeyListener(this);
    
    setTemplateImage(new File("src/display/images/musicpage_template.png"));
  }
  
  @Override
  public void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D) g;
    
    super.paintComponent(g2);

      g2.drawImage(templateImage, 0, 0, Color.BLACK, new ImageObserver() {
        public boolean imageUpdate(Image img, int infoflags, int x, int y,
            int width, int height) {
          return false;
        }
      });

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
  
  JButton templateButton() {
    JButton button = new JButton("load new template image");
    button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        JFileChooser fc = new JFileChooser("src/display/images");
        fc.setFileFilter(new ExtensionFileFilter("images", new String[] {
            "JPG", "jpg", "JPEG", "jpeg", "GIF", "gif", "BMP", "bmp", "PNG", "png" }));
        int returnVal = fc.showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
          File selectedFile = fc.getSelectedFile();
          setTemplateImage(selectedFile);
        }
      }
    });
    return button;
  }
  
  private void setTemplateImage(File fileLocation) {
    try{
      templateImage = ImageIO.read(fileLocation);
      // create a media tracker to track the loading of
      // my_image. my_component cannot be null, set it
      // to the component where you will draw the image
      // or to the main Frame in your application
      MediaTracker media_tracker = new MediaTracker(this);

      // add your image to the tracker with an arbitrary id
      int id = 0;
      media_tracker.addImage(templateImage,id);

      // try to wait for image to be loaded
      // catch if loading was interrupted
      try
      {
        media_tracker.waitForID(id);
      }
      catch(InterruptedException e)
      {
        System.out.println("Image loading interrupted : " + e);
      }
      
      SetUp.CANVAS_X = templateImage.getWidth(null);
      SetUp.CANVAS_Y = templateImage.getHeight(null);
      setSize(SetUp.CANVAS_X, SetUp.CANVAS_Y);
      setPreferredSize(new Dimension(SetUp.CANVAS_X, SetUp.CANVAS_Y));
    } catch (IOException ioe) {
      // well, poop
      ioe.printStackTrace();
    }
    setUp.repaint();
    repaint();
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
    
    prevPoint = event.getPoint();
  }

  @Override
  public void mouseReleased(MouseEvent event) {
    draggingGroup = null;
  }
  
  @Override
  public void mouseEntered(MouseEvent event) {
    requestFocus();
  }

  @Override
  public void mouseExited(MouseEvent event) { }

  @Override
  public void mouseDragged(MouseEvent event) {
    if (!isInteractive) { return; }

    if(draggingGroup != null) {
      if (keyCodePressed == KeyEvent.VK_SHIFT) {
        if (isFurtherFromCenterOf(prevPoint, event.getPoint(), draggingGroup)) {
          draggingGroup.larger();
        } else {
          draggingGroup.smaller();
        }
      } else {
        draggingGroup.moveTo(event.getPoint());
      }
      repaint();
    }
    for(ArduinoToDisplayBridge bridge : setUp.bridgeObjects) {
      if (bridge.interfacePiece == draggingGroup) {
        setUp.setSelectedBridge(bridge);
      }
    }
    
    prevPoint = event.getPoint();
  }

  @Override
  public void mouseMoved(MouseEvent event) { }

  @Override
  public void keyPressed(KeyEvent event) {
    keyCodePressed = event.getKeyCode();
  }

  @Override
  public void keyReleased(KeyEvent event) {
    keyCodePressed = 0;
  }
  
  @Override
  public void keyTyped(KeyEvent event) { }
  
  public boolean isFurtherFromCenterOf(Point prevPoint, Point curPoint, SensorButtonGroup draggingGroup) {
    return (prevPoint.distance(draggingGroup.center()) < curPoint.distance(draggingGroup.center()));
  }

}
