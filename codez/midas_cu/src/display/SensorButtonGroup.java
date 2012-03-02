package display;

import java.awt.BorderLayout;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;

import util.Direction;

public class SensorButtonGroup extends JPanel {
  private static final long serialVersionUID = -3154036436928212098L;
  private static final int BASE = 10;
  public static final int SIZE_CHANGE = 4;

  public List<ArduinoSensorButton> triggerButtons = new ArrayList<ArduinoSensorButton>();
  private Point base = new Point(BASE, BASE);

  // TODO these buttons will be used in the properties interface at right
  private JButton horizontal = new JButton("horizontal");
  private JButton vertical = new JButton("vertical");
  private JButton larger = new JButton("larger");
  private JButton smaller = new JButton("smaller");
  private JButton delete = new JButton("delete");

  private SensorShape.shapes shape;
  private int spacing = 5;
  private int size = 45;
  private Direction orientation = Direction.VERTICAL;

  public String name;

  public boolean deleteMe = false;
  public boolean isSlider;
  public boolean isPad;
  public Integer sensitivity;

  public SensorButtonGroup(SensorShape.shapes shape) {
    isSlider = (shape == SensorShape.shapes.SLIDER);
    isPad = (shape == SensorShape.shapes.PAD);

    setLayout(new BorderLayout());

    initializeButtons();

    name = shape.name().toLowerCase();
    this.shape = shape;
  }

  public void setSensitivity(Integer sensitivity) {
    this.sensitivity = sensitivity;
    triggerButtons = new ArrayList<ArduinoSensorButton>();
    // the way we have this right now, we can case on sensitivity < 9 vs >= 9
    // for pad vs slider!
    if (!isPad()) { // we have a slider or a single button
      if (orientation == Direction.VERTICAL) {
        for (int i = 0; i < sensitivity.intValue(); i++) {
          triggerButtons.add(new ArduinoSensorButton(shape, new Point(base.x, i*(size + spacing) + base.y), size));
        }
      } else {
        for (int i = 0; i < sensitivity.intValue(); i++) {
          triggerButtons.add(new ArduinoSensorButton(shape, new Point(i*(size + spacing) + base.x,base.y), size));
        }
      }
    } else { // we have a pad!
      for (int i = 0; i < Math.floor(Math.sqrt(sensitivity)); i++) {
        for (int j = 0; j < Math.floor(Math.sqrt(sensitivity)); j++) {
          triggerButtons.add(new ArduinoSensorButton(shape, new Point(i*(size + spacing) + base.x,
                                                                      j*(size + spacing) + base.y),
                                                     size));
        }
      }
    }
    repaint();
  }

  private void initializeButtons() {
    horizontal.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        orientation = Direction.HORIZONTAL;
        setSensitivity(sensitivity);
        repaint();
      }
    });
    vertical.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        orientation = Direction.VERTICAL;
        setSensitivity(sensitivity);
        repaint();
      }
    });
    smaller.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        size -= SIZE_CHANGE;
        for (ArduinoSensorButton button : triggerButtons) {
          button.smaller();
        }
        repaint();
      }
    });
    larger.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        size += SIZE_CHANGE;
        for (ArduinoSensorButton button : triggerButtons) {
          button.larger();
        }
        repaint();
      }
    });
    delete.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        deleteMe = true;
      }
    });
  }

  public void paint(Graphics2D g) {
    if (!deleteMe) {
      for (ArduinoSensorButton button : triggerButtons) {
        button.paint(g);
      }
    }
  }

  public String toString() {
    return name;
  }

  @Override
  public boolean contains(Point p) {
    boolean contains = false;
    for (ArduinoSensorButton butt : triggerButtons) {
      contains |= butt.contains(p);
    }
    return contains;
  }

  public boolean contains(ArduinoSensorButton button) {
    return triggerButtons.contains(button);
  }

  public void moveTo(Point p) {
    base = p;
    if (isPad()) { // we have a pad
      for (int i = 0; i < Math.floor(Math.sqrt(sensitivity)); i++) {
        for (int j = 0; j < Math.floor(Math.sqrt(sensitivity)); j++) {
          triggerButtons.get((int) (i * Math.floor(Math.sqrt(sensitivity)) + j))
                         .moveTo(new Point(i * (size + spacing) + base.x,
                                           j * (size + spacing) + base.y));
        }
      }
    } else { // we have a slider or single button
      if(orientation == Direction.HORIZONTAL) {
        for (int i=0; i<sensitivity; i++) {
          triggerButtons.get(i).moveTo(new Point(i * (size + spacing) + base.x,
                                                 base.y));
        }
      }
      else {
        for (int i=0; i<sensitivity; i++) {
          triggerButtons.get(i).moveTo(new Point(base.x,
                                                 i * (size + spacing) + base.y));
        }
      }
    }
    repaint();
  }
  
  private boolean isPad() {
    return sensitivity >= 9;
  }
}
