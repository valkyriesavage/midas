package capture;

import java.awt.AWTException;
import java.awt.Point;

public class MousePressAction extends RobotAction implements UIAction {
  
  Point p;
  int buttons;
  
  public MousePressAction(Point p, int buttons) throws AWTException {
    super();
    this.p = p;
    this.buttons = RobotAction.cleanMouseButtons(buttons);
  }

  public void doAction() {
    robot.mouseMove(p.x, p.y);
    robot.mousePress(buttons);
  }
  
  public String toString() {
    return "click @ " + p.x + "," + p.y;
  }

}
