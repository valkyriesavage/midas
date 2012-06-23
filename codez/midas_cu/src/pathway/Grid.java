package pathway;

import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import display.SetUp;

class Grid {
  private interface State {
    boolean canPass(Shape b);
  }

  private final State OPEN = new State() {
    @Override
    public boolean canPass(Shape b) {
      return true;
    }
  };
  private final State CLOSED = new State() {
    @Override
    public boolean canPass(Shape b) {
      return false;
    }
  };

  /**
   * belongsTo does reference identity.
   * 
   * @param shape
   * @return
   */
  private State belongsTo(final Shape shape) {
    return new State() {
      @Override
      public boolean canPass(Shape b) {
        return b == shape;
      }
    };
  }

  private final int width, height;
  private final State[][] arr;

  // private Map<Shape, Point> connectionMap;

  Grid() {
    this(SetUp.CANVAS_X, SetUp.CANVAS_Y);
  }

  Grid(int w, int h) {
    width = w;
    height = h;
    arr = new State[width][height];
    // connectionMap = new HashMap();
    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        arr[x][y] = OPEN;
      }
    }
  }

  /**
   * Return the four adjacent neighbors of Point p.
   * 
   * @param p
   * @return
   */
  public static List<Point> adj(Point p) {
    // add all adjacent points
    List<Point> list = new LinkedList<Point>();
    list.add(new Point(p.x - 1, p.y));
    list.add(new Point(p.x + 1, p.y));
    list.add(new Point(p.x, p.y - 1));
    list.add(new Point(p.x, p.y + 1));

    return list;
  }


  public List<Point> findPath(Shape button, Point port) throws PathwayGenerationException {
    Set<Point> set = new HashSet();
    set.add(port);
    return findPath(button, set);
  }
  
  /**
   * Returns a path connecting End to any one of the starts without crossing
   * over any of the other paths/shapes; throws an exception if such a path
   * doesn't exist.
   * 
   * @param starts
   * @param end
   * @return
   */
  public List<Point> findPath(Shape shape, Set<Point> ends)
      throws PathwayGenerationException {
    Set<Point> starts = SVGPathwaysGenerator.immediateOutlineFor(shape);
    if(starts.isEmpty()) throw new IllegalArgumentException("Shape is empty!");
    if(ends.isEmpty()) throw new IllegalArgumentException("Ends are empty!");
    // step 1: wave expansion to create a mapping between locations and tags
    Map<Point, Integer> edges = new HashMap<Point, Integer>();
    for (Point p : starts)
      edges.put(p, 0);
    int i = 0;

    Map<Point, Integer> flood = new HashMap<Point, Integer>();
//    flood.putAll(edges);

    while (isExclusive(flood.keySet(), ends)) {
      flood.putAll(edges);
      Map<Point, Integer> newEdges = new HashMap<Point, Integer>();
      for (Point loc : edges.keySet()) {
        // loc.adj.filter(x => arr(x) && !flood.contains(x)).map(_ ->
        // i+1)
        for (Point adj : adj(loc)) {
          if (bounded(adj) && arr[adj.x][adj.y].canPass(shape)
              && !flood.containsKey(adj)) {
            newEdges.put(adj, i + 1);
          }
        }
      }
      i += 1;
      edges = newEdges;
      if(edges.isEmpty()) {
        if (SVGPathwaysGenerator.PRINT_DEBUG)
          System.out.println("\t\t:( Floodfill failed!");
        throw new PathwayGenerationException();
      }
    }

    // Flood is now filled in with the location/tag mapping.

    // step 2: backtrack from the target point using this algorithm:
    // for the current point P, find an adjacent node that has a tag that's
    // lower than P's tag
    // if one exists, set that node to P and add it to the list.
    // otherwise, you've failed and return null
    // go until P's tag is zero, and return the list
    List<Point> backtrack = new ArrayList<Point>();
    
    // flood.keySet() and ends are MUTUALLY EXCLUSIVE?
    Point p = findOneElementInIntersecting(flood.keySet(), ends);

    backtrack.add(p);
    while (flood.get(p) != 0) {
      // find an adjacent node whose tag is lower than P's tag
      boolean found = false;
      for (Point adj : adj(p)) {
        if (bounded(adj) && flood.containsKey(adj)
            && flood.get(adj) < flood.get(p)) {
          p = adj;
          backtrack.add(p);
          found = true;
          break;
        }
      }
      if (!found) {
        //This means that p didn't have any that were adjacent to it and also less than.
        Set<Point> intersection = new HashSet(flood.keySet());
        intersection.retainAll(ends);
        if (SVGPathwaysGenerator.PRINT_DEBUG)
          System.out.println("\t\t:(Backtracking got stuck at " + p);
        throw new PathwayGenerationException(":(Backtracking got stuck at " + p);
      }
    }
    // connectionMap.put(shape, end);
    return backtrack;
  }
  
  private boolean isExclusive(Set<Point> p1, Set<Point> p2) {
    boolean exclusive = true;
    for(Point p : p2) {
      if(p1.contains(p)) {
        exclusive = false;
        break;
      }
    }
    return exclusive;
  }

  /**
   * Finds one common point between the two sets.
   * @param keySet
   * @param ends
   * @return
   */
  private Point findOneElementInIntersecting(Set<Point> s, Set<Point> ends) {
    Set<Point> keySet = new HashSet<Point>(s);
    keySet.retainAll(ends);
    return keySet.iterator().next();
  }

  public void close(Iterable<Point> pts) {
    for (Point p : pts) {
      if (bounded(p))
        arr[p.x][p.y] = CLOSED;
    }
  }

  public void restrict(Iterable<Point> pts, final Shape owner) {
    State belongs = new State() {

      @Override
      public boolean canPass(Shape s) {
        return s == owner;
      }

    };
    for (Point p : pts) {
      if (bounded(p)) {
        if (arr[p.x][p.y] == OPEN)
          arr[p.x][p.y] = belongs;
        else
          arr[p.x][p.y] = CLOSED;
      }
    }
  }

  public void obstacle(Iterable<Point> pts) {
    for (Point p : pts) {
      if (p.x >= 0 && p.y >= 0)
        arr[p.x][p.y] = CLOSED;
    }
  }

  public void restrictAll(Iterable<Shape> shapes) {
    for (Shape s : shapes) {
      restrict(SVGPathwaysGenerator.cellsOfInfluence(s), s);
    }
  }

  public void restrictExactly(Iterable<Shape> shapes) {
    for (Shape s : shapes) {  
      obstacle(SVGPathwaysGenerator.immediateOutlineFor(s));
    }
  }

  public boolean bounded(Point temp) {
    return !(temp.x < 0 || temp.x >= width || temp.y < 0 || temp.y >= height);
  }
}