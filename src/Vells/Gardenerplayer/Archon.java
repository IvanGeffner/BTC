package Gardenerplayer;

import battlecode.common.*;


/**
 * Created by Ivan on 1/9/2017.
 */
public class Archon {

    static RobotController rc;
    static int whoIam = 0;
    static int treeSpending;
    static int initialMessage = 0;

    @SuppressWarnings("unused")
    public static void run(RobotController rcc) {

        rc = rcc;

        InitializeStuff();
        //code executed onece at the begining

        while (true) {
            //code executed continually, don't let it end

            treeSpending = 0;

            readMessages();

            tryConstruct();
            //tryMove();
            randomMove();

            //Greedy.moveGreedy(rc, rc.getLocation());

            Clock.yield();
        }
    }

    //ho fico nomes perque no em molesti, estic provant gardeners
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

    static void InitializeStuff(){
        try{
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


    static void readMessages(){
        try {
            int lastMessage = rc.readBroadcast(Communication.MAX_BROADCAST_MESSAGE);
            for (int i = initialMessage; i != lastMessage; ) {
                int a = rc.readBroadcast(i);
                workMessage(a);
                ++i;
                if (i >= Communication.MAX_BROADCAST_MESSAGE) i -= Communication.MAX_BROADCAST_MESSAGE;
            }
            initialMessage = lastMessage;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    static void workMessage(int a){
        int[] m = Communication.decode(a);
        if (m[0] == Communication.PLANTTREE) treeSpending += GameConstants.BULLET_TREE_COST;
    }

    static void tryConstruct(){
        //if (!shouldConstructGardener()) return;
        try {
            if (!myTurn()) return;
            if (whichRobotToBuild(rc.readBroadcast(Communication.ROBOTS_BUILT)) != RobotType.GARDENER) return;
            try{
                for (int i = 0; i < 4; ++i){
                    if (rc.canHireGardener(Constants.main_dirs[i])){
                        rc.hireGardener(Constants.main_dirs[i]);
                        incrementRobotsBuilt();
                        //updateConstruct(0);
                        return;
                    }
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        } catch (GameActionException e) {
            e.printStackTrace();
        }

        //updateTurn();

    }

    private static RobotType whichRobotToBuild(int index){
        int unit_to_build;
        if (index < Constants.initialBuild.length){
            unit_to_build = Constants.initialBuild[index];
        }else{
            int aux = index - Constants.initialBuild.length;
            unit_to_build = Constants.sequenceBuild[aux%(Constants.sequenceBuild.length)];
        }
        System.out.println("index " + index + "="+Constants.getRobotTypeFromIndex(unit_to_build));
        return Constants.getRobotTypeFromIndex(unit_to_build);
    }

    private static void incrementRobotsBuilt(){
        try {
            int robots_built = rc.readBroadcast(Communication.ROBOTS_BUILT);
            rc.broadcast(Communication.ROBOTS_BUILT, robots_built + 1);
        } catch (GameActionException e) {
            e.printStackTrace();
        }
    }


    static boolean myTurn(){
        try {
            //int archonTurn = rc.readBroadcast(Communication.ARCHONTURN);
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
            int archonTurn = rc.readBroadcast(Communication.ARCHONTURN);
            int archonNumber = rc.readBroadcast(Communication.ARCHONNUMBER);
            ++archonTurn;
            if (archonTurn >= archonNumber) archonTurn -= archonNumber;
            rc.broadcast(Communication.ARCHONTURN, archonTurn);
            return;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return;
    }

    static void updateConstruct (int a){
        try {
            int x = rc.readBroadcast(Communication.unitChannels[a]);
            int ans = x;
            boolean found = false;
            if (x < Constants.IBL) {
                for (int i = x + 1; i < Constants.IBL; ++i) {
                    ++ans;
                    if (Constants.initialBuild[i] == a) {
                        found = true;
                        break;
                    }
                }
                if (!found){
                    for (int i = 0; i < Constants.SBL; ++i){
                        ++ans;
                        if (Constants.sequenceBuild[i]== a){
                            found = true;
                            break;
                        }
                    }
                    if (!found) ans = 9999;
                }
            }
            else {
                for (int i = 0; i < Constants.SBL; ++i) {
                    ++ans;
                    if (Constants.sequenceBuild[(x+1+i) % Constants.SBL] == a) {
                        found = true;
                        break;
                    }
                }
                if (!found) ans = 9999;
            }

            rc.broadcast(Communication.unitChannels[a], ans);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    static boolean shouldConstructGardener(){
        try {
            float totalMoney = rc.getTeamBullets() - treeSpending;

            for (int i = 1; i < 5; ++i){
                totalMoney -= computeHowManyBehind(i, 0);
            }

            if (totalMoney >= Constants.ProductionUnits[0].bulletCost) return true;
            return false;


        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    static int computeHowManyBehind(int a, int b) { //b is 0 in this case, we can take modules out [bytecode]
        try {
            int x = rc.readBroadcast(Communication.unitChannels[a]);
            int y = rc.readBroadcast(Communication.unitChannels[b]);
            if(y < x) return 0;
            int ans = 0;
            if(y < Constants.IBL){
                for (int i = x; i < y; ++i) if (Constants.initialBuild[i] == a) ++ ans;
            } else if (x < Constants.IBL) {
                int totalInSequence = 0;
                int totalOffSet = 0;
                for (int i = x; i < Constants.IBL; ++i) if (Constants.initialBuild[i] == a) ++ ans;
                for (int i = 0; i < Constants.SBL; ++i){
                    if (Constants.sequenceBuild[i] == a){
                        ++totalInSequence;
                        if (i < y% Constants.SBL) ++totalOffSet;
                    }
                }
                ans += totalOffSet + totalInSequence*((y - Constants.IBL)/ Constants.SBL);
            } else {
                int totalInSequence = 0;
                int totalOffSet = 0;
                for (int i = 0; i < Constants.SBL; ++i) {
                    if (Constants.sequenceBuild[i] == a) {
                        ++totalInSequence;
                    }
                }
                int z = y % Constants.SBL;
                for (int i = x; true ;++i){
                    int realI = i% Constants.SBL;
                    if (realI == z) break;
                    if (Constants.sequenceBuild[realI] == a) ++ans;
                    ++totalOffSet;
                }

                ans += ((y - x - totalOffSet)/ Constants.SBL)*totalInSequence;
            }
            if (a < 5) return ans* Constants.ProductionUnits[a].bulletCost;
            else return ans*(int)GameConstants.BULLET_TREE_COST;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }
*/
}
