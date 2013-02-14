package com.maelstrom.astronomicon;

public class TransgressThroughWarp implements IGenerates<Void> {
    
    Ship ship;
    
    Ship.MatterIterator brush;
    
    IWarpGate gate;

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
        this.vacuum = gate.vacuum();
        
        brush = ship.iterator();
    }
    
    @Override
    public void process(int max_count) {
        for (; max_count > 0 && hasWork(); max_count--) {
            Point point = ship.poll();
            
            IBlock block = income.getBlock(point);
            
            Direction direction = outcome.getRotation();
            
            block.rotate(direction);
            
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
