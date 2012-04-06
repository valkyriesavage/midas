package pathway;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import display.ArduinoSensorButton;
import display.HellaSliderPositioner;
import display.SensorButtonGroup;
import display.SetUp;

public class SVGPathwaysGenerator {

	SetUp mySetup;
	private List<List<Point>> allPaths = new ArrayList<List<Point>>();

	public SVGPathwaysGenerator(SetUp s) {
		mySetup = s;
	}

	public static boolean PRINT_DEBUG = true;

	public static final int LINE_EXTENT = 3;

	public static final int LINE_WIDTH = LINE_EXTENT * 2 + 1;
	public static final int BUTTON_INFLUENCE_WIDTH = LINE_WIDTH; // should be
																	// LINE_WIDTH
																	// +
																	// LINE_EXTENT
	public static final int PATH_INFLUENCE_WIDTH = 2 * LINE_WIDTH; // should be
																	// 2*LINE_WIDTH

	static List<Point> cellsOfInfluence(Point p, int extent) {
		List<Point> list = new LinkedList<Point>();
		for (int x = p.x - extent; x <= p.x + extent; x++) {
			for (int y = p.y - extent; y <= p.y + extent; y++) {
				list.add(new Point(x, y));
			}
		}
		list.remove(p);

		return list;
	}

	/**
	 * Returns all points that are influenced by the given Shape. A point is influenced by a shape if it's coordinates are both within
	 * <code>BUTTON_INFLUENCE_WIDTH<code> of the Shape's outline.
	 * @param s
	 * @return
	 */
	static Iterable<Point> cellsOfInfluence(Shape s) {
		Set<Point> flattened = new HashSet<Point>();
		for (Point p : outlineFor(s)) {
			flattened.addAll(cellsOfInfluence(p, BUTTON_INFLUENCE_WIDTH));
		}
		return flattened;
	}

	/**
	 * Returns all points that are influenced by the given path. A point is influenced by a path if the point's coordinates are both within
	 * <code>PATH_INFLUENCE_WIDTH</code> distance from any point on the Path.
	 * @param path
	 * @return
	 */
	static Iterable<Point> cellsOfInfluence(List<Point> path) {
		Set<Point> flattened = new HashSet<Point>();
		for (Point p : path) {
			flattened.addAll(cellsOfInfluence(p, PATH_INFLUENCE_WIDTH));
		}
		return flattened;
	}

	private void point(Graphics2D g, int x1, int y1) {
		g.drawRect(x1 - LINE_EXTENT, y1 - LINE_EXTENT, LINE_WIDTH, LINE_WIDTH);
	}
	public void paint(Graphics2D g) {
		g.setColor(Color.red);
		for (List<Point> path : allPaths) {
			if (path != null) {
				for (Point p : path) {
					point(g, p.x, p.y);
				}
			}
		}
	}
	
	private static enum Corner {

		TOPLEFT(1, 		new Point(LINE_EXTENT, 					LINE_EXTENT)),
		TOPRIGHT(1, 	new Point(SetUp.CANVAS_X - LINE_EXTENT, LINE_EXTENT)),
		BOTTOMLEFT(-1, 	new Point(LINE_EXTENT, 					SetUp.CANVAS_Y - LINE_EXTENT)),
		BOTTOMRIGHT(-1, new Point(SetUp.CANVAS_X - LINE_EXTENT, SetUp.CANVAS_Y - LINE_EXTENT));
		
		private final int ySortDir;
		private final Point start;
		Corner(int yDir, Point start) {
			this.ySortDir = yDir;
			this.start = start;
		}
		
		Point port(int x) {
			Point p = new Point(start);
			p.y += (1 + PATH_INFLUENCE_WIDTH) * x * ySortDir;
			return p;
		}
		
		void sort(List<Shape> shapes) {
			Collections.sort(shapes, new Comparator<Shape>() {

				@Override
				public int compare(Shape o1, Shape o2) {
					return (new Double(o1.getBounds2D().getY()*ySortDir)).compareTo(o2.getBounds2D().getY()*ySortDir);
				}

			});
		}
		
		Corner[] others() {
			Corner[] others = new Corner[3];
			int i = 0;
			for(Corner c : values()) if(c != this) others[i++] = c;
			return others;
		}
	}

	/**
	 * Hella Slider "direction"; either do the ports 1-2-3 or 3-2-1
	 * @author hellochar
	 *
	 */
	private static enum HSDirection {
		FORWARD, REVERSE
	}
	/**
	 * Routing order; either the buttons go first or the hella goes first
	 * @author hellochar
	 *
	 */
	private static enum RouteOrder {
		BUTTONFIRST, HELLAFIRST
	}
	/**
	 * Locations of the hella slider ports (for the case that the hella slider is in the same corner as the normal buttons); 
	 * the hella slider ports either go before the button ports or after.
	 * @author hellochar
	 *
	 */
	private static enum HSPortLocation {
		BEFORE, AFTER
	}
	
	private class SuccessfulException extends Exception {
		List<List<Point>> paths;
		SuccessfulException(List<List<Point>> paths) {
			super();
			this.paths = paths;
		}
	}
	
	/**
	 * Returns true if the pathways were successfully generated; false otherwise.
	 * @param buttonsToConnect
	 * @param generatePathways
	 * @return
	 */
	public boolean generatePathways(List<SensorButtonGroup> buttonsToConnect, boolean generatePathways) {
		allPaths.clear();
		
		List<Shape> buttonGenShapes = new ArrayList<Shape>();
		List<Shape> sliderGenShapes = null; //possibly null!
		for (SensorButtonGroup s : buttonsToConnect) {
			if (s.sensitivity == SetUp.HELLA_SLIDER) {
				sliderGenShapes = new LinkedList();
				HellaSliderPositioner h = s.getHSP();
				sliderGenShapes.add(h.getOuter());
				sliderGenShapes.add(h.getSeg1());
				sliderGenShapes.add(h.getSeg2());
			} else {
				for(ArduinoSensorButton b : s.triggerButtons) {
					buttonGenShapes.add(b.getShape());
				}
			}
		}
		
		if (generatePathways) {
			Grid g = new Grid(SetUp.CANVAS_X, SetUp.CANVAS_Y);
			
			try{
				for(Corner C : Corner.values()) {
					if(sliderGenShapes == null) {
						tryFullGeneration(buttonGenShapes, C);
					} else {
						for(HSDirection D : HSDirection.values()) {
							for(RouteOrder O : RouteOrder.values()) {
								
								for(HSPortLocation L : HSPortLocation.values()) {
									tryFullGeneration(buttonGenShapes, sliderGenShapes, C, D, O, L);
								}
								
								for(Corner Cc : C.others()) {
									tryFullGeneration(buttonGenShapes, sliderGenShapes, C, Cc, D, O);
								}
							}
						}
					}
				}
				throw new PathwayGenerationException();
			}catch(SuccessfulException s) {
				s.paths; //woo we have the paths now
			}catch(PathwayGenerationException e) {
				//oh noes we failed
			}
			
			//==========OLD CODE==============
			try{
				// We take each button and set its cells of influence to “restricted to
				// B”, where B is that button.
				g.restrictAll(buttonGenShapes);
				
				if(sliderGenShapes != null) {
					g.restrictAll(sliderGenShapes);
				}

				//create the mapping between buttons and their ports.
				List<Pair<Shape, Point>> buttonGenPairs = new LinkedList<Pair<Shape, Point>>();
				List<Pair<Shape, Point>> sliderGenPairs = null;
				{
					int x = 0;
					for(Shape b : buttonGenShapes) {
						buttonGenPairs.add(new Pair<Shape, Point>(b, new Point(LINE_EXTENT, (1 + PATH_INFLUENCE_WIDTH) * x + LINE_EXTENT)));
						x++;
					}
					if(sliderGenShapes != null) {
						sliderGenPairs = new LinkedList<Pair<Shape, Point>>();
						for(Shape b : sliderGenShapes) {
							sliderGenPairs.add(new Pair<Shape, Point>(b, new Point(LINE_EXTENT, (1 + PATH_INFLUENCE_WIDTH) * x + LINE_EXTENT)));
							x++;
						}
					}
				}
			// if (btns.size() <= 12)
				if(PRINT_DEBUG) System.out.println("Generating paths for buttons...");
				allPaths.addAll(generateIndividual(g, buttonGenPairs));
				if(sliderGenPairs != null) {
					if(PRINT_DEBUG) System.out.println("Generating paths for hella slider...");
					allPaths.addAll(generateIndividual(g, sliderGenPairs));
				}
			// else
			// allPaths.addAll(generateGrid(btns, ports));
			}catch(PathwayGenerationException e) {
				JOptionPane.showMessageDialog(null, "Buttons could not be routed! You will have to route your own buttons.",
						"button routing failure", JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}

		if (PRINT_DEBUG)
			System.out.println("Paths generated!");

		List<Shape> allShapes = new LinkedList();
		allShapes.addAll(buttonGenShapes);
		if(sliderGenShapes != null)
			allShapes.addAll(sliderGenShapes);
		writeSVG(new File("outline.svg").getAbsoluteFile(), allShapes, allPaths, generatePathways);
		
		return true;
	}

	//On failure, tryFullGeneration simply returns and does nothing. On success, will throw SuccessfulException carrying the data.
	private void tryFullGeneration(List<Shape> buttonShapes, Corner C) throws SuccessfulException {
		Grid g = new Grid();
		C.sort(buttonShapes);

		/*
		restrict shape outlines - 										restrict all buttonShapes
		create port-maps between buttons and their respective points - 	iterate through, make points
		generate wirings depending on order - 							possibly do generateIndividual; may have to decompose method
		 */
	}
	//On failure, tryFullGeneration simply returns and does nothing. On success, will throw SuccessfulException carrying the data.
	private void tryFullGeneration(List<Shape> buttonShapes, List<Shape> sliderShapes, 
			Corner C, HSDirection D, RouteOrder O, HSPortLocation L) throws SuccessfulException {
		Grid g = new Grid();
		C.sort(buttonShapes);

		/*
		restrict shape outlines - 										restrict all buttonShapes, all sliderShapes
		create port-maps between buttons and their respective points - 	iterate through, make points depending on L and D
		generate wirings depending on order - 							possibly do generateIndividual; may have to decompose method; generate depending on O
		 */
	}
	//On failure, tryFullGeneration simply returns and does nothing. On success, will throw SuccessfulException carrying the data.
	private void tryFullGeneration(List<Shape> buttonShapes, List<Shape> sliderShapes,
			Corner C, Corner Cc, HSDirection D, RouteOrder O) throws SuccessfulException {
		Grid g = new Grid();
		C.sort(buttonShapes);

		/*
		restrict shape outlines - 										restrict all buttonShapes, all sliderShapes
		create port-maps between buttons and their respective points - 	iterate through, make points depending on D and Cc
		generate wirings depending on order - 							possibly do generateIndividual; may have to decompose method; generate depending on O
		 */
	}
	
	
	private List<List<Point>> generateIndividual(Grid g, List<Pair<Shape, Point>> pairs) throws PathwayGenerationException {
		List<List<Point>> paths = new ArrayList<List<Point>>();

		int i = 0;
		for (Pair<Shape, Point> p : pairs) { // generate pathways for
														// each button
			i++;
			if (PRINT_DEBUG)
				System.out.println("\tGenerating path " + i + " of "
						+ pairs.size());
			Shape button = p._1;
			Point port = p._2;

			List<Point> nearButton = outlineFor(button);
			List<Point> path;
			try{
				path = g.findPath(nearButton, port, button);
			} catch(PathwayGenerationException e) { //todo: temporary fix to see when things go wrong
				path = null;
			}

			paths.add(path);
			if(path != null)
				g.close(cellsOfInfluence(path));
		}

		return paths;
	}
	
	private class Pair<S, T> {
		public S _1;
		public T _2;
		public Pair(S s, T t) {
			super();
			this._1 = s;
			this._2 = t;
		}
	}
	

	public List<ArduinoSensorButton> sortButtonsByUpperLeft (List<SensorButtonGroup> buttonsToSort) {
		List<ArduinoSensorButton> allButtons = new ArrayList<ArduinoSensorButton>();
	    for (SensorButtonGroup s : buttonsToSort)
	      allButtons.addAll(s.triggerButtons);
	
//	    Collections.sort(allButtons, new Comparator<ArduinoSensorButton>() {
//	
//	      @Override
//	      public int compare(ArduinoSensorButton o1, ArduinoSensorButton o2) {
//	        return (new Integer(o1.upperLeft.y)).compareTo(new Integer(
//	            o2.upperLeft.y));
//	      }
//	
//	    });
	    
	    Collections.sort(allButtons, new Comparator<ArduinoSensorButton>() {

			@Override
			public int compare(ArduinoSensorButton o1, ArduinoSensorButton o2) {
				return (new Double(o1.getShape().getBounds2D().getY())).compareTo(o2.getShape().getBounds2D().getY());
			}

		});
	    
	    return allButtons;
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

	/**
	 * Returns a list of points representing the immediate outline for the given Shape.
	 * @param b
	 * @return
	 */
	public static List<Point> outlineFor(Shape b) {
		List<Point> outline = new LinkedList<Point>();

		FlatteningPathIterator p = new FlatteningPathIterator(b.getPathIterator(null), 1);
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

	private void writeSVG(File svg, List<Shape> buttons,
			List<List<Point>> paths, boolean generatePathways) {
		if (PRINT_DEBUG)
			System.out.println("Writing SVG file to " + svg);

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

		// draw all of the buttons
		Iterator<List<Point>> pathsIterator = paths.iterator();
		g.setStroke(new BasicStroke(.5f));
		for (Shape b : buttons) {
			g.setColor(new Color((float) Math.random(), (float) Math.random(), (float) Math.random()));
//			b.paint(g);
			g.draw(b);

			if (generatePathways) {
				List<Point> path = pathsIterator.next();
				if (path != null) {
//					g.setStroke(new BasicStroke(.2f));
					for (Point p : path) {
						point(g, p.x, p.y);
					}
				}
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

			if (PRINT_DEBUG)
				System.out.println("SVG successfully written!"
						+ (generatePathways ? " Simplifying..." : ""));

			if (generatePathways)
				simplifySVG(svg.getName());
			if (PRINT_DEBUG)
				System.out.println("Finished!");
			mySetup.repaint();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("An error " + e
					+ " occured while trying to write the SVG file to disk.");
		}
	}

	private void simplifySVG(String fileName) {
		try {
			String line;
			String commandStart;
			String osName = System.getProperty("os.name").toLowerCase();

			if (osName.startsWith("windows")) {
				commandStart = "inkscape/inkscape";
			} else if (osName.startsWith("mac")) {
				commandStart = "Inkscape.app/Contents/Resources/bin/inkscape";
			} else {
				System.err.println("Unrecognised OS " + osName
						+ "... aborting SVG simplification!");
				return;
			}

			String commandEnd = fileName
					+ " --verb=EditSelectAll --verb=SelectionCombine --verb=SelectionUnion --verb=FileSave --verb=FileClose";

			String command = commandStart + " " + commandEnd;
			Process p = Runtime.getRuntime().exec(command);
			BufferedReader bri = new BufferedReader(new InputStreamReader(
					p.getInputStream()));
			BufferedReader bre = new BufferedReader(new InputStreamReader(
					p.getErrorStream()));
			while ((line = bri.readLine()) != null) {
				System.out.println("\t" + line);
			}
			bri.close();
			while ((line = bre.readLine()) != null) {
				System.out.println("\t" + line);
			}
			bre.close();
			p.waitFor();
		} catch (Exception err) {
			err.printStackTrace();
		}
	}
}
