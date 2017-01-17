package simpleplayer;

import battlecode.common.*;
import java.util.*;


/**
 * Created by Ivan on 1/9/2017.
 */
public class Scout {

    static RobotController rc;

    static MapLocation realTarget;

    static MapLocation randomTarget;

    static Direction currentDirection;

    static int initialMessage;

    static MapLocation base;
    static int xBase, yBase;

    static HashSet<Integer> readMes;

    static int round;


    @SuppressWarnings("unused")
    public static void run(RobotController rcc) {
        //code executed onece at the begining

        rc = rcc;

        Initialize();

        while (true) {
            //code executed continually, don't let it end

            round = rc.getRoundNum();
            readMessages();
            broadcastLocations();

            tryShake();

            MapLocation newTarget = findBestTree();
            updateTarget(newTarget);
            if (realTarget == null) moveInYourDirection();
            else Greedy.moveGreedy(rc,realTarget);



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
                Greedy.resetObstacle();
                moveInYourDirection();
                return;
            }
            if (rc.getLocation().distanceTo(randomTarget) < Constants.pushTarget){
                randomTarget = randomTarget.add(currentDirection, Constants.pushTarget);
                Greedy.resetObstacle();
                moveInYourDirection();
                return;
            }
            rc.setIndicatorDot(randomTarget, 0, 0, 255);
            Greedy.moveGreedy(rc,randomTarget);
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
        Greedy.resetObstacle();
    }

    static void readMessages(){
        readMes.clear();
        try {
            int lastMessage = rc.readBroadcast(Communication.MAX_BROADCAST_MESSAGE);
            for (int i = initialMessage; i != lastMessage && Clock.getBytecodesLeft() > Constants.BYTECODEPOSTMESSAGES; ) {
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

    static void broadcastLocations() {
        if (round != rc.getRoundNum()) return;
        RobotInfo[] Ri = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        for (RobotInfo ri : Ri) {
            if (Clock.getBytecodesLeft() < Constants.SAFETYMARGIN) return;
            if (ri.type == RobotType.SCOUT) continue;
            MapLocation enemyPos = ri.getLocation();
            int x = Math.round(enemyPos.x);
            int y = Math.round(enemyPos.y);
            int a = Constants.getIndex(ri.type);
            int m = Communication.encodeFinding(Communication.ENEMY, x - xBase, y - yBase, a);
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
            if (Clock.getBytecodesLeft() < Constants.SAFETYMARGIN) return;
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
            if (Clock.getBytecodesLeft() < Constants.SAFETYMARGIN) return;
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
}
