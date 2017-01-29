package ArchonsVisionPlayer;

import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

/**
 * Created by Pau on 24/01/2017.
 */
public class Build {

    private static RobotController rc;

    static void init(RobotController rc2){
        rc = rc2;
    }


    static boolean allowedToConstruct(int unitToConstruct){
        float cost = totalBulletCost(unitToConstruct);
        System.out.println("Construir " + unitToConstruct + " val " + cost + " (" + rc.getTeamBullets() + ")");
        return rc.getTeamBullets() > cost;
    }

    //Calcula les bales necessaries per construir una unit del tipus, comptant tambe les que fan falta per construir
    //les unitats que ens hem saltat en la llista
    static float totalBulletCost(int unit){
        float totalMoney = 0;
        boolean aliveArchon = Communication.areArchonsAlive();
        boolean aliveGardener = Communication.areGardenersAlive();
        if (!aliveGardener){
            if (unit == Constants.GARDENER) return RobotType.GARDENER.bulletCost;
            else return Constants.INF;
        }

        if (unit == Constants.TREE){
            if (aliveArchon) totalMoney += computeHowManyBehind(Constants.GARDENER, unit);
            totalMoney += computeHowManyBehind(Constants.LUMBERJACK, unit);
            totalMoney += computeHowManyBehind(Constants.SOLDIER, unit);
            totalMoney += computeHowManyBehind(Constants.TANK, unit);
            totalMoney += computeHowManyBehind(Constants.SCOUT, unit);
            //totalMoney += computeHowManyBehind(Constants.TREE, unit);
        }else if(unit == Constants.TANK){
            if (aliveArchon) totalMoney += computeHowManyBehind(Constants.GARDENER, unit);
            //totalMoney += computeHowManyBehind(Constants.LUMBERJACK, unit);
            //totalMoney += computeHowManyBehind(Constants.SOLDIER, unit);
            //totalMoney += computeHowManyBehind(Constants.TANK, unit);
            //totalMoney += computeHowManyBehind(Constants.SCOUT, unit);
            //totalMoney += computeHowManyBehind(Constants.TREE, unit);
        }else if(unit == Constants.GARDENER){
            //totalMoney += computeHowManyBehind(Constants.GARDENER, unit);
            totalMoney += computeHowManyBehind(Constants.LUMBERJACK, unit);
            totalMoney += computeHowManyBehind(Constants.SOLDIER, unit);
            totalMoney += computeHowManyBehind(Constants.TANK, unit);
            totalMoney += computeHowManyBehind(Constants.SCOUT, unit);
            //totalMoney += computeHowManyBehind(Constants.TREE, unit);
        }else{
            if (aliveArchon) totalMoney += computeHowManyBehind(Constants.GARDENER, unit);
            //totalMoney += computeHowManyBehind(Constants.LUMBERJACK, unit);
            //totalMoney += computeHowManyBehind(Constants.SOLDIER, unit);
            totalMoney += computeHowManyBehind(Constants.TANK, unit);
            //totalMoney += computeHowManyBehind(Constants.SCOUT, unit);
            //totalMoney += computeHowManyBehind(Constants.TREE, unit);
        }
        float myBulletCost;
        if (unit == Constants.TREE) myBulletCost = GameConstants.BULLET_TREE_COST;
        else myBulletCost = Constants.ProductionUnits[unit].bulletCost;
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

    //Diu quina de les unitats petites (scout, soldier, lumber) va abans en la llista de construccio
    static int bestSmallUnitToBuild(){
        try {
            int minQueue = 999999;
            int bestUnit = -1;
            for (int i = 1; i < 5; ++i) {
                if (i == 3) continue; //els tanks no son petits
                //mirem nomes lumberjacks, soldiers i scouts
                int a = rc.readBroadcast(Communication.unitChannels[i]);
                if (a < minQueue){
                    minQueue = a;
                    bestUnit = i;
                }
            }
            return bestUnit;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }


    static void incrementTreesBuilt(){
        try {
            int trees_built = rc.readBroadcast(Communication.TREES_BUILT);
            rc.broadcast(Communication.TREES_BUILT, trees_built + 1);
        } catch (GameActionException e) {
            e.printStackTrace();
        }
    }


    static void incrementRobotsBuilt(){
        try {
            int robots_built = rc.readBroadcast(Communication.ROBOTS_BUILT);
            rc.broadcast(Communication.ROBOTS_BUILT, robots_built + 1);
        } catch (GameActionException e) {
            e.printStackTrace();
        }
    }

    //quan construim el robot, actualitzem l'index de la llista de construccio
    static void updateAfterConstruct(int unitConstructed){
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
}
