package com.maelstrom.astronomicon;


public interface ISpaceCompositionStrategy {

    Point  offset (Point address, IWorld[] worlds);
    IWorld segment(Point address, IWorld[] worlds);

}
