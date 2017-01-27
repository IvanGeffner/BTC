package MergedplayerProvesDiana;

import battlecode.common.*;


public class Gardener {

    private static RobotController rc;

    private static MapLocation realTarget;


    private static int[] zone = ZoneG.nullZone();
    private static int[] zoneIWant = ZoneG.nullZone();


    private static float maxDistToCenter = 3f;

    static int initialMessageNeedTroop = 0;


    private static int[] Xsorted = {0, -1, 1, 0, 0, 1, -1, -1, 1, -2, 2, 0, 0, 1, -1, -1, -2, -2, 1, 2, 2, -2, 2, 2, -2, 0, 3, 0, -3, -3, -1, 3, -1, 3, -3, 1, 1, -3, 2, -2, -3, 3, 3, 2, -2, -4, 0, 4, 0, -4, -4, 4, -1, -1, 1, 1, 4, -3, 3, 3, -3, -4, -2, 4, 4, -4, 2, 2, -2, 3, 4, 0, 3, -3, -5, 0, -3, -4, 4, -4, 5, 5, 1, -1, -1, -5, -5, 5, 1, 2, 5, 2, -2, -2, -5, 5, -5, 4, -4, -4, 4, -3, -3, 5, 3, 3, -5, -5, 5, 6, -6, 0, 0, 1, -1, 6, -6, -6, 6, 1, -1, -6, -2, -6, 2, 6, 6, 2, -2, -4, -4, -5, 5, 5, 4, -5, 4, -6, 6, -3, -3, 6, 3, 3, -6, -7, 0, 0, 7, -1, -7, -7, -5, 7, 1, 7, 1, 5, -1, 5, -5, 6, 4, -4, -4, 6, 4, -6, -6, -2, 2, 7, -2, 2, 7, -7, -7, 3, -7, 7, 3, -3, 7, -7, -3, 6, 5, -6, -6, 5, -5, -5, 6, -8, 0, 0, 8, -4, 7, 8, 4, 8, -1, -1, -4, -8, -8, 1, -7, -7, 1, 4, 7, 8, 8, 2, 2, -2, -2, -8, -8, 6, -6, -6, 6, -3, 8, -8, -3, -8, 8, 3, 3, -7, 7, -7, 7, -5, 5, 5, -5, -8, -4, 4, 8, -4, 4, -8, 8, -7, 7, 6, -7, 7, -6, -6, 6, 8, 5, -5, -8, -8, 8, -5, 5, 7, -7, -7, 7, -8, 8, 6, -8, 8, 6, -6, -6, 8, -8, 8, 7, 7, -7, -7, -8, 8, -8, -8, 8};
    private static int[] Ysorted = {0, 0, 0, -1, 1, 1, -1, 1, -1, 0, 0, 2, -2, -2, 2, -2, -1, 1, 2, -1, 1, 2, -2, 2, -2, -3, 0, 3, 0, 1, -3, 1, 3, -1, -1, -3, 3, -2, -3, 3, 2, -2, 2, 3, -3, 0, -4, 0, 4, 1, -1, 1, -4, 4, -4, 4, -1, 3, -3, 3, -3, -2, 4, 2, -2, 2, 4, -4, -4, -4, 3, -5, 4, 4, 0, 5, -4, 3, -3, -3, 0, -1, 5, 5, -5, -1, 1, 1, -5, 5, 2, -5, -5, 5, -2, -2, 2, -4, -4, 4, 4, 5, -5, -3, 5, -5, 3, -3, 3, 0, 0, -6, 6, 6, 6, -1, 1, -1, 1, -6, -6, 2, 6, -2, -6, -2, 2, 6, -6, -5, 5, 4, 4, -4, -5, -4, 5, 3, 3, -6, 6, -3, -6, 6, -3, 0, -7, 7, 0, -7, -1, 1, 5, -1, -7, 1, 7, 5, 7, -5, -5, 4, 6, -6, 6, -4, -6, 4, -4, 7, 7, -2, -7, -7, 2, 2, -2, 7, -3, 3, -7, 7, -3, 3, -7, -5, 6, 5, -5, -6, -6, 6, 5, 0, 8, -8, 0, 7, 4, -1, -7, 1, -8, 8, -7, -1, 1, -8, 4, -4, 8, 7, -4, -2, 2, 8, -8, 8, -8, 2, -2, -6, 6, -6, 6, -8, 3, 3, 8, -3, -3, -8, 8, 5, -5, -5, 5, 7, 7, -7, -7, 4, -8, 8, -4, 8, -8, -4, 4, -6, -6, 7, 6, 6, -7, 7, -7, -5, -8, 8, -5, 5, 5, -8, 8, -7, -7, 7, 7, -6, -6, -8, 6, 6, 8, 8, -8, 7, -7, -7, 8, -8, 8, -8, 7, -8, 8, -8, 8};


    public static boolean lumberjackBuilt; 
    public static int myRound; 
    
    public static void run(RobotController rcc) {
        rc = rcc;
        Initialize();
        while (true) {
            Shake.shake(rc);
            Communication.askForUnits();
            readMessages();
            Communication.sendReport(Communication.GARDENER_REPORT);
            if (ZoneG.hasValue(zone)) ZoneG.broadcastMyZone();
            MapLocation newTarget;
            newTarget = checkNearbyEnemies();
            if (newTarget != null){
                System.out.println("Fuig de " + rc.getLocation() + " a " + newTarget);
                if (Constants.DEBUG == 1) rc.setIndicatorLine(rc.getLocation(),newTarget, 0, 255, 255);
            }else if (!ZoneG.hasValue(zone)) {
                if (rc.getRoundNum() % ZoneG.turnsResetZone == 0) zoneIWant = ZoneG.nullZone();
                zoneIWant = searchZone();
                if (ZoneG.hasValue(zoneIWant)) {
                    newTarget = ZoneG.center(zoneIWant);
                    System.out.println("Va a zona " + zoneIWant[0] + "," + zoneIWant[1] + "  " + rc.getLocation() + " a " + newTarget + ", " + ZoneG.inMap(zoneIWant));
                    if (Constants.DEBUG == 1) rc.setIndicatorLine(rc.getLocation(), newTarget, 255, 255, 255);
                }
                //System.out.println("Soc a la zona "+ getZoneFromPos(rc.getLocation())[0] + "," + getZoneFromPos(rc.getLocation())[1] + " i vull anar a "+zoneIWant[0] + "," + zoneIWant[1]);
                checkIfArrivedToZone();
                tryConstruct();
            } else {
                ZoneG.updateTreeHP();
                checkNeutralTreesInZone();
                newTarget = returnToZone();
                if (newTarget != null) {
                    ZoneG.broadcastInfo(zone, Constants.abandonedZone);
                    zone = ZoneG.nullZone();
                    ZoneG.resetMyZone();
                    System.out.println("Retorna: de " + rc.getLocation() + " a " + newTarget);
                    if (Constants.DEBUG == 1) rc.setIndicatorLine(rc.getLocation(), newTarget, 255, 220, 28);
                } else {
                    newTarget = ZoneG.findLowHPTree();
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
                                newTarget = ZoneG.center(zone);
                                //Greedy.resetObstacle(rc);
                                if (Constants.DEBUG == 1) rc.setIndicatorLine(rc.getLocation(), newTarget, 0, 0, 0);
                            }
                        }
                    }
                }
            }
            //System.out.println("despres de decidir tot " + Clock.getBytecodeNum());

            try {
                if (rc.getTeamBullets() > Constants.BULLET_LIMIT) rc.donate(rc.getTeamBullets() - Constants.BULLET_LIMIT);
                if (rc.getRoundNum() > Constants.LAST_ROUND_BUILD) {
                    float donation = Math.max(0, rc.getTeamBullets() - 20);
                    if (donation > 20)
                        rc.donate(donation);
                }
            } catch (GameActionException e) {
                e.printStackTrace();
            }
            Map.checkMapBounds();
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
    	initialMessageNeedTroop = 0; 
    	lumberjackBuilt = false; 
    	myRound = rc.getRoundNum(); 
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
            
            initialMessageNeedTroop = rc.readBroadcast(Communication.TREEWITHGOODIES + Communication.CYCLIC_CHANNEL_LENGTH);
        } catch (GameActionException e) {
            e.printStackTrace();
        }
    }

    private static int[] searchZone() {
        if (ZoneG.hasValue(zoneIWant)) return zoneIWant;
        int[] closest_empty_zone = ZoneG.nullZone();
        float minDist = Constants.INF;
        int[] myZone = ZoneG.getZoneFromPos(rc.getLocation());
        for (int i = 0; i < Xsorted.length; i++){
            if (i > 10 && ZoneG.hasValue(closest_empty_zone)){
                //nomes busquem zones abandonades fins a 10 pel bytecode
                System.out.println("Retorna closest empty zone = " + closest_empty_zone[0] + "," + closest_empty_zone[1]);
                return closest_empty_zone;
            }
            int[] newZone = ZoneG.newZone(myZone[0] + Xsorted[i], myZone[1] + Ysorted[i]);
            if (!ZoneG.insideLimits(newZone)) continue;
            //System.out.println("Prova la zona " + newZone[0] + "," + newZone[1] + " a " + rc.getLocation().distanceTo(getCenterPosFromZone(newZone)));
            int[] zoneInfo = ZoneG.readInfoBroadcast(newZone);
            if (zoneInfo == null) continue;
            int zoneType = zoneInfo[0];
            int lastTurn = zoneInfo[1];
            int thisTurn = rc.getRoundNum();
            if (zoneType != Constants.outOfMapZone) {
                if (!ZoneG.updateInMap(newZone)){
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
            float distToZone = rc.getLocation().distanceTo(ZoneG.center(newZone));
            if (zoneType == Constants.emptyZone && distToZone < minDist){
                closest_empty_zone = newZone;
                minDist = distToZone;
            }
        }
        return ZoneG.nullZone();
    }

    private static void checkIfArrivedToZone(){
        MapLocation centerIWant = ZoneG.center(zoneIWant);
        //System.out.println("El centre esta dintre? " + onCurrentMap(centerIWant));
        if (!Map.onCurrentMap(centerIWant)){
            zoneIWant = ZoneG.nullZone();
            return;
        }
        if (rc.canSenseLocation(centerIWant)){
            TreeInfo[] treesNearCenter = rc.senseNearbyTrees(centerIWant,-1,Team.NEUTRAL);
            ZoneG.messageNeutralTreesInBox(centerIWant,treesNearCenter);
        }
        if (!rc.canSenseAllOfCircle(centerIWant,rc.getType().bodyRadius)) return;
        int zoneType = ZoneG.readTypeBroadcast(zoneIWant);
        try{
            if (zoneType == Constants.busyZone) {
                zoneIWant = ZoneG.nullZone();
                return;
            }
            if (!rc.onTheMap(centerIWant,rc.getType().bodyRadius)){
                ZoneG.updateInMap(zoneIWant);
                zoneIWant = ZoneG.nullZone();
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
            if (Constants.DEBUG == 1) rc.setIndicatorDot(rc.senseTree(minID).getLocation(),0, 255, 0);
            rc.water(minID);
        } catch (GameActionException e) {
            e.printStackTrace();
        }
    }

    private static void checkNeutralTreesInZone(){
        TreeInfo[] neutralTrees = rc.senseNearbyTrees(-1,Team.NEUTRAL);
        ZoneG.messageNeutralTreesInBox(ZoneG.center(),neutralTrees);
    }

    private static MapLocation checkNearbyEnemies(){
        //return null;
        RobotInfo[] enemies = rc.senseNearbyRobots(4, rc.getTeam().opponent());
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
        return null;
        //return escapePos;
    }

    private static MapLocation returnToZone(){
        if (Math.abs(rc.getLocation().x - ZoneG.center().x) < maxDistToCenter &&
                Math.abs(rc.getLocation().y - ZoneG.center().y) < maxDistToCenter) return null;
        return ZoneG.center();
    }


    private static MapLocation tryPlanting(){
        //System.out.println("Entra plantar");
        if (rc.getRoundNum() > Constants.LAST_ROUND_BUILD) return null;
        if (!Build.allowedToConstruct(Constants.TREE)) {
            //System.out.println("No tinc prou bullets per plantar");
            return null; //comprova bullets
        }
        int index = ZoneG.indexToPlant(); //si hi ha algun arbre no ocupat
        System.out.println("Planta l'arbre " + index);
        if (index == -1) return null;
        MapLocation plantingPosition = ZoneG.plantingPos[index];
        MapLocation newTreePosition = ZoneG.treePos[index];
        Direction plantingDirection = plantingPosition.directionTo(newTreePosition);
        if (rc.getLocation().distanceTo(plantingPosition) < Constants.eps && rc.canPlantTree(plantingDirection)){
            try {
                //Si pot plantar l'arbre, el planta i no cal que retorni cap direccio
                rc.plantTree(plantingDirection);
                Build.incrementTreesBuilt();
                Build.updateAfterConstruct(Constants.TREE);
                ZoneG.treeHP[index] = GameConstants.BULLET_TREE_MAX_HEALTH;
            } catch (GameActionException e) {
                e.printStackTrace();
            }
            return null;
        }
        return plantingPosition;
    }





    private static MapLocation tryConstruct(){
        //System.out.println("Entra construct");
        if (rc.getRobotCount() > Constants.MAX_ROBOTS) return null;
        if (rc.getRoundNum() > Constants.LAST_ROUND_BUILD) return null;
        int smallUnit = Build.bestSmallUnitToBuild();
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
        //System.out.println("Tria les units " + firstUnit + ", " + secondUnit);
        MapLocation firstTry = tryConstructUnit(firstUnit);
        System.out.println("First try es " + firstUnit + " loc = " + firstTry );
        if (firstTry != null) return firstTry;
        return tryConstructUnit(secondUnit);
    }

    private static MapLocation tryConstructUnit(int unit){
        if (unit == -1) return null;
        if (!Build.allowedToConstruct(unit)) {
            //System.out.println("No tinc prou bales per construir " + unit);
            //System.out.println("Tinc " + rc.getTeamBullets() + " i calen " + totalBulletCost(unit));
            return null;
        }
        MapLocation buildingPosition;
        MapLocation newRobotPosition;
        if (!ZoneG.hasValue(zone)){
            buildingPosition = rc.getLocation();
            newRobotPosition = getBuildPositionWithoutZone(unit);
            if (newRobotPosition == null) return null;
        }else{
            int index = ZoneG.indexToBuild(unit);
            if (index == -1) return null;
            if (unit == Constants.TANK){
                buildingPosition = ZoneG.buildTankPos[index];
                newRobotPosition = ZoneG.newTankPos[index];
            }else {
                buildingPosition = ZoneG.buildPos[index];
                newRobotPosition = ZoneG.newRobotPos[index];
            }
        }
        Direction buildDirection = buildingPosition.directionTo(newRobotPosition);
        RobotType newRobotType = Constants.getRobotTypeFromIndex(unit);
        if (rc.getLocation().distanceTo(buildingPosition) < Constants.eps && rc.canBuildRobot(newRobotType,buildDirection)){
            try {
                rc.buildRobot(Constants.getRobotTypeFromIndex(unit),rc.getLocation().directionTo(newRobotPosition));
                Build.incrementRobotsBuilt();
                Build.updateAfterConstruct(unit);
                return rc.getLocation();
            } catch (GameActionException e) {
                e.printStackTrace();
            }
            return null;
        }else return buildingPosition;
    }

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

    private static void readMessages()
    {
    	boolean needTroop = false; 
    	boolean needLumberjack = false;
    	try {
    		int channel = Communication.NEEDTROOPCHANNEL;
            int lastMessage = rc.readBroadcast(channel + Communication.CYCLIC_CHANNEL_LENGTH);
            for(int i = initialMessageNeedTroop; i != lastMessage && Clock.getBytecodesLeft() > Constants.BYTECODEPOSTMESSAGES;)
            {
                int a = rc.readBroadcast(channel + i);
                int t = workMessageTroopNeeded(a);
                if(t == Communication.NEEDSOLDIERTANK) needTroop = true; 
                if(t == Communication.NEEDLUMBERJACK) needLumberjack = true; 
                ++i;
                if (i >= Communication.CYCLIC_CHANNEL_LENGTH) i -= Communication.CYCLIC_CHANNEL_LENGTH;
            }
            initialMessageNeedTroop = lastMessage;
        } catch (GameActionException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    	if(needTroop) tryTroop(); 
    	if(needLumberjack && !lumberjackBuilt) tryLumberjack();
    }


    static int workMessageTroopNeeded(int a)
    {
        int[] m = Communication.decode(a);
       //MapLocation unitTreePos = new MapLocation(m[1], m[2]);
        //System.out.println("Unit Cost in this tree: " + m[3]);
        //System.out.println("sent by: " + m[0]);
        //if(rc.canSenseLocation(unitTreePos)) return;
        return m[3];
    }

    static void tryTroop()
    {
        
    	try
		{
    		/*
    		 * TODO arreglar-ho per poder fer tanks
    		int tankIndex = rc.readBroadcast(Communication.unitChannels[Constants.TANK]);
            int smallUnitIndex = rc.readBroadcast(Communication.unitChannels[Constants.SOLDIER]);
            RobotType t; 
            int c; 
            if(tankIndex < smallUnitIndex && Build.allowedToConstruct(Constants.TANK)) 
            {
            	c = Constants.TANK;
            	t = RobotType.TANK; 
            }
            
            else 
            {
            	c = Constants.SOLDIER; 
            	t = RobotType.SOLDIER; 
            }
            */
            
            int c = Constants.SOLDIER; 
            RobotType t = RobotType.SOLDIER; 
            
            if(!Build.allowedToConstruct(Constants.SOLDIER)) return; 
    		Direction d = Direction.NORTH; 
    		if(rc.canBuildRobot(RobotType.SOLDIER, d)) 
    		{
    			rc.buildRobot(t, d);
    			Build.updateAfterConstruct(c);
    		}
    		else
    		{
	    		d = Direction.SOUTH; 
	    		if(rc.canBuildRobot(RobotType.SOLDIER, d)) 
	    		{
	    			rc.buildRobot(t, d);
	    			Build.updateAfterConstruct(c);
	    		}
	    		else
	    		{
		    		d = Direction.EAST; 
		    		if(rc.canBuildRobot(RobotType.SOLDIER, d)) 
		    		{
		    			rc.buildRobot(t, d);
		    			Build.updateAfterConstruct(c);
		    		}
		    		else
		    		{
			    		d = Direction.WEST; 
			    		if(rc.canBuildRobot(RobotType.SOLDIER, d))
			    		{
			    			rc.buildRobot(t, d);
			    			Build.updateAfterConstruct(c);
			    		}
		    		}
	    		}
    		}
		} catch (GameActionException e){
            e.printStackTrace();
		}
	}
    static void tryLumberjack()
    {
    	try
		{
    		Direction d = Direction.NORTH; 
    		if(rc.canBuildRobot(RobotType.SOLDIER, d)) 
    		{
    			lumberjackBuilt = true;
    			rc.buildRobot(RobotType.LUMBERJACK, d);
    		}
    		else
    		{
	    		d = Direction.SOUTH; 
	    		if(rc.canBuildRobot(RobotType.SOLDIER, d))
	    		{
	    			lumberjackBuilt = true;
	    			rc.buildRobot(RobotType.LUMBERJACK, d);
	    		}
	    		else
	    		{
		    		d = Direction.EAST; 
		    		if(rc.canBuildRobot(RobotType.SOLDIER, d)) 
		    		{
		    			lumberjackBuilt = true;
		    			rc.buildRobot(RobotType.LUMBERJACK, d);
		    		}
		    		else
		    		{
			    		d = Direction.WEST; 
			    		if(rc.canBuildRobot(RobotType.SOLDIER, d)) 
			    		{
			    			lumberjackBuilt = true;
			    			rc.buildRobot(RobotType.LUMBERJACK, d);
			    		}
		    		}
	    		}
    		}
		} catch (GameActionException e){
            e.printStackTrace();
		}
	}


    private static void updateTarget(MapLocation newTarget){
        if (realTarget != null && newTarget != null && newTarget.distanceTo(realTarget) < Constants.eps) return;
        realTarget = newTarget;
        //Greedy.resetObstacle(rc);
    }
}
