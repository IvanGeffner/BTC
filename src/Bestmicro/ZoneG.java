package Bestmicro;

import battlecode.common.*;

/**
 * Created by Pau on 24/01/2017.
 */
public class ZoneG {

    private static RobotController rc = null;
    private static int[] zone;

    private static MapLocation center;

    private static float STREET_WIDTH = 5f;
    private static float STREET_HEIGHT = 5f;
    private static float BLOCK_WIDTH = 6f;
    private static float BLOCK_HEIGHT = 6f;
    private static float zoneWidth = STREET_WIDTH + BLOCK_WIDTH;
    private static float zoneHeight = STREET_HEIGHT + BLOCK_HEIGHT;

    private static float zoneOriginX;
    private static float zoneOriginY;

    private static int zoneRows = 2*GameConstants.MAP_MAX_WIDTH / (int) zoneWidth; // = 2*100 / 11 = 18
    private static int zoneColumns = 2*GameConstants.MAP_MAX_HEIGHT / (int) zoneHeight; // = 18
    private static int zonesPerChannel = 6;

    private static int bitsZoneType = 3;
    private static int bitsZoneTurn = 2;
    private static int bitsPerZone = bitsZoneType + bitsZoneTurn;

    private static int treesPerZone = 7;
    private static int buildPositionsPerZone = 4;

    static int turnsResetZone = 3;

    static MapLocation[] treePos = new MapLocation[treesPerZone];
    static MapLocation[] plantingPos = new MapLocation[treesPerZone];
    static MapLocation[] wateringPos = new MapLocation[treesPerZone];
    static MapLocation[] buildPos = new MapLocation[buildPositionsPerZone];
    static MapLocation[] newRobotPos = new MapLocation[buildPositionsPerZone];
    static MapLocation[] buildTankPos = new MapLocation[buildPositionsPerZone];
    static MapLocation[] newTankPos = new MapLocation[buildPositionsPerZone];
    static float treeHP[] = new float[treesPerZone];
    static int indexVertexTrees[] = new int[2];


    static void init(RobotController rc2){
        rc = rc2;
    }

    static boolean hasValue(int[] z){
        return (z != null && z[0] != (int) Constants.INF);
    }

    static int[] newZone(int x, int y){
        return new int[]{x,y};
    }

    static int[] nullZone(){
        return new int[] {(int) Constants.INF, (int) Constants.INF};
    }

    static void resetMyZone(){
        zone = nullZone();
    }

    static MapLocation center(int[] z){
        return new MapLocation(zoneWidth * z[0] + zoneOriginX,
                zoneHeight * z[1] + zoneOriginY);
    }

    static MapLocation center(){
        if (!hasValue(zone)){
            try {
                throw new GameActionException(GameActionExceptionType.CANT_DO_THAT,"ERROR: crida de center() sense tenir zona assignada");
            } catch (GameActionException e) {
                e.printStackTrace();
            }
            return null;
        }
        return center;
    }

    static int[] getZoneFromPos(MapLocation pos){
        int[] z = {0,0};
        z[0] = (int) (pos.x - zoneOriginX + zoneWidth/2 + 127*zoneWidth) / (int)zoneWidth;
        z[0] -= 127;
        z[1] = (int) (pos.y - zoneOriginY + zoneWidth/2 + 127*zoneHeight) / (int) zoneHeight;
        z[1] -= 127;
        return z;
    }

    static void setOrigin(float x, float y) {
        zoneOriginX = x;
        zoneOriginY = y;
    }

    static int getID(int[] z){
        //rang de zoneid:
        // inici: -9 + 18*(-9) + 18*18/2 + 9 = 0
        // final: 9 + 18*9 + 18*18/2 + 9 = 342
        // amb 6 zones per canal fan falta 57 canals
        return z[0] + zoneColumns * z[1] + zoneColumns*zoneRows/2 + 9;
    }

    static int[] readInfoBroadcast(int[] z){
        if (!ZoneG.hasValue(z)) return null;
        int zone_id =  ZoneG.getID(z);
        int channel_id = zone_id / zonesPerChannel;
        try {
            int info = rc.readBroadcast(Communication.ZONE_FIRST_POSITION + channel_id);
            info = (info >> (bitsPerZone * (zone_id % zonesPerChannel))) & 0x1F;
            //System.out.println("read channel " + channel_id + ": " + Integer.toBinaryString(info) + "  " + Integer.toBinaryString(rc.readInfoBroadcast(channel_id)));
            int zoneType = info & 0x7;
            int lastTurn = (info >> 3) & 0x3;
            return new int[]{zoneType, lastTurn};
        } catch (GameActionException e) {
            e.printStackTrace();
        }
        return null;
    }

    static void broadcastInfo(int[] z, int newZoneType){
        if (z[0] == Constants.INF) return;
        int zone_id = ZoneG.getID(z);
        int channel_id = zone_id / zonesPerChannel;
        int info = (rc.getRoundNum() & 0x03) * 8 + (newZoneType & 0x7);
        info = info << (bitsPerZone* (zone_id % zonesPerChannel));
        try {
            int old_channel_info = rc.readBroadcast(channel_id + Communication.ZONE_FIRST_POSITION);
            int mask = ~((0x1F) << (bitsPerZone * (zone_id % zonesPerChannel)));
            int new_channel_info = (old_channel_info & mask) + info;
            rc.broadcast(channel_id + Communication.ZONE_FIRST_POSITION, new_channel_info);
        } catch (GameActionException e) {
            e.printStackTrace();
        }
    }

    static void broadcastLimit(int channel, int value){
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
        } catch (GameActionException e) {
            e.printStackTrace();
        }
    }

    static boolean updateInMap(int[] z){
        MapLocation center = ZoneG.center(z);
        try {
            if (!Map.onCurrentMap(center) || (rc.canSenseAllOfCircle(center,rc.getType().bodyRadius) && !rc.onTheMap(center, rc.getType().bodyRadius))){
                ZoneG.broadcastInfo(z, Constants.outOfMapZone);
                if (center.x < Map.minX) {
                    ZoneG.broadcastLimit(Communication.MIN_ZONE_X, z[0] + 1);
                }
                if (center.x > Map.maxX) {
                    ZoneG.broadcastLimit(Communication.MAX_ZONE_X, z[0] - 1);
                }
                if (center.y < Map.minY) {
                    ZoneG.broadcastLimit(Communication.MIN_ZONE_Y, z[1] + 1);
                }
                if (center.y > Map.maxY) {
                    ZoneG.broadcastLimit(Communication.MAX_ZONE_Y, z[1] - 1);
                }
                return false;
            }else {
                return true;
            }
        } catch (GameActionException e) {
            e.printStackTrace();
        }
        return false;
    }

    static int readLimitBroadcast(int channel){
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

    static int readTypeBroadcast(int[] z){
        int[] info = readInfoBroadcast(z);
        if (info == null) return -1;
        return info[0];
    }

    static void broadcastMyZone(){
        if (!hasValue(zone)){
            try {
                throw new GameActionException(GameActionExceptionType.CANT_DO_THAT,"ERROR: crida de broadcastMyZone() sense tenir zona assignada");
            } catch (GameActionException e) {
                e.printStackTrace();
            }
            return;
        }
        broadcastInfo(zone, Constants.busyZone);
    }

    static void assign(int[] assignedZone){
        if (hasValue(zone)){
            try {
                String errmsg = "ERROR: Intent d'assignar la zona " + assignedZone[0] + "," + assignedZone[1] + " quan ja hi havia assignada la zona " +  + zone[0] + "," + zone[1];
                throw new GameActionException(GameActionExceptionType.CANT_DO_THAT,errmsg);
            } catch (GameActionException e) {
                e.printStackTrace();
            }
            return;
        }
        float[] treeOffsetX = {-2.01f,0f,2.01f, -2.01f, 0f, 2.01f,2.01f};
        float[] treeOffsetY = {-2.05f,-2.05f,-2.05f,2.05f,2.05f,2.05f,0f};
        float[] plantingOffsetX = {-2.01f,0f,2.01f, -2.01f, 0f, 2.01f,0f};
        float[] plantingOffsetY = {-0.05f,-0.05f,-0.05f,0.05f,0.05f,0.05f,0f};
        float[] wateringOffsetX = {0f,0f,0f,0f,0f,0f,0f};
        float[] wateringOffsetY = {0f,0f,0f,0f,0f,0f,0f};
        float[] buildOffsetX = {0f,0f,0f,0f};
        float[] buildOffsetY = {0f,0f,0f,0f};
        float[] newRobotOffsetX= {-2f,2f,0f,0f};
        float[] newRobotOffsetY= {0f,0f,-2f,2f};
        float[] buildTankOffsetX= {-2f,2f,0f,0f};
        float[] buildTankOffsetY= {0f,0f,-2f,2f};
        float[] newTankOffsetX = {-5f,5f,0f,0f};
        float[] newTankOffsetY = {0f,0f,-5f,5f};
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
        center = center(zone);
        broadcastInfo(assignedZone, Constants.busyZone);

        try {
            if(treesPerZone == 7 && !rc.onTheMap(center.add(Direction.WEST,6f))){
                treeOffsetX[6] = -2.01f;
                newRobotOffsetX[0] = 2f;
                buildTankOffsetX[0] = 2f;
                newTankOffsetX[0] = 5f;
                indexVertexTrees = new int[] {0,3};
            }else {
                //newRobotOffsetX[1] = -2f;
                //buildTankOffsetX[1] = -2f;
                //newTankOffsetX[1] = -5f;
                indexVertexTrees = new int[] {2,5};
            }
        } catch (GameActionException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < treesPerZone; i++){
            treePosX[i] = center.x + treeOffsetX[i];
            treePosY[i] = center.y + treeOffsetY[i];
            treePos[i] = new MapLocation(treePosX[i], treePosY[i]);
            plantingPosX[i] = center.x + plantingOffsetX[i];
            plantingPosY[i] = center.y + plantingOffsetY[i];
            plantingPos[i] = new MapLocation(plantingPosX[i], plantingPosY[i]);
            wateringPosX[i] = center.x + wateringOffsetX[i];
            wateringPosY[i] = center.y + wateringOffsetY[i];
            wateringPos[i] = new MapLocation(wateringPosX[i], wateringPosY[i]);
            treeHP[i] = -1;
        }

        for (int i = 0; i < buildPositionsPerZone; i++){
            buildPosX[i] = center.x + buildOffsetX[i];
            buildPosY[i] = center.y + buildOffsetY[i];
            buildPos[i] = new MapLocation(buildPosX[i], buildPosY[i]);
            newRobotPosX[i] = center.x + newRobotOffsetX[i];
            newRobotPosY[i] = center.y + newRobotOffsetY[i];
            newRobotPos[i] = new MapLocation(newRobotPosX[i],newRobotPosY[i]);
            buildTankPosX[i] = center.x + buildTankOffsetX[i];
            buildTankPosY[i] = center.y + buildTankOffsetY[i];
            buildTankPos[i] = new MapLocation(buildTankPosX[i],buildTankPosY[i]);
            newTankPosX[i] = center.x + newTankOffsetX[i];
            newTankPosY[i] = center.y + newTankOffsetY[i];
            newTankPos[i] = new MapLocation(newTankPosX[i],newTankPosY[i]);
        }
    }

    static void updateTreeHP(){
        if (!hasValue(zone)){
            try {
                throw new GameActionException(GameActionExceptionType.CANT_DO_THAT,"ERROR: crida de updateTreeHP() sense tenir zona assignada");
            } catch (GameActionException e) {
                e.printStackTrace();
            }
            return;
        }
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

    //envia missatge de tallar els arbres en la capsa de 3x3 i si no n'hi ha cap en la de 5.5x5.5
    static void messageNeutralTreesInBox(MapLocation center, TreeInfo[] trees){
        int max_bytecode = 3000;
        int bytecode_init = Clock.getBytecodeNum();
        MapLocation[] outerTrees = new MapLocation[trees.length];
        boolean sendOuterTrees = true;

        float innerDistance = 3f;
        float outerDistance = zoneWidth/2;

        int outerTreeCount = 0;

        for (TreeInfo ti: trees){
            MapLocation treePos = ti.getLocation();
            if (Math.abs(treePos.x - center.x) - ti.getRadius() < innerDistance && Math.abs(treePos.y - center.y) - ti.getRadius() < innerDistance){
                sendOuterTrees = false;
                messageCutNeutralTree(treePos);
            }else if (sendOuterTrees && Math.abs(treePos.x - center.x) - ti.getRadius() < outerDistance &&
                    Math.abs(treePos.y - center.y) - ti.getRadius() < outerDistance){
                outerTrees[outerTreeCount] = treePos;
                outerTreeCount++;
            }
        }
        if (!sendOuterTrees) return;
        for (int i = 0; i < outerTreeCount; i++) {
            messageCutNeutralTree(outerTrees[i]);
            if (Clock.getBytecodeNum() - bytecode_init > max_bytecode) return;
        }
    }

    static void messageCutNeutralTree(MapLocation treeLocation) {
        if (Constants.DEBUG == 1) rc.setIndicatorDot(treeLocation,255,120,0);
        Communication.sendMessage(Communication.CHOPCHANNEL,Math.round(treeLocation.x),Math.round(treeLocation.y),0);
    }

    static MapLocation findLowHPTree(){
        if (!hasValue(zone)){
            try {
                throw new GameActionException(GameActionExceptionType.CANT_DO_THAT,"ERROR: crida de findLowHPTree() sense tenir zona assignada");
            } catch (GameActionException e) {
                e.printStackTrace();
            }
            return null;
        }
        for (int i = 0; i < treePos.length; i++){
            if (rc.getLocation().distanceTo(wateringPos[i]) > Constants.eps &&
                treeHP[i] < Constants.minHPGoWater && treeHP[i] >= 0) return wateringPos[i];
        }
        for (int i = 0; i < treePos.length; i++){
            if (treePos[i].x == Constants.INF) continue;
            if (rc.getLocation().distanceTo(wateringPos[i]) > Constants.eps &&
                rc.canWater(treePos[i]) && treeHP[i] < Constants.minHPWater) return wateringPos[i];
        }
        return null;
    }

    static int countAvailableRobotBuildPositions(){
        //no ho fa be si la zona esta abandonada
        if (!hasValue(zone)){
            try {
                throw new GameActionException(GameActionExceptionType.CANT_DO_THAT,"ERROR: crida de countAvailableRobotBuildPositions() sense tenir zona assignada");
            } catch (GameActionException e) {
                e.printStackTrace();
            }
            return -1;
        }
        int count = 0;
        for (int i = 0; i < buildPositionsPerZone; i++){
            try {
                if (rc.canSenseAllOfCircle(newRobotPos[i],RobotType.SOLDIER.bodyRadius) &&
                        !rc.isCircleOccupiedExceptByThisRobot(newRobotPos[i],RobotType.SOLDIER.bodyRadius)) count++;
            } catch (GameActionException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Te " + count + " posicions per construir");
        return count;
    }

    static int indexToPlant(){
        if (!hasValue(zone)){
            try {
                throw new GameActionException(GameActionExceptionType.CANT_DO_THAT,"ERROR: crida de indexToPlant() sense tenir zona assignada");
            } catch (GameActionException e) {
                e.printStackTrace();
            }
            return -1;
        }
        MapLocation myPos = rc.getLocation();
        float minDist = Constants.INF;
        int minIndex = -1;
        for (int i = 0; i < treesPerZone; i++){
            if (treeHP[i] > 0) continue;
            if (!Map.onCurrentMap(treePos[i]) || !Map. onCurrentMap(plantingPos[i])) {
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

    static int indexToBuild(int unit){
        if (!hasValue(zone)){
            try {
                throw new GameActionException(GameActionExceptionType.CANT_DO_THAT,"ERROR: crida de indexToBuild() sense tenir zona assignada");
            } catch (GameActionException e) {
                e.printStackTrace();
            }
            return -1;
        }
        if (unit == Constants.GARDENER) return -1;
        MapLocation myPos = rc.getLocation();
        MapLocation myBuildingPos[];
        MapLocation robotSpawnPos[];
        float newRobotRadius;
        if (unit == Constants.TANK){
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
            if (!Map.onCurrentMap(myBuildingPos[i]) || !Map.onCurrentMap(robotSpawnPos[i])) {
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

    static boolean inMap(int[] z){
        return !(z[0] < readLimitBroadcast(Communication.MIN_ZONE_X) ||
                z[0] > readLimitBroadcast(Communication.MAX_ZONE_X) ||
                z[1] < readLimitBroadcast(Communication.MIN_ZONE_Y) ||
                z[1] > readLimitBroadcast(Communication.MAX_ZONE_Y));
    }

    static boolean insideLimits(int[] z){
        return (Math.abs(z[0]) < zoneColumns && Math.abs(z[1]) < zoneRows);
    }
}
