class SensorAddButton extends ClickableBox {
  Shape shape;
  PFont f = createFont("Lato-Bold.ttf", 45);
  
  public SensorAddButton(float posX, float posY, Shape shape) {
    super();
    this.posX = posX;
    this.posY = posY;
    this.boxX = 20;
    this.boxY = 20;
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
    rect(posX, posY, boxX, boxY, corner, corner, corner, corner);
    stroke(Sensor.COPPER);
    fill(Sensor.COPPER);
    if (shape == Shape.RECTANGLE) {
      rect(posX, posY, boxX*.7, boxY*.7); 
    }
    if (shape == Shape.CIRCLE) {
      ellipse(posX, posY, boxX*.75, boxY*.75);
    }
    if (shape == Shape.OTHER) {
      textAlign(CENTER);
      textFont(f);
      text("?", posX, posY+17);
    }
    drawPlus();
  }
  
  private void drawPlus() {
    fill(#ED2B2B);
    stroke(#ED2B2B);
    rect(posX,posY,10,1);
    rect(posX,posY,1,10);
  }
}