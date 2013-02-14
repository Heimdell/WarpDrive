package com.maelstrom.astronomicon;

public class Shift implements IMovement {

    Point delta, corrected;
    int   current, nullifying;

    public Shift(int x, int y, int z) {
        init(new Point(x, y, z));
    }

    public Shift(Point delta) {
        init(delta);
    }

    public void init(Point delta) {
        this.delta = delta;
        this.nullifying = delta.maxAbs();

        // protecting from division by zero
        if (nullifying == 0) nullifying = current = 1;

        corrected = buildCurrent();
    }

    @Override
    public Point apply(Point what, Point center) {
        return what.offset(corrected);
    }

    private Point buildCurrent() {
        float k = ((nullifying - current) / (float) nullifying);
        int x = Math.round(delta.x * k);
        int y = Math.round(delta.y * k);
        int z = Math.round(delta.z * k);

        return new Point(x, y, z);
    }

    @Override
    public void stepback() {
        assert nonzero();
        
        current++;
        corrected = buildCurrent();
    }

    @Override
    public boolean nonzero() {
        return current < nullifying;
    }

    @Override
    public Direction getSummaryRotation() {
        // TODO Auto-generated method stub
        return Direction.UNCHANGED;
    }

}
