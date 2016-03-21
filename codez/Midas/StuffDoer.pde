interface StuffDoer {
  public void doStuff(); 
}

class SensorAdder implements StuffDoer {
  public SensorAdder() { }
  public void doStuff() {
    
  }
}

class BackgroundChooser implements StuffDoer {
  String callback;
  public BackgroundChooser(String callback) {
    this.callback = callback;
  }
  public void doStuff() {
    selectInput("choose an object to make sensors for:", callback);
  }
}