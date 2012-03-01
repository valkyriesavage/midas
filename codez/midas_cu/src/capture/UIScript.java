package capture;

import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;

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
    } else {
      ret = "unrecorded";
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

    GlobalScreen.getInstance().removeNativeKeyListener(capturer);
    GlobalScreen.getInstance().removeNativeMouseListener(capturer);
    capturer = null;
    isRecording = false;
  }
  
  public ImageIcon icon() {
    return actions.get(0).icon();
  }
}
