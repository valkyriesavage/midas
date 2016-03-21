PImage background;

ClickableBox[] menu;
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
  rectMode(CORNER);
  rect(0,0,1000,1000);
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
  String[] buttonLabels = {"load image", "add sensor", "route traces", "test mode is off"};
  menu = new ClickableBox[buttonLabels.length];
  int idx = 0;
  for (String buttonLabel : buttonLabels) {
    ClickableBox newButton = new ClickableBox(buttonLabel, 850, idx*100+100, 95, 20);
    if (buttonLabel.equals("load image")) {
      newButton.setStuffDoer(new BackgroundChooser("backgroundSelected"));
    }
    if (buttonLabel.equals("add sensor")) {
      newButton.setStuffDoer(new SensorAdder(sensors)); 
    }
    menu[idx] = newButton;
    idx++;
  }
  String[] sectionLabels = {"add a background",
                            "add some sensors",
                            "route your traces",
                            "test your design"};
  labels = new DrawableLabel[sectionLabels.length];
  idx = 0;
  for (String sectionLabel : sectionLabels) {
    labels[idx] = new DrawableLabel(sectionLabel, 850, idx*100+75);
    idx++;
  }
}

void drawMenu() {
  rectMode(RADIUS);
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