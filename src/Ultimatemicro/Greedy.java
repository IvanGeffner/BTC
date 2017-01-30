package Ultimatemicro;

import battlecode.common.*;

import java.util.Arrays;
import java.util.HashMap;

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
    static int trueBullets;

    static int bulletDodge = -10; //Ultim torn on has esquivat una bala

    static int inter;

    static RobotInfo[] sortedEnemies;



    static MapLocation pos;
    static float R = -1, r = -1, rr;
    static int cont, contBullets;
    static MapLocation target;
    static Direction dir;

    static RobotInfo[] Ri;
    static TreeInfo[] Ti;
    static BulletInfo[] bullets;
    static RobotInfo[] RiE;



    static void moveToSelf(RobotController rc, int bytecodeleft){
        moveGreedy(rc, rc.getLocation(), bytecodeleft);
    }

    static void stop(RobotController rc, int bytecodeleft){
        int auxLeft = left;
        MapLocation auxObstacle = obstacle;
        MapLocation auxPos = rc.getLocation();
        float auxminDistToTarget = minDistToTarget;
        HashMap<Integer, Integer> auxCollisionLocations = collisionLocations;

        moveToSelf(rc, bytecodeleft);

        if (rc.getLocation().distanceTo(auxPos) < Constants.eps){
            left = auxLeft;
            obstacle = auxObstacle;
            minDistToTarget = auxminDistToTarget;
            collisionLocations = auxCollisionLocations;
        }


    }

    static void changeTarget(MapLocation tar, RobotController rc){
        if (target != null && tar != null && target.distanceTo(tar) < Constants.eps) return;
        target = tar;
        if (target != null && tar != null && !(rc.getType() == RobotType.GARDENER) && !(rc.getType() == RobotType.LUMBERJACK)){
            if (target.distanceTo(tar) < 1.5f){
                if (!rc.canMove(tar)){
                    minDistToTarget = rc.getLocation().distanceTo(tar);
                    return;
                }
            }
        }
        Greedy.resetObstacle(rc);
    }



    static void moveGreedy(RobotController rc, MapLocation tar, int bytecodeleft){
        changeTarget(tar, rc);
        if (target == null) return;

        //System.out.println("heyaa");

        try {
            pos = rc.getLocation();
            if (R < 0) {
                R = rc.getType().bodyRadius;
                r = rc.getType().strideRadius;
                rr = r * r;
            }

            //SI L'OBSTACLE JA NO HI ES, RESET!
            if (obstacle != null) {
                MapLocation nextPos = pos.add(pos.directionTo(obstacle), r);
                if (!rc.onTheMap(nextPos, rc.getType().bodyRadius)) resetObstacle(rc);
                else if (!rc.isCircleOccupiedExceptByThisRobot(nextPos, rc.getType().bodyRadius)) resetObstacle(rc);
            }


            //SI BAIXAEM DISTMIN I ENS PODEM MOURE AL TARGET, RESET [I EL GREEDY HI ANIRA]
            float dist = pos.distanceTo(target);
            //System.out.println(dist);
            if (left != 0 && dist < minDistToTarget && rc.canMove(target)) {
                resetObstacle(rc);
            }

            //ESCOLLIM LA DIRECCIO PRINCIPAL
            if (obstacle == null) dir = pos.directionTo(target);
            else dir = pos.directionTo(obstacle);

            //TRIEM I FEM EL GREEDY
            Ri = rc.senseNearbyRobots(R + r, rc.getTeam());
            RiE = rc.senseNearbyRobots(R+r+ Constants.SAFETYDISTANCE, rc.getTeam().opponent());
            Ti = new TreeInfo[0];
            if (!(rc.getType() == RobotType.SCOUT)) {
                Ti = rc.senseNearbyTrees(R + r, null);
            }
            getBullets(rc);

            Direction dirGreedy;
            float expectedByteCode = Clock.getBytecodeNum();
            expectedByteCode += Ri.length * Constants.COSTCYCLE1 + (Constants.COSTSORT + Constants.COSTSELECTION) * 2 * Ri.length;
            expectedByteCode += 2*RiE.length * Constants.COSTCYCLE1 + (Constants.COSTSORT + Constants.COSTSELECTION) * 4 * RiE.length;
            expectedByteCode += Ti.length * Constants.COSTCYCLE1 + (Constants.COSTSORT + Constants.COSTSELECTION) * 2 * Ti.length;
            expectedByteCode += bullets.length * Constants.COSTCYCLE2 + (Constants.COSTSORT + Constants.COSTSELECTION) * 2 * bullets.length;

            if (expectedByteCode < bytecodeleft) dirGreedy = greedyStep(rc, bytecodeleft);
            else if (expectedByteCode + (Constants.COSTCYCLE1 - Constants.COSTCYCLE2 - (Constants.COSTSORT + Constants.COSTSELECTION))*bullets.length  < bytecodeleft) dirGreedy = greedyStepLowBytecode(rc, bytecodeleft);
            else dirGreedy = greedySuperLowBytecode(rc, dir, bytecodeleft, 0);

            //dirGreedy = greedySuperLowBytecode(rc, dir, bytecodeleft, 0);
            //dirGreedy = greedyStepLowBytecode(rc, bytecodeleft);

            boolean shoot = true;
            if (rc.getType() == RobotType.SOLDIER || rc.getType() == RobotType.TANK){
                sortEnemies(rc);
                shoot = false;
            }

            if (!shoot && dirGreedy != null){
                if (sortedEnemies.length > 0){
                    MapLocation enemyLoc = sortedEnemies[0].getLocation();
                    if (moveSafely(rc, rc.getLocation.directionTo(enemyLoc), dirGreedy)) shoot = Shoot.tryShoot(rc, 1);
                }
            }

            //if (obstacle != null && rc.senseRobotAtLocation(obstacle) != null) Communication.sendMessage(rc, Communication.STOPCHANNEL, Math.round(obstacle.x), Math.round(obstacle.y), 0);


            if (dirGreedy == null) {
                if (!shoot) shoot = Shoot.tryShoot(rc, 1);
                return;
            }

            //System.out.println(minDistToTarget);
            //ACTUALITZEM
            if (left != 0) {
                //System.out.println(minDistToTarget);
                if (dist < minDistToTarget) minDistToTarget = dist;
                addCollisionLocation(rc);
            }

            //System.out.println(minDistToTarget);

            //if (!shouldMove) System.out.println("STAYY STILLL");

            //SI EL TARGET ESTA MES A PROP, INTENTEM ANAR DIRECTE
            if (!shouldMove && rc.canMove(target)){
                resetObstacle(rc);
                rc.move(target);
                if (!shoot){
                    sortEnemies(rc);
                    shoot = Shoot.tryShoot(rc, 2);
                }
                return;
            }
            //ELSE ANEM EN LA DIRECCIO
            if (rc.canMove(dirGreedy)){
                rc.move(dirGreedy);
                //Ivan, aixo em peta quan no puc fer el rc.senserobot
                if (obstacle != null && rc.canSenseLocation(obstacle)) {
                    RobotInfo r = rc.senseRobotAtLocation(obstacle);
                    if (r != null && r.getTeam() == rc.getTeam()) {
                        Communication.sendMessage(rc, Communication.STOPCHANNEL, Math.round(obstacle.x), Math.round(obstacle.y), 0);
                    }
                }
                if (!shoot){
                    sortEnemies(rc);
                    shoot = Shoot.tryShoot(rc, 2);
                }
                return;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    static boolean moveSafely (RobotController rc, Direction dir1, Direction dir){
        float a = rc.getType().bodyRadius + GameConstants.BULLET_SPAWN_OFFSET;
        float b = rc.getType().strideRadius;
        float c = rc.getType().bodyRadius;
        float angle = (a*a + b*b - c*c)/(2.0f*a*b);
        angle = (float)Math.acos(angle);

        return (Math.abs(dir1.radiansBetween(dir)) > angle + Constants.pentadAngle2 + Constants.eps);
    }

    static void sortEnemies(RobotController rc){
        int cont = 0;
        int cont2 = 0;
        RobotInfo[] enemies = rc.senseNearbyRobots(-1,rc.getTeam().opponent());
        sortedEnemies = new RobotInfo[enemies.length];
        RobotInfo[] auxEnemies = new RobotInfo[enemies.length];
        for (RobotInfo enemy : enemies){
            RobotType rt = enemy.getType();
            if (rt == RobotType.ARCHON){
                if (rc.getRoundNum() < 500) continue;
                else{
                    auxEnemies[cont2] = enemy;
                    ++cont2;
                }
            }
            else if (rt == RobotType.SCOUT || rt == RobotType.LUMBERJACK){
                auxEnemies[cont2] = enemy;
                ++cont2;
            }
            else {
                sortedEnemies[cont] = enemy;
                ++cont;
            }
        }
        for (int i = 0; i < cont2; ++i) sortedEnemies[cont+i] = auxEnemies[i];
        if (cont + cont2 < enemies.length) sortedEnemies = Arrays.copyOf(sortedEnemies, cont + cont2);
    }

    static void getBullets(RobotController rc){
        bullets = rc.senseNearbyBullets(Constants.BULLETSIGHT + R+r);
        BulletInfo[] bulletAux = new BulletInfo[bullets.length];
        //System.out.println(bullets.length);
        int cont = 0;
        for (BulletInfo bi : bullets){

            //System.out.println(Clock.getBytecodeNum());
            if (bi.getLocation().distanceTo(pos) > r+R + Constants.eps && Math.abs(pos.directionTo(bi.getLocation()).radiansBetween(bi.getDir())) < Math.PI/2) continue;
            if (bi.getLocation().distanceTo(pos) > r+R+ Constants.eps + bi.getSpeed()) continue;
            bulletAux[cont] = bi;
            ++cont;
        }
        bullets = Arrays.copyOf(bulletAux, cont);
    }

    static void resetObstacle(RobotController rc){
        //System.out.println ("OLLLSLLSA");
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

    public static Direction greedySuperLowBytecode(RobotController rc, Direction mainDir, int bytecodeLeft, int tries){

        if (tries == 0) System.out.println("SUPER LOW BYTECODE");

        if (tries >= Constants.GREEDYTRIES) return null;

        if (tries == 0 && mainDir == null){
            mainDir = new Direction ((float)Math.random(), (float)Math.random());
            shouldMove = false;
        } else if (tries == 0) {
            if (target.distanceTo(pos) < r) shouldMove = false;
            else shouldMove = true;
        }

        if (tries > 0 && rc.canMove(mainDir)) return mainDir;

        obstacle = null;

        MapLocation nextPos = pos.add(mainDir, r);

        RobotInfo[] RI = rc.senseNearbyRobots(nextPos, R, null);

        Direction dirL = mainDir, dirR = mainDir;
        boolean scout = (rc.getType() == RobotType.SCOUT);

        MapLocation obstacleLeft = null, obstacleRight = null;

        for (RobotInfo ri : RI){

            if (Clock.getBytecodeNum() > bytecodeLeft) break;

            if (ri.getID() == rc.getID())continue;
            MapLocation m2 = ri.getLocation();
            Direction dir2 = pos.directionTo(m2);

            float l = (R+ri.getType().bodyRadius);
            float t = (pos.distanceSquaredTo(m2) + rr - l*l)/(2.0f * pos.distanceTo(m2)*r);
            if (t <= 1 && t >= -1) {

                float angle = (float) Math.acos(t) + Constants.eps;
                Direction newDirL = dir2.rotateLeftRads(angle);
                Direction newDirR = dir2.rotateRightRads(angle);
                if (dirL.radiansBetween(newDirL) > 0){
                    dirL = newDirL;
                    obstacleLeft = m2;
                }
                if (dirR.radiansBetween(newDirR) < 0){
                    obstacleRight = m2;
                    dirR = newDirR;
                }
            }
        }

        if (!scout){
            TreeInfo[] TI = rc.senseNearbyTrees(nextPos, R, null);
            for (TreeInfo ti : TI){
                if (Clock.getBytecodeNum() > bytecodeLeft) break;
                MapLocation m2 = ti.getLocation();
                Direction dir2 = pos.directionTo(m2);

                float l = (R+ti.getRadius());
                float t = (pos.distanceSquaredTo(m2) + rr - l*l)/(2.0f * pos.distanceTo(m2)*r);
                if (t <= 1 && t >= -1) {

                    float angle = (float) Math.acos(t) + Constants.eps;
                    Direction newDirL = dir2.rotateLeftRads(angle);
                    Direction newDirR = dir2.rotateRightRads(angle);
                    if (dirL.radiansBetween(newDirL) > 0){
                        dirL = newDirL;
                        obstacleLeft = m2;
                    }
                    if (dirR.radiansBetween(newDirR) < 0){
                        obstacleRight = m2;
                        dirR = newDirR;
                    }
                }
            }
        }

        BulletInfo[] BI = rc.senseNearbyBullets(Constants.BULLETSIGHT);

        float angL = getPositiveAngle(mainDir, dirL), angR = getPositiveAngle(mainDir, dirR);
        if (angR == 0) angR = Constants.PI2;

        for (BulletInfo bi : BI){
            if (Clock.getBytecodeNum() > bytecodeLeft) break;
            MapLocation m2 = bi.getLocation().add(bi.getDir(),(bi.getSpeed()/2));
            Direction dir2 = pos.directionTo(m2);
            float l = (R+bi.getSpeed()/2);

            if (!shouldMove && target.distanceTo(m2) <= l) shouldMove = true;

            //if (nextPos.distanceTo(m2) > l) continue;


            float t = (pos.distanceSquaredTo(m2) + rr - l*l)/(2.0f * pos.distanceTo(m2)*r);
            if (t <= 1 && t >= -1) {

                float angle = (float) Math.acos(t) + Constants.eps;


                Direction newDirL = dir2.rotateLeftRads(angle);
                Direction newDirR = dir2.rotateRightRads(angle);


                float newAngL = getPositiveAngle(mainDir, newDirL);
                float newAngR = getPositiveAngle(mainDir, newDirR);

                if (newAngL > angL){
                    if (newAngL < angR) {
                        angL = newAngL;
                        dirL = newDirL;
                    }
                }

                if (newAngR < angR){
                    if (newAngR > angL){
                        angR = newAngR;
                        dirR = newDirR;

                    }
                }
            }
        }

        if (left == 0) {
            if (Clock.getBytecodeNum() > bytecodeLeft) return dirL;
            if (Math.abs(dirL.radiansBetween(mainDir)) < Constants.eps && Math.abs(dirL.radiansBetween(mainDir)) < Constants.eps) return dirR;
            left = -1;
            Direction UltimateRight = greedySuperLowBytecode(rc, dirR, bytecodeLeft, tries + 1);
            MapLocation auxObs;
            if (obstacle == null) auxObs = obstacleRight;
            else auxObs = obstacle;
            left = 1;
            Direction UltimateLeft = greedySuperLowBytecode(rc, dirL, bytecodeLeft, tries + 1);
            if (obstacle == null) obstacle = obstacleLeft;
            if (UltimateLeft != null) {
                if (UltimateRight == null || !rc.canMove(UltimateRight) || pos.add(UltimateLeft, r).distanceTo(target) < pos.add(UltimateRight, r).distanceTo(target))
                    return UltimateLeft;
                obstacle = auxObs;
                left = -1;
                return UltimateRight;
            }
            obstacle = auxObs;
            left = -1;
            return UltimateRight;

        } else if (left == -1) {
            if (Clock.getBytecodeNum() > bytecodeLeft) return dirR;
            if (Math.abs(dirR.radiansBetween(mainDir)) < Constants.eps) {
                if (rc.canMove(dirR)) {
                    obstacle = obstacleRight;
                    return dirR;
                } else return null;
            } else {
                Direction UltimateRight = greedySuperLowBytecode(rc, dirR, bytecodeLeft, tries + 1);
                if (UltimateRight != null && rc.canMove(UltimateRight)) {
                    if (obstacle == null) obstacle = obstacleRight;
                    return UltimateRight;
                } else{
                    left = 1;
                    Direction UltimateLeft = greedySuperLowBytecode(rc, dirL, bytecodeLeft, tries + 1);
                    if (UltimateLeft != null && rc.canMove(UltimateLeft)) {
                        if (obstacle == null) obstacle = obstacleLeft;
                    }
                    return UltimateLeft;
                }
            }
        } else {
            if (Clock.getBytecodeNum() > bytecodeLeft) return dirL;
            if (Math.abs(dirL.radiansBetween(mainDir)) < Constants.eps) {
                if (rc.canMove(dirL)) {
                    obstacle = obstacleLeft;
                    return dirL;
                } else return null;
            } else {
                Direction UltimateLeft = greedySuperLowBytecode(rc, dirL, bytecodeLeft, tries + 1);
                if (UltimateLeft != null && rc.canMove(UltimateLeft)) {
                    if (obstacle == null) obstacle = obstacleLeft;
                    return UltimateLeft;
                } else {
                    left = -1;
                    Direction UltimateRight = greedySuperLowBytecode(rc, dirR, bytecodeLeft, tries + 1);
                    if (UltimateRight != null && rc.canMove(UltimateRight)) {
                        if (obstacle == null) obstacle = obstacleRight;
                    }
                    return UltimateRight;
                }
            }
        }
    }

    public static float getPositiveAngle(Direction A, Direction B){
        float a = A.radiansBetween(B);
        if (a < 0) a += Constants.PI2;
        return a;
    }

    public static Direction greedyStep(RobotController rc, int bytecodeLeft){

        System.out.println("LOW BYTECODEE!!");


        if (dir == null){
            dir = new Direction ((float)Math.random(), (float)Math.random());
            shouldMove = false;
        } else{
            if (pos.distanceTo(target) < r) shouldMove = false;
            else shouldMove = true;
        }

        inter = 0; //position at intervals

        int a = 1;
        if (left == 0) ++a;

        MapLocation pos = rc.getLocation();
        boolean scout = (rc.getType() == RobotType.SCOUT);

        cont = 0;
        contBullets = 0;

        //System.out.println("PRecomputation" + Clock.getBytecodeNum());

        int maxLength = 2*Ti.length + 2*Ri.length + 4*RiE.length + 4*bullets.length;

        intervals = new int[maxLength];
        obstacles = new int[maxLength];

        int obstacleCount = -1;

        for (RobotInfo ri : Ri){
            ++obstacleCount;
            if (Clock.getBytecodeNum() + Constants.COSTCYCLE1 + inter*(Constants.COSTSORT + a* Constants.COSTSELECTION) >= bytecodeLeft) break;
            if (ri.getID() == rc.getID()) continue;

            //CALCULA INTERVAL
            MapLocation m2 = ri.getLocation();
            Direction dir2 = pos.directionTo(m2);

            float l = (R+ri.getType().bodyRadius);

            float t = (pos.distanceSquaredTo(m2) + rr - l*l)/(2.0f * pos.distanceTo(m2)*r);

            if (t <= 1 && t >= -1) {

                float angle = (float) Math.acos(t) + Constants.eps;


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

        for (RobotInfo ri : RiE){
            ++obstacleCount;
            if (Clock.getBytecodeNum() + 2* Constants.COSTCYCLE1 + inter*(Constants.COSTSORT + a* Constants.COSTSELECTION) >= bytecodeLeft) break;
            if (ri.getID() == rc.getID()) continue;

            //CALCULA INTERVAL
            MapLocation m2 = ri.getLocation();
            Direction dir2 = pos.directionTo(m2);

            float l = (R+ri.getType().bodyRadius);

            float t = (pos.distanceSquaredTo(m2) + rr - l*l)/(2.0f * pos.distanceTo(m2)*r);

            if (t <= 1 && t >= -1) {

                float angle = (float) Math.acos(t) + Constants.eps;


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

            float safe = Constants.safetyDistance(ri.getType());
            if (safe > 0){
                l += safe;
                t = (pos.distanceSquaredTo(m2) + rr - l*l)/(2.0f * pos.distanceTo(m2)*r);

                if (t <= 1 && t >= -1) {

                    float angle = (float) Math.acos(t) + Constants.eps;


                    float x = dir.radiansBetween(dir2.rotateLeftRads(angle));
                    if (x < 0) x += Constants.PI2;
                    intervals[inter] = (Math.round(x * Constants.ANGLEFACTOR) << (Constants.NUMELEMENTS + 2)) +1+ 4 * (inter & 0x1F);
                    obstacles[inter] = obstacleCount;
                    ++inter;
                    float y = dir.radiansBetween(dir2.rotateRightRads(angle));
                    if (y < 0) y += Constants.PI2;
                    intervals[inter] = (Math.round(y * Constants.ANGLEFACTOR) << (Constants.NUMELEMENTS + 2)) + 3 + 4 * (inter & 0x1F);
                    obstacles[inter] = obstacleCount;
                    ++inter;
                    if (y > x) ++contBullets;
                }
            }

        }

        obstacleCount = Ri.length + RiE.length - 1;

        if (!scout) {
            for (TreeInfo ti : Ti) {
                ++obstacleCount;
                if (Clock.getBytecodeNum() + Constants.COSTCYCLE1 + inter*(Constants.COSTSORT + a* Constants.COSTSELECTION) >= bytecodeLeft) break;
                if (ti.getID() == rc.getID()) continue;

                MapLocation m2 = ti.getLocation();
                Direction dir2 = pos.directionTo(m2);

                float l = (R+ti.getRadius());

                float t = (pos.distanceSquaredTo(m2) + rr - l*l)/(2.0f * pos.distanceTo(m2)*r);

                if (t <= 1 && t >= -1) {

                    float angle = (float) Math.acos(t) + Constants.eps;

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

        for (BulletInfo bul : bullets) {
            if (Clock.getBytecodeNum() + Constants.COSTCYCLE2 + inter*(Constants.COSTSORT + a* Constants.COSTSELECTION) >= bytecodeLeft) break;

            addIntervalsImproved(bul);

        }

        if (inter < maxLength){
            intervals = Arrays.copyOf(intervals, inter);
            obstacles = Arrays.copyOf(obstacles, inter);
        }

        //System.out.println("PRE-SORT: " + intervals.length + "  " + Clock.getBytecodeNum() );
        if (intervals.length > 1) IfSorting.quickSortOnly(intervals);
        //System.out.println("POST-SORT: " + intervals.length + "  " + Clock.getBytecodeNum() );
        //Arrays.sort(intervals);

        newObs = -1;

        if (left != 0){
            Direction di = bestDirection(rc, (left > 0), 0);
            if (newObs >= 0) {
                if (newObs < Ri.length) obstacle = Ri[newObs].getLocation();
                else {
                    newObs -= Ri.length;
                    if(newObs < RiE.length){
                        obstacle = RiE[newObs].getLocation();
                    } else {
                        if (newObs < Ti.length) obstacle = Ti[newObs].getLocation();
                        else bulletDodge = rc.getRoundNum();
                    }
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
                    if(newObs < RiE.length){
                        obstacle = RiE[newObs].getLocation();
                    } else {
                        if (newObs < Ti.length) obstacle = Ti[newObs].getLocation();
                        else bulletDodge = rc.getRoundNum();
                    }
                }
            }
            return di2;
        }
    }

    public static Direction greedyStepLowBytecode(RobotController rc, int bytecodeLeft){

       //System.out.println("SUPER HIGH BYTECODEE!!");


        if (dir == null){
            dir = new Direction ((float)Math.random(), (float)Math.random());
            shouldMove = false;
        } else {
            if (pos.distanceTo(target) < r) shouldMove = false;
            else shouldMove = true;
        }

        inter = 0; //position at intervals

        int a = 1;
        if (left == 0) ++a;

        MapLocation pos = rc.getLocation();
        boolean scout = (rc.getType() == RobotType.SCOUT);

        cont = 0;

        //System.out.println("PRecomputation" + Clock.getBytecodeNum());

        int maxLength = 2*Ti.length + 2*Ri.length + 4*RiE.length + 2*bullets.length;

        intervals = new int[maxLength];
        obstacles = new int[maxLength];

        int obstacleCount = -1;

        for (RobotInfo ri : Ri){
            ++obstacleCount;
            if (Clock.getBytecodeNum() + Constants.COSTCYCLE1 + inter*(Constants.COSTSORT + a* Constants.COSTSELECTION) >= bytecodeLeft) break;
            if (ri.getID() == rc.getID()) continue;

            //CALCULA INTERVAL
            MapLocation m2 = ri.getLocation();
            Direction dir2 = pos.directionTo(m2);

            float l = (R+ri.getType().bodyRadius);

            float t = (pos.distanceSquaredTo(m2) + rr - l*l)/(2.0f * pos.distanceTo(m2)*r);

            if (t <= 1 && t >= -1) {

                float angle = (float) Math.acos(t) + Constants.eps;


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
        contBullets = 0;

        obstacleCount = Ri.length - 1;

        for (RobotInfo ri : RiE){
            ++obstacleCount;
            if (Clock.getBytecodeNum() + 2* Constants.COSTCYCLE1 + inter*(Constants.COSTSORT + a* Constants.COSTSELECTION) >= bytecodeLeft) break;
            if (ri.getID() == rc.getID()) continue;

            //CALCULA INTERVAL
            MapLocation m2 = ri.getLocation();
            Direction dir2 = pos.directionTo(m2);

            float l = (R+ri.getType().bodyRadius);

            float t = (pos.distanceSquaredTo(m2) + rr - l*l)/(2.0f * pos.distanceTo(m2)*r);

            if (t <= 1 && t >= -1) {

                float angle = (float) Math.acos(t) + Constants.eps;


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

            float safe = Constants.safetyDistance(ri.getType());
            if (safe > 0){
                l += safe;
                t = (pos.distanceSquaredTo(m2) + rr - l*l)/(2.0f * pos.distanceTo(m2)*r);

                if (t <= 1 && t >= -1) {

                    float angle = (float) Math.acos(t) + Constants.eps;


                    float x = dir.radiansBetween(dir2.rotateLeftRads(angle));
                    if (x < 0) x += Constants.PI2;
                    intervals[inter] = (Math.round(x * Constants.ANGLEFACTOR) << (Constants.NUMELEMENTS + 2)) +1+ 4 * (inter & 0x1F);
                    obstacles[inter] = obstacleCount;
                    ++inter;
                    float y = dir.radiansBetween(dir2.rotateRightRads(angle));
                    if (y < 0) y += Constants.PI2;
                    intervals[inter] = (Math.round(y * Constants.ANGLEFACTOR) << (Constants.NUMELEMENTS + 2)) + 3 + 4 * (inter & 0x1F);
                    obstacles[inter] = obstacleCount;
                    ++inter;
                    if (y > x) ++contBullets;
                }
            }

        }

        obstacleCount = Ri.length + RiE.length - 1;

        if (!scout) {
            for (TreeInfo ti : Ti) {
                ++obstacleCount;
                if (Clock.getBytecodeNum() + Constants.COSTCYCLE1 + inter*(Constants.COSTSORT + a* Constants.COSTSELECTION) >= bytecodeLeft) break;
                if (ti.getID() == rc.getID()) continue;

                MapLocation m2 = ti.getLocation();
                Direction dir2 = pos.directionTo(m2);

                float l = (R+ti.getRadius());

                float t = (pos.distanceSquaredTo(m2) + rr - l*l)/(2.0f * pos.distanceTo(m2)*r);

                if (t <= 1 && t >= -1) {

                    float angle = (float) Math.acos(t) + Constants.eps;

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

        for (BulletInfo bul : bullets) {
            if (Clock.getBytecodeNum() + Constants.COSTCYCLE1 + inter*(Constants.COSTSORT + a* Constants.COSTSELECTION) >= bytecodeLeft) break;

            MapLocation m2 = bul.getLocation().add(bul.getDir(), bul.getSpeed() / 2);
            Direction dir2 = pos.directionTo(m2);

            float d = pos.distanceTo(m2);
            float l = (R + bul.getSpeed() / 2);

            if (!shouldMove && target.distanceTo(m2) <= l) shouldMove = true;


            float t = (pos.distanceSquaredTo(m2) + rr - l * l) / (2.0f * d * r);
            if (-1 <= t && t <= 1) {
                float angle = (float) Math.acos(t) + Constants.eps;

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
        if (intervals.length > 1) IfSorting.quickSortOnly(intervals);
        //System.out.println("POST-SORT: " + intervals.length + "  " + Clock.getBytecodeNum() );
        //Arrays.sort(intervals);

        newObs = -1;

        if (left != 0){
            Direction di = bestDirection(rc, (left > 0), 0);
            if (newObs >= 0) {
                if (newObs < Ri.length) obstacle = Ri[newObs].getLocation();
                else {
                    newObs -= Ri.length;
                    if(newObs < RiE.length){
                        obstacle = RiE[newObs].getLocation();
                    } else {
                        if (newObs < Ti.length) obstacle = Ti[newObs].getLocation();
                        else bulletDodge = rc.getRoundNum();
                    }
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
                    if(newObs < RiE.length){
                        obstacle = RiE[newObs].getLocation();
                    } else {
                        if (newObs < Ti.length) obstacle = Ti[newObs].getLocation();
                        else bulletDodge = rc.getRoundNum();
                    }
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

    static void addIntervalsImproved (BulletInfo b){

        //System.out.println("Tractament bala: " + Clock.getBytecodeNum());

        MapLocation m1 = b.getLocation();
        MapLocation m2 = m1.add(b.getDir(),b.getSpeed());
        Direction perp = b.getDir().rotateLeftRads((float) Math.PI / 2);

        //NO IMPORTA
        if (!shouldMove && m1.add(b.getDir(), b.getSpeed()/2).distanceTo(target) <= b.getSpeed()/2 + R) shouldMove = true;

        Direction dirv11 = null, dirv21 = null, dirv12 = null, dirv22 = null;

        Direction Dirm1 = pos.directionTo(m1), Dirm2 = pos.directionTo(m2);
        float distm1 = pos.distanceTo(m1), distm2 = pos.distanceTo(m2);

        float dist = distm1 * (float) Math.cos(perp.radiansBetween(Dirm1));

        float hCoordm1 = distm1*(float) Math.sin(perp.radiansBetween(Dirm1)), hCoordm2 = distm2*(float) Math.sin(perp.radiansBetween(Dirm2));


        float dist1 = dist + R;
        float c = dist1/r;
        if (-1 <= c && c <= 1) {

            float ang = (float) Math.acos(c);
            float hCoord1 = r*(float)Math.sin(ang);
            if (hCoordm1 >= hCoord1 && hCoordm2 <= hCoord1) dirv11 = perp.rotateLeftRads(ang);
            if (hCoordm1 >= -hCoord1 && hCoordm2 <= -hCoord1) dirv21 = perp.rotateRightRads(ang);

        }

        float dist2 = dist-R;

        c = dist2/r;
        if (-1 <= c && c <= 1) {
            float ang = (float) Math.acos(c);
            float hCoord1 = r*(float)Math.sin(ang);
            if (hCoordm1 >= hCoord1 && hCoordm2 <= hCoord1) dirv12 = perp.rotateLeftRads(ang);
            if (hCoordm1 >= -hCoord1 && hCoordm2 <= -hCoord1) dirv22 = perp.rotateRightRads(ang);
        }

        if (dirv11 == null || dirv12 == null) {
            float t = (distm1*distm1 + rr - R * R) / (2.0f * distm1 * r);
            if (-1 <= t && t <= 1) {
                float angle = (float) Math.acos(t);
                float hCoord1 = r*(float)Math.sin(perp.radiansBetween(Dirm1.rotateRightRads(angle)));
                if (hCoord1 >= hCoordm1) dirv11 = Dirm1.rotateRightRads(angle);
                hCoord1 = r*(float)Math.sin(perp.radiansBetween(Dirm1.rotateLeftRads(angle)));
                if (hCoord1 >= hCoordm1) dirv12 = Dirm1.rotateLeftRads(angle);
            }
        }

        if (dirv21 == null || dirv22 == null) {
            float t = (distm2*distm2 + rr - R * R) / (2.0f * distm2 * r);
            if (-1 <= t && t <= 1) {
                float angle = (float) Math.acos(t);
                float hCoord2 = r*(float)Math.sin(perp.radiansBetween(Dirm2.rotateLeftRads(angle)));
                if (hCoord2 <= hCoordm2) dirv21 = Dirm2.rotateLeftRads(angle);
                hCoord2 = r*(float)Math.sin(perp.radiansBetween(Dirm2.rotateRightRads(angle)));
                if (hCoord2 <= hCoordm2) dirv22 = Dirm2.rotateRightRads(angle);
            }
        }

        //System.out.println("Final Tractament bala: " + Clock.getBytecodeNum());


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

    }

    static void addIntervals(BulletInfo b){

        //System.out.println("Bytecode pls1: " + Clock.getBytecodeNum());

        try {


            MapLocation m1 = b.getLocation();
            MapLocation m2 = m1.add(b.getDir(), b.getSpeed());
            Direction perp = b.getDir().rotateLeftRads((float) Math.PI / 2);

            if (!shouldMove && m1.add(b.getDir(), b.getSpeed()/2).distanceTo(target) <= b.getSpeed()/2 + R) shouldMove = true;

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


        //System.out.println("Bytecode pls2: " + Clock.getBytecodeNum());



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
        intervals[inter] = (Math.round(right* Constants.ANGLEFACTOR) << (Constants.NUMELEMENTS + 2)) + 3+ 4*(inter&0x1F);
        obstacles[inter] = Constants.INTINF;
        ++inter;
    }


}
