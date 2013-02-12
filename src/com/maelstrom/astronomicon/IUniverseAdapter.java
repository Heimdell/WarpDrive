package com.maelstrom.astronomicon;

import java.util.HashMap;

public interface IUniverseAdapter {

	HashMap<Point, IBlock> grabShip(Location location);

	void putShip(HashMap<Point, IBlock> ship, Location location);

}
