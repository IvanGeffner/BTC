package Lumbestplayer;

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

            shouldStop = false;

            //code executed continually, don't let it end
            targetUpdated = false;
            if (realTarget != null && rc.canSenseLocation(realTarget)){
                newTarget = null;
                maxUtil = 0;
            } else if (realTarget != null && rc.getRoundNum() - roundTarget < Constants.CHANGETARGET){
                newTarget = realTarget;
            } else{
                newTarget = null;
                maxUtil = 0;
            }

            float val = 5.0f/(1.0f + rc.getLocation().distanceTo(enemyBase));
            if (!rc.canSenseLocation(enemyBase) && val > maxUtil){
                maxUtil = val;
                newTarget = enemyBase;
            }


            round = rc.getRoundNum();
            readMessages();
            broadcastLocations();

            if (targetUpdated) maxUtil += 1;
            else maxUtil -= 0.03f;

            updateTarget();
            try {
                //if (realTarget != null) rc.setIndicatorDot(realTarget, 125, 125, 125);
            }catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }

            if (shouldStop) Greedy.stop(rc, Constants.BYTECODEATSHOOTING);
            else Greedy.moveGreedy(rc, realTarget, Constants.BYTECODEATSHOOTING);

            Clock.yield();
        }
    }

    static void Initialize(){
        enemyBase = rc.getInitialArchonLocations(rc.getTeam().opponent())[0];
        base = rc.getInitialArchonLocations(rc.getTeam())[0];
        xBase = Math.round(base.x);
        yBase = Math.round(base.y);

        Communication.setBase(xBase, yBase);

        maxUtil = 5.0f/(1.0f + rc.getLocation().distanceTo(enemyBase));
        newTarget = enemyBase;
        roundTarget = 1;

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

    static void updateTarget(){
        if(targetUpdated) roundTarget = rc.getRoundNum();
        realTarget = newTarget;
    }

    static void readMessages(){
        try {
            int channel = Communication.ENEMYCHANNEL;
            int lastMessage = rc.readBroadcast(channel + Communication.CYCLIC_CHANNEL_LENGTH);
            System.out.println("Last and Initial: " + lastMessage + " " + initialMessageEnemy);
            for (int i = initialMessageEnemy; i != lastMessage && Clock.getBytecodesLeft() > Constants.BYTECODEPOSTMESSAGES; ) {
                int a = rc.readBroadcast(channel + i);
                workMessageEnemy(a);
                ++i;
                if (i >= Communication.CYCLIC_CHANNEL_LENGTH) i -= Communication.CYCLIC_CHANNEL_LENGTH;
            }
            initialMessageEnemy = lastMessage;

            channel = Communication.EMERGENCYCHANNEL;
            lastMessage = rc.readBroadcast(channel + Communication.CYCLIC_CHANNEL_LENGTH);
            System.out.println("Last and Initial: " + lastMessage + " " + initialMessageEmergency);
            for (int i = initialMessageEmergency; i != lastMessage && Clock.getBytecodesLeft() > Constants.BYTECODEPOSTMESSAGES; ) {
                int a = rc.readBroadcast(channel + i);
                workMessageEmergency(a);
                ++i;
                if (i >= Communication.CYCLIC_CHANNEL_LENGTH) i -= Communication.CYCLIC_CHANNEL_LENGTH;
            }
            initialMessageEmergency = lastMessage;

            channel = Communication.STOPCHANNEL;
            lastMessage = rc.readBroadcast(channel + Communication.CYCLIC_CHANNEL_LENGTH);
            System.out.println("Last and Initial: " + lastMessage + " " + initialMessageStop);
            for (int i = initialMessageStop; i != lastMessage && Clock.getBytecodesLeft() > Constants.BYTECODEPOSTMESSAGES; ) {
                int a = rc.readBroadcast(channel + i);
                workMessageStop(a);
                ++i;
                if (i >= Communication.CYCLIC_CHANNEL_LENGTH) i -= Communication.CYCLIC_CHANNEL_LENGTH;
            }
            initialMessageStop = lastMessage;

            channel = Communication.ENEMYGARDENERCHANNEL;
            lastMessage = rc.readBroadcast(channel + Communication.CYCLIC_CHANNEL_LENGTH);
            System.out.println("Last and Initial: " + lastMessage + " " + initialMessageEnemyGardener);
            for (int i = initialMessageEnemyGardener; i != lastMessage && Clock.getBytecodesLeft() > Constants.BYTECODEPOSTMESSAGES; ) {
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
        float val = enemyScore(enemyPos, m[3]);
        if (val > maxUtil){
            maxUtil = val;
            newTarget = enemyPos;
            targetUpdated = true;
        }
    }

    static void workMessageEnemyGardener(int a){
        int[] m = Communication.decode(a);
        MapLocation enemyPos = new MapLocation(m[1], m[2]);
        float val = enemyScore(enemyPos, 0);
        if (val > maxUtil){
            maxUtil = val;
            newTarget = enemyPos;
            targetUpdated = true;
        }
    }

    static void workMessageStop(int a){
        int[] m = Communication.decode(a);
        MapLocation pos = new MapLocation(m[1], m[2]);
        if (pos.distanceTo(rc.getLocation()) < rc.getType().bodyRadius) shouldStop = true;
    }

    static void workMessageEmergency(int a){
        int[] m = Communication.decode(a);
        MapLocation enemyPos = new MapLocation(m[1], m[2]);
        float val = Constants.EMERGENCYSCORE/(1.0f + enemyPos.distanceTo(rc.getLocation()));
        if (val > maxUtil){
            maxUtil = val;
            newTarget = enemyPos;
            targetUpdated = true;
        }
    }

    static float enemyScore (MapLocation m, int a){
        if (m == null) return 0;
        float d = rc.getLocation().distanceTo(m);
        float s = 0;
        if (a == 5) s = 8;
        else if (a == 4) s = 20;
        else if (a == 3) s = 15;
        else if (a == 2) s = 20;
        else if (a == 1) s = 8;
        else if (a == 0) s = 15;
        return s/(1.0f + d);
    }

    static void broadcastLocations() {
        if (round != rc.getRoundNum()) return;
        RobotInfo[] Ri = rc.senseNearbyRobots(-1, rc.getTeam().opponent());

        float maxUtil2 = 0;
        MapLocation newTarget2 = null;
        int a2 = 0;

        for (RobotInfo ri : Ri) {
            if (Clock.getBytecodesLeft() < Constants.SAFETYMARGIN) return;
            MapLocation enemyPos = ri.getLocation();
            int x = Math.round(enemyPos.x);
            int y = Math.round(enemyPos.y);
            int a = Constants.getIndex(ri.type);
            if (a == 0) Communication.sendMessage(rc, Communication.ENEMYGARDENERCHANNEL, x, y, 0);
            else if (a == 5) Communication.sendMessage(rc, Communication.ENEMYGARDENERCHANNEL, x, y, 5);
            float val = enemyScore(enemyPos, a);
            if (val > maxUtil2) {
                maxUtil2 = val;
                newTarget2 = enemyPos;
                a2 = a;
                targetUpdated = true;
            }
        }

        if (maxUtil2 > maxUtil){
            maxUtil = maxUtil2;
            newTarget = newTarget2;
        }

        if (newTarget2 != null) Communication.sendMessage(rc, Communication.ENEMYCHANNEL, Math.round(newTarget2.x), Math.round(newTarget2.y), a2);

        TreeInfo[] Ti = rc.senseNearbyTrees(-1, rc.getTeam().opponent());
        if (Ti.length > 0) {
            TreeInfo ti = Ti[0];
            if (Clock.getBytecodesLeft() < Constants.SAFETYMARGIN) return;
            MapLocation treePos = ti.getLocation();
            int x = Math.round(treePos.x);
            int y = Math.round(treePos.y);
            Communication.sendMessage(rc, Communication.ENEMYTREECHANNEL, x, y, 0);
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
                Communication.sendMessage(rc, Communication.TREEWITHGOODIES, x, y, a);
            }
        }
    }


}
