package capture;

import java.awt.AWTException;
import java.awt.Point;
import java.awt.event.InputEvent;
import java.util.List;

import org.jnativehook.GlobalScreen;

public class UISlider {
  public Point lowEndOfSlider;
  public Point highEndOfSlider;
  public int sensitivity;
  private InputCapturer capturer;
  
  public boolean isRecording = false;
  
  public UISlider() {}
  
  private boolean isHorizontal() {
    return ((highEndOfSlider.y - lowEndOfSlider.y) > (highEndOfSlider.x - lowEndOfSlider.x));
  }
  
  public void execute() {
    MousePressAction action;
    Point clickPoint;
    if (this.isHorizontal()) {
      clickPoint = new Point((highEndOfSlider.x-lowEndOfSlider.x)/sensitivity + lowEndOfSlider.x, highEndOfSlider.y);
    } else {
      clickPoint = new Point(highEndOfSlider.x, (highEndOfSlider.y-lowEndOfSlider.y)/sensitivity + lowEndOfSlider.y);
    }
    try {
      action = new MousePressAction(clickPoint, InputEvent.BUTTON1_MASK);
      action.doAction();
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
  
  public void stopRecording() {
    List<UIAction> actions = capturer.reportBack();
    // be sure to pop off the last two events ; that's the click and release where they stopped recording.
    actions.remove(actions.size() - 1);
    actions.remove(actions.size() - 1);
    
    // we want the locations of the clicks of the slider top and bottom.  we will assume that they were the first and last clicks.
    MousePressAction topClick = (MousePressAction)actions.get(0);
    MousePressAction bottomClick = (MousePressAction)actions.get(actions.size() - 2);
    lowEndOfSlider = topClick.p;
    highEndOfSlider = bottomClick.p;

    GlobalScreen.getInstance().removeNativeMouseListener(capturer);
    capturer = null;
    isRecording = false;
  }
}
