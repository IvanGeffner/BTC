package UltimateplayerDodge;

import battlecode.common.*;


/**
 * Created by Ivan on 1/9/2017.
 */
public class Scout {

    static RobotController rc;

    static MapLocation realTarget;

    static MapLocation randomTarget;

    static Direction currentDirection;

    static int escaping;

    static int initialMessage;

    static MapLocation base;
    static int xBase, yBase;

    //static HashSet<Integer> readMes;

    static int round;
    static MapLocation pos;

    static int[] sight_zones = new int[14];
    static float mapUpperBound = Constants.INF, mapLowerBound = -Constants.INF, mapLeftBound = -Constants.INF, mapRightBound = Constants.INF;
    static int zoneXmax = 100, zoneXmin = -100, zoneYmax = 100, zoneYmin = -100;

    static int[] dxs = {-1,-1,-1,0,1,1,1,0};
    static int[] dys = {-1,0,1,1,1,0,-1,-1};

    @SuppressWarnings("unused")
    public static void run(RobotController rcc) {
        //code executed once at the begining

        rc = rcc;

        Initialize();

        while (true) {
            //code executed continually, don't let it end

            round = rc.getRoundNum();
            pos = rc.getLocation();
            //readMessages();
            checkMapBounds();

            Bot.shake(rc);
            Bot.donate(rc);

            try {
                //serveix per saber quants scouts tenim vius (per saber si n'hem de fer mes)
                rc.broadcast(Communication.SCOUT_LAST_TURN_ALIVE, rc.getRoundNum());
            } catch (GameActionException e) {
                e.printStackTrace();
            }
            System.out.println("0");
            MapLocation newTarget = findBestTree();
            // si no estem escapant, o si ho estem fent pero la direccio de l'arbre es semblant a la d'escapament
            if (escaping <= 0 || (newTarget != null && Math.abs(currentDirection.degreesBetween(pos.directionTo(newTarget))) < 90)) {
                updateTarget(newTarget);
            }

            updateSightZones();
            MapLocation escapePos = checkNearbyEnemies();
            if (escapePos == null) {
                //escapePos = checkNearbyScouts();
            }
            if (escapePos != null) {
                realTarget = escapePos;
                escaping = Constants.SCOUT_ESCAPE_TURNS;
                randomTarget = realTarget;
                currentDirection = pos.directionTo(realTarget);
                System.out.println("1");
            }
            if (realTarget == null && rc.getRoundNum() > 50 && escaping <= 0) {
                newTarget = findNearbyUnexploredZone();
                updateTarget(newTarget);
                System.out.println("2");
            }
            if (realTarget == null) {
                moveInYourDirection();
                System.out.println("3");
            }
            else {
                realTarget = checkInsideMap(realTarget);
                Greedy.moveGreedy(rc, realTarget, 13500);
            }

            broadcastLocations();
            escaping--;

            Clock.yield();
        }
    }

    static void Initialize(){
        currentDirection = rc.getLocation().directionTo(rc.getInitialArchonLocations(rc.getTeam().opponent())[0]);
        randomTarget = rc.getLocation();
        base = rc.getInitialArchonLocations(rc.getTeam())[0];
        xBase = Math.round(base.x);
        yBase = Math.round(base.y);
        //readMes = new HashSet<>();

        escaping = 0;
        Communication.init(rc,xBase, yBase);

        initialMessage = 0;
        try{
            initialMessage = rc.readBroadcast(Communication.MAX_BROADCAST_MESSAGE);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }



    static void moveInYourDirection(){
        try {
            if (escaping <= 0 && rc.canSenseAllOfCircle(randomTarget, rc.getType().bodyRadius) && !rc.onTheMap(randomTarget,rc.getType().bodyRadius)) {
                randomTarget = pos;
                currentDirection = currentDirection.rotateLeftRads((float) Math.PI - Constants.rotationAngle);
                Greedy.resetObstacle(rc);
                moveInYourDirection();
                return;
            }
            if (pos.distanceTo(randomTarget) < Constants.pushTarget){
                randomTarget = randomTarget.add(currentDirection, Constants.pushTarget);
                Greedy.resetObstacle(rc);
                moveInYourDirection();
                return;
            }
            if (Constants.DEBUG == 1) rc.setIndicatorDot(randomTarget, 0, 0, 255);
            Greedy.moveGreedy(rc,randomTarget, 13500);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

    }



    static MapLocation findBestTree() {
        boolean foundLumberjackAlly = false;

        RobotInfo[] Ri = rc.senseNearbyRobots(-1, rc.getTeam());
        for(RobotInfo ri : Ri)
        {
            if(ri.getType().equals(RobotType.LUMBERJACK))
            {
                foundLumberjackAlly = true;
                break;
            }
        }

        MapLocation target2 = null;
        float maxUtil = 0;
        TreeInfo[] Ti = rc.senseNearbyTrees (-1, Team.NEUTRAL);
        for (TreeInfo ti : Ti) {
            if (Clock.getBytecodesLeft() < Constants.SAFETYMARGINCHECKTREES) break;
            MapLocation treePos = ti.getLocation();
            int x = Math.round(treePos.x);
            int y = Math.round(treePos.y);
            RobotType r = ti.getContainedRobot();
            if (r != null) {
                int a = r.bulletCost;
                if (r == RobotType.ARCHON) a = 1000;
                Communication.sendMessage(Communication.TREEWITHGOODIES, x, y, a);
                if(!foundLumberjackAlly && Bot.needLumberjack(Constants.getIndex(r))) Communication.sendMessage(Communication.NEEDTROOPCHANNEL, x, y, Communication.NEEDLUMBERJACK);
            }
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
        /*try {
            int lastMessage = rc.readInfoBroadcast(Communication.MAX_BROADCAST_MESSAGE);
            for (int i = initialMessage; i != lastMessage && Clock.getBytecodesLeft() > Constants.BYTECODEPOSTMESSAGES; ) {
                int a = rc.readInfoBroadcast(i);
                workMessage(a);
                ++i;
                if (i >= Communication.MAX_BROADCAST_MESSAGE) i -= Communication.MAX_BROADCAST_MESSAGE;
            }
            initialMessage = lastMessage;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }*/
    }

    static void workMessage(int a){
        return;
    }

    static void broadcastLocations() {
        if (round != rc.getRoundNum()) return;
        RobotInfo[] Ri = rc.senseNearbyRobots(-1, rc.getTeam().opponent());

        float maxUtil2 = 0;
        MapLocation newTarget2 = null;
        int a2 = 0;

        for (RobotInfo ri : Ri) {
            if (Clock.getBytecodesLeft() < Constants.SAFETYMARGINSCOUTS) continue;
            MapLocation enemyPos = ri.getLocation();
            int x = Math.round(enemyPos.x);
            int y = Math.round(enemyPos.y);
            int a = Constants.getIndex(ri.type);
            if (a == 0) Communication.sendMessage(Communication.ENEMYGARDENERCHANNEL, x, y, 0);
            else if (a == 5) Communication.sendMessage(Communication.ENEMYGARDENERCHANNEL, x, y, 5);
            float val = enemyScore(enemyPos, a);
            if (val > maxUtil2) {
                maxUtil2 = val;
                newTarget2 = enemyPos;
                a2 = a;
            }
        }


        if (newTarget2 != null) Communication.sendMessage(Communication.ENEMYCHANNEL, Math.round(newTarget2.x), Math.round(newTarget2.y), a2);

        TreeInfo[] Ti = rc.senseNearbyTrees(-1, rc.getTeam().opponent());
        if (Ti.length > 0) {
            TreeInfo ti = Ti[0];
            if (Clock.getBytecodesLeft() < Constants.SAFETYMARGINSCOUTS) return;
            MapLocation treePos = ti.getLocation();
            int x = Math.round(treePos.x);
            int y = Math.round(treePos.y);
            Communication.sendMessage(Communication.ENEMYTREECHANNEL, x, y, 0);
        }
    }

    static float enemyScore (MapLocation m, int a){
        if (m == null) return 0;
        float d = pos.distanceTo(m);
        float s = 0;
        if (a == 5) s = 8;
        else if (a == 4) s = 20;
        else if (a == 3) s = 15;
        else if (a == 2) s = 20;
        else if (a == 1) s = 8;
        else if (a == 0) s = 15;
        return s/(1.0f + d);
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
            if (!rc.onTheMap(pos.add(dir, rc.getType().sensorRadius- Constants.eps))) {
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
        /*if (readMes.contains(m)) return;
        try {
            rc.broadcastInfo(initialMessage, m);
            ++initialMessage;
            if (initialMessage >= Communication.MAX_BROADCAST_MESSAGE)
                initialMessage -= Communication.MAX_BROADCAST_MESSAGE;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }*/
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
        //rc.setIndicatorLine(new MapLocation(x-5, y-5), new MapLocation(x-5, y+5), 255,255,255);
        //rc.setIndicatorLine(new MapLocation(x+5, y-5), new MapLocation(x+5, y+5), 255,255,255);
        //rc.setIndicatorLine(new MapLocation(x-5, y-5), new MapLocation(x+5, y-5), 255,255,255);
        //rc.setIndicatorLine(new MapLocation(x-5, y+5), new MapLocation(x+5, y+5), 255,255,255);
        int dini = (int)Math.random()*8;
        for (int d = 0; d < 8; ++d) {
            int dx = dxs[dini+d]; int dy = dys[dini+d];
            int newZoneX = zoneX+dx;
            if (newZoneX >= zoneXmax || newZoneX <= zoneXmin) continue;
            int newZoneY = zoneY+dy;
            if (newZoneY >= zoneYmax || newZoneY <= zoneYmin) continue;
            int newZone = (newZoneX)*21+(newZoneY);
            if ((sight_zones[newZone/32] & 1<<(newZone&31)) == 0) {
                MapLocation newTarget = new MapLocation(findX(newZoneX), findY(newZoneY));
                System.out.println("zoneY:" + zoneY + ", newZoneY:" + newZoneY + ", zoneYmin:" + zoneYmin + "("+dx+","+dy+")");
                if (Constants.DEBUG == 1) rc.setIndicatorLine(pos, newTarget, 0, 0, 0);
                return newTarget;
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

    private static MapLocation checkNearbyEnemies(){
        RobotInfo[] enemies = rc.senseNearbyRobots(Constants.SCOUT_RISK_DISTANCE, rc.getTeam().opponent());
        MapLocation escapePos = new MapLocation(pos.x, pos.y);
        for (RobotInfo enemy: enemies){
            if (enemy.getType() == RobotType.ARCHON || enemy.getType() == RobotType.GARDENER
                    || enemy.getType() == RobotType.SCOUT) continue;
            Direction enemyDir = enemy.getLocation().directionTo(pos);
            escapePos = escapePos.add(enemyDir, 1f/(1f + pos.distanceTo(enemy.getLocation())));
        }
        if (pos.isWithinDistance(escapePos, Constants.eps)) return null;
        escapePos = pos.add(pos.directionTo(escapePos), Constants.pushTarget);
        if (Constants.DEBUG == 1) rc.setIndicatorLine(pos, escapePos, 255,255,255);
        return escapePos;
    }

    static MapLocation checkInsideMap(MapLocation target) {
        // per evitar que intenti anar fora del mapa, fiquem topes a les coordenades del realTarget
        float dist = pos.distanceTo(target);
        float maxLeft = mapLeftBound+ Constants.PRECISION_MAP_BOUNDS+rc.getType().bodyRadius;
        float maxRight = mapRightBound- Constants.PRECISION_MAP_BOUNDS-rc.getType().bodyRadius;
        if (target.x < maxLeft) {
            target = new MapLocation(maxLeft, target.y);
        }
        else if (target.x > maxRight) {
            target = new MapLocation(maxRight, target.y);
        }
        float maxDown = mapLowerBound+ Constants.PRECISION_MAP_BOUNDS+rc.getType().bodyRadius;
        float maxUp = mapUpperBound- Constants.PRECISION_MAP_BOUNDS-rc.getType().bodyRadius;
        if (target.y < maxDown) {
            target = new MapLocation(target.x, maxDown);
        }
        else if (target.y > maxUp) {
            target = new MapLocation(target.x, maxUp);
        }
        if (dist-rc.getType().strideRadius > -Constants.eps) { // si el target d'abans estava mes lluny que l'stride
            target = pos.add(pos.directionTo(target), dist);
        }
        return target;
    }

}
