package Mergedplayer;

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

    static int[][][] dibuix = {{{255,255,255},{255,255,255},{255,255,255},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{255,255,255},{255,255,255},{255,255,255},{255,255,255},{255,255,255},{255,255,255},{255,255,255},{255,255,255},{255,255,255},{255,255,255}},{{255,255,255},{255,255,255},{0,0,0},{175,175,167},{175,175,167},{175,175,167},{111,111,103},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{255,255,255},{255,255,255},{255,255,255},{255,255,255},{255,255,255},{255,255,255}},{{255,255,255},{0,0,0},{0,0,0},{111,111,103},{175,175,167},{175,175,167},{175,175,167},{175,175,167},{175,175,167},{175,175,167},{175,175,167},{0,0,0},{0,0,0},{0,0,0},{255,255,255},{255,255,255},{255,255,255}},{{0,0,0},{175,175,167},{175,175,167},{175,175,167},{175,175,167},{175,175,167},{175,175,167},{175,175,167},{175,175,167},{175,175,167},{175,175,167},{175,175,167},{175,175,167},{175,175,167},{0,0,0},{255,255,255},{255,255,255}},{{0,0,0},{111,111,103},{175,175,167},{175,175,167},{175,175,167},{175,175,167},{175,175,167},{175,175,167},{175,175,167},{175,175,167},{175,175,167},{175,175,167},{175,175,167},{175,175,167},{143,143,127},{0,0,0},{255,255,255}},{{0,0,0},{143,143,127},{111,111,103},{175,175,167},{175,175,167},{175,175,167},{175,175,167},{175,175,167},{175,175,167},{175,175,167},{175,175,167},{175,175,167},{175,175,167},{143,143,127},{143,143,127},{0,0,0},{255,255,255}},{{255,255,255},{0,0,0},{143,143,127},{175,175,167},{111,111,103},{111,111,103},{175,175,167},{248,208,183},{175,175,167},{175,175,167},{111,111,103},{111,111,103},{111,111,103},{143,143,127},{143,143,127},{0,0,0},{255,255,255}},{{255,255,255},{0,0,0},{143,143,127},{111,111,103},{248,208,183},{248,208,183},{248,208,183},{175,175,167},{248,208,183},{175,175,167},{248,208,183},{248,208,183},{248,208,183},{111,111,103},{143,143,127},{0,0,0},{255,255,255}},{{255,255,255},{95,63,63},{0,0,0},{216,159,119},{248,208,183},{248,208,183},{248,208,183},{248,208,183},{248,208,183},{248,208,183},{248,208,183},{248,208,183},{248,208,183},{216,159,119},{0,0,0},{95,63,63},{255,255,255}},{{0,0,0},{216,159,119},{0,0,0},{216,159,119},{95,63,63},{248,208,183},{248,208,183},{248,208,183},{248,208,183},{248,208,183},{248,208,183},{248,208,183},{95,63,63},{216,159,119},{0,0,0},{216,159,119},{0,0,0}},{{0,0,0},{216,159,119},{95,63,63},{111,111,103},{95,63,63},{95,63,63},{248,208,183},{216,159,119},{248,208,183},{216,159,119},{248,208,183},{95,63,63},{95,63,63},{111,111,103},{95,63,63},{216,159,119},{0,0,0}},{{255,255,255},{0,0,0},{95,63,63},{216,159,119},{159,119,79},{232,232,248},{0,0,0},{216,159,119},{248,208,183},{216,159,119},{0,0,0},{232,232,248},{159,119,79},{216,159,119},{95,63,63},{0,0,0},{255,255,255}},{{255,255,255},{255,255,255},{95,63,63},{216,159,119},{216,159,119},{248,208,183},{248,208,183},{248,208,183},{248,208,183},{248,208,183},{248,208,183},{248,208,183},{216,159,119},{216,159,119},{95,63,63},{255,255,255},{255,255,255}},{{255,255,255},{255,255,255},{255,255,255},{95,63,63},{216,159,119},{248,208,183},{248,208,183},{248,208,183},{248,208,183},{248,208,183},{248,208,183},{248,208,183},{216,159,119},{95,63,63},{255,255,255},{255,255,255},{255,255,255}},{{255,255,255},{255,255,255},{0,0,0},{95,63,63},{216,159,119},{216,159,119},{248,208,183},{248,208,183},{216,159,119},{248,208,183},{248,208,183},{216,159,119},{216,159,119},{95,63,63},{0,0,0},{255,255,255},{255,255,255}},{{255,255,255},{0,0,0},{208,200,216},{208,200,216},{95,63,63},{95,63,63},{159,119,79},{216,159,119},{216,159,119},{216,159,119},{159,119,79},{95,63,63},{95,63,63},{208,200,216},{208,200,216},{0,0,0},{255,255,255}},{{0,0,0},{151,143,175},{208,200,216},{208,200,216},{151,143,175},{232,232,248},{151,143,175},{95,63,63},{95,63,63},{95,63,63},{151,143,175},{232,232,248},{151,143,175},{208,200,216},{208,200,216},{151,143,175},{0,0,0}},{{0,0,0},{232,232,248},{232,232,248},{111,111,103},{151,143,175},{232,232,248},{232,232,248},{143,95,135},{143,95,135},{143,95,135},{232,232,248},{232,232,248},{151,143,175},{111,111,103},{232,232,248},{232,232,248},{0,0,0}},{{0,0,0},{208,200,216},{111,111,103},{111,111,103},{232,232,248},{151,143,175},{232,232,248},{143,95,135},{143,95,135},{143,95,135},{232,232,248},{151,143,175},{232,232,248},{111,111,103},{111,111,103},{208,200,216},{0,0,0}},{{255,255,255},{0,0,0},{111,111,103},{151,143,175},{232,232,248},{151,143,175},{232,232,248},{183,135,183},{183,135,183},{183,135,183},{232,232,248},{151,143,175},{232,232,248},{151,143,175},{111,111,103},{0,0,0},{255,255,255}},{{255,255,255},{255,255,255},{0,0,0},{0,0,0},{232,232,248},{232,232,248},{143,95,135},{183,135,183},{183,135,183},{183,135,183},{143,95,135},{232,232,248},{232,232,248},{0,0,0},{0,0,0},{255,255,255},{255,255,255}},{{255,255,255},{255,255,255},{0,0,0},{127,87,63},{0,0,0},{0,0,0},{183,135,183},{183,135,183},{183,135,183},{183,135,183},{183,135,183},{0,0,0},{0,0,0},{127,87,63},{0,0,0},{255,255,255},{255,255,255}},{{255,255,255},{0,0,0},{0,0,0},{95,63,63},{127,87,63},{127,87,63},{127,87,63},{0,0,0},{0,0,0},{0,0,0},{127,87,63},{127,87,63},{127,87,63},{95,63,63},{0,0,0},{255,255,255},{255,255,255}},{{0,0,0},{0,0,0},{0,0,0},{0,0,0},{151,143,175},{151,143,175},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{151,143,175},{151,143,175},{0,0,0},{0,0,0},{0,0,0},{255,255,255}},{{255,255,255},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{255,255,255}}};


    @SuppressWarnings("unused")
    public static void run(RobotController rcc) {

        rc = rcc;
        if (rc.getRoundNum() > 1) init(); //pels archons que guanyem mes tard

        while (true) {
            Shake.shake(rc);
            if (rc.getRoundNum() == 2) init2();
            updateArchonCount();
            if (rc.getRoundNum() == 1) init();
            MapLocation newTarget;
            newTarget = checkNearbyEnemies();
            if (newTarget != null){
                System.out.println("Fuig de " + rc.getLocation() + " a " + newTarget);
                //if (Constants.DEBUG == 1) rc.setIndicatorLine(rc.getLocation(),newTarget, 0, 255, 255);
            }else {
                newTarget = checkShakeTrees();
                if (newTarget != null){
                    System.out.println("Va a fer shake de " + rc.getLocation() + " a " + newTarget);
                }else{
                    try {
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
                    }
                }
            }

            Map.checkMapBounds();
            if (myTurn() && rc.getRoundNum() > 5) tryConstruct();
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

            broadcastLocations();

            if (whoAmI == 0 && rc.getRoundNum() < 10) {
                try {
                    float x = 0, y = 0;
                    MapLocation[] inis = rc.getInitialArchonLocations(Team.A);
                    for (MapLocation ml : inis) {
                        x += ml.x;
                        y += ml.y;
                    }
                    inis = rc.getInitialArchonLocations(Team.B);
                    for (MapLocation ml : inis) {
                        x += ml.x;
                        y += ml.y;
                    }
                    x = x / inis.length/2;
                    y = y / inis.length/2;
                    float stride = 0.75f;
                    MapLocation posDibuix = new MapLocation(x - 17 / 2, y + 25 / 2);
                    for (int i = 0; i < 25; ++i) {
                        for (int j = 0; j < 17; ++j) {
                            rc.setIndicatorDot(posDibuix, dibuix[i][j][0], dibuix[i][j][1], dibuix[i][j][2]);
                            posDibuix = posDibuix.add(Direction.EAST, stride);
                        }
                        posDibuix = posDibuix.add(Direction.WEST, stride * 17);
                        posDibuix = posDibuix.add(Direction.SOUTH, stride);
                    }
                }
                catch(Exception e) {}
            }

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
                Build.updateAfterConstruct(i);
                //inicialitzem els build indexs
            }
            tryConstruct();

        }
    }


    private static float getInitialScore(){
        float distToEnemy = distToEnemyArchons();
        float extraBullets = freeRobotsNearby();
        float freeArea = freeAreaNearby();

        float score = distToEnemy * extraBullets * freeArea;
        System.out.println("dist bullets area " + distToEnemy + "," + extraBullets + "," + freeArea);
        System.out.println("Score = " + score);
        return score;
    }


    private static void chooseBuildOrder(){
        float distToEnemy = Math.min(100,distToEnemyArchons()) / 100;   //entre 0 i 1
        float freeArea = freeAreaToEnemy(); //entre 0 i 1

        System.out.println("Dist enemy = " + distToEnemy);
        System.out.println("free area = " + freeArea);

        float x = distToEnemy;
        float y = freeArea;
        //component x: dist
        //component y: freearea
        float[] p1 = {0.3f,0.7f};
        float[] p2 = {0.6f,0.35f};
        float[] p3 = {0.8f,0.7f};
        float[] p4 = {0.5f,0.85f};


        try {
            if (x < p4[0] && y >= p1[1] && (x - p1[0]) * (p4[1] - p1[1]) <= (y - p1[1]) * (p4[0] - p1[0])) {
                rc.broadcast(Communication.BUILDPATH, Constants.RUSH_BUILD);
            }else if (x < p2[0] && y < p1[1] && (x - p1[0]) * (p2[1] - p1[1]) > (y - p1[1]) * (p2[0] - p1[0])) {
                rc.broadcast(Communication.BUILDPATH, Constants.CLOSE_CAGED_BUILD);
            }else if (x >= p4[0] && y >= p3[1] && (x - p4[0]) * (p3[1] - p4[1]) <= (y - p4[1]) * (p3[0] - p4[0])) {
                rc.broadcast(Communication.BUILDPATH, Constants.FAR_OPEN_BUILD);
            }else if (x >= p2[0] && y < p3[1] && (x - p2[0]) * (p3[1] - p2[1]) > (y - p2[1]) * (p3[0] - p2[0])) {
                rc.broadcast(Communication.BUILDPATH, Constants.FAR_CAGED_BUILD);
            }else rc.broadcast(Communication.BUILDPATH, Constants.BALANCED_BUILD);
            System.out.println("El build order es " + rc.readBroadcast(Communication.BUILDPATH));
        } catch (GameActionException e) {
            e.printStackTrace();
        }

    }

    //a l'archon mes proper inicial enemic
    private static float distToEnemyArchons(){
        float distToEnemy = Constants.INF;
        MapLocation myPos = rc.getLocation();
        MapLocation enemies[] = rc.getInitialArchonLocations(rc.getTeam().opponent());
        for (MapLocation enemy: enemies){
            distToEnemy = Math.min(distToEnemy,myPos.distanceTo(enemy));
        }
        return distToEnemy;
    }

    private static float freeRobotsNearby(){
        TreeInfo[] trees = rc.senseNearbyTrees(-1,Team.NEUTRAL);
        float extraBullets = GameConstants.BULLETS_INITIAL_AMOUNT;
        for (TreeInfo tree: trees){
            if (tree.getContainedRobot() == null) continue;
            extraBullets += tree.getContainedRobot().bulletCost;
        }
        return extraBullets;
    }

    private static float freeAreaNearby(){
        float totalArea = getSurfaceArea();
        float treeArea = 0;
        TreeInfo[] trees = rc.senseNearbyTrees(-1,Team.NEUTRAL);
        for (TreeInfo tree: trees){
            //se que aixo no esta be pero es una merda fer-ho exacte
            treeArea += tree.getRadius()*tree.getRadius()*(float)Math.PI;
        }
        float freeArea = totalArea - treeArea;
        return Math.max(0,freeArea);
    }

    private static float freeAreaToEnemy(){
        MapLocation enemies[] = rc.getInitialArchonLocations(rc.getTeam().opponent());
        TreeInfo[] trees = rc.senseNearbyTrees(-1,Team.NEUTRAL);
        MapLocation myPos = rc.getLocation();
        float r = rc.getType().sensorRadius;
        float meanFreeArea = 0;
        for (MapLocation enemy: enemies){
            Direction dirEnemy = myPos.directionTo(enemy);
            float treeArea = 0;
            for (TreeInfo tree: trees){
                Direction dirTree = myPos.directionTo(tree.getLocation());
                if (Math.abs(dirEnemy.radiansBetween(dirTree)) > Math.PI / 4) continue; //agafem nomes l'angle de 90 graus cap a l'archon
                treeArea += tree.getRadius() * tree.getRadius() * Math.PI;
            }
            float quarter = (r*r*(float)Math.PI)/4;
            float ratioFreeArea = (quarter -treeArea) / quarter;
            meanFreeArea += Math.max(0,ratioFreeArea);
        }
        meanFreeArea = meanFreeArea / enemies.length;
        System.out.println("% area cap a l'enemic = " + meanFreeArea);
        return meanFreeArea;
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
        try{
            for (int i = 0; i < 4; ++i){
                if (rc.canHireGardener(Constants.main_dirs[i])){
                    rc.hireGardener(Constants.main_dirs[i]);
                    Build.incrementRobotsBuilt();
                    Build.updateAfterConstruct(Constants.GARDENER);
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


        for (RobotInfo ri : Ri) {
            if (Clock.getBytecodesLeft() < 1500) return;
            MapLocation enemyPos = ri.getLocation();
            int x = Math.round(enemyPos.x);
            int y = Math.round(enemyPos.y);
            int a = Constants.getIndex(ri.type);
            if (a == 0) Communication.sendMessage(Communication.ENEMYGARDENERCHANNEL, x, y, 0);
            else if (a == 5) Communication.sendMessage(Communication.ENEMYGARDENERCHANNEL, x, y, 5);
            Communication.sendMessage(Communication.ENEMYCHANNEL, Math.round(enemyPos.x), Math.round(enemyPos.y), a);
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

}
