package Ultimatemicro;

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
            else return;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    static void donate(RobotController rc) {
        try {
            if (rc.getTeamBullets() > Constants.BULLET_LIMIT) rc.donate(Math.max(0,rc.getTeamBullets() - Constants.BULLET_LIMIT-20));
            if (rc.getRoundNum() > Constants.LAST_ROUND_BUILD) {
                float donation = Math.max(0, rc.getTeamBullets() - 20);
                if (donation > 20) ;
                rc.donate(donation);
            }
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
