package com.maelstrom.astronomicon.movements;

import java.util.Vector;

import com.maelstrom.astronomicon.Point;

public class ComposeMove implements IMovement {

    Vector<IMovement> list = new Vector<IMovement>();

    int stepped_back;

    public ComposeMove(IMovement... transformations) {
        for (IMovement t : transformations) {
            list.add(t);
        }
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
        assert nonzero();
        
        IMovement last = list.elementAt(list.size() - 1 - stepped_back);

        if (last.nonzero())
            last.stepback();
        else
            stepped_back++;
    }

    @Override
    public boolean nonzero() {
        return !(list.isEmpty() || list.size() <= stepped_back
                && !list.firstElement().nonzero());
    }

}
