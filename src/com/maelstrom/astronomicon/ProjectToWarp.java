package com.maelstrom.astronomicon;

import java.util.Iterator;

public class ProjectToWarp implements IGenerates<Ship> {
    Ship ship = new Ship();

    Iterator<Point> cursor;
    
    IWarpGate gate;

    IBlockClassifier prospector;

    private boolean falture;
    
    public ProjectToWarp(IWarpGate gate, IBlockClassifier prospector) {
        this.cursor = gate.iterator();
        this.gate = gate;
        this.prospector = prospector;
    }
    
    public void process(int max_count) {
        for (; max_count > 0 && hasWork(); max_count--) {
            Point point = cursor.next();
            
            IBlock block = gate.getBlock(point);

            Kind kind = prospector.classify(block);
            
            //     VOID, BREAKABLE, SOLID, FORBIDDEN_SOURCE, FORBIDDEN_DESTINATION
            switch (kind) {
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
    
    public Ship results() throws ForbiddenSourceBlockException {
        assert !hasWork();
        
        if (falture)
            throw new ForbiddenSourceBlockException();
        
        return ship;
    }

}
