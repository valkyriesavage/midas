package pathway;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import display.ArduinoSensorButton;
import display.SensorButtonGroup;
import display.SetUp;

public class SVGPathwaysGenerator {
	
	public static boolean PRINT_DEBUG = true;
	
	public static final int LINE_EXTENT = 3;
	
	
	
	public static final int LINE_WIDTH = LINE_EXTENT * 2 + 1;
	public static final int INFLUENCE_WIDTH = LINE_WIDTH + LINE_EXTENT;

	private void line(Graphics2D g, int x1, int y1, int x2, int y2) {
		int width = x2 - x1;
		int height = y2 - y1;
		g.drawRect(x1-LINE_EXTENT, y1-LINE_EXTENT, width + LINE_WIDTH, height + LINE_WIDTH);
	}
	
	private static List<Point> cellsOfInfluence(Point p) {
		List<Point> list = new LinkedList();
		for (int x = p.x - INFLUENCE_WIDTH; x <= p.x + INFLUENCE_WIDTH; x++) {
			for (int y = p.y - INFLUENCE_WIDTH; y <= p.y + INFLUENCE_WIDTH; y++) {
				list.add(new Point(x, y));
			}
		}
		list.remove(p);
	
		return list;
	}
	private static Iterable<Point> cellsOfInfluence(ArduinoSensorButton b) {
		return cellsOfInfluence(outlineFor(b));
	}
	private static Iterable<Point> cellsOfInfluence(List<Point> path) {
		Set<Point> flattened = new HashSet();
		for(Point p : path) {
			flattened.addAll(cellsOfInfluence(p));
		}
		return flattened;
	}

	public void generatePathways(List<SensorButtonGroup> buttonsToConnect) {
		//We create the SVG file by simply using SVGGraphics2D, saving to a file, and using the following command:
		//"inkscape non-union.svg --verb=EditSelectAll --verb=SelectionCombine --verb=SelectionUnion --verb=FileSave --verb=FileClose"
		
		
		List<ArduinoSensorButton> allButtons = new ArrayList();
		for (SensorButtonGroup s : buttonsToConnect)
			allButtons.addAll(s.triggerButtons);

		List<Point> allPorts = new ArrayList(allButtons.size());
		for (int x = 0; x < allButtons.size(); x++)
			allPorts.add(new Point(LINE_EXTENT, 2*LINE_WIDTH * x + LINE_EXTENT));

		Collections.sort(allButtons, new Comparator<ArduinoSensorButton>() {

			@Override
			public int compare(ArduinoSensorButton o1, ArduinoSensorButton o2) {
				return (new Integer(o1.upperLeft.y)).compareTo(new Integer(
						o2.upperLeft.y));
			}

		});

		List<List<Point>> allPaths = new ArrayList();

//		if (btns.size() <= 12)
			allPaths.addAll(generateIndividual(allButtons, allPorts));
//		else
//			allPaths.addAll(generateGrid(btns, ports));

		if (PRINT_DEBUG)
			System.out.println("Paths generated! Simplifying paths...");

		writeSVG(new File("outline.svg").getAbsoluteFile(), allButtons, allPaths);
	}

	private List<List<Point>> generateIndividual(List<ArduinoSensorButton> allButtons, List<Point> allPorts) {
		List<List<Point>> paths = new ArrayList();

		Grid g = new Grid(SetUp.CANVAS_X, SetUp.CANVAS_Y);
		//We take each button and set its cells of influence to “restricted to B”, where B is that button.
		for (ArduinoSensorButton b : allButtons) {
			g.restrict(cellsOfInfluence(b), b);
		}

		Iterator<Point> portIterator = allPorts.iterator();
		int i = 0;
		for (ArduinoSensorButton button : allButtons) { // generate pathways for each
												// button
			i++;
			if (PRINT_DEBUG) System.out.println("\tGenerating path " + i + " of " + allButtons.size());
			Point port = portIterator.next();
			
//			List<Point> nearButton = new LinkedList();
//			for (Point p : outlineFor(b)) {
//				nearButton.addAll(g.removeOutOfBounds(adj(p)));
//			}

			List<Point> nearButton = outlineFor(button);
			List<Point> path = g.findPath(nearButton, port, button);

			if (path != null) { // yay we found one
				paths.add(path);
				g.close(cellsOfInfluence(path));
			} else {
				paths.add(null); //placeholder
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


	private void writeSVG(File svg, List<ArduinoSensorButton> buttons, List<List<Point>> paths) {
		if (PRINT_DEBUG) System.out.println("Paths simplified! Writing SVG file to " + svg);

		// taken from
		// http://xmlgraphics.apache.org/batik/using/svg-generator.html

		// Get a DOMImplementation.
		DOMImplementation domImpl = GenericDOMImplementation
				.getDOMImplementation();

		// Create an instance of org.w3c.dom.Document.
		String svgNS = "http://www.w3.org/2000/svg";
		Document document = domImpl.createDocument(svgNS, "svg", null);

		// Create an instance of the SVG Generator.
		SVGGraphics2D g = new SVGGraphics2D(document);

		//draw all of the buttons
		for(ArduinoSensorButton b : buttons) {
			b.paint(g);
		}

		g.setStroke(new BasicStroke(.2f));
		for(List<Point> path : paths) {
			for(Point p : path) {
				line(g, p.x, p.y, p.x, p.y);
			}
		}

		// Finally, stream out SVG to the standard output using
		// UTF-8 encoding.
		boolean useCSS = true; // we want to use CSS style attributes

		try {
			Writer out = new FileWriter(svg);
			g.stream(out, useCSS);
			out.flush();
			out.close();

			if (PRINT_DEBUG) System.out.println("SVG successfully written!");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("An error " + e + " occured while trying to write the SVG file to disk.");
		}
	}
}

//
//
///**
// * Return the eight adjacent neighbors of Point p.
// * 
// * @param p
// * @return
// */
//public static List<Point> adjDiags(Point p) {
//	List<Point> list = new LinkedList();
//	for (int x = p.x - 1; x <= p.x + 1; x++) {
//		for (int y = p.y - 1; y <= p.y + 1; y++) {
//			list.add(new Point(x, y));
//		}
//	}
//	list.remove(p);
//
//	return list;
//}