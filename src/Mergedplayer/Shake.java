package Mergedplayer;

import battlecode.common.RobotController;
import battlecode.common.Team;
import battlecode.common.TreeInfo;

/**
 * Created by Pau on 24/01/2017.
 */
public class Shake {

    static void shake(RobotController rc){

        float maxBullets = 0;
        int id = -1;
        TreeInfo[] Ti = rc.senseNearbyTrees(3, Team.NEUTRAL);

        for (TreeInfo ti : Ti){
            if (ti.getContainedBullets() > maxBullets){
                if (!rc.canShake(ti.getID())) continue;
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
}
