package scoutplayer;

import battlecode.common.*;
import scala.collection.immutable.Stream;

import java.util.HashSet;


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
    static MapLocation pos;

    static int[] sight_zones = new int[14];
    static float mapUpperBound = Constants.INF, mapLowerBound = -Constants.INF, mapLeftBound = -Constants.INF, mapRightBound = Constants.INF;
    static int zoneXmax = 100, zoneXmin = -100, zoneYmax = 100, zoneYmin = -100;

    @SuppressWarnings("unused")
    public static void run(RobotController rcc) {
        //code executed onece at the begining

        rc = rcc;

        Initialize();

        while (true) {
            //code executed continually, don't let it end

            round = rc.getRoundNum();
            pos = rc.getLocation();
            readMessages();
            broadcastLocations();
            checkMapBounds();

            tryShake();

            MapLocation newTarget = findBestTree();
            updateTarget(newTarget);
            if (realTarget == null) {
                updateSightZones();
                newTarget = findNearbyUnexploredZone();
                updateTarget(newTarget);
            }
            if (realTarget == null) {
                moveInYourDirection();
            }
            else Greedy.moveGreedy(rc, realTarget, 9200);



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
                Greedy.resetObstacle(rc);
                moveInYourDirection();
                return;
            }
            if (rc.getLocation().distanceTo(randomTarget) < Constants.pushTarget){
                randomTarget = randomTarget.add(currentDirection, Constants.pushTarget);
                Greedy.resetObstacle(rc);
                moveInYourDirection();
                return;
            }
            rc.setIndicatorDot(randomTarget, 0, 0, 255);
            Greedy.moveGreedy(rc,randomTarget, 9200);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

    }



    static MapLocation findBestTree() {
        MapLocation target2 = null;
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
        Greedy.resetObstacle(rc);
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
            trySend(m);
        }

        TreeInfo[] Ti = rc.senseNearbyTrees(-1, rc.getTeam().opponent());
        for (TreeInfo ti : Ti) {
            if (Clock.getBytecodesLeft() < Constants.SAFETYMARGIN) return;
            MapLocation treePos = ti.getLocation();
            int x = Math.round(treePos.x);
            int y = Math.round(treePos.y);
            int m = Communication.encodeFinding(Communication.ENEMYTREE, x - xBase, y - yBase);
            trySend(m);
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
                trySend(m);
            }
        }
        try {
            rc.broadcast(Communication.MAX_BROADCAST_MESSAGE, initialMessage);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    static void checkMapBounds() {
        try {
            if (mapUpperBound == Constants.INF) {
                float bound = Float.intBitsToFloat(rc.readBroadcast(Communication.MAP_UPPER_BOUND));
                if (bound == Constants.INF) {
                    MapLocation m = checkMapBound(Direction.NORTH);
                    if (m != null) {
                        rc.broadcast(Communication.MAP_UPPER_BOUND, Float.floatToIntBits(m.y));
                        mapUpperBound = bound;
                        zoneYmax = findZoneY(m.y);
                    }
                }
                else {
                    mapUpperBound = bound;
                    zoneYmax = findZoneY(bound);
                }
            }
            if (mapLowerBound == -Constants.INF) {
                float bound = Float.intBitsToFloat(rc.readBroadcast(Communication.MAP_LOWER_BOUND));
                if (bound == -Constants.INF) {
                    MapLocation m = checkMapBound(Direction.SOUTH);
                    if (m != null) {
                        rc.broadcast(Communication.MAP_LOWER_BOUND, Float.floatToIntBits(m.y));
                        mapLowerBound = m.y;
                        zoneYmin = findZoneY(m.y);
                    }
                }
                else {
                    mapLowerBound = bound;
                    zoneYmin = findZoneY(bound);
                }
            }
            if (mapLeftBound == -Constants.INF) {
                float bound = Float.intBitsToFloat(rc.readBroadcast(Communication.MAP_LEFT_BOUND));
                if (bound == -Constants.INF) {
                    MapLocation m = checkMapBound(Direction.WEST);
                    if (m != null) {
                        rc.broadcast(Communication.MAP_LEFT_BOUND, Float.floatToIntBits(m.x));
                        mapLeftBound = m.x;
                        zoneXmin = findZoneX(m.x);
                    }
                }
                else {
                    mapLeftBound = bound;
                    zoneXmin = findZoneX(bound);
                }
            }
            if (mapRightBound == Constants.INF) {
                float bound = Float.intBitsToFloat(rc.readBroadcast(Communication.MAP_RIGHT_BOUND));
                if (bound == Constants.INF) {
                    MapLocation m = checkMapBound(Direction.EAST);
                    if (m != null) {
                        rc.broadcast(Communication.MAP_RIGHT_BOUND, Float.floatToIntBits(m.x));
                        mapRightBound = m.x;
                        zoneXmax = findZoneX(m.x);
                    }
                }
                else {
                    mapRightBound = bound;
                    zoneXmax = findZoneX(bound);
                }
            }

        } catch (GameActionException e) {
            e.printStackTrace();
        }
    }

    static MapLocation checkMapBound(Direction dir) {
        try {
            if (!rc.onTheMap(pos.add(dir, rc.getType().sensorRadius))) {
                float a = 0, b = rc.getType().sensorRadius;
                while (b-a >= Constants.PRECISION_MAP_BOUNDS) {
                    float c = (b+a)/2;
                    if (rc.onTheMap(pos.add(dir, c))) a = c;
                    else b = c;
                }
                return pos.add(dir, (a+b)/2);
            }
        } catch (GameActionException e) {
            e.printStackTrace();
        }
        return null;
    }

    static void trySend(int m) {
        if (readMes.contains(m)) return;
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

    static void updateSightZones() {
        try {
            for (int i = 0; i < 14; ++i) {
                sight_zones[i] = rc.readBroadcast(Communication.SIGHT_ZONES + i);
            }
            int zoneX = findZoneX(pos.x);
            int zoneY = findZoneY(pos.y);
            int zone = zoneX*21+zoneY;
            if ((sight_zones[zone/32] & 1<<(zone&31)) == 0) {
                rc.broadcast(Communication.SIGHT_ZONES+zone/32, sight_zones[zone/32] | 1<<(zone&31));
                sight_zones[zone/32] |= 1<<(zone&31);
            }
        } catch (GameActionException e) {
            e.printStackTrace();
        }
    }

    static MapLocation findNearbyUnexploredZone() {
        int zoneX = findZoneX(pos.x); // entre 0 i 20
        int zoneY = findZoneY(pos.y); // entre 0 i 20
        System.out.println("Xbounds:"+zoneXmin+","+zoneXmax+", Ybounds" + zoneYmin+","+zoneYmax);
        System.out.println("now:"+zoneX+","+zoneY);
        float x = findX(zoneX);float y = findY(zoneY);
        rc.setIndicatorLine(new MapLocation(x-5, y-5), new MapLocation(x-5, y+5), 255,255,255);
        rc.setIndicatorLine(new MapLocation(x+5, y-5), new MapLocation(x+5, y+5), 255,255,255);
        rc.setIndicatorLine(new MapLocation(x-5, y-5), new MapLocation(x+5, y-5), 255,255,255);
        rc.setIndicatorLine(new MapLocation(x-5, y+5), new MapLocation(x+5, y+5), 255,255,255);
        for (int dx = -1; dx <= 1; dx++) {
            int newZoneX = zoneX+dx;
            if (newZoneX >= zoneXmax || newZoneX <= zoneXmin) continue;
            for (int dy = -1; dy <= 1; ++dy) {
                if (dx == 0 && dy == 0) continue;
                int newZoneY = zoneY+dy;
                if (newZoneY >= zoneYmax || newZoneY <= zoneYmin) continue;
                int newZone = (newZoneX)*21+(newZoneY);
                if ((sight_zones[newZone/32] & 1<<(newZone&31)) == 0) {
                    MapLocation newTarget = new MapLocation(findX(newZoneX), findY(newZoneY));
                    System.out.println("zoneY:" + zoneY + ", newZoneY:" + newZoneY + ", zoneYmin:" + zoneYmin + "("+dx+","+dy+")");
                    rc.setIndicatorLine(pos, newTarget, 0, 0, 0);
                    return newTarget;
                }
            }
        }
        return null;
    }

    static int findZoneX (float x) {
        return Math.round((Math.round(x)-xBase+100)/10);
    }

    static float findX (int zoneX) {
        return zoneX*10f+5f-100f+(float)xBase;
    }

    static int findZoneY (float y) {
        return Math.round((Math.round(y)-yBase+100)/10);
    }

    static float findY (int zoneY) {
        return zoneY*10f+5f-100f+(float)yBase;
    }

    static int onceMod(int n, int p) {
        if (n >= p) return n-p;
        return n;
    }

}
