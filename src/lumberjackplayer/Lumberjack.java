package lumberjackplayer;

import battlecode.common.*;


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
	static float greedyRatio;
	static int xBase;
	static int yBase;

	static float minDistToTarget;
	static float maxDistToTarget;
	static int stopGreedying;
	static float stayChopping = 4;

	//static HashSet<Integer> readMes;
	static int initialMessageGoodieTree;
	static int initialMessageEnemyGardener;
	static int initialMessageChop;
	static int initialMessageEnemy;
	static int initialMessageStop;

	static float maxUtil;
	static boolean shouldMove;
	static boolean dontMove;

	static int round;

	static boolean shouldStop = false;

	@SuppressWarnings("unused")
	public static void run(RobotController rcc) {
		rc = rcc;
		Initialize();

		while (true) {
			//code executed continually, don't let it end
			shouldStop = false;

			beginRound();

			readMessages();
			broadcastLocations();
			findBestTree();
			updateTarget();

			//rc.setIndicatorLine(rc.getLocation(), realTarget, 250, 0, 255);

			tryChop();
			if(!shouldStop){ //TODO
				if(stopGreedying < 10 && minDistToTarget > 15 && changeTarget < 2.0f*maxDistToTarget*minDistToTarget/(1.0f + rc.getLocation().distanceTo(realTarget))) shouldMove = true;
				if (shouldMove) Greedy.moveGreedy(rc,realTarget,Clock.getBytecodesLeft()); //TODO canviar bytecode
				else Greedy.moveToSelf(rc,Clock.getBytecodesLeft());
			}

			if(rc.getLocation().distanceTo(realTarget) < minDistToTarget)
			{
				stopGreedying = 0;
				minDistToTarget = rc.getLocation().distanceTo(realTarget);
			}

			if(rc.getLocation().distanceTo(realTarget) < Constants.eps) //remirar target si el sensejo
			{
				realTarget = enemyBase;
				maxUtil = 6.0f/(1.0f + rc.getLocation().distanceTo(enemyBase));
			}

			Clock.yield();
		}
	}

	static void Initialize(){
		enemyBase = rc.getInitialArchonLocations(rc.getTeam().opponent())[0];
		newTarget = enemyBase;
		maxUtil = 6.0f/(1.0f + rc.getLocation().distanceTo(enemyBase));
		changeTarget  = 0;
		stopGreedying = 0;
		minDistToTarget = Constants.INF;
		maxDistToTarget = rc.getLocation().distanceTo(enemyBase);
		greedyRatio = 0;
		base = rc.getInitialArchonLocations(rc.getTeam())[0];;
		xBase = Math.round(base.x);
		yBase = Math.round(base.y);
		Communication.setBase(xBase, yBase);


		initialMessageGoodieTree = 0;
		initialMessageEnemyGardener = 0;
		initialMessageChop = 0;
		initialMessageEnemy = 0;
		initialMessageStop = 0;
		try{
			initialMessageGoodieTree = rc.readBroadcast(Communication.TREEWITHGOODIES + Communication.CYCLIC_CHANNEL_LENGTH);
			initialMessageEnemyGardener = rc.readBroadcast(Communication.ENEMYGARDENERCHANNEL + Communication.CYCLIC_CHANNEL_LENGTH);
			initialMessageChop = rc.readBroadcast(Communication.CHOPCHANNEL + Communication.CYCLIC_CHANNEL_LENGTH);
			initialMessageEnemy = rc.readBroadcast(Communication.ENEMYCHANNEL + Communication.CYCLIC_CHANNEL_LENGTH);
			initialMessageStop = rc.readBroadcast(Communication.STOPCHANNEL + Communication.CYCLIC_CHANNEL_LENGTH);

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
		enemyBaseUtil =  6.0f/(1.0f + rc.getLocation().distanceTo(enemyBase));
		//maxUtil = enemyBaseUtil;
		//attackingArchon = true;
		dontMove = false;
	}

	static void tryChop(){

		int chopID = -1;
		float strikeUtil = 0;
		float chopUtil = 0;

		boolean obstacleTree = false;
		Direction desired = new Direction(rc.getLocation(),realTarget);

		TreeInfo[] Ti = rc.senseNearbyTrees(2);
		RobotInfo[] Ri = rc.senseNearbyRobots(2);

		for (TreeInfo ti: Ti){
			if (!rc.canChop(ti.getID())) continue; //break?
			if (!ti.getTeam().equals(rc.getTeam()))
			{
				if (ti.getTeam().equals(rc.getTeam().opponent())){
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

				if(!obstacleTree)
				{
					MapLocation m2 = ti.getLocation();
					Direction dir = new Direction(rc.getLocation(),m2);

					float a = Math.abs(desired.radiansBetween(dir));
					if(a < Math.PI/6)
					{
						obstacleTree = true;
						chopUtil = 10;
						chopID = ti.getID();
						//if(attackingArchon)rc.setIndicatorDot(rc.getLocation(), 100, 0, 21);
						if(rc.getLocation().distanceTo(realTarget) <= minDistToTarget + stayChopping) shouldMove = false; //TODO func to do greedy at beginning
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
				if(tree.location.distanceTo(realTarget) < Constants.eps) myTarget = true;
				rc.chop(chopID);
				if(!rc.canSenseTree(chopID))
				{
					changeTarget = 0;
					stopGreedying = 0;
					maxDistToTarget = rc.getLocation().distanceTo(realTarget);
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
				//shouldMove = false;
				rc.strike();
				//rc.setIndicatorDot(rc.getLocation(),255,0,0);
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

	static void readMessages(){
		try {
			int channel = Communication.TREEWITHGOODIES;
			int lastMessage = rc.readBroadcast(channel + Communication.CYCLIC_CHANNEL_LENGTH);
			for(int i = initialMessageGoodieTree; i != lastMessage && Clock.getBytecodesLeft() > Constants.BYTECODEPOSTMESSAGES;)
			{
				int a = rc.readBroadcast(channel + i);
				workMessageUnitTree(a);
				++i;
			}
			initialMessageGoodieTree = lastMessage;
		} catch (GameActionException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		try {
			int channel = Communication.CHOPCHANNEL;
			int lastMessage = rc.readBroadcast(channel + Communication.CYCLIC_CHANNEL_LENGTH);
			for(int i = initialMessageChop; i != lastMessage && Clock.getBytecodesLeft() > Constants.BYTECODEPOSTMESSAGES;)
			{
				int a = rc.readBroadcast(channel + i);
				workMessageChopTree(a);
				++i;
			}
			initialMessageChop = lastMessage;
		} catch (GameActionException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		try {
			int channel = Communication.ENEMYGARDENERCHANNEL;
			int lastMessage = rc.readBroadcast(channel + Communication.CYCLIC_CHANNEL_LENGTH);
			for(int i = initialMessageEnemyGardener; i != lastMessage && Clock.getBytecodesLeft() > Constants.BYTECODEPOSTMESSAGES;)
			{
				int a = rc.readBroadcast(channel + i);
				workMessageEnemyTree(a);
				++i;
			}
			initialMessageEnemyGardener = lastMessage;
		} catch (GameActionException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		try {
			int channel = Communication.ENEMYCHANNEL;
			int lastMessage = rc.readBroadcast(channel + Communication.CYCLIC_CHANNEL_LENGTH);
			for(int i = initialMessageEnemy; i != lastMessage && Clock.getBytecodesLeft() > Constants.BYTECODEPOSTMESSAGES;)
			{
				int a = rc.readBroadcast(channel + i);
				workMessageEnemyUnit(a);
				++i;
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
		} catch (GameActionException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

	static void workMessageUnitTree(int a)
	{
		int[] m = Communication.decode(a);
		MapLocation unitTreePos = new MapLocation(m[1], m[2]);
		float val = unitTreeScore(rc.getLocation().distanceTo(unitTreePos),m[3]);
		if(val > maxUtil)
		{
			maxUtil = val;
			newTarget = unitTreePos;
		}
	}
	static void workMessageEnemyTree(int a)
	{
		int[] m = Communication.decode(a);
		MapLocation enemyTreePos = new MapLocation(m[1], m[2] );
		float val = 7.0f/(1.0f + rc.getLocation().distanceTo(enemyTreePos));
		if(val > maxUtil)
		{
			maxUtil = val;
			newTarget = enemyTreePos;
		}
	}

	static void workMessageChopTree(int a){
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
		float val = 2.5f/((1.0f + rc.getLocation().distanceTo(enemyPos)));
		if (val > maxUtil){
			maxUtil = val;
			newTarget = enemyPos;
		}
	}

	static void workMessageEnemyUnit(int a)
	{
		int[] m = Communication.decode(a);
		if(m[3] == Constants.getIndex(RobotType.ARCHON))
		{
			MapLocation newArchon = new MapLocation(m[1], m[2]);
			if(!archonCertainty)
			{
				archonCertainty = true;
				enemyBase = newArchon;
				enemyBaseUtil = 6.0f/(1.0f + rc.getLocation().distanceTo(enemyBase));
				if(enemyBaseUtil > maxUtil)
				{
					maxUtil = enemyBaseUtil;
					newTarget = enemyBase;
				}
			} else
			{
				float val = 6.0f/(1.0f + rc.getLocation().distanceTo(newArchon));
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

	static void findBestTree(){

		MapLocation pos = rc.getLocation();
		TreeInfo[] Ti = rc.senseNearbyTrees();

		for (TreeInfo ti : Ti){
			if (ti.getTeam() == rc.getTeam()) continue;
			else if (ti.getTeam() == rc.getTeam().opponent()){
				float newUtil = 7.0f/(1.0f + pos.distanceTo(ti.getLocation())); //TODO ara mateix vaig igual granger que arbre (vull primer granger)
				if (newUtil > maxUtil){
					maxUtil = newUtil;
					newTarget = ti.getLocation();
				}
			}
			else{
				if(ti.containedRobot != null)
				{
					float a = ti.containedRobot.bulletCost;
					if(ti.containedRobot == RobotType.ARCHON) a = 1000;
					float val = unitTreeScore(rc.getLocation().distanceTo(ti.getLocation()),a);
					if(val > maxUtil)
					{
						maxUtil = val;
						newTarget = ti.getLocation();
					}
				}
			}
		}

		RobotInfo[] Ri = rc.senseNearbyRobots();
		{
			for(RobotInfo ri : Ri)
			{
				if (ri.getTeam() == rc.getTeam()) continue;
				else
				{
					if(ri.getType().equals(RobotType.ARCHON))
					{
						MapLocation newArchon = ri.getLocation();
						if(!archonCertainty)
						{
							archonCertainty = true;
							enemyBase = newArchon;
							enemyBaseUtil = 6.0f/(1.0f + rc.getLocation().distanceTo(enemyBase));
							if(enemyBaseUtil > maxUtil)
							{
								maxUtil = enemyBaseUtil;
								newTarget = enemyBase;
							}
						} else
						{
							float val = 6.0f/(1.0f + rc.getLocation().distanceTo(newArchon));
							if(val > enemyBaseUtil)
							{
								enemyBase = newArchon;
								enemyBaseUtil = val;
								if(val > maxUtil)
								{
									maxUtil = val;
									newTarget = newArchon;
								};
							}
						}
					}
					if(ri.getType().equals(RobotType.GARDENER))
					{
						float newUtil = 8.0f/(1.0f + pos.distanceTo(ri.getLocation())); //TODO ara mateix vaig igual granger que arbre (vull primer granger)
						if (newUtil > maxUtil){
							maxUtil = newUtil;
							newTarget = ri.getLocation();
						}
					}
				}
			}
		}
	}

	//TODO supposing it works...
	static void broadcastLocations() {
		if (round != rc.getRoundNum()) return;
		RobotInfo[] Ri = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
		for (RobotInfo ri : Ri) {
			if (Clock.getBytecodesLeft() < Constants.SAFETYMARGIN) return;
			MapLocation enemyPos = ri.getLocation();
			int x = Math.round(enemyPos.x);
			int y = Math.round(enemyPos.y);
			int a = Constants.getIndex(ri.type);
			if (a == 0)
			{
				Communication.sendMessage(rc, Communication.ENEMYGARDENERCHANNEL, x, y, 0);
				++initialMessageEnemyGardener;
			}
			else
			{
				Communication.sendMessage(rc, Communication.ENEMYCHANNEL, x, y, a);
				++initialMessageEnemy;
			}

		}

		TreeInfo[] Ti = rc.senseNearbyTrees(-1, rc.getTeam().opponent());
		for (TreeInfo ti : Ti) {
			if (Clock.getBytecodesLeft() < Constants.SAFETYMARGIN) return;
			MapLocation treePos = ti.getLocation();
			int x = Math.round(treePos.x);
			int y = Math.round(treePos.y);
			Communication.sendMessage(rc, Communication.ENEMYTREECHANNEL, x, y, 0);
			++initialMessageEnemyGardener;
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
				++initialMessageGoodieTree;
			}
		}
	}

	static void updateTarget(){
		if (realTarget != null && newTarget != null && newTarget.distanceTo(realTarget) < Constants.eps)
		{
			if(rc.getLocation().distanceTo(realTarget) - minDistToTarget > Constants.eps) ++stopGreedying;
			++changeTarget;
			return;
		}
		if(newTarget != null && realTarget != null && newTarget.distanceTo(realTarget) > 50)
		{
			maxDistToTarget = rc.getLocation().distanceTo(realTarget);
			stopGreedying = 0;
			changeTarget = 0;
		}
		minDistToTarget = Constants.INF;
		realTarget = newTarget;
		Greedy.resetObstacle(rc);
	}

	static float unitTreeScore(float dist, float rt)
	{
		if(rt >= 150) rt = 150;
		return rt/(25*(1.0f + dist));
	}

	static void workMessageStop(int a){
		int[] m = Communication.decode(a);
		MapLocation pos = new MapLocation(m[1], m[2]);
		if (pos.distanceTo(rc.getLocation()) < rc.getType().bodyRadius) shouldStop = true;
	}


}
