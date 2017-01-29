package Vells.EfficientShooting;

import battlecode.common.*;

/**
 * Created by Ivan on 1/21/2017.
 */


public class Shoot {

        static boolean shouldShoot;
        static boolean cramped;


        static void setShooting(boolean b){
            shouldShoot = b;
        }
        static void setCramped(boolean b){
            cramped = b;
        }

        static int[] expectedHits;

        static boolean tryShoot(RobotController rc, int tries){

            if (Greedy.sortedEnemies.length > 1) shouldShoot = true;

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
                    if (r == RobotType.GARDENER && tree.getTeam() == rc.getTeam().opponent()) continue;
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

                    if (Constants.triadAngle >= oberture - Constants.eps) {
                        Communication.sendMessage(rc, Communication.SHOOTCHANNEL, Math.round(ri.getLocation().x), Math.round(ri.getLocation().y), Constants.getIndex(r));
                        if (rc.getType() == RobotType.SOLDIER){
                            Soldier.initialMessageShoot = (Soldier.initialMessageShoot+1)%Communication.CYCLIC_CHANNEL_LENGTH;
                        } else if (rc.getType() == RobotType.TANK){
                            Tank.initialMessageShoot = (Tank.initialMessageShoot+1)%Communication.CYCLIC_CHANNEL_LENGTH;
                        }
                    }

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


        static void computeExpectedHits(float ob, float maxob, float d, RobotType r, RobotController rc) {
            expectedHits = new int[3];
            //SINGLESHOT

            if (rc.getType() == RobotType.SOLDIER) {
                if (r == RobotType.SOLDIER) {
                    if (d < 3.24f) expectedHits[0] = 1;
                }
                if (r == RobotType.TANK) {
                    if (d < 9.03999f) expectedHits[0] = 1;
                }
                if (r == RobotType.GARDENER) {
                    if (d < 5.04f) expectedHits[0] = 1;
                }
                if (r == RobotType.LUMBERJACK) {
                    if (d < 3.29f) expectedHits[0] = 1;
                }
                if (r == RobotType.SCOUT) {
                    if (d < 2.04f) expectedHits[0] = 1;
                }
                if (r == RobotType.ARCHON) {
                    if (d < 9.03999f) expectedHits[0] = 1;
                }
            }
            if (rc.getType() == RobotType.TANK) {
                if (r == RobotType.SOLDIER) {
                    if (d < 6.24f) expectedHits[0] = 1;
                }
                if (r == RobotType.TANK) {
                    if (d < 22.04f) expectedHits[0] = 1;
                }
                if (r == RobotType.GARDENER) {
                    if (d < 10.04f) expectedHits[0] = 1;
                }
                if (r == RobotType.LUMBERJACK) {
                    if (d < 6.29f) expectedHits[0] = 1;
                }
                if (r == RobotType.SCOUT) {
                    if (d < 3.04f) expectedHits[0] = 1;
                }
                if (r == RobotType.ARCHON) {
                    if (d < 22.04f) expectedHits[0] = 1;
                }
            }

            //TRIADSHOT

            if (Constants.triadAngle >= maxob - Constants.eps) {
                expectedHits[0] = 1;
                return;
            }

            if (shouldShoot || cramped){
                expectedHits[1] = 1;
                expectedHits[2] = 1;
            }

            if (rc.getType() == RobotType.SOLDIER) {
                if (r == RobotType.SOLDIER) {
                    if (d < 4.46228f) expectedHits[1] = 1;
                    if (d < 2.1638f) expectedHits[1] = 2;
                    if (d < 2.04f) expectedHits[1] = 3;
                }
                if (r == RobotType.TANK) {
                    if (d < 9.88256f) expectedHits[1] = 1;
                    if (d < 5.05637f) expectedHits[1] = 2;
                    if (d < 3.95324f) expectedHits[1] = 3;
                }
                if (r == RobotType.GARDENER) {
                    if (d < 5.04f) expectedHits[1] = 1;
                    if (d < 2.4638f) expectedHits[1] = 2;
                    if (d < 2.04f) expectedHits[1] = 3;
                }
                if (r == RobotType.LUMBERJACK) {
                    if (d < 4.59325f) expectedHits[1] = 1;
                    if (d < 2.2138f) expectedHits[1] = 2;
                    if (d < 2.04f) expectedHits[1] = 3;
                }
                if (r == RobotType.SCOUT) {
                    if (d < 3.41999f) expectedHits[1] = 1;
                    if (d < 2.04f) expectedHits[1] = 2;
                    if (d < 2.04f) expectedHits[1] = 3;
                }
                if (r == RobotType.ARCHON) {
                    if (d < 9.88256f) expectedHits[1] = 1;
                    if (d < 5.05637f) expectedHits[1] = 2;
                    if (d < 3.95324f) expectedHits[1] = 3;
                }
            }
            if (rc.getType() == RobotType.TANK) {
                if (r == RobotType.SOLDIER) {
                    if (d < 6.24f) expectedHits[1] = 1;
                    if (d < 3.04f) expectedHits[1] = 2;
                    if (d < 3.04f) expectedHits[1] = 3;
                }
                if (r == RobotType.TANK) {
                    if (d < 22.04f) expectedHits[1] = 1;
                    if (d < 5.3876f) expectedHits[1] = 2;
                    if (d < 4.42571f) expectedHits[1] = 3;
                }
                if (r == RobotType.GARDENER) {
                    if (d < 10.04f) expectedHits[1] = 1;
                    if (d < 3.04f) expectedHits[1] = 2;
                    if (d < 3.04f) expectedHits[1] = 3;
                }
                if (r == RobotType.LUMBERJACK) {
                    if (d < 6.29f) expectedHits[1] = 1;
                    if (d < 3.04f) expectedHits[1] = 2;
                    if (d < 3.04f) expectedHits[1] = 3;
                }
                if (r == RobotType.SCOUT) {
                    if (d < 4.96128f) expectedHits[1] = 1;
                    if (d < 3.04f) expectedHits[1] = 2;
                    if (d < 3.04f) expectedHits[1] = 3;
                }
                if (r == RobotType.ARCHON) {
                    if (d < 22.04f) expectedHits[1] = 1;
                    if (d < 5.3876f) expectedHits[1] = 2;
                    if (d < 4.42571f) expectedHits[1] = 3;
                }
            }


            //PENTADSHOT1

            if (Constants.pentadAngle >= ob - Constants.eps) return;

            if (Constants.pentadAngle2 >= maxob - Constants.eps)return;

            if (rc.getType() == RobotType.SOLDIER){
                if (r == RobotType.SOLDIER){
                    if (d < 5.45401f) expectedHits[2] = 1;
                    if (d < 3.1037f) expectedHits[2] = 2;
                    if (d < 2.04f) expectedHits[2] = 3;
                    if (Constants.pentadAngle2 >= ob - Constants.eps &&d < 2.04f) expectedHits[2] = 4;
                    if (Constants.pentadAngle2 >= ob - Constants.eps &&d < 2.04f) expectedHits[2] = 5;
                }
                if (r == RobotType.TANK){
                    if (d < 12.9954f) expectedHits[2] = 1;
                    if (d < 6.2674f) expectedHits[2] = 2;
                    if (d < 4.49034f) expectedHits[2] = 3;
                    if (Constants.pentadAngle2 >= ob - Constants.eps &&d < 3.5041f) expectedHits[2] = 4;
                    if (Constants.pentadAngle2 >= ob - Constants.eps &&d < 3.04f) expectedHits[2] = 5;
                }
                if (r == RobotType.GARDENER){
                    if (d < 6.51772f) expectedHits[2] = 1;
                    if (d < 3.14583f) expectedHits[2] = 2;
                    if (d < 2.26517f) expectedHits[2] = 3;
                    if (Constants.pentadAngle2 >= ob - Constants.eps &&d < 2.04f) expectedHits[2] = 4;
                    if (Constants.pentadAngle2 >= ob - Constants.eps &&d < 2.04f) expectedHits[2] = 5;
                }
                if (r == RobotType.LUMBERJACK){
                    if (d < 5.62019f) expectedHits[2] = 1;
                    if (d < 3.14583f) expectedHits[2] = 2;
                    if (d < 2.04f) expectedHits[2] = 3;
                    if (Constants.pentadAngle2 >= ob - Constants.eps &&d < 2.04f) expectedHits[2] = 4;
                    if (Constants.pentadAngle2 >= ob - Constants.eps &&d < 2.04f) expectedHits[2] = 5;
                }
                if (r == RobotType.SCOUT){
                    if (d < 5.04f) expectedHits[2] = 1;
                    if (d < 2.6537f) expectedHits[2] = 2;
                    if (d < 2.04f) expectedHits[2] = 3;
                    if (Constants.pentadAngle2 >= ob - Constants.eps &&d < 2.04f) expectedHits[2] = 4;
                    if (Constants.pentadAngle2 >= ob - Constants.eps &&d < 2.04f) expectedHits[2] = 5;
                }
                if (r == RobotType.ARCHON){
                    if (d < 12.9954f) expectedHits[2] = 1;
                    if (d < 6.2674f) expectedHits[2] = 2;
                    if (d < 4.49034f) expectedHits[2] = 3;
                    if (Constants.pentadAngle2 >= ob - Constants.eps &&d < 3.5041f) expectedHits[2] = 4;
                    if (Constants.pentadAngle2 >= ob - Constants.eps &&d < 3.04f) expectedHits[2] = 5;
                }
            }

            if (rc.getType() == RobotType.TANK){
                if (r == RobotType.SOLDIER){
                    if (d < 6.38675f) expectedHits[2] = 1;
                    if (d < 3.1037f) expectedHits[2] = 2;
                    if (d < 3.04f) expectedHits[2] = 3;
                    if (Constants.pentadAngle2 >= ob - Constants.eps &&d < 3.04f) expectedHits[2] = 4;
                    if (Constants.pentadAngle2 >= ob - Constants.eps &&d < 3.04f) expectedHits[2] = 5;
                }
                if (r == RobotType.TANK){
                    if (d < 22.04f) expectedHits[2] = 1;
                    if (d < 6.7674f) expectedHits[2] = 2;
                    if (d < 5.83556f) expectedHits[2] = 3;
                    if (Constants.pentadAngle2 >= ob - Constants.eps &&d < 4.04f) expectedHits[2] = 4;
                    if (Constants.pentadAngle2 >= ob - Constants.eps &&d < 4.04f) expectedHits[2] = 5;
                }
                if (r == RobotType.GARDENER){
                    if (d < 10.04f) expectedHits[2] = 1;
                    if (d < 3.4037f) expectedHits[2] = 2;
                    if (d < 3.04f) expectedHits[2] = 3;
                    if (Constants.pentadAngle2 >= ob - Constants.eps &&d < 3.04f) expectedHits[2] = 4;
                    if (Constants.pentadAngle2 >= ob - Constants.eps &&d < 3.04f) expectedHits[2] = 5;
                }
                if (r == RobotType.LUMBERJACK){
                    if (d < 6.51771f) expectedHits[2] = 1;
                    if (d < 3.1537f) expectedHits[2] = 2;
                    if (d < 3.04f) expectedHits[2] = 3;
                    if (Constants.pentadAngle2 >= ob - Constants.eps &&d < 3.04f) expectedHits[2] = 4;
                    if (Constants.pentadAngle2 >= ob - Constants.eps &&d < 3.04f) expectedHits[2] = 5;
                }
                if (r == RobotType.SCOUT){
                    if (d < 6.03999f) expectedHits[2] = 1;
                    if (d < 3.04f) expectedHits[2] = 2;
                    if (d < 3.04f) expectedHits[2] = 3;
                    if (Constants.pentadAngle2 >= ob - Constants.eps &&d < 3.04f) expectedHits[2] = 4;
                    if (Constants.pentadAngle2 >= ob - Constants.eps &&d < 3.04f) expectedHits[2] = 5;
                }
                if (r == RobotType.ARCHON){
                    if (d < 22.04f) expectedHits[2] = 1;
                    if (d < 6.7674f) expectedHits[2] = 2;
                    if (d < 5.83556f) expectedHits[2] = 3;
                    if (Constants.pentadAngle2 >= ob - Constants.eps &&d < 4.04f) expectedHits[2] = 4;
                    if (Constants.pentadAngle2 >= ob - Constants.eps &&d < 4.04f) expectedHits[2] = 5;
                }
            }

            if (Constants.pentadAngle2 >= ob - Constants.eps && Greedy.sortedEnemies.length >= 3) expectedHits[2] = Math.max(expectedHits[2], 2);
            //PENTADSHOT2

        }

}
