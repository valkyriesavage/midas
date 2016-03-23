class Obstacle {
  final public static color OBSTACLE = #CBB3AB;
  
  ArrayList<Point> vertices;
  boolean locked = false;
  
  final static float LOCK_THRESH = 10;
  final static float XMIN = 300; // sloppy, but don't want to draw any obstacles in these places
  
  public Obstacle() {
    vertices = new ArrayList<Point>(); 
  }
  
  public void drawIt() {
    fill(OBSTACLE);
    stroke(OBSTACLE);
    beginShape();
    for (Point v : vertices) {
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
    if (vertices.size() > 30 && vertices.get(0).distance(new Point(mouseX,mouseY)) < LOCK_THRESH) {
      locked = true; 
    }
    if (!locked) {
      vertices.add(new Point(mouseX, mouseY));
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
    if (vertices.size() > 2 && vertices.get(0).distance(new Point(mouseX,mouseY)) < LOCK_THRESH) {
      locked = true; 
    }
    if (!locked) {
      vertices.add(new Point(mouseX, mouseY));
    }
  }
  
  public void releaseMouse() {}

}