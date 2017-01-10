package firstplayer;
import battlecode.common.*;


public class Util {
    public static Direction[] main_dirs = {Direction.getEast(), Direction.getNorth(), Direction.getSouth(), Direction.getWest()};
    public static float rows_dist = 3.0f;
    public static float eps = 0.01f;
    public static int greedySteps = 3;
    public static int greedyTries = 4;
    public static MapLocation newTarget;
    public static boolean goLeft;

    public static Direction greedyMove(RobotController rc, Direction dir, int tries, boolean left){
        goLeft = left;
        if (tries > greedyTries) return null;
        float r = rc.getType().strideRadius;
        float R = rc.getType().bodyRadius;
        MapLocation pos = rc.getLocation();
        if (dir == null) return null;
        if (rc.canMove(dir)) return dir;

        MapLocation nextPos = pos.add(dir, r);

        try{
            if (!rc.onTheMap(nextPos)){
                if (left) goLeft = false;
                else goLeft = true;
                return null;
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        RobotInfo[] Ri = rc.senseNearbyRobots(nextPos, R, null);
        TreeInfo[] Ti = rc.senseNearbyTrees(nextPos, R, null);

        MapLocation m;
        float rm;
        boolean isRobot;
        if (Ri.length == 0 && Ti.length == 0) {
            if (left) goLeft = false;
            else goLeft = true;
            return null;
        }
        if (Ri.length > 0){

                m = Ri[0].getLocation();
                rm = Ri[0].getType().bodyRadius;
                isRobot = true;

        }
        else {
            m = Ti[0].getLocation();
            rm = Ti[0].getRadius();
            isRobot = false;
        }
        Direction currentDir = new Direction (pos, m);

        for (RobotInfo ri : Ri){
            MapLocation m2 = ri.getLocation();
            Direction newDir = new Direction(pos, m2);
            if (cclockwise(newDir, currentDir, dir) == goLeft){
                m = m2;
                rm = ri.getType().bodyRadius;
                currentDir = newDir;
                isRobot = true;
            }
        }

        for (TreeInfo ti : Ti){
            MapLocation m2 = ti.getLocation();
            Direction newDir = new Direction(pos, m2);
            if (cclockwise(newDir, currentDir, dir) == goLeft){
                m = m2;
                rm = ti.getRadius();
                currentDir = newDir;
                isRobot = false;
            }
        }

        if (!isRobot) newTarget = m;
        else newTarget = null;
        float val = (R + rm)/pos.distanceTo(m);
        double angle = Math.asin(val) + eps;
        Direction ultimateDir;
        if (goLeft) ultimateDir = dir.rotateRightRads((float)angle);
        else ultimateDir = dir.rotateLeftRads((float)angle);
        return greedyMove(rc, ultimateDir, tries+1, left);
    }

    public static boolean cclockwise (Direction a, Direction b, Direction c){ //podem passar directament cx,cy
        float da = c.radiansBetween(a);
        float db = c.radiansBetween(b);
        if (da > db) {
            float ax = a.getDeltaX(1);
            float ay = a.getDeltaY(1);
            float cx = c.getDeltaX(1);
            float cy = c.getDeltaY(1);
            if (cx*ay - cy*ax > 0) return true;
            else return false;
        }
        float bx = b.getDeltaX(1);
        float by = b.getDeltaY(1);
        float cx = c.getDeltaX(1);
        float cy = c.getDeltaY(1);
        if (cx*by - cy*bx > 0) return true;
        else return false;
    }

    public static boolean nonZero(float dx){
        if (dx > eps || dx < -eps) return false;
        return true;
    }


}
