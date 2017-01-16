package simpleplayer;

import battlecode.common.*;

import java.util.*;


/**
 * Created by Ivan on 1/9/2017.
 */
public class Soldier {

    static RobotController rc;

    static MapLocation realTarget;
    static MapLocation newTarget;
    static MapLocation obstacle = null;
    static boolean left = true;
    static float minDistToTarget = Constants.INF;

    static MapLocation base;
    static MapLocation enemyBase;
    static int xBase;
    static int yBase;

    static HashSet<Integer> readMes;
    static int initialMessage = 0;

    static float maxUtil;


    @SuppressWarnings("unused")
    public static void run(RobotController rcc) {
        //code executed onece at the begining

        rc = rcc;
        Initialize();

        while (true) {
            //code executed continually, don't let it end


            maxUtil = 5.0f/(1.0f + rc.getLocation().distanceTo(enemyBase));
            newTarget = enemyBase;

            readMessages();
            broadcastLocations();
            updateTarget();
            moveGreedy(realTarget);

            System.out.println(maxUtil);

            tryShoot();

            Clock.yield();
        }
    }

    static void Initialize(){
        enemyBase = rc.getInitialArchonLocations(rc.getTeam().opponent())[0];
        base = rc.getInitialArchonLocations(rc.getTeam())[0];
        xBase = Math.round(base.x);
        yBase = Math.round(base.y);
        readMes = new HashSet<>();

        initialMessage = 0;
        try{
            initialMessage = rc.readBroadcast(Communication.MAX_BROADCAST_MESSAGE);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    static void updateTarget(){
        if (realTarget != null && newTarget != null && newTarget.distanceTo(realTarget) < Constants.eps) return;
        realTarget = newTarget;
        resetObstacle();
    }

    static void tryShoot(){

        float maxUtilSingle = 0;
        float maxUtilTriad = 0;
        float maxUtilPentad = 0;
        Direction dirSingle = null;
        Direction dirTriad = null;
        Direction dirPentad = null;

        MapLocation pos = rc.getLocation();
        ArrayList<RobotInfo> rArray = new ArrayList<RobotInfo>();
        rArray.clear();
        RobotInfo[] enemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        int cont = 0;
        for (RobotInfo ri : enemies){
            RobotType a = ri.getType();
            MapLocation m = ri.getLocation();
            float d = pos.distanceTo(m);
            if (a == RobotType.SCOUT && d > 5) continue;
            if (a == RobotType.SOLDIER && d > 7) continue;
            Direction dir = pos.directionTo(m);
            boolean addIt = true;
            for (int i = 0; i < cont && !addIt; ++i){
                MapLocation m2 = rArray.get(i).getLocation();
                if (dir.radiansBetween(pos.directionTo(m2)) < Constants.minAngleShoot){
                    addIt = false;
                }
            }
            if (addIt){
                rArray.add(ri);
                ++cont;
                if (cont >= Constants.shootTries) break;
            }
        }

        for (int i = 0; i < cont; ++i){
            RobotInfo ri = rArray.get(i);
            RobotType r = ri.getType();
            MapLocation m = ri.getLocation();


            Direction dir = pos.directionTo(m);

            MapLocation nextPos = pos.add(dir, rc.getType().bodyRadius);
            float d = m.distanceTo(nextPos);

            RobotInfo[] allies = rc.senseNearbyRobots(nextPos, d, rc.getTeam());

            TreeInfo[] trees = rc.senseNearbyTrees(nextPos, d, null);

            float a = (float)Math.asin(r.bodyRadius/d);

            float a2 = a;

            for (RobotInfo ally : allies){
                if (ally.getID() == rc.getID()) continue;
                MapLocation m2 = ally.getLocation();
                Direction dir2 = nextPos.directionTo(m2);

                float d2 = nextPos.distanceTo(m);
                float ang = (float)Math.asin(ally.getType().bodyRadius/d2);

                float angle = dir.radiansBetween(dir2);
                if (angle < ang + a + Constants.eps){
                    a = angle - ang;
                    a2 = a;
                }
            }

            for (TreeInfo tree : trees){
                if (tree.getID() == rc.getID()) continue;
                MapLocation m2 = tree.getLocation();
                Direction dir2 = nextPos.directionTo(m2);

                float d2 = nextPos.distanceTo(m);
                float ang = (float)Math.asin(tree.getRadius()/d2);

                float angle = dir.radiansBetween(dir2);
                if (angle < ang + a + Constants.eps){
                    a = angle - ang;
                }
            }

            if (a > 0){

                System.out.println(a);
                float multiplier = 1;

                if (r == RobotType.SCOUT) multiplier = 0.2f;
                else if (r == RobotType.LUMBERJACK) multiplier = 1.2f;


                float x;
                if (r == RobotType.ARCHON) x = 10;
                else x = 2.0f*((float)r.bulletCost)/r.maxHealth;

                float ut = 0;
                float utTriad = 0;
                float utPentad = 0;

                ut = x*multiplier - 1;
                if (a > Constants.triadAngle) utTriad = multiplier*x*3.0f - 4;
                if (a > Constants.pentadAngle && a2 > Constants.pentadAngle2) utPentad = multiplier*x*3.0f - 6;
                if (a > Constants.pentadAngle2) utPentad = multiplier*x*5.0f - 6;

                if (ut > maxUtilSingle){
                    dirSingle = dir;
                    maxUtilSingle = ut;
                }

                if (utTriad > maxUtilTriad){
                    dirTriad = dir;
                    maxUtilTriad = utTriad;
                }

                if (utPentad > maxUtilPentad){
                    dirPentad = dir;
                    maxUtilPentad = utPentad;
                }
            }

        }

        try {
            if (maxUtilPentad > 0 && rc.canFirePentadShot()) {
                if (maxUtilPentad > maxUtilTriad) {
                    if (maxUtilPentad > maxUtilSingle) {
                        rc.setIndicatorDot(rc.getLocation(), 255,0, 0);
                        rc.setIndicatorDot(rc.getLocation().add(dirPentad), 0,255, 0);
                        rc.firePentadShot(dirPentad);
                        return;
                    }
                }
            }
            else if (maxUtilTriad> 0 && rc.canFireTriadShot()) {
                if (maxUtilTriad > maxUtilSingle) {
                    rc.setIndicatorDot(rc.getLocation(), 255,0, 0);
                    rc.setIndicatorDot(rc.getLocation().add(dirTriad), 0,0, 255);
                    rc.fireTriadShot(dirTriad);
                    return;
                }
            }
            else if (maxUtilSingle > 0 && rc.canFireSingleShot()) {
                rc.setIndicatorDot(rc.getLocation(), 255,0, 0);
                rc.setIndicatorDot(rc.getLocation().add(dirSingle), 120,120, 0);
                rc.fireSingleShot(dirSingle);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    static void readMessages(){
        readMes.clear();
        int cont = 0;
        try {
            int lastMessage = rc.readBroadcast(Communication.MAX_BROADCAST_MESSAGE);
            for (int i = initialMessage; i != lastMessage; ) {
                int a = rc.readBroadcast(i);
                workMessage(a);
                readMes.add(a);
                ++i;
                ++cont;
                if (i >= Communication.MAX_BROADCAST_MESSAGE) i -= Communication.MAX_BROADCAST_MESSAGE;
            }
            initialMessage = lastMessage;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    static void workMessage(int a){
        int[] m = Communication.decode(a);
        if (m[0] == Communication.ENEMY){
            MapLocation enemyPos = new MapLocation(m[1]+xBase, m[2]+yBase);
            float val = enemyScore(enemyPos, m[3]);
            if (val > maxUtil){
                maxUtil = val;
                newTarget = enemyPos;
            }
        }
    }

    static float enemyScore (MapLocation m, int a){
        if (m == null) return 0;
        float d = rc.getLocation().distanceTo(m);
        float s = 0;
        if (a == 5) s = Constants.INF;
        else if (a == 4) s = 1;
        else if (a == 3) s = 40;
        else if (a == 2) s = 5;
        else if (a == 1) s = 15;
        else if (a == 0) s = 20;
        return s/(1.0f + d);
    }

    static void broadcastLocations() {
        RobotInfo[] Ri = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        for (RobotInfo ri : Ri) {
            if (ri.type == RobotType.SCOUT) continue;
            MapLocation enemyPos = ri.getLocation();
            int x = Math.round(enemyPos.x);
            int y = Math.round(enemyPos.y);
            int a = Constants.getIndex(ri.type);
            int m = Communication.encodeFinding(Communication.ENEMY, x - xBase, y - yBase, a);
            float val = enemyScore(enemyPos, a);
            if (val > maxUtil) {
                maxUtil = val;
                newTarget = enemyPos;
            }
            if (readMes.contains(m)) continue;
            try {
                rc.broadcast(initialMessage, m);
                ++initialMessage;
                if (initialMessage >= Communication.MAX_BROADCAST_MESSAGE)
                    initialMessage -= Communication.MAX_BROADCAST_MESSAGE;
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }

        TreeInfo[] Ti = rc.senseNearbyTrees(-1, rc.getTeam().opponent());
        for (TreeInfo ti : Ti) {
            MapLocation treePos = ti.getLocation();
            int x = Math.round(treePos.x);
            int y = Math.round(treePos.y);
            int m = Communication.encodeFinding(Communication.ENEMYTREE, x - xBase, y - yBase);
            if (readMes.contains(m)) continue;
            try {
                rc.broadcast(initialMessage, m);
                ++initialMessage;
                if (initialMessage >= Communication.MAX_BROADCAST_MESSAGE)
                    initialMessage -= Communication.MAX_BROADCAST_MESSAGE;
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }

        Ti = rc.senseNearbyTrees(-1, Team.NEUTRAL);
        for (TreeInfo ti : Ti) {
            MapLocation treePos = ti.getLocation();
            int x = Math.round(treePos.x);
            int y = Math.round(treePos.y);
            RobotType r = ti.getContainedRobot();
            if (r != null) {
                int a = (int) r.bulletCost;
                int m = Communication.encodeFinding(Communication.UNITTREE, x - xBase, y - yBase, a);
                if (readMes.contains(m)) continue;
                try {
                    rc.broadcast(initialMessage, m);
                    ++initialMessage;
                    if (initialMessage >= Communication.MAX_BROADCAST_MESSAGE)
                        initialMessage -= Communication.MAX_BROADCAST_MESSAGE;
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        try {
            rc.broadcast(Communication.MAX_BROADCAST_MESSAGE, initialMessage);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    static void moveGreedy(MapLocation target){
        if (target == null) return;
        try {
            MapLocation pos = rc.getLocation();
            float stride = rc.getType().strideRadius;
            Direction dirObstacle = null;
            if (obstacle == null) {
                if (rc.canMove(target)){
                    rc.move(target);
                    return;
                }
                Direction dir = pos.directionTo(target);
                if (rc.canMove(dir)) {
                    rc.move(dir);
                    return;
                }
                else{
                    dirObstacle = dir;
                }
            }
            else dirObstacle = pos.directionTo(obstacle);
            if (minDistToTarget == Constants.INF) {
                minDistToTarget = pos.distanceTo(target);
                Direction dir1 = Greedy.greedyMove(rc, dirObstacle, 0, left, false);
                obstacle = Greedy.newObstacle;
                Direction dir2 = Greedy.greedyMove(rc, dirObstacle, 0, !left, false);
                MapLocation nextPos1 = null;
                float dist1 = Constants.INF;
                if (dir1 != null){
                    nextPos1 = pos.add(dir1, stride);
                    dist1 = nextPos1.distanceTo(target);
                }
                MapLocation nextPos2 = null;
                float dist2 = Constants.INF;
                if (dir2 != null){
                    nextPos2 = pos.add(dir2, stride);
                    dist2 = nextPos2.distanceTo(target);
                }

                if (dir2 != null &&  dist2 < dist1 && rc.canMove(dir2)){
                    left = !left;
                    rc.move(dir2);
                    obstacle = Greedy.newObstacle;
                }
                else if (dir1 != null && rc.canMove(dir1)){
                    rc.move(dir1);
                }
            } else {
                Direction dir = pos.directionTo(target);
                float dist = pos.distanceTo(target);
                if (dist < rc.getType().strideRadius && rc.canMove(target)){
                    resetObstacle();
                    rc.move(target);
                    return;
                }
                if (dist < minDistToTarget && rc.canMove(dir)){
                    resetObstacle();
                    rc.move(dir);
                    return;
                }
                Direction dirGreedy = Greedy.greedyMove(rc, dirObstacle, 0, left, false);
                if (dirGreedy != null){
                    obstacle = Greedy.newObstacle;
                    if (dist < minDistToTarget) minDistToTarget = dist;
                    rc.move(dirGreedy);
                } else if (Greedy.newLeft != left){
                    left = Greedy.newLeft;
                    dirGreedy = Greedy.greedyMove(rc, dirObstacle, 0, left, false);
                    if (dirGreedy != null) {
                        obstacle = Greedy.newObstacle;
                        if (dist < minDistToTarget) minDistToTarget = dist;
                        rc.move(dirGreedy);
                    } else if (!Greedy.finished){
                        if (Greedy.newObstacle != null) obstacle = Greedy.newObstacle;
                    }
                } else if (!Greedy.finished){
                    if (Greedy.newObstacle != null) obstacle = Greedy.newObstacle;
                }

            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

    }

    static void resetObstacle(){
        obstacle = null;
        minDistToTarget = Constants.INF;
    }


}
