package firstplayer;
import battlecode.common.*;

/**
 * Created by Ivan on 1/9/2017.
 */
public class Gardener {

    static RobotController rc;

    static MapLocation[] previousPositions = new MapLocation[Util.greedySteps];
    static MapLocation target;
    static boolean left;

    static MapLocation[] basis;
    static int xBase;
    static int yBase;
    static boolean moved;

    static int[] Xsorted = {0, 1, -1, 0, 0, 1, -1, -1, 1, -2, 2, 0, 0, 1, -1, -1, -2, -2, 1, 2, 2, -2, 2, 2, -2, 0, 3, 0, -3, -3, -1, 3, -1, 3, -3, 1, 1, -3, 2, -2, -3, 3, 3, 2, -2, -4, 0, 4, 0, -4, -4, 4, -1, -1, 1, 1, 4, -3, 3, 3, -3, -4, -2, 4, 4, -4, 2, 2, -2, 3, 4, 0, 3, -3, -5, 0, -3, -4, 4, -4, 5, 5, 1, -1, -1, -5, -5, 5, 1, 2, 5, 2, -2, -2, -5, 5, -5, 4, -4, -4, 4, -3, -3, 5, 3, 3, -5, -5, 5, 6, -6, 0, 0, 1, -1, 6, -6, -6, 6, 1, -1, -6, -2, -6, 2, 6, 6, 2, -2, -4, -4, -5, 5, 5, 4, -5, 4, -6, 6, -3, -3, 6, 3, 3, -6, -7, 0, 0, 7, -1, -7, -7, -5, 7, 1, 7, 1, 5, -1, 5, -5, 6, 4, -4, -4, 6, 4, -6, -6, -2, 2, 7, -2, 2, 7, -7, -7, 3, -7, 7, 3, -3, 7, -7, -3, 6, 5, -6, -6, 5, -5, -5, 6, -8, 0, 0, 8, -4, 7, 8, 4, 8, -1, -1, -4, -8, -8, 1, -7, -7, 1, 4, 7, 8, 8, 2, 2, -2, -2, -8, -8, 6, -6, -6, 6, -3, 8, -8, -3, -8, 8, 3, 3, -7, 7, -7, 7, -5, 5, 5, -5, -8, -4, 4, 8, -4, 4, -8, 8, -7, 7, 6, -7, 7, -6, -6, 6, 8, 5, -5, -8, -8, 8, -5, 5, 7, -7, -7, 7, -8, 8, 6, -8, 8, 6, -6, -6, 8, -8, 8, 7, 7, -7, -7, -8, 8, -8, -8, 8};
    static int[] Ysorted = {0, 0, 0, -1, 1, 1, -1, 1, -1, 0, 0, 2, -2, -2, 2, -2, -1, 1, 2, -1, 1, 2, -2, 2, -2, -3, 0, 3, 0, 1, -3, 1, 3, -1, -1, -3, 3, -2, -3, 3, 2, -2, 2, 3, -3, 0, -4, 0, 4, 1, -1, 1, -4, 4, -4, 4, -1, 3, -3, 3, -3, -2, 4, 2, -2, 2, 4, -4, -4, -4, 3, -5, 4, 4, 0, 5, -4, 3, -3, -3, 0, -1, 5, 5, -5, -1, 1, 1, -5, 5, 2, -5, -5, 5, -2, -2, 2, -4, -4, 4, 4, 5, -5, -3, 5, -5, 3, -3, 3, 0, 0, -6, 6, 6, 6, -1, 1, -1, 1, -6, -6, 2, 6, -2, -6, -2, 2, 6, -6, -5, 5, 4, 4, -4, -5, -4, 5, 3, 3, -6, 6, -3, -6, 6, -3, 0, -7, 7, 0, -7, -1, 1, 5, -1, -7, 1, 7, 5, 7, -5, -5, 4, 6, -6, 6, -4, -6, 4, -4, 7, 7, -2, -7, -7, 2, 2, -2, 7, -3, 3, -7, 7, -3, 3, -7, -5, 6, 5, -5, -6, -6, 6, 5, 0, 8, -8, 0, 7, 4, -1, -7, 1, -8, 8, -7, -1, 1, -8, 4, -4, 8, 7, -4, -2, 2, 8, -8, 8, -8, 2, -2, -6, 6, -6, 6, -8, 3, 3, 8, -3, -3, -8, 8, 5, -5, -5, 5, 7, 7, -7, -7, 4, -8, 8, -4, 8, -8, -4, 4, -6, -6, 7, 6, 6, -7, 7, -7, -5, -8, 8, -5, 5, 5, -8, 8, -7, -7, 7, 7, -6, -6, -8, 6, 6, 8, 8, -8, 7, -7, -7, 8, -8, 8, -8, 7, -8, 8, -8, 8};
    @SuppressWarnings("unused")
    public static void run(RobotController rcc) {
        //code executed onece at the begining

        rc = rcc;
        target = null;
        left = true;
        basis = rc.getInitialArchonLocations(rc.getTeam());
        xBase = (int)Math.floor(basis[0].x);
        yBase = (int)Math.floor(basis[0].y);
        MapLocation superTarget = null;
        resetPositions(previousPositions);


        while (true) {
            try {

                moved = false;

                if (!tryConstruct()){
                    if (!tryWatering()) {
                        MapLocation newTarget = checkWatering(superTarget);
                        if (newTarget == null) newTarget = checkEmptySpots(superTarget);
                        if (superTarget == null){
                            superTarget = newTarget;
                            resetPositions(previousPositions);
                            target = superTarget;
                        }
                        else if (newTarget != null && newTarget.distanceTo(superTarget) > Util.eps){
                            superTarget = newTarget;
                            resetPositions(previousPositions);
                            target = superTarget;
                        }
                        else if (newTarget == null){
                            superTarget = null;
                            resetPositions(previousPositions);
                            target = superTarget;
                        }
                        if (!moved && superTarget == null) moveRandom(rc);
                        else if (!moved){
                            moveGreedy(superTarget);
                        }
                    }
                }

            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
            Clock.yield();
        }
    }

    static void moveGreedy(MapLocation superTarget){

        try {
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
                if (dir != null) rc.move(dir);
                else if (Util.goLeft != left) {
                    left = Util.goLeft;
                    dir = tryGreedyMove(rc, proDir);
                    if (dir != null) rc.move(dir);

                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    static boolean tryWatering(){
        TreeInfo[] Ti = rc.senseNearbyTrees(2.1f, rc.getTeam());
        float minHP = 1000f;
        TreeInfo t = null;
        for (TreeInfo ti : Ti){
            if (rc.canWater(ti.getID()) && ti.getHealth() < minHP){
                t = ti;
                minHP = ti.getHealth();
            }
        }
        try {
            if (t != null && minHP < Util.minHPWater) {
                rc.water(t.getID());
                return true;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    static MapLocation checkWatering(MapLocation tar){

        try {
            if (tar != null) {
                if (rc.canSenseLocation(tar)) {
                    TreeInfo k = rc.senseTreeAtLocation(tar);
                    if (k != null) {
                        if (k.getTeam() == rc.getTeam() && k.getHealth() < Util.minHPGoWater) {
                            return tar;
                        }
                    }
                }
            }


            TreeInfo[] Ti = rc.senseNearbyTrees (-1, rc.getTeam());
            float maxDist = 1000f;
            TreeInfo m = null;
            for (TreeInfo ti : Ti) {
                if (ti.getHealth() < Util.minHPGoWater) {
                    float d = rc.getLocation().distanceTo(ti.getLocation());
                    if (d < maxDist) {
                        m = ti;
                        maxDist = d;
                    }
                }
            }
            if (m != null) return m.getLocation();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    static MapLocation checkEmptySpots(MapLocation tar){
        if (tar != null) {
            try {
                System.out.println (xBase + " " + yBase);

                int tx = (int) Math.round(tar.x);
                int ty = (int) Math.round(tar.y);
                int tyy = ty + 1000*Util.ModulR - yBase;
                boolean posFound = false;
                if (tyy % (Util.ModulR) == Util.SouthTree - 2){
                    ty += 2;
                    posFound = true;
                }
                else if (tyy % (Util.ModulR) == (Util.NorthTree + 2)%Util.ModulR){
                    ty -= 2;
                    posFound = true;
                }

                if (posFound) {

                    MapLocation m = emptySpot(tx, ty);
                    if (m != null) {

                        if (rc.getLocation().distanceTo(m) < 0.00001f) {
                            Direction dir = new Direction (0f, -1f);
                            if (tyy % (Util.ModulR) == Util.SouthTree - 2) dir = new Direction (0f, 1f);
                            if (rc.canPlantTree(dir)) {
                                rc.plantTree(dir);
                                moved = true;
                                return null;
                            }
                        }
                        return m;
                    }
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }

        MapLocation pos = rc.getLocation();
        int x = (int)Math.round(pos.x);
        int y = (int)Math.round(pos.y);
        for (int i = 0; i < Xsorted.length; ++i){
            int xx = x+Xsorted[i];
            int yy = y+Ysorted[i];
            MapLocation m = emptySpot(xx,yy);
            try {
                if (m != null && rc.onTheMap(m) && rc.canSenseLocation(m)) return m;
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
        return null;
    }

    static MapLocation emptySpot (int x, int y){
        MapLocation m = adjacent(x,y);
        if (m == null) return null;
        RobotInfo[] Ri = rc.senseNearbyRobots(m, 1, null);
        TreeInfo[] Ti = rc.senseNearbyTrees(m, 1, null);
        if (Ti.length > 0) return null;
        if (Ri.length > 1) return null;
        if (Ri.length == 1 && Ri[0].getID() != rc.getID()) return null;
        return gardenerPositionToPlant(x,y);
    }

    static MapLocation adjacent (int x, int y){
        int a = x + 1000*Util.ModulC - xBase;
        int b = y + 1000*Util.ModulR - yBase;
        if ((a%Util.ModulC)%2 == 1 || a%Util.ModulC <= 2 || a%Util.ModulC >= (Util.ModulC) - 2) return null;
        if ((b%Util.ModulR)%2 == 1 || b%Util.ModulR <= 2 || b%Util.ModulR >= (Util.ModulR) - 2) return null;
        float extraY = (float)((a%Util.ModulC) -2) * 0.02f;
        if (b%Util.ModulR == Util.SouthTree) extraY = -extraY;
        return new MapLocation(x, (float)y + extraY);
    }

    static MapLocation gardenerPositionToPlant(int x, int y){
        int a = x + 1000*Util.ModulC - xBase;
        int b = y + 1000*Util.ModulR - yBase;
        if ((a%Util.ModulC)%2 == 1 || a%Util.ModulC <= 2 || a%Util.ModulC >= (Util.ModulC) - 2) return null;
        if ((b%Util.ModulR)%2 == 1 || b%Util.ModulR <= 2 || b%Util.ModulR >= (Util.ModulR) - 2) return null;
        float extraY = (float)((a%Util.ModulC) -2) * 0.02f;
        if (b%Util.ModulR == Util.SouthTree) extraY = -extraY  - 2.0f;
        else extraY += 2.0f;
        return new MapLocation(x, (float)y + extraY);
    }


    static boolean tryConstruct(){
        try{
            for (int i = 0; i < 4; ++i){
                if (rc.canBuildRobot(RobotType.SOLDIER, Util.main_dirs[i])){
                    rc.buildRobot(RobotType.SOLDIER, Util.main_dirs[i]);
                    return true;
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return false;

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

}