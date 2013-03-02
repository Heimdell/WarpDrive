package com.maelstrom.astronomicon.workers;

import com.maelstrom.astronomicon.IUniverse;
import com.maelstrom.astronomicon.IWarpGate;
import com.maelstrom.astronomicon.Location;
import com.maelstrom.astronomicon.Ship;
import com.maelstrom.astronomicon.StateMachine;



public class Warping implements IGenerates<Void> {

    enum State { 
        INITIAL, SCANNING, FITTING, RELEASING,
        SCAN_FAILED, FITTING_FAILED,
        FINISHED,
    }
    
    enum Action {
        SCAN, FIT, RELEASE,
        FAIL_SCANNING, FAIL_FITTING,
        FINISH,
    }
    
    StateMachine<State, Action> state =
        new StateMachine<State, Action>(State.INITIAL) {

            @Override
            public void initTransitions() {
                when(State.INITIAL,  Action.SCAN,          State.SCANNING);
                when(State.SCANNING, Action.FAIL_SCANNING, State.SCAN_FAILED);
                when(State.SCANNING, Action.FIT,           State.FITTING);
                when(State.FITTING,  Action.FAIL_FITTING,  State.FITTING_FAILED);
                when(State.FITTING,  Action.RELEASE,       State.RELEASING);
    
                when(array(State.RELEASING, State.SCAN_FAILED, State.FITTING_FAILED), 
                     Action.FINISH,
                     State.FINISHED);
            }
            
            @Override
            public boolean onlyOneTransitionAvailable() { return true; }
    
            @Override
            public void performSideEffect(State state, Action event) {
                switch (state) {
                case INITIAL:
                    // event always is SCAN
                    currentWorker = warpIn = new ProjectToWarp(income);                    
                    break;
                    
                case SCANNING:
                    if (event == Action.FIT)
                        currentWorker = warpThrough = new RamToTheOtherSideWithStepback(outcome, ship);
                    // do nothing in FAIL_SCANNING
                    break;
                
                case FITTING:
                    if (event == Action.RELEASE)
                        currentWorker = transgress = new TransgressThroughWarp(ship, income, outcome);
                    // do nothing in FAIL_FITTING
                    break;
                
                case FINISHED:
                    currentWorker = null;

                default:
                    break;
                }
                
                if (event == Action.FINISH) {
                    universe.removeGate(income);
                    universe.removeGate(outcome);
                }
            }

            @Override
            public boolean customValidation(State state, Action event) {
                switch (state) {
                case SCANNING:
                    if (warpIn.hasWork()) return false;
                    
                    ship = warpIn.results();

                    if (event == Action.FIT)           return ship != null;
                    if (event == Action.FAIL_SCANNING) return ship == null;
                    
                    return false;

                case FITTING:
                    if (warpThrough.hasWork()) return false;

                    if (event == Action.RELEASE)      return  warpThrough.results();
                    if (event == Action.FAIL_FITTING) return !warpThrough.results();

                    return false;

                case RELEASING:
                    return !transgress.hasWork();

                default:
                    return true;                
                }
            }
        };
    
    IWarpGate income;
    IWarpGate outcome;
    
    IUniverse universe;
    
    IGenerates<Ship>    warpIn;
    IGenerates<Boolean> warpThrough;
    IGenerates<Void>    transgress;
    
    IGenerates currentWorker;
    
    Ship ship;
    
    boolean running;
    boolean workIsDone;

    public Warping(Location from, Location to, IUniverse universe) {
        this.universe = universe;
        this.income   = universe.establishGate(from);
        this.outcome  = universe.establishGate(to);
    }
    
    @Override
    public void process(int max_count) {
        assert hasWork();
        
        state.selfDecide();

        if (currentWorker != null && currentWorker.hasWork())
            currentWorker.process(max_count);
    }

    @Override
    public boolean hasWork() {
        return !state.is(State.FINISHED);
    }

    @Override
    public Void results() {
        return null;
    }

}
