package display;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class CanvasPanel extends JPanel implements MouseListener {
  private static final long serialVersionUID = 7046692110388368464L;
  
  public static final Color COPPER = new Color(184,115,51);
  
  List<SensorButtonGroup> displayedButtons;

  public CanvasPanel(List<SensorButtonGroup> buttonsToDisplay) {
    super();
    displayedButtons = buttonsToDisplay;
    setSize(SetUp.CANVAS_X, SetUp.CANVAS_Y);
    setPreferredSize(new Dimension(SetUp.CANVAS_X, SetUp.CANVAS_Y));
    setVisible(true);
    this.addMouseListener(this);
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
          System.out.println("we have some info?");
          return false;
        }
      });
    } catch (IOException e) {
      // well, poop
      e.printStackTrace();
    }
    
    for (SensorButtonGroup sbg : displayedButtons) {
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
  public void mouseClicked(MouseEvent arg0) {
    // TODO Auto-generated method stub
    
  }
  
  @Override
  public void mousePressed(MouseEvent arg0) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void mouseReleased(MouseEvent arg0) {
    // TODO Auto-generated method stub
    
  }
  
  @Override
  public void mouseEntered(MouseEvent arg0) { }

  @Override
  public void mouseExited(MouseEvent arg0) { }

}
