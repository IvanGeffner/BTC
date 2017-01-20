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
    static int[] obstacles;
    static float minDistToTarget = Constants.INF;

    static HashMap<Integer, Integer> collisionLocations = new HashMap<>();
    static int[] intervals;

    static boolean shouldMove = true; //Si t'has de moure

    static int bulletDodge = -10; //Ultim torn on has esquivat una bala

    static int inter;



    static MapLocation pos;
    static float R = -1, r = -1, rr;
    static int cont, contBullets;
    static MapLocation target;
    static Direction dir;

    static RobotInfo[] Ri;
    static TreeInfo[] Ti;
    static BulletInfo[] bullets;



    static void moveToSelf(RobotController rc, int bytecodeleft){
        resetObstacle(rc);
        moveGreedy(rc, rc.getLocation(), bytecodeleft);
    }



    static void moveGreedy(RobotController rc, MapLocation tar, int bytecodeleft){
        target = tar;
        if (target == null) return;

        try {
            pos = rc.getLocation();
            if (R < 0) {
                R = rc.getType().bodyRadius;
                r = rc.getType().strideRadius;
                rr = r * r;
            }

            //SI L'OBSTACLE JA NO HI ES, RESET!
            if (obstacle != null) {
                if (!rc.canSenseAllOfCircle(obstacle, rc.getType().bodyRadius)) resetObstacle(rc);
                else if (!rc.onTheMap(obstacle, rc.getType().bodyRadius)) resetObstacle(rc);
                else if (!rc.isCircleOccupiedExceptByThisRobot(obstacle, rc.getType().bodyRadius)) resetObstacle(rc);
            }

            //ESCOLLIM LA DIRECCIO PRINCIPAL
            if (obstacle == null) dir = pos.directionTo(target);
            else dir = pos.directionTo(obstacle);


            //SI BAIXAEM DISTMIN I ENS PODEM MOURE AL TARGET, RESET [I EL GREEDY HI ANIRA]
            float dist = pos.distanceTo(target);
            if (left != 0 && dist < minDistToTarget && rc.canMove(target)) {
                resetObstacle(rc);
            }

            //TRIEM I FEM EL GREEDY
            Ri = rc.senseNearbyRobots(R + r, null);
            Ti = new TreeInfo[0];
            if (!(rc.getType() == RobotType.SCOUT)) {
                Ti = rc.senseNearbyTrees(R + r, null);
            }
            bullets = rc.senseNearbyBullets(Constants.BULLETSIGHT);


            float expectedByteCode = Clock.getBytecodeNum();
            expectedByteCode += Ri.length * Constants.COSTCYCLE1 + (Constants.COSTSORT + Constants.COSTSELECTION) * 2 * Ri.length;
            expectedByteCode += Ti.length * Constants.COSTCYCLE1 + (Constants.COSTSORT + Constants.COSTSELECTION) * 2 * Ti.length;
            expectedByteCode += bullets.length * Constants.COSTCYCLE2 + (Constants.COSTSORT + Constants.COSTSELECTION) * 2 * bullets.length;

            Direction dirGreedy;
            if (expectedByteCode < bytecodeleft) dirGreedy = greedyStep(rc, bytecodeleft);
            else dirGreedy = greedyStepLowBytecode(rc, bytecodeleft);

            //ACTUALITZEM
            if (left != 0) {
                if (dist < minDistToTarget) minDistToTarget = dist;
                addCollisionLocation(rc);
            }

            //SI EL TARGET ESTA MES A PROP, INTENTEM ANAR DIRECTE
            if ((!shouldMove && pos.directionTo(target) == null) || (pos.directionTo(target) != null && Math.abs(dirGreedy.radiansBetween(pos.directionTo(target))) < Constants.eps)){
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

    public static Direction greedyStep(RobotController rc, int bytecodeLeft){


        if (dir == null){
            dir = new Direction ((float)Math.random(), (float)Math.random());
            shouldMove = false;
        } else shouldMove = true;

        inter = 0; //position at intervals

        int a = 1;
        if (left == 0) ++a;

        MapLocation pos = rc.getLocation();
        boolean scout = (rc.getType() == RobotType.SCOUT);

        cont = 0;

        //System.out.println("PRecomputation" + Clock.getBytecodeNum());

        int maxLength = 2*Ti.length + 2*Ri.length + 4*bullets.length;

        intervals = new int[maxLength];
        obstacles = new int[maxLength];

        int obstacleCount = -1;

        for (RobotInfo ri : Ri){
            ++obstacleCount;
            if (Clock.getBytecodeNum() + Constants.COSTCYCLE1 + inter*(Constants.COSTSORT + a*Constants.COSTSELECTION) >= bytecodeLeft) break;
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
                obstacles[inter] = obstacleCount;
                ++inter;
                float y = dir.radiansBetween(dir2.rotateRightRads(angle));
                if (y < 0) y += Constants.PI2;
                intervals[inter] = (Math.round(y * Constants.ANGLEFACTOR) << (Constants.NUMELEMENTS + 2)) + 2 + 4 * (inter & 0x1F);
                obstacles[inter] = obstacleCount;
                ++inter;
                if (y > x) ++cont;
            }

        }

        obstacleCount = Ri.length - 1;

        if (!scout) {
            for (TreeInfo ti : Ti) {
                ++obstacleCount;
                if (Clock.getBytecodeNum() + Constants.COSTCYCLE1 + inter*(Constants.COSTSORT + a*Constants.COSTSELECTION) >= bytecodeLeft) break;
                if (ti.getID() == rc.getID()) continue;

                MapLocation m2 = ti.getLocation();
                Direction dir2 = pos.directionTo(m2);

                float l = (R+ti.getRadius());

                float t = (pos.distanceSquaredTo(m2) + rr - l*l)/(2.0f * pos.distanceTo(m2)*r);

                if (t <= 1 && t >= -1) {

                    float angle = (float) Math.acos(t) + 0.001f;

                    float x = dir.radiansBetween(dir2.rotateLeftRads(angle));
                    if (x < 0) x += Constants.PI2;
                    intervals[inter] = (Math.round(x * Constants.ANGLEFACTOR) << (Constants.NUMELEMENTS + 2)) + 4 * (inter & 0x1F);
                    obstacles[inter] = obstacleCount;
                    ++inter;
                    float y = dir.radiansBetween(dir2.rotateRightRads(angle));
                    if (y < 0) y += Constants.PI2;
                    intervals[inter] = (Math.round(y * Constants.ANGLEFACTOR) << (Constants.NUMELEMENTS + 2)) + 2 + 4 * (inter & 0x1F);
                    obstacles[inter] = obstacleCount;
                    ++inter;
                    if (y > x) ++cont;
                }

            }

        }

        contBullets = 0;

        for (BulletInfo bul : bullets) {
            if (maxLength - inter <= 3) break;
            if (Clock.getBytecodeNum() + Constants.COSTCYCLE2 + inter*(Constants.COSTSORT + a*Constants.COSTSELECTION) >= bytecodeLeft) break;

            addIntervals(bul);

        }

        if (inter < maxLength){
            intervals = Arrays.copyOf(intervals, inter);
            obstacles = Arrays.copyOf(obstacles, inter);
        }

        //System.out.println("PRE-SORT: " + intervals.length + "  " + Clock.getBytecodeNum() );
        if (intervals.length > 1) quickSort(0, intervals.length-1);
        //System.out.println("POST-SORT: " + intervals.length + "  " + Clock.getBytecodeNum() );
        //Arrays.sort(intervals);

        newObs = -1;

        if (left != 0){
            Direction di = bestDirection(rc, (left > 0), 0);
            if (newObs >= 0) {
                if (newObs < Ri.length) obstacle = Ri[newObs].getLocation();
                else {
                    newObs -= Ri.length;
                    if (newObs < Ti.length) obstacle = Ti[newObs].getLocation();
                    else bulletDodge = rc.getRoundNum();
                }
            }
            return di;
        } else{
            Direction di1 =  bestDirection(rc, true, 1);
            int aux = newObs;
            Direction di2 =  bestDirection(rc, false, 1);
            if (di1 != null && pos.add(di1, r).distanceTo(target) < pos.add(di2, r).distanceTo(target)){
                newObs = aux;
                di2 = di1;
                left = 1;
            }
            if (newObs >= 0) {
                if (newObs < Ri.length) obstacle = Ri[newObs].getLocation();
                else {
                    newObs -= Ri.length;
                    if (newObs < Ti.length) obstacle = Ti[newObs].getLocation();
                    else bulletDodge = rc.getRoundNum();
                }
            }
            return di2;
        }
    }

    public static Direction greedyStepLowBytecode(RobotController rc, int bytecodeLeft){


        if (dir == null){
            dir = new Direction ((float)Math.random(), (float)Math.random());
            shouldMove = false;
        } else shouldMove = true;

        inter = 0; //position at intervals

        MapLocation pos = rc.getLocation();
        boolean scout = (rc.getType() == RobotType.SCOUT);

        cont = 0;

        int a = 1;
        if (left == 0) ++a;

        //System.out.println("PRecomputation" + Clock.getBytecodeNum());

        int maxLength = 2*Ti.length + 2*Ri.length + 2*bullets.length;

        intervals = new int[maxLength];
        obstacles = new int[maxLength];

        int obstacleCount = -1;

        for (RobotInfo ri : Ri){
            ++obstacleCount;
            if (Clock.getBytecodeNum() + Constants.COSTCYCLE1 + inter*(Constants.COSTSORT + a*Constants.COSTSELECTION) >= bytecodeLeft) break;
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
                obstacles[inter] = obstacleCount;
                ++inter;
                float y = dir.radiansBetween(dir2.rotateRightRads(angle));
                if (y < 0) y += Constants.PI2;
                intervals[inter] = (Math.round(y * Constants.ANGLEFACTOR) << (Constants.NUMELEMENTS + 2)) + 2 + 4 * (inter & 0x1F);
                obstacles[inter] = obstacleCount;
                ++inter;
                if (y > x) ++cont;
            }

        }

        obstacleCount = Ri.length - 1;

        if (!scout) {
            for (TreeInfo ti : Ti) {
                ++obstacleCount;
                if (Clock.getBytecodeNum() + Constants.COSTCYCLE1 + inter*(Constants.COSTSORT + a*Constants.COSTSELECTION) >= bytecodeLeft) break;
                if (ti.getID() == rc.getID()) continue;

                MapLocation m2 = ti.getLocation();
                Direction dir2 = pos.directionTo(m2);

                float l = (R+ti.getRadius());

                float t = (pos.distanceSquaredTo(m2) + rr - l*l)/(2.0f * pos.distanceTo(m2)*r);

                if (t <= 1 && t >= -1) {

                    float angle = (float) Math.acos(t) + 0.001f;

                    float x = dir.radiansBetween(dir2.rotateLeftRads(angle));
                    if (x < 0) x += Constants.PI2;
                    intervals[inter] = (Math.round(x * Constants.ANGLEFACTOR) << (Constants.NUMELEMENTS + 2)) + 4 * (inter & 0x1F);
                    obstacles[inter] = obstacleCount;
                    ++inter;
                    float y = dir.radiansBetween(dir2.rotateRightRads(angle));
                    if (y < 0) y += Constants.PI2;
                    intervals[inter] = (Math.round(y * Constants.ANGLEFACTOR) << (Constants.NUMELEMENTS + 2)) + 2 + 4 * (inter & 0x1F);
                    obstacles[inter] = obstacleCount;
                    ++inter;
                    if (y > x) ++cont;
                }

            }

        }

        contBullets = 0;

        for (BulletInfo bul : bullets) {
            if (Clock.getBytecodeNum() + Constants.COSTCYCLE1 + inter*(Constants.COSTSORT + a*Constants.COSTSELECTION) >= bytecodeLeft) break;

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
                obstacles[inter] = Constants.INTINF;
                ++inter;
                float y = dir.radiansBetween(dir2.rotateRightRads(angle));
                if (y < 0) y += Constants.PI2;
                intervals[inter] = (Math.round(y * Constants.ANGLEFACTOR) << (Constants.NUMELEMENTS + 2)) + 3 + 4 * (inter & 0x1F);
                obstacles[inter] = Constants.INTINF;
                ++inter;
                if (y > x) ++contBullets;
            }
        }

        if (inter < maxLength){
            intervals = Arrays.copyOf(intervals, inter);
            obstacles = Arrays.copyOf(obstacles, inter);
        }

        //System.out.println("PRE-SORT: " + intervals.length + "  " + Clock.getBytecodeNum() );
        if (intervals.length > 1) quickSort(0, intervals.length-1);
        //System.out.println("POST-SORT: " + intervals.length + "  " + Clock.getBytecodeNum() );
        //Arrays.sort(intervals);

        newObs = -1;

        if (left != 0){
            Direction di = bestDirection(rc, (left > 0), 0);
            if (newObs >= 0) {
                if (newObs < Ri.length) obstacle = Ri[newObs].getLocation();
                else {
                    newObs -= Ri.length;
                    if (newObs < Ti.length) obstacle = Ti[newObs].getLocation();
                    else bulletDodge = rc.getRoundNum();
                }
            }
            return di;
        } else{
            Direction di1 =  bestDirection(rc, true, 1);
            int aux = newObs;
            Direction di2 =  bestDirection(rc, false, 1);
            if (di1 != null && pos.add(di1, r).distanceTo(target) < pos.add(di2, r).distanceTo(target)){
                newObs = aux;
                di2 = di1;
                left = 1;
            }
            if (newObs >= 0) {
                if (newObs < Ri.length) obstacle = Ri[newObs].getLocation();
                else {
                    newObs -= Ri.length;
                    if (newObs < Ti.length) obstacle = Ti[newObs].getLocation();
                    else bulletDodge = rc.getRoundNum();
                }
            }
            return di2;
        }
    }


    static Direction bestDirection(RobotController rc, boolean l, int tries){
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
                        newObs = obstacles[(k >> 2)&0x1F];
                        if (l) left = 1;
                        else left = -1;
                    }
                    return ans;
                }
                else{
                    return bestDirection(rc, !l, tries + 1);
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
                    newObs = obstacles[(k >> 2)&0x1F];
                    if (l) left = 1;
                    else left = -1;
                }
                return ans;
            }
            else{
                return bestDirection(rc, !l, tries + 1);
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

    static void addIntervals(BulletInfo b){

        System.out.println("Bytecode pls1: " + Clock.getBytecodeNum());

        try {


            MapLocation m1 = b.getLocation();
            MapLocation m2 = m1.add(b.getDir(), b.getSpeed());
            Direction perp = b.getDir().rotateLeftRads((float) Math.PI / 2);

            if (!shouldMove && m1.add(b.getDir(), b.getSpeed()/2).distanceTo(pos) <= b.getSpeed()/2 + R) shouldMove = true;
            if (m1.distanceTo(pos) > r+R + Constants.eps && Math.abs(pos.directionTo(m1).radiansBetween(b.getDir())) < Math.PI/2) return;

            MapLocation V11 = m1.add(perp, R);
            MapLocation V21 = m2.add(perp, R);

            Direction DV11 = pos.directionTo(V11);
            Direction DV21 = pos.directionTo(V21);

            float dist1 = pos.distanceTo(V11) * (float) Math.cos(perp.radiansBetween(pos.directionTo(V11)));

            MapLocation V12 = m1.add(perp.opposite(), R);
            MapLocation V22 = m2.add(perp.opposite(), R);
            Direction DV12 = pos.directionTo(V12);
            Direction DV22 = pos.directionTo(V22);

            float dist2 = pos.distanceTo(V12) * (float) Math.cos(perp.radiansBetween(pos.directionTo(V12)));

            Direction dirv11 = null, dirv21 = null, dirv12 = null, dirv22 = null;

            float c = dist1/r;
            if (-1 <= c && c <= 1) {

                float ang = (float) Math.acos(c);
                dirv11 = perp.rotateLeftRads(ang);
                if (!Mates.areOrdered(DV11, dirv11, DV21)) dirv11 = null;
                dirv21 = perp.rotateRightRads(ang);
                if (!Mates.areOrdered(DV11, dirv21, DV21)) dirv21 = null;

            }

            c = dist2/r;

            if (-1 <= c && c <= 1) {

                float ang2 = (float) Math.acos(c);
                dirv12 = perp.rotateLeftRads(ang2);
                if (!Mates.areOrdered(DV12, dirv12, DV22)) dirv12 = null;
                dirv22 = perp.rotateRightRads(ang2);
                if (!Mates.areOrdered(DV12, dirv22, DV22)) dirv22 = null;

            }

            if (dirv11 == null || dirv12 == null) {
                float t = (pos.distanceSquaredTo(m1) + rr - R * R) / (2.0f * pos.distanceTo(m1) * r);
                if (-1 <= t && t <= 1) {
                    Direction d = pos.directionTo(m1);
                    float angle = (float) Math.acos(t);
                    float angle2 = (float)Math.acos((pos.distanceSquaredTo(m1) + R*R - rr) / (2.0f * pos.distanceTo(m1) * R));
                    if (dirv11 == null){
                        if (m1.directionTo(pos).rotateLeftRads(angle2).radiansBetween(perp) <= 0) dirv11 = d.rotateRightRads(angle);
                    }
                    if (dirv12 == null) {
                        if (m1.directionTo(pos).rotateRightRads(angle2).radiansBetween(perp) <= 0) dirv12 = d.rotateLeftRads(angle);
                    }
                }
            }

            if (dirv21 == null || dirv22 == null) {
                float t = (pos.distanceSquaredTo(m2) + rr - R * R) / (2.0f * pos.distanceTo(m2) * r);
                if (-1 <= t && t <= 1) {
                    Direction d = pos.directionTo(m2);
                    float angle = (float) Math.acos(t);
                    float angle2 = (float)Math.acos((pos.distanceSquaredTo(m2) + R*R - rr) / (2.0f * pos.distanceTo(m2) * R));
                    if (dirv21 == null){
                        if (m2.directionTo(pos).rotateRightRads(angle2).radiansBetween(perp) >= 0) dirv21 = d.rotateLeftRads(angle);
                    }
                    if (dirv22 == null) {
                        if (m2.directionTo(pos).rotateLeftRads(angle2).radiansBetween(perp) >= 0) dirv22 = d.rotateRightRads(angle);
                    }
                }
            }

            if (dirv11 != null) {
                if (dirv21 != null) {
                    if (dirv12 != null) {
                        addInterval(dir.radiansBetween(dirv22), dir.radiansBetween(dirv21));
                        addInterval(dir.radiansBetween(dirv11), dir.radiansBetween(dirv12));
                    } else addInterval(dir.radiansBetween(dirv11), dir.radiansBetween(dirv21));
                } else {
                    if (dirv12 != null) addInterval(dir.radiansBetween(dirv11), dir.radiansBetween(dirv12));
                }
            } else {
                if (dirv21 != null) addInterval(dir.radiansBetween(dirv22), dir.radiansBetween(dirv21));
                else if (dirv12 != null) addInterval(dir.radiansBetween(dirv22), dir.radiansBetween(dirv12));
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }


        System.out.println("Bytecode pls2: " + Clock.getBytecodeNum());



    }

    static void addInterval (float right, float left) {
        right -= Constants.eps;
        left += Constants.eps;
        if (right < 0) right += Constants.PI2;
        if (left < 0) left += Constants.PI2;

        if (left < right){
            ++contBullets;
        }
        intervals[inter] = (Math.round(left * Constants.ANGLEFACTOR) << (Constants.NUMELEMENTS + 2)) + 1 + 4*(inter&0x1F);
        obstacles[inter] = Constants.INTINF;
        ++inter;
        intervals[inter] = (Math.round(right*Constants.ANGLEFACTOR) << (Constants.NUMELEMENTS + 2)) + 3+ 4*(inter&0x1F);
        obstacles[inter] = Constants.INTINF;
        ++inter;
    }


}
