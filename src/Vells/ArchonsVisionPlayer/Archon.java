package Vells.ArchonsVisionPlayer;

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
    static boolean allowedToConstruct;

  //coses de buscar zona per pagesos
  private static int[] xHex = {0, 0, 1, 1, 0, -1, -1, 0, 1, 2, 2, 2, 1, 0, -1, -2, -2, -2, -1, 0, 1, 2, 3, 3, 3, 3, 2, 1, 0, -1, -2, -3, -3, -3, -3, -2, -1, 0, 1, 2, 3, 4, 4, 4, 4, 4, 3, 2, 1, 0, -1, -2, -3, -4, -4, -4, -4, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 5, 5, 5, 5, 5, 4, 3, 2, 1, 0, -1, -2, -3, -4, -5, -5, -5, -5, -5, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 6, 6, 6, 6, 6, 6, 5, 4, 3, 2, 1, 0, -1, -2, -3, -4, -5, -6, -6, -6, -6, -6, -6, -6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 7, 7, 7, 7, 7, 7, 7, 6, 5, 4, 3, 2, 1, 0, -1, -2, -3, -4, -5, -6, -7, -7, -7, -7, -7, -7, -7, -7, -6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 8, 8, 8, 8, 8, 8, 8, 8, 7, 6, 5, 4, 3, 2, 1, 0, -1, -2, -3, -4, -5, -6, -7, -8, -8, -8, -8, -8, -8, -8, -8, -8, -7, -6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, -1, -2, -3, -4, -5, -6, -7, -8, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -8, -7, -6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, -1, -2, -3, -4, -5, -6, -7, -8, -9, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -9, -8, -7, -6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, -1, -2, -3, -4, -5, -6, -7, -8, -9, -10, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -10, -9, -8, -7, -6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, -1, -2, -3, -4, -5, -6, -7, -8, -9, -10, -11, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -11, -10, -9, -8, -7, -6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, -1, -2, -3, -4, -5, -6, -7, -8, -9, -10, -11, -12, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -12, -11, -10, -9, -8, -7, -6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, -1, -2, -3, -4, -5, -6, -7, -8, -9, -10, -11, -12, -13, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -13, -12, -11, -10, -9, -8, -7, -6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, -1, -2, -3, -4, -5, -6, -7, -8, -9, -10, -11, -12, -13, -14, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -14, -13, -12, -11, -10, -9, -8, -7, -6, -5, -4, -3, -2, -1};
  private static int[] yHex = {0, -1, -1, 0, 1, 1, 0, -2, -2, -2, -1, 0, 1, 2, 2, 2, 1, 0, -1, -3, -3, -3, -3, -2, -1, 0, 1, 2, 3, 3, 3, 3, 2, 1, 0, -1, -2, -4, -4, -4, -4, -4, -3, -2, -1, 0, 1, 2, 3, 4, 4, 4, 4, 4, 3, 2, 1, 0, -1, -2, -3, -5, -5, -5, -5, -5, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 5, 5, 5, 5, 5, 4, 3, 2, 1, 0, -1, -2, -3, -4, -6, -6, -6, -6, -6, -6, -6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 6, 6, 6, 6, 6, 6, 5, 4, 3, 2, 1, 0, -1, -2, -3, -4, -5, -7, -7, -7, -7, -7, -7, -7, -7, -6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 7, 7, 7, 7, 7, 7, 7, 6, 5, 4, 3, 2, 1, 0, -1, -2, -3, -4, -5, -6, -8, -8, -8, -8, -8, -8, -8, -8, -8, -7, -6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 8, 8, 8, 8, 8, 8, 8, 8, 7, 6, 5, 4, 3, 2, 1, 0, -1, -2, -3, -4, -5, -6, -7, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -8, -7, -6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, -1, -2, -3, -4, -5, -6, -7, -8, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -9, -8, -7, -6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, -1, -2, -3, -4, -5, -6, -7, -8, -9, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -10, -9, -8, -7, -6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, -1, -2, -3, -4, -5, -6, -7, -8, -9, -10, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -11, -10, -9, -8, -7, -6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, -1, -2, -3, -4, -5, -6, -7, -8, -9, -10, -11, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -12, -11, -10, -9, -8, -7, -6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, -1, -2, -3, -4, -5, -6, -7, -8, -9, -10, -11, -12, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -13, -12, -11, -10, -9, -8, -7, -6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, -1, -2, -3, -4, -5, -6, -7, -8, -9, -10, -11, -12, -13, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -14, -13, -12, -11, -10, -9, -8, -7, -6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, -1, -2, -3, -4, -5, -6, -7, -8, -9, -10, -11, -12, -13, -14};

    
    @SuppressWarnings("unused")
    public static void run(RobotController rcc) {

        rc = rcc;
        ZoneG.init(rc);
        initializedZone = false;
        turnsSinceAllowed = 0;


        if (rc.getRoundNum() > 5)
        {
            init();
            initializeZone();
            turnsSinceAllowed = 0;
        }

        while (true) {
            Bot.shake(rc);
            Bot.donate(rc);
            Map.checkMapBounds();
            if (rc.getRoundNum() == 2) init2();
            if(rc.getRoundNum() >= 5 && !initializedZone) //TODO arreglar perque el pages SEMPRE sigui jefazo
            {
                initializedZone = true;
                initializeZone();
            }
            updateArchonCount();
            if (rc.getRoundNum() == 1) init();
            MapLocation newTarget;
            broadcastLocations();

            if(Build.allowedToConstruct(Constants.GARDENER)) allowedToConstruct = true;
            else allowedToConstruct = false;

            boolean danger = (emergencyTarget != null);
            if (emergencyTarget != null){
                System.out.println("Fuig de " + rc.getLocation() + " a " + emergencyTarget);
                //if (Constants.DEBUG == 1) rc.setIndicatorLine(rc.getLocation(),newTarget, 0, 255, 255);
                newTarget = emergencyTarget;
            }else {
                if(allowedToConstruct && initializedZone)
                {
                    ++turnsSinceAllowed;
                    bestZone = findBestZone();
                    rc.setIndicatorLine(rc.getLocation(),bestZone, 200, 0, 200);
                    drawZone();
                    Direction dirBestZone = rc.getLocation().directionTo(bestZone);
                    newTarget = bestZone.add(dirBestZone.opposite(),RobotType.ARCHON.bodyRadius+RobotType.GARDENER.bodyRadius+ GameConstants.GENERAL_SPAWN_OFFSET);
                    if(rc.getLocation().distanceTo(newTarget) < Constants.eps)tryConstruct();
                    else if(turnsSinceAllowed > 50) tryConstruct();
                }
                else
                {
                    newTarget = checkShakeTrees();
                    if (newTarget != null){
                        System.out.println("Va a fer shake de " + rc.getLocation() + " a " + newTarget);
                    }else{
                    /*try {
                        int a = (int) Math.floor(Math.random() * 4.0);
                        for (int i = 0; i < 4; i++){
                            Direction dirMove = Constants.main_dirs[(a + i) % 4];
                            if (rc.canMove(dirMove)) {
                                newTarget = rc.getLocation().add(dirMove);
                            }
                        }
                        if (newTarget == null) newTarget = rc.getLocation();
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                        e.printStackTrace();
                    }*/
                        Sight.computeSightRange(rc);
                        if (Sight.gradientX != 0 || Sight.gradientY != 0) {

                            Direction optim = new Direction(Sight.gradientX, Sight.gradientY);

                            newTarget = rc.getLocation().add(optim, 3.0f);
                        } else newTarget = rc.getLocation();
                        
                    }

                }
            }

            //if (myTurn() && rc.getRoundNum() > 10) {
            //    if (Communication.countArchons() == 1 || !danger) tryConstruct();
            //}
            try {
                if(rc.getTeamVictoryPoints() + rc.getTeamBullets()/(Constants.costOfVictoryPoints(rc.getRoundNum())) >= Constants.MAXVICTORYPONTS) rc.donate(rc.getTeamBullets());
                if (rc.getTeamBullets() > Constants.BULLET_LIMIT) rc.donate(rc.getTeamBullets() - Constants.BULLET_LIMIT);
                if (rc.getRoundNum() > Constants.LAST_ROUND_BUILD) {
                    float donation = Math.max(0, rc.getTeamBullets() - 20);
                    if (donation > 20)
                        rc.donate(donation);
                }
            } catch (GameActionException e) {
                e.printStackTrace();
            }

            updateTarget(newTarget);
            if (realTarget == null) {
                //if (Constants.DEBUG == 1) rc.setIndicatorDot(rc.getLocation(), 255, 0, 0);
            }else if (realTarget.distanceTo(rc.getLocation()) < Constants.eps){
                Greedy.moveToSelf(rc,Clock.getBytecodesLeft() - 500);
            } else Greedy.moveGreedy(rc, realTarget, Clock.getBytecodesLeft() - 500);

            Clock.yield();
        }
    }

    static MapLocation bestZ = new MapLocation(-Constants.INF, 0);
    

    private static void init(){
        try{
            MapLocation base = rc.getInitialArchonLocations(rc.getTeam())[0];
            xBase = Math.round(base.x);
            yBase = Math.round(base.y);
            Communication.init(rc,xBase,yBase);
            Build.init(rc);
            Map.init(rc);
            MapLocation[] archons = rc.getInitialArchonLocations(rc.getTeam());

            float score = getInitialScore();
            rc.broadcast(Communication.ARCHON_INIT_SCORE[whoAmI],Float.floatToIntBits(score));

            if (whoAmI == 0) { // first to execute
                rc.broadcast(Communication.ARCHONS_LAST_TURN, archons.length);


                // inicialitzem el limits del mapa
                rc.broadcast(Communication.MAP_UPPER_BOUND, Float.floatToIntBits(Constants.INF));
                rc.broadcast(Communication.MAP_LOWER_BOUND, Float.floatToIntBits(-Constants.INF));
                rc.broadcast(Communication.MAP_LEFT_BOUND, Float.floatToIntBits(-Constants.INF));
                rc.broadcast(Communication.MAP_RIGHT_BOUND, Float.floatToIntBits(Constants.INF));
            }

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
            chooseBuildOrder();
            for (int i = 0; i < Communication.unitChannels.length; ++i) {
                try {
                    rc.broadcast(Communication.unitChannels[i], Constants.initialPositions[i]);
                } catch (GameActionException e) {
                    e.printStackTrace();
                }
            }
            if(rc.getRoundNum() <= 50) tryConstruct();

        }
    }


    private static float getInitialScore(){
        float distToEnemy = Constants.INF;
        MapLocation myPos = rc.getLocation();
        MapLocation enemies[] = rc.getInitialArchonLocations(rc.getTeam().opponent());
        for (MapLocation enemy: enemies){
            distToEnemy = Math.min(distToEnemy,myPos.distanceTo(enemy));
        }


        float totalArea = getSurfaceArea();
        float treeArea = 0;
        TreeInfo[] trees = rc.senseNearbyTrees(-1,Team.NEUTRAL);
        float extraBullets = GameConstants.BULLETS_INITIAL_AMOUNT;
        for (TreeInfo tree: trees){
            //se que aixo no esta be pero es una merda fer-ho exacte
            treeArea += tree.getRadius()*tree.getRadius()*(float)Math.PI;
            if (tree.getContainedRobot() == null) continue;
            extraBullets += tree.getContainedRobot().bulletCost;
        }

        float freeArea = totalArea - treeArea;
        float score = distToEnemy * extraBullets * freeArea;
        System.out.println("dist bullets area " + distToEnemy + "," + extraBullets + "," + freeArea);
        System.out.println("Score = " + score);
        return score;
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
        return escapePos;
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
        if (!Build.allowedToConstruct(Constants.GARDENER)) return;
        //if (whichRobotToBuild(rc.readInfoBroadcast(Communication.ROBOTS_BUILT)) != RobotType.GARDENER) return;
        try {
            System.out.println("Index " + 0 + " = " + rc.readBroadcast(Communication.unitChannels[0]));
            System.out.println("Index " + 1 + " = " + rc.readBroadcast(Communication.unitChannels[1]));
            System.out.println("Index " + 2 + " = " + rc.readBroadcast(Communication.unitChannels[2]));
            System.out.println("Index " + 3 + " = " + rc.readBroadcast(Communication.unitChannels[3]));
            System.out.println("Index " + 4 + " = " + rc.readBroadcast(Communication.unitChannels[4]));
            System.out.println("Index " + 5 + " = " + rc.readBroadcast(Communication.unitChannels[5]));

        } catch (GameActionException e) {
            e.printStackTrace();
        }
        try{
            Direction d = Direction.EAST;
            if (bestZone != null)
                d = rc.getLocation().directionTo(bestZone);
            for (int i = 0; i < 25; ++i){
                Direction d2 = d.rotateLeftDegrees(360*i/50);
                if (rc.canHireGardener(d2)){
                    rc.hireGardener(d2);
                    Build.incrementRobotsBuilt();
                    Build.updateAfterConstruct(Constants.GARDENER);
                    turnsSinceAllowed = 0;
                    bestZ = new MapLocation(-Constants.INF, 0);
                    return;
                }
                d2 = d.rotateRightDegrees(360*(i+1)/50);
                if (rc.canHireGardener(d2)){
                    rc.hireGardener(d2);
                    Build.incrementRobotsBuilt();
                    Build.updateAfterConstruct(Constants.GARDENER);
                    turnsSinceAllowed = 0;
                    bestZ = new MapLocation(-Constants.INF, 0);
                    return;
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    static boolean myTurn(){
        try {
            int archonNumber = rc.readBroadcast(Communication.ARCHONS_LAST_TURN);
            return (rc.getRoundNum()%archonNumber == whoAmI);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    static void updateArchonCount(){
        try {
            int archonTurn = rc.readBroadcast(Communication.ARCHON_TURN);
            int archonCount = rc.readBroadcast(Communication.ARCHON_COUNT);
            if (archonTurn != rc.getRoundNum()){
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

    static void chooseBuildOrder(){
        //TODO
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
        boolean sent = false;

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
            if (dir != null){
                emergencyTarget = pos.add(dir, rc.getType().strideRadius+1);
            }
        } else if (foundSoldier > 0){
            Direction dir = new Direction(xSol, ySol).rotateLeftRads(randomDev);
            if (dir != null){
                emergencyTarget = pos.add(dir, rc.getType().strideRadius+1);
            }
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
    }
    
    private static MapLocation findBestZone() {
    	float bestScore = -1f;
    	MapLocation zone = rc.getLocation().add(Direction.NORTH);
    	for(int i = 0; i < 19; ++i) {
    			MapLocation zonePos = ZoneG.centerArchon(new int[] {xHex[i],yHex[i]});
    			
    			
    			int[] realZone = ZoneG.getZoneFromPos(zonePos);
    			
    			MapLocation center = ZoneG.center(realZone);
    			if(!rc.canSenseLocation(center)) continue;
    			float score = zoneScore(center); 
    			if(score > bestScore)
    			{
    				bestScore = score;
    				if(score == 2.0f) return center;
    				zone = center;
    			}

    			if(Clock.getBytecodeNum() >= Constants.BYTECODEMAXARCHONZONE)
                {
                    if(score != -1.0f) return zone;
                    else return center;
                }
    	}
    	return zone; 
    }
    static float zoneScore(MapLocation realZone)
    {
    	float score = 2.0f;
    	try {
    		if(!rc.onTheMap(realZone)) return -1.0f;
            RobotInfo gardener = rc.senseRobotAtLocation(realZone);
            if(gardener != null && gardener.getType().equals(RobotType.GARDENER)) return -1.0f;

            TreeInfo[] Ti = rc.senseNearbyTrees(realZone, 1.0f, null);
	    	if(Ti.length > 0) return -1.0f; 
	    	//RobotInfo[] Ri = rc.senseNearbyRobots(realZone,1.0f, null);
	    	//if(Ri.length > 0) score = 1.0f;

	    	
	    	float a = (float)Math.PI/6; //ara l'angle es 30 /// 0.713724379f; //radiants de desfase = arcsin(sqrt(3/7))
	        Direction dBase = new Direction(a);
	        for (int i = 0; i < ZoneG.buildPositionsPerZone; i++){
            
            	MapLocation toBuild = realZone.add(dBase.rotateLeftRads((float)Math.PI*i/3),2.01f);
            	if(!rc.canSenseAllOfCircle(toBuild,1.0f))
            	{
            		if(score > 0.5f) score = 0.5f;
            		continue;
            	}
            	
            	if(!rc.onTheMap(toBuild)) return 0; 
            	
            	Ti = rc.senseNearbyTrees(toBuild, 1.0f, null); 
    	    	if(Ti.length > 0) return 0; 

            	//Ri = rc.senseNearbyRobots(toBuild,1.0f,null);
            	//if(Ri.length > 0) if(score > 1.0f) score = 1.0f;
            } 
        }catch (GameActionException e) {
            e.printStackTrace();
         }
         score = score -Math.abs((rc.getLocation().distanceTo(realZone)-3.0f)/100.0f);
    	return score;
    }

    
    static void drawZone()
    {

        float a = (float)Math.PI/6; //ara l'angle es 30 /// 0.713724379f; //radiants de desfase = arcsin(sqrt(3/7))
        Direction dBase = new Direction(a);
        for (int i = 0; i < ZoneG.buildPositionsPerZone; i++){
            
            MapLocation toBuild = bestZone.add(dBase.rotateLeftRads((float)Math.PI*i/3),2.01f);
            rc.setIndicatorDot(toBuild, 200, 0, 200);
        }


    }

    static void initializeZone()
    {

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

}
