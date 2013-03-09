package com.maelstrom.astronomicon.movements;

import com.maelstrom.astronomicon.Direction;
import com.maelstrom.astronomicon.Point;

public class Rotate implements IMovement {

    Direction roll;

    public Rotate(Direction angle) {
        this.roll = angle;
    }

    @Override
    public Point apply(Point what, Point center) {
        if (roll == Direction.UNCHANGED) return what;

        Point delta = what.delta(center);

        delta = xyTransform(roll, delta);

        return delta.offset(center);
    }

    Point xyTransform(Direction angle, Point what) {
        // do we need any matrices?
        switch (angle) {
        case RIGHT:
            return new Point(-what.y, +what.x, what.z);

        case HALF_CIRCLE:
            return new Point(-what.x, -what.y, what.z);

        case LEFT:
            return new Point(+what.y, -what.x, what.z);

        default:
            return what;
        }
    }

    @Override
    public void stepback() {
        assert nonzero();
        
        roll = Direction.UNCHANGED;
    }

    @Override
    public boolean nonzero() {
        return roll != Direction.UNCHANGED;
    }
    
    public String toString() {
        return roll.toString();
    }

}
