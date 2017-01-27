package Dynamicplayer;

import battlecode.common.*;


public class Gardener {

    private static RobotController rc;

    private static MapLocation realTarget;


    private static int[] zone = ZoneG.nullZone();
    private static int[] zoneIWant = ZoneG.nullZone();


    private static int[] xHex = {0, 0, 1, 1, 0, -1, -1, 0, 1, 2, 2, 2, 1, 0, -1, -2, -2, -2, -1, 0, 1, 2, 3, 3, 3, 3, 2, 1, 0, -1, -2, -3, -3, -3, -3, -2, -1, 0, 1, 2, 3, 4, 4, 4, 4, 4, 3, 2, 1, 0, -1, -2, -3, -4, -4, -4, -4, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 5, 5, 5, 5, 5, 4, 3, 2, 1, 0, -1, -2, -3, -4, -5, -5, -5, -5, -5, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 6, 6, 6, 6, 6, 6, 5, 4, 3, 2, 1, 0, -1, -2, -3, -4, -5, -6, -6, -6, -6, -6, -6, -6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 7, 7, 7, 7, 7, 7, 7, 6, 5, 4, 3, 2, 1, 0, -1, -2, -3, -4, -5, -6, -7, -7, -7, -7, -7, -7, -7, -7, -6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 8, 8, 8, 8, 8, 8, 8, 8, 7, 6, 5, 4, 3, 2, 1, 0, -1, -2, -3, -4, -5, -6, -7, -8, -8, -8, -8, -8, -8, -8, -8, -8, -7, -6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, -1, -2, -3, -4, -5, -6, -7, -8, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -8, -7, -6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, -1, -2, -3, -4, -5, -6, -7, -8, -9, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -9, -8, -7, -6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, -1, -2, -3, -4, -5, -6, -7, -8, -9, -10, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -10, -9, -8, -7, -6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, -1, -2, -3, -4, -5, -6, -7, -8, -9, -10, -11, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -11, -10, -9, -8, -7, -6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, -1, -2, -3, -4, -5, -6, -7, -8, -9, -10, -11, -12, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -12, -11, -10, -9, -8, -7, -6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, -1, -2, -3, -4, -5, -6, -7, -8, -9, -10, -11, -12, -13, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -13, -12, -11, -10, -9, -8, -7, -6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, -1, -2, -3, -4, -5, -6, -7, -8, -9, -10, -11, -12, -13, -14, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -14, -13, -12, -11, -10, -9, -8, -7, -6, -5, -4, -3, -2, -1};
    private static int[] yHex = {0, -1, -1, 0, 1, 1, 0, -2, -2, -2, -1, 0, 1, 2, 2, 2, 1, 0, -1, -3, -3, -3, -3, -2, -1, 0, 1, 2, 3, 3, 3, 3, 2, 1, 0, -1, -2, -4, -4, -4, -4, -4, -3, -2, -1, 0, 1, 2, 3, 4, 4, 4, 4, 4, 3, 2, 1, 0, -1, -2, -3, -5, -5, -5, -5, -5, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 5, 5, 5, 5, 5, 4, 3, 2, 1, 0, -1, -2, -3, -4, -6, -6, -6, -6, -6, -6, -6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 6, 6, 6, 6, 6, 6, 5, 4, 3, 2, 1, 0, -1, -2, -3, -4, -5, -7, -7, -7, -7, -7, -7, -7, -7, -6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 7, 7, 7, 7, 7, 7, 7, 6, 5, 4, 3, 2, 1, 0, -1, -2, -3, -4, -5, -6, -8, -8, -8, -8, -8, -8, -8, -8, -8, -7, -6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 8, 8, 8, 8, 8, 8, 8, 8, 7, 6, 5, 4, 3, 2, 1, 0, -1, -2, -3, -4, -5, -6, -7, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -8, -7, -6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, -1, -2, -3, -4, -5, -6, -7, -8, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -9, -8, -7, -6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, -1, -2, -3, -4, -5, -6, -7, -8, -9, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -10, -9, -8, -7, -6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, -1, -2, -3, -4, -5, -6, -7, -8, -9, -10, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -11, -10, -9, -8, -7, -6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, -1, -2, -3, -4, -5, -6, -7, -8, -9, -10, -11, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -12, -11, -10, -9, -8, -7, -6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, -1, -2, -3, -4, -5, -6, -7, -8, -9, -10, -11, -12, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -13, -12, -11, -10, -9, -8, -7, -6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, -1, -2, -3, -4, -5, -6, -7, -8, -9, -10, -11, -12, -13, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -14, -13, -12, -11, -10, -9, -8, -7, -6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, -1, -2, -3, -4, -5, -6, -7, -8, -9, -10, -11, -12, -13, -14};


    public static void run(RobotController rcc) {
        rc = rcc;
        Initialize();
        while (true) {
            Bot.shake(rc);
            Bot.donate(rc);
            Communication.sendReport(Communication.GARDENER_REPORT);
            MapLocation newTarget = null;
            if (ZoneG.hasValue(zone)) {
                //si soc a la zona
                ZoneG.broadcastMyZone();
                checkNeutralTreesInZone();
                tryPlanting();
                if (rc.getLocation().distanceTo(ZoneG.center) > Constants.eps) {
                    ZoneG.broadcastInfo(zone, Constants.abandonedZone);
                    zone = ZoneG.nullZone();
                    ZoneG.resetMyZone();
                    System.out.println("No esta a la zona, reseteja");
                }
            }else{
                //si no soc a la zona
                newTarget = checkNearbyEnemies(); //si te algun enemic a prop, fuig
                if (newTarget != null){
                    System.out.println("Fuig de " + rc.getLocation() + " a " + newTarget);
                    if (Constants.DEBUG == 1) rc.setIndicatorLine(rc.getLocation(),newTarget, 0, 255, 255);
                }else if (!ZoneG.hasValue(zone)) {
                    //si esta buscant zona
                    if (rc.getRoundNum() % ZoneG.turnsResetZone == 0) zoneIWant = ZoneG.nullZone();
                    zoneIWant = searchZone();
                    if (ZoneG.hasValue(zoneIWant)) {
                        //es posa la zona triada com a objectiu
                        newTarget = ZoneG.center(zoneIWant);
                        System.out.println("Va a zona " + zoneIWant[0] + "," + zoneIWant[1] + "  " + rc.getLocation() + " a " + newTarget);
                        if (Constants.DEBUG == 1) rc.setIndicatorLine(rc.getLocation(), newTarget, 255, 255, 255);
                    }
                    //System.out.println("Soc a la zona "+ getZoneFromPos(rc.getLocation())[0] + "," + getZoneFromPos(rc.getLocation())[1] + " i vull anar a "+zoneIWant[0] + "," + zoneIWant[1]);
                    checkIfArrivedToZone();
                }
            }
            tryConstruct();
            Map.checkMapBounds();
            updateTarget(newTarget);
            waterNearbyTree();
            if (realTarget == null) {
                //if (Constants.DEBUG == 1) rc.setIndicatorDot(rc.getLocation(), 255, 0, 0);
            }else if (realTarget.distanceTo(rc.getLocation()) < Constants.eps){
                Greedy.moveToSelf(rc,Clock.getBytecodesLeft() - 500);
            } else Greedy.moveGreedy(rc, realTarget, Clock.getBytecodesLeft() - 500);
            Clock.yield();
        }
    }

    //nomes es fa la primera ronda
    private static void Initialize(){
        ZoneG.init(rc);
        Map.init(rc);
        Build.init(rc);
        MapLocation base = rc.getInitialArchonLocations(rc.getTeam())[0];
        int xBase = Math.round(base.x);
        int yBase = Math.round(base.y);
        Communication.init(rc,xBase, yBase);
        try {
            float xOrigin = Float.intBitsToFloat(rc.readBroadcast(Communication.ZONE_ORIGIN_X));
            if (xOrigin == 0){
                rc.broadcast(Communication.ZONE_ORIGIN_X, Float.floatToIntBits(rc.getLocation().x));
                rc.broadcast(Communication.ZONE_ORIGIN_Y, Float.floatToIntBits(rc.getLocation().y));
                ZoneG.setOrigin(rc.getLocation().x,rc.getLocation().y);
            }else{
                ZoneG.setOrigin(xOrigin,Float.intBitsToFloat(rc.readBroadcast(Communication.ZONE_ORIGIN_Y)));
            }
        } catch (GameActionException e) {
            e.printStackTrace();
        }
    }

    //retorna una zona del voltant que estigui buida
    private static int[] searchZone() {
        if (ZoneG.hasValue(zoneIWant)) return zoneIWant;
        int[] closest_empty_zone = ZoneG.nullZone();
        float minDist = Constants.INF;
        int[] myZone = ZoneG.getZoneFromPos(rc.getLocation());
        for (int i = 0; i < xHex.length; i++){
            if (i > 25 && ZoneG.hasValue(closest_empty_zone)){
                //nomes busquem zones abandonades fins a 25 pel bytecode
                System.out.println("Retorna closest empty zone = " + closest_empty_zone[0] + "," + closest_empty_zone[1]);
                return closest_empty_zone;
            }
            int[] newZone = ZoneG.newZone(myZone[0] + xHex[i], myZone[1] + yHex[i]);
            if (!ZoneG.insideLimits(newZone)) continue;
            //System.out.println("Prova la zona " + newZone[0] + "," + newZone[1] + " a " + rc.getLocation().distanceTo(getCenterPosFromZone(newZone)));
            int[] zoneInfo = ZoneG.readInfoBroadcast(newZone);
            if (zoneInfo == null) continue;
            int zoneType = zoneInfo[0];
            int lastTurn = zoneInfo[1];
            int thisTurn = rc.getRoundNum();
            if (zoneType != Constants.outOfMapZone) {
                if (!ZoneG.insideLimits(newZone)){
                    ZoneG.broadcastInfo(newZone,Constants.outOfMapZone);
                    zoneType = Constants.outOfMapZone;
                }
            }
            if (zoneType == Constants.busyZone){
                if ((lastTurn & 0x3) == ((thisTurn + 2) & 0x3) || ((lastTurn+3) & 0x3) == (thisTurn & 0x3) ){
                    zoneType = Constants.abandonedZone;
                    ZoneG.broadcastInfo(newZone, Constants.abandonedZone);
                }
            }
            if (zoneType == Constants.abandonedZone){
                return newZone;
            }
            MapLocation newCenter = ZoneG.center(newZone);
            if (Map.distToEdge(newCenter) < 5f) continue;
            float distToZone = rc.getLocation().distanceTo(newCenter);
            if (Constants.DEBUG == 1) rc.setIndicatorDot(newCenter,(int)Math.min(255,distToZone*15),0,0);
            if (zoneType == Constants.emptyZone && distToZone < minDist){
                closest_empty_zone = newZone;
                minDist = distToZone;
            }
        }
        return ZoneG.nullZone();
    }

    //mira si ja esta al centre de la zona a la que estic anant
    private static void checkIfArrivedToZone(){
        MapLocation centerIWant = ZoneG.center(zoneIWant);
        //System.out.println("El centre esta dintre? " + onCurrentMap(centerIWant));
        if (!Map.onCurrentMap(centerIWant)){
            zoneIWant = ZoneG.nullZone();
            return;
        }
        if (rc.canSenseLocation(centerIWant)){ //avisa als lumbers que tallin els arbres d'on vull anar
            TreeInfo[] treesNearCenter = rc.senseNearbyTrees(centerIWant,-1,Team.NEUTRAL);
            ZoneG.messageNeutralTreesInCircle(centerIWant,treesNearCenter);
        }
        if (!rc.canSenseAllOfCircle(centerIWant,rc.getType().bodyRadius)) return;
        int zoneType = ZoneG.readTypeBroadcast(zoneIWant);
        try{
            if (zoneType == Constants.busyZone) {
                zoneIWant = ZoneG.nullZone(); //si la zona esta ocupada, resetejo
                return;
            }
            if (!rc.onTheMap(centerIWant,rc.getType().bodyRadius)){
                ZoneG.broadcastInfo(zoneIWant,Constants.outOfMapZone);
                zoneIWant = ZoneG.nullZone(); //si esta fora del mapa, resetejo
                return;
            }
            if (Map.distToEdge(centerIWant) < 5){
                ZoneG.broadcastInfo(zoneIWant,Constants.outOfMapZone);
                zoneIWant = ZoneG.nullZone(); //aixo es un parche que he ficat, nose si esta be fer-ho
                return;
            }
            //System.out.println("El punt " + centerIWant + " esta dintre el mapa");
            if (Constants.DEBUG == 1) rc.setIndicatorDot(centerIWant,255,255,255);
            if (rc.getLocation().distanceTo(centerIWant) < Constants.eps) {
                zone = zoneIWant;
                ZoneG.assign(zoneIWant);
            }
        }catch (GameActionException e){
            e.printStackTrace();
        }
    }

    //rega l'arbre amb menys vida
    private static void waterNearbyTree(){
        if (!rc.canWater()) return;
        TreeInfo[] myTrees = rc.senseNearbyTrees(rc.getType().bodyRadius + Constants.interaction_dist_from_edge, rc.getTeam());
        float minHP = Constants.INF;
        int minID = -1;
        for (TreeInfo tree: myTrees){
            if (tree.getHealth() < minHP){
                minHP = tree.getHealth();
                minID = tree.getID();
            }
        }
        if (minID != -1) try {
            //if (Constants.DEBUG == 1) rc.setIndicatorDot(rc.senseTree(minID).getLocation(),0, 255, 0);
            rc.water(minID);
        } catch (GameActionException e) {
            e.printStackTrace();
        }
    }

    private static void checkNeutralTreesInZone(){
        TreeInfo[] neutralTrees = rc.senseNearbyTrees(-1,Team.NEUTRAL);
        ZoneG.messageNeutralTreesInCircle(ZoneG.center(),neutralTrees);
    }

    //si hi ha enemics, pondera les distancies i fuig cap a la direccio oposada
    private static MapLocation checkNearbyEnemies(){
        //return null;
        RobotInfo[] enemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        MapLocation myPos = rc.getLocation();
        MapLocation escapePos = rc.getLocation();
        //System.out.println("Numero enemics: " + enemies.length);
        for (RobotInfo enemy: enemies){
            if (enemy.getType() == RobotType.ARCHON || enemy.getType() == RobotType.GARDENER) continue;
            Communication.sendMessage(Communication.EMERGENCYCHANNEL,Math.round(enemy.getLocation().x),Math.round(enemy.getLocation().y),0);
            Direction enemyDir = myPos.directionTo(enemy.getLocation());
            escapePos = escapePos.add(enemyDir, -1/(1 + myPos.distanceTo(enemy.getLocation())));
        }
        //System.out.println("Escape pos: " + escapePos);
        if (myPos.isWithinDistance(escapePos, Constants.eps)) return null;
        escapePos = myPos.add(myPos.directionTo(escapePos), 6);

        //rc.setIndicatorLine(myPos,escapePos, 0,255,255);
        //return null;
        return escapePos;
    }

    private static void tryPlanting(){
        //System.out.println("Entra plantar");
        if (rc.getRoundNum() > Constants.LAST_ROUND_BUILD) return;
        if (ZoneG.countAvailableRobotBuildPositions() < 2) return; //Si nomes hi ha una posicio, la reservem per robots
        if (rc.getLocation().distanceTo(ZoneG.center) > Constants.eps){
            System.out.println("No planto perque no soc al centre");
            return;
        }
        if (!Build.allowedToConstruct(Constants.TREE)) {
            //System.out.println("No tinc prou bullets per plantar");
            return; //comprova bullets
        }
        int index = ZoneG.indexToPlant(); //si hi ha algun arbre no ocupat
        //System.out.println("Planta l'arbre " + index);
        if (index == -1) return;
        MapLocation myPos = rc.getLocation();
        Direction plantingDirection = myPos.directionTo(ZoneG.hexPos[index]);
        if (rc.getLocation().distanceTo(myPos) < Constants.eps && rc.canPlantTree(plantingDirection)){
            try {
                //Planta l'arbre
                rc.plantTree(plantingDirection);
                Build.incrementTreesBuilt();
                Build.updateAfterConstruct(Constants.TREE);
            } catch (GameActionException e) {
                e.printStackTrace();
            }
        }
    }


    private static void tryConstruct(){
        //System.out.println("Entra construct");
        if (rc.getRobotCount() > Constants.MAX_ROBOTS) return;
        if (rc.getRoundNum() > Constants.LAST_ROUND_BUILD) return;
        int smallUnit = Build.bestSmallUnitToBuild();
        int firstUnit = -1;
        int secondUnit = -1;
        try {
            int tankIndex = rc.readBroadcast(Communication.unitChannels[Constants.TANK]);
            int smallUnitIndex = rc.readBroadcast(Communication.unitChannels[smallUnit]);
            //System.out.println("tankindex "+ tankIndex + " unitindex " + smallUnitIndex);
            if (tankIndex < smallUnitIndex) { //decideix si es mes prioritari fer tank o fer una altra cosa
                firstUnit = Constants.TANK;
                secondUnit = smallUnit;
            }else{
                firstUnit = smallUnit;
                secondUnit = Constants.TANK;
            }
        } catch (GameActionException e) {
            e.printStackTrace();
        }
        //System.out.println("Tria les units " + firstUnit + ", " + secondUnit);
        tryConstructUnit(firstUnit);
        tryConstructUnit(secondUnit);
    }

    private static void tryConstructUnit(int unit){
        if (!rc.isBuildReady()) return;
        if (unit == -1) return;
        if (!Build.allowedToConstruct(unit)) {
            //System.out.println("No tinc prou bales per construir " + unit);
            //System.out.println("Tinc " + rc.getTeamBullets() + " i calen " + totalBulletCost(unit));
            return;
        }
        RobotType newRobotType = Constants.getRobotTypeFromIndex(unit);
        Direction enemyDir = rc.getLocation().directionTo(rc.getInitialArchonLocations(rc.getTeam().opponent())[0]);
        for (int i = 0; i < 24; i++){
            Direction d2 = enemyDir.rotateLeftDegrees(360*i/12);
            if (rc.canBuildRobot(newRobotType,d2)){
                try {
                    rc.buildRobot(Constants.getRobotTypeFromIndex(unit),d2);
                } catch (GameActionException e) {
                    e.printStackTrace();
                }
                Build.incrementRobotsBuilt();
                Build.updateAfterConstruct(unit);
            }
            d2 = enemyDir.rotateRightDegrees(360*i/12);
            if (rc.canBuildRobot(newRobotType,d2)){
                try {
                    rc.buildRobot(Constants.getRobotTypeFromIndex(unit),d2);
                } catch (GameActionException e) {
                    e.printStackTrace();
                }
                Build.incrementRobotsBuilt();
                Build.updateAfterConstruct(unit);
            }
        }
    }
/*
    private static MapLocation getBuildPositionWithoutZone(int unit){
        Direction back = rc.getLocation().directionTo(ZoneG.center(zoneIWant)).opposite();
        RobotType type = Constants.getRobotTypeFromIndex(unit);
        for (int i = 0; i < 10; i++){
            Direction dirBuild = back.rotateRightRads(i*(float)Math.PI/20);
            if (rc.canBuildRobot(type,dirBuild)) return rc.getLocation().add(dirBuild,rc.getType().bodyRadius + type.bodyRadius);
            Direction dirBuildInv = back.rotateLeftRads(i*(float)Math.PI/20);
            if (rc.canBuildRobot(type,dirBuildInv)) return rc.getLocation().add(dirBuildInv,rc.getType().bodyRadius + type.bodyRadius);
        }
        return null;
    }
   */

    private static void updateTarget(MapLocation newTarget){
        if (realTarget != null && newTarget != null && newTarget.distanceTo(realTarget) < Constants.eps) return;
        realTarget = newTarget;
        //Greedy.resetObstacle(rc);
    }
}
