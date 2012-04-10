package actions;

import javax.swing.ImageIcon;

public interface UIAction {

  public static final String[] POSSIBLE_INTERACTIONS = {"record and replay", "websocket callback"};
  
  public void doAction();
  public ImageIcon icon();
}