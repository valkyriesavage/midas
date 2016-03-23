class TraceRouter implements StuffDoer {
  
  SVGExporter exporter = new SVGExporter();
  public boolean save = false;
  
  ArrayList<Sensor> sensors;
  ArrayList<Obstacle> obstacles;
  
  public TraceRouter(ArrayList<Sensor> sensors, ArrayList<Obstacle> obstacles) {
    this.sensors = sensors;
    this.obstacles = obstacles;
  }
  
  public void doStuff() {}
  
  public void prepSVGSave(Paths chosen) {
    exporter.prepSVGSave(chosen); 
  }
  
  public void saveToSVG(Paths chosen) {
    exporter.saveToSVG(chosen);
  }
}