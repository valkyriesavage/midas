package display;

import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SVGPathwaysGenerator {
  
  private List<SensorConnector> sensorConnectors = new ArrayList<SensorConnector>();
  public int lineWidth = 3;

  public SVGPathwaysGenerator(List<SensorButtonGroup> displayedButtons) {}
  
  public void paint(Graphics2D g) {
    for (SensorConnector connector : sensorConnectors) {
      connector.paint(g);
    }
  }
  
  public void generatePathways(List<SensorButtonGroup> buttonsToConnect) {
	  /* 
	   * Recreate the connectors each time -
	   * 	Get all of the groups' button's positions: 
	   */
	  sensorConnectors.clear();
	  List<ArduinoSensorButton> btns = new ArrayList();
	  for(SensorButtonGroup s : buttonsToConnect)
		  btns.add(s.triggerButton);
	  if(btns.size() <= 12)
		  sensorConnectors.addAll(generateIndividual(btns));
	  else 
		  sensorConnectors.addAll(generateGrid(btns));
  }
  
  private List<SensorConnector> generateGrid(List<ArduinoSensorButton> buttons) {
	  
  }
  
  private List<SensorConnector> generateIndividual(List<ArduinoSensorButton> buttons) {
	  Grid g = new Grid(SetUp.CANVAS_X, SetUp.CANVAS_Y);
	  int idx = 0;
	  for(ArduinoSensorButton b : buttons) {
		  List<Point> nearButton = new LinkedList();
		  nearButton.add(b.upperLeft);
		  List<Point> path = g.findPath(nearButton, new Point(5, idx*5));
		  
		  if(path != null) { //yay we found one
			  
		  }
		  
		  idx++;
	  }
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
		  for(int x = p.x-1; x < p.x+1; x++) {
			  for(int y = p.y-1; y < p.y+1; y++) {
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
