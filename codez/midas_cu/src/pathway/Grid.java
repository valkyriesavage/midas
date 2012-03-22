package pathway;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

class Grid {
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
				  for(Point adj : removeOutOfBounds(SVGPathwaysGenerator.adj(loc))) {
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
			  for(Point adj : removeOutOfBounds(SVGPathwaysGenerator.adj(p))) {
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
			  falsify(removeOutOfBounds(SVGPathwaysGenerator.adjDiags(p)));
		  }
	  }
	  
	  List<Point> removeOutOfBounds(List<Point> list) {
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