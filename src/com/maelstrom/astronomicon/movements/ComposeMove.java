package com.maelstrom.astronomicon.movements;

import java.util.Stack;

import com.maelstrom.astronomicon.Point;

public class ComposeMove implements IMovement {

    Stack<IMovement> list = new Stack<IMovement>();

    public ComposeMove(IMovement... transformations) {
        for (IMovement t : transformations) {
            list.push(t);
        }
        
        normalize(list);
    }

    @Override
    public Point apply(Point what, Point center) {
        for (IMovement t : list) {
            what = t.apply(what, center);
            center = t.apply(center, center);
        }
        return what;
    }

    @Override
    public void stepback() {
        assert nonzero() : "ZERO!";
        
        list.peek().stepback();
        
        normalize(list);
    }

    void normalize(Stack<IMovement> list) {
        while (!list.empty() && !list.peek().nonzero()) {
            list.pop();
        }
    }
    
    @Override
    public boolean nonzero() {
        return !list.isEmpty();
    }
    
    public String toString() {
        return list.toString();
    }

}
