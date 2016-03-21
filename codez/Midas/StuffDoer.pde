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

class ObstacleAdder implements StuffDoer {
  ArrayList<Obstacle> obstacles;
  public ObstacleAdder(ArrayList<Obstacle> obstacles) {
    this.obstacles = obstacles;
  }
  public void doStuff() {
    for (Obstacle obstacle : obstacles) {
      obstacle.locked = true;
    }
    Obstacle obstacle = new Obstacle();
    obstacles.add(obstacle);
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

class TestToggler implements StuffDoer {
  ClickableBox toUpdate;
  boolean testing = false;
  public TestToggler(ClickableBox toUpdate) {
    this.toUpdate = toUpdate;
  }
  public void doStuff() {
    testing = !testing;
    if (testing) {
      toUpdate.label = "test mode is on";
      toUpdate.setBaseColor(#54ED64);
    }
    else {
      toUpdate.label = "test mode is off";
      toUpdate.setBaseColor(ClickableBox.DEFAULT_COLOR);
    }
  }
}