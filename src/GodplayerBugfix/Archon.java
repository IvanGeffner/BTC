package GodplayerBugfix;

import battlecode.common.*;


/**
 * Created by Ivan on 1/9/2017.
 */
public class Archon {

    static RobotController rc;
    private static int whoAmI = -1; //none of your business
    static int xBase, yBase;

    static boolean leader;

    static MapLocation realTarget;

    static MapLocation emergencyTarget;

    static int turnsSinceAllowed;
    static MapLocation bestZone;

    static boolean initializedZone = false;
    static MapLocation bestZ = new MapLocation(-Constants.INF, 0);
    static Direction firstVertical;
    static Direction firstHorizontal;

    //coses de buscar zona per pagesos
    private static int[] xHex = {0, 0, 1, 1, 0, -1, -1, 0, 1, 2, 2, 2, 1, 0, -1, -2, -2, -2, -1, 0, 1, 2, 3, 3, 3, 3, 2, 1, 0, -1, -2, -3, -3, -3, -3, -2, -1, 0, 1, 2, 3, 4, 4, 4, 4, 4, 3, 2, 1, 0, -1, -2, -3, -4, -4, -4, -4, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 5, 5, 5, 5, 5, 4, 3, 2, 1, 0, -1, -2, -3, -4, -5, -5, -5, -5, -5, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 6, 6, 6, 6, 6, 6, 5, 4, 3, 2, 1, 0, -1, -2, -3, -4, -5, -6, -6, -6, -6, -6, -6, -6, -5, -4, -3, -2, -1};
    private static int[] yHex = {0, -1, -1, 0, 1, 1, 0, -2, -2, -2, -1, 0, 1, 2, 2, 2, 1, 0, -1, -3, -3, -3, -3, -2, -1, 0, 1, 2, 3, 3, 3, 3, 2, 1, 0, -1, -2, -4, -4, -4, -4, -4, -3, -2, -1, 0, 1, 2, 3, 4, 4, 4, 4, 4, 3, 2, 1, 0, -1, -2, -3, -5, -5, -5, -5, -5, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 5, 5, 5, 5, 5, 4, 3, 2, 1, 0, -1, -2, -3, -4, -6, -6, -6, -6, -6, -6, -6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 6, 6, 6, 6, 6, 6, 5, 4, 3, 2, 1, 0, -1, -2, -3, -4, -5};


    static boolean firstTurn = true;
    static boolean secondTurn = true;
    static boolean firstArchon;
    static boolean shouldBuildGardener;
    static boolean danger;
    static int totalFreeSpots;
    static int aliveGardeners;

    static int initialMessageFreeSpots = 0;
    static int initialMessageGardCount = 0;
    static RobotInfo[] allies;
    static RobotInfo[] enemies;
    static TreeInfo[] neutralTrees;
    static TreeInfo[] enemyTrees;

    static int turnsSinceLastGardener = 0;

    static MapLocation[] enemyArchons;


    static float sv;

    @SuppressWarnings("unused")
    public static void run(RobotController rcc) {
        rc = rcc;
        ZoneG.init(rc);
        initializedZone = false;
        turnsSinceAllowed = 0;
        turnsSinceLastGardener = 0;
        enemyArchons = rc.getInitialArchonLocations(rc.getTeam().opponent());


        if (rc.getRoundNum() > 5) init();
        while (true) {
            initTurn();
            System.out.println("Who am i " + whoAmI);
            MapLocation newTarget = null;
            boolean danger = (emergencyTarget != null);
            System.out.println(Sight.closed);
            if (Sight.closed){
                int x = Math.round(rc.getLocation().x);
                int y = Math.round(rc.getLocation().y);
                Communication.sendMessage(Communication.NEEDTROOPCHANNEL, x, y, Communication.NEEDLUMBERJACK);
                System.out.println("asking for a lumbi ");
                rc.setIndicatorDot(rc.getLocation(), 120, 130, 120);
            }
            if (emergencyTarget != null){
                //Si esta en perill
                System.out.println("Fuig de " + rc.getLocation() + " a " + emergencyTarget);
                //if (Constants.DEBUG == 1) rc.setIndicatorLine(rc.getLocation(),newTarget, 0, 255, 255);
                newTarget = emergencyTarget;
            }else {
                //busca la millor zona per deixar un pages
                if(turnsSinceLastGardener >= 20)
                {
                    if (!initializedZone){
                        System.out.println("NO S'HA INICIALITZAT EL ZONE ORIGIN I S'HA CRIDAT FINDBESTZONE!!!!");
                    }else {
                        bestZone = findBestZone();
                        //drawZone();
                        newTarget = buildFrom();
                        rc.setIndicatorLine(rc.getLocation(), newTarget, 200, 0, 200);
                        if (shouldBuildGardener && initializedZone) {
                            System.out.println("Vaig a buscar zona");
                            ++turnsSinceAllowed;
                            System.out.println("La millor zona es " + bestZone);
                            tryConstruct();
                        }
                    }
                }
                if(newTarget == null)
                {
                    newTarget = checkShakeTrees();
                    if (newTarget != null){
                        System.out.println("Va a fer shake de " + rc.getLocation() + " a " + newTarget);
                    }else{
                        System.out.println("Va a buscar la zona mes buida");
                        if (Sight.gradientX != 0 || Sight.gradientY != 0) {
                            Direction optim = new Direction(Sight.gradientX, Sight.gradientY);
                            float dist = (float)Math.sqrt(Sight.gradientX* Sight.gradientX + Sight.gradientY* Sight.gradientY);
                            if (dist < 0.1f) newTarget = realTarget;
                            else newTarget = rc.getLocation().add(optim, dist);
                        } else newTarget = rc.getLocation();
                    }
                    if (shouldBuildGardener) tryConstruct(); //poso aixo aqui perque si no, quan no pot fer un pages al primer torn no en fa mai
                }
            }
            updateTarget(newTarget);
            if (realTarget == null) {
                //if (Constants.DEBUG == 1) rc.setIndicatorDot(rc.getLocation(), 255, 0, 0);
            }else if (realTarget.distanceTo(rc.getLocation()) < Constants.eps){
                Greedy.moveToSelf(rc,28000);
            } else Greedy.moveGreedy(rc, realTarget, 28000);
            Clock.yield();
        }
    }

    private static void init(){
        try{
            MapLocation base = rc.getInitialArchonLocations(rc.getTeam())[0];
            xBase = Math.round(base.x);
            yBase = Math.round(base.y);
            Communication.init(rc,xBase,yBase);
            Build.init(rc);
            MapLocation[] archons = rc.getInitialArchonLocations(rc.getTeam());
            Map.init(rc);
            if (whoAmI == 0) { // first to execute
                rc.broadcast(Communication.ARCHONS_LAST_TURN, archons.length);
                // inicialitzem el limits del mapa
                rc.broadcast(Communication.MAP_UPPER_BOUND, Float.floatToIntBits(Constants.INF));
                rc.broadcast(Communication.MAP_LOWER_BOUND, Float.floatToIntBits(-Constants.INF));
                rc.broadcast(Communication.MAP_LEFT_BOUND, Float.floatToIntBits(-Constants.INF));
                rc.broadcast(Communication.MAP_RIGHT_BOUND, Float.floatToIntBits(Constants.INF));
            }

            Map.checkMapBounds();
            float score = getInitialScore();
            System.out.println("my score: " + score);
            rc.broadcast(Communication.ARCHON_INIT_SCORE[whoAmI],Float.floatToIntBits(score));

        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private static void init2(){
        float bestScore = -1;
        int bestArchon = -1;
        for (int i = 0; i < 3; i++){
            try {
                float score = Float.intBitsToFloat(rc.readBroadcast(Communication.ARCHON_INIT_SCORE[i]));
                if (score > bestScore){
                    bestScore = score;
                    bestArchon = i;
                }
            } catch (GameActionException e) {
                e.printStackTrace();
            }
        }
        if (bestArchon == whoAmI){
            leader = true;
            System.out.println("I'm the leader ");
            //bestZone = firstGardener();
            tryConstruct();
        }
    }

    private static void initTurn(){
        firstArchon = false;
        shouldBuildGardener = false;
        if(turnsSinceLastGardener > 40 && rc.getRoundNum() < 25000 && rc.getTeamBullets() > 150) shouldBuildGardener = true;
        totalFreeSpots = 0;
        ++turnsSinceLastGardener;
        allies = rc.senseNearbyRobots(-1, rc.getTeam());
        enemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        neutralTrees = rc.senseNearbyTrees(-1, Team.NEUTRAL);
        enemyTrees = rc.senseNearbyTrees(-1, rc.getTeam().opponent());
        Bot.shake(rc);
        Bot.donate(rc);
        sv = Sight.computeSightRange(rc);;
        readMessages(); //aqui sap si cal que construeixi un pages
        if (secondTurn && !firstTurn) {
            secondTurn = false;
            init2();
        }
        if(!initializedZone){
            //intenta inicialitzar la zona (aixo nomes es fa quan un pages ja ha fixat l'origen de coordenades)
            // si encara no esta fixat l'origen doncs no fa res i s'espera que el pages ho faci
            initializedZone = initializeZone();
        }
        updateArchonCount();
        if (firstTurn) {
            firstTurn = false;
            init();
        }
        broadcastLocations();
        if(!firstTurn) Map.checkMapBounds(); //aixo ha d'anar al final del initturn, sino dona excepcio pq s'ha d'inicialitzar
        checkSuicide();
        if (whoAmI < 5){
            MapLocation myPos = rc.getLocation();
            Communication.encodeFinding(0,Math.round(myPos.x), Math.round(myPos.y),rc.getID());
        }
    }



    private static float getInitialScore(){
        float sightScore = sv/10000.0f;
        Direction canBuild = Build.findDirectionToBuild(Direction.NORTH, 3.0f);
        if (canBuild != null) return sightScore + 3.0f;
        canBuild = Build.findDirectionToBuild(Direction.NORTH, 2.0f);
        if (canBuild != null) return sightScore + 2.0f;
        canBuild = Build.findDirectionToBuild(Direction.NORTH, 1.0f);
        if (canBuild != null) return sightScore + 1.0f;
        return 0.0f;
    }

    private static MapLocation checkShakeTrees(){
        TreeInfo[] neutralTrees = rc.senseNearbyTrees(-1,Team.NEUTRAL);
        float bestScore = 0;
        MapLocation bestPos = null;
        for (TreeInfo tree: neutralTrees){
            if (tree.getContainedBullets() == 0) continue;
            float dist = rc.getLocation().distanceTo(tree.getLocation()) - tree.getRadius();
            float score = tree.getContainedBullets() / (dist + 1);
            if (score > bestScore){
                bestScore = score;
                bestPos = tree.getLocation();
            }
        }
        return bestPos;
    }

    private static void updateTarget(MapLocation newTarget){
        if (realTarget != null && newTarget != null && newTarget.distanceTo(realTarget) < Constants.eps) return;
        realTarget = newTarget;
        //Greedy.resetObstacle(rc);
    }

    private static void tryConstruct(){
        System.out.println("Entra try construct");
        if (!rc.hasRobotBuildRequirements(RobotType.GARDENER)){
            System.out.println("- Tinc cooldown");
            return;
        }
        if (!shouldBuildGardener) {
            System.out.println("- should build gardener false");
            return;
        }
        if (Communication.countArchons() > 1 && danger) {
            System.out.println("- no construeixo pq estic en perill");
            return; //no fa gardener si esta en perill i hi ha mes archons
        }
        if (!myTurn()) {
            System.out.println("- no es el meu torn");
            return; //per repartir-se els pagesos entre els archons
        }

        try{
            if (rc.readBroadcast(Communication.HAS_BUILT_GARDENER) == 1 && rc.getRoundNum() < 25){
                System.out.println("- Nomes podem fer 1 pages els primers 25 torns");
                return;
            }
            if(!initializedZone) firstGardener(); //calcula on anira el primer gardener
            Direction d = Direction.EAST;
            if (bestZone != null)
                d = rc.getLocation().directionTo(bestZone);

            if(initializedZone || bestZone != null)
            {
	            Direction dirBuild = Build.findDirectionToBuild(d,RobotType.GARDENER.bodyRadius);
	            if (dirBuild == null){
	                System.out.println("- No pot construir en cap direccio");
	                return;
	            }
	            buildGardener(dirBuild);
            }else {
            	rc.setIndicatorDot(rc.getLocation(), 0, 200, 0);
            	if(firstHorizontal != null && rc.canBuildRobot(RobotType.GARDENER, firstHorizontal))
            	    buildGardener(firstHorizontal);
        		else
        		{
        			if(firstVertical != null && rc.canBuildRobot(RobotType.GARDENER, firstVertical))
        			    buildGardener(firstVertical);
        			else {
        				if(firstHorizontal != null && rc.canBuildRobot(RobotType.GARDENER, firstHorizontal.opposite()))
        				    buildGardener(firstHorizontal.opposite());
        	    		else {
        	    		    if(firstVertical != null && rc.canBuildRobot(RobotType.GARDENER, firstVertical.opposite()))
        	    		        buildGardener(firstVertical.opposite());
        	    			else {
        	    				d = Build.findDirectionToBuild(firstHorizontal, RobotType.GARDENER.bodyRadius);
        	    				buildGardener(d);
        	    			}
        	    		}
        			}
        		}
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private static void buildGardener(Direction d){
        if (d == null) return;
        if (!rc.canBuildRobot(RobotType.GARDENER, d)) return;
        try {
            rc.buildRobot(RobotType.GARDENER, d);
            Build.incrementRobotsBuilt();
            rc.broadcast(Communication.HAS_BUILT_GARDENER,1);
            System.out.println("- Faig pages ");
            turnsSinceAllowed = 0;
            turnsSinceLastGardener = 0;
            bestZ = new MapLocation(-Constants.INF, 0);
        } catch (GameActionException e) {
            e.printStackTrace();
        }
    }

    private static boolean myTurn(){
        try {
            if (rc.getRoundNum() < 25) return leader; //al principi nomes fa pagesos el lider
            int archonNumber = Math.max(0,rc.readBroadcast(Communication.ARCHONS_LAST_TURN));
            return (rc.getRoundNum()%archonNumber == whoAmI);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    private static void updateArchonCount(){
        try {
            int archonTurn = rc.readBroadcast(Communication.ARCHON_TURN);
            int archonCount = rc.readBroadcast(Communication.ARCHON_COUNT);
            if (archonTurn != rc.getRoundNum()){
                firstArchon = true;
                rc.broadcast(Communication.ARCHON_TURN,rc.getRoundNum());
                rc.broadcast(Communication.ARCHON_COUNT,1);
                rc.broadcast(Communication.ARCHONS_LAST_TURN, archonCount);
                whoAmI = 0;
            } else {
                rc.broadcast(Communication.ARCHON_COUNT, archonCount+1);
                whoAmI = archonCount;
            }
        } catch (GameActionException e) {
            e.printStackTrace();
        }
    }

    private static void readMessages(){
        try {
            int channel = Communication.GARD_FREE_SPOTS;
            int lastMessage = rc.readBroadcast(channel + Communication.CYCLIC_CHANNEL_LENGTH);
            for(int i = initialMessageFreeSpots; i != lastMessage;) {
                int a = rc.readBroadcast(channel + i);
                workMessageFreeSpots(a);
                ++i;
                if (i >= Communication.CYCLIC_CHANNEL_LENGTH) i -= Communication.CYCLIC_CHANNEL_LENGTH;
            }
            System.out.println("Hi ha " + totalFreeSpots + " free spots");
            initialMessageFreeSpots = lastMessage;
        } catch (GameActionException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }


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

        if (aliveGardeners == 0)
        {
        	shouldBuildGardener = true;
        }
        else{
            float ratio = (float) totalFreeSpots/(float) aliveGardeners;
            float maxRatioToBuildGardener = 0.6f;
            if (ratio < maxRatioToBuildGardener && rc.getRoundNum() > 30) shouldBuildGardener = true;

        }

    }


    private static void workMessageFreeSpots(int bitmap){
        int[] m = Communication.decode(bitmap);
        totalFreeSpots += m[3];
    }


    public static float getSurfaceArea(){
        MapLocation center = rc.getLocation();
        float r = rc.getType().sensorRadius;
        float dTop = Map.maxY - center.y;
        float dRight = Map.maxX - center.x;
        float dBot = center.y - Map.minY;
        float dLeft = center.x - Map.minX;
        float dHor = Math.min(dLeft,dRight);
        float dVer = Math.min(dBot,dTop);
        float dMax = Math.max(dHor,dVer);
        float dMin = Math.min(dHor,dVer);
        if (dMin > r){
            //tot dintre el mapa
            return (float) Math.PI * r * r;
        }
        if (dMax > r){
            //prop de l'aresta
            float angle = 2 * (float) Math.acos(dMin / r);
            return r*r*(2*(float)Math.PI+(float)Math.sin(angle)-angle)/2;
        }
        if (dMin*dMin + dMax*dMax < r*r){
            //el vertex esta dintre el cercle
            float a1 = (float) Math.acos(dMin/r);
            float a2 = (float) Math.acos(dMax/r);
            float S1 = dMax*dMin;
            float S2 = r*dMin*(float)Math.sin(a1)/2;
            float S3 = r*dMax*(float)Math.sin(a2)/2;
            float S4 = r*r*(1.5f*(float)Math.PI -a1-a2)/2;
            return S1+S2+S3+S4;
        }
        //el vertex esta casi a dins
        float a1 = 2*(float) Math.acos(dMin/r);
        float a2 = 2*(float) Math.acos(dMax/r);
        return r*r*(2*(float)Math.PI + +(float)Math.sin(a1)-a1+(float)Math.sin(a2)-a2);
    }


    static void broadcastLocations() {

        RobotInfo[] Ri = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        emergencyTarget = null;

        int foundSoldier = 0;
        int foundTank = 0;

        float xSol = 0, ySol = 0, xTank = 0, yTank = 0;

        MapLocation pos = rc.getLocation();


        for (RobotInfo ri : Ri) {
            if (Clock.getBytecodesLeft() < 1500) return;
            MapLocation enemyPos = ri.getLocation();
            int x = Math.round(enemyPos.x);
            int y = Math.round(enemyPos.y);
            int a = Constants.getIndex(ri.type);
            if (a == 0) Communication.sendMessage(Communication.ENEMYGARDENERCHANNEL, x, y, 0);
            else if (a == 5) Communication.sendMessage(Communication.ENEMYGARDENERCHANNEL, x, y, 5);
            Communication.sendMessage(Communication.ENEMYCHANNEL, Math.round(enemyPos.x), Math.round(enemyPos.y), a);

            if (a == 2){
                ++foundSoldier;
                float dinv = 1/pos.distanceTo(enemyPos);
                xSol += dinv*(pos.x - enemyPos.x);
                ySol += dinv*(pos.y - enemyPos.y);
            }

            if (a == 3){
                ++foundTank;
                float dinv = 1/pos.distanceTo(enemyPos);
                xTank += dinv*(pos.x - enemyPos.x);
                yTank += dinv*(pos.y - enemyPos.y);
            }

        }

        float randomDev = (0.5f - (float)Math.random())/5.0f;

        if (foundTank > 0){
            Direction dir = new Direction(xTank, yTank).rotateLeftRads(randomDev);
            emergencyTarget = pos.add(dir, rc.getType().strideRadius+1);
        } else if (foundSoldier > 0){
            Direction dir = new Direction(xSol, ySol).rotateLeftRads(randomDev);
            emergencyTarget = pos.add(dir, rc.getType().strideRadius+1);
        }


        TreeInfo[] Ti = rc.senseNearbyTrees(-1, rc.getTeam().opponent());
        if (Ti.length > 0) {
            TreeInfo ti = Ti[0];
            if (Clock.getBytecodesLeft() < 1000) return;
            MapLocation treePos = ti.getLocation();
            int x = Math.round(treePos.x);
            int y = Math.round(treePos.y);
            Communication.sendMessage(Communication.ENEMYTREECHANNEL, x, y, 0);
        }

        Ti = rc.senseNearbyTrees(-1, Team.NEUTRAL);

        float numberOfBullets = 0;

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
            numberOfBullets += ti.getContainedBullets();
        }

        if (numberOfBullets >= 40) {
            int x = Math.round(rc.getLocation().x);
            int y = Math.round(rc.getLocation().y);
            Communication.sendMessage(Communication.NEEDTROOPCHANNEL, x, y, Communication.NEEDSCOUT);
        }


        float score = 0;
        for (RobotInfo enemy: enemies){
            if (enemy.getType() == RobotType.SCOUT) score += 0.2;
            if (enemy.getType() == RobotType.LUMBERJACK) score += 0.5;
            if (enemy.getType() == RobotType.SOLDIER) score += 1;
            if (enemy.getType() == RobotType.TANK) score += 2;
        }
        for (RobotInfo ally: allies){
            if (ally.getType() == RobotType.SCOUT) score -= 0.2;
            if (ally.getType() == RobotType.LUMBERJACK) score -= 0.5;
            if (ally.getType() == RobotType.SOLDIER) score -= 1;
            if (ally.getType() == RobotType.TANK) score -= 2;
        }
        if (score > 0)
            Communication.sendMessage(Communication.EMERGENCYCHANNEL,Math.round(rc.getLocation().x),Math.round(rc.getLocation().y),rc.getID());

    }

    //Agafa les zones mes properes, calcula el score i retorna la millor
    private static MapLocation findBestZone() {

        float bestScore = -1f;
        int[] myZone = ZoneG.getZoneFromPos(rc.getLocation());
        System.out.println("myZone: " + myZone[0] + " " + myZone[1]);
        MapLocation bestCenter = ZoneG.center(myZone);
        for(int i = 0; i < 19; ++i) { //busca les 19 zones mes properes
            int[] newZone = new int[]{myZone[0] + xHex[i], myZone[1] + yHex[i]};
            MapLocation newCenter = ZoneG.center(newZone);

            if(!rc.canSenseLocation(newCenter)) continue;
            float score = zoneScore(newCenter);
            if(score > bestScore) {
                bestScore = score;
                if(score == 2.0f) return newCenter;
                bestCenter = newCenter;
            }
            if(Clock.getBytecodeNum() >= Constants.BYTECODEMAXARCHONZONE) return bestCenter;
        }
        return bestCenter;
    }

   static float zoneScore(MapLocation center) {
        // score = -1: fora del mapa/ ja ocupada / whatever
        // score = 0: un dels llocs a construir esta ocupat
        // score = 0.5: pot veure nomes un tros de la zona
        // score = 2: zona lliure
        // retorna el score menys una quantitat en funcio de la distancia
        float score = 0;
        float R = RobotType.GARDENER.bodyRadius;
        try {
        	if (!rc.canSenseAllOfCircle(center, R)) return -1.0f;
            if(!rc.onTheMap(center, R)) return -1.0f;

            RobotInfo gardener = rc.senseRobotAtLocation(center);
            if(gardener != null && gardener.getType().equals(RobotType.GARDENER)) return -1.0f;

            float r = GameConstants.BULLET_TREE_RADIUS;

            float a = (float)Math.PI/6; //ara l'angle es 30
	        Direction dBase = new Direction(a);

            TreeInfo[] trees = rc.senseNearbyTrees(center, R + r, null);
            for (int i = 0; i < 6; i++) {
            	float score_i = 2;
            	MapLocation toBuild = center.add(dBase.rotateLeftRads((float)Math.PI*i/3),R+r+GameConstants.GENERAL_SPAWN_OFFSET);
            	if (!rc.canSenseAllOfCircle(toBuild, r)) score_i = 1;
                else if (!rc.onTheMap(toBuild, r)) score_i = -0.5f;
		        for (TreeInfo tree: trees) {
		            if (i == 0 && center.distanceTo(tree.getLocation()) <= R + tree.getRadius()) return -1; //si l'arbre talla el centre

	                if (toBuild.distanceTo(tree.getLocation()) <= r + tree.getRadius()){
	                    score_i = Math.min(score_i,0);
	                }
	            }
		        score += score_i;
		        if(score_i == 2) rc.setIndicatorDot(toBuild, 0, 200, 0);
		        if(score_i == 1) rc.setIndicatorDot(toBuild, 0, 0, 200);
		        if(score_i == 0) rc.setIndicatorDot(toBuild, 200, 0, 0);
	        }
        }catch (GameActionException e) {
            e.printStackTrace();
        }


        float score2 = 9999.0f;
        for (MapLocation m : enemyArchons) score2 = Math.min(score2, center.distanceTo(m));

        return score + score2/10000;
    }
    private static void drawZone() {
        float a = (float)Math.PI/6; //ara l'angle es 30 /// 0.713724379f; //radiants de desfase = arcsin(sqrt(3/7))
        Direction dBase = new Direction(a);
        for (int i = 0; i < ZoneG.buildPositionsPerZone; i++){
            MapLocation toBuild = bestZone.add(dBase.rotateLeftRads((float)Math.PI*i/3),2.01f);
            if (Constants.DEBUG == 1) rc.setIndicatorDot(toBuild, 200, 0, 200);
        }
    }

    private static boolean initializeZone() {
        try {
            float xOrigin = Float.intBitsToFloat(rc.readBroadcast(Communication.ZONE_ORIGIN_X));
            if (xOrigin == 0){
                return false;
            }else{
                ZoneG.setOrigin(xOrigin,Float.intBitsToFloat(rc.readBroadcast(Communication.ZONE_ORIGIN_Y)));
                return true;
            }
        } catch (GameActionException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static void checkSuicide(){
        try {
            int previousSuicides = rc.readBroadcast(Communication.ARCHONS_SUICIDED);
            if (rc.getRoundNum() < 200 + previousSuicides) return;
            int gardsBuilt = rc.readBroadcast(Communication.HAS_BUILT_GARDENER);
            int robotsBuilt = rc.readBroadcast(Communication.ROBOTS_BUILT);
            if (rc.getInitialArchonLocations(rc.getTeam()).length == 1 && gardsBuilt == 0) return;
            if (robotsBuilt > 1) return;
            rc.broadcast(Communication.ARCHONS_SUICIDED, previousSuicides + 1);
            rc.disintegrate();
        } catch (GameActionException e) {
            e.printStackTrace();
        }
    }

    private static void firstGardener()
    {
    	if(!cornered())
    	{
    	    rc.setIndicatorDot(rc.getLocation(),200,0,0);
	    	MapLocation nearestEnemy = enemyArchons[0];

	    	float distance = rc.getLocation().distanceTo(enemyArchons[0]);
	    	for(int i = 1; i < enemyArchons.length; ++i)
	    	{
	    		float d = rc.getLocation().distanceTo(enemyArchons[i]);
	    		if(d < distance)
	    		{
	    			distance = d;
	    			nearestEnemy = enemyArchons[i];
	    		}
	    	}

	    	Direction desiredDir = rc.getLocation().directionTo(nearestEnemy).opposite();
	    	Direction realDir = Build.findDirectionToBuild(desiredDir, 3.0f);
	    	if(realDir == null)
	    	{
	    		realDir = Build.findDirectionToBuild(desiredDir, 2.0f);
	    		if(realDir == null)
	    		{
	    			realDir = Build.findDirectionToBuild(desiredDir, 1.0f);
	    		}
	    	}
	    	if(realDir != null) bestZone = rc.getLocation().add(realDir,RobotType.GARDENER.bodyRadius + RobotType.ARCHON.bodyRadius);
    	}
    }

    private static boolean cornered()
    {
    	float maxDist = 4.7f + Constants.eps;
    	float myX = rc.getLocation().x;
    	float myY = rc.getLocation().y;
    	boolean first = false;
    	//System.out.println("myX: " + myX + "myY: " + myY);
    	//System.out.println("maxX: " + Map.maxX + " maxY: " + Map.maxY + " minX: "+ Map.minX + " minY: " + Map.minY);
    	//TODO canviar per no gastar bytecode
    	if(Math.abs(myX - Map.maxX) <=  maxDist || Math.abs(myX - Map.minX) <= maxDist)
    	{
    		if(Map.checkMapBound(Direction.EAST) != null) firstHorizontal = Direction.EAST;
    		else firstHorizontal = Direction.WEST;
    		first = true;
    	}
    	if(first && (Math.abs(myY - Map.maxY) <=  maxDist || Math.abs(myY - Map.minY) <=  maxDist))
    	{
    		if(Map.checkMapBound(Direction.NORTH) != null) firstVertical = Direction.NORTH;
    		else firstVertical = Direction.SOUTH;
    		bestZone = null; 
    		return true;
    	}
    	return false; 
    }

    private static MapLocation buildFrom()
    {

        //rc.setIndicatorDot(rc.getLocation(),200,0,0);

        float x = 0, y = 0;
        for(int i = 0; i < enemyArchons.length; ++i)
        {
            x+= enemyArchons[i].x; y += enemyArchons[i].y;
        }

        x/= enemyArchons.length;
        y/= enemyArchons.length;

        MapLocation nearestEnemy = new MapLocation(x,y);
        if(bestZone == null)
        {
            System.out.println("Best Zone es null!");
            return null;
        }
        Direction bestZoneToArchon = bestZone.directionTo(nearestEnemy);

        return bestZone.add(Build.findDirectionToBuild(bestZone, bestZoneToArchon, RobotType.GARDENER.bodyRadius, RobotType.ARCHON.bodyRadius), RobotType.ARCHON.bodyRadius + RobotType.GARDENER.bodyRadius + GameConstants.GENERAL_SPAWN_OFFSET + Constants.eps);
    }
}