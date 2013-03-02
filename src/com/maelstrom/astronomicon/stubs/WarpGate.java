package com.maelstrom.astronomicon.stubs;

import java.util.Iterator;

import com.maelstrom.astronomicon.IBlock;
import com.maelstrom.astronomicon.IWarpGate;
import com.maelstrom.astronomicon.Location;
import com.maelstrom.astronomicon.Point;


public class WarpGate implements IWarpGate {

    Universe universe;
    Location location;
    
    public WarpGate(Universe universe, Location location) {
        this.universe = universe;
        this.location = location;
    }

    @Override
    public Block getBlock(Point point) {
        return universe.getBlock(location.project(point));
    }

    @Override
    public void putBlock(IBlock block, Point point) {
        assert block instanceof Block;
        
        System.out.println("putting at " + point + "" + block.getKind());
        
        universe.putBlock((Block) block, location.project(point));
    }

    @Override
    public Block vacuum() {
        return universe.vacuum();
    }

    @Override
    public Iterator<Point> iterator() {
        // TODO Auto-generated method stub
        return location.iterator();
    }

    @Override
    public void stepback() {
        location.stepback();
    }

    @Override
    public boolean nonzero() {
        // TODO Auto-generated method stub
        return location.nonzero();
    }

}
