package firstplayer;
import battlecode.common.*;

/**
 * Created by Ivan on 1/9/2017.
 */



public class Lumberjack {

    static RobotController rc;


    static MapLocation[] previousPositions = new MapLocation[Util.greedySteps];
    static MapLocation target;
    static boolean left;

    static MapLocation[] basis;
    static int xBase;
    static int yBase;
    static boolean moved;
    static MapLocation superTarget;
    static boolean free;
    static boolean chopped;

    @SuppressWarnings("unused")
    public static void run(RobotController rcc) {
        //code executed onece at the begining

        rc = rcc;
        target = null;
        left = true;
        basis = rc.getInitialArchonLocations(rc.getTeam());
        xBase = (int)Math.floor(basis[0].x);
        yBase = (int)Math.floor(basis[0].y);
        superTarget = null;
        resetPositions(previousPositions);
        free = true;


        while (true) {
            //code executed continually, don't let it end

            chopped = false;


            moved = false;

            tryChop();

            try {

                MapLocation newTarget = checkBestTree();
                if (superTarget == null) {
                    superTarget = newTarget;
                    resetPositions(previousPositions);
                    target = superTarget;
                    free = true;
                } else if (newTarget != null && newTarget.distanceTo(superTarget) > Util.eps) {
                    superTarget = newTarget;
                    resetPositions(previousPositions);
                    target = superTarget;
                    free = true;
                } else if (newTarget == null) {
                    MapLocation m = rc.getInitialArchonLocations(rc.getTeam().opponent())[0];
                    if (superTarget.distanceTo(m) > 0.1f) {
                        superTarget = null;
                        resetPositions(previousPositions);
                        target = superTarget;
                        free = true;
                    }
                }
                if (superTarget == null) {
                    superTarget = rc.getInitialArchonLocations(rc.getTeam().opponent())[0];

                    target = superTarget;
                    free = true;
                }
                if (!moved && !chopped) {
                    //rc.setIndicatorDot(rc.getLocation(), 0, 255, 0);
                    //rc.setIndicatorDot(superTarget, 255, 0, 0);
                    moveGreedy(superTarget);
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
            Clock.yield();
        }
    }

    static void tryChop(){

        int chopID = -1;
        float strikeUtil = 0;
        float chopUtil = 0;

        TreeInfo[] Ti = rc.senseNearbyTrees(rc.getType().strideRadius);
        RobotInfo[] Ri = rc.senseNearbyRobots(rc.getType().strideRadius);

        for (TreeInfo ti: Ti){
            if (!rc.canChop(ti.getID())) continue; //break?
            if (ti.getTeam() == rc.getTeam()) strikeUtil -= 4;
            else if (ti.getTeam() ==  rc.getTeam().opponent()){
                strikeUtil += 4;
                if (chopUtil < 10 && rc.canChop(ti.getID())){
                    chopUtil = 10;
                    chopID = ti.getID();
                }
            }
            else {
                strikeUtil += 2.0f*ti.getRadius();
                if (chopUtil < 5.0f*ti.getRadius()){
                    chopUtil = 5.0f*ti.getRadius();
                    chopID = ti.getID();
                }
            }
        }

        for (RobotInfo ri : Ri){
            if (ri.getID() == rc.getID()) continue;
            if (ri.getTeam() == rc.getTeam()){
                strikeUtil -= ((float)ri.getType().bulletCost*2.0f)/(ri.getType().maxHealth);
            }
            else if (ri.getTeam() == rc.getTeam().opponent()){
                strikeUtil += ((float)ri.getType().bulletCost*2.0f)/(ri.getType().maxHealth);
            }
        }

        try {
            if (chopUtil > strikeUtil && chopUtil > 0) {
                rc.chop(chopID);
                chopped = true;
            }
            else if (strikeUtil > 0) rc.strike();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

    }


    static MapLocation checkBestTree(){

        MapLocation pos = rc.getLocation();

        TreeInfo[] Ti = rc.senseNearbyTrees();

        MapLocation ans = null;
        float util = 0;

        for (TreeInfo ti : Ti){
            if (ti.getTeam() == rc.getTeam()) continue;
            else if (ti.getTeam() == rc.getTeam().opponent()){
                float newUtil = 1000.0f*10.0f - pos.distanceTo(ti.getLocation());
                if (newUtil > util){
                    ans = ti.getLocation();
                    util = newUtil;
                }
            }
            else{
                float newUtil = 1000.0f*5.0f*ti.getRadius() - pos.distanceTo(ti.getLocation());
                if (newUtil > util){
                    ans = ti.getLocation();
                    util = newUtil;
                }
            }
        }

        return ans;
    }




    static void resetPositions(MapLocation[] v){
        for (int i = 0; i < Util.greedySteps; ++i) v[i] = null;
    }

    static void moveGreedy(MapLocation superTarget){

        try {
            if (rc.canSenseLocation(superTarget)){
                TreeInfo tree = rc.senseTreeAtLocation(superTarget);
                if (tree != null){
                    if (rc.getLocation().distanceTo(superTarget) < rc.getType().strideRadius) return;
                }
            }
            if (free) checkLeft(); // change this shit
            MapLocation pos = rc.getLocation();
            Direction naiveDir = new Direction(pos, superTarget);
            if (rc.canMove(superTarget)) {
                rc.move(superTarget);
            } else if (rc.canMove(naiveDir) && !goingBackwards(rc, previousPositions, naiveDir)) {
                rc.move(naiveDir);
                resetPositions(previousPositions);
                target = superTarget;
            } else if (target != null) {
                Direction proDir = new Direction(pos, target);
                Direction dir = tryGreedyMove(rc, proDir);
                if (dir != null){
                    rc.move(dir);
                    free = false;
                }
                else if (Util.goLeft != left) {
                    left = Util.goLeft;
                    dir = tryGreedyMove(rc, proDir);
                    if (dir != null){
                        rc.move(dir);
                        free = false;
                    }

                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    static Direction tryGreedyMove(RobotController rc, Direction proDir){
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

    static void moveRandom(RobotController rc){
        try {
            int a = (int) Math.floor(Math.random() * 4.0);
            for (int i = 0; i < 4; ++i) {
                if (rc.canMove(Util.main_dirs[(a + i) % 4])) {
                    rc.move(Util.main_dirs[(a + i) % 4]);
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    static void checkLeft(){
        try {
            if (superTarget == null) return;
            MapLocation pos = rc.getLocation();
            Direction proDir = new Direction(pos, superTarget);
            Direction dir = Util.greedyMove(rc, proDir, 0, left);
            float r = rc.getType().strideRadius;
            if (dir != null) {
                MapLocation nextPos = pos.add(dir, r);
                boolean noLeft = true;
                if (left) noLeft = false;
                Direction dir2 = Util.greedyMove(rc, proDir, 0, noLeft);
                if (dir2 != null) {
                    MapLocation nextPos2 = pos.add(dir2, r);
                    if (superTarget.distanceTo(nextPos2) < superTarget.distanceTo((nextPos))) {
                        left = noLeft;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
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


}
