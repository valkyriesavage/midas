class TraceRouter implements StuffDoer {
  
  SVGExporter exporter = new SVGExporter();
  public boolean save = false;
  
  public TraceRouter() {}
  
  public void doStuff() {}
  
  public void prepSVGSave() {
    exporter.prepSVGSave(); 
  }
  
  public void saveToSVG() {
    exporter.saveToSVG();
  }
}