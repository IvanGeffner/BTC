package lumberjackplayer;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.HashSet;


/**
 * Created by Ivan on 1/9/2017.
 */
public class Tank {

    static RobotController rc;

    static MapLocation realTarget;
    static MapLocation newTarget;

    static MapLocation base;
    static MapLocation enemyBase;
    static int xBase;
    static int yBase;

    static HashSet<Integer> readMes;
    static int initialMessage = 0;

    static float maxUtil;

    static int round;
    static int roundTarget;
    static boolean targetUpdated;


    @SuppressWarnings("unused")
    public static void run(RobotController rcc) {
        //code executed onece at the begining

        rc = rcc;
        Initialize();

        while (true) {
            //code executed continually, don't let it end
            targetUpdated = false;
            if (realTarget != null && rc.canSenseLocation(realTarget)){
                newTarget = null;
                maxUtil = 0;
            } else if (realTarget != null && rc.getRoundNum() - roundTarget < Constants.CHANGETARGET){
                newTarget = realTarget;
            } else{
                newTarget = null;
                maxUtil = 0;
            }

            float val = 5.0f/(1.0f + rc.getLocation().distanceTo(enemyBase));
            if (!rc.canSenseLocation(enemyBase) && val > maxUtil){
                maxUtil = val;
                newTarget = enemyBase;
            }


            round = rc.getRoundNum();

            System.out.println("PreMessages " + Clock.getBytecodeNum());

            readMessages();

            System.out.println("PostMessages " + Clock.getBytecodeNum());
            broadcastLocations();

            System.out.println("PostBroadcast " + Clock.getBytecodeNum());

            updateTarget();
            try {
                if (realTarget != null) rc.setIndicatorDot(realTarget, 125, 125, 125);
            }catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }

            Greedy.moveGreedy(rc, realTarget, Constants.BYTECODEATSHOOTING);

            tryShoot();

            System.out.println(maxUtil);

            Clock.yield();
        }
    }

    static void Initialize(){
        enemyBase = rc.getInitialArchonLocations(rc.getTeam().opponent())[0];
        base = rc.getInitialArchonLocations(rc.getTeam())[0];
        xBase = Math.round(base.x);
        yBase = Math.round(base.y);
        readMes = new HashSet<>();

        maxUtil = 5.0f/(1.0f + rc.getLocation().distanceTo(enemyBase));
        newTarget = enemyBase;
        roundTarget = 1;

        initialMessage = 0;
        try{
            initialMessage = rc.readBroadcast(Communication.MAX_BROADCAST_MESSAGE);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    static void updateTarget(){
        if(targetUpdated) roundTarget = rc.getRoundNum();
        if (realTarget != null && newTarget != null && newTarget.distanceTo(realTarget) < Constants.NEWTARGET) return;
        realTarget = newTarget;
        Greedy.resetObstacle(rc);
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
            float R = r.bodyRadius;


            Direction dir = pos.directionTo(m);

            float d = m.distanceTo(pos);

            float a = (float)Math.asin(R/d);

            float l = (float)Math.sqrt(R*R*(1.0f + (float)Math.cos(2*a)));
            float rad = l/(2.0f*(float)Math.sin(2*a));

            RobotInfo[] allies = rc.senseNearbyRobots(pos.add(rad), rad, rc.getTeam());

            TreeInfo[] trees = rc.senseNearbyTrees(pos.add(rad), rad, null);

            Direction dirRight = dir.rotateRightRads(a);
            Direction dirLeft = dir.rotateLeftRads(a);

            for (RobotInfo ally : allies){
                if (ally.getID() == rc.getID()) continue;
                if (dirLeft.radiansBetween(dirRight) > 0) continue;
                MapLocation m2 = ally.getLocation();
                Direction dir2 = pos.directionTo(m2);

                float d2 = pos.distanceTo(m2);
                float ang = (float)Math.asin(ally.getType().bodyRadius/d2);

                Direction dirRight2 = dir2.rotateRightRads(ang);
                Direction dirLeft2 = dir2.rotateLeftRads(ang);

                if (dirRight.radiansBetween(dirRight2) >= 0 && dirLeft.radiansBetween(dirRight2) <= 0) dirLeft = dirRight2;
                if (dirRight.radiansBetween(dirLeft2) >= 0 && dirLeft.radiansBetween(dirRight2) <= 0) dirRight = dirLeft2;
                if (dirRight2.radiansBetween(dirRight) >= 0 && dirRight2.radiansBetween(dirLeft) >= 0){
                    if (dirLeft2.radiansBetween(dirRight)<= 0 && dirLeft2.radiansBetween(dirLeft) <= 0){
                        dirRight = dirLeft2;
                        dirLeft = dirRight2;
                    }
                }
            }

            Direction dirRightA = dirRight;
            Direction dirLeftA = dirLeft;

            for (TreeInfo tree : trees){
                if (tree.getID() == rc.getID()) continue;
                if (dirLeft.radiansBetween(dirRight) > 0) continue;
                MapLocation m2 = tree.getLocation();
                Direction dir2 = pos.directionTo(m2);

                float d2 = pos.distanceTo(m2);
                float ang = (float)Math.asin(tree.getRadius()/d2);

                Direction dirRight2 = dir2.rotateRightRads(ang);
                Direction dirLeft2 = dir2.rotateLeftRads(ang);

                if (dirRight.radiansBetween(dirRight2) >= 0 && dirLeft.radiansBetween(dirRight2) <= 0) dirLeft = dirRight2;
                if (dirRight.radiansBetween(dirLeft2) >= 0 && dirLeft.radiansBetween(dirRight2) <= 0) dirRight = dirLeft2;
                if (dirRight2.radiansBetween(dirRight) >= 0 && dirRight2.radiansBetween(dirLeft) >= 0){
                    if (dirLeft2.radiansBetween(dirRight)<= 0 && dirLeft2.radiansBetween(dirLeft) <= 0){
                        dirRight = dirLeft2;
                        dirLeft = dirRight2;
                    }
                }
            }

            if (dirRight.radiansBetween(dirLeft) > Constants.eps){

                float realAngle = dirRight.radiansBetween(dirLeft)/2;

                System.out.println("Shooting Angle: " + realAngle);
                float multiplier = 1;

                if (r == RobotType.SCOUT) multiplier = 0.2f;
                else if (r == RobotType.LUMBERJACK) multiplier = 1.2f;


                float x;
                if (r == RobotType.ARCHON) x = 10;
                else x = 2.0f*((float)r.bulletCost)/r.maxHealth;

                System.out.println("x = " + x);

                float ut = 0;
                float utTriad = 0;
                float utPentad = 0;

                boolean shootPentad = false;

                ut = x*multiplier - 1;
                if (realAngle > Constants.triadAngle) utTriad = multiplier*x*3.0f - 4;
                if (realAngle > Constants.pentadAngle && dirRightA.radiansBetween(dirLeftA) > Constants.pentadAngle2 ) utPentad = multiplier*x*3.0f - 6;
                if (realAngle > Constants.pentadAngle2){
                    utPentad = multiplier*x*5.0f - 6;
                    shootPentad = true;
                }

                if (ut > maxUtilSingle){
                    dirSingle = dirRight.rotateLeftRads(realAngle);
                    maxUtilSingle = ut;
                }

                if (utTriad > maxUtilTriad){
                    dirTriad = dirRight.rotateLeftRads(realAngle);
                    maxUtilTriad = utTriad;
                }

                if (utPentad > maxUtilPentad){
                    if (shootPentad) dirPentad = dirRight.rotateLeftRads(realAngle);
                    else dirPentad = dirRightA.rotateLeftRads(dirRightA.radiansBetween(dirLeftA)/2);
                    maxUtilPentad = utPentad;
                }
            }

        }

        System.out.println(maxUtilSingle + " " + maxUtilTriad + " " + maxUtilPentad);

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
            if (maxUtilTriad > 0 && rc.canFireTriadShot()) {
                if (maxUtilTriad > maxUtilSingle) {
                    rc.setIndicatorDot(rc.getLocation(), 255,0, 0);
                    rc.setIndicatorDot(rc.getLocation().add(dirTriad), 0,0, 255);
                    rc.fireTriadShot(dirTriad);
                    return;
                }
            }
            if (maxUtilSingle > 0 && rc.canFireSingleShot()) {
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
            System.out.println("Last and Initial: " + lastMessage + " " + initialMessage);
            for (int i = initialMessage; i != lastMessage && Clock.getBytecodesLeft() > Constants.BYTECODEPOSTMESSAGES; ) {
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
                targetUpdated = true;
            }
        }
    }

    static float enemyScore (MapLocation m, int a){
        if (m == null) return 0;
        float d = rc.getLocation().distanceTo(m);
        float s = 0;
        if (a == 5) s = 8;
        else if (a == 4) s = 20;
        else if (a == 3) s = 15;
        else if (a == 2) s = 20;
        else if (a == 1) s = 8;
        else if (a == 0) s = 15;
        return s/(1.0f + d);
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
            float val = enemyScore(enemyPos, a);
            if (val > maxUtil) {
                maxUtil = val;
                newTarget = enemyPos;
                targetUpdated = true;
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
