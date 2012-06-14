package display;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

import util.Direction;

public class SensorButtonGroup extends JPanel {
  private static final long serialVersionUID = -3154036436928212098L;
  private static final int BASE = 30;
  private static final int MIN_SIZE = 30;
  public static final int SIZE_CHANGE = 1;
  public static final int BUFFER = 2;
  private static final int WIDTH_OF_NAME_FIELD = 10;
  private static final int UNMARKED = -1;

  public List<ArduinoSensorButton> triggerButtons = new ArrayList<ArduinoSensorButton>();
  private Point base = new Point(BASE, BASE);

  public JButton orientationFlip = new JButton("make horizontal");
  public JButton delete = new JButton("delete");
  public JTextField nameField;

  protected SensorShape.shapes shape;
  protected Polygon customShape;
  public List<Point> vertices;
  protected Image customImage;

  private int spacing = 5;
  private int size = MIN_SIZE + 4 * SIZE_CHANGE;
  public Direction orientation = Direction.VERTICAL;

  public String name;

  public boolean deleteMe = false;
  public boolean isSlider;
  public boolean isPad;
  public boolean isCustom = false;
  public Integer sensitivity;

  private boolean isIntersecting = false;
  private int marker = UNMARKED;

  protected SensorButtonGroup() {}
  
  public SensorButtonGroup(SensorShape.shapes shape) {
    isSlider = (shape == SensorShape.shapes.SLIDER);
    isPad = (shape == SensorShape.shapes.PAD);
    
    if (shape == SensorShape.shapes.POLYGON) {
      customShape = new Polygon();
    }

    name = shape.name().toLowerCase();
    this.shape = shape;

    generalSetup();
  }
  
  public SensorButtonGroup(List<Point> vertices) {
    isSlider = false;
    isPad = false;
    
    customShape = new Polygon();
    
    for (Point p : vertices) {
      customShape.addPoint(p.x, p.y);
    }
    this.vertices = vertices;
    
    generalSetup();
  }

  public SensorButtonGroup(Image customImage, String name) {
    isSlider = false;
    isPad = false;

    this.name = name;
    this.customImage = customImage;

    generalSetup();

    isCustom = true;
  }
  
  public void setIsObstacle(boolean isObstacle) {
    for (ArduinoSensorButton button : triggerButtons) {
      button.setIsObstacle(isObstacle);
    }
  }
  
  public void setVertices(List<Point> vertices) {
    customShape = new Polygon();
    for (Point p : vertices) {
      customShape.addPoint(p.x, p.y);
    }
  }

  public Image getCustomImage() {
    return customImage;
  }

  public Point center() {
    return base;
  }

  private void generalSetup() {
    initializeButtons();

    nameField = new JTextField(WIDTH_OF_NAME_FIELD);
    nameField.setText(name);
    nameField.getDocument().addDocumentListener(new DocumentListener() {
      @Override
      public void changedUpdate(DocumentEvent event) {
      }

      @Override
      public void insertUpdate(DocumentEvent event) {
        try {
          name = event.getDocument()
              .getText(0, event.getDocument().getLength());
        } catch (BadLocationException e) {
          e.printStackTrace();
        }
      }

      @Override
      public void removeUpdate(DocumentEvent event) {
        try {
          name = event.getDocument()
              .getText(0, event.getDocument().getLength());
        } catch (BadLocationException e) {
          e.printStackTrace();
        }
      }
    });
  }

  public void setSensitivity(Integer sensitivity) {
    this.sensitivity = sensitivity;
    triggerButtons = new ArrayList<ArduinoSensorButton>();

    if (isCustom) {
      triggerButtons.add(new ArduinoSensorButton(customImage, new Point(base.x,
          (size + spacing) + base.y), size));
    } else if (customShape != null) {
      triggerButtons.clear();
      triggerButtons.add(new ArduinoSensorButton(customShape));
    } else {
      if (!isPad && sensitivity != SetUp.HELLA_SLIDER) { // we have a slider or
                                                         // a single button
        if (orientation == Direction.VERTICAL) {
          for (int i = 0; i < sensitivity.intValue(); i++) {
            triggerButtons.add(new ArduinoSensorButton(shape, new Point(base.x,
                i * (size + spacing) + base.y), size));
          }
        } else {
          for (int i = 0; i < sensitivity.intValue(); i++) {
            triggerButtons.add(new ArduinoSensorButton(shape, new Point(i
                * (size + spacing) + base.x, base.y), size));
          }
        }
      } else if (isPad) { // we have a pad!
        for (int i = 0; i < Math.floor(Math.sqrt(sensitivity)); i++) {
          for (int j = 0; j < Math.floor(Math.sqrt(sensitivity)); j++) {
            triggerButtons.add(new ArduinoSensorButton(shape, new Point(i
                * (size + spacing) + base.x, j * (size + spacing) + base.y),
                size));
          }
        }
      } else { // we have a hella slider
        if (orientation == Direction.VERTICAL) {
          for (int i = 0; i < SetUp.SLIDER_SENSITIVITIES[0]; i++) {
            triggerButtons.add(new ArduinoSensorButton(shape, new Point(base.x,
                i * (size) + base.y), size));
          }
        } else {
          for (int i = 0; i < SetUp.SLIDER_SENSITIVITIES[0]; i++) {
            triggerButtons.add(new ArduinoSensorButton(shape, new Point(i
                * (size) + base.x, base.y), size));
          }
        }
      }
    }
    repaint();
  }

  private void initializeButtons() {
    orientationFlip.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        if (orientation == Direction.VERTICAL) {
          orientation = Direction.HORIZONTAL;
          orientationFlip.setText("make vertical");
        } else {
          orientation = Direction.VERTICAL;
          orientationFlip.setText("make horizontal");
        }
        setSensitivity(sensitivity);
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
      if (isPad) {
        PadPositioner p = getPP();
        g.setColor(triggerButtons.get(0).relevantColor);
        for (Shape s : p.getShapes()) {
          g.fill(s);
        }
      } else if (sensitivity == SetUp.HELLA_SLIDER) {
        HellaSliderPositioner hsp = getHSP();
        g.setColor(triggerButtons.get(0).relevantColor);
        g.fill(hsp.getSeg1());
        g.fill(hsp.getSeg2());
        g.fill(hsp.getOuter());
        if (marker > UNMARKED) {
          g.setColor(Color.PINK);
          if (orientation == Direction.VERTICAL) {
            g.fillRect((int) getHSP().bounds().getX(), yAt(marker) - 5,
                (int) getHSP().bounds().getWidth(), 10);
          } else {
            g.fillRect(xAt(marker) - 5, (int) getHSP().bounds().getY(), 10,
                (int) getHSP().bounds().getWidth());
          }
        }
      } else {
        for (ArduinoSensorButton button : triggerButtons) {
          button.paint(g);
        }
      }
    }
  }

  private int xAt(int hellaSliderPosition) {
    return (int) ((getHSP().bounds().getWidth()*hellaSliderPosition / (SetUp.HELLA_SLIDER*1.0)) + getHSP()
        .bounds().getMinX());
  }

  private int yAt(int hellaSliderPosition) {
    return (int) ((getHSP().bounds().getHeight()*hellaSliderPosition / (SetUp.HELLA_SLIDER*1.0)) + getHSP()
        .bounds().getMinY());
  }

  public String toString() {
    return name;
  }

  @Override
  public boolean contains(Point p) {
    boolean contains = false;
    for (ArduinoSensorButton button : triggerButtons) {
      contains |= button.contains(p);
    }
    return contains;
  }

  public boolean contains(ArduinoSensorButton button) {
    return triggerButtons.contains(button);
  }

  public void moveTo(Point p) {
    base = p;
    if (isPad) { // we have a pad
      for (int i = 0; i < Math.floor(Math.sqrt(sensitivity)); i++) {
        for (int j = 0; j < Math.floor(Math.sqrt(sensitivity)); j++) {
          triggerButtons
              .get((int) (i * Math.floor(Math.sqrt(sensitivity)) + j)).moveTo(
                  new Point(i * (size + spacing) + base.x, j * (size + spacing)
                      + base.y));
        }
      }
    } else if (sensitivity == SetUp.HELLA_SLIDER) { // we have a hella slider
      if (orientation == Direction.HORIZONTAL) {
        for (int i = 0; i < SetUp.SLIDER_SENSITIVITIES[0]; i++) {
          triggerButtons.get(i).moveTo(new Point(i * (size) + base.x, base.y));
        }
      } else {
        for (int i = 0; i < SetUp.SLIDER_SENSITIVITIES[0]; i++) {
          triggerButtons.get(i).moveTo(new Point(base.x, i * (size) + base.y));
        }
      }
    } else { // we have a slider or single button
      if (orientation == Direction.HORIZONTAL) {
        for (int i = 0; i < sensitivity; i++) {
          triggerButtons.get(i).moveTo(
              new Point(i * (size + spacing) + base.x, base.y));
        }
      } else {
        for (int i = 0; i < sensitivity; i++) {
          triggerButtons.get(i).moveTo(
              new Point(base.x, i * (size + spacing) + base.y));
        }
      }
    }
    repaint();
  }

  public JTextField nameField() {
    return nameField;
  }

  public void setSelected(boolean selected) {
    for (ArduinoSensorButton button : triggerButtons) {
      button.setSelected(selected);
    }
  }

  public void setIntersecting(boolean intersecting) {
    for (ArduinoSensorButton button : triggerButtons) {
      button.setIntersecting(intersecting);
    }
    isIntersecting = intersecting;
  }

  public boolean isIntersecting() {
    return isIntersecting;
  }

  public boolean intersects(Rectangle rectangle) {
    for (ArduinoSensorButton button : triggerButtons) {
      if (button.intersects(rectangle)) {
        return true;
      }
    }
    return false;
  }

  public HellaSliderPositioner getHSP() {
    HellaSliderPositioner h = new HellaSliderPositioner();

    h.moveToOrigin();
    // the current bounds; could be anywhere. We bring it to the origin.
    // If the slider should be vertical, we rotate 90 degrees, bring it back to
    // the origin
    // we then scale it such that the width and height are equal to the wanted
    // bounds' dimensions,
    // and finally translate to the wanted bounds' position

    if (orientation == Direction.VERTICAL) { // vertical
      h.transformed(AffineTransform.getRotateInstance(Math.PI / 2));
      h.moveToOrigin();
    } else { // horizontal
      h.transformed(AffineTransform.getRotateInstance(Math.PI));
      h.moveToOrigin();
    }

    Rectangle2D wantedBounds = null;
    for (ArduinoSensorButton b : triggerButtons) {
      Rectangle2D temp = b.getShape().getBounds2D();
      if (wantedBounds == null)
        wantedBounds = temp;
      else
        wantedBounds = wantedBounds.createUnion(temp);
    }

    h.setDimension(wantedBounds.getWidth(), wantedBounds.getHeight());
    h.transformed(AffineTransform.getTranslateInstance(wantedBounds.getX(),
        wantedBounds.getY()));

    return h;
  }

  public void smaller() {
    size -= SIZE_CHANGE;
    if (size < MIN_SIZE) {
      size = MIN_SIZE;
    } else {
      for (ArduinoSensorButton button : triggerButtons) {
        button.smaller();
      }
    }
    moveTo(base);
    repaint();
  }

  public void larger() {
    size += SIZE_CHANGE;
    for (ArduinoSensorButton button : triggerButtons) {
      button.larger();
    }
    moveTo(base);
    repaint();
  }

  public PadPositioner getPP() {
    PadPositioner p = new PadPositioner();

    p.moveToOrigin();
    // bring to origin;
    // set the bounds to be the bounds of the trigger buttons

    Rectangle2D wantedBounds = null;
    for (ArduinoSensorButton b : triggerButtons) {
      Rectangle2D temp = b.getShape().getBounds2D();
      if (wantedBounds == null)
        wantedBounds = temp;
      else
        wantedBounds = wantedBounds.createUnion(temp);
    }

    p.setDimension(wantedBounds.getWidth(), wantedBounds.getHeight());
    p.transformed(AffineTransform.getTranslateInstance(wantedBounds.getX(),
        wantedBounds.getY()));

    return p;
  }

  @Override
  public Rectangle getBounds() {

    // we want the upper corner of the first one and the lower corner of the
    // last one
    Rectangle first = triggerButtons.get(0).getBounds();
    Rectangle last = triggerButtons.get(triggerButtons.size() - 1).getBounds();

    Rectangle bounds = new Rectangle(first.x, first.y, (last.x - first.x)
        + last.width, (last.y - first.y) + last.width);
    bounds.grow(BUFFER, BUFFER);
    return bounds;
  }

  public void touch() {
    for (ArduinoSensorButton b : triggerButtons) {
      b.activate();
    }
  }
  
  public void touch(int whichInSlider) {
    triggerButtons.get(whichInSlider).activate();
  }
  
  public void touch(Point whereInPad) {
    
  }

  public void hellaTouch(int hellaSliderPosition) {
    marker = hellaSliderPosition;
  }

  public void release() {
    for (ArduinoSensorButton b : triggerButtons) {
        b.deactivate();
    }
    marker = UNMARKED;
  }
  
  public boolean isObstacle() {
    return triggerButtons.get(0).isObstacle();
  }
}
