import interfascia.*;
//import jnativehook.*;

GUIController c;
IFTextField bgwd;
DrawableLabel bght;

PImage background;
float pxPermm = 10.0;

int menuWd = 300;
ArrayList<ClickableBox> menu = new ArrayList<ClickableBox>();
ArrayList<DrawableLabel> labels = new ArrayList<DrawableLabel>();
ArrayList<Sensor> sensors = new ArrayList<Sensor>();
ArrayList<Obstacle> obstacles = new ArrayList<Obstacle>();
ArrayList<Trace> traces = new ArrayList<Trace>();

TraceRouter router = new TraceRouter(sensors, obstacles, traces);

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
  if (router.save) {
    router.prepSVGSave(Paths.TRACES);
  }
  for (Sensor sensor : sensors) {
    sensor.drawIt();
  }
  if (router.save) {
    router.saveToSVG(Paths.TRACES);
    router.prepSVGSave(Paths.OBSTACLES);
    // first draw an outline of the image
    fill(255);
    stroke(0);
    // outline of image; for some reason this is drawing in corner mode instead of radius mode??
    rect(menuWd,0,width-menuWd,height);
  }
  for (Obstacle obstacle : obstacles) {
    obstacle.drawIt();
  }
  if (router.save) {
    router.saveToSVG(Paths.OBSTACLES);
    router.save = false;
  }
}

void mousePressed() {
  for (ClickableBox button : menu) {
    button.clickMouse();
  }
  for (Sensor sensor : sensors) {
    sensor.clickMouse();
  }
  if (obstacles.size() > 0) {
    obstacles.get(obstacles.size() - 1).clickMouse();
  }
}

void mouseDragged() {
  for (ClickableBox button : menu) {
    button.dragMouse();
  }
  for (Sensor sensor : sensors) {
    sensor.dragMouse();
  }
  if (obstacles.size() > 0) {
    obstacles.get(obstacles.size() - 1).dragMouse();
  }
}

void mouseReleased() {
  for (ClickableBox button : menu) {
    button.releaseMouse();
  }
  for (Sensor sensor : sensors) {
    sensor.releaseMouse();
  }
  if (obstacles.size() > 0) {
    obstacles.get(obstacles.size() - 1).releaseMouse();
  }
}

void actionPerformed (GUIEvent e) {
  float pxWide = background.width;
  float pxHigh = background.height;
  if (e.getSource() == bgwd) {
    pxPermm = pxWide/float(bgwd.getValue());
    float roundedHt = round(pxHigh/pxPermm * 10.0)/10.0;
    bght.setText(str(roundedHt));
    router.setScaleFactor(pxPermm);
  } else {
    return; 
  }
}

void createMenu() {
  int wd = 95;
  int ht = 20;
  int posX = int(menuWd/2.0);
  
  ClickableBox loadImage =  new ClickableBox("load image", posX, 100, wd, ht);
  loadImage.setStuffDoer(new BackgroundChooser("backgroundSelected"));
  menu.add(loadImage);
 
  PFont f = createFont("Lato-Bold.ttf", 12);DrawableLabel wid = new DrawableLabel("width (mm):", posX-wd+15, 140);
  DrawableLabel hte = new DrawableLabel("height (mm):", posX+35, 140);
  bght = new DrawableLabel("NaN", posX+85, 140);
  wid.setFont(f);
  hte.setFont(f);
  bght.setFont(f);
  labels.add(wid);
  labels.add(hte);
  labels.add(bght);
  c = new GUIController(this);
  bgwd = new IFTextField("width", posX-45, 125, 40);
  c.add(bgwd);
  bgwd.addActionListener(this);
  bgwd.setValue("83");
  
  int rowY = 225;
  SensorAddButton addRectangle = new SensorAddButton(posX-80,rowY,Shape.RECTANGLE);
  addRectangle.setStuffDoer(new SensorAdder(sensors, Shape.RECTANGLE));
  menu.add(addRectangle);
  SensorAddButton addCircle = new SensorAddButton(posX,rowY,Shape.CIRCLE);
  addCircle.setStuffDoer(new SensorAdder(sensors, Shape.CIRCLE));
  menu.add(addCircle);
  SensorAddButton addCustom = new SensorAddButton(posX+80,rowY,Shape.OTHER);
  addCustom.setStuffDoer(new CustomSensorAdder(sensors));
  menu.add(addCustom);
  
  ClickableBox addObstacle = new ClickableBox("draw new obstacle", posX, 325, wd, ht);
  addObstacle.setStuffDoer(new ObstacleAdder(obstacles));
  menu.add(addObstacle);
  
  ClickableBox routeTraces =  new ClickableBox("route traces", posX, 425, wd, ht);
  routeTraces.setStuffDoer(router);
  menu.add(routeTraces);
  
  ClickableBox toggleTest =  new ClickableBox("test mode is off", posX, 525, wd, ht);
  TestToggler toggler = new TestToggler(toggleTest);
  /*try {
    GlobalScreen.registerNativeHook();
  } catch (NativeHookException ex) {
    println(ex.getMessage());
    System.exit(1);
  }
  GlobalScreen.addNativeMouseListener(toggler);
  GlobalScreen.addNativeMouseMotionListener(toggler);
  GlobalScreen.addNativeKeyListener(toggler);*/
  toggleTest.setStuffDoer(toggler);
  menu.add(toggleTest);
  
  String[] sectionLabels = {"change background object",
                            "add some sensors",
                            "add some routing obstacles",
                            "route your traces",
                            "test your design"};
  int idx = 0;
  for (String sectionLabel : sectionLabels) {
    int adjustment = 70;
    if (idx > 0) adjustment += 25;
    labels.add(new DrawableLabel(sectionLabel, posX, idx*100+adjustment));
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