package capture;

import java.awt.AWTException;

import javax.swing.KeyStroke;

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
  
  public String toString() {
    return "" + KeyStroke.getKeyStroke(keyCode, 0).getKeyChar();
  }
}
