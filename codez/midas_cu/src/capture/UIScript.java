package capture;

import java.util.ArrayList;
import java.util.List;

import org.jnativehook.GlobalScreen;

public class UIScript {
  public List<UIAction> actions = new ArrayList<UIAction>();
  private InputCapturer capturer;
  
  public boolean isRecording = false;
  
  public UIScript() {}
  
  public void execute() {
    for (UIAction action : actions) {
      action.doAction();
    }
  }
  
  public String toString() {
    String ret = "";
    for (UIAction action : actions) {
      if (action instanceof MouseReleaseAction) { continue; }
      ret += action + ", ";
    }
    if (ret.length() > 2) {
      ret = ret.substring(0, ret.length() - 2);
    }
    return ret;
  }
  
  public void record() {
    capturer = new InputCapturer();
    GlobalScreen.getInstance().addNativeKeyListener(capturer);
    GlobalScreen.getInstance().addNativeMouseListener(capturer);
    isRecording = true;
  }
  
  public void stopRecording() {
    actions = capturer.reportBack();
    // be sure to pop off the last two events ; that's the click and release where they stopped recording.
    actions.remove(actions.size() - 1);
    actions.remove(actions.size() - 1);
    GlobalScreen.getInstance().removeNativeKeyListener(capturer);
    GlobalScreen.getInstance().removeNativeMouseListener(capturer);
    capturer = null;
    isRecording = false;
  }
}
