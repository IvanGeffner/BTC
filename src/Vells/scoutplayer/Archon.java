package scoutplayer;

import battlecode.common.*;


/**
 * Created by Ivan on 1/9/2017.
 */
public class Archon {

    static RobotController rc;
    static int whoIam = 0;
    static int treeSpending;
    static int initialMessage = 0;

    static int[][][] dibuix = {{{255,255,255},{255,255,255},{255,255,255},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{255,255,255},{255,255,255},{255,255,255},{255,255,255},{255,255,255},{255,255,255},{255,255,255},{255,255,255},{255,255,255},{255,255,255}},{{255,255,255},{255,255,255},{0,0,0},{175,175,167},{175,175,167},{175,175,167},{111,111,103},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{255,255,255},{255,255,255},{255,255,255},{255,255,255},{255,255,255},{255,255,255}},{{255,255,255},{0,0,0},{0,0,0},{111,111,103},{175,175,167},{175,175,167},{175,175,167},{175,175,167},{175,175,167},{175,175,167},{175,175,167},{0,0,0},{0,0,0},{0,0,0},{255,255,255},{255,255,255},{255,255,255}},{{0,0,0},{175,175,167},{175,175,167},{175,175,167},{175,175,167},{175,175,167},{175,175,167},{175,175,167},{175,175,167},{175,175,167},{175,175,167},{175,175,167},{175,175,167},{175,175,167},{0,0,0},{255,255,255},{255,255,255}},{{0,0,0},{111,111,103},{175,175,167},{175,175,167},{175,175,167},{175,175,167},{175,175,167},{175,175,167},{175,175,167},{175,175,167},{175,175,167},{175,175,167},{175,175,167},{175,175,167},{143,143,127},{0,0,0},{255,255,255}},{{0,0,0},{143,143,127},{111,111,103},{175,175,167},{175,175,167},{175,175,167},{175,175,167},{175,175,167},{175,175,167},{175,175,167},{175,175,167},{175,175,167},{175,175,167},{143,143,127},{143,143,127},{0,0,0},{255,255,255}},{{255,255,255},{0,0,0},{143,143,127},{175,175,167},{111,111,103},{111,111,103},{175,175,167},{248,208,183},{175,175,167},{175,175,167},{111,111,103},{111,111,103},{111,111,103},{143,143,127},{143,143,127},{0,0,0},{255,255,255}},{{255,255,255},{0,0,0},{143,143,127},{111,111,103},{248,208,183},{248,208,183},{248,208,183},{175,175,167},{248,208,183},{175,175,167},{248,208,183},{248,208,183},{248,208,183},{111,111,103},{143,143,127},{0,0,0},{255,255,255}},{{255,255,255},{95,63,63},{0,0,0},{216,159,119},{248,208,183},{248,208,183},{248,208,183},{248,208,183},{248,208,183},{248,208,183},{248,208,183},{248,208,183},{248,208,183},{216,159,119},{0,0,0},{95,63,63},{255,255,255}},{{0,0,0},{216,159,119},{0,0,0},{216,159,119},{95,63,63},{248,208,183},{248,208,183},{248,208,183},{248,208,183},{248,208,183},{248,208,183},{248,208,183},{95,63,63},{216,159,119},{0,0,0},{216,159,119},{0,0,0}},{{0,0,0},{216,159,119},{95,63,63},{111,111,103},{95,63,63},{95,63,63},{248,208,183},{216,159,119},{248,208,183},{216,159,119},{248,208,183},{95,63,63},{95,63,63},{111,111,103},{95,63,63},{216,159,119},{0,0,0}},{{255,255,255},{0,0,0},{95,63,63},{216,159,119},{159,119,79},{232,232,248},{0,0,0},{216,159,119},{248,208,183},{216,159,119},{0,0,0},{232,232,248},{159,119,79},{216,159,119},{95,63,63},{0,0,0},{255,255,255}},{{255,255,255},{255,255,255},{95,63,63},{216,159,119},{216,159,119},{248,208,183},{248,208,183},{248,208,183},{248,208,183},{248,208,183},{248,208,183},{248,208,183},{216,159,119},{216,159,119},{95,63,63},{255,255,255},{255,255,255}},{{255,255,255},{255,255,255},{255,255,255},{95,63,63},{216,159,119},{248,208,183},{248,208,183},{248,208,183},{248,208,183},{248,208,183},{248,208,183},{248,208,183},{216,159,119},{95,63,63},{255,255,255},{255,255,255},{255,255,255}},{{255,255,255},{255,255,255},{0,0,0},{95,63,63},{216,159,119},{216,159,119},{248,208,183},{248,208,183},{216,159,119},{248,208,183},{248,208,183},{216,159,119},{216,159,119},{95,63,63},{0,0,0},{255,255,255},{255,255,255}},{{255,255,255},{0,0,0},{208,200,216},{208,200,216},{95,63,63},{95,63,63},{159,119,79},{216,159,119},{216,159,119},{216,159,119},{159,119,79},{95,63,63},{95,63,63},{208,200,216},{208,200,216},{0,0,0},{255,255,255}},{{0,0,0},{151,143,175},{208,200,216},{208,200,216},{151,143,175},{232,232,248},{151,143,175},{95,63,63},{95,63,63},{95,63,63},{151,143,175},{232,232,248},{151,143,175},{208,200,216},{208,200,216},{151,143,175},{0,0,0}},{{0,0,0},{232,232,248},{232,232,248},{111,111,103},{151,143,175},{232,232,248},{232,232,248},{143,95,135},{143,95,135},{143,95,135},{232,232,248},{232,232,248},{151,143,175},{111,111,103},{232,232,248},{232,232,248},{0,0,0}},{{0,0,0},{208,200,216},{111,111,103},{111,111,103},{232,232,248},{151,143,175},{232,232,248},{143,95,135},{143,95,135},{143,95,135},{232,232,248},{151,143,175},{232,232,248},{111,111,103},{111,111,103},{208,200,216},{0,0,0}},{{255,255,255},{0,0,0},{111,111,103},{151,143,175},{232,232,248},{151,143,175},{232,232,248},{183,135,183},{183,135,183},{183,135,183},{232,232,248},{151,143,175},{232,232,248},{151,143,175},{111,111,103},{0,0,0},{255,255,255}},{{255,255,255},{255,255,255},{0,0,0},{0,0,0},{232,232,248},{232,232,248},{143,95,135},{183,135,183},{183,135,183},{183,135,183},{143,95,135},{232,232,248},{232,232,248},{0,0,0},{0,0,0},{255,255,255},{255,255,255}},{{255,255,255},{255,255,255},{0,0,0},{127,87,63},{0,0,0},{0,0,0},{183,135,183},{183,135,183},{183,135,183},{183,135,183},{183,135,183},{0,0,0},{0,0,0},{127,87,63},{0,0,0},{255,255,255},{255,255,255}},{{255,255,255},{0,0,0},{0,0,0},{95,63,63},{127,87,63},{127,87,63},{127,87,63},{0,0,0},{0,0,0},{0,0,0},{127,87,63},{127,87,63},{127,87,63},{95,63,63},{0,0,0},{255,255,255},{255,255,255}},{{0,0,0},{0,0,0},{0,0,0},{0,0,0},{151,143,175},{151,143,175},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{151,143,175},{151,143,175},{0,0,0},{0,0,0},{0,0,0},{255,255,255}},{{255,255,255},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{255,255,255}}};

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

            Greedy.moveToSelf(rc, 9200);

            float stride = 0.75f;
            MapLocation posDibuix = rc.getLocation();
            for (int i = 0; i < 25; ++i) {
                for (int j = 0; j < 17; ++j) {
                    rc.setIndicatorDot(posDibuix, dibuix[i][j][0], dibuix[i][j][1], dibuix[i][j][2]);
                    posDibuix = posDibuix.add(Direction.EAST, stride);
                }
                posDibuix = posDibuix.add(Direction.WEST, stride*17);
                posDibuix = posDibuix.add(Direction.SOUTH, stride);
            }

            Clock.yield();
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
        if (!shouldConstructGardener()) return;
        if (!myTurn()) return;
        try{
            for (int i = 0; i < 4; ++i){
                if (rc.canHireGardener(Constants.main_dirs[i])){
                    rc.hireGardener(Constants.main_dirs[i]);
                    updateConstruct(0);
                    return;
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        //updateTurn();

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

}
