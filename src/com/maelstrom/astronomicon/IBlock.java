package com.maelstrom.astronomicon;

public interface IBlock {

    Kind getKind();
    
    void rotate(Direction direction);
}
