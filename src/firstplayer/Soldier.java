package firstplayer;
import battlecode.common.*;

import java.util.*;

/**
 * Created by Ivan on 1/9/2017.
 */
public class Soldier {

    //code executed onece at the begining
    static MapLocation[] previousPositions = new MapLocation[Util.greedySteps];
    static MapLocation target;
    static boolean left = true;
    static RobotController rc;

    static ArrayList<Integer> emptyMessages;
    static int emptyMessageIndex;
    static int emptyMessageNumber;
    static int lastMessage; //if last message is at n, this is n+1
    static HashSet<Integer> messagesSeen;
    static MapLocation opponents[];
    static MapLocation[] basis;
    static int xBase;
    static int yBase;
    static boolean stop;
    static boolean free = true;

    @SuppressWarnings("unused")
    public static void run(RobotController rcc) {

        rc = rcc;

        opponents = rc.getInitialArchonLocations(rc.getTeam().opponent());
        resetPositions(previousPositions);
        target = opponents[0];
        basis = rc.getInitialArchonLocations(rc.getTeam());
        xBase = (int)Math.floor(basis[0].x);
        yBase = (int)Math.floor(basis[0].y);
        emptyMessages = new ArrayList<Integer>();
        messagesSeen = new HashSet<Integer>();

        while (true) {
            //code executed continually, don't let it end
            try {

                stop = false;


                readMessages();

                if (stop) System.out.println("STOP!!!!!");
                else System.out.println("Move!! :D");

                moveGreedy();

                tryShoot();

                rc.broadcast(Util.MAX_BROADCAST_MESSAGE, lastMessage);


                Clock.yield();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }

    }


    /*static void tryShoot(){
        MapLocation pos = rc.getLocation();

        float maxUtil = 0;
        float maxUtilTriad = 0;
        float maxUtilPentad = 0;
        Direction dir = null;
        Direction dirTriad = null;
        Direction dirPentad = null;

        RobotInfo[] enemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        int enemiesLength = enemies.length;
        RobotInfo[] allies = rc.senseNearbyRobots(-1, rc.getTeam());
        int alliesLength = allies.length;
        float[] distancesAllies = new float[alliesLength];
        float[] semiAngleAllies = new float[alliesLength];
        Direction[] angleDirs = new Direction[alliesLength];

        TreeInfo[] trees = rc.senseNearbyTrees();
        int treesLength = trees.length;
        float[] distancesTrees = new float[treesLength];
        float[] semiAngleTrees = new float[treesLength];
        Direction[] angleTrees = new Direction[treesLength];


        for (RobotInfo ri : enemies){
            MapLocation m = ri.getLocation();
            float d = pos.distanceTo(ri.getLocation());
            RobotType r = ri.getType();
            if (r == RobotType.SCOUT && d > 5) continue;
            if (r == RobotType.SOLDIER && d > 6) continue;
            float a = (float)Math.asin(ri.getType().bodyRadius/d);
            Direction enemyDir = pos.directionTo(m);
            boolean canShoot = true;
            for (int i = 0; i < alliesLength; ++i){
                if (allies[i].getID() == rc.getID()) continue;
                if (distancesAllies[i] == 0){
                    distancesAllies[i] =pos.distanceTo(allies[i].getLocation());
                    semiAngleAllies[i] = (float)Math.asin(allies[i].getType().bodyRadius/d);
                    angleDirs[i] = pos.directionTo(allies[i].getLocation());
                }
                if (distancesAllies[i] > d) break;

                float angle = enemyDir.radiansBetween(angleDirs[i]);
                if (angle < semiAngleAllies[i]+ a + Util.eps){
                    a = angle - semiAngleAllies[i];
                }


            }

            for (int i = 0; i < treesLength; ++i){
                if (distancesTrees[i] == 0){
                    distancesTrees[i] =pos.distanceTo(trees[i].getLocation());
                    semiAngleTrees[i] = (float)Math.asin(trees[i].getRadius());
                    angleTrees[i] = pos.directionTo(trees[i].getLocation());
                }
                if (distancesTrees[i] > d) break;

                float angle = enemyDir.radiansBetween(angleTrees[i]);
                if (angle < semiAngleTrees[i]+ a + Util.eps){
                    a = angle - semiAngleTrees[i];
                }


            }

            if (a > 0){
                float multiplier = 1;

                if (r == RobotType.SCOUT) multiplier = 0.1f;
                else if (r == RobotType.LUMBERJACK) multiplier = 1.2f;


                float x;
                if (r == RobotType.ARCHON) x = 10;
                else x = r.bulletCost/r.maxHealth;

                float ut = 0;
                float utTriad = 0;
                float utPentad = 0;

                ut = multiplier*x - 1;
                if (a > 20) utTriad = multiplier*x*3 - 4;
                if (a > 15) utPentad = multiplier*x*3 - 6;
                if (a > 30) utPentad = multiplier*x*5 - 6;

                if (ut > maxUtil){
                    dir = enemyDir;
                    maxUtil = ut;
                }

                if (utTriad > maxUtilTriad){
                    dirTriad = enemyDir;
                    maxUtilTriad = utTriad;
                }

                if (utPentad > maxUtilPentad){
                    dirPentad = enemyDir;
                    maxUtilPentad = utPentad;
                }
            }
        }

        try {
            if (maxUtilPentad > 0 && rc.canFirePentadShot()) {
                if (maxUtilPentad > maxUtilTriad) {
                    if (maxUtilPentad > maxUtil) {
                        rc.firePentadShot(dirPentad);
                        return;
                    }
                }
            }
            else if (maxUtilTriad> 0 && rc.canFireTriadShot()) {
                if (maxUtilTriad > maxUtil) {
                    rc.fireTriadShot(dirTriad);
                    return;
                }
            }
            if (maxUtil > 0 && rc.canFireSingleShot()) {
                rc.fireSingleShot(dir);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

    }*/


    static void tryShoot(){

        float maxUtilSingle = 0;
        float maxUtilTriad = 0;
        float maxUtilPentad = 0;
        Direction dirSingle = null;
        Direction dirTriad = null;
        Direction dirPentad = null;

        MapLocation pos = rc.getLocation();
        ArrayList<RobotInfo> rArray = new ArrayList<RobotInfo>();
        rArray.clear();
        RobotInfo[] enemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        int cont = 0;
        for (RobotInfo ri : enemies){
            RobotType a = ri.getType();
            MapLocation m = ri.getLocation();
            float d = pos.distanceTo(m);
            if (a == RobotType.SCOUT && d > 5) continue;
            if (a == RobotType.SOLDIER && d > 6) continue;
            Direction dir = pos.directionTo(m);
            boolean addIt = true;
            for (int i = 0; i < cont && !addIt; ++i){
                MapLocation m2 = rArray.get(i).getLocation();
                if (dir.radiansBetween(pos.directionTo(m2)) < Util.minAngleShoot){
                    addIt = false;
                }
            }
            if (addIt){
                rArray.add(ri);
                ++cont;
                if (cont >= Util.shootsTries) break;
            }
        }

        for (int i = 0; i < cont; ++i){
            RobotInfo ri = rArray.get(i);
            RobotType r = ri.getType();
            MapLocation m = ri.getLocation();


            Direction dir = pos.directionTo(m);

            MapLocation nextPos = pos.add(dir, rc.getType().bodyRadius);
            float d = m.distanceTo(nextPos);

            RobotInfo[] allies = rc.senseNearbyRobots(nextPos, d, rc.getTeam());

            TreeInfo[] trees = rc.senseNearbyTrees(nextPos, d, null);

            float a = (float)Math.asin(r.bodyRadius/d);

            for (RobotInfo ally : allies){
                if (ally.getID() == rc.getID()) continue;
                MapLocation m2 = ally.getLocation();
                Direction dir2 = nextPos.directionTo(m2);

                float d2 = nextPos.distanceTo(m);
                float ang = (float)Math.asin(ally.getType().bodyRadius/d2);

                float angle = dir.radiansBetween(dir2);
                if (angle < ang + a + Util.eps){
                    a = angle - ang;
                }
            }

            for (TreeInfo tree : trees){
                if (tree.getID() == rc.getID()) continue;
                MapLocation m2 = tree.getLocation();
                Direction dir2 = nextPos.directionTo(m2);

                float d2 = nextPos.distanceTo(m);
                float ang = (float)Math.asin(tree.getRadius()/d2);

                float angle = dir.radiansBetween(dir2);
                if (angle < ang + a + Util.eps){
                    a = angle - ang;
                }
            }

            if (a > 0){

                System.out.println(a);
                float multiplier = 1;

                if (r == RobotType.SCOUT) multiplier = 0.2f;
                else if (r == RobotType.LUMBERJACK) multiplier = 1.2f;


                float x;
                if (r == RobotType.ARCHON) x = 10;
                else x = 2.0f*((float)r.bulletCost)/r.maxHealth;

                float ut = 0;
                float utTriad = 0;
                float utPentad = 0;

                ut = x*multiplier - 1;
                if (a > Util.triadAngle) utTriad = multiplier*x*3.0f - 4;
                if (a > Util.pentadAngle) utPentad = multiplier*x*3.0f - 6;
                if (a > Util.pentadAngle2) utPentad = multiplier*x*5.0f - 6;

                if (ut > maxUtilSingle){
                    dirSingle = dir;
                    maxUtilSingle = ut;
                }

                if (utTriad > maxUtilTriad){
                    dirTriad = dir;
                    maxUtilTriad = utTriad;
                }

                if (utPentad > maxUtilPentad){
                    dirPentad = dir;
                    maxUtilPentad = utPentad;
                }
            }

        }

        try {
            if (maxUtilPentad > 0 && rc.canFirePentadShot()) {
                if (maxUtilPentad > maxUtilTriad) {
                    if (maxUtilPentad > maxUtilSingle) {
                        rc.setIndicatorDot(rc.getLocation(), 255,0, 0);
                        rc.setIndicatorDot(rc.getLocation().add(dirPentad), 0,255, 0);
                        rc.firePentadShot(dirPentad);
                        return;
                    }
                }
            }
            else if (maxUtilTriad> 0 && rc.canFireTriadShot()) {
                if (maxUtilTriad > maxUtilSingle) {
                    rc.setIndicatorDot(rc.getLocation(), 255,0, 0);
                    rc.setIndicatorDot(rc.getLocation().add(dirTriad), 0,0, 255);
                    rc.fireTriadShot(dirTriad);
                    return;
                }
            }
            else if (maxUtilSingle > 0 && rc.canFireSingleShot()) {
                rc.setIndicatorDot(rc.getLocation(), 255,0, 0);
                rc.setIndicatorDot(rc.getLocation().add(dirSingle), 120,120, 0);
                rc.fireSingleShot(dirSingle);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }


    }

    static void moveGreedy(){
        try {
            if (stop) return;
            MapLocation pos = rc.getLocation();
            Direction naiveDir = new Direction(pos, opponents[0]);
            if (target == null) target = opponents[0];
            Direction proDir = new Direction(pos, target);
            if (rc.canMove(naiveDir) && !goingBackwards(rc, previousPositions, naiveDir)) {
                rc.move(naiveDir);
                resetPositions(previousPositions);
                target = opponents[0];
                free = true;
            } else {

                if (free) checkLeft();

                Direction dir = tryGreedyMove(rc, proDir, left);
                if (dir != null) {
                    rc.move(dir);
                    if (Util.isUnit && target != null) {
                        sendStop();
                    }
                } else if (Util.goLeft != left) {
                    left = Util.goLeft;
                    dir = tryGreedyMove(rc, proDir, left);
                    if (dir != null) {
                        rc.move(dir);
                        if (Util.isUnit && target != null) {
                            sendStop();
                        }
                    }

                }
                free = false;

            }
            


        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    static void checkLeft(){
        try {
            MapLocation pos = rc.getLocation();
            Direction proDir = new Direction(pos, opponents[0]);
            Direction dir = Util.greedyMove(rc, proDir, 0, left);
            float r = rc.getType().strideRadius;
            if (dir != null) {
                MapLocation nextPos = pos.add(dir, r);
                boolean noLeft = true;
                if (left) noLeft = false;
                Direction dir2 = Util.greedyMove(rc, proDir, 0, noLeft);
                if (dir2 != null) {
                    MapLocation nextPos2 = pos.add(dir2, r);
                    if (opponents[0].distanceTo(nextPos2) < opponents[0].distanceTo((nextPos))) {
                        left = noLeft;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    static void resetPositions(MapLocation[] v){
        for (int i = 0; i < Util.greedySteps; ++i) v[i] = null;
    }

    static boolean goingBackwards(RobotController rc, MapLocation[] prevPos, Direction dir){
        MapLocation pos = rc.getLocation();
        MapLocation nextPos = pos.add(dir, rc.getType().strideRadius);
        for (int i = 0; i < Util.greedySteps; ++i) {
            if (prevPos[i] == null) break;
            float d = prevPos[i].distanceTo(pos);
            float nextd = prevPos[i].distanceTo(nextPos);
            if (d > nextd) {
                Direction newDir = new Direction(pos, prevPos[i]);
                if (rc.canMove(newDir)) return true;
            }
        }
        return false;
    }

    static Direction tryGreedyMove(RobotController rc, Direction proDir, boolean left){
        Direction dir = Util.greedyMove(rc, proDir, 0, left);
        if (dir != null){
            for (int i = Util.greedySteps-1; i > 0; --i){
                previousPositions[i] = previousPositions[i-1];
            }
            previousPositions[0] = rc.getLocation();
            if (Util.newTarget != null) target = Util.newTarget;
        }
        return dir;
    }

    static void readMessages() {
        try {
            emptyMessages.clear();
            HashSet<Integer> newMessagesSeen = new HashSet<Integer>(0);
            int total_messages = rc.readBroadcast(Util.MAX_BROADCAST_MESSAGE);
            lastMessage = 0;
            emptyMessageIndex = 0;
            for (int i = 0; i < total_messages; ++i){
                int a = rc.readBroadcast(i);
                workMessage(a);
                if (a == 0) emptyMessages.add(i);
                else {
                    if (messagesSeen.contains(a)) {
                        rc.broadcast(i, 0);
                        emptyMessages.add(i);
                    }
                    else {
                        newMessagesSeen.add(a);
                        lastMessage = i+1;
                    }
                }
            }
            //rc.broadcast(Util.MAX_BROADCAST_MESSAGE, lastMessage);
            messagesSeen = newMessagesSeen;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    static void workMessage(int m){
        if (m == 0) return;
        int[] a = Util.decode(m);
        if (a[0] == Util.STOP){
            int x = xBase + a[1];
            int y = yBase + a[2];
            MapLocation stopPos = new MapLocation(x,y);
            if (stopPos.distanceTo(rc.getLocation()) < rc.getType().bodyRadius) stop = true;
        }
    }

    static void sendStop(){
        int x = Math.round(target.x);
        int y = Math.round(target.y);
        int m = Util.encodeFinding(Util.STOP, x-xBase, y-yBase, rc.getRoundNum());
        getNextMessageIndex();
        try {
            if (emptyMessageNumber < Util.MAX_BROADCAST_MESSAGE) {
                rc.broadcast(emptyMessageNumber, m);
                messagesSeen.add(m);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    static void getNextMessageIndex(){
        if (emptyMessageIndex < emptyMessages.size()){
            emptyMessageNumber = emptyMessages.get(emptyMessageIndex);
            ++emptyMessageIndex;
        }
        else {
            emptyMessageNumber = lastMessage;
            ++lastMessage;
        }
    }

}
