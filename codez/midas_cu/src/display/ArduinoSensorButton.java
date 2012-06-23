package display;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.util.Random;

import serialtalk.ArduinoSensor;
import sl.shapes.StarPolygon;

public class ArduinoSensorButton {
  public static final Color BASE_COLOR = CanvasPanel.COPPER;

  public ArduinoSensor sensor;
  SensorShape.shapes shape = null;
  Polygon customShape = null;
  Image customImage = null;
  boolean locationChecked = false;

  public Point upperLeft;
  private int size;

  private int width;
  private int height;

  private boolean isObstacle;
  private boolean isSelected = true;

  public Color relevantColor = CanvasPanel.COPPER;

  static Area imageOutline(BufferedImage bi) {
    GeneralPath gp = new GeneralPath();

    boolean cont = false;
    for (int xx = 0; xx < bi.getWidth(); xx++) {
      for (int yy = 0; yy < bi.getHeight(); yy++) {
        if ((new Color(bi.getRGB(xx, yy), true)).getAlpha() > 0) {
          if (cont) {
            gp.lineTo(xx, yy);
            gp.lineTo(xx, yy + 1);
            gp.lineTo(xx + 1, yy + 1);
            gp.lineTo(xx + 1, yy);
            gp.lineTo(xx, yy);
          } else {
            gp.moveTo(xx, yy);
          }
          cont = true;
        } else {
          cont = false;
        }
      }
      cont = false;
    }
    gp.closePath();

    // construct the Area from the GP & return it
    return new Area(gp);
  }

  public Area imageOutline() {
    Area outline = imageOutline((BufferedImage) customImage);
    outline.transform(AffineTransform.getTranslateInstance(upperLeft.x,
        upperLeft.y));
    return outline;
  }

  public Shape getPathwayShape() {
    if (customImage == null) {
      return getShape();
    } else {
      return imageOutline();
    }
  }

  public ArduinoSensorButton(SensorShape.shapes shape) {
    this.shape = shape;
  }

  public void setIsObstacle(boolean isObstacle) {
    this.isObstacle = isObstacle;
    changeColor();
  }

  public boolean isObstacle() {
    return isObstacle;
  }

  public ArduinoSensorButton(SensorShape.shapes shape, Point upperLeft, int size) {
    this.shape = shape;
    this.upperLeft = upperLeft;
    this.size = size;
  }

  public ArduinoSensorButton(Image customImage, Point upperLeft, int size) {
    this.customImage = customImage;
    this.upperLeft = upperLeft;
    this.size = size;
  }

  public ArduinoSensorButton(Polygon shape) {
    this.customShape = shape;
    this.upperLeft = new Point(customShape.xpoints[0], customShape.ypoints[0]);
  }

  public void setSensor(ArduinoSensor sensor) {
    this.sensor = sensor;
  }

  public ArduinoSensor getSensor() {
    return sensor;
  }

  public void smaller() {
    size -= SensorButtonGroup.SIZE_CHANGE;

    if (customImage != null) {
      /*
       * customImage = customImage.getScaledInstance(customImage.getWidth(null)
       * - SensorButtonGroup.SIZE_CHANGE, customImage.getHeight(null) -
       * SensorButtonGroup.SIZE_CHANGE, Image.SCALE_DEFAULT);
       * /*BufferedImageUtil.scaleImage((BufferedImage) customImage,
       * customImage.getWidth(null) - SensorButtonGroup.SIZE_CHANGE,
       * customImage.getHeight(null) - SensorButtonGroup.SIZE_CHANGE,
       * Color.BLACK);
       */
    } else {
      upperLeft.x += SensorButtonGroup.SIZE_CHANGE / 2;
      upperLeft.y += SensorButtonGroup.SIZE_CHANGE / 2;
    }
  }

  public void larger() {
    size += SensorButtonGroup.SIZE_CHANGE;

    if (customImage != null) {
      /*
       * customImage = customImage.getScaledInstance(customImage.getWidth(null)
       * + SensorButtonGroup.SIZE_CHANGE, customImage.getHeight(null) +
       * SensorButtonGroup.SIZE_CHANGE, Image.SCALE_DEFAULT); /*
       * BufferedImageUtil.scaleImage((BufferedImage) customImage,
       * customImage.getWidth(null) + SensorButtonGroup.SIZE_CHANGE,
       * customImage.getHeight(null) + SensorButtonGroup.SIZE_CHANGE,
       * Color.BLACK);
       */
    } else if (customShape != null) {

    } else {
      upperLeft.x -= SensorButtonGroup.SIZE_CHANGE / 2;
      upperLeft.y -= SensorButtonGroup.SIZE_CHANGE / 2;
    }
  }

  public void activate() {
    relevantColor = Color.PINK;
  }

  public void deactivate() {
    changeColor();
  }

  public void changeShape(SensorShape.shapes newShape) {
    this.shape = newShape;
    this.customImage = null;
  }

  public void changeImage(Image customImage) {
    this.customImage = customImage;
    this.shape = null;
  }

  public boolean locationChecked() {
    return locationChecked;
  }

  public void paint(Graphics2D g) {
    if (shape != null) {
      Shape drawShape = getShape();
      g.setColor(relevantColor);
      g.fill(drawShape);
    } else if (customShape != null) {
      g.setColor(relevantColor);
      g.fill(customShape);
    } else if (customImage != null) {
      g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
          RenderingHints.VALUE_INTERPOLATION_BICUBIC);
      g.drawImage(customImage, upperLeft.x, upperLeft.y,
          null, new ImageObserver() {
            public boolean imageUpdate(Image img, int infoflags, int x, int y,
                int imgWidth, int imgHeight) {
              width = imgWidth;
              height = imgHeight;
              return false;
            }
          });
    }
  }

  public Shape getShape() {
    if (customShape != null) {
      return customShape;
    }
    if (shape == null) {
      return new Rectangle2D.Double(upperLeft.x - width / 2, upperLeft.y
          - width / 2, width, height);
    }
    if (shape.equals(SensorShape.shapes.CIRCLE)) {
      return circle();
    }
    if (shape.equals(SensorShape.shapes.STAR)) {
      return star();
    }
    if (shape.equals(SensorShape.shapes.SLIDER)) {
      return slider();
    }
    if (shape.equals(SensorShape.shapes.PAD)) {
      return pad();
    }
    return square();
  }

  private Shape circle() {
    return new Ellipse2D.Double(upperLeft.x - size / 2, upperLeft.y - size / 2,
        size, size);
  }

  private Shape square() {
    return new Rectangle2D.Double(upperLeft.x - size / 2, upperLeft.y - size
        / 2, size, size);
  }

  private Shape star() {
    return new StarPolygon(upperLeft.x, upperLeft.y, size, (int) (size * .5), 5);
  }

  private Shape slider() {
    return square();
  }

  private Shape pad() {
    return square();
  }

  public boolean contains(Point p) {
    if (customShape != null) {
      return customShape.contains(p);
    }
    if (shape == null && customImage == null) {
      return false;
    }
    if (shape != null) {
      return getShape().contains(p);
    }
    return (p.x > upperLeft.x && p.x < upperLeft.x + customImage.getWidth(null)
        && p.y > upperLeft.y && p.y < upperLeft.y + customImage.getHeight(null));
  }

  public void moveTo(Point upperLeft) {
    if (customShape != null) {
      customShape.translate(upperLeft.x - this.upperLeft.x, upperLeft.y
          - this.upperLeft.y);
    }
    this.upperLeft = upperLeft;
  }

  public void setSelected(boolean selected) {
    this.isSelected = selected;
    changeColor();
  }
  
  private void changeColor() {
    if (isObstacle) {
      if (isSelected) {
        relevantColor = CanvasPanel.OBSTACLE_SELECTED_COLOR;
      } else {
        relevantColor = CanvasPanel.OBSTACLE_COLOR;
      }
    } else {
      if (isSelected) {
        relevantColor = CanvasPanel.LIGHT_COPPER;
      } else {
        relevantColor = CanvasPanel.COPPER;
      }
    }
    
  }

  public void setIntersecting(boolean intersecting) {
    if (intersecting && !isObstacle) {
      relevantColor = Color.RED;
    }
  }

  public boolean intersects(Rectangle rectangle) {
    return getShape().intersects(rectangle);
  }

  public Rectangle getBounds() {
    Rectangle bounds = getShape().getBounds();
    bounds.grow(SensorButtonGroup.BUFFER, SensorButtonGroup.BUFFER);
    return bounds;
  }
}
