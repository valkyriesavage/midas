package capture;

import java.awt.AWTException;
import java.awt.Point;
import java.awt.Robot;

public class ClickAction implements UIAction {
  private static Robot robot;
  
  Point start;
  Point end;
  int buttons;
  
  public ClickAction(Point start, Point end, int buttons) throws AWTException {
    if (robot == null) {
      robot = new Robot();
    }
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
