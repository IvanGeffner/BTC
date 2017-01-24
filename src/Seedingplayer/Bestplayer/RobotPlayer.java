package Seedingplayer.Bestplayer;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;

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
        switch (rc.getType()) {
            case ARCHON:
                Archon.run(rc);
                break;
            case GARDENER:
                Gardener.run(rc);
                break;
            case SOLDIER:
                Soldier.run(rc);
                break;
            case LUMBERJACK:
                Lumberjack.run(rc);
                break;
            case TANK:
                Tank.run(rc);
                break;
            case SCOUT:
                Scout.run(rc);
                break;
        }
    }
}