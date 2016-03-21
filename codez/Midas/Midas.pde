PImage background;

ArrayList<ClickableBox> menu = new ArrayList<ClickableBox>();
DrawableLabel[] labels;
ArrayList<Sensor> sensors = new ArrayList<Sensor>();

void setup() {
  size(1000, 1000);
  background = loadImage("/Users/valkyrie/documents/images/hatrix-deepdream.jpg");
  createMenu();
  surface.setTitle("Midas: Create Custom Capacitive Touch Sensors");
}

void draw() {
  fill(155);
  stroke(155);
  rectMode(RADIUS);
  ellipseMode(RADIUS);
  rect(0,0,3000,3000);
  image(background, 0, 0);
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
  int posX = 850;
  String[] buttonLabels = {"load image", "add sensor", "route traces", "test mode is off"};
  
  ClickableBox loadImage =  new ClickableBox("load image", posX, 100, wd, ht);
  loadImage.setStuffDoer(new BackgroundChooser("backgroundSelected"));
  menu.add(loadImage);
  
  int row1Y = 200;
  int row2Y = 250;
  SensorAddButton addRectangle = new SensorAddButton(posX-80,row1Y,Shape.RECTANGLE);
  addRectangle.setStuffDoer(new SensorAdder(sensors, Shape.RECTANGLE));
  menu.add(addRectangle);
  SensorAddButton addCircle = new SensorAddButton(posX,row1Y,Shape.CIRCLE);
  addCircle.setStuffDoer(new SensorAdder(sensors, Shape.CIRCLE));
  menu.add(addCircle);
  
  
  String[] sectionLabels = {"add a background",
                            "add some sensors",
                            "route your traces",
                            "test your design"};
  labels = new DrawableLabel[sectionLabels.length];
  int idx = 0;
  for (String sectionLabel : sectionLabels) {
    labels[idx] = new DrawableLabel(sectionLabel, 850, idx*100+75);
    idx++;
  }
}

void drawMenu() {
  fill(195);
  stroke(195);
  rect(1000, 0, 300, 1000);
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