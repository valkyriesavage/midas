class TraceRouter implements StuffDoer {
  
  SVGExporter exporter = new SVGExporter();
  public boolean save = false;
  
  public TraceRouter() {}
  
  public void doStuff() {}
  
  public void prepSVGSave(Paths chosen) {
    exporter.prepSVGSave(chosen); 
  }
  
  public void saveToSVG(Paths chosen) {
    exporter.saveToSVG(chosen);
  }
}