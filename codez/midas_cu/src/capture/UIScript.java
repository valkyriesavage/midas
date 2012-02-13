package capture;

import java.util.ArrayList;
import java.util.List;

public class UIScript {
  private List<UIAction> actions = new ArrayList<UIAction>();
  
  public UIScript() {}
  
  public void execute() {
    for (UIAction action : actions) {
      action.doAction();
    }
  }
  
  public String toString() {
    return "Hi, I am a UIScript";
  }
  
  public void replace() {
    //here we capture! 
  }
}
