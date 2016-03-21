class DrawableLabel {
  PFont f;
  
  String label;
  float posX;
  float posY;
  public DrawableLabel(String label, float posX, float posY) {
    this.label = label;
    this.posX = posX;
    this.posY = posY;
    
    f = createFont("Lato-Bold.ttf", 24);
  }
  
  public void drawIt() {
    fill(0);
    textAlign(CENTER);
    textFont(f);
    text(label, posX, posY); 
  }
  
  public void setFont(PFont f) {
    this.f = f; 
  }
}