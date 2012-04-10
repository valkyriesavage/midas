package display;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.parser.AWTPathProducer;
import org.apache.batik.parser.PathParser;
import org.w3c.dom.Document;

import util.SVGPathLoader;

public class HellaSliderPositioner {
	private Shape seg1, seg2, outer;
	
	private static HellaSliderPositioner base;
	static {
		List<Shape> shapes = SVGPathLoader.loadPaths("slider.svg", "seg1", "seg2", "outer");
		base = new HellaSliderPositioner(shapes.get(0), shapes.get(1), shapes.get(2));
	}

	private HellaSliderPositioner(Shape seg1, Shape seg2, Shape outer) {
		super();
		this.seg1 = seg1;
		this.seg2 = seg2;
		this.outer = outer;
	}
	
	HellaSliderPositioner() {
		this(base.seg1, base.seg2, base.outer);
		transformed(new AffineTransform());
	}
	
	void setDimension(double width, double height) {
		Rectangle2D oldBounds = bounds();
		moveToOrigin();
		transformed(AffineTransform.getScaleInstance(width / bounds().getWidth(), height / bounds().getHeight()));
		transformed(AffineTransform.getTranslateInstance(oldBounds.getX(), oldBounds.getY()));
	}
	
	void transformed(AffineTransform trans) {
		seg1 = trans.createTransformedShape(seg1);
		seg2 = trans.createTransformedShape(seg2);
		outer = trans.createTransformedShape(outer);
	}
	
	Rectangle2D bounds() {
		return seg1.getBounds2D().createUnion(seg2.getBounds2D()).createUnion(outer.getBounds2D());
	}

	//moves the top-left of the slider to the origin.
	void moveToOrigin() {
		Rectangle2D b = bounds();
		transformed(AffineTransform.getTranslateInstance(-b.getX(), -b.getY()));
	}

	
	public Shape getSeg1() {
		return seg1;
	}

	public Shape getSeg2() {
		return seg2;
	}

	public Shape getOuter() {
		return outer;
	}
	
	public List<Shape> getShapes() {
		return Arrays.asList(new Shape[] {seg1, seg2, outer});
	}

}