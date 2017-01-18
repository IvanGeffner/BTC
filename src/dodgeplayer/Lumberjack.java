package dodgeplayer;

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

    static HashSet<Integer> readMes;
    static int initialMessage = 0;

    static float maxUtil;
    static boolean shouldMove;

    static int round;

    @SuppressWarnings("unused")
    public static void run(RobotController rcc) {
        rc = rcc;
        Initialize();

        while (true) {
            //code executed continually, don't let it end

            round = rc.getRoundNum();
            maxUtil = 0.5f/(1.0f + rc.getLocation().distanceTo(enemyBase));
            newTarget = enemyBase;
            shouldMove = true;

            tryChop();

            readMessages();
            broadcastLocations();
            findBestTree();
            updateTarget();
            if (shouldMove) Greedy.moveGreedy(rc,realTarget);
            else Greedy.moveGreedy(rc, rc.getLocation());

            Clock.yield();
        }
    }

    static void Initialize(){
        enemyBase = rc.getInitialArchonLocations(rc.getTeam().opponent())[0];
        base = rc.getInitialArchonLocations(rc.getTeam())[0];
        xBase = Math.round(base.x);
        yBase = Math.round(base.y);
        readMes = new HashSet<>();

        initialMessage = 0;
        try{
            initialMessage = rc.readBroadcast(Communication.MAX_BROADCAST_MESSAGE);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    static void tryChop(){

        int chopID = -1;
        float strikeUtil = 0;
        float chopUtil = 0;

        TreeInfo[] Ti = rc.senseNearbyTrees(rc.getType().strideRadius+rc.getType().bodyRadius);
        RobotInfo[] Ri = rc.senseNearbyRobots(rc.getType().strideRadius+rc.getType().bodyRadius);

        int cont = 0;

        for (TreeInfo ti: Ti){
            if (!rc.canChop(ti.getID())) continue; //break?
            int x = (int)(ti.maxHealth - ti.getHealth());
            cont += x/(int)GameConstants.LUMBERJACK_CHOP_DAMAGE;
            if (ti.getTeam() == rc.getTeam()) strikeUtil -= 4;
            else if (ti.getTeam() ==  rc.getTeam().opponent()){
                strikeUtil += 4;
                if (chopUtil < 10 && rc.canChop(ti.getID())){
                    chopUtil = 10;
                    chopID = ti.getID();
                }
            }
            else {
                strikeUtil += 2.0f*ti.getRadius();
                if (chopUtil < 5.0f*ti.getRadius()){
                    chopUtil = 5.0f*ti.getRadius();
                    chopID = ti.getID();
                }
            }
        }

        if(cont > 1) shouldMove = false;

        for (RobotInfo ri : Ri){
            if (ri.getID() == rc.getID()) continue;
            if (ri.getTeam() == rc.getTeam()){
                strikeUtil -= ((float)ri.getType().bulletCost*2.0f)/(ri.getType().maxHealth);
            }
            else if (ri.getTeam() == rc.getTeam().opponent()){
                strikeUtil += ((float)ri.getType().bulletCost*2.0f)/(ri.getType().maxHealth);
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

        for (TreeInfo ti : Ti){
            if (ti.getTeam() == rc.getTeam()) continue;
            else if (ti.getTeam() == rc.getTeam().opponent()){
                float newUtil = 2.5f/(1.0f + pos.distanceTo(ti.getLocation()));
                if (newUtil > maxUtil){
                    maxUtil = newUtil;
                    newTarget = ti.getLocation();
                }
            }
            else{
                float newUtil = (2.0f*ti.getRadius())/(1.0f + pos.distanceTo(ti.getLocation()));
                if (newUtil > maxUtil){
                    maxUtil = newUtil;
                    newTarget = ti.getLocation();
                }
            }
        }
    }



    static void updateTarget(){
        if (realTarget != null && newTarget != null && newTarget.distanceTo(realTarget) < Constants.eps) return;
        realTarget = newTarget;
        Greedy.resetObstacle();
    }

    static void readMessages(){
        readMes.clear();
        try {
            int lastMessage = rc.readBroadcast(Communication.MAX_BROADCAST_MESSAGE);
            for (int i = initialMessage; i != lastMessage && Clock.getBytecodesLeft() > Constants.BYTECODEPOSTMESSAGES; ) {
                int a = rc.readBroadcast(i);
                workMessage(a);
                readMes.add(a);
                ++i;
                if (i >= Communication.MAX_BROADCAST_MESSAGE) i -= Communication.MAX_BROADCAST_MESSAGE;
            }
            initialMessage = lastMessage;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    static void workMessage(int a){
        int[] m = Communication.decode(a);
        if (m[0] == Communication.ENEMY){
            MapLocation enemyPos = new MapLocation(m[1]+xBase, m[2]+yBase);
            float val = enemyScore(enemyPos, m[3]);
            if (val > maxUtil){
                maxUtil = val;
                newTarget = enemyPos;
            }
        } else if (m[0] == Communication.UNITTREE){
            MapLocation treePos = new MapLocation(m[1]+xBase, m[2]+yBase);
            float val = m[3]/10/ (1.0f + rc.getLocation().distanceTo(treePos));
            if (val > maxUtil){
                maxUtil = val;
                newTarget = treePos;
            }
        } else if (m[0] == Communication.ENEMYTREE){
            MapLocation treePos = new MapLocation(m[1]+xBase, m[2]+yBase);
            float val = 2.5f/(1.0f + rc.getLocation().distanceTo(treePos));
            if (val > maxUtil){
                maxUtil = val;
                newTarget = treePos;
            }
        }
    }

    static float enemyScore (MapLocation m, int a){
        if (m == null) return 0;
        float d = rc.getLocation().distanceTo(m);
        float s = 0;
        if (a == 5) s = 5;
        else if (a == 4) s = 0.1f;
        else if (a == 3) s = 0.1f;
        else if (a == 2) s = 0.2f;
        else if (a == 1) s = 0.5f;
        else if (a == 0) s = 3;
        return s/(d+1);
    }

    static void broadcastLocations() {
        if (round != rc.getRoundNum()) return;
        RobotInfo[] Ri = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        for (RobotInfo ri : Ri) {
            if (Clock.getBytecodesLeft() < Constants.SAFETYMARGIN) return;
            if (ri.type == RobotType.SCOUT) continue;
            MapLocation enemyPos = ri.getLocation();
            int x = Math.round(enemyPos.x);
            int y = Math.round(enemyPos.y);
            int a = Constants.getIndex(ri.type);
            int m = Communication.encodeFinding(Communication.ENEMY, x - xBase, y - yBase, a);
            if (readMes.contains(m)) continue;
            try {
                rc.broadcast(initialMessage, m);
                ++initialMessage;
                if (initialMessage >= Communication.MAX_BROADCAST_MESSAGE)
                    initialMessage -= Communication.MAX_BROADCAST_MESSAGE;
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }

        TreeInfo[] Ti = rc.senseNearbyTrees(-1, rc.getTeam().opponent());
        for (TreeInfo ti : Ti) {
            if (Clock.getBytecodesLeft() < Constants.SAFETYMARGIN) return;
            MapLocation treePos = ti.getLocation();
            int x = Math.round(treePos.x);
            int y = Math.round(treePos.y);
            int m = Communication.encodeFinding(Communication.ENEMYTREE, x - xBase, y - yBase);
            if (readMes.contains(m)) continue;
            try {
                rc.broadcast(initialMessage, m);
                ++initialMessage;
                if (initialMessage >= Communication.MAX_BROADCAST_MESSAGE)
                    initialMessage -= Communication.MAX_BROADCAST_MESSAGE;
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }

        Ti = rc.senseNearbyTrees(-1, Team.NEUTRAL);
        for (TreeInfo ti : Ti) {
            if (Clock.getBytecodesLeft() < Constants.SAFETYMARGIN) return;
            MapLocation treePos = ti.getLocation();
            int x = Math.round(treePos.x);
            int y = Math.round(treePos.y);
            RobotType r = ti.getContainedRobot();
            if (r != null) {
                int a = (int) r.bulletCost;
                int m = Communication.encodeFinding(Communication.UNITTREE, x - xBase, y - yBase, a);
                if (readMes.contains(m)) continue;
                try {
                    rc.broadcast(initialMessage, m);
                    ++initialMessage;
                    if (initialMessage >= Communication.MAX_BROADCAST_MESSAGE)
                        initialMessage -= Communication.MAX_BROADCAST_MESSAGE;
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        try {
            rc.broadcast(Communication.MAX_BROADCAST_MESSAGE, initialMessage);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

}
