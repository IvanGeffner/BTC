package Gardenerplayer;

import battlecode.common.Direction;
import battlecode.common.RobotType;

/**
 * Created by Ivan on 1/15/2017.
 */
public class Constants {

    public static float eps = 0.001f;
    public static float INF = 9999f;
    public static Direction[] main_dirs = {Direction.getEast(), Direction.getNorth(), Direction.getSouth(), Direction.getWest()};

    public static RobotType[] ProductionUnits = {RobotType.GARDENER, RobotType.LUMBERJACK, RobotType.SOLDIER, RobotType.TANK, RobotType.SCOUT};
    public static int[] initialBuild = {0, 4, 5, 5, 4, 5, 5, 2, 1, 0, 5, 5, 5, 2, 1, 5, 5, 2, 1, 5};
    public static int[]  initialPositions = {0, 8, 7, 9999, 1, 2};
    public static int[] sequenceBuild = {2, 5, 5, 2, 5, 0, 2, 5, 1, 5, 2 ,5};
    public static int IBL = initialBuild.length;
    public static int SBL = sequenceBuild.length;

    public static float rotationAngle = 4.0f*(float)Math.PI/13.0f;
    public static float pushTarget = 5.0f;

    public static int UNIT_GARDENER = 0;
    public static int UNIT_LUMBERJACK = 1;
    public static int UNIT_SOLDIER = 2;
    public static int UNIT_TANK = 3;
    public static int UNIT_SCOUT = 4;
    public static int UNIT_TREE = 5;



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
    public static int SAFETYMARGIN = 8000;
    public static int CHANGETARGET = 30;
    public static float NEWTARGET = 2.01f;
    public static float BULLETSIGHT = 6f;
    public static int ANGLEFACTOR = (1 << 13);

    public static float COLLISIONDIST = 0.75f;
    public static float COLLISIONRANGE = 1.2f;
    public static int COLLISIONHASH = 1000;
    public static int COLLISIONROUND = 5;
    public static float MAXBULLETSPEED = 3.0f;
    public static int MAXSORT = 22;

    public static int NUMELEMENTS = 5;

    public static float PI2 = 2*(float)Math.PI;

    public static int shootTries = 2;
    public static float minAngleShoot = (60.0f*(float)Math.PI)/180.0f;

    public static float triadAngle = (20.0f*(float)Math.PI)/180.0f;
    public static float pentadAngle = (15.0f*(float)Math.PI)/180.0f;
    public static float pentadAngle2 = (30.0f*(float)Math.PI)/180.0f;

    static int getIndex(RobotType r){
        if (r == RobotType.GARDENER) return 0;
        else if (r == RobotType.LUMBERJACK) return 1;
        else if (r == RobotType.SOLDIER) return 2;
        else if (r == RobotType.TANK) return 3;
        else if (r == RobotType.SCOUT) return 4;
        else if (r == RobotType.ARCHON) return 5;
        return 0;
    }

    static float safetyDistance(RobotType r){
        if (r == RobotType.LUMBERJACK) return 1f;
        else if (r == RobotType.SOLDIER) return 2.2f;
        return 0;
    }


}
