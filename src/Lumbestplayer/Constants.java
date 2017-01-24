package Lumbestplayer;

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
    public static int[] initialBuild = {0, 4, 5, 5, 4, 2, 1, 5, 5,  5, 5, 0, 5, 2, 1, 5, 5, 2, 1, 5};
    public static int[]  initialPositions = {0, 8, 7, 30, 1, 2};
    public static int[] sequenceBuild = {2, 5, 5, 2, 5, 0, 2, 5, 1, 5, 3 ,5};

    //public static int[] initialBuild = {0,4,1,2,4,2,0};
    //public static int[] initialPositions = {0,2,3,14,1,9999};
    //public static int[] sequenceBuild = {1,2,2,2,3,2,2,3,0};


    public static int IBL = initialBuild.length;
    public static int SBL = sequenceBuild.length;

    public static float rotationAngle = 4.0f*(float)Math.PI/13.0f;
    public static float pushTarget = 5.0f;

    public static int TC = 4; //TreeColumns
    public static int TR = 2; //TRows
    public static int DR = 5; //Distance Row-Row
    public static int ModulR = 2*TR + DR;
    public static int ModulC = 2*TC + DR;
    public static int SouthTree = 4;
    public static int NorthTree = 6;
    public static float minHPWater = 45f;
    public static float minHPGoWater = 25f;
    public static int TREEBUCLEBYTE = 1000;
    public static int BYTECODEPOSTMESSAGES = 9000;
    public static int BYTECODEPOSTMESSAGESLUMBERJACK = 5000;
    public static int SAFETYMARGIN = 8000;
    public static int SAFETYMARGINCHECKTREES = 4000;
    public static int SAFETYMARGINSCOUTS = 9700;
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
    public static float EMERGENCYSCORE = 20f;

    public static final int COSTCYCLE1 = 135;
    public static final int COSTCYCLE2 = 420;
    public static final int COSTSORT = 110;
    public static final int COSTSELECTION = 33;

    public static final int BYTECODEATSHOOTING = 7500;

    public static int NUMELEMENTS = 5;

    public static float PRECISION_MAP_BOUNDS = 0.1f;

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
    public static int TREE = 5;

    //LUMBIS
    static float CONVERSIONBULLETCOST = 25.0f;
    static float BASESCORELUMBERJACK = 0.5f;
    static float ENEMYTREESCORE = 7.5f;
    static float NEUTRALTREESCORE = 5.0f;
    static float GARDENERSCORELUMBERJACK = 10.0f;
    static float ARCHONSCORELUMBERJACK = 1.0f;
    static float ARCHONVALUE = 1000.0f;

    static int CHANGETARGETLUMBERJACKS = 50;


    static int getIndex(RobotType r){
        if (r == RobotType.GARDENER) return 0;
        else if (r == RobotType.LUMBERJACK) return 1;
        else if (r == RobotType.SOLDIER) return 2;
        else if (r == RobotType.TANK) return 3;
        else if (r == RobotType.SCOUT) return 4;
        else if (r == RobotType.ARCHON) return -1;
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

    static float safetyDistance(RobotType r){
        if (r == RobotType.LUMBERJACK) return 2f;
        else if (r == RobotType.SOLDIER) return 2.8f;
        else if (r == RobotType.TANK) return 2.8f;
        //else if (r == RobotType.SCOUT) return 0.25f;
        return 0;
    }

    static float getRadius(int a){
        if (a == 5) return 2;
        return ProductionUnits[a].bodyRadius;
    }


}
