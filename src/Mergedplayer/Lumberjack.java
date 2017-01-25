package Mergedplayer;

import battlecode.common.*;


/**
 * Created by Ivan on 1/9/2017.
 */
public class Lumberjack {

    static RobotController rc;

    static MapLocation realTarget;
    static MapLocation newTarget;
    static float maxUtil;
    static float scoreTarget;

    static MapLocation base;
    static int xBase;
    static int yBase;

    static MapLocation enemyBase;

    static int initialMessageEmergency = 0; //TODO utilitzar-lo si cal
    static int initialMessageEnemy = 0;
    static int initialMessageEnemyGardener = 0;
    static int initialMessageStop = 0;
    static int initialMessageChop = 0;
    static int initialMessageGoodieTree = 0;
    static int initialMessageEnemyTree = 0;

    static int round;

    static int roundTarget;
    static boolean targetUpdated;

    static boolean shouldStop = false;
    static boolean shouldGreedy;
    static boolean shouldMove;

    @SuppressWarnings("unused")
    public static void run(RobotController rcc) {
        rc = rcc;
        Initialize();
        while (true) {
            //code executed continually, don't let it end
            beginRound();
            findBestTarget();

            if (realTarget != null){
                System.out.println("Target1: " + realTarget.x + " " + realTarget.y + " " + maxUtil);
                //rc.setIndicatorLine(rc.getLocation(), realTarget, 255, 0, 0 );

            }

            readMessages();

            updateTarget();

            if (realTarget != null) System.out.println("Target1: " + realTarget.x + " " + realTarget.y + " " + maxUtil);


            tryChop();

            System.out.println("stopGreedyMove: " + shouldStop + " "+ shouldGreedy + " " + shouldMove);
            if(shouldMove)
            {
                if(shouldStop) Greedy.stop(rc, 9200);
                else Greedy.moveGreedy(rc, realTarget, 9200);
            } else Greedy.moveToSelf(rc, 9200);

            Clock.yield();
        }
    }

    //initializing variables
    static void Initialize()
    {
        enemyBase = rc.getInitialArchonLocations((rc.getTeam().opponent()))[0];
        maxUtil = 0;
        scoreTarget = 0;

        base = rc.getInitialArchonLocations(rc.getTeam())[0];
        xBase = Math.round(base.x);
        yBase = Math.round(base.y);
        Communication.init(rc, xBase, yBase);

        initialMessageGoodieTree = 0;
        initialMessageEnemyGardener = 0;
        initialMessageChop = 0;
        initialMessageEnemy = 0;
        initialMessageStop = 0;
        initialMessageEnemyTree = 0;
        try{
            initialMessageGoodieTree = rc.readBroadcast(Communication.TREEWITHGOODIES + Communication.CYCLIC_CHANNEL_LENGTH);
            initialMessageEnemyGardener = rc.readBroadcast(Communication.ENEMYGARDENERCHANNEL + Communication.CYCLIC_CHANNEL_LENGTH);
            initialMessageChop = rc.readBroadcast(Communication.CHOPCHANNEL + Communication.CYCLIC_CHANNEL_LENGTH);
            initialMessageEnemy = rc.readBroadcast(Communication.ENEMYCHANNEL + Communication.CYCLIC_CHANNEL_LENGTH);
            initialMessageStop = rc.readBroadcast(Communication.STOPCHANNEL + Communication.CYCLIC_CHANNEL_LENGTH);
            initialMessageEnemyTree = rc.readBroadcast(Communication.ENEMYTREECHANNEL + Communication.CYCLIC_CHANNEL_LENGTH);

        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    static void beginRound()
    {
        shouldStop = false;
        shouldMove = true;
        targetUpdated = false;
        Shake.shake(rc);
        try {
            if(realTarget != null)
            {
                float dist1 = rc.getLocation().distanceTo(realTarget) + Constants.ADDTODISTANCELUMBERJACK;
                maxUtil = scoreTarget/(dist1*dist1);
                newTarget = realTarget;
            }

            if(realTarget != null && rc.canSenseLocation(realTarget))
            {

                TreeInfo ti = rc.senseTreeAtLocation(realTarget);
                if (ti == null || ti.getTeam() == rc.getTeam()) {
                    realTarget = null;
                    newTarget = null;
                    scoreTarget = 0;
                    maxUtil = 0;
                }

            } else if(rc.getRoundNum() - roundTarget >= Constants.CHANGETARGETLUMBERJACKS)
            {
                realTarget = null;
                newTarget = null;
                scoreTarget = 0;
                maxUtil = 0;
            }

            calculateNewTarget(enemyBase, Constants.BASESCORELUMBERJACK, false);

            System.out.println("GREEDY: " + shouldGreedy);

        }catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    //messages related
    static void readMessages(){
        try {
            int channel = Communication.TREEWITHGOODIES;
            int lastMessage = rc.readBroadcast(channel + Communication.CYCLIC_CHANNEL_LENGTH);
            for(int i = initialMessageGoodieTree; i != lastMessage && Clock.getBytecodesLeft() > Constants.BYTECODEPOSTMESSAGESLUMBERJACK;)
            {
                int a = rc.readBroadcast(channel + i);
                workMessageUnitTree(a);
                ++i;
                if (i >= Communication.CYCLIC_CHANNEL_LENGTH) i -= Communication.CYCLIC_CHANNEL_LENGTH;
            }
            initialMessageGoodieTree = lastMessage;
        } catch (GameActionException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        try {
            int channel = Communication.CHOPCHANNEL;
            int lastMessage = rc.readBroadcast(channel + Communication.CYCLIC_CHANNEL_LENGTH);
            for(int i = initialMessageChop; i != lastMessage && Clock.getBytecodesLeft() > Constants.BYTECODEPOSTMESSAGESLUMBERJACK;)
            {
                int a = rc.readBroadcast(channel + i);
                workMessageChopTree(a);
                ++i;
                if (i >= Communication.CYCLIC_CHANNEL_LENGTH) i -= Communication.CYCLIC_CHANNEL_LENGTH;
            }
            initialMessageChop = lastMessage;
        } catch (GameActionException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        try {
            int channel = Communication.ENEMYGARDENERCHANNEL;
            int lastMessage = rc.readBroadcast(channel + Communication.CYCLIC_CHANNEL_LENGTH);
            for(int i = initialMessageEnemyGardener; i != lastMessage && Clock.getBytecodesLeft() > Constants.BYTECODEPOSTMESSAGESLUMBERJACK;)
            {
                int a = rc.readBroadcast(channel + i);
                workMessageEnemyUnit(a);
                ++i;
                if (i >= Communication.CYCLIC_CHANNEL_LENGTH) i -= Communication.CYCLIC_CHANNEL_LENGTH;
            }
            initialMessageEnemyGardener = lastMessage;
        } catch (GameActionException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        try {
            int channel = Communication.ENEMYTREECHANNEL;
            int lastMessage = rc.readBroadcast(channel + Communication.CYCLIC_CHANNEL_LENGTH);
            for(int i = initialMessageEnemyTree; i != lastMessage && Clock.getBytecodesLeft() > Constants.BYTECODEPOSTMESSAGESLUMBERJACK;)
            {
                int a = rc.readBroadcast(channel + i);
                workMessageEnemyTree(a);
                ++i;
                if (i >= Communication.CYCLIC_CHANNEL_LENGTH) i -= Communication.CYCLIC_CHANNEL_LENGTH;
            }
            initialMessageEnemyTree = lastMessage;
        } catch (GameActionException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        try {
        	/*
            int channel = Communication.ENEMYCHANNEL;
            int lastMessage = rc.readBroadcast(channel + Communication.CYCLIC_CHANNEL_LENGTH);
            for(int i = initialMessageEnemy; i != lastMessage && Clock.getBytecodesLeft() > Constants.BYTECODEPOSTMESSAGESLUMBERJACK;)
            {
                int a = rc.readBroadcast(channel + i);
                workMessageEnemyUnit(a);
                ++i;
            }
            initialMessageEnemy = lastMessage;
			*/

            int channel = Communication.STOPCHANNEL;
            int lastMessage = rc.readBroadcast(channel + Communication.CYCLIC_CHANNEL_LENGTH);
            System.out.println("Last and Initial: " + lastMessage + " " + initialMessageStop);
            for (int i = initialMessageStop; i != lastMessage && Clock.getBytecodesLeft() > Constants.BYTECODEPOSTMESSAGESLUMBERJACK; ) {
                int a = rc.readBroadcast(channel + i);
                workMessageStop(a);
                ++i;
                if (i >= Communication.CYCLIC_CHANNEL_LENGTH) i -= Communication.CYCLIC_CHANNEL_LENGTH;
            }
            initialMessageStop = lastMessage;
        } catch (GameActionException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    static void workMessageUnitTree(int a)
    {
        int[] m = Communication.decode(a);
        MapLocation unitTreePos = new MapLocation(m[1], m[2]);
        //System.out.println("Unit Cost in this tree: " + m[3]);
        //System.out.println("sent by: " + m[0]);
        if(rc.canSenseLocation(unitTreePos)) return;
        if(m[3] == 1000 || m[3] == -1 || m[3] == 80) m[3] = Constants.ARCHONVALUE; 
        calculateNewTarget(unitTreePos, m[3]/Constants.CONVERSIONBULLETCOST, m[0] != Constants.SCOUT);
    }
    static void workMessageEnemyTree(int a)
    {
        int[] m = Communication.decode(a);
        MapLocation enemyTreePos = new MapLocation(m[1], m[2]);
        if(rc.canSenseLocation(enemyTreePos)) return;
        calculateNewTarget(enemyTreePos,Constants.ENEMYTREESCORE, m[0] != Constants.SCOUT);
    }
    static void workMessageChopTree(int a)
    {
        int[] m = Communication.decode(a);
        MapLocation neutralTreePos = new MapLocation(m[1], m[2]);
        if (rc.canSenseLocation(neutralTreePos)){
            try{
                TreeInfo t = rc.senseTreeAtLocation(neutralTreePos);
                if(t == null) t = rc.senseNearbyTrees(neutralTreePos, 0.5f, Team.NEUTRAL)[0];
                if (t != null) neutralTreePos = t.getLocation();
                else return;
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
        calculateNewTarget(neutralTreePos, Constants.CHOPTREESCORE, false);
    }
    static void workMessageEnemyUnit(int a)
    {
        int[] m = Communication.decode(a);
        MapLocation enemyPos = new MapLocation(m[1],m[2]);
        if(rc.canSenseLocation(enemyPos)) return;
        if(m[3] == Constants.getIndex(RobotType.ARCHON)) enemyBase = enemyPos;
        calculateNewTarget(enemyPos,enemyScore(m[3]), m[0] != Constants.SCOUT);
    }
    static void workMessageStop(int a){
        int[] m = Communication.decode(a);
        MapLocation pos = new MapLocation(m[1], m[2]);
        if (pos.distanceTo(rc.getLocation()) < rc.getType().bodyRadius) shouldStop = true;
    }

    //finding new target
    static void findBestTarget(){
        TreeInfo[] Ti = rc.senseNearbyTrees(-1,rc.getTeam().opponent());
        boolean sent = false;

        for (TreeInfo ti : Ti)
        {
            MapLocation enemyTree = ti.location;
            calculateNewTarget(enemyTree, Constants.ENEMYTREESCORE + Constants.eps, false);
            if(!sent)
            {
                sent = true;
                int x = Math.round(enemyTree.x);
                int y = Math.round(enemyTree.y);
                Communication.sendMessage(Communication.ENEMYTREECHANNEL, x, y, 0);
                ++initialMessageEnemyTree;
            }
        }

        Ti = rc.senseNearbyTrees(-1, Team.NEUTRAL);
        sent = false;
        for (TreeInfo ti : Ti)
        {
            RobotType rt = ti.getContainedRobot();
            MapLocation neutralTree = ti.location;
            if (rt == null)
            {
                calculateNewTarget(ti.location, Constants.RANDOMTREESCORE, false);
            } else
            {

                int val = rt.bulletCost;
                if(rt == RobotType.ARCHON) val = Constants.ARCHONVALUE;
                if(rt.bulletCost == 80) val = Constants.ARCHONVALUE; 
                calculateNewTarget(ti.location, val/Constants.CONVERSIONBULLETCOST + Constants.eps, false);
                if(!sent)
                {
                    sent = true;
                    int x = Math.round(neutralTree.x);
                    int y = Math.round(neutralTree.y);
                    Communication.sendMessage(Communication.TREEWITHGOODIES, x, y, val);
                    ++initialMessageGoodieTree;
                }
            }

        }

        sent = false;
        RobotInfo[] Ri = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        for(RobotInfo ri : Ri){
            MapLocation enemyPos = ri.getLocation();
            if(ri.getType().equals(RobotType.ARCHON)) enemyBase = enemyPos;
            calculateNewTarget(enemyPos, enemyScore(Constants.getIndex(ri.getType())) + Constants.eps, false);
            if(!sent)
            {
                sent = true;
                int x = Math.round(enemyPos.x);
                int y = Math.round(enemyPos.y);
                int a = Constants.getIndex(ri.type);
                if (a == 0)
                {
                    Communication.sendMessage(Communication.ENEMYGARDENERCHANNEL, x, y, 0);
                    ++initialMessageEnemyGardener;
                }
                else if (a == 5)
                {
                    Communication.sendMessage(Communication.ENEMYGARDENERCHANNEL, x, y, 5);
                    ++initialMessageEnemyGardener;
                }
                else
                {
                    Communication.sendMessage(Communication.ENEMYCHANNEL, x, y, a);
                    ++initialMessageEnemy;
                }
            }
        }
    }

    //check if it's a better target than what I haves
    static void calculateNewTarget(MapLocation target, float score, boolean greedy)
    {
        float dist1 = rc.getLocation().distanceTo(target) + Constants.ADDTODISTANCELUMBERJACK;
        float val = score/(dist1*dist1);
        if(val > maxUtil || (val == maxUtil && greedy == true))
        {
            scoreTarget = score;
            shouldGreedy = greedy;
            targetUpdated = true;
            maxUtil = val;
            newTarget = target;
        }
    }

    static float enemyScore(int m)
    {
        if(m == 5) return Constants.ARCHONSCORELUMBERJACK;
        if(m == 0) return Constants.GARDENERSCORELUMBERJACK;
        return 0;
    }

    static void updateTarget()
    {
        if(targetUpdated) roundTarget = rc.getRoundNum();
        realTarget = newTarget;
    }

    static void tryChop() {
        MapLocation pos = rc.getLocation();
        int cont = 0;
        int bestID = 0;
        float bestValue = 0;
        if(!shouldGreedy && realTarget != null && pos.distanceTo(realTarget) > Constants.eps)
        {
            TreeInfo[] Ti = rc.senseNearbyTrees(pos.add(pos.directionTo(realTarget), rc.getType().strideRadius), rc.getType().bodyRadius, null);
            for(TreeInfo ti : Ti)
            {
                if(!rc.canChop(ti.getID())) continue;
                if(ti.getTeam() == rc.getTeam()) continue;
                cont += Math.floor((ti.getHealth()-Constants.eps)/GameConstants.LUMBERJACK_CHOP_DAMAGE)+1;
                if(ti.getTeam() == rc.getTeam().opponent())
                {
                    float val = Constants.ENEMYTREESCORE - ti.getHealth()/100000.0f;
                    if(val > bestValue)
                    {
                        bestValue = val;
                        bestID = ti.ID;
                    }
                }
                if(ti.getTeam() == Team.NEUTRAL)
                {
                    float val = Constants.NEUTRALTREESCORE - ti.getHealth()/100000.0f;
                    if(val > bestValue)
                    {
                        bestValue = val;
                        bestID = ti.ID;
                    }
                }
            }

            if(cont > 1)shouldMove = false;
        }
        float strikeValue = 0;


        TreeInfo[] Ti = rc.senseNearbyTrees(2);
        for(TreeInfo ti : Ti)
        {
            if(!rc.canChop(ti.getID())) continue;
            if(ti.getTeam() == rc.getTeam().opponent())
            {
                float val = Constants.ENEMYTREESCORE;
                strikeValue += val;
                if(cont == 0)
                {
                    val = Constants.ENEMYTREESCORE - ti.getHealth()/100000.0f;
                    if(val > bestValue)
                    {
                        bestValue = val;
                        bestID = ti.ID;
                    }
                }
            }
            if(ti.getTeam() == rc.getTeam())
            {
                float val = Constants.ENEMYTREESCORE;
                strikeValue -= val;
            }
            if(ti.getTeam() == Team.NEUTRAL)
            {
                if(cont == 0)
                {
                    float val = Constants.NEUTRALTREESCORE - ti.getHealth()/100000.0f;
                    if(val > bestValue)
                    {
                        bestValue = val;
                        bestID = ti.ID;
                    }
                }
            }
        }

        RobotInfo[] Ri = rc.senseNearbyRobots(2);
        for(RobotInfo ri : Ri)
        {
            if(ri.getTeam() == rc.getTeam().opponent())
            {
                float val = 0.001f;
                if(ri.getType() != RobotType.ARCHON && ri.getType() != RobotType.SCOUT) val = ri.getType().bulletCost/ri.getType().maxHealth;
                strikeValue += val*1000.0f;
            }
            if(ri.getTeam() == rc.getTeam())
            {
                float val = 0.001f;
                if(ri.getType() != RobotType.ARCHON && ri.getType() != RobotType.SCOUT) val = ri.getType().bulletCost/ri.getType().maxHealth;
                strikeValue -= val*1000.0f;
            }
        }
        try{
            if(strikeValue >= 1000)
            {
                rc.strike();
                return;
            }
            if(bestValue > 0)
            {
                rc.chop(bestID);
                return;
            }
            if(strikeValue >= 0)
            {
                rc.strike();
                return;
            }
        }catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

    }
}
