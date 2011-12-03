package capture;

import java.awt.AWTException;

public class TypeAction extends RobotAction implements UIAction {
  
  int keyCode;

  public TypeAction(int keyCode) throws AWTException {
    super();
    this.keyCode = keyCode;
  }
  
  public void doAction() {
    robot.keyPress(keyCode);
    robot.keyRelease(keyCode);
  }

}
