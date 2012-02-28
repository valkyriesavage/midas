package capture;

import java.awt.AWTException;
import java.awt.Point;
import java.awt.event.InputEvent;

public class MousePressAction extends RobotAction implements UIAction {
  
  Point p;
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
}