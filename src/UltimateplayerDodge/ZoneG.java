package UltimateplayerDodge;

import battlecode.common.*;

/**
 * Created by Pau on 24/01/2017.
 */
public class ZoneG {

    private static RobotController rc = null;
    private static int[] zone;

    static MapLocation center;


    private static int initialMessageGardCount = 0;
    private static int initialMessageClosedGard = 0;

    private static float zoneOriginX;
    private static float zoneOriginY;
    static MapLocation zoneOrigin;
    private static Direction enemyDir;

    private static int zoneRows = 44;
    private static int zoneColumns = 44;
    private static int zonesPerChannel = 6;

    static int freeSpots;

    private static int bitsZoneType = 3;
    private static int bitsZoneTurn = 2;
    private static int bitsPerZone = bitsZoneType + bitsZoneTurn;

    private static int treesPerZone = 6;
    static int buildPositionsPerZone = 6;

    static int turnsResetZone = 30;

    static MapLocation[] hexPos = new MapLocation[6];
    static MapLocation[] neighbors = new MapLocation[6];
    private static MapLocation[] newTankPos = new MapLocation[buildPositionsPerZone];

    // 0 = lliure, 1 = arbre aliat, 2 = altre arbre, 3 = tropa, 4 = fora mapa
    static int[] surroundings = new int[5]; //surroundings[2] = 3 => hi ha 3 arbres no aliats al voltant
    static int[] hexStatus = new int[6]; //hexStatus[2] = 3 => a la hexPos[2] hi ha una tropa

    static RobotInfo[] allies;
    static RobotInfo[] enemies;
    static TreeInfo[] neutralTrees;
    static TreeInfo[] allTrees;

    static int aliveGardeners;
    static int closedGardeners;

    static void init(RobotController rc2){
        rc = rc2;
        try {
            initialMessageGardCount = rc.readBroadcast(Communication.GARD_COUNT + Communication.CYCLIC_CHANNEL_LENGTH);
            initialMessageClosedGard = rc.readBroadcast(Communication.CLOSED_GARDENERS + Communication.CYCLIC_CHANNEL_LENGTH);
        } catch (GameActionException e) {
            e.printStackTrace();
        }
    }

    static void initTurn() {
        allies = rc.senseNearbyRobots(-1, rc.getTeam());
        enemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        neutralTrees = rc.senseNearbyTrees(-1, Team.NEUTRAL);
        allTrees = rc.senseNearbyTrees();
        readMessages();
        if (!hasValue(zone)) freeSpots = 6;
        else freeSpots = freeSpots();
        System.out.println("Envia "+ freeSpots + " free spots");
        Communication.sendMessage(Communication.GARD_FREE_SPOTS,Math.round(rc.getLocation().x),Math.round(rc.getLocation().y),freeSpots);
        if (surroundings[1] + surroundings[4] == 6){
            System.out.println("Envia pages tancat");
            Communication.sendMessage(Communication.CLOSED_GARDENERS,Math.round(rc.getLocation().x),Math.round(rc.getLocation().y),0);
        }
    }

    static void readMessages(){
        try {
            int channel = Communication.GARD_COUNT;
            int lastMessage = rc.readBroadcast(channel + Communication.CYCLIC_CHANNEL_LENGTH);
            int count = lastMessage - initialMessageGardCount;
            if (count < 0) count += Communication.CYCLIC_CHANNEL_LENGTH;
            System.out.println(aliveGardeners + " alive gardeners");
            aliveGardeners = count;
            initialMessageGardCount = lastMessage;
        } catch (GameActionException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        try {
            int channel = Communication.CLOSED_GARDENERS;
            int lastMessage = rc.readBroadcast(channel + Communication.CYCLIC_CHANNEL_LENGTH);
            int count = lastMessage - initialMessageClosedGard;
            if (count < 0) count += Communication.CYCLIC_CHANNEL_LENGTH;
            System.out.println(closedGardeners + " closed gardeners");
            closedGardeners = count;
            initialMessageClosedGard = lastMessage;
        } catch (GameActionException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
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
        float d = 5.5f; //arrel de 28 + epsilon
        //float d = 3.5f; //2sqrt3
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
        //float a00 = 2f/7f;
        //float a01 = -1f/6f;
        //float a10 = 0f;
        //float a11 = 1f/3f;
        float a00 = 0.18898f; //1/(float)Math.sqrt(28);
        float a01 = -0.10911f; //-1/(float)Math.sqrt(84);
        float a10 = 0f;
        float a11 = 0.21822f; //2/(float)Math.sqrt(84);
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
        //float a = (float)Math.PI/6; //ara l'angle es 30
        float a = 0.713724379f; //radiants de desfase = arcsin(sqrt(3/7))
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
            neighbors[i] = center.add(Direction.EAST.rotateLeftRads((float)Math.PI*i/3),5.5f);
            newTankPos[i] = center.add(dBase.rotateLeftRads((float)Math.PI/6).rotateLeftRads((float)Math.PI*i/3),3.02f);
        }

    }

    //envia missatge de tallar els arbres en el cercle de radi 3 i si no n'hi ha cap en el de radi 5
    static void messageNeutralTreesInCircle(MapLocation center, TreeInfo[] trees){
        int max_bytecode = 4000;
        int bytecode_init = Clock.getBytecodeNum();
        MapLocation[] outerTrees = new MapLocation[trees.length];
        boolean sendOuterTrees = true;
        float innerDistance = 3f;
        float outerDistance = 5f;
        int outerTreeCount = 0;
        for (TreeInfo ti: trees){
            if (Clock.getBytecodeNum() - bytecode_init > max_bytecode) return;
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

        for (int i = 0; i < 6; i++){
            int j = (i+1)%6;
            if (hexStatus[i] == 0){
                try {
                    //RobotInfo r1 = rc.senseRobotAtLocation(ZoneG.neighbors[i]);
                    RobotInfo r2 = rc.senseRobotAtLocation(ZoneG.neighbors[j]);
                    //if (r1 != null && r1.getTeam() == rc.getTeam() && r1.getType() == RobotType.GARDENER) return i;
                    if (r2 != null && r2.getTeam() == rc.getTeam() && r2.getType() == RobotType.GARDENER) return i;
                    Direction dirHex = rc.getLocation().directionTo(hexPos[i]);
                    if (!rc.onTheMap(rc.getLocation().add(dirHex, 5f))) return i;
                } catch (GameActionException e) {
                    e.printStackTrace();
                }
            }
        }

        if (surroundings[0] == 1 && !shouldBuildSixTrees()) return -1;

        float maxAngle = 0;
        int bestIndex = -1;
        for (int i = 0; i < 6; i++){
            if (hexStatus[i] != 0) continue;
            Direction d = rc.getLocation().directionTo(hexPos[i]);
            if (Math.abs(d.radiansBetween(enemyDir)) > maxAngle){
                maxAngle = Math.abs(d.radiansBetween(enemyDir));
                bestIndex = i;
            }
        }
        return bestIndex;
    }


    private static boolean shouldBuildSixTrees(){
        float minHP = 10;
        if (rc.getHealth() < minHP) return true;
        //float ratio = (float)closedGardeners / (float)aliveGardeners;
        //if (ratio < 0.2 && aliveGardeners - closedGardeners > 1) return true;
        MapLocation myPos = rc.getLocation();
        for (RobotInfo enemy: ZoneG.enemies){
            if (enemy.getType() != RobotType.SCOUT && myPos.distanceTo(enemy.getLocation()) < 5) return true;
        }
        return false;
    }


    static boolean shouldRequestLumberjack(){
        //si no te cap lloc lliure i hi ha algun arbre neutral/enemic
        if (surroundings[2] != 0 && surroundings[0] <= 1) return true;
        if (rc.getTeamBullets() < 500) return false;
        TreeInfo[] trees = rc.senseNearbyTrees(5f, Team.NEUTRAL);
        //fem request quan hi ha un arbre a distancia <5 i tenim moltes bullets
        return trees.length > 0;
    }

    private static int freeSpots(){
        int frees = 0;
        //int myTrees = 0;
        surroundings = new int[]{0,0,0,0,0};
        for (int i = 0; i < 6; i++){
            int obstacle = isFree(ZoneG.hexPos[i], GameConstants.BULLET_TREE_RADIUS);
            surroundings[obstacle]++;
            hexStatus[i] = obstacle;
            if (obstacle == 0){
                frees++;
            }//else if (obstacle == 1) myTrees++;
        }
        System.out.println("Surroundings: " + surroundings[0] + "," + surroundings[1] + "," + surroundings[2] + "," + surroundings[3] + "," + surroundings[4]);
        System.out.println("Hexes: " + hexStatus[0] + "," + hexStatus[1] + "," + hexStatus[2] + "," + hexStatus[3] + "," + hexStatus[4] + "," + hexStatus[5]);
        //if (myTrees == 5) return 0;
        return frees;
    }

    private static int isFree(MapLocation pos, float r){
        // 0 = lliure, 1 = arbre aliat, 2 = altre arbre, 3 = tropa, 4 = fora mapa
        if (Map.distToEdge(pos) <= r) return 4;
        for (TreeInfo tree: allTrees){
            if (tree.getLocation().distanceTo(pos) <= tree.getRadius() + r) {
                if (tree.getTeam() == rc.getTeam()) return 1;
                return 2;
            }
        }
        for (RobotInfo enemy: enemies){
            if (enemy.getLocation().distanceTo(pos) <= enemy.getRadius() + r) {
                return 3;
            }
        }
        for (RobotInfo enemy: allies){
            if (enemy.getLocation().distanceTo(pos) <= enemy.getRadius() + r) {
                return 3;
            }
        }
        return 0;
    }

    static boolean insideLimits(int[] z){
        return Map.onCurrentMap(center(z), rc.getType().bodyRadius);
    }

}
