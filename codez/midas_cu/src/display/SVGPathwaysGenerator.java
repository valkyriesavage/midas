package display;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SVGPathwaysGenerator {
  
  private List<List<Point>> paths = new ArrayList();
  public static final int SPACING_WIDTH = 3;

  //todo: create a path class that's more efficient than storing ALL of the points
  
  public SVGPathwaysGenerator(List<SensorButtonGroup> displayedButtons) {}
  
  public void paint(Graphics2D g) {
	  g.setColor(Color.red);
	  System.out.println("Painting "+paths.size()+" paths!");
    for (List<Point> path : paths) {
    	for(Point p : path) {
    		g.drawRect(p.x, p.y, 1, 1); //todo: optimize
    	}
//      connector.paint(g);
    }
    g.setPaintMode();
  }
  
  public void generatePathways(List<SensorButtonGroup> buttonsToConnect) {
	  System.out.println("Pathways generating: "+buttonsToConnect);
	  /* 
	   * Recreate the connectors each time -
	   * 	Get all of the groups' button's positions, and delegate to appropriate method
	   */
	  paths.clear();
	  
	  
	  List<ArduinoSensorButton> btns = new ArrayList();
	  for(SensorButtonGroup s : buttonsToConnect)
		  btns.addAll(s.triggerButtons);
	  
	  if(btns.size() <= 12)
		  paths.addAll(generateIndividual(btns));
	  else 
		  paths.addAll(generateGrid(btns));
	  
	  System.out.println("Paths is now "+paths);
  }
  
  private List<Point> outlineFor(ArduinoSensorButton b) {
	  Rectangle r = b.getShape().getBounds();
	  List<Point> outline = new LinkedList<Point>();
	  
	  int x = r.x;
	  int y = r.y;
	  
	  for( ; x < r.x + r.width; x++) { //from top-left corner to top-right
		  outline.add(new Point(x, y));
	  }
	  
	  for( ; y < r.y + r.height; y++) { //from top-right corner to bottom-right
		  outline.add(new Point(x, y));
	  }

	  for(; x > r.x ; x--) { //from top-left corner to top-right
		  outline.add(new Point(x, y));
	  }

	  for(; y > r.y ; y--) { //from top-left corner to top-right
		  outline.add(new Point(x, y));
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
//		  for(Point p : outlineFor(b)) {
//			  g.falsify(g.adjDiags(p));
//		  }
		  g.falsify(outlineFor(b));
	  }
	  
	  int idx = 0;
	  for(ArduinoSensorButton b : buttons) { //generate pathways for each button
//		  List<Point> nearButton = new LinkedList();
//		  for(Point p : outlineFor(b)) {
//			  nearButton.addAll(g.adjDiags(p));
//		  }
		  
		  List<Point> nearButton = outlineFor(b);
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
		  for(int x = p.x-SPACING_WIDTH; x <= p.x+SPACING_WIDTH; x++) {
			  for(int y = p.y-SPACING_WIDTH; y <= p.y+SPACING_WIDTH; y++) {
				  list.add(new Point(x, y));
			  }
		  }
		  list.remove(p);
		  
		  removeOutOfBounds(list);
		  
		  return list;
	  }
	  
	  private void removeOutOfBounds(List<Point> list) {
		  //remove out of bounds points
		  List<Point> toRemove = new LinkedList();
		  for(Point temp : list) {
			  if(temp.x < 0 || temp.x >= width || temp.y < 0 || temp.y >= height)
				  toRemove.add(temp);
		  }
		  list.removeAll(toRemove);
	  }
  }
}
