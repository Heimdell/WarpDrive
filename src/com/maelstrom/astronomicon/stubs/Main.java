package com.maelstrom.astronomicon.stubs;

import com.maelstrom.astronomicon.*;
import com.maelstrom.astronomicon.movements.*;
import com.maelstrom.astronomicon.workers.Warping;

public class Main {

    public static void main(String[] args) {
        
        String[][] was = {
            {
                "    BBBBBC ",
                "        B  ",
                "        B  ",
                "        B  ",
                "        B  ",
            }, {
                "    BBBBB  ",
                " CCC    B  ",
                " C B    B  ",
                " CCC    B  ",
                " ###    B  ",
            }, {
                "           ",
                " CCC       ",
                "  CB       ",
                " CCC       ",
                " ###       ",
            }
        };
        
        String[][] mustBecome = {
            {
                " BBBCBC ",
                "    CBC ",
                "    CCC ",
                "     B  ",
                "     B  ",
            }, {
                " BBBCBC ",
                "    CCC ",
                "    CBC ",
                "     B  ",
                " ### B  ",
            }, {
                "        ",
                "        ",
                "        ",
                "        ",
                " ###    ",
            }
        };

        Object[] map = {
            ' ', Kind.VOID,
            'C', Kind.SOLID,
            'B', Kind.BREAKABLE,
            '#', Kind.FORBIDDEN_SOURCE,
            '%', Kind.FORBIDDEN_DESTINATION
        };
        
        Universe universe = new Universe(map, was);
        Universe arcadia  = new Universe(map, mustBecome);
        
        Location source =
            new Location(
                new Point(1,1,1),
                new Point(2,2,2),
                new Point(3,3,2)
            );

        Location destination =
             source.route(
                 new ComposeMove(
                     new Rotate(Direction.RIGHT),
                     new Shift(7, -1, -1)
                 )
             );
        
        Warping warping = new Warping(source, destination, universe);
        
        while (warping.hasWork())
            warping.process(10);

        System.out.println("#####");
        
        for (int i = 0; i < 3; i++) {
            System.out.println("<=======");
            universe.showZ(i);
            System.out.println("=======>");
        }
    }
}
