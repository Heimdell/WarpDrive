package com.maelstrom.astronomicon.workers;

import com.maelstrom.astronomicon.IWarpGate;
import com.maelstrom.astronomicon.Ship;
import com.maelstrom.astronomicon.StateMachine;

public class RamToTheOtherSideWithStepback implements IGenerates<Boolean> {
    
    enum State {
        INITIAL, LOOKING_UP, FOUND, STEPBACKING, STEPBACKS_EXCEEDED
    }
    
    enum Action {
        LOOK_UP, FIND, STEPBACK, EXCEEDE
    }
    
    StateMachine<State, Action> state =
        new StateMachine<State, Action>(State.INITIAL) {

            @Override
            public void initTransitions() {
                when(State.INITIAL,     Action.LOOK_UP,  State.LOOKING_UP);
                when(State.LOOKING_UP,  Action.FIND,     State.FOUND); // terminal
                when(State.LOOKING_UP,  Action.STEPBACK, State.STEPBACKING);
                when(State.STEPBACKING, Action.LOOK_UP,  State.LOOKING_UP);
                when(State.STEPBACKING, Action.EXCEEDE,  State.STEPBACKS_EXCEEDED); // terminal
            }
        
            @Override
            public void performSideEffect(State state, Action event) {
                switch (event) {
                case LOOK_UP:
                    worker = new RamToTheOtherSide(gate, ship);
                    break;

                case STEPBACK:
                    gate.stepback();
                    
                    available_stepbacks--;
                    break;
                    
                default:
                }
            }

            @Override
            public boolean customValidation(State state, Action event) {
                switch (event) {
                case EXCEEDE:  return available_stepbacks <= 0 || !gate.nonzero();
                case LOOK_UP:  return state == State.STEPBACKING && available_stepbacks > 0
                                   || state == State.INITIAL;
                case FIND:     return !worker.hasWork() && worker.results();
                case STEPBACK: return !worker.hasWork() && !worker.results();
                default:       return true;
                }
            }

            @Override
            public boolean onlyOneTransitionAvailable() {
                return true;
            }
        };
    
    RamToTheOtherSide worker;

    IWarpGate gate;
    Ship ship;
    
    int available_stepbacks;
    
    boolean placeFound;
    
    public RamToTheOtherSideWithStepback(IWarpGate gate, Ship ship) {
        this.gate = gate;
        this.ship = ship;
        
        // TODO: replace it with loading from configuration 
        this.available_stepbacks = 20;
    }

    @Override
    public void process(int max_count) {
        assert hasWork();

        state.selfDecide();
        
        if (worker != null && worker.hasWork())
            worker.process(max_count);
    }

    @Override
    public boolean hasWork() {
        return !(state.is(State.FOUND) || state.is(State.STEPBACKS_EXCEEDED));
    }

    @Override
    public Boolean results() {
        assert !hasWork();
        
        return state.is(State.FOUND);
    }

}
