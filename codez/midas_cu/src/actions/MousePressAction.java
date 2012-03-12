package actions;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;

public class MousePressAction extends RobotAction implements UIAction {
  
  public Point p;
  // secretly we don't use the captured button at all for the moment... how dumb.
  // TODO (someone) : figure out why using buttons isn't good enough
  int buttons;
  
  public MousePressAction(Point p, int buttons) throws AWTException {
    super();
    this.p = p;
    this.buttons = RobotAction.cleanMouseButtons(buttons);
  }

  public void doAction() {
    robot.mouseMove(p.x, p.y);
    robot.mousePress(InputEvent.BUTTON1_MASK);
  }
  
  public String toString() {
    return "click @ " + p.x + "," + p.y;
  }
  
  public ImageIcon icon() {
    try {
      BufferedImage image = new Robot().createScreenCapture(new Rectangle(new Point(p.x - 20, p.y - 20), 
                                                            new Dimension(40, 40)));
      return new ImageIcon(image);
    } catch (AWTException e) {
      e.printStackTrace();
    }
    return new ImageIcon();
  }
}