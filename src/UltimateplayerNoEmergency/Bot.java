package UltimateplayerNoEmergency;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.Team;
import battlecode.common.TreeInfo;

/**
 * Created by Pau on 27/01/2017.
 */
public class Bot {
//aqui fiquem tot el que facin tots els robots


    static void shake(RobotController rc){
        float maxBullets = 0;
        int id = -1;
        TreeInfo[] Ti = rc.senseNearbyTrees(rc.getType().bodyRadius + 1.0f, Team.NEUTRAL);

        for (TreeInfo ti : Ti){
            if (ti.getContainedBullets() > maxBullets){
                if (!rc.canShake(ti.getID())) break;
                maxBullets = ti.getContainedBullets();
                id = ti.getID();
            }
        }
        try {
            if (maxBullets > 0) rc.shake(id);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    static void donate(RobotController rc) {
        try {
            float cost = rc.getVictoryPointCost();
            if(rc.getTeamVictoryPoints() + rc.getTeamBullets()/cost >= Constants.MAXVICTORYPONTS) rc.donate(rc.getTeamBullets());
            float extraBullets;
            if (rc.getRoundNum() > Constants.LAST_ROUND_BUILD) extraBullets = Math.max(0, rc.getTeamBullets() - 20);
            else extraBullets = rc.getTeamBullets()- Constants.BULLET_LIMIT;
            int extraVP = (int)extraBullets/(int)cost;
            float toDonate = extraVP * cost;
            if (toDonate > 0) rc.donate(toDonate);
        } catch (GameActionException e) {
            e.printStackTrace();
        }
    }
    
    //ASK FOR UNITS
    static final float LUMBERJACKSCORE = 5.0f;

    static float dangerScore(int rt)
    {
      	if(rt == 4) return 1.0f; //scouts
      	//if(rt == 0) return 2; //granjers?
      	if(rt == 2) return 5.0f; //soldier mega important
      	if(rt == 3) return 10.0f; // tank hiper mega important
      	return 0.0f; //archons (i granjers) 0
    }

    static float unitTreeScore(int rt)
    {
      	if(rt == 0) return 1.0f;
      	if(rt == 2) return 2.5f;
      	if(rt == 3) return 5.0f;
      	return 0.0f; //sudando d'scouts i archons
	}
}
