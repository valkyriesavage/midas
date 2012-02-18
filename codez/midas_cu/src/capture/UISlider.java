package capture;

import java.awt.AWTException;
import java.awt.Point;
import java.awt.event.InputEvent;
import java.util.List;

import org.jnativehook.GlobalScreen;

public class UISlider {
  public Point lowEndOfSlider;
  public Point highEndOfSlider;
  public Integer sensitivity;
  private InputCapturer capturer;
  
  public boolean isRecording = false;
  
  public UISlider(Integer sensitivity) {
    this.sensitivity = sensitivity;
  }
  
  private boolean isHorizontal() {
    return ((highEndOfSlider.y - lowEndOfSlider.y) < (highEndOfSlider.x - lowEndOfSlider.x));
  }
  
  public void execute(int whichPad) {
    Point clickPoint;
    if (this.isHorizontal()) {
      clickPoint = new Point((highEndOfSlider.x-lowEndOfSlider.x)/(sensitivity-1)*whichPad + lowEndOfSlider.x, highEndOfSlider.y);
    } else {
      clickPoint = new Point(highEndOfSlider.x, (highEndOfSlider.y-lowEndOfSlider.y)/(sensitivity-1)*whichPad + lowEndOfSlider.y);
    }
    try {
      MousePressAction action = new MousePressAction(clickPoint, InputEvent.BUTTON1_MASK);
      action.doAction();
      MouseReleaseAction unAction = new MouseReleaseAction(clickPoint, InputEvent.BUTTON1_MASK);
      unAction.doAction();
    } catch (AWTException e) {
      e.printStackTrace();
    }
  }
  
  public String toString() {
    if (lowEndOfSlider != null && highEndOfSlider != null) {
      return "" + lowEndOfSlider.x + "," + lowEndOfSlider.y + " -> " + highEndOfSlider.x + "," + highEndOfSlider.y;
    }
    return "mark slider";
  }
  
  public void record() {
    capturer = new InputCapturer();
    GlobalScreen.getInstance().addNativeMouseListener(capturer);
    isRecording = true;
  }
  
  private boolean firstIsLeftOrDownOfSecond(Point first, Point second) {
    if (Math.abs(first.y - second.y) < Math.abs(first.x - second.x)) {
      // that means it's horizontal, so we need to know if the left one is first
      return (first.x < second.x);
    }
    // otherwise it's vertical, so we want to know if the first one is down
    return (first.y < second.y);
  }
  
  public void stopRecording() {
    List<UIAction> actions = capturer.reportBack();

    if (actions.size() < 3) { System.out.println("not enough clicks!"); return; }
    
    // we want the locations of the clicks of the slider top and bottom.
    // we will assume that they were the first and second click/release sets.
    MousePressAction oneEndClick = (MousePressAction)actions.get(0);
    MousePressAction otherEndClick = (MousePressAction)actions.get(2);
    
    if (firstIsLeftOrDownOfSecond(oneEndClick.p, otherEndClick.p)) {
      lowEndOfSlider = oneEndClick.p;
      highEndOfSlider = otherEndClick.p;
    } else {
      lowEndOfSlider = otherEndClick.p;
      highEndOfSlider = oneEndClick.p;
    }   

    GlobalScreen.getInstance().removeNativeMouseListener(capturer);
    capturer = null;
    isRecording = false;
  }
}
