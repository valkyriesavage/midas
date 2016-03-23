class Obstacle {
  final public static color OBSTACLE = #CBB3AB;
  
  ArrayList<Vertex> vertices;
  boolean locked = false;
  
  final static float LOCK_THRESH = 10;
  final static float XMIN = 300; // sloppy, but don't want to draw any obstacles in these places
  
  public Obstacle() {
    vertices = new ArrayList<Vertex>(); 
  }
  
  public void drawIt() {
    fill(OBSTACLE);
    stroke(OBSTACLE);
    beginShape();
    for (Vertex v : vertices) {
      vertex(v.x,v.y); 
    }
    endShape(CLOSE);
  }
  
  public void dragMouse() {
    if (mouseX < XMIN) {
      if (vertices.size() > 2) {
        locked = true; 
      }
      else {
        return;
      }
    }
    if (vertices.size() > 30 && vertices.get(0).distance(new Vertex(mouseX,mouseY)) < LOCK_THRESH) {
      locked = true; 
    }
    if (!locked) {
      vertices.add(new Vertex(mouseX, mouseY));
    }
  }
  
  public void clickMouse() {
    if (mouseX < XMIN) {
      if (vertices.size() > 2) {
        locked = true; 
      }
      else {
        return;
      }
    }
    if (vertices.size() > 2 && vertices.get(0).distance(new Vertex(mouseX,mouseY)) < LOCK_THRESH) {
      locked = true; 
    }
    if (!locked) {
      vertices.add(new Vertex(mouseX, mouseY));
    }
  }
  
  public void releaseMouse() {}

  class Vertex {
    float x;
    float y;
    public Vertex(float x, float y) {
      this.x = x;
      this.y = y;
    }
    
    public float distance(Vertex v) {
      return sqrt(pow(v.x-x,2)+pow(v.y-y,2)); 
    }
  }
}