package pathway;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import display.ArduinoSensorButton;
import display.CanvasPanel;
import display.SensorButtonGroup;
import display.SetUp;

public class SVGPathwaysGenerator {

  SetUp mySetup;
  private List<List<Point>> topLayerPaths = new ArrayList<List<Point>>(),
                            bottomLayerPaths = new ArrayList<List<Point>>();
  private Map<ArduinoSensorButton, Integer> buttonMap = new HashMap<ArduinoSensorButton, Integer>();
  private Map<ArduinoSensorButton, Pair<Integer, Integer>> buttonPadMap = new HashMap<ArduinoSensorButton, Pair<Integer, Integer>>();

  public SVGPathwaysGenerator(SetUp s) {
    mySetup = s;
  }

  public static boolean PRINT_DEBUG = true;

  public static final int LINE_EXTENT = 1;

  public static final int LINE_WIDTH = LINE_EXTENT * 2 + 1;
  public static final int BUTTON_INFLUENCE_WIDTH = LINE_WIDTH + LINE_EXTENT;
  public static final int PATH_INFLUENCE_WIDTH = 2 * LINE_WIDTH;
  
  // TODO : debug why we need this.  paths don't draw correctly on the interface if we don't have it,
  // and the outline of the canvas draws wrong on the SVG if we don't have it.
  private static final int CORRECTION = PATH_INFLUENCE_WIDTH*3;

  /**
   * Call this method after a successful generatePathways
   * 
   * @return
   */
  public Map<ArduinoSensorButton, Integer> getButtonMap() {
    return buttonMap;
  }

  public Map<ArduinoSensorButton, Pair<Integer, Integer>> getButtonPadMap() {
    return buttonPadMap;
  }

  /**
   * Returns
   * 
   * @param p
   * @param extent
   * @return
   */
  static List<Point> cellsOfInfluence(Point p, int extent) {
    List<Point> list = new LinkedList<Point>();
    for (int x = p.x - extent; x <= p.x + extent; x++) {
      for (int y = p.y - extent; y <= p.y + extent; y++) {
        list.add(new Point(x, y));
      }
    }
    list.remove(p);

    return list;
  }

  /**
   * Returns all points that are influenced by the given Shape. A point is
   * influenced by a shape if it's coordinates are both within
   * <code>BUTTON_INFLUENCE_WIDTH<code> of the Shape's outline.
   * 
   * @param s
   * @return
   */
  static Iterable<Point> cellsOfInfluence(Shape s) {
    Set<Point> flattened = new HashSet<Point>();
    for (Point p : immediateOutlineFor(s)) {
      flattened.addAll(cellsOfInfluence(p, BUTTON_INFLUENCE_WIDTH));
    }
    return flattened;
  }

  /**
   * Returns all points that are influenced by the given path. A point is
   * influenced by a path if the point's coordinates are both within
   * <code>PATH_INFLUENCE_WIDTH</code> distance from any point on the Path.
   * 
   * @param path
   * @return
   */
  static Iterable<Point> cellsOfInfluence(List<Point> path) {
    Set<Point> flattened = new HashSet<Point>();
    for (Point p : path) {
      flattened.addAll(cellsOfInfluence(p, PATH_INFLUENCE_WIDTH));
    }
    return flattened;
  }

  private void point(Graphics2D g, int x1, int y1) {
    g.drawRect(x1 - LINE_EXTENT, y1 - LINE_EXTENT, LINE_WIDTH, LINE_WIDTH);
  }
  
  public void removePaths() {
    topLayerPaths = new ArrayList<List<Point>>();
    bottomLayerPaths = new ArrayList<List<Point>>();
  }

  public void paint(Graphics2D g) {
    g.setColor(CanvasPanel.LIGHT_COPPER);
    for (List<Point> path : topLayerPaths) {
      for (Point p : path) {
        point(g, p.x, p.y);
      }
    }

    g.setColor(CanvasPanel.DARK_COPPER);
    for (List<Point> path : bottomLayerPaths) {
      for (Point p : path) {
        point(g, p.x, p.y);
      }
    }
  }

  private static enum Corner {

    TOPLEFT(1, new Point(LINE_EXTENT, LINE_EXTENT)), TOPRIGHT(1, new Point(
        SetUp.CANVAS_X - LINE_EXTENT, LINE_EXTENT)), BOTTOMLEFT(-1, new Point(
        LINE_EXTENT, SetUp.CANVAS_Y - LINE_EXTENT)), BOTTOMRIGHT(-1, new Point(
        SetUp.CANVAS_X - LINE_EXTENT, SetUp.CANVAS_Y - LINE_EXTENT));

    private final int ySortDir;
    private final Point start;

    Corner(int yDir, Point start) {
      this.ySortDir = yDir;
      this.start = start;
    }

    Point port(int x) {
      Point p = new Point(start);
      p.y += (1 + PATH_INFLUENCE_WIDTH) * x * ySortDir;
      return p;
    }

    void sort(List<Shape> shapes) {
      Collections.sort(shapes, new Comparator<Shape>() {

        @Override
        public int compare(Shape o1, Shape o2) {
          return (new Double(o1.getBounds2D().getY() * ySortDir)).compareTo(o2
              .getBounds2D().getY() * ySortDir);
        }

      });
    }
  }

  /**
   * Hella Slider "direction"; either do the ports 1-2-3 or 3-2-1
   * 
   * @author hellochar
   * 
   */
  private static enum HSDirection {
    FORWARD, REVERSE
  }

  /**
   * Routing order; either the buttons go first or the hella goes first
   * 
   * @author hellochar
   * 
   */
  private static enum RouteOrder {
    BUTTONFIRST, HELLAFIRST
  }

  /**
   * Locations of the hella slider ports (for the case that the hella slider is
   * in the same corner as the normal buttons); the hella slider ports either go
   * before the button ports or after.
   * 
   * @author hellochar
   * 
   */
  private static enum HSPortLocation {
    BEFORE, AFTER
  }

  private static class SuccessfulException extends Exception {
    private static final long serialVersionUID = 23053463129L;

    List<List<Point>> paths;
    Map<ArduinoSensorButton, Integer> map;
    int buttonStartIndex;
    Corner C;

    SuccessfulException(List<List<Point>> paths,
        Map<ArduinoSensorButton, Integer> map, int buttonStartIndex, Corner C) {
      super();
      this.paths = paths;
      this.map = map;
      this.buttonStartIndex = buttonStartIndex;
      this.C = C;
    }
  }

  /**
   * slider may be null.
   * 
   * @param buttons
   * @param obstacles
   * @param slider
   * @return
   * @throws PathwayGenerationException
   */
  private static SuccessfulException generateLayer(
      List<ArduinoSensorButton> buttons, List<SensorButtonGroup> obstacles,
      SensorButtonGroup slider, int sliderOffset, int startIndex)
      throws PathwayGenerationException {
    try {
      int configNum = 1;
      for (Corner C : Corner.values()) {
        if (slider == null) {
          if (PRINT_DEBUG)
            System.out.println("Attempting config " + (configNum++));
          tryFullGeneration(buttons, obstacles, C, startIndex);
        } else {
          for (HSDirection D : HSDirection.values()) {
            for (RouteOrder O : RouteOrder.values()) {
              for (HSPortLocation L : HSPortLocation.values()) {
                if (PRINT_DEBUG)
                  System.out.println("Attempting config " + (configNum++));
                tryFullGeneration(buttons, obstacles, slider, C, D, O, L,
                    sliderOffset, startIndex);
              }
            }
          }
        }
      }
      throw new PathwayGenerationException();
    } catch (SuccessfulException s) {
      return s;
    }
  }

  /**
   * Returns true if the pathways were successfully generated; false otherwise.
   * 
   * @param groups
   * @param generatePathways
   * @return
   */
  public boolean generatePathways(List<SensorButtonGroup> groups,
      List<SensorButtonGroup> obstacles, boolean generatePathways) {
    topLayerPaths.clear();
    bottomLayerPaths.clear();
    buttonMap.clear();
    buttonPadMap.clear();

    List<ArduinoSensorButton> buttons = new ArrayList<ArduinoSensorButton>();
    SensorButtonGroup slider = null; // possibly null!
    SensorButtonGroup pad = null;

    List<Shape> allShapes = new LinkedList<Shape>();
    List<Shape> mask = new LinkedList<Shape>();

    // Iterate through the groups, separating the slider from the normal buttons
    // while also aggregating all the shapes.
    for (SensorButtonGroup s : groups) {
      if (s.isPad) {
        pad = s;
        for (ArduinoSensorButton b : s.triggerButtons) {
          mask.add(b.getPathwayShape());
        }
      } else if (s.sensitivity == SetUp.HELLA_SLIDER) {
        slider = s;
        allShapes.addAll(s.getHSP().getShapes());
        mask.addAll(s.getHSP().getShapes());
      } else {
        buttons.addAll(s.triggerButtons);
        for (ArduinoSensorButton b : s.triggerButtons) {
          allShapes.add(b.getPathwayShape());
          mask.add(b.getPathwayShape());
        }
      }
    }

    for (SensorButtonGroup sbg : obstacles) {
      for (ArduinoSensorButton b : sbg.triggerButtons) {
        mask.add(b.getPathwayShape());
      }
    }

    writeSVG("mask.svg", mask, null, null, null);
    if (!generatePathways)
      return true;

    try {
      if (pad == null) {
        SuccessfulException s = generateLayer(buttons, obstacles, slider, 0, 0);
        if (PRINT_DEBUG)
          System.out.println("Paths generated!");

        // a generation should have the following information available:
        // a) a list of lists of points, representing the paths
        // b) a mapping between ArduinoSensorButtons to indices, representing
        // which button is mapped to which port.

        topLayerPaths = s.paths; // woo we have the paths now
        buttonMap = s.map;

        writeSVG("outline.svg", allShapes, s.paths, null, null);
      } else {
        // get all grouped projections for pad
        // create fake arduinosensorbuttons for top layer of projections; call
        // it
        int side_num = (int) Math.sqrt(pad.sensitivity);
        List<ArduinoSensorButton> padButtons = pad.triggerButtons;
        List<Shape> padShapes = new ArrayList<Shape>(); // This contains all 9
                                                        // square buttons of the
                                                        // pad
        for (ArduinoSensorButton b : padButtons) {
          padShapes.add(b.getPathwayShape());
        }

        Pair<List<Area>, List<Area>> projections = GenerateGridUtils
            .splitDiagonally(padShapes);

        // topLefts will be grouped horizontally
        List<Area> topLefts = projections._1;
        List<Shape> topLeftGrouped = new ArrayList<Shape>();
        for (int x = 0; x < side_num; x++) {
          Area groupedArea = connectShapes(new LinkedList<Area>(topLefts.subList(x * side_num,
              (x + 1) * side_num)));
          topLeftGrouped.add(groupedArea);
        }

        // bottomRights will be grouped vertically, and routed on the bottom
        // layer
        List<Area> bottomRights = projections._2;
        List<Shape> bottomRightGrouped = new ArrayList<Shape>();
        for (int x = 0; x < side_num; x++) {
          // We want to get item x, x + side_num, x + 2*side_num, ... x +
          // (side_num - 1) * side_num
          List<Area> components = new LinkedList<Area>();
          for (int y = 0; y < side_num; y++)
            components.add(bottomRights.get(x + y * side_num));
          Area groupedArea = connectShapes(components);
          bottomRightGrouped.add(groupedArea);
        }

        List<FakeArduinoSensorButton> fakeTopLefts = new ArrayList<FakeArduinoSensorButton>();
        for (Shape a : topLeftGrouped) {
          fakeTopLefts.add(fakeArduinoButtonFor(a));
        }

        List<FakeArduinoSensorButton> fakeBottomRights = new ArrayList<FakeArduinoSensorButton>();
        for (Shape a : bottomRightGrouped) {
          fakeBottomRights.add(fakeArduinoButtonFor(a));
        }

        List<ArduinoSensorButton> topLayerButtons = new ArrayList<ArduinoSensorButton>(buttons);
        topLayerButtons.addAll(fakeTopLefts); // topLefts will be routed on the
                                              // top layer

        List<ArduinoSensorButton> bottomLayerButtons = new ArrayList<ArduinoSensorButton>();
        bottomLayerButtons.addAll(fakeBottomRights); // bottomRights on bottom
                                                  // layer

        SuccessfulException sTop = generateLayer(topLayerButtons, obstacles,
            slider, bottomLayerButtons.size(), 0);

        SuccessfulException sBottom;
        try {
          tryFullGeneration(bottomLayerButtons, obstacles, sTop.C,
              sTop.buttonStartIndex + topLayerButtons.size());
          throw new PathwayGenerationException();
        } catch (SuccessfulException e) {
          sBottom = e;
        }

        // Need: allPaths, buttonMap (for top layer individual), buttonPadMap
        // (reconstruct)
        topLayerPaths = sTop.paths;
        bottomLayerPaths = sBottom.paths;

        buttonMap = sTop.map;

        buttonPadMap = new HashMap<ArduinoSensorButton, Pair<Integer, Integer>>();
        for (int x = 0; x < padButtons.size(); x++) {
          int p = x % side_num, q = x / side_num;

          int jm = sBottom.map.get(fakeBottomRights.get(p)) + sTop.buttonStartIndex + topLayerButtons.size();
          int in = sTop.map.get(fakeTopLefts.get(q));
          buttonPadMap.put(padButtons.get(x), new Pair<Integer, Integer>(jm, in));
        }

        // WriteSVG for bottom layer (need shapes for bottom layer, paths for
        // bottom layer), top layer (need shapes and paths)
        List<Shape> topLayerShapes = new LinkedList<Shape>();
        for (ArduinoSensorButton b : topLayerButtons) {
          topLayerShapes.add(b.getPathwayShape());
        }
        if(slider != null)
          topLayerShapes.addAll(slider.getHSP().getShapes());

        List<Shape> bottomLayerShapes = new LinkedList<Shape>();
        for (ArduinoSensorButton b : bottomLayerButtons) {
          bottomLayerShapes.add(b.getPathwayShape());
        }
        writeSVG("outline.svg", topLayerShapes, topLayerPaths, bottomLayerShapes, bottomLayerPaths);

      }
      return true;
    } catch (PathwayGenerationException e) {
      JOptionPane
          .showMessageDialog(
              null,
              "Stickers could not be routed!  Move shapes away from the edge and each other.",
              "sticker routing failure", JOptionPane.ERROR_MESSAGE);
      return false;
    }
  }

  public static class FakeArduinoSensorButton extends ArduinoSensorButton {
    Shape shape;

    public FakeArduinoSensorButton(Shape a) {
      super((display.SensorShape.shapes) null);
      shape = a;
    }

    @Override
    public Shape getPathwayShape() {
      return shape;
    }

  }

  private FakeArduinoSensorButton fakeArduinoButtonFor(Shape a) {
    return new FakeArduinoSensorButton(a);
  }

  private Area connectShapes(List<Area> subList)
      throws PathwayGenerationException {
    Area total = subList.get(0);
    for(int i = 1; i < subList.size(); i++) {
      Area a = subList.get(i);
      Grid g = new Grid();
      List<Point> path = g.findPath(total, immediateOutlineFor(a));
      total.add(pathToArea(path));
      total.add(a);
    }
    return total;
  }

  private Area pathToArea(List<Point> path) {
    Area a = new Area();
    for(Point p : path) {
      a.add(new Area(new Rectangle(p.x - LINE_EXTENT, p.y - LINE_EXTENT,
          LINE_WIDTH, LINE_WIDTH)));
    }
    return a;
  }

  private static void avoidObstacles(Grid g, List<SensorButtonGroup> obstacles) {
    List<Shape> obstaclePolygons = new ArrayList<Shape>();
    for (SensorButtonGroup obstacle : obstacles) {
      obstaclePolygons.add(obstacle.triggerButtons.get(0).getPathwayShape());
    }
    g.restrictExactly(obstaclePolygons);
  }

  // On failure, tryFullGeneration simply returns and does nothing. On success,
  // will throw SuccessfulException carrying the data.
  private static void tryFullGeneration(List<ArduinoSensorButton> buttons,
      List<SensorButtonGroup> obstacles, Corner C, int startIndex)
      throws SuccessfulException {
    Grid g = new Grid();

    Map<Shape, ArduinoSensorButton> shapeToButton = new HashMap<Shape, ArduinoSensorButton>();
    List<Shape> buttonShapes = new ArrayList<Shape>();
    for (ArduinoSensorButton b : buttons) {
      Shape s = b.getPathwayShape();
      buttonShapes.add(s);
      shapeToButton.put(s, b);
    }
    C.sort(buttonShapes);
    // restrict shape outlines - restrict all buttonShapes
    g.restrictAll(buttonShapes);

    avoidObstacles(g, obstacles);

    // create port-maps between buttons and their respective points - iterate
    // through, make points
    List<Pair<Shape, Point>> buttonGenPairs = new LinkedList<Pair<Shape, Point>>();
    int x = startIndex;
    for (Shape s : buttonShapes) {
      buttonGenPairs.add(new Pair<Shape, Point>(s, C.port(x)));
      x++;
    }

    try {
      // generate wirings depending on order - possibly do generateIndividual;
      // may have to decompose method

      Pair<List<List<Point>>, Map<Shape, Integer>> buttonInfo = generateIndividual(
          g, buttonGenPairs);
      List<List<Point>> paths = new ArrayList<List<Point>>();
      paths.addAll(buttonInfo._1);

      Map<ArduinoSensorButton, Integer> buttonMap = new HashMap<ArduinoSensorButton, Integer>();
      for (Map.Entry<Shape, Integer> entry : buttonInfo._2.entrySet()) {
        buttonMap.put(shapeToButton.get(entry.getKey()), entry.getValue());
      }

      throw new SuccessfulException(paths, buttonMap, 0, C);

    } catch (PathwayGenerationException e) {
      return;
    }
  }

  // On failure, tryFullGeneration simply returns and does nothing. On success,
  // will throw SuccessfulException carrying the data.
  private static void tryFullGeneration(List<ArduinoSensorButton> buttons,
      List<SensorButtonGroup> obstacles, SensorButtonGroup slider, Corner C,
      HSDirection D, RouteOrder O, HSPortLocation L, int sliderOffset,
      int startIndex) throws SuccessfulException {
    Grid g = new Grid();

    Map<Shape, ArduinoSensorButton> shapeToButton = new HashMap<Shape, ArduinoSensorButton>();
    List<Shape> buttonShapes = new ArrayList<Shape>();
    for (ArduinoSensorButton b : buttons) {
      Shape s = b.getPathwayShape();
      buttonShapes.add(s);
      shapeToButton.put(s, b);
    }
    List<Shape> sliderShapes = slider.getHSP().getShapes();
    C.sort(buttonShapes);

    // restrict shape outlines - restrict all buttonShapes, all sliderShapes
    g.restrictAll(buttonShapes);
    g.restrictAll(sliderShapes);

    avoidObstacles(g, obstacles);

    // create port-maps between buttons and their respective points - iterate
    // through, make points depending on L and D
    List<Pair<Shape, Point>> buttonGenPairs = new LinkedList<Pair<Shape, Point>>();
    List<Pair<Shape, Point>> sliderGenPairs = new LinkedList<Pair<Shape, Point>>();
    int x = startIndex;
    int buttonStartIndex;
    switch (L) {
    case AFTER:
      buttonStartIndex = 0;
      for (Shape s : buttonShapes) {
        buttonGenPairs.add(new Pair<Shape, Point>(s, C.port(x)));
        x++;
      } //fills ports 0...buttonShapes.size
      x += sliderOffset;
      for (Shape s : sliderShapes) {
        sliderGenPairs.add(new Pair<Shape, Point>(s, C.port(x)));
        x++;
      } //fills ports buttonShapes.size + sliderOffset ... buttonShapes.size + sliderOffset + sliderShapes.size
      break;

    case BEFORE:
      buttonStartIndex = sliderShapes.size();
      for (Shape s : sliderShapes) {
        sliderGenPairs.add(new Pair<Shape, Point>(s, C.port(x)));
        x++;
      } //fills ports 0...sliderShapes.size
      for (Shape s : buttonShapes) {
        buttonGenPairs.add(new Pair<Shape, Point>(s, C.port(x)));
        x++;
      } //fills ports sliderShapes.size ... sliderShapes.size + buttonShapes.size
      break;
    default:
      throw new RuntimeException("Should not be here");
    }
    if (D == HSDirection.REVERSE) {
      Point p1 = sliderGenPairs.get(0)._2, p3 = sliderGenPairs.get(2)._2;
      sliderGenPairs.get(0)._2 = p3;
      sliderGenPairs.get(2)._2 = p1; // swap orders
    }
    // generate wirings depending on order - possibly do generateIndividual; may
    // have to decompose method; generate depending on O

    switch (O) {
    case BUTTONFIRST:

      try {
        // generate wirings depending on order - possibly do
        // generateIndividual; may have to decompose method
        Pair<List<List<Point>>, Map<Shape, Integer>> buttonInfo = generateIndividual(
            g, buttonGenPairs);
        Pair<List<List<Point>>, Map<Shape, Integer>> sliderInfo = generateIndividual(
            g, sliderGenPairs);
        List<List<Point>> paths = new ArrayList<List<Point>>();
        paths.addAll(buttonInfo._1);
        paths.addAll(sliderInfo._1);

        Map<ArduinoSensorButton, Integer> buttonMap = new HashMap<ArduinoSensorButton, Integer>();
        for (Map.Entry<Shape, Integer> entry : buttonInfo._2.entrySet()) {
          buttonMap.put(shapeToButton.get(entry.getKey()), entry.getValue());
        }

        throw new SuccessfulException(paths, buttonMap, buttonStartIndex, C);
      } catch (PathwayGenerationException e) {
        return;
      }

    case HELLAFIRST:

      try {
        // generate wirings depending on order - possibly do
        // generateIndividual; may have to decompose method

        Pair<List<List<Point>>, Map<Shape, Integer>> sliderInfo = generateIndividual(
            g, sliderGenPairs); // WOO DO SLIDER FIRST

        Pair<List<List<Point>>, Map<Shape, Integer>> buttonInfo = generateIndividual(
            g, buttonGenPairs);

        List<List<Point>> paths = new ArrayList<List<Point>>();
        paths.addAll(buttonInfo._1);
        paths.addAll(sliderInfo._1);

        Map<ArduinoSensorButton, Integer> buttonMap = new HashMap<ArduinoSensorButton, Integer>();
        for (Map.Entry<Shape, Integer> entry : buttonInfo._2.entrySet()) {
          buttonMap.put(shapeToButton.get(entry.getKey()), entry.getValue());
        }

        throw new SuccessfulException(paths, buttonMap, buttonStartIndex, C);
      } catch (PathwayGenerationException e) {
        return;
      }
    }

    // should this ever actually run?
    throw new RuntimeException("not implemented yet");
  }

  /**
   * Returns a pair whose _1 is the list of paths, and whose _2 is the mapping
   * between input shapes and integers.
   * 
   * @param g
   * @param pairs
   * @return
   * @throws PathwayGenerationException
   */
  private static Pair<List<List<Point>>, Map<Shape, Integer>> generateIndividual(
      Grid g, List<Pair<Shape, Point>> pairs) throws PathwayGenerationException {
    List<List<Point>> paths = new ArrayList<List<Point>>();
    Map<Shape, Integer> map = new HashMap<Shape, Integer>();

    int i = 0;
    for (Pair<Shape, Point> p : pairs) { // generate pathways for each button
      if (PRINT_DEBUG)
        System.out
            .println("\tGenerating path " + (++i) + " of " + pairs.size());
      Shape button = p._1;
      Point port = p._2;

      List<Point> path = g.findPath(button, port);
      paths.add(path);

      map.put(button, i);
      // if(path != null)
      g.close(cellsOfInfluence(path));
    }

    if(PRINT_DEBUG) System.out.println("\tFinished generating "+pairs.size()+" paths!");
    return new Pair<List<List<Point>>, Map<Shape, Integer>>(paths, map);
  }

  // taken from
  // http://stackoverflow.com/questions/8144156/using-pathiterator-to-return-all-line-segments-that-constrain-an-area
  private static List<Line2D.Double> toSegments(FlatteningPathIterator pi) {
    ArrayList<double[]> areaPoints = new ArrayList<double[]>();
    ArrayList<Line2D.Double> areaSegments = new ArrayList<Line2D.Double>();
    double[] coords = new double[6];

    for (; !pi.isDone(); pi.next()) {
      // The type will be SEG_LINETO, SEG_MOVETO, or SEG_CLOSE
      // Because the Area is composed of straight lines
      int type = pi.currentSegment(coords);
      // We record a double array of {segment type, x coord, y coord}
      double[] pathIteratorCoords = { type, coords[0], coords[1] };
      areaPoints.add(pathIteratorCoords);
    }

    double[] start = new double[3]; // To record where each polygon starts

    for (int i = 0; i < areaPoints.size(); i++) {
      // If we're not on the last point, return a line from this point to
      // the next
      double[] currentElement = areaPoints.get(i);

      // We need a default value in case we've reached the end of the
      // ArrayList
      double[] nextElement = { -1, -1, -1 };
      if (i < areaPoints.size() - 1) {
        nextElement = areaPoints.get(i + 1);
      }

      // Make the lines
      if (currentElement[0] == PathIterator.SEG_MOVETO) {
        start = currentElement; // Record where the polygon started to
        // close it later
      }

      if (nextElement[0] == PathIterator.SEG_LINETO) {
        areaSegments.add(new Line2D.Double(currentElement[1],
            currentElement[2], nextElement[1], nextElement[2]));
      } else if (nextElement[0] == PathIterator.SEG_CLOSE) {
        areaSegments.add(new Line2D.Double(currentElement[1],
            currentElement[2], start[1], start[2]));
      }
    }

    return areaSegments;
  }

  /**
   * Returns a list of points representing the immediate outline for the given
   * Shape.
   * 
   * @param b
   * @return
   */
  public static Set<Point> immediateOutlineFor(Shape b) {
    Set<Point> outline = new HashSet<Point>();

    FlatteningPathIterator p = new FlatteningPathIterator(
        b.getPathIterator(null), 1);
    List<Line2D.Double> segments = toSegments(p);
    for (Line2D.Double seg : segments) {
      int x0 = (int) seg.x1, // possibly do rounding later
      x1 = (int) seg.x2, y0 = (int) seg.y1, y1 = (int) seg.y2;
      // Rasterization from
      // http://en.wikipedia.org/wiki/Bresenham's_line_algorithm
      boolean steep = Math.abs(y1 - y0) > Math.abs(x1 - x0);

      if (steep) {
        int temp = y0; // swap x0, y0
        y0 = x0;
        x0 = temp;

        temp = y1; // swap x1, y1
        y1 = x1;
        x1 = temp;
      }
      if (x0 > x1) {
        // swap x0, x1
        int temp = x1;
        x1 = x0;
        x0 = temp;

        // swap y0, y1
        temp = y1;
        y1 = y0;
        y0 = temp;
      }

      int dx = x1 - x0;
      int dy = Math.abs(y1 - y0);
      float err = 0;
      float dErr = (float) dy / dx;

      int yStep;
      int y = y0;
      if (y0 < y1)
        yStep = 1;
      else
        yStep = -1;
      for (int x = x0; x <= x1; x++) {
        if (steep)
          outline.add(new Point(y, x));
        else
          outline.add(new Point(x, y));

        err += dErr;
        if (err > .5f) {
          y += yStep;
          err -= 1;
        }
      }
    }

    return outline;
  }

  private void writeSVG(String filename, Iterable<Shape> buttons,
      List<List<Point>> paths, Iterable<Shape> bottomButtons, List<List<Point>> bottomPaths) {
    File svg = new File(filename).getAbsoluteFile();
    if (PRINT_DEBUG)
      System.out.println("Writing SVG file to " + svg);

    // taken from
    // http://xmlgraphics.apache.org/batik/using/svg-generator.html

    // Get a DOMImplementation.
    DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();

    // Create an instance of org.w3c.dom.Document.
    String svgNS = "http://www.w3.org/2000/svg";
    Document document = domImpl.createDocument(svgNS, "svg", null);

    // Create an instance of the SVG Generator.
    SVGGraphics2D g = new SVGGraphics2D(document);
    g.setStroke(new BasicStroke(.5f));
    g.setColor(Color.red);

    // draw all of the buttons
    Area sum = new Area();

    if (paths != null) {
      // here, we want to convert the path into an area
      for (List<Point> path : paths) {
        // for (Point p : path) {
        // point(g, p.x, p.y);
        // }
        // Area a = new Area();
        sum.add(pathToArea(path));
        // g.draw(a);
      }
    }
    for (Shape b : buttons) {
      sum.add(new Area(b));
    }
    g.draw(sum);
    g.draw(new Area(new Rectangle(0, 0, SetUp.CANVAS_X+CORRECTION, SetUp.CANVAS_Y+CORRECTION)));
    
    if (bottomButtons != null) {
      sum = new Area();

      if (bottomPaths != null) {
        // here, we want to convert the path into an area
        for (List<Point> path : bottomPaths) {
          // for (Point p : path) {
          // point(g, p.x, p.y);
          // }
          // Area a = new Area();
          sum.add(pathToArea(path));
          // g.draw(a);
        }
      }
      for (Shape b : bottomButtons) {
        sum.add(new Area(b));
      }
      AffineTransform translateToSide = new AffineTransform();
      translateToSide.setToIdentity();
      translateToSide.translate(SetUp.CANVAS_X+CORRECTION*2, 0);
      g.setTransform(translateToSide);
      g.draw(sum);
    }
    
    // be sure to add the outline of the whole thing for sizing.
    g.draw(new Area(new Rectangle(0, 0, SetUp.CANVAS_X+CORRECTION, SetUp.CANVAS_Y+CORRECTION)));

    // Finally, stream out SVG to the standard output using
    // UTF-8 encoding.
    boolean useCSS = true; // we want to use CSS style attributes

    try {
      Writer out = new FileWriter(svg);
      g.stream(out, useCSS);
      out.flush();
      out.close();

      if (PRINT_DEBUG)
        System.out.println("SVG successfully written! Simplifying...");

      // simplifySVG(svg.getName());

      mySetup.repaint();
    } catch (IOException e) {
      e.printStackTrace();
      System.out.println("An error " + e
          + " occured while trying to write the SVG file to disk.");
    }
  }

}
