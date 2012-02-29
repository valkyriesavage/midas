package display;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JComponent;

public class CanvasPanel extends JComponent {
  private static final long serialVersionUID = 7046692110388368464L;
  
  List<SensorButtonGroup> displayedButtons;

  public CanvasPanel(List<SensorButtonGroup> buttonsToDisplay) {
    super();
    displayedButtons = buttonsToDisplay;
    setSize(SetUp.CANVAS_X, SetUp.CANVAS_Y);
  }
  
  public void paintComponent(Graphics2D g) {
    super.paintComponent(g);
    g.setColor(Color.white);
    g.fillRect(0, 0, getWidth(), getHeight());
    g.setColor(Color.yellow);
    g.fillRect(10,10,10,10);
    
    BufferedImage templateImage;
    try {
      templateImage = ImageIO.read(new File(SetUp.PROJ_HOME + "display/images/iPhone_template.png"));
      g.drawImage(templateImage, 0, 0, Color.BLACK, new ImageObserver() {
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
    
    g.draw(new Rectangle(0,10,15,15));
    
    for (SensorButtonGroup sbg : displayedButtons) {
      sbg.paint(g);
    }
  }
}
