package Seedingplayer;

import battlecode.common.*;

/**
 * Created by Pau on 24/01/2017.
 */
public class Shake {

    static void shake(RobotController rc){

        float maxBullets = 0;
        int id = -1;
        TreeInfo[] Ti = rc.senseNearbyTrees(3, Team.NEUTRAL);

        System.out.println("Entra shake " + Ti.length);
        for (TreeInfo ti : Ti){
            System.out.println("bullets trobades: " + ti.getContainedBullets());
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
