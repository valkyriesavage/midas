class TraceRouter implements StuffDoer {
  
  SVGExporter exporter = new SVGExporter();
  public boolean save = false;
  
  ArrayList<Sensor> sensors;
  ArrayList<Obstacle> obstacles;
  ArrayList<Trace> traces;
  ArrayList<Point> terminals;
  
  float pxPermm;
  
  public TraceRouter(ArrayList<Sensor> sensors, ArrayList<Obstacle> obstacles, ArrayList<Trace> traces) {
    this.sensors = sensors;
    this.obstacles = obstacles;
    this.traces = traces;
    this.pxPermm = 1.0;
  }
  
  public void setScaleFactor(float scaleFactor) {
    this.pxPermm = scaleFactor;
  }
  
  public void doStuff() {
    for (Sensor sensor : sensors) {
      sensor.lock(); 
    }
  }
  
  public void prepSVGSave(Paths chosen) {
    exporter.prepSVGSave(chosen); 
  }
  
  public void saveToSVG(Paths chosen) {
    exporter.saveToSVG(chosen);
  }
}

class Grid {
  float wd, ht;
  float pxPermm;
  
  ArrayList<Point> padCoordinates;
  
  ArrayList<Integer> grid;
  
  private void initializePadCoordinates() {
    padCoordinates = new ArrayList<Point>();
    int[] pads = {0,1,2,3,4};
    float firstCenter = 5.08;
    float interPadDistance = 10.16;
    for (int i : pads) {
      Point pad = new Point(0,(firstCenter + i*interPadDistance)/pxPermm);
      padCoordinates.add(pad);
    }
  }
  
  public Grid(float wd, float ht, float pxPermm) {
    this.wd = wd;
    this.ht = ht;
    this.pxPermm = pxPermm;
    initializePadCoordinates();
  }
  
  public Trace routeAToB(Point a, Point b) {
    return new Trace(new Point(0,0), new Point(0,0), 0); 
  }
}

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