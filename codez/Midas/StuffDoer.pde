interface StuffDoer {
  public void doStuff(); 
}

class SensorAdder implements StuffDoer {
  ArrayList<Sensor> sensors;
  Shape shape;
  public SensorAdder(ArrayList<Sensor> sensors, Shape shape) {
    this.sensors = sensors;
    this.shape = shape;
  }
  public void doStuff() {
    Sensor newSensor = new Sensor(shape);
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