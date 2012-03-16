package actions;

import javax.swing.ImageIcon;

public interface UIAction {

  public static final String[] POSSIBLE_INTERACTIONS = {"UI Script", "Web Socket Callback"};
  
  public void doAction();
  public ImageIcon icon();
}