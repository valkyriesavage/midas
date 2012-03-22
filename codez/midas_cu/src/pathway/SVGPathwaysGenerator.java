package pathway;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.dom.GenericDOMImplementation;

import org.w3c.dom.Document;
import org.w3c.dom.DOMImplementation;

import display.ArduinoSensorButton;
import display.SensorButtonGroup;
import display.SetUp;

public class SVGPathwaysGenerator {

	public static boolean PRINT_DEBUG = true;

	private List<List<Line>> outlines = new ArrayList();

	public SVGPathwaysGenerator() {
	}

	public void paint(Graphics2D g) {
		for (List<Line> list : outlines) {
			g.setColor(new Color((float) Math.random(), (float) Math.random(),
					(float) Math.random()));
			for (Line l : list) {
				g.drawLine(l.start.x, l.start.y, l.end.x, l.end.y);
			}
		}
	}

	private void drawPath(List<Point> path, Graphics2D g) {
		for (Point p : path) {
			g.fillRect(p.x, p.y, 1, 1);
		}
	}

	public void generatePathways(List<SensorButtonGroup> buttonsToConnect) {
		/*
		 * Recreate the connectors each time - Get all of the groups' button's
		 * positions, and delegate to appropriate method
		 */
		outlines.clear();

		List<ArduinoSensorButton> btns = new ArrayList();
		for (SensorButtonGroup s : buttonsToConnect)
			btns.addAll(s.triggerButtons);

		List<Point> ports = new ArrayList(btns.size());
		for (int x = 0; x < btns.size(); x++)
			ports.add(new Point(1, 5 * x));

		Collections.sort(btns, new Comparator<ArduinoSensorButton>() {

			@Override
			public int compare(ArduinoSensorButton o1, ArduinoSensorButton o2) {
				return (new Integer(o1.upperLeft.y)).compareTo(new Integer(
						o2.upperLeft.y));
			}

		});

		List<List<Point>> allPaths = new ArrayList();

		if (btns.size() <= 12)
			allPaths.addAll(generateIndividual(btns, ports));
		else
			allPaths.addAll(generateGrid(btns, ports));

		if (PRINT_DEBUG)
			System.out.println("Paths generated! Simplifying paths...");

		for (ArduinoSensorButton b : btns) {
			allPaths.add(outlineFor(b));
		}

		outlines.addAll((new Simplifier()).simplify(allPaths, ports));

		writeSVG(new File("outline.svg").getAbsoluteFile());
	}

	private List<List<Point>> generateGrid(List<ArduinoSensorButton> buttons,
			List<Point> ports) {
		throw new UnsupportedOperationException("Not implemented yet!");
	}

	private List<List<Point>> generateIndividual(List<ArduinoSensorButton> buttons, List<Point> ports) {
		List<List<Point>> paths = new ArrayList();

		Grid g = new Grid(SetUp.CANVAS_X, SetUp.CANVAS_Y);
		for (ArduinoSensorButton b : buttons) {
			for (Point p : outlineFor(b)) {
				g.falsify(g.removeOutOfBounds(adjDiags(p)));
			}
		}

		Iterator<Point> portIterator = ports.iterator();
		int i = 0;
		for (ArduinoSensorButton b : buttons) { // generate pathways for each
												// button
			i++;
			if (PRINT_DEBUG)
				System.out.println("\tGenerating path " + i + " of "
						+ buttons.size());
			Point port = portIterator.next();
			List<Point> nearButton = new LinkedList();
			for (Point p : outlineFor(b)) {
				nearButton.addAll(g.removeOutOfBounds(adj(p)));
			}

			// List<Point> nearButton = outlineFor(b);
			List<Point> path = g.findPath(nearButton, port);

			if (path != null) { // yay we found one
				paths.add(path);
				g.falsifyPath(path);
			}
		}
		assert (!portIterator.hasNext());

		return paths;
	}

	// taken from
	// http://stackoverflow.com/questions/8144156/using-pathiterator-to-return-all-line-segments-that-constrain-an-area
	private static List<Line2D.Double> toSegments(FlatteningPathIterator pi) {
		ArrayList<double[]> areaPoints = new ArrayList<double[]>();
		ArrayList<Line2D.Double> areaSegments = new ArrayList<Line2D.Double>();
		double[] coords = new double[6];

		for (; !pi.isDone(); pi.next()) {
			// The type will be SEG_LINETO, SEG_MOVETO, or SEG_CLOSE
			// Because the Area is composed of straight lines
			int type = pi.currentSegment(coords);
			// We record a double array of {segment type, x coord, y coord}
			double[] pathIteratorCoords = { type, coords[0], coords[1] };
			areaPoints.add(pathIteratorCoords);
		}

		double[] start = new double[3]; // To record where each polygon starts

		for (int i = 0; i < areaPoints.size(); i++) {
			// If we're not on the last point, return a line from this point to
			// the next
			double[] currentElement = areaPoints.get(i);

			// We need a default value in case we've reached the end of the
			// ArrayList
			double[] nextElement = { -1, -1, -1 };
			if (i < areaPoints.size() - 1) {
				nextElement = areaPoints.get(i + 1);
			}

			// Make the lines
			if (currentElement[0] == PathIterator.SEG_MOVETO) {
				start = currentElement; // Record where the polygon started to
										// close it later
			}

			if (nextElement[0] == PathIterator.SEG_LINETO) {
				areaSegments.add(new Line2D.Double(currentElement[1],
						currentElement[2], nextElement[1], nextElement[2]));
			} else if (nextElement[0] == PathIterator.SEG_CLOSE) {
				areaSegments.add(new Line2D.Double(currentElement[1],
						currentElement[2], start[1], start[2]));
			}
		}

		return areaSegments;
	}

	public static List<Point> outlineFor(ArduinoSensorButton b) {
		List<Point> outline = new LinkedList<Point>();

		FlatteningPathIterator p = new FlatteningPathIterator(b.getShape()
				.getPathIterator(null), 1);
		List<Line2D.Double> segments = toSegments(p);
		for (Line2D.Double seg : segments) {
			int x0 = (int) seg.x1, // possibly do rounding later
			x1 = (int) seg.x2, y0 = (int) seg.y1, y1 = (int) seg.y2;
			// Rasterization from
			// http://en.wikipedia.org/wiki/Bresenham's_line_algorithm
			boolean steep = Math.abs(y1 - y0) > Math.abs(x1 - x0);

			if (steep) {
				int temp = y0; // swap x0, y0
				y0 = x0;
				x0 = temp;

				temp = y1; // swap x1, y1
				y1 = x1;
				x1 = temp;
			}
			if (x0 > x1) {
				// swap x0, x1
				int temp = x1;
				x1 = x0;
				x0 = temp;

				// swap y0, y1
				temp = y1;
				y1 = y0;
				y0 = temp;
			}

			int dx = x1 - x0;
			int dy = Math.abs(y1 - y0);
			float err = 0;
			float dErr = (float) dy / dx;

			int yStep;
			int y = y0;
			if (y0 < y1)
				yStep = 1;
			else
				yStep = -1;
			for (int x = x0; x <= x1; x++) {
				if (steep)
					outline.add(new Point(y, x));
				else
					outline.add(new Point(x, y));

				err += dErr;
				if (err > .5f) {
					y += yStep;
					err -= 1;
				}
			}
		}

		return outline;
	}

	/**
	 * Return the four adjacent neighbors of Point p.
	 * 
	 * @param p
	 * @return
	 */
	public static List<Point> adj(Point p) {
		// add all adjacent points
		List<Point> list = new LinkedList();
		list.add(new Point(p.x - 1, p.y));
		list.add(new Point(p.x + 1, p.y));
		list.add(new Point(p.x, p.y - 1));
		list.add(new Point(p.x, p.y + 1));

		return list;
	}

	/**
	 * Return the eight adjacent neighbors of Point p.
	 * 
	 * @param p
	 * @return
	 */
	public static List<Point> adjDiags(Point p) {
		List<Point> list = new LinkedList();
		for (int x = p.x - 1; x <= p.x + 1; x++) {
			for (int y = p.y - 1; y <= p.y + 1; y++) {
				list.add(new Point(x, y));
			}
		}
		list.remove(p);

		return list;
	}

	private void writeSVG(File svg) {
		if (PRINT_DEBUG)
			System.out.println("Paths simplified! Writing SVG file to " + svg);

		// taken from
		// http://xmlgraphics.apache.org/batik/using/svg-generator.html

		// Get a DOMImplementation.
		DOMImplementation domImpl = GenericDOMImplementation
				.getDOMImplementation();

		// Create an instance of org.w3c.dom.Document.
		String svgNS = "http://www.w3.org/2000/svg";
		Document document = domImpl.createDocument(svgNS, "svg", null);

		// Create an instance of the SVG Generator.
		SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
		svgGenerator.setStroke(new BasicStroke(.2f));

		// Ask the test to render into the SVG Graphics2D implementation.
		paint(svgGenerator);

		// Finally, stream out SVG to the standard output using
		// UTF-8 encoding.
		boolean useCSS = true; // we want to use CSS style attributes

		try {
			Writer out = new FileWriter(svg);
			svgGenerator.stream(out, useCSS);
			out.flush();
			out.close();

			if (PRINT_DEBUG)
				System.out.println("SVG successfully written!");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("An error " + e
					+ " occured while trying to write the SVG file to disk.");
		}
	}
}