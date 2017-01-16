package simpleplayer;

import battlecode.common.*;

import java.util.*;


/**
 * Created by Ivan on 1/9/2017.
 */
public class Lumberjack {

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
    static boolean shouldMove;
    @SuppressWarnings("unused")
    public static void run(RobotController rcc) {
        rc = rcc;
        Initialize();

        while (true) {
            //code executed continually, don't let it end

            maxUtil = 0.5f/(1.0f + rc.getLocation().distanceTo(enemyBase));
            newTarget = enemyBase;
            shouldMove = true;

            tryChop();

            readMessages();
            broadcastLocations();
            findBestTree();
            updateTarget();
            if (shouldMove) moveGreedy(realTarget);

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

    static void tryChop(){

        int chopID = -1;
        float strikeUtil = 0;
        float chopUtil = 0;

        TreeInfo[] Ti = rc.senseNearbyTrees(rc.getType().strideRadius+rc.getType().bodyRadius);
        RobotInfo[] Ri = rc.senseNearbyRobots(rc.getType().strideRadius+rc.getType().bodyRadius);

        int cont = 0;

        for (TreeInfo ti: Ti){
            if (!rc.canChop(ti.getID())) continue; //break?
            int x = (int)(ti.maxHealth - ti.getHealth());
            cont += x/(int)GameConstants.LUMBERJACK_CHOP_DAMAGE;
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

        if(cont > 1) shouldMove = false;

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
            }
            else if (strikeUtil > 0) rc.strike();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }


    static void findBestTree(){

        MapLocation pos = rc.getLocation();
        TreeInfo[] Ti = rc.senseNearbyTrees();

        for (TreeInfo ti : Ti){
            if (ti.getTeam() == rc.getTeam()) continue;
            else if (ti.getTeam() == rc.getTeam().opponent()){
                float newUtil = 2.5f/(1.0f + pos.distanceTo(ti.getLocation()));
                if (newUtil > maxUtil){
                    maxUtil = newUtil;
                    newTarget = ti.getLocation();
                }
            }
            else{
                float newUtil = (2.0f*ti.getRadius())/(1.0f + pos.distanceTo(ti.getLocation()));
                if (newUtil > maxUtil){
                    maxUtil = newUtil;
                    newTarget = ti.getLocation();
                }
            }
        }
    }



    static void updateTarget(){
        if (realTarget != null && newTarget != null && newTarget.distanceTo(realTarget) < Constants.eps) return;
        realTarget = newTarget;
        resetObstacle();
    }

    static void readMessages(){
        readMes.clear();
        try {
            int lastMessage = rc.readBroadcast(Communication.MAX_BROADCAST_MESSAGE);
            for (int i = initialMessage; i != lastMessage; ) {
                int a = rc.readBroadcast(i);
                workMessage(a);
                readMes.add(a);
                ++i;
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
        } else if (m[0] == Communication.UNITTREE){
            MapLocation treePos = new MapLocation(m[1]+xBase, m[2]+yBase);
            float val = m[3]/10/ (1.0f + rc.getLocation().distanceTo(treePos));
            if (val > maxUtil){
                maxUtil = val;
                newTarget = treePos;
            }
        } else if (m[0] == Communication.ENEMYTREE){
            MapLocation treePos = new MapLocation(m[1]+xBase, m[2]+yBase);
            float val = 2.5f/(1.0f + rc.getLocation().distanceTo(treePos));
            if (val > maxUtil){
                maxUtil = val;
                newTarget = treePos;
            }
        }
    }

    static float enemyScore (MapLocation m, int a){
        if (m == null) return 0;
        float d = rc.getLocation().distanceTo(m);
        float s = 0;
        if (a == 5) s = 5;
        else if (a == 4) s = 0.1f;
        else if (a == 3) s = 0.1f;
        else if (a == 2) s = 0.2f;
        else if (a == 1) s = 0.5f;
        else if (a == 0) s = 3;
        return s/(d+1);
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
