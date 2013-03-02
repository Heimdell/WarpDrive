package com.maelstrom.astronomicon;

import java.util.ArrayDeque;
import java.util.Iterator;

public class Ship implements Iterable<Point> {

    public ArrayDeque<Point> ship = new ArrayDeque<Point>();

    public void add(Point point) {
        ship.add(point);
    }
    
    public Point poll() {
        return ship.pollFirst();
    }

    @Override
    public MatterIterator iterator() {
        return new MatterIterator();
    }

    public boolean isEmpty() {
        return ship.isEmpty();
    }
    
    public class MatterIterator implements Iterator<Point> {

        Iterator<Point> iterator = ship.iterator();
        
        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public Point next() {
            return iterator.next();
        }

        @Override
        public void remove() {
            iterator.remove();
        }
        
    }
}