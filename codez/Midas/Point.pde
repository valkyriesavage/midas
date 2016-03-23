class Point {
  float x;
  float y;
  public Point(float x, float y) {
    this.x = x;
    this.y = y;
  }
  
  public float distance(Point v) {
    return sqrt(pow(v.x-x,2)+pow(v.y-y,2)); 
  }
}