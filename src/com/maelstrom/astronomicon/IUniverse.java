package com.maelstrom.astronomicon;


public interface IUniverse {
    
    IWarpGate establishGate(Location location);
    
    void removeGate(IWarpGate gate);
    
}
