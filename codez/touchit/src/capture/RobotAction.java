package capture;

import java.awt.AWTException;
import java.awt.Robot;

public class RobotAction {

  static protected Robot robot;
  
  protected RobotAction() throws AWTException {
    if (robot == null) {
      robot = new Robot();
    }
  }

}
