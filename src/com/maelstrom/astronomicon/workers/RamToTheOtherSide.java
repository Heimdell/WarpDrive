package com.maelstrom.astronomicon.workers;

import com.maelstrom.astronomicon.IBlock;
import com.maelstrom.astronomicon.IWarpGate;
import com.maelstrom.astronomicon.Point;
import com.maelstrom.astronomicon.Ship;
import com.maelstrom.astronomicon.Ship.MatterIterator;


public class RamToTheOtherSide implements IGenerates<Boolean> {

    IWarpGate gate;
    
    Ship.MatterIterator cursor;
    
    boolean exitIsPossible;
    
    public RamToTheOtherSide(IWarpGate gate, Ship ship) {
        this.gate = gate;
        this.cursor = ship.iterator();
        this.exitIsPossible = true;
    }

    @Override
    public void process(int max_count) {
        for (; max_count > 0 && hasWork(); max_count--) {
            Point point = cursor.next();
            
            IBlock block = gate.getBlock(point);
            
            switch (block.getKind()) {
            case SOLID:
            case FORBIDDEN_DESTINATION:
                markExitAsImpossible();
                break;

            default:
                break;
            }
        }
    }

    @Override
    public boolean hasWork() {
        return exitIsPossible && cursor.hasNext();
    }

    @Override
    public Boolean results() {
        return exitIsPossible;
    }
    
    void markExitAsImpossible() {
        exitIsPossible = false;
    }

}
