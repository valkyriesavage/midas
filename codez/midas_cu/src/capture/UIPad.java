package capture;

import java.awt.AWTException;
import java.awt.Point;
import java.awt.event.InputEvent;
import java.util.List;

import org.jnativehook.GlobalScreen;

public class UIPad {
  public Point topLeftOfPad, bottomRightOfPad;
  public Integer sensitivity;
  private InputCapturer capturer;
  
  public boolean isRecording = false;
  
  public UIPad(Integer sensitivity) {
    this.sensitivity = sensitivity;
  }
  
  public void execute(Point whichPad) {
    MousePressAction action;
    // also no idea why this has to be cast to int twice...
    int clickX = (int) ((int)(bottomRightOfPad.x-topLeftOfPad.x)/(Math.sqrt(sensitivity)-1)*whichPad.x+topLeftOfPad.x);
    int clickY = (int) ((int)(topLeftOfPad.y - bottomRightOfPad.y)/(Math.sqrt(sensitivity)-1)*whichPad.y+bottomRightOfPad.y);
    
    Point clickPoint = new Point(clickX, clickY);
    try {
      action = new MousePressAction(clickPoint, InputEvent.BUTTON1_MASK);
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
    MousePressAction oneClick = (MousePressAction)actions.get(0);
    MousePressAction otherClick = (MousePressAction)actions.get(actions.size() - 2);
    
    Point topClick;
    Point bottomClick;
    if(oneClick.p.x < otherClick.p.x && oneClick.p.y < otherClick.p.y) {
      //oneClick is in top left, otherClick is in bottom right
      topClick = oneClick.p;
      bottomClick = otherClick.p;
    } else if (oneClick.p.x > otherClick.p.x && oneClick.p.y > otherClick.p.y) {
      //oneClick is in bottom right, otherClick is in top left
      topClick = otherClick.p;
      bottomClick = oneClick.p;
    } else if (oneClick.p.y > otherClick.p.y) {
      //oneClick is in bottom left, otherClick is in top right
      topClick = new Point(oneClick.p.x, otherClick.p.y);
      bottomClick = new Point(otherClick.p.x, oneClick.p.y);
    } else {
      //oneClick is in top right, otherClick is in bottom left
      topClick = new Point(otherClick.p.x, oneClick.p.y);
      bottomClick = new Point(oneClick.p.x, otherClick.p.y);
    }
    
    topLeftOfPad = topClick;
    bottomRightOfPad = bottomClick;

    GlobalScreen.getInstance().removeNativeMouseListener(capturer);
    capturer = null;
    isRecording = false;
  }
}
