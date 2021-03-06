package Seedingplayer;

import battlecode.common.*;


/**
 * Created by Ivan on 1/9/2017.
 */
public class Tank {

    static RobotController rc;

    static MapLocation realTarget;
    static MapLocation newTarget;

    static MapLocation base;
    static MapLocation enemyBase;
    static int xBase;
    static int yBase;

    static int initialMessageEmergency = 0;
    static int initialMessageEnemy = 0;
    static int initialMessageEnemyGardener = 0;
    static int initialMessageStop = 0;

    static float maxUtil;
    static float maxScore;

    static int round;
    static int roundTarget;
    static boolean targetUpdated;

    static boolean shouldStop = false;


    @SuppressWarnings("unused")
    public static void run(RobotController rcc) {
        //code executed onece at the begining

        rc = rcc;
        Initialize();

        while (true) {

            beginRound();
            Shake.shake(rc);


            round = rc.getRoundNum();
            readMessages();
            broadcastLocations();

            updateTarget();
            try {
                //if (realTarget != null) rc.setIndicatorDot(realTarget, 125, 125, 125);
            }catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }

            if (shouldStop) Greedy.stop(rc, Constants.BYTECODEATSHOOTING);
            else{
                adjustTarget();
                Greedy.moveGreedy(rc, realTarget, Constants.BYTECODEATSHOOTING);
            }

            Clock.yield();
        }
    }

    static void Initialize(){
        enemyBase = rc.getInitialArchonLocations(rc.getTeam().opponent())[0];
        base = rc.getInitialArchonLocations(rc.getTeam())[0];
        xBase = Math.round(base.x);
        yBase = Math.round(base.y);

        Communication.init(rc,xBase, yBase);

        maxUtil = 0;
        maxScore = 0;
        newTarget = enemyBase;

        roundTarget = rc.getRoundNum();

        initialMessageEmergency = 0;
        initialMessageEnemy = 0;
        initialMessageEnemyGardener = 0;
        initialMessageStop = 0;
        try{
            initialMessageEnemy = rc.readBroadcast(Communication.ENEMYCHANNEL + Communication.CYCLIC_CHANNEL_LENGTH);
            initialMessageEnemyGardener = rc.readBroadcast(Communication.ENEMYGARDENERCHANNEL + Communication.CYCLIC_CHANNEL_LENGTH);
            initialMessageStop = rc.readBroadcast(Communication.STOPCHANNEL + Communication.CYCLIC_CHANNEL_LENGTH);
            initialMessageEmergency = rc.readBroadcast(Communication.EMERGENCYCHANNEL + Communication.CYCLIC_CHANNEL_LENGTH);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    static void beginRound(){

        shouldStop = false;
        targetUpdated = false;
        if (realTarget != null && rc.canSenseLocation(realTarget)){
            newTarget = null;
            maxUtil = 0;
            maxScore = 0;
        } else if (realTarget != null && rc.getRoundNum() - roundTarget < Constants.CHANGETARGET){
            maxUtil = 0;
            updateNewTarget(realTarget, maxScore, false);
        } else{
            newTarget = null;
            maxUtil = 0;
            maxScore = 0;
        }

        updateNewTarget(enemyBase, Constants.ENEMYBASESCORE, true);
    }

    static void adjustTarget(){
        try {
            if (realTarget == null) {
                realTarget = rc.getLocation();
                return;
            }
            if (!rc.canSenseLocation(realTarget)) return;
            RobotInfo r = rc.senseRobotAtLocation(realTarget);
            if (r == null) return;
            RobotType rt = r.getType();
            if (rt == RobotType.GARDENER) {
                if (rc.getLocation().distanceTo(r.getLocation()) < rc.getType().bodyRadius + 1.5f) realTarget = rc.getLocation();
            }
            if (rt == RobotType.ARCHON) {
                if (rc.getLocation().distanceTo(r.getLocation()) < rc.getType().bodyRadius + 2.5f) realTarget = rc.getLocation();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    static void updateNewTarget(MapLocation target, float score, boolean update){
        float dist1 = rc.getLocation().distanceTo(target) + 1.0f;
        float val = score/(dist1*dist1);
        if (val > maxUtil){
            maxUtil = val;
            maxScore = score;
            newTarget = target;
            if (update) targetUpdated = true;
        }
    }

    static void updateTarget(){
        if(targetUpdated) roundTarget = rc.getRoundNum();
        realTarget = newTarget;
    }

    static void readMessages(){
        try {
            int channel = Communication.ENEMYCHANNEL;
            int lastMessage = rc.readBroadcast(channel + Communication.CYCLIC_CHANNEL_LENGTH);
            System.out.println("Last and Initial: " + lastMessage + " " + initialMessageEnemy);
            for (int i = initialMessageEnemy; i != lastMessage && Clock.getBytecodesLeft() > Constants.BYTECODEPOSTENEMYMESSAGES; ) {
                int a = rc.readBroadcast(channel + i);
                workMessageEnemy(a);
                ++i;
                if (i >= Communication.CYCLIC_CHANNEL_LENGTH) i -= Communication.CYCLIC_CHANNEL_LENGTH;
            }
            initialMessageEnemy = lastMessage;

            channel = Communication.EMERGENCYCHANNEL;
            lastMessage = rc.readBroadcast(channel + Communication.CYCLIC_CHANNEL_LENGTH);
            System.out.println("Last and Initial: " + lastMessage + " " + initialMessageEmergency);
            for (int i = initialMessageEmergency; i != lastMessage && Clock.getBytecodesLeft() > Constants.BYTECODEPOSTEMERGENCYMESSAGES; ) {
                int a = rc.readBroadcast(channel + i);
                workMessageEmergency(a);
                ++i;
                if (i >= Communication.CYCLIC_CHANNEL_LENGTH) i -= Communication.CYCLIC_CHANNEL_LENGTH;
            }
            initialMessageEmergency = lastMessage;

            channel = Communication.STOPCHANNEL;
            lastMessage = rc.readBroadcast(channel + Communication.CYCLIC_CHANNEL_LENGTH);
            System.out.println("Last and Initial: " + lastMessage + " " + initialMessageStop);
            for (int i = initialMessageStop; i != lastMessage && Clock.getBytecodesLeft() > Constants.BYTECODEPOSTSTOPMESSAGES; ) {
                int a = rc.readBroadcast(channel + i);
                workMessageStop(a);
                ++i;
                if (i >= Communication.CYCLIC_CHANNEL_LENGTH) i -= Communication.CYCLIC_CHANNEL_LENGTH;
            }
            initialMessageStop = lastMessage;

            channel = Communication.ENEMYGARDENERCHANNEL;
            lastMessage = rc.readBroadcast(channel + Communication.CYCLIC_CHANNEL_LENGTH);
            System.out.println("Last and Initial: " + lastMessage + " " + initialMessageEnemyGardener);
            for (int i = initialMessageEnemyGardener; i != lastMessage && Clock.getBytecodesLeft() > Constants.BYTECODEPOSTENEMYGARDENERMESSAGES; ) {
                int a = rc.readBroadcast(channel + i);
                workMessageEnemyGardener(a);
                ++i;
                if (i >= Communication.CYCLIC_CHANNEL_LENGTH) i -= Communication.CYCLIC_CHANNEL_LENGTH;
            }
            initialMessageEnemyGardener = lastMessage;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    static void workMessageEnemy(int a){
        int[] m = Communication.decode(a);
        MapLocation enemyPos = new MapLocation(m[1], m[2]);
        if (a == 5) enemyBase = enemyPos;
        if (rc.canSenseLocation(enemyPos)) return;
        updateNewTarget(enemyPos, enemyScore(a), true);
    }

    static void workMessageEnemyGardener(int a){
        int[] m = Communication.decode(a);
        MapLocation enemyPos = new MapLocation(m[1], m[2]);
        if (rc.canSenseLocation(enemyPos)) return;
        if (a == 5) enemyBase = enemyPos;
        updateNewTarget(enemyPos, enemyScore(a), true);
    }

    static void workMessageStop(int a){
        int[] m = Communication.decode(a);
        MapLocation pos = new MapLocation(m[1], m[2]);
        if (pos.distanceTo(rc.getLocation()) < rc.getType().bodyRadius) shouldStop = true;
    }

    static void workMessageEmergency(int a){
        int[] m = Communication.decode(a);
        MapLocation enemyPos = new MapLocation(m[1], m[2]);
        if (rc.canSenseLocation(enemyPos)) return;
        updateNewTarget(enemyPos, Constants.EMERGENCYSCORE, true);
    }

    static float enemyScore (int a) {
        if (a == 5) return 8;
        if (a == 4) return 15;
        if (a == 3) return 6;
        if (a == 2) return 12;
        if (a == 1) return 8;
        return 20;
    }

    static void broadcastLocations() {
        if (round != rc.getRoundNum()) return;

        RobotInfo[] Ri = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        boolean sent = false;


        for (RobotInfo ri : Ri) {
            if (Clock.getBytecodesLeft() < Constants.SAFETYMARGIN) return;
            MapLocation enemyPos = ri.getLocation();
            int x = Math.round(enemyPos.x);
            int y = Math.round(enemyPos.y);
            int a = Constants.getIndex(ri.type);
            if (a == 0) Communication.sendMessage(Communication.ENEMYGARDENERCHANNEL, x, y, 0);
            else if (a == 5) Communication.sendMessage(Communication.ENEMYGARDENERCHANNEL, x, y, 5);
            updateNewTarget(enemyPos, enemyScore(a), true);
            if (!sent){
                Communication.sendMessage(Communication.ENEMYCHANNEL, Math.round(enemyPos.x), Math.round(enemyPos.y), a);
                sent = true;
            }
        }

        TreeInfo[] Ti = rc.senseNearbyTrees(-1, rc.getTeam().opponent());
        if (Ti.length > 0) {
            TreeInfo ti = Ti[0];
            if (Clock.getBytecodesLeft() < Constants.SAFETYMARGIN) return;
            MapLocation treePos = ti.getLocation();
            int x = Math.round(treePos.x);
            int y = Math.round(treePos.y);
            Communication.sendMessage(Communication.ENEMYTREECHANNEL, x, y, 0);
        }

        Ti = rc.senseNearbyTrees(-1, Team.NEUTRAL);
        for (TreeInfo ti : Ti) {
            if (Clock.getBytecodesLeft() < Constants.SAFETYMARGIN) return;
            MapLocation treePos = ti.getLocation();
            int x = Math.round(treePos.x);
            int y = Math.round(treePos.y);
            RobotType r = ti.getContainedRobot();
            if (r != null) {
                int a = r.bulletCost;
                if (r == RobotType.ARCHON) a = 1000;
                Communication.sendMessage(Communication.TREEWITHGOODIES, x, y, a);
            }
        }
    }


}
