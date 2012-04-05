package display;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
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

import util.Direction;

public class SensorButtonGroup extends JPanel {
  private static final long serialVersionUID = -3154036436928212098L;
  private static final int BASE = 30;
  private static final int MIN_SIZE = 30;
  public static final int SIZE_CHANGE = 5;
  public static final int BUFFER = 2;
  private static final int WIDTH_OF_NAME_FIELD = 10;

  public List<ArduinoSensorButton> triggerButtons = new ArrayList<ArduinoSensorButton>();
  private Point base = new Point(BASE, BASE);

  public JButton orientationFlip = new JButton("make horizontal");
  public JButton larger = new JButton("larger");
  public JButton smaller = new JButton("smaller");
  public JButton delete = new JButton("delete");
  public JTextField nameField;

  private SensorShape.shapes shape;
  private Image customImage;
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

  public SensorButtonGroup(SensorShape.shapes shape) {
    isSlider = (shape == SensorShape.shapes.SLIDER);
    isPad = (shape == SensorShape.shapes.PAD);

    name = shape.name().toLowerCase();
    this.shape = shape;

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

  private void generalSetup() {
    initializeButtons();

    nameField = new JTextField(WIDTH_OF_NAME_FIELD);
    nameField.setText(name);
    nameField.getDocument().addDocumentListener(new DocumentListener() {
      @Override
      public void changedUpdate(DocumentEvent event) {
        JTextField target = (JTextField) event.getDocument();
        name = target.getText();
      }

      @Override
      public void insertUpdate(DocumentEvent event) {
      }

      @Override
      public void removeUpdate(DocumentEvent event) {
      }
    });
  }

  public void setSensitivity(Integer sensitivity) {
    this.sensitivity = sensitivity;
    triggerButtons = new ArrayList<ArduinoSensorButton>();

    if (isCustom) {
      triggerButtons.add(new ArduinoSensorButton(customImage, new Point(base.x,
          (size + spacing) + base.y), size));
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
    smaller.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
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
    });
    larger.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        size += SIZE_CHANGE;
        for (ArduinoSensorButton button : triggerButtons) {
          button.larger();
        }
        moveTo(base);
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
    	if(sensitivity == SetUp.HELLA_SLIDER) {
    		HellaSliderPositioner hsp = getHSP();
    		g.setColor(triggerButtons.get(0).relevantColor);
    		g.fill(hsp.getSeg1());
    		g.fill(hsp.getSeg2());
    		g.fill(hsp.getOuter());
    	} else {
	      for (ArduinoSensorButton button : triggerButtons) {
	        button.paint(g);
	      }
    	}
    }
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
      // note that for hella sliders we render a full bar with no spaces in it.
      if (orientation == Direction.HORIZONTAL) {
        for (int i = 0; i < SetUp.SLIDER_SENSITIVITIES[0]; i++) {
          triggerButtons.get(i).moveTo(
              new Point(i * (size) + base.x, base.y));
        }
      } else {
        for (int i = 0; i < SetUp.SLIDER_SENSITIVITIES[0]; i++) {
          triggerButtons.get(i).moveTo(
              new Point(base.x, i * (size) + base.y));
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
		//the current bounds; could be anywhere. We bring it to the origin.
		//If the slider should be vertical, we rotate 90 degrees, bring it back to the origin
		//we then scale it such that the width and height are equal to the wanted bounds' dimensions,
		//and finally translate to the wanted bounds' position
		
		
		if(orientation == Direction.VERTICAL) { //vertical
			h.transformed(AffineTransform.getRotateInstance(Math.PI/2));
			h.moveToOrigin();
		} else { //horizontal
			h.transformed(AffineTransform.getRotateInstance(Math.PI));
			h.moveToOrigin();
		}
		

		Rectangle2D wantedBounds = null;
		for( ArduinoSensorButton b : triggerButtons) {
			Rectangle2D temp = b.getShape().getBounds2D();
			if(wantedBounds == null) wantedBounds = temp;
			else wantedBounds = wantedBounds.createUnion(temp);
		}
		
		h.setDimension(wantedBounds.getWidth(), wantedBounds.getHeight());
		h.transformed(AffineTransform.getTranslateInstance(wantedBounds.getX(), wantedBounds.getY()));
		
		return h;
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
}
