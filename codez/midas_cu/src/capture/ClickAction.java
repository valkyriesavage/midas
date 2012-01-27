package capture;

import java.awt.AWTException;
import java.awt.Point;

public class ClickAction extends RobotAction implements UIAction {
  
  Point p;
  int buttons;
  
  public ClickAction(Point p, int buttons) throws AWTException {
    super();
    this.p = p;
    this.buttons = buttons;
  }

  public void doAction() {
    robot.mouseMove(p.x, p.y);
    robot.mousePress(buttons);
  }

}
