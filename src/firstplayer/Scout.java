package firstplayer;
import battlecode.common.*;

/**
 * Created by Ivan on 1/9/2017.
 */
public class Scout {

    static boolean explorer;
    static boolean moved;
    static MapLocation subTarget;
    static MapLocation target;

    static MapLocation exploreTarget;
    static RobotController rc;
    static MapLocation previousExplored;
    static MapLocation base;

    static MapLocation[] myArchons;
    static MapLocation[] enemyArchons;

    static  float rotatingAngle;
    static  float currentAngle;
    static  MapLocation center;
    static boolean goingToCenter;

    static int xBase, yBase;


    @SuppressWarnings("unused")
    public static void run(RobotController rcc) {

        rc = rcc;

        base = rc.getInitialArchonLocations(rc.getTeam())[0];
        MapLocation baseOpponent = rc.getInitialArchonLocations(rc.getTeam().opponent())[0];
        xBase = Math.round(base.x);
        yBase = Math.round(base.y);
        center = base;
        goingToCenter = false;
        exploreTarget = rc.getLocation();
        currentAngle = base.directionTo(baseOpponent).radians;

        myArchons = rc.getInitialArchonLocations(rc.getTeam());
        enemyArchons = rc.getInitialArchonLocations(rc.getTeam().opponent());

        //code executed onece at the begining

        explorer = true;

        while (true) {
            //code executed continually, don't let it end

            moved = false;

            tryShake();

            Direction dir = FindTarget();
            if (dir == null || !rc.canMove(dir)) dir = selectDirection();

            try {

                if (dir != null && rc.canMove(dir)) rc.move(dir);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }

            markExplored();



            Clock.yield();
        }
    }

    static void markExplored(){
        MapLocation pos = rc.getLocation();
        int x = Math.round(pos.x) - xBase +100; // 0 to 200
        int y = Math.round(pos.y) - yBase +100; // 0 to 200

    }


    static void tryShake(){

        float maxBullets = 0;
        int id = -1;

        TreeInfo[] Ti = rc.senseNearbyTrees (rc.getType().strideRadius, Team.NEUTRAL);
        for (TreeInfo ti : Ti){
            if (ti.getContainedBullets() > maxBullets){
                if (!rc.canShake(ti.getID())) continue;
                maxBullets = ti.getContainedBullets();
                id = ti.getID();
            }
        }


        try {
            if (maxBullets > 0) rc.shake(id);
            else return;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        if (rc.canShake()) tryShake();

    }

    static Direction FindTarget(){
        MapLocation target2 = null;
        MapLocation pos = rc.getLocation();
        float maxUtil = 0;
        TreeInfo[] Ti = rc.senseNearbyTrees (-1, Team.NEUTRAL);
        for (TreeInfo ti : Ti) {
            float f = ti.getContainedBullets() / (1 + pos.distanceTo(ti.getLocation()));
            if (f > maxUtil) {
                maxUtil = f;
                target2 = ti.getLocation();
            }
        }


        if (maxUtil > 0) return pos.directionTo(target2);
        return null;


    }


    static Direction selectDirection () {
        try {
            MapLocation pos = rc.getLocation();
            //RobotInfo[] R = rc.senseHostileRobots(pos, sightRange);

            //Non-Combat

            if (pos.distanceTo(exploreTarget) <= 5) {
                if (goingToCenter) {
                    rotatingAngle += 4 * Math.PI / 13.0;
                    currentAngle = rotatingAngle;
                    goingToCenter = false;
                }
                exploreTarget = exploreTarget.add(currentAngle, 13.0f);
            }
            Direction dir = pos.directionTo(exploreTarget);
            if (dir == null) return dir;
            MapLocation nextPos = pos.add(dir, rc.getType().strideRadius);

            if (!rc.onTheMap(nextPos, 1)){
                exploreTarget = center;
                dir = pos.directionTo(exploreTarget);
                goingToCenter = true;
            }


            if (rc.getRoundNum() < 400 && !inMyZone(nextPos)){
                exploreTarget = center;
                goingToCenter = true;
                dir = pos.directionTo(exploreTarget);
            }

            if (rc.canMove(dir)) return dir;
            if (dir == null) return null;
            Direction dirL = Util.greedyMoveScout(rc, dir, 0, true);
            if (dirL == null) dirL = Util.greedyMoveScout(rc,dir,0,false);
            return dirL;


        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return null;
    }


    static boolean inMyZone(MapLocation loc){
        float dist1 = 100000, dist2 = 100000;
        for (MapLocation m : myArchons) dist1 = Math.min(dist1, loc.distanceTo(m));
        for (MapLocation m : enemyArchons) dist2 = Math.min(dist2, loc.distanceTo(m));
        return (dist1 <= dist2);

    }



}
