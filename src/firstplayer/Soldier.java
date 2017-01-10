package firstplayer;
import battlecode.common.*;

/**
 * Created by Ivan on 1/9/2017.
 */
public class Soldier {

    //code executed onece at the begining
    static MapLocation[] previousPositions = new MapLocation[Util.greedySteps];
    static MapLocation target;
    static boolean left = true;
    @SuppressWarnings("unused")
    public static void run(RobotController rc) {

        MapLocation[] opponents = rc.getInitialArchonLocations(rc.getTeam().opponent());
        resetPositions(previousPositions);
        target = opponents[0];

        while (true) {
            //code executed continually, don't let it end
            try {
                MapLocation pos = rc.getLocation();
                Direction naiveDir = new Direction(pos, opponents[0]);
                Direction proDir = new Direction(pos, target);
                if (rc.canMove(naiveDir) && !goingBackwards(rc, previousPositions, naiveDir)) {
                    rc.move(naiveDir);
                    resetPositions(previousPositions);
                    target = opponents[0];
                } else {

                    Direction dir = tryGreedyMove(rc, proDir, left);
                    if (dir != null) rc.move(dir);
                    else if (Util.goLeft != left) {
                        left = Util.goLeft;
                        dir = tryGreedyMove(rc, proDir, left);
                        if (dir != null) rc.move(dir);

                    }
                }


                Clock.yield();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }

    }

    static void resetPositions(MapLocation[] v){
        for (int i = 0; i < Util.greedySteps; ++i) v[i] = null;
    }

    static boolean goingBackwards(RobotController rc, MapLocation[] prevPos, Direction dir){
        MapLocation pos = rc.getLocation();
        MapLocation nextPos = pos.add(dir, rc.getType().strideRadius);
        for (int i = 0; i < Util.greedySteps; ++i) {
            if (prevPos[i] == null) break;
            float d = prevPos[i].distanceTo(pos);
            float nextd = prevPos[i].distanceTo(nextPos);
            if (d > nextd) {
                Direction newDir = new Direction(pos, prevPos[i]);
                if (rc.canMove(newDir)) return true;
            }
        }
        return false;
    }

    static Direction tryGreedyMove(RobotController rc, Direction proDir, boolean left){
        Direction dir = Util.greedyMove(rc, proDir, 0, left);
        if (dir != null){
            for (int i = Util.greedySteps-1; i > 0; --i){
                previousPositions[i] = previousPositions[i-1];
            }
            previousPositions[0] = rc.getLocation();
            if (Util.newTarget != null) target = Util.newTarget;
        }
        return dir;
    }

}
