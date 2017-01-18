package dodgeplayer;

import battlecode.common.*;

import java.util.*;

/**
 * Created by Ivan on 1/15/2017.
 */
public class Greedy {

    static final int greedyTries = 6;

    static MapLocation target;

    static int newObs;
    static boolean finished;
    static MapLocation obstacle = null;
    static float minDistToTarget = Constants.INF;
    static int left = 0;

    static HashMap<Integer, Integer> collisionLocations;
    static BulletInfo[] bullets;
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
            if (obstacle == null) {
                if (rc.canMove(target)){
                    rc.move(target);
                    return;
                }
                dirObstacle = pos.directionTo(target);
            }
            else dirObstacle = pos.directionTo(obstacle);
            float dist = pos.distanceTo(target);

            if (left != 0){
                dist = pos.distanceTo(target);
                if (dist < minDistToTarget && rc.canMove(target)){
                    resetObstacle();
                    rc.move(target);
                    return;
                }
            } else{
                collisionLocations = new HashMap<>();
            }

            Direction dirGreedy = greedyMove(rc, dirObstacle);
            if (dist < minDistToTarget) minDistToTarget = dist;
            if (left != 0) addCollisionLocation(rc);
            if (rc.canMove(dirGreedy)) rc.move(dirGreedy);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

    }

    static void resetObstacle(){
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



    /*public static Direction greedyMove2(RobotController rc, Direction dir, int tries, boolean left){
        if (tries == 0){
            finished = false;
        }
        if (tries > greedyTries){
            finished = false;
            return null;
        }
        float r = rc.getType().strideRadius;
        float R = rc.getType().bodyRadius;
        MapLocation pos = rc.getLocation();
        if (dir == null){
            finished = true;
            return null;
        }

        MapLocation nextPos = pos.add(dir, r);

        if (rc.canMove(dir)){
            Direction newProDir = getExtremalBulletDirection(rc, nextPos, left);
            if (newProDir == null) return dir;
            return greedyMove2(rc, newProDir, tries+1, left);
        }

        try{
            if (!rc.onTheMap(nextPos, R)){
                if (left) newLeft = false;
                else newLeft = true;
                finished = true;
                return null;
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        boolean scout = (rc.getType() == RobotType.SCOUT);

        RobotInfo[] Ri = rc.senseNearbyRobots(nextPos, R, null);

        Direction currentProDir = null;

        MapLocation m = null;

        for (RobotInfo ri : Ri){
            if (ri.getID() == rc.getID()) continue;
            MapLocation m2 = ri.getLocation();
            float angle = Mates.getAngle(pos.distanceTo(m2), r, R + ri.getType().bodyRadius)+ 0.001f;
            Direction newProDir = null;
            if (newLeft) newProDir = pos.directionTo(m2).rotateLeftRads(angle);
            else newProDir = pos.directionTo(m2).rotateRightRads(angle);

            if (Mates.cclockwise(newProDir, currentProDir, dir, newLeft)){
                currentProDir = newProDir;
                m = m2;
            }
        }

        if (currentProDir != null) {
            nextPos = pos.add(currentProDir, r);
            dir = currentProDir;
        }

        if (!scout) {
            TreeInfo[] Ti = rc.senseNearbyTrees(nextPos, R, null);
            for (TreeInfo ti : Ti) {
                MapLocation m2 = ti.getLocation();
                float angle = Mates.getAngle(pos.distanceTo(m2), r, R + ti.getRadius()) + 0.001f;
                Direction newProDir = null;
                if (newLeft) newProDir = pos.directionTo(m2).rotateLeftRads(angle);
                else newProDir = pos.directionTo(m2).rotateRightRads(angle);

                if (Mates.cclockwise(newProDir, currentProDir, dir, newLeft)) {
                    currentProDir = newProDir;
                    m = m2;
                }
            }
        }

        if (currentProDir != null) {
            nextPos = pos.add(currentProDir, r);
            dir = currentProDir;
        }

        Direction newProDir = getExtremalBulletDirection(rc, nextPos, newLeft);
        if (Mates.cclockwise(newProDir, currentProDir, dir, newLeft)){
            currentProDir = newProDir;
        }


        if (currentProDir == null){
            newLeft  = !newLeft;
            finished = true;
            return null;
        }


        newObstacle = m;

        try {
            rc.setIndicatorDot(rc.getLocation().add(currentProDir, 2.0f), 0, 0, 255);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return greedyMove2(rc, currentProDir, tries+1, left);
    }*/

    public static Direction greedyMove(RobotController rc, Direction dir){

        int inter = 0;

        float r = rc.getType().strideRadius;
        float rr = r*r;
        float R = rc.getType().bodyRadius;
        MapLocation pos = rc.getLocation();
        boolean scout = (rc.getType() == RobotType.SCOUT);

        RobotInfo[] Ri = rc.senseNearbyRobots(pos, R + r, null);

        int cont = 0;

        TreeInfo Ti[] = new TreeInfo[0];
        if (!scout){
            Ti = rc.senseNearbyTrees(pos, R + r, null);
        }

        System.out.println("PRecomputation" + Clock.getBytecodeNum());

        intervals = new int[2*Ti.length + 2*Ri.length];

        for (RobotInfo ri : Ri){
            if (ri.getID() == rc.getID()) continue;
            MapLocation m2 = ri.getLocation();
            Direction dir2 = pos.directionTo(m2);

            float l = (R+ri.getType().bodyRadius);

            float angle = (float)Math.acos((pos.distanceSquaredTo(m2) + rr - l*l)/(2.0f * pos.distanceTo(m2)*r))+ 0.001f;

            float x = dir.radiansBetween(dir2.rotateLeftRads(angle));
            if (x < 0) x+= Constants.PI2;
            intervals[inter] = (Math.round(x*Constants.ANGLEFACTOR) << (Constants.NUMELEMENTS + 2)) + 4*(inter&0x1F);
            ++inter;
            float y = dir.radiansBetween(dir2.rotateRightRads(angle));
            if (y < 0) y+= Constants.PI2;
            intervals[inter] = (Math.round(y*Constants.ANGLEFACTOR) << (Constants.NUMELEMENTS + 2)) + 2 + 4*(inter&0x1F);
            ++inter;
            if (y > x) ++cont;
        }

        if (!scout) {
            for (TreeInfo ti : Ti) {
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


        if (left != 0){
            Direction di = bestDirection(rc, dir, (left > 0), 0, cont, 0);
            if (newObs < 2*Ri.length) obstacle = Ri[newObs/2].getLocation();
            else obstacle = Ti[newObs/2 - Ri.length].getLocation();
            return di;
        } else{
            Direction di1 =  bestDirection(rc, dir, true, 1, cont, 0);
            int aux = newObs;
            Direction di2 =  bestDirection(rc, dir, false, 1, cont, 0);
            if (di1 != null && pos.add(di1, r).distanceTo(target) < pos.add(di2, r).distanceTo(target)){
                newObs = aux;
                di2 = di1;
                left = 1;
            }
            if (newObs < 2*Ri.length) obstacle = Ri[newObs/2].getLocation();
            else obstacle = Ti[newObs/2 - Ri.length].getLocation();
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
        if (l) left = 1;
        else left = -1;
        Arrays.sort(intervals);

        System.out.println("Postsort" + intervals.length+ " " + Clock.getBytecodeNum());

        Direction ans = null;
        int minBulletcont = 999;

        int k = 0;

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
                    newObs = (k >> 2)&0x1F;
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
                newObs = (k >> 2)&0x1F;
                return ans;
            }
            else{
                return bestDirection(rc, dir, !l, tries + 1, cont, contBullets);
            }
        }
        return null;
    }


    public static Direction getExtremalBulletDirection(RobotController rc, MapLocation nextPos, boolean l){

        MapLocation m = nextPos;
        BulletInfo[] Bi = rc.senseNearbyBullets(nextPos, rc.getType().bodyRadius + Constants.MAXBULLETSPEED);

        MapLocation pos = rc.getLocation();

        Direction dir = pos.directionTo(nextPos);

        Direction ans = null;


        int cont = 0;
        for (BulletInfo bi : Bi) {
            System.out.println("Bytecode1: " + cont + " " +  Clock.getBytecodeNum());
            ++cont;
            Direction newProDir = Mates.extremeBulletDirection(dir, pos, m, rc.getType().strideRadius, rc.getType().bodyRadius , bi, l);
            System.out.println("Bytecode2: " + Clock.getBytecodeNum());
            if (Mates.cclockwise(newProDir, ans , dir, l)){
                ans = newProDir;
                if (ans != null){
                    System.out.println("heyaa");
                    dir = ans;
                    m = pos.add(dir, rc.getType().strideRadius);
                }
            }
        }
        return ans;
    }

}
