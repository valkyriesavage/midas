class Trace {
  Point padConnection;
  Point goalAnchor;
  ArrayList<Point> vertices;
  float widthInPx;
  
  public Trace(Point padConnection, Point goalAnchor, float widthInPx) {
    this.padConnection = padConnection;
    this.goalAnchor = goalAnchor;
    this.widthInPx = widthInPx;
  }
  
  
}