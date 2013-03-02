package com.maelstrom.astronomicon.stubs;

import com.maelstrom.astronomicon.*;

public class Block implements IBlock {

    Kind kind;
    
    Direction fromNorth;
    
    public Block(Kind kind, Direction fromNorth) {
        this.kind = kind;
        this.fromNorth = fromNorth;
    }

    public Block(Kind kind) {
        this.kind = kind;
        this.fromNorth = Direction.UNCHANGED;
    }

    @Override
    public Kind getKind() {
        return kind;
    }

    @Override
    public void rotate(Direction angle) {
        fromNorth.add(angle);
    }

}
