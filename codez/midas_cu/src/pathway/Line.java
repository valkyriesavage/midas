package pathway;

import java.awt.Point;

class Line {
	Point start, end;

	private static int sgn(int val) {
		return (int)Math.signum(val);
	}
	
	Line(Point start, Point end) {
		this.start = start;
		this.end = end;
	}
	
	public int direction() {
		return sgn(end.x - start.x) * 3 + sgn(end.y - start.y);
	}
	
	public boolean isContinuation(Line l) {
		return (direction() == l.direction()) && end.equals(l.start); 
	}
}