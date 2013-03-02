package com.maelstrom.astronomicon.workers;

import com.maelstrom.astronomicon.IBlock;
import com.maelstrom.astronomicon.IWarpGate;
import com.maelstrom.astronomicon.Point;
import com.maelstrom.astronomicon.Ship;
import com.maelstrom.astronomicon.Ship.MatterIterator;


public class TransgressThroughWarp implements IGenerates<Void> {
    
    Ship ship;
    
    Ship.MatterIterator brush;
    
        IBlock vacuum;

    IWarpGate income, outcome;

    public TransgressThroughWarp(
            Ship ship,
            IWarpGate income,
            IWarpGate outcome) 
    {
        this.ship = ship;
        this.income = income;
        this.outcome = outcome;
        this.vacuum = income.vacuum();
        
        brush = ship.iterator();
    }
    
    @Override
    public void process(int max_count) {
        for (; max_count > 0 && hasWork(); max_count--) {
            Point point = ship.poll();
            
            IBlock block = income.getBlock(point);
            
            outcome.putBlock(block, point);
            income.putBlock(vacuum, point);
        }
    }

    @Override
    public boolean hasWork() {
        return !ship.isEmpty();
    }

    @Override
    public Void results() {
        return null;
    }
    
}
