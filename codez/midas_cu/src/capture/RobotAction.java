package capture;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.InputEvent;

public abstract class RobotAction {

  static protected Robot robot;
  
  protected RobotAction() throws AWTException {
    if (robot == null) {
      robot = new Robot();
      robot.setAutoDelay(400);
    }
  }
  
  protected static int cleanMouseButtons(int buttons) {
    int retButtons = 0;
    if((buttons&InputEvent.BUTTON1_DOWN_MASK) == InputEvent.BUTTON1_DOWN_MASK) {
      retButtons |= InputEvent.BUTTON1_MASK;
    }
    if((buttons&InputEvent.BUTTON2_DOWN_MASK) == InputEvent.BUTTON2_DOWN_MASK) {
      retButtons |= InputEvent.BUTTON2_MASK;
    }
    if((buttons&InputEvent.BUTTON3_DOWN_MASK) == InputEvent.BUTTON3_DOWN_MASK) {
      retButtons |= InputEvent.BUTTON3_MASK;
    }
    return retButtons;
  }

}
