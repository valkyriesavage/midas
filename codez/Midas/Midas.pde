PImage background;

int menuWd = 300;
ArrayList<ClickableBox> menu = new ArrayList<ClickableBox>();
DrawableLabel[] labels;
ArrayList<Sensor> sensors = new ArrayList<Sensor>();

void setup() {
  size(855, 1100);
  background = loadImage("example.jpg");
  createMenu();
  surface.setTitle("Midas: Create Custom Capacitive Touch Sensors");
  surface.setResizable(true);
}

void draw() {
  fill(155);
  stroke(155);
  rectMode(RADIUS);
  ellipseMode(RADIUS);
  rect(0,0,width*2,height*2);
  image(background, menuWd, 0);
  drawMenu();
  for (Sensor sensor : sensors) {
    sensor.drawIt();
  }
}

void mousePressed() {
  for (ClickableBox button : menu) {
    button.clickMouse();
  }
  for (Sensor sensor : sensors) {
    sensor.clickMouse();
  }
}

void mouseDragged() {
  for (ClickableBox button : menu) {
    button.dragMouse();
  }
  for (Sensor sensor : sensors) {
    sensor.dragMouse();
  }
}

void mouseReleased() {
  for (ClickableBox button : menu) {
    button.releaseMouse();
  }
  for (Sensor sensor : sensors) {
    sensor.releaseMouse();
  }
}

void createMenu() {
  int wd = 95;
  int ht = 20;
  int posX = int(menuWd/2.0);
  
  ClickableBox loadImage =  new ClickableBox("load image", posX, 100, wd, ht);
  loadImage.setStuffDoer(new BackgroundChooser("backgroundSelected"));
  menu.add(loadImage);
  
  int rowY = 200;
  SensorAddButton addRectangle = new SensorAddButton(posX-80,rowY,Shape.RECTANGLE);
  addRectangle.setStuffDoer(new SensorAdder(sensors, Shape.RECTANGLE));
  menu.add(addRectangle);
  SensorAddButton addCircle = new SensorAddButton(posX,rowY,Shape.CIRCLE);
  addCircle.setStuffDoer(new SensorAdder(sensors, Shape.CIRCLE));
  menu.add(addCircle);
  SensorAddButton addCustom = new SensorAddButton(posX+80,rowY,Shape.OTHER);
  addCustom.setStuffDoer(new CustomSensorAdder(sensors));
  menu.add(addCustom);
  
  ClickableBox routeTraces =  new ClickableBox("route traces", posX, 300, wd, ht);
  routeTraces.setStuffDoer(new TraceRouter());
  menu.add(routeTraces);
  
  ClickableBox toggleTest =  new ClickableBox("test mode is off", posX, 400, wd, ht);
  toggleTest.setStuffDoer(new TestToggler());
  menu.add(toggleTest);
  
  String[] sectionLabels = {"change background object",
                            "add some sensors",
                            "route your traces",
                            "test your design"};
  labels = new DrawableLabel[sectionLabels.length];
  int idx = 0;
  for (String sectionLabel : sectionLabels) {
    labels[idx] = new DrawableLabel(sectionLabel, posX, idx*100+75);
    idx++;
  }
}

void drawMenu() {
  fill(195);
  stroke(195);
  rect(0, 0, menuWd, height*2);
  for (DrawableLabel label : labels) {
    label.drawIt();
  }
  for (ClickableBox button : menu) {
    button.drawIt();
  }
}

void backgroundSelected(File bg) {
  if (bg != null) {
    background = loadImage(bg.getAbsolutePath());
  }
}
  
void makeNewCustomSensor(File sensor) {
  if (sensor != null) {
    PImage sensorImg = loadImage(sensor.getAbsolutePath());
    Sensor newSensor = new Sensor(Shape.OTHER);
    newSensor.setImage(sensorImg);
    sensors.add(newSensor);
  }
}