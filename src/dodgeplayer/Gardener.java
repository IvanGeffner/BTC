package dodgeplayer;

import battlecode.common.*;

import java.util.HashSet;

/**
 * Created by Ivan on 1/9/2017.
 */
public class Gardener {

    private static RobotController rc;

    private static MapLocation realTarget;

    private static int xBase;
    private static int yBase;
    private static MapLocation basePos;
    private static MapLocation centerPos;

    private static int zoneX = (int) Constants.INF;
    private static int zoneY = (int) Constants.INF;

    private static int whatShouldIConstruct;
    private static int whatUnitShouldIConstruct;

    private static float maxDistToCenter = 4.25f;

    private static int treeSpending;
    private static int initialMessage = 0;

    private static MapLocation treeToPlant = null;
    private static MapLocation treeToWater = null;
    private static boolean shouldMove;

    private static HashSet<Integer> readMes;

    private static int initLoopRound; //serveix per mirar que no es passi de bytecode

    private static int[] Xsorted = {0, 1, -1, 0, 0, 1, -1, -1, 1, -2, 2, 0, 0, 1, -1, -1, -2, -2, 1, 2, 2, -2, 2, 2, -2, 0, 3, 0, -3, -3, -1, 3, -1, 3, -3, 1, 1, -3, 2, -2, -3, 3, 3, 2, -2, -4, 0, 4, 0, -4, -4, 4, -1, -1, 1, 1, 4, -3, 3, 3, -3, -4, -2, 4, 4, -4, 2, 2, -2, 3, 4, 0, 3, -3, -5, 0, -3, -4, 4, -4, 5, 5, 1, -1, -1, -5, -5, 5, 1, 2, 5, 2, -2, -2, -5, 5, -5, 4, -4, -4, 4, -3, -3, 5, 3, 3, -5, -5, 5, 6, -6, 0, 0, 1, -1, 6, -6, -6, 6, 1, -1, -6, -2, -6, 2, 6, 6, 2, -2, -4, -4, -5, 5, 5, 4, -5, 4, -6, 6, -3, -3, 6, 3, 3, -6, -7, 0, 0, 7, -1, -7, -7, -5, 7, 1, 7, 1, 5, -1, 5, -5, 6, 4, -4, -4, 6, 4, -6, -6, -2, 2, 7, -2, 2, 7, -7, -7, 3, -7, 7, 3, -3, 7, -7, -3, 6, 5, -6, -6, 5, -5, -5, 6, -8, 0, 0, 8, -4, 7, 8, 4, 8, -1, -1, -4, -8, -8, 1, -7, -7, 1, 4, 7, 8, 8, 2, 2, -2, -2, -8, -8, 6, -6, -6, 6, -3, 8, -8, -3, -8, 8, 3, 3, -7, 7, -7, 7, -5, 5, 5, -5, -8, -4, 4, 8, -4, 4, -8, 8, -7, 7, 6, -7, 7, -6, -6, 6, 8, 5, -5, -8, -8, 8, -5, 5, 7, -7, -7, 7, -8, 8, 6, -8, 8, 6, -6, -6, 8, -8, 8, 7, 7, -7, -7, -8, 8, -8, -8, 8};
    private static int[] Ysorted = {0, 0, 0, -1, 1, 1, -1, 1, -1, 0, 0, 2, -2, -2, 2, -2, -1, 1, 2, -1, 1, 2, -2, 2, -2, -3, 0, 3, 0, 1, -3, 1, 3, -1, -1, -3, 3, -2, -3, 3, 2, -2, 2, 3, -3, 0, -4, 0, 4, 1, -1, 1, -4, 4, -4, 4, -1, 3, -3, 3, -3, -2, 4, 2, -2, 2, 4, -4, -4, -4, 3, -5, 4, 4, 0, 5, -4, 3, -3, -3, 0, -1, 5, 5, -5, -1, 1, 1, -5, 5, 2, -5, -5, 5, -2, -2, 2, -4, -4, 4, 4, 5, -5, -3, 5, -5, 3, -3, 3, 0, 0, -6, 6, 6, 6, -1, 1, -1, 1, -6, -6, 2, 6, -2, -6, -2, 2, 6, -6, -5, 5, 4, 4, -4, -5, -4, 5, 3, 3, -6, 6, -3, -6, 6, -3, 0, -7, 7, 0, -7, -1, 1, 5, -1, -7, 1, 7, 5, 7, -5, -5, 4, 6, -6, 6, -4, -6, 4, -4, 7, 7, -2, -7, -7, 2, 2, -2, 7, -3, 3, -7, 7, -3, 3, -7, -5, 6, 5, -5, -6, -6, 6, 5, 0, 8, -8, 0, 7, 4, -1, -7, 1, -8, 8, -7, -1, 1, -8, 4, -4, 8, 7, -4, -2, 2, 8, -8, 8, -8, 2, -2, -6, 6, -6, 6, -8, 3, 3, 8, -3, -3, -8, 8, 5, -5, -5, 5, 7, 7, -7, -7, 4, -8, 8, -4, 8, -8, -4, 4, -6, -6, 7, 6, 6, -7, 7, -7, -5, -8, 8, -5, 5, 5, -8, 8, -7, -7, 7, 7, -6, -6, -8, 6, 6, 8, 8, -8, 7, -7, -7, 8, -8, 8, -8, 7, -8, 8, -8, 8};


    public static void run(RobotController rcc) {
        rc = rcc;
        Initialize();

        while (true) {
            initLoopRound = rc.getRoundNum();

            shouldMove = true;
            treeSpending = 0;

            readMessages();
            broadcastLocations();

            tryWatering();
            updateWhatConstruct();

            if (!tryPlant()) tryConstruct();

            MapLocation newTarget = findTreeToWater();
            if (newTarget == null) {
                newTarget = findTreeToPlant();
            }

            if (newTarget == null && zoneX != Constants.INF){
                newTarget = basePos;
            }

            if (zoneX != Constants.INF){
                if (Math.abs(rc.getLocation().x - centerPos.x) > maxDistToCenter || Math.abs(rc.getLocation().y - centerPos.y) > maxDistToCenter){
                    newTarget = centerPos;
                    if (realTarget != centerPos) Greedy.resetObstacle();
                }
            }

            updateTarget(newTarget);

            try {
                if (realTarget == null) {
                    rc.setIndicatorDot(rc.getLocation(), 255, 0, 0);
                    randomMove();
                }
                else {
                    rc.setIndicatorLine(rc.getLocation(),realTarget, 0, 255, 0);
                }
            }catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }

            if (!shouldMove) realTarget = rc.getLocation();

            Greedy.moveGreedy(rc, realTarget);

            Clock.yield();
        }
    }

    //nomes es fa la primera ronda
    static void Initialize(){
        MapLocation base = rc.getInitialArchonLocations(rc.getTeam())[0];
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

    static int[] getZoneFromPos(MapLocation pos){
        int[] z = {0,0};
        z[0] = (Math.round(pos.x) - xBase + 127*Constants.ModulC - 1) / Constants.ModulC;
        z[0] -= 127;
        z[1] = (Math.round(pos.y) - yBase + 127*Constants.ModulR - 5) / Constants.ModulR;
        z[1] -= 127;
        return z;
    }

    static MapLocation getBasePosFromZone(int zx, int zy){
        return new MapLocation((float) Constants.ModulC * zx + xBase + (Constants.ModulC + 1)/2,Constants.ModulR * zy + yBase + 8);
    }


    static MapLocation getCenterPosFromZone(int zx, int zy){
        return new MapLocation((float)Constants.ModulC * zx + xBase + (Constants.ModulC + 1)/2,Constants.ModulR * zy + yBase + 9.5f);
    }

    static void tryWatering(){
        TreeInfo[] Ti = rc.senseNearbyTrees(2 + Constants.eps, rc.getTeam());
        float minHP = 1000f;
        TreeInfo t = null;
        int cont = 0;
        for (TreeInfo ti : Ti){
            if (rc.canWater(ti.getID())){
                if(ti.getHealth() < minHP) {
                    t = ti;
                    minHP = ti.getHealth();
                }
                int dif = (int)(GameConstants.BULLET_TREE_MAX_HEALTH - ti.getHealth());
                cont += dif/(int)GameConstants.WATER_HEALTH_REGEN_RATE;
            }
        }

        if (cont > 1) shouldMove = false;
        try {
            if (t != null && minHP < Constants.minHPWater) {
                rc.water(t.getID());
                return;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return;
    }

    static MapLocation findTreeToWater(){
        try {
            if (treeToWater != null) {
                if (rc.canSenseLocation(treeToWater)) {
                    TreeInfo k = rc.senseTreeAtLocation(treeToWater);
                    if (k != null) {
                        if (k.getTeam() == rc.getTeam() && k.getHealth() < Constants.minHPGoWater) {
                            return treeToWater;
                        }
                    }
                }
            }
            TreeInfo[] Ti = rc.senseNearbyTrees (-1, rc.getTeam());
            float maxDist = 1000f;
            TreeInfo m = null;
            for (TreeInfo ti : Ti) {
                if (ti.getHealth() < Constants.minHPGoWater) {
                    float d = rc.getLocation().distanceTo(ti.getLocation());
                    if (d < maxDist) {
                        m = ti;
                        maxDist = d;
                    }
                }
            }
            if (m != null){
                if (zoneX != Constants.INF){
                    int[] z = getZoneFromPos(m.getLocation());
                    if (zoneX == z[0] && zoneY == z[1]){
                        treeToWater = m.getLocation();
                        return treeToWater;
                    }else return null;
                }
                treeToWater = m.getLocation();
                return treeToWater;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    static void updateTarget(MapLocation newTarget){
        if (realTarget != null && newTarget != null && newTarget.distanceTo(realTarget) < Constants.eps) return;
        realTarget = newTarget;
        Greedy.resetObstacle();
    }

    static void tryConstruct(){
        try{
            if (!allowedToConstruct(whatUnitShouldIConstruct)) return;
            RobotType t = Constants.ProductionUnits[whatUnitShouldIConstruct];
            for (int i = 0; i < 4; ++i){
                if (rc.canBuildRobot(t, Constants.main_dirs[i])){
                    rc.buildRobot(t, Constants.main_dirs[i]);
                    updateConstruct(whatUnitShouldIConstruct);
                    return;
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    static void updateConstruct (int a){
        try {
            int x = rc.readBroadcast(Communication.unitChannels[a]);
            int ans = x;
            boolean found = false;
            if (x < Constants.IBL) {
                for (int i = x + 1; i < Constants.IBL; ++i) {
                    ++ans;
                    if (Constants.initialBuild[i] == a) {
                        found = true;
                        break;
                    }
                }
                if (!found){
                    for (int i = 0; i < Constants.SBL; ++i){
                        ++ans;
                        if (Constants.sequenceBuild[i]== a){
                            found = true;
                            break;
                        }
                    }
                    if (!found) ans = 9999;
                }
            }
            else {
                for (int i = 0; i < Constants.SBL; ++i) {
                    ++ans;
                    if (Constants.sequenceBuild[(x+1+i) % Constants.SBL] == a) {
                        found = true;
                        break;
                    }
                }
                if (!found) ans = 9999;
            }

            rc.broadcast(Communication.unitChannels[a], ans);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    static boolean allowedToConstruct(int a){
        try {
            float totalMoney = rc.getTeamBullets() - treeSpending;

            totalMoney -= computeHowManyBehind(0, a);

            if (totalMoney >= Constants.ProductionUnits[a].bulletCost) return true;
            return false;


        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    static int computeHowManyBehind(int a, int b) {
        try {
            int x = rc.readBroadcast(Communication.unitChannels[a]);
            int y = rc.readBroadcast(Communication.unitChannels[b]);
            if(y < x) return 0;
            int ans = 0;
            if(y < Constants.IBL){
                for (int i = x; i < y; ++i) if (Constants.initialBuild[i] == a) ++ ans;
            } else if (x < Constants.IBL) {
                int totalInSequence = 0;
                int totalOffSet = 0;
                for (int i = x; i < Constants.IBL; ++i) if (Constants.initialBuild[i] == a) ++ ans;
                for (int i = 0; i < Constants.SBL; ++i){
                    if (Constants.sequenceBuild[i] == a){
                        ++totalInSequence;
                        if (i < y% Constants.SBL) ++totalOffSet;
                    }
                }
                ans += totalOffSet + totalInSequence*((y - Constants.IBL)/ Constants.SBL);
            } else {
                int totalInSequence = 0;
                int totalOffSet = 0;
                for (int i = 0; i < Constants.SBL; ++i) {
                    if (Constants.sequenceBuild[i] == a) {
                        ++totalInSequence;
                    }
                }
                int z = y % Constants.SBL;
                for (int i = x; true ;++i){
                    int realI = i% Constants.SBL;
                    if (realI == z) break;
                    if (Constants.sequenceBuild[realI] == a) ++ans;
                    ++totalOffSet;
                }

                ans += ((y - x - totalOffSet)/ Constants.SBL)*totalInSequence;
            }
            if (a < 4) return ans* Constants.ProductionUnits[a].bulletCost;
            else return ans*50;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return 0;
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
        int[] m = Communication.decode(a);
        if (m[0] == Communication.PLANTTREE) treeSpending += GameConstants.BULLET_TREE_COST;
    }

    static void updateWhatConstruct(){
        try {
            int minQueue = 999999;
            for (int i = 1; i < Communication.unitChannels.length; ++i) {
                int a = rc.readBroadcast(Communication.unitChannels[i]);
                if (a < minQueue){
                    minQueue = a;
                    whatShouldIConstruct = i;
                    if (i < 5) whatUnitShouldIConstruct = i;
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    static boolean tryPlant(){
        if (whatShouldIConstruct != 5) return false;
        try {
            MapLocation pos = rc.getLocation();
            MapLocation gardenerPos = gardenerPosition();
            if (gardenerPos == null) return false;
            Direction treeDir = treeDirection(gardenerPos);
            if (treeDir!= null && pos.distanceTo(gardenerPos) < Constants.eps) {
                MapLocation treePos = pos.add(treeDir, 2.0f);
                if (!rc.isCircleOccupiedExceptByThisRobot(treePos, 1.0f)) {
                    if (rc.getTeamBullets() < GameConstants.BULLET_TREE_COST) {
                        int message = Communication.encodeFinding(Communication.PLANTTREE, 0, 0);
                        rc.broadcast(initialMessage, message);
                        ++initialMessage;
                        if (initialMessage >= Communication.MAX_BROADCAST_MESSAGE) initialMessage -= Communication.MAX_BROADCAST_MESSAGE;
                        rc.broadcast(Communication.MAX_BROADCAST_MESSAGE, initialMessage);
                        return false;
                    } else if (rc.canPlantTree(treeDir)){
                        rc.plantTree(treeDir);
                        if (zoneX == Constants.INF){
                            int z[] = getZoneFromPos(treePos);
                            zoneX = z[0];
                            zoneY = z[1];
                            basePos = getBasePosFromZone(zoneX,zoneY);
                            centerPos = getCenterPosFromZone(zoneX,zoneY);
                            System.out.println("assignen la zone " + zoneX + "  " + zoneY);
                        }
                        updateConstruct(5);
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    static MapLocation findTreeToPlant(){
        if (treeToPlant != null) {
            int tx = Math.round(treeToPlant.x);
            int ty = Math.round(treeToPlant.y);
            int tyy = ty + 1000* Constants.ModulR - yBase;
            boolean posFound = false;
            if (tyy % (Constants.ModulR) == Constants.SouthTree - 2){
                ty += 2;
                posFound = true;
            }
            else if (tyy % (Constants.ModulR) == (Constants.NorthTree + 2)% Constants.ModulR){
                ty -= 2;
                posFound = true;
            }
            if (posFound) {
                MapLocation m = emptySpot(tx, ty);
                if (m != null) return m;
            }
        }

        MapLocation pos = rc.getLocation();
        int x = Math.round(pos.x);
        int y = Math.round(pos.y);
        for (int i = 0; i < Xsorted.length && Clock.getBytecodesLeft() > Constants.TREEBUCLEBYTE; ++i){
            int xx = x+Xsorted[i];
            int yy = y+Ysorted[i];
            MapLocation m = emptySpot(xx,yy);
            if (m != null) {
                int[] z = getZoneFromPos(m);
                if (zoneX != Constants.INF){
                    //System.out.println("zone = " + zoneX + zoneY + ", z = "+ z[0] + " " + z[1]);
                    if (zoneX == z[0] && zoneY == z[1]) {
                        treeToPlant = m;
                        return m;
                    }
                    return null;
                }
                treeToPlant = m;
                return m;
            }
        }
        return null;
    }

    static MapLocation emptySpot (int x, int y){
        int a = x + 1000* Constants.ModulC - xBase;
        int b = y + 1000* Constants.ModulR - yBase;
        int amod = a % Constants.ModulC;
        int bmod = b % Constants.ModulR;
        if (amod%2 == 1 || amod <= 2 || amod >= (Constants.ModulC) - 2) return null;
        if (bmod%2 == 1 || bmod <= 2 || bmod >= (Constants.ModulR) - 2) return null;
        float extraY = (amod -2) * 0.02f;
        float extraextraY = 2.0f;
        if (bmod == Constants.SouthTree){
            extraY = -extraY;
            extraextraY = -extraextraY;
        }
        MapLocation treePos = new MapLocation (x, y+extraY);
        MapLocation plantPos = new MapLocation (x, y + extraY + extraextraY);
        try {
            if (!rc.canSenseAllOfCircle(treePos, 1.0f)) return null;
            if (!rc.canSenseAllOfCircle(plantPos, 1.0f)) return null;
            if (rc.isCircleOccupiedExceptByThisRobot(treePos, 1.0f)) return null;
            if (!rc.onTheMap(treePos, 1.0f)) return null;
            if (rc.isCircleOccupiedExceptByThisRobot(plantPos, 1.0f)) return null;
            if (!rc.onTheMap(plantPos, 1.0f)) return null;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return plantPos;
    }

    static Direction treeDirection(MapLocation m){
        if (m == null) return null;
        int ty = (int) Math.round(m.y);
        int tyy = ty + 1000* Constants.ModulR - yBase;
        if (tyy % (Constants.ModulR) == Constants.SouthTree - 2){
            return new Direction(0f, 1f);
        }
        else if (tyy % (Constants.ModulR) == (Constants.NorthTree + 2)% Constants.ModulR){
            return new Direction (0f, -1f);
        }
        return null;
    }

    static MapLocation gardenerPosition(){
        MapLocation pos = rc.getLocation();
        int x = Math.round(pos.x);
        int y = Math.round(pos.y);
        int yy = y + 1000* Constants.ModulR - yBase;
        if (yy % (Constants.ModulR) == Constants.SouthTree - 2) y += 2;
        else if (yy % (Constants.ModulR) == (Constants.NorthTree + 2)% Constants.ModulR) y -=2;
        else return null;
        return emptySpot(x,y);
    }

    static void randomMove(){
        try {
            int a = (int) Math.floor(Math.random() * 4.0);
            for (int i = 0; i < 4; ++i) {
                if (rc.canMove(Constants.main_dirs[(a + i) % 4])) {
                    rc.move(Constants.main_dirs[(a + i) % 4]);
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    static void broadcastLocations() {
        if (initLoopRound != rc.getRoundNum()) return;
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
