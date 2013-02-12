
package com.maelstrom.astronomicon;

public class Main {
	public static void main(String[] args) {
		Point center = new Point( 0,  0,  0);
		Point from   = new Point(-1, -1, -1);
		Point to     = new Point(+1, +1, +1);
		
		Location location = new Location(from, center, to);
		
		location.aquire(new Shift(5, 6, 7));		
		location.aquire(new Rotate(Direction.LEFT));
		
		int limit = 40;
		
		for (Point pt : location) {
			System.out.println(pt);
			
			if (limit-- == 0) break;
		}
		
		System.out.println("====");

		showComplexMoveRegression();
	}

	private static void showComplexMoveRegression() {
		Location location = new Location(
			new Point( 5,  5,  5), // from
			new Point(10, 10, 10), // center
			new Point(12, 12, 12)  // to
		);
		
		IMovement test_move = new ComposeMove( 
			new Shift(+3,  0,  0),
			new Rotate(Direction.LEFT),
			new Shift( 0,  0, -3)
		);
		
		location.aquire(test_move);
		
		for (; test_move.nonzero(); test_move.stepback()) 
		{
			System.out.println(location);
		}
	}
}
