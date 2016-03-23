enum Shape {
  RECTANGLE,CIRCLE,SLIDER,PAD,INTERDIGITATED,OTHER
};

class Sensor extends ClickableBox {
  final public static color COPPER = #ED9E37;
  
  Shape shape;
  
  boolean resizingX = false;
  boolean resizingY = false;
  int resizingThresh = 10;
  
  PImage customSensor;
  
  Point anchor;
  
  private void initDefaults() {
    this.draggable = true;
    this.setBaseColor(COPPER);
    this.shape = Shape.RECTANGLE;
    this.anchor = new Point(0,0);
  }
  
  public Sensor() {
    super();
    initDefaults();
    determineAnchor();
  }
  
  public Sensor(Shape shape) {
    super();
    initDefaults();
    this.shape = shape;
    determineAnchor();
  }
  
  public void drawIt() {
    stroke(baseStroke);
    fill(baseFill);
    if (isMouseOver() && !disabled) {
      stroke(baseStroke + 8<<16 + 8<<8 + 8);
    }
    if (mouseClick && !disabled) {
      fill(baseFill + 2<<15 + 2<<7 + 2);
    }
    if (shape==Shape.RECTANGLE) {
      rect(posX, posY, boxX, boxY); 
    } else if (shape==Shape.CIRCLE) {
      ellipse(posX,posY,boxX,boxY);
    } else if (shape==Shape.OTHER) {
      image(customSensor,posX,posY);
    }
  }
  
  public void dragMouse() {
    if(mouseClick && !disabled) {
      if (!resizingX && !resizingY) {
        posX = mouseX-xOffset; 
        posY = mouseY-yOffset;
      }
      if (resizingX) {
        float oldBoxX = boxX;
        boxX = int(abs(mouseX-posX));
        if (mouseX < posX) {
          posX = posX - (boxX-oldBoxX);
        } else {
          posX = posX + (boxX-oldBoxX);
        }
      } if (resizingY) {
        float oldBoxY = boxY;
        boxY = int(abs(mouseY-posY));
        if (mouseY < posY) {
          posY = posY - (boxY-oldBoxY);
        } else {
          posY = posY + (boxY-oldBoxY);
        }
      }
      determineAnchor();
    }
  }
  
  public void clickMouse() {
    if(isMouseOver() && !disabled) { 
      mouseClick = true;
      resizingX = (abs(boxX - abs(mouseX-posX)) < resizingThresh);
      resizingY = (abs(boxY - abs(mouseY-posY)) < resizingThresh);
    } else {
      mouseClick = false;
      resizingX = false;
      resizingY = false;
    }
    xOffset = mouseX-posX; 
    yOffset = mouseY-posY;
  }
  
  public void releaseMouse() {
    mouseClick = false;
    resizingX = false;
    resizingY = false;
  }
  
  public void setImage(PImage customSensor) {
    this.customSensor = customSensor; 
  }
  
  public void lock() {
    this.disabled = true;
    this.draggable = false;
  }
  
  public void unlock() {
    this.disabled = false;
    this.draggable = true;
  }
  
  private void determineAnchor() {
    
  }
}