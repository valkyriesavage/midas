package display;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import util.SVGPathLoader;

public class PadPositioner {
	private List<Shape> shapes;
	
	private static PadPositioner base;
	static {
		List<Shape> shapes = SVGPathLoader.loadPaths("pads.svg", "row1", "row2", "row3", "col1", "col2", "col3");
		base = new PadPositioner(shapes);
	}

	private PadPositioner(List<Shape> shapes) {
		super();
		this.shapes = shapes;
	}
	
	PadPositioner() {
		this(base.shapes);
		transformed(new AffineTransform());
	}
	
	void setDimension(double width, double height) {
		Rectangle2D oldBounds = bounds();
		moveToOrigin();
		transformed(AffineTransform.getScaleInstance(width / bounds().getWidth(), height / bounds().getHeight()));
		transformed(AffineTransform.getTranslateInstance(oldBounds.getX(), oldBounds.getY()));
	}
	
	void transformed(AffineTransform trans) {
		List<Shape> newShapes = new ArrayList();
		for(Shape s : shapes) {
			newShapes.add(trans.createTransformedShape(s));
		}
		shapes = newShapes;
	}
	
	Rectangle2D bounds() {
		Rectangle2D bounds = shapes.get(0).getBounds2D();
		for(Shape s : shapes) bounds = bounds.createUnion(s.getBounds2D());
		return bounds;
	}

	//moves the top-left of the slider to the origin.
	void moveToOrigin() {
		Rectangle2D b = bounds();
		transformed(AffineTransform.getTranslateInstance(-b.getX(), -b.getY()));
	}

	public List<Shape> getShapes() {
		return shapes;
	}

}