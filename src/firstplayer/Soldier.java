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

                rc.broadcast(Util.MAX_BROADCAST_MESSAGE, lastMessage);


                Clock.yield();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
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
            } else {

                Direction dir = tryGreedyMove(rc, proDir, left);
                if (dir != null){
                    rc.move(dir);
                    if (Util.isUnit && target != null){
                        sendStop();
                    }
                }
                else if (Util.goLeft != left) {
                    left = Util.goLeft;
                    dir = tryGreedyMove(rc, proDir, left);
                    if (dir != null){
                        rc.move(dir);
                        if (Util.isUnit && target != null){
                            sendStop();
                        }
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
            rc.broadcast(Util.MAX_BROADCAST_MESSAGE, lastMessage);
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
