package Vells.Visionplayer;

import battlecode.common.*;


/**
 * Created by Ivan on 1/9/2017.
 */
public class Soldier {

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

    static MapLocation emergencyTarget;


    @SuppressWarnings("unused")
    public static void run(RobotController rcc) {
        //code executed onece at the begining

        rc = rcc;
        Initialize();

        while (true) {

            beginRound();


            round = rc.getRoundNum();
            readMessages();
            broadcastLocations();

            updateTarget();
            try {
                //if (realTarget != null) rc.setIndicatorDot(realTarget, 125, 125, 125);

                if (emergencyTarget != null && rc.canSenseAllOfCircle(emergencyTarget, rc.getType().bodyRadius) && rc.onTheMap(emergencyTarget, rc.getType().bodyRadius)) Greedy.moveGreedy(rc,emergencyTarget, Constants.BYTECODEATSHOOTING);
                else {

                    if (shouldStop) Greedy.stop(rc, Constants.BYTECODEATSHOOTING);
                    else {
                        adjustTarget();

                        //rc.setIndicatorLine(rc.getLocation(), realTarget, 255, 0, 0);

                        Greedy.moveGreedy(rc, realTarget, Constants.BYTECODEATSHOOTING);
                    }
                }
            }catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
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

        Bot.shake(rc);
        Bot.donate(rc);

        shouldStop = false;
        targetUpdated = false;
        if (realTarget != null && rc.canSenseLocation(realTarget)){
            newTarget = null;
            maxUtil = 0;
            maxScore = 0;
        } else if (realTarget != null && rc.getRoundNum() - roundTarget < Constants.CHANGETARGET){
            System.out.println("He entrat!!!");
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
        System.out.println("Possible target: " + target.x + " " + target.y + " " + score + " " + rc.getLocation().distanceTo(target));
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
        boolean shouldRead = true;
        if (rc.getRoundNum() - Greedy.bulletDodge <= 1) shouldRead = false;

        try {
            int channel = Communication.ENEMYCHANNEL;
            int lastMessage = rc.readBroadcast(channel + Communication.CYCLIC_CHANNEL_LENGTH);
            //System.out.println("Last and Initial: " + lastMessage + " " + initialMessageEnemy);
            for (int i = initialMessageEnemy; shouldRead && i != lastMessage && Clock.getBytecodesLeft() > Constants.BYTECODEPOSTENEMYMESSAGES; ) {
                int a = rc.readBroadcast(channel + i);
                workMessageEnemy(a);
                ++i;
                if (i >= Communication.CYCLIC_CHANNEL_LENGTH) i -= Communication.CYCLIC_CHANNEL_LENGTH;
            }
            initialMessageEnemy = lastMessage;

            channel = Communication.EMERGENCYCHANNEL;
            lastMessage = rc.readBroadcast(channel + Communication.CYCLIC_CHANNEL_LENGTH);
            //System.out.println("Last and Initial: " + lastMessage + " " + initialMessageEmergency);
            for (int i = initialMessageEmergency; shouldRead && i != lastMessage && Clock.getBytecodesLeft() > Constants.BYTECODEPOSTEMERGENCYMESSAGES; ) {
                int a = rc.readBroadcast(channel + i);
                workMessageEmergency(a);
                ++i;
                if (i >= Communication.CYCLIC_CHANNEL_LENGTH) i -= Communication.CYCLIC_CHANNEL_LENGTH;
            }
            initialMessageEmergency = lastMessage;

            channel = Communication.STOPCHANNEL;
            lastMessage = rc.readBroadcast(channel + Communication.CYCLIC_CHANNEL_LENGTH);
            //System.out.println("Last and Initial: " + lastMessage + " " + initialMessageStop);
            for (int i = initialMessageStop; shouldRead && i != lastMessage && Clock.getBytecodesLeft() > Constants.BYTECODEPOSTSTOPMESSAGES; ) {
                int a = rc.readBroadcast(channel + i);
                workMessageStop(a);
                ++i;
                if (i >= Communication.CYCLIC_CHANNEL_LENGTH) i -= Communication.CYCLIC_CHANNEL_LENGTH;
            }
            initialMessageStop = lastMessage;

            channel = Communication.ENEMYGARDENERCHANNEL;
            lastMessage = rc.readBroadcast(channel + Communication.CYCLIC_CHANNEL_LENGTH);
            //System.out.println("Last and Initial: " + lastMessage + " " + initialMessageEnemyGardener);
            for (int i = initialMessageEnemyGardener; shouldRead && i != lastMessage && Clock.getBytecodesLeft() > Constants.BYTECODEPOSTENEMYGARDENERMESSAGES; ) {
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
        if (rc.canSenseLocation(enemyPos)) return;
        if (m[3] == 5) enemyBase = enemyPos;
        updateNewTarget(enemyPos, Constants.enemyScore(m[3]), true);
    }

    static void workMessageEnemyGardener(int a){
        int[] m = Communication.decode(a);
        MapLocation enemyPos = new MapLocation(m[1], m[2]);
        if (rc.canSenseLocation(enemyPos)) return;
        if (m[3] == 5) enemyBase = enemyPos;
        updateNewTarget(enemyPos, Constants.enemyScore(m[3]), true);
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


    static void broadcastLocations() {
        int byte1 = Clock.getBytecodeNum();

        emergencyTarget = null;

        if (round != rc.getRoundNum()) return;

        RobotInfo[] Ri = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        boolean sent = false;

        int foundSoldier = 0;
        int foundTank = 0;

        float xSol = 0, ySol = 0, xTank = 0, yTank = 0;


        MapLocation pos = rc.getLocation();

        for (RobotInfo ri : Ri) {
            MapLocation enemyPos = ri.getLocation();
            int x = Math.round(enemyPos.x);
            int y = Math.round(enemyPos.y);
            int a = Constants.getIndex(ri.type);
            if (a == 0){
                Communication.sendMessage(Communication.ENEMYGARDENERCHANNEL, x, y, 0);
                initialMessageEnemyGardener = (initialMessageEnemyGardener+1)% Communication.CYCLIC_CHANNEL_LENGTH;
            }
            else if (a == 5){
                Communication.sendMessage(Communication.ENEMYGARDENERCHANNEL, x, y, 5);
                initialMessageEnemyGardener = (initialMessageEnemyGardener+1)% Communication.CYCLIC_CHANNEL_LENGTH;
                enemyBase = enemyPos;
            }
            else if (!sent){
                Communication.sendMessage(Communication.ENEMYCHANNEL, Math.round(enemyPos.x), Math.round(enemyPos.y), a);
                initialMessageEnemy = (initialMessageEnemy+1)% Communication.CYCLIC_CHANNEL_LENGTH;
                sent = true;
            }

            if (a == 2){
                ++foundSoldier;
                float dinv = 1/pos.distanceTo(enemyPos);
                xSol += dinv*(pos.x - enemyPos.x);
                ySol += dinv*(pos.y - enemyPos.y);
            }

            if (a == 3){
                ++foundTank;
                float dinv = 1/pos.distanceTo(enemyPos);
                xTank += dinv*(pos.x - enemyPos.x);
                yTank += dinv*(pos.y - enemyPos.y);
            }
            updateNewTarget(enemyPos, Constants.enemyScore(a), true);
        }


        float randomDev = (0.5f - (float)Math.random())/5.0f;

        if (foundTank > 0){
            Direction dir = new Direction(xTank, yTank).rotateLeftRads(randomDev);
            if (dir != null){
                emergencyTarget = pos.add(dir, rc.getType().strideRadius+1);
            }
        } else if (foundSoldier > 0){
            Direction dir = new Direction(xSol, ySol).rotateLeftRads(randomDev);
            if (dir != null){
                emergencyTarget = pos.add(dir, rc.getType().strideRadius+1);
            }
        }

        if (Clock.getBytecodeNum() - byte1 >= Constants.BROADCASTMAXSOLDIER) return;

        TreeInfo[] Ti = rc.senseNearbyTrees(-1, rc.getTeam().opponent());
        if (Ti.length > 0) {
            TreeInfo ti = Ti[0];
            MapLocation treePos = ti.getLocation();
            int x = Math.round(treePos.x);
            int y = Math.round(treePos.y);
            Communication.sendMessage(Communication.ENEMYTREECHANNEL, x, y, 0);
        }

        if (Clock.getBytecodeNum() - byte1 >= Constants.BROADCASTMAXSOLDIER) return;

        Ti = rc.senseNearbyTrees(-1, Team.NEUTRAL);
        for (TreeInfo ti : Ti) {
            if (Clock.getBytecodeNum() - byte1 >= Constants.BROADCASTMAXSOLDIER) return;
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
