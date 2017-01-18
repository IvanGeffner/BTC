package bytecodetest;
import battlecode.common.*;

import java.util.HashMap;
import java.util.HashSet;

public strictfp class RobotPlayer {
    static RobotController rc;

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {

        // This is the RobotController object. You use it to perform actions from this robot,
        // and to get information on its current status.
        RobotPlayer.rc = rc;

        // Here, we've separated the controls into a different method for each RobotType.
        // You can add the missing ones or rewrite this into your own control structure.

        while (true) {
            MapLocation m1 = new MapLocation(1,2);
            MapLocation m2 = new MapLocation(1,2);
            int i;
            boolean j;
            int a = 6;
            int b = 5;
            int c = a+b;
            System.out.println("Bytecode abans: " + Clock.getBytecodeNum());
            j = (c>b && c>a);
            System.out.println("Bytecode despres: " + Clock.getBytecodeNum());
            Clock.yield();
        }
    }
}