package Seedingplayer;

import battlecode.common.*;

/**
 * Created by Ivan on 1/21/2017.
 */
public class Shoot {


        static boolean tryShoot(RobotController rc, int tries){

            float maxUtilSingle = 0;
            float maxUtilTriad = 0;
            float maxUtilPentad = 0;
            Direction dirSingle = null;
            Direction dirTriad = null;
            Direction dirPentad = null;

            MapLocation pos = rc.getLocation();
            RobotInfo[]rArray = new RobotInfo[tries];
            RobotInfo[] enemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
            int cont = 0;
            for (RobotInfo ri : enemies){
                RobotType a = ri.getType();
                MapLocation m = ri.getLocation();
                float d = pos.distanceTo(m);
                //if (a == RobotType.SCOUT && d > 5) continue;
                //if (a == RobotType.SOLDIER && d > 7) continue;
                Direction dir = pos.directionTo(m);
                boolean addIt = true;
                for (int i = 0; i < cont && !addIt; ++i){
                    MapLocation m2 = rArray[i].getLocation();
                    if (dir.radiansBetween(pos.directionTo(m2)) < Constants.minAngleShoot){
                        addIt = false;
                    }
                }
                if (addIt){
                    rArray[cont] = ri;
                    ++cont;
                    if (cont >= tries) break;
                }
            }

            for (int i = 0; i < cont; ++i){
                RobotInfo ri = rArray[i];
                RobotType r = ri.getType();
                MapLocation m = ri.getLocation();
                float R = r.bodyRadius;

                if (ri.getType() == RobotType.SOLDIER || ri.getType() == RobotType.SCOUT) R += r.strideRadius;


                Direction dir = pos.directionTo(m);

                float d = m.distanceTo(pos);

                float a = (float)Math.asin(R/d);

                float l = (float)Math.sqrt(R*R*(1.0f + (float)Math.cos(2*a)));
                float rad = l/(2.0f*(float)Math.sin(2*a));

                RobotInfo[] allies = rc.senseNearbyRobots(pos.add(dir, rad), rad, rc.getTeam());

                TreeInfo[] trees = rc.senseNearbyTrees(pos.add(dir, rad), rad, null);

                Direction dirRight = dir.rotateRightRads(a);
                Direction dirLeft = dir.rotateLeftRads(a);

                for (RobotInfo ally : allies){

                    if (Clock.getBytecodesLeft() < 400)  break;
                    if (ally.getID() == rc.getID()) continue;
                    if (dirLeft.radiansBetween(dirRight) > 0) continue;
                    MapLocation m2 = ally.getLocation();
                    Direction dir2 = pos.directionTo(m2);

                    float d2 = pos.distanceTo(m2);
                    float ang = (float)Math.asin(ally.getType().bodyRadius/d2);

                    Direction dirRight2 = dir2.rotateRightRads(ang);
                    Direction dirLeft2 = dir2.rotateLeftRads(ang);

                    if (dirRight.radiansBetween(dirRight2) >= 0 && dirLeft.radiansBetween(dirRight2) <= 0) dirLeft = dirRight2;
                    if (dirRight.radiansBetween(dirLeft2) >= 0 && dirLeft.radiansBetween(dirRight2) <= 0) dirRight = dirLeft2;
                    if (dirRight2.radiansBetween(dirRight) >= 0 && dirRight2.radiansBetween(dirLeft) >= 0){
                        if (dirLeft2.radiansBetween(dirRight)<= 0 && dirLeft2.radiansBetween(dirLeft) <= 0){
                            dirRight = dirLeft2;
                            dirLeft = dirRight2;
                        }
                    }
                }

                Direction dirRightA = dirRight;
                Direction dirLeftA = dirLeft;

                for (TreeInfo tree : trees){
                    if (Clock.getBytecodesLeft() < 400)  break;
                    if (tree.getID() == rc.getID()) continue;
                    if (dirLeft.radiansBetween(dirRight) > 0) continue;
                    MapLocation m2 = tree.getLocation();
                    Direction dir2 = pos.directionTo(m2);

                    float d2 = pos.distanceTo(m2);
                    float ang = (float)Math.asin(tree.getRadius()/d2);

                    Direction dirRight2 = dir2.rotateRightRads(ang);
                    Direction dirLeft2 = dir2.rotateLeftRads(ang);

                    if (dirRight.radiansBetween(dirRight2) >= 0 && dirLeft.radiansBetween(dirRight2) <= 0) dirLeft = dirRight2;
                    if (dirRight.radiansBetween(dirLeft2) >= 0 && dirLeft.radiansBetween(dirRight2) <= 0) dirRight = dirLeft2;
                    if (dirRight2.radiansBetween(dirRight) >= 0 && dirRight2.radiansBetween(dirLeft) >= 0){
                        if (dirLeft2.radiansBetween(dirRight)<= 0 && dirLeft2.radiansBetween(dirLeft) <= 0){
                            dirRight = dirLeft2;
                            dirLeft = dirRight2;
                        }
                    }
                }

                if (Clock.getBytecodesLeft() < 400)  break;

                if (dirRight.radiansBetween(dirLeft) > Constants.eps){

                    float realAngle = dirRight.radiansBetween(dirLeft)/2;

                    System.out.println("Shooting Angle: " + realAngle);
                    float multiplier = 1;

                    if (r == RobotType.SCOUT) multiplier = 0.2f;
                    else if (r == RobotType.LUMBERJACK) multiplier = 1.2f;


                    float x;
                    if (r == RobotType.ARCHON) x = 10;
                    else x = 2.0f*((float)r.bulletCost)/r.maxHealth;

                    System.out.println("x = " + x);

                    float ut = 0;
                    float utTriad = 0;
                    float utPentad = 0;

                    boolean shootPentad = false;

                    ut = x*multiplier - 1;
                    if (realAngle > Constants.triadAngle) utTriad = multiplier*x*3.0f - 4;
                    if (realAngle > Constants.pentadAngle && dirRightA.radiansBetween(dirLeftA) > Constants.pentadAngle2 ) utPentad = multiplier*x*3.0f - 6;
                    if (realAngle > Constants.pentadAngle2){
                        utPentad = multiplier*x*5.0f - 6;
                        shootPentad = true;
                    }

                    if (ut > maxUtilSingle){
                        dirSingle = dirRight.rotateLeftRads(realAngle);
                        maxUtilSingle = ut;
                    }

                    if (utTriad > maxUtilTriad){
                        dirTriad = dirRight.rotateLeftRads(realAngle);
                        maxUtilTriad = utTriad;
                    }

                    if (utPentad > maxUtilPentad){
                        if (shootPentad) dirPentad = dirRight.rotateLeftRads(realAngle);
                        else dirPentad = dirRightA.rotateLeftRads(dirRightA.radiansBetween(dirLeftA)/2);
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
                        return true;
                    }
                }
                if (maxUtilSingle > 0 && rc.canFireSingleShot()) {
                    //if (Constants.DEBUG == 1) rc.setIndicatorDot(rc.getLocation(), 255,0, 0);
                    //if (Constants.DEBUG == 1) rc.setIndicatorDot(rc.getLocation().add(dirSingle), 120,120, 0);
                    if (Constants.DEBUG == 1) rc.setIndicatorLine(rc.getLocation(), rc.getLocation().add(dirSingle),255,255,0);
                    rc.fireSingleShot(dirSingle);
                    return true;
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
            return false;
        }

}
