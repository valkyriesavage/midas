package capture;

import java.awt.AWTException;
import java.awt.Point;
import java.awt.Robot;

public class DragAction extends RobotAction implements UIAction {
  
  Point start;
  Point end;
  int buttons;
  
  public DragAction(Point start, Point end, int buttons) throws AWTException {
    super();
    this.start = start;
    this.end = end;
    this.buttons = buttons;
  }

  public void doAction() {
    robot.mouseMove(start.x, start.y);
    robot.mousePress(buttons);
    robot.mouseMove(end.x, end.y);
    robot.mouseRelease(buttons);
  }

}
