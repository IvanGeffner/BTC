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
            Communication.sendReport(rc, Communication.ARCHON_REPORT);
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

    static void InitializeStuff(){
        try{

            MapLocation base = rc.getInitialArchonLocations(rc.getTeam())[0];
            xBase = Math.round(base.x);
            yBase = Math.round(base.y);

            Communication.setBase(xBase,yBase);
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
/*
    static void readMessages(){
        try {
            int channel = Communication.PLANTTREECHANNEL;
            int lastMessage = rc.readInfoBroadcast(channel + Communication.CYCLIC_CHANNEL_LENGTH);
            System.out.println("Last and Initial: " + lastMessage + " " + initialMessagePlant);
            for (int i = initialMessagePlant; i != lastMessage && Clock.getBytecodesLeft() > Constants.BYTECODEPOSTMESSAGES; ) {
                int a = rc.readInfoBroadcast(channel + i);
                workMessagePlantTree(a);
                ++i;
                if (i >= Communication.CYCLIC_CHANNEL_LENGTH) i -= Communication.CYCLIC_CHANNEL_LENGTH;
            }
            initialMessagePlant = lastMessage;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    static void workMessagePlantTree(int a){
        treeSpending += GameConstants.BULLET_TREE_COST;
    }
*/
    static void tryConstruct(){
        if (!allowedToConstruct(Constants.GARDENER)) return;

        if (!myTurn()) return;
        //if (whichRobotToBuild(rc.readInfoBroadcast(Communication.ROBOTS_BUILT)) != RobotType.GARDENER) return;
        try{
            for (int i = 0; i < 4; ++i){
                if (rc.canHireGardener(Constants.main_dirs[i])){
                    rc.hireGardener(Constants.main_dirs[i]);
                    incrementRobotsBuilt();
                    updateAfterConstruct(Constants.GARDENER);
                    //updateConstruct(0);
                    return;
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        //updateTurn();

    }


    static boolean allowedToConstruct(int unitToConstruct){
        float cost = totalBulletCost(unitToConstruct);
        System.out.println("Cost gard: "+ cost + " (" + rc.getTeamBullets() + ")");
        return rc.getTeamBullets() > totalBulletCost(unitToConstruct);
    }

    private static float totalBulletCost(int unit){
        float totalMoney = 0;
        totalMoney += computeHowManyBehind(Constants.LUMBERJACK, unit);
        totalMoney += computeHowManyBehind(Constants.SOLDIER, unit);
        totalMoney += computeHowManyBehind(Constants.SCOUT, unit);
        float myBulletCost;
        myBulletCost = Constants.ProductionUnits[unit].bulletCost;
        return totalMoney + myBulletCost;
    }

    //calcula les bales que calen per construir tots els unit1 que van abans de unit2 en la cua
    static int computeHowManyBehind(int unit1, int unit2) {
        try {
            int indexUnit1 = rc.readBroadcast(Communication.unitChannels[unit1]);
            int indexUnit2 = rc.readBroadcast(Communication.unitChannels[unit2]);
            if(indexUnit1 > indexUnit2) return 0;
            //Sabem que index1 <= index2

            int howManyBehind = 0;
            if(indexUnit2 < Constants.IBL){
                // index1 <= index2 < IBL
                for (int i = indexUnit1; i < indexUnit2; ++i)
                    if (Constants.initialBuild[i] == unit1) howManyBehind++;

            } else if (indexUnit1 < Constants.IBL) {
                // index1 < IBL <= index2
                int totalInSequence = 0;
                int totalLastSequence = 0;
                for (int i = indexUnit1; i < Constants.IBL; ++i) if (Constants.initialBuild[i] == unit1) howManyBehind++;
                for (int i = 0; i < Constants.SBL; ++i){
                    if (Constants.sequenceBuild[i] == unit1){
                        ++totalInSequence;
                        if (i < indexUnit2% Constants.SBL) ++totalLastSequence;
                    }
                }
                int extraWholeSequences = ((indexUnit2 - Constants.IBL)/ Constants.SBL);
                howManyBehind += totalLastSequence + totalInSequence*extraWholeSequences;
            } else {
                // IBL < index1 <= index2
                int totalInSequence = 0;
                int totalOffSet = 0;
                for (int i = 0; i < Constants.SBL; ++i) {
                    if (Constants.sequenceBuild[i] == unit1) {
                        ++totalInSequence;
                    }
                }
                int z = indexUnit2% Constants.SBL;
                for (int i = indexUnit1; true ;++i){
                    int realI = i% Constants.SBL;
                    if (realI == z) break;
                    if (Constants.sequenceBuild[realI] == unit1) ++howManyBehind;
                    ++totalOffSet;
                }

                howManyBehind += ((indexUnit2 - indexUnit1 - totalOffSet)/ Constants.SBL)*totalInSequence;
            }
            System.out.println("Hi ha " + howManyBehind + " " +unit1 + " behind " + unit2);
            if (unit1 < 5) return howManyBehind* Constants.ProductionUnits[unit1].bulletCost;
            else return howManyBehind* (int)GameConstants.BULLET_TREE_COST;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    private static RobotType whichRobotToBuild(int index){
        int unit_to_build;
        if (index < Constants.initialBuild.length){
            unit_to_build = Constants.initialBuild[index];
        }else{
            int aux = index - Constants.initialBuild.length;
            unit_to_build = Constants.sequenceBuild[aux%(Constants.sequenceBuild.length)];
        }
        System.out.println("index " + index + "="+ Constants.getRobotTypeFromIndex(unit_to_build));
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


    //quan construim el robot, actualitzem tot
    private static void updateAfterConstruct(int unitConstructed){
        try {
            int unitCurrentIndex = rc.readBroadcast(Communication.unitChannels[unitConstructed]);
            int unitNextIndex;
            if (unitCurrentIndex < Constants.IBL) {
                //si esta a la build inicial
                int i = unitCurrentIndex+1;
                while (i < Constants.IBL && Constants.initialBuild[i] != unitConstructed) i++;
                //ja no es torna a fer a la build inicial
                if (i == Constants.IBL){
                    int j = 0;
                    while (j < Constants.SBL && Constants.sequenceBuild[j] != unitConstructed) j++;
                    if (j == Constants.SBL){
                        //ja no la tornem a fer mai mes
                        unitNextIndex = (int) Constants.INF;
                    }else{
                        unitNextIndex = Constants.IBL + j;
                    }
                }else unitNextIndex = i;
            }else {
                int i = 0;
                while (i < Constants.SBL && Constants.sequenceBuild[(unitCurrentIndex+1+i) % Constants.SBL] != unitConstructed) i++;
                if (i == Constants.SBL) unitNextIndex = (int) Constants.INF;
                else unitNextIndex = unitCurrentIndex + 1 + i;
            }
            rc.broadcast(Communication.unitChannels[unitConstructed], unitNextIndex);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }


    //al fer el merge he ficat el codi del archon del gardener player
    //el codi antic esta aqui comentat

/*
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
*/
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

    static void updateConstruct (int a){
        try {
            int x = rc.readInfoBroadcast(Communication.unitChannels[a]);
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

            rc.broadcastInfo(Communication.unitChannels[a], ans);
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
            int x = rc.readInfoBroadcast(Communication.unitChannels[a]);
            int y = rc.readInfoBroadcast(Communication.unitChannels[b]);
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
