package com.maelstrom.astronomicon.movements;

import com.maelstrom.astronomicon.Point;

public interface IMovement {

    public Point apply(Point what, Point center);

    public void stepback();

    public boolean nonzero();

}
