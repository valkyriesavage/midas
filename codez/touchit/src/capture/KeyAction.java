package capture;

import java.awt.Robot;

public class KeyAction implements UIAction {
  int keyCode;
  
  public KeyAction(int keyCode) {
    this.keyCode = keyCode;
  }
  
  public void performAction(Robot r) {
    r.keyPress(keyCode);
  }

}
