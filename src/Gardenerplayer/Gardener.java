package Gardenerplayer;

import battlecode.common.*;
import com.sun.tools.internal.jxc.ap.Const;

import java.util.HashSet;


public class Gardener {

    private static RobotController rc;

    private static MapLocation realTarget;

    private static int xBase;
    private static int yBase;
    private static MapLocation zoneBasePos;
    private static MapLocation zoneCenterPos;

    private static int zoneX = (int) Constants.INF;
    private static int zoneY = (int) Constants.INF;
    private static int[] zone = {(int) Constants.INF, (int) Constants.INF};
    private static int zoneRows = 12;
    private static int zoneColumns = 10;
    private static int zonesPerChannel = 8;

    private static int[] zoneIWant = {-1,-1};

    private static int emptyZone = 0;
    private static int busyZone = 1;
    private static int abandonedZone = 2;
    private static int outOfMapZone = 3;

    private static HashSet<MapLocation> neutralTreesInMyZone = new HashSet<>();
    private static float[] bulletTreeHP;

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
            broadcastMyZone();

            MapLocation newTarget = rc.getLocation();
            if (zone[0] == Constants.INF) {
                searchZone(); //aqui se li dona un valor a zoneIWant 99.99999999% segur
                if (zoneIWant[0] != -1){
                    newTarget = getBasePosFromZone(zoneIWant[0],zoneIWant[1]);
                }

                if (getZoneFromPos(rc.getLocation()) == zoneIWant){
                    if (getZoneTypeFromBroadcast(zoneIWant) != busyZone) {
                        zone = zoneIWant;
                        broadcastZone(zone, busyZone);
                    }else{
                        //he arribat a la zona que volia pero ara ja esta ocupada
                        zoneIWant = new int[]{-1, -1};
                    }
                }
            }else{
                updateTreeHP();
                checkNeutralTreesInZone();
                newTarget = checkNearbyEnemies();
                if (newTarget == null) newTarget = returnToZone();
                if (newTarget == null) newTarget = findLowHPTree();
                if (newTarget == null) newTarget = tryBuilding();
                if (newTarget == null) newTarget = zoneBasePos;
            }

            updateTarget(newTarget);
            waterNearbyTree();

            try {
                if (realTarget == null) {
                    rc.setIndicatorDot(rc.getLocation(), 255, 0, 0);
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
    private static void Initialize(){
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

    private static void broadcastZone(int[] z, int newZoneType){
        if (z[0] == Constants.INF) return;
        int zone_id = z[0] + zoneColumns * z[1];
        if (zone_id < 0) zone_id += zoneColumns*zoneRows;
        int channel_id = zone_id / zonesPerChannel;
        int info = (rc.getRoundNum() & 0x03) * 4 + newZoneType;
        info = info << (4* (zone_id % zonesPerChannel));
        try {
            int old_channel_info = rc.readBroadcast(channel_id + Communication.ZONE_FIRST_POSITION);
            int mask = ~((0x0F) << (4 * (zone_id % zonesPerChannel)));
            int new_channel_info = (old_channel_info & mask) + info;
            rc.broadcast(channel_id + Communication.ZONE_FIRST_POSITION, new_channel_info);
        } catch (GameActionException e) {
            e.printStackTrace();
        }
    }

    private static int getZoneTypeFromBroadcast(int[] z){
        int zone_id = z[0] + zoneColumns * z[1];
        if (zone_id < 0) zone_id += zoneColumns*zoneRows;
        int channel_id = zone_id / zonesPerChannel;
        try {
            int info = rc.readBroadcast(channel_id);
            info = info >> (4*(zone_id % zonesPerChannel));
            return info & 0x3;
        } catch (GameActionException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private static void broadcastMyZone(){
        if (zone[0] == Constants.INF) return;
        broadcastZone(zone, busyZone);

    }

    private static void searchZone() {
        if (zoneIWant[0] != -1) return;
        int[] ch_info = new int[Communication.ZONE_CHANNELS];
        for (int i = Communication.ZONE_FIRST_POSITION; i < Communication.ZONE_FIRST_POSITION + Communication.ZONE_CHANNELS; i++){
            try {
                ch_info[i - Communication.ZONE_FIRST_POSITION] = rc.readBroadcast(i);
            } catch (GameActionException e) {
                e.printStackTrace();
                return;
            }
        }
        int[] closest_empty_zone = {-1,-1};
        int[] myZone = getZoneFromPos(rc.getLocation());
        for (int i = 0; i < Xsorted.length; i++){
            if (i > 25 && closest_empty_zone[0] != -1){
                //nomes busquem zones abandonades fins a 25 per bytecode
                zoneIWant = closest_empty_zone;
                return;
            }
            int[] newZone = {myZone[0] + Xsorted[i], myZone[1] + Ysorted[i]};
            int new_zone_id = newZone[0] + zoneColumns * newZone[1];
            if (new_zone_id < 0) new_zone_id += zoneColumns*zoneRows;
            int channel_id = new_zone_id / zonesPerChannel;
            int info = ch_info[channel_id];
            info = info >> (4*(new_zone_id % zonesPerChannel));
            int zoneType = info & 0x3;
            int lastTurn = (info >> 2) & 0x3;
            int thisTurn = rc.getRoundNum();
            if (zoneType == busyZone){
                if ((lastTurn & 0x3) == ((thisTurn + 2) & 0x3) || ((lastTurn+3) & 0x3) == (thisTurn & 0x3) ){
                    zoneType = abandonedZone;
                    broadcastZone(newZone, abandonedZone);
                }
            }
            if (zoneType == abandonedZone){
                zoneIWant = newZone;
                return;
            }
            if (zoneType == emptyZone && closest_empty_zone[0] == -1){
                closest_empty_zone = newZone;
            }

        }
    }

    private static void waterNearbyTree(){
        if (!rc.canWater()) return;
        TreeInfo[] myTrees = rc.senseNearbyTrees(rc.getType().strideRadius);
        float minHP = Constants.INF;
        int minID = -1;
        for (TreeInfo tree: myTrees){
            if (tree.getHealth() < minHP){
                minHP = tree.getHealth();
                minID = tree.getID();
            }
        }
        if (minID != -1) try {
            rc.water(minID);
        } catch (GameActionException e) {
            e.printStackTrace();
        }
    }

    private static void updateTreeHP(){
        for (int i = 0; i < bulletTreeHP.length; i++){
            if (bulletTreeHP[i] >= 0){
                bulletTreeHP[i] -= GameConstants.BULLET_TREE_DECAY_RATE;
            }
        }
    }

    private static void checkNeutralTreesInZone(){
        TreeInfo[] neutralTrees = rc.senseNearbyTrees(rc.getType().sensorRadius,Team.NEUTRAL);
        for (TreeInfo ti: neutralTrees){
            MapLocation treeLocation = ti.getLocation();
            int[] treeZone = getZoneFromPos(treeLocation);
            if (treeZone == zone){
                if (neutralTreesInMyZone.contains(treeLocation)){
                    messageCutNeutralTree(treeLocation);
                    neutralTreesInMyZone.add(treeLocation);
                }
            }
        }
    }

    private static void messageCutNeutralTree(MapLocation treeLocation) {
        //TODO
    }

    private static MapLocation checkNearbyEnemies(){
        RobotInfo[] enemies = rc.senseNearbyRobots(rc.getType().sensorRadius, rc.getTeam().opponent());
        MapLocation myPos = rc.getLocation();
        MapLocation escapePos = rc.getLocation();
        for (RobotInfo enemy: enemies){
            if (enemy.getType() == RobotType.ARCHON || enemy.getType() == RobotType.GARDENER) continue;
            Direction enemyDir = myPos.directionTo(enemy.getLocation());
            escapePos.add(enemyDir, -1/(1 + myPos.distanceTo(enemy.getLocation())));
        }
        if (myPos.isWithinDistance(escapePos, Constants.eps)) return null;
        escapePos = escapePos.add(myPos.directionTo(escapePos), 100);
        return escapePos;
    }

    private static MapLocation returnToZone(){
        if (Math.abs(rc.getLocation().x - zoneCenterPos.x) < maxDistToCenter &&
            Math.abs(rc.getLocation().y - zoneCenterPos.y) < maxDistToCenter) return null;
        return zoneCenterPos;
    }

    private static MapLocation findLowHPTree(){
        MapLocation[] treeLocations = getTreeLocationsInZone(zone);
        for (int i = 0; i < treeLocations.length; i++){
            if (bulletTreeHP[i] < Constants.minHPGoWater && bulletTreeHP[i] >= 0) return treeLocations[i];
        }
        return null;
    }

    private static MapLocation tryBuilding(){
        try {
            int unit_index = rc.readBroadcast(Communication.UNITS_BUILT);
            int unit_to_build;
            if (unit_index < Constants.initialBuild.length){
                unit_to_build = Constants.initialBuild[unit_index];
            }else{
                int aux = unit_index - Constants.initialBuild.length;
                unit_to_build = Constants.sequenceBuild[aux%(Constants.sequenceBuild.length)];
            }
            //aixo s'ha de millorar
            if (unit_to_build == Constants.UNIT_TREE){
                return tryPlant();
            }else if (unit_to_build == Constants.UNIT_GARDENER) return null;
            else return tryBuild(unit_to_build);
        } catch (GameActionException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static MapLocation tryPlant(){
        float[] treePosX = {-3f,-1f,1f,3f,-3f,-1f,1f,3f};
        float[] treePosY = {-3.5f,-3.49f,-3.48f,-3.47f,3.47f,3.48f,3.49f,3.5f};
        MapLocation myPos = rc.getLocation();
        float minDist = Constants.INF;
        int minIndex = -1;
        for (int i = 0; i < 8; i++){
            if (bulletTreeHP[i] > 0) continue;
            //falta fer continue si la posicio de plantar esta ocupada
            MapLocation treePos = new MapLocation(zoneCenterPos.x + treePosX[i],zoneCenterPos.y + treePosY[i]);
            if (myPos.distanceTo(treePos) < minDist){
                minDist = myPos.distanceTo(treePos);
                minIndex = i;
            }
        }
        if (minIndex == -1) return null;
        MapLocation bestTreePos = new MapLocation(zoneCenterPos.x + treePosX[minIndex],zoneCenterPos.y + treePosY[minIndex]);
        if (rc.canPlantTree(myPos.directionTo(bestTreePos))){
            try {
                //Si pot plantar l'arbre, el planta i no cal que retorni cap direccio
                rc.plantTree(myPos.directionTo(bestTreePos));
                incrementUnitsBuilt();
            } catch (GameActionException e) {
                e.printStackTrace();
            }
        }else {
            Direction offsetDir;
            if (minIndex < 4) offsetDir = Direction.NORTH;
            else offsetDir = Direction.SOUTH;
            return bestTreePos.add(offsetDir,2f);
        }
        return null;
    }

    private static void incrementUnitsBuilt(){
        try {
            int units_built = rc.readBroadcast(Communication.UNITS_BUILT);
            rc.broadcast(Communication.UNITS_BUILT, units_built + 1);
        } catch (GameActionException e) {
            e.printStackTrace();
        }
    }

    private static MapLocation tryBuild(int unit_to_build){
        float buildPosX[] = {-3f,3f,-3f,3f};
        float buildPosY[] = {-1.5f,-1.5f,1.5f,1.5f};
        float newRobotPosX[] = {-4f,4f,-4f,4f};
        MapLocation myPos = rc.getLocation();
        float minDist = Constants.INF;
        int minIndex = -1;

        for (int i = 0; i < 4; i++){
            MapLocation buildingPos = new MapLocation(zoneCenterPos.x + buildPosX[i], zoneCenterPos.y + buildPosY[i]);
            if (!rc.canSenseLocation(buildingPos)) continue;
            try {
                if (rc.isLocationOccupied(buildingPos)) continue;
                if (myPos.distanceTo(buildingPos) < minDist){
                    minDist = myPos.distanceTo(buildingPos);
                    minIndex = i;
                }

            } catch (GameActionException e) {
                e.printStackTrace();
            }
        }
        if (minIndex == -1) return null;

        MapLocation bestBuildLocation = new MapLocation(zoneCenterPos.x + buildPosX[minIndex], zoneCenterPos.y + buildPosY[minIndex]);
        MapLocation newRobotLocation  = new MapLocation(zoneCenterPos.x + newRobotPosX[minIndex], zoneCenterPos.y + buildPosY[minIndex]);
        RobotType newRobotType = Constants.getRobotTypeFromIndex(unit_to_build);
        Direction buildDirection = bestBuildLocation.directionTo(newRobotLocation);

        if (myPos.distanceTo(bestBuildLocation) < Constants.eps && rc.canBuildRobot(newRobotType,buildDirection)){
            try {
                rc.buildRobot(newRobotType,buildDirection);
                incrementUnitsBuilt();
                return null;
            } catch (GameActionException e) {
                e.printStackTrace();
            }
        }
        return bestBuildLocation;
    }









    private static MapLocation[] getTreeLocationsInZone(int[] zone) {
        return null;
    }


    private static int[] getZoneFromPos(MapLocation pos){
        int[] z = {0,0};
        z[0] = (Math.round(pos.x) - xBase + 127*Constants.ModulC - 1) / Constants.ModulC;
        z[0] -= 127;
        z[1] = (Math.round(pos.y) - yBase + 127*Constants.ModulR - 5) / Constants.ModulR;
        z[1] -= 127;
        return z;
    }

    private static MapLocation getBasePosFromZone(int zx, int zy){
        return new MapLocation((float) Constants.ModulC * zx + xBase + (Constants.ModulC + 1)/2,Constants.ModulR * zy + yBase + 8);
    }



    private static MapLocation getCenterPosFromZone(int zx, int zy){
        return new MapLocation((float)Constants.ModulC * zx + xBase + (Constants.ModulC + 1)/2,Constants.ModulR * zy + yBase + 9.5f);
    }

    private static void tryWatering(){
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

    /*static boolean tryPlant(){
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
                            zoneBasePos = getBasePosFromZone(zoneX,zoneY);
                            zoneCenterPos = getCenterPosFromZone(zoneX,zoneY);
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
    }*/

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
