package Seedingplayer;

import battlecode.common.*;


/**
 * Created by Ivan on 1/9/2017.
 */
public class Archon {

    static RobotController rc;
    static int whoAmI = -1; //none of your business
    static int xBase, yBase;

    static boolean leader;

    static MapLocation realTarget;

    @SuppressWarnings("unused")
    public static void run(RobotController rcc) {

        rc = rcc;

        while (true) {
            Shake.shake(rc);
            if (rc.getRoundNum() == 2) init2();
            updateArchonCount();
            if (rc.getRoundNum() == 1) init();
            MapLocation newTarget;
            newTarget = checkNearbyEnemies();
            if (newTarget != null){
                System.out.println("Fuig de " + rc.getLocation() + " a " + newTarget);
                if (Constants.DEBUG == 1) rc.setIndicatorLine(rc.getLocation(),newTarget, 0, 255, 255);
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

            if (myTurn() && rc.getRoundNum() > 5) tryConstruct();
            //Greedy.moveToSelf(rc, 9200);
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

            updateTarget(newTarget);
            if (realTarget == null) {
                if (Constants.DEBUG == 1) rc.setIndicatorDot(rc.getLocation(), 255, 0, 0);
            }else if (realTarget.distanceTo(rc.getLocation()) < Constants.eps){
                Greedy.moveToSelf(rc,Clock.getBytecodesLeft() - 500);
            } else Greedy.moveGreedy(rc, realTarget, Clock.getBytecodesLeft() - 500);


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
                try {
                    rc.broadcast(Communication.unitChannels[i], Constants.initialPositions[i]);
                } catch (GameActionException e) {
                    e.printStackTrace();
                }
            }
            tryConstruct();

        }
    }


    private static float getInitialScore(){

        return whoAmI + 10;
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

        rc.setIndicatorLine(myPos,escapePos, 0,255,255);
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

    static void chooseBuildOrder(){

    }

    static boolean firstToExecute(){
        try {
            return rc.readBroadcast(Communication.ARCHON_INIT_SCORE[0]) != 0;
        } catch (GameActionException e) {
            e.printStackTrace();
        }
        return false;
    }
}
