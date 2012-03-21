package display;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SVGPathwaysGenerator {
  
  private List<List<Point>> paths = new ArrayList();

  //todo: create a path class that's more efficient than storing ALL of the points
  
  public SVGPathwaysGenerator(List<SensorButtonGroup> displayedButtons) {}
  
  public void paint(Graphics2D g) {
	  g.setColor(Color.red);
//	  System.out.println("Painting "+paths.size()+" paths!");
    for (List<Point> path : paths) {
//      connector.paint(g);
    	drawPath(path, g);
    }
  }
  private void drawPath(List<Point> path, Graphics2D g) {
  	for(Point p : path) {
  		g.fillRect(p.x, p.y, 1, 1); //todo: optimize
  	}
  }
  
  private int portDist(ArduinoSensorButton b, Point p1) {
	  Point bp = b.upperLeft;
	  return Math.abs(p1.x - bp.x) + Math.abs(p1.y - bp.y);
  }
  private Point closestPort(final ArduinoSensorButton b, Set<Point> P) {
	  Point min = Collections.min(P, new Comparator<Point>() {
		  public int compare(Point p1, Point p2) {
			  return (new Integer(portDist(b, p1))).compareTo(
					  new Integer(portDist(b, p2)));
		  }
	  });
	  
	  return min;
  }
  
  /**
   * Sort the input buttons.
   * @param in
   * @return
   */
  private List<ArduinoSensorButton> sort(List<ArduinoSensorButton> in) {

//	  Collections.sort(in, new Comparator<ArduinoSensorButton>() {
//
//		@Override
//		public int compare(ArduinoSensorButton arg0, ArduinoSensorButton arg1) {
//			return (new Integer(arg0.upperLeft.x + arg0.upperLeft.y)).compareTo(new Integer(arg1.upperLeft.x + arg1.upperLeft.y));
//		}
//
//	  });
//	  return in;

	  
	  //--new method--
	  //For a given set of buttons S and a set of ports P, find the one button B that has a minimum distance to a port T.
	  //Give that button its choice, and recursively ask.
	  ArduinoSensorButton[] sorted = new ArduinoSensorButton[in.size()];
	  
	  
	  final Set<ArduinoSensorButton> S = new HashSet(); S.addAll(in);
	  final Set<Point> P = new HashSet(); for(int x = 0; x < in.size(); x++) { P.add(new Point(5, x*5)); }
	  
	  while(!S.isEmpty()) {
		  //Find button B that minimizes distance to any port
		  ArduinoSensorButton closest = Collections.min(S, new Comparator<ArduinoSensorButton>() {
	
			@Override
			public int compare(ArduinoSensorButton o1, ArduinoSensorButton o2) {
				// TODO Auto-generated method stub
				int dist1 = portDist(o1, closestPort(o1, P));
				int dist2 = portDist(o2, closestPort(o2, P));
				return (new Integer(dist1)).compareTo(dist2);
			}
			  
		  });
		  //find the port it's closest to
		  Point closestPort = closestPort(closest, P);
		  int index = closestPort.y / 5;
		  sorted[index] = closest;
		  
		  S.remove(closest);
		  P.remove(closestPort);
	  }
	  
	  return Arrays.asList(sorted);
	  
	  //Create all permutations of the input
	  //	for each permutation, calculate the sum of (manhattan distances from each button to its corresponding port)
	  //	find the permutation that minimizes that sum
	  //	choose that
	  
  }
  public void generatePathways(List<SensorButtonGroup> buttonsToConnect) {
//	  System.out.println("Pathways generating: "+buttonsToConnect);
	  /* 
	   * Recreate the connectors each time -
	   * 	Get all of the groups' button's positions, and delegate to appropriate method
	   */
	  paths.clear();
	  
	  
	  List<ArduinoSensorButton> btns = new ArrayList();
	  for(SensorButtonGroup s : buttonsToConnect)
		  btns.addAll(s.triggerButtons);
	  
	  btns = sort(btns);
	  
	  if(btns.size() <= 12)
		  paths.addAll(generateIndividual(btns));
	  else 
		  paths.addAll(generateGrid(btns));
	  
//	  System.out.println("Paths is now "+paths);
  }
  
  //taken from http://stackoverflow.com/questions/8144156/using-pathiterator-to-return-all-line-segments-that-constrain-an-area
  private List<Line2D.Double> toSegments(FlatteningPathIterator pi) {    
	  ArrayList<double[]> areaPoints = new ArrayList<double[]>();
	  ArrayList<Line2D.Double> areaSegments = new ArrayList<Line2D.Double>();
	  double[] coords = new double[6];

	  for ( ; !pi.isDone(); pi.next()) {
	      // The type will be SEG_LINETO, SEG_MOVETO, or SEG_CLOSE
	      // Because the Area is composed of straight lines
	      int type = pi.currentSegment(coords);
	      // We record a double array of {segment type, x coord, y coord}
	      double[] pathIteratorCoords = {type, coords[0], coords[1]};
	      areaPoints.add(pathIteratorCoords);
	  }

	  double[] start = new double[3]; // To record where each polygon starts

	  for (int i = 0; i < areaPoints.size(); i++) {
	      // If we're not on the last point, return a line from this point to the next
	      double[] currentElement = areaPoints.get(i);

	      // We need a default value in case we've reached the end of the ArrayList
	      double[] nextElement = {-1, -1, -1};
	      if (i < areaPoints.size() - 1) {
	          nextElement = areaPoints.get(i + 1);
	      }

	      // Make the lines
	      if (currentElement[0] == PathIterator.SEG_MOVETO) {
	          start = currentElement; // Record where the polygon started to close it later
	      } 

	      if (nextElement[0] == PathIterator.SEG_LINETO) {
	          areaSegments.add(
	                  new Line2D.Double(
	                      currentElement[1], currentElement[2],
	                      nextElement[1], nextElement[2]
	                  )
	              );
	      } else if (nextElement[0] == PathIterator.SEG_CLOSE) {
	          areaSegments.add(
	                  new Line2D.Double(
	                      currentElement[1], currentElement[2],
	                      start[1], start[2]
	                  )
	              );
	      }
	  }

	  return areaSegments;
  }
  private List<Point> outlineFor(ArduinoSensorButton b) {
	  List<Point> outline = new LinkedList<Point>();
	  
	  FlatteningPathIterator p = new FlatteningPathIterator(b.getShape().getPathIterator(null), 1);
	  List<Line2D.Double> segments = toSegments(p);
	  for(Line2D.Double seg : segments) {
		  int x0 = (int)seg.x1, //possibly do rounding later
		      x1 = (int)seg.x2,
		      y0 = (int)seg.y1,
		      y1 = (int)seg.y2;
		  //Rasterization from http://en.wikipedia.org/wiki/Bresenham's_line_algorithm
		  boolean steep = Math.abs(y1 - y0) > Math.abs(x1 - x0);
		  
		  if(steep) {
			  int temp = y0; //swap x0, y0
			  y0 = x0; x0 = temp;
			  
			  temp = y1;  //swap x1, y1
			  y1 = x1; x1 = temp;
		  }
		  if(x0 > x1) {
			  //swap x0, x1
			  int temp = x1;
			  x1 = x0; x0 = temp;
			  
			  //swap y0, y1
			  temp = y1;
			  y1 = y0; y0 = temp;
		  }
		  
		  int dx = x1 - x0;
		  int dy = Math.abs(y1 - y0);
		  float err = 0;
		  float dErr = (float)dy/dx;
		  
		  int yStep;
		  int y = y0;
		  if(y0 < y1) yStep = 1; else yStep = -1;
		  for(int x = x0; x <= x1; x++) {
			  if(steep) outline.add(new Point(y, x));
			  else		outline.add(new Point(x, y));
			  
			  err += dErr;
			  if(err > .5f) {
				  y += yStep;
				  err -= 1;
			  }
		  }
	  }
	  
	  return outline;
  }
  
  private List<List<Point>> generateGrid(List<ArduinoSensorButton> buttons) {
	  throw new UnsupportedOperationException("Not implemented yet!");
  }
  
  private List<List<Point>> generateIndividual(List<ArduinoSensorButton> buttons) {
	  List<List<Point>> paths = new ArrayList();
	  
	  Grid g = new Grid(SetUp.CANVAS_X, SetUp.CANVAS_Y);
	  for(ArduinoSensorButton b : buttons) {
		  for(Point p : outlineFor(b)) {
			  g.falsify(g.adjDiags(p));
		  }
//		  g.falsify(g.removeOutOfBounds(outlineFor(b)));
	  }
	  
	  int idx = 0;
	  for(ArduinoSensorButton b : buttons) { //generate pathways for each button
		  List<Point> nearButton = new LinkedList();
		  for(Point p : outlineFor(b)) {
			  nearButton.addAll(g.adj(p));
		  }
		  
//		  List<Point> nearButton = outlineFor(b);
		  List<Point> path = g.findPath(nearButton, new Point(5, idx*5));
		  
		  if(path != null) { //yay we found one
			  paths.add(path);
			  g.falsifyPath(path);
		  }
		  
		  idx++;
	  }
	  
	  return paths;
  }
  
  private class Grid {
	  private final int width, height;
	  private final boolean[][] arr;
	  
	  Grid(int w, int h) {
		  width = w;
		  height = h;
		  arr = new boolean[width][height];
		  for(int x = 0; x < width; x++) {
			  for(int y = 0; y < height; y++) {
				  arr[x][y] = true;
			  }
		  }
	  }
	  
	  /**
	   * Returns a path connecting End to any one of the starts without crossing over any of the other paths/shapes; may return
	   * null if such a path doesn't exist.
	   * @param starts
	   * @param end
	   * @return
	   */
	  List<Point> findPath(List<Point> starts, Point end) {
		  //step 1: wave expansion to create a mapping between locations and tags
		  Map<Point, Integer> edges = new HashMap();
		  for(Point p : starts) edges.put(p, 0);
		  int i = 0;
		  
		  Map<Point, Integer> flood = new HashMap();
		  
		  while(!flood.containsKey(end) && edges.size() != 0) {
			  flood.putAll(edges);
			  
			  
			  Map<Point, Integer> newEdges = new HashMap();
			  for(Point loc : edges.keySet()) {
				  //loc.adj.filter(x => arr(x) && !flood.contains(x)).map(_ -> i+1)
				  for(Point adj : adj(loc)) {
					  if(arr[adj.x][adj.y] && !flood.containsKey(adj)) {
						  newEdges.put(adj, i+1);
					  }
				  }
			  }
			  i += 1;
			  edges = newEdges;
		  }
		  
		  //Flood is now filled in with the location/tag mapping.
		  
		  
		  //step 2: backtrack from the target point using this algorithm:
		  //	for the current point P, find an adjacent node that has a tag that's lower than P's tag
		  //		if one exists, set that node to P and add it to the list.
		  //		otherwise, you've failed and return null
		  //	go until P's tag is zero, and return the list
		  List<Point> backtrack = new ArrayList();
		  Point p = end;
		  
		  if(!flood.containsKey(p)) {
			  return null;
		  }
		  
		  backtrack.add(p);
		  while(flood.get(p) != 0) {
			  //find an adjacent node whose tag is lower than P's tag
			  boolean found = false;
			  for(Point adj : adj(p)) {
				  if(flood.containsKey(adj) && flood.get(adj) < flood.get(p)) {
					  p = adj;
					  backtrack.add(p);
					  found = true;
					  break;
				  }
			  }
			  if(!found) return null;
		  }
		  return backtrack;
	  }
	  
	  
	  void falsify(Iterable<Point> pts) {
		  for(Point p : pts) {
			  arr[p.x][p.y] = false;
		  }
	  }
	  
	  void falsifyPath(List<Point> path) {
		  for(Point p : path) {
			  falsify(adjDiags(p));
		  }
	  }
	  
	  /**
	   * Return the four adjacent neighbors of Point p.
	   * @param p
	   * @return
	   */
	  private List<Point> adj(Point p) {
		  //add all adjacent points
		  List<Point> list = new LinkedList();
		  list.add(new Point(p.x-1, p.y));
		  list.add(new Point(p.x+1, p.y));
		  list.add(new Point(p.x, p.y-1));
		  list.add(new Point(p.x, p.y+1));
		  
		  removeOutOfBounds(list);
		  return list;
	  }
	  
	  /**
	   * Return the eight adjacent neighbors of Point p.
	   * @param p
	   * @return
	   */
	  private List<Point> adjDiags(Point p) {
		  List<Point> list = new LinkedList();
		  for(int x = p.x-1; x <= p.x+1; x++) {
			  for(int y = p.y-1; y <= p.y+1; y++) {
				  list.add(new Point(x, y));
			  }
		  }
		  list.remove(p);
		  
		  removeOutOfBounds(list);
		  
		  return list;
	  }
	  
	  private List<Point> removeOutOfBounds(List<Point> list) {
		  //remove out of bounds points
		  List<Point> toRemove = new LinkedList();
		  for(Point temp : list) {
			  if(temp.x < 0 || temp.x >= width || temp.y < 0 || temp.y >= height)
				  toRemove.add(temp);
		  }
		  list.removeAll(toRemove);
		  return list;
	  }
  }
}
