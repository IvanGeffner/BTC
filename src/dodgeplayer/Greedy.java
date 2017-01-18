package dodgeplayer;

import battlecode.common.*;
import com.sun.tools.internal.jxc.ap.Const;

import java.util.*;

/**
 * Created by Ivan on 1/15/2017.
 */
public class Greedy {


    static MapLocation target;
    static int newObs;
    static MapLocation obstacle = null;
    static float minDistToTarget = Constants.INF;
    static int left = 0;

    static HashMap<Integer, Integer> collisionLocations = new HashMap<>();
    static int[] intervals;



    static void moveGreedy(RobotController rc, MapLocation tar){
        if (tar == null) return;
        target = tar;
        try {
            //SABER SI ES FA GREEDY O ES VA DIRECTE
            MapLocation pos = rc.getLocation();
            Direction dirObstacle = null;
            if (obstacle != null){
                if (!rc.canSenseAllOfCircle(obstacle, rc.getType().bodyRadius)) resetObstacle();
                else if (!rc.onTheMap(obstacle, rc.getType().bodyRadius)) resetObstacle();
                else if (!rc.isCircleOccupiedExceptByThisRobot(obstacle, rc.getType().bodyRadius)) resetObstacle();
            }
            if (obstacle == null) dirObstacle = pos.directionTo(target);
            else dirObstacle = pos.directionTo(obstacle);
            float dist = pos.distanceTo(target);

            if (left != 0 && dist < minDistToTarget && rc.canMove(target)){
                resetObstacle();
            }

            Direction dirGreedy = greedyStep(rc, dirObstacle);
            if (dist < minDistToTarget) minDistToTarget = dist;
            if (left != 0) addCollisionLocation(rc);
            if (Math.abs(dirGreedy.radiansBetween(pos.directionTo(target))) < Constants.eps){
                if (rc.canMove(target)){
                    rc.move(target);
                    return;
                }
            }
            if (rc.canMove(dirGreedy)){
                rc.move(dirGreedy);
                return;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    static void resetObstacle(){
        collisionLocations = new HashMap<>();
        obstacle = null;
        minDistToTarget = Constants.INF;
        left = 0;
    }


    public static void addCollisionLocation(RobotController rc){
        MapLocation pos = rc.getLocation();
        MapLocation collisionPos = pos.add(pos.directionTo(obstacle), Constants.COLLISIONDIST);
        int x = Math.round(collisionPos.x/ Constants.COLLISIONRANGE), y = Math.round(collisionPos.y/ Constants.COLLISIONRANGE);
        int a = 0;
        if (left > 0) a = 1;
        int hash = 2*(x* Constants.COLLISIONHASH + y)+a;
        if (!collisionLocations.containsKey(hash)){
            collisionLocations.put(hash, rc.getRoundNum());
        } else {
            int round = collisionLocations.get(hash);
            if (rc.getRoundNum() - round > Constants.COLLISIONROUND){
                resetObstacle();
            }
        }
    }

    public static Direction greedyStep(RobotController rc, Direction dir){

        int inter = 0; //position at intervals

        float r = rc.getType().strideRadius;
        float rr = r*r;
        float R = rc.getType().bodyRadius;
        MapLocation pos = rc.getLocation();
        boolean scout = (rc.getType() == RobotType.SCOUT);

        RobotInfo[] Ri = rc.senseNearbyRobots(R + r, rc.getTeam());
        RobotInfo[] RiE = rc.senseNearbyRobots(R + r + 4.2f, rc.getTeam().opponent());

        int cont = 0;

        TreeInfo Ti[] = new TreeInfo[0];
        if (!scout){
            Ti = rc.senseNearbyTrees(R + r, null);
        }

        //System.out.println("PRecomputation" + Clock.getBytecodeNum());

        BulletInfo[] bullets = rc.senseNearbyBullets(Constants.BULLETSIGHT);

        int maxLength = 2*Ti.length + 2*Ri.length + 2*RiE.length + 4*bullets.length;
        if (maxLength > Constants.MAXSORT) maxLength = Constants.MAXSORT;

        intervals = new int[maxLength];

        for (RobotInfo ri : Ri){
            if (maxLength - inter <= 1) break;
            if (ri.getID() == rc.getID()) continue;
            MapLocation m2 = ri.getLocation();
            Direction dir2 = pos.directionTo(m2);

            float l = (R+ri.getType().bodyRadius);

            float t = (pos.distanceSquaredTo(m2) + rr - l*l)/(2.0f * pos.distanceTo(m2)*r);

            if (t <= 1 && t >= -1) {

                float angle = (float) Math.acos(t) + 0.001f;


                float x = dir.radiansBetween(dir2.rotateLeftRads(angle));
                if (x < 0) x += Constants.PI2;
                intervals[inter] = (Math.round(x * Constants.ANGLEFACTOR) << (Constants.NUMELEMENTS + 2)) + 4 * (inter & 0x1F);
                ++inter;
                float y = dir.radiansBetween(dir2.rotateRightRads(angle));
                if (y < 0) y += Constants.PI2;
                intervals[inter] = (Math.round(y * Constants.ANGLEFACTOR) << (Constants.NUMELEMENTS + 2)) + 2 + 4 * (inter & 0x1F);
                ++inter;
                if (y > x) ++cont;
            }
        }

        for (RobotInfo ri : RiE){
            if (maxLength - inter <= 1) break;
            if (ri.getID() == rc.getID()) continue;
            MapLocation m2 = ri.getLocation();
            Direction dir2 = pos.directionTo(m2);

            float l = (R+ri.getType().bodyRadius) + Constants.safetyDistance(ri.getType());
            if(ri.getMoveCount() == 0) l += ri.getType().strideRadius;
            if (l >= pos.distanceTo(m2) + pos.distanceTo(m2)) l = pos.distanceTo(m2) + pos.distanceTo(m2) + 0.25f;

            float t = (pos.distanceSquaredTo(m2) + rr - l*l)/(2.0f * pos.distanceTo(m2)*r);

            if (t < -1 || t > 1){
                l = (R+ri.getType().bodyRadius);
                t = (pos.distanceSquaredTo(m2) + rr - l*l)/(2.0f * pos.distanceTo(m2)*r);
            }

            if (t <= 1 && t >= -1) {

                float angle = (float) Math.acos(t) + 0.001f;


                float x = dir.radiansBetween(dir2.rotateLeftRads(angle));
                if (x < 0) x += Constants.PI2;
                intervals[inter] = (Math.round(x * Constants.ANGLEFACTOR) << (Constants.NUMELEMENTS + 2)) + 4 * (inter & 0x1F);
                ++inter;
                float y = dir.radiansBetween(dir2.rotateRightRads(angle));
                if (y < 0) y += Constants.PI2;
                intervals[inter] = (Math.round(y * Constants.ANGLEFACTOR) << (Constants.NUMELEMENTS + 2)) + 2 + 4 * (inter & 0x1F);
                ++inter;
                if (y > x) ++cont;
            }
        }



        if (!scout) {
            for (TreeInfo ti : Ti) {
                if (maxLength - inter <= 1) break;
                if (ti.getID() == rc.getID()) continue;
                MapLocation m2 = ti.getLocation();
                Direction dir2 = pos.directionTo(m2);

                float l = (R+ti.getRadius());

                float angle = (float)Math.acos((pos.distanceSquaredTo(m2) + rr - l*l)/(2.0f * pos.distanceTo(m2)*r))+ 0.001f;

                float x = dir.radiansBetween(dir2.rotateLeftRads(angle));
                if (x < 0) x+= Constants.PI2;
                intervals[inter] = (Math.round(x*Constants.ANGLEFACTOR) << (Constants.NUMELEMENTS + 2))+ 4*(inter&0x1F);
                ++inter;
                float y = dir.radiansBetween(dir2.rotateRightRads(angle));
                if (y < 0) y+= Constants.PI2;
                intervals[inter] = (Math.round(y*Constants.ANGLEFACTOR) << (Constants.NUMELEMENTS + 2)) + 2+ 4*(inter&0x1F);
                ++inter;
                if (y > x) ++cont;
            }

        }

        int contBullets = 0;

        for (BulletInfo bul : bullets){
            if (maxLength - inter <= 1) break;


            MapLocation m2 = bul.getLocation().add(bul.getDir(), bul.getSpeed()/2);
            Direction dir2 = pos.directionTo(m2);


            float l = (R + bul.getSpeed());

            float t = (pos.distanceSquaredTo(m2) + rr - l*l)/(2.0f * pos.distanceTo(m2)*r);
            if (-1 <= t && t <= 1){
                float angle = (float)Math.acos(t)+ 0.001f;

                float x = dir.radiansBetween(dir2.rotateLeftRads(angle));
                if (x < 0) x+= Constants.PI2;
                intervals[inter] = (Math.round(x*Constants.ANGLEFACTOR) << (Constants.NUMELEMENTS + 2))+1+ 4*(inter&0x1F);
                ++inter;
                float y = dir.radiansBetween(dir2.rotateRightRads(angle));
                if (y < 0) y+= Constants.PI2;
                intervals[inter] = (Math.round(y*Constants.ANGLEFACTOR) << (Constants.NUMELEMENTS + 2)) + 3+ 4*(inter&0x1F);
                ++inter;
                if (y > x) ++contBullets;
            }




            /*if (maxLength - inter <= 3) break;



            MapLocation m2 = bul.getLocation();
            Direction dir2 = pos.directionTo(m2);


            float t = (pos.distanceSquaredTo(m2) + rr - R*R)/(2.0f * pos.distanceTo(m2)*r);
            if (-1 <= t && t <= 1){
                float angle = (float)Math.acos(t)+ 0.001f;

                float x = dir.radiansBetween(dir2.rotateLeftRads(angle));
                if (x < 0) x+= Constants.PI2;
                intervals[inter] = (Math.round(x*Constants.ANGLEFACTOR) << (Constants.NUMELEMENTS + 2))+1+ 4*(inter&0x1F);
                ++inter;
                float y = dir.radiansBetween(dir2.rotateRightRads(angle));
                if (y < 0) y+= Constants.PI2;
                intervals[inter] = (Math.round(y*Constants.ANGLEFACTOR) << (Constants.NUMELEMENTS + 2)) + 3+ 4*(inter&0x1F);
                ++inter;
                if (y > x) ++contBullets;
            }


            m2 = bul.getLocation().add(bul.getDir(), bul.getSpeed());
            dir2 = pos.directionTo(m2);


            t = (pos.distanceSquaredTo(m2) + rr - R*R)/(2.0f * pos.distanceTo(m2)*r);
            if (-1 <= t && t <= 1){
                float angle = (float)Math.acos(t)+ 0.001f;

                float x = dir.radiansBetween(dir2.rotateLeftRads(angle));
                if (x < 0) x+= Constants.PI2;
                intervals[inter] = (Math.round(x*Constants.ANGLEFACTOR) << (Constants.NUMELEMENTS + 2))+1+ 4*(inter&0x1F);
                ++inter;
                float y = dir.radiansBetween(dir2.rotateRightRads(angle));
                if (y < 0) y+= Constants.PI2;
                intervals[inter] = (Math.round(y*Constants.ANGLEFACTOR) << (Constants.NUMELEMENTS + 2)) + 3+ 4*(inter&0x1F);
                ++inter;
                if (y > x) ++contBullets;
            }*/

        }

        if (inter < maxLength) intervals = Arrays.copyOf(intervals, inter);

        newObs = -1;

        if (left != 0){
            Direction di = bestDirection(rc, dir, (left > 0), 0, cont, contBullets);
            if (newObs >= 0) {
                if (newObs < 2 * Ri.length) obstacle = Ri[newObs / 2].getLocation();
                else {
                    newObs -= 2 * Ri.length;
                    if (newObs < 2 * RiE.length) obstacle = RiE[newObs / 2].getLocation();
                    else{
                        newObs -= 2*RiE.length;
                        if (newObs < 2 * Ti.length) obstacle = Ti[newObs / 2].getLocation();
                    }
                }
            }
            return di;
        } else{
            Direction di1 =  bestDirection(rc, dir, true, 1, cont, contBullets);
            int aux = newObs;
            Direction di2 =  bestDirection(rc, dir, false, 1, cont, contBullets);
            if (di1 != null && pos.add(di1, r).distanceTo(target) < pos.add(di2, r).distanceTo(target)){
                newObs = aux;
                di2 = di1;
                left = 1;
            }
            if (newObs >= 0) {
                if (newObs < 2 * Ri.length) obstacle = Ri[newObs / 2].getLocation();
                else {
                    newObs -= 2 * Ri.length;
                    if (newObs < 2 * RiE.length) obstacle = RiE[newObs / 2].getLocation();
                    else{
                        newObs -= 2*RiE.length;
                        if (newObs < 2 * Ti.length) obstacle = Ti[newObs / 2].getLocation();
                    }
                }
            }
            return di2;
        }
    }


    static void addInterval (float left, float right, int a, int p){
        int x = (Math.round(left*Constants.ANGLEFACTOR) << 2) + a;
        int y =  (Math.round(right*Constants.ANGLEFACTOR) << 2) + 2 + a;
        intervals[p] = x;
        intervals[p+1] = y;
    }

    static Direction bestDirection(RobotController rc, Direction dir, boolean l, int tries, int cont, int contBullets){
        if (tries > 2) return null;
        Arrays.sort(intervals);

        //System.out.println("Postsort" + intervals.length+ " " + Clock.getBytecodeNum());

        Direction ans = null;
        int minBulletcont = 999;

        int k = -1;

        if (cont == 0){
            if (contBullets == 0) return dir;
            ans = dir;
            minBulletcont = contBullets;
        }

        if (l) {

            //cont keeps open (

            for (int i = 0; i < intervals.length; ++i) {
                //System.out.println("bucle 1 " + Clock.getBytecodeNum());
                int a = intervals[i];
                //System.out.println("bucle 2 " + Clock.getBytecodeNum());
                i = i&31;
                //System.out.println("bucle 3 " + Clock.getBytecodeNum());

                if ((a&2) != 0){
                    if ((a&1) == 0) ++cont;
                    else ++contBullets;
                } else{
                    if ((a&1) == 0) --cont;
                    else --contBullets;
                    if (cont == 0) {
                        if (contBullets < minBulletcont) {
                            minBulletcont = contBullets;
                            float x = (float) (a >> (2 + Constants.NUMELEMENTS)) / Constants.ANGLEFACTOR;
                            k = a;

                            ans = dir.rotateLeftRads(x);
                        }
                    }
                }
                //System.out.println("bucle 4 " + Clock.getBytecodeNum());
            }


            if (ans != null) {
                if (rc.canMove(ans)) {
                    if (k >= 0) {
                        newObs = (k >> 2)&0x1F;
                        if (l) left = 1;
                        else left = -1;
                    }
                    return ans;
                }
                else{
                    return bestDirection(rc, dir, !l, tries + 1, cont, contBullets);
                }
            } else return null;
        }
        //cont keeps open (

        for (int i = intervals.length - 1; i >= 0; --i) {
            int a = intervals[i];
            if ((a&2) == 0){
                if ((a&1) == 0) ++cont;
                else ++contBullets;
            } else{
                if ((a&1) == 0) --cont;
                else --contBullets;
                if (cont == 0) {
                    if (contBullets < minBulletcont) {
                        minBulletcont = contBullets;
                        float x = (float) (a >> (2 + Constants.NUMELEMENTS)) / Constants.ANGLEFACTOR;
                        k = a;
                        ans = dir.rotateLeftRads(x);
                    }
                }
            }

        }


        if (ans != null) {
            if (rc.canMove(ans)){
                if (k >= 0) {
                    newObs = (k >> 2)&0x1F;
                    if (l) left = 1;
                    else left = -1;
                }
                return ans;
            }
            else{
                return bestDirection(rc, dir, !l, tries + 1, cont, contBullets);
            }
        }
        return null;
    }

}
