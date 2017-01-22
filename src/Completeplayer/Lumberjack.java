package Completeplayer;

import battlecode.common.*;

import java.util.HashSet;


/**
 * Created by Ivan on 1/9/2017.
 */
public class Lumberjack {

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
    static int initialMessageChop = 0;
    static int initialMessageEnemyTree = 0;
    static int initialMessageGoodies = 0;

    static float maxUtil;
    static boolean shouldMove;

    static int round;

    static int roundTarget;
    static boolean targetUpdated;

    static boolean shouldStop = false;

    @SuppressWarnings("unused")
    public static void run(RobotController rcc) {
        rc = rcc;
        Initialize();

        while (true) {
            //code executed continually, don't let it end

            shouldStop = false;
            shouldMove = true;

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

            tryChop();

            readMessages();
            broadcastLocations();

            if (targetUpdated) maxUtil += 1;
            else maxUtil -= 0.03f;

            findBestTree();
            updateTarget();
            if (shouldMove) Greedy.moveGreedy(rc,realTarget, 9200);
            else {
                Greedy.moveToSelf(rc, 9200);
            }

            Clock.yield();
        }
    }

    static void Initialize(){
        enemyBase = rc.getInitialArchonLocations(rc.getTeam().opponent())[0];
        base = rc.getInitialArchonLocations(rc.getTeam())[0];
        xBase = Math.round(base.x);
        yBase = Math.round(base.y);

        Communication.setBase(xBase, yBase);

        initialMessageEmergency = 0;
        initialMessageEnemy = 0;
        initialMessageEnemyGardener = 0;
        initialMessageStop = 0;
        initialMessageChop = 0;
        initialMessageEnemyTree = 0;
        initialMessageGoodies = 0;


        try{
            initialMessageEnemy = rc.readBroadcast(Communication.ENEMYCHANNEL + Communication.CYCLIC_CHANNEL_LENGTH);
            initialMessageEnemyGardener = rc.readBroadcast(Communication.ENEMYGARDENERCHANNEL + Communication.CYCLIC_CHANNEL_LENGTH);
            initialMessageStop = rc.readBroadcast(Communication.STOPCHANNEL + Communication.CYCLIC_CHANNEL_LENGTH);
            initialMessageEmergency = rc.readBroadcast(Communication.EMERGENCYCHANNEL + Communication.CYCLIC_CHANNEL_LENGTH);
            initialMessageChop = rc.readBroadcast(Communication.CHOPCHANNEL + Communication.CYCLIC_CHANNEL_LENGTH);
            initialMessageEnemyTree = rc.readBroadcast(Communication.ENEMYTREECHANNEL + Communication.CYCLIC_CHANNEL_LENGTH);
            initialMessageGoodies = rc.readBroadcast(Communication.TREEWITHGOODIES + Communication.CYCLIC_CHANNEL_LENGTH);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    static void tryChop(){

        int chopID = -1;
        float strikeUtil = 0;
        float chopUtil = 0;

        TreeInfo[] Ti = rc.senseNearbyTrees(2);
        RobotInfo[] Ri = rc.senseNearbyRobots(2);

        int cont = 0;

        for (TreeInfo ti: Ti){
            if (!rc.canChop(ti.getID())) continue; //break?
            if (ti.getTeam() == rc.getTeam()) strikeUtil -= 40;
            else if (ti.getTeam() ==  rc.getTeam().opponent()){
                int x = (int) ti.getHealth();
                cont += (x + (int)GameConstants.LUMBERJACK_CHOP_DAMAGE - 1)/(int)GameConstants.LUMBERJACK_CHOP_DAMAGE;
                strikeUtil += 40;
                if (chopUtil < 100 && rc.canChop(ti.getID())){
                    chopUtil = 100;
                    chopID = ti.getID();
                }
            }
            else {
                if (realTarget != null && Math.abs(rc.getLocation().directionTo(realTarget).radiansBetween(rc.getLocation().directionTo(ti.getLocation()))) < Math.PI/3) {
                    int x = (int) ti.getHealth();
                    cont += (x + (int)GameConstants.LUMBERJACK_CHOP_DAMAGE - 1)/(int)GameConstants.LUMBERJACK_CHOP_DAMAGE;
                    strikeUtil += 2.0f * ti.getRadius();
                    if (chopUtil < 5.0f * ti.getRadius()) {
                        chopUtil = 5.0f * ti.getRadius();
                        chopID = ti.getID();
                    }
                }
            }
        }

        if(cont > 1) shouldMove = false;

        for (RobotInfo ri : Ri){
            if (ri.getID() == rc.getID()) continue;
            if (ri.getTeam() == rc.getTeam()){
                if (ri.getType() == RobotType.ARCHON) strikeUtil -= (100f*20.0f)/(ri.getType().maxHealth);
                strikeUtil -= ((float)ri.getType().bulletCost*20.0f)/(ri.getType().maxHealth);
            }
            else if (ri.getTeam() == rc.getTeam().opponent()){
                if (ri.getType() == RobotType.ARCHON) strikeUtil += (100f*20.0f)/(ri.getType().maxHealth);
                strikeUtil += ((float)ri.getType().bulletCost*20.0f)/(ri.getType().maxHealth);
            }
        }

        try {
            if (chopUtil > strikeUtil && chopUtil > 0) {
                rc.chop(chopID);
            }
            else if (strikeUtil > 0) rc.strike();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }


    static void findBestTree(){

        MapLocation pos = rc.getLocation();
        TreeInfo[] Ti = rc.senseNearbyTrees();

        boolean sent = false;

        for (TreeInfo ti : Ti){
            if (Clock.getBytecodeNum() > Constants.LUMBERCHECK) return;
            if (ti.getTeam() == rc.getTeam()) continue;
            else if (ti.getTeam() == rc.getTeam().opponent()){
                float newUtil = 10f/(1.0f + pos.distanceTo(ti.getLocation()));
                if (newUtil > maxUtil){
                    maxUtil = newUtil;
                    newTarget = ti.getLocation();
                    targetUpdated = true;
                }
                if (!sent){
                    sent = true;
                    MapLocation treePos = ti.getLocation();
                    int x = Math.round(treePos.x);
                    int y = Math.round(treePos.y);
                    Communication.sendMessage(rc, Communication.ENEMYTREECHANNEL, x, y, 0);
                }
            }
            else{
                int a = 0;
                MapLocation treePos = ti.getLocation();
                int x = Math.round(treePos.x);
                int y = Math.round(treePos.y);
                if (ti.getContainedRobot() != null) a = ti.getContainedRobot().bulletCost;
                float newUtil = a/(20.0f*(1.0f + pos.distanceTo(ti.getLocation())));
                if (newUtil > maxUtil){
                    maxUtil = newUtil;
                    newTarget = ti.getLocation();
                    targetUpdated = true;
                }
                if (a > 0) Communication.sendMessage(rc, Communication.TREEWITHGOODIES, x, y, a);
            }
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

            channel = Communication.CHOPCHANNEL;
            lastMessage = rc.readBroadcast(channel + Communication.CYCLIC_CHANNEL_LENGTH);
            System.out.println("Last and Initial: " + lastMessage + " " + initialMessageChop);
            for (int i = initialMessageChop; i != lastMessage && Clock.getBytecodesLeft() > Constants.BYTECODEPOSTMESSAGES; ) {
                int a = rc.readBroadcast(channel + i);
                workMessageChop(a);
                ++i;
                if (i >= Communication.CYCLIC_CHANNEL_LENGTH) i -= Communication.CYCLIC_CHANNEL_LENGTH;
            }
            initialMessageChop = lastMessage;

            channel = Communication.TREEWITHGOODIES;
            lastMessage = rc.readBroadcast(channel + Communication.CYCLIC_CHANNEL_LENGTH);
            System.out.println("Last and Initial: " + lastMessage + " " + initialMessageGoodies);
            for (int i = initialMessageGoodies; i != lastMessage && Clock.getBytecodesLeft() > Constants.BYTECODEPOSTMESSAGES; ) {
                int a = rc.readBroadcast(channel + i);
                workMessageGoodies(a);
                ++i;
                if (i >= Communication.CYCLIC_CHANNEL_LENGTH) i -= Communication.CYCLIC_CHANNEL_LENGTH;
            }
            initialMessageGoodies = lastMessage;

            channel = Communication.ENEMYTREECHANNEL;
            lastMessage = rc.readBroadcast(channel + Communication.CYCLIC_CHANNEL_LENGTH);
            System.out.println("Last and Initial: " + lastMessage + " " + initialMessageEnemyTree);
            for (int i = initialMessageEnemyTree; i != lastMessage && Clock.getBytecodesLeft() > Constants.BYTECODEPOSTMESSAGES; ) {
                int a = rc.readBroadcast(channel + i);
                workMessageEnemyTree(a);
                ++i;
                if (i >= Communication.CYCLIC_CHANNEL_LENGTH) i -= Communication.CYCLIC_CHANNEL_LENGTH;
            }
            initialMessageEnemyTree = lastMessage;

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

    static void workMessageEnemyTree(int a){
        int[] m = Communication.decode(a);
        MapLocation enemyPos = new MapLocation(m[1], m[2]);
        if (rc.canSenseLocation(enemyPos)){
            try{
                TreeInfo t = rc.senseTreeAtLocation(enemyPos);
                if (t != null) enemyPos = t.getLocation();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }

        }
        float val = 10f/(1.0f + rc.getLocation().distanceTo(enemyPos));
        if (val > maxUtil){
            maxUtil = val;
            newTarget = enemyPos;
            targetUpdated = true;
        }
    }

    static void workMessageGoodies(int a){
        int[] m = Communication.decode(a);
        MapLocation enemyPos = new MapLocation(m[1], m[2]);
        if (rc.canSenseLocation(enemyPos)){
            try{
                TreeInfo t = rc.senseNearbyTrees(enemyPos, 0.5f, Team.NEUTRAL)[0];
                if (t != null) enemyPos = t.getLocation();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
        float val = ((float)m[3])/(20.0f*((1.0f + rc.getLocation().distanceTo(enemyPos))));
        if (val > maxUtil){
            maxUtil = val;
            newTarget = enemyPos;
            targetUpdated = true;
        }
    }

    static void workMessageChop(int a){
        int[] m = Communication.decode(a);
        MapLocation enemyPos = new MapLocation(m[1], m[2]);
        if (rc.canSenseLocation(enemyPos)){
            try{
                TreeInfo t = rc.senseNearbyTrees(enemyPos, 0.5f, Team.NEUTRAL)[0];
                if (t != null) enemyPos = t.getLocation();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
        float val = 7.0f/((1.0f + rc.getLocation().distanceTo(enemyPos)));
        if (val > maxUtil){
            maxUtil = val;
            newTarget = enemyPos;
            targetUpdated = true;
        }
    }

    static void workMessageEnemyGardener(int a){
        int[] m = Communication.decode(a);
        MapLocation enemyPos = new MapLocation(m[1], m[2]);
        float val = enemyScore(enemyPos, m[3]);
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
        /*int[] m = Communication.decode(a);
        MapLocation enemyPos = new MapLocation(m[1]+xBase, m[2]+yBase);
        float val = Constants.EMERGENCYSCORE/(1.0f + enemyPos.distanceTo(rc.getLocation()));
        if (val > maxUtil){
            maxUtil = val;
            newTarget = enemyPos;
            targetUpdated = true;
        }*/
    }

    static float enemyScore (MapLocation m, int a){
        if (m == null) return 0;
        float d = rc.getLocation().distanceTo(m);
        float s = 0;
        if (a == 5) s = 5;
        else if (a == 4) s = 0;
        else if (a == 3) s = 0;
        else if (a == 2) s = 0;
        else if (a == 1) s = 0;
        else if (a == 0) s = 8;
        return s/(d+1);
    }

    static void broadcastLocations() {
        if (round != rc.getRoundNum()) return;
        RobotInfo[] Ri = rc.senseNearbyRobots(-1, rc.getTeam().opponent());

        float maxUtil2 = 0;
        MapLocation newTarget2 = null;
        int a2 = 0;

        for (RobotInfo ri : Ri) {
            if (Clock.getBytecodesLeft() < Constants.SAFETYMARGIN) continue;
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
    }

}
