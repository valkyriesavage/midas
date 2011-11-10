package capture;

import java.awt.Robot;
import java.awt.event.KeyEvent;

public class KeyAction implements UIAction {
  int keyCode;
  
  public KeyAction(int keyCode) {
    this.keyCode = keyCode;
  }
  
  public void performAction(Robot r) {
    r.keyPress(keyCode);
  }

  public String toString() {
    return new String("press " + KeyEvent.getKeyText(keyCode));
  }
}
