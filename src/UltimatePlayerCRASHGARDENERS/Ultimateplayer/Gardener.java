package UltimatePlayerCRASHGARDENERS.Ultimateplayer;

import battlecode.common.*;


public class Gardener {



    private static RobotController rc;

    private static MapLocation realTarget;
    private static int initialMessageNeedTroop = 0;
    private static int initialMessageEmergency = 0;
    static boolean lumberjackBuilt = false;
    static boolean shouldBuildTroop = false;
    static boolean shouldBuildLumber = false;
    static boolean shouldBuildScout = false;
    static boolean myFirstTurn = true;
    private static boolean firstGardener;

    private static int[] firstRushQueue = {2,5,2,5,5,2,5,5};
    private static int[] firstQueue = {5,2,5,2,5,5,2,5};
    private static int[] normalQueue = {5,2,5,5,5,5};
    private static int[] myQueue;
    private static int soldiersSkipped = 0;
    private static int queueIndex = 0;

    private static int[] zone = ZoneG.nullZone();
    private static int[] zoneIWant = ZoneG.nullZone();

    private static int[] xHex = {0, 0, 1, 1, 0, -1, -1, 0, 1, 2, 2, 2, 1, 0, -1, -2, -2, -2, -1, 0, 1, 2, 3, 3, 3, 3, 2, 1, 0, -1, -2, -3, -3, -3, -3, -2, -1, 0, 1, 2, 3, 4, 4, 4, 4, 4, 3, 2, 1, 0, -1, -2, -3, -4, -4, -4, -4, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 5, 5, 5, 5, 5, 4, 3, 2, 1, 0, -1, -2, -3, -4, -5, -5, -5, -5, -5, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 6, 6, 6, 6, 6, 6, 5, 4, 3, 2, 1, 0, -1, -2, -3, -4, -5, -6, -6, -6, -6, -6, -6, -6, -5, -4, -3, -2, -1};
    private static int[] yHex = {0, -1, -1, 0, 1, 1, 0, -2, -2, -2, -1, 0, 1, 2, 2, 2, 1, 0, -1, -3, -3, -3, -3, -2, -1, 0, 1, 2, 3, 3, 3, 3, 2, 1, 0, -1, -2, -4, -4, -4, -4, -4, -3, -2, -1, 0, 1, 2, 3, 4, 4, 4, 4, 4, 3, 2, 1, 0, -1, -2, -3, -5, -5, -5, -5, -5, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 5, 5, 5, 5, 5, 4, 3, 2, 1, 0, -1, -2, -3, -4, -6, -6, -6, -6, -6, -6, -6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 6, 6, 6, 6, 6, 6, 5, 4, 3, 2, 1, 0, -1, -2, -3, -4, -5};


    public static void run(RobotController rcc) {
        rc = rcc;
        Initialize();
        while (true) {
            System.out.println("init bucle " + Clock.getBytecodeNum());
            initTurn();
            System.out.println("after init turn " + Clock.getBytecodeNum());
            MapLocation newTarget = checkNearbyEnemies(); //si te algun enemic a prop, fuig
            System.out.println("Despres check nearby enemies " + Clock.getBytecodeNum());
            if (ZoneG.hasValue(zone)) {
                //si soc a la zona
                ZoneG.broadcastMyZone();
                checkNeutralTreesInZone();
                if (rc.getLocation().distanceTo(ZoneG.center) > Constants.eps) {
                    ZoneG.broadcastInfo(zone, Constants.abandonedZone);
                    zone = ZoneG.nullZone();
                    ZoneG.resetMyZone();
                    System.out.println("No esta a la zona, reseteja");

                }
                System.out.println("Post analisi zona: " + Clock.getBytecodeNum());
            }else{
                System.out.println("no te zona");
                //si no soc a la zona
                if (newTarget != null){
                    System.out.println("Fuig de " + rc.getLocation() + " a " + newTarget);
                    if (Constants.DEBUG == 1) rc.setIndicatorLine(rc.getLocation(),newTarget, 0, 255, 255);
                }else if (!ZoneG.hasValue(zone)) {
                    //si esta buscant zona
                    if (rc.getRoundNum() % ZoneG.turnsResetZone == 0)
                    {
                        zoneIWant = ZoneG.nullZone();
                        System.out.println("Ha assignat null zone a zone I want: "+ Clock.getBytecodeNum());
                    }
                    System.out.println("va a buscar zona: " + Clock.getBytecodeNum());
                    zoneIWant = searchZone();
                    System.out.println("SearchoZone: "+ Clock.getBytecodeNum());
                    if (ZoneG.hasValue(zoneIWant)) {
                        //es posa la zona triada com a objectiu
                        newTarget = ZoneG.center(zoneIWant);
                        System.out.println("Busca centre zona : "+ Clock.getBytecodeNum());
                        System.out.println("Va a zona " + zoneIWant[0] + "," + zoneIWant[1] + "  " + rc.getLocation() + " a " + newTarget);
                        if (Constants.DEBUG == 1) rc.setIndicatorLine(rc.getLocation(), newTarget, 255, 255, 255);
                    }
                    //System.out.println("Soc a la zona "+ getZoneFromPos(rc.getLocation())[0] + "," + getZoneFromPos(rc.getLocation())[1] + " i vull anar a "+zoneIWant[0] + "," + zoneIWant[1]);
                    checkIfArrivedToZone();
                    System.out.println("Check If arrived : "+ Clock.getBytecodeNum());
                }
            }
            System.out.println("Despres ifos" + Clock.getBytecodeNum());
            tryConstruct();
            updateTarget(newTarget);
            waterNearbyTree();
            if (realTarget == null) {
                //if (Constants.DEBUG == 1) rc.setIndicatorDot(rc.getLocation(), 255, 0, 0);
            }else if (realTarget.distanceTo(rc.getLocation()) < Constants.eps){
                Greedy.moveToSelf(rc,13000);
            } else Greedy.moveGreedy(rc, realTarget, 13000);

            myFirstTurn = false;
            Clock.yield();
        }
    }

    //nomes es fa la primera ronda
    private static void Initialize(){
        if (rc.getRoundNum() < 5) {
            firstGardener = true;
            MapLocation[] enemies = rc.getInitialArchonLocations(rc.getTeam().opponent());
            float minDist = 9999;
            for (MapLocation enemy: enemies) minDist = Math.min(minDist, rc.getLocation().distanceTo(enemy));
            if (minDist < 30) myQueue = firstRushQueue;
            else myQueue = firstQueue;
        }else {
            firstGardener = false;
            myQueue = normalQueue;
        }
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
                System.out.println("xOrigin: " + xOrigin);
                System.out.println("yOrigin: " + Float.intBitsToFloat(rc.readBroadcast(Communication.ZONE_ORIGIN_Y)));
            }
        } catch (GameActionException e) {
            e.printStackTrace();
        }
    }

    private static void initTurn(){
        Bot.shake(rc);
        Bot.donate(rc);
        Map.checkMapBounds();
        Communication.sendMessage(Communication.GARD_COUNT,Math.round(rc.getLocation().x),Math.round(rc.getLocation().y),0);
        shouldBuildLumber = false;
        shouldBuildTroop = false;
        System.out.println("Before read " + Clock.getBytecodeNum());
        readMessages();
        System.out.println("Before zone init " + Clock.getBytecodeNum());
        ZoneG.initTurn();
        System.out.println("Before broadcast " + Clock.getBytecodeNum());
        broadcastLocations();
    }

    private static void readMessages(){
        try {
            int channel = Communication.NEEDTROOPCHANNEL;
            int lastMessage = rc.readBroadcast(channel + Communication.CYCLIC_CHANNEL_LENGTH);
            System.out.println("first last " + initialMessageNeedTroop + "," + lastMessage);
            for(int i = initialMessageNeedTroop; i != lastMessage && Clock.getBytecodesLeft() > Constants.BYTECODEPOSTMESSAGES;i++) {
                if (i >= Communication.CYCLIC_CHANNEL_LENGTH) i -= Communication.CYCLIC_CHANNEL_LENGTH;
                System.out.println("loop " + Clock.getBytecodeNum());
                int bitmap = rc.readBroadcast(channel + i);
                int t = workMessageTroopNeeded(bitmap);
                if(t == -1) continue;
                if(t == Communication.NEEDSOLDIERTANK) shouldBuildTroop = true;
                if(t == Communication.NEEDLUMBERJACK && !lumberjackBuilt) shouldBuildLumber = true;
                if(t == Communication.NEEDSCOUT) shouldBuildScout = true;
            }
            initialMessageNeedTroop = lastMessage;


            channel = Communication.EMERGENCYCHANNEL;
            lastMessage = rc.readBroadcast(channel + Communication.CYCLIC_CHANNEL_LENGTH);
            if (initialMessageEmergency != lastMessage) shouldBuildTroop = true;
            initialMessageEmergency = lastMessage;
        } catch (GameActionException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }


    }

    private static int workMessageTroopNeeded(int a) {
        int[] m = Communication.decode(a);
        if(m[3] == Communication.NEEDLUMBERJACK){
            MapLocation sender = new MapLocation(m[1], m[2]);
            if(m[0] == Constants.GARDENER){
                if(rc.getLocation().distanceTo(sender) > 1.0f) return -1;
            }
            if(rc.getLocation().distanceTo(sender) > 10.0f) return -1;

            //Els 200 primers torns no fa lumbs excepte per pagesos o archons
            if (rc.getRoundNum() < 200 && m[0] != Constants.GARDENER  && m[0] != 5) return -1;
        }
        return m[3];
    }

    static void broadcastLocations() {
        for (RobotInfo ri : ZoneG.enemies) {
            MapLocation enemyPos = ri.getLocation();
            int x = Math.round(enemyPos.x);
            int y = Math.round(enemyPos.y);
            int a = Constants.getIndex(ri.type);
            if (a == 0) Communication.sendMessage(Communication.ENEMYGARDENERCHANNEL, x, y, 0);
            else if (a == 5) Communication.sendMessage(Communication.ENEMYGARDENERCHANNEL, x, y, 5);
        }
        TreeInfo[] Ti = rc.senseNearbyTrees(-1, rc.getTeam().opponent());
        if (Ti.length > 0) {
            TreeInfo ti = Ti[0];
            MapLocation treePos = ti.getLocation();
            int x = Math.round(treePos.x);
            int y = Math.round(treePos.y);
            Communication.sendMessage(Communication.ENEMYTREECHANNEL, x, y, 0);
        }
        Ti = rc.senseNearbyTrees(-1, Team.NEUTRAL);
        for (TreeInfo ti : Ti) {
            if (Clock.getBytecodesLeft() < 500) return;
            MapLocation treePos = ti.getLocation();
            int x = Math.round(treePos.x);
            int y = Math.round(treePos.y);
            RobotType r = ti.getContainedRobot();
            if (r != null) {
                int a = r.bulletCost;
                if (r == RobotType.ARCHON) a = 1000;
                Communication.sendMessage(Communication.TREEWITHGOODIES, x, y, a);
            }
        }

        float score = 0;
        for (RobotInfo enemy: ZoneG.enemies){
            if (enemy.getType() == RobotType.SCOUT) score += 0.2;
            if (enemy.getType() == RobotType.LUMBERJACK) score += 0.5;
            if (enemy.getType() == RobotType.SOLDIER) score += 1;
            if (enemy.getType() == RobotType.TANK) score += 2;
        }
        for (RobotInfo ally: ZoneG.allies){
            if (ally.getType() == RobotType.SCOUT) score -= 0.2;
            if (ally.getType() == RobotType.LUMBERJACK) score -= 0.5;
            if (ally.getType() == RobotType.SOLDIER) score -= 1;
            if (ally.getType() == RobotType.TANK) score -= 2;
        }
        if (score > 0)
            Communication.sendMessage(Communication.EMERGENCYCHANNEL,Math.round(rc.getLocation().x),Math.round(rc.getLocation().y),0);

    }

    //retorna una zona del voltant que estigui buida
    private static int[] searchZone() {
        //System.out.println("Entra a la funcio buscar zona: " + Clock.getBytecodeNum());
        if (ZoneG.hasValue(zoneIWant)) return zoneIWant;
        int[] closest_empty_zone = ZoneG.nullZone();
        float minDist = Constants.INF;
        int[] myZone = ZoneG.getZoneFromPos(rc.getLocation());
        System.out.println("Soc a la zona " + myZone[0] + "," + myZone[1]);
        //System.out.println("Troba la zona de la meva pos: " + Clock.getBytecodeNum());
        for (int i = 0; i < xHex.length; i++){
            //System.out.println("bytecode inici for: " + Clock.getBytecodeNum());
            if (i > 25 && ZoneG.hasValue(closest_empty_zone)){
                //nomes busquem zones abandonades fins a 25 pel bytecode
                System.out.println("Retorna closest empty zone = " + closest_empty_zone[0] + "," + closest_empty_zone[1]);
                System.out.println("bytecode: " + Clock.getBytecodeNum());
                return closest_empty_zone;
            }
            int[] newZone = ZoneG.newZone(myZone[0] + xHex[i], myZone[1] + yHex[i]);
            //System.out.println("bytecode buscant la zona: " + Clock.getBytecodeNum());
            //di
            System.out.println("ID de la zona: " + newZone[0] + " " + newZone[1]);
            MapLocation aa= ZoneG.center(newZone);
            System.out.println("centre de la zona: " + aa.x + " " + aa.y);
            if(aa != null) rc.setIndicatorLine(rc.getLocation(), aa, 200,0,0);
            //fi di
            if (!ZoneG.insideLimits(newZone))
            {
                System.out.println("considera que esta outside Limits ");
                continue;
            }
            System.out.println("bytecode de mirar insideLimits : " + Clock.getBytecodeNum());
            //System.out.println("Prova la zona " + newZone[0] + "," + newZone[1] + " a " + rc.getLocation().distanceTo(getCenterPosFromZone(newZone)));
            int[] zoneInfo = ZoneG.readInfoBroadcast(newZone);
            System.out.println("bytecode de llegir broadcast : " + Clock.getBytecodeNum());
            if (zoneInfo == null) continue;
            int zoneType = zoneInfo[0];
            int lastTurn = zoneInfo[1];
            int thisTurn = rc.getRoundNum();
            if (zoneType != Constants.outOfMapZone) {
                if (!ZoneG.insideLimits(newZone)){
                    ZoneG.broadcastInfo(newZone, Constants.outOfMapZone);
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
            if (Map.distToEdge(newCenter) < 1f) continue;
            float distToZone = rc.getLocation().distanceTo(newCenter);
            //if (Constants.DEBUG == 1) rc.setIndicatorDot(newCenter,(int)Math.min(255,distToZone*15),0,0);
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
            System.out.println("- La zona que volia esta fora del mapa");
            zoneIWant = ZoneG.nullZone();
            return;
        }
        if (rc.canSenseLocation(centerIWant)){ //avisa als lumbers que tallin els arbres d'on vull anar
            TreeInfo[] treesNearCenter = rc.senseNearbyTrees(centerIWant,-1,Team.NEUTRAL);
            ZoneG.messageNeutralTreesInCircle(centerIWant,treesNearCenter);
        }
        if (!rc.canSenseAllOfCircle(centerIWant,rc.getType().bodyRadius)) {
            System.out.println("- Estic massa lluny del centre de la zona");
            return;
        }
        TreeInfo[] tree = rc.senseNearbyTrees(centerIWant,rc.getType().bodyRadius,null);
        if (tree.length != 0)
            Communication.sendMessage(Communication.NEEDTROOPCHANNEL, Math.round(rc.getLocation().x), Math.round(rc.getLocation().y), Communication.NEEDLUMBERJACK);

        int zoneType = ZoneG.readTypeBroadcast(zoneIWant);
        try{
            if (zoneType == Constants.busyZone) {
                zoneIWant = ZoneG.nullZone(); //si la zona esta ocupada, resetejo
                System.out.println("- La zona ja esta ocupada");
                return;
            }
            if (!rc.onTheMap(centerIWant,rc.getType().bodyRadius)){
                ZoneG.broadcastInfo(zoneIWant, Constants.outOfMapZone);
                zoneIWant = ZoneG.nullZone(); //si esta fora del mapa, resetejo
                System.out.println("- La zona que volia esta fora del mapa 2");
                return;
            }/*
            if (Map.distToEdge(centerIWant) < 5){
                ZoneG.broadcastInfo(zoneIWant,Constants.outOfMapZone);
                zoneIWant = ZoneG.nullZone(); //aixo es un parche que he ficat, nose si esta be fer-ho
                return;
            }*/
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
        ZoneG.messageNeutralTreesInCircle(ZoneG.center(), ZoneG.neutralTrees);
        // trees = arbres del inner circle
        if (!lumberjackBuilt && ZoneG.shouldRequestLumberjack())
            Communication.sendMessage(Communication.NEEDTROOPCHANNEL, Math.round(rc.getLocation().x), Math.round(rc.getLocation().y), Communication.NEEDLUMBERJACK);
    }

    //si hi ha enemics, pondera les distancies i fuig cap a la direccio oposada
    private static MapLocation checkNearbyEnemies(){
        //return null;
        MapLocation myPos = rc.getLocation();
        MapLocation escapePos = rc.getLocation();
        //System.out.println("Numero enemics: " + enemies.length);
        for (RobotInfo enemy: ZoneG.enemies){
            if (enemy.getType() == RobotType.ARCHON || enemy.getType() == RobotType.GARDENER) continue;
            if (enemy.getType() == RobotType.SCOUT) continue;
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

    private static void tryConstruct(){
        System.out.println("Entra construct");
        if (myFirstTurn){
            System.out.println("- No construeixo el meu primer torn");
            return;
        }
        if (rc.getRobotCount() > Constants.MAX_ROBOTS) {
            System.out.println("- Ja tinc massa robots");
            return;
        }
        if (rc.getRoundNum() > Constants.LAST_ROUND_BUILD) {
            System.out.println("- Ja es massa tard");
            return;
        }
        if (rc.getTeamVictoryPoints() > 500 && rc.getOpponentVictoryPoints() > 500){
            System.out.println("- No construeix, cursa de victory points!");
            return;
        }
        if (!rc.isBuildReady()) {
            System.out.println("- Tinc cooldown");
            return;
        }
        try {
            if (shouldBuildTroop) {
                System.out.println("- He rebut request de soldat/tank");
                //tria el que hi ha abans entre tank i soldat
                int tankIndex = rc.readBroadcast(Communication.unitChannels[Constants.TANK]);
                int soldierIndex = rc.readBroadcast(Communication.unitChannels[Constants.SOLDIER]);
                if (tankIndex < soldierIndex){
                    tryConstructUnit(Constants.TANK);
                    tryConstructUnit(Constants.SOLDIER);
                }else{
                    tryConstructUnit(Constants.SOLDIER);
                    tryConstructUnit(Constants.TANK);
                }
            }
        } catch (GameActionException e) {
            e.printStackTrace();
        }
        if (shouldBuildLumber) {
            System.out.println("- He rebut request de lumberjack");
            tryConstructUnit(Constants.LUMBERJACK);
        }
        if (shouldBuildScout){
            System.out.println("- He rebut request de scout");
            tryConstructUnit(Constants.SCOUT);
        }
        int early_game_length = 500;
        if (rc.getRoundNum() > early_game_length) {
            System.out.println("- Decideixo fer arbre");
            tryPlanting();
        }else {
            boolean soldierInSight = false;
            for (RobotInfo ally: ZoneG.allies){
                if (ally.getType() == RobotType.SOLDIER) soldierInSight = true;
            }

            if (soldiersSkipped > 0){
                //si s'ha saltat algun soldat de la cua i no en veu cap, intenta fer-ne un
                if (!soldierInSight) {
                    System.out.println("- Intento fer un soldat que m'he saltat");
                    boolean built = tryConstructUnit(Constants.SOLDIER);
                    if (built) soldiersSkipped--;
                }
                if (firstGardener) return;
            }

            if (queueIndex < myQueue.length) {
                int unit = myQueue[queueIndex];
                System.out.println("- Construeixo de la cua, index " + queueIndex + " = " + unit);
                if (!firstGardener && soldierInSight && unit == Constants.SOLDIER){
                    //Si veig un soldat, no el construeixo
                    queueIndex++;
                    soldiersSkipped++;
                    System.out.println("- Em toca soldat pero ja en veig un, provo arbre");
                    tryPlanting();
                    return;
                }
                boolean built = tryConstructUnit(myQueue[queueIndex]);
                if (built) queueIndex++;
                else if (myQueue[queueIndex] == Constants.SOLDIER){
                    queueIndex++;
                    soldiersSkipped++;
                    System.out.println("- No puc fer soldat, intento arbre");
                    if (!firstGardener) tryPlanting();
                }
            } else {
                System.out.println("- No em toca construir res");
            }
        }
    }

    private static boolean tryConstructUnit(int unit){
        if (unit == -1) {
            System.out.println("- unit = -1");
            return false;
        }
        if (unit == Constants.LUMBERJACK && lumberjackBuilt && rc.getTeamBullets() < 500) {
            System.out.println("- Ja he fet un lumberjack");
            return false;
        }
        if (unit == Constants.SCOUT){
            try {
                int last_round_scout_alive = rc.readBroadcast(Communication.SCOUT_LAST_TURN_ALIVE);
                int last_round_scout_built = rc.readBroadcast(Communication.SCOUT_LAST_TURN_BUILT);
                if (rc.getRoundNum() - last_round_scout_alive < 3 ||
                        rc.getRoundNum() - last_round_scout_built < 23){
                    System.out.println("- Ja hi ha un scout");
                    return false;
                }
            } catch (GameActionException e) {
                e.printStackTrace();
            }
        }
        if (unit == Constants.TREE) return tryPlanting();
        System.out.println("- Intenta construir " + Constants.getRobotTypeFromIndex(unit));
        if (!rc.hasRobotBuildRequirements(Constants.getRobotTypeFromIndex(unit))) {
            System.out.println("- No te els requisits per construir");
            return false;
        }
        if (!shouldBuildUnit()){
            System.out.println("- No fa units perque te veins");
            return false;
        }
        //if (!Build.allowedToConstruct(unit)) {
        //System.out.println("No tinc prou bales per construir " + unit);
        //System.out.println("Tinc " + rc.getTeamBullets() + " i calen " + totalBulletCost(unit));
        //return;
        //}
        RobotType newRobotType = Constants.getRobotTypeFromIndex(unit);
        Direction dirToBuild = rc.getLocation().directionTo(rc.getInitialArchonLocations(rc.getTeam().opponent())[0]);
        if (unit == Constants.LUMBERJACK){
            Direction bestDir = null;
            float minDist = 9999;
            for (TreeInfo tree: ZoneG.neutralTrees){
                if (rc.getLocation().distanceTo(tree.getLocation()) - tree.getRadius() < minDist){
                    minDist = rc.getLocation().distanceTo(tree.getLocation()) - tree.getRadius();
                    bestDir = rc.getLocation().directionTo(tree.getLocation());
                }
            }
            if (bestDir != null) dirToBuild = bestDir;
        }

        float r;
        if (unit == Constants.TANK) r = 2;
        else r = 1;
        dirToBuild = Build.findDirectionToBuild(dirToBuild,r);
        if (dirToBuild == null) return false;
        if (rc.canBuildRobot(newRobotType,dirToBuild)){
            try {
                System.out.println("- Construeix " + Constants.getRobotTypeFromIndex(unit));
                rc.buildRobot(Constants.getRobotTypeFromIndex(unit),dirToBuild);
                Build.incrementRobotsBuilt();
                if (unit == Constants.LUMBERJACK) lumberjackBuilt = true;
                if (unit == Constants.SCOUT) rc.broadcast(Communication.SCOUT_LAST_TURN_BUILT,rc.getRoundNum());
                return true;
            } catch (GameActionException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private static boolean tryPlanting(){
        System.out.println("Entra tryplant");
        if (!ZoneG.hasValue(zone)){
            System.out.println("- No te zona");
            return false;
        }
        if (rc.getRoundNum() > Constants.LAST_ROUND_BUILD) {
            System.out.println("- Massa tard per construir");
            return false;
        }
        /*if (ZoneG.freeSpots < 2/* && !shouldBuildSixTrees()) {
            System.out.println("- Nomes tinc una posicio oberta");
            return false; //Si nomes hi ha una posicio, la reservem per robots
        }*/
        if (shouldBuildLumber || shouldBuildTroop) {
            System.out.println("- He rebut ordres de construir tropa");
            return false; //no planta si te alguna cosa mes prioritaria
        }
        if (rc.getLocation().distanceTo(ZoneG.center) > Constants.eps){
            System.out.println("- No planto perque no soc al centre");
            return false;
        }
        int index = ZoneG.indexToPlant(); //si hi ha algun arbre no ocupat
        System.out.println("- Planta l'arbre " + index);
        if (index == -1) {
            System.out.println("- Index = -1");
            return false;
        }
        MapLocation myPos = rc.getLocation();
        Direction plantingDirection = myPos.directionTo(ZoneG.hexPos[index]);
        if (rc.getLocation().distanceTo(myPos) < Constants.eps && rc.canPlantTree(plantingDirection)){
            try {
                //Planta l'arbre
                rc.plantTree(plantingDirection);
                return true;
            } catch (GameActionException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private static boolean shouldBuildUnit(){
        if (!ZoneG.hasValue(zone)) return true;
        for (int i = 0; i < 6; i++){
            MapLocation tree = ZoneG.hexPos[i];
            try {
                if(rc.senseTreeAtLocation(tree) != null) continue;
                int j = (i+1)%6;
                RobotInfo r1 = rc.senseRobotAtLocation(ZoneG.neighbors[i]);
                RobotInfo r2 = rc.senseRobotAtLocation(ZoneG.neighbors[j]);
                if (r1 != null && r1.getTeam() == rc.getTeam() && r1.getType() == RobotType.GARDENER) continue;
                if (r2 != null && r2.getTeam() == rc.getTeam() && r2.getType() == RobotType.GARDENER) continue;
                return true;
            } catch (GameActionException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private static void updateTarget(MapLocation newTarget){
        if (realTarget != null && newTarget != null && newTarget.distanceTo(realTarget) < Constants.eps) return;
        realTarget = newTarget;
        //Greedy.resetObstacle(rc);
    }










































}
