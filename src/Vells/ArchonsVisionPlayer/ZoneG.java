package Vells.ArchonsVisionPlayer;

import battlecode.common.*;

/**
 * Created by Pau on 24/01/2017.
 */
public class ZoneG {

    private static RobotController rc = null;
    private static int[] zone;

    static MapLocation center;

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
    static int buildPositionsPerZone = 6;

    static int turnsResetZone = 50;

    static MapLocation[] hexPos = new MapLocation[6];
    private static MapLocation[] newTankPos = new MapLocation[buildPositionsPerZone];


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

    //centre de la zona z
    static MapLocation center(int[] z){
        //float d = 5.5f; //arrel de 28 + epsilon
        float d = 3.5f; //2sqrt3
        Direction v1 = Direction.EAST;
        Direction v2 = v1.rotateLeftRads((float)Math.PI/3); //Aquests dos vectors son la base de coordenades de les zones
        return zoneOrigin.add(v1,d * z[0]).add(v2,d*z[1]);
    }

    //centre de la meva zona
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

    //diu a quina zona pertany una posicio
    //els calculs son una merde pero estan be, els he trobat per internet
    static int[] getZoneFromPos(MapLocation pos){
        int[] ret = new int[2];
        float a00 = 2f/7f; //1/(float)Math.sqrt(28);
        float a01 = -1f/6f; //-1/(float)Math.sqrt(84);
        float a10 = 0f;
        float a11 = 1f/3f; //2/(float)Math.sqrt(84);
        float x = a00 * (pos.x - zoneOriginX) + a01 * (pos.y - zoneOriginY);
        float y = a10 * (pos.x - zoneOriginX) + a11 * (pos.y - zoneOriginY);
        float z = -x-y;
        int rx = Math.round(x);
        int ry = Math.round(y);
        int rz = Math.round(z);
        float dx = Math.abs(rx - x);
        float dy = Math.abs(ry - y);
        float dz = Math.abs(rz - z);
        if (dx > dy && dx > dz){
            rx = -ry-rz;
        }else if (dy > dz) {
            ry = -rx - rz;
        }
        ret[0] = rx;
        ret[1] = ry;
        return ret;
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

    //broadcast d'un tipus de zona
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

    //assigna una zona a la variable zone i inicialitza la resta de coses
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

    //envia missatge de tallar els arbres en el cercle de radi 3 i si no n'hi ha cap en el de radi 5
    static void messageNeutralTreesInCircle(MapLocation center, TreeInfo[] trees){
        int max_bytecode = 3000;
        int bytecode_init = Clock.getBytecodeNum();
        MapLocation[] outerTrees = new MapLocation[trees.length];
        boolean sendOuterTrees = true;

        float innerDistance = 3f;
        float outerDistance = 5f;

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

    private static void messageCutNeutralTree(MapLocation treeLocation) {
        if (Constants.DEBUG == 1) rc.setIndicatorDot(treeLocation,255,120,0);
        Communication.sendMessage(Communication.CHOPCHANNEL,Math.round(treeLocation.x),Math.round(treeLocation.y),0);
    }

    //conta a quants dels 6 forats pot construir un robot
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
                        !rc.isCircleOccupiedExceptByThisRobot(hexPos[i],RobotType.SOLDIER.bodyRadius)) {
                    //if (Constants.DEBUG == 1) rc.setIndicatorDot(hexPos[i],0,255,0);
                    count++;
                }//else if (Constants.DEBUG == 1) rc.setIndicatorDot(hexPos[i],255,0,0);
            } catch (GameActionException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Te " + count + " posicions per construir");
        return count;
    }

    //retorna el millor index per plantar un arbre
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
            float enemy_angle = 60;
            int min_turn_tank = 700;
            int low_HP = 10;
            if (rc.getRoundNum() < min_turn_tank) enemy_angle = 30;
            if (rc.getHealth() > low_HP && Math.abs(d.degreesBetween(enemyDir)) < enemy_angle) continue;
            try {
                if (rc.isCircleOccupiedExceptByThisRobot(hexPos[i],GameConstants.BULLET_TREE_RADIUS)) continue;
                return i;
            } catch (GameActionException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    static boolean insideLimits(int[] z){
        return Map.onCurrentMap(center(z), rc.getType().bodyRadius);
    }
    
    static MapLocation centerArchon(int[] z) {
    	//float d = 5.5f; //arrel de 28 + epsilon
        float d = 3.5f; //2sqrt3
        Direction v1 = Direction.EAST;
        Direction v2 = v1.rotateLeftRads((float)Math.PI/3); //Aquests dos vectors son la base de coordenades de les zones
        return rc.getLocation().add(v1,d * z[0]).add(v2,d*z[1]);
    }
}
