package capture;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.InputEvent;
import java.util.List;

import org.jnativehook.GlobalScreen;

public class UIPad {
  public Point topLeftOfPad, bottomRightOfPad;
  public Dimension sensitivity;
  private InputCapturer capturer;
  
  public boolean isRecording = false;
  
  public UIPad() {}
  
  public void execute(Point position) {
    MousePressAction action;
    try {
      action = new MousePressAction(topLeftOfPad, InputEvent.BUTTON1_MASK);
      action.doAction();
    } catch (AWTException e) {
      e.printStackTrace();
    }
  }
  
  public String toString() {
    if (topLeftOfPad != null && bottomRightOfPad != null) {
      return "" + topLeftOfPad.x + "," + topLeftOfPad.y + " -> " + bottomRightOfPad.x + "," + bottomRightOfPad.y;
    }
    return "mark pad";
  }
  
  public void record() {
    capturer = new InputCapturer();
    GlobalScreen.getInstance().addNativeMouseListener(capturer);
    isRecording = true;
  }
  
  public void stopRecording() {
    List<UIAction> actions = capturer.reportBack();
    // be sure to pop off the last two events ; that's the click and release where they stopped recording.
    actions.remove(actions.size() - 1);
    actions.remove(actions.size() - 1);
    
    // we want the locations of the clicks of the pad top left and bottom right.  we will assume that they were the first and last clicks.
    MousePressAction topClick = (MousePressAction)actions.get(0);
    MousePressAction bottomClick = (MousePressAction)actions.get(actions.size() - 2);
    topLeftOfPad = topClick.p;
    bottomRightOfPad = bottomClick.p;

    GlobalScreen.getInstance().removeNativeMouseListener(capturer);
    capturer = null;
    isRecording = false;
  }
}
