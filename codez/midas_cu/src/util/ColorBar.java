package util;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

public class ColorBar extends JPanel {

  private static final long serialVersionUID = 1998891698514473572L;
  private Color color;

  public ColorBar(Color[] color) {
    super();
    if (color == null) {
      this.color = null; 
    } else {
      this.color = color[0];
    }
  }
  
  @Override
  public void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D) g;
    if (color != null) {
      g2.setColor(color);
      g2.fillRect(0, 0, getWidth(), getHeight());
    } else {
      g2.drawString("none", 0, 0);
    }
  }
  
  public void setColor(Color[] color) {
    if (color == null) {
      this.color = null; 
    } else {
      this.color = color[0];
    }
  }
}
