package actions;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;

public abstract class RobotAction {

  static protected Robot robot;
  protected BufferedImage screenshot;
  
  protected RobotAction() throws AWTException {
    if (robot == null) {
      robot = new Robot();
      robot.setAutoDelay(50);
    }
  }
  
  protected static int cleanMouseButtons(int buttons) {
    int retButtons = 0;
    if((buttons&InputEvent.BUTTON1_DOWN_MASK) == InputEvent.BUTTON1_DOWN_MASK) {
      retButtons = InputEvent.BUTTON1_MASK;
    }
    else if((buttons&InputEvent.BUTTON2_DOWN_MASK) == InputEvent.BUTTON2_DOWN_MASK) {
      retButtons = InputEvent.BUTTON2_MASK;
    }
    else if((buttons&InputEvent.BUTTON3_DOWN_MASK) == InputEvent.BUTTON3_DOWN_MASK) {
      retButtons = InputEvent.BUTTON3_MASK;
    }
    return retButtons;
  }

  protected void addScreenshot() {
    screenshot = robot.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
  }
}
