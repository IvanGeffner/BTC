package dodgeplayer;

import battlecode.common.*;
import com.sun.tools.internal.jxc.ap.Const;

import java.util.*;

/**
 * Created by Ivan on 1/15/2017.
 */
public class Greedy {

    static int left = 0;
    static int newObs;
    static MapLocation obstacle = null;
    static float minDistToTarget = Constants.INF;

    static HashMap<Integer, Integer> collisionLocations = new HashMap<>();
    static int[] intervals;

    static boolean shouldMove = false; //Si t'has de moure

    static int bulletDodge = -10; //Ultim torn on has esquivat una bala

    RobotInfo[] Ri;
    TreeInfo[] Ti;
    BulletInfo[] bullets;



    static void moveGreedy(RobotController rc, MapLocation target){
        if (target == null) return;
        try {
            MapLocation pos = rc.getLocation();

            //SI L'OBSTACLE JA NO HI ES, RESET!
            if (obstacle != null){
                if (!rc.canSenseAllOfCircle(obstacle, rc.getType().bodyRadius)) resetObstacle(rc);
                else if (!rc.onTheMap(obstacle, rc.getType().bodyRadius)) resetObstacle(rc);
                else if (!rc.isCircleOccupiedExceptByThisRobot(obstacle, rc.getType().bodyRadius)) resetObstacle(rc);
            }

            //ESCOLLIM LA DIRECCIO PRINCIPAL
            Direction dirObstacle;
            if (obstacle == null) dirObstacle = pos.directionTo(target);
            else dirObstacle = pos.directionTo(obstacle);


            //SI BAIXAEM DISTMIN I ENS PODEM MOURE AL TARGET, RESET [I EL GREEDY HI ANIRA]
            float dist = pos.distanceTo(target);
            if (left != 0 && dist < minDistToTarget && rc.canMove(target)){
                resetObstacle(rc);
            }

            //FEM EL GREEDY
            Direction dirGreedy = greedyStep(rc, dirObstacle, target);

            //ACTUALITZEM
            if (left != 0) {
                if (dist < minDistToTarget) minDistToTarget = dist;
                addCollisionLocation(rc);
            }

            //SI EL TARGET ESTA MES A PROP, INTENTEM ANAR DIRECTE
            if ((!shouldMove && dirObstacle == null) || Math.abs(dirGreedy.radiansBetween(pos.directionTo(target))) < Constants.eps){
                if (rc.canMove(target)){
                    rc.move(target);
                    return;
                }
            }
            //ELSE ANEM EN LA DIRECCIO
            if (rc.canMove(dirGreedy)){
                rc.move(dirGreedy);
                return;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    static void resetObstacle(RobotController rc){
        collisionLocations = new HashMap<>();
        obstacle = null;
        minDistToTarget = Constants.INF;
        if (rc.getRoundNum() - bulletDodge > 1) left = 0; //Si estavem esquivant bales, no volem que canvii el sentit de gir durant un torn
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
                resetObstacle(rc);
            }
        }
    }

    public static Direction greedyStep(RobotController rc, Direction dir, MapLocation target){


        if (dir == null){
            dir = new Direction ((float)Math.random(), (float)Math.random());
            shouldMove = false;
        }

        int inter = 0; //position at intervals

        float r = rc.getType().strideRadius;
        float rr = r*r;
        float R = rc.getType().bodyRadius;

        MapLocation pos = rc.getLocation();
        boolean scout = (rc.getType() == RobotType.SCOUT);

        RobotInfo[] Ri = rc.senseNearbyRobots(R + r, null);

        int cont = 0;

        TreeInfo Ti[] = new TreeInfo[0];
        if (!scout){
            Ti = rc.senseNearbyTrees(R + r, null);
        }

        //System.out.println("PRecomputation" + Clock.getBytecodeNum());

        BulletInfo[] bullets = rc.senseNearbyBullets(Constants.BULLETSIGHT);

        int maxLength = 2*Ti.length + 2*Ri.length + 2*bullets.length;
        if (maxLength > Constants.MAXSORT) maxLength = Constants.MAXSORT;

        intervals = new int[maxLength];

        for (RobotInfo ri : Ri){
            if (maxLength - inter <= 1) break;
            if (ri.getID() == rc.getID()) continue;

            //CALCULA INTERVAL
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

        for (BulletInfo bul : bullets) {
            if (maxLength - inter <= 1) break;


            MapLocation m2 = bul.getLocation().add(bul.getDir(), bul.getSpeed() / 2);
            Direction dir2 = pos.directionTo(m2);

            float d = pos.distanceTo(m2);
            float l = (R + bul.getSpeed() / 2);

            if (!shouldMove && d <= R + l) shouldMove = true;


            float t = (pos.distanceSquaredTo(m2) + rr - l * l) / (2.0f * d * r);
            if (-1 <= t && t <= 1) {
                float angle = (float) Math.acos(t) + 0.001f;

                float x = dir.radiansBetween(dir2.rotateLeftRads(angle));
                if (x < 0) x += Constants.PI2;
                intervals[inter] = (Math.round(x * Constants.ANGLEFACTOR) << (Constants.NUMELEMENTS + 2)) + 1 + 4 * (inter & 0x1F);
                ++inter;
                float y = dir.radiansBetween(dir2.rotateRightRads(angle));
                if (y < 0) y += Constants.PI2;
                intervals[inter] = (Math.round(y * Constants.ANGLEFACTOR) << (Constants.NUMELEMENTS + 2)) + 3 + 4 * (inter & 0x1F);
                ++inter;
                if (y > x) ++contBullets;
            }
        }

        if (inter < maxLength) intervals = Arrays.copyOf(intervals, inter);

        System.out.println("PRE-SORT: " + intervals.length + "  " + Clock.getBytecodeNum() );
        if (intervals.length > 1) quickSort(0, intervals.length-1);
        System.out.println("PRE-SORT: " + intervals.length + "  " + Clock.getBytecodeNum() );
        //Arrays.sort(intervals);

        newObs = -1;

        if (left != 0){
            Direction di = bestDirection(rc, dir, (left > 0), 0, cont, contBullets);
            if (newObs >= 0) {
                if (newObs < 2 * Ri.length) obstacle = Ri[newObs / 2].getLocation();
                else {
                    newObs -= 2 * Ri.length;
                    if (newObs < 2 * Ti.length) obstacle = Ti[newObs / 2].getLocation();
                    else bulletDodge = rc.getRoundNum();
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
                    if (newObs < 2 * Ti.length) obstacle = Ti[newObs / 2].getLocation();
                    else bulletDodge = rc.getRoundNum();
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


    static void InsertionSort (int a, int b){
        for (int i = a; i <= b; ++i){
            int minj = i;
            for (int j = i+1; j <= b; ++j){
                if (intervals[j] < intervals[minj]) minj = j;
            }
            int aux = intervals[i];
            intervals[i] = intervals[minj];
            intervals[minj] = aux;
        }
    }

    static void quickSort(int lowerIndex, int higherIndex) {

        int i = lowerIndex;
        int j = higherIndex;
        //System.out.println("INDEX: "+ higherIndex + " " + lowerIndex);
        int pivot = intervals[(higherIndex+lowerIndex)/2];
        while (i <= j) {

            while (intervals[i] < pivot) {
                i++;
            }
            while (intervals[j] > pivot) {
                j--;
            }
            if (i <= j) {
                int temp = intervals[i];
                intervals[i] = intervals[j];
                intervals[j] = temp;
                i++;
                j--;
            }
        }
        // call quickSort() method recursively
        if (lowerIndex < j) {
            quickSort(lowerIndex, j);
        }
        if (i < higherIndex){
            quickSort(i, higherIndex);
        }
    }

    void addIntervals(BulletInfo b){






    }


}
