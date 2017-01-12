package firstplayer;
import battlecode.common.*;


public class Util {
    public static Direction[] main_dirs = {Direction.getEast(), Direction.getNorth(), Direction.getSouth(), Direction.getWest()};
    public static float rows_dist = 3.0f;
    public static float eps = 0.01f;
    public static int greedySteps = 3;
    public static int greedyTries = 7;
    public static int TC = 5; //TreeColumns
    public static int TR = 2; //TRows
    public static int DR = 5; //Distance Row-Row
    public static int ModulR = 2*TR + DR;
    public static int ModulC = 2*TC + DR;
    public static int SouthTree = 4;
    public static int NorthTree = 6;
    public static float minHPWater = 45f;
    public static float minHPGoWater = 25f;
    public static MapLocation newTarget;
    public static boolean goLeft;
    public static boolean isUnit;

    //Broadcast Messages!!

    /*Types of message*/

    static final int STOP = 0x10000000; //tells ally soldier to stop moving (collision)
    static final int NTREE = 0x20000000; //Neutral tree found!


    //BC parameters

    static final int MAX_BROADCAST_MESSAGE = 500;

    static final int typeMask = 0xF0000000; //at most 15
    static final int iOffMask = 0x0FF00000; //at most 255
    static final int jOffMask = 0x000FF000; //at most 255
    static final int iOffShift = 20;
    static final int jOffShift = 12;
    static final int valueShift = 2;
    static final int valueMask = 0x00000FFC; // at most 1023
    static final int roundMask = 0x00000003;



    public static int encodeFinding(int type, int iOffset, int jOffset, int value, int round) {
        int ret = type |
                (((iOffset + 127) & 0xFF) << iOffShift) | /*-100 <= iOffset <= 100*/
                (((jOffset + 127) & 0xFF) << jOffShift) |
                ((value & 0x3FF) << valueShift) |
                (round & roundMask);
        return ret;
    }

    public static int encodeFinding(int type, int iOffset, int jOffset, int round) {
        return encodeFinding(type, iOffset, jOffset, 0, round);
    }

    public static int[] decode(int bitmap) {
        int[] ret = new int[5];
        ret[0] = bitmap & typeMask;
        ret[1] = ((bitmap & iOffMask) >> iOffShift) - 127;
        ret[2] = ((bitmap & jOffMask) >> jOffShift) - 127;
        ret[3] = (bitmap & valueMask) >> valueShift;
        ret[4] = bitmap & roundMask;
        return ret;
    }


    public static Direction greedyMove(RobotController rc, Direction dir, int tries, boolean left){
        if (tries == 0) isUnit = false;
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
        isUnit = false;

        if (Ri.length == 0 && Ti.length == 0) {
            if (left) goLeft = false;
            else goLeft = true;
            return null;
        }
        if (Ri.length > 0){

                m = Ri[0].getLocation();
                rm = Ri[0].getType().bodyRadius;
                isRobot = (Ri[0].getType() == RobotType.SOLDIER);
                if (Ri[0].getID() != rc.getID() && Ri[0].getTeam() == rc.getTeam()) isUnit = true;

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
                isRobot = (ri.getType() == RobotType.SOLDIER);
                if (ri.getID() != ri.getID() && ri.getTeam() == rc.getTeam()) isUnit = true;
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

       // if (!isRobot) newTarget = m;
        //else newTarget = null;
        newTarget = m;

        float angle = getAngle(pos.distanceTo(m), r, R + rm) + eps;
        //float val = (R + rm)/pos.distanceTo(m);
        //double angle = Math.asin(val) + eps;
        Direction ultimateDir;
        if (goLeft) ultimateDir = dir.rotateRightRads(angle);
        else ultimateDir = dir.rotateLeftRads(angle);
        return greedyMove(rc, ultimateDir, tries+1, left);
    }

    public static float getAngle (float a, float b, float c){
        double x = (a*a + b*b - c*c)/(2.0f*a*b);
        return (float)Math.acos(x);
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
