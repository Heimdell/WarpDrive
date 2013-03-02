package com.maelstrom.astronomicon;

import java.util.Iterator;

import com.maelstrom.astronomicon.movements.ComposeMove;
import com.maelstrom.astronomicon.movements.IMovement;
import com.maelstrom.astronomicon.movements.Shift;

public class Location implements Iterable<Point> {

    Point from, to, center;

    IMovement transform;

    public Location(Point from, Point center, Point to) {
        init(from, center, to, new Shift(0, 0, 0));
    }

    public Location(Point from, Point center, Point to, IMovement transform) {
        init(from, center, to, transform);
    }
    
    public Location route(IMovement transform) {
        return new Location(from.copy(), center.copy(), to.copy()).aquire(this.transform).aquire(transform);
    }

    private void init(Point from, Point center, Point to, IMovement transform) {
        this.from = from;
        this.to = to;
        this.center = center;
        this.transform = transform;
    }

    public Point bottom() {
        Point t = transform.apply(to, center),
              f = transform.apply(from, center);

        return new Point(
            Math.min(f.x, t.x),
            Math.min(f.y, t.y),
            Math.min(f.z, t.z)
        );
    }

    public Point top() {
        Point t = transform.apply(to, center),
              f = transform.apply(from, center);

        return new Point(
            Math.max(f.x, t.x),
            Math.max(f.y, t.y),
            Math.max(f.z, t.z)
        );
    }

    public Location aquire(IMovement move) {
        transform = new ComposeMove(transform, move);
        
        return this;
    }

    public Point project(Point point) {
        return transform.apply(point, center);
    }
    
    public class PointIterator implements Iterator<Point> {

        Point i    = from.copy();

        @Override
        public boolean hasNext() {
            // some unavoidable black magic here, due to ugly Iterator interface
            return !new Point(from.x, from.y, to.z + 1).equalsTo(i);
        }

        @Override
        public Point next() {
            Point n = transform.apply(i, center);

            if (i.x < to.x) {
                i.x++;
            }
            else if (i.y < to.y) {
                i.y++;
                i.x = from.x;
            }
            else {
                i.z++;
                i.x = from.x;
                i.y = from.y;
            }

            return n;
        }

        @Override
        public void remove() {}

    }

    @Override
    public PointIterator iterator() {
        return new PointIterator();
    }

    @Override
    public String toString() {
        return bottom() + " - " + top();
    }

    public void stepback() {
        transform.stepback();
    }
    
    public boolean nonzero() {
        return transform.nonzero();
    }
}
