package simpleplayer;

import battlecode.common.*;
import java.util.*;


/**
 * Created by Ivan on 1/9/2017.
 */
public class Scout {

    static RobotController rc;

    static MapLocation realTarget;
    static MapLocation obstacle;
    static boolean left = true;

    static MapLocation randomTarget;
    static float minDistToTarget = Constants.INF;

    static Direction currentDirection;

    static int initialMessage;

    static MapLocation base;
    static int xBase, yBase;

    static HashSet<Integer> readMes;


    @SuppressWarnings("unused")
    public static void run(RobotController rcc) {
        //code executed onece at the begining

        rc = rcc;

        Initialize();

        while (true) {
            //code executed continually, don't let it end


            readMessages();
            broadcastLocations();

            tryShake();

            MapLocation newTarget = findBestTree();
            updateTarget(newTarget);
            if (realTarget == null) moveInYourDirection();
            else moveGreedy(realTarget);



            Clock.yield();
        }
    }

    static void Initialize(){
        currentDirection = rc.getLocation().directionTo(rc.getInitialArchonLocations(rc.getTeam().opponent())[0]);
        randomTarget = rc.getLocation();
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

    static void moveInYourDirection(){
        try {
            if (rc.canSenseAllOfCircle(randomTarget, rc.getType().bodyRadius) && !rc.onTheMap(randomTarget,rc.getType().bodyRadius)) {
                randomTarget = rc.getLocation();
                currentDirection = currentDirection.rotateLeftRads((float) Math.PI - Constants.rotationAngle);
                resetObstacle();
                moveInYourDirection();
                return;
            }
            if (rc.getLocation().distanceTo(randomTarget) < Constants.pushTarget){
                randomTarget = randomTarget.add(currentDirection, Constants.pushTarget);
                resetObstacle();
                moveInYourDirection();
                return;
            }
            rc.setIndicatorDot(randomTarget, 0, 0, 255);
            moveGreedy(randomTarget);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

    }

    static void moveGreedy(MapLocation target){
        if (target == null) return;
        try {
            randomTarget = rc.getLocation();
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
                Direction dir1 = Greedy.greedyMove(rc, dirObstacle, 0, left, true);
                obstacle = Greedy.newObstacle;
                Direction dir2 = Greedy.greedyMove(rc, dirObstacle, 0, !left, true);
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
                Direction dirGreedy = Greedy.greedyMove(rc, dirObstacle, 0, left, true);
                if (dirGreedy != null){
                    obstacle = Greedy.newObstacle;
                    if (dist < minDistToTarget) minDistToTarget = dist;
                    rc.move(dirGreedy);
                } else if (Greedy.newLeft != left){
                    left = Greedy.newLeft;
                    dirGreedy = Greedy.greedyMove(rc, dirObstacle, 0, left, true);
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



    static MapLocation findBestTree() {
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


        if (maxUtil > 0) return target2;
        return null;
    }

    static void updateTarget(MapLocation newTarget){
        if (realTarget != null && newTarget != null && newTarget.distanceTo(realTarget) < Constants.eps) return;
        realTarget = newTarget;
        resetObstacle();
    }

    static void resetObstacle(){
        obstacle = null;
        minDistToTarget = Constants.INF;
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
        return;
    }

    static void broadcastLocations(){
        RobotInfo[] Ri = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        for (RobotInfo ri : Ri){
            if (ri.type == RobotType.SCOUT) continue;
            MapLocation enemyPos = ri.getLocation();
            int x = Math.round(enemyPos.x);
            int y = Math.round(enemyPos.y);
            int a = Constants.getIndex(ri.type);
            int m = Communication.encodeFinding(Communication.ENEMY, x-xBase, y-yBase,a);
            if (readMes.contains(m)) continue;
            try {
                rc.broadcast(initialMessage, m);
                ++initialMessage;
                if (initialMessage >= Communication.MAX_BROADCAST_MESSAGE) initialMessage -= Communication.MAX_BROADCAST_MESSAGE;
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }

        TreeInfo[] Ti = rc.senseNearbyTrees(-1, rc.getTeam().opponent());
        for(TreeInfo ti : Ti){
            MapLocation treePos = ti.getLocation();
            int x = Math.round(treePos.x);
            int y = Math.round(treePos.y);
            int m = Communication.encodeFinding(Communication.ENEMYTREE, x-xBase, y-yBase);
            if (readMes.contains(m)) continue;
            try {
                rc.broadcast(initialMessage, m);
                ++initialMessage;
                if (initialMessage >= Communication.MAX_BROADCAST_MESSAGE) initialMessage -= Communication.MAX_BROADCAST_MESSAGE;
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }

        Ti = rc.senseNearbyTrees(-1, Team.NEUTRAL);
        for(TreeInfo ti : Ti){
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
        try{
            rc.broadcast(Communication.MAX_BROADCAST_MESSAGE, initialMessage);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
