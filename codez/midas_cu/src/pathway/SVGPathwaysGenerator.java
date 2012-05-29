package pathway;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.image.BufferedImage;
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
import display.CanvasPanel;
import display.SensorButtonGroup;
import display.SetUp;

//todo: replace inkscape with jts
public class SVGPathwaysGenerator {

	SetUp mySetup;
	private List<List<Point>> allPaths = new ArrayList<List<Point>>();
	private Map<ArduinoSensorButton, Integer> buttonMap = new HashMap();

	public SVGPathwaysGenerator(SetUp s) {
		mySetup = s;
	}

	public static boolean PRINT_DEBUG = true;

	public static final int LINE_EXTENT = 1;

	public static final int LINE_WIDTH = LINE_EXTENT * 2 + 1;
	public static final int BUTTON_INFLUENCE_WIDTH = LINE_WIDTH + 1; // should be
																	// LINE_WIDTH
																	// +
																	// LINE_EXTENT
	public static final int PATH_INFLUENCE_WIDTH = 2 * LINE_WIDTH; // should be
																	// 2*LINE_WIDTH

	/**
	 * Call this method after a successful generatePathways
	 * @return
	 */
	public Map<ArduinoSensorButton, Integer> getButtonMap() {
		return buttonMap;
	}
	
	/**
	 * Returns 
	 * @param p
	 * @param extent
	 * @return
	 */
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
		g.setColor(CanvasPanel.LIGHT_COPPER);
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
		//	a) a list of lists of points, representing the paths
		//	b) a mapping between ArduinoSensorButtons to indices, representing which button is mapped to which port.
		
		List<List<Point>> paths;
		Map<ArduinoSensorButton, Integer> map;
		SuccessfulException(List<List<Point>> paths, Map<ArduinoSensorButton, Integer> map) {
			super();
			this.paths = paths;
			this.map = map;
		}
	}
	
	/**
	 * Returns true if the pathways were successfully generated; false otherwise.
	 * @param groups
	 * @param generatePathways
	 * @return
	 */
	public boolean generatePathways(List<SensorButtonGroup> groups, boolean generatePathways) {
		allPaths.clear();
		buttonMap.clear();
		
		List<ArduinoSensorButton> buttons = new ArrayList<ArduinoSensorButton>();
		SensorButtonGroup slider = null; //possibly null!
		
		List<Shape> allShapes = new LinkedList();
		
		//Iterate through the groups, separating the slider from the normal buttons while also aggregating all the shapes.
		for (SensorButtonGroup s : groups) {
//			if(s.isCustom) {
//				allShapes.add(s.triggerButtons.get(0).imageOutline());
//			} else 
			if (s.sensitivity == SetUp.HELLA_SLIDER) {
				slider = s;
				allShapes.addAll(s.getHSP().getShapes());
			} else {
				buttons.addAll(s.triggerButtons);
				for(ArduinoSensorButton b : s.triggerButtons) {
					allShapes.add(b.getPathwayShape());
				}
			}
		}

		writeSVG(new File("mask.svg").getAbsoluteFile(), allShapes, null, false); //always output mask.svg
		
		if (generatePathways) {
//			Grid g = new Grid(SetUp.CANVAS_X, SetUp.CANVAS_Y);
			
			//This block will iterate through all the possible configurations and call tryFullGeneration with that configuration.
			//tryFullGeneration will either return void on failure, or throw a SuccessfulException on success, which will be caught
			//by the try/catch block to leave the iteration early.
			try{
				int configNum = 1;
				for(Corner C : Corner.values()) {
					if(slider == null) {
						if(PRINT_DEBUG) System.out.println("Attempting config "+(configNum++));
						tryFullGeneration(buttons, C);
					} else {
						for(HSDirection D : HSDirection.values()) {
							for(RouteOrder O : RouteOrder.values()) {
								for(HSPortLocation L : HSPortLocation.values()) {
									if(PRINT_DEBUG) System.out.println("Attempting config "+(configNum++));
									tryFullGeneration(buttons, slider, C, D, O, L);
								}
							}
						}
					}
				}
				throw new PathwayGenerationException();
			}catch(SuccessfulException s) {
				if (PRINT_DEBUG) System.out.println("Paths generated!");
				
				//a generation should have the following information available:
				//	a) a list of lists of points, representing the paths
				//	b) a mapping between ArduinoSensorButtons to indices, representing which button is mapped to which port.
				
				allPaths.addAll(s.paths); //woo we have the paths now
				buttonMap = s.map;

				writeSVG(new File("outline.svg").getAbsoluteFile(), allShapes, s.paths, generatePathways);
				return true;
				
			}catch(PathwayGenerationException e) {
				//oh noes we failed
				JOptionPane.showMessageDialog(null, "Buttons could not be routed! You will have to route your own buttons.",
						"button routing failure", JOptionPane.ERROR_MESSAGE);
				return false;
			}
		} else { //not generating pathways; you're done
			return true;
		}
	}

	//On failure, tryFullGeneration simply returns and does nothing. On success, will throw SuccessfulException carrying the data.
	private void tryFullGeneration(List<ArduinoSensorButton> buttons, Corner C) throws SuccessfulException {
		Grid g = new Grid();
		
		Map<Shape, ArduinoSensorButton> shapeToButton = new HashMap();
		List<Shape> buttonShapes = new ArrayList();
		for(ArduinoSensorButton b : buttons) {
			Shape s = b.getPathwayShape();
			buttonShapes.add(s);
			shapeToButton.put(s, b);
		}
		
		C.sort(buttonShapes);

//		restrict shape outlines - 										restrict all buttonShapes
		g.restrictAll(buttonShapes);
		

//		create port-maps between buttons and their respective points - 	iterate through, make points
		List<Pair<Shape, Point>> buttonGenPairs = new LinkedList<Pair<Shape, Point>>();
		for(int x = 0; x < buttonShapes.size(); x++) {
			buttonGenPairs.add(new Pair<Shape, Point>(buttonShapes.get(x), C.port(x)));
		}
		
		try{
//			generate wirings depending on order - 							possibly do generateIndividual; may have to decompose method
			
			Pair<List<List<Point>>, Map<Shape, Integer>> buttonInfo = generateIndividual(g, buttonGenPairs);
			List<List<Point>> paths = new ArrayList();
		    paths.addAll(buttonInfo._1);
		    
		    Map<ArduinoSensorButton, Integer> buttonMap = new HashMap();
		    for(Map.Entry<Shape, Integer> entry : buttonInfo._2.entrySet()) {
		    	buttonMap.put(shapeToButton.get(entry.getKey()), entry.getValue());
		    }
		    
			throw new SuccessfulException(paths, buttonMap);
			
		}catch(PathwayGenerationException e) {
			return;
		}
	}
	
	//On failure, tryFullGeneration simply returns and does nothing. On success, will throw SuccessfulException carrying the data.
	private void tryFullGeneration(List<ArduinoSensorButton> buttons, SensorButtonGroup slider, 
			Corner C, HSDirection D, RouteOrder O, HSPortLocation L) throws SuccessfulException {
		Grid g = new Grid();
		
		Map<Shape, ArduinoSensorButton> shapeToButton = new HashMap();
		List<Shape> buttonShapes = new ArrayList();
		for(ArduinoSensorButton b : buttons) {
			Shape s = b.getPathwayShape();
			buttonShapes.add(s);
			shapeToButton.put(s, b);
		}
		List<Shape> sliderShapes = slider.getHSP().getShapes();
		
		C.sort(buttonShapes);

//		restrict shape outlines - 										restrict all buttonShapes, all sliderShapes
		g.restrictAll(buttonShapes);
		g.restrictAll(sliderShapes);
		
//		create port-maps between buttons and their respective points - 	iterate through, make points depending on L and D
		List<Pair<Shape, Point>> buttonGenPairs = new LinkedList<Pair<Shape, Point>>();
		List<Pair<Shape, Point>> sliderGenPairs = new LinkedList<Pair<Shape, Point>>();
		int x = 0;
		switch(L) {
			case AFTER:
				for(Shape s : buttonShapes) {
					buttonGenPairs.add(new Pair<Shape, Point>(s, C.port(x)));
					x++;
				}
				for(Shape s : sliderShapes) {
					sliderGenPairs.add(new Pair<Shape, Point>(s, C.port(x)));
					x++;
				}
				break;
				
			case BEFORE:
				for(Shape s : sliderShapes) {
					sliderGenPairs.add(new Pair<Shape, Point>(s, C.port(x)));
					x++;
				}
				for(Shape s : buttonShapes) {
					buttonGenPairs.add(new Pair<Shape, Point>(s, C.port(x)));
					x++;
				}
				break;
		}
		if(D == HSDirection.REVERSE) {
			Point p1 = sliderGenPairs.get(0)._2,
				  p3 = sliderGenPairs.get(2)._2;
			sliderGenPairs.get(0)._2 = p3;
			sliderGenPairs.get(2)._2 = p1; //swap orders
		}
//		generate wirings depending on order - 							possibly do generateIndividual; may have to decompose method; generate depending on O
		
		switch(O) {
			case BUTTONFIRST:

				try{
//					generate wirings depending on order - 							possibly do generateIndividual; may have to decompose method
					Pair<List<List<Point>>, Map<Shape, Integer>> buttonInfo = generateIndividual(g, buttonGenPairs);
					Pair<List<List<Point>>, Map<Shape, Integer>> sliderInfo = generateIndividual(g, sliderGenPairs);
					List<List<Point>> paths = new ArrayList();
				    paths.addAll(buttonInfo._1); paths.addAll(sliderInfo._1);
				    
				    Map<ArduinoSensorButton, Integer> buttonMap = new HashMap();
				    for(Map.Entry<Shape, Integer> entry : buttonInfo._2.entrySet()) {
				    	buttonMap.put(shapeToButton.get(entry.getKey()), entry.getValue());
				    }
				    
					throw new SuccessfulException(paths, buttonMap);
				}catch(PathwayGenerationException e) {
					return;
				}
				
			case HELLAFIRST:

				try{
//					generate wirings depending on order - 							possibly do generateIndividual; may have to decompose method
					
					Pair<List<List<Point>>, Map<Shape, Integer>> sliderInfo = generateIndividual(g, sliderGenPairs); //WOO DO SLIDER FIRST
					
					
					
					Pair<List<List<Point>>, Map<Shape, Integer>> buttonInfo = generateIndividual(g, buttonGenPairs);
					
					List<List<Point>> paths = new ArrayList();
				    paths.addAll(buttonInfo._1); paths.addAll(sliderInfo._1);
				    
				    Map<ArduinoSensorButton, Integer> buttonMap = new HashMap();
				    for(Map.Entry<Shape, Integer> entry : buttonInfo._2.entrySet()) {
				    	buttonMap.put(shapeToButton.get(entry.getKey()), entry.getValue());
				    }
				    
					throw new SuccessfulException(paths, buttonMap);
				}catch(PathwayGenerationException e) {
					return;
				}
		}
		
		//should this ever actually run?
		throw new RuntimeException("not implemented yet");
	}
	
//	//On failure, tryFullGeneration simply returns and does nothing. On success, will throw SuccessfulException carrying the data.
//	private void tryFullGeneration(List<Shape> buttonShapes, List<Shape> sliderShapes,
//			Corner C, Corner Cc, HSDirection D, RouteOrder O) throws SuccessfulException {
//		Grid g = new Grid();
//		C.sort(buttonShapes);
//		
//		throw new RuntimeException("not implemented yet");
//
//		/*
//		restrict shape outlines - 										restrict all buttonShapes, all sliderShapes
//		create port-maps between buttons and their respective points - 	iterate through, make points depending on D and Cc
//		generate wirings depending on order - 							possibly do generateIndividual; may have to decompose method; generate depending on O
//		 */
//	}
	
	
	/**
	 * Returns a pair whose _1 is the list of paths, and whose _2 is the mapping between input shapes and integers.
	 * @param g
	 * @param pairs
	 * @return
	 * @throws PathwayGenerationException
	 */
	private Pair<List<List<Point>>, Map<Shape, Integer>> generateIndividual(Grid g, List<Pair<Shape, Point>> pairs) throws PathwayGenerationException {
		List<List<Point>> paths = new ArrayList<List<Point>>();
		Map<Shape, Integer> map = new HashMap();

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
//			try{
				path = g.findPath(nearButton, port, button);
//			} catch(PathwayGenerationException e) { //todo: temporary fix to see when things go wrong
//				path = null;
//			}

			paths.add(path);
			
			map.put(button, i);
//			if(path != null)
				g.close(cellsOfInfluence(path));
		}

		return new Pair<List<List<Point>>, Map<Shape, Integer>>(paths, map);
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

	private void writeSVG(File svg, Iterable<Shape> buttons,
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
		g.setStroke(new BasicStroke(.5f));
		g.setColor(Color.red);

		// draw all of the buttons
		Area sum = new Area();
		
		if(generatePathways) {
			//here, we want to convert the path into an area
			for(List<Point> path : paths) {
//				for (Point p : path) {
//					point(g, p.x, p.y);
//				}
//				Area a = new Area();
				for(Point p : path) {
					sum.add(new Area(new Rectangle(p.x - LINE_EXTENT, p.y - LINE_EXTENT, LINE_WIDTH, LINE_WIDTH)));
				}
//				g.draw(a);
			}
		}
		for (Shape b : buttons) {
//			b.paint(g);
//			g.setColor(new Color((float) Math.random(), (float) Math.random(), (float) Math.random()));
//			g.setColor(Color.black);
//			g.draw(b);
			sum.add(new Area(b));
		}
		g.draw(sum);

		// Finally, stream out SVG to the standard output using
		// UTF-8 encoding.
		boolean useCSS = true; // we want to use CSS style attributes

		try {
			Writer out = new FileWriter(svg);
			g.stream(out, useCSS);
			out.flush();
			out.close();

			if (PRINT_DEBUG)
				System.out.println("SVG successfully written! Simplifying...");

//			simplifySVG(svg.getName());
			
			mySetup.repaint();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("An error " + e
					+ " occured while trying to write the SVG file to disk.");
		}
	}

//	private void simplifySVG(String fileName) {
//		try {
//			String line;
//			String commandStart;
//			String osName = System.getProperty("os.name").toLowerCase();
//
//			if (osName.startsWith("windows")) {
//				commandStart = "inkscape/inkscape";
//			} else if (osName.startsWith("mac")) {
//				commandStart = "Inkscape.app/Contents/Resources/bin/inkscape";
//			} else {
//				System.err.println("Unrecognised OS " + osName
//						+ "... aborting SVG simplification!");
//				return;
//			}
//
//			String commandEnd = fileName
//					+ " --verb=EditSelectAll --verb=SelectionCombine --verb=SelectionUnion --verb=FileSave --verb=FileClose";
//
//			String command = commandStart + " " + commandEnd;
//			Process p = Runtime.getRuntime().exec(command);
//			BufferedReader bri = new BufferedReader(new InputStreamReader(
//					p.getInputStream()));
//			BufferedReader bre = new BufferedReader(new InputStreamReader(
//					p.getErrorStream()));
//			while ((line = bri.readLine()) != null) {
//				System.out.println("\t" + line);
//			}
//			bri.close();
//			while ((line = bre.readLine()) != null) {
//				System.out.println("\t" + line);
//			}
//			bre.close();
//			p.waitFor();
//		} catch (Exception err) {
//			err.printStackTrace();
//		}
//	}
}
