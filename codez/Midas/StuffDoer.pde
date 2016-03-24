/*import jnativehook.*;
import jnativehook.keyboard.*;
import jnativehook.mouse.*;*/

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

class TestToggler implements StuffDoer { //, NativeKeyListener, NativeMouseInputListener {
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
  
  /*public void nativeKeyPressed(NativeKeyEvent e) {
    if(testing) {
      println("Key pressed: " + e.getKeyCode());
    }
  }
  public void nativeKeyReleased(NativeKeyEvent e) {
    if(testing) {
      println("Key released:" + e.getKeyCode());
    }
  }
  public void nativeKeyTyped(NativeKeyEvent e) {
    if(testing) {
      println("Key typed:" + e.getKeyCode());
    }
  }
  
  public void nativeMouseClicked(NativeMouseEvent e) {
    if(testing) {
      println("Mouse Clicked: " + e.getClickCount());
    }
  }
  public void nativeMousePressed(NativeMouseEvent e) {
    if(testing) {
      println("Mouse Pressed: " + e.getButton());
    }
  }
  public void nativeMouseReleased(NativeMouseEvent e) {
    if(testing) {
      println("Mouse Pressed: " + e.getButton());
    }
  }
  public void nativeMouseMoved(NativeMouseEvent e) {
    if(testing) {
      println("Mouse Moved: " + e.getX() + ", " + e.getY());
    }
  }
  public void nativeMouseDragged(NativeMouseEvent e) {
    if(testing) {
      println("Mouse Dragged: " + e.getX() + ", " + e.getY());
    }
  }*/
}