
package com.maelstrom.astronomicon;

import java.util.Iterator;

public class Location implements Iterable<Point> {
	Point from, to, center;
	
	IMovement transform;
	
	public Location(Point from, Point center, Point to) {
		init(from, center, to, new Shift(0, 0, 0));
	}

	public Location(Point from, Point center, Point to, IMovement transform) {
		init(from, center, to, transform);
	}

	private void init(Point from, Point center, Point to, IMovement transform) {
		this.from      = from;
		this.to        = to;
		this.center    = center;
		this.transform = transform;
	}

	public Point bottom() {
		Point 
			t = transform.apply(to,   center),
			f = transform.apply(from, center);
		
		return new Point(
			Math.min(f.x, t.x),
			Math.min(f.y, t.y),
			Math.min(f.z, t.z)
		);
	}
	
	public Point top() {
		Point 
			t = transform.apply(to,   center),
			f = transform.apply(from, center);
		
		return new Point(
			Math.max(f.x, t.x),
			Math.max(f.y, t.y),
			Math.max(f.z, t.z)
		);
	}
	
	public void aquire(IMovement move) {
		transform = new ComposeMove(transform, move);
	}
	
	public class PointIterator implements Iterator<Point> {

		Point i = from.copy();
		
		Point diag = to.diagonal(from);
		
		@Override
		public boolean hasNext() {
			// some unavoidable black magic here, due to ugly Iterator interface
			return !new Point(from.x, from.y, to.z + 1).equalsTo(i);
		}

		@Override
		public Point next() {
			Point n = transform.apply(i, center);
			
			if (i.x < to.x) {
				i.x++;
			} else if (i.y < to.y) {
				i.y++;
				i.x = from.x;
			} else {
				i.z++;
				i.x = from.x;
				i.y = from.y;
			}
			
			return n;
		}

		@Override
		public void remove() { }
		
	}

	@Override
	public Iterator<Point> iterator() {
		return new PointIterator();
	}
	
	@Override
	public String toString() {
		return bottom() + " - " + top();
	}
}
