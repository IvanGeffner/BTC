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
    static int changeTarget; 
    static int roundsSameTarget = 50; 

    static MapLocation base;
    static MapLocation enemyBase;
    static float enemyBaseUtil;
    static boolean archonCertainty; 
    static boolean attackingArchon; 
    static int xBase;
    static int yBase;
    
    static float minDistToTarget; 
    static float stayChopping = 2; 

    static HashSet<Integer> readMes;
    static int initialMessage = 0;

    static float maxUtil;
    static boolean shouldMove;
    static boolean dontMove; 

    static int round;

    @SuppressWarnings("unused")
    public static void run(RobotController rcc) {
        rc = rcc;
        Initialize();

        while (true) {
            //code executed continually, don't let it end

        	beginRound();
            
            readMessages();
            broadcastLocations();
            findBestTree();
            updateTarget();
            
            rc.setIndicatorLine(rc.getLocation(), realTarget, 250, 0, 255);
            
            tryChop();
            
            if(!dontMove)
            {
	            if (shouldMove) Greedy.moveGreedy(rc,realTarget,Clock.getBytecodesLeft()); //TODO canviar bytecode si fa falta
	            else Greedy.moveToSelf(rc,Clock.getBytecodesLeft());
	            askToStop(); 
            }
            
            if(rc.getLocation().distanceTo(realTarget) < minDistToTarget) minDistToTarget = rc.getLocation().distanceTo(realTarget);
            
            Clock.yield();
        }
    }

    static void Initialize(){
        enemyBase = rc.getInitialArchonLocations(rc.getTeam().opponent())[0];
        newTarget = enemyBase;
        maxUtil = 0.5f/(1.0f + rc.getLocation().distanceTo(enemyBase));
        changeTarget  = 0; 
        minDistToTarget = Constants.INF; 
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
    
    static void beginRound()
    {
    	round = rc.getRoundNum();
        shouldMove = true;
        archonCertainty = false; 
        enemyBaseUtil =  0.5f/(1.0f + rc.getLocation().distanceTo(enemyBase)); 
        maxUtil = enemyBaseUtil; 
        attackingArchon = true; 
        dontMove = false; 
    }
    
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
		            MapLocation m2 = ti.getLocation();
	            	Direction dir = new Direction(rc.getLocation(),m2); 

	            	float a = desired.radiansBetween(dir); 
	            	if(a < 0) a = -a;	            	
	            	if(a < Math.PI/6)
	            	{
	            		rc.setIndicatorLine(rc.getLocation(),ti.getLocation(),255,0,0);
	            		obstacleTree = true; 
	            		chopUtil = 10; 
	            		chopID = ti.getID();
	            		if(attackingArchon)rc.setIndicatorDot(rc.getLocation(), 100, 0, 21);
	            		if(rc.getLocation().distanceTo(realTarget) <= minDistToTarget + stayChopping) shouldMove = false; 
	            	}
	            }
            }
            else strikeUtil -= 4;
        }

        for (RobotInfo ri : Ri){
            if (ri.getID() == rc.getID()) continue;
            if (ri.getTeam() == rc.getTeam()){
            	if(ri.getType().equals(RobotType.ARCHON)) strikeUtil -=10; 
            	else strikeUtil -= ((float)ri.getType().bulletCost*2.0f)/(ri.getType().maxHealth);
            }
            else if (ri.getTeam() == rc.getTeam().opponent()){
            	if(ri.getType().equals(RobotType.ARCHON)) strikeUtil +=10; 
            	else strikeUtil += ((float)ri.getType().bulletCost*2.0f)/(ri.getType().maxHealth);
            }
        }
        
        try {
            if (chopUtil > strikeUtil && chopUtil > 0) {
            	TreeInfo tree = rc.senseTree(chopID);
            	boolean myTarget = false; 
            	if(tree.location.equals(realTarget)) myTarget = true; 
                rc.chop(chopID);
                if(!rc.canSenseTree(chopID))
                {
                	changeTarget = 0; 
                	Greedy.resetObstacle(rc);
                	if(myTarget)
                	{
                		attackingArchon = true; 
                		realTarget = enemyBase; 
                		maxUtil = enemyBaseUtil; 
                	}
                }
                
            }
            else if (strikeUtil > 0) {
            	shouldMove = false; 
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
    	
    	//POSSIBLE TARGETS
    	if(m[0] == Communication.UNITTREE)
    	{
    		MapLocation unitTreePos = new MapLocation(m[1] + xBase, m[2] + yBase);
    		float val = unitTreeScore(rc.getLocation().distanceTo(unitTreePos),m[3]);
    		if(val > maxUtil)
    		{
    			attackingArchon = false; 
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
            	attackingArchon = false;
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
    			attackingArchon = false;
    			maxUtil = val; 
    			newTarget = treeInZonePos;
    		}
    	}
    	if(m[0] == Communication.ENEMY)
    	{
    		if(m[3] == Constants.getIndex(RobotType.ARCHON))
    		{
    			MapLocation newArchon = new MapLocation(m[1] + xBase, m[2] + yBase); 
    			if(!archonCertainty)
    			{
    				archonCertainty = true;
    				enemyBase = newArchon;
    				 enemyBaseUtil = 0.5f/(1.0f + rc.getLocation().distanceTo(enemyBase));
    				 if(enemyBaseUtil > maxUtil)
    		    		{
    		    			attackingArchon = true;
    		    			maxUtil = enemyBaseUtil; 
    		    			newTarget = enemyBase;
    		    		}
    			} else
    			{
    				float val = 0.5f/(1.0f + rc.getLocation().distanceTo(newArchon)); 
    				if(val > enemyBaseUtil)
    				{
    					enemyBase = newArchon; 
    					enemyBaseUtil = val; 
    					if(val > maxUtil)
    					{
    		    			attackingArchon = true;
    		    			maxUtil = val; 
    		    			newTarget = newArchon;
    		    		};
    				}
    			}
    		}
    	}
    	
    	//STOP
    	if(m[0] == Communication.STOP)
    	{
    		MapLocation bodering = new MapLocation(m[1] + xBase, m[2] + yBase); 
    		if(bodering.equals(rc.getLocation()))
    		{
    			dontMove = true; 
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
                	attackingArchon = false; 
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
	        			attackingArchon = false;
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
            
            if(ri.type == RobotType.ARCHON) //TODO fer una function
            {
            	MapLocation newArchon = new MapLocation(x,y); 
            	enemyBase = newArchon;
				 enemyBaseUtil = 0.5f/(1.0f + rc.getLocation().distanceTo(enemyBase));
				 if(enemyBaseUtil > maxUtil)
		    	{
		    		attackingArchon = true;
		    		maxUtil = enemyBaseUtil; 
		   			newTarget = enemyBase;
		   		}
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

    static void updateTarget(){
        if (realTarget != null && newTarget != null && newTarget.distanceTo(realTarget) < Constants.eps) 
        {
        	++changeTarget;
        	if(changeTarget > roundsSameTarget)
        	{
        		changeTarget = 0; 
        		newTarget = enemyBase; 
        		realTarget = enemyBase;
        		maxUtil = 0.5f/(1.0f + rc.getLocation().distanceTo(enemyBase));
        	}
        	return;
        }
        
        minDistToTarget = Constants.INF; 
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

    
    static void askToStop()
    {
    	if(Clock.getBytecodesLeft() < Constants.SAFETYMARGIN) return; 
    	if(Greedy.obstacle != null) 
    	{
    		if(rc.canSenseLocation(Greedy.obstacle)) 
    		{
    			try {
					RobotInfo ri = rc.senseRobotAtLocation(Greedy.obstacle);
					if(ri != null)
					{
						int x = Math.round(Greedy.obstacle.x); 
			    		int y = Math.round(Greedy.obstacle.y);
			    		int m = Communication.encodeFinding(Communication.STOP, x - xBase, y - yBase, Constants.getIndex(RobotType.LUMBERJACK)); 
			    		rc.broadcast(initialMessage, m);
			    		++initialMessage;
			    		if (initialMessage >= Communication.MAX_BROADCAST_MESSAGE) initialMessage -= Communication.MAX_BROADCAST_MESSAGE;
					}
				} catch (GameActionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		}
    	}
    }
    
}
