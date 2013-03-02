package com.maelstrom.astronomicon;


public interface IWorld {
    
    IBlock get(Point point);
    void   put(Point point, IBlock block);

}
