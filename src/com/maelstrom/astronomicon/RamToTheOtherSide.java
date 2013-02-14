package com.maelstrom.astronomicon;

public class RamToTheOtherSide implements IGenerates<Boolean> {

    IWarpGate gate;
    
    Ship ship;
    
    Ship.MatterIterator cursor;
    
    IBlockClassifier prospector;
    
    boolean exitIsPossible;
    
    public RamToTheOtherSide(IWarpGate gate, Ship ship, IBlockClassifier prospector) {
        this.gate = gate;
        this.ship = ship;
        this.cursor = ship.iterator();
        this.prospector = prospector;
        this.exitIsPossible = true;
    }

    @Override
    public void process(int max_count) {
        for (; max_count > 0 && hasWork(); max_count--) {
            Point point = cursor.next();
            
            IBlock block = gate.getBlock(point);
            
            Kind kind = prospector.classify(block);
            
            //     VOID, BREAKABLE, SOLID, FORBIDDEN_SOURCE, FORBIDDEN_DESTINATION
            
            switch (kind) {
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
    public Boolean results() throws ForbiddenDestinationBlockException {
        return exitIsPossible;
    }
    
    void markExitAsImpossible() {
        exitIsPossible = false;
    }

}
