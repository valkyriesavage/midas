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

class CustomSensorAdder extends SensorAdder {
  public CustomSensorAdder(ArrayList<Sensor> sensors) {
    super(sensors, Shape.OTHER); 
  }
  public void doStuff() {
    selectInput("choose an object to make sensors for:", "makeNewCustomSensor");
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

class TraceRouter implements StuffDoer {
  public TraceRouter() {}
  public void doStuff() {}
}

class TestToggler implements StuffDoer {
  public TestToggler() {}
  public void doStuff() {}
}