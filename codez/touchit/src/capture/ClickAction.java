package capture;

import java.awt.Point;
import java.awt.Robot;

public class ClickAction implements UIAction {
  Point mouseLocation;
  int buttonCode;
  
  public ClickAction(Point mouseLocation, int buttonCode) {
    this.mouseLocation = mouseLocation;
    this.buttonCode = buttonCode;
  }
  
  public void performAction(Robot r) {
    r.mouseMove(mouseLocation.x, mouseLocation.y);
    r.mousePress(buttonCode);
    r.mouseRelease(buttonCode);
  }

  public String toString() {
    return new String("click at " + mouseLocation.toString());
  }
}
