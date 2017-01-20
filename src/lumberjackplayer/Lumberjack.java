package lumberjackplayer;

import battlecode.common.*;

import java.util.HashSet;


/**
 * Created by Ivan on 1/9/2017.
 * NOU!
 */
public class Lumberjack {

    static RobotController rc;
    private static int strikeRadius = 2; 
    
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
            
            readMessages();
            broadcastLocations();
            findBestTree();
            updateTarget();
            rc.setIndicatorLine(rc.getLocation(), realTarget, 250, 0, 255);
            
            //TODO el que vull fer es que nomes li digui que no es mogui si l'arbre esta pel mig
            tryChop();
            
            if (shouldMove) Greedy.moveGreedy(rc,realTarget,9200);
            else Greedy.moveToSelf(rc,9200);

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
    
   //TODO cal canviar algunes coses de si es pot moure
    static void tryChop(){

        int chopID = -1;
        float strikeUtil = 0;
        float chopUtil = 0;
        
        boolean obstacleTree = false; 
        Direction desired = new Direction(rc.getLocation(),realTarget);

        TreeInfo[] Ti = rc.senseNearbyTrees(rc.getType().strideRadius+rc.getType().bodyRadius);
        RobotInfo[] Ri = rc.senseNearbyRobots(rc.getType().strideRadius+rc.getType().bodyRadius);

        for (TreeInfo ti: Ti){
            if (!rc.canChop(ti.getID())) continue; //break?
            if (!ti.getTeam().equals(rc.getTeam()))
            {
	            if (ti.getTeam() ==  rc.getTeam().opponent()){
	                strikeUtil += 4;
	                if (chopUtil < 10 && rc.canChop(ti.getID()) && !obstacleTree){
	                    chopUtil = 10;
	                    chopID = ti.getID();
	                }
	            }
	            else {
	                if (chopUtil < 5.0f*ti.getRadius() && !obstacleTree){
	                    chopUtil = 5.0f*ti.getRadius();
	                    chopID = ti.getID();
	                }
	            }
	            
	            if(!obstacleTree && rc.canChop(ti.getID()))
	            {
	            	Direction dir = new Direction(rc.getLocation(),ti.getLocation()); 
	            	float a = desired.radiansBetween(dir); 
	            	if(a < 0) a = -a;
	            	
	            	if(a < Math.PI/4) //de moment he posat pi/4, pero potser si fem tanks mes currats aniria be pi/2 i aixi fan cami? 
	            	{
	            		rc.setIndicatorLine(rc.getLocation(),ti.getLocation(),255,0,0);
	            		obstacleTree = true; 
	            		chopUtil = 10; 
	            		chopID = ti.getID();
	            		shouldMove = false; 
	            		obstacleTree = true; 
	            	}
	            }
            }
        }

        for (RobotInfo ri : Ri){
            if (ri.getID() == rc.getID()) continue;
            if (ri.getTeam() == rc.getTeam()){
                strikeUtil -= ((float)ri.getType().bulletCost*2.0f)/(ri.getType().maxHealth);
            }
            else if (ri.getTeam() == rc.getTeam().opponent()){
                strikeUtil += ((float)ri.getType().bulletCost*2.0f)/(ri.getType().maxHealth);
            }
        }
        
        //check if obstacle TODO do it well
        /*
        MapLocation myLoc = rc.getLocation();
        Direction dir = new Direction(myLoc,realTarget);
        boolean foundObstacle = false; 
        for(int i = 0; i < 4 && !foundObstacle; ++i)
        {
	        MapLocation possibleObstacle = myLoc.add(dir,rc.getType().bodyRadius+(float)i*0.5f);
	
	        try {
				TreeInfo ti = rc.senseTreeAtLocation(possibleObstacle);
				if(ti != null) foundObstacle = true; 
				if(ti != null && !ti.getTeam().equals(rc.getTeam()))
				{
					if(ti.getTeam().equals(rc.getTeam().opponent()))
					{
						chopUtil = 10;
	                    chopID = ti.getID();
	                    shouldMove = false;
					} else
					{
						chopUtil = 5.0f*ti.getRadius();
		                chopID = ti.getID();
						shouldMove = false; 
					}
				}
			} catch (GameActionException e) {
				e.printStackTrace();
			}
        }
        */
        
        try {
            if (chopUtil > strikeUtil && chopUtil > 0) {
                rc.chop(chopID);
                TreeInfo treeInfo = rc.senseTree(chopID);
                
            }
            else if (strikeUtil > 0) {
                rc.strike();
                rc.setIndicatorDot(rc.getLocation(),255,0,0);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
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
    	if(m[0] == Communication.UNITTREE)
    	{
    		MapLocation unitTreePos = new MapLocation(m[1] + xBase, m[2] + yBase);
    		float val = unitTreeScore(rc.getLocation().distanceTo(unitTreePos),m[3]);
    		if(val > maxUtil)
    		{
    			maxUtil = val; 
    			newTarget = unitTreePos;
    		}
    	}
    	if(m[0] == Communication.ENEMYTREE)
    	{
    		MapLocation enemyTreePos = new MapLocation(m[1]+xBase, m[2]+yBase);
            float val = 2.0f/(1.0f + rc.getLocation().distanceTo(enemyTreePos));
            if(val > maxUtil)
    		{
    			maxUtil = val; 
    			newTarget = enemyTreePos;
    		}
    	}
    	if(m[0] == Communication.TREEZONE)
    	{
    		MapLocation treeInZonePos = new MapLocation(m[1] + xBase, m[2] + yBase); 
    		float val = 1.0f/(1.0f + rc.getLocation().distanceTo(treeInZonePos));
    		if(val > maxUtil)
    		{
    			maxUtil = val; 
    			newTarget = treeInZonePos;
    		}
    	}
    	
    }
    
    static void findBestTree(){

        MapLocation pos = rc.getLocation();
        TreeInfo[] Ti = rc.senseNearbyTrees();

        for (TreeInfo ti : Ti){
            if (ti.getTeam() == rc.getTeam()) continue;
            else if (ti.getTeam() == rc.getTeam().opponent()){
                float newUtil = 2.0f/(1.0f + pos.distanceTo(ti.getLocation()));
                if (newUtil > maxUtil){
                    maxUtil = newUtil;
                    newTarget = ti.getLocation();
                }
            }
            else{
            	if(ti.containedRobot != null)
            	{
	        		float val = unitTreeScore(rc.getLocation().distanceTo(ti.getLocation()),Constants.getIndex(ti.containedRobot));
	        		if(val > maxUtil)
	        		{
	        			maxUtil = val; 
	        			newTarget = ti.getLocation();
	        		}
            	}
            }
        }
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

    
    
    
    
    
    
    
    
    //antic
    



    static void updateTarget(){
        if (realTarget != null && newTarget != null && newTarget.distanceTo(realTarget) < Constants.eps) return;
        realTarget = newTarget;
        Greedy.resetObstacle(rc);
    }

    
    static float unitTreeScore(float dist, float rt)
    {
    	//order of most wanted: scout < gardener < lumberjack < soldier < tank < archon
    	if(rt == 4) return 3.5f/(1.0f + dist); 
    	if(rt == 0) return 4.0f/(1.0f + dist);
    	if(rt == 1) return 4.5f/(1.0f + dist);
    	if(rt == 2) return 5.0f/(1.0f + dist);
    	if(rt == 3) return 5.5f/(1.0f + dist);
    	if(rt == 5) return 6.0f/(1.0f + dist);
    	return -1; 
    }

    
}
