class ClickableBox {
  final public static color DEFAULT_COLOR = #777777;
  
  boolean mouseClick = false;
  float xOffset = 0.0; 
  float yOffset = 0.0;
  int corner = 12;
  int centeringOffset = 8;
  
  color baseStroke = color(153,153,153);
  color baseFill = color(153,153,153);
  PFont f;
  
  float posX, posY;
  boolean draggable;
  int boxX, boxY;
  String label;
  
  StuffDoer stuffDoer;
  
  boolean disabled = false;
  
  private void initDefaults() {
    boxX = 75;
    boxY = 75;
    draggable = false;
    posX = width/2.0;
    posY = height/2.0;
    label = "";
    f = createFont("Lato-Regular.ttf", 18);    
  }
  
  public ClickableBox() {
    initDefaults();
  }
  
  public ClickableBox(String label, float posX, float posY, int boxX, int boxY) {
    initDefaults();
    this.boxX = boxX;
    this.boxX = boxX;
    this.boxY = boxY;
    this.posX = posX;
    this.posY = posY;
    this.label = label;
  }
  
  public void setStuffDoer(StuffDoer stuffDoer) {
    this.stuffDoer = stuffDoer;
  }
  
  public boolean isMouseOver() {
      return mouseX > posX-boxX && mouseX < posX+boxX && 
             mouseY > posY-boxY && mouseY < posY+boxY;
  }
  
  public void dragMouse() {
    if(mouseClick && draggable && !disabled) {
      posX = mouseX-xOffset; 
      posY = mouseY-yOffset; 
    }
  }
  
  public void clickMouse() {
    if(isMouseOver() && !disabled) { 
      mouseClick = true;
      stuffDoer.doStuff();
    } else {
      mouseClick = false;
    }
    xOffset = mouseX-posX; 
    yOffset = mouseY-posY; 
  }
  
  public void releaseMouse() {
    mouseClick = false; 
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
    fill(0);
    textAlign(CENTER);
    textFont(f);
    text(label, posX, posY+centeringOffset);
  }
  
  public void disable() {
    this.disabled = true; 
  }
  public void enable() {
    this.disabled = false;
  }
  
  public void setBaseColor(color c) {
    this.baseStroke = c;
    this.baseFill = c;
  }
  
  public void setFont(PFont f) {
    this.f = f; 
  }
  
  public void setDraggable(boolean draggable) {
    this.draggable = draggable;
  }
}