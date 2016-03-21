enum Shape {
  RECTANGLE,CIRCLE,SLIDER,PAD,INTERDIGITATED,OTHER
};

class Sensor extends ClickableBox {
  Shape shape;
  
  boolean resizing = false;
  
  private void initDefaults() {
    this.draggable = true;
    this.setBaseColor(#ED9E37);
    this.shape = Shape.RECTANGLE;
  }
  
  public Sensor() {
    super();
    initDefaults();
  }
  
  public Sensor(Shape shape) {
    super();
    initDefaults();
    this.shape = shape;
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
    } else {
      
    }
  }
  
  public void dragMouse() {
    if(mouseClick && !disabled) {
      posX = mouseX-xOffset; 
      posY = mouseY-yOffset; 
    }
  }
  
  public void clickMouse() {
    if(isMouseOver() && !disabled) { 
      mouseClick = true;
    } else {
      mouseClick = false;
    }
    xOffset = mouseX-posX; 
    yOffset = mouseY-posY; 
  }
  
  public void releaseMouse() {
    mouseClick = false; 
  }
}