package com.maelstrom.astronomicon.stubs;

import java.util.HashMap;

import com.maelstrom.astronomicon.*;

public class Universe implements IUniverse {

    Block [][][] world;
    
    Location boudary;
    
    HashMap<Character, Kind> compile   = new HashMap<Character, Kind>();
    HashMap<Kind, Character> decompile = new HashMap<Kind, Character>();
    
    public Block vacuum() {
        return new Block(Kind.VOID);
    }

    public Block getBlock(Point point) {
        return world[point.z][point.y][point.x];
    }
    
    public void putBlock(Block block, Point point) {
        world[point.z][point.y][point.x] = block;
    }

    public void showZ(int z) {
        for (Block[] line : world[z]) {
            for (Block block : line) {
                System.out.print(decompile.get(block.kind));
            }
            
            System.out.println(";");
        }
    }
    
    public Universe(Object [] mapping, String [][] space) {
        assert mapping.length % 2 == 0;
        
        for (int i = 0; i < mapping.length; i += 2) {
            char name = (char) mapping[i];
            Kind kind = (Kind) mapping[i + 1];
            
            compile  .put(name, kind);            
            decompile.put(kind, name);
        }
        
        Point diag =
            new Point(
                space[0][0].length(),
                space[0].length, 
                space.length
            );
        
        world = new Block[diag.z][][];

        for (int z = 0; z < space.length; z++) {
            world[z] = new Block[diag.y][];

            for (int y = 0; y < space[z].length; y++) {
                world[z][y] = new Block[diag.x];
                
                for (int x = 0; x < space[z][y].length(); x++) {
                    world[z][y][x] =
                        new Block(
                            compile.get(space[z][y].charAt(x))
                        );
                }
            }
        }
    }
    
    @Override
    public IWarpGate establishGate(Location location) {
        return new WarpGate(this, location);
    }

    @Override
    public void removeGate(IWarpGate gate) {
        // do nothing
    }

}
