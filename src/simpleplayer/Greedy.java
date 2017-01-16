package simpleplayer;

import battlecode.common.*;

/**
 * Created by Ivan on 1/15/2017.
 */
public class Greedy {

    static final int greedyTries = 6;

    static boolean newLeft;
    static MapLocation newObstacle;
    static boolean isUnit;
    static boolean finished;

    public static Direction greedyMove(RobotController rc, Direction dir, int tries, boolean left, boolean scout){
        if (tries == 0){
            isUnit = false;
            finished = false;
        }
        newLeft = left;
        if (tries > greedyTries){
            finished = false;
            return null;
        }
        float r = rc.getType().strideRadius;
        float R = rc.getType().bodyRadius;
        MapLocation pos = rc.getLocation();
        if (dir == null){
            finished = true;
            return null;
        }
        if (rc.canMove(dir)) return dir;

        MapLocation nextPos = pos.add(dir, r);

        try{
            if (!rc.onTheMap(nextPos, R)){
                if (left) newLeft = false;
                else newLeft = true;
                finished = true;
                return null;
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        RobotInfo[] Ri = rc.senseNearbyRobots(nextPos, R, null);
        TreeInfo[] Ti = rc.senseNearbyTrees(nextPos, R, null);

        isUnit = false;

        if (Ri.length == 0 && Ti.length == 0) {
            if (left) newLeft = false;
            else newLeft = true;
            finished = true;
            return null;
        }

        Direction currentProDir = null;

        MapLocation m = null;




        if (Ri.length > 0){
            int f = 0;
            while (f < Ri.length && Ri[f].getID() == rc.getID()){
                ++f;
            }
            if (f < Ri.length) {

                m = Ri[f].getLocation();
                float rm = Ri[f].getType().bodyRadius;
                float angle = Mates.getAngle(pos.distanceTo(m), r, R + rm) + 0.001f;
                if (newLeft) currentProDir = pos.directionTo(m).rotateRightRads(angle);
                else currentProDir = pos.directionTo(m).rotateLeftRads(angle);
                if (Ri[f].getTeam() == rc.getTeam()) isUnit = true;
            }
            else if (Ti.length == 0 && !scout){
                if (left) newLeft = false;
                else newLeft = true;
                return null;
            }
            else if (!scout){
                m = Ti[0].getLocation();
                float rm = Ti[0].getRadius();
                float angle = Mates.getAngle(pos.distanceTo(m), r, R + rm)+ 0.001f;
                if (newLeft) currentProDir = pos.directionTo(m).rotateRightRads(angle);
                else currentProDir = pos.directionTo(m).rotateLeftRads(angle);
                isUnit = false;
            }
        }
        else if (!scout){
            m = Ti[0].getLocation();
            float rm = Ti[0].getRadius();
            float angle = Mates.getAngle(pos.distanceTo(m), r, R + rm)+ 0.001f;
            if (newLeft) currentProDir = pos.directionTo(m).rotateRightRads(angle);
            else currentProDir = pos.directionTo(m).rotateLeftRads(angle);
            isUnit = false;
        }

        for (RobotInfo ri : Ri){
            if (ri.getID() == rc.getID()) continue;
            MapLocation m2 = ri.getLocation();
            float angle = Mates.getAngle(pos.distanceTo(m2), r, R + ri.getType().bodyRadius)+ 0.001f;
            Direction newProDir = null;
            if (newLeft) newProDir = pos.directionTo(m2).rotateRightRads(angle);
            else newProDir = pos.directionTo(m2).rotateLeftRads(angle);

            if (Mates.cclockwise(newProDir, currentProDir, dir) == newLeft){
                currentProDir = newProDir;
                m = m2;
                if (ri.getTeam() == rc.getTeam()) isUnit = true;
            }
        }

        if (!scout) {
            for (TreeInfo ti : Ti) {
                MapLocation m2 = ti.getLocation();
                float angle = Mates.getAngle(pos.distanceTo(m2), r, R + ti.getRadius()) + 0.001f;
                Direction newProDir = null;
                if (newLeft) newProDir = pos.directionTo(m2).rotateRightRads(angle);
                else newProDir = pos.directionTo(m2).rotateLeftRads(angle);

                if (Mates.cclockwise(newProDir, currentProDir, dir) == newLeft) {
                    currentProDir = newProDir;
                    m = m2;
                    isUnit = false;
                }
            }
        }


        newObstacle = m;

        try {
            //rc.setIndicatorDot(rc.getLocation().add(currentProDir, 2.0f), 0, 0, 255);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return greedyMove(rc, currentProDir, tries+1, left, scout);
    }


}
