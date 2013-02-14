package com.maelstrom.astronomicon;

import java.util.Iterator;


public interface IWarpGate {

    IBlock getBlock(Point point);

    void putBlock(IBlock block, Point point);
    
    IBlock vacuum();
    
    Iterator<Point> iterator();
    
    Direction getRotation();
    
}
