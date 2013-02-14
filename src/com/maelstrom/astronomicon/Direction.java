package com.maelstrom.astronomicon;

public enum Direction {
    UNCHANGED(0), RIGHT(1), HALF_CIRCLE(2), LEFT(3);

    public int angle;
    
    private Direction(int angle) {
        this.angle = angle;
    }


    public Direction add(Direction direction) {
        int sum = this.angle + direction.angle;

        switch(sum % 4) {
            case 0:  return UNCHANGED;
            case 1:  return RIGHT;
            case 2:  return HALF_CIRCLE;
            default: return LEFT;
        }
    }
    
    
}
