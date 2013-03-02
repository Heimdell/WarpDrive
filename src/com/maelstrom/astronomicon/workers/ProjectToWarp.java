package com.maelstrom.astronomicon.workers;

import java.util.Iterator;

import com.maelstrom.astronomicon.IBlock;
import com.maelstrom.astronomicon.IWarpGate;
import com.maelstrom.astronomicon.Point;
import com.maelstrom.astronomicon.Ship;


public class ProjectToWarp implements IGenerates<Ship> {
    Ship ship = new Ship();

    Iterator<Point> cursor;
    
    IWarpGate gate;

    private boolean falture;
    
    public ProjectToWarp(IWarpGate gate) {
        this.cursor = gate.iterator();
        this.gate = gate;
    }
    
    public void process(int max_count) {
        for (; max_count > 0 && hasWork(); max_count--) {
            Point point = cursor.next();
            
            IBlock block = gate.getBlock(point);

            //     VOID, BREAKABLE, SOLID, FORBIDDEN_SOURCE, FORBIDDEN_DESTINATION
            switch (block.getKind()) {
            case BREAKABLE:
            case SOLID:
                ship.add(point);
                break;

            case FORBIDDEN_SOURCE:
                stopFetching();
                break;
                
            default:
                break;
            }
        }
    }
    
    private void stopFetching() {
        falture = true;
    }

    public boolean hasWork() {
        return cursor.hasNext() && !falture;
    }
    
    public Ship results(){
        assert !hasWork();
        
        if (falture) return null;
        
        return ship;
    }

}
