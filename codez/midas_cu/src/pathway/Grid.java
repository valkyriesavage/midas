package pathway;

import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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

  /**
   * Returns a path connecting End to any one of the starts without crossing
   * over any of the other paths/shapes; throws an exception if such a path
   * doesn't exist.
   * 
   * @param starts
   * @param end
   * @return
   */
  public List<Point> findPath(List<Point> starts, Point end, Shape shape)
      throws PathwayGenerationException {
    // step 1: wave expansion to create a mapping between locations and tags
    Map<Point, Integer> edges = new HashMap<Point, Integer>();
    for (Point p : starts)
      edges.put(p, 0);
    int i = 0;

    Map<Point, Integer> flood = new HashMap<Point, Integer>();

    while (!flood.containsKey(end) && edges.size() != 0) {
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
    }

    // Flood is now filled in with the location/tag mapping.

    // step 2: backtrack from the target point using this algorithm:
    // for the current point P, find an adjacent node that has a tag that's
    // lower than P's tag
    // if one exists, set that node to P and add it to the list.
    // otherwise, you've failed and return null
    // go until P's tag is zero, and return the list
    List<Point> backtrack = new ArrayList<Point>();
    Point p = end;

    if (!flood.containsKey(p)) {
      if (SVGPathwaysGenerator.PRINT_DEBUG)
        System.out.println("\t\t:( Floodfill couldn't get to " + p);
      throw new PathwayGenerationException();
    }

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
        if (SVGPathwaysGenerator.PRINT_DEBUG)
          System.out.println("\t\t:(Backtracking got stuck at " + p);
        throw new PathwayGenerationException();
      }
    }
    // connectionMap.put(shape, end);
    return backtrack;
  }

  // public Map<Shape, Point> getConnections() {
  // return new HashMap<Shape, Point>(connectionMap);
  // }

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
      obstacle(SVGPathwaysGenerator.outlineFor(s));
    }
  }

  public boolean bounded(Point temp) {
    return !(temp.x < 0 || temp.x >= width || temp.y < 0 || temp.y >= height);
  }
}