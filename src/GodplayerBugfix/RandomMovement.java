package GodplayerBugfix;


import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

/**
 * Created by Ivan on 1/30/2017.
 */
public class RandomMovement {

    static final float dist = 4.0f;
    static MapLocation randomTarget;

    static Direction dir = null;

    static void resetRandom(RobotController rc){
        dir = null;
        float x = (float)Math.random(), y = (float)Math.random();
        if (Math.abs(x-0.5f) > Constants.eps || Math.abs(y-0.5f) > Constants.eps){
            dir = new Direction(x-0.5f,y-0.5f);
        }
        if (dir == null) resetRandom(rc);
        randomTarget = rc.getLocation().add(dir, dist);
        if (rc.canSenseAllOfCircle(randomTarget, rc.getType().bodyRadius)){
            try {
                if (!rc.onTheMap(randomTarget, rc.getType().bodyRadius)) resetRandom(rc);
            }catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
    }

    static void updateTarget(RobotController rc){
        if (dir == null) resetRandom(rc);
        randomTarget = rc.getLocation().add(dir, dist);
        if (rc.canSenseAllOfCircle(randomTarget, rc.getType().bodyRadius)){
            try {
                if (!rc.onTheMap(randomTarget, rc.getType().bodyRadius)) resetRandom(rc);
            }catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
    }




}
