package simpleplayer;

import battlecode.common.*;
import java.util.*;

/**
 * Created by Ivan on 1/15/2017.
 */
public class Greedy {

    static final int greedyTries = 6;

    static boolean newLeft;
    static MapLocation newObstacle;
    static boolean finished;
    static MapLocation obstacle = null;
    static float minDistToTarget = Constants.INF;
    static boolean left = true;

    static HashMap<Integer, Integer> collisionLocations;

    static BulletInfo[] bullets;



    static void moveGreedy(RobotController rc, MapLocation target){
        if (target == null) return;
        try {
            //SABER SI ES FA GREEDY O ES VA DIRECTE
            MapLocation pos = rc.getLocation();
            float stride = rc.getType().strideRadius;
            Direction dirObstacle = null;
            bullets = rc.senseNearbyBullets(Constants.BULLETSIGHT);
            if (obstacle != null){
                if (!rc.canSenseAllOfCircle(obstacle, rc.getType().bodyRadius)) obstacle = null;
                else if (!rc.onTheMap(obstacle, rc.getType().bodyRadius)) obstacle = null;
                else if (!rc.isCircleOccupiedExceptByThisRobot(obstacle, rc.getType().bodyRadius)) obstacle = null;
            }
            if (obstacle == null) {
                Direction dir = pos.directionTo(target);
                if (rc.canMove(target)){
                    float str = rc.getType().strideRadius;
                    float d = pos.distanceTo(target);
                    if (str < d) {
                        Direction dirG = greedyMove(rc, dir, d, left);
                        if (rc.canMove(dirG)) {
                            rc.move(dirG);
                            obstacle = newObstacle;
                            left = newLeft;
                        }
                    } else {
                        Direction dirG = greedyMove(rc, dir, str, left);
                        if (rc.canMove(dirG)) {
                            rc.move(dirG);
                            obstacle = newObstacle;
                            left = newLeft;
                        }
                    }
                }
                else{
                    dirObstacle = dir;
                }
            }
            else dirObstacle = pos.directionTo(obstacle);
            if (minDistToTarget == Constants.INF) { //PRIMER COP GREEDY
                collisionLocations = new HashMap<>();
                minDistToTarget = pos.distanceTo(target);
                Direction dir1 = Greedy.greedyMove(rc, dirObstacle, 0, left);
                obstacle = Greedy.newObstacle;
                Direction dir2 = Greedy.greedyMove(rc, dirObstacle, 0, !left);
                MapLocation nextPos1 = null;
                float dist1 = Constants.INF;
                if (dir1 != null){
                    nextPos1 = pos.add(dir1, stride);
                    dist1 = nextPos1.distanceTo(target);
                }
                MapLocation nextPos2 = null;
                float dist2 = Constants.INF;
                if (dir2 != null){
                    nextPos2 = pos.add(dir2, stride);
                    dist2 = nextPos2.distanceTo(target);
                }

                if (dir2 != null &&  dist2 < dist1 && rc.canMove(dir2)){
                    left = !left;
                    obstacle = Greedy.newObstacle;
                    addCollisionLocation(rc, left);
                    rc.move(dir2);
                }
                else if (dir1 != null && rc.canMove(dir1)){
                    addCollisionLocation(rc, left);
                    rc.move(dir1);
                }
            } else { //GREEDY GENERAL
                Direction dir = pos.directionTo(target);
                float dist = pos.distanceTo(target);
                if (dist < rc.getType().strideRadius && rc.canMove(target)){  //SHIIIT
                    resetObstacle();
                    float str = rc.getType().strideRadius;
                    float d = pos.distanceTo(target);

                    if (str < d) {
                        Direction dirG = greedyMove(rc, dir, d, left);
                        if (rc.canMove(dirG)) {
                            rc.move(dirG);
                            obstacle = newObstacle;
                            left = newLeft;
                        }
                    } else {
                        Direction dirG = greedyMove(rc, dir, str, left);
                        if (rc.canMove(dirG)) {
                            rc.move(dirG);
                            obstacle = newObstacle;
                            left = newLeft;
                        }
                    }
                }
                if (dist < minDistToTarget && rc.canMove(dir)){ //SHIIIIIT
                    resetObstacle();
                    rc.move(dir);
                    return;
                }
                System.out.println(Clock.getBytecodeNum());
                Direction dirGreedy = Greedy.greedyMove(rc, dirObstacle, 0, left);
                System.out.println(Clock.getBytecodeNum());
                if (dirGreedy != null){
                    obstacle = Greedy.newObstacle;
                    if (dist < minDistToTarget) minDistToTarget = dist;
                    addCollisionLocation(rc, left);
                    rc.move(dirGreedy);
                } else if (Greedy.newLeft != left){
                    left = Greedy.newLeft;
                    dirGreedy = Greedy.greedyMove(rc, dirObstacle, 0, left);
                    if (dirGreedy != null) {
                        obstacle = Greedy.newObstacle;
                        if (dist < minDistToTarget) minDistToTarget = dist;
                        addCollisionLocation(rc, left);
                        rc.move(dirGreedy);
                    } else if (!Greedy.finished){
                        if (Greedy.newObstacle != null) obstacle = Greedy.newObstacle;
                    }
                } else if (!Greedy.finished){
                    if (Greedy.newObstacle != null) obstacle = Greedy.newObstacle;
                }

            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

    }

    static void resetObstacle(){
        obstacle = null;
        minDistToTarget = Constants.INF;
    }


    public static void addCollisionLocation(RobotController rc, boolean left){
        MapLocation pos = rc.getLocation();
        MapLocation collisionPos = pos.add(pos.directionTo(obstacle), Constants.COLLISIONDIST);
        int x = Math.round(collisionPos.x/Constants.COLLISIONRANGE), y = Math.round(collisionPos.y/Constants.COLLISIONRANGE);
        int a = 0;
        if (left) a = 1;
        int hash = 2*(x*Constants.COLLISIONHASH + y)+a;
        if (!collisionLocations.containsKey(hash)){
            collisionLocations.put(hash, rc.getRoundNum());
        } else {
            int round = collisionLocations.get(hash);
            if (rc.getRoundNum() - round > Constants.COLLISIONROUND){
                resetObstacle();
            }
        }
    }



    public static Direction greedyMove(RobotController rc, Direction dir, int tries, boolean left){
        if (tries == 0){
            finished = false;
        }
        newLeft = left;
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
        if (rc.canMove(dir)) return dir;

        MapLocation nextPos = pos.add(dir, r);

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

        BulletInfo[] Bi = rc.senseNearbyBullets(nextPos, R + Constants.MAXBULLETSPEED);

        for (BulletInfo bi : Bi) {
            Direction newProDir = Mates.extremeBulletDirection(dir, pos, r, R, bi, left);
            if (Mates.cclockwise(newProDir, currentProDir, dir, left)){
                currentProDir = newProDir;
                m = bi.getLocation();
            }
        }


        if (currentProDir == null){
            newLeft  = !newLeft;
            finished = true;
            return null;
        }


        newObstacle = m;

        try {
            //rc.setIndicatorDot(rc.getLocation().add(currentProDir, 2.0f), 0, 0, 255);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return greedyMove(rc, currentProDir, tries+1, left);
    }

    public static boolean canMoveWithBullers(RobotController rc, Direction dir){
        if (dir == null) return false;
        MapLocation pos = rc.getLocation();
        pos = pos.add(dir, rc.getType().strideRadius);
        if (!rc.canMove(dir)) return false;
        for (BulletInfo bullet : bullets){
            MapLocation loc = bullet.getLocation();
            Direction bulletDir = bullet.getDir();
            float v = bullet.getSpeed();
            loc = loc.add(bulletDir, v/2);
            if (pos.distanceTo(loc) < v/2 + rc.getType().bodyRadius + Constants.eps) return false;
        }
        return true;
    }


}