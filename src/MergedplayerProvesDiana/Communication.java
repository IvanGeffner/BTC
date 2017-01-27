package MergedplayerProvesDiana;


import battlecode.common.*;

/**
 * Created by Ivan on 1/15/2017.
 */
public class Communication {

    static RobotController rc;

    /*Types of message*/
    static final int ENEMYCHANNEL = 1000;
    static final int STOPCHANNEL = 1100;
    static final int ENEMYGARDENERCHANNEL = 1200;
    static final int CHOPCHANNEL = 1300;
    static final int ENEMYTREECHANNEL = 1400;
    static final int EMERGENCYCHANNEL = 1500;
    static final int TREEWITHGOODIES = 1600;
    static final int PLANTTREECHANNEL = 1700;
    //demanar tropes
    static final int NEEDTROOPCHANNEL = 2000; 

    static final int CYCLIC_CHANNEL_LENGTH = 99;



    //CANALS GARDENERS

    //teoricament ocupo del 600 al 656 pero no estic segur
    static final int ZONE_FIRST_POSITION = 600;
    static final int ROBOTS_BUILT = 695;
    static final int TREES_BUILT = 696;

    static int ZONE_ORIGIN_X = 697;//coordenades del centre de la zona (0,0)
    static int ZONE_ORIGIN_Y = 698;//son les coordenades on spawneja el primer gardener

    static int MIN_ZONE_X = 691;//tenen els valors de les zones desfasats +-20 per tal que el 0 mai caigui entre minim i maxim
    static int MIN_ZONE_Y = 692;//osigui si la x de zona maxima es 6, aqui es guardara un -14
    static int MAX_ZONE_X = 693;//i si la de zona minima es -3 aqui es guardara un 17
    static int MAX_ZONE_Y = 694;
    static int ZONE_LIMIT_OFFSET = 20;

    static final int MAP_UPPER_BOUND = 700; // valors amb precisio de Constants.PRECISION_MAP_BOUNDS de
    static final int MAP_LOWER_BOUND = 701; // les coordenades x o y respecte l'archon 0 dels limits del mapa
    static final int MAP_LEFT_BOUND = 702; // son floats, s'obtenen a partir de l'int amb la funcio Float.intBitsToFloat
    static final int MAP_RIGHT_BOUND = 703;

    //ocupen 13 channels
    static final int SIGHT_ZONES = 704; // bits que indiquen si cada zona de visio ha estat explorada

    static final int GARDENER_REPORT = 750; // ultim torn que hem tingut gardeners
    static final int ARCHON_REPORT = 751;

    static final int[] ARCHON_INIT_SCORE = {760,761,762};


    
    static int xBase = 9999, yBase = 9999;


    //BC parameters
    //gardener lumberjack soldier tank scout archon trees
    static final int[] unitChannels = {501, 502, 503, 504, 505, 506, 507};
    static final int INITIALIZED = 507;

    static final int ARCHON_TURN = 508;
    static final int ARCHON_COUNT = 509;
    static final int ARCHONS_LAST_TURN = 510;

    static final int MAX_BROADCAST_MESSAGE = 500;


    static final int BUILDPATH = 515;

    //ask for units
    static final int NEEDSOLDIERTANK = 0; 
    static final int NEEDLUMBERJACK = 1; 
    
    static final float LUMBERJACKSCORE = 5.0f; 

    static final int typeMask = 0xF0000000; //at most 15
    static final int iOffMask = 0x0FF00000; //at most 255
    static final int jOffMask = 0x000FF000; //at most 255
    static final int iOffShift = 20;
    static final int jOffShift = 12;
    static final int valueMask = 0x00000FFF; // at most 4095


    static void init(RobotController rc2, int x, int y){
        rc = rc2;
        xBase = x;
        yBase = y;
    }

    public static int encodeFinding(int type, int iOffset, int jOffset, int value) {
        int ret = ((type&0xF) << 28)|
                (((iOffset + 127) & 0xFF) << iOffShift) | /*-100 <= iOffset <= 100*/
                (((jOffset + 127) & 0xFF) << jOffShift) |
                (value & 0xFFF);
        return ret;
    }

    public static int encodeFinding(int type, int iOffset, int jOffset) {
        return encodeFinding(type, iOffset, jOffset, 0);
    }

    public static int[] decode(int bitmap) {
        int[] ret = new int[4];
        ret[0] = (bitmap & typeMask) >> 28;
        ret[1] = ((bitmap & iOffMask) >> iOffShift) - 127 + xBase;
        ret[2] = ((bitmap & jOffMask) >> jOffShift) - 127 + yBase;
        ret[3] = (bitmap & valueMask);
        return ret;
    }

    //aquest no l'he canviat perque el greedy el fa servir
    public static void sendMessage(RobotController rc, int channel, int x, int y, int value) {
        try {
            if (rc == null || xBase == 9999){
                throw new GameActionException(GameActionExceptionType.CANT_DO_THAT,"ERROR: no pots enviar un missatge sense haver fet setBase");
            }
            int lastMessage = rc.readBroadcast(channel + CYCLIC_CHANNEL_LENGTH);
            int message = encodeFinding(Constants.getIndex(rc.getType()), x-xBase, y-yBase, value);
            rc.broadcast(channel + lastMessage, message);
            rc.broadcast(channel + CYCLIC_CHANNEL_LENGTH, (lastMessage + 1) % CYCLIC_CHANNEL_LENGTH);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    static void sendMessage(int channel, int x, int y, int value){
        sendMessage(rc,channel,x,y,value);
    }

    //cada torn les tropes envien el numero de torn, aixi sabem quan no en tenim
    static void sendReport(int channel){
        try {
            System.out.println("Envia report a " + channel);
            rc.broadcast(channel,rc.getRoundNum());
        } catch (GameActionException e) {
            e.printStackTrace();
        }
    }

    static boolean areArchonsAlive(){
        try {
            int lastRoundArchons = rc.readBroadcast(ARCHONS_LAST_TURN);
            return lastRoundArchons > 0;
        } catch (GameActionException e) {
            e.printStackTrace();
        }
        return false;
    }

    static boolean areGardenersAlive(){
        try {
            int lastRoundGardeners = rc.readBroadcast(GARDENER_REPORT);
            return lastRoundGardeners >= rc.getRoundNum() - 1;
        } catch (GameActionException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    //funcio per demanar unitats
    static void askForUnits()
    {
    	MapLocation me = rc.getLocation(); 
    	RobotInfo[] Ri = rc.senseNearbyRobots(); 
    	float soldiertank = 0.0f;
    	float lumberjack = 0.0f; 
    	boolean found = false; 
    	for(RobotInfo ri : Ri)
    	{
    		if(ri.getTeam().equals(rc.getTeam())) 
    		{
    			soldiertank -= dangerScore(Constants.getIndex(ri.getType()));
    			if(ri.getType().equals(RobotType.LUMBERJACK)) lumberjack -= LUMBERJACKSCORE; 
    		}
    		if(ri.getTeam().equals(rc.getTeam().opponent())) 
    		{
    			found = true; 
    			soldiertank += dangerScore(Constants.getIndex(ri.getType()));
    		}
    	}
    	soldiertank -= dangerScore(Constants.getIndex(rc.getType()));
    	if(soldiertank >= 0.0f && found)
    	{
    		int x = Math.round(me.x);
            int y = Math.round(me.y);
        	sendMessage(rc, NEEDTROOPCHANNEL, x, y, NEEDSOLDIERTANK);
    	}
    	

    	
    	TreeInfo[] Ti = rc.senseNearbyTrees();
    	found = false;
    	for(TreeInfo ti : Ti)
    	{
    		if(ti.getTeam().equals(rc.getTeam())) continue; 
    		
    		if(ti.getTeam().equals(Team.NEUTRAL) && ti.containedRobot != null) 
    		{
    			found = true; 
    			lumberjack += unitTreeScore(Constants.getIndex(ti.containedRobot));
    		}
    	}
    	
    	if(rc.getType().equals(RobotType.LUMBERJACK)) lumberjack -= LUMBERJACKSCORE; 
    	
    	if(found && lumberjack >= 0)
    	{
    		int x = Math.round(me.x);
            int y = Math.round(me.y);
        	sendMessage(rc, NEEDTROOPCHANNEL, x, y, NEEDLUMBERJACK);
    	}
    }
    
    static float dangerScore(int rt)
    {
    	if(rt == 4) return 1.0f; //scouts
    	//if(rt == 0) return 2; //granjers?
    	if(rt == 2) return 5.0f; //soldier mega important
    	if(rt == 3) return 10.0f; // tank hiper mega important
    	return 0.0f; //archons (i granjers) 0
    }
    
    static float unitTreeScore(int rt)
    {
    	if(rt == 0) return 1.0f; 
    	if(rt == 2) return 2.5f; 
    	if(rt == 3) return 5.0f; 
    	return 0.0f; //sudando d'scouts i archons
    }
}
