package Bestplayer;

import battlecode.common.*;

import java.util.HashSet;


public class Gardener {

    private static RobotController rc;

    private static MapLocation realTarget;

    private static int xBase;
    private static int yBase;
    private static int zoneOriginX;
    private static int zoneOriginY;
    private static MapLocation zoneCenterPos;

    private static float STREET_WIDTH = 5f;
    private static float STREET_HEIGHT = 5f;
    private static float zoneWidth = 11f;
    private static float zoneHeight = 11f;

    private static int[] zone = {(int) Constants.INF, (int) Constants.INF};
    private static int zoneRows = 2*GameConstants.MAP_MAX_WIDTH / (int) zoneWidth; // = 2*100 / 11 = 18
    private static int zoneColumns = 2*GameConstants.MAP_MAX_HEIGHT / (int) zoneHeight; // = 18
    private static int zonesPerChannel = 6;

    private static int bitsPerZone = 5;
    private static int treesPerZone = 7;
    private static int buildPositionsPerZone = 2;

    private static float interaction_dist_from_edge = 1f;

    private static int[] zoneIWant = {-20,-20};
    private static int turnsResetZone = 3;

    private static int emptyZone = 0;
    private static int busyZone = 1;
    private static int abandonedZone = 2;
    private static int outOfMapZone = 3;

    private static HashSet<MapLocation> neutralTreesInMyZone = new HashSet<>();

    private static float maxDistToCenter = 3f;

    private static MapLocation[] treePos = new MapLocation[treesPerZone];
    private static MapLocation[] plantingPos = new MapLocation[treesPerZone];
    private static MapLocation[] wateringPos = new MapLocation[treesPerZone];
    private static MapLocation[] buildPos = new MapLocation[buildPositionsPerZone];
    private static MapLocation[] newRobotPos = new MapLocation[buildPositionsPerZone];
    private static MapLocation[] buildTankPos = new MapLocation[buildPositionsPerZone];
    private static MapLocation[] newTankPos = new MapLocation[buildPositionsPerZone];
    private static float treeHP[] = new float[treesPerZone];
    private static int indexVertexTrees[] = new int[2];


    private static float mapMinX = -Constants.INF;
    private static float mapMinY = -Constants.INF;
    private static float mapMaxX = Constants.INF;
    private static float mapMaxY = Constants.INF;

    private static int[] Xsorted = {0, -1, 1, 0, 0, 1, -1, -1, 1, -2, 2, 0, 0, 1, -1, -1, -2, -2, 1, 2, 2, -2, 2, 2, -2, 0, 3, 0, -3, -3, -1, 3, -1, 3, -3, 1, 1, -3, 2, -2, -3, 3, 3, 2, -2, -4, 0, 4, 0, -4, -4, 4, -1, -1, 1, 1, 4, -3, 3, 3, -3, -4, -2, 4, 4, -4, 2, 2, -2, 3, 4, 0, 3, -3, -5, 0, -3, -4, 4, -4, 5, 5, 1, -1, -1, -5, -5, 5, 1, 2, 5, 2, -2, -2, -5, 5, -5, 4, -4, -4, 4, -3, -3, 5, 3, 3, -5, -5, 5, 6, -6, 0, 0, 1, -1, 6, -6, -6, 6, 1, -1, -6, -2, -6, 2, 6, 6, 2, -2, -4, -4, -5, 5, 5, 4, -5, 4, -6, 6, -3, -3, 6, 3, 3, -6, -7, 0, 0, 7, -1, -7, -7, -5, 7, 1, 7, 1, 5, -1, 5, -5, 6, 4, -4, -4, 6, 4, -6, -6, -2, 2, 7, -2, 2, 7, -7, -7, 3, -7, 7, 3, -3, 7, -7, -3, 6, 5, -6, -6, 5, -5, -5, 6, -8, 0, 0, 8, -4, 7, 8, 4, 8, -1, -1, -4, -8, -8, 1, -7, -7, 1, 4, 7, 8, 8, 2, 2, -2, -2, -8, -8, 6, -6, -6, 6, -3, 8, -8, -3, -8, 8, 3, 3, -7, 7, -7, 7, -5, 5, 5, -5, -8, -4, 4, 8, -4, 4, -8, 8, -7, 7, 6, -7, 7, -6, -6, 6, 8, 5, -5, -8, -8, 8, -5, 5, 7, -7, -7, 7, -8, 8, 6, -8, 8, 6, -6, -6, 8, -8, 8, 7, 7, -7, -7, -8, 8, -8, -8, 8};
    private static int[] Ysorted = {0, 0, 0, -1, 1, 1, -1, 1, -1, 0, 0, 2, -2, -2, 2, -2, -1, 1, 2, -1, 1, 2, -2, 2, -2, -3, 0, 3, 0, 1, -3, 1, 3, -1, -1, -3, 3, -2, -3, 3, 2, -2, 2, 3, -3, 0, -4, 0, 4, 1, -1, 1, -4, 4, -4, 4, -1, 3, -3, 3, -3, -2, 4, 2, -2, 2, 4, -4, -4, -4, 3, -5, 4, 4, 0, 5, -4, 3, -3, -3, 0, -1, 5, 5, -5, -1, 1, 1, -5, 5, 2, -5, -5, 5, -2, -2, 2, -4, -4, 4, 4, 5, -5, -3, 5, -5, 3, -3, 3, 0, 0, -6, 6, 6, 6, -1, 1, -1, 1, -6, -6, 2, 6, -2, -6, -2, 2, 6, -6, -5, 5, 4, 4, -4, -5, -4, 5, 3, 3, -6, 6, -3, -6, 6, -3, 0, -7, 7, 0, -7, -1, 1, 5, -1, -7, 1, 7, 5, 7, -5, -5, 4, 6, -6, 6, -4, -6, 4, -4, 7, 7, -2, -7, -7, 2, 2, -2, 7, -3, 3, -7, 7, -3, 3, -7, -5, 6, 5, -5, -6, -6, 6, 5, 0, 8, -8, 0, 7, 4, -1, -7, 1, -8, 8, -7, -1, 1, -8, 4, -4, 8, 7, -4, -2, 2, 8, -8, 8, -8, 2, -2, -6, 6, -6, 6, -8, 3, 3, 8, -3, -3, -8, 8, 5, -5, -5, 5, 7, 7, -7, -7, 4, -8, 8, -4, 8, -8, -4, 4, -6, -6, 7, 6, 6, -7, 7, -7, -5, -8, 8, -5, 5, 5, -8, 8, -7, -7, 7, 7, -6, -6, -8, 6, 6, 8, 8, -8, 7, -7, -7, 8, -8, 8, -8, 7, -8, 8, -8, 8};


    //static int whatShouldIConstruct;
    //static int whatUnitShouldIConstruct;

    //static int treeSpending;
    //static int initialMessage = 0;


    public static void run(RobotController rcc) {
        rc = rcc;
        Initialize();
        while (true) {
            broadcastMyZone();
            //System.out.println("despres de broadcast " + Clock.getBytecodeNum());
            MapLocation newTarget;
            //treeSpending = 0;
            //readMessages();
            //updateWhatConstruct();
            //tryConstruct();
            newTarget = checkNearbyEnemies();
            if (newTarget != null){
                System.out.println("Fuig de " + rc.getLocation() + " a " + newTarget);
                if (Constants.DEBUG == 1) rc.setIndicatorLine(rc.getLocation(),newTarget, 255, 0, 0);
            }else if (zone[0] == Constants.INF) {
                if (rc.getRoundNum() % turnsResetZone == 0) zoneIWant[0] = zoneIWant[1] = -20;
                searchZone(); //aqui se li dona un valor a zoneIWant 99.99999999% segur
                //System.out.println("He decidit anar a " + zoneIWant[0] + "," + zoneIWant[1] + "  " + getCenterPosFromZone(zoneIWant));
                //System.out.println("despres de searchzone " + Clock.getBytecodeNum());
                if (zoneIWant[0] != -20) {
                    newTarget = getCenterPosFromZone(zoneIWant);
                    System.out.println("Va a zona " + zoneIWant[0] + "," + zoneIWant[1] + "  " + rc.getLocation() + " a " + newTarget + ", " + isZoneInMap(zoneIWant));
                    if (Constants.DEBUG == 1) rc.setIndicatorLine(rc.getLocation(), newTarget, 255, 255, 255);
                }
                //System.out.println("Soc a la zona "+ getZoneFromPos(rc.getLocation())[0] + "," + getZoneFromPos(rc.getLocation())[1] + " i vull anar a "+zoneIWant[0] + "," + zoneIWant[1]);
                checkIfArrivedToZone();
                tryConstruct();
            } else {
                updateTreeHP();
                checkNeutralTreesInZone();
                newTarget = returnToZone();
                if (newTarget != null) {
                    broadcastZone(zone, abandonedZone);
                    zone = new int[] {(int) Constants.INF, (int) Constants.INF};
                    System.out.println("Retorna: de " + rc.getLocation() + " a " + newTarget);
                    if (Constants.DEBUG == 1) rc.setIndicatorLine(rc.getLocation(), newTarget, 255, 220, 28);
                } else {
                    newTarget = findLowHPTree();
                    if (newTarget != null) {
                        System.out.println("Rega, de " + rc.getLocation() + " a " + newTarget);
                        if (Constants.DEBUG == 1) rc.setIndicatorLine(rc.getLocation(), newTarget, 0, 119, 255);
                    } else {
                        newTarget = tryPlanting();
                        if (newTarget != null){
                            System.out.println("Va a plantar " + rc.getLocation() + " a " + newTarget);
                            if (Constants.DEBUG == 1) rc.setIndicatorLine(rc.getLocation(), newTarget, 0, 255, 0);
                        }else {
                            newTarget = tryConstruct();
                            if (newTarget != null){
                                System.out.println("Va a construir " + rc.getLocation() + " a " + newTarget);
                            }else {
                                System.out.println("No tinc res a fer");
                                newTarget = zoneCenterPos;
                                //Greedy.resetObstacle(rc);
                                if (Constants.DEBUG == 1) rc.setIndicatorLine(rc.getLocation(), newTarget, 0, 0, 0);
                            }
                        }
                    }
                }
            }
            //System.out.println("despres de decidir tot " + Clock.getBytecodeNum());
            updateMapBounds();
            updateTarget(newTarget);
            waterNearbyTree();
            if (realTarget == null) {
                if (Constants.DEBUG == 1) rc.setIndicatorDot(rc.getLocation(), 255, 0, 0);
            }else if (realTarget.distanceTo(rc.getLocation()) < Constants.eps){
                Greedy.moveToSelf(rc,Clock.getBytecodesLeft() - 500);
            } else Greedy.moveGreedy(rc, realTarget, Clock.getBytecodesLeft() - 500);
            Clock.yield();
        }
    }


    //nomes es fa la primera ronda
    private static void Initialize(){
        MapLocation base = rc.getInitialArchonLocations(rc.getTeam())[0];
        xBase = Math.round(base.x);
        yBase = Math.round(base.y);
        Communication.setBase(xBase,yBase);
        try {
            int xOrigin = rc.readBroadcast(Communication.ZONE_ORIGIN_X);
            if (xOrigin == 0){
                rc.broadcast(Communication.ZONE_ORIGIN_X, Math.round(rc.getLocation().x));
                rc.broadcast(Communication.ZONE_ORIGIN_Y, Math.round(rc.getLocation().y));
                zoneOriginX = Math.round(rc.getLocation().x);
                zoneOriginY = Math.round(rc.getLocation().y);
            }else{
                zoneOriginX = xOrigin;
                zoneOriginY = rc.readBroadcast(Communication.ZONE_ORIGIN_Y);
            }
        } catch (GameActionException e) {
            e.printStackTrace();
        }
    }

    private static int getZoneID(int[] z){
        //rang de zoneid:
        // inici: -9 + 18*(-9) + 18*18/2 + 9 = 0
        // final: 9 + 18*9 + 18*18/2 + 9 = 342
        // amb 6 zones per canal fan falta 57 canals
        return z[0] + zoneColumns * z[1] + zoneColumns*zoneRows/2 + 9;
    }

    private static int[] readZoneBroadcast(int[] z){
        if (z[0] == Constants.INF) return null;
        int zone_id =  getZoneID(z);
        int channel_id = zone_id / zonesPerChannel;
        try {
            int info = rc.readBroadcast(Communication.ZONE_FIRST_POSITION + channel_id);
            info = (info >> (bitsPerZone * (zone_id % zonesPerChannel))) & 0x1F;
            //System.out.println("read channel " + channel_id + ": " + Integer.toBinaryString(info) + "  " + Integer.toBinaryString(rc.readBroadcast(channel_id)));
            int zoneType = info & 0x7;
            int lastTurn = (info >> 3) & 0x3;
            return new int[]{zoneType, lastTurn};
        } catch (GameActionException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void broadcastZone(int[] z, int newZoneType){
        if (z[0] == Constants.INF) return;
        int zone_id = getZoneID(z);
        //System.out.println("Es marca la zone " + zone_id + " com a " + newZoneType);
        int channel_id = zone_id / zonesPerChannel;
        int info = (rc.getRoundNum() & 0x03) * 8 + (newZoneType & 0x7);
        info = info << (bitsPerZone* (zone_id % zonesPerChannel));
        //System.out.println(Integer.toBinaryString(info));
        try {
            int old_channel_info = rc.readBroadcast(channel_id + Communication.ZONE_FIRST_POSITION);
            int mask = ~((0x1F) << (bitsPerZone * (zone_id % zonesPerChannel)));
            //System.out.println(Integer.toBinaryString(mask));
            int new_channel_info = (old_channel_info & mask) + info;
            //System.out.println("Write channel " + channel_id + ":");
            //System.out.println("  old " + Integer.toBinaryString(old_channel_info));
            //System.out.println("  mask " + Integer.toBinaryString(mask));
            //System.out.println("  info " + Integer.toBinaryString(info));
            //System.out.println("  new " + Integer.toBinaryString(new_channel_info));
            rc.broadcast(channel_id + Communication.ZONE_FIRST_POSITION, new_channel_info);
        } catch (GameActionException e) {
            e.printStackTrace();
        }
    }

    private static void broadcastZoneLimit(int channel, int value){
        try {
            int old_value = rc.readBroadcast(channel);
            int new_value;
            if (channel == Communication.MAX_ZONE_X || channel == Communication.MAX_ZONE_Y){
                new_value = Math.min(old_value,value - Communication.ZONE_LIMIT_OFFSET);
                rc.broadcast(channel, new_value);
            }else{
                new_value = Math.max(old_value,value + Communication.ZONE_LIMIT_OFFSET);
                rc.broadcast(channel, new_value);
            }
            //System.out.println(old_value + " -> " + new_value + ", " +value);
        } catch (GameActionException e) {
            e.printStackTrace();
        }
    }

    private static int getZoneLimitFromBroadcast(int channel){
        try {
            int raw_value = rc.readBroadcast(channel);
            if (channel == Communication.MAX_ZONE_X || channel == Communication.MAX_ZONE_Y){
                return raw_value + Communication.ZONE_LIMIT_OFFSET;
            }else{
                return raw_value - Communication.ZONE_LIMIT_OFFSET;
            }
        } catch (GameActionException e) {
            e.printStackTrace();
        }
        return (int) Constants.INF;
    }

    private static int getZoneTypeFromBroadcast(int[] z){
        int[] info = readZoneBroadcast(z);
        if (info == null) return -1;
        return info[0];
    }

    private static void broadcastMyZone(){
        if (zone[0] == Constants.INF) return;
        broadcastZone(zone, busyZone);
    }

    private static void assignZone(int[] assignedZone){
        float[] treeOffsetX = {-2.01f,0f,2.01f, -2.01f, 0f, 2.01f,2.01f};
        float[] treeOffsetY = {-2.05f,-2.05f,-2.05f,2.05f,2.05f,2.05f,0f};
        float[] plantingOffsetX = {-2.01f,0f,2.01f, -2.01f, 0f, 2.01f,0f};
        float[] plantingOffsetY = {-0.05f,-0.05f,-0.05f,0.05f,0.05f,0.05f,0f};
        float[] wateringOffsetX = {0f,0f,0f,0f,0f,0f,0f};
        float[] wateringOffsetY = {0f,0f,0f,0f,0f,0f,0f};
        float[] buildOffsetX = {0f,0f};
        float[] buildOffsetY = {0f,0f};
        float[] newRobotOffsetX= {-2f,2f};
        float[] newRobotOffsetY= {0f,0f};
        float[] buildTankOffsetX= {-2f,2f};
        float[] buildTankOffsetY= {0f,0f};
        float[] newTankOffsetX = {-5f,5f};
        float[] newTankOffsetY = {0f,0f};
        float[] treePosX = new float[treesPerZone];
        float[] treePosY = new float[treesPerZone];
        float[] plantingPosX = new float[treesPerZone];
        float[] plantingPosY = new float[treesPerZone];
        float[] wateringPosX = new float[treesPerZone];
        float[] wateringPosY = new float[treesPerZone];
        float[] buildPosX = new float[buildPositionsPerZone];
        float[] buildPosY = new float[buildPositionsPerZone];
        float[] newRobotPosX = new float[buildPositionsPerZone];
        float[] newRobotPosY = new float[buildPositionsPerZone];
        float[] buildTankPosX = new float[buildPositionsPerZone];
        float[] buildTankPosY = new float[buildPositionsPerZone];
        float[] newTankPosX = new float[buildPositionsPerZone];
        float[] newTankPosY = new float[buildPositionsPerZone];

        zone = assignedZone;
        zoneCenterPos = getCenterPosFromZone(zone);
        broadcastZone(zone, busyZone);

        try {
            if(treesPerZone == 7 && !rc.onTheMap(zoneCenterPos.add(Direction.WEST,6f))){
                treeOffsetX[6] = -2.01f;
                newRobotOffsetX[0] = 2f;
                buildTankOffsetX[0] = 2f;
                newTankOffsetX[0] = 5f;
                indexVertexTrees = new int[] {0,3};
            }else {
                newRobotOffsetX[1] = -2f;
                buildTankOffsetX[1] = -2f;
                newTankOffsetX[1] = -5f;
                indexVertexTrees = new int[] {2,5};
            }
        } catch (GameActionException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < treesPerZone; i++){
            treePosX[i] = zoneCenterPos.x + treeOffsetX[i];
            treePosY[i] = zoneCenterPos.y + treeOffsetY[i];
            treePos[i] = new MapLocation(treePosX[i], treePosY[i]);
            plantingPosX[i] = zoneCenterPos.x + plantingOffsetX[i];
            plantingPosY[i] = zoneCenterPos.y + plantingOffsetY[i];
            plantingPos[i] = new MapLocation(plantingPosX[i], plantingPosY[i]);
            wateringPosX[i] = zoneCenterPos.x + wateringOffsetX[i];
            wateringPosY[i] = zoneCenterPos.y + wateringOffsetY[i];
            wateringPos[i] = new MapLocation(wateringPosX[i], wateringPosY[i]);
            treeHP[i] = -1;
        }

        for (int i = 0; i < buildPositionsPerZone; i++){
            buildPosX[i] = zoneCenterPos.x + buildOffsetX[i];
            buildPosY[i] = zoneCenterPos.y + buildOffsetY[i];
            buildPos[i] = new MapLocation(buildPosX[i], buildPosY[i]);
            newRobotPosX[i] = zoneCenterPos.x + newRobotOffsetX[i];
            newRobotPosY[i] = zoneCenterPos.y + newRobotOffsetY[i];
            newRobotPos[i] = new MapLocation(newRobotPosX[i],newRobotPosY[i]);
            buildTankPosX[i] = zoneCenterPos.x + buildTankOffsetX[i];
            buildTankPosY[i] = zoneCenterPos.y + buildTankOffsetY[i];
            buildTankPos[i] = new MapLocation(buildTankPosX[i],buildTankPosY[i]);
            newTankPosX[i] = zoneCenterPos.x + newTankOffsetX[i];
            newTankPosY[i] = zoneCenterPos.y + newTankOffsetY[i];
            newTankPos[i] = new MapLocation(newTankPosX[i],newTankPosY[i]);
        }
    }

    private static void searchZone() {
        //System.out.println("entra search " + zoneIWant[0]+","+zoneIWant[1]);
        if (zoneIWant[0] != -20) return;

        int[] closest_empty_zone = {-20,-20};
        float minDist = Constants.INF;
        int[] myZone = getZoneFromPos(rc.getLocation());
        for (int i = 0; i < Xsorted.length; i++){
            if (i > 10 && closest_empty_zone[0] != -20){
                //nomes busquem zones abandonades fins a 25 pel bytecode
                System.out.println("closest empty zone es " + closest_empty_zone[0] + "," + closest_empty_zone[1] + " " + isZoneInMap(closest_empty_zone));
                zoneIWant = closest_empty_zone;
                return;
            }
            int[] newZone = {myZone[0] + Xsorted[i], myZone[1] + Ysorted[i]};
            if (Math.abs(newZone[0]) >= zoneColumns || Math.abs(newZone[1]) >= zoneRows) continue;
            //System.out.println("Prova la zona " + newZone[0] + "," + newZone[1] + " a " + rc.getLocation().distanceTo(getCenterPosFromZone(newZone)));
            int[] zoneInfo = readZoneBroadcast(newZone);
            if (zoneInfo == null) continue;
            int zoneType = zoneInfo[0];
            if (newZone[0] == 0 && newZone[1] == 6){
                System.out.println("Zona 0 6 type " + zoneType);
            }
            int lastTurn = zoneInfo[1];
            int thisTurn = rc.getRoundNum();
            //System.out.println("zone "+newZone[0] + "," + newZone[1] + " type " + zoneType);
            if (zoneType != outOfMapZone) {
                if (!updateZoneInMap(newZone)){
                    zoneType = outOfMapZone;
                }
            }
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
            float distToZone = rc.getLocation().distanceTo(getCenterPosFromZone(newZone));
            if (zoneType == emptyZone && distToZone < minDist){
                //System.out.println("Assigna la zone " + newZone[0] + "," + newZone[1] + " type " + zoneType + " " + isZoneInMap(newZone));
                closest_empty_zone = newZone;
                minDist = distToZone;
            }

        }
    }

    private static void updateMapBounds(){
        MapLocation myPos = rc.getLocation();
        MapLocation posN = myPos.add(Direction.NORTH,1.5f);
        MapLocation posS = myPos.add(Direction.SOUTH,1.5f);
        MapLocation posE = myPos.add(Direction.EAST,1.5f);
        MapLocation posW = myPos.add(Direction.WEST,1.5f);
        try {
            if (!rc.onTheMap(posN)){
                mapMaxY = Math.min(mapMaxY,posN.y);
            }
            if (!rc.onTheMap(posS)){
                mapMinY = Math.max(mapMinY,posS.y);
            }
            if (!rc.onTheMap(posE)){
                mapMaxX = Math.min(mapMaxX,posE.x);
            }
            if (!rc.onTheMap(posW)){
                mapMinX = Math.max(mapMinX,posW.x);
            }

        } catch (GameActionException e) {
            e.printStackTrace();
        }
    }

    private static boolean updateZoneInMap(int[] z){
        MapLocation center = getCenterPosFromZone(z);
        try {
            if (!onCurrentMap(center) || (rc.canSenseAllOfCircle(center,rc.getType().bodyRadius) && !rc.onTheMap(center, rc.getType().bodyRadius))){
                //System.out.println("Zona "+ z[0] + "," + z[1] + " fora del mapa");
                broadcastZone(z,outOfMapZone);
                if (center.x < mapMinX) {
                    broadcastZoneLimit(Communication.MIN_ZONE_X, z[0] + 1);
                }
                if (center.x > mapMaxX) {
                    broadcastZoneLimit(Communication.MAX_ZONE_X, z[0] - 1);
                }
                if (center.y < mapMinY) {
                    broadcastZoneLimit(Communication.MIN_ZONE_Y, z[1] + 1);
                }
                if (center.y > mapMaxY) {
                    broadcastZoneLimit(Communication.MAX_ZONE_Y, z[1] - 1);
                }
                return false;
            }else {
                return true;
                //System.out.println("El centre " + center + " esta dintre de:");
                //System.out.println(mapMinX + "-" + mapMaxX + ", " + mapMinY + "-" +mapMaxY);
            }
        } catch (GameActionException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static boolean isZoneInMap(int[] z){
        return !(z[0] < getZoneLimitFromBroadcast(Communication.MIN_ZONE_X) ||
                z[0] > getZoneLimitFromBroadcast(Communication.MAX_ZONE_X) ||
                z[1] < getZoneLimitFromBroadcast(Communication.MIN_ZONE_Y) ||
                z[1] > getZoneLimitFromBroadcast(Communication.MAX_ZONE_Y));
    }

    private static void checkIfArrivedToZone(){
        MapLocation centerIWant = getCenterPosFromZone(zoneIWant);
        //System.out.println("El centre esta dintre? " + onCurrentMap(centerIWant));
        if (!onCurrentMap(centerIWant)){
            zoneIWant[0] = zoneIWant[1] = -20;
            return;
        }

        if (!rc.canSenseAllOfCircle(centerIWant,rc.getType().bodyRadius)) return;
        int zoneType = getZoneTypeFromBroadcast(zoneIWant);
        try{
            if (zoneType == busyZone) {
                zoneIWant[0] = zoneIWant[1] = -20;
                return;
            }
            if (!rc.onTheMap(centerIWant,rc.getType().bodyRadius)){
                updateZoneInMap(zoneIWant);
                zoneIWant[0] = zoneIWant[1] = -20;
                return;
            }
            if (rc.isCircleOccupiedExceptByThisRobot(centerIWant,rc.getType().bodyRadius)){

            }
            //System.out.println("El punt " + centerIWant + " esta dintre el mapa");
            if (Constants.DEBUG == 1) rc.setIndicatorDot(centerIWant,255,255,255);
            if (rc.getLocation().distanceTo(centerIWant) < Constants.eps) assignZone(zoneIWant);
        }catch (GameActionException e){
            e.printStackTrace();
        }
    }

    private static void waterNearbyTree(){
        if (!rc.canWater()) return;
        //System.out.println("entra regar");
        TreeInfo[] myTrees = rc.senseNearbyTrees(rc.getType().bodyRadius + interaction_dist_from_edge, rc.getTeam());
        float minHP = Constants.INF;
        int minID = -1;
        for (TreeInfo tree: myTrees){
            if (tree.getHealth() < minHP){
                minHP = tree.getHealth();
                minID = tree.getID();
            }
        }
        if (minID != -1) try {
            //System.out.println("Rego arbre "+minID +  " amb " + rc.senseTree(minID).getHealth() + " hp");
            if (Constants.DEBUG == 1) rc.setIndicatorDot(rc.senseTree(minID).getLocation(),0, 255, 0);
            rc.water(minID);
        } catch (GameActionException e) {
            e.printStackTrace();
        }
    }

    private static void updateTreeHP(){
        //revisar que no curi arbres nous
        for (int i = 0; i < treesPerZone; i++){
            if (rc.canSenseLocation(treePos[i])){
                try {
                    TreeInfo tree = rc.senseTreeAtLocation(treePos[i]);
                    if (tree == null){
                        treeHP[i] = -1;
                    }else{
                        if (treeHP[i] > GameConstants.BULLET_TREE_MAX_HEALTH) treeHP[i] -= GameConstants.BULLET_TREE_DECAY_RATE;
                        else{
                            treeHP[i] = tree.getHealth();
                        }
                    }
                } catch (GameActionException e) {
                    e.printStackTrace();
                }
            }else{
                if (treeHP[i] >= 0 && treeHP[i] < GameConstants.BULLET_TREE_MAX_HEALTH){
                    treeHP[i] -= GameConstants.BULLET_TREE_DECAY_RATE;
                }
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
                    messageCutNeutralTree(ti.getID(),treeLocation);
                    neutralTreesInMyZone.add(treeLocation);
                }
            }
        }
    }

    private static void messageCutNeutralTree(int id, MapLocation treeLocation) {
        Communication.sendMessage(rc, Communication.CHOPCHANNEL,Math.round(treeLocation.x),Math.round(treeLocation.y),id&0xFFF);
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
        escapePos = myPos.add(myPos.directionTo(escapePos), 100);
        return escapePos;
    }

    private static MapLocation returnToZone(){
        if (Math.abs(rc.getLocation().x - zoneCenterPos.x) < maxDistToCenter &&
                Math.abs(rc.getLocation().y - zoneCenterPos.y) < maxDistToCenter) return null;
        return zoneCenterPos;
    }

    private static MapLocation findLowHPTree(){
        for (int i = 0; i < treePos.length; i++){
            if (treePos[i].x == Constants.INF) continue;
            if (rc.canWater(treePos[i]) && treeHP[i] < Constants.minHPWater) return wateringPos[i];
            //canviar-ho pel centre?
        }
        for (int i = 0; i < treePos.length; i++){
            if (treeHP[i] < Constants.minHPGoWater && treeHP[i] >= 0) return wateringPos[i];
        }
        return null;
    }
/*
    private static MapLocation tryConstructing(){
        try {
            int treesPlanted = rc.readBroadcast(Communication.TREES_BUILT);
            int robotsBuilt = rc.readBroadcast(Communication.ROBOTS_BUILT);
            MapLocation mostImportant;
            MapLocation leastImportant;
            if (treesPlanted < maxTreesBuilt(robotsBuilt)){
                //tenim permis per construir un arbre
                //System.out.println("Puc construir un arbre");
                mostImportant = tryPlant();
                leastImportant = tryBuilding();
            }else{
                //hem d'esperar a construir un robot abans de fer un arbre
                //System.out.println("No puc construir un arbre");
                mostImportant = tryBuilding();
                leastImportant = null;
            }
            if (mostImportant != null) return mostImportant;
            return leastImportant;

        } catch (GameActionException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static float maxTreesBuilt(int robotsBuilt){
        return robotsBuilt - 1;
    }
*/
    private static MapLocation tryPlanting(){
        System.out.println("Entra plantar");
        if (!allowedToConstruct(Constants.TREE)) {
            System.out.println("No tinc prou bullets per plantar");
            return null; //comprova bullets
        }
        int index = whichTreeToPlant(); //si hi ha algun arbre no ocupat
        System.out.println("Planta l'arbre " + index);
        if (index == -1) return null;
        MapLocation plantingPosition = plantingPos[index];
        MapLocation newTreePosition = treePos[index];
        Direction plantingDirection = plantingPosition.directionTo(newTreePosition);
        if (rc.getLocation().distanceTo(plantingPosition) < Constants.eps && rc.canPlantTree(plantingDirection)){
            try {
                //Si pot plantar l'arbre, el planta i no cal que retorni cap direccio
                rc.plantTree(plantingDirection);
                incrementTreesBuilt();
                updateAfterConstruct(Constants.TREE);
                treeHP[index] = GameConstants.BULLET_TREE_MAX_HEALTH + 40; //sumem 40 per tenir en compte el temps que triguen en estar full vida
            } catch (GameActionException e) {
                e.printStackTrace();
            }
            return null;
        }
        return plantingPosition;
    }

    private static int whichTreeToPlant(){
        MapLocation myPos = rc.getLocation();
        float minDist = Constants.INF;
        int minIndex = -1;
        for (int i = 0; i < treesPerZone; i++){
            if (treeHP[i] > 0) continue;
            if (!onCurrentMap(treePos[i]) || !onCurrentMap(plantingPos[i])) {
                //System.out.println("arbre " + i + " fora del mapa");
                continue;
            }
            if (i == 6){
                if (treeHP[indexVertexTrees[0]] == -1 || treeHP[indexVertexTrees[1]] == -1) continue;
            }
            try {
                if (rc.canSenseAllOfCircle(treePos[i],GameConstants.BULLET_TREE_RADIUS)){
                    if (!rc.onTheMap(treePos[i], GameConstants.BULLET_TREE_RADIUS) ||
                            rc.isCircleOccupiedExceptByThisRobot(treePos[i],GameConstants.BULLET_TREE_RADIUS)) continue;
                }
                if (rc.canSenseAllOfCircle(plantingPos[i],rc.getType().bodyRadius))
                    if (!rc.onTheMap(plantingPos[i], rc.getType().bodyRadius) ||
                            rc.isCircleOccupiedExceptByThisRobot(plantingPos[i],GameConstants.BULLET_TREE_RADIUS)) continue;

                if (myPos.distanceTo(treePos[i]) < minDist){
                    minDist = myPos.distanceTo(treePos[i]);
                    minIndex = i;
                }
            } catch (GameActionException e) {
                e.printStackTrace();
            }
        }
        return minIndex;
    }

    private static void incrementTreesBuilt(){
        try {
            int trees_built = rc.readBroadcast(Communication.TREES_BUILT);
            rc.broadcast(Communication.TREES_BUILT, trees_built + 1);
        } catch (GameActionException e) {
            e.printStackTrace();
        }
    }

    private static MapLocation tryConstruct(){
        System.out.println("Entra construct");
        int smallUnit = bestSmallUnitToBuild();
        int firstUnit = -1;
        int secondUnit = -1;
        try {
            int tankIndex = rc.readBroadcast(Communication.unitChannels[Constants.TANK]);
            int smallUnitIndex = rc.readBroadcast(Communication.unitChannels[smallUnit]);
            System.out.println("tankindex "+ tankIndex + " unitindex " + smallUnitIndex);
            if (tankIndex < smallUnitIndex) {
                firstUnit = Constants.TANK;
                secondUnit = smallUnit;
            }else{
                firstUnit = smallUnit;
                secondUnit = Constants.TANK;
            }
        } catch (GameActionException e) {
            e.printStackTrace();
        }
        System.out.println("Tria les units " + firstUnit + ", " + secondUnit);
        MapLocation firstTry = tryConstructUnit(firstUnit);
        if (firstTry != null) return firstTry;
        return tryConstructUnit(secondUnit);
    }

    private static MapLocation tryConstructUnit(int unit){
        if (unit == -1) return null;
        if (!allowedToConstruct(unit)) {
            System.out.println("No tinc prou bales per construir " + unit);
            System.out.println("Tinc " + rc.getTeamBullets() + " i calen " + totalBulletCost(unit));
            return null;
        }
        MapLocation buildingPosition;
        MapLocation newRobotPosition;
        if (zone[0] == Constants.INF){
            buildingPosition = rc.getLocation();
            newRobotPosition = getBuildPositionWithoutZone(unit);
            if (newRobotPosition == null) return null;
        }else{
            int index = whichPositionToBuildInZone(unit);
            if (index == -1) return null;
            buildingPosition = buildPos[index];
            newRobotPosition = newRobotPos[index];
        }
        Direction buildDirection = buildingPosition.directionTo(newRobotPosition);
        RobotType newRobotType = Constants.getRobotTypeFromIndex(unit);
        if (rc.getLocation().distanceTo(buildingPosition) < Constants.eps && rc.canBuildRobot(newRobotType,buildDirection)){
            try {
                rc.buildRobot(Constants.getRobotTypeFromIndex(unit),rc.getLocation().directionTo(newRobotPosition));
                incrementRobotsBuilt();
                updateAfterConstruct(unit);
                return rc.getLocation();
            } catch (GameActionException e) {
                e.printStackTrace();
            }
            return null;
        }else return buildingPosition;
    }

    private static MapLocation getBuildPositionWithoutZone(int unit){
        Direction back = rc.getLocation().directionTo(getCenterPosFromZone(zoneIWant)).opposite();
        RobotType type = Constants.getRobotTypeFromIndex(unit);
        for (int i = 0; i < 10; i++){
            Direction dirBuild = back.rotateRightRads(i*(float)Math.PI/20);
            if (rc.canBuildRobot(type,dirBuild)) return rc.getLocation().add(dirBuild,rc.getType().bodyRadius + type.bodyRadius);
            Direction dirBuildInv = back.rotateLeftRads(i*(float)Math.PI/20);
            if (rc.canBuildRobot(type,dirBuildInv)) return rc.getLocation().add(dirBuildInv,rc.getType().bodyRadius + type.bodyRadius);
        }
        return null;
    }

    private static int whichPositionToBuildInZone(int smallUnit){
        if (smallUnit == Constants.GARDENER) return -1;
        MapLocation myPos = rc.getLocation();
        MapLocation myBuildingPos[];
        MapLocation robotSpawnPos[];
        float newRobotRadius;
        if (smallUnit == Constants.TANK){
            myBuildingPos = buildTankPos;
            robotSpawnPos = newTankPos;
            newRobotRadius = RobotType.TANK.bodyRadius;
        }else{
            myBuildingPos = buildPos;
            robotSpawnPos = newRobotPos;
            newRobotRadius = RobotType.SOLDIER.bodyRadius;
        }
        float minDist = Constants.INF;
        int minIndex = -1;

        for (int i = 0; i < buildPositionsPerZone; i++){
            if (!onCurrentMap(myBuildingPos[i]) || !onCurrentMap(robotSpawnPos[i])) {
                //System.out.println(i + " fora del mapa");
                if (Constants.DEBUG == 1) rc.setIndicatorDot(myBuildingPos[i], 255,0,0);
                continue;
            }
            try {
                if (rc.canSenseAllOfCircle(myBuildingPos[i],rc.getType().bodyRadius)){
                    if (!rc.onTheMap(myBuildingPos[i], rc.getType().bodyRadius) ||
                            rc.isCircleOccupiedExceptByThisRobot(myBuildingPos[i],rc.getType().bodyRadius)) {
                        //System.out.println("mybuildingpos bloquejada "+ myBuildingPos[i]);
                        continue;
                    }
                }
                if (rc.canSenseAllOfCircle(robotSpawnPos[i],newRobotRadius)){
                    if (!rc.onTheMap(robotSpawnPos[i], newRobotRadius) ||
                            rc.isCircleOccupiedExceptByThisRobot(robotSpawnPos[i],newRobotRadius)) {
                        //System.out.println("newrobotpos bloquejada "+ robotSpawnPos[i]);
                        continue;
                    }
                }
                if (myPos.distanceTo(myBuildingPos[i]) < minDist){
                    minDist = myPos.distanceTo(myBuildingPos[i]);
                    minIndex = i;
                }
            } catch (GameActionException e) {
                e.printStackTrace();
            }
        }
        //System.out.println("min index de build = "+minIndex);
        return minIndex;
    }


/*
    //old
    private static MapLocation tryPlant(){
        //System.out.println("intenta plantar");
        MapLocation myPos = rc.getLocation();
        float minDist = Constants.INF;
        int minIndex = -1;
        for (int i = 0; i < treesPerZone; i++){
            if (treeHP[i] > 0) continue;
            if (!onCurrentMap(treePos[i]) || !onCurrentMap(plantingPos[i])) {
                //System.out.println("arbre " + i + " fora del mapa");
                continue;
            }
            if (i == 6){
                if (treeHP[indexVertexTrees[0]] == -1 || treeHP[indexVertexTrees[1]] == -1) continue;
            }
            try {
                if (rc.canSenseAllOfCircle(treePos[i],GameConstants.BULLET_TREE_RADIUS)){
                    if (!rc.onTheMap(treePos[i], GameConstants.BULLET_TREE_RADIUS) ||
                            rc.isCircleOccupiedExceptByThisRobot(treePos[i],GameConstants.BULLET_TREE_RADIUS)) continue;
                }
                if (rc.canSenseAllOfCircle(plantingPos[i],rc.getType().bodyRadius))
                    if (!rc.onTheMap(plantingPos[i], rc.getType().bodyRadius) ||
                            rc.isCircleOccupiedExceptByThisRobot(plantingPos[i],GameConstants.BULLET_TREE_RADIUS)) continue;

                if (myPos.distanceTo(treePos[i]) < minDist){
                    minDist = myPos.distanceTo(treePos[i]);
                    minIndex = i;
                }
            } catch (GameActionException e) {
                e.printStackTrace();
            }
        }

        if (minIndex == -1) {
            //System.out.println("no te cap lloc per plantar");
            return null;
        }
        MapLocation bestTreePos = treePos[minIndex];
        if (rc.getLocation().distanceTo(plantingPos[minIndex]) < Constants.eps && rc.canPlantTree(myPos.directionTo(bestTreePos))){
            try {
                //Si pot plantar l'arbre, el planta i no cal que retorni cap direccio
                rc.plantTree(myPos.directionTo(bestTreePos));
                incrementTreesBuilt();
                treeHP[minIndex] = GameConstants.BULLET_TREE_MAX_HEALTH + 40; //sumem 40 per tenir en compte el temps que triguen en estar full vida
            } catch (GameActionException e) {
                e.printStackTrace();
            }
        }else return plantingPos[minIndex];
        return null;
    }

    private static MapLocation tryBuilding(){
        //System.out.println("Entra trybuild");
        try {
            int index = rc.readBroadcast(Communication.ROBOTS_BUILT);
            RobotType type = whichRobotToBuild(index);
            if (zone[0] == Constants.INF){
                while (type == RobotType.GARDENER) type = whichRobotToBuild(++index);
                buildWithoutZone(type);
                return null;
            }else{
                return buildInZone(type);
            }
/*
            int accBulletCost = 0;
            int tries = 3;
            for (int i = 0; i < tries; i++){
                RobotType type = whichRobotToBuild(index + i);
                if (type == RobotType.GARDENER){
                    index++;
                    type = whichRobotToBuild(index + i);
                    incrementRobotsBuilt();
                }
                System.out.println("Vol construir " + accBulletCost + "  " + i);
                if (rc.getTeamBullets() < accBulletCost*0.8 + type.bulletCost) return null;
                accBulletCost += type.bulletCost;
                MapLocation buildPos;
                if (zone[0] == Constants.INF) {
                    buildWithoutZone(type);
                    buildPos = null;
                } else buildPos = buildInZone(type);
                if (buildPos != null) return buildPos;
            }*/
/*
        } catch (GameActionException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static RobotType whichRobotToBuild(int index){
        int unit_to_build;
        if (index < Constants.initialBuild.length){
            unit_to_build = Constants.initialBuild[index];
        }else{
            int aux = index - Constants.initialBuild.length;
            unit_to_build = Constants.sequenceBuild[aux%(Constants.sequenceBuild.length)];
        }
        //System.out.println("index " + index + "="+Constants.getRobotTypeFromIndex(unit_to_build));
        return Constants.getRobotTypeFromIndex(unit_to_build);
    }

    private static MapLocation buildInZone(RobotType robotType){
        //System.out.println("Vol construir un " + robotType);
        //System.out.println("Tinc " + rc.getTeamBullets() + " bales");
        if (robotType == RobotType.GARDENER) return null;
        MapLocation myPos = rc.getLocation();
        MapLocation myBuildingPos[];
        MapLocation robotSpawnPos[];
        float newRobotRadius;
        if (robotType == RobotType.TANK){
            myBuildingPos = buildTankPos;
            robotSpawnPos = newTankPos;
            newRobotRadius = RobotType.TANK.bodyRadius;
        }else{
            myBuildingPos = buildPos;
            robotSpawnPos = newRobotPos;
            newRobotRadius = RobotType.SOLDIER.bodyRadius;
        }
        float minDist = Constants.INF;
        int minIndex = -1;

        for (int i = 0; i < buildPositionsPerZone; i++){
            if (!onCurrentMap(myBuildingPos[i]) || !onCurrentMap(robotSpawnPos[i])) {
                //System.out.println(i + " fora del mapa");
                rc.setIndicatorDot(myBuildingPos[i], 255,0,0);
                continue;
            }
            try {
                if (rc.canSenseAllOfCircle(myBuildingPos[i],rc.getType().bodyRadius)){
                    if (!rc.onTheMap(myBuildingPos[i], rc.getType().bodyRadius) ||
                            rc.isCircleOccupiedExceptByThisRobot(myBuildingPos[i],rc.getType().bodyRadius)) {
                        //System.out.println("mybuildingpos bloquejada "+ myBuildingPos[i]);
                        continue;
                    }
                }
                if (rc.canSenseAllOfCircle(robotSpawnPos[i],newRobotRadius)){
                    if (!rc.onTheMap(robotSpawnPos[i], newRobotRadius) ||
                            rc.isCircleOccupiedExceptByThisRobot(robotSpawnPos[i],newRobotRadius)) {
                        //System.out.println("newrobotpos bloquejada "+ robotSpawnPos[i]);
                        continue;
                    }
                }

                if (myPos.distanceTo(myBuildingPos[i]) < minDist){
                    minDist = myPos.distanceTo(myBuildingPos[i]);
                    minIndex = i;
                }
            } catch (GameActionException e) {
                e.printStackTrace();
            }
        }
        //System.out.println("min index de build = "+minIndex);
        if (minIndex == -1) return null;

        MapLocation bestBuildLocation = myBuildingPos[minIndex];
        MapLocation newRobotLocation  = robotSpawnPos[minIndex];
        //System.out.println("Estic a "+ rc.getLocation() + " i vull anar a " + bestBuildLocation + " per construir a " +newRobotLocation);
        Direction buildDirection = bestBuildLocation.directionTo(newRobotLocation);

        if (myPos.distanceTo(bestBuildLocation) < Constants.eps && rc.canBuildRobot(robotType,buildDirection)){
            try {
                rc.buildRobot(robotType,buildDirection);
                incrementRobotsBuilt();
                return null;
            } catch (GameActionException e) {
                e.printStackTrace();
            }
        }
        if (rc.getTeamBullets() < robotType.bulletCost-40) return zoneCenterPos;
        return bestBuildLocation;
    }

    private static void buildWithoutZone(RobotType robotType){
        //System.out.println("zone i want " + zoneIWant[0] + "," + zoneIWant[1]);
        Direction front = rc.getLocation().directionTo(getCenterPosFromZone(zoneIWant));
        Direction back = front.opposite();
        for (int i = 0; i < 10; i++){
            Direction dirBuild = back.rotateRightRads(i*(float)Math.PI/20);
            if (rc.canBuildRobot(robotType,dirBuild)){
                try {
                    rc.buildRobot(robotType,dirBuild);
                    incrementRobotsBuilt();
                    return;
                } catch (GameActionException e) {
                    e.printStackTrace();
                }
            }
        }
        for (int i = 0; i > -10; i--){
            Direction dirBuild = back.rotateRightRads(i*(float)Math.PI/20);
            if (rc.canBuildRobot(robotType,dirBuild)){
                try {
                    rc.buildRobot(robotType,dirBuild);
                    incrementRobotsBuilt();
                    return;
                } catch (GameActionException e) {
                    e.printStackTrace();
                }
            }
        }
    }
*/
    private static void incrementRobotsBuilt(){
        try {
            int robots_built = rc.readBroadcast(Communication.ROBOTS_BUILT);
            rc.broadcast(Communication.ROBOTS_BUILT, robots_built + 1);
        } catch (GameActionException e) {
            e.printStackTrace();
        }
    }



    //quan construim el robot, actualitzem tot
    private static void updateAfterConstruct(int unitConstructed){
        try {
            int unitCurrentIndex = rc.readBroadcast(Communication.unitChannels[unitConstructed]);
            int unitNextIndex;
            if (unitCurrentIndex < Constants.IBL) {
                //si esta a la build inicial
                int i = unitCurrentIndex+1;
                while (i < Constants.IBL && Constants.initialBuild[i] != unitConstructed) i++;
                //ja no es torna a fer a la build inicial
                if (i == Constants.IBL){
                    int j = 0;
                    while (j < Constants.SBL && Constants.sequenceBuild[j] != unitConstructed) j++;
                    if (j == Constants.SBL){
                        //ja no la tornem a fer mai mes
                        unitNextIndex = (int)Constants.INF;
                    }else{
                        unitNextIndex = Constants.IBL + j;
                    }
                }else unitNextIndex = i;
            }else {
                int i = 0;
                while (i < Constants.SBL && Constants.sequenceBuild[(unitCurrentIndex+1+i) % Constants.SBL] != unitConstructed) i++;
                if (i == Constants.SBL) unitNextIndex = (int) Constants.INF;
                else unitNextIndex = unitCurrentIndex + 1 + i;
            }
            rc.broadcast(Communication.unitChannels[unitConstructed], unitNextIndex);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }


    static boolean allowedToConstruct(int unitToConstruct){
        return rc.getTeamBullets() > totalBulletCost(unitToConstruct);
    }

    private static float totalBulletCost(int unit){
        float totalMoney = 0;
        totalMoney += computeHowManyBehind(Constants.GARDENER, unit);
        if (unit == Constants.TREE){
            totalMoney += computeHowManyBehind(Constants.LUMBERJACK, unit);
            totalMoney += computeHowManyBehind(Constants.SOLDIER, unit);
            totalMoney += computeHowManyBehind(Constants.SCOUT, unit);
            totalMoney += computeHowManyBehind(Constants.TANK, unit);
        }else if(unit == Constants.TANK){
            totalMoney += computeHowManyBehind(Constants.TREE, unit);
        }else{
            //totalMoney += computeHowManyBehind(Constants.TREE, unit);
            totalMoney += computeHowManyBehind(Constants.TANK, unit);
        }
        float myBulletCost;
        if (unit == Constants.TREE) myBulletCost = GameConstants.BULLET_TREE_COST;
        else myBulletCost = Constants.ProductionUnits[unit].bulletCost;
        return totalMoney + myBulletCost;
    }

    //calcula les bales que calen per construir tots els unit1 que van abans de unit2 en la cua
    static int computeHowManyBehind(int unit1, int unit2) {
        try {
            int indexUnit1 = rc.readBroadcast(Communication.unitChannels[unit1]);
            int indexUnit2 = rc.readBroadcast(Communication.unitChannels[unit2]);
            if(indexUnit1 > indexUnit2) return 0;
            //Sabem que index1 <= index2

            int howManyBehind = 0;
            if(indexUnit2 < Constants.IBL){
                // index1 <= index2 < IBL
                for (int i = indexUnit1; i < indexUnit2; ++i)
                    if (Constants.initialBuild[i] == unit1) howManyBehind++;

            } else if (indexUnit1 < Constants.IBL) {
                // index1 < IBL <= index2
                int totalInSequence = 0;
                int totalLastSequence = 0;
                for (int i = indexUnit1; i < Constants.IBL; ++i) if (Constants.initialBuild[i] == unit1) howManyBehind++;
                for (int i = 0; i < Constants.SBL; ++i){
                    if (Constants.sequenceBuild[i] == unit1){
                        ++totalInSequence;
                        if (i < indexUnit2%Constants.SBL) ++totalLastSequence;
                    }
                }
                int extraWholeSequences = ((indexUnit2 - Constants.IBL)/Constants.SBL);
                howManyBehind += totalLastSequence + totalInSequence*extraWholeSequences;
            } else {
                // IBL < index1 <= index2
                int totalInSequence = 0;
                int totalOffSet = 0;
                for (int i = 0; i < Constants.SBL; ++i) {
                    if (Constants.sequenceBuild[i] == unit1) {
                        ++totalInSequence;
                    }
                }
                int z = indexUnit2%Constants.SBL;
                for (int i = indexUnit1; true ;++i){
                    int realI = i%Constants.SBL;
                    if (realI == z) break;
                    if (Constants.sequenceBuild[realI] == unit1) ++howManyBehind;
                    ++totalOffSet;
                }

                howManyBehind += ((indexUnit2 - indexUnit1 - totalOffSet)/Constants.SBL)*totalInSequence;
            }
            System.out.println("Hi ha " + howManyBehind + " " +unit1 + " behind " + unit2);
            if (unit1 < 4) return howManyBehind*Constants.ProductionUnits[unit1].bulletCost;
            else return howManyBehind* (int)GameConstants.BULLET_TREE_COST;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }


    static int bestSmallUnitToBuild(){
        try {
            int minQueue = 999999;
            int bestUnit = -1;
            for (int i = 1; i < 5; ++i) {
                if (i == 3) continue; //els tanks no son petits
                //mirem nomes lumberjacks, soldiers i scouts
                int a = rc.readBroadcast(Communication.unitChannels[i]);
                if (a < minQueue){
                    minQueue = a;
                    bestUnit = i;
                }
            }
            return bestUnit;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }


    private static boolean onCurrentMap(MapLocation pos){
        //System.out.println(mapMinX + "<" + pos.x + "<" + mapMaxX);
        //System.out.println(mapMinY + "<" + pos.y + "<" + mapMaxY);
        return mapMinX < pos.x && pos.x < mapMaxX && mapMinY < pos.y && pos.y < mapMaxY;
    }

    private static int[] getZoneFromPos(MapLocation pos){
        int[] z = {0,0};
        z[0] = (int) (Math.round(pos.x) - zoneOriginX + 127*zoneWidth) / (int)zoneWidth;
        z[0] -= 127;
        z[1] = (int) (Math.round(pos.y) - zoneOriginY + 127*zoneHeight) / (int) zoneHeight;
        z[1] -= 127;
        return z;
    }


    private static MapLocation getCenterPosFromZone(int[] z){
        return new MapLocation(zoneWidth * z[0] + zoneOriginX,
                zoneHeight * z[1] + zoneOriginY);
    }

    private static void updateTarget(MapLocation newTarget){
        if (realTarget != null && newTarget != null && newTarget.distanceTo(realTarget) < Constants.eps) return;
        realTarget = newTarget;
        //Greedy.resetObstacle(rc);
    }
}
