package pathway;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.dom.svg.SVGOMDocument;
import org.apache.batik.parser.AWTPathProducer;
import org.apache.batik.parser.PathParser;
import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGPath;

/**
 * A collection of static utility methods to help cut shapes into their diagonals.
 * @author hellochar
 *
 */
class GenerateGridUtils {
  public static Area toCutout(Area a, double thickness) {
	  Area p = new Area(a);
	  Rectangle2D bounds = p.getBounds2D();
	  double length = Math.max(bounds.getWidth(), bounds.getHeight()) * 2;
	  Rectangle2D rectInit = new Rectangle2D.Double(-length/2, -thickness/2, length, thickness);
	  AffineTransform transform = AffineTransform.getTranslateInstance(bounds.getX() + bounds.getWidth()/2, bounds.getY() + bounds.getHeight()/2);
	  transform.rotate(-Math.PI/4);
	  Area negative = new Area(transform.createTransformedShape(rectInit));
	  p.subtract(negative);
	  return p;
  }
  
  /**
   * Splits the Shape parameter into its non-overlapping, non-intersecting subshapes.
   * @param s
   * @return
   */
  public static List<Shape> splitIntoSubshapes(Shape s) {
	  SVGOMDocument doc = (SVGOMDocument) SVGDOMImplementation.getDOMImplementation().createDocument("http://www.w3.org/2000/svg", "svg", null);

	  String pathString = SVGPath.toSVGPathData(s, SVGGeneratorContext.createDefault(doc));
	  String[] paths = pathString.split("M");
	  
	  PathParser pp = new PathParser();
	  AWTPathProducer producer = new AWTPathProducer();
	  pp.setPathHandler(producer);
	  
	  List<Shape> list = new ArrayList<Shape>();
	  for(int i = 1; i < paths.length; i++) { //Start at index 1 because paths[0] == "" since pathString starts with an M)
		  pp.parse("M" + paths[i]);
		  Shape shape = producer.getShape();
		  list.add(shape);
	  }
	  return list;
  }

  public static double SPLIT_THICKNESS = 5;
  public static double BOX_SIDE = SPLIT_THICKNESS;
  
  public static Pair<Area, Area> splitDiagonally(Shape s) {
	  List<Shape> subshapes = splitIntoSubshapes(toCutout(new Area(s), SPLIT_THICKNESS));
	  Area topLeft = new Area();
	  Area bottomRight = new Area();
	  
	  Rectangle2D box = s.getBounds2D();
	  

	  //the line cutting the shape can be expressed as the equation y = B - x, where B = x1 + y1, where x1 and y1 are the center point of the uncut shape. Shapes on the top-left will have a y < B - x; bottom-right will be y > B - x.
	  double x1 = box.getCenterX(),
	         y1 = box.getCenterY();
	  double B = x1 + y1;
	  
	  for(Shape subshape : subshapes) {
		  
		  if(subshape.getBounds2D().getCenterY() < B - subshape.getBounds2D().getCenterX()) { //top-left
			  topLeft.add(new Area(subshape));
		  } else {
			  bottomRight.add(new Area(subshape));
		  }
	  }
	  
	  Area bottomLeftBox = new Area(new Rectangle2D.Double(
	      box.getX(), box.getY() + box.getHeight() - BOX_SIDE,
	      BOX_SIDE, BOX_SIDE));
	  Area topRightBox =   new Area(new Rectangle2D.Double(
	      box.getX() + box.getWidth() - BOX_SIDE, box.getY(),
	      BOX_SIDE, BOX_SIDE));
	  
	  topLeft.subtract(bottomLeftBox);
	  topLeft.subtract(topRightBox);
	  
	  bottomRight.subtract(bottomLeftBox);
	  bottomRight.subtract(topRightBox);
	  
	  return new Pair<Area, Area>(topLeft, bottomRight);
  }
  
  /**
   * topLefts are in ._1, bottomRights are ._2
   * @param shapes
   * @return
   */
  public static Pair<List<Area>, List<Area>> splitDiagonally(List<Shape> shapes) {
	  List<Area> topLefts = new ArrayList<Area>(shapes.size()),
			     bottomRights = new ArrayList<Area>(shapes.size());
	  for(Shape s : shapes) {
		  Pair<Area, Area> pair = splitDiagonally(s);
		  topLefts.add(pair._1);
		  bottomRights.add(pair._2);
	  }
	  return new Pair<List<Area>, List<Area>>(topLefts, bottomRights);
  }
}
