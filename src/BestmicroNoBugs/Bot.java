package BestmicroNoBugs;

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
}
