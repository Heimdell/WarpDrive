package com.maelstrom.astronomicon;

public interface IMovement {

    public Point apply(Point what, Point center);

    public void stepback();

    public boolean nonzero();
    
    public Direction getSummaryRotation();

}
