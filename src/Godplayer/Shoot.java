package Godplayer;

import battlecode.common.*;

/**
 * Created by Ivan on 1/21/2017.
 */
public class Shoot {

    static float t = 0.75f;
    static int WASTEBULLETTURN = 200;


    static boolean tryShoot(RobotController rc, int tries){

        float maxUtilSingle = 0;
        float maxUtilTriad = 0;
        float maxUtilPentad = 0;
        Direction dirSingle = null;
        Direction dirTriad = null;
        Direction dirPentad = null;

        MapLocation pos = rc.getLocation();

        for (int i = 0; i < tries && i < Greedy.sortedEnemies.length; ++i){
            RobotInfo ri = Greedy.sortedEnemies[i];
            RobotType r = ri.getType();
            MapLocation m = ri.getLocation();
            float R = r.bodyRadius;
            float d = m.distanceTo(pos);

            Direction dir = pos.directionTo(m);

            float a = (float) Math.asin(R / d);

            float l = (float) Math.sqrt(R * R * (1.0f + (float) Math.cos(2 * a)));
            float rad = l / (2.0f * (float) Math.sin(2 * a));

            RobotInfo[] allies = rc.senseNearbyRobots(pos.add(dir, rad), rad, rc.getTeam());
            TreeInfo[] trees = rc.senseNearbyTrees(pos.add(dir, rad), rad, null);

            Direction dirRightExact = dir.rotateRightRads(a);
            Direction dirLeftExact = dir.rotateLeftRads(a);

            float R2 = R;
            if (rc.getRoundNum() > WASTEBULLETTURN) R2 += r.strideRadius;
            R2 = Math.min(d - rc.getType().bodyRadius, R2);
            a = (float) Math.asin(R2 / d);
            //if (r == RobotType.SOLDIER || r == RobotType.TANK) a = 31;

            Direction dirRight = dir.rotateRightRads(a);
            Direction dirLeft = dir.rotateLeftRads(a);

            Direction dirRightA = dir.rotateRightDegrees(90);
            Direction dirLeftA = dir.rotateLeftDegrees(90);

            for (RobotInfo ally : allies) {

                if (Clock.getBytecodesLeft() < 400) break;
                if (ally.getID() == rc.getID()) continue;
                if (dirLeft.radiansBetween(dirRight) > 0) continue;
                MapLocation m2 = ally.getLocation();
                Direction dir2 = pos.directionTo(m2);

                float d2 = pos.distanceTo(m2);
                float ang = (float) Math.asin(ally.getType().bodyRadius / d2);

                Direction dirRight2 = dir2.rotateRightRads(ang);
                Direction dirLeft2 = dir2.rotateLeftRads(ang);

                if (dirRight.radiansBetween(dirRight2) >= 0 && dirLeft.radiansBetween(dirRight2) <= 0)
                    dirLeft = dirRight2;
                else if (dirRight.radiansBetween(dirLeft2) >= 0 && dirLeft.radiansBetween(dirLeft2) <= 0)
                    dirRight = dirLeft2;
                if (dirRight2.radiansBetween(dirRight) >= 0 && dirRight2.radiansBetween(dirLeft) >= 0) {
                    if (dirLeft2.radiansBetween(dirRight) <= 0 && dirLeft2.radiansBetween(dirLeft) <= 0) {
                        dirRight = dirLeft2;
                        dirLeft = dirRight2;
                    }
                }

                if (dirRightA.radiansBetween(dirRight2) >= 0 && dirLeftA.radiansBetween(dirRight2) <= 0)
                    dirLeftA = dirRight2;
                else if (dirRightA.radiansBetween(dirLeft2) >= 0 && dirLeftA.radiansBetween(dirLeft2) <= 0)
                    dirRightA = dirLeft2;
                if (dirRight2.radiansBetween(dirRightA) >= 0 && dirRight2.radiansBetween(dirLeftA) >= 0) {
                    if (dirLeft2.radiansBetween(dirRightA) <= 0 && dirLeft2.radiansBetween(dirLeftA) <= 0) {
                        dirRightA = dirLeft2;
                        dirLeftA = dirRight2;
                    }
                }
            }

            for (TreeInfo tree : trees) {
                if (Clock.getBytecodesLeft() < 400) break;
                if (r == RobotType.GARDENER && tree.getTeam() == rc.getTeam().opponent() && rc.getLocation().distanceTo(m) <= Constants.SHOOTGARDENER) continue;
                if (dirLeft.radiansBetween(dirRight) > 0) continue;
                MapLocation m2 = tree.getLocation();
                Direction dir2 = pos.directionTo(m2);

                float d2 = pos.distanceTo(m2);
                float ang = (float) Math.asin(tree.getRadius() / d2);

                Direction dirRight2 = dir2.rotateRightRads(ang);
                Direction dirLeft2 = dir2.rotateLeftRads(ang);

                if (dirRight.radiansBetween(dirRight2) >= 0 && dirLeft.radiansBetween(dirRight2) <= 0)
                    dirLeft = dirRight2;
                else if (dirRight.radiansBetween(dirLeft2) >= 0 && dirLeft.radiansBetween(dirLeft2) <= 0)
                    dirRight = dirLeft2;
                if (dirRight2.radiansBetween(dirRight) >= 0 && dirRight2.radiansBetween(dirLeft) >= 0) {
                    if (dirLeft2.radiansBetween(dirRight) <= 0 && dirLeft2.radiansBetween(dirLeft) <= 0) {
                        dirRight = dirLeft2;
                        dirLeft = dirRight2;
                    }
                }
            }

            if (dirRightExact.radiansBetween(dirRight) >= 0) dirRightExact = dirRight;
            if (dirLeftExact.radiansBetween(dirLeft) <= 0) dirLeftExact = dirLeft;

            if (Clock.getBytecodesLeft() < 400) break;

            if (dirRightExact.radiansBetween(dirLeftExact) > Constants.eps){

                float realAngle = dirRightExact.radiansBetween(dirLeftExact) / 2;

                Direction shootingDir = dirRightExact.rotateLeftRads(realAngle);
                float oberture = Math.min(shootingDir.radiansBetween(dirLeft), dirRight.radiansBetween(shootingDir));
                shootingDir = shootingDir.rotateLeftRads(t*realAngle* Greedy.factor);
                float maxOberture = Math.min(shootingDir.radiansBetween(dirLeftA), dirRightA.radiansBetween(shootingDir));


                System.out.println("Shooting Angle: " + realAngle);
                System.out.println("oberture: " + oberture);
                System.out.println("max oberture: " + maxOberture);
                float multiplier = 1;

                if (ri.getType() == RobotType.SOLDIER || ri.getType() == RobotType.TANK) multiplier = 2;


                float x;
                if (r == RobotType.ARCHON) x = 2.5f;
                else x = 2.0f*((float)r.bulletCost)/r.maxHealth;

                System.out.println("x = " + x);

                float ut = 0;
                float utTriad = 0;
                float utPentad = 0;

                boolean shootPentad = false;

                ut = x*multiplier - 1;
                if (oberture > Constants.triadAngle) utTriad = multiplier*x*3.0f - 4;
                if (oberture > Constants.pentadAngle && maxOberture > Constants.pentadAngle2) utPentad = multiplier*x*3.0f - 6;
                if (oberture > Constants.pentadAngle2){
                    utPentad = multiplier*x*5.0f - 6;
                    shootPentad = true;
                }

                if (ut > maxUtilSingle){
                    dirSingle = shootingDir;
                    maxUtilSingle = ut;
                }

                if (utTriad > maxUtilTriad){
                    dirTriad = shootingDir;
                    maxUtilTriad = utTriad;
                }

                if (utPentad > maxUtilPentad){
                    dirPentad = shootingDir;
                    maxUtilPentad = utPentad;
                }
            }

        }

        System.out.println(maxUtilSingle + " " + maxUtilTriad + " " + maxUtilPentad);

        try {
            if (maxUtilPentad > 0 && rc.canFirePentadShot()) {
                if (maxUtilPentad > maxUtilTriad) {
                    if (maxUtilPentad > maxUtilSingle) {
                        //if (Constants.DEBUG == 1) rc.setIndicatorDot(rc.getLocation(), 255,0, 0);
                        //if (Constants.DEBUG == 1) rc.setIndicatorDot(rc.getLocation().add(dirPentad), 0,255, 0);
                        if (Constants.DEBUG == 1) rc.setIndicatorLine(rc.getLocation(), rc.getLocation().add(dirPentad.rotateRightRads((float) Math.PI/6)),255,255,0);
                        if (Constants.DEBUG == 1) rc.setIndicatorLine(rc.getLocation(), rc.getLocation().add(dirPentad.rotateRightRads((float) Math.PI/12)),255,255,0);
                        if (Constants.DEBUG == 1) rc.setIndicatorLine(rc.getLocation(), rc.getLocation().add(dirPentad),255,255,0);
                        if (Constants.DEBUG == 1) rc.setIndicatorLine(rc.getLocation(), rc.getLocation().add(dirPentad.rotateLeftRads((float) Math.PI/12)),255,255,0);
                        if (Constants.DEBUG == 1) rc.setIndicatorLine(rc.getLocation(), rc.getLocation().add(dirPentad.rotateLeftRads((float) Math.PI/6)),255,255,0);
                        rc.firePentadShot(dirPentad);
                        Greedy.flipFactor();
                        return true;
                    }
                }
            }
            if (maxUtilTriad > 0 && rc.canFireTriadShot()) {
                if (maxUtilTriad > maxUtilSingle) {
                    //if (Constants.DEBUG == 1) rc.setIndicatorDot(rc.getLocation(), 255,0, 0);
                    //if (Constants.DEBUG == 1) rc.setIndicatorDot(rc.getLocation().add(dirTriad), 0,0, 255);
                    if (Constants.DEBUG == 1) rc.setIndicatorLine(rc.getLocation(), rc.getLocation().add(dirTriad.rotateRightRads((float) Math.PI/9)),255,128,0);
                    if (Constants.DEBUG == 1) rc.setIndicatorLine(rc.getLocation(), rc.getLocation().add(dirTriad),255,128,0);
                    if (Constants.DEBUG == 1) rc.setIndicatorLine(rc.getLocation(), rc.getLocation().add(dirTriad.rotateLeftRads((float) Math.PI/9)),255,128,0);
                    rc.fireTriadShot(dirTriad);
                    Greedy.flipFactor();
                    return true;
                }
            }
            if (maxUtilSingle > 0 && rc.canFireSingleShot()) {
                //if (Constants.DEBUG == 1) rc.setIndicatorDot(rc.getLocation(), 255,0, 0);
                //if (Constants.DEBUG == 1) rc.setIndicatorDot(rc.getLocation().add(dirSingle), 120,120, 0);
                if (Constants.DEBUG == 1) rc.setIndicatorLine(rc.getLocation(), rc.getLocation().add(dirSingle),255,255,0);
                rc.fireSingleShot(dirSingle);
                Greedy.flipFactor();
                return true;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

}
