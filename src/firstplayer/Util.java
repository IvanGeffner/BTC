package firstplayer;
import battlecode.common.*;


public class Util {
    public static Direction[] main_dirs = {Direction.getEast(), Direction.getNorth(), Direction.getSouth(), Direction.getWest()};
    public static float rows_dist = 3.0f;
    public static float eps = 0.01f;
    public static int greedySteps = 10;
    public static int greedyTries = 6;
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
    public static boolean finished;
    public static int shootsTries = 2;
    public static float minAngleShoot = (30.0f*(float)Math.PI)/180.0f;
    public static float triadAngle = (20.0f*(float)Math.PI)/180.0f;
    public static float pentadAngle = (15.0f*(float)Math.PI)/180.0f;
    public static float pentadAngle2 = (30.0f*(float)Math.PI)/180.0f;

    public static RobotType[] ProductionUnits = {RobotType.GARDENER, RobotType.LUMBERJACK, RobotType.SOLDIER, RobotType.TANK, RobotType.SCOUT};
    //0 == gardener, 1 == lumberjack, 2 == soldier, 3 == tank, 4 == scout, 5 == tree


    //EXPLORING

    public static int INITIALEXPLORE = 510;
    public static int NUMBITS = 30;

    //CONSTRUCTION!!!

    public static int[] initialBuild = {0, 4, 5, 5, 4, 5, 5, 2, 1, 0, 5, 5, 5, 2, 2, 5, 5, 2, 2, 5};
    public static int[]  initialPositions = {0, 8, 7, 9999, 1, 2};
    public static int[] sequenceBuild = {2, 5, 5, 2, 2, 0, 2, 5, 1, 2, 2 ,1};
    public static int IBL = initialBuild.length;
    public static int SBL = sequenceBuild.length;




    //Broadcast Messages!!

    /*Types of message*/

    static final int STOP = 0x10000000; //tells ally soldier to stop moving (collision)
    static final int NTREE = 0x20000000; //Neutral tree found!


    //BC parameters

    static final int[] unitChannels = {501, 502, 503, 504, 505, 506};
    static final int INITIALIZED = 507;

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
        if (tries == 0){
            isUnit = false;
            finished = false;
        }
        goLeft = left;
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
                if (left) goLeft = false;
                else goLeft = true;
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
            if (left) goLeft = false;
            else goLeft = true;
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
                    float angle = getAngle(pos.distanceTo(m), r, R + rm) + 0.001f;
                    if (goLeft) currentProDir = pos.directionTo(m).rotateRightRads(angle);
                    else currentProDir = pos.directionTo(m).rotateLeftRads(angle);
                    if (Ri[f].getTeam() == rc.getTeam()) isUnit = true;
                }
                else if (Ti.length == 0){
                        if (left) goLeft = false;
                        else goLeft = true;
                        return null;
                }
                else{
                    m = Ti[0].getLocation();
                    float rm = Ti[0].getRadius();
                    float angle = getAngle(pos.distanceTo(m), r, R + rm)+ 0.001f;
                    if (goLeft) currentProDir = pos.directionTo(m).rotateRightRads(angle);
                    else currentProDir = pos.directionTo(m).rotateLeftRads(angle);
                    isUnit = false;
                }
        }
        else {
            m = Ti[0].getLocation();
            float rm = Ti[0].getRadius();
            float angle = getAngle(pos.distanceTo(m), r, R + rm)+ 0.001f;
            if (goLeft) currentProDir = pos.directionTo(m).rotateRightRads(angle);
            else currentProDir = pos.directionTo(m).rotateLeftRads(angle);
            isUnit = false;
        }

        for (RobotInfo ri : Ri){
            if (ri.getID() == rc.getID()) continue;
            MapLocation m2 = ri.getLocation();
            float angle = getAngle(pos.distanceTo(m2), r, R + ri.getType().bodyRadius)+ 0.001f;
            Direction newProDir = null;
            if (goLeft) newProDir = pos.directionTo(m2).rotateRightRads(angle);
            else newProDir = pos.directionTo(m2).rotateLeftRads(angle);

            if (cclockwise(newProDir, currentProDir, dir) == goLeft){
                currentProDir = newProDir;
                m = m2;
                if (ri.getTeam() == rc.getTeam()) isUnit = true;
            }
        }

        for (TreeInfo ti : Ti){
            MapLocation m2 = ti.getLocation();
            float angle = getAngle(pos.distanceTo(m2), r, R + ti.getRadius())+ 0.001f;
            Direction newProDir = null;
            if (goLeft) newProDir = pos.directionTo(m2).rotateRightRads(angle);
            else newProDir = pos.directionTo(m2).rotateLeftRads(angle);

            if (cclockwise(newProDir, currentProDir, dir) == goLeft){
                currentProDir = newProDir;
                m = m2;
                isUnit = false;
            }
        }


        newTarget = m;

        try {
            rc.setIndicatorDot(rc.getLocation().add(currentProDir, 2.0f), 0, 0, 255);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return greedyMove(rc, currentProDir, tries+1, left);
    }

    public static Direction greedyMoveScout(RobotController rc, Direction dir, int tries, boolean left){
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
            if (!rc.onTheMap(nextPos, R)){
                if (left) goLeft = false;
                else goLeft = true;
                return null;
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        RobotInfo[] Ri = rc.senseNearbyRobots(nextPos, R, null);

        MapLocation m = new MapLocation(0,0);
        float rm = 0;
        boolean isRobot;
        isUnit = false;

        if (Ri.length == 0) {
            if (left) goLeft = false;
            else goLeft = true;
            return null;
        }
        if (Ri.length > 0){
            int f = 0;
            while (f < Ri.length && Ri[f].getID() == rc.getID()){
                ++f;
            }
            if (f < Ri.length) {

                m = Ri[f].getLocation();
                rm = Ri[f].getType().bodyRadius;
                isRobot = (Ri[f].getType() == RobotType.SOLDIER);
                if (Ri[f].getTeam() == rc.getTeam()) isUnit = true;
            }
            else return null;
        }
        Direction currentDir = new Direction (pos, m);

        for (RobotInfo ri : Ri){
            if (ri.getID() == rc.getID()) continue;
            MapLocation m2 = ri.getLocation();
            Direction newDir = new Direction(pos, m2);
            if (cclockwise(newDir, currentDir, dir) == goLeft){
                m = m2;
                rm = ri.getType().bodyRadius;
                currentDir = newDir;
                isRobot = (ri.getType() == RobotType.SOLDIER);
                if (ri.getTeam() == rc.getTeam()) isUnit = true;
                else isUnit = false;
            }
        }

        // if (!isRobot) newTarget = m;
        //else newTarget = null;
        newTarget = m;

        float angle = getAngle(pos.distanceTo(m), r, R + rm) + 0.001f;
        //float val = (R + rm)/pos.distanceTo(m);
        //double angle = Math.asin(val) + eps;
        Direction ultimateDir;
        if (goLeft) ultimateDir = currentDir.rotateRightRads(angle);
        else ultimateDir = currentDir.rotateLeftRads(angle);
        return greedyMove(rc, ultimateDir, tries+1, left);
    }

    public static float getAngle (float a, float b, float c){
        double x = (a*a + b*b - c*c)/(2.0f*a*b);
        return (float)Math.acos(x);
    }

    public static boolean cclockwise (Direction a, Direction b, Direction c){ //podem passar directament cx,cy
        float da = c.radiansBetween(a);
        float db = c.radiansBetween(b);
        return (da > db);
    }

    public static boolean nonZero(float dx){
        if (dx > eps || dx < -eps) return false;
        return true;
    }


}
