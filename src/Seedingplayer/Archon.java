package Seedingplayer;

import battlecode.common.*;


/**
 * Created by Ivan on 1/9/2017.
 */
public class Archon {

    static RobotController rc;
    static int whoIam = 0;
    static int treeSpending;
    static int initialMessagePlant = 0;
    static MapLocation base;
    static int xBase, yBase;

    @SuppressWarnings("unused")
    public static void run(RobotController rcc) {

        rc = rcc;

        InitializeStuff();

        while (true) {
            Communication.sendReport(Communication.ARCHON_REPORT);
            treeSpending = 0;

            //readMessages();

            tryConstruct();
            //tryMove();
            randomMove();
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
            Clock.yield();
        }
    }

    private static void InitializeStuff(){
        try{

            MapLocation base = rc.getInitialArchonLocations(rc.getTeam())[0];
            xBase = Math.round(base.x);
            yBase = Math.round(base.y);

            Communication.init(rc,xBase,yBase);
            Build.init(rc);
            Map.init(rc);

            initialMessagePlant = 0;
            try{
                initialMessagePlant = rc.readBroadcast(Communication.PLANTTREECHANNEL + Communication.CYCLIC_CHANNEL_LENGTH);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }


            if (rc.getRoundNum() <= 1) {
                MapLocation[] archons = rc.getInitialArchonLocations(rc.getTeam());
                rc.broadcast(Communication.ARCHONNUMBER, archons.length);
                int a = rc.readBroadcast(Communication.INITIALIZED);
                if (a == 0) {
                    rc.broadcast(Communication.INITIALIZED, 1);
                    for (int i = 0; i < Communication.unitChannels.length; ++i) {
                        rc.broadcast(Communication.unitChannels[i], Constants.initialPositions[i]);
                    }
                }
                for (int i = 0; i < archons.length; ++i) if (archons[i].distanceTo(rc.getLocation()) < Constants.eps){
                    whoIam = i;
                    break;
                }
                // inicialitzem el limits del mapa
                rc.broadcast(Communication.MAP_UPPER_BOUND, Float.floatToIntBits(Constants.INF));
                rc.broadcast(Communication.MAP_LOWER_BOUND, Float.floatToIntBits(-Constants.INF));
                rc.broadcast(Communication.MAP_LEFT_BOUND, Float.floatToIntBits(-Constants.INF));
                rc.broadcast(Communication.MAP_RIGHT_BOUND, Float.floatToIntBits(Constants.INF));
            } else{
                int arch = rc.readBroadcast(Communication.ARCHONNUMBER);
                rc.broadcast(Communication.ARCHONNUMBER,arch+1);
                whoIam = arch;
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    static void randomMove(){
        try {
            int a = (int) Math.floor(Math.random() * 4.0);
            for (int i = 0; i < 4; ++i) {
                if (rc.canMove(Constants.main_dirs[(a + i) % 4])) {
                    rc.move(Constants.main_dirs[(a + i) % 4]);
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private static void tryConstruct(){
        if (!Build.allowedToConstruct(Constants.GARDENER)) return;

        if (!myTurn()) return;
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
            //int archonTurn = rc.readInfoBroadcast(Communication.ARCHONTURN);
            int archonNumber = rc.readBroadcast(Communication.ARCHONNUMBER);
            return (rc.getRoundNum()%archonNumber == whoIam);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

/*
    static void updateTurn(){
        try {
            int archonTurn = rc.readInfoBroadcast(Communication.ARCHONTURN);
            int archonNumber = rc.readInfoBroadcast(Communication.ARCHONNUMBER);
            ++archonTurn;
            if (archonTurn >= archonNumber) archonTurn -= archonNumber;
            rc.broadcastInfo(Communication.ARCHONTURN, archonTurn);
            return;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return;
    }


*/


}
