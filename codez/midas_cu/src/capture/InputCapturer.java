package capture;

import java.awt.AWTException;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseInputListener;

public class InputCapturer implements NativeKeyListener, NativeMouseInputListener {

  private List<UIAction> actions = new ArrayList<UIAction>();
  
  public InputCapturer() {}
  
  @Override
  public void mousePressed(NativeMouseEvent event) {
    try {
      MousePressAction action = new MousePressAction(new Point(event.getX(), event.getY()), event.getButton());
      actions.add(action);
    } catch (AWTException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void mouseReleased(NativeMouseEvent event) {
    try {
      MouseReleaseAction action = new MouseReleaseAction(new Point(event.getX(), event.getY()), event.getButton());
      actions.add(action);
    } catch (AWTException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void mouseMoved(NativeMouseEvent event) {}

  @Override
  public void keyPressed(NativeKeyEvent event) {
    try {
      TypeAction action = new TypeAction(event.getKeyCode());
      actions.add(action);
    } catch (AWTException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void keyReleased(NativeKeyEvent event) {}

  public List<UIAction> reportBack() {
    // before returning, pop off the last two events; it's the click and release where they hit "stop recording"
    actions.remove(actions.size() - 1);
    actions.remove(actions.size() - 1);
    
    return actions;
  }
  
}
