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