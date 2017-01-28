package Dynamicplayer;

import MergedplayerProvesDiana.Constants;
import battlecode.common.*;

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
            if (rc.getTeamBullets() > Constants.BULLET_LIMIT) rc.donate(rc.getTeamBullets() - Constants.BULLET_LIMIT-20);
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
    static final int NEEDSOLDIERTANK = 0;
    static final int NEEDLUMBERJACK = 1;

    //funcio per demanar unitats
    static void askForUnits(RobotController rc)
    {
    	
    	MapLocation me = rc.getLocation();
    	RobotInfo[] Ri = rc.senseNearbyRobots();
      	float soldiertank = 0.0f;
      	boolean found = false;
      	for(RobotInfo ri : Ri)
      	{
      		if(ri.getTeam().equals(rc.getTeam()))
      		{
      			soldiertank -= dangerScore(Constants.getIndex(ri.getType()));
      		}
      		if(ri.getTeam().equals(rc.getTeam().opponent()))
      		{
      			found = true;
      			soldiertank += dangerScore(Constants.getIndex(ri.getType()));
      		}
      	}
      	soldiertank -= dangerScore(Constants.getIndex(rc.getType()));
      	if(soldiertank >= 0.0f && found)
      	{
      		int x = Math.round(me.x);
              int y = Math.round(me.y);
          	Communication.sendMessage(rc, Communication.NEEDTROOPCHANNEL, x, y, NEEDSOLDIERTANK);
      	}
	}

    static float dangerScore(int rt)
    {
      	if(rt == 4) return 1.0f; //scouts
      	//if(rt == 0) return 2; //granjers?
      	if(rt == 2) return 5.0f; //soldier mega important
      	if(rt == 3) return 10.0f; // tank hiper mega important
      	return 0.0f; //archons (i granjers) 0
    }
}
