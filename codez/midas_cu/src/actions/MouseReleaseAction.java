package actions;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;

public class MouseReleaseAction extends RobotAction implements UIAction {
  
  Point p;
  int buttons;
  
  public MouseReleaseAction(Point p, int buttons) throws AWTException {
    super();
    this.p = p;
    this.buttons = RobotAction.cleanMouseButtons(buttons);
  }

  public void doAction() {
    robot.mouseMove(p.x, p.y);
    robot.mouseRelease(InputEvent.BUTTON1_MASK);
  }
  
  public String toString() {
    return "";
  }
  
  public ImageIcon icon() {
    try {
      BufferedImage image = new Robot().createScreenCapture(new Rectangle(new Point(p.x - 10, p.y - 10), 
                                                            new Dimension(20, 20)));
      return new ImageIcon(image);
    } catch (AWTException e) {
      e.printStackTrace();
    }
    return new ImageIcon();
  }
}
