package util;

import java.awt.Shape;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.parser.AWTPathProducer;
import org.apache.batik.parser.PathParser;
import org.w3c.dom.Document;

public class SVGPathLoader {

	public static List<Shape> loadPaths(String svgName, String... pathIds) {
		try {
			UserAgentAdapter ua = new UserAgentAdapter();
			DocumentLoader loader = new DocumentLoader(ua);
			String svgURI = new File(svgName).toURL().toString();
			Document doc = loader.loadDocument(svgURI);
	
			PathParser pp = new PathParser();
			AWTPathProducer producer = new AWTPathProducer();
			pp.setPathHandler(producer);
			
			List<Shape> shapes = new ArrayList();
			for(String id : pathIds) {
				pp.parse(doc.getElementById(id).getAttribute("d"));
				shapes.add(producer.getShape());
			}
			return shapes;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
}
