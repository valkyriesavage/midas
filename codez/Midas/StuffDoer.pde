interface StuffDoer {
  public void doStuff(); 
}

class SensorAdder implements StuffDoer {
  ArrayList<Sensor> sensors;
  public SensorAdder(ArrayList<Sensor> sensors) {
    this.sensors = sensors;
  }
  public void doStuff() {
    Sensor newSensor = new Sensor();
    sensors.add(newSensor);
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