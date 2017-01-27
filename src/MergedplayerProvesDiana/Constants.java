package MergedplayerProvesDiana;

import battlecode.common.Direction;
import battlecode.common.RobotType;

/**
 * Created by Ivan on 1/15/2017.
 */
public class Constants {
    public static int DEBUG = 1; // posar-ho a 0 quan es pugi el player a la web
    //controla si es fan els indicator dots i lines

    public static float eps = 0.001f;
    public static float INF = 9999f;
    public static Direction[] main_dirs = {Direction.getEast(), Direction.getNorth(), Direction.getSouth(), Direction.getWest()};

    public static RobotType[] ProductionUnits = {RobotType.GARDENER, RobotType.LUMBERJACK, RobotType.SOLDIER, RobotType.TANK, RobotType.SCOUT};
    //public static int[] initialBuild = {0, 2, 2, 6, 4, 6, 1, 4, 2, 1, 6, 6,  6, 6, 0, 6, 2, 1, 6, 6, 2, 1, 6}; //22
    //public static int[] initialPositions = {0, 6, 1, 33, 4, 9999, 3};
    //public static int[] sequenceBuild = {2, 6, 6, 2, 6, 0, 2, 6, 1, 6, 3 ,6};


    //gardener 0, lumbi 1, soldier 2, tank 3, scout 4, archon 5, 6 soldat
    public static int[] rushInitBuild = {-1,0,2,2,6,2,6,6,6,6,6,6};
    public static int[] rushSeqBuild =  {0,6,6,6,6,6,6,6,2,2,3};

    public static int[] closeCagedInitBuild = {-1,0,2,2,6,2,6,6,6,6,6,6};
    public static int[] closeCagedSeqBuild = {0,6,6,6,6,6,6,6,2,2,3};

    public static int[] farOpenInitBuild = {-1,0,2,2,6,2,6,6,6,6,6,6};
    public static int[] farOpenSeqBuild = {0,6,6,6,6,6,6,6,2,2,3};

    public static int[] farCagedInitBuild = {-1,0,2,6,2,2,6,6,6,6,6,6};
    public static int[] farCagedSeqBuild =  {0,6,6,6,6,6,6,6,2,2,3};

    public static int[] balancedInitBuild = {-1,0,2,2,6,2,6,6,6,6,6,6};
    public static int[] balancedSeqBuild = {0,6,6,6,6,6,6,6,2,2,3};

    public static int[][] initBuilds = {rushInitBuild,closeCagedInitBuild,farOpenInitBuild,farCagedInitBuild,balancedInitBuild};
    public static int[][] seqBuids = {rushSeqBuild,closeCagedSeqBuild,farOpenSeqBuild,farCagedSeqBuild,balancedSeqBuild};

    public static int RUSH_BUILD = 0;
    public static int CLOSE_CAGED_BUILD = 1;
    public static int FAR_OPEN_BUILD = 2;
    public static int FAR_CAGED_BUILD = 3;
    public static int BALANCED_BUILD = 4;




    //public static int[] initialBuild = {0,4,1,2,4,2,0};
    //public static int[] initialPositions = {0,2,3,14,1,9999};
    //public static int[] sequenceBuild = {1,2,2,2,3,2,2,3,0};

    public static int MAX_ROBOTS = 250;
    public static int LAST_ROUND_BUILD = 2900;
    public static float BULLET_LIMIT = 2500;

    //public static int IBL = initialBuild.length;
    //public static int SBL = sequenceBuild.length;

    public static float rotationAngle = 4.0f*(float)Math.PI/13.0f;
    public static float pushTarget = 5.0f;

    //SOLDIERS, TANKS
    public static float ENEMYBASESCORE = 0.0001f;
    public static int BYTECODEPOSTENEMYMESSAGES = 9250;
    public static int BYTECODEPOSTEMERGENCYMESSAGES = 9000;
    public static int BYTECODEPOSTSTOPMESSAGES = 8750;
    public static int BYTECODEPOSTENEMYGARDENERMESSAGES = 8500;
    public static int BROADCASTMAXSOLDIER = 600;

    public static int SouthTree = 4;
    public static int NorthTree = 6;
    public static float minHPWater = 45f;
    public static float minHPGoWater = 25f;
    public static int TREEBUCLEBYTE = 1000;
    public static int BYTECODEPOSTMESSAGES = 9000;
    public static int SAFETYMARGIN = 7500;
    public static int SAFETYMARGINCHECKTREES = 4000;
    public static int SAFETYMARGINSCOUTS = 400;
    public static int LUMBERCHECK = 5000;
    public static int CHANGETARGET = 30;
    public static float NEWTARGET = 2.01f;
    public static float BULLETSIGHT = 4;
    public static int ANGLEFACTOR = (1 << 13);
    public static int INTINF = (1 << 27);

    public static float COLLISIONDIST = 0.75f;
    public static float COLLISIONRANGE = 1.2f;
    public static int COLLISIONHASH = 1000;
    public static int COLLISIONROUND = 5;
    public static int MAXSORT = 22;
    public static int GREEDYTRIES = 8;

    public static float SAFETYDISTANCE = 3f;
    public static float EMERGENCYSCORE = 25f;

    public static final int COSTCYCLE1 = 135;
    public static final int COSTCYCLE2 = 420;
    public static final int COSTSORT = 110;
    public static final int COSTSELECTION = 33;

    public static int emptyZone = 0;
    public static int busyZone = 1;
    public static int abandonedZone = 2;
    public static int outOfMapZone = 3;

    public static float interaction_dist_from_edge = 1f;

    public static final int BYTECODEATSHOOTING = 7500;

    public static int NUMELEMENTS = 5;

    public static float PRECISION_MAP_BOUNDS = 0.1f;
    public static float SCOUT_RISK_DISTANCE = 6;
    public static int SCOUT_ESCAPE_TURNS = 6;

    public static float PI2 = 2*(float)Math.PI;

    public static int shootTries = 2;
    public static float minAngleShoot = (60.0f*(float)Math.PI)/180.0f;

    public static float triadAngle = (20.0f*(float)Math.PI)/180.0f;
    public static float pentadAngle = (15.0f*(float)Math.PI)/180.0f;
    public static float pentadAngle2 = (30.0f*(float)Math.PI)/180.0f;

    public static int GARDENER = 0;
    public static int LUMBERJACK = 1;
    public static int SOLDIER = 2;
    public static int TANK = 3;
    public static int SCOUT = 4;
    public static int ARCHON = 5;
    public static int TREE = 6;


    //LUMBIS
    static float CONVERSIONBULLETCOST = 12.5f;
    static float BASESCORELUMBERJACK = 0.003f;
    static float ENEMYTREESCORE = 7.5f;
    static float NEUTRALTREESCORE = 5.0f;
    static float CHOPTREESCORE = 3.0f;
    static float RANDOMTREESCORE = 0.005f;
    static float GARDENERSCORELUMBERJACK = 10.0f;
    static float ARCHONSCORELUMBERJACK = 1.0f;
    static int ARCHONVALUE = 1;
    public static int BYTECODEPOSTMESSAGESLUMBERJACK = 5000;
    static int CHANGETARGETLUMBERJACKS = 50;
    static float ADDTODISTANCELUMBERJACK = 5.0f; 


    static float MAXVICTORYPONTS = 1000.0f;

    //gardener 0, lumbi 1, soldier 2, tank 3, scout 4, archon 5
    static int getIndex(RobotType r){
        if (r == RobotType.GARDENER) return 0;
        else if (r == RobotType.LUMBERJACK) return 1;
        else if (r == RobotType.SOLDIER) return 2;
        else if (r == RobotType.TANK) return 3;
        else if (r == RobotType.SCOUT) return 4;
        else if (r == RobotType.ARCHON) return 5;
        return -1;
    }

    public static RobotType getRobotTypeFromIndex(int i){
        switch (i){
            case 0: return RobotType.GARDENER;
            case 1: return RobotType.LUMBERJACK;
            case 2: return RobotType.SOLDIER;
            case 3: return RobotType.TANK;
            case 4: return RobotType.SCOUT;
            case 5: return RobotType.ARCHON;
            default: return null;
        }
    }

    public static float enemyScore(int a){
        if (a == 5) return 0.001f;
        if (a == 4) return 15;
        if (a == 3) return 0.002f;
        if (a == 2) return 12;
        if (a == 1) return 8;
        return 20;

    }

    static float safetyDistance(RobotType r){
        if (r == RobotType.LUMBERJACK) return 2f;
        else if (r == RobotType.SOLDIER) return 3.1f;
        else if (r == RobotType.TANK) return 3.1f;
        else if (r == RobotType.SCOUT) return 0.25f;
        return 0;
    }

    static float getRadius(int a){
        if (a == 5) return 2;
        return ProductionUnits[a].bodyRadius;
    }

    static float costOfVictoryPoints(int round)
    {
        float cost = 12.5f/(2999.0f)*((float)round-1.0f)+7.5f;
        return cost;
    }

}
