package com.maelstrom.astronomicon;

import java.util.HashMap;
import java.util.HashSet;

public abstract class StateMachine<State, Event> {

    public abstract void    performSideEffect(State state, Event event);
    public abstract boolean customValidation (State state, Event event);
    public abstract void    initTransitions  ();
    public abstract boolean onlyOneTransitionAvailable();

    HashMap<Event, HashMap<State, State>> transitions;
    
    State state;

    boolean self_driven;
    
    public StateMachine(State state) {
        this.state = state;
        this.transitions = new HashMap<Event, HashMap<State, State>>();
        this.self_driven = onlyOneTransitionAvailable();
        
        initTransitions();
    }
    
    @SuppressWarnings("unchecked")
    public State[] array(State... states) { return states; }
    
    public void selfDecide() {
        assert self_driven;
        
        HashSet<Event> available = new HashSet<Event>();
        
        for (Event event : transitions.keySet()) {
            if (can(event))
                available.add(event);
        }
        
        assert available.size() <= 1;
        
        for (Event single : available) {
            tryPerform(single);
        }
    }
    
    @SuppressWarnings("unchecked")
    public void when(State fromState, Event event, State toState) {
        when(array(fromState), event, toState);
    }

    public void when(State[] fromStates, Event event, State toState) 
    {
        HashMap<State, State> transition = transitions.get(event);
        
        if (transition == null)
            transition = new HashMap<State, State>(); 
        
        for (State fromState : fromStates) {
            assert transition.get(fromState) == null;
            
            transition.put(fromState, toState);
        }
        
        transitions.put(event, transition);
    }

    public boolean can(Event event) {
        HashMap<State, State> transition = transitions.get(event);
        
        if (transition.get(state) == null) return false;
        
        return customValidation(state, event);
    }
    
    public void perform(Event event) {
        assert this.can(event) : "This cannot make event!";
        
        HashMap<State, State> transition = transitions.get(event);
        
        // capturing state before we move
        performSideEffect(state, event);
        
        state = transition.get(state);

    }
    
    public boolean tryPerform(Event event) {

        boolean possible = can(event);
        
        if (possible) perform(event);
        
        return possible;
    }
    
    public boolean is(State state) { return this.state.equals(state); }
}
