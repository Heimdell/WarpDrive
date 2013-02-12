package com.maelstrom.astronomicon;

public class Point {

	public int x, y, z;
	
	public Point(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public boolean hasInPlusOctant(Point other) {
		return x <= other.x
			&& y <= other.y
			&& x <= other.z;
	}
	
	public boolean hasInMinusOctant(Point other) {
		return x >= other.x
			&& y >= other.y
			&& x >= other.z;
	}

	public Point offset(Point delta) {
		return new Point(x + delta.x, y + delta.y, z + delta.z);
	}

	public Point delta(Point pivot) {
		return new Point(x - pivot.x, y - pivot.y, z - pivot.z);
	}

	public Point diagonal(Point pivot) {
		return new Point(x - pivot.x + 1, y - pivot.y + 1, z - pivot.z + 1);
	}

	@Override
	public String toString() {
		return "(" + x + ", " + y + ", " + z + ")";
	}

	public int maxAbs() {
		return Math.max(Math.abs(x), Math.max(Math.abs(y), Math.abs(z)));
	}

	public boolean xBetween(int x1, int x2) {
		return x >= x1 && x <= x2;
	}

	public boolean yBetween(int y1, int y2) {
		return y >= y1 && y <= y2;
	}

	public boolean zBetween(int z1, int z2) {
		return z >= z1 && z <= z2;
	}

	public Point copy() {
		return new Point(x, y, z);
	}
	
	public int volume() {
		return Math.abs(x * y * z);
	}

	public boolean equalsTo(Point i) {
		return x == i.x && y == i.y && z == i.z;
	}
}
