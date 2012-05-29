package pathway;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

//Not currently used, as Inkscape gives me what I need
class Simplifier {
	
	private void tryAdd(Set<Line> lines, Line l) {
		for(Line temp : lines) {
			if(temp.start.equals(l.end) && temp.end.equals(l.start)) { //removal: the two are opposites
				lines.remove(temp);
				return;
			}
//			if(sgn(temp.end.x - temp.start.x) == sgn(l.end.x - l.start.x) &&
//			   sgn(temp.end.y - temp.start.y) == sgn(l.end.y - l.start.y)) { //make sure the two lines are in the same direction before trying extension
//				if(temp.end.equals(l.start)) { //extension from temp's end
//					lines.remove(temp);
//					lines.add(new Line(temp.start, l.end));
//					return;
//				}
//				else if(l.end.equals(temp.start)) { //extension from l's end
//					lines.remove(temp);
//					lines.add(new Line(l.start, temp.end));
//					return;
//				}
//			}
		}
		lines.add(l);
	}
	
	private void combine(Set<Line> lines) {
		boolean repeat = true;
		boolean found;
		while(repeat) {
			found = false;
			
			for(Line l : lines) {
				for(Line temp : lines) {
					if(l.isContinuation(temp)) { //make sure the two lines are in the same direction before trying extension
						//temp is the continuation of l.
						lines.remove(temp);
						lines.remove(l);
						lines.add(new Line(l.start, temp.end));
						found = true;
						break;
					}
				}
				if(found) break;
			}
			
			repeat = found;
		}
	}
	
	private List<Line> makeSinglePath(Set<Line> lineSet, Point startPoint) {

		ArrayList<Line> lineList = new ArrayList();
		//a) find the singular line whose start has the smallest x and y coordinate; let that line be the "current"
		//	find the line connected to "current"; add current to list, set newly found line to current
		//	keep going until you've found a line that already exists in the list. Your list is now done.

//		Line current = Collections.min(lineSet, new Comparator<Line>() {
//			@Override
//			public int compare(Line arg0, Line arg1) {
//				return (new Integer(arg0.start.x + arg0.start.y)).compareTo(arg1.start.x + arg1.start.y);
//			}
//		});
		Line current = null;
		for(Line l : lineSet) if(l.start.equals(startPoint)) { current = l; break; }
		if(current == null) throw new RuntimeException("No line starts at "+startPoint+" in the lineSet!");
		
		
		lineSet.remove(current);
		lineList.add(current);
		while(!current.end.equals(startPoint)) {
			boolean found = false;
			for(Line l : lineSet) {
				if(l.start.equals(current.end)) {
					current = l;
					lineSet.remove(current);
					lineList.add(current);
					found = true;
					break;
				}
			}
			if(!found) {
				throw new RuntimeException("Couldn't find a connector to "+current+"!");
			}
		}
		
		return lineList;
	}
	/**
	 * Each List of Lines in the returned list will have a fully closed path.
	 * @param paths
	 * @param ports
	 * @return
	 */
	public List<List<Line>> simplify(List<List<Point>> paths, List<Point> ports) {
		if(paths.size() == 0) {
			return new LinkedList();
		}
		

	    if(SVGPathwaysGenerator.PRINT_DEBUG) System.out.print("\tSetting up simplification... ");
	  
		Set<Point> allPointsSet = new HashSet();
		for(List<Point> path : paths) for(Point p : path) allPointsSet.add(p);
		
		//For each point, do a clockwise path around it, creating four "line" objects. Do special 
		//insertion rules for each line you try to insert.
		Set<Line> lineSet = new HashSet();

		
		int ps = allPointsSet.size();
		int i = 0;
		for(Point p : allPointsSet) {
			i++;
			tryAdd(lineSet, new Line(p, 					  	new Point(p.x+1, p.y)));
			tryAdd(lineSet, new Line(new Point(p.x+1, p.y), 	new Point(p.x+1, p.y+1)));
			tryAdd(lineSet, new Line(new Point(p.x+1, p.y+1),   new Point(p.x, p.y+1)));
			tryAdd(lineSet, new Line(new Point(p.x, p.y+1),		p));
			if(i % (ps / 10) == 0) {
				if(SVGPathwaysGenerator.PRINT_DEBUG) System.out.print((100*i/ps)+"%... ");
			}
		}
		if(SVGPathwaysGenerator.PRINT_DEBUG) System.out.print("Done!\n\tSimplifying combinations...");
		
		combine(lineSet);
		if(SVGPathwaysGenerator.PRINT_DEBUG) System.out.println("Done!");
		
		List<List<Line>> outlines = new LinkedList();
		i = 0;
		for(Point p : ports) {
			i++;
			if(SVGPathwaysGenerator.PRINT_DEBUG) System.out.println("\tSimplifying path "+i+" of "+ports.size());
			outlines.add(makeSinglePath(lineSet, p));
		}
		return outlines;
	}
}