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
import java.util.Iterator;
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
  		g.fillRect(p.x, p.y, 1, 1);
  	}
  }
  
  public void generatePathways(List<SensorButtonGroup> buttonsToConnect) {
	  /* 
	   * Recreate the connectors each time -
	   * 	Get all of the groups' button's positions, and delegate to appropriate method
	   */
	  paths.clear();
	  
	  
	  List<ArduinoSensorButton> btns = new ArrayList();
	  for(SensorButtonGroup s : buttonsToConnect)
		  btns.addAll(s.triggerButtons);
	  
	  List<Point> ports = new ArrayList(btns.size()); for(int x = 0; x < btns.size(); x++) ports.add(new Point(1, 5*x));

//	  (new GreedyMinSorter()).sort(buttons, ports);
	  (new YSorter()).sort(btns, ports);
	  
	  if(btns.size() <= 12)
		  paths.addAll(generateIndividual(btns, ports));
	  else 
		  paths.addAll(generateGrid(btns, ports));
  }
  
  private List<List<Point>> generateGrid(List<ArduinoSensorButton> buttons, List<Point> ports) {
	  throw new UnsupportedOperationException("Not implemented yet!");
  }
  
  private List<List<Point>> generateIndividual(List<ArduinoSensorButton> buttons, List<Point> ports) {
	  
	  
	  List<List<Point>> paths = new ArrayList();
	  
	  Grid g = new Grid(SetUp.CANVAS_X, SetUp.CANVAS_Y);
	  for(ArduinoSensorButton b : buttons) {
		  for(Point p : outlineFor(b)) {
			  g.falsify(g.removeOutOfBounds(adjDiags(p)));
		  }
//		  g.falsify(g.removeOutOfBounds(outlineFor(b)));
	  }

	  Iterator<Point> portIterator = ports.iterator();
	  for(ArduinoSensorButton b : buttons) { //generate pathways for each button
		  Point port = portIterator.next();
		  List<Point> nearButton = new LinkedList();
		  for(Point p : outlineFor(b)) {
			  nearButton.addAll(g.removeOutOfBounds(adj(p)));
		  }
		  
//		  List<Point> nearButton = outlineFor(b);
		  List<Point> path = g.findPath(nearButton, port);
		  
		  if(path != null) { //yay we found one
			  paths.add(path);
			  g.falsifyPath(path);
		  }
	  }
	  assert(!portIterator.hasNext());
	  
	  return paths;
  }
  
  //taken from http://stackoverflow.com/questions/8144156/using-pathiterator-to-return-all-line-segments-that-constrain-an-area
  private static List<Line2D.Double> toSegments(FlatteningPathIterator pi) {    
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
  public static List<Point> outlineFor(ArduinoSensorButton b) {
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

  /**
   * Return the four adjacent neighbors of Point p.
   * @param p
   * @return
   */
  public static List<Point> adj(Point p) {
	  //add all adjacent points
	  List<Point> list = new LinkedList();
	  list.add(new Point(p.x-1, p.y));
	  list.add(new Point(p.x+1, p.y));
	  list.add(new Point(p.x, p.y-1));
	  list.add(new Point(p.x, p.y+1));
	  
	  return list;
  }
  
  /**
   * Return the eight adjacent neighbors of Point p.
   * @param p
   * @return
   */
  public static List<Point> adjDiags(Point p) {
	  List<Point> list = new LinkedList();
	  for(int x = p.x-1; x <= p.x+1; x++) {
		  for(int y = p.y-1; y <= p.y+1; y++) {
			  list.add(new Point(x, y));
		  }
	  }
	  list.remove(p);
	  
	  return list;
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
				  for(Point adj : removeOutOfBounds(adj(loc))) {
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
			  for(Point adj : removeOutOfBounds(adj(p))) {
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
			  falsify(removeOutOfBounds(adjDiags(p)));
		  }
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


interface Sorter {
	void sort(List<ArduinoSensorButton> in, List<Point> ports);
}

class NoSorter implements Sorter {
	@Override
	public void sort(List<ArduinoSensorButton> in, List<Point> ports) {
		//do nothing
	}
}


class YSorter implements Sorter {
	@Override
	public void sort(List<ArduinoSensorButton> in, List<Point> ports) {
		Collections.sort(in, new Comparator<ArduinoSensorButton>() {

			@Override
			public int compare(ArduinoSensorButton o1, ArduinoSensorButton o2) {
				return (new Integer(o1.upperLeft.y)).compareTo(new Integer(o2.upperLeft.y));
			}
			
		});
	}
}

class GreedyMinSorter implements Sorter {

	protected int dist(Point p1, Point bp) {
		return Math.abs(p1.x - bp.x) + Math.abs(p1.y - bp.y);
	}

	protected int portDist(ArduinoSensorButton b, Point p1) {
		return dist(p1, b.upperLeft);
	}

	protected Point closestPort(final ArduinoSensorButton b, Set<Point> P) {
		Point min = Collections.min(P, new Comparator<Point>() {
			public int compare(Point p1, Point p2) {
				return (new Integer(portDist(b, p1))).compareTo(new Integer(
						portDist(b, p2)));
			}
		});

		return min;
	}

	/**
	 * Sort the input buttons.
	 * 
	 * @param in
	 * @return
	 */
	public void sort(List<ArduinoSensorButton> in, List<Point> ports) {
		// For a given set of buttons S and a set of ports P, find the one
		// button B that has a minimum distance to a port T.
		// Give that button its choice, and recursively ask.
		List<ArduinoSensorButton> sortedButtons = new ArrayList();
		List<Point> sortedPorts = new ArrayList();

		//S is the set of buttons
		final Set<ArduinoSensorButton> S = new HashSet(); S.addAll(in);
		
		//P is the set of ports
		final Set<Point> P = new HashSet(); P.addAll(ports);

		while (!S.isEmpty()) {
			// Find the button that minimizes distance to any port
			ArduinoSensorButton closest = Collections.min(S,
					new Comparator<ArduinoSensorButton>() {

						@Override
						public int compare(ArduinoSensorButton o1,
								ArduinoSensorButton o2) {
							// TODO Auto-generated method stub
							int dist1 = portDist(o1, closestPort(o1, P));
							int dist2 = portDist(o2, closestPort(o2, P));
							return (new Integer(dist1)).compareTo(dist2);
						}

					});
			// find the port it's closest to
			Point closestPort = closestPort(closest, P);
			
			
//			int index = closestPort.y / 5;
//			sortedButtons[index] = closest;
//			sortedPorts[index] = closestPort;
			
			sortedButtons.add(closest);
			sortedPorts.add(closestPort);

			S.remove(closest);
			P.remove(closestPort);
		}
		
		in.clear(); ports.clear();
		in.addAll(sortedButtons);
		ports.addAll(sortedPorts);
//		in.addAll(Arrays.asList(sortedButtons));
//		ports.addAll(Arrays.asList(sortedPorts));
	}
}

class OutlinedGreedyMinSorter extends GreedyMinSorter {

	@Override
	protected int portDist(ArduinoSensorButton b, final Point p1) {
		//outline the button, find the closest distance from anywhere on the outline to the given point
		List<Point> outline = SVGPathwaysGenerator.outlineFor(b);
		Point min = Collections.min(outline, new Comparator<Point>() {

			@Override
			public int compare(Point arg0, Point arg1) {
				return (new Integer(dist(arg0, p1))).compareTo(dist(arg1, p1));
			}
			
		});
		return dist(min, p1);
	}
	
}