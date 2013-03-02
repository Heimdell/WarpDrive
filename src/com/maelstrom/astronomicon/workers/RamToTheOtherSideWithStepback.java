package com.maelstrom.astronomicon.workers;

import com.maelstrom.astronomicon.IWarpGate;
import com.maelstrom.astronomicon.Ship;

public class RamToTheOtherSideWithStepback implements IGenerates<Boolean> {
    
    // TODO: Understand, how this shit should work
    
    RamToTheOtherSide worker;

    IWarpGate gate;
    Ship ship;
    
    int available_stepbacks;
    
    boolean placeFound;
    
    RamToTheOtherSideWithStepback(IWarpGate gate, Ship ship) {
        this.gate = gate;
        this.ship = ship;
        
        // TODO: replace it with loading from configuration 
        this.available_stepbacks = 20;
        
        resetWorker();
    }

    void stepback() {
        gate.stepback();

        resetWorker();
        
        available_stepbacks--;
    }
    
    void resetWorker() {
        worker = new RamToTheOtherSide(gate, ship);
    }

    boolean workerFailed() {
        return !worker.hasWork() && !worker.results();
    }
    
    boolean workerSucceed() {
        return !worker.hasWork() && worker.results();
    }
    
    @Override
    public void process(int max_count) {
        assert hasWork();

        worker.process(max_count);
        
        if (workerSucceed())
            placeFound = true;
        else 
            if (workerFailed())
                stepback();
    }

    @Override
    public boolean hasWork() {
        return available_stepbacks > 0 && gate.nonzero() && !placeFound;
    }

    @Override
    public Boolean results() {
        return available_stepbacks > 0 && gate.nonzero() && placeFound;
    }

}
