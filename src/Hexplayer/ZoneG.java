package Hexplayer;

import battlecode.common.*;

/**
 * Created by Pau on 24/01/2017.
 */
public class ZoneG {

    private static RobotController rc = null;
    private static int[] zone;

    static MapLocation center;

    //private static float STREET_WIDTH = 5f;
    //private static float STREET_HEIGHT = 5f;
    //private static float BLOCK_WIDTH = 6f;
    //private static float BLOCK_HEIGHT = 6f;
    //private static float zoneWidth = STREET_WIDTH + BLOCK_WIDTH;
    //private static float zoneHeight = STREET_HEIGHT + BLOCK_HEIGHT;

    private static float zoneOriginX;
    private static float zoneOriginY;
    static MapLocation zoneOrigin;
    private static Direction enemyDir;

    private static int zoneRows = 44;
    private static int zoneColumns = 44;
    private static int zonesPerChannel = 6;

    private static int bitsZoneType = 3;
    private static int bitsZoneTurn = 2;
    private static int bitsPerZone = bitsZoneType + bitsZoneTurn;

    private static int treesPerZone = 6; // !!!
    private static int buildPositionsPerZone = 6;

    static int turnsResetZone = 45;

    static MapLocation[] hexPos = new MapLocation[6];
    //static MapLocation[] treePos = new MapLocation[treesPerZone];
    //static MapLocation plantingPos;
    //static MapLocation wateringPos;
    //static MapLocation buildPos = new MapLocation;
    //static MapLocation[] newRobotPos = new MapLocation[buildPositionsPerZone];
    //static MapLocation buildTankPos = new MapLocation;
    static MapLocation[] newTankPos = new MapLocation[buildPositionsPerZone];
    //static float treeHP[] = new float[treesPerZone];
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
        //float d = 5.5f; //arrel de 28 + epsilon
        float d = 3.5f; //2sqrt3
        Direction v1 = Direction.EAST;
        Direction v2 = v1.rotateLeftRads((float)Math.PI/3); //Aquests dos vectors son la base de coordenades de les zones
        return zoneOrigin.add(v1,d * z[0]).add(v2,d*z[1]);
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

    static void setOrigin(float x, float y) {
        zoneOriginX = x;
        zoneOriginY = y;
        zoneOrigin = new MapLocation(zoneOriginX,zoneOriginY);
    }

    static int[] getZoneFromPos(MapLocation pos){
        System.out.println("AAAAA");
        int[] z = new int[2];
        float a00 = 2f/7f; //1/(float)Math.sqrt(28);
        float a01 = -1f/6f; //-1/(float)Math.sqrt(84);
        float a10 = 0f;
        float a11 = 1f/3f; //2/(float)Math.sqrt(84);
        System.out.println(a00 * (pos.x - zoneOriginX));
        z[0] = Math.round(a00 * (pos.x - zoneOriginX) + a01 * (pos.y - zoneOriginY));
        z[1] = Math.round(a10 * (pos.x - zoneOriginX) + a11 * (pos.y - zoneOriginY)); //arrodonit no esta del tot be pero close enough
        return z;
    }


    static int getID(int[] z){
        //rang de zoneid:
        // inici: -22 + 44*(-22) + 44*44/2 + 22 = 0
        // final: 22 + 44*22 + 44*44/2 + 22 = 1980
        // amb 6 zones per canal fan falta 330 canals
        return z[0] + zoneColumns * z[1] + zoneColumns*zoneRows/2 + 22;
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

    /*
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
*/
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
        float a = (float)Math.PI/6; //ara l'angle es 30 /// 0.713724379f; //radiants de desfase = arcsin(sqrt(3/7))
        Direction dBase = new Direction(a);



        zone = assignedZone;
        MapLocation myPos = rc.getLocation();
        MapLocation[] enemies = rc.getInitialArchonLocations(rc.getTeam().opponent());
        MapLocation enemyPos = rc.getLocation();
        //System.out.println("Numero enemics: " + enemies.length);
        for (MapLocation enemy: enemies){
            Direction enemyDir = myPos.directionTo(enemy);
            enemyPos = enemyPos.add(enemyDir, 1/(1 + myPos.distanceTo(enemy)));
        }
        enemyDir = myPos.directionTo(enemyPos);



        center = center(zone);
        broadcastInfo(assignedZone, Constants.busyZone);

        for (int i = 0; i < treesPerZone; i++){
            hexPos[i] = center.add(dBase.rotateLeftRads((float)Math.PI*i/3),2.01f);
            newTankPos[i] = center.add(dBase.rotateLeftRads((float)Math.PI/6).rotateLeftRads((float)Math.PI*i/3),3.02f);
        }

    }
/*
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
*/

    //envia missatge de tallar els arbres en la capsa de 3x3 i si no n'hi ha cap en la de 5.5x5.5
    static void messageNeutralTreesInCircle(MapLocation center, TreeInfo[] trees){
        int max_bytecode = 3000;
        int bytecode_init = Clock.getBytecodeNum();
        MapLocation[] outerTrees = new MapLocation[trees.length];
        boolean sendOuterTrees = true;

        float innerDistance = 3f;
        float outerDistance = 6.3f; //perque hi capiguen els pagesos veins = sqrt(28)+1

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
/*
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
*/
//arreglarla
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
                if (rc.canSenseAllOfCircle(hexPos[i],RobotType.SOLDIER.bodyRadius) &&
                        !rc.isCircleOccupiedExceptByThisRobot(hexPos[i],RobotType.SOLDIER.bodyRadius)) count++;
            } catch (GameActionException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Te " + count + " posicions per construir");
        return count;
    }

    static int indexToPlant(){
        //quan arriba aqui esta garantit que som al centre
        if (!hasValue(zone)){
            try {
                throw new GameActionException(GameActionExceptionType.CANT_DO_THAT,"ERROR: crida de indexToPlant() sense tenir zona assignada");
            } catch (GameActionException e) {
                e.printStackTrace();
            }
            return -1;
        }
        for (int i = 0; i < treesPerZone; i++){
            if (Map.distToEdge(hexPos[i]) < 5f) {
                //System.out.println("arbre " + i + " fora del mapa");
                continue;
            }
            Direction d = rc.getLocation().directionTo(hexPos[i]);
            if (Math.abs(d.degreesBetween(enemyDir)) < 60) continue;
            try {
                if (rc.isCircleOccupiedExceptByThisRobot(hexPos[i],GameConstants.BULLET_TREE_RADIUS)) continue;
                return i;
            } catch (GameActionException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    static MapLocation posToBuild(int unit){
        if (!hasValue(zone)){
            try {
                throw new GameActionException(GameActionExceptionType.CANT_DO_THAT,"ERROR: crida de indexToBuild() sense tenir zona assignada");
            } catch (GameActionException e) {
                e.printStackTrace();
            }
            return null;
        }
        if (unit == Constants.GARDENER) return null;
        //que sigui el mes proper a l'enemic?
        MapLocation myPos = rc.getLocation();
        MapLocation robotSpawnPos[] = new MapLocation[6];
        float newRobotRadius;
        if (unit == Constants.TANK){
            robotSpawnPos = newTankPos;
            newRobotRadius = RobotType.TANK.bodyRadius;
        }else{
            robotSpawnPos = hexPos;
            newRobotRadius = RobotType.SOLDIER.bodyRadius;
        }

        float a = (float)Math.PI/6; //ara l'angle es 30 /// 0.713724379f; //radiants de desfase = arcsin(sqrt(3/7))
        Direction dBase = new Direction(a);
        for (int i = 0; i < 6; i++){
            if (unit == Constants.TANK){
                robotSpawnPos[i] = myPos.add(dBase.rotateLeftRads((float)Math.PI/6).rotateLeftRads((float)Math.PI*i/3),3.02f);
            }
        }
        //float minDist = Constants.INF;
        //int minIndex = -1;

        for (int i = 0; i < 6; i++){
            if (!Map.onCurrentMap(robotSpawnPos[i])) { //System.out.println(i + " fora del mapa");
                continue;
            }
            try {
                if (!rc.onTheMap(robotSpawnPos[i], newRobotRadius) ||
                        rc.isCircleOccupiedExceptByThisRobot(robotSpawnPos[i],newRobotRadius)) {
                    //System.out.println("newrobotpos bloquejada "+ robotSpawnPos[i]);
                    continue;
                }
                return robotSpawnPos[i];
            } catch (GameActionException e) {
                e.printStackTrace();
            }
        }
        //System.out.println("min index de build = "+minIndex);
        return null;
    }

    static boolean insideLimits(int[] z){
        return Map.onCurrentMap(center(z), rc.getType().bodyRadius);
    }
}
