package capture;

import java.awt.Point;
import java.awt.Robot;

public class DragAction implements UIAction {
  Point mouseLocation0;
  Point mouseLocation1;
  int buttonCode;
  
  public DragAction(Point mouseLocation0, Point mouseLocation1, int buttonCode) {
    this.mouseLocation0 = mouseLocation0;
    this.mouseLocation1 = mouseLocation1;
    this.buttonCode = buttonCode;
  }
  
  public void performAction(Robot r) {
    r.mouseMove(mouseLocation0.x, mouseLocation0.y);
    r.mousePress(buttonCode);
    r.mouseMove(mouseLocation1.x, mouseLocation1.y);
    r.mouseRelease(buttonCode);
  }

  public String toString() {
    return new String("drag from " + mouseLocation0 + " to " + mouseLocation1);
  }
}
