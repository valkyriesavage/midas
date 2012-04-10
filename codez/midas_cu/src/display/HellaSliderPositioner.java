package display;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.File;

import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.parser.AWTPathProducer;
import org.apache.batik.parser.PathParser;
import org.w3c.dom.Document;

public class HellaSliderPositioner {
	private Shape seg1, seg2, outer;
	
	private static HellaSliderPositioner base;
	static {
		try {
			UserAgentAdapter ua = new UserAgentAdapter();
			DocumentLoader loader = new DocumentLoader(ua);
			String svgURI;
			svgURI = new File("slider.svg").toURL().toString();
			Document doc = loader.loadDocument(svgURI);

			PathParser pp = new PathParser();
			AWTPathProducer producer = new AWTPathProducer();
			pp.setPathHandler(producer);
			pp.parse(doc.getElementById("seg1").getAttribute("d"));
			Shape seg1 = producer.getShape();
			pp.parse(doc.getElementById("seg2").getAttribute("d"));
			Shape seg2 = producer.getShape();
			pp.parse(doc.getElementById("outer").getAttribute("d"));
			Shape outer = producer.getShape();
			base = new HellaSliderPositioner(seg1, seg2, outer);
		} catch (Exception e) {
			e.printStackTrace();
		}
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

}