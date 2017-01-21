package Gardenerplayer;

import battlecode.common.*;
import com.sun.tools.internal.jxc.ap.Const;

import java.util.HashSet;
import java.util.Map;


public class Gardener {

    private static RobotController rc;

    private static MapLocation realTarget;

    private static int xBase;
    private static int yBase;
    private static MapLocation zoneBasePos;
    private static MapLocation zoneCenterPos;

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
    private static float[] bulletTreeHP = {-1,-1,-1,-1,-1,-1,-1,-1};

    private static float maxDistToCenter = 4.25f;

    private static float treePosX[] = new float[8];
    private static float treePosY[] = new float[8];
    private static MapLocation[] treePos = new MapLocation[8];
    private static float plantingPosX[] = new float[8];
    private static float plantingPosY[] = new float[8];
    private static MapLocation[] plantingPos = new MapLocation[8];
    private static float buildPosX[] = new float[4];
    private static float buildPosY[] = new float[4];
    private static MapLocation[] buildPos = new MapLocation[4];
    private static float newRobotPosX[] = new float[4];
    private static float newRobotPosY[] = new float[4];
    private static MapLocation[] newRobotPos = new MapLocation[4];

    private static float mapMinX = -Constants.INF;
    private static float mapMinY = -Constants.INF;
    private static float mapMaxX = Constants.INF;
    private static float mapMaxY = Constants.INF;


    private static int[] Xsorted = {0, -1, 1, 0, 0, 1, -1, -1, 1, -2, 2, 0, 0, 1, -1, -1, -2, -2, 1, 2, 2, -2, 2, 2, -2, 0, 3, 0, -3, -3, -1, 3, -1, 3, -3, 1, 1, -3, 2, -2, -3, 3, 3, 2, -2, -4, 0, 4, 0, -4, -4, 4, -1, -1, 1, 1, 4, -3, 3, 3, -3, -4, -2, 4, 4, -4, 2, 2, -2, 3, 4, 0, 3, -3, -5, 0, -3, -4, 4, -4, 5, 5, 1, -1, -1, -5, -5, 5, 1, 2, 5, 2, -2, -2, -5, 5, -5, 4, -4, -4, 4, -3, -3, 5, 3, 3, -5, -5, 5, 6, -6, 0, 0, 1, -1, 6, -6, -6, 6, 1, -1, -6, -2, -6, 2, 6, 6, 2, -2, -4, -4, -5, 5, 5, 4, -5, 4, -6, 6, -3, -3, 6, 3, 3, -6, -7, 0, 0, 7, -1, -7, -7, -5, 7, 1, 7, 1, 5, -1, 5, -5, 6, 4, -4, -4, 6, 4, -6, -6, -2, 2, 7, -2, 2, 7, -7, -7, 3, -7, 7, 3, -3, 7, -7, -3, 6, 5, -6, -6, 5, -5, -5, 6, -8, 0, 0, 8, -4, 7, 8, 4, 8, -1, -1, -4, -8, -8, 1, -7, -7, 1, 4, 7, 8, 8, 2, 2, -2, -2, -8, -8, 6, -6, -6, 6, -3, 8, -8, -3, -8, 8, 3, 3, -7, 7, -7, 7, -5, 5, 5, -5, -8, -4, 4, 8, -4, 4, -8, 8, -7, 7, 6, -7, 7, -6, -6, 6, 8, 5, -5, -8, -8, 8, -5, 5, 7, -7, -7, 7, -8, 8, 6, -8, 8, 6, -6, -6, 8, -8, 8, 7, 7, -7, -7, -8, 8, -8, -8, 8};
    private static int[] Ysorted = {0, 0, 0, -1, 1, 1, -1, 1, -1, 0, 0, 2, -2, -2, 2, -2, -1, 1, 2, -1, 1, 2, -2, 2, -2, -3, 0, 3, 0, 1, -3, 1, 3, -1, -1, -3, 3, -2, -3, 3, 2, -2, 2, 3, -3, 0, -4, 0, 4, 1, -1, 1, -4, 4, -4, 4, -1, 3, -3, 3, -3, -2, 4, 2, -2, 2, 4, -4, -4, -4, 3, -5, 4, 4, 0, 5, -4, 3, -3, -3, 0, -1, 5, 5, -5, -1, 1, 1, -5, 5, 2, -5, -5, 5, -2, -2, 2, -4, -4, 4, 4, 5, -5, -3, 5, -5, 3, -3, 3, 0, 0, -6, 6, 6, 6, -1, 1, -1, 1, -6, -6, 2, 6, -2, -6, -2, 2, 6, -6, -5, 5, 4, 4, -4, -5, -4, 5, 3, 3, -6, 6, -3, -6, 6, -3, 0, -7, 7, 0, -7, -1, 1, 5, -1, -7, 1, 7, 5, 7, -5, -5, 4, 6, -6, 6, -4, -6, 4, -4, 7, 7, -2, -7, -7, 2, 2, -2, 7, -3, 3, -7, 7, -3, 3, -7, -5, 6, 5, -5, -6, -6, 6, 5, 0, 8, -8, 0, 7, 4, -1, -7, 1, -8, 8, -7, -1, 1, -8, 4, -4, 8, 7, -4, -2, 2, 8, -8, 8, -8, 2, -2, -6, 6, -6, 6, -8, 3, 3, 8, -3, -3, -8, 8, 5, -5, -5, 5, 7, 7, -7, -7, 4, -8, 8, -4, 8, -8, -4, 4, -6, -6, 7, 6, 6, -7, 7, -7, -5, -8, 8, -5, 5, 5, -8, 8, -7, -7, 7, 7, -6, -6, -8, 6, 6, 8, 8, -8, 7, -7, -7, 8, -8, 8, -8, 7, -8, 8, -8, 8};


    public static void run(RobotController rcc) {
        rc = rcc;
        Initialize();

        while (true) {

            broadcastMyZone();
            //System.out.println("despres de broadcast " + Clock.getBytecodeNum());
            MapLocation newTarget = rc.getLocation();
            if (zone[0] == Constants.INF) {
                searchZone(); //aqui se li dona un valor a zoneIWant 99.99999999% segur
                //System.out.println("despres de searchzone " + Clock.getBytecodeNum());
                if (zoneIWant[0] != -1){
                    newTarget = getCenterPosFromZone(zoneIWant[0],zoneIWant[1]);
                    System.out.println("Va a zona " + rc.getLocation() + " a " + newTarget + ", " + isZoneInMap(zoneIWant));
                    rc.setIndicatorLine(rc.getLocation(),newTarget, 255, 255, 255);
                }
                //System.out.println("Soc a la zona "+ getZoneFromPos(rc.getLocation())[0] + "," + getZoneFromPos(rc.getLocation())[1] + " i vull anar a "+zoneIWant[0] + "," + zoneIWant[1]);
                checkIfArrivedToZone();
            }else{
                updateTreeHP();
                checkNeutralTreesInZone();
                newTarget = checkNearbyEnemies();
                if (newTarget != null) {
                    System.out.println("Fuig de " + rc.getLocation() + " a " + newTarget);
                    rc.setIndicatorLine(rc.getLocation(),newTarget, 255, 0, 0);
                }
                else {
                    newTarget = returnToZone();
                    if (newTarget != null) {
                        System.out.println("Retorna: de " + rc.getLocation() + " a " + newTarget);
                        rc.setIndicatorLine(rc.getLocation(),newTarget, 255, 220, 28);
                    }
                    else {
                        newTarget = findLowHPTree();
                        if (newTarget != null) {
                            System.out.println("Rega, de " + rc.getLocation() + " a " + newTarget);
                            rc.setIndicatorLine(rc.getLocation(),newTarget, 0, 255, 0);
                        }
                        else {
                            newTarget = tryBuilding();
                            if (newTarget != null) {
                                System.out.println("Builds, de " + rc.getLocation() + " a " + newTarget);
                                rc.setIndicatorLine(rc.getLocation(),newTarget, 0, 119, 255);
                            }
                            else {
                                System.out.println("No tinc res a fer");
                                newTarget = zoneBasePos;
                                rc.setIndicatorLine(rc.getLocation(),newTarget, 0, 0, 0);
                            }
                        }
                    }
                }
            }

            //System.out.println("despres de decidir tot " + Clock.getBytecodeNum());
            updateMapBounds(newTarget);



            updateTarget(newTarget);
            waterNearbyTree();

            try {
                if (realTarget == null) {
                    rc.setIndicatorDot(rc.getLocation(), 255, 0, 0);
                }
            }catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }

            //System.out.println("Estic a " + rc.getLocation() + " i vaig a " + realTarget);
            Greedy.moveGreedy(rc, realTarget);
            Clock.yield();
        }
    }


    //nomes es fa la primera ronda
    private static void Initialize(){
        MapLocation base = rc.getInitialArchonLocations(rc.getTeam())[0];
        xBase = Math.round(base.x);
        yBase = Math.round(base.y);
    }

    private static void broadcastZone(int[] z, int newZoneType){
        if (z[0] == Constants.INF) return;
        int zone_id = z[0] + zoneColumns * z[1];
        if (zone_id < 0) zone_id += zoneColumns*zoneRows;
        //System.out.println("Es marca la zone " + zone_id + " com a " + newZoneType);
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

    private static void broadcastZoneLimit(int channel, int value){
        try {
            int old_value = rc.readBroadcast(channel);
            if (channel == Communication.MAX_ZONE_X || channel == Communication.MAX_ZONE_Y){
                int new_value = Math.min(old_value,value - Communication.ZONE_LIMIT_OFFSET);
                rc.broadcast(channel, new_value);
            }else{
                int new_value = Math.max(old_value,value + Communication.ZONE_LIMIT_OFFSET);
                rc.broadcast(channel, new_value);
            }
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
        return (int)Constants.INF;
    }


    private static int getZoneTypeFromBroadcast(int[] z){
        int zone_id = z[0] + zoneColumns * z[1];
        if (zone_id < 0) zone_id += zoneColumns*zoneRows;
        int channel_id = zone_id / zonesPerChannel;
        try {
            int info = rc.readBroadcast(Communication.ZONE_FIRST_POSITION + channel_id);
            info = info >> (4*(zone_id % zonesPerChannel));
            //System.out.println("La zona " + zone_id + " te tipus " + (info&0x3));
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

    private static void assignZone(int[] assignedZone){
        float[] treeOffsetX = {-3f,-1f,1f,3f,-3f,-1f,1f,3f};
        float[] treeOffsetY = {-3.5f,-3.49f,-3.48f,-3.47f,3.47f,3.48f,3.49f,3.5f};
        float[] plantingOffsetX = {-3f,-1f,1f,3f,-3f,-1f,1f,3f};
        float[] plantingOffsetY = {-1.5f,-1.49f,-1.48f,-1.47f,1.47f,1.48f,1.49f,1.5f};
        float[] buildOffsetX= {-3f,3f,-3f,3f};
        float[] buildOffsetY= {-1.45f,-1.45f,1.45f,1.45f};
        float[] newRobotOffsetX= {-4f,4f,-4f,4f};
        float[] newRobotOffsetY= {-1.45f,-1.45f,1.45f,1.45f};

        zone = assignedZone;
        zoneBasePos = getBasePosFromZone(zone[0],zone[1]);
        zoneCenterPos = getCenterPosFromZone(zone[0],zone[1]);
        broadcastZone(zone, busyZone);

        for (int i = 0; i < 8; i++){
            treePosX[i] = zoneCenterPos.x + treeOffsetX[i];
            treePosY[i] = zoneCenterPos.y + treeOffsetY[i];
            treePos[i] = new MapLocation(treePosX[i], treePosY[i]);
            plantingPosX[i] = zoneCenterPos.x + plantingOffsetX[i];
            plantingPosY[i] = zoneCenterPos.y + plantingOffsetY[i];
            plantingPos[i] = new MapLocation(plantingPosX[i], plantingPosY[i]);
        }
        for (int i = 0; i < 4; i++){
            buildPosX[i] = zoneCenterPos.x + buildOffsetX[i];
            buildPosY[i] = zoneCenterPos.y + buildOffsetY[i];
            buildPos[i] = new MapLocation(buildPosX[i], buildPosY[i]);
            newRobotPosX[i] = zoneCenterPos.x + newRobotOffsetX[i];
            newRobotPosY[i] = zoneCenterPos.y + newRobotOffsetY[i];
            newRobotPos[i] = new MapLocation(newRobotPosX[i],newRobotPosY[i]);
        }

    }

    private static void searchZone() {
        //System.out.println("entra search " + zoneIWant[0]+","+zoneIWant[1]);
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
            //System.out.println("zone "+new_zone_id+" type " + zoneType);
            if (zoneType != outOfMapZone) updateZoneInMap(newZone);
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

    private static void updateMapBounds(MapLocation newTarget){
        MapLocation myPos = rc.getLocation();
        Direction targetDir = myPos.directionTo(newTarget);
        MapLocation closerTarget = myPos.add(targetDir, 2f);
        try {
            if (rc.onTheMap(closerTarget)) return;
            if (zone[0] == Constants.INF) {
                updateZoneInMap(zoneIWant);
                System.out.println("Ha arribat al limit del mapa, actualitza la zona " + isZoneInMap(zoneIWant));
                zoneIWant[0] = zoneIWant[1] = -1;
            }
            float dx = targetDir.getDeltaX(1);
            float dy = targetDir.getDeltaY(1);
            if (dx < 0){
                if (!rc.onTheMap(myPos.add(Direction.WEST,2))){
                    mapMinX = Math.max(mapMinX,myPos.add(Direction.WEST,2).x);
                }
            }
            if (dx > 0){
                if (!rc.onTheMap(myPos.add(Direction.EAST,2))){
                    mapMaxX = Math.min(mapMaxX,myPos.add(Direction.EAST,2).x);
                }
            }
            if (dy < 0){
                if (!rc.onTheMap(myPos.add(Direction.SOUTH,2))){
                    mapMinY = Math.max(mapMinY,myPos.add(Direction.SOUTH,2).y);
                }
            }
            if (dy > 0){
                if (!rc.onTheMap(myPos.add(Direction.NORTH,2))){
                    mapMaxY = Math.min(mapMaxY,myPos.add(Direction.NORTH,2).y);
                }
            }
        } catch (GameActionException e) {
            e.printStackTrace();
        }
    }

    private static void updateZoneInMap(int[] z){
        MapLocation center = getCenterPosFromZone(z[0],z[1]);
        if (!onCurrentMap(center)){
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
        }else {
            System.out.println("El centre " + center + " esta dintre de:");
            System.out.println(mapMinX + "-" + mapMaxX + ", " + mapMinY + "-" +mapMaxY);
        }
    }

    private static boolean isZoneInMap(int[] z){
        return !(z[0] < getZoneLimitFromBroadcast(Communication.MIN_ZONE_X) ||
                 z[0] > getZoneLimitFromBroadcast(Communication.MAX_ZONE_X) ||
                 z[1] < getZoneLimitFromBroadcast(Communication.MIN_ZONE_Y) ||
                 z[1] > getZoneLimitFromBroadcast(Communication.MAX_ZONE_Y));
    }

    private static void checkIfArrivedToZone(){
        MapLocation centerIWant = getCenterPosFromZone(zoneIWant[0],zoneIWant[1]);
        if (!rc.canSenseLocation(centerIWant)) return;
        int zoneType = getZoneTypeFromBroadcast(zoneIWant);
        try{
            if (zoneType == busyZone) {
                zoneIWant[0] = zoneIWant[1] = -1;
                return;
            }
            if (!rc.onTheMap(centerIWant)){
                updateZoneInMap(zoneIWant);
                zoneIWant[0] = zoneIWant[1] = -1;
                return;
            }
            System.out.println("El punt " + centerIWant + " esta dintre el mapa");
            rc.setIndicatorDot(centerIWant,255,255,255);
            assignZone(zoneIWant);
        }catch (GameActionException e){
            e.printStackTrace();
        }
    }

    private static void waterNearbyTree(){
        if (!rc.canWater()) return;
        //System.out.println("entra regar");
        TreeInfo[] myTrees = rc.senseNearbyTrees(rc.getType().bodyRadius + rc.getType().strideRadius, rc.getTeam());
        float minHP = Constants.INF;
        int minID = -1;
        for (TreeInfo tree: myTrees){
            if (tree.getHealth() < minHP){
                minHP = tree.getHealth();
                minID = tree.getID();
            }
        }
        //System.out.println("Rego arbre "+minID);
        if (minID != -1) try {
            rc.water(minID);
        } catch (GameActionException e) {
            e.printStackTrace();
        }
    }

    private static void updateTreeHP(){
        for (int i = 0; i < bulletTreeHP.length; i++){
            if (rc.canSenseLocation(treePos[i])){
                try {
                    TreeInfo tree = rc.senseTreeAtLocation(treePos[i]);
                    if (tree == null){
                        bulletTreeHP[i] = -1;
                    }else{
                        if (bulletTreeHP[i] > GameConstants.BULLET_TREE_MAX_HEALTH) bulletTreeHP[i] -= GameConstants.BULLET_TREE_DECAY_RATE;
                        else{
                            bulletTreeHP[i] = tree.getHealth();
                        }
                    }
                } catch (GameActionException e) {
                    e.printStackTrace();
                }
            }else{
                if (bulletTreeHP[i] >= 0 && bulletTreeHP[i] < GameConstants.BULLET_TREE_MAX_HEALTH){
                    bulletTreeHP[i] -= GameConstants.BULLET_TREE_DECAY_RATE;
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
        for (int i = 0; i < treePos.length; i++){
            if (treePosX[i] == Constants.INF) continue;
            if (rc.canWater(treePos[i]) && bulletTreeHP[i] < Constants.minHPWater) return plantingPos[i];
        }
        for (int i = 0; i < treePos.length; i++){
            if (bulletTreeHP[i] < Constants.minHPGoWater && bulletTreeHP[i] >= 0) return plantingPos[i];
        }
        return null;
    }

    private static MapLocation tryBuilding(){
        //System.out.println("Entra trybuild");
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
            //System.out.println("Lo seguent a construir es " + unit_to_build);
            if (unit_to_build == Constants.UNIT_TREE){
                return tryPlant();
            }else if (unit_to_build == Constants.UNIT_GARDENER) {
                incrementUnitsBuilt(); // TREURE AIXO QUAN ESTIGUIN FETS ELS ARCHONS
                return null;
            }
            else return tryBuild(unit_to_build);
        } catch (GameActionException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static MapLocation tryPlant(){
        System.out.println("intenta plantar");
        MapLocation myPos = rc.getLocation();
        float minDist = Constants.INF;
        int minIndex = -1;
        for (int i = 0; i < 8; i++){
            if (bulletTreeHP[i] > 0) continue;
            if (!onCurrentMap(treePos[i]) || !onCurrentMap(plantingPos[i])) {
                System.out.println("arbre " + i + " fora del mapa");
                continue;
            }
            try {
                if (rc.canSenseAllOfCircle(treePos[i],GameConstants.BULLET_TREE_RADIUS) && !rc.onTheMap(treePos[i], GameConstants.BULLET_TREE_RADIUS)){
                    updateMapBounds(treePos[i]);
                    System.out.println("La posicio de l'arbre " + i + " esta fora del mapa");
                    continue;
                }
                if (rc.canSenseAllOfCircle(plantingPos[i],GameConstants.BULLET_TREE_RADIUS) && !rc.onTheMap(plantingPos[i], GameConstants.BULLET_TREE_RADIUS)){
                    updateMapBounds(plantingPos[i]);
                    System.out.println("La posicio de plantar " + i + " esta fora del mapa");
                    continue;
                }
                if (rc.canSenseAllOfCircle(treePos[i],GameConstants.BULLET_TREE_RADIUS) &&
                    rc.isCircleOccupiedExceptByThisRobot(treePos[i],GameConstants.BULLET_TREE_RADIUS)) {
                    System.out.println("La posicio de l'arbre " + i + " esta ocupada");
                    continue;
                }
                if (myPos.distanceTo(treePos[i]) < minDist){
                    minDist = myPos.distanceTo(treePos[i]);
                    minIndex = i;
                }
            } catch (GameActionException e) {
                e.printStackTrace();
            }
        }

        if (minIndex == -1) {
            System.out.println("no te cap lloc per plantar");
            return null;
        }
        MapLocation bestTreePos = treePos[minIndex];
        if (rc.getLocation().distanceTo(plantingPos[minIndex]) < Constants.eps && rc.canPlantTree(myPos.directionTo(bestTreePos))){
            try {
                //Si pot plantar l'arbre, el planta i no cal que retorni cap direccio
                rc.plantTree(myPos.directionTo(bestTreePos));
                incrementUnitsBuilt();
                bulletTreeHP[minIndex] = GameConstants.BULLET_TREE_MAX_HEALTH + 40; //sumem 40 per tenir en compte el temps que triguen en estar full vida
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
        System.out.println("Vol construir un " + Constants.getRobotTypeFromIndex(unit_to_build));
        MapLocation myPos = rc.getLocation();
        float minDist = Constants.INF;
        int minIndex = -1;

        for (int i = 0; i < 4; i++){
            if (!onCurrentMap(buildPos[i]) || !onCurrentMap(newRobotPos[i])) {
                System.out.println("fora del mapa");
                rc.setIndicatorDot(buildPos[i], 255,0,0);
                continue;
            }
            try {
                if (rc.canSenseAllOfCircle(buildPos[i],rc.getType().bodyRadius) &&
                    rc.isCircleOccupiedExceptByThisRobot(buildPos[i],rc.getType().bodyRadius)) continue;
                if (myPos.distanceTo(buildPos[i]) < minDist){
                    minDist = myPos.distanceTo(buildPos[i]);
                    minIndex = i;
                }
            } catch (GameActionException e) {
                e.printStackTrace();
            }
        }
        System.out.println("min index de build = "+minIndex);
        if (minIndex == -1) return null;

        MapLocation bestBuildLocation = buildPos[minIndex];
        MapLocation newRobotLocation  = newRobotPos[minIndex];
        //System.out.println("Estic a "+ rc.getLocation() + " i vull anar a " + bestBuildLocation + " per construir a " +newRobotLocation);
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

    private static boolean onCurrentMap(MapLocation pos){
        return mapMinX < pos.x && pos.x < mapMaxX && mapMinY < pos.y && pos.y < mapMaxY;
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

    private static void updateTarget(MapLocation newTarget){
        if (realTarget != null && newTarget != null && newTarget.distanceTo(realTarget) < Constants.eps) return;
        realTarget = newTarget;
        Greedy.resetObstacle();
    }



}
