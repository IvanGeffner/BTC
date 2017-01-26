package firstplayer;
import battlecode.common.*;

/**
 * Created by Ivan on 1/9/2017.
 */
public class Archon {
    @SuppressWarnings("unused")

    static RobotController rc;

    public static void run(RobotController rcc) {

        rc = rcc;
        int currentRound = 10000;

        while (true) {
            //code executed continually, don't let it end

            if (rc.getRoundNum() < currentRound){
                currentRound = rc.getRoundNum();
                try {
                    Initialize();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
            }

            construct();


            Clock.yield();
        }
    }

    static void construct(){
        if (!shouldConstructGardener()) return;
        try{
            for (int i = 0; i < 4; ++i){
                if (rc.canHireGardener(Util.main_dirs[i])){
                    rc.hireGardener(Util.main_dirs[i]);
                    updateConstruct(0);
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

    }

    static void updateConstruct (int a){
        try {
            int x = rc.readBroadcast(Util.unitChannels[a]);
            int ans = x;
            boolean found = false;
            if (x < Util.IBL) {
                for (int i = x + 1; i < Util.IBL; ++i) {
                    ++ans;
                    if (Util.initialBuild[i] == a) {
                        found = true;
                        break;
                    }
                }
                if (!found){
                    for (int i = 0; i < Util.SBL; ++i){
                        ++ans;
                        if (Util.sequenceBuild[i]== a){
                            found = true;
                            break;
                        }
                    }
                    if (!found) ans = 9999;
                }
            }
            else {
                for (int i = 0; i < Util.SBL; ++i) {
                    ++ans;
                    if (Util.sequenceBuild[(x+1+i) % Util.SBL] == a) {
                        found = true;
                        break;
                    }
                }
                if (!found) ans = 9999;
            }

            rc.broadcast(Util.unitChannels[a], ans);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    static boolean shouldConstructGardener(){
        try {
            float totalMoney = rc.getTeamBullets();

            for (int i = 1; i < 6; ++i){
                totalMoney -= computeHowManyBehind(i, 0);
            }

            if (totalMoney >= Util.ProductionUnits[0].bulletCost) return true;
            return false;


        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    static int computeHowManyBehind(int a, int b) {
        try {
            int x = rc.readBroadcast(Util.unitChannels[a]);
            int y = rc.readBroadcast(Util.unitChannels[b]);
            if(y < x) return 0;
            int ans = 0;
            if(y < Util.IBL){
                for (int i = x; i < y; ++i) if (Util.initialBuild[i] == a) ++ ans;
            } else if (x < Util.IBL) {
                int totalInSequence = 0;
                int totalOffSet = 0;
                for (int i = x; i < Util.IBL; ++i) if (Util.initialBuild[i] == a) ++ ans;
                for (int i = 0; i < Util.SBL; ++i){
                    if (Util.sequenceBuild[i] == a){
                        ++totalInSequence;
                        if (i < y%Util.SBL) ++totalOffSet;
                    }
                }
                ans += totalOffSet + totalInSequence*((y - Util.IBL)/Util.SBL);
            } else {
                int totalInSequence = 0;
                int totalOffSet = 0;
                for (int i = 0; i < Util.SBL; ++i) {
                    if (Util.sequenceBuild[i] == a) {
                        ++totalInSequence;
                    }
                }
                int z = y %Util.SBL;
                for (int i = x; true ;++i){
                    int realI = i%Util.SBL;
                    if (realI == z) break;
                    if (Util.sequenceBuild[realI] == a) ++ans;
                    ++totalOffSet;
                }

                ans += ((y - x - totalOffSet)/Util.SBL)*totalInSequence;
            }
            if (a < 4) return ans*Util.ProductionUnits[a].bulletCost;
            else return ans*50;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    static void Initialize(){
        try {
            int a = rc.readBroadcast(Util.INITIALIZED);
            if (a == 0){
                rc.broadcast(Util.INITIALIZED, 1);
                for (int i = 0; i < Util.unitChannels.length; ++i){
                    rc.broadcast(Util.unitChannels[i], Util.initialPositions[i]);
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

    }



}
