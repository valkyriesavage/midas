package util;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

public class ColorBar extends JPanel {

  private static final long serialVersionUID = 1998891698514473572L;
  private Color[] colors;

  public ColorBar(Color[] colors) {
    super();
    this.colors = colors;
  }
  
  @Override
  public void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D) g;
    if (colors != null) {
      for (int i=0; i<colors.length; i++) {
        Color color = colors[i];
        g2.setColor(color);
        g2.fillRect((int)((1.0*i/colors.length)*getWidth()), 0, getWidth()/colors.length, getHeight());
      }     
    } else {
      g2.drawString("none", 0, 0);
    }
  }
  
  public void setColor(Color[] colors) {
    this.colors = colors;
  }
}
