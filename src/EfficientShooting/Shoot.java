package EfficientShooting;

import battlecode.common.*;

/**
 * Created by Ivan on 1/21/2017.
 */


public class Shoot {

        static boolean shouldShoot;
        static boolean cramped;

        static RobotInfo[] sortedEnemies;


        static void setShooting(boolean b){
            shouldShoot = b;
        }
        static void setCramped(boolean b){
            cramped = b;
        }

        static int[] expectedHits;

        static boolean tryShoot(RobotController rc, int tries){

            float maxUtilSingle = 0;
            float maxUtilTriad = 0;
            float maxUtilPentad = 0;
            Direction dirSingle = null;
            Direction dirTriad = null;
            Direction dirPentad = null;

            if (sortedEnemies.length > 1) shouldShoot = true;

            MapLocation pos = rc.getLocation();

            for (int i = 0; i < tries && i < Greedy.sortedEnemies.length; ++i) {

                RobotInfo ri = Greedy.sortedEnemies[i];
                RobotType r = ri.getType();
                MapLocation m = ri.getLocation();
                float R = r.bodyRadius;
                float d = m.distanceTo(pos);

                //R += r.strideRadius;
                R = Math.min(d, R);

                Direction dir = pos.directionTo(m);

                float a = (float) Math.asin(R / d);

                float l = (float) Math.sqrt(R * R * (1.0f + (float) Math.cos(2 * a)));
                float rad = l / (2.0f * (float) Math.sin(2 * a));

                RobotInfo[] allies = rc.senseNearbyRobots(pos.add(dir, rad), rad, rc.getTeam());
                TreeInfo[] trees = rc.senseNearbyTrees(pos.add(dir, rad), rad, null);

                Direction dirRightExact = dir.rotateRightRads(a);
                Direction dirLeftExact = dir.rotateLeftRads(a);

                Direction dirRight = dir.rotateRightDegrees(31);
                Direction dirLeft = dir.rotateLeftDegrees(31);

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
                    else if (dirRight.radiansBetween(dirLeft2) >= 0 && dirLeft.radiansBetween(dirRight2) <= 0)
                        dirRight = dirLeft2;
                    if (dirRight2.radiansBetween(dirRight) >= 0 && dirRight2.radiansBetween(dirLeft) >= 0) {
                        if (dirLeft2.radiansBetween(dirRight) <= 0 && dirLeft2.radiansBetween(dirLeft) <= 0) {
                            dirRight = dirLeft2;
                            dirLeft = dirRight2;
                        }
                    }
                }

                Direction dirRightA = dirRight;
                Direction dirLeftA = dirLeft;

                for (TreeInfo tree : trees) {
                    if (Clock.getBytecodesLeft() < 400) break;
                    if (tree.getTeam() == rc.getTeam().opponent()) continue;
                    if (dirLeft.radiansBetween(dirRight) > 0) continue;
                    MapLocation m2 = tree.getLocation();
                    Direction dir2 = pos.directionTo(m2);

                    float d2 = pos.distanceTo(m2);
                    float ang = (float) Math.asin(tree.getRadius() / d2);

                    Direction dirRight2 = dir2.rotateRightRads(ang);
                    Direction dirLeft2 = dir2.rotateLeftRads(ang);

                    if (dirRight.radiansBetween(dirRight2) >= 0 && dirLeft.radiansBetween(dirRight2) <= 0)
                        dirLeft = dirRight2;
                    else if (dirRight.radiansBetween(dirLeft2) >= 0 && dirLeft.radiansBetween(dirRight2) <= 0)
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

                if (dirRightExact.radiansBetween(dirLeftExact) > Constants.eps) {

                    float realAngle = dirRightExact.radiansBetween(dirLeftExact) / 2;

                    Direction shootingDir = dirRightExact.rotateLeftRads(realAngle);
                    float oberture = Math.min(shootingDir.radiansBetween(dirLeft), dirRight.radiansBetween(shootingDir));
                    float maxOberture = Math.min(shootingDir.radiansBetween(dirLeftA), dirRightA.radiansBetween(shootingDir));

                    computeExpectedHits(oberture, maxOberture, d, r, rc);

                    try {

                        if (expectedHits[2] > expectedHits[1] && expectedHits[2] > expectedHits[0]) {
                            if (rc.canFirePentadShot()) {
                                rc.firePentadShot(shootingDir);
                                return true;
                            }
                        }


                        if (expectedHits[1] > expectedHits[0]) {
                            if (rc.canFireTriadShot()) {
                                rc.fireTriadShot(shootingDir);
                                return true;
                            }
                        }

                        if (expectedHits[0] > 0) {
                            if (rc.canFireSingleShot()) {
                                rc.fireSingleShot(shootingDir);
                                return true;
                            }
                        }

                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                        e.printStackTrace();
                    }
                }
            }

            return false;
        }


        static void computeExpectedHits(float ob, float maxob, float d, RobotType r, RobotController rc){
        }

}
